---
title: Hooks
description: 通过 Hooks 在特定生命周期事件时自动执行脚本，实现可预测的自动化
---

# Hooks

**本文你会学到**：

- 🎯 Hook 是什么，为什么它比「写在 CLAUDE.md 里提醒 Claude」更可靠
- 🔧 Claude Code 的生命周期事件分别什么时候触发
- ⚙️ 三种配置方式（settings.json、脚本目录、Plugin/Skill）的选择策略
- 🚀 5 个拿来即用的 Hook 实践案例
- 📌 Hook 的上下文成本几乎是零——除非你的脚本主动向 stdout 返回内容

## 🤔 什么时候需要 Hook？

想象你去一家公司上班，HR 在入职时给你发了一份清单：进门要刷卡、离开要锁屏、每周五要写周报。这些规则不是"希望你遵守"，而是"系统强制执行"——你不刷卡，门不会开；你不写周报，系统自动催你。

Hook 就是 Claude Code 世界里的这份**自动化检查清单**。

具体来说，Hook 是你在 Claude Code 的特定生命周期节点上注册的**用户自定义脚本**。当 Claude 执行到某个节点（比如准备运行一个 Bash 命令），你注册的脚本会自动执行，然后根据脚本的返回结果决定下一步：放行、阻止、还是把反馈交给 Claude 让它调整策略。

### 为什么不直接写在 CLAUDE.md 里

你可能想：我直接在 CLAUDE.md 里写"禁止执行 `rm -rf` 命令"不就行了？

确实可以，但 CLAUDE.md 本质上是一段**提示词建议**。Claude 会尽量遵守，但在以下场景中可能失效：

- 上下文过长时，CLAUDE.md 的指令可能被稀释
- Claude 在复杂推理中可能"忘记"这条规则
- 模型本身的判断可能和规则产生冲突

Hook 则是**确定性执行**的——它不依赖模型的"记忆力"或"判断力"，而是在特定事件触发时**必然运行**你的脚本。这就好比"提醒员工注意安全"和"安装安全门禁"的区别：前者靠自觉，后者靠机制。

### Hook 的五种类型

Claude Code 支持五种 Hook 类型，覆盖从简单到复杂的各种场景：

| 类型 | 运行方式 | 适用场景 |
|------|---------|---------|
| `command` | 执行 Shell 命令 | 最常用，适合格式化、校验、拦截等确定性任务 |
| `http` | 向 URL 发送 POST 请求 | 需要外部服务处理逻辑时（如审计日志、远程校验）（v2.1.84 扩展到 `WorktreeCreate`） |
| `prompt` | 单轮 LLM 评估 | 需要判断力但不需要读文件的场景（如检查任务是否完成） |
| `agent` | 多轮子代理验证 | 需要读文件、搜索代码才能做出判断的场景 |
| `mcp_tool` | 调用 MCP 工具 | 需要直接调用已连接的 MCP 服务器工具时（v2.1.118 新增） |

💡 大多数情况下，`command` 类型就够用了。只有在需要"智能判断"而非"确定性规则"时，才考虑 `prompt` 或 `agent` 类型。

### 基于 HTTP 的 Hook：将事件转发给外部服务

当你的 Hook 逻辑不适合用本地 Shell 脚本实现时——比如需要调用跨团队的共享审计服务、云函数、或者你已经有一个现成的 Web 服务来处理校验逻辑——`type: "http"` 就派上用场了。

HTTP Hook 会把与 `command` Hook 相同的 JSON 数据（本来通过 stdin 传入的数据）通过 POST 请求发送到你指定的 URL，然后从 HTTP 响应体中读取结果，格式与 stdout JSON 完全一致：

``` json title=".claude/settings.json"
{
  "hooks": {
    "PostToolUse": [
      {
        "hooks": [
          {
            "type": "http",
            "url": "http://localhost:8080/hooks/tool-use",
            "headers": {
              "Authorization": "Bearer $MY_TOKEN"
            },
            "allowedEnvVars": ["MY_TOKEN"]
          }
        ]
      }
    ]
  }
}
```

几个需要注意的细节：

- HTTP 响应的状态码不影响操作结果。要阻止工具调用，必须在 2xx 响应体中返回包含 `hookSpecificOutput` 中相应决策字段的 JSON——即使返回 4xx 或 5xx，操作也不会被阻断
- `headers` 中的值支持环境变量插值，语法为 `$VAR_NAME` 或 `${VAR_NAME}`。但只有列在 `allowedEnvVars` 数组中的变量才会被解析，其余的 `$VAR` 会保持原样（空值），这是一层安全保护
- HTTP Hook 的超时默认为 10 分钟，可以通过 `timeout` 字段（单位秒）自定义

### 基于提示的 Hook：让 LLM 做判断

