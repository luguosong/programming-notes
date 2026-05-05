---
title: Channels
description: 通过 Channels 将外部事件推送到运行中的 Claude Code 会话，实现远程交互和自动化通知
---

# Channels

**本文你会学到**：

- 🎯 Channel 是什么，它和普通 MCP 服务器有什么本质区别
- 🔧 如何安装和配置 Telegram、Discord、iMessage 三种官方 Channel
- 🚀 使用 fakechat 在本地快速体验 Channel 的工作流程
- 🔒 安全机制：发送者允许列表、配对流程和企业级管控
- 📊 Channel 与 Web 会话、Slack 集成、远程控制的功能对比

## ⚙️ 为什么需要 Channel

当你让 Claude Code 跑一个耗时任务（比如跑测试、重构代码）然后离开终端去处理其他事情时，你会发现一个问题：

> CI 构建失败了，但 Claude 不知道——因为它只在**你主动发消息时**才工作。

又或者：

> 同事在 Telegram 里问了一个关于代码的问题，你想让 Claude 帮你回答，但不想一直守在电脑前。

Channel 就是解决这类问题的机制。它本质上是一个**特殊类型的 MCP 服务器**，可以把外部事件（消息、webhook、告警）**推送到你正在运行的 Claude Code 会话中**——不需要你主动发指令，Claude 就能「听到」外面发生了什么。

