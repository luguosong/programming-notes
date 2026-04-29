---
title: MCP
description: 通过 MCP（Model Context Protocol）将 Claude Code 连接到外部服务和工具
---

# MCP

**本文你会学到**：

- 🎯 MCP 是什么，为什么要用它
- 💡 MCP 的三种传输方式（HTTP、SSE、stdio）及各自适用场景
- 🔧 如何在项目级和用户级配置 MCP 服务器
- 📡 MCP 工具的发现机制和上下文成本
- 🧩 常见 MCP 服务器实战示例（GitHub、数据库、Sentry 等）
- ⚠️ 排查 MCP 连接问题的思路

## 🌐 什么是 MCP

MCP 全称 **Model Context Protocol**（模型上下文协议），是 Anthropic 推出的一个开源标准，用于让 AI 模型与外部工具、数据库、API 之间进行标准化通信。

打个比方：假设 Claude Code 是一个能写代码的实习生，他的工位上只有一台连着代码仓库的电脑。没有 MCP 时，这个实习生只能看代码、改代码——想查数据库？没办法。想看 JIRA 工单？做不到。想发 Slack 消息？也办不到。

MCP 就像是给这个实习生**配了一台能连外网的外部电脑**，上面装了各种客户端工具。通过标准化的协议，实习生可以查数据库、读工单、看监控、发消息——而这一切只需要你用自然语言下一句指令。

在这个比喻中：
- **MCP 服务器**（MCP Server）= 外部电脑上运行的某个工具客户端（比如 GitHub 客户端、数据库客户端）
- **MCP 客户端**（MCP Client）= Claude Code 自身，它负责与这些工具通信
- **传输协议** = 实习生和外部电脑之间的通信方式（本地进程通信或网络 HTTP）

## 🤔 为什么需要 MCP

没有 MCP 的时候，Claude Code 只能操作本地的文件系统和终端命令。但真实的开发流程远不止于此——你需要：

- 🔗 **连接外部服务**：在 JIRA 中读取需求、在 GitHub 上创建 PR、在 Sentry 中查看报错
- 🗄️ **查询数据库**：直接从 PostgreSQL 中获取用户数据，而不是手动导出
- 🔄 **自动化工作流**：根据 Figma 设计更新代码模板，根据数据库查询结果自动起草邮件
- 📡 **响应外部事件**：MCP 服务器还能作为通道，把 CI 构建结果、监控告警等外部消息推送到你的会话中

⚠️ 注意：MCP 是一个**开放协议标准**，不是 Claude Code 的私有功能。其他 AI 工具也可以接入 MCP 生态。

## ⚙️ 配置 MCP 服务器

配置 MCP 服务器有三种传输方式，适用于不同场景：

| 传输方式 | 适用场景 | 配置方式 |
|----------|---------|---------|
| **HTTP** | 云端远程服务（推荐） | `--transport http` |
| **SSE** | 传统远程服务 | `--transport sse` |
| **stdio** | 本地进程、需要直接系统访问 | `--transport stdio` |

### 远程 HTTP 服务器

HTTP 是连接远程 MCP 服务的**推荐方式**，几乎所有云端服务都支持这种传输方式。

``` bash title="基本语法"
claude mcp add --transport http <name> <url>
```

``` bash title="连接 Notion"
claude mcp add --transport http notion https://mcp.notion.com/mcp
```

如果服务需要认证，可以通过 `--header` 添加请求头：

``` bash title="带 Bearer Token 认证"
claude mcp add --transport http secure-api https://api.example.com/mcp \
  --header "Authorization: Bearer your-token"
```

### 远程 SSE 服务器

SSE（Server-Sent Events）是一种较老的远程传输方式，部分服务仍在使用：

``` bash title="连接 Asana"
claude mcp add --transport sse asana https://mcp.asana.com/sse
```

### 本地 stdio 服务器

stdio 服务器作为本地进程运行，适合需要直接访问系统资源的工具。使用 `--` 分隔服务器命令：

``` bash title="基本语法"
claude mcp add --transport stdio <name> -- <command> [args...]
```

