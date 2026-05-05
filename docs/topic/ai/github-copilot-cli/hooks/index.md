---
title: Hook 扩展
description: 通过 Hook 系统在工具调用生命周期中注入自定义逻辑
---

# Hook 扩展

**本文你会学到**：

- 🪝 Hook 是什么，解决什么问题
- ⏱️ 生命周期事件及其阻塞/非阻塞行为
- 🛠️ 如何编写和配置 Hook
- 📋 Payload 格式与决策控制机制
- 🏢 组织级安全策略的完整教程
- ⚖️ Hook 与 Git Hook 的区别
- ⚙️ 高级特性（Prompt Hook、HTTP Hook）与已知限制

打个比方：Hook 就像是你给 Copilot 安装的`自动感应器`——在特定时刻（会话开始、工具调用前后、出错时）自动触发你预设的操作。比如，每次 Copilot 修改文件后自动运行格式化工具，每次会话结束时自动记录日志。

---

## ❓ 为什么需要 Hook？

没有 Hook 的情况下，Copilot 的行为只能通过 prompt 和指令来间接影响。但有些操作需要`强制执行`——比如"每次写完代码必须格式化"或"出错时必须通知我"。这些需求超出了 prompt 控制的范围，Hook 正是为此而生。

---

## ⏱️ 生命周期事件

| 事件 | 阻塞 | 触发时机 | 典型用途 |
|------|:----:|---------|---------|
| `sessionStart` | 否 | 会话启动时（新会话或恢复会话） | 日志记录、环境初始化、显示策略横幅 |
| `sessionEnd` | 否 | 会话终止时 | 清理临时文件、发送统计 |
| `userPromptSubmitted` | 否 | 用户提交 prompt 后 | 输入验证、日志审计 |
| `preToolUse` | 是 | 工具执行前 | 权限检查、参数验证、阻止执行 |
| `postToolUse` | 是 | 工具执行成功后 | 结果检查、格式化（SDK programmatic hooks 可替换结果） |
| `postToolUseFailure` | 是 | 工具执行失败后 | 通过 `additionalContext` 提供恢复引导（退出码 2 时 stderr 作为恢复指导） |
| `errorOccurred` | 否 | 执行过程中发生错误时 | 错误通知、自动恢复 |
| `notification` | 否 | CLI 发出系统通知时（异步，fire-and-forget） | shell 完成、权限提示、Agent 完成/空闲 |
| `permissionRequest` | 是 | 权限服务运行前（规则引擎、会话审批、自动允许/拒绝、用户提示之前） | 以编程方式批准或拒绝工具权限，可短路整个权限流程 |
| `preCompact` | 否 | 上下文压缩前（手动或自动） | 注入保留信息、执行清理脚本 |
| `subagentStart` | 否 | 子 Agent 启动时（运行前） | 监控子 Agent 行为、注入上下文（`additionalContext` 附加到子 Agent prompt） |
| `subagentStop` | 是 | 子 Agent 完成时 | 可阻塞并强制子 Agent 继续执行 |
| `agentStop` | 是 | 主 Agent 完成一轮对话时 | 可阻塞并强制 Agent 继续执行 |

!!! info "阻塞与非阻塞"

    - **阻塞**：Hook 执行完成后，主流程才会继续。可用于阻止操作或强制续行。
    - **非阻塞**：Hook 触发后不等待结果，主流程立即继续。适用于日志记录和通知。
    - `notification` 是特殊的非阻塞事件——fire-and-forget，任何错误仅记录日志并跳过，永远不会阻塞会话。
    - `subagentStart` 虽然不阻塞子 Agent 的创建，但可以通过 `additionalContext` 注入上下文到子 Agent 的 prompt 中。
    - `postToolUseFailure` 的 command hooks 可以通过退出码 `2` 让 stderr 作为恢复指导返回给 Agent。

## 🛠️ 创建 Hook

在仓库的 `.github/hooks/` 文件夹中创建 JSON 文件（文件名可自由选择，建议用功能命名如 `format.json`、`notify.json`）：

!!! tip "Hook 可以提交到 Git"

    与 Git Hook 不同（`.git/hooks/` 不被版本控制），Copilot Hook 放在 `.github/hooks/` 中，可以提交到 Git 让团队共享。

