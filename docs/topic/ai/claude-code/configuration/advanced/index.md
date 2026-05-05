---
title: 高级配置
description: 状态栏定制、检查点回溯、环境变量、企业网络代理、语音输入
---

# 高级配置

**本文你会学到**：

- 📊 自定义状态栏（StatusLine）的配置、完整数据字段与实用脚本示例
- 🕐 Checkpointing 检查点：自动跟踪编辑、回溯恢复与限制
- 🔑 常用环境变量速查
- 🌐 企业网络代理、自定义 CA 证书与 mTLS 配置
- 🎙️ 语音输入的启用与按键绑定

## 📊 怎么自定义状态栏？

### 什么是状态栏？

状态栏是 Claude Code 底部的一个自定义信息条。它运行你配置的 shell 脚本，实时显示会话状态——上下文用量、费用、Git 分支等。

### 快速设置

刚用上 Claude Code，想在状态栏里一眼看到当前模型和上下文用量，但完全不想手写脚本？或者团队里每个人都想要不同的状态栏信息，不想让每个人去写 Bash？

Claude Code 提供了自然语言配置方式，用一句话描述你想要的效果即可：

``` bash
# 用自然语言描述你想要的效果
/statusline show model name and context percentage with a progress bar
```

Claude Code 会自动生成脚本并配置好 settings.json。

### 手动配置

``` json title="settings.json"
{
  "statusLine": {
    "type": "command",
    "command": "~/.claude/statusline.sh",
    "padding": 2
  }
}
```

也可以使用内联命令（用 `jq` 解析 JSON）：

``` json title="内联命令示例"
{
  "statusLine": {
    "type": "command",
    "command": "jq -r '\"[\\(.model.display_name)] \\(.context_window.used_percentage // 0)% context\"'"
  }
}
```

### 可用的数据字段

Claude Code 通过 stdin 向你的脚本发送 JSON 数据。下表列出全部字段，⭐ 标记为常用核心字段：

| 字段 | 说明 |
|------|------|
| ⭐ `model.id`, `model.display_name` | 当前模型标识符和显示名称 |
| ⭐ `context_window.used_percentage` | 上下文窗口已用百分比 |
| ⭐ `context_window.remaining_percentage` | 上下文窗口剩余百分比 |
| ⭐ `cost.total_cost_usd` | 本次会话总费用（USD） |
| ⭐ `cost.total_duration_ms` | 会话总时长（毫秒） |
| ⭐ `cost.total_lines_added`, `cost.total_lines_removed` | 新增/删除代码行数 |
| ⭐ `session_id` | 会话唯一标识 |
| ⭐ `version` | Claude Code 版本号 |
| `cwd`, `workspace.current_dir` | 当前工作目录（两个字段值相同，推荐用 `workspace.current_dir`） |
| `workspace.project_dir` | 启动 Claude Code 的原始目录，会话期间工作目录变更时可能与 `cwd` 不同 |
| `workspace.added_dirs` | 通过 `/add-dir` 或 `--add-dir` 添加的额外目录，未添加时为空数组 |
| `workspace.git_worktree` | 当前目录在 `git worktree add` 创建的链接 worktree 内时的名称（v2.1.97 新增） |
| `cost.total_api_duration_ms` | 等待 API 响应的总时间（毫秒） |
| `context_window.total_input_tokens`, `context_window.total_output_tokens` | 整个会话中的累积令牌计数 |
| `context_window.context_window_size` | 最大上下文窗口大小（令牌），默认 200000，扩展上下文模型为 1000000 |
| `context_window.current_usage` | 来自最近一次 API 调用的令牌计数（详见下方） |
| `exceeds_200k_tokens` | 最近一次 API 响应的总令牌数是否超过 200k（固定阈值，与实际上下文窗口大小无关） |
| `effort.level` | 当前推理工作量（`low`/`medium`/`high`/`xhigh`/`max`），不支持该参数的模型不存在此字段 |
| `thinking.enabled` | 是否启用了扩展思考 |
| `rate_limits.five_hour.used_percentage`, `rate_limits.seven_day.used_percentage` | 5 小时/7 天速率限制已消耗百分比（0-100），仅 Claude.ai 订阅者第一次 API 响应后出现 |
| `rate_limits.five_hour.resets_at`, `rate_limits.seven_day.resets_at` | 速率限制窗口重置的 Unix 纪元秒 |
| `session_name` | 通过 `--name` 或 `/rename` 设置的自定义会话名称，未设置时不存在 |
| `transcript_path` | 对话记录文件路径 |
| `output_style.name` | 当前输出样式名称 |
| `vim.mode` | Vim 模式（`NORMAL`/`INSERT`/`VISUAL`/`VISUAL LINE`），仅启用 Vim 模式时存在 |
| `agent.name` | 使用 `--agent` 标志或代理配置运行时的代理名称 |
| `worktree.name`, `worktree.path`, `worktree.branch` | `--worktree` 会话的 worktree 信息（名称、路径、分支） |
| `worktree.original_cwd`, `worktree.original_branch` | 进入 worktree 前的目录和分支 |