有些场景你需要的是"判断力"而不是"确定性规则"。比如你希望 Claude 在声称完成所有任务之前，先由另一个 LLM 评估"这些任务真的都完成了吗？"。`type: "prompt"` Hook 正是为这种场景设计的。

当你不使用 `command` 而使用 `prompt` 时，Claude Code 不会执行 Shell 命令，而是将你的提示词和 Hook 的输入数据发送给 Claude 模型（默认使用 Haiku，可通过 `model` 字段指定其他模型）做一次**单轮 LLM 评估**。

模型的唯一任务就是返回一个 JSON 决策：

- `"ok": true`：操作继续
- `"ok": false`：操作被阻止。对于 `Stop` 和 `SubagentStop` Hook，`reason` 字段的内容会反馈给 Claude，让它知道下一步该做什么。对于其他事件，当前回合直接结束，`reason` 在聊天中显示为警告行，但 Claude 看不到它

``` json title=".claude/settings.json"
{
  "hooks": {
    "Stop": [
      {
        "hooks": [
          {
            "type": "prompt",
            "prompt": "检查所有任务是否完成。如果未完成，返回 {\"ok\": false, \"reason\": \"剩余的工作\"}。"
          }
        ]
      }
    ]
  }
}
```

`prompt` Hook 的优势是零配置、无需写脚本文件，适合 Hook 输入数据本身足以做出判断的场景。

### 基于代理的 Hook：让子代理深入验证

当你需要的不只是"判断"，而是"调查"——需要读文件、搜索代码、运行命令才能得出结论时，`type: "agent"` Hook 是更合适的选择。

Agent Hook 会启动一个完整的子代理（subagent），它拥有工具访问权限，可以读文件、搜索代码、执行命令，然后返回与 `prompt` Hook 相同的 `"ok"` / `"reason"` JSON 决策格式。默认超时 60 秒，最多 50 个工具使用轮次。

``` json title=".claude/settings.json"
{
  "hooks": {
    "Stop": [
      {
        "hooks": [
          {
            "type": "agent",
            "prompt": "验证所有单元测试是否通过。运行测试套件并检查结果。$ARGUMENTS",
            "timeout": 120
          }
        ]
      }
    ]
  }
}
```

⚠️ Agent Hook 目前是实验性功能，API 和行为可能在未来版本中改变。对于生产工作流，优先使用 `command` Hook。

三种 Hook 类型的选择可以这样理解：`prompt` 是"凭数据判断"，`agent` 是"去现场调查"，`command` 是"按规则执行"。

## ⏱️ Hook 能在哪些时机触发？

Claude Code 的一个会话从开始到结束，经历了多个可注册 Hook 的生命周期事件。为了方便理解，我们可以把它们分成**四个阶段**。

### 会话阶段

| 事件 | 触发时机 | 能否阻止 |
|------|---------|---------|
| `SessionStart` | 会话启动、恢复、清屏或压缩后 | ❌ 但 stdout 内容会注入 Claude 上下文 |
| `Setup` | 使用 `--init-only`、`--init` 或 `--maintenance` 启动时 | ❌ 适合 CI/脚本的一次性准备 |
| `SessionEnd` | 会话终止 | ❌ 但适合做清理工作 |
| `InstructionsLoaded` | CLAUDE.md 或 rules 文件加载到上下文时 | ❌ 仅用于审计/可观测（v2.1.69 新增） |
| `CwdChanged` | 工作目录切换时 | ❌ 但可刷新环境变量（v2.1.83 新增） |
| `FileChanged` | 监听的文件发生变化时 | ❌ 但可刷新环境变量（v2.1.83 新增） |

### 用户交互阶段

| 事件 | 触发时机 | 能否阻止 |
|------|---------|---------|
| `UserPromptSubmit` | 你提交提示词后、Claude 处理前 | ✅ 可阻止提示词被处理 |
| `UserPromptExpansion` | 用户输入的命令展开为提示词时 | ✅ 可阻止展开 |
| `Notification` | Claude Code 发送通知时 | ❌ 仅用于通知（如桌面弹窗） |

### 工具执行阶段（最常用）

| 事件 | 触发时机 | 能否阻止 |
|------|---------|---------|
| `PreToolUse` | 工具调用执行前 | ✅ 可阻止或修改工具参数 |
| `PostToolUse` | 工具调用成功后 | ⚠️ 工具已执行，无法回滚 |
| `PostToolUseFailure` | 工具调用失败后 | ⚠️ 工具已失败，可提供修复建议 |
| `PostToolBatch` | 一批并行工具调用全部完成后 | ❌ 用于批处理后汇总处理 |
| `PermissionRequest` | 权限对话框出现时 | ✅ 可自动批准或拒绝（v2.0.45 新增） |
| `PermissionDenied` | 自动模式拒绝工具调用时 | ✅ 可告诉模型可以重试 |

### 子代理与任务阶段

