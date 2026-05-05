---
title: 流式与结构化输出
description: 实时流式响应与 JSON Schema 结构化输出
---

当你的 Agent 需要几十秒甚至几分钟才能完成任务时，用户盯着空白屏幕等待是很糟糕的体验。Agent SDK 提供了两种解决方案：**流式输出**让你实时显示 Agent 的思考过程和工具调用，**结构化输出**让你获取类型安全的 JSON 数据而非自由文本。

**本文你会学到**：

- 流式输入模式与单次输入模式的区别和选型建议
- 如何启用流式输出并构建实时 UI
- 如何用 JSON Schema、Zod、Pydantic 获取结构化输出
- 错误处理与已知限制

## 流式输入 vs 单次输入

Agent SDK 提供两种向 Agent 发送消息的方式，它们的能力差异很大：

### 对比一览

| 维度 | 流式输入模式（推荐） | 单次输入模式 |
|------|---------------------|-------------|
| **本质** | 持久化交互会话 | 一次性查询 |
| **多轮对话** | 原生支持，上下文自然保持 | 需手动管理 `continue_conversation` |
| **图片上传** | 支持直接附加图片 | 不支持 |
| **消息排队** | 可按序发送多条，支持中断 | 仅一条 |
| **Hooks** | 完整支持 | 不支持 |
| **实时中断** | 支持 | 不支持 |
| **适用场景** | 交互式应用、Web 聊天、需要工具调用 | Lambda 函数、一次性批处理 |

### 为什么推荐流式输入

流式输入模式让 Agent 作为长期运行的进程工作，接收多轮用户输入、处理图片、执行工具调用、响应中断。它还支持 Hooks、消息排队和实时反馈——这些都是单次输入模式做不到的。

单次输入模式适用于无状态的一次性任务（比如 Lambda 函数），但功能受限。当你不需要图片上传、Hooks 或实时中断时，才考虑使用它。

### 流式输入示例

下面的例子展示了流式输入的核心模式——用异步生成器逐步 `yield` 消息：

```python title="流式输入模式"
import asyncio
import base64
from claude_agent_sdk import ClaudeSDKClient, ClaudeAgentOptions, AssistantMessage, TextBlock


async def streaming_analysis():
    async def message_generator():
        # 第一条消息
        yield {
            "type": "user",
            "message": {
                "role": "user",
                "content": "Analyze this codebase for security issues",
            },
        }

        # 等待条件或用户输入
        await asyncio.sleep(2)

        # 第二条消息：附带图片
        with open("diagram.png", "rb") as f:
            image_data = base64.b64encode(f.read()).decode()

        yield {
            "type": "user",
            "message": {
                "role": "user",
                "content": [
                    {"type": "text", "text": "Review this architecture diagram"},
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": "image/png",
                            "data": image_data,
                        },
                    },
                ],
            },
        }

    options = ClaudeAgentOptions(max_turns=10, allowed_tools=["Read", "Grep"])

    async with ClaudeSDKClient(options) as client:
        await client.query(message_generator())
        async for message in client.receive_response():
            if isinstance(message, AssistantMessage):
                for block in message.content:
                    if isinstance(block, TextBlock):
                        print(block.text)


asyncio.run(streaming_analysis())
```

```typescript title="流式输入模式"
import { query } from "@anthropic-ai/claude-agent-sdk";
import { readFile } from "fs/promises";

async function* generateMessages() {
  // 第一条消息
  yield {
    type: "user" as const,
    message: {
      role: "user" as const,
      content: "Analyze this codebase for security issues"
    }
  };

  // 等待条件
  await new Promise((resolve) => setTimeout(resolve, 2000));

  // 第二条消息：附带图片
  yield {
    type: "user" as const,
    message: {
      role: "user" as const,
      content: [
        { type: "text", text: "Review this architecture diagram" },
        {
          type: "image",
          source: {
            type: "base64",
            media_type: "image/png",
            data: await readFile("diagram.png", "base64")
          }
        }
      ]
    }
  };
}

for await (const message of query({
  prompt: generateMessages(),
  options: {
    maxTurns: 10,
    allowedTools: ["Read", "Grep"]
  }
})) {
  if (message.type === "result") {
    console.log(message.result);
  }
}
```

