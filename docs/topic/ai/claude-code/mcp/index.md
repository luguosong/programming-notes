---
title: MCP
description: 通过 MCP（Model Context Protocol）将 Claude Code 连接到外部服务和工具
---

# MCP

**本文你会学到**：

- 🎯 MCP 是什么，为什么要用它
- 💡 MCP 的三种传输方式（HTTP、SSE、stdio）及各自适用场景
- 🔧 如何配置 MCP 服务器（命令行、JSON、从 Claude Desktop 导入）
- 📂 配置的三级作用域（本地、项目、用户）与五级优先级
- 🔐 OAuth 2.0 认证流程，以及自定义认证（`headersHelper`）
- 📡 Tool Search 按需加载机制与上下文成本管控
- 📋 MCP 资源引用（`@` 语法）、提示命令（`/mcp__` 语法）、Elicitation 交互
- 🏢 企业级托管配置（`managed-mcp.json`、allow/deny 策略）
- 🧩 常见 MCP 服务器实战示例（GitHub、数据库、Sentry 等）

## 🌐 什么是 MCP

MCP 全称 **Model Context Protocol**（模型上下文协议），是一个开源标准，用于让 AI 模型与外部工具、数据库、API 之间进行标准化通信。

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
- 📡 **响应外部事件**：MCP 服务器还能作为通道（Channel），把 CI 构建结果、监控告警等外部消息推送到你的会话中

⚠️ 注意：MCP 是一个**开放协议标准**，不是 Claude Code 的私有功能。其他 AI 工具也可以接入 MCP 生态。使用第三方 MCP 服务器需自担风险——Anthropic 未验证所有服务器的安全性和正确性，尤其是可能获取不受信任内容的服务器，面临提示注入风险。

## ⚙️ 配置 MCP 服务器

配置 MCP 服务器有三种传输方式，适用于不同场景：

| 传输方式 | 适用场景 | 配置方式 |
|----------|---------|---------|
| **HTTP** | 云端远程服务（推荐） | `--transport http` |
| **SSE** | 传统远程服务（已弃用） | `--transport sse` |
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

SSE（Server-Sent Events）传输已弃用，请在可用时使用 HTTP 服务器。

``` bash title="连接 Asana（SSE）"
claude mcp add --transport sse asana https://mcp.asana.com/sse
```

### 本地 stdio 服务器

stdio 服务器作为本地进程运行，适合需要直接访问系统资源的工具。使用 `--` 分隔 Claude 的选项与传递给 MCP 服务器的命令：

``` bash title="基本语法"
claude mcp add [options] <name> -- <command> [args...]
```

``` bash title="连接 Airtable"
claude mcp add --transport stdio --env AIRTABLE_API_KEY=YOUR_KEY airtable \
  -- npx -y airtable-mcp-server
```

💡 **选项顺序**：所有选项（`--transport`、`--env`、`--scope`、`--header`）必须在服务器名称**之前**。`--`（双破折号）将服务器名称与传递给 MCP 服务器的命令和参数分开。

### 从 JSON 配置添加

如果你有现成的 JSON 配置，可以直接导入：

``` bash title="从 JSON 添加 MCP 服务器"
# HTTP 服务器
claude mcp add-json weather-api '{"type":"http","url":"https://api.weather.com/mcp","headers":{"Authorization":"Bearer token"}}'

# stdio 服务器
claude mcp add-json local-weather '{"type":"stdio","command":"/path/to/weather-cli","args":["--api-key","abc123"],"env":{"CACHE_DIR":"/tmp"}}'

# 带 OAuth 凭据的 HTTP 服务器
claude mcp add-json my-server '{"type":"http","url":"https://mcp.example.com/mcp","oauth":{"clientId":"your-client-id","callbackPort":8080}}' --client-secret
```

### 从 Claude Desktop 导入

如果你已在 Claude Desktop 中配置了 MCP 服务器，可以一键导入：

``` bash title="从 Claude Desktop 导入"
claude mcp add-from-claude-desktop
```

运行后会显示交互式对话框，让你选择要导入的服务器。使用 `--scope user` 可将服务器添加到用户级配置。此功能仅支持 macOS 和 WSL。

### 管理 MCP 服务器

``` bash
# 查看所有已配置的服务器
claude mcp list

# 查看某个服务器的详细信息
claude mcp get github

# 移除某个服务器
claude mcp remove github
```

在 Claude Code 会话中，使用 `/mcp` 查看所有服务器的连接状态、进行 OAuth 认证或清除身份验证。

## 📂 配置的作用域

MCP 服务器可以配置在三个不同的作用域级别，决定谁能使用、在哪能用：

