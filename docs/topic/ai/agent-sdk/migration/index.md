---
title: 迁移指南
description: 从旧版 SDK 迁移到 Agent SDK 的完整指南
---

## 包名变更

Claude Code SDK 已更名为 **Claude Agent SDK**，以反映其构建各类 AI Agent（不仅限于编码任务）的能力。

| 维度 | 旧版 | 新版 |
|:---|:---|:---|
| **TypeScript 包名** | `@anthropic-ai/claude-code` | `@anthropic-ai/claude-agent-sdk` |
| **Python 包名** | `claude-code-sdk` | `claude-agent-sdk` |
| **Python 类型名** | `ClaudeCodeOptions` | `ClaudeAgentOptions` |

---

## TypeScript 迁移步骤

**卸载旧包，安装新包：**

``` bash
npm uninstall @anthropic-ai/claude-code
npm install @anthropic-ai/claude-agent-sdk
```

**更新所有 import 路径：**

``` typescript
// 旧
import { query, tool, createSdkMcpServer } from "@anthropic-ai/claude-code";

// 新
import { query, tool, createSdkMcpServer } from "@anthropic-ai/claude-agent-sdk";
```

**更新 `package.json` 依赖：**

``` json
{
  "dependencies": {
    "@anthropic-ai/claude-agent-sdk": "^0.2.0"
  }
}
```

除 import 路径和包名外，无需其他代码变更。

---

## Python 迁移步骤

**卸载旧包，安装新包：**

``` bash
pip uninstall claude-code-sdk
pip install claude-agent-sdk
```

**更新 import 路径和类型名：**

``` python
# 旧
from claude_code_sdk import query, ClaudeCodeOptions

# 新
from claude_agent_sdk import query, ClaudeAgentOptions
```

**更新类型使用：**

``` python
# 旧
options = ClaudeCodeOptions(model="claude-opus-4-7", permission_mode="acceptEdits")

# 新
options = ClaudeAgentOptions(model="claude-opus-4-7", permission_mode="acceptEdits")
```

---

## Breaking Changes

### 系统提示不再默认使用 Claude Code 的预设

SDK 不再默认使用 Claude Code 的系统提示，改用最小提示。

``` typescript
// 恢复旧行为：显式请求 Claude Code 预设
const result = query({
  prompt: "Hello",
  options: {
    systemPrompt: { type: "preset", preset: "claude_code" }
  }
});

// 或使用自定义系统提示
const result = query({
  prompt: "Hello",
  options: {
    systemPrompt: "You are a helpful coding assistant"
  }
});
```

``` python
# 恢复旧行为
async for message in query(
    prompt="Hello",
    options=ClaudeAgentOptions(
        system_prompt={"type": "preset", "preset": "claude_code"}
    ),
):
    print(message)
```

**原因：** 让 SDK 应用具有自定义行为，而不继承 Claude Code CLI 的指令。

### 文件系统设置不再默认加载

SDK 不再自动读取 `CLAUDE.md`、`settings.json`、slash commands 等文件系统设置。

``` typescript
// 恢复旧行为：显式指定设置来源
const result = query({
  prompt: "Hello",
  options: {
    settingSources: ["user", "project", "local"]
  }
});

// 仅加载项目设置
const result = query({
  prompt: "Hello",
  options: {
    settingSources: ["project"]
  }
});
```

``` python
# 恢复旧行为
async for message in query(
    prompt="Hello",
    options=ClaudeAgentOptions(setting_sources=["user", "project", "local"]),
):
    print(message)
```

**注意：** 当前版本的 `query()` 已恢复默认行为（省略 `settingSources` 会加载所有设置）。如需隔离行为，传递 `settingSources: []`。Python SDK 0.1.59 及更早版本中空列表等同于省略，需升级后方可依赖 `setting_sources=[]`。

**原因：** 确保 SDK 应用行为可预测，不受本地文件系统配置影响，对 CI/CD、部署应用、测试和多租户系统尤为重要。

### Python 类型重命名

`ClaudeCodeOptions` 重命名为 `ClaudeAgentOptions`，仅影响类型名和 import 路径，字段名不变。

---

## TypeScript V2 接口（预览版）

V2 接口简化了多轮对话模式，将发送和流式传输分为独立的 `send()` / `stream()` 周期，无需管理异步生成器状态。

> **注意：** V2 是不稳定预览版，API 可能随反馈变更。会话分叉等部分功能仅在 V1 中可用。

### 核心 API

``` typescript
import { unstable_v2_createSession, unstable_v2_resumeSession, unstable_v2_prompt } from "@anthropic-ai/claude-agent-sdk";
```

| 函数 | 说明 |
|:---|:---|
| `unstable_v2_prompt(prompt, options)` | 单轮查询便捷函数，返回 `Promise<SDKResultMessage>` |
| `unstable_v2_createSession(options)` | 创建新会话，返回 `SDKSession` |
| `unstable_v2_resumeSession(sessionId, options)` | 按会话 ID 恢复已有会话 |

### SDKSession 接口

``` typescript
interface SDKSession {
  readonly sessionId: string;
  send(message: string | SDKUserMessage): Promise<void>;
  stream(): AsyncGenerator<SDKMessage, void>;
  close(): void;
}
```

### 多轮对话示例

``` typescript
await using session = unstable_v2_createSession({ model: "claude-opus-4-7" });

// 第一轮
await session.send("What is 5 + 3?");
for await (const msg of session.stream()) {
  if (msg.type === "assistant") { /* 处理响应 */ }
}

// 第二轮（Claude 记住上下文）
await session.send("Multiply that by 2");
for await (const msg of session.stream()) {
  if (msg.type === "assistant") { /* 处理响应 */ }
}
```

### V1 与 V2 对比

| 维度 | V1（稳定） | V2（预览） |
|:---|:---|:---|
| 多轮对话 | `streamInput()` 异步生成器 + yield 协调 | `send()` / `stream()` 独立调用 |
| 单轮查询 | `query()` + `for await` | `unstable_v2_prompt()` 返回 Promise |
| 会话恢复 | `options.resume` | `unstable_v2_resumeSession()` |
| 会话分叉 | `options.forkSession` | 不支持 |
| 资源管理 | `query.close()` | `await using` 自动清理或 `close()` |
