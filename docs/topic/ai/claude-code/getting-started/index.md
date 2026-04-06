---
title: 快速入门
description: 从安装到第一次使用 Claude Code，掌握交互模式和常用命令
---

# 快速入门

**本文你会学到**：

- 🎯 如何在各种操作系统上安装 Claude Code（重点介绍 Windows）
- 🔧 首次登录、认证与基本配置
- ⚡ 交互模式（REPL）的核心操作：斜杠命令、快捷键、多行输入
- 📋 CLI 命令速查：交互模式与非交互模式的常用写法

## 📥 怎么安装 Claude Code？

### 什么是 Claude Code？

打个比方：如果你用过 ChatGPT 的网页版，那 Claude Code 就是把「和 AI 对话」这件事搬到了终端（Terminal）里——但它不只是聊天，它还能直接读写你的项目文件、执行命令、操作 Git。你可以把它理解为一个**住在终端里的 AI 编程助手**。

### 系统要求

在安装之前，先确认你的环境满足以下条件：

| 项目 | 最低要求 |
|------|---------|
| 操作系统 | Windows 10 1809+、macOS 13.0+、Ubuntu 20.04+、Debian 10+ |
| 内存 | 4 GB+ RAM |
| 网络 | 需要互联网连接 |
| Shell | Bash、Zsh、PowerShell 或 CMD |
| Windows 额外依赖 | Git for Windows（必须） |
| 账户 | Claude Pro/Max/Team/Enterprise 或 Claude Console（免费版不支持） |

### 安装方式

Claude Code 提供三种安装方式。对于大多数开发者来说，**原生安装**最简单——它自动更新，不需要 Node.js 等额外依赖。

=== "Windows PowerShell（推荐）"

``` powershell
irm https://claude.ai/install.ps1 | iex
```

=== "Windows CMD"

``` cmd
curl -fsSL https://claude.ai/install.cmd -o install.cmd && install.cmd && del install.cmd
```

=== "WinGet"

``` powershell
winget install Anthropic.ClaudeCode
```

=== "macOS / Linux / WSL"

``` bash
curl -fsSL https://claude.ai/install.sh | bash
```

=== "Homebrew"

``` bash
brew install --cask claude-code
```

