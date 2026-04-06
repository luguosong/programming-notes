---
title: Settings 与权限
description: settings.json 配置结构、六种权限模式、沙箱安全机制
---

# Settings 与权限

**🎯 本文你会学到**：

- ⚙️ settings.json 的层级结构与常用配置项
- 🔐 六种权限模式及其适用场景
- 🛡️ 沙箱安全机制（操作系统级隔离）

## ⚙️ settings.json 能配什么？

### 结构示例

每个层级的 `settings.json` 都遵循相同的 JSON 格式：

``` json title="示例：.claude/settings.json"
{
  "permissions": {
    "allow": [
      "Bash(git status)",
      "Bash(git diff*)",
      "Read",
      "Write"
    ],
    "deny": [
      "Bash(rm -rf *)"
    ]
  },
  "env": {
    "MY_CUSTOM_VAR": "value"
  },
  "outputStyle": "Explanatory",
  "fastMode": false
}
```

### 常用配置项速查

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| `permissions.allow` | 允许的工具操作 | `["Bash(git *)", "Read"]` |
| `permissions.deny` | 禁止的工具操作 | `["Bash(rm -rf *)"]` |
| `env` | 环境变量 | `{"NODE_VERSION": "20"}` |
| `outputStyle` | 输出风格 | `"Explanatory"`, `"Learning"` |
| `fastMode` | 快速模式 | `true` / `false` |
| `model` | 默认模型 | `"claude-sonnet-4-20250514"` |
| `language` | 响应语言 | `"chinese"` |
| `voiceEnabled` | 语音输入 | `true` / `false` |
| `sandboxType` | 沙箱类型 | `"none"`, `"readonly"` |
| `disableAllHooks` | 禁用所有 Hook | `true` / `false` |

## 🔐 Claude 能自主操作到什么程度？——权限系统

### 为什么需要权限控制？

Claude Code 可以执行 shell 命令、读写文件——这些操作如果不受约束，可能会误删文件或执行恶意代码。权限系统就是给 Claude 的操作加上「红绿灯」。

### 权限规则语法

权限规则控制 Claude 对特定工具的访问。格式为 `工具名(参数匹配模式)`：

``` json title="权限规则示例"
{
  "permissions": {
    "allow": [
      "Bash(git status)",
      "Bash(git diff*)",
      "Bash(git commit*)",
      "Read",
      "Write",
      "Edit"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(sudo *)"
    ]
  }
}
```

| 规则语法 | 含义 |
|---------|------|
| `Bash(git status)` | 精确匹配：只允许 `git status` |
| `Bash(git diff*)` | 通配符匹配：允许所有以 `git diff` 开头的命令 |
| `Read` | 允许整个 Read 工具 |
| `Bash(rm -rf *)` | 禁止所有 `rm -rf` 命令 |

💡 你还可以使用 `Bash(git log:*)` 来匹配包含空格后接任意内容的命令，如 `git log --oneline`。

### 六种权限模式

Claude Code 提供六种权限模式，从最保守到最宽松依次为：

| 模式 | 行为 | 适用场景 |
|------|------|---------|
| `default` | 读写文件自动允许，shell 命令需要确认 | 日常开发 |
| `acceptEdits` | 文件编辑自动允许，shell 命令需要确认 | 信任代码修改 |
| `plan` | 只读模式，所有写入操作需要确认 | 代码审查 |
| `auto` | 大部分操作自动执行，高危操作仍需确认 | 自动化任务 |
| `dontAsk` | 不询问，自动执行所有操作 | CI/CD 环境 |
| `bypassPermissions` | 绕过所有权限检查 | ⚠️ 极其危险，仅限调试 |

⚠️ `bypassPermissions` 模式会完全禁用安全保护，Claude 可以执行任何命令而不需要你的确认。除非你非常清楚自己在做什么，否则不要使用。

### 受保护路径

无论权限模式如何，以下路径始终受到保护，Claude 不会在这些位置执行写入操作：

- `~/.claude/`（Claude 配置目录）
- `/etc/sudoers`（系统 sudoers 文件）
- `/etc/ssh/`（SSH 配置目录）
- `~/.ssh/`（用户 SSH 目录）
- `~/.gnupg/`（GPG 密钥目录）

### 通过 CLI 切换权限模式

你可以通过命令行参数在启动时指定权限模式：

``` bash
# 只读模式
claude --permission-mode plan

# 自动模式
claude --permission-mode auto
```

也可以在会话中使用 `/permissions` 命令查看和切换当前模式。

## 🛡️ 怎么防止 Claude 执行危险命令？——沙箱安全

### 什么是沙箱？

沙箱（Sandbox）就像给 Claude Code 套了一个「隔离舱」——它在里面可以正常工作，但无法触碰隔离舱外面的东西。即使 Claude 执行了恶意命令，影响范围也被限制在沙箱内。

### 工作原理

Claude Code 使用操作系统级别的安全机制来实现沙箱：

| 操作系统 | 底层技术 | 隔离能力 |
|---------|---------|---------|
| macOS | Seatbelt（`sandbox-exec`） | 文件系统 + 网络 |
| Linux | bubblewrap（容器级隔离） | 文件系统 + 网络 + 进程 |
| Windows | ❌ 暂不支持 | — |

### 两种沙箱模式

``` json title="settings.json 中配置沙箱"
{
  "sandboxType": "readonly"
}
```

| 模式 | 文件系统 | 网络 | 适用场景 |
|------|---------|------|---------|
| `none`（默认） | 无限制 | 无限制 | 日常开发 |
| `readonly` | 只读访问项目目录 | 无限制 | 代码审查、学习 |

💡 在 `readonly` 模式下，Claude 仍然可以通过写入临时文件来执行构建等操作，但它无法修改项目文件。这对代码审查场景非常实用。

⚠️ 目前 Windows 暂不支持沙箱。如果你使用 WSL 2（Ubuntu），则可以使用 Linux 的 bubblewrap 沙箱。

### 沙箱网络限制

在 macOS 和 Linux 上，沙箱还可以限制网络访问。你可以配置允许访问的域名白名单：

``` json title="限制网络访问"
{
  "sandboxNetwork": {
    "outbound": "allow",
    "allowedHosts": ["api.example.com", "registry.npmjs.org"]
  }
}
```