状态栏在以下场景特别有用：监控上下文窗口用量、跟踪会话成本、在多个会话间做区分、让 Git 分支和状态始终可见。

### 状态栏的工作原理

Claude Code 通过 stdin 向你的脚本发送 JSON 会话数据，你的脚本读取 JSON、提取需要的字段，然后将文本打印到 stdout。Claude Code 会显示脚本输出的任何内容。

**更新时机**：脚本在每条新的助手消息之后、权限模式更改时或 Vim 模式切换时运行。更新在 300ms 处防抖，快速更改会批处理在一起。如果脚本仍在运行时触发新更新，正在执行的会被取消。

**输出能力**：

- **多行**：每个 `echo` 或 `print` 语句显示为单独的行
- **颜色**：使用 ANSI 转义码（如 `\033[32m` 表示绿色），需要终端支持
- **链接**：使用 OSC 8 转义序列让文本可点击（macOS 上 Cmd+click，Windows/Linux 上 Ctrl+click），需要 iTerm2、Kitty、WezTerm 等支持超链接的终端

!!! tip "本地运行，不消耗 API Token"

    状态栏脚本在本地运行，不消耗 API 令牌。在自动完成建议、帮助菜单和权限提示等 UI 交互期间会临时隐藏。

你也可以通过 `refreshInterval` 设置自动刷新间隔（单位秒，最小值 1），让状态栏定期更新而不必等待 Claude 回复。当显示基于时间的数据（如时钟）或后台子代理在主会话空闲时更改了 Git 状态时，这个设置很有用：

``` json title="带自动刷新的状态栏配置"
{
  "statusLine": {
    "type": "command",
    "command": "~/.claude/statusline.sh",
    "refreshInterval": 30
  }
}
```

可选的 `hideVimModeIndicator` 字段会抑制提示符下方的内置 `-- INSERT --` 文本。当你的脚本自己渲染 `vim.mode` 时，将其设为 `true`，避免模式显示两次。

#### 上下文窗口的两种计数方式

`context_window` 提供了两种视角：

- **累积总计**（`total_input_tokens`, `total_output_tokens`）：整个会话中所有令牌的总和，用于跟踪总消耗量
- **当前使用情况**（`current_usage`）：来自最近一次 API 调用的令牌计数，反映实际上下文状态

`current_usage` 对象包含四个子字段：

| 子字段 | 说明 |
|--------|------|
| `input_tokens` | 当前上下文中的输入令牌 |
| `output_tokens` | 生成的输出令牌 |
| `cache_creation_input_tokens` | 写入缓存的令牌 |
| `cache_read_input_tokens` | 从缓存读取的令牌 |

`used_percentage` 字段仅从输入令牌计算（`input_tokens + cache_creation_input_tokens + cache_read_input_tokens`），不包括 `output_tokens`。在第一次 API 调用之前，`current_usage` 为 `null`。

#### 完整 JSON 结构示例