除了仓库级 Hook，还有其他几种存放位置（1.0.8 / 1.0.11 新增）：

| 位置 | 作用范围 | 说明 |
|------|---------|------|
| `.github/hooks/*.json` | 仓库级 | 提交到 Git，团队共享 |
| `.claude/settings.json` 的 `hooks` 字段 | 仓库级 | 与其他配置合并存放 |
| `~/.copilot/hooks/` | 个人级（0.0.422 新增） | 所有项目生效，不提交 Git |

多个来源的 Hook 会合并而非覆盖（1.0.11 改进），`sessionStart` Hook 的 `additionalContext` 字段内容会注入到对话上下文中。

如需在某个项目中临时禁用所有 Hook，在配置中设置 `disableAllHooks: true`（1.0.4 新增）。

``` json title=".github/hooks/session-log.json - 记录会话"
[
  {
    "event": "sessionStart",
    "steps": [
      {
        "command": "echo \"Session started at $(date)\" >> /tmp/copilot-sessions.log"
      }
    ]
  },
  {
    "event": "sessionEnd",
    "steps": [
      {
        "command": "echo \"Session ended at $(date)\" >> /tmp/copilot-sessions.log"
      }
    ]
  }
]
```

## 📋 Hook 配置字段

每个 Hook 是一个 JSON 数组，数组中的每个对象代表一条规则。当对应的 `event` 被触发时，`steps` 中的命令会按顺序依次执行。

| 字段 | 必需 | 说明 |
|------|:----:|------|
| `event` | ✅ | 触发事件名称（见上方生命周期表） |
| `steps` | ✅ | 步骤数组，每步包含一个 `command` |
| `steps[].command` | ✅ | 要执行的 shell 命令 |
| `matcher` | ❌ | 嵌套 matcher/hooks 结构，细粒度匹配工具名称（1.0.6 新增），示例见下方。注意：`matcher` 中的正则表达式仅对**完全匹配**的工具名称生效（1.0.36 修复，此前 `matcher` 字段会被忽略） |
| `url` | ❌ | HTTP Hook 目标 URL（1.0.35 新增），设置后 Hook 会将 JSON payload POST 到该 URL 而非运行本地命令 |
| `permissionDecision` | ❌ | `preToolUse` 专用：设为 `allow` 时自动批准，设为 `ask` 时请求用户确认（1.0.4 / 1.0.18 改进） |
| `timeout` | ❌ | 步骤超时时间（秒），`timeoutSec` 的别名（1.0.2 新增） |

> 💡 **1.0.24 改进**：`preToolUse` Hook 的 stdout 输出现在支持 `modifiedArgs`/`updatedInput`（修改工具调用参数）和 `additionalContext`（注入额外上下文）字段，Hook 可以动态调整工具行为而无需用户介入。

⚠️ 注意：`command` 中的命令会在当前项目根目录下执行。如果命令执行失败（非零退出码），后续步骤仍会继续执行——Hook 不会因为某一步失败而中断整个流程。

??? example "`matcher` 嵌套匹配示例（1.0.6 新增）"

    当需要针对特定工具名称执行不同的 Hook 时，可以使用 `matcher` 进行细粒度匹配：

    ``` json
    [
      {
        "event": "postToolUse",
        "matcher": {
          "tool_name": "edit_file"
        },
        "hooks": [
          {
            "steps": [{ "command": "npm run lint --fix $FILEPATH" }]
          }
        ]
      }
    ]
    ```

    `matcher` 中的 `tool_name` 匹配工具调用的名称，匹配成功时执行内层 `hooks` 而非外层 `steps`。

!!! info "跨工具兼容"

    Hook 配置文件格式兼容 VS Code、Claude Code 和 Copilot CLI（1.0.6 改进）——同一份 Hook 配置无需修改即可在三个工具中使用。支持 Claude Code 的嵌套 `matcher/hooks` 结构。

!!! info "HTTP Hook（1.0.35 新增）"

    除了运行本地命令，Hook 现在支持将 JSON payload POST 到配置的 URL。当你需要把 Hook 事件转发到远程服务（如 Slack 通知、自定义 API）时，无需编写本地脚本中转，直接配置 `url` 字段即可。这种方式特别适合无服务器架构或需要集中管理 Hook 响应的场景。

