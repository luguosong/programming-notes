---
title: 监控与成本
description: OpenTelemetry 可观测性与多级成本追踪
---

你的 Agent 在生产环境跑了几天，用户反馈有些请求很慢但你不知道瓶颈在哪。你可能还想知道这个月 token 消耗了多少、哪些模型的成本最高。没有可观测性，Agent 就是一个黑盒——你能看到输入和输出，但看不到中间发生了什么。Agent SDK 内置了 OpenTelemetry 集成和成本追踪，让你对 Agent 的每一次运行都有完整的洞察。

**本文你会学到**：

- 如何启用 Agent SDK 的 OpenTelemetry 遥测导出
- 三种遥测信号（Traces、Metrics、Logs）分别包含什么
- 如何读取和关联 Agent 的追踪数据
- 如何在 SDK 响应流中追踪 token 使用和成本
- 处理缓存令牌和多轮对话的成本累积

## 遥测数据是如何流动的

Agent SDK 启动 Claude Code CLI 作为子进程，通过本地管道与之通信。CLI 本身内置了 OpenTelemetry instrumentation——它会在每次模型请求和工具执行周围创建 span，为 token 和成本计数器发送 metrics，为 prompt 和工具结果发送结构化 log 事件。SDK 自身不产生遥测数据，而是将配置透传给 CLI 子进程，由 CLI 直接导出到你的 collector。

遥测配置通过环境变量传递。子进程默认继承宿主应用的环境变量，所以你可以在两个地方配置：

| 配置位置 | 适用场景 | 注意事项 |
|---------|---------|---------|
| 进程环境变量（Shell、容器、编排器） | 生产部署，所有 `query()` 自动生效 | 推荐，零代码改动 |
| `ClaudeAgentOptions.env`（Python）/ `options.env`（TypeScript） | 同一进程中不同 Agent 需要不同配置 | Python 是合并环境变量，TypeScript 是完全替换（需展开 `...process.env`） |

CLI 导出三个独立的 OpenTelemetry 信号，每个信号有自己的开关和导出器：

| 信号 | 包含内容 | 启用方式 |
|------|---------|---------|
| Metrics | token、成本、会话、代码行数、工具决策的计数器 | `OTEL_METRICS_EXPORTER` |
| Log events | 每个 prompt、API 请求、API 错误、工具结果的结构化记录 | `OTEL_LOGS_EXPORTER` |
| Traces | 每次 interaction、模型请求、工具调用、Hook 的 span（beta） | `OTEL_TRACES_EXPORTER` + `CLAUDE_CODE_ENHANCED_TELEMETRY_BETA=1` |

## 启用遥测导出

遥测默认关闭。你需要设置 `CLAUDE_CODE_ENABLE_TELEMETRY=1` 并选择至少一个导出器。最常见的配置是把三种信号都通过 OTLP HTTP 发送到 collector。

```python title="Python：启用全部三种遥测信号"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions

OTEL_ENV = {
    "CLAUDE_CODE_ENABLE_TELEMETRY": "1",
    # Traces 处于 beta，需要额外启用
    "CLAUDE_CODE_ENHANCED_TELEMETRY_BETA": "1",
    # 为每种信号选择导出器，SDK 中使用 otlp
    "OTEL_TRACES_EXPORTER": "otlp",
    "OTEL_METRICS_EXPORTER": "otlp",
    "OTEL_LOGS_EXPORTER": "otlp",
    # 标准 OTLP 传输配置
    "OTEL_EXPORTER_OTLP_PROTOCOL": "http/protobuf",
    "OTEL_EXPORTER_OTLP_ENDPOINT": "http://collector.example.com:4318",
    "OTEL_EXPORTER_OTLP_HEADERS": "Authorization=Bearer your-token",
}


async def main():
    options = ClaudeAgentOptions(env=OTEL_ENV)
    async for message in query(
        prompt="List the files in this directory", options=options
    ):
        print(message)


asyncio.run(main())
```

