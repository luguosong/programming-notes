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

### 更新

``` bash
# npm 更新
npm update -g @github/copilot

# 检查当前版本
copilot --version
```

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
| ++ctrl+o++ | 展开时间线所有条目（1.0.26 新增） |
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
| `/ask` | 快速提问，不影响当前对话历史（1.0.27 新增） |
| `/statusline` | 自定义状态栏显示内容（1.0.30 新增） |

!!! tip "用量配额提醒"

    从 1.0.32 起，Copilot CLI 会在你接近每周用量配额的 75% 和 90% 时主动显示警告，避免被突然限流打断工作。配合 `--session-idle-timeout` 配置（1.0.32 新增）控制空闲超时，可以更精细地管理订阅配额。

### 模式与权限

| 命令 | 功能 |
|------|------|
| `/plan` | 进入计划模式 |
| `/allow-all` | 启用所有权限（工具、路径和 URL），支持 `on`/`off`/`show` 子命令（1.0.12 新增） |
| `/experimental` | 启用实验性功能（如 Autopilot） |

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
