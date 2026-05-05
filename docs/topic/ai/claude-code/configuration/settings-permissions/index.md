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

### 为什么单纯靠权限提示不够？

权限系统虽然能控制 Claude 可以做什么，但它有一个根本问题——每个 Bash 命令都需要你逐条确认。当你在做自动化任务时，这种「逐条审批」的模式会带来三个痛点：

- **审批疲劳**：反复点击「允许」后，你会不自觉地放松警惕
- **工作流中断**：每次确认都会打断开发节奏
- **自主性受限**：Claude 在等待审批时无法高效推进

沙箱（Sandbox）用另一种思路解决这个问题：与其逐条审批命令，不如预先划定一条明确的边界——边界内的操作自动放行，边界外的操作直接在操作系统层面被拦截。这样 Claude 可以在安全范围内自主工作，你也不用盯着每一条命令。

!!! warning "文件系统与网络隔离缺一不可"

    有效的沙箱必须**同时**启用文件系统隔离和网络隔离。如果没有网络隔离，被攻击的代理可能通过出站连接泄露你的 SSH 密钥等敏感文件；如果没有文件系统隔离，被攻击的代理可能通过修改系统文件来获得网络访问权限。配置沙箱时，务必确保不会在两个维度上留下绕过缺口。

### 工作原理

沙箱的核心思路是利用操作系统自带的安全原语，在内核层面限制进程的文件系统访问和网络连接。这意味着隔离不仅对 Claude 的直接操作生效，对它启动的所有子进程（如 `kubectl`、`terraform`、`npm` 等）同样有效。

#### 文件系统隔离

沙箱对文件系统的访问做了分层控制：

- **默认写入范围**：仅允许读写当前工作目录及其子目录
- **默认读取范围**：允许读取整个文件系统，少数敏感路径除外
- **越界拦截**：在操作系统层面阻止对工作目录外文件的修改，且这一限制对所有子进程生效

当子进程需要在项目目录外写入时（比如 `kubectl` 需要更新 `~/.kube/config`），你可以通过 `sandbox.filesystem.allowWrite` 显式授权——这比把整个命令排除出沙箱更安全。

#### 网络隔离

网络访问通过在沙箱外运行的代理服务器进行控制：

- **域名白名单**：只能连接到明确允许的域名
- **请求提示**：首次遇到新域名时会触发权限确认（除非启用了 `allowManagedDomainsOnly`，此时未允许的域名直接拦截）
- **全面覆盖**：限制对所有子进程、脚本和网络调用生效

#### 操作系统级强制