### 单次输入示例

单次输入更简单，但不支持图片、Hooks 等高级功能：

```python title="单次输入模式"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage


async def single_message_example():
    # 一次性查询
    async for message in query(
        prompt="Explain the authentication flow",
        options=ClaudeAgentOptions(max_turns=1, allowed_tools=["Read", "Grep"]),
    ):
        if isinstance(message, ResultMessage):
            print(message.result)

    # 继续对话（需要手动管理）
    async for message in query(
        prompt="Now explain the authorization process",
        options=ClaudeAgentOptions(continue_conversation=True, max_turns=1),
    ):
        if isinstance(message, ResultMessage):
            print(message.result)


asyncio.run(single_message_example())
```

## 启用流式输出

默认情况下，Agent SDK 在 Claude 完成整段回复后才产出 `AssistantMessage`。如果你想实时看到文本和工具调用，需要启用**部分消息流式传输**（partial message streaming）。

在选项中设置 `include_partial_messages`（Python）或 `includePartialMessages`（TypeScript）为 `true`，SDK 会在正常的 `AssistantMessage` 和 `ResultMessage` 之外，额外产出 `StreamEvent` 消息，包含原始 API 事件流。

启用后，你的代码需要：

1. 检查每条消息的类型，区分 `StreamEvent` 和其他消息类型
2. 对于 `StreamEvent`，提取 `event` 字段并检查其 `type`
3. 查找 `content_block_delta` 事件中 `delta.type` 为 `text_delta` 的内容——这就是实际的文本片段

```python title="启用流式输出"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions
from claude_agent_sdk.types import StreamEvent


async def stream_response():
    options = ClaudeAgentOptions(
        include_partial_messages=True,
        allowed_tools=["Bash", "Read"],
    )

    async for message in query(prompt="List the files in my project", options=options):
        if isinstance(message, StreamEvent):
            event = message.event
            if event.get("type") == "content_block_delta":
                delta = event.get("delta", {})
                if delta.get("type") == "text_delta":
                    print(delta.get("text", ""), end="", flush=True)


asyncio.run(stream_response())
```

```typescript title="启用流式输出"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "List the files in my project",
  options: {
    includePartialMessages: true,
    allowedTools: ["Bash", "Read"]
  }
})) {
  if (message.type === "stream_event") {
    const event = message.event;
    if (event.type === "content_block_delta") {
      if (event.delta.type === "text_delta") {
        process.stdout.write(event.delta.text);
      }
    }
  }
}
```

### StreamEvent 结构

`StreamEvent` 包装的是原始 Claude API 流式事件，不是累积文本——你需要自己拼接。

| 事件类型 | 说明 |
|----------|------|
| `message_start` | 新消息开始 |
| `content_block_start` | 新内容块开始（文本或工具调用） |
| `content_block_delta` | 内容增量更新 |
| `content_block_stop` | 内容块结束 |
| `message_delta` | 消息级别更新（停止原因、用量统计） |
| `message_stop` | 消息结束 |

### 消息流时序

启用流式后，消息的产出顺序如下：

```text
StreamEvent (message_start)
StreamEvent (content_block_start) — 文本块
StreamEvent (content_block_delta) — 文本片段...
StreamEvent (content_block_stop)
StreamEvent (content_block_start) — 工具调用块
StreamEvent (content_block_delta) — 工具输入片段...
StreamEvent (content_block_stop)
StreamEvent (message_delta)
StreamEvent (message_stop)
AssistantMessage — 完整消息
... 工具执行 ...
ResultMessage — 最终结果
```

## 流式文本响应

要实时显示文本，查找 `content_block_delta` 事件中 `delta.type` 为 `text_delta` 的内容，每个事件包含一个文本片段：