```typescript title="TypeScript：启用全部三种遥测信号"
import { query } from "@anthropic-ai/claude-agent-sdk";

const otelEnv = {
  CLAUDE_CODE_ENABLE_TELEMETRY: "1",
  // Traces 处于 beta，需要额外启用
  CLAUDE_CODE_ENHANCED_TELEMETRY_BETA: "1",
  OTEL_TRACES_EXPORTER: "otlp",
  OTEL_METRICS_EXPORTER: "otlp",
  OTEL_LOGS_EXPORTER: "otlp",
  OTEL_EXPORTER_OTLP_PROTOCOL: "http/protobuf",
  OTEL_EXPORTER_OTLP_ENDPOINT: "http://collector.example.com:4318",
  OTEL_EXPORTER_OTLP_HEADERS: "Authorization=Bearer your-token",
};

for await (const message of query({
  prompt: "List the files in this directory",
  // TypeScript 中 env 完全替换继承的环境，所以要先展开 process.env
  options: { env: { ...process.env, ...otelEnv } },
})) {
  console.log(message);
}
```

> 不要把 `console` 作为导出器值——SDK 用标准输出作为消息通道，`console` 导出器会把遥测写到同一个通道上，造成冲突。本地调试时，把 `OTEL_EXPORTER_OTLP_ENDPOINT` 指向本地 collector 或 Jaeger 容器即可。

### 短期调用的遥测刷新

CLI 默认按固定间隔批量导出遥测数据：metrics 每 60 秒、traces 和 logs 每 5 秒。如果你的 `query()` 调用很快完成，数据可能还卡在缓冲区里没发出去。虽然进程退出时 CLI 会尝试 flush，但有时间限制，如果 collector 响应慢，数据仍然会丢失。

对于短期任务，缩短导出间隔可以确保数据在任务完成前到达 collector：

```python title="Python：缩短导出间隔"
OTEL_ENV = {
    # ... 前面的导出器配置 ...
    "OTEL_METRIC_EXPORT_INTERVAL": "1000",   # metrics 每 1 秒导出
    "OTEL_LOGS_EXPORT_INTERVAL": "1000",     # logs 每 1 秒导出
    "OTEL_TRACES_EXPORT_INTERVAL": "1000",   # traces 每 1 秒导出
}
```

```typescript title="TypeScript：缩短导出间隔"
const otelEnv = {
  // ... 前面的导出器配置 ...
  OTEL_METRIC_EXPORT_INTERVAL: "1000",
  OTEL_LOGS_EXPORT_INTERVAL: "1000",
  OTEL_TRACES_EXPORT_INTERVAL: "1000",
};
```

## 读取 Agent 追踪数据

Traces（追踪）是三种信号中最详细的。启用 `CLAUDE_CODE_ENHANCED_TELEMETRY_BETA=1` 后，Agent 循环的每一步都变成一个 span：

| Span 名称 | 包含内容 |
|----------|---------|
| `claude_code.interaction` | Agent 循环的一次完整轮次（从接收 prompt 到产生响应） |
| `claude_code.llm_request` | 每次对 Claude API 的调用，附带模型名、延迟和 token 计数 |
| `claude_code.tool` | 每次工具调用，包含子 span：`claude_code.tool.blocked_on_user`（等待权限）和 `claude_code.tool.execution`（执行） |
| `claude_code.hook` | 每次 Hook 执行，需要额外配置 `ENABLE_BETA_TRACING_DETAILED=1` 和 `BETA_TRACING_ENDPOINT` |

`llm_request`、`tool`、`hook` 都是 `claude_code.interaction` 的子 span。当 Agent 通过 Task 工具生成子 Agent 时，子 Agent 的 span 会嵌套在父 Agent 的 `claude_code.tool` span 下，整个委派链呈现为一棵完整的追踪树。

每个 span 默认携带 `session.id` 属性。如果你对同一个 session 进行了多次 `query()` 调用，在后端按 `session.id` 过滤就能看到完整的时间线。设置 `OTEL_METRICS_INCLUDE_SESSION_ID` 为 falsy 值可以隐藏此属性。

> Traces 仍处于 beta 阶段，span 名称和属性可能在版本间变化。

## 关联应用追踪

当你的应用本身也在使用 OpenTelemetry 时，SDK 会自动将 W3C trace context 传播到 CLI 子进程。你在应用中有一个活跃的 span，调用 `query()` 时 SDK 会自动将 `TRACEPARENT` 和 `TRACESTATE` 注入子进程环境变量，CLI 读取这些变量后会让 `claude_code.interaction` span 成为你的 span 的子节点。这样 Agent 运行就出现在你应用的追踪链路中，而不是孤立的根节点。

CLI 还会把 `TRACEPARENT` 转发给它执行的每个 Bash 和 PowerShell 命令。如果命令本身也产生 OpenTelemetry span，这些 span 会嵌套在 `claude_code.tool.execution` span 下。