| 事件 | 触发时机 | 能否阻止 |
|------|---------|---------|
| `SubagentStart` | 子代理启动时 | ❌ 但可注入上下文 |
| `SubagentStop` | 子代理完成时 | ✅ 可阻止子代理停止 |
| `TaskCreated` | 通过 TaskCreate 创建任务时 | ✅ 可阻止任务创建（v2.1.84 新增） |
| `TaskCompleted` | 任务被标记为完成时 | ✅ 可阻止任务完成 |
| `TeammateIdle` | 团队成员即将空闲时 | ✅ 可阻止成员空闲 |
| `Stop` | Claude 完成响应时 | ✅ 可阻止 Claude 停止 |
| `StopFailure` | 因 API 错误导致回合结束时 | ❌ 输出被忽略 |

### 其他事件

| 事件 | 触发时机 | 能否阻止 |
|------|---------|---------|
| `ConfigChange` | 配置文件在会话中被修改时 | ✅ 可阻止配置变更生效（v2.1.49 新增） |
| `PreCompact` | 上下文压缩前 | ✅ exit code 2 可阻止压缩执行（v2.1.105 改进） |
| `PostCompact` | 上下文压缩完成后 | ❌ |
| `Elicitation` | MCP 服务器请求用户输入时 | ✅ 可自动响应 |
| `ElicitationResult` | 用户响应 MCP 请求后、发送给服务器前 | ✅ 可修改或阻止响应 |
| `WorktreeCreate` | 创建 worktree 时 | ✅ 自定义创建逻辑 |
| `WorktreeRemove` | 删除 worktree 时 | ❌ 但可执行清理 |

### Matcher 过滤

不是每个事件都需要响应所有情况。`matcher` 字段用正则表达式精确过滤触发条件：

``` json title=".claude/settings.json"
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          { "type": "command", "command": "prettier --write ..." }
        ]
      }
    ]
  }
}
```

这里的 `"Edit|Write"` 表示只在 Claude 使用 `Edit` 或 `Write` 工具时才触发，`Bash`、`Read` 等工具不会触发这个 Hook。

⚠️ 注意：`matcher` 的匹配规则是正则表达式，大小写敏感。`Edit` 不会匹配 `edit`。

不同事件类型的 matcher 匹配字段不同：

| 事件类型 | matcher 匹配的是什么 | 常用值示例 |
|---------|-------------------|-----------|
| 工具类事件 | 工具名称 | `Bash`、`Edit\|Write`、`mcp__.*` |
| `SessionStart` | 会话启动方式 | `startup`、`resume`、`compact` |
| `Setup` | 哪个 CLI 标志触发了设置 | `init`、`maintenance` |
| `SessionEnd` | 会话结束原因 | `clear`、`resume`、`logout`、`prompt_input_exit`、`other` |
| `Notification` | 通知类型 | `permission_prompt`、`idle_prompt`、`auth_success`、`elicitation_dialog`、`elicitation_complete`、`elicitation_response` |
| `SubagentStart` / `SubagentStop` | 代理类型 | `general-purpose`、`Explore`、`Plan` 或自定义代理名 |
| `ConfigChange` | 配置来源 | `user_settings`、`project_settings`、`local_settings`、`policy_settings`、`skills` |
| `PreCompact` / `PostCompact` | 压缩触发方式 | `manual`、`auto` |
| `StopFailure` | 错误类型 | `rate_limit`、`authentication_failed`、`billing_error`、`server_error`、`unknown` |
| `InstructionsLoaded` | 加载原因 | `session_start`、`nested_traversal`、`path_glob_match`、`include`、`compact` |
| `Elicitation` / `ElicitationResult` | MCP 服务器名称 | 你配置的 MCP 服务器名称 |
| `FileChanged` | 文字文件名 | `.envrc\|.env`（用 `\|` 分隔多个文件名） |
| `UserPromptExpansion` | 命令名称 | Skill 或命令名称 |

以下事件**不支持 matcher**，始终在每次出现时触发：

`UserPromptSubmit`、`PostToolBatch`、`Stop`、`TeammateIdle`、`TaskCreated`、`TaskCompleted`、`WorktreeCreate`、`WorktreeRemove`、`CwdChanged`

除了 `matcher`，还可以用 `if` 字段做更精细的过滤，同时匹配工具名和参数：

``` json title=".claude/settings.json"
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "if": "Bash(git *)",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/check-git-policy.sh"
          }
        ]
      }
    ]
  }
}
```

这个 `if` 字段只在工具事件上有效，表示"只有当 Bash 命令以 `git` 开头时才运行这个 Hook"。

### `if` 与 `matcher` 的区别

当你想过滤 Hook 的触发条件时，`matcher` 和 `if` 是两个不同层级的工具：