!!! info "Plugin Hook 环境变量（1.0.26 新增）"

    当 Hook 由 Plugin 提供时，会自动接收指向插件安装目录的环境变量：
    - `PLUGIN_ROOT`：插件根目录路径
    - `COPILOT_PLUGIN_ROOT`：同上，兼容前缀
    - `CLADE_PLUGIN_ROOT`：同上，旧版兼容

    这让你在 Hook 脚本中可以引用插件自带的资源文件，例如运行插件附带的格式化工具或读取插件配置。

## 💡 实战示例

### 工具调用后运行脚本

``` json title=".github/hooks/post-tool.json"
[
  {
    "event": "postToolUse",
    "steps": [
      {
        "command": "bash .github/hooks/scripts/post-tool.sh"
      }
    ]
  }
]
```

### 自动格式化代码

``` json title=".github/hooks/format.json"
[
  {
    "event": "postToolUse",
    "steps": [
      {
        "command": "npx prettier --write '**/*.{ts,tsx,js,jsx}' 2>/dev/null || true"
      }
    ]
  }
]
```

### 错误时发送通知

``` json title=".github/hooks/notify.json"
[
  {
    "event": "errorOccurred",
    "steps": [
      {
        "command": "echo '[Copilot Error]' | mail -s 'Copilot Error Alert' dev@example.com"
      }
    ]
  }
]
```

!!! tip "macOS 执行权限"

    在 macOS 上，Plugin 提供的 Hook 脚本需要有执行权限才能正常运行。如果你的 Hook 脚本不生效，检查是否设置了执行权限（1.0.19 修复）：

    ``` bash
    chmod +x .github/hooks/your-script.sh
    ```

## ⚖️ Hook vs Git Hook

你可能已经熟悉 Git Hook（如 `pre-commit`），Copilot Hook 和它类似但作用于不同层面：

| 维度 | Copilot Hook | Git Hook |
|------|-------------|----------|
| `触发时机` | Copilot Agent 生命周期事件 | Git 操作事件（commit、push 等） |
| `配置方式` | `.github/hooks/*.json` | `.git/hooks/*`（或 Husky 等工具） |
| `提交到 Git` | ✅ | ❌（`.git/hooks/` 不跟踪） |
| `用途` | 扩展 Copilot 行为 | 代码质量门禁 |

两者可以互补：Git Hook 确保提交质量，Copilot Hook 增强 AI 辅助体验。

---

## 📋 Payload 格式与决策控制

### 两种 Payload 格式

每个 Hook 事件都会向 Hook 处理程序传递 JSON payload。通过配置中使用的**事件名格式**来选择 payload 格式：

| 格式 | 事件名写法 | 字段命名 | 适用场景 |
|------|-----------|---------|---------|
| **camelCase（CLI 默认）** | `sessionStart`、`preToolUse` | camelCase（如 `toolName`、`toolArgs`） | Copilot CLI 本地 Hook |
| **VS Code 兼容** | `SessionStart`、`PreToolUse` | snake_case（如 `tool_name`、`tool_input`） | 需与 VS Code Copilot 扩展共享配置 |

两种格式可以共存于同一配置文件中，不同事件可以使用不同格式。

### preToolUse 决策控制

`preToolUse` Hook 可以通过 stdout 输出 JSON 来控制工具的执行行为：

| 字段 | 可选值 | 说明 |
|------|--------|------|
| `permissionDecision` | `"allow"` / `"deny"` / `"ask"` | 是否允许工具执行。空输出则使用默认行为 |
| `permissionDecisionReason` | string | 拒绝原因，**`deny` 时必须提供** |
| `modifiedArgs` | object | 替换原始工具参数 |

``` json
{
  "permissionDecision": "deny",
  "permissionDecisionReason": "Privilege escalation requires manual approval."
}
```

``` json
{
  "permissionDecision": "allow",
  "modifiedArgs": { "command": "git status" }
}
```

如果配置了多个 `preToolUse` Hook，它们按顺序执行。**任意一个 Hook 返回 `"deny"` 即会阻止工具执行**。

### permissionRequest 决策控制

`permissionRequest` 在权限服务运行之前触发——早于规则检查、会话审批、自动允许/拒绝和用户提示。如果 Hook 返回 `behavior: "allow"` 或 `"deny"`，将**短路整个权限流程**，后续步骤不再执行。返回空输出则回退到正常的权限处理流程。

