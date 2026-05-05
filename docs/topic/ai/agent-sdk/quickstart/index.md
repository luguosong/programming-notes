---
title: 快速开始
description: 十分钟构建你的第一个 Claude Agent
---

# 快速开始

本指南带你用 Agent SDK 构建一个能自主读取代码、发现 Bug 并修复的 AI Agent。整个过程中你不需要手动实现任何工具逻辑——Agent SDK 已经帮你做好了。

## 前置条件

开始之前，确保你已准备好：

- **Node.js 18+** 或 **Python 3.10+**
- 一个 **Anthropic 账户**和 API Key（在 [Claude 控制台](https://platform.claude.com/)获取）

## 安装 SDK

### TypeScript

``` bash
npm install @anthropic-ai/claude-agent-sdk
```

### Python（推荐使用 uv）

``` bash
uv init && uv add claude-agent-sdk
```

### Python（使用 pip）

``` bash
python3 -m venv .venv && source .venv/bin/activate
pip3 install claude-agent-sdk
```

TypeScript SDK 会为你的平台捆绑一个本地 Claude Code 二进制文件作为可选依赖项，无需单独安装 Claude Code。

## 设置 API Key

在项目目录中创建 `.env` 文件：

``` text
ANTTHROPIC_API_KEY=your-api-key
```

SDK 还支持通过第三方 API 提供商进行认证：

- **Amazon Bedrock**：设置 `CLAUDE_CODE_USE_BEDROCK=1` 并配置 AWS 凭证
- **Google Vertex AI**：设置 `CLAUDE_CODE_USE_VERTEX=1` 并配置 Google Cloud 凭证
- **Microsoft Azure**：设置 `CLAUDE_CODE_USE_FOUNDRY=1` 并配置 Azure 凭证

## 准备一个有 Bug 的文件

我们的 Agent 要修复代码中的 Bug，所以先需要一个「靶子」。创建 `utils.py`：

``` python
def calculate_average(numbers):
    total = 0
    for num in numbers:
        total += num
    return total / len(numbers)


def get_user_name(user):
    return user["name"].upper()
```

这段代码有两个 Bug：

- `calculate_average([])` 会因为除以零而崩溃
- `get_user_name(None)` 会因为 `TypeError` 而崩溃

## 构建你的第一个 Agent

### TypeScript 版本

创建 `agent.ts`：

``` typescript
import { query } from "@anthropic-ai/claude-agent-sdk";

// Agent 循环：流式输出 Claude 的工作过程
for await (const message of query({
  prompt: "Review utils.py for bugs that would cause crashes. Fix any issues you find.",
  options: {
    allowedTools: ["Read", "Edit", "Glob"], // 允许 Claude 使用的工具
    permissionMode: "acceptEdits" // 自动批准文件编辑
  }
})) {
  // 过滤出人类可读的输出
  if (message.type === "assistant" && message.message?.content) {
    for (const block of message.message.content) {
      if ("text" in block) {
        console.log(block.text); // Claude 的推理过程
      } else if ("name" in block) {
        console.log(`Tool: ${block.name}`); // 正在调用的工具
      }
    }
  } else if (message.type === "result") {
    console.log(`Done: ${message.subtype}`); // 最终结果
  }
}
```

### Python 版本

创建 `agent.py`：

``` python
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, AssistantMessage, ResultMessage


async def main():
    # Agent 循环：流式输出 Claude 的工作过程
    async for message in query(
        prompt="Review utils.py for bugs that would cause crashes. Fix any issues you find.",
        options=ClaudeAgentOptions(
            allowed_tools=["Read", "Edit", "Glob"],  # 允许 Claude 使用的工具
            permission_mode="acceptEdits",  # 自动批准文件编辑
        ),
    ):
        # 过滤出人类可读的输出
        if isinstance(message, AssistantMessage):
            for block in message.content:
                if hasattr(block, "text"):
                    print(block.text)  # Claude 的推理过程
                elif hasattr(block, "name"):
                    print(f"Tool: {block.name}")  # 正在调用的工具
        elif isinstance(message, ResultMessage):
            print(f"Done: {message.subtype}")  # 最终结果


asyncio.run(main())
```

### 代码解读

这段代码有三个关键部分：

- **`query`**——创建 Agent 循环的主入口。它返回一个异步迭代器，用 `async for` 流式传输 Claude 工作时的消息
- **`prompt`**——告诉 Claude 你想让它做什么。Claude 会根据任务自行决定使用哪些工具
- **`options`**——Agent 的配置。`allowedTools` 预先批准指定工具，`permissionMode: "acceptEdits"` 自动批准文件修改

`async for` 循环在 Claude 思考、调用工具、观察结果、决定下一步的过程中持续运行。每次迭代产生一条消息：Claude 的推理、工具调用、工具结果或最终结果。SDK 处理编排（工具执行、上下文管理、重试），你只需要消费这个流。当 Claude 完成任务或遇到错误时，循环结束。

### 运行 Agent

**TypeScript**：

``` bash
npx tsx agent.ts
```

**Python**：

``` bash
python3 agent.py
```

运行后检查 `utils.py`，你会看到 Claude 自主完成了以下操作：

- **读取** `utils.py` 理解代码逻辑
- **分析** 边界情况，识别出会导致崩溃的 Bug
- **编辑** 文件，添加了处理空列表和空用户的防御性代码

这就是 Agent SDK 的核心价值：Claude 直接执行工具，而不是要求你来实现工具。

## 基本概念

### 工具（Tools）

工具控制 Agent 能做什么。根据你开放的工具组合，Agent 的能力范围也不同：

| 工具组合 | Agent 能力 |
|---------|-----------|
| `Read`、`Glob`、`Grep` | 只读分析（代码审查、搜索） |
| `Read`、`Edit`、`Glob` | 分析 + 修改代码 |
| `Read`、`Edit`、`Bash`、`Glob`、`Grep` | 完全自动化（含运行命令） |

### 权限模式（Permission Modes）

权限模式控制你希望多大程度的人工监督：

| 模式 | 行为 | 适用场景 |
|------|------|---------|
| `acceptEdits` | 自动批准文件编辑和常见文件系统命令，其他操作需确认 | 受信任的开发工作流 |
| `dontAsk` | 拒绝 `allowedTools` 之外的任何请求 | 锁定的无头 Agent |
| `auto`（仅 TypeScript） | 模型分类器自动批准或拒绝每个工具调用 | 带安全防护的自主 Agent |
| `bypassPermissions` | 所有工具直接执行，不弹确认 | 沙箱 CI、完全受信环境 |
| `default` | 需要通过 `canUseTool` 回调处理审批 | 自定义审批流程 |

上面的示例使用了 `acceptEdits` 模式，让 Agent 可以在没有交互式确认的情况下运行。如果你想在运行时让用户确认操作，使用 `default` 模式并提供一个 `canUseTool` 回调。

## 自定义你的 Agent

### 添加网络搜索能力

``` python
options = ClaudeAgentOptions(
    allowed_tools=["Read", "Edit", "Glob", "WebSearch"],
    permission_mode="acceptEdits"
)
```

``` typescript
const options = {
  allowedTools: ["Read", "Edit", "Glob", "WebSearch"],
  permissionMode: "acceptEdits"
};
```

### 自定义系统提示

``` python
options = ClaudeAgentOptions(
    allowed_tools=["Read", "Edit", "Glob"],
    permission_mode="acceptEdits",
    system_prompt="You are a senior Python developer. Always follow PEP 8 style guidelines.",
)
```

``` typescript
const options = {
  allowedTools: ["Read", "Edit", "Glob"],
  permissionMode: "acceptEdits",
  systemPrompt: "You are a senior Python developer. Always follow PEP 8 style guidelines."
};
```

### 启用终端命令执行

``` python
options = ClaudeAgentOptions(
    allowed_tools=["Read", "Edit", "Glob", "Bash"],
    permission_mode="acceptEdits"
)
```

``` typescript
const options = {
  allowedTools: ["Read", "Edit", "Glob", "Bash"],
  permissionMode: "acceptEdits"
};
```

启用 `Bash` 后，你可以让 Agent 做更复杂的事情，比如 `"Write unit tests for utils.py, run them, and fix any failures"`。

## 更多提示词灵感

Agent 构建好之后，试试这些不同的提示：

- `"Add docstrings to all functions in utils.py"`
- `"Add type hints to all functions in utils.py"`
- `"Create a README.md documenting the functions in utils.py"`

## 故障排除

### API 错误：`thinking.type.enabled` 不支持此模型

Claude Opus 4.7 使用 `thinking.type.adaptive` 替换了 `thinking.type.enabled`。如果你选择了 `claude-opus-4-7` 模型但 SDK 版本较旧，会看到这个错误：

``` text
API Error: 400 {"type":"invalid_request_error","message":"\"thinking.type.enabled\" is not supported for this model..."}
```

**解决方法**：升级到 Agent SDK v0.2.111 或更高版本。

### API Key not found

确保你已在 `.env` 文件或 shell 环境中设置了 `ANTHROPIC_API_KEY` 环境变量。

## 下一步

你已经构建了第一个 Agent，接下来可以探索更强大的功能：

- [权限控制](../tools/) — 精确控制 Agent 的能力边界和审批流程
- [Hooks](../extensions/) — 在工具调用前后插入自定义逻辑（日志、审计、拦截）
- [会话管理](../sessions/) — 构建多轮对话，恢复和分叉会话
- [MCP 服务器](../tools/) — 连接数据库、浏览器、API 等外部系统
- [配置与部署](../configuration/) — 系统提示、环境变量、生产环境部署
