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
| `postToolUse` | 工具调用后 | 结果检查、格式化 |
| `errorOccurred` | 发生错误时 | 错误通知、自动恢复 |

## 🛠️ 创建 Hook

在仓库的 `.github/hooks/` 文件夹中创建 JSON 文件（文件名可自由选择，建议用功能命名如 `format.json`、`notify.json`）：

!!! tip "Hook 可以提交到 Git"

    与 Git Hook 不同（`.git/hooks/` 不被版本控制），Copilot Hook 放在 `.github/hooks/` 中，可以提交到 Git 让团队共享。

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

| 字段 | 说明 |
|------|------|
| `event` | 触发事件名称 |
| `steps` | 步骤数组，每步包含一个 `command` |
| `steps[].command` | 要执行的 shell 命令 |

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

## ⚖️ Hook vs Git Hook

你可能已经熟悉 Git Hook（如 `pre-commit`），Copilot Hook 和它类似但作用于不同层面：

| 维度 | Copilot Hook | Git Hook |
|------|-------------|----------|
| `触发时机` | Copilot Agent 生命周期事件 | Git 操作事件（commit、push 等） |
| `配置方式` | `.github/hooks/*.json` | `.git/hooks/*`（或 Husky 等工具） |
| `提交到 Git` | ✅ | ❌（`.git/hooks/` 不跟踪） |
| `用途` | 扩展 Copilot 行为 | 代码质量门禁 |

两者可以互补：Git Hook 确保提交质量，Copilot Hook 增强 AI 辅助体验。