- **`matcher`** 只能按工具名称过滤（正则匹配），写在 hook 组级别，作用于该组下所有 Hook。例如 `matcher: "Bash"` 让所有子 Hook 只在 Bash 工具调用时触发
- **`if`** 使用权限规则语法（permissions syntax），可以同时按工具名称和参数过滤，写在单个 Hook 级别。例如 `if: "Bash(git *)"` 进一步限定只匹配以 `git` 开头的 Bash 命令

`if` 字段接受与权限规则相同的模式语法，例如 `"Bash(git *)"`、`"Edit(*.ts)"` 等。对于复合命令（如 `npm test && git push`），Claude Code 会评估每个子命令，只要有一个子命令匹配就会触发 Hook。

!!! warning "使用限制"

    `if` 字段需要 Claude Code v2.1.85 或更高版本。它**仅适用于工具事件**：`PreToolUse`、`PostToolUse`、`PostToolUseFailure`、`PermissionRequest` 和 `PermissionDenied`。将其添加到任何其他事件会阻止 Hook 运行。

## ⚙️ 怎么配置 Hook？

### settings.json 配置方式

Hook 的配置分为三层嵌套：

1. **选择事件**：你要响应哪个生命周期事件
2. **设置 matcher**：什么时候触发（可选）
3. **定义 handler**：触发后执行什么

配置文件的位置决定了 Hook 的作用范围：

| 位置 | 作用范围 | 可否提交到仓库 |
|------|---------|-------------|
| `~/.claude/settings.json` | 所有项目 | ❌ 仅本机 |
| `.claude/settings.json` | 当前项目 | ✅ 可提交 |
| `.claude/settings.local.json` | 当前项目 | ❌ 已 gitignore |
| 企业托管策略 | 整个组织 | ✅ 管理员控制 |

一个完整的 `PreToolUse` Hook 配置示例：

``` json title=".claude/settings.json"
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "if": "Bash(rm *)",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/block-rm.sh"
          }
        ]
      }
    ]
  }
}
```

### .claude/hooks/ 目录方式

当 Hook 逻辑较复杂时，建议把脚本放在 `.claude/hooks/` 目录下，然后在 settings.json 中引用。这是一种**组织惯例**——Claude Code 不会自动扫描这个目录，你仍然需要在 settings.json 中注册。

目录结构示例：

```
.claude/
├── settings.json          # Hook 配置入口
└── hooks/
    ├── block-rm.sh        # 阻止 rm -rf 命令
    ├── check-style.sh     # 文件风格检查
    └── run-tests.sh       # 自动运行测试
```

引用脚本时使用 `$CLAUDE_PROJECT_DIR` 环境变量指向项目根目录，确保无论 Claude 当前工作目录在哪里，都能找到脚本：

``` json title=".claude/settings.json"
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/check-style.sh"
          }
        ]
      }
    ]
  }
}
```

### Hook 脚本如何与 Claude Code 通信

可以把 Hook 脚本和 Claude Code 的交互想象成一份**标准的表单流程**：Claude Code 通过 stdin 把「表格」（JSON 数据）递给你的脚本，你的脚本填好「审批意见」（stdout JSON 或纯文本）再交回去，最终用「印章」（退出码）决定是放行、阻止还是报错。

Hook 脚本通过标准输入输出和退出码与 Claude Code 交互：

- **stdin**：Claude Code 以 JSON 格式传入事件上下文
- **stdout**：返回 JSON 决策（exit 0 时解析）或纯文本上下文
- **stderr**：返回错误信息，Claude 可以看到（exit 2 时作为阻止原因）
- **退出码**：决定行为走向

### Hook 输入增强

`PostToolUse` 和 `PostToolUseFailure` hook 的输入现在包含 `duration_ms` 字段（v2.1.119 新增），表示工具执行耗时（排除权限提示和 PreToolUse hooks 的时间）。这让你可以根据执行时间做条件判断——比如耗时超过 5 秒才触发通知，避免频繁打扰。

### PostToolUse 替换工具输出

`PostToolUse` hooks 可通过 `hookSpecificOutput.updatedToolOutput` 替换工具的输出内容。v2.1.118 起支持 MCP 工具输出的替换，v2.1.121 扩展到**所有工具**——包括 `Bash`、`Read`、`Grep` 等内置工具。这个能力在以下场景特别有用：

- **截断冗长输出**：工具输出太长时，用 Hook 提取关键信息再返回给 Claude
- **敏感信息过滤**：在工具输出进入上下文前，自动脱敏
- **格式转换**：将原始输出转换为更结构化的格式

退出码的含义：

| 退出码 | 含义 | Claude 的反应 |
|-------|------|-------------|
| `0` | 放行 | 解析 stdout 的 JSON（如有），继续执行 |
| `2` | 阻止 | stderr 内容作为反馈告知 Claude，Claude 可据此调整 |
| 其他 | 非阻塞错误 | stderr 内容仅记录日志，不中断流程 |

一个简单的阻止脚本示例：