| 范围 | 加载位置 | 与团队共享 | 存储位置 |
|------|---------|-----------|---------|
| **本地**（默认） | 仅当前项目 | 否 | `~/.claude.json` |
| **项目** | 仅当前项目 | 是，通过版本控制 | 项目根目录 `.mcp.json` |
| **用户** | 你的所有项目 | 否 | `~/.claude.json` |

### 本地配置（默认）

不指定 `--scope` 时，默认为本地配置。**只有你自己能用，仅限当前项目目录生效**。

``` bash title="本地配置（默认）"
claude mcp add --transport http stripe https://mcp.stripe.com

# 显式指定本地作用域
claude mcp add --transport http stripe --scope local https://mcp.stripe.com
```

适合场景：个人开发服务器、实验性配置、包含敏感凭证的服务器。

!!! warning "术语区分"

    MCP 的"本地范围"存储在 `~/.claude.json`（主目录），而一般本地设置使用 `.claude/settings.local.json`（项目目录）。两者位置不同。

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

支持 `${VAR}`（展开变量值）和 `${VAR:-default}`（未设置时使用默认值）两种语法。可展开的位置包括 `command`、`args`、`env`、`url`、`headers`。

### 用户级配置

用户级配置**跨项目生效**——你在机器上的任何项目目录中都能使用。

``` bash title="用户级配置"
claude mcp add --transport http hubspot --scope user https://mcp.hubspot.com/anthropic
```

适合场景：个人常用工具、跨项目通用的开发工具。

### 配置优先级

当同名 MCP 服务器在多个位置存在时，Claude Code 连接一次，使用**最高优先级**的定义：

| 优先级 | 来源 | 说明 |
|--------|------|------|
| 1（最高） | 本地（local） | 个人覆盖 |
| 2 | 项目（project） | 团队共享 |
| 3 | 用户（user） | 全局默认 |
| 4 | 插件提供的 MCP 服务器 | 随插件启用 |
| 5（最低） | claude.ai 连接器 | 从 Claude.ai 同步 |

前三个范围按**名称**匹配重复项。插件和连接器按**端点**匹配（相同的 URL 或命令视为重复）。

## 🔐 认证方式

许多云端 MCP 服务器需要认证。Claude Code 支持多种认证方式。

### OAuth 2.0 自动认证

大多数 MCP 服务器支持 **Dynamic Client Registration**（动态客户端注册），Claude Code 会自动完成 OAuth 流程：

1. 添加需要认证的服务器：`claude mcp add --transport http sentry https://mcp.sentry.dev/mcp`
2. 在 Claude Code 中运行 `/mcp`，按照浏览器中的步骤登录

💡 认证令牌安全存储并自动刷新。使用 `/mcp` 菜单中的"清除身份验证"可撤销访问权限。如果浏览器重定向后出现连接错误，将浏览器地址栏中的完整回调 URL 粘贴到 Claude Code 中即可。

#### 指定回调端口

某些 MCP 服务器需要预先注册的特定重定向 URI。使用 `--callback-port` 固定端口：

``` bash title="指定 OAuth 回调端口"
claude mcp add --transport http \
  --callback-port 8080 \
  my-server https://mcp.example.com/mcp
```

#### 预配置 OAuth 凭据

如果服务器不支持动态客户端注册（报错"不兼容的身份验证服务器"），需要通过开发者门户注册 OAuth 应用，然后提供凭据：

``` bash title="使用预配置的 OAuth 凭据"
claude mcp add --transport http \
  --client-id your-client-id --client-secret --callback-port 8080 \
  my-server https://mcp.example.com/mcp
```

也可以通过环境变量跳过交互式密钥输入：

``` bash title="通过环境变量传递密钥"
MCP_CLIENT_SECRET=your-secret claude mcp add --transport http \
  --client-id your-client-id --client-secret --callback-port 8080 \
  my-server https://mcp.example.com/mcp
```

#### 限制 OAuth 范围

`oauth.scopes` 可以固定授权请求的 scope，限制到安全团队批准的子集：

``` json title="限制 OAuth 范围"
{
  "mcpServers": {
    "slack": {
      "type": "http",
      "url": "https://mcp.slack.com/mcp",
      "oauth": {
        "scopes": "channels:read chat:write search:read"
      }
    }
  }
}
```

如果服务器返回 403 `insufficient_scope`，Claude Code 会用相同的固定范围重新认证。

#### 覆盖 OAuth 元数据发现

默认情况下，Claude Code 会依次检查 `/.well-known/oauth-protected-resource` 和 `/.well-known/oauth-authorization-server`。如果需要覆盖，可以设置 `authServerMetadataUrl`：

