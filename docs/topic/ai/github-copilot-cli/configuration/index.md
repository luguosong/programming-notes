---
title: 配置体系
description: 信任目录、工具权限、路径权限与 URL 权限的完整配置指南
---

**本文你会学到**：

- 📂 配置文件与 `COPILOT_HOME` 环境变量的作用
- 🛡️ 信任目录的安全含义与持久化方式
- 🔧 工具权限的三种审批选项和模式匹配语法
- 📁 路径权限的默认范围与扩展方式
- 🌐 URL 权限的 allow/deny 规则与协议差异
- 🔓 `--allow-all` / `--yolo` 组合权限的使用场景
- 💾 权限持久化机制（1.0.37 新增）

配置就像给 Copilot 发放不同级别的门禁卡——你可以精确控制它能在哪些房间走动、能打开哪些抽屉。

---

## 📂 配置文件概述

Copilot CLI 的行为通过多个配置文件协同控制。除了常见的自定义指令文件外，还有自动管理的 `config.json` 和 `permissions-config.json`。

### 配置方式总览

| 配置方式 | 文件 | 适用范围 | 管理方式 |
|---------|------|---------|---------|
| 自定义指令 | `.github/copilot-instructions.md` | 项目级：定制 Copilot 行为和编码规范 | 手动编辑，提交到仓库 |
| 全局指令 | `~/.copilot/copilot-instructions.md` | 用户级：跨项目通用的个人偏好 | 手动编辑 |
| 用户设置 | `~/.copilot/settings.json` | 用户级：全局默认配置 | 手动编辑或 `/model`、`/theme` 等命令 |
| 仓库设置 | `.github/copilot/settings.json` | 项目级：团队共享配置 | 手动编辑，提交到仓库 |
| 本地设置 | `.github/copilot/settings.local.json` | 项目级：个人覆盖（不提交） | 手动编辑，加入 `.gitignore` |
| 内部状态 | `~/.copilot/config.json` | 用户级：认证、插件等运行时数据 | CLI 自动管理 |
| 权限记录 | `~/.copilot/permissions-config.json` | 用户级：按项目保存的工具和目录审批 | CLI 自动管理 |

### 配置目录结构

默认配置目录为 `~/.copilot`（即 `$HOME/.copilot`），其中存储了所有配置、会话历史、日志和自定义内容：

=== "macOS / Linux"

    ```text
    ~/.copilot/
    ├── config.json                  # 自动管理的内部状态
    ├── settings.json                # 用户可编辑的全局设置
    ├── copilot-instructions.md      # 全局自定义指令
    ├── permissions-config.json      # 保存的权限审批记录
    ├── mcp-config.json              # 用户级 MCP 服务器定义
    ├── lsp-config.json              # 用户级 LSP 服务器定义
    ├── agents/                      # 个人自定义 Agent 定义
    ├── skills/                      # 个人自定义 Skill 定义
    ├── hooks/                       # 用户级 Hook 脚本
    ├── instructions/                # 额外的 *.instructions.md 文件
    ├── session-state/               # 会话历史数据
    ├── logs/                        # 会话日志
    └── installed-plugins/           # 已安装的插件
    ```

=== "Windows"

    ```text
    %USERPROFILE%\.copilot\
    ├── config.json
    ├── settings.json
    ├── copilot-instructions.md
    ├── permissions-config.json
    └── ...
    ```

!!! tip "COPILOT_HOME 环境变量"

    通过设置 `COPILOT_HOME` 环境变量可以改变配置目录的位置：

    ```bash
    export COPILOT_HOME=/path/to/my/copilot-config
    ```

    注意：更换目录后，旧目录中的配置、会话历史和已保存的权限不会自动迁移到新位置。如需保留，需手动复制或移动。

!!! warning "不要手动编辑自动管理的文件"

    `config.json` 和 `permissions-config.json` 由 CLI 自动管理。`config.json` 存储认证数据、插件元数据等内部运行时信息，手动修改可能导致异常。如需重置权限，可以删除 `permissions-config.json` 中对应项目的条目，但**不要在会话运行期间编辑**。

### 配置优先级

配置的优先级从高到低为：**命令行选项 > 环境变量 > 本地设置 > 仓库设置 > 用户设置**。更具体的范围总是覆盖更通用的范围。