```python title="流式显示文本"
import asyncio
import sys
from claude_agent_sdk import query, ClaudeAgentOptions
from claude_agent_sdk.types import StreamEvent


async def stream_text():
    options = ClaudeAgentOptions(include_partial_messages=True)

    async for message in query(prompt="Explain how databases work", options=options):
        if isinstance(message, StreamEvent):
            event = message.event
            if event.get("type") == "content_block_delta":
                delta = event.get("delta", {})
                if delta.get("type") == "text_delta":
                    # 实时打印每个文本片段
                    sys.stdout.write(delta.get("text", ""))
                    sys.stdout.flush()

    print()  # 最终换行


asyncio.run(stream_text())
```

```typescript title="流式显示文本"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "Explain how databases work",
  options: { includePartialMessages: true }
})) {
  if (message.type === "stream_event") {
    const event = message.event;
    if (event.type === "content_block_delta" && event.delta.type === "text_delta") {
      process.stdout.write(event.delta.text);
    }
  }
}
console.log();
```

## 流式工具调用

工具调用也是流式传输的。你可以跟踪工具何时开始、输入参数如何逐步生成、以及何时完成。需要监听三种事件类型：

- `content_block_start`：工具开始调用
- `content_block_delta`（`input_json_delta`）：工具输入 JSON 片段逐块到达
- `content_block_stop`：工具调用完成

```python title="流式跟踪工具调用"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions
from claude_agent_sdk.types import StreamEvent


async def stream_tool_calls():
    options = ClaudeAgentOptions(
        include_partial_messages=True,
        allowed_tools=["Read", "Bash"],
    )

    # 跟踪当前工具和累积的输入 JSON
    current_tool = None
    tool_input = ""

    async for message in query(prompt="Read the README.md file", options=options):
        if isinstance(message, StreamEvent):
            event = message.event
            event_type = event.get("type")

            if event_type == "content_block_start":
                # 新工具调用开始
                content_block = event.get("content_block", {})
                if content_block.get("type") == "tool_use":
                    current_tool = content_block.get("name")
                    tool_input = ""
                    print(f"Starting tool: {current_tool}")

            elif event_type == "content_block_delta":
                delta = event.get("delta", {})
                if delta.get("type") == "input_json_delta":
                    # 累积 JSON 输入片段
                    chunk = delta.get("partial_json", "")
                    tool_input += chunk
                    print(f"  Input chunk: {chunk}")

            elif event_type == "content_block_stop":
                # 工具调用完成——显示最终输入
                if current_tool:
                    print(f"Tool {current_tool} called with: {tool_input}")
                    current_tool = None


asyncio.run(stream_tool_calls())
```

```typescript title="流式跟踪工具调用"
import { query } from "@anthropic-ai/claude-agent-sdk";

// 跟踪当前工具和累积的输入 JSON
let currentTool: string | null = null;
let toolInput = "";

for await (const message of query({
  prompt: "Read the README.md file",
  options: {
    includePartialMessages: true,
    allowedTools: ["Read", "Bash"]
  }
})) {
  if (message.type === "stream_event") {
    const event = message.event;

    if (event.type === "content_block_start") {
      if (event.content_block.type === "tool_use") {
        currentTool = event.content_block.name;
        toolInput = "";
        console.log(`Starting tool: ${currentTool}`);
      }
    } else if (event.type === "content_block_delta") {
      if (event.delta.type === "input_json_delta") {
        const chunk = event.delta.partial_json;
        toolInput += chunk;
        console.log(`  Input chunk: ${chunk}`);
      }
    } else if (event.type === "content_block_stop") {
      if (currentTool) {
        console.log(`Tool ${currentTool} called with: ${toolInput}`);
        currentTool = null;
      }
    }
  }
}
```

## 构建流式 UI

把文本流式和工具流式结合起来，就能构建一个完整的聊天界面。核心思路是维护一个 `in_tool` 标志——当工具正在执行时显示状态指示器（如 `[Using Read...]`），文本只在非工具执行时流式显示：