``` json title="stdin 接收的 JSON 结构"
{
  "cwd": "/current/working/directory",
  "session_id": "abc123...",
  "session_name": "my-session",
  "model": {
    "id": "claude-opus-4-7",
    "display_name": "Opus"
  },
  "workspace": {
    "current_dir": "/current/working/directory",
    "project_dir": "/original/project/directory",
    "added_dirs": [],
    "git_worktree": "feature-xyz"
  },
  "cost": {
    "total_cost_usd": 0.01234,
    "total_duration_ms": 45000,
    "total_api_duration_ms": 2300,
    "total_lines_added": 156,
    "total_lines_removed": 23
  },
  "context_window": {
    "total_input_tokens": 15234,
    "total_output_tokens": 4521,
    "context_window_size": 200000,
    "used_percentage": 8,
    "remaining_percentage": 92,
    "current_usage": {
      "input_tokens": 8500,
      "output_tokens": 1200,
      "cache_creation_input_tokens": 5000,
      "cache_read_input_tokens": 2000
    }
  },
  "effort": { "level": "high" },
  "thinking": { "enabled": true },
  "vim": { "mode": "NORMAL" },
  "agent": { "name": "security-reviewer" }
}
```

!!! warning "缺失字段处理"

    在脚本中用条件访问处理可能缺失的字段，用回退默认值处理可能为 `null` 的值。例如 jq 中用 `// 0`，Python 中用 `or 0`，Node.js 中用 `?. \|\| 0`。

### 实用脚本示例

#### 上下文窗口使用进度条

``` bash title="statusline.sh — 上下文进度条"
#!/bin/bash
input=$(cat)

MODEL=$(echo "$input" | jq -r '.model.display_name')
PCT=$(echo "$input" | jq -r '.context_window.used_percentage // 0' | cut -d. -f1)

# 构建进度条
BAR_WIDTH=10
FILLED=$((PCT * BAR_WIDTH / 100))
EMPTY=$((BAR_WIDTH - FILLED))
BAR=""
[ "$FILLED" -gt 0 ] && printf -v FILL "%${FILLED}s" && BAR="${FILL// /█}"
[ "$EMPTY" -gt 0 ] && printf -v PAD "%${EMPTY}s" && BAR="${BAR}${PAD// /░}"

echo "[$MODEL] $BAR $PCT%"
```

#### Git 状态与颜色

显示 Git 分支，用颜色编码指示暂存（绿色 `+`）和修改（黄色 `~`）文件数量：

``` bash title="statusline.sh — Git 状态"
#!/bin/bash
input=$(cat)

MODEL=$(echo "$input" | jq -r '.model.display_name')
DIR=$(echo "$input" | jq -r '.workspace.current_dir')

GREEN='\033[32m'
YELLOW='\033[33m'
RESET='\033[0m'

if git rev-parse --git-dir > /dev/null 2>&1; then
    BRANCH=$(git branch --show-current 2>/dev/null)
    STAGED=$(git diff --cached --numstat 2>/dev/null | wc -l | tr -d ' ')
    MODIFIED=$(git diff --numstat 2>/dev/null | wc -l | tr -d ' ')

    GIT_STATUS=""
    [ "$STAGED" -gt 0 ] && GIT_STATUS="${GREEN}+${STAGED}${RESET}"
    [ "$MODIFIED" -gt 0 ] && GIT_STATUS="${GIT_STATUS}${YELLOW}~${MODIFIED}${RESET}"

    echo -e "[$MODEL] ${DIR##*/} | $BRANCH $GIT_STATUS"
else
    echo "[$MODEL] ${DIR##*/}"
fi
```

#### 可点击 GitHub 链接

使用 OSC 8 转义序列创建可点击链接，按住 Cmd（macOS）或 Ctrl（Windows/Linux）点击即可在浏览器中打开：

``` bash title="statusline.sh — 可点击链接"
#!/bin/bash
input=$(cat)
MODEL=$(echo "$input" | jq -r '.model.display_name')

# 将 SSH 格式转换为 HTTPS
REMOTE=$(git remote get-url origin 2>/dev/null | sed 's/git@github.com:/https:\/\/github.com\//' | sed 's/\.git$//')

if [ -n "$REMOTE" ]; then
    REPO_NAME=$(basename "$REMOTE")
    # OSC 8 格式
    printf '%b' "[$MODEL] \e]8;;${REMOTE}\a${REPO_NAME}\e]8;;\a\n"
else
    echo "[$MODEL]"
fi
```

#### 缓存昂贵的 Git 操作

状态栏脚本在活跃会话中频繁运行，`git status` 等命令在大仓库中可能很慢。用 `session_id` 作为缓存文件名，确保会话内稳定且跨会话唯一：