---

## 🛡️ 信任目录管理

信任目录（Trusted Directories）控制 Copilot CLI 可以在哪些位置读取、修改和执行文件。这就像给 Copilot 划定"工作区域"——只有被信任的目录内的文件，它才有权接触。

### 单次信任 vs 永久信任

启动 Copilot CLI 时，系统会询问你是否信任当前目录。你有两个选择：

| 选项 | 效果 | 适用场景 |
|------|------|---------|
| 仅当前会话 | 本次会话结束后，下次启动仍会询问 | 临时查看不熟悉的代码仓库 |
| 当前及未来会话 | 信任记录写入配置，不再重复询问 | 你确信该位置始终安全的常用项目 |

!!! warning "安全考量"

    - 只应在你信任的目录中启动 Copilot CLI。不要在可能包含不可信可执行文件的目录中使用。
    - 如果你启动 CLI 的目录包含敏感或机密数据，或者你不希望被修改的文件，这些文件可能会面临风险。
    - **不要从家目录（Home）启动 Copilot CLI**——这会暴露整个用户目录。

!!! info "权限范围是启发式的"

    GitHub 不保证所有受信任目录之外的文件都会受到保护。权限的限定是启发式的（heuristic），这意味着它基于合理的规则来推断，但并非严格的沙箱隔离。

### 编辑信任目录

永久信任的目录存储在 `config.json` 的 `trustedFolders` 数组中。你可以手动编辑这个数组：

=== "macOS / Linux"

    ```bash
    # 打开配置文件
    vi ~/.copilot/config.json
    ```

=== "Windows"

    ```bash
    # 打开配置文件
    vi $HOME/.copilot/config.json
    ```

编辑 `trustedFolders` 数组，添加或移除目录路径即可。

---

## 🔧 工具权限系统

Copilot CLI 使用多种工具来完成你的任务：执行 Shell 命令、读写文件、搜索代码、获取网页内容、调用 MCP 服务器等。只读操作（如搜索、读取文件）默认自动允许，而可能修改系统的操作（如执行 Shell 命令、编辑文件、访问 URL）则需要你的明确审批。

### 交互式审批

当 Copilot 首次需要使用某个可能需要审批的工具时（例如 `touch`、`chmod`、`node`、`sed`），会弹出审批提示，提供三个选项：

| 选项 | 行为 | 注意事项 |
|------|------|---------|
| **Yes** | 仅允许本次运行该特定命令 | 下次使用同一工具时仍会询问 |
| **Yes, and approve for session** | 允许该工具在当前会话中自由使用 | 恢复会话后需重新审批 |
| **No** | 取消命令，让 Copilot 换一种方式 | 安全第一的选择 |

!!! warning "会话级审批的潜在风险"

    如果你批准 Copilot 运行 `rm ./this-file.txt` 并选择了会话级审批，那么 Copilot 在当前会话中可以运行**任何** `rm` 命令（例如 `rm -rf ./*`），而无需再次询问。请谨慎使用会话级审批。

### 命令行权限控制

你可以通过命令行选项在启动时就指定工具权限，避免运行时反复确认。

| 选项 | 作用 |
|------|------|
| `--allow-tool=TOOL` | 允许特定工具免审批 |
| `--deny-tool=TOOL` | 禁止使用特定工具 |
| `--allow-all-tools` | 允许所有工具免审批 |
| `--available-tools=TOOLS` | 仅暴露指定工具给 AI 模型 |
| `--excluded-tools=TOOLS` | 从可用工具中排除指定工具 |

!!! tip "deny 优先于 allow"

    `--deny-tool` 的优先级始终高于 `--allow-tool` 和 `--allow-all-tools`。即使设置了 `--allow-all`，deny 规则仍然生效。这就像"黑名单"比"白名单"更有否决权。

### 工具模式匹配语法

`--allow-tool` 和 `--deny-tool` 的值使用 `Kind(argument)` 格式。`argument` 是可选的——省略时匹配该类型下的所有工具。

