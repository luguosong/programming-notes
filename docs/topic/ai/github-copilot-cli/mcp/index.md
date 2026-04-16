---
title: MCP 服务器
description: 配置和管理 MCP 服务器，扩展 Copilot CLI 的工具能力
---

# MCP 服务器

**本文你会学到**：

- 🔌 MCP 是什么，解决什么问题
- ⚙️ 如何配置和使用 MCP 服务器
- 📦 常用的 MCP 服务器推荐
- 🔒 MCP 配置文件层级和安全注意事项
- 🔐 OAuth 认证、Sampling 和安全策略

打个比方：Copilot 本身像一台电脑，`MCP（Model Context Protocol）` 就像是 USB 接口标准——通过它，你可以给 Copilot 接上各种"外设"：数据库连接器、浏览器控制、文档查询器等，让它能访问原本接触不到的外部资源。

---

## ❓ 什么是 MCP？

MCP 提供了一种标准化的方式，让 AI 模型与外部系统通信：

``` mermaid
graph LR
    A["Copilot CLI"] <-->|MCP 协议| B["MCP 服务器"]
    B <--> C["GitHub API"]
    B <--> D["文件系统"]
    B <--> E["数据库"]
    B <--> F["第三方 API"]

    classDef default fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
```

!!! info "自动调用"

    添加 MCP 服务器后，Copilot 会在与你的 prompt 相关时`自动使用`它提供的工具。你也可以在 prompt 中显式提及 MCP 工具名称来确保调用。

---

## 🐙 内置 GitHub MCP 服务器

Copilot CLI 内置了 GitHub MCP 服务器，无需额外配置即可使用。它提供对 GitHub 平台的完整访问：

``` text
# 查看仓库的 Issue
> 列出所有标记为 "bug" 的 Issue

# 搜索代码
> 在组织的所有仓库中搜索使用了 deprecated API 的代码

# 查看 PR
> 总结 PR #42 的所有变更
```

### GitHub MCP 提供的能力

- 读取和搜索 Issue / PR
- 查看仓库文件和代码
- 查看 CI/CD 工作流状态
- 搜索组织范围的代码
- 查看 commit 历史

---

## ➕ 添加 MCP 服务器

### 方法一：交互式添加（推荐新手使用）

``` text
/mcp add
```

按照交互提示输入服务器名称、命令、参数等信息。适合第一次配置 MCP 的用户。

### 方法二：直接编辑配置文件

如果你已经熟悉配置格式，直接编辑配置文件更快。

MCP 服务器配置文件位于 `~/.copilot/mcp-config.json`（全局）或项目根目录 `.mcp.json`（项目级）：

``` json title="~/.copilot/mcp-config.json"
{
  "mcpServers": {
    "filesystem": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/path/to/allowed/directory"
      ]
    },
    "context7": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp"]
    }
  }
}
```

### 配置字段说明

| 字段 | 必需 | 说明 |
|------|:----:|------|
| `type` | ✅ | 通信类型，通常为 `"stdio"` |
| `command` | ✅ | 启动 MCP 服务器的命令 |
| `args` | ❌ | 命令参数数组 |
| `env` | ❌ | 环境变量键值对 |

---

## 📦 常用 MCP 服务器

### Filesystem MCP

提供受控的文件系统访问：

``` json title="文件系统 MCP 配置"
{
  "mcpServers": {
    "filesystem": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/home/user/projects",
        "/home/user/documents"
      ]
    }
  }
}
```

最后的路径参数指定 MCP 服务器可以访问的目录。

### Context7

查询最新的库文档和 API 参考：

``` json title="Context7 MCP 配置"
{
  "mcpServers": {
    "context7": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp"]
    }
  }
}
```

``` text
# 使用 Context7 查询最新文档
> 使用 Context7 查询 React 19 的最新 API 变更
```

### Microsoft Learn MCP

访问 Microsoft 技术文档：

``` json title="Microsoft Learn MCP 配置"
{
  "mcpServers": {
    "microsoft-learn": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@nicobailon/mcp-microsoft-learn"]
    }
  }
}
```

---

## 🌐 Web 访问

除了通过 MCP 服务器访问外部资源，Copilot CLI 还内置了 `web_fetch` 工具，可以直接获取网页内容——不需要安装任何 MCP 服务器就能使用。

``` text
# 获取 API 文档
> @https://api.example.com/docs 根据这个文档生成客户端代码

# 获取 JSON 数据
> @https://jsonplaceholder.typicode.com/users 分析这个 API 的数据结构
```

---

## 🔧 管理 MCP 服务器

除了在交互会话中使用 `/mcp` 命令，还可以通过 CLI 直接管理 MCP 服务器（1.0.21 新增）：