``` bash title="statusline.sh — 缓存 Git 信息"
#!/bin/bash
input=$(cat)

MODEL=$(echo "$input" | jq -r '.model.display_name')
DIR=$(echo "$input" | jq -r '.workspace.current_dir')
SESSION_ID=$(echo "$input" | jq -r '.session_id')

CACHE_FILE="/tmp/statusline-git-cache-$SESSION_ID"
CACHE_MAX_AGE=5  # 秒

cache_is_stale() {
    [ ! -f "$CACHE_FILE" ] || \
    [ $(($(date +%s) - $(stat -c %Y "$CACHE_FILE" 2>/dev/null || echo 0))) -gt $CACHE_MAX_AGE ]
}

if cache_is_stale; then
    if git rev-parse --git-dir > /dev/null 2>&1; then
        BRANCH=$(git branch --show-current 2>/dev/null)
        STAGED=$(git diff --cached --numstat 2>/dev/null | wc -l | tr -d ' ')
        MODIFIED=$(git diff --numstat 2>/dev/null | wc -l | tr -d ' ')
        echo "$BRANCH|$STAGED|$MODIFIED" > "$CACHE_FILE"
    else
        echo "||" > "$CACHE_FILE"
    fi
fi

IFS='|' read -r BRANCH STAGED MODIFIED < "$CACHE_FILE"
[ -n "$BRANCH" ] && echo "[$MODEL] ${DIR##*/} | $BRANCH +$STAGED ~$MODIFIED" || echo "[$MODEL] ${DIR##*/}"
```

#### Windows 配置

Windows 上 Claude Code 优先通过 Git Bash 运行状态栏命令，没有 Git Bash 时回退到 PowerShell：

``` json title="settings.json — PowerShell 状态栏"
{
  "statusLine": {
    "type": "command",
    "command": "powershell -NoProfile -File C:/Users/username/.claude/statusline.ps1"
  }
}
```

``` powershell title="statusline.ps1"
$input_json = $input | Out-String | ConvertFrom-Json
$cwd = $input_json.cwd
$model = $input_json.model.display_name
$used = $input_json.context_window.used_percentage
$dirname = Split-Path $cwd -Leaf

if ($used) {
    Write-Host "$dirname [$model] ctx: $used%"
} else {
    Write-Host "$dirname [$model]"
}
```

安装了 Git Bash 时也可以直接运行 Bash 脚本。在 Git Bash 中没有 `jq` 时，可以用 `grep`/`cut` 解析 JSON：

``` bash title="statusline.sh — 无 jq 的 Git Bash 版本"
#!/bin/env bash
input=$(cat)
cwd=$(echo "$input" | grep -o '"cwd":"[^"]*"' | cut -d'"' -f4)
model=$(echo "$input" | grep -o '"display_name":"[^"]*"' | cut -d'"' -f4)
dirname="${cwd##*[/\\]}"
echo "$dirname [$model]"
```

### 子代理状态行

`subagentStatusLine` 设置为代理面板中的每个子代理呈现自定义行体，替换默认的 `name · description · token count` 格式：

``` json title="子代理状态行配置"
{
  "subagentStatusLine": {
    "type": "command",
    "command": "~/.claude/subagent-statusline.sh"
  }
}
```

该命令在每个刷新周期运行一次，所有可见的子代理行作为单个 JSON 对象传入 stdin。输入包含钩子公共字段加上 `columns`（可用行宽）和 `tasks` 数组，每个任务有 `id`、`name`、`type`、`status`、`description`、`label`、`startTime`、`tokenCount`、`tokenSamples` 和 `cwd`。

向 stdout 写入 JSON 行 `{"id": "<task id>", "content": "<行内容>"}` 来覆盖对应行，省略 `id` 则保持默认渲染，`content` 为空字符串则隐藏该行。`content` 中的 ANSI 颜色和 OSC 8 超链接会原样渲染。

### 状态栏故障排除