| 操作系统 | 底层技术 | 隔离能力 |
|---------|---------|---------|
| macOS | Seatbelt（`sandbox-exec`） | 文件系统 + 网络 |
| Linux | [bubblewrap](https://github.com/containers/bubblewrap)（容器级隔离） | 文件系统 + 网络 + 进程 |
| WSL2 | bubblewrap（与 Linux 相同） | 文件系统 + 网络 + 进程 |
| WSL1 | ❌ 不支持 | bubblewrap 需要仅在 WSL2 中可用的内核命名空间原语 |
| Windows 原生 | ❌ 暂不支持 | 计划未来支持 |

macOS 开箱即用（内置 Seatbelt 框架）。Linux 和 WSL2 需要手动安装依赖：

``` bash title="Ubuntu/Debian 安装依赖"
sudo apt-get install bubblewrap socat
```

``` bash title="Fedora 安装依赖"
sudo dnf install bubblewrap socat
```

在 WSL2 上有一个额外限制：沙箱化的命令无法启动 Windows 二进制文件（如 `cmd.exe`、`powershell.exe` 或 `/mnt/c/` 下的任何程序）。原因是 WSL 通过 Unix Socket 将这些调用转发给 Windows 主机，而沙箱会阻止这类 Socket 连接。如果确实需要调用 Windows 二进制文件，请将其加入 `excludedCommands` 让它在沙箱外运行。

### 启用沙箱

在 Claude Code 中输入 `/sandbox` 命令即可打开沙箱配置菜单。菜单会根据当前平台显示可用选项，如果缺少依赖（如 Linux 上的 `bubblewrap`），会给出安装指引。

默认情况下，如果沙箱无法启动（缺少依赖或不支持的平台），Claude Code 会显示警告并回退到无沙箱模式运行。如果你需要确保沙箱必须生效（例如企业托管环境），将 `sandbox.failIfUnavailable` 设为 `true`——此时沙箱启动失败会直接报错退出。

### 沙箱模式

沙箱提供两种运行模式，两种模式下文件系统和网络限制完全一致，区别只在于沙箱化命令是否需要权限确认：

**自动允许模式**：在沙箱内运行的 Bash 命令自动放行，无需逐条确认。如果命令因沙箱限制无法执行（如需要访问未允许的网络域名），会回退到常规权限流程。显式的 `deny` 规则始终生效，对 `/`、主目录或关键系统路径执行 `rm`/`rmdir` 仍会触发权限提示。

**常规权限模式**：所有 Bash 命令都走标准权限流程，即使已经沙箱化了。控制更精细，但需要更多确认操作。

!!! info "自动允许与权限模式独立工作"

    自动允许模式独立于你的权限模式（`default`、`acceptEdits` 等）。即使你不在 `acceptEdits` 模式下，启用自动允许后沙箱内的 Bash 命令也会自动运行。这意味着在沙箱边界内修改文件的 Bash 命令会直接执行而不提示，即使文件编辑工具（Edit）本身通常需要确认。

### 沙箱配置结构

沙箱通过 `sandbox` 对象统一配置。最简配置只需启用沙箱：

``` json title="最简沙箱配置"
{
  "sandbox": {
    "enabled": true
  }
}
```

这已经能提供文件系统隔离（限制在工作目录内写入）和网络隔离（通过代理控制）。当你需要更精细的控制时，可以逐步引入文件系统、网络和逃生舱等配置：

``` json title="完整沙箱配置"
{
  "sandbox": {
    "enabled": true,
    "autoAllowBashIfSandboxed": true,
    "failIfUnavailable": false,
    "excludedCommands": ["docker *"],
    "allowUnsandboxedCommands": true,
    "filesystem": {
      "allowWrite": ["/tmp/build", "~/.kube"],
      "denyWrite": ["/etc", "/usr/local/bin"],
      "denyRead": ["~/.aws/credentials", "~/.ssh"],
      "allowRead": ["."]
    },
    "network": {
      "allowedDomains": ["github.com", "*.npmjs.org"],
      "deniedDomains": ["sensitive.cloud.example.com"]
    }
  }
}
```

各顶层配置项说明：

| 字段 | 说明 | 默认值 |
|------|------|--------|
| `sandbox.enabled` | 启用沙箱 | `false` |
| `sandbox.autoAllowBashIfSandboxed` | 沙箱内自动批准 Bash 命令 | `true` |
| `sandbox.failIfUnavailable` | 沙箱无法启动时报错退出（而非警告后回退） | `false` |
| `sandbox.excludedCommands` | 不在沙箱内运行的命令（在沙箱外以常规权限流程执行） | `[]` |
| `sandbox.allowUnsandboxedCommands` | 是否允许命令通过 `dangerouslyDisableSandbox` 参数跳过沙箱 | `true` |

`autoAllowBashIfSandboxed` 默认为 `true`，因为沙箱本身已经提供了隔离，命令不需要再逐条确认。企业环境可以设为 `false` 以获得更严格的审批控制。

### 文件系统限制

沙箱可以精细控制 Claude 及其子进程的文件读写权限：

| 字段 | 说明 |
|------|------|
| `filesystem.allowWrite` | 允许写入的额外路径（追加合并到项目目录之上） |
| `filesystem.denyWrite` | 禁止写入的路径（追加合并） |
| `filesystem.denyRead` | 禁止读取的路径（追加合并） |
| `filesystem.allowRead` | 在 `denyRead` 区域内重新允许读取的路径（优先于 `denyRead`） |

路径前缀决定了路径的解析方式：

| 前缀 | 含义 | 示例 |
|------|------|------|
| `/` | 从文件系统根目录的绝对路径 | `/tmp/build` 保持为 `/tmp/build` |
| `~/` | 相对于主目录 | `~/.kube` 解析为 `$HOME/.kube` |
| `./` 或无前缀 | 相对于项目根目录（项目设置）或 `~/.claude`（用户设置） | 项目设置中 `./output` 解析为 `<project-root>/output` |

注意沙箱路径与权限规则路径的语法差异：沙箱文件系统路径使用标准约定——`/tmp/build` 是绝对路径，`./output` 是项目相对路径；而 `Read`/`Edit` 权限规则中 `//path` 表示绝对路径，`/path` 表示项目相对路径。如果你之前在沙箱中使用单斜杠 `/path` 期望项目相对解析，请改为 `./path`。

#### 多层级路径合并

当 `allowWrite`、`denyWrite`、`denyRead`、`allowRead` 在多个设置层级（托管设置、用户设置、项目设置）中同时定义时，数组会被**合并**而非替换。例如托管设置允许写入 `/opt/company-tools`，你在个人设置中添加 `~/.kube`，最终两个路径都生效。这意味着用户和项目可以在不覆盖上级配置的前提下扩展路径列表。

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

这个示例中 `allowRead` 的 `.` 解析为项目根目录（因为配置位于项目设置中）。如果把同样的配置放在 `~/.claude/settings.json` 中，`.` 会解析为 `~/.claude`，项目文件仍会被 `denyRead` 规则阻止。

!!! tip "不兼容沙箱的工具"

    并非所有命令都与沙箱开箱兼容：

    - `watchman` 与沙箱不兼容。如果你运行 `jest`，请使用 `jest --no-watchman`
    - `docker` 与沙箱不兼容。在 `excludedCommands` 中指定 `docker *` 让它在沙箱外运行
    - 许多 CLI 工具需要访问特定主机，首次使用时会请求权限，授予权限后即可在沙箱内安全执行

### 网络限制

沙箱可以限制 Claude 的网络访问：

| 字段 | 说明 |
|------|------|
| `network.allowedDomains` | 允许出站流量的域名（支持通配符，如 `*.example.com`） |
| `network.deniedDomains` | 阻止出站流量的域名（优先于 `allowedDomains`） |
| `network.httpProxyPort` | 自定义 HTTP 代理端口（高级网络安全场景） |
| `network.socksProxyPort` | 自定义 SOCKS 代理端口（高级网络安全场景） |

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

`deniedDomains` 的优先级高于 `allowedDomains`。例如允许 `*.example.com` 的同时禁止 `internal.example.com`，两者同时生效。

对于需要高级网络安全的组织，可以配置自定义代理来解密检查 HTTPS 流量、应用自定义过滤规则、记录所有网络请求：

``` json title="自定义代理配置"
{
  "sandbox": {
    "network": {
      "httpProxyPort": 8080,
      "socksProxyPort": 8081
    }
  }
}
```

!!! warning "域名的安全边界"

    网络过滤系统通过限制进程可以连接的域名来工作，但不会以其他方式检查通过代理的流量。用户需要确保只允许受信任的域名。此外，允许过于宽泛的域名（如 `github.com`）可能存在数据泄露风险，且在特定条件下可能通过 [domain fronting](https://en.wikipedia.org/wiki/Domain_fronting) 绕过网络过滤。

### 沙箱逃生舱：`dangerouslyDisableSandbox`

Claude Code 内置了一个有意的逃生舱机制。当命令因沙箱限制失败时（例如网络连接问题或不兼容的工具），Claude 会分析失败原因，并可能使用 `dangerouslyDisableSandbox` 参数重试命令。使用此参数的命令会脱离沙箱，走常规权限流程（需要用户确认）。

这个设计是为了处理某些工具或网络操作确实无法在沙箱约束内运行的边界情况。如果你需要完全禁止这种逃生行为，将 `allowUnsandboxedCommands` 设为 `false`——此时 `dangerouslyDisableSandbox` 参数会被完全忽略，所有命令必须在沙箱内运行或通过 `excludedCommands` 明确排除。

### 沙箱与权限的协同关系

沙箱和权限系统是互补的两个安全层，它们各自在不同维度发挥作用：

| 维度 | 权限系统 | 沙箱 |
|------|---------|------|
| **控制对象** | 所有工具（Bash、Read、Edit、WebFetch、MCP 等） | 仅 Bash 命令及其子进程 |
| **评估时机** | 工具执行之前 | 操作系统级别实时拦截 |
| **生效层级** | Claude Code 应用层 | 操作系统内核层 |
| **绕过难度** | 权限漏洞可能导致绕过 | 需要内核级漏洞才能绕过 |

来自 `sandbox.filesystem` 的路径和权限规则中的路径会被**合并**到最终的沙箱配置中。例如 `Read`/`Edit` 的 `deny` 规则会被合并到 `denyRead` 中，`WebFetch` 的域名规则与沙箱的 `allowedDomains`/`deniedDomains` 共同决定网络访问权限。

### 沙箱的安全边界

了解沙箱能保护什么、不能保护什么，才能正确使用它。

**安全优势**——即使攻击者通过提示注入成功操控了 Claude 的行为，沙箱也能确保系统安全：

- **文件系统保护**：无法修改 `~/.bashrc` 等关键配置文件；无法修改 `/bin/` 中的系统文件；无法读取被权限设置拒绝的文件
- **网络保护**：无法向攻击者控制的服务器泄露数据；无法从未授权的域名下载恶意脚本；无法向未批准的服务发起 API 调用
- **透明监控**：所有越界访问尝试在操作系统级别被拦截；当边界被试探时会立即通知你

**不涵盖的内容**——沙箱只隔离 Bash 子进程，其他 Claude Code 工具在不同的安全边界下运行：

- **内置文件工具**（`Read`、`Edit`、`Write`）：直接使用权限系统控制，不经过沙箱
- **计算机使用**（Computer Use）：当 Claude 打开应用并操控屏幕时，它在你的真实桌面上运行，而非隔离环境中

**安全限制**——除了前面提到的域名过滤边界和 domain fronting 风险，还有几点需要关注：

- **Unix Socket 权限提升**：`allowUnixSockets` 配置可能无意中授予对强大系统服务的访问。例如允许 `/var/run/docker.sock` 实际上通过 Docker Socket 授予了对主机系统的完全访问权，这等同于沙箱绕过。使用此配置时务必审慎评估每个 Socket 的安全影响。

- **文件系统权限提升**：过于宽泛的写入权限可能导致提权攻击。允许写入包含 `$PATH` 中可执行文件的目录、系统配置目录或用户 shell 配置文件（`.bashrc`、`.zshrc`）可能导致在其他用户或系统进程的安全上下文中执行恶意代码。

- **Linux 嵌套沙箱**：Linux 实现提供了一个 `enableWeakerNestedSandbox` 模式，使其能在 Docker 容器内运行而无需特权命名空间。但此选项会显著削弱安全性，仅在已有其他隔离措施时才应使用。

- **性能开销**：沙箱的性能开销很小，但某些文件系统操作可能略慢。

### 环境变量补充

#### `CLAUDE_CODE_SUBPROCESS_ENV_SCRUB`

Claude Code 的子进程默认不会继承所有父进程环境变量。`CLAUDE_CODE_SUBPROCESS_ENV_SCRUB` 环境变量控制哪些环境变量会被从子进程环境中清除，防止子进程（如被注入的恶意脚本）读取到凭证等敏感信息。

``` bash
# 启用子进程环境变量清除
export CLAUDE_CODE_SUBPROCESS_ENV_SCRUB=1
```

实际效果：启用后，Claude 启动的所有 Bash 子进程将无法读取 `ANTHROPIC_API_KEY`、`AWS_SECRET_ACCESS_KEY` 等敏感环境变量。在 Linux 上还会在隔离的 PID 命名空间中运行子进程，进一步增强安全性。

``` json title="或在 settings.json 中配置"
{
  "env": {
    "CLAUDE_CODE_SUBPROCESS_ENV_SCRUB": "1"
  }
}
```

#### `CLAUDE_CODE_SCRIPT_CAPS`

`CLAUDE_CODE_SCRIPT_CAPS` 环境变量用于限制脚本调用的次数，防止失控的脚本通过无限循环或大量子进程消耗系统资源。

``` bash
# 限制 deploy.sh 最多调用 2 次
export CLAUDE_CODE_SCRIPT_CAPS='{"deploy.sh": 2}'
```

### 开源沙箱运行时

沙箱运行时作为开源 npm 包发布，你可以在自己的 AI 代理项目中复用它。例如，要沙箱化一个 MCP 服务器：

``` bash
npx @anthropic-ai/sandbox-runtime <command-to-sandbox>
```

实现细节和源代码参见 [GitHub 仓库](https://github.com/anthropic-experimental/sandbox-runtime)。

### 最佳实践

- **从最小权限开始**：先用最严格的配置，根据实际需求逐步放宽
- **监控违规尝试**：查看沙箱拦截记录，了解 Claude 尝试访问哪些被限制的资源
- **区分环境**：开发环境和生产环境使用不同的沙箱规则
- **与权限配合**：将沙箱与 IAM 策略、权限规则结合，构建纵深防御
- **验证配置**：确认沙箱设置不会阻止合法的工作流程
- **优先用 `allowWrite` 而非 `excludedCommands`**：前者只放开文件写入权限，后者让整个命令脱离沙箱保护
