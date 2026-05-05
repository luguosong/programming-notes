---
title: 安装与快速入门
description: 从安装认证到快捷键命令，快速上手 GitHub Copilot CLI
---

# 安装与快速入门

**本文你会学到**：

- 📦 如何安装和认证 Copilot CLI
- 💻 交互式会话的基本操作（启动、退出、快捷键）
- ✨ `@` 和 `/` 等输入前缀的含义
- ⌨️ 最常用的斜杠命令

如果你已经有使用经验，可以直接跳到需要的部分。

---

## 📦 安装

=== "npm（推荐）"

    ``` bash
    # 安装稳定版
    npm install -g @github/copilot

    # 安装预发布版本（获取最新功能）
    npm install -g @github/copilot@prerelease
    ```

=== "Homebrew（macOS/Linux）"

    ``` bash
    brew install gh-copilot
    ```

=== "WinGet（Windows）"

    ``` bash
    winget install GitHub.CopilotCLI
    ```

=== "一键安装脚本"

    ``` bash
    # macOS / Linux
    curl -fsSL https://cli.github.com/copilot/install.sh | sh

    # Windows（PowerShell）
    irm https://cli.github.com/copilot/install.ps1 | iex
    ```

### 安装补充

除了上述包管理器，Copilot CLI 还提供以下安装方式：