支持可选的 `matcher` 正则表达式，仅对匹配 `toolName` 的请求触发 Hook（锚定为 `^(?:pattern)$`）。

| 字段 | 可选值 | 说明 |
|------|--------|------|
| `behavior` | `"allow"` / `"deny"` | 批准或拒绝工具调用 |
| `message` | string | 拒绝时反馈给 LLM 的消息 |
| `interrupt` | boolean | 设为 `true` 并搭配 `"deny"` 时，完全停止 Agent |

对于 command hooks，退出码 `2` 等同于 deny——stdout JSON（如有）会与 `{"behavior":"deny"}` 合并，stderr 被忽略。

> [!note]
> `read` 和 `hook` 权限类型会在 Hook 之前短路，不会触发 `permissionRequest`。

### agentStop / subagentStop 决策控制

这两个事件可以阻止 Agent 结束，强制其继续执行：

| 字段 | 可选值 | 说明 |
|------|--------|------|
| `decision` | `"block"` / `"allow"` | `"block"` 强制 Agent 继续执行一轮 |
| `reason` | string | 当 `decision` 为 `"block"` 时，作为下一轮对话的 prompt |

``` json
{
  "decision": "block",
  "reason": "Please verify all test cases pass before finishing."
}
```

### notification Hook

`notification` Hook 是异步 fire-and-forget 的，永远不会阻塞会话。通过可选的 `matcher` 正则表达式匹配 `notification_type`（锚定为 `^(?:pattern)$`）。

**通知类型：**

| 类型 | 触发场景 |
|------|---------|
| `shell_completed` | 后台（异步）shell 命令完成 |
| `shell_detached_completed` | 脱离的 shell 会话完成 |
| `agent_completed` | 后台子 Agent 完成（成功或失败） |
| `agent_idle` | 后台 Agent 完成一轮对话并进入空闲状态 |
| `permission_prompt` | Agent 请求执行工具的权限 |
| `elicitation_dialog` | Agent 请求用户提供额外信息 |

**输出控制：** 可通过 `additionalContext` 字段向会话注入文本（作为前置用户消息），如果会话处于空闲状态则触发 Agent 继续处理。

---

## 🏢 组织级策略教程

以下内容面向 DevOps 工程师、平台团队和工程负责人，展示如何通过 Hook 为团队建立安全合规的 Copilot CLI 使用策略。

### 策略定义流程

在编写任何 Hook 脚本之前，先明确哪些操作应自动放行、哪些需要人工审查。

**识别高风险命令：**

| 类别 | 模式示例 | 风险 |
|------|---------|------|
| 提权操作 | `sudo`、`su`、`runas` | 获取超出预期的系统权限 |
| 破坏性操作 | `rm -rf /`、`mkfs`、`dd`、`format` | 不可逆的文件系统或数据损坏 |
| 下载执行 | `curl ... \| bash`、`wget ... \| sh`、`iex (irm ...)` | 远程代码执行 |

**决定日志策略：**

- **全量日志**：记录所有 prompt 和工具调用，适合合规要求严格的组织
- **仅高风险日志**：只记录被拒绝的操作和敏感工具调用，减少存储开销
- 日志中**禁止**包含明文密钥或凭证，必须脱敏后再写入

**与利益相关者对齐：**

- 安全/合规团队：确认风险边界
- 平台/基础设施团队：确认是否需要更宽泛的权限
- 开发团队：让开发者理解什么会被阻止以及为什么

### 策略横幅示例

通过 `sessionStart` Hook 在每次会话启动时显示策略提醒。

**Bash 脚本（`.github/hooks/scripts/session-banner.sh`）：**

``` bash
#!/bin/bash
set -euo pipefail

cat << 'EOF'
COPILOT CLI POLICY ACTIVE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Prompts and tool use may be logged for auditing
• High-risk commands may be blocked automatically
• If something is blocked, follow the guidance shown
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
EOF
exit 0
```

**PowerShell 脚本（`.github/hooks/scripts/session-banner.ps1`）：**

``` powershell
$ErrorActionPreference = "Stop"

Write-Host @"
COPILOT CLI POLICY ACTIVE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
• Prompts and tool use may be logged for auditing
• High-risk commands may be blocked automatically
• If something is blocked, follow the guidance shown
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
"@
exit 0
```