| Kind | 说明 | 示例 |
|------|------|------|
| `shell` | Shell 命令执行 | `shell(git push)`、`shell(git:*)`、`shell` |
| `write` | 文件创建或修改 | `write`、`write(.env)` |
| `read` | 文件或目录读取 | `read`、`read(.env)` |
| `memory` | Agent 记忆存储 | `memory` |
| `url` | URL 访问（web_fetch 或 Shell） | `url(github.com)`、`url(https://*.api.com)` |
| `SERVER-NAME` | MCP 服务器工具 | `MyMCP(create_issue)`、`MyMCP` |

!!! info "Shell 通配符语法"

    对于 `shell` 规则，`:*` 后缀匹配命令词干后跟空格，可以防止部分匹配。例如 `shell(git:*)` 匹配 `git push` 和 `git pull`，但**不**匹配 `gitea`。对于 `git` 和 `gh` 命令，可以指定一级子命令来精确控制。

### 常见权限配置示例

```bash
# 允许所有工具免审批（适合 CI/CD 等自动化场景）
copilot -p "任务描述" --allow-all-tools

# 禁止 rm 和 git push，但允许其他所有工具
copilot --allow-all-tools --deny-tool='shell(rm)' --deny-tool='shell(git push)'

# 允许所有 git 命令但禁止 git push
copilot --allow-tool='shell(git:*)' --deny-tool='shell(git push)'

# 允许所有 Shell 命令
copilot --allow-tool='shell'

# 允许编辑文件但禁止写操作中的特定文件
copilot --allow-tool='write' --deny-tool='write(.env)'

# 禁止特定 MCP 服务器工具
copilot --deny-tool='My-MCP-Server(tool_name)'

# 允许 MCP 服务器的所有工具，但排除其中一个
copilot --allow-tool='My-MCP-Server' --deny-tool='My-MCP-Server(tool_name)'
```

### 限制可用工具集

`--available-tools` 和 `--excluded-tools` 从"工具认知"层面控制 AI 模型能看到哪些工具：

```bash
# 只让 Copilot 知道 bash、edit、view、grep、glob 这几个工具
copilot --available-tools='bash,edit,view,grep,glob' --allow-tool='shell(git:*)' --deny-tool='shell(git push)'
```

如果同时使用两个选项，`--available-tools` 生效而 `--excluded-tools` 被忽略。被排除的工具，AI 模型根本无法选择使用，即使你用 `--allow-tool` 指定了它。

!!! tip "会话内重置权限"

    在交互式会话中，可以使用 `/reset-allowed-tools` 斜杠命令撤销当前会话中授予的所有权限。重置后会回到启动时通过命令行选项定义的状态。

---

## 📁 路径权限

路径权限控制 Copilot CLI 可以访问哪些目录和文件。这就像给 Copilot 规定"只能在哪些架子上取东西"。

### 默认范围

默认情况下，Copilot CLI 可以访问以下位置：

| 范围 | 说明 |
|------|------|
| 当前工作目录 | 启动 CLI 时所在的目录 |
| 当前目录的所有子目录 | 递归向下 |
| 系统临时目录 | 用于创建临时文件 |

路径权限应用于 Shell 命令、文件操作（创建、编辑、查看）以及搜索工具（`grep`、`glob`）。对于 Shell 命令，路径通过分词命令文本并识别类似路径的 token 来启发式提取。

!!! warning "路径检测的限制"

    - 嵌入在复杂 Shell 构造中的路径可能无法被检测到
    - 只有特定环境变量会被展开（`HOME`、`TMPDIR`、`PWD` 等），自定义变量如 `$MY_PROJECT_DIR` 不会被展开，可能导致验证失败
    - 符号链接（symlink）对已存在的文件会解析，但对正在创建的文件不会解析

### 扩展路径权限

| 选项 | 作用 |
|------|------|
| `--allow-all-paths` | 禁用路径验证，允许访问任意路径 |
| `--disallow-temp-dir` | 禁止访问系统临时目录 |
| `--add-dir=PATH` | 添加额外允许的目录（可多次使用） |

```bash
# 允许访问任意路径
copilot -p "任务描述" --allow-all-paths

# 禁止使用临时目录
copilot -p "任务描述" --disallow-temp-dir

# 添加额外的可访问目录
copilot -p "任务描述" --add-dir=/opt/my-project --add-dir=/data/shared
```

---

## 🌐 URL 权限