=== "GitHub Releases 下载"

    直接从 [copilot-cli Releases](https://github.com/github/copilot-cli/releases/) 下载对应平台的可执行文件，解压后即可运行，无需安装 Node.js 或其他依赖。

=== "安装脚本进阶用法"

    一键安装脚本支持通过环境变量自定义行为：

    ``` bash
    # VERSION 指定安装版本，PREFIX 自定义安装目录
    curl -fsSL https://gh.io/copilot-install | VERSION="v0.0.369" PREFIX="$HOME/custom" bash

    # 以 root 身份安装到 /usr/local/bin
    curl -fsSL https://gh.io/copilot-install | sudo bash
    ```

    - `VERSION`：指定安装版本，默认为最新版本
    - `PREFIX`：自定义安装目录，默认为 `/usr/local`（root 用户）或 `$HOME/.local`（非 root 用户）

!!! warning "Windows PowerShell 版本要求"

    Windows 下使用 WinGet 或一键安装脚本（PowerShell）需要 **PowerShell v6 或更高版本**。可通过 `$PSVersionTable.PSVersion` 检查当前版本。

!!! tip "npm ignore-scripts 配置"

    如果你的 `~/.npmrc` 中设置了 `ignore-scripts=true`，需要临时覆盖该配置才能正常安装：

    ``` bash
    npm_config_ignore_scripts=false npm install -g @github/copilot
    ```

### 更新

``` bash
# npm 更新
npm update -g @github/copilot

# 检查当前版本
copilot --version
```

### Shell 补全

1.0.37 新增 `copilot completion` 子命令，可生成静态 Shell 补全脚本，让你在终端中输入 `copilot` 命令时自动提示子命令和参数：

``` bash
# 生成 Bash 补全脚本
copilot completion bash

# 生成 Zsh 补全脚本
copilot completion zsh

# 生成 Fish 补全脚本
copilot completion fish
```

生成的脚本需要写入 Shell 的补全目录（如 Bash 的 `/etc/bash_completion.d/`），或通过 `source` 加载到当前会话中。

---

## 🔑 认证

首次使用需要通过 GitHub 账号认证。这就像给你的实习生办工牌——没有工牌他进不了办公室。

``` bash
# 启动认证流程（会在浏览器中打开 GitHub 授权页面）
copilot auth

# 验证认证状态
copilot auth status
```

!!! tip "前提条件"

    需要一个启用了 GitHub Copilot 的 GitHub 账号（个人、Business 或 Enterprise 订阅均可）。

!!! info "环境变量补充"

    - `COPILOT_DISABLE_TERMINAL_TITLE=1`（1.0.28 新增）：设置后 Copilot CLI 不再修改终端标题栏。适用于终端管理工具自行控制标题的场景，避免 Copilot 覆盖你的自定义标题。
    - `COPILOT_GH_HOST`（1.0.35 新增）：指定 GitHub 主机名，优先于 `GH_HOST`。适合 GitHub Enterprise 用户需要固定连接特定 GitHub 实例的场景。

### 认证方式详解

Copilot CLI 支持三种认证方式，按优先级从高到低排列：

**OAuth 设备流（默认，推荐交互式使用）**

在 CLI 中执行 `/login` 或在终端执行 `copilot login`，CLI 生成一次性验证码并自动复制到剪贴板、打开浏览器：

```text
Waiting for authorization...
Enter one-time code: 1234-5678 at https://github.com/login/device
Press any key to copy to clipboard and open browser...
```

1. 在浏览器中访问 `https://github.com/login/device`
2. 输入终端显示的一次性验证码
3. 如组织使用 SAML SSO，点击对应组织的 **Authorize**
4. 审查权限请求后点击 **Authorize GitHub Copilot CLI**
5. 返回终端，CLI 显示 `Signed in successfully as <username>`

!!! tip "GitHub Enterprise Cloud 用户"

    使用 GitHub Enterprise Cloud 时，可在登录时指定主机名：

    ``` bash
    copilot login --host HOSTNAME
    ```

**环境变量（推荐 CI/CD 和非交互式环境）**

通过环境变量传入支持的 Token，CLI 按以下优先级查找：

| 优先级 | 环境变量 | 说明 |
|--------|---------|------|
| 最高 | `COPILOT_GITHUB_TOKEN` | Copilot CLI 专用 |
| 中 | `GH_TOKEN` | GitHub CLI 通用 |
| 最低 | `GITHUB_TOKEN` | GitHub 通用 |

支持 Fine-grained PAT（`github_pat_` 前缀）和 GitHub App user-to-server Token（`ghu_` 前缀）。Fine-grained PAT 需要在个人账户下创建（不能选择组织），并开启 **Account → Copilot Requests** 权限。

!!! warning "环境变量会静默覆盖已存储的 OAuth Token"

    如果你为其他工具设置了 `GH_TOKEN`，Copilot CLI 会优先使用该 Token 而非 `copilot login` 存储的 OAuth Token。建议仅设置你希望 CLI 使用的环境变量。

**GitHub CLI 回退认证**

如果已安装并认证了 GitHub CLI（`gh`），Copilot CLI 在未找到其他凭据时会自动使用 `gh auth token` 作为回退。这是最低优先级的认证方式。

### 多账户管理

Copilot CLI 支持同时登录多个账户并自由切换：

``` bash
# 在 CLI 内查看当前账户
/user

# 列出所有已登录账户
/user list

# 切换到其他账户
/user switch
```

添加新账户可通过在终端执行 `copilot login` 或在 CLI 内执行 `/login` 并使用另一个账户授权。CLI 会记住最后使用的账户，下次启动时自动切换。

### Token 存储

认证成功后，Token 默认存储在操作系统的安全存储中：

| 平台 | 存储方式 | 服务名称 |
|------|---------|---------|
| macOS | Keychain Access | `copilot-cli` |
| Windows | Credential Manager | `copilot-cli` |
| Linux | libsecret（GNOME Keyring / KWallet） | `copilot-cli` |

如果系统密钥链不可用（如无 `libsecret` 的无头 Linux 服务器），CLI 会提示将 Token 存储到明文文件 `~/.copilot/config.json`。

| Token 类型 | 前缀 | 是否支持 |
|-----------|------|---------|
| OAuth Token（设备流） | `gho_` | 支持 |
| Fine-grained PAT | `github_pat_` | 支持 |
| GitHub App user-to-server | `ghu_` | 支持 |
| Classic PAT | `ghp_` | 不支持 |

注销和撤销：

- `/logout`：移除本地存储的 Token，但不会在 GitHub 端撤销
- 彻底撤销：前往 GitHub **Settings → Applications → Authorized OAuth Apps**，找到 Copilot CLI 并点击 **Revoke**

### 配置目录

Copilot CLI 的配置文件存放在以下目录：

- **macOS / Linux**：`~/.copilot/`
- **Windows**：`$HOME\.copilot\`

可通过 `COPILOT_HOME` 环境变量改变配置目录的位置。

### 未认证使用

如果使用自带 LLM API Key（BYOK 模式），GitHub 认证**不是必需的**——Copilot CLI 可以直接连接你配置的 LLM 提供商，无需 GitHub 账号或 Token。

但未认证时以下功能不可用：`/delegate`（需要 Copilot 云端 Agent）、GitHub MCP 服务器、GitHub Code Search。

你也可以混合使用 BYOK 和 GitHub 认证，同时享受自定义模型和 GitHub 托管功能。

!!! info "离线模式"

    设置 `COPILOT_OFFLINE=true` 后，Copilot CLI 不会尝试联系 GitHub 服务器，遥测完全禁用，仅向 BYOK 提供商发送请求。详见 BYOK 笔记。

---

## 💻 启动与退出

安装完成后，只需在终端输入 `copilot` 即可启动交互式会话——就像打开一个聊天窗口，你和 AI 之间通过自然语言对话来完成任务。

``` bash
# 启动交互式会话
copilot

# 启动并恢复上次会话
copilot --resume

# 非交互式执行单条指令（Programmatic 模式）
copilot -p "解释这段代码的作用"

# 退出会话
/exit
```

---

## ⌨️ 快捷键

熟练使用快捷键能大幅提升交互效率。++shift+tab++ 是最常用的——它让你在不同模式间快速切换，后面「交互模式」会详细介绍。

| 快捷键 | 功能 |
|--------|------|
| ++shift+tab++ | 循环切换交互模式（Interactive → Plan → Autopilot） |
| ++esc++ | 取消当前操作（双击打开 /rewind 时间线） |
| ++ctrl+c++ | 若在思考中：清空输入；否则退出 |
| ++ctrl+l++ | 清屏 |
| ++ctrl+r++ | 反向增量搜索命令历史（0.0.422 新增） |
| ++ctrl+y++ | 在编辑器中打开计划文件（Plan 模式） |
| ++ctrl+q++ / ++ctrl+enter++ | 排队发送消息（1.0.15 调整） |
| ++ctrl+v++ / ++meta+v++ | 从剪贴板粘贴图片（1.0.30 新增） |
| ++ctrl+y++ | 接受补全弹窗中的高亮选项（`@` 提及、路径补全、斜杠命令），除 ++tab++ 外的另一种确认方式（1.0.35 新增） |
| ++ctrl+o++ | 展开时间线所有条目（1.0.26 新增） |
| ++ctrl+x++ → `B` | 将正在运行的任务或 Shell 命令移至后台继续执行（1.0.39 新增） |
| ++up++ / ++down++ | 导航命令历史 |

---

## ✨ 输入前缀

在 Copilot CLI 中，输入内容的第一个字符决定了它的含义——这就像命令行的"万能遥控器"，不同按键触发不同功能。

| 前缀 | 功能 | 示例 |
|------|------|------|
| `@` | 引用文件或目录作为上下文 | `@src/main.py 解释这段代码` |
| `/` | 执行斜杠命令 | `/help` |
| `?` | 显示分类帮助 | `?commands` |
| `&` | 快速委派任务到云端 Agent | `& 完成集成测试` |

---

## 📋 常用斜杠命令速查

斜杠命令（以 `/` 开头）是 Copilot CLI 的内置指令，类似于聊天应用中的"快捷操作按钮"。以下是按使用频率分组的最常用命令。

斜杠命令现在支持参数和子命令的 ++tab++ 补全（1.0.35 新增），输入 `/` 后按 ++tab++ 可查看可用参数，减少记忆负担。1.0.39 进一步改进了参数选择器的响应速度——当你输入到精确的命令边界时，选择器会立即弹出，不再需要额外输入尾随空格。

!!! tip "不需要全部记住"

    只需记住 `/help` 和 `/clear` 两个命令即可上手，其余命令在需要时通过 `/help` 或 `?` 查询。

### 通用命令

| 命令 | 功能 |
|------|------|
| `/help` | 查看所有快捷方式和可用命令 |
| `/clear` | 放弃当前会话 |
| `/new` | 开始新对话（1.0.11 新增） |
| `/exit` | 退出 Copilot CLI |
| `/compact` | 压缩上下文，释放 token 空间 |
| `/session` | 查看当前会话信息 |
| `/version` | 显示 CLI 版本并检查更新（1.0.5 新增） |
| `/restart` | 热重启 CLI 同时保留会话（1.0.3 新增） |
| `/copy` | 复制最近的 AI 回复到剪贴板（0.0.422 新增），Windows 上写入格式化 HTML（1.0.10 改进） |
| `/undo` | 撤销最后一轮对话和文件更改（1.0.10 新增） |
| `/rewind` | 时间线回滚到任意节点（1.0.13 新增） |
| `/share html` | 导出会话为交互式 HTML（1.0.15 新增） |
| `/changelog` | 查看更新日志，支持 `last N`、`since <ver>`、`summarize`（1.0.5 新增） |
| `/ask` | 快速提问，不影响当前对话历史（1.0.27 新增），响应现在渲染 Markdown（包括表格和格式化链接）（1.0.37 改进） |
| `/statusline` | 自定义状态栏显示内容（1.0.30 新增） |

!!! tip "用量配额提醒"

    从 1.0.32 起，Copilot CLI 会在你接近每周用量配额的 75% 和 90% 时主动显示警告，避免被突然限流打断工作。配合 `--session-idle-timeout` 配置（1.0.32 新增）控制空闲超时，可以更精细地管理订阅配额。

### 模式与权限

| 命令 | 功能 |
|------|------|
| `/plan` | 进入计划模式 |
| `/allow-all` | 启用所有权限（工具、路径和 URL），支持 `on`/`off`/`show` 子命令（1.0.12 新增） |
| `/experimental` | 启用实验性功能（如 Autopilot） |

!!! tip "权限持久化（1.0.37 新增）"

    权限持久化现在默认启用。当你在某个目录下审批了工具权限后，该审批会自动保留到后续会话——下次在同一目录启动 Copilot 时，之前批准过的权限不需要再次确认。这避免了每次会话都要重复审批相同操作的麻烦。

### 开发相关

| 命令 | 功能 |
|------|------|
| `/review` | 审查代码变更 |
| `/diff` | 查看当前工作区的 diff |
| `/pr` | 创建或查看 Pull Request |
| `/research` | 对主题进行深入研究 |

### 扩展管理

| 命令 | 功能 |
|------|------|
| `/agent` | 选择并激活 Agent |
| `/skills list` | 列出可用的 Skills |
| `/mcp show` | 列出已配置的 MCP 服务器 |
| `/plugin list` | 列出已安装的插件 |
| `/extensions` | 查看、启用和禁用 Extensions（1.0.5 新增） |

---

## ✅ 验证安装

安装完成后，运行以下命令验证一切正常：

``` bash
# 检查版本
copilot --version

# 检查认证状态
copilot auth status

# 启动一次简单对话测试
copilot -p "你好，请用一句话介绍你自己"
```

如果都能正确输出，说明安装和认证成功。