**Hook 配置：**

``` json
{
  "version": 1,
  "hooks": {
    "sessionStart": [
      {
        "type": "command",
        "bash": "./scripts/session-banner.sh",
        "powershell": "./scripts/session-banner.ps1",
        "cwd": ".github/hooks",
        "timeoutSec": 10
      }
    ]
  }
}
```

### 提示词日志记录

通过 `userPromptSubmitted` Hook 记录用户提交的 prompt，用于审计和合规。

**Bash 脚本（`.github/hooks/scripts/log-prompt.sh`）：**

``` bash
#!/bin/bash
set -euo pipefail

INPUT="$(cat)"

TIMESTAMP_MS="$(echo "$INPUT" | jq -r '.timestamp // empty')"
CWD="$(echo "$INPUT" | jq -r '.cwd // empty')"

LOG_DIR=".github/hooks/logs"
mkdir -p "$LOG_DIR"
chmod 700 "$LOG_DIR"

# 仅记录元数据，避免存储敏感数据
jq -n \
  --arg ts "$TIMESTAMP_MS" \
  --arg cwd "$CWD" \
  '{event:"userPromptSubmitted", timestampMs:$ts, cwd:$cwd}' \
  >> "$LOG_DIR/audit.jsonl"

exit 0
```

**PowerShell 脚本（`.github/hooks/scripts/log-prompt.ps1`）：**

``` powershell
$ErrorActionPreference = "Stop"

$inputObj = [Console]::In.ReadToEnd() | ConvertFrom-Json

$timestampMs = $inputObj.timestamp
$cwd = $inputObj.cwd
$prompt = $inputObj.prompt

# 脱敏：将敏感 token 替换为占位符
$redactedPrompt = $prompt -replace 'ghp_[A-Za-z0-9]{20,}', '[REDACTED_TOKEN]'
$redactedPrompt = $redactedPrompt -replace 'Bearer [A-Za-z0-9_\-\.]+', 'Bearer [REDACTED]'

$logDir = ".github/hooks/logs"
if (-not (Test-Path $logDir)) {
  New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

$logEntry = @{
  event       = "userPromptSubmitted"
  timestampMs = $timestampMs
  cwd         = $cwd
  prompt      = $redactedPrompt
} | ConvertTo-Json -Compress

Add-Content -Path "$logDir/audit.jsonl" -Value $logEntry
exit 0
```

> [!important]
> Prompt 可能包含敏感信息。记录前必须进行脱敏处理，并遵循组织的数据处理和保留策略。

### 工具使用策略

通过 `preToolUse` Hook 在工具执行前评估策略，阻止高风险操作。

**完整 Bash 策略脚本（`.github/hooks/scripts/pre-tool-policy.sh`）：**

