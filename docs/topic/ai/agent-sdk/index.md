---
title: Agent SDK
description: 使用 Anthropic Agent SDK 构建自定义 AI Agent 应用
icon: lucide/bot
---

# Agent SDK

**本文你会学到**：

- Agent SDK 是什么，它与 Claude API、Claude Code CLI 有什么本质区别
- 为什么需要 Agent SDK 而非直接调用 Claude API
- Agent SDK 的完整功能全景
- 与其他 Claude 工具的对比和选型建议
- 推荐的学习路线

## Agent SDK 是什么

想象你在终端里用 Claude Code 修了一个 Bug，你觉得这个能力太好了——能不能把它嵌入到你的 Web 应用、CI 流水线或者内部工具里？Agent SDK 就是干这个的。

**Agent SDK（原名 Claude Code SDK）是 Anthropic 官方提供的 SDK**，让你在 Python 和 TypeScript 中以编程方式使用与 Claude Code 相同的工具、Agent 循环和上下文管理能力。简单来说：Claude Code 是终端里的 Agent，Agent SDK 是代码里的 Agent。

``` python
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions


async def main():
    async for message in query(
        prompt="Find and fix the bug in auth.py",
        options=ClaudeAgentOptions(allowed_tools=["Read", "Edit", "Bash"]),
    ):
        print(message)  # Claude 读取文件、定位 Bug、编辑修复


asyncio.run(main())
```

``` typescript
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "Find and fix the bug in auth.ts",
  options: { allowedTools: ["Read", "Edit", "Bash"] }
})) {
  console.log(message); // Claude 读取文件、定位 Bug、编辑修复
}
```

## 为什么需要 Agent SDK

### 直接用 Claude API 有什么问题

Claude API（即 Messages API）提供的是「模型调用」能力——你发一段文字，模型回一段文字。想让模型「读文件」「跑命令」「改代码」？你得自己实现：

``` python
# 用 Claude API：你需要自己实现工具循环
response = client.messages.create(...)
while response.stop_reason == "tool_use":
    result = your_tool_executor(response.tool_use)  # 你来执行工具
    response = client.messages.create(tool_result=result, **params)
```

``` typescript
// 用 Claude API：你需要自己实现工具循环
let response = await client.messages.create({ ...params });
while (response.stop_reason === "tool_use") {
  const result = yourToolExecutor(response.tool_use); // 你来执行工具
  response = await client.messages.create({ tool_result: result, ...params });
}
```

这意味着你要自己处理工具解析、执行、结果回传、错误重试、上下文管理等一系列问题。

### Agent SDK 怎么解决

Agent SDK 把上述所有工作都封装好了。你只需要描述任务，SDK 内部的 Agent 循环会自动完成「思考 -> 调用工具 -> 观察结果 -> 再思考」的完整流程：

``` python
# 用 Agent SDK：Claude 自己处理工具执行
async for message in query(prompt="Fix the bug in auth.py"):
    print(message)
```

``` typescript
// 用 Agent SDK：Claude 自己处理工具执行
for await (const message of query({ prompt: "Fix the bug in auth.ts" })) {
  console.log(message);
}
```

### 与 Claude Code CLI 的关系

| 用例 | 最佳选择 |
|------|---------|
| 交互式开发、日常编码 | Claude Code CLI |
| 嵌入到自定义应用 | Agent SDK |
| CI/CD 流水线自动化 | Agent SDK |
| 生产环境部署 | Agent SDK |
| 一次性任务 | CLI |

很多团队两者配合使用：CLI 用于日常开发，SDK 用于生产自动化。两者共享 `CLAUDE.md` 配置和 MCP 服务器，工作流可以直接迁移。

## 与其他 Claude 工具的对比

Anthropic 提供了多种使用 Claude 构建的方式，它们的定位各不相同：

| 维度 | Client SDK | Claude Code CLI | Agent SDK | Managed Agents |
|------|-----------|----------------|-----------|---------------|
| **本质** | API 客户端库 | 终端 Agent 工具 | 编程式 Agent 库 | 托管式 Agent API |
| **工具执行** | 你自己实现 | Claude 自动执行 | Claude 自动执行 | 你执行，Claude 触发 |
| **运行位置** | 你的进程 | 你的机器 | 你的进程 | Anthropic 基础设施 |
| **界面** | Python/TS 库 | 终端 CLI | Python/TS 库 | REST API |
| **会话管理** | 你自己管理 | 自动管理 | 自动管理 | Anthropic 托管 |
| **适用场景** | 自定义工具和 Agent | 开发者日常使用 | 嵌入应用、CI/CD | 无需运维的生产 Agent |