``` json title="覆盖 OAuth 元数据发现"
{
  "mcpServers": {
    "my-server": {
      "type": "http",
      "url": "https://mcp.example.com/mcp",
      "oauth": {
        "authServerMetadataUrl": "https://auth.example.com/.well-known/openid-configuration"
      }
    }
  }
}
```

!!! tip "OAuth 稳定性改进（v2.1.118）"

    - Token 缺少 `expires_in` 时能正确处理，不再每小时重新认证
    - 跨进程锁修复了 MCP OAuth token 刷新竞争条件
    - `insufficient_scope` 403 智能处理：静默刷新而非反复提示
    - 超时/取消时不再出现未处理的 promise rejection

### 自定义认证方式

如果 MCP 服务器使用非 OAuth 的认证方式（如 Kerberos、内部 SSO、短期 token），可以通过 `headersHelper` 指定一个命令来动态生成请求头：

``` json title="使用外部脚本生成认证头"
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

也可以内联命令：

``` json title="内联 headersHelper 命令"
{
  "mcpServers": {
    "internal-api": {
      "type": "http",
      "url": "https://mcp.internal.example.com",
      "headersHelper": "echo '{\"Authorization\": \"Bearer '\"$(get-token)\"'\"}'"
    }
  }
}
```

`headersHelper` 每次连接时执行（会话启动和重连），超时 10 秒，输出必须是字符串键值对的 JSON 对象。Claude Code 会设置 `CLAUDE_CODE_MCP_SERVER_NAME` 和 `CLAUDE_CODE_MCP_SERVER_URL` 环境变量供脚本使用。

⚠️ `headersHelper` 在项目或本地范围定义时，仅在用户接受工作区信任对话框后运行。

## 🔍 MCP 工具的发现与调用

### Tool Search：按需加载，节省上下文

连接了很多 MCP 服务器，会不会撑爆上下文窗口？Claude Code 的 **Tool Search**（工具搜索）机制解决了这个问题：

1. 会话启动时，**只加载 MCP 工具的名称**，不加载完整定义
2. 当 Claude 执行某个任务需要外部工具时，通过搜索发现相关的工具定义
3. **只有 Claude 实际调用的工具**才会进入上下文

这意味着你可以放心连接多个 MCP 服务器，对上下文的影响微乎其微。

💡 如果你在开发 MCP 服务器，建议编写清晰的**服务器说明**（server instructions），告诉 Claude 你的工具擅长什么、什么时候该搜索它们——这和 Skills 的工作方式类似。工具描述和服务器说明截断为每条 2KB，保持简洁。

### 配置 Tool Search

Tool Search 默认启用。通过 `ENABLE_TOOL_SEARCH` 环境变量控制行为：

| 值 | 行为 |
|----|------|
| （未设置） | 所有 MCP 工具延迟加载。Vertex AI 或非第一方 `ANTHROPIC_BASE_URL` 回退到预先加载 |
| `true` | 强制所有 MCP 工具延迟加载（包括 Vertex AI） |
| `auto` | 阈值模式：工具适合上下文窗口 10% 内时预先加载 |
| `auto:<N>` | 自定义阈值（`auto:5` = 5%） |
| `false` | 完全禁用，所有工具预先加载 |

``` bash title="配置 Tool Search"
# 使用自定义 5% 阈值
ENABLE_TOOL_SEARCH=auto:5 claude

