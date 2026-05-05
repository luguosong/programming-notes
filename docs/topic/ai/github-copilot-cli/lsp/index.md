---
title: LSP 服务器
description: 通过 Language Server Protocol 提升 Copilot CLI 的代码理解精度
---

# LSP 服务器

> LSP 就像给 Copilot 配了一个母语翻译——它不再靠"猜"来理解代码结构，而是拿到编译器级别的精确信息。

**本文你会学到**：

- 理解 LSP 如何提升 Copilot CLI 的代码理解能力
- 掌握添加和配置 LSP 服务器的方法
- 了解 LSP 配置的加载优先级机制
- 能够使用 `/lsp` 命令管理语言服务器

## 概念

Language Server Protocol（LSP）是编辑器与语言服务器之间的开放通信标准。语言服务器是一个独立进程，提供跳转定义、查找引用、重命名符号等语言特性。Copilot CLI 可以接入 LSP 服务器，获得编译器/分析器级别的代码理解能力，从而更精准地操作项目代码。

!!! note "自动使用"
    LSP 服务器就绪后，Copilot CLI 会在合适的场景下自动调用，无需手动触发。Copilot 会根据你的提问选择最合适的 LSP 操作——例如问"where is `handlePayment` defined?"时自动使用 go-to-definition。

## 优势

| 优势 | 说明 |
|------|------|
| **精确性** | 结果来自语言自身的编译器/分析器，能区分真正的定义与文本相似的匹配项 |
| **Token 效率** | "列出所有符号"、"查找引用"返回紧凑的结构化数据，无需将整个文件读入对话 |
| **安全重构** | 重命名符号时，LSP 可靠地更新项目中所有引用，不会遗漏或误改 |
| **速度** | 语言服务器在后台索引项目，响应近乎即时 |

## 支持的操作

| 操作 | 说明 |
|------|------|
| Go to definition | 跳转到符号（函数、类、变量）的定义位置 |
| Find references | 查找符号在项目中的所有使用位置 |
| Hover | 获取符号的类型信息与文档 |
| Rename | 跨项目重命名符号，同步更新所有引用 |
| Document symbols | 列出文件中定义的所有符号 |
| Workspace symbol search | 按名称在整个项目中搜索符号 |
| Go to implementation | 查找接口或抽象方法的具体实现 |
| Incoming calls | 查看哪些函数调用了指定函数 |
| Outgoing calls | 查看指定函数调用了哪些函数 |

## 添加 LSP 服务器

添加 LSP 服务器分为两步：

1. **安装语言服务器软件**——通过 npm、gem、pip 等包管理器安装到本地机器
2. **在配置文件中注册**——告诉 Copilot CLI 如何启动该服务器

!!! tip "快速安装"
    可以使用 `lsp-setup` skill 自动化安装与配置流程。从 Awesome GitHub Copilot 下载 `lsp-setup` skill，放入 `~/.copilot/skills/` 或 `.github/skills/` 目录，然后在 Copilot CLI 中输入 `setup lsp` 按提示操作即可。

!!! warning "安全提示"
    只从你信任的来源安装 LSP 服务器。

### 配置文件格式

配置文件使用统一的 JSON 结构：

```json
{
  "lspServers": {
    "服务器名称": {
      "command": "启动命令",
      "args": ["参数1", "参数2"],
      "fileExtensions": {
        ".扩展名": "语言ID"
      }
    }
  }
}
```

各字段说明：

| 字段 | 必填 | 说明 |
|------|------|------|
| `command` | 是 | 启动 LSP 服务器的命令 |
| `args` | 否 | 传递给命令的参数 |
| `fileExtensions` | 是 | 文件扩展名与语言 ID 的映射 |
| `env` | 否 | 启动服务器时设置的环境变量，支持 `${VAR}` 和 `${VAR:-default}` 语法 |
| `rootUri` | 否 | 服务器的根目录（相对于 Git 根目录），默认为 `.`，适用于 monorepo 场景 |
| `initializationOptions` | 否 | 启动时发送给服务器的自定义选项 |
| `requestTimeoutMs` | 否 | 请求超时时间（毫秒），默认 90 秒 |

### 配置文件位置

| 级别 | 路径 | 作用范围 |
|------|------|---------|
| 项目级 | `.github/lsp.json` | 当前仓库的所有贡献者 |
| 用户级 | `~/.copilot/lsp-config.json` | 当前用户的所有项目 |
| 插件级 | 随插件自动安装 | 安装该插件的项目 |

## 加载优先级

Copilot CLI 启动时按以下优先级加载 LSP 配置（从高到低）：

1. **项目级** `.github/lsp.json`——优先级最高
2. **插件级**——已安装插件提供的语言服务器
3. **用户级** `~/.copilot/lsp-config.json`——优先级最低

同名服务器，高优先级覆盖低优先级。这意味着项目可以为所有贡献者统一定制或禁用特定语言服务器。

!!! note "自动启动"
    工作目录被信任后，Copilot CLI 会自动在后台启动与项目相关的 LSP 服务器，确保需要时能立即响应。

## 管理命令

在 Copilot CLI 交互会话中，使用 `/lsp` 斜杠命令管理语言服务器：

| 命令 | 说明 |
|------|------|
| `/lsp` 或 `/lsp show` | 查看所有已配置 LSP 服务器的状态 |
| `/lsp test 服务器名称` | 测试指定服务器能否正常启动 |
| `/lsp reload` | 从磁盘重新加载 LSP 配置 |
| `/lsp help` | 显示 `/lsp` 命令帮助 |

## 参考资源

- [Language Server Protocol 官方站点](https://microsoft.github.io/language-server-protocol/)
- [LSP 服务器实现列表](https://microsoft.github.io/language-server-protocol/implementors/servers/)