如果你在 `options.env` 中显式设置了 `TRACEPARENT`，自动注入会被跳过，方便你固定特定的父上下文。

## 标记遥测数据

CLI 默认报告 `service.name` 为 `claude-code`。如果你运行多个 Agent，或者 SDK 和其他服务共用同一个 collector，你需要覆盖 service name 并添加 resource attributes，以便在后端按 Agent 过滤。

```python title="Python：自定义 service name 和 resource attributes"
options = ClaudeAgentOptions(
    env={
        # ... 导出器配置 ...
        "OTEL_SERVICE_NAME": "support-triage-agent",
        "OTEL_RESOURCE_ATTRIBUTES": "service.version=1.4.0,deployment.environment=production",
    },
)
```

```typescript title="TypeScript：自定义 service name 和 resource attributes"
const options = {
  env: {
    ...process.env,
    // ... 导出器配置 ...
    OTEL_SERVICE_NAME: "support-triage-agent",
    OTEL_RESOURCE_ATTRIBUTES:
      "service.version=1.4.0,deployment.environment=production",
  },
};
```

这些值会作为 OpenTelemetry resource attributes 应用到 Agent 发出的每个 span、metric 和 event 上。

## 控制敏感数据导出

默认情况下，遥测数据是结构性的：只记录持续时间、模型名、工具名等元数据。Agent 读写的内容不会被记录。以下可选变量可以开启更详细的数据导出：

| 变量 | 导出内容 |
|------|---------|
| `OTEL_LOG_USER_PROMPTS=1` | prompt 文本（记录在 `claude_code.user_prompt` event 和 `claude_code.interaction` span 上） |
| `OTEL_LOG_TOOL_DETAILS=1` | 工具输入参数（文件路径、shell 命令、搜索模式等） |
| `OTEL_LOG_TOOL_CONTENT=1` | 完整的工具输入和输出 body（span event，截断至 60 KB，需启用 traces） |
| `OTEL_LOG_RAW_API_BODIES` | 完整的 Anthropic Messages API 请求和响应 JSON。设为 `1` 导出截断至 60 KB 的内联 body，设为 `file:<dir>` 将未截断的 body 写入磁盘并在 event 中附带 `body_ref` 路径 |

> 除非你的可观测性管道已被授权存储 Agent 处理的数据，否则不要启用这些选项。`OTEL_LOG_RAW_API_BODIES` 包含完整对话历史和 extended-thinking 内容（已脱敏），启用它意味着接受前三个选项可能暴露的所有内容。

## 成本追踪

遥测适合长期监控，但有时候你只想在代码里直接拿到 token 用量和成本数字，而不需要部署一个 collector。SDK 在响应流中提供了三级粒度的成本数据。

### 理解使用数据的三个层级

| 层级 | 范围 | 说明 |
|------|------|------|
| `query()` 调用 | 一次 `query()` 的完整执行 | 一次调用可能包含多个步骤（Claude 响应→使用工具→获取结果→再次响应） |
| 步骤（Step） | `query()` 内的一个请求/响应周期 | 每步产生带有 `usage` 的助手消息 |
| 会话（Session） | 通过 session ID 关联的多次 `query()` 调用 | 每个 `query()` 独立报告成本，SDK 不提供会话级汇总 |

TypeScript 和 Python SDK 公开相同的数据，只是字段命名不同：

- **TypeScript**：助手消息通过 `message.message.usage` 获取每步 token 细分，结果消息通过 `modelUsage` 获取按模型分类的成本
- **Python**：助手消息通过 `message.usage` 获取每步 token 细分，结果消息通过 `model_usage` 获取按模型分类的成本

> `total_cost_usd` 是客户端估算值，不是权威计费数据。SDK 从内置的价格表在本地计算，当定价变动、SDK 版本较旧或存在特殊计费规则时，可能与实际账单有差异。用于开发洞察和大致预算编制即可，不要用于向最终用户计费。

### 获取单次调用的总成本

结果消息（`ResultMessage`）标志着 Agent 循环的结束，包含 `total_cost_usd` 字段——该调用中所有步骤的累积估算成本：

```python title="Python：获取总成本"
from claude_agent_sdk import query, ResultMessage
import asyncio


async def main():
    async for message in query(prompt="Summarize this project"):
        if isinstance(message, ResultMessage):
            print(f"Total cost: ${message.total_cost_usd or 0}")


asyncio.run(main())
```

```typescript title="TypeScript：获取总成本"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({ prompt: "Summarize this project" })) {
  if (message.type === "result") {
    console.log(`Total cost: $${message.total_cost_usd}`);
  }
}
```

