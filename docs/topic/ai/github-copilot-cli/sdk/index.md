---
title: SDK 集成
description: 通过 Copilot SDK 将 CLI 能力嵌入到桌面应用和工具中
---

# SDK 集成

> SDK 集成就像把 Copilot 的"大脑"拆成标准零件——你不用自己造 AI，只需把对应模块嵌入产品即可。

!!! note

    Copilot SDK 目前处于 public preview 阶段，功能和可用性可能随时变化。

## 概述

Copilot SDK 通过 JSON-RPC 协议与 Copilot CLI 通信，将 CLI 的核心能力暴露为可编程 API。SDK 提供两种集成模式：

| 模式 | 说明 | 适用场景 |
|------|------|---------|
| Bundled CLI | 将 CLI 二进制文件随应用一起分发 | 桌面应用、Electron、独立 CLI 工具 |
| Local CLI | 使用用户系统上已安装的 CLI | 个人项目、原型验证、本地开发 |

两者共享相同的 API 接口，区别仅在于 CLI 的来源和管理方式。

## Bundled CLI

将 CLI 二进制文件打包到应用中，用户无需额外安装或配置。

!!! tip

    Best for: 桌面应用（Electron、Tauri）、独立分发工具、需要完全控制 CLI 版本的产品。

### 工作原理

SDK 通过 `cliPath` 选项指向应用内打包的 CLI 副本，关键特征：

- CLI 二进制随应用一起分发，用户无需单独安装
- 开发者控制 CLI 的精确版本
- 用户通过应用、环境变量或 BYOK 进行认证
- 会话按用户在本地机器上管理

### 快速开始

```shell
npm install @github/copilot
```

**Node.js / TypeScript**

```typescript
import { CopilotClient } from "@github/copilot-sdk";
import path from "path";

const client = new CopilotClient({
    // 指向应用中打包的 CLI 二进制文件
    cliPath: path.join(__dirname, "vendor", "copilot"),
});

const session = await client.createSession({ model: "gpt-4.1" });
const response = await session.sendAndWait({ prompt: "Hello!" });
console.log(response?.data.content);

await client.stop();
```

**Python**

```python
from copilot import CopilotClient, PermissionHandler
from pathlib import Path

client = CopilotClient({
    "cli_path": str(Path(__file__).parent / "vendor" / "copilot"),
})
await client.start()

session = await client.create_session(
    on_permission_request=PermissionHandler.approve_all,
    model="gpt-4.1"
)
response = await session.send_and_wait({"prompt": "Hello!"})
print(response.data.content)

await client.stop()
```

### 认证策略

Bundled 模式下需要决定用户的认证方式，共三种方案：

**方案 A：使用已登录的 GitHub 凭证（最简单）**

用户在 CLI 中登录一次，打包应用自动复用这些凭证，无需额外代码：

```typescript
const client = new CopilotClient({
    cliPath: path.join(__dirname, "vendor", "copilot"),
    // 默认行为：使用已登录用户凭证
});
```

**方案 B：通过环境变量提供 Token**

```typescript
const client = new CopilotClient({
    cliPath: path.join(__dirname, "vendor", "copilot"),
    env: {
        COPILOT_GITHUB_TOKEN: getUserToken(),
    },
});
```

**方案 C：BYOK（自带模型密钥，无需 GitHub 认证）**

```typescript
const client = new CopilotClient({
    cliPath: path.join(__dirname, "vendor", "copilot"),
});

const session = await client.createSession({
    model: "gpt-4.1",
    provider: {
        type: "openai",
        baseUrl: "https://api.openai.com/v1",
        apiKey: process.env.OPENAI_API_KEY,
    },
});
```

### 会话管理

```typescript
// 创建一个与会话 ID 绑定的命名会话
const sessionId = `project-${projectName}`;
const session = await client.createSession({
    sessionId,
    model: "gpt-4.1",
});

// 在后续运行中恢复会话
const resumed = await client.resumeSession(sessionId);
```

会话状态存储在 `~/.copilot/session-state/SESSION-ID/` 目录下。

### 平台支持

分发多平台版本时，需要为每个目标平台包含对应的二进制文件：

```text
my-app/
├── vendor/
│   ├── copilot-darwin-arm64    # macOS Apple Silicon
│   ├── copilot-darwin-x64      # macOS Intel
│   ├── copilot-linux-x64       # Linux x64
│   └── copilot-win-x64.exe     # Windows x64
└── src/
    └── index.ts
```

运行时根据平台动态选择二进制：

```typescript
import os from "os";

function getCLIPath(): string {
    const platform = process.platform;
    const arch = os.arch();
    const ext = platform === "win32" ? ".exe" : "";
    const name = `copilot-${platform}-${arch}${ext}`;
    return path.join(__dirname, "vendor", name);
}

const client = new CopilotClient({ cliPath: getCLIPath() });
```

### 已知限制

| 限制 | 说明 |
|------|------|
| 打包体积 | CLI 二进制会增加应用的分发大小 |
| 版本更新 | 需要在应用自身的发布周期中管理 CLI 版本 |
| 多平台构建 | 每种操作系统/架构需要单独的二进制文件 |
| 单用户 | 每个 CLI 实例仅服务于一个用户 |

