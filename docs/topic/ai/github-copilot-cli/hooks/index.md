---
title: Hook 扩展
description: 通过 Hook 系统在工具调用生命周期中注入自定义逻辑
---

# Hook 扩展

**本文你会学到**：
- 🪝 Hook 是什么，解决什么问题
- ⏱️ 六种生命周期事件及其触发时机
- 🛠️ 如何编写和配置 Hook
- ⚖️ Hook 与 Git Hook 的区别

打个比方：Hook 就像是你给 Copilot 安装的`自动感应器`——在特定时刻（会话开始、工具调用前后、出错时）自动触发你预设的操作。比如，每次 Copilot 修改文件后自动运行格式化工具，每次会话结束时自动记录日志。

---

## ❓ 为什么需要 Hook？

没有 Hook 的情况下，Copilot 的行为只能通过 prompt 和指令来间接影响。但有些操作需要`强制执行`——比如"每次写完代码必须格式化"或"出错时必须通知我"。这些需求超出了 prompt 控制的范围，Hook 正是为此而生。

---

## ⏱️ 生命周期事件

| 事件 | 触发时机 | 典型用途 |
|------|---------|---------|
| `sessionStart` | 会话启动时 | 日志记录、环境初始化 |
| `sessionEnd` | 会话结束时 | 清理临时文件、发送统计 |
| `userPromptSubmitted` | 用户提交 prompt 后 | 输入验证、日志审计 |
| `preToolUse` | 工具调用前 | 权限检查、参数验证 |
| `postToolUse` | 工具调用成功后 | 结果检查、格式化 |
| `postToolUseFailure` | 工具调用失败后（1.0.15 新增） | 失败后的补救操作 |
| `errorOccurred` | 发生错误时 | 错误通知、自动恢复 |
| `notification` | 异步通知时（1.0.18 新增） | shell 命令完成、权限提示、Agent 完成 |
| `preCompact` | 上下文压缩前（1.0.5 新增） | 注入保留信息、执行清理脚本 |
| `subagentStart` | 子 Agent 启动时（1.0.7 新增） | 监控子 Agent 行为、注入上下文 |
| `PermissionRequest` | 权限请求时（1.0.16 新增） | 以编程方式批准或拒绝工具权限 |

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
| `matcher` | ❌ | 嵌套 matcher/hooks 结构，细粒度匹配工具名称（1.0.6 新增） |
| `permissionDecision` | ❌ | `preToolUse` 专用：设为 `allow` 时自动批准，设为 `ask` 时请求用户确认（1.0.4 / 1.0.18 改进） |
| `timeout` | ❌ | 步骤超时时间（秒），`timeoutSec` 的别名（1.0.2 新增） |

⚠️ 注意：`command` 中的命令会在当前项目根目录下执行。如果命令执行失败（非零退出码），后续步骤仍会继续执行——Hook 不会因为某一步失败而中断整个流程。

!!! info "跨工具兼容"

    Hook 配置文件格式兼容 VS Code、Claude Code 和 Copilot CLI（1.0.6 改进）——同一份 Hook 配置无需修改即可在三个工具中使用。支持 Claude Code 的嵌套 `matcher/hooks` 结构。

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