### 按步骤追踪 token 使用

每条助手消息包含 `usage` 对象（input_tokens、output_tokens）。当 Claude 并行使用多个工具时，同一轮的所有消息共享相同的 `id` 和 usage 数据，因此需要按 ID 去重以避免重复计数。

```typescript title="TypeScript：按步骤追踪 token"
import { query } from "@anthropic-ai/claude-agent-sdk";

const seenIds = new Set<string>();
let totalInputTokens = 0;
let totalOutputTokens = 0;

for await (const message of query({ prompt: "Summarize this project" })) {
  if (message.type === "assistant") {
    const msgId = message.message.id;

    // 并行工具调用共享同一个 ID，只计数一次
    if (!seenIds.has(msgId)) {
      seenIds.add(msgId);
      totalInputTokens += message.message.usage.input_tokens;
      totalOutputTokens += message.message.usage.output_tokens;
    }
  }
}

console.log(`Steps: ${seenIds.size}`);
console.log(`Input tokens: ${totalInputTokens}`);
console.log(`Output tokens: ${totalOutputTokens}`);
```

> 并行工具调用会产生多条助手消息，它们共享相同的 `id` 和 `usage`。始终按 ID 去重才能得到准确的每步 token 计数。

### 按模型追踪成本

结果消息的 `modelUsage`（TypeScript）或 `model_usage`（Python）是一个从模型名到对应 token 计数和成本的映射。当你使用多个模型（比如子 Agent 用 Haiku、主 Agent 用 Opus）时，这个字段能帮你看到 token 花在了哪里。

```typescript title="TypeScript：按模型查看成本细分"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({ prompt: "Summarize this project" })) {
  if (message.type !== "result") continue;

  for (const [modelName, usage] of Object.entries(message.modelUsage)) {
    console.log(`${modelName}: $${usage.costUSD.toFixed(4)}`);
    console.log(`  Input tokens: ${usage.inputTokens}`);
    console.log(`  Output tokens: ${usage.outputTokens}`);
    console.log(`  Cache read: ${usage.cacheReadInputTokens}`);
    console.log(`  Cache creation: ${usage.cacheCreationInputTokens}`);
  }
}
```

### 累积多次调用的成本

每个 `query()` 调用返回自己的 `total_cost_usd`。SDK 不提供会话级汇总，所以如果你需要跨多次调用（多轮会话或不同用户）计算总花费，需要自己累加。

```python title="Python：累积多次调用的成本"
from claude_agent_sdk import query, ResultMessage
import asyncio


async def main():
    total_spend = 0.0

    prompts = [
        "Read the files in src/ and summarize the architecture",
        "List all exported functions in src/auth.ts",
    ]

    for prompt in prompts:
        async for message in query(prompt=prompt):
            if isinstance(message, ResultMessage):
                cost = message.total_cost_usd or 0
                total_spend += cost
                print(f"This call: ${cost}")

    print(f"Total spend: ${total_spend:.4f}")


asyncio.run(main())
```

### 处理缓存差异

SDK 自动使用 Prompt Caching 减少重复内容的成本，你不需要自己配置缓存。`usage` 对象中包含两个额外的缓存字段：

- `cache_creation_input_tokens`：用于创建新缓存条目的 token（按比标准输入更高的费率计费）
- `cache_read_input_tokens`：从已有缓存读取的 token（按降低的费率计费）

将这两个字段与 `input_tokens` 分开追踪，可以了解缓存为你节省了多少。

默认情况下缓存 TTL 为 5 分钟。如果你的工作负载在多个短会话之间复用相同的系统提示和上下文，且会话间隔超过 5 分钟，缓存会在会话之间过期，每个新会话都要付完整的输入价格。设置 `ENABLE_PROMPT_CACHING_1H=1` 可以将缓存写入 TTL 延长到 1 小时（1 小时写入的费率高于 5 分钟写入），Claude 订阅用户自动获得 1 小时 TTL。

### 异常情况处理

几个需要注意的边界情况：

- **输出 token 差异**：极少数情况下，相同 ID 的消息 `output_tokens` 值可能不同。使用一组中的最高值，或直接使用结果消息的 `total_cost_usd`
- **失败对话的成本**：成功和错误的结果消息都包含 `usage` 和 `total_cost_usd`。对话中途失败意味着你已经消耗了到失败点为止的 token，始终从结果消息读取成本数据
