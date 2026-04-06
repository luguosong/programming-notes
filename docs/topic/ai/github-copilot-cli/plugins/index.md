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

| 命令 | 功能 |
|------|------|
| `/plugin list` | 显示已安装的所有插件及其状态 |
| `/plugin uninstall <name>` | 卸载指定插件 |
| `/plugin enable <name>` | 启用插件 |
| `/plugin disable <name>` | 禁用插件 |

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

插件可以为 Copilot CLI 添加：

- `Agent`：新的专业角色（如数据库管理 Agent）
- `Skills`：新的自动触发技能
- `MCP 服务器`：新的外部数据源连接
- `Slash 命令`：新的斜杠命令