``` bash title="连接 Airtable"
claude mcp add --transport stdio --env AIRTABLE_API_KEY=YOUR_KEY airtable \
  -- npx -y airtable-mcp-server
```

💡 `--env` 参数用于向服务器传递环境变量（如 API Key），这些变量只在服务器进程中可见，不会泄露到 Claude Code 本身。

## 📂 配置的作用域

MCP 服务器可以配置在三个不同的作用域级别，决定谁能使用、在哪能用：

### 项目级配置（`.mcp.json`）

项目级配置存储在项目根目录的 `.mcp.json` 文件中，**可以提交到版本库**，确保团队成员使用相同的 MCP 工具。

``` bash title="添加项目级 MCP 服务器"
claude mcp add --transport http github --scope project https://api.githubcopilot.com/mcp/
```

执行后会自动生成或更新 `.mcp.json` 文件：

``` json title=".mcp.json"
{
  "mcpServers": {
    "github": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/"
    }
  }
}
```

⚠️ 出于安全考虑，Claude Code 在首次使用项目级 MCP 服务器时会**提示你确认**。如果需要重置这些批准选项，运行 `claude mcp reset-project-choices`。

`.mcp.json` 支持**环境变量展开**，方便团队共享配置但各自设置敏感信息：

``` json title="使用环境变量展开"
{
  "mcpServers": {
    "api-server": {
      "type": "http",
      "url": "${API_BASE_URL:-https://api.example.com}/mcp",
      "headers": {
        "Authorization": "Bearer ${API_KEY}"
      }
    }
  }
}
```

- `${VAR}` — 展开环境变量 `VAR` 的值
- `${VAR:-default}` — 如果 `VAR` 未设置，则使用 `default` 作为默认值

### 本地配置（默认）

不指定 `--scope` 时，默认为本地配置，存储在 `~/.claude.json` 中。**只有你自己能用，仅限当前项目目录生效**。

``` bash title="本地配置（默认）"
claude mcp add --transport http stripe https://mcp.stripe.com

# 显式指定本地作用域
claude mcp add --transport http stripe --scope local https://mcp.stripe.com
```

适合场景：个人开发服务器、实验性配置、包含敏感凭证的服务器。

### 用户级配置

用户级配置同样存储在 `~/.claude.json` 中，但**跨项目生效**——你在机器上的任何项目目录中都能使用。

``` bash title="用户级配置"
claude mcp add --transport http hubspot --scope user https://mcp.hubspot.com/anthropic
```

适合场景：个人常用工具、跨项目通用的开发工具。

### 配置优先级

当同名的 MCP 服务器在多个作用域中存在时，按以下优先级解析冲突：

| 优先级 | 作用域 | 说明 |
|--------|--------|------|
| 1（最高） | 本地（local） | 个人覆盖 |
| 2 | 项目（project） | 团队共享 |
| 3（最低） | 用户（user） | 全局默认 |

换句话说：个人配置可以覆盖团队配置，团队配置可以覆盖全局配置。如果同名服务器同时存在于本地配置和 Claude.ai 连接器中，**本地配置优先**。

## 🛠️ 管理 MCP 服务器

配置好 MCP 服务器后，可以通过以下命令进行管理：

```bash
# 查看所有已配置的服务器
claude mcp list

# 查看某个服务器的详细信息
claude mcp get github

# 移除某个服务器
claude mcp remove github
```

在 Claude Code 会话中，你也可以使用 `/mcp` 命令查看所有服务器的连接状态。

## 🔍 MCP 工具的发现与调用

### Tool Search：按需加载，节省上下文

你可能会担心：连接了很多 MCP 服务器，会不会撑爆上下文窗口？

Claude Code 采用了 **Tool Search**（工具搜索）机制来解决这个问题：

1. 会话启动时，**只加载 MCP 工具的名称**，不加载完整定义
2. 当 Claude 执行某个任务需要用到外部工具时，通过搜索发现相关的工具定义
3. **只有 Claude 实际调用的工具**才会进入上下文

这意味着你可以放心连接多个 MCP 服务器，对上下文的影响微乎其微。