``` bash
#!/bin/bash
set -euo pipefail

INPUT="$(cat)"

TOOL_NAME="$(echo "$INPUT" | jq -r '.toolName // empty')"
TOOL_ARGS_RAW="$(echo "$INPUT" | jq -r '.toolArgs // empty')"

LOG_DIR=".github/hooks/logs"
mkdir -p "$LOG_DIR"

# 脱敏：替换敏感模式
REDACTED_TOOL_ARGS="$(echo "$TOOL_ARGS_RAW" | \
  sed -E 's/ghp_[A-Za-z0-9]{20,}/[REDACTED_TOKEN]/g' | \
  sed -E 's/gho_[A-Za-z0-9]{20,}/[REDACTED_TOKEN]/g' | \
  sed -E 's/ghu_[A-Za-z0-9]{20,}/[REDACTED_TOKEN]/g' | \
  sed -E 's/ghs_[A-Za-z0-9]{20,}/[REDACTED_TOKEN]/g' | \
  sed -E 's/Bearer [A-Za-z0-9_\-\.]+/Bearer [REDACTED]/g' | \
  sed -E 's/--password[= ][^ ]+/--password=[REDACTED]/g' | \
  sed -E 's/--token[= ][^ ]+/--token=[REDACTED]/g')"

# 记录工具使用日志
jq -n \
  --arg tool "$TOOL_NAME" \
  --arg toolArgs "$REDACTED_TOOL_ARGS" \
  '{event:"preToolUse", toolName:$tool, toolArgs:$toolArgs}' \
  >> "$LOG_DIR/audit.jsonl"

# 仅对 bash 工具执行策略规则
if [ "$TOOL_NAME" != "bash" ]; then
  exit 0
fi

if ! echo "$TOOL_ARGS_RAW" | jq -e . >/dev/null 2>&1; then
  exit 0
fi

COMMAND="$(echo "$TOOL_ARGS_RAW" | jq -r '.command // empty')"

deny() {
  local reason="$1"
  local redacted_cmd="$(echo "$COMMAND" | \
    sed -E 's/ghp_[A-Za-z0-9]{20,}/[REDACTED_TOKEN]/g' | \
    sed -E 's/Bearer [A-Za-z0-9_\-\.]+/Bearer [REDACTED]/g' | \
    sed -E 's/--password[= ][^ ]+/--password=[REDACTED]/g' | \
    sed -E 's/--token[= ][^ ]+/--token=[REDACTED]/g')"

  jq -n \
    --arg cmd "$redacted_cmd" \
    --arg r "$reason" \
    '{event:"policyDeny", toolName:"bash", command:$cmd, reason:$r}' \
    >> "$LOG_DIR/audit.jsonl"

  jq -n \
    --arg r "$reason" \
    '{permissionDecision:"deny", permissionDecisionReason:$r}'
  exit 0
}

# 提权操作
if echo "$COMMAND" | grep -qE '\b(sudo|su|runas)\b'; then
  deny "Privilege escalation requires manual approval."
fi

# 针对根目录的破坏性操作
if echo "$COMMAND" | grep -qE 'rm\s+-rf\s*/($|\s)|rm\s+.*-rf\s*/($|\s)'; then
  deny "Destructive operations targeting the filesystem root require manual approval."
fi

# 系统级破坏性操作
if echo "$COMMAND" | grep -qE '\b(mkfs|dd|format)\b'; then
  deny "System-level destructive operations are not allowed via automated execution."
fi

# 下载执行模式
if echo "$COMMAND" | grep -qE 'curl.*\|\s*(bash|sh)|wget.*\|\s*(bash|sh)'; then
  deny "Download-and-execute patterns require manual approval."
fi

# 默认允许
exit 0
```

**完整 PowerShell 策略脚本（`.github/hooks/scripts/pre-tool-policy.ps1`）：**

``` powershell
$ErrorActionPreference = "Stop"

$inputObj = [Console]::In.ReadToEnd() | ConvertFrom-Json
$toolName = $inputObj.toolName
$toolArgsRaw = $inputObj.toolArgs

$logDir = ".github/hooks/logs"
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir -Force | Out-Null }

# 脱敏
$redactedToolArgs = $toolArgsRaw `
  -replace 'ghp_[A-Za-z0-9]{20,}', '[REDACTED_TOKEN]' `
  -replace 'gho_[A-Za-z0-9]{20,}', '[REDACTED_TOKEN]' `
  -replace 'ghu_[A-Za-z0-9]{20,}', '[REDACTED_TOKEN]' `
  -replace 'ghs_[A-Za-z0-9]{20,}', '[REDACTED_TOKEN]' `
  -replace 'Bearer [A-Za-z0-9_\-\.]+', 'Bearer [REDACTED]' `
  -replace '--password[= ][^ ]+', '--password=[REDACTED]' `
  -replace '--token[= ][^ ]+', '--token=[REDACTED]'

(@{
  event    = "preToolUse"
  toolName = $toolName
  toolArgs = $redactedToolArgs
} | ConvertTo-Json -Compress) | Add-Content -Path "$logDir/audit.jsonl"

if ($toolName -ne "bash") { exit 0 }

$toolArgs = $null
try { $toolArgs = $toolArgsRaw | ConvertFrom-Json } catch { exit 0 }

$command = $toolArgs.command

function Deny([string]$reason) {
  $redactedCommand = $command `
    -replace 'ghp_[A-Za-z0-9]{20,}', '[REDACTED_TOKEN]' `
    -replace 'Bearer [A-Za-z0-9_\-\.]+', 'Bearer [REDACTED]' `
    -replace '--password[= ][^ ]+', '--password=[REDACTED]' `
    -replace '--token[= ][^ ]+', '--token=[REDACTED]'

  (@{
    event    = "policyDeny"
    toolName = "bash"
    command  = $redactedCommand
    reason   = $reason
  } | ConvertTo-Json -Compress) | Add-Content -Path "$logDir/audit.jsonl"

  (@{
    permissionDecision = "deny"
    permissionDecisionReason = $reason
  } | ConvertTo-Json -Compress)

  exit 0
}

