---
title: 插件系统
description: 安装和管理 Copilot CLI 插件，扩展 Agent、Skill 和斜杠命令
---

# 插件系统

**本文你会学到**：
- 🤔 插件解决什么问题（为什么不直接手动配置）
- 📦 如何安装、管理和浏览插件
- 🛒 插件市场的工作方式
- 🧩 插件可以提供哪些类型的扩展

打个比方：如果说 MCP 服务器是给 Copilot 接"外设"，Skill 是给它"操作手册"，那插件就是一整套`预配置的工具箱`——一个插件可能同时包含 Agent、Skill、MCP 服务器和斜杠命令，安装一个插件就获得了多种能力的组合。

---

## 🤔 为什么用插件？

手动逐个创建 Agent、Skill、MCP 配置虽然灵活，但对于常见的场景（如 Java 开发、测试自动化）来说，社区已经有人做好了一套完整的配置。插件就是把这些配置打包在一起，让你一键安装、即装即用。

---

## 📦 安装插件

=== "从应用市场安装"

    ``` text
    /plugin install PLUGIN-NAME@MARKETPLACE-NAME
    ```

    例如：

    ``` text
    /plugin install database-data-management@awesome-copilot
    ```

=== "从 GitHub 仓库安装"

    ``` text
    # 从 GitHub.com 上的仓库
    /plugin install OWNER/REPO

    # 从任意在线 Git 仓库
    /plugin install URL-OF-GIT-REPO
    ```

=== "从本地路径安装"

    ``` text
    /plugin install ./PATH/TO/PLUGIN
    ```

## 🔧 管理插件

安装插件后，可以随时查看、启用或禁用它们。`disable` 会保留插件文件但暂时停用其功能，适合调试时临时关闭某个插件。

| 命令 | 功能 |
|------|------|
| `/plugin list` | 显示已安装的所有插件及其状态 |
| `/plugin uninstall <name>` | 卸载指定插件（删除文件） |
| `/plugin enable <name>` | 启用已禁用的插件 |
| `/plugin disable <name>` | 暂时禁用插件（保留文件） |

## 🛒 插件市场

Copilot CLI 自带两个默认插件市场——就像手机的应用商店：

| 市场 | 说明 |
|------|------|
| `copilot-plugins` | 官方插件市场（经过验证，质量有保障） |
| `awesome-copilot` | 社区插件市场（种类更多，需自行评估质量） |

| 命令 | 功能 |
|------|------|
| `/plugin marketplace list` | 列出可用的市场 |
| `/plugin marketplace browse <name>` | 浏览指定市场中的插件 |

## 🧩 插件提供的内容

一个插件可以同时包含以下一种或多种扩展——这也是插件比手动配置更方便的原因：一次安装就能获得完整的能力组合。

- `Agent`：新的专业角色（如数据库管理 Agent、安全审计 Agent）
- `Skills`：新的自动触发技能（如测试生成、代码审查清单）
- `MCP 服务器`：新的外部数据源连接（如数据库、API 文档服务）
- `Slash 命令`：新的斜杠命令（如 `/deploy`、`/analyze`）
- `LSP 服务器`：语言服务器协议支持（0.0.422 新增），在 `/lsp show` 中查看

💡 安装一个 `database-data-management` 插件，你可能同时获得：一个 DBA Agent + 一套 SQL 审查 Skill + 一个 PostgreSQL MCP 服务器。

---

## 🔌 Extensions（实验性）

Extensions 是比插件更底层的扩展机制（1.0.3 新增，实验性）。与插件侧重于提供工具和上下文不同，Extension 可以深度介入 Copilot 的内部流程——比如接入权限系统或提供语言服务器。Extension 以 CommonJS 模块编写（1.0.4 新增），可以：

- 接入权限系统（1.0.6 新增）
- 提供 LSP 服务器支持
- 通过 Extension 模式设置控制可扩展性（1.0.8 新增）

使用 `/extensions` 命令查看、启用和禁用已安装的 Extensions（1.0.5 新增）。

在配置中设置 `enabledPlugins` 可实现插件自动安装（0.0.422 新增），无需手动执行 `/plugin install`。