💡 如果你在开发 MCP 服务器，建议编写清晰的**服务器说明**（server instructions），告诉 Claude 你的工具擅长什么、什么时候该搜索它们——这和 Skills 的工作方式类似。

### 动态工具更新

MCP 服务器支持 `list_changed` 通知（v2.1.0 新增）。当服务器更新了可用的工具、提示或资源时，Claude Code 会**自动刷新**，无需你断开重连。

!!! bug "v2.1.113 关键修复"

    - **并发调用超时正确隔离**：之前一个工具调用的消息可能默默撤销另一个调用的看门狗，导致后者无限挂起；现在每个调用的超时独立计算
    - **`ToolSearch` 排名修正**：粘贴的 MCP 工具名现在会返回**实际工具**，而非描述匹配的"兄弟"工具——这意味着复制工具名再粘贴的工作流终于稳定了

### `alwaysLoad` 选项：跳过工具搜索延迟

当你配置了多个 MCP 服务器时，某些关键工具可能因为 Tool Search 的延迟加载机制而无法立即可用。v2.1.121 新增的 `alwaysLoad` 选项解决了这个问题——设为 `true` 时，该服务器的所有工具**跳过工具搜索延迟，始终可用**：

``` json title=".mcp.json"
{
  "mcpServers": {
    "critical-db": {
      "type": "http",
      "url": "https://mcp.example.com/mcp",
      "alwaysLoad": true
    }
  }
}
```

适合需要 Claude 立即可用的核心工具（如数据库查询、CI 状态检查），而非让 Claude 在需要时才搜索发现它们。

### MCP 服务器启动自动重试

当你启动 Claude Code 时，MCP 服务器连接偶尔会因为瞬态错误（网络抖动、服务冷启动慢）而失败。v2.1.121 起，瞬态错误时会**自动重试最多 3 次**，而非直接标记为断开——这大幅减少了启动时 MCP 服务器不可用的情况。

子代理和 SDK 的 MCP 服务器连接也做了优化：重新配置时**并行连接**（v2.1.119 改进），而非逐个串行等待，启动速度更快。

## 💰 MCP 的上下文成本

虽然 Tool Search 大幅降低了上下文消耗，但 MCP 工具的**输出内容**仍然会占用上下文。Claude Code 内置了保护机制：

| 参数 | 说明 |
|------|------|
| 警告阈值 | 单个 MCP 工具输出超过 10,000 tokens 时显示警告 |
| 默认上限 | 25,000 tokens（可通过环境变量调整） |
| 硬性上限 | 500,000 字符（单个工具返回结果的最大值） |

如果某个工具经常返回大量数据（比如查询数据库、生成报告），可以调高上限：

``` bash title="调整 MCP 输出上限"
export MAX_MCP_OUTPUT_TOKENS=50000
claude
```

⚠️ 超过上限的输出会被持久化到磁盘，在对话中替换为文件引用，不会直接撑爆上下文。

## 🔐 认证方式

许多云端 MCP 服务器需要认证。Claude Code 支持 OAuth 2.0 标准流程：

### 自动 OAuth 认证

大多数 MCP 服务器支持 **Dynamic Client Registration**（动态客户端注册），Claude Code 会自动完成 OAuth 流程——你只需要在浏览器中确认授权即可。对于不支持动态注册的 MCP 服务器（如 Slack），可以使用 `--client-id` 和 `--client-secret` 参数传入预配置的 OAuth 客户端凭证（v2.1.30 新增）。MCP 工具描述和服务器指令的单条上限为 2KB（v2.1.84 改进）。

如果服务器要求固定的回调端口，可以使用 `--callback-port` 指定：

``` bash title="指定 OAuth 回调端口"
claude mcp add --transport http \
  --callback-port 8080 \
  my-server https://mcp.example.com/mcp
```