```python title="流式 UI 完整示例"
import asyncio
import sys
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage
from claude_agent_sdk.types import StreamEvent


async def streaming_ui():
    options = ClaudeAgentOptions(
        include_partial_messages=True,
        allowed_tools=["Read", "Bash", "Grep"],
    )

    # 跟踪是否正在执行工具
    in_tool = False

    async for message in query(
        prompt="Find all TODO comments in the codebase", options=options
    ):
        if isinstance(message, StreamEvent):
            event = message.event
            event_type = event.get("type")

            if event_type == "content_block_start":
                content_block = event.get("content_block", {})
                if content_block.get("type") == "tool_use":
                    # 工具调用开始——显示状态指示器
                    tool_name = content_block.get("name")
                    print(f"\n[Using {tool_name}...]", end="", flush=True)
                    in_tool = True

            elif event_type == "content_block_delta":
                delta = event.get("delta", {})
                # 只在非工具执行时流式显示文本
                if delta.get("type") == "text_delta" and not in_tool:
                    sys.stdout.write(delta.get("text", ""))
                    sys.stdout.flush()

            elif event_type == "content_block_stop":
                if in_tool:
                    # 工具调用完成
                    print(" done", flush=True)
                    in_tool = False

        elif isinstance(message, ResultMessage):
            # Agent 完成所有工作
            print(f"\n\n--- Complete ---")


asyncio.run(streaming_ui())
```

```typescript title="流式 UI 完整示例"
import { query } from "@anthropic-ai/claude-agent-sdk";

// 跟踪是否正在执行工具
let inTool = false;

for await (const message of query({
  prompt: "Find all TODO comments in the codebase",
  options: {
    includePartialMessages: true,
    allowedTools: ["Read", "Bash", "Grep"]
  }
})) {
  if (message.type === "stream_event") {
    const event = message.event;

    if (event.type === "content_block_start") {
      if (event.content_block.type === "tool_use") {
        // 工具调用开始——显示状态指示器
        process.stdout.write(`\n[Using ${event.content_block.name}...]`);
        inTool = true;
      }
    } else if (event.type === "content_block_delta") {
      // 只在非工具执行时流式显示文本
      if (event.delta.type === "text_delta" && !inTool) {
        process.stdout.write(event.delta.text);
      }
    } else if (event.type === "content_block_stop") {
      if (inTool) {
        // 工具调用完成
        console.log(" done");
        inTool = false;
      }
    }
  } else if (message.type === "result") {
    // Agent 完成所有工作
    console.log("\n\n--- Complete ---");
  }
}
```

## 结构化输出

Agent 默认返回自由文本，这适合聊天场景，但当你需要把输出传给程序、数据库或 UI 组件时，自由文本就不够用了——你得自己解析。结构化输出让你定义期望的 JSON 结构，Agent 完成所有工具调用后，SDK 返回经过验证的结构化数据。

### 为什么需要结构化输出

想象一个菜谱应用：Agent 在网上搜索菜谱并返回结果。没有结构化输出，你拿到的是自由文本——需要自己解析标题、把"15 分钟"转成数字、分离配料和步骤。有了结构化输出，你定义好 schema，直接拿到类型安全的数据对象。

### 快速上手

定义一个 JSON Schema 描述你想要的数据结构，通过 `output_format`（Python）或 `outputFormat`（TypeScript）选项传给 `query()`。Agent 完成后，`ResultMessage` 中的 `structured_output` 字段包含验证后的数据：

```python title="结构化输出快速上手"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage


schema = {
    "type": "object",
    "properties": {
        "company_name": {"type": "string"},
        "founded_year": {"type": "number"},
        "headquarters": {"type": "string"},
    },
    "required": ["company_name"],
}


async def main():
    async for message in query(
        prompt="Research Anthropic and provide key company information",
        options=ClaudeAgentOptions(
            output_format={"type": "json_schema", "schema": schema}
        ),
    ):
        if isinstance(message, ResultMessage) and message.structured_output:
            print(message.structured_output)
            # {'company_name': 'Anthropic', 'founded_year': 2021, 'headquarters': 'San Francisco, CA'}


asyncio.run(main())
```