| 问题 | 排查方向 |
|------|----------|
| 状态栏未出现 | 确认脚本可执行（`chmod +x`）、输出到 stdout 而非 stderr、`disableAllHooks` 未设为 `true` |
| 显示 `--` 或空值 | 第一次 API 响应完成前字段可能为 `null`，用 `// 0` 等回退值处理 |
| 上下文百分比异常 | 用 `used_percentage` 而非累积总计；`total_input_tokens` 可能超过窗口大小 |
| OSC 8 链接不可点击 | 确认终端支持（iTerm2/Kitty/WezTerm）；Windows Terminal 可能需要设置 `FORCE_HYPERLINK=1` |
| 转义序列显示乱码 | 用 `printf '%b'` 替代 `echo -e` 获得更可靠的转义处理 |
| 脚本挂起或报错 | 非零退出码或无输出会导致状态栏变空白；保持脚本快速执行 |
| 显示 `statusline skipped` | 需要接受当前目录的工作区信任对话框，重启 Claude Code 并接受信任提示

## 🔑 环境变量速查

Claude Code 通过环境变量提供细粒度的控制能力。常用环境变量速查如下，完整列表和详细说明请参见「环境变量参考」。

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `ANTHROPIC_API_KEY` | API 密钥 | `sk-ant-...` |
| `CLAUDE_CODE_USE_BEDROCK` | 使用 AWS Bedrock | `1` |
| `CLAUDE_CODE_USE_VERTEX` | 使用 Google Vertex AI | `1` |
| `DISABLE_AUTOUPDATER` | 禁用自动更新 | `1` |
| `CLAUDE_CODE_DISABLE_FAST_MODE` | 禁用 Fast Mode | `1` |
| `CLAUDE_CODE_PERFORCE_MODE` | Perforce 模式：只读文件操作提示 `p4 edit` 而非静默覆盖（v2.1.98 新增） | `1` |
| `CLAUDE_CODE_SUBPROCESS_ENV_SCRUB` | 启用子进程沙箱（PID 命名空间隔离，仅 Linux）（v2.1.98 新增） | `1` |
| `CLAUDE_CODE_SCRIPT_CAPS` | 限制每会话脚本调用次数（v2.1.98 新增） | `10` |
| `NODE_EXTRA_CA_CERTS` | 自定义 CA 证书路径 | `/path/to/ca.pem` |

💡 你可以在 `settings.json` 的 `env` 字段中设置环境变量，也可以在 shell 的 profile 文件（如 `.bashrc`）中设置。

## 🌐 公司有代理/防火墙怎么办？——企业网络配置

### 代理配置

在企业环境中，Claude Code 需要通过代理服务器访问外部 API。它尊重标准的代理环境变量：

``` bash
# HTTPS 代理（推荐）
export HTTPS_PROXY=https://proxy.example.com:8080

# 代理绕过列表
export NO_PROXY="localhost,192.168.1.1,example.com"
```

如果代理需要认证，直接在 URL 中包含凭证：

``` bash
export HTTPS_PROXY=http://username:password@proxy.example.com:8080
```

### 自定义 CA 证书

如果你的企业使用自签名证书：

``` bash
export NODE_EXTRA_CA_CERTS=/path/to/enterprise-ca.pem
```

> 💡 **v2.1.101 起**：Claude Code 默认信任操作系统的 CA 证书存储，企业 TLS 代理通常无需额外配置。如果只需要内置 CA，设置 `CLAUDE_CODE_CERT_STORE=bundled` 即可。

### mTLS 双向认证

对安全性要求极高的环境可以启用 mTLS：

``` bash
# 客户端证书
export CLAUDE_CODE_CLIENT_CERT=/path/to/client-cert.pem

# 客户端私钥
export CLAUDE_CODE_CLIENT_KEY=/path/to/client-key.pem

# 私钥密码（如果加密了）
export CLAUDE_CODE_CLIENT_KEY_PASSPHRASE="your-passphrase"
```

### 网络访问要求

确保防火墙允许访问以下域名：

| 域名 | 用途 |
|------|------|
| `api.anthropic.com` | Claude API 端点 |
| `claude.ai` | Claude.ai 账户认证 |
| `platform.claude.com` | Anthropic Console 认证 |

## 🎙️ 能用语音跟 Claude 对话吗？

### 什么是语音输入？

语音输入让你可以**按住一个键、说话、松开**——Claude Code 会把你的语音实时转写成文字，插入到提示框中。你可以混合使用语音和键盘输入。

### 启用方式

``` bash
/voice
# 输出：Voice mode enabled. Hold Space to record.
```

⚠️ 语音输入**仅支持 Claude.ai 账户认证**，不支持 API Key 或第三方云服务（Bedrock、Vertex AI 等）。此外，语音需要在本地录音，因此不支持 SSH 会话或 Web 版 Claude Code。