### Agent SDK vs Client SDK

Client SDK 提供直接的 API 访问，你需要自己实现工具循环。Agent SDK 提供内置工具执行，Claude 自主处理整个 Agent 循环。

### Agent SDK vs Managed Agents

Managed Agents 是 Anthropic 托管的 REST API，Agent 运行在 Anthropic 的沙箱中。Agent SDK 在你自己的进程内运行 Agent 循环。常见路径是先用 Agent SDK 本地原型验证，再迁移到 Managed Agents 用于生产。

## 功能全景

Agent SDK 包含 Claude Code 的所有核心能力：

### 内置工具

开箱即用，无需自己实现工具执行逻辑：

| 工具 | 功能 |
|------|------|
| `Read` | 读取工作目录中的任何文件 |
| `Write` | 创建新文件 |
| `Edit` | 对现有文件进行精确编辑 |
| `Bash` | 运行终端命令、脚本、git 操作 |
| `Monitor` | 监视后台脚本并对每个输出行做出反应 |
| `Glob` | 按模式查找文件（`**/*.ts`、`src/**/*.py`） |
| `Grep` | 使用正则表达式搜索文件内容 |
| `WebSearch` | 搜索网络获取当前信息 |
| `WebFetch` | 获取并解析网页内容 |
| `AskUserQuestion` | 向用户提出带多选选项的澄清问题 |

### Hooks

在 Agent 生命周期的关键点运行自定义代码，用于验证、日志、拦截或转换 Agent 行为。可用的 Hook 包括 `PreToolUse`、`PostToolUse`、`Stop`、`SessionStart`、`SessionEnd`、`UserPromptSubmit` 等。

### Subagents

生成专门的子 Agent 处理子任务。主 Agent 委派工作，子 Agent 报告结果——适合代码审查、安全扫描等需要专门上下文的场景。

### MCP（Model Context Protocol）

通过 MCP 连接到外部系统：数据库、浏览器、API 等。可以连接 GitHub 上已有的数百个 MCP 服务器，也可以自建。

### 权限控制

精确控制 Agent 可以使用哪些工具。支持多种权限模式：`acceptEdits`（自动批准编辑）、`dontAsk`（只允许预批准的工具）、`bypassPermissions`（沙箱环境全放开）、`default`（自定义审批回调）。

### 会话管理

在多次交互中保持上下文。支持恢复会话、分叉会话探索不同方案，Claude 会记住之前读取的文件和完成的分析。

### 流式输出

通过异步迭代器实时获取 Agent 的工作进度，适合交互式场景。也支持单轮模式，一次性收集所有结果，适合后台任务和 CI 流水线。

### 扩展能力

SDK 还支持 Claude Code 的文件系统配置：

| 功能 | 描述 | 位置 |
|------|------|------|
| Skills | 在 Markdown 中定义的专门能力 | `.claude/skills/*/SKILL.md` |
| Slash Commands | 用于常见任务的自定义命令 | `.claude/commands/*.md` |
| Memory | 项目上下文和说明 | `CLAUDE.md` |
| Plugins | 自定义命令、Agent 和 MCP 服务器 | 通过 `plugins` 选项编程 |

## 支持的语言

- **TypeScript**（Node.js 18+）—— 通过 npm 安装，内置捆绑 Claude Code 二进制文件
- **Python**（3.10+）—— 通过 pip 或 uv 安装

两者 API 设计和功能完全对等，选择你团队最熟悉的语言即可。

## 学习路线

推荐按以下顺序阅读：

**入门**：

- [快速开始](quickstart/) — 十分钟构建你的第一个 Bug 修复 Agent

**核心概念**：

- [Agent 循环](agent-loop/) — 理解 Agent 的思考-行动-观察循环
- [权限控制](tools/) — 控制 Agent 可以做什么、什么时候需要审批
- [会话管理](sessions/) — 构建多轮对话、恢复和分叉会话
- [流式输出](streaming/) — 实时进度 vs 单轮模式

**进阶**：

- [Hooks](extensions/) — 在工具调用前后插入自定义逻辑
- [工具与 MCP](tools/) — 连接外部系统和自定义工具
- [配置与部署](configuration/) — 系统提示、环境变量、生产部署
- [可观测性](observability/) — 日志、监控和调试
- [API 参考](api-reference/) — 完整的 API 文档

**迁移**：

- [迁移指南](migration/) — 从旧版 Claude Code SDK 迁移到 Agent SDK