if ($command -match '\b(sudo|su|runas)\b') { Deny "Privilege escalation requires manual approval." }
if ($command -match 'rm\s+-rf\s*/(\s|$)|rm\s+.*-rf\s*/(\s|$)') { Deny "Destructive operations targeting the filesystem root require manual approval." }
if ($command -match '\b(mkfs|dd|format)\b') { Deny "System-level destructive operations are not allowed via automated execution." }
if ($command -match 'curl.*\|\s*(bash|sh)|wget.*\|\s*(bash|sh)') { Deny "Download-and-execute patterns require manual approval." }

exit 0
```

### 端到端测试

完成配置后，在仓库中验证 Hook 行为：

**验证配置文件有效性：**

``` bash
jq '.' < .github/hooks/copilot-cli-policy.json
```

**Unix 系统设置脚本执行权限：**

``` bash
chmod +x .github/hooks/scripts/*.sh
```

**运行基本会话测试：**

``` bash
copilot -p "Show me the status of this repository"
```

预期结果：显示策略横幅，`audit.jsonl` 中出现 `userPromptSubmitted` 记录。

**测试工具调用日志：**

``` bash
copilot -p "Show me the last 5 git commits"
```

预期结果：`audit.jsonl` 中出现 `preToolUse` 记录。

**查看审计日志：**

``` bash
# 查看最近 50 条
tail -n 50 .github/hooks/logs/audit.jsonl

# 筛选被拒绝的操作
jq 'select(.event=="policyDeny")' .github/hooks/logs/audit.jsonl
```

### 推出策略

验证通过后，按以下策略逐步推广：

**日志先行（推荐）：** 先只记录日志不拒绝执行，观察一段时间了解常见使用模式后再引入拒绝规则。

**逐团队推广：** 先在一个团队或仓库部署，收集反馈后再扩展到其他团队。

**基于风险推广：** 从处理敏感系统或生产基础设施的仓库开始，再扩展到低风险仓库。

**沟通期望：** 确保开发者知道 Hook 已激活、哪些命令可能被阻止、被拒绝后如何处理。

**持续维护：** 将 Hook 配置和脚本纳入版本控制，定期审查审计日志，增量更新拒绝规则，为每条规则记录存在原因。

---

## ⚙️ 高级特性与限制

### Prompt Hook

`type: "prompt"` 类型的 Hook 可以自动提交文本，就像用户输入一样。**仅在 `sessionStart` 事件上支持，且仅对新交互式会话触发**——不会在恢复会话或非交互模式（`-p`）下触发。

``` json
{
  "version": 1,
  "hooks": {
    "sessionStart": [
      {
        "type": "prompt",
        "prompt": "Your prompt text or /slash-command"
      }
    ]
  }
}
```

`prompt` 字段支持自然语言消息或斜杠命令。

### HTTP Hooks

在 Hook 配置中使用 `url` 字段（1.0.35 新增），Hook 会将 JSON payload POST 到指定的远程端点，无需编写本地脚本中转。

``` json
{
  "version": 1,
  "hooks": {
    "preToolUse": [
      {
        "type": "command",
        "url": "https://your-webhook.example.com/copilot-hook",
        "timeoutSec": 30
      }
    ]
  }
}
```

这种方式特别适合：无服务器架构、集中管理 Hook 响应、转发事件到 Slack 通知或自定义 API。

### 已知限制

| 限制 | 说明 |
|------|------|
| 大文件跳过快照 | 文件超过 **10MB** 时跳过快照 |
| 大量文件跳过快照 | 文件数量超过 **500** 时跳过快照 |
| Hook 失败不阻塞 | Hook 执行失败（非零退出码或超时）仅记录日志并跳过，**永远不会阻塞 Agent 执行** |
| 多 Hook 合并 | `permissionRequest` 的多个 Hook 按顺序执行，后输出的覆盖前输出的；`preToolUse` 中任意一个返回 `deny` 即阻止执行 |