```typescript title="结构化输出快速上手"
import { query } from "@anthropic-ai/claude-agent-sdk";

const schema = {
  type: "object",
  properties: {
    company_name: { type: "string" },
    founded_year: { type: "number" },
    headquarters: { type: "string" }
  },
  required: ["company_name"]
};

for await (const message of query({
  prompt: "Research Anthropic and provide key company information",
  options: {
    outputFormat: {
      type: "json_schema",
      schema: schema
    }
  }
})) {
  if (message.type === "result" && message.subtype === "success" && message.structured_output) {
    console.log(message.structured_output);
    // { company_name: "Anthropic", founded_year: 2021, headquarters: "San Francisco, CA" }
  }
}
```

### 用 Zod 和 Pydantic 实现类型安全

手写 JSON Schema 容易出错且缺乏类型推断。推荐使用 Zod（TypeScript）或 Pydantic（Python）——它们自动生成 JSON Schema，并提供完整的类型安全和运行时验证：

```python title="使用 Pydantic 定义结构化输出"
import asyncio
from pydantic import BaseModel
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage


class Step(BaseModel):
    step_number: int
    description: str
    estimated_complexity: str  # 'low', 'medium', 'high'


class FeaturePlan(BaseModel):
    feature_name: str
    summary: str
    steps: list[Step]
    risks: list[str]


async def main():
    async for message in query(
        prompt="Plan how to add dark mode support to a React app. Break it into implementation steps.",
        options=ClaudeAgentOptions(
            output_format={
                "type": "json_schema",
                "schema": FeaturePlan.model_json_schema(),
            }
        ),
    ):
        if isinstance(message, ResultMessage) and message.structured_output:
            # 验证并获取完全类型化的结果
            plan = FeaturePlan.model_validate(message.structured_output)
            print(f"Feature: {plan.feature_name}")
            print(f"Summary: {plan.summary}")
            for step in plan.steps:
                print(
                    f"{step.step_number}. [{step.estimated_complexity}] {step.description}"
                )


asyncio.run(main())
```

```typescript title="使用 Zod 定义结构化输出"
import { z } from "zod";
import { query } from "@anthropic-ai/claude-agent-sdk";

// 用 Zod 定义 schema
const FeaturePlan = z.object({
  feature_name: z.string(),
  summary: z.string(),
  steps: z.array(
    z.object({
      step_number: z.number(),
      description: z.string(),
      estimated_complexity: z.enum(["low", "medium", "high"])
    })
  ),
  risks: z.array(z.string())
});

type FeaturePlan = z.infer<typeof FeaturePlan>;

// 转换为 JSON Schema
const schema = z.toJSONSchema(FeaturePlan);

for await (const message of query({
  prompt:
    "Plan how to add dark mode support to a React app. Break it into implementation steps.",
  options: {
    outputFormat: {
      type: "json_schema",
      schema: schema
    }
  }
})) {
  if (message.type === "result" && message.subtype === "success" && message.structured_output) {
    // 验证并获取完全类型化的结果
    const parsed = FeaturePlan.safeParse(message.structured_output);
    if (parsed.success) {
      const plan: FeaturePlan = parsed.data;
      console.log(`Feature: ${plan.feature_name}`);
      console.log(`Summary: ${plan.summary}`);
      plan.steps.forEach((step) => {
        console.log(`${step.step_number}. [${step.estimated_complexity}] ${step.description}`);
      });
    }
  }
}
```

使用 Zod/Pydantic 的好处：

- 完整的类型推断（TypeScript）和类型提示（Python）
- 运行时验证，使用 `safeParse()` 或 `model_validate()`
- 更好的错误信息
- Schema 可组合、可复用

### 多步工具调用与结构化输出