# 完全禁用
ENABLE_TOOL_SEARCH=false claude
```

此功能需要支持 `tool_reference` 块的模型（Sonnet 4+、Opus 4+）。Haiku 模型不支持。

### `alwaysLoad` 选项

对于需要 Claude 立即可用的核心工具，设 `alwaysLoad: true` 跳过 Tool Search 延迟：

``` json title=".mcp.json"
{
  "mcpServers": {
    "core-tools": {
      "type": "http",
      "url": "https://mcp.example.com/mcp",
      "alwaysLoad": true
    }
  }
}
```

MCP 服务器也可以在工具的 `_meta` 中标记单个工具为始终加载：`"anthropic/alwaysLoad": true`。

### 动态工具更新

MCP 服务器支持 `list_changed` 通知。当服务器更新了可用的工具、提示或资源时，Claude Code 会**自动刷新**，无需你断开重连。

### 自动重连

HTTP 或 SSE 服务器断开连接时，Claude Code 自动以**指数退避**重连：最多 5 次尝试，从 1 秒延迟开始，每次翻倍。重连期间服务器在 `/mcp` 中显示为待处理状态。5 次失败后标记为失败，可手动重试。

启动时的初始连接失败也采用相同策略。从 v2.1.121 起，瞬态错误（5xx 响应、连接拒绝、超时）最多重试 3 次。认证错误和 404 不会重试。

## 💰 MCP 输出限制

虽然 Tool Search 大幅降低了上下文消耗，但 MCP 工具的**输出内容**仍然会占用上下文。Claude Code 内置了保护机制：

| 参数 | 说明 |
|------|------|
| 警告阈值 | 单个 MCP 工具输出超过 10,000 tokens 时显示警告 |
| 默认上限 | 25,000 tokens（可通过环境变量调整） |
| 硬性上限 | 500,000 字符（单个工具返回结果的最大值） |

``` bash title="调整 MCP 输出上限"
export MAX_MCP_OUTPUT_TOKENS=50000
claude
```

### 为特定工具提高限制

如果你在开发 MCP 服务器，可以在工具的 `tools/list` 响应中设置 `_meta["anthropic/maxResultSizeChars"]` 来提高单个工具的持久化阈值（硬上限 500,000 字符）：

``` json title="工具注解提高输出限制"
{
  "name": "get_schema",
  "description": "Returns the full database schema",
  "_meta": {
    "anthropic/maxResultSizeChars": 200000
  }
}
```

对于文本内容，此注解独立于 `MAX_MCP_OUTPUT_TOKENS` 生效。返回图像数据的工具仍受令牌限制。

## 📋 MCP 资源引用

MCP 服务器可以公开**资源**，你可以用 `@` 提及来引用，类似于引用文件。

**引用格式**：`@server:protocol://resource/path`

```
分析 @github:issue://123 并建议修复方案
审查 @docs:file://api/authentication 中的 API 文档
对比 @postgres:schema://users 和 @docs:file://database/user-model
```

在提示中键入 `@` 即可看到所有已连接 MCP 服务器的可用资源。资源路径支持模糊搜索。引用时自动获取并作为附件包含。

## 📝 MCP 提示作为命令

MCP 服务器可以公开**提示**（Prompts），在 Claude Code 中作为命令可用。

**命令格式**：`/mcp__servername__promptname`

```
/mcp__github__list_prs
/mcp__github__pr_review 456
/mcp__jira__create_issue "Bug in login flow" high
```

键入 `/` 即可在命令列表中看到所有 MCP 提示命令。参数根据提示的定义解析，结果直接注入对话。

## ☁️ Claude.ai MCP 同步

如果你已用 Claude.ai 帐户登录 Claude Code，你在 Claude.ai 中添加的 MCP 服务器会**自动同步**：