URL 权限控制 Copilot CLI 可以访问哪些外部 URL。这就像控制 Copilot 能拨打哪些外部电话——默认所有电话都需要你的批准。

### 默认行为

默认情况下，所有 URL 在被访问之前都需要你的审批。URL 权限应用于 `web_fetch` 工具以及网络相关的 Shell 命令（如 `curl`、`wget`、`fetch`）。

!!! warning "URL 检测的限制"

    - Shell 命令读取的文件内容、配置文件或环境变量中的 URL 不会被检测到
    - 混淆的 URL（如拆分的字符串或转义序列）可能无法被检测到
    - **HTTP 和 HTTPS 被视为不同协议**，需要分别审批

### 配置 URL 权限

| 选项 | 作用 |
|------|------|
| `--allow-all-urls` | 禁用 URL 验证，允许访问所有 URL |
| `--allow-url=DOMAIN` | 预批准特定域名（可多次使用） |
| `--deny-url=DOMAIN` | 拒绝特定域名（可多次使用） |

```bash
# 允许访问所有 URL
copilot -p "任务描述" --allow-all-urls

# 预批准特定域名
copilot -p "任务描述" --allow-url=github.com --allow-url=docs.github.com

# 拒绝特定域名（优先级高于 allow）
copilot -p "任务描述" --allow-url=example.com --deny-url=evil.example.com
```

!!! tip "URL 权限也可在 settings.json 中配置"

    你可以在 `~/.copilot/settings.json` 中配置 `allowedUrls` 和 `deniedUrls` 数组来持久化 URL 权限：

    ```json
    {
      "allowedUrls": ["github.com", "docs.github.com"],
      "deniedUrls": ["evil.example.com"]
    }
    ```

    支持精确 URL、域名模式和通配符子域名（如 `"*.github.com"`）。deny 规则优先于 allow 规则。

---

## 🔓 组合权限

如果你希望在三个维度（工具、路径、URL）上同时放开所有限制，可以使用组合权限选项。

### --allow-all / --yolo

```bash
# 两种写法等价
copilot -p "任务描述" --allow-all
copilot -p "任务描述" --yolo
```

`--allow-all`（别名 `--yolo`）等价于同时设置以下三个选项：

| 组合项 | 等价于 |
|--------|--------|
| 工具维度 | `--allow-all-tools`（跳过工具审批） |
| 路径维度 | `--allow-all-paths`（禁用路径验证） |
| URL 维度 | `--allow-all-urls`（禁用 URL 验证） |

!!! tip "会话内启用"

    在交互式会话中，你也可以随时使用 `/allow-all` 或 `/yolo` 斜杠命令来启用所有权限，无需重启会话。支持 `on`/`off`/`show` 子命令。

!!! warning "强烈建议仅在隔离环境中使用"

    GitHub 官方强烈建议只在隔离环境（如虚拟机、容器或专用系统）中使用 `--allow-all` / `--yolo`。**不要**用 alias 将这个选项设为每次启动的默认行为——这意味着 Copilot 每次都能在未经你审查的情况下执行任何操作，可能导致数据丢失或其他安全问题。

---

## 💾 权限持久化

从 1.0.37 版本起，权限持久化默认启用。这就像 Copilot 会记住你之前给过的"通行证"——下次在同一项目中启动时，不需要重复审批。

### 工作原理

当你在某个目录下审批了工具权限后，该审批会自动保存到 `~/.copilot/permissions-config.json` 中。保存的审批按项目路径组织，后续在同一目录启动 Copilot 时，之前批准过的权限会自动生效，不再弹出确认提示。

### 重置权限

如果你需要重新审批权限，有以下方式：

| 方式 | 命令 | 效果 |
|------|------|------|
| 重置当前会话权限 | `/reset-allowed-tools` | 撤销当前会话中授予的所有权限，回到启动时的状态 |
| 重置特定项目权限 | 删除 `permissions-config.json` 中对应条目 | 下次在该项目启动时重新审批 |
| 重置所有项目权限 | 删除整个 `permissions-config.json` | 所有项目的权限记录被清除 |

!!! warning "编辑权限文件的风险"

    不要在会话运行期间编辑 `permissions-config.json`，这可能导致不可预期的行为。如果需要重置，建议先退出所有 Copilot 会话，再编辑或删除文件。