结构化输出的强大之处在于：Agent 可以自主使用任意工具完成复杂任务，最终仍返回符合 schema 的 JSON。下面的示例让 Agent 搜索 TODO 注释并用 `git blame` 查找作者——它自主决定使用哪些工具，最后把结果整理成结构化数据：

```python title="TODO 追踪 Agent"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage


todo_schema = {
    "type": "object",
    "properties": {
        "todos": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "text": {"type": "string"},
                    "file": {"type": "string"},
                    "line": {"type": "number"},
                    "author": {"type": "string"},
                    "date": {"type": "string"},
                },
                "required": ["text", "file", "line"],
            },
        },
        "total_count": {"type": "number"},
    },
    "required": ["todos", "total_count"],
}


async def main():
    # Agent 自主使用 Grep 搜索 TODO，使用 Bash 执行 git blame
    async for message in query(
        prompt="Find all TODO comments in this codebase and identify who added them",
        options=ClaudeAgentOptions(
            output_format={"type": "json_schema", "schema": todo_schema}
        ),
    ):
        if isinstance(message, ResultMessage) and message.structured_output:
            data = message.structured_output
            print(f"Found {data['total_count']} TODOs")
            for todo in data["todos"]:
                print(f"{todo['file']}:{todo['line']} - {todo['text']}")
                if "author" in todo:
                    print(f"  Added by {todo['author']} on {todo['date']}")


asyncio.run(main())
```

注意 schema 中 `author` 和 `date` 是可选字段——因为某些文件的 git blame 信息可能不可用。Agent 会填写能找到的信息，缺失的则自动省略。

## 错误处理

结构化输出可能失败——当 Agent 无法生成符合 schema 的 JSON 时（比如 schema 太复杂、任务太模糊、或重试次数耗尽），`ResultMessage` 的 `subtype` 会指示错误：

| subtype | 含义 |
|---------|------|
| `success` | 输出生成并验证成功 |
| `error_max_structured_output_retries` | Agent 多次尝试后仍无法产出合法输出 |

```python title="结构化输出错误处理"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage


async def main():
    async for message in query(
        prompt="Extract contact info from the document",
        options=ClaudeAgentOptions(
            output_format={"type": "json_schema", "schema": contact_schema}
        ),
    ):
        if isinstance(message, ResultMessage):
            if message.subtype == "success" and message.structured_output:
                # 使用验证后的输出
                print(message.structured_output)
            elif message.subtype == "error_max_structured_output_retries":
                # 处理失败——用更简单的 prompt 重试、回退到非结构化输出等
                print("Could not produce valid output")


asyncio.run(main())
```

```typescript title="结构化输出错误处理"
for await (const msg of query({
  prompt: "Extract contact info from the document",
  options: {
    outputFormat: {
      type: "json_schema",
      schema: contactSchema
    }
  }
})) {
  if (msg.type === "result") {
    if (msg.subtype === "success" && msg.structured_output) {
      // 使用验证后的输出
      console.log(msg.structured_output);
    } else if (msg.subtype === "error_max_structured_output_retries") {
      // 处理失败——用更简单的 prompt 重试、回退到非结构化输出等
      console.error("Could not produce valid output");
    }
  }
}
```

避免错误的几个建议：

- **保持 schema 聚焦**——深层嵌套、大量必填字段的 schema 更难满足。从简单开始，按需增加复杂度
- **匹配 schema 和任务**——如果任务可能缺少某些信息，把对应字段设为可选
- **使用清晰的 prompt**——模糊的 prompt 让 Agent 难以判断该输出什么

## 已知限制

一些 SDK 功能与流式不兼容：

| 功能 | 影响 |
|------|------|
| **Extended Thinking** | 当你显式设置 `maxThinkingTokens` / `max_thinking_tokens` 时，不会产出 `StreamEvent`，只能收到完整的 turn 后消息。Thinking 默认关闭，所以通常不影响 |
| **结构化输出** | JSON 结果只出现在最终的 `ResultMessage.structured_output` 中，不会作为流式 delta 产出 |