```bash
# 在终端中直接管理 MCP（无需进入交互会话）
copilot mcp
```

1.0.25 起，`copilot mcp` 新增了**注册表安装向导**——直接在 CLI 中通过 MCP 注册表搜索、选择并引导完成 MCP 服务器配置，无需手动编写 JSON 配置文件。

交互会话内的 `/mcp` 命令：

| 命令 | 功能 |
|------|------|
| `/mcp show` | 列出所有已配置的 MCP 服务器 |
| `/mcp show <name>` | 查看特定服务器的详细信息和工具列表 |
| `/mcp add` | 交互式添加新的 MCP 服务器 |
| `/mcp edit <name>` | 编辑服务器配置 |
| `/mcp delete <name>` | 删除服务器 |
| `/mcp disable <name>` | 临时禁用服务器（保留配置） |
| `/mcp enable <name>` | 重新启用之前禁用的服务器 |
| `/mcp reload` | 重新加载所有 MCP 服务器 |
| `/mcp auth <name>` | 重新认证 MCP 服务器的 OAuth（1.0.15 新增） |

`/mcp disable` 和 `/mcp enable` 的状态跨会话持久化（1.0.19 新增），关闭终端后再次启动 Copilot CLI 时，之前禁用的服务器仍然保持禁用状态，无需每次重新配置。

MCP 工具调用在时间线中会显示工具名称和参数摘要（1.0.16 新增），方便追踪 MCP 服务器的调用情况。

---

## 📂 配置文件层级

MCP 配置支持项目级和全局级两个层级：

| 层级 | 路径 | 适用范围 |
|------|------|---------|
| 项目级 | `.mcp.json`（仓库根目录） | 仅当前项目 |
| 全局级 | `~/.copilot/mcp-config.json` | 所有项目 |

!!! warning "1.0.22 配置源精简"

    1.0.22 起，CLI 只读取 `.mcp.json` 作为项目级 MCP 配置源。之前支持的 `.vscode/mcp.json` 和 `.devcontainer/devcontainer.json` 已移除。如果 CLI 检测到 `.vscode/mcp.json` 但没有 `.mcp.json`，会显示迁移提示。

项目级配置优先于全局配置。团队共享的 MCP 服务器建议放在项目级配置中并提交到 Git。

!!! warning "安全注意"

    `env` 字段中的 API Key 等敏感信息不应提交到 Git。对于需要密钥的 MCP 服务器，建议：

    - 在全局配置中设置（不提交到 Git）
    - 或使用环境变量引用（如 `"API_KEY": "$MY_API_KEY"`）

---

## 🔐 MCP OAuth 认证

某些 MCP 服务器需要 OAuth 认证才能访问（如 Slack、Atlassian Rovo 等）。Copilot CLI 提供了完整的 OAuth 支持：

- **浏览器回调流程**：在支持 HTTPS 的环境中自动打开浏览器完成认证（1.0.17 新增自签名证书回退，兼容要求 HTTPS 的 OAuth 提供商）
- **设备码流程**（Device Code Flow，RFC 8628）：在无头环境或 CI 中作为回退方案（1.0.15 新增）
- **带外交互**（Out-of-Band）：MCP 服务器可请求用户访问 URL 进行交互式认证，如 OAuth 授权、API Key 输入等（0.0.423 新增）

使用 `/mcp auth <name>` 命令可重新认证或切换账号（1.0.15 新增）：

``` text
# 重新认证 MCP 服务器
/mcp auth slack-mcp
```

---

## 🧠 MCP Sampling

MCP 服务器可以请求 LLM 推理能力（sampling），让服务器端逻辑借助 AI 做出更智能的决策（1.0.13 新增）。

Sampling 的工作流程：MCP 服务器向 Copilot 发送 sampling 请求 → Copilot 弹出审查提示，展示请求内容 → 用户批准后，LLM 生成响应并返回给服务器。出于安全考虑，**每次** sampling 请求都需要用户明确批准，不支持自动放行。

---

## 🛡️ MCP 安全策略

- **Allowlist 验证**：通过 `MCP_ALLOWLIST` 环境变量指定可信注册表地址列表，只有来源匹配的 MCP 服务器才会被加载——防止不可信来源的服务器被注入（1.0.8 新增）
- **组织级策略**：组织管理员可统一配置第三方 MCP 服务器策略，对所有成员强制执行，个人配置无法覆盖（1.0.11 改进）
- **Workspace 信任**：Workspace 级别配置的 MCP 服务器不会自动加载，需要用户在首次打开项目时确认信任该文件夹（1.0.10 改进）
