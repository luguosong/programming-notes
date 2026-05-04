---
title: Settings 与权限
description: settings.json 配置结构、六种权限模式、沙箱安全机制
---

# Settings 与权限

**本文你会学到**：

- ⚙️ settings.json 的层级结构与常用配置项
- 🔐 六种权限模式及其适用场景
- 🛡️ 沙箱安全机制（操作系统级隔离）

## ⚙️ settings.json 能配什么？

### 结构示例

每个层级的 `settings.json` 都遵循相同的 JSON 格式：

``` json title="示例：.claude/settings.json"
{
  "$schema": "https://json.schemastore.org/claude-code-settings.json",
  "permissions": {
    "allow": [
      "Bash(npm run lint)",
      "Bash(npm run test *)",
      "Read"
    ],
    "deny": [
      "Bash(curl *)",
      "Read(./.env)",
      "Read(./secrets/**)"
    ]
  },
  "env": {
    "MY_CUSTOM_VAR": "value"
  },
  "outputStyle": "Explanatory"
}
```

💡 在文件头部添加 `$schema` 字段可以在 VS Code、Cursor 等编辑器中获得**自动补全和内联验证**。已发布的 schema 会定期更新，但可能不包含最新版本的全部设置。

### 常用配置项速查

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| `$schema` | JSON Schema 引用（编辑器自动补全） | `"https://json.schemastore.org/claude-code-settings.json"` |
| `permissions.allow` | 允许的工具操作 | `["Bash(git *)", "Read"]` |
| `permissions.deny` | 禁止的工具操作 | `["Bash(rm -rf *)", "Read(./.env)"]` |
| `permissions.defaultMode` | 默认权限模式 | `"acceptEdits"` |
| `env` | 环境变量 | `{"NODE_VERSION": "20"}` |
| `model` | 默认模型 | `"claude-sonnet-4-20250514"` |
| `language` | 响应语言 | `"chinese"` |
| `sandbox` | 沙箱配置（对象格式） | 见[沙箱配置结构](#沙箱配置结构) |
| `attribution` | Git 提交/PR 归属 | `{"commit": "...", "pr": ""}` |
| `disableAllHooks` | 禁用所有 Hook | `true` / `false` |

!!! tip "归属设置（attribution）"

    Claude Code 默认在 git 提交中添加 `Co-Authored-By` 信息，在 PR 描述中添加生成标记。你可以通过 `attribution` 自定义或禁用：

    ``` json
    {
      "attribution": {
        "commit": "Generated with AI\n\nCo-Authored-By: AI <ai@example.com>",
        "pr": ""
      }
    }
    ```

    将 `commit` 或 `pr` 设为空字符串即可隐藏对应归属。此设置优先于已弃用的 `includeCoAuthoredBy`。

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
      "Bash(npm run test *)",
      "Read",
      "Write",
      "Edit"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(sudo *)",
      "Read(./.env)",
      "Read(./secrets/**)",
      "WebFetch"
    ]
  }
}
```

| 规则语法 | 含义 |
|---------|------|
| `Bash(git status)` | 精确匹配：只允许 `git status` |
| `Bash(git diff*)` | 通配符匹配：允许所有以 `git diff` 开头的命令 |
| `Bash(npm run test *)` | 通配符匹配：允许 `npm run test` 后接任意参数 |
| `Read` | 允许整个 Read 工具 |
| `Read(./.env)` | 精确匹配：禁止读取 `.env` 文件 |
| `Read(./secrets/**)` | 通配符匹配：禁止读取 `secrets/` 下所有文件 |
| `Edit(./config/production.json)` | 精确匹配：禁止编辑生产配置文件 |
| `WebFetch` | 完全禁止网络获取 |
| `WebFetch(domain:example.com)` | 只允许获取 `example.com` 域名 |
| `Bash(rm -rf *)` | 禁止所有 `rm -rf` 命令 |

💡 权限规则按顺序评估：先检查 `deny`，再检查 `allow`。第一个匹配的规则获胜。`deny` 规则的优先级最高——即使在 `allow` 中也允许了，`deny` 中匹配到的操作仍然被阻止。

💡 你还可以使用 `Bash(git log:*)` 来匹配包含空格后接任意内容的命令，如 `git log --oneline`。

!!! warning "v2.1.98 安全修复"

    v2.1.98 修复了多个权限绕过漏洞，包括：
    
    - 反斜杠转义标志可能被自动允许为只读，导致任意代码执行
    - 复合 Bash 命令绕过 `auto` 和 `bypass-permissions` 模式下的强制权限提示
    - `Bash(cmd:*)` 和 `Bash(git commit *)` 通配符规则无法匹配含多余空格或制表符的命令
    - 权限规则名称匹配 JavaScript 原型属性（如 `toString`）导致 `settings.json` 被静默忽略
    
    建议尽快升级到 v2.1.98 或更高版本。

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

### 受保护路径与敏感文件排除

无论权限模式如何，以下路径始终受到保护，Claude 不会在这些位置执行写入操作：

- `~/.claude/`（Claude 配置目录）
- `/etc/sudoers`（系统 sudoers 文件）
- `/etc/ssh/`（SSH 配置目录）
- `~/.ssh/`（用户 SSH 目录）
- `~/.gnupg/`（GPG 密钥目录）

对于包含敏感信息的项目文件（如 API 密钥、环境变量），推荐在项目的 `.claude/settings.json` 中使用 `deny` 规则排除：

``` json title=".claude/settings.json"
{
  "permissions": {
    "deny": [
      "Read(./.env)",
      "Read(./.env.*)",
      "Read(./secrets/**)",
      "Read(./config/credentials.json)"
    ]
  }
}
```

匹配这些模式的文件被排除在文件发现和搜索结果之外，对这些文件的读取操作会被拒绝。

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

### 沙箱配置结构

沙箱通过 `sandbox` 对象统一配置，包含文件系统和网络两大维度的精细控制：

``` json title="settings.json 中配置沙箱"
{
  "sandbox": {
    "enabled": true,
    "autoAllowBashIfSandboxed": true,
    "excludedCommands": ["docker *"],
    "filesystem": {
      "allowWrite": ["/tmp/build", "~/.kube"],
      "denyRead": ["~/.aws/credentials"]
    },
    "network": {
      "allowedDomains": ["github.com", "*.npmjs.org"],
      "deniedDomains": ["sensitive.cloud.example.com"]
    }
  }
}
```

各配置项说明：

| 字段 | 说明 | 默认值 |
|------|------|--------|
| `sandbox.enabled` | 启用沙箱 | `false` |
| `sandbox.autoAllowBashIfSandboxed` | 沙箱内自动批准 Bash 命令 | `true` |
| `sandbox.failIfUnavailable` | 沙箱无法启动时报错退出 | `false` |
| `sandbox.excludedCommands` | 不在沙箱内运行的命令 | `[]` |
| `sandbox.allowUnsandboxedCommands` | 允许命令通过 `dangerouslyDisableSandbox` 跳过沙箱 | `true` |

💡 `autoAllowBashIfSandboxed` 默认为 `true`——因为沙箱本身已经提供了隔离，所以命令不需要再逐条确认。企业环境可以设为 `false` 以获得更严格的控制。

### 文件系统限制

沙箱可以精细控制 Claude 的文件读写权限：

| 字段 | 说明 |
|------|------|
| `filesystem.allowWrite` | 允许写入的额外路径（追加合并） |
| `filesystem.denyWrite` | 禁止写入的路径（追加合并） |
| `filesystem.denyRead` | 禁止读取的路径（追加合并） |
| `filesystem.allowRead` | 在 `denyRead` 区域内重新允许读取的路径（优先于 `denyRead`） |

路径支持以下前缀：

| 前缀 | 含义 | 示例 |
|------|------|------|
| `/` | 绝对路径 | `/tmp/build` |
| `~/` | 相对于主目录 | `~/.kube` 变为 `$HOME/.kube` |
| `./` 或无前缀 | 相对于项目根目录 | `./output` |

``` json title="文件系统限制示例"
{
  "sandbox": {
    "enabled": true,
    "filesystem": {
      "allowWrite": ["/tmp/build", "~/.kube"],
      "denyWrite": ["/etc", "/usr/local/bin"],
      "denyRead": ["~/.aws/credentials", "~/.ssh"],
      "allowRead": ["."]
    }
  }
}
```

### 网络限制

沙箱可以限制 Claude 的网络访问：

| 字段 | 说明 |
|------|------|
| `network.allowedDomains` | 允许出站流量的域名（支持通配符，如 `*.example.com`） |
| `network.deniedDomains` | 阻止出站流量的域名（优先于 `allowedDomains`） |

``` json title="网络限制示例"
{
  "sandbox": {
    "enabled": true,
    "network": {
      "allowedDomains": ["github.com", "*.npmjs.org", "registry.yarnpkg.com"],
      "deniedDomains": ["uploads.github.com"]
    }
  }
}
```

💡 `deniedDomains` 的优先级高于 `allowedDomains`。例如允许 `*.example.com` 的同时禁止 `internal.example.com`，两者同时生效。

⚠️ 目前 Windows 暂不支持沙箱。如果你使用 WSL 2（Ubuntu），则可以使用 Linux 的 bubblewrap 沙箱。