!!! warning "研究预览功能"

    Channel 目前处于研究预览阶段（v2.1.80 新增），需要 claude.ai 登录，不支持控制台和 API 密钥认证。Team 和 Enterprise 组织需要管理员[显式启用](#企业管理)。

## 🗂️ Channel 的核心概念

### Channel 和普通 MCP 的区别

你可能已经在用 MCP 服务器给 Claude 接入了数据库、Slack 等外部服务。那 Channel 和普通 MCP 有什么不同？

| 维度 | 普通 MCP 服务器 | Channel |
|------|---------------|---------|
| **通信方向** | Claude 主动查询 | 外部事件**推送**到会话 |
| **触发方式** | Claude 在任务中按需调用 | 外部事件到达时自动触发 |
| **会话关系** | 工具调用，无状态关联 | 绑定到**当前运行的会话** |
| **典型用途** | 查数据库、调用 API | 接收消息、webhook、告警 |

💡 简单类比：普通 MCP 是你让实习生「去查一下数据库」——你发起，他执行。Channel 是实习员的手机响了——外部事件主动到达，他需要回应。

### 双向通信

Channel 可以是**双向**的：

1. **入站**：外部消息（Telegram、Discord 等）到达 Claude Code 会话
2. **出站**：Claude 通过同一个 Channel 回复消息

当你通过 Telegram 给 Claude 发消息时，Claude 处理完后会通过 Telegram 回复你——这就是一个完整的聊天桥接。

!!! info "终端中的显示"

    当 Claude 通过 Channel 回复时，终端会显示工具调用和确认信息（如「已发送」），但不会显示回复的文本内容。实际的回复出现在对应的平台（Telegram、Discord 等）上。

### 事件的生命周期

Channel 的事件**只在会话打开时才会到达**。如果你的 Claude Code 会话已经关闭，发送的消息不会排队等待。

对于需要「始终在线」的场景，可以在后台进程或持久终端（如 `tmux`、`screen`）中运行 Claude Code。

## 📱 支持的 Channel

目前官方支持三种 Channel，每种都以插件形式安装，需要 [Bun](https://bun.sh) 运行时。

=== "Telegram"

    **源码**：[claude-plugins-official/telegram](https://github.com/anthropics/claude-plugins-official/tree/main/external_plugins/telegram)

    **配置步骤**：

    **创建机器人**：在 Telegram 中打开 [BotFather](https://t.me/BotFather)，发送 `/newbot` 创建机器人，复制返回的令牌。

    **安装并配置**：安装插件并配置令牌：

    ```text
    /plugin install telegram@claude-plugins-official
    /reload-plugins
    /telegram:configure <token>
    ```

    令牌会保存到 `~/.claude/channels/telegram/.env`。也可以在启动前设置环境变量 `TELEGRAM_BOT_TOKEN`。

    **启动 Channel**：

    ```bash
    claude --channels plugin:telegram@claude-plugins-official
    ```

    **配对账户**：向机器人发送任意消息，机器人会回复配对代码。在 Claude Code 中运行：

    ```text
    /telegram:access pair <code>
    /telegram:access policy allowlist
    ```

=== "Discord"

    **源码**：[claude-plugins-official/discord](https://github.com/anthropics/claude-plugins-official/tree/main/external_plugins/discord)

    **配置步骤**：

    **创建应用与机器人**：在 [Discord 开发者门户](https://discord.com/developers/applications) 创建应用，在**机器人**部分创建并复制令牌。在**特权网关意图**中启用**消息内容意图**。

    **配置权限与邀请**：在 **OAuth2 > URL 生成器** 中选择 `bot` 范围，勾选以下权限：

    - 查看频道、发送消息、在线程中发送消息
    - 读取消息历史记录、附加文件、添加反应

    打开生成的 URL 将机器人添加到服务器。

    **安装并配置**：安装插件并配置令牌：

    ```text
    /plugin install discord@claude-plugins-official
    /reload-plugins
    /discord:configure <token>
    ```

    令牌会保存到 `~/.claude/channels/discord/.env`。也可以设置环境变量 `DISCORD_BOT_TOKEN`。

    **启动 Channel**：

    ```bash
    claude --channels plugin:discord@claude-plugins-official
    ```

    **配对账户**：向机器人发送私信获取配对代码，然后在 Claude Code 中运行：

    ```text
    /discord:access pair <code>
    /discord:access policy allowlist
    ```

=== "iMessage"

    **源码**：[claude-plugins-official/imessage](https://github.com/anthropics/claude-plugins-official/tree/main/external_plugins/imessage)

    iMessage 直接读取 macOS 消息数据库，通过 AppleScript 发送回复。不需要机器人令牌或外部服务，但**仅限 macOS**。

    **配置步骤**：

    **授予磁盘访问权限**：首次读取 `~/Library/Messages/chat.db` 时，macOS 会弹出权限提示，点击**允许**。如果没有弹出，在**系统设置 > 隐私和安全 > 完全磁盘访问**中手动添加终端。

    **安装并启动**：安装插件并启用：

    ```text
    /plugin install imessage@claude-plugins-official
    ```

    ```bash
    claude --channels plugin:imessage@claude-plugins-official
    ```

    **添加联系人**：给自己发短信即可开始使用（自聊天绕过访问控制）。如果需要允许其他联系人，添加他们的句柄：

    ```text
    /imessage:access allow +15551234567
    ```

    句柄支持 `+国家代码` 格式的电话号码或 Apple ID 邮箱。

## 🚀 快速体验：fakechat

在连接真实平台之前，可以用 fakechat 在本地体验 Channel 的完整工作流程。fakechat 在 `localhost` 上运行一个简单的聊天 UI，无需任何外部服务配置。

### 前提条件

- Claude Code 已安装并使用 claude.ai 账户认证
- [Bun](https://bun.sh) 已安装（用 `bun --version` 检查）
- Team/Enterprise 用户需要管理员[启用 Channel](#企业管理)

### 操作步骤

**安装插件**：安装 fakechat 插件：

```text
/plugin install fakechat@claude-plugins-official
```

!!! tip "市场找不到插件？"

    运行 `/plugin marketplace update claude-plugins-official` 刷新市场，或 `/plugin marketplace add anthropics/claude-plugins-official` 添加官方市场后重试。

**启动 Claude Code**：使用 `--channels` 标志启动 Claude Code：

```bash
claude --channels plugin:fakechat@claude-plugins-official
```

多个 Channel 用空格分隔：`claude --channels plugin:telegram@... plugin:discord@...`

**发送消息**：打开 [http://localhost:8787](http://localhost:8787)，输入消息：

```text
hey, what's in my working directory?
```

消息以 `<channel source="fakechat">` 事件到达 Claude Code 会话，Claude 处理后通过 `reply` 工具回复，答案显示在聊天 UI 中。

!!! warning "无人值守时的权限提示"

    如果 Claude 在你离开终端时遇到权限提示，会话会暂停直到你响应。支持[权限中继](#权限中继)的 Channel 可以远程转发这些提示。对于完全自动化的场景，`--dangerously-skip-permissions` 会绕过提示，但只在可信环境中使用。

## 🔒 安全机制

### 发送者允许列表

每个 Channel 插件维护一个**发送者允许列表**——只有你添加的 ID 才能推送消息，其他人全部静默丢弃。

Telegram 和 Discord 通过**配对流程**引导：

1. 在平台中向机器人发送任意消息
2. 机器人回复一个**配对代码**
3. 在 Claude Code 中批准配对代码
4. 你的发送者 ID 被添加到允许列表

iMessage 的方式不同：给自己发短信自动绕过门禁，通过 `/imessage:access allow` 添加其他联系人。

### 权限中继

支持权限中继的 Channel 可以将 Claude Code 的权限提示转发到外部平台（如 Telegram），让你远程批准或拒绝工具使用。这意味着**允许列表中的发送者实际上拥有了批准或拒绝你会话中工具使用的权限**，因此只添加你信任的发送者。

### 多层防护

Channel 的安全由三个层面控制：

| 层面 | 控制方式 | 说明 |
|------|---------|------|
| **安装** | `--channels` 标志 | 仅在启动时指定的 Channel 才会注册 |
| **配置** | `.mcp.json` 不够 | 即使配置了 MCP 服务器，也必须在 `--channels` 中命名才能推送消息 |
| **发送者** | 允许列表 | 只有经过配对的 ID 才能发送消息 |

## 🏢 企业管理

Team 和 Enterprise 计划上，Channel 默认关闭。管理员通过两个[托管设置](../configuration/settings-permissions/index.md)控制：

| 设置 | 作用 | 未配置时 |
|------|------|---------|
| `channelsEnabled` | 主开关，必须为 `true` 才能让任何 Channel 传递消息 | Channel 被阻止 |
| `allowedChannelPlugins` | 启用后哪些插件可以注册，设置后替换 Anthropic 默认列表 | 应用默认列表 |

Pro 和 Max 用户不受这些限制，直接使用 `--channels` 即可。

### 启用 Channel

管理员通过 [claude.ai 管理员设置](https://claude.ai/admin-settings/claude-code) 启用，或在托管设置中设置：

```json
{
  "channelsEnabled": true
}
```

### 限制允许的 Channel 插件

默认情况下，Anthropic 维护的允许列表上的插件都可以注册。管理员可以通过 `allowedChannelPlugins` 替换为自定义列表：

``` json title="托管设置"
{
  "channelsEnabled": true,
  "allowedChannelPlugins": [
    { "marketplace": "claude-plugins-official", "plugin": "telegram" },
    { "marketplace": "claude-plugins-official", "plugin": "discord" },
    { "marketplace": "acme-corp-plugins", "plugin": "internal-alerts" }
  ]
}
```

设置 `allowedChannelPlugins` 后，只有列出的插件可以注册。空数组会阻止所有 Channel 插件。要完全阻止（包括开发标志），保持 `channelsEnabled` 未设置即可。

## 📊 Channel 与其他集成方式的对比

Claude Code 提供了多种连接外部系统的能力，每种适合不同的场景：

| 功能 | 工作方式 | 适合场景 |
|------|---------|---------|
| **Channel** | 外部事件**推送到已运行的本地会话** | 聊天桥接、webhook 接收、远程交互 |
| [Web 上的 Claude Code](../platforms/index.md) | 在新云沙箱中运行任务 | 自包含的异步工作，稍后检查结果 |
| [Slack 集成](../integrations/index.md) | 从 `@Claude` 生成新的 Web 会话 | 从团队对话上下文启动任务 |
| 标准 [MCP 服务器](../mcp/index.md) | Claude 按需查询，无事件推送 | 给 Claude 按需访问外部系统 |
| 远程控制 | 从 claude.ai 或手机驱动本地会话 | 离开工位时指导进行中的会话 |

Channel 填补了这些方式中的空白——它把来自非 Claude 源的事件推送到你**已经运行**的本地会话中，而不是创建新会话。

### 两个典型用法

**聊天桥接**：通过 Telegram、Discord 或 iMessage 从手机向 Claude 提问，答案在同一聊天中返回，而工作在你的机器上针对真实文件运行。

```text
# 在 Telegram 中发送
刚才跑的测试有 3 个失败了，帮我看看是什么原因

# Claude Code 在你的机器上检查测试日志、定位失败原因
# 回复通过 Telegram 返回到你的手机
```

**Webhook 接收器**：CI、错误跟踪器、部署管道的 webhook 到达 Claude 时，Claude 已经打开着你的文件并记得你正在调试的内容——比新开一个会话高效得多。

```text
# GitHub Actions 构建失败后触发 webhook
# Claude Code 收到事件，自动分析构建日志并定位失败步骤

# Sentry 捕获到新的生产错误后触发 webhook
# Claude Code 收到事件，结合当前打开的代码上下文分析根因
```