``` bash title=".claude/hooks/block-rm.sh"
#!/bin/bash
# 读取 stdin 传入的 JSON 数据
COMMAND=$(jq -r '.tool_input.command')

# 检查是否包含危险命令
if echo "$COMMAND" | grep -q 'rm -rf'; then
  echo "危险命令被 Hook 拦截：$COMMAND" >&2  # stderr 会反馈给 Claude
  exit 2  # exit 2 = 阻止执行
fi

exit 0  # exit 0 = 放行
```

除了退出码，还可以通过 stdout 返回 JSON 来实现更精细的控制：

``` json title="stdout JSON 输出示例"
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "请使用 rg 替代 grep 以获得更好性能"
  }
}
```

`permissionDecision` 支持四个值：

| 值 | 效果 |
|----|------|
| `allow` | 跳过权限提示，直接执行。但**不会覆盖权限拒绝规则**——如果 `permissions.deny` 中有匹配的规则，即使 Hook 返回 `allow`，调用仍会被阻止 |
| `deny` | 阻止工具调用，把原因反馈给 Claude |
| `ask` | 正常弹出权限确认对话框 |
| `defer` | 仅在 `-p` 非交互模式下有效，暂停等待外部处理（Agent SDK 可收集输入后恢复） |

`permissionDecision` 只适用于 `PreToolUse` 事件。其他事件使用不同的决策模式——比如 `PostToolUse` 和 `Stop` Hook 使用顶级 `decision: "block"` 字段，`PermissionRequest` 使用 `hookSpecificOutput.decision.behavior`。

### 用 `updatedInput` 修改工具参数

`PreToolUse` Hook 不只能决定"执行还是阻止"，还能**修改工具的输入参数**后再放行。当你发现 Claude 调用的参数有问题，但不需要完全阻止时，这个功能特别有用。

例如，你想强制让所有 `Bash` 工具调用都加上 `rtk` 前缀：

``` json title="通过 updatedInput 修改工具参数"
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "updatedInput": {
      "command": "rtk npm test"
    }
  }
}
```

Claude Code 会用修改后的参数替换原始参数，然后继续执行。

!!! warning "多 Hook 竞争风险"

    当多个 `PreToolUse` Hook 同时返回 `updatedInput` 时，由于 Hook 是并行运行的，最后完成的那一个会"赢"——这是一个**非确定性**的结果。所以应避免让多个 Hook 修改同一个工具的输入。

### `PermissionRequest` 的 `updatedPermissions`

`PermissionRequest` Hook 的 JSON 输出除了 `behavior` 字段外，还可以包含 `updatedPermissions` 数组来动态切换权限模式。例如，当某个工具被调用时自动将会话切换到 `acceptEdits` 模式：

``` json title="PermissionRequest 切换权限模式"
{
  "hookSpecificOutput": {
    "hookEventName": "PermissionRequest",
    "decision": {
      "behavior": "allow",
      "updatedPermissions": [
        { "type": "setMode", "mode": "acceptEdits", "destination": "session" }
      ]
    }
  }
}
```

`destination: "session"` 表示仅在当前会话中生效。`mode` 支持的值包括 `default`、`acceptEdits` 和 `bypassPermissions`——但 `bypassPermissions` 有严格前提条件：会话必须已通过 `--dangerously-skip-permissions` 等方式启动了绕过模式，且未被 `permissions.disableBypassPermissionsMode` 禁用。

`hookSpecificOutput` 还支持事件特有的输出字段。例如 `UserPromptSubmit` Hook 可以返回 `sessionTitle` 来设置当前会话标题（v2.1.94 新增）：

``` json title="UserPromptSubmit 设置会话标题"
{
  "hookSpecificOutput": {
    "hookEventName": "UserPromptSubmit",
    "sessionTitle": "feat: 添加用户认证模块"
  }
}
```

## 🔧 Hook 能用来做什么？——实践指南

### CLAUDE.md + Skills + Hooks：三层叠加

这三层机制各有分工，单独使用任何一层都会有漏洞。放在一起才形成完整的治理体系：

| 层级 | 机制 | 做什么 | 特点 |
|------|------|--------|------|
| **规则层** | `CLAUDE.md` | 声明"提交前必须通过测试和 lint" | 建议性的，Claude 经常当没看见 |
| **流程层** | `Skills` | 告诉 Claude 在什么顺序下运行测试、如何看失败、如何修复 | 引导性的，但不强制执行 |
| **强制层** | `Hooks` | 对关键路径执行硬性校验，测试不过就阻止 | 确定性的，必然执行 |

一个实际例子——"提交前必须通过测试"：

- **只写 CLAUDE.md**：Claude 经常"忘了"跑测试就直接提交
- **再加 Skill**：Claude 知道该怎么跑测试了，但仍可能跳过
- **再加 Hook**：`Stop` 事件触发测试脚本，测试不通过直接阻止提交