!!! tip "OAuth 稳定性改进（v2.1.118）"

    v2.1.118 对 MCP OAuth 进行了大量稳定性修复：

    - **Token 缺少 `expires_in` 时的处理**：之前 OAuth token 响应如果缺少 `expires_in` 字段，每小时就需要重新认证；现在能正确处理这种情况
    - **跨进程锁**：修复了 MCP OAuth token 刷新在竞争条件下可能导致的 token 覆盖问题
    - **`insufficient_scope` 403 智能处理**：当服务器返回 403 但 token 已有对应 scope 时，会静默刷新而非反复提示重新授权
    - **超时/取消安全处理**：OAuth 流超时或取消时不再出现未处理的 promise rejection

### 自定义认证方式

如果 MCP 服务器使用非 OAuth 的认证方式（如 Kerberos、内部 SSO、短期 token），可以通过 `headersHelper` 指定一个命令来动态生成请求头：

``` json title=".mcp.json 中使用 headersHelper"
{
  "mcpServers": {
    "internal-api": {
      "type": "http",
      "url": "https://mcp.internal.example.com",
      "headersHelper": "/opt/bin/get-mcp-auth-headers.sh"
    }
  }
}
```

⚠️ `headersHelper` 命令每次连接时执行（会话启动和重连），没有缓存，且超时时间为 10 秒。命令必须向标准输出写入一个 JSON 对象。

## 📦 常见 MCP 服务器示例

### 连接 GitHub

``` bash title="添加 GitHub MCP 服务器"
claude mcp add --transport http github https://api.githubcopilot.com/mcp/
```

连接后，你可以直接用自然语言操作 GitHub：

```
Review PR #456 并提出改进建议
为刚发现的 bug 创建一个 Issue
查看分配给我的所有开放 PR
```

### 查询 PostgreSQL 数据库

``` bash title="添加数据库 MCP 服务器"
claude mcp add --transport stdio db -- npx -y @bytebase/dbhub \
  --dsn "postgresql://readonly:password@localhost:5432/analytics"
```

连接后可以直接用自然语言查询数据库：

```
我们这个月的总收入是多少？
显示 orders 表的 schema
找出 90 天没有下单的客户
```

### 监控 Sentry 错误

``` bash title="添加 Sentry MCP 服务器"
claude mcp add --transport http sentry https://mcp.sentry.dev/mcp
```

连接后可以快速排查线上问题：

```
过去 24 小时最常见的错误是什么？
显示错误 ID abc123 的堆栈跟踪
是哪次部署引入了这些新错误？
```

### Claude Code 自身作为 MCP 服务器

Claude Code 也可以**反过来作为 MCP 服务器**，供其他应用连接：

``` bash title="启动 Claude Code 作为 MCP 服务器"
claude mcp serve
```

你可以在 Claude Desktop 中通过以下配置来使用：

``` json title="claude_desktop_config.json"
{
  "mcpServers": {
    "claude-code": {
      "type": "stdio",
      "command": "claude",
      "args": ["mcp", "serve"],
      "env": {}
    }
  }
}
```

## 🔧 排查 MCP 连接问题

当 MCP 服务器连接出现问题时，按以下步骤排查：

1. **检查服务器状态**：在 Claude Code 会话中运行 `/mcp` 查看连接状态
2. **检查配置**：运行 `claude mcp get <name>` 确认 URL、命令、环境变量是否正确
3. **网络连通性**：HTTP/SSE 服务器需要确认目标 URL 可达
4. **认证问题**：检查 OAuth 授权是否过期，API Key 是否有效
5. **环境变量**：如果使用了 `${VAR}` 展开语法，确认对应变量已设置且有默认值
6. **代理设置**：如果通过代理访问，确认 `ANTHROPIC_BASE_URL` 配置正确（非官方代理可能导致 Tool Search 失效）

!!! tip "导入已有配置"

    如果你之前在 Claude Desktop 中已经配置了 MCP 服务器，可以直接导入到 Claude Code，无需重新配置。同样，在 Claude.ai 中配置的 MCP 服务器也会自动同步到 Claude Code。

📝 小结：MCP 是 Claude Code 扩展能力的核心机制。通过配置 MCP 服务器，你可以让 Claude Code 连接到几乎所有外部服务——从代码托管平台到数据库、从监控系统到设计工具。关键是选对传输方式（HTTP 优先）、配对作用域（团队用 project，个人用 local），并注意上下文成本的管控。
