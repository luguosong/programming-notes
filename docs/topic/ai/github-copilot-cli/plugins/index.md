# 插件系统

插件（Plugins）为 Copilot CLI 添加新的 Agent、Skill、MCP 服务器和 Slash 命令，是最直接的扩展方式。

## 安装插件

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

## 管理插件

| 命令 | 功能 |
|------|------|
| `/plugin list` | 显示已安装的所有插件及其状态 |
| `/plugin uninstall <name>` | 卸载指定插件 |
| `/plugin enable <name>` | 启用插件 |
| `/plugin disable <name>` | 禁用插件 |

## 插件市场

Copilot CLI 自带两个默认插件市场：

| 市场 | 说明 |
|------|------|
| `copilot-plugins` | 官方插件市场 |
| `awesome-copilot` | 社区插件市场 |

| 命令 | 功能 |
|------|------|
| `/plugin marketplace list` | 列出可用的市场 |
| `/plugin marketplace browse <name>` | 浏览指定市场中的插件 |

## 插件提供的内容

插件可以为 Copilot CLI 添加：

- `Agent`：新的专业角色（如数据库管理 Agent）
- `Skills`：新的自动触发技能
- `MCP 服务器`：新的外部数据源连接
- `Slash 命令`：新的斜杠命令