⚠️ 实践经验表明，三层机制缺一不可。只靠 Hooks 做细节判断又不够——Hooks 擅长"做不做"，不擅长"怎么做"，"怎么做"还是交给 Skill。

### 适合 vs 不适合放入 Hooks

| ✅ 适合 | ❌ 不适合 |
|---------|----------|
| 阻断修改受保护文件（确定性的） | 需要读大量上下文的复杂语义判断 |
| Edit 后自动格式化 / lint / 轻量校验 | 长时间运行的业务流程 |
| SessionStart 后注入动态上下文（Git 分支、环境变量） | 需要多步推理和权衡的决策 |
| 任务完成后推送通知 | 需要灵活调整的工作流 |
| 提交前强制检查测试通过 | — |

💡 判断标准很简单：**这件事能不能交给 Claude 临场发挥？** 如果不能（比如保护文件不被改、测试必须通过），就用 Hook 收回到确定性流程里。

### 文件编辑后自动格式化

这是最常见的 Hook 用法之一。每次 Claude 编辑或写入文件后，自动运行格式化工具，确保代码风格一致。

``` json title=".claude/settings.json"
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "jq -r '.tool_input.file_path' | xargs npx prettier --write"
          }
        ]
      }
    ]
  }
}
```

💡 这个 Hook 的工作原理：`jq -r '.tool_input.file_path'` 从 stdin 的 JSON 中提取被编辑文件的路径，然后传给 Prettier 格式化。因为使用 `PostToolUse`，格式化在文件写入之后执行，所以不会被阻止。

### 提交前自动运行测试

在 Claude 声称完成一个任务后，自动检查测试是否通过，防止"声称完成但测试挂了"的情况。

方法一：使用 `command` 类型（确定性规则）

``` json title=".claude/settings.json"
{
  "hooks": {
    "Stop": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/check-tests.sh"
          }
        ]
      }
    ]
  }
}
```

``` bash title=".claude/hooks/check-tests.sh"
#!/bin/bash
INPUT=$(cat)

# 防止无限循环：如果已经是 Stop Hook 触发的继续，就放行
if [ "$(echo "$INPUT" | jq -r '.stop_hook_active')" = "true" ]; then
  exit 0
fi

# 运行测试
if ! rtk mvn test -q 2>&1; then
  # exit 2 + stderr = 阻止 Claude 停止，把原因反馈给它
  echo "测试未通过，请修复失败的测试后再尝试结束" >&2
  exit 2
fi

exit 0
```

方法二：使用 `agent` 类型（让子代理验证）

``` json title=".claude/settings.json"
{
  "hooks": {
    "Stop": [
      {
        "hooks": [
          {
            "type": "agent",
            "prompt": "验证所有单元测试是否通过。运行测试套件并检查结果。$ARGUMENTS",
            "timeout": 120
          }
        ]
      }
    ]
  }
}
```

⚠️ 注意：`Stop` Hook 在 Claude 每次完成响应时都会触发，不仅仅是任务完成时。务必检查 `stop_hook_active` 字段，否则会导致无限循环。

### 工具执行前/后的自定义逻辑

#### 阻止编辑受保护文件

防止 Claude 修改 `.env`、`package-lock.json` 等敏感文件：

``` bash title=".claude/hooks/block-protected-files.sh"
#!/bin/bash
INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

# 定义受保护的文件模式
PROTECTED_PATTERNS=(
  ".env"
  "package-lock.json"
  ".git/"
  "credentials"
  "secret"
)

for PATTERN in "${PROTECTED_PATTERNS[@]}"; do
  if echo "$FILE_PATH" | grep -q "$PATTERN"; then
    echo "受保护文件不可修改：$FILE_PATH" >&2
    exit 2
  fi
done

exit 0
```

``` json title=".claude/settings.json"
{
  "hooks": {
    "PreToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/block-protected-files.sh"
          }
        ]
      }
    ]
  }
}
```

#### 压缩后重新注入上下文

当 Claude 的上下文窗口满了触发压缩时，重要信息可能丢失。用 `SessionStart` 的 `compact` matcher 在每次压缩后重新注入关键上下文：

``` json title=".claude/settings.json"
{
  "hooks": {
    "SessionStart": [
      {
        "matcher": "compact",
        "hooks": [
          {
            "type": "command",
            "command": "echo '提醒：本项目使用 Bun 而非 npm。提交前运行 bun test。当前冲刺：认证重构。'"
          }
        ]
      }
    ]
  }
}
```

💡 这里 stdout 输出的纯文本会直接注入 Claude 的上下文。你也可以用 `git log --oneline -5` 动态获取最近提交信息。

#### Claude 需要你关注时发送桌面通知

在 Claude 等待你的输入时弹出桌面通知，这样你可以切去做别的事：