### 支持的语言

| 语言 | 代码 | 语言 | 代码 |
|------|------|------|------|
| 英语 | `en` | 法语 | `fr` |
| 德语 | `de` | 日语 | `ja` |
| 西班牙语 | `es` | 韩语 | `ko` |
| 中文（未列出） | — | — | — |

⚠️ 目前中文不在官方支持列表中。如果 `language` 设置为不支持的值，语音输入会回退到英语。

### 按键绑定

默认按住 `Space` 开始录音。你可以通过 `~/.claude/keybindings.json` 重新绑定：

``` json title="~/.claude/keybindings.json"
{
  "bindings": [
    {
      "context": "Chat",
      "bindings": {
        "meta+k": "voice:pushToTalk",
        "space": null
      }
    }
  ]
}
```

💡 使用修饰键组合（如 `meta+k`）可以在第一次按下时立即开始录音，无需等待长按检测。

## 🕐 怎么安全地回退和撤销修改？——Checkpointing

### 为什么需要 Checkpointing？

当 Claude 在一个复杂任务中改了十几个文件，你突然发现方向跑偏了怎么办？手动 `git checkout` 太慢，而且不一定所有改动都提交过。Checkpointing（检查点）就是为这种场景设计的「会话级撤销」——它自动记录每次编辑前的代码状态，让你随时可以回到任意一个节点。

### Checkpointing 如何工作

当 Claude 使用文件编辑工具修改文件时，Checkpointing 会自动捕获编辑前的代码快照：

- **每个用户提示**都会创建一个新的检查点
- 检查点在会话之间持久存在，恢复的对话中也能访问
- 30 天后自动清理（可配置）

### 怎么回溯？

按两次 `Esc`（`Esc` + `Esc`）或使用 `/rewind` 命令打开回溯菜单，会看到一个可滚动的会话提示列表。选择目标节点后，可以选择以下操作：

| 操作 | 效果 |
|------|------|
| 恢复代码和对话 | 代码和对话都回到该节点 |
| 恢复对话 | 只回溯对话，保持当前代码不变 |
| 恢复代码 | 只恢复文件修改，保持对话历史 |
| 从此处总结 | 将该节点之后的对话压缩为摘要，释放上下文窗口空间 |
| 算了 | 不做任何更改 |

恢复对话或总结后，所选消息的原始提示会恢复到输入框中，方便你重新发送或编辑。

#### 恢复与总结的区别

三个「恢复」选项会撤销变更（代码、对话或两者），而「从此处总结」的工作方式不同：

- 所选消息之前的消息保持不变
- 所选消息及其后的所有消息被替换为 AI 生成的紧凑摘要
- 磁盘上的文件不会改变
- 原始消息保存在会话记录中，Claude 需要时可以参考详细信息

这类似于 `/compact`，但更有针对性——保持早期上下文的完整细节，只压缩占用空间的部分。你还可以输入可选说明来指导摘要的重点。

!!! tip "总结 vs Fork"

    总结会压缩当前会话的上下文。如果你想尝试不同的方法、同时保留原始会话完整，请改用 Fork（`claude --continue --fork-session`）。

### 适合什么场景？

- **探索替代方案**：尝试不同实现方法，不丢失起点
- **从错误中恢复**：快速撤销引入 Bug 或破坏功能的更改
- **迭代功能**：做变体实验，随时可以恢复到工作状态
- **释放上下文空间**：从中点开始总结冗长的调试会话，保持初始说明完整

### 有哪些限制？

Checkpointing 只跟踪通过 Claude 文件编辑工具进行的修改，以下情况不受覆盖：

**Bash 命令修改的文件不受跟踪**。例如 Claude 执行 `rm file.txt`、`mv old.txt new.txt`、`cp source.txt dest.txt` 等操作，这些变更无法通过回溯撤销。

**外部修改不受跟踪**。在 Claude Code 外部手动做的文件更改、其他并发会话的编辑，通常不会被捕获（除非恰好修改了当前会话涉及的文件）。

**不是版本控制的替代品**。Checkpoints 是「本地撤销」，Git 是「永久历史」。对于永久版本记录和协作，继续使用 Git 进行提交和分支管理。