⚠️ 注意：如果你在 PowerShell 中执行 CMD 版的命令，会看到 `The token '&&' is not a valid statement separator` 错误——这是因为 PowerShell 不支持 `&&`。请改用 PowerShell 版的命令。判断方法很简单：命令行提示符显示 `PS C:\` 就说明你在 PowerShell 中。

#### Windows 上的两个选项

Claude Code 在 Windows 上有两种运行方式：

- **原生 Windows + Git Bash**：安装 Git for Windows 后，直接从 PowerShell 或 CMD 启动。Claude Code 内部通过 Git Bash 来执行 shell 命令。不需要管理员权限。
- **WSL**：如果你已经习惯使用 WSL（Windows Subsystem for Linux），Claude Code 在 WSL 1 和 WSL 2 中都可以运行。推荐 WSL 2，因为它支持沙箱（Sandbox）安全特性。

💡 如果 Claude Code 找不到 Git Bash，可以在 `settings.json` 中手动指定路径：

``` json
{
  "env": {
    "CLAUDE_CODE_GIT_BASH_PATH": "C:\\Program Files\\Git\\bin\\bash.exe"
  }
}
```

### 首次登录与认证

安装完成后，在终端输入 `claude` 就会启动交互模式。第一次使用时，系统会引导你登录。

``` bash
claude
# 首次启动会自动弹出浏览器进行登录
```

支持的账户类型：

| 账户类型 | 说明 |
|---------|------|
| Claude Pro / Max / Team / Enterprise | 推荐，订阅制 |
| Claude Console | API 访问，按量计费，首次登录自动创建工作空间 |
| Amazon Bedrock / Google Vertex AI / Microsoft Foundry | 企业云服务商 |

登录成功后，凭证会自动保存，后续不需要重复登录。如果需要切换账户，在交互模式中输入 `/login` 即可。

你也可以通过命令行预先认证：

``` bash
claude auth login              # 标准登录
claude auth login --console    # 使用 API Console 登录
claude auth login --sso        # 强制 SSO 认证
claude auth status --text      # 查看当前登录状态
```

### 更新与卸载

原生安装方式会**自动在后台更新**——Claude Code 启动时和运行期间都会检查新版本。你也可以手动触发更新：

``` bash
claude update    # 立即更新到最新版
```

如果需要卸载：

``` powershell
# Windows PowerShell
Remove-Item -Path "$env:USERPROFILE\.local\bin\claude.exe" -Force
Remove-Item -Path "$env:USERPROFILE\.local\share\claude" -Recurse -Force
```

## 💬 怎么跟 Claude Code 对话？——交互模式

### REPL 对话模式——像聊天一样写代码

在项目目录下输入 `claude`，你就进入了**交互模式**（也叫 REPL 模式）。这个名字来源于编程中的一个经典概念：**Read-Eval-Print Loop**（读取-求值-打印-循环）。你输入一句话，Claude 思考后回复，如此往复。

``` bash
cd /path/to/your/project
claude
```

进入后，你会看到欢迎界面，显示当前会话信息、最近对话和更新日志。现在可以直接用自然语言和 Claude 对话了。

💡 Claude Code 会根据你项目的 Git 历史自动生成**输入建议**（灰色提示文字）。按 `Tab` 或 `右方向键` 接受建议，也可以直接开始输入自己的内容。

#### 第一次对话：了解你的项目

刚进入一个项目时，可以让 Claude 先帮你「摸底」：

```
这个项目是做什么的？
```

```
用了哪些技术栈？
```

```
主入口文件在哪里？
```

Claude 会分析你的项目文件，给出有针对性的回答。

#### 第一次改代码

接下来让 Claude 做点实际的事：

```
在主文件里添加一个 hello world 函数
```

Claude Code 的执行流程是这样的：

1. 定位合适的文件
2. 展示拟修改的内容（Diff 视图）
3. 等待你确认（按 `y` 接受，或按 `n` 拒绝）
4. 应用修改

⚠️ 这就是 Claude Code 和普通 AI 聊天的核心区别——它不只是「说」，还会「做」，但每次操作都会征求你的同意。

#### Git 操作也能用自然语言

```
我改了哪些文件？
```

```
帮我提交代码，写个描述性的 commit message
```

```
创建一个叫 feature/login 的分支
```

### 快速命令前缀

除了自然语言对话，Claude Code 还支持几个特殊的前缀字符来快速切换模式：

| 前缀 | 作用 | 示例 |
|------|------|------|
| `/` | 触发斜杠命令或自定义 Skill | `/help`、`/clear` |
| `!` | 直接执行 Bash 命令，结果加入上下文 | `! git status`、`! npm test` |
| `@` | 文件路径提及，触发自动补全 | `@src/main.java` |

`!` 前缀特别实用——当你只想快速跑一条 shell 命令，但又希望结果留在对话上下文中时，直接 `! 命令` 即可，不需要 Claude 介入解释和审批。

#### 多行输入

写代码时经常需要输入多行内容。Claude Code 提供了几种方式：

| 方式 | 快捷键 | 适用场景 |
|------|--------|---------|
| 反斜杠换行 | `\` + `Enter` | 所有终端通用 |
| Ctrl+J | `Ctrl+J` | 发送换行符，所有终端通用 |
| Shift+Enter | `Shift+Enter` | iTerm2、WezTerm、Ghostty、Kitty 开箱即用 |
| 直接粘贴 | 粘贴多行文本 | 代码块、日志 |

💡 在 Claude Code 内部运行 `/terminal-setup` 可以自动为 VS Code、Alacritty、Zed 等终端配置 `Shift+Enter`。

### 常用斜杠命令

在交互模式中输入 `/` 就能看到所有可用命令。以下是最常用的：

| 命令 | 作用 |
|------|------|
| `/help` | 显示帮助信息 |
| `/clear` | 清除当前对话历史（之前的会话仍可恢复） |
| `/config` | 打开配置面板（主题、更新通道、Vim 模式等） |
| `/login` | 切换账户 |
| `/resume` | 恢复之前的会话 |
| `/compact` | 压缩对话上下文（上下文太长时使用） |
| `/vim` | 开启 Vim 编辑模式 |
| `/terminal-setup` | 自动配置终端的 `Shift+Enter` |
| `/theme` | 切换界面主题 |
| `/btw` | 问一个不影响主对话的快速问题 |

📌 `/btw` 是一个很巧妙的设计：它能看到当前对话的完整上下文，但回答后不会写入对话历史。适合在 Claude 正在忙的时候插一句「刚才那个配置文件叫什么来着？」。

#### 会话恢复

Claude Code 的对话是**按项目目录**保存的。你可以随时恢复之前的对话：

``` bash
claude -c                  # 继续当前目录最近一次对话
claude -r                  # 交互式选择要恢复的会话
claude -r "auth-refactor"  # 恢复名为 "auth-refactor" 的会话
claude -n "my-feature"     # 给当前会话命名，方便以后恢复
```

使用 `/clear` 清除当前对话后，之前的对话仍然保存在磁盘上，随时可以恢复。

### 键盘快捷键

掌握快捷键能大幅提升使用效率。以下是按使用频率整理的核心快捷键：

#### 通用控制

| 快捷键 | 作用 |
|--------|------|
| `Ctrl+C` | 取消当前输入或中断生成 |
| `Ctrl+D` | 退出 Claude Code |
| `Ctrl+L` | 重绘屏幕（画面乱了的时候用） |
| `Ctrl+R` | 反向搜索命令历史 |
| `Ctrl+O` | 切换详细输出（Transcript 查看器） |
| `Ctrl+T` | 切换任务列表显示 |
| `Esc` + `Esc` | 回退代码或总结对话 |
| `Shift+Tab` | 切换权限模式 |
| `Alt+P`（Windows/Linux） | 切换模型 |
| `Alt+T`（Windows/Linux） | 切换扩展思考模式 |
| `Alt+O`（Windows/Linux） | 切换快速模式 |

#### 文本编辑

| 快捷键 | 作用 |
|--------|------|
| `Ctrl+K` | 删除光标到行尾 |
| `Ctrl+U` | 删除光标到行首 |
| `Ctrl+Y` | 粘贴刚才删除的文本 |
| `Alt+B` | 光标后退一个单词 |
| `Alt+F` | 光标前进一个单词 |
| `Ctrl+V` / `Alt+V`（Windows） | 粘贴剪贴板中的图片 |

💡 `Ctrl+R` 搜索历史命令时，按 `Ctrl+R` 可以在匹配结果中继续向前翻，按 `Tab` 接受当前匹配，按 `Enter` 直接执行。

#### 自定义快捷键

如果你对默认快捷键不满意，可以通过配置文件自定义。运行 `/keybindings` 会创建或打开 `~/.claude/keybindings.json`：

``` json
{
  "$schema": "https://www.schemastore.org/claude-code-keybindings.json",
  "bindings": [
    {
      "context": "Chat",
      "bindings": {
        "ctrl+e": "chat:externalEditor",
        "ctrl+u": null
      }
    }
  ]
}
```

将某个动作设为 `null` 即可解绑默认快捷键。配置支持多种上下文（`Chat`、`Global`、`Autocomplete`、`Confirmation` 等），每个上下文下有不同的可用动作。

⚠️ 以下三个快捷键**不能重新绑定**：`Ctrl+C`（中断）、`Ctrl+D`（退出）、`Ctrl+M`（等同于 Enter）。

### 终端配置建议

#### 主题匹配

Claude Code 的界面主题通过 `/config` 命令设置。它无法控制终端本身的颜色方案，但你可以在 `/config` 中将 Claude Code 的主题调整为与终端一致。

#### 减少闪烁

如果长时间使用时出现画面闪烁或滚动位置跳动，可以启用全屏渲染模式：

``` bash
# 设置环境变量
export CLAUDE_CODE_NO_FLICKER=1
```

或者在 `settings.json` 中配置：

``` json
{
  "env": {
    "CLAUDE_CODE_NO_FLICKER": "1"
  }
}
```

#### 大段内容处理

当需要让 Claude 处理大段代码或长文本时：

- 避免直接粘贴很长的内容——Claude Code 可能处理困难
- 把内容写到文件里，然后让 Claude 读取文件
- 注意 VS Code 内置终端对长粘贴内容的截断问题更严重

## ⌨️ 有哪些常用命令？——CLI 参考

### 常用命令速查表格

Claude Code 的命令可以分为两类：**启动交互模式**的命令和**执行一次性任务**的命令。

#### 启动类命令

| 命令 | 作用 | 说明 |
|------|------|------|
| `claude` | 启动交互模式 | 最常用的启动方式 |
| `claude "解释这个项目"` | 带初始提示启动 | 进入交互模式后自动发送第一条消息 |
| `claude -c` | 继续当前目录最近的对话 | 适合中断后恢复 |
| `claude -r` | 恢复指定会话 | 交互式选择，也可以指定名称 |
| `claude -n "名称"` | 命名当前会话 | 方便以后通过名称恢复 |
| `claude -w feature-auth` | 在 Git Worktree 中启动 | 隔离开发，不影响当前分支 |
| `claude update` | 更新到最新版本 | |

#### 一次性任务命令（非交互模式）

| 命令 | 作用 | 说明 |
|------|------|------|
| `claude -p "查询内容"` | 执行一次查询后退出 | 适合脚本和自动化 |
| `claude -c -p "检查类型错误"` | 继续对话并执行查询 | 结合上下文做一次性操作 |
| `claude --model opus "查询"` | 指定模型执行 | 可用 `sonnet` 或 `opus` 别名 |

#### 认证与诊断

| 命令 | 作用 |
|------|------|
| `claude auth login` | 登录 |
| `claude auth logout` | 登出 |
| `claude auth status --text` | 查看登录状态 |
| `claude doctor` | 检查安装和配置是否正常 |

### 非交互模式——管道与脚本

`-p`（`--print`）标志是 Claude Code 的非交互模式。它在自动化场景中非常实用，因为你可以把输出通过管道传递给其他工具。

#### 管道处理

最经典的用法是把文件内容通过管道传给 Claude 分析：

``` bash
# 分析日志文件
cat error.log | claude -p "分析这些错误日志，找出根本原因"