``` json title=".claude/settings.json"
{
  "hooks": {
    "Notification": [
      {
        "matcher": "",
        "hooks": [
          {
            "type": "command",
            "command": "powershell.exe -Command \"[System.Reflection.Assembly]::LoadWithPartialName('System.Windows.Forms'); [System.Windows.Forms.MessageBox]::Show('Claude Code 需要你的关注', 'Claude Code')\"",
            "shell": "powershell"
          }
        ]
      }
    ]
  }
}
```

### 其他常用场景速查

#### 自动批准特定权限提示

跳过你总是允许的工具调用批准对话。比如自动批准 `ExitPlanMode`，这样每次计划准备好时不需要手动确认。与退出码方式不同，自动批准需要 Hook 通过 stdout 返回 JSON 决策：

``` json title=".claude/settings.json"
{
  "hooks": {
    "PermissionRequest": [
      {
        "matcher": "ExitPlanMode",
        "hooks": [
          {
            "type": "command",
            "command": "echo '{\"hookSpecificOutput\": {\"hookEventName\": \"PermissionRequest\", \"decision\": {\"behavior\": \"allow\"}}}'"
          }
        ]
      }
    ]
  }
}
```

⚠️ 保持 matcher 尽可能狭窄。匹配 `.*` 或留空会自动批准**每个**权限提示，包括文件写入和 Shell 命令。

#### 审计配置变更

跟踪会话期间设置或 Skills 文件何时变更，用于合规性检查或阻止未授权的修改：

``` json title="~/.claude/settings.json"
{
  "hooks": {
    "ConfigChange": [
      {
        "matcher": "",
        "hooks": [
          {
            "type": "command",
            "command": "jq -c '{timestamp: now | todate, source: .source, file: .file_path}' >> ~/claude-config-audit.log"
          }
        ]
      }
    ]
  }
}
```

#### 目录切换时重新加载环境

配合 direnv 等工具，在 Claude 切换工作目录时自动加载新的环境变量。`SessionStart` 和 `CwdChanged` 搭配使用，前者处理启动时的加载，后者处理运行时切换：

``` json title="~/.claude/settings.json"
{
  "hooks": {
    "SessionStart": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "direnv export bash > \"$CLAUDE_ENV_FILE\""
          }
        ]
      }
    ],
    "CwdChanged": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "direnv export bash > \"$CLAUDE_ENV_FILE\""
          }
        ]
      }
    ]
  }
}
```

两个 Hook 都将环境变量写入 `CLAUDE_ENV_FILE`，Claude Code 在每个 Bash 命令前作为脚本前缀运行它。

#### SessionEnd 的超时机制

当你想在会话结束时做清理工作（比如删除临时文件、上报统计数据），`SessionEnd` Hook 是自然的选择。但这里有一个容易踩的坑：Claude Code 不会无限等待你的清理脚本执行完毕。

`SessionEnd` Hook 的默认超时为 **1.5 秒**，最大可配置到 **60 秒**。如果你的清理脚本执行时间超过这个限制，它会被强制终止。对于大部分轻量清理工作，1.5 秒足够了；但如果需要上传文件或调用网络 API，记得显式设置 `timeout`：

``` json title=".claude/settings.json"
{
  "hooks": {
    "SessionEnd": [
      {
        "hooks": [
          {
            "type": "command",
            "command": "\"$CLAUDE_PROJECT_DIR\"/.claude/hooks/upload-coverage.sh",
            "timeout": 30
          }
        ]
      }
    ]
  }
}
```

### Hook 与权限模式的关系

理解 Hook 和权限模式的交互方式很重要，否则你可能会困惑"为什么我的 Hook 返回了 `allow` 但工具还是被阻止了"。

核心原则是：**Hook 只能收紧限制，不能放松超过权限规则允许的范围。**

- `PreToolUse` Hook 在任何权限模式检查**之前**触发。返回 `permissionDecision: "deny"` 的 Hook 会阻止工具，**即使在 `bypassPermissions` 模式或使用 `--dangerously-skip-permissions` 时也是如此**。这意味着你用 Hook 强制执行的策略，用户无法通过切换权限模式来绕过
- 反过来，返回 `"allow"` 的 Hook **不会绕过**来自设置文件的拒绝规则。如果 `permissions.deny` 中有匹配的规则，即使 Hook 返回 `allow`，调用仍会被阻止
- 当多个 `PreToolUse` Hook 同时返回不同的决策时，Claude Code 选择**最严格**的答案：一个返回 `deny` 的 Hook 会取消工具调用，无论其他 Hook 返回什么

### 安全批准对话框的触发条件

当 `PreToolUse` Hook 返回 `"ask"` 作为 `permissionDecision`，或者 Hook 没有返回决策（默认行为）时，Claude Code 会向用户展示安全批准对话框。以下情况总是会触发对话框，无论权限模式如何：