## Local CLI

使用用户系统上已安装的 Copilot CLI——最快的上手方式，无需任何认证代码或基础设施。

!!! tip

    Best for: 个人项目、原型验证、本地开发、学习 SDK。

### 工作原理

当用户安装并登录 Copilot CLI 后，凭证存储在系统 keychain 中。SDK 自动将 CLI 作为子进程启动，复用已存储的凭证：

- SDK 自动启动 CLI，无需任何配置
- 认证使用系统 keychain 中的用户凭证
- 通过 stdio 通信，不开放网络端口
- 会话限定在本机

### 快速开始

无需任何配置选项即可使用：

```typescript
import { CopilotClient } from "@github/copilot-sdk";

const client = new CopilotClient();
const session = await client.createSession({ model: "gpt-4.1" });

const response = await session.sendAndWait({ prompt: "Hello!" });
console.log(response?.data.content);

await client.stop();
```

### 自定义配置

```typescript
const client = new CopilotClient({
    cliPath: "/usr/local/bin/copilot",    // 覆盖 CLI 路径
    logLevel: "debug",                      // 调试日志级别
    cliArgs: ["--log-dir=/tmp/copilot-logs"], // 传递额外 CLI 参数
    cwd: "/path/to/project",               // 设置工作目录
});
```

### 环境变量认证

适合 CI 环境或不想交互式登录的场景，按优先级递减：

```shell
export COPILOT_GITHUB_TOKEN="YOUR-GITHUB-TOKEN"   # 推荐
export GH_TOKEN="YOUR-GITHUB-TOKEN"               # GitHub CLI 兼容
export GITHUB_TOKEN="YOUR-GITHUB-TOKEN"           # GitHub Actions 兼容
```

SDK 自动识别这些环境变量，无需修改代码。

### 已知限制

| 限制 | 说明 |
|------|------|
| 单用户 | 凭证绑定到 CLI 中登录的用户 |
| 仅限本地 | CLI 必须运行在与应用相同的机器上 |
| 不支持多租户 | 无法从一个 CLI 实例服务多个用户 |
| 需要 CLI 登录 | 用户必须先运行 `copilot` 完成认证 |

## SDK 与 CLI 兼容性

SDK 只能访问通过 CLI 的 JSON-RPC 协议暴露的功能。许多交互式 CLI 功能是终端特有的，无法通过编程方式调用。

### 功能对照

SDK 涵盖以下核心能力：

| 类别 | 代表功能 |
|------|---------|
| 会话管理 | `createSession()`、`resumeSession()`、`destroy()`、`listSessions()` |
| 消息收发 | `send()`、`sendAndWait()`、`abort()`、`getMessages()` |
| 工具系统 | `registerTools()`、`onPreToolUse` / `onPostToolUse` hook |
| 模型切换 | `listModels()`、`session.setModel()`、`session.rpc.model.getCurrent()` |
| MCP 服务器 | 本地 stdio 和远程 HTTP/SSE 服务器连接 |
| 事件订阅 | `on()`、`once()`，支持 40+ 事件类型和 streaming |
| 上下文压缩 | `infiniteSessions` 自动压缩、手动 `session.rpc.compaction.compact()` |
| 计划管理 | `session.rpc.plan.read()` / `update()` / `delete()` |

以下功能仅在 CLI 中可用，SDK 未暴露：

| 类别 | 代表功能 |
|------|---------|
| 会话导出 | `--share`、`--share-gist` |
| 交互式 UI | `/help`、`/clear`、`/agent`、`/diff`、`/feedback`、`/model` |
| 权限快捷方式 | `--yolo`、`--allow-all-paths`、`--allow-all-urls` |
| 非交互模式 | `-p`（prompt 模式）、`--continue`、`--silent` |

### 常见替代方案

**权限控制**——用 `onPermissionRequest` 替代 CLI 的 `--yolo`：

```typescript
const session = await client.createSession({
    onPermissionRequest: approveAll,
});
```

**Token 用量追踪**——订阅 usage 事件替代 `/usage`：

```typescript
session.on("assistant.usage", (event) => {
    console.log("Tokens used:", {
        input: event.data.inputTokens,
        output: event.data.outputTokens,
    });
});
```

**上下文压缩**——配置自动压缩或手动触发：

```typescript
// 自动压缩
const session = await client.createSession({
    infiniteSessions: {
        enabled: true,
        backgroundCompactionThreshold: 0.80,
        bufferExhaustionThreshold: 0.95,
    },
});

// 手动压缩（实验性）
const result = await session.rpc.compaction.compact();
```

### 协议版本兼容

| SDK 协议范围 | CLI 协议版本 | 兼容性 |
|-------------|-------------|--------|
| v2–v3 | v3 | 完整支持 |
| v2–v3 | v2 | 通过自动 v2 adapter 支持 |

SDK 在启动时与 CLI 协商协议版本。当连接到 v2 CLI 时，SDK 自动将 `tool.call` 和 `permission.request` 消息适配为 v3 事件模型，无需修改代码。

运行时检查版本：

```typescript
const status = await client.getStatus();
console.log("Protocol version:", status.protocolVersion);
```