# 代码审查
git diff | claude -p "审查这次变更，提出改进建议"

# 生成 commit message
git diff --staged | claude -p "根据这些变更生成一个规范的 commit message"
```

#### 结构化输出

`-p` 模式支持多种输出格式，适合在脚本中进一步处理：

``` bash
# JSON 格式输出
claude -p "列出项目中所有 TODO 注释" --output-format json

# 流式 JSON（适合程序实时处理）
claude -p "分析代码结构" --output-format stream-json

# 指定 JSON Schema（获取结构化数据）
claude -p "分析依赖版本" --json-schema '{"type":"object","properties":{...}}'
```

#### 脚本中的常用标志

| 标志 | 作用 | 示例 |
|------|------|------|
| `--max-turns N` | 限制最大执行轮数 | `claude -p --max-turns 3 "修复 bug"` |
| `--allowedTools` | 指定免确认的工具 | `claude -p --allowedTools "Read" "Bash(git log *)"` |
| `--model` | 指定模型 | `claude -p --model opus "复杂任务"` |
| `--bare` | 极简模式，跳过所有初始化 | `claude --bare -p "简单查询"` |
| `--append-system-prompt` | 追加自定义系统提示 | `claude -p --append-system-prompt "始终使用中文回复"` |
| `--max-budget-usd` | 设置 API 花费上限 | `claude -p --max-budget-usd 1.00 "查询"` |

💡 `--bare` 模式特别适合 CI/CD 场景——它会跳过 Hooks、Skills、Plugins、MCP 服务器、自动记忆和 `CLAUDE.md` 的加载，让 Claude 只保留基础的 Bash、文件读取和编辑能力，启动速度更快。

📝 小结：日常开发中，用 `claude` 进入交互模式进行探索式工作，用 `claude -p` 在脚本和管道中进行自动化操作。两者配合使用，能覆盖绝大多数场景。

## 🎯 还有哪些隐藏技巧？

除了前面介绍的核心命令，Claude Code 还有一些不太显眼但非常实用的功能。

### /simplify：三维代码质量检查

对刚改完的代码做三维检查——代码复用、质量和效率，发现问题直接修掉。特别适合改完一段逻辑后立刻跑一遍，代替手动 review。

### /btw：不打断主任务的旁路问答

在不打断主任务的前提下快速问一个侧问题。适合"两个命令有什么区别"这类单轮旁路问答，不适合需要读仓库或调用工具的问题。

### /insight：提炼会话中的沉淀

让 Claude 分析当前会话，提炼出哪些内容值得沉淀到 CLAUDE.md。用法是会话做了一段之后跑一次，它会指出"这个约定你们反复提到，但没有写进契约"之类的盲点，是迭代优化 CLAUDE.md 的好手段。

### 双击 ESC：编辑上一条消息

按两次 ESC 可以回到上一条输入重新编辑，不用重新手打。Claude 走偏了、或者上一句话没说清楚，双击 ESC 修改后重发，比重新开会话省事得多。

### 会话历史：所有对话都在本地

所有会话记录存放在 `~/.claude/projects/` 下，文件夹名按项目路径命名（斜杠变横杠），每个会话是一个 `.jsonl` 文件。

```bash
# 找某个话题的历史讨论
grep -rl "关键词" ~/.claude/projects/

# 或者直接让 Claude 帮你搜
# "帮我搜一下之前关于认证配置的讨论"
```

💡 这意味着你的对话历史不会被上传到云端，完全在本地，隐私有保障。