1. 在 [claude.ai/customize/connectors](https://claude.ai/customize/connectors) 添加服务器（Team 和 Enterprise 计划仅管理员可添加）
2. 在 Claude.ai 中完成认证
3. 在 Claude Code 中使用 `/mcp` 查看，Claude.ai 服务器会带标识显示

要禁用 Claude.ai MCP 服务器：

``` bash
ENABLE_CLAUDEAI_MCP_SERVERS=false claude
```

## 🧩 插件 MCP 服务器

[插件](plugins.md)可以捆绑 MCP 服务器，启用插件时自动提供工具和集成。插件 MCP 服务器的工作方式与手动配置的服务器相同。

**工作原理**：

- 插件在 `.mcp.json` 或 `plugin.json` 中定义 MCP 服务器
- 启用插件时自动连接，禁用时通过 `/reload-plugins` 断开
- 使用 `${CLAUDE_PLUGIN_ROOT}` 引用插件目录，`${CLAUDE_PLUGIN_DATA}` 引用持久数据目录

``` json title="plugin.json 中的 MCP 配置"
{
  "name": "my-plugin",
  "mcpServers": {
    "plugin-api": {
      "command": "${CLAUDE_PLUGIN_ROOT}/servers/api-server",
      "args": ["--port", "8080"]
    }
  }
}
```

## 🙋 Elicitation：服务器请求用户输入

MCP 服务器可以在任务中途**请求你的结构化输入**。当服务器需要无法自行获取的信息时，Claude Code 会显示交互式对话框。

两种请求模式：

- **表单模式**：显示服务器定义的表单字段（如用户名、密码）
- **URL 模式**：打开浏览器 URL 进行认证或批准

要自动响应 Elicitation 请求而不显示对话框，可以使用 `Elicitation` hook。

## 🏢 托管 MCP 配置

企业需要对 MCP 服务器进行集中控制时，Claude Code 提供两种方案。

### 方案一：`managed-mcp.json` 独占控制

部署固定 MCP 服务器集，用户无法修改或扩展。系统管理员将配置文件部署到：

- macOS：`/Library/Application Support/ClaudeCode/managed-mcp.json`
- Linux/WSL：`/etc/claude-code/managed-mcp.json`
- Windows：`C:\Program Files\ClaudeCode\managed-mcp.json`

``` json title="managed-mcp.json"
{
  "mcpServers": {
    "github": {
      "type": "http",
      "url": "https://api.githubcopilot.com/mcp/"
    },
    "company-internal": {
      "type": "stdio",
      "command": "/usr/local/bin/company-mcp-server",
      "args": ["--config", "/etc/company/mcp-config.json"]
    }
  }
}
```

⚠️ `managed-mcp.json` 存在时，用户无法通过 `claude mcp add` 或配置文件添加任何 MCP 服务器。

### 方案二：Allow/Deny 策略控制

允许用户配置自己的服务器，但限制允许的服务器。在[托管设置文件](configuration/settings-permissions.md)中配置 `allowedMcpServers` 和 `deniedMcpServers`。

每条规则通过 `serverName`、`serverCommand` 或 `serverUrl` 之一匹配：

``` json title="Allow/Deny 策略示例"
{
  "allowedMcpServers": [
    { "serverName": "github" },
    { "serverCommand": ["npx", "-y", "@modelcontextprotocol/server-filesystem"] },
    { "serverUrl": "https://mcp.company.com/*" }
  ],
  "deniedMcpServers": [
    { "serverName": "dangerous-server" },
    { "serverUrl": "https://*.untrusted.com/*" }
  ]
}
```

**匹配规则**：

- 命令数组必须**精确**匹配（包括参数顺序）
- URL 支持 `*` 通配符（如 `https://*.internal.corp/*`）
- 当允许列表存在 `serverCommand` 条目时，stdio 服务器必须匹配命令
- 拒绝列表具有绝对优先级——即使同时在允许列表中也会被阻止

!!! tip "方案选择"

    需要部署固定服务器集（不允许用户自定义）→ 方案一。需要允许用户在策略约束内添加自己的服务器 → 方案二。两者可以组合使用。

## 📦 常见 MCP 服务器示例

### 连接 GitHub

GitHub 的远程 MCP 服务器使用个人访问令牌认证：

``` bash title="添加 GitHub MCP 服务器"
claude mcp add --transport http github https://api.githubcopilot.com/mcp/ \
  --header "Authorization: Bearer YOUR_GITHUB_PAT"
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

在 Claude Desktop 中通过以下配置来使用：

``` json title="claude_desktop_config.json"
{
  "mcpServers": {
    "claude-code": {
      "type": "stdio",
      "command": "/full/path/to/claude",
      "args": ["mcp", "serve"]
    }
  }
}
```

⚠️ 如果 `claude` 命令不在系统 PATH 中，需要使用 `which claude` 找到完整路径并替换 `command` 字段。

## 🔧 排查 MCP 连接问题

当 MCP 服务器连接出现问题时，按以下步骤排查：

1. **检查服务器状态**：在 Claude Code 会话中运行 `/mcp` 查看连接状态
2. **检查配置**：运行 `claude mcp get <name>` 确认 URL、命令、环境变量是否正确
3. **网络连通性**：HTTP/SSE 服务器需要确认目标 URL 可达
4. **认证问题**：检查 OAuth 授权是否过期，API Key 是否有效
5. **环境变量**：如果使用了 `${VAR}` 展开语法，确认对应变量已设置且有默认值
6. **启动超时**：使用 `MCP_TIMEOUT` 环境变量调整服务器启动超时（如 `MCP_TIMEOUT=10000 claude` 设置 10 秒）
7. **代理设置**：如果通过代理访问，确认 `ANTHROPIC_BASE_URL` 配置正确（非官方代理可能导致 Tool Search 失效）

!!! bug "v2.1.113 关键修复"

    - **并发调用超时正确隔离**：之前一个工具调用的消息可能默默撤销另一个调用的看门狗，导致后者无限挂起；现在每个调用的超时独立计算
    - **`ToolSearch` 排名修正**：粘贴的 MCP 工具名现在会返回**实际工具**，而非描述匹配的"兄弟"工具

📝 小结：MCP 是 Claude Code 扩展能力的核心机制。通过配置 MCP 服务器，你可以让 Claude Code 连接到几乎所有外部服务——从代码托管平台到数据库、从监控系统到设计工具。关键是选对传输方式（HTTP 优先）、配对作用域（团队用 project，个人用 local），善用 Tool Search 管控上下文成本，企业场景可通过托管配置实现集中管控。
