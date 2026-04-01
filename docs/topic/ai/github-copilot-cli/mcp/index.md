# MCP 服务器

Model Context Protocol（MCP）是一种开放标准，用于将 AI 助手连接到外部数据源和工具。通过 MCP，Copilot CLI 可以访问数据库、API、文件系统等外部资源。

---

## 什么是 MCP？

MCP 提供了一种标准化的方式，让 AI 模型与外部系统通信：

``` text
Copilot CLI ←→ MCP 协议 ←→ MCP 服务器 ←→ 外部系统
                                            ├── GitHub API
                                            ├── 文件系统
                                            ├── 数据库
                                            └── 第三方 API
```

!!! info "自动调用"

    添加 MCP 服务器后，Copilot 会在与你的 prompt 相关时**自动使用**它提供的工具。你也可以在 prompt 中显式提及 MCP 工具名称来确保调用。

---

## 内置 GitHub MCP 服务器

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

## 添加 MCP 服务器

### 方法一：交互式添加

``` text
/mcp add
```

按照交互提示输入服务器名称、命令、参数等信息。

### 方法二：直接编辑配置文件

MCP 服务器配置文件位于 `~/.copilot/mcp-config.json`（全局）或项目根目录 `.github/mcp-config.json`（项目级）：

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

## 常用 MCP 服务器

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

## Web 访问

Copilot CLI 内置了 `web_fetch` 工具，可以直接获取网页内容，无需额外的 MCP 服务器：

``` text
# 获取 API 文档
> @https://api.example.com/docs 根据这个文档生成客户端代码

# 获取 JSON 数据
> @https://jsonplaceholder.typicode.com/users 分析这个 API 的数据结构
```

---

## 管理 MCP 服务器

| 命令 | 功能 |
|------|------|
| `/mcp show` | 列出所有已配置的 MCP 服务器 |
| `/mcp show <name>` | 查看特定服务器的详细信息和工具列表 |
| `/mcp add` | 交互式添加新的 MCP 服务器 |
| `/mcp edit <name>` | 编辑服务器配置 |
| `/mcp delete <name>` | 删除服务器 |
| `/mcp disable <name>` | 临时禁用服务器（保留配置） |
| `/mcp enable <name>` | 重新启用之前禁用的服务器 |

---

## 配置文件层级

MCP 配置支持项目级和全局级两个层级：

| 层级 | 路径 | 适用范围 |
|------|------|---------|
| 项目级 | `.github/mcp-config.json`（仓库根目录） | 仅当前项目 |
| 全局级 | `~/.copilot/mcp-config.json` | 所有项目 |

项目级配置优先于全局配置。团队共享的 MCP 服务器建议放在项目级配置中并提交到 Git。

!!! warning "安全注意"

    `env` 字段中的 API Key 等敏感信息不应提交到 Git。对于需要密钥的 MCP 服务器，建议：

    - 在全局配置中设置（不提交到 Git）
    - 或使用环境变量引用（如 `"API_KEY": "$MY_API_KEY"`）