- 工具调用匹配了 `permissions.ask` 规则
- 工具调用没有被任何 Hook 明确批准（`allow`）或拒绝（`deny`）
- 企业托管策略中的拒绝规则匹配了工具调用（此时对话框显示拒绝信息，而非询问）

#### 常用场景速查表

| 场景 | 事件 | Matcher | 说明 |
|------|------|---------|------|
| 自动批准特定权限提示 | `PermissionRequest` | `ExitPlanMode` | 自动批准退出计划模式 |
| 记录所有 Bash 命令 | `PostToolUse` | `Bash` | 将命令追加到日志文件 |
| 清理临时文件 | `SessionEnd` | `clear` | 执行 `/clear` 时清理 |
| 审计配置变更 | `ConfigChange` | （空） | 记录谁改了什么配置 |
| 环境变量热加载 | `CwdChanged` | — | 切换目录时重新加载 direnv |
| 监听文件变化 | `FileChanged` | `.envrc\|.env` | 环境配置文件修改时刷新 |

## 💰 Hook 会消耗额外 Token 吗？

这是 Hook 和其他自动化机制（如 Skills、Subagents）之间的一个关键区别。

### 为什么 Hook 几乎是"免费的"

当你写一个 `PreToolUse` Hook 只是检查命令是否合法时：

```bash
#!/bin/bash
COMMAND=$(jq -r '.tool_input.command')
if echo "$COMMAND" | grep -q 'rm -rf'; then
  echo "不允许执行 rm -rf" >&2
  exit 2
fi
exit 0
```

这个脚本 exit 0 且没有向 stdout 输出任何内容。结果是什么？**Claude 的上下文窗口几乎不受影响**——Claude 只知道"这个工具调用被放行了"，不会收到任何额外文本。

### 什么时候会产生上下文成本

只有在以下情况下，Hook 才会向 Claude 的上下文注入内容：

- stdout 输出了**纯文本**（非 JSON）：文本被当作上下文注入
- stdout 输出了 JSON 且包含 `additionalContext` 字段：该字段内容注入上下文
- exit 2 阻止时：stderr 内容作为反馈告知 Claude

### 对比表格

| 场景 | 上下文影响 | 说明 |
|------|-----------|------|
| exit 0，无 stdout | 🟢 零成本 | Hook 静默放行，Claude 无感知 |
| exit 0，stdout 有 JSON（无 `additionalContext`） | 🟢 零成本 | JSON 被解析但无文本注入 |
| exit 0，stdout 有 `additionalContext` | 🟡 注入指定文本 | 文本上限 10,000 字符 |
| exit 0，stdout 有纯文本（`UserPromptSubmit`/`SessionStart`） | 🟡 注入全部文本 | 文本上限 10,000 字符 |
| exit 2，stderr 有内容 | 🟡 注入反馈文本 | Claude 据此调整策略 |
| exit 其他，stderr 有内容 | 🟢 零成本 | 仅在 verbose 模式（`Ctrl+O`）可见 |

💡 这意味着你可以放心注册大量"守门"型 Hook（检查、校验、拦截），只要它们在正常情况下不输出内容，就不会消耗 Claude 宝贵的上下文窗口。

### 使用 verbose 模式调试

按 `Ctrl+O` 可以切换 verbose 模式，在对话记录中看到每个 Hook 的执行细节（匹配了哪些 Hook、退出码、输出内容），方便排查问题。或者用 `claude --debug` 启动获取完整的调试日志。

## 版本更新记录

- v2.1.0 新增了 Hooks 在 Skill 和 Agent frontmatter 中的支持，以及 `once: true` 配置（Hook 只执行一次后自动停用）
- v2.1.69 为 Hook 事件新增了 `agent_id`（子代理）和 `agent_type`（子代理及 `--agent`）字段，以及 `worktree` 状态行字段
- v2.1.83 新增 `CwdChanged`（工作目录切换）和 `FileChanged`（文件变化监听）事件
- v2.1.84 新增 `TaskCreated` 事件，HTTP 类型扩展到 `WorktreeCreate`
- v2.1.85 `if` 字段支持按工具名称和参数精细过滤（需此版本或更高）
- v2.1.105 改进 `PreCompact` 事件的 exit code 2 行为
- v2.1.110 修复 `PermissionRequest` hook 返回 `updatedInput` 时未重新检查 `permissions.deny` 规则
- v2.1.110 修复 `PreToolUse` hook 的 `additionalContext` 在 tool 调用失败时被丢弃
- v2.1.110 安全加固："在编辑器中打开"操作防止不受信任的文件名注入命令

📝 小结：Hook 是 Claude Code 中"确定性自动化"的核心机制。通过在生命周期事件上注册脚本，你可以确保某些规则**始终被执行**，而不依赖模型的判断力。配置 Hook 的核心是选择正确的事件、设置合适的 matcher、然后编写一个快速且安静的脚本——快速是为了不阻塞 Claude，安静是为了不消耗上下文。
