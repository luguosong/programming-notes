---
title: 配置体系
description: 理解 Claude Code 的配置层级、权限系统、沙箱安全、模型选择、输出风格等核心配置能力
---

# 配置体系

**本文你会学到**：

- 🎯 理解 Claude Code 五层配置优先级的含义和作用范围
- 📝 CLAUDE.md 记忆文件的工作原理与自动记忆机制
- ⚙️ settings.json 的层级结构与常用配置项
- 🔐 六种权限模式及其适用场景
- 🛡️ 沙箱安全机制（操作系统级隔离）
- 🧠 模型配置：别名、思考强度、扩展上下文
- 🎨 输出风格定制与自定义风格创建
- ⚡ Fast Mode：用更高的成本换取更快的响应
- 📊 状态栏、语音输入与企业网络配置

## 为什么 Claude Code 的配置很重要？

想象一下：你刚入职一家公司，有公司制度（所有员工都要遵守）、部门规章（你所在部门的额外规则）、还有你个人的工作习惯。这三层规则共同决定了你「能做什么」和「该怎么做」。

Claude Code 的配置体系也是同样的道理——它有五层配置，从「管理员强制执行的安全策略」到「你个人的偏好」，层层叠加、逐级覆盖。理解这个体系，你就掌握了让 Claude Code 按你的意愿高效工作的钥匙。

## 配置层级总览

### 五层优先级

Claude Code 的所有配置都遵循相同的优先级规则：**数字越小优先级越高**。

| 优先级 | 层级 | 谁能修改 | 作用范围 | 典型用途 |
|--------|------|---------|---------|---------|
| 1（最高） | Managed（托管） | 系统管理员 | 整个组织 | 强制安全策略、合规要求 |
| 2 | CLI 参数 | 用户（启动时） | 单次会话 | 临时覆盖、调试 |
| 3 | Local（本地） | 用户 | 本机、本项目 | 个人偏好（不提交 git） |
| 4 | Project（项目） | 团队 | 当前项目 | 团队规范（提交 git 共享） |
| 5（最低） | User（用户） | 用户 | 所有项目 | 全局个人偏好 |

💡 把它想成 CSS 的优先级：`!important > inline style > ID > class > tag`。Managed 配置就像 `!important`，无论你怎么设置，它说了算。

### 配置文件位置

每一层都有对应的配置文件路径：

| 层级 | 配置文件路径 | 是否提交 git |
|------|-------------|-------------|
| Managed | 由系统管理器注入，路径因平台而异 | 不适用 |
| Local | `.claude/settings.local.json` | ❌ 不提交 |
| Project | `.claude/settings.json` | ✅ 提交共享 |
| User | `~/.claude/settings.json` | 不适用（全局） |

⚠️ 注意：`.claude/settings.local.json` 和 `.gitignore` 默认会忽略本地配置文件，所以你的个人偏好不会意外提交到仓库。

### 冲突解决规则

当多个层级对同一个配置项设置了不同的值时，**高优先级覆盖低优先级**。但有一个重要的例外：**权限规则（permissions）是追加的，不是覆盖的**。所有层级的 `allow` 规则会合并，而 `deny` 规则会从最严格的层级生效。

## CLAUDE.md：Claude 的「长期记忆」

### 什么是 CLAUDE.md？

CLAUDE.md 是 Claude Code 的「记忆文件」——每次对话开始时，Claude 会自动读取这些文件，把它们当作上下文的一部分。你可以把它理解为给 Claude 写的一封「长期指令信」。

### 四个作用域的 CLAUDE.md

和配置层级类似，CLAUDE.md 也有多个作用域：

| 作用域 | 文件路径 | 谁能看到 | 用途 |
|--------|---------|---------|------|
| Managed | 管理器注入 | 组织全员 | 强制性团队指令 |
| User（全局） | `~/.claude/CLAUDE.md` | 仅自己 | 全局偏好，适用于所有项目 |
| Project（项目） | 项目根目录 `CLAUDE.md` | 团队全员 | 项目规范，提交 git 共享 |
| Local（本地） | `.claude/CLAUDE.md` | 仅自己 | 个人偏好，不提交 git |

💡 一个实际例子：在 User CLAUDE.md 中写「回复使用简体中文」，在 Project CLAUDE.md 中写「本项目使用 Java 17」——Claude 会同时遵守这两个文件。

### `.claude/rules/` 目录组织

当 CLAUDE.md 文件变得很长时，可以用 `.claude/rules/` 目录来组织规则。Claude Code 会自动读取这个目录下所有 Markdown 文件，按文件名排序后拼接在一起。

```
.claude/
├── CLAUDE.md          # 本地规则
└── rules/
    ├── java-style.md  # Java 编码规范
    └── git-flow.md    # Git 工作流规范
```

对于 User 级别的规则，路径为 `~/.claude/rules/`。

### 自动记忆（Auto Memory）

Claude Code 还有一个更智能的机制：**自动记忆**。当 Claude 在对话中觉得某些信息对未来的会话有用时，它会自动写入 CLAUDE.md。

- 写入位置：User 级别的 `~/.claude/CLAUDE.md`（追加到末尾）
- 触发条件：Claude 判断这些信息值得记住（如项目架构偏好、常用命令等）
- 用户可控：你可以随时手动编辑或删除自动写入的内容

⚠️ 自动记忆默认开启。如果不想让 Claude 自动修改你的 CLAUDE.md，可以在设置中关闭。

## settings.json 详解

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

## 权限系统

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

## 沙箱安全

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

## 模型配置

### 模型别名

Claude Code 内置了一组简洁的模型别名，方便快速切换：

| 别名 | 实际模型 | 特点 |
|------|---------|------|
| `sonnet` | Claude Sonnet 4 | 平衡性能与成本 |
| `opus` | Claude Opus 4.6 | 最强能力，速度较慢 |
| `haiku` | Claude Haiku 3.5 | 最快速度，成本最低 |
| `opusplan` | Opus（用于规划）+ Sonnet（用于执行） | 规划用强模型，执行用快模型 |

💡 在会话中输入 `/model` 即可查看和切换模型。

### 思考强度（Effort Level）

思考强度控制 Claude 在回答前「思考多久」。你可以把它理解为考试时「打草稿的时间」：

| 级别 | 行为 | 适用场景 |
|------|------|---------|
| 高 | 深度推理，适合复杂问题 | 架构设计、复杂 Bug 排查 |
| 中（默认） | 适中的推理 | 日常开发 |
| 低 | 快速响应，减少思考时间 | 简单修改、代码格式化 |

在会话中输入 `/effort` 可以动态调整思考强度。

### 扩展上下文

Claude Code 的默认上下文窗口为 200K tokens。对于特别大的代码库或长对话，可以启用扩展上下文，将窗口扩展到 **1M tokens**（约 5 倍）。

``` bash
# 启动时指定扩展上下文
claude --model claude-sonnet-4-20250514 --max-tokens 1000000
```

⚠️ 扩展上下文会增加输入 token 的成本。如果你的对话未超过 200K tokens，启用扩展上下文并不会带来额外好处。

### 模型固定（Model Pinning）

你可以在配置中固定使用特定版本的模型，避免 Claude Code 自动升级到新版模型：

``` json title="settings.json"
{
  "model": "claude-sonnet-4-20250514"
}
```

这对需要稳定行为的 CI/CD 环境很有用——确保每次运行的模型行为一致。

## 输出风格

### 三种内置风格

输出风格（Output Style）控制 Claude 的回复方式和语气。它直接修改 Claude Code 的系统提示词：

| 风格 | 行为 | 适用场景 |
|------|------|---------|
| `Default` | 高效完成软件工程任务 | 日常开发（默认） |
| `Explanatory` | 在完成任务的同时提供教学性的 Insight | 学习新代码库 |
| `Learning` | 协作模式，会让你自己动手写关键代码 | 深度学习、实践练习 |

💡 你可以把它想成三种「老师风格」：Default 是「安静高效的上司」，Explanatory 是「边做边讲解的导师」，Learning 是「让你自己动手的教练」。

### 切换输出风格

``` bash
# 通过 /config 交互式选择
/config

# 或直接在 settings.json 中设置
```

``` json title="settings.json"
{
  "outputStyle": "Explanatory"
}
```

⚠️ 输出风格在会话启动时加载到系统提示词中，因此**修改后需要重新启动会话才能生效**。这是为了保持系统提示词的稳定，以便利用 Prompt Caching 降低延迟和成本。

### 创建自定义输出风格

自定义输出风格是一个带 front matter 的 Markdown 文件，存放在 `~/.claude/output-styles/`（用户级）或 `.claude/output-styles/`（项目级）：

``` markdown title="~/.claude/output-styles/reviewer.md"
---
name: Code Reviewer
description: 以代码审查者的角色审查代码
keep-coding-instructions: true
---

# 审查模式

你是一个严格的代码审查者。对每个变更：
1. 检查是否有潜在的 Bug
2. 评估代码可读性
3. 提出改进建议

## 格式要求

- 先总结变更内容
- 按严重程度排列问题
- 每个建议附带具体的修改方案
```

| front matter 字段 | 说明 | 默认值 |
|-------------------|------|--------|
| `name` | 风格名称（显示在 /config 菜单中） | 文件名 |
| `description` | 风格描述 | 无 |
| `keep-coding-instructions` | 是否保留默认的编码相关系统提示词 | `false` |

📌 **Output Style vs CLAUDE.md vs Skills**：
- **Output Style** 直接修改系统提示词，改变 Claude 的「角色设定」
- **CLAUDE.md** 作为用户消息追加在系统提示词之后，不改变 Claude 的角色
- **Skills** 是任务特定的提示词，通过 `/skill-name` 手动触发

## Fast Mode：速度优先

### 什么是 Fast Mode？

Fast Mode 是 Claude Opus 4.6 的高速度配置——**不是换了模型，而是换了 API 参数**。你可以把它理解成「同一位厨师，从正常烹饪模式切到快炒模式」：菜品质量不变，但上菜速度提升约 2.5 倍。

### 开启方式

``` bash
# 在会话中切换
/fast

# 或在 settings.json 中设置
```

``` json title="settings.json"
{
  "fastMode": true
}
```

开启后，提示符旁会出现 `↯` 图标，表示 Fast Mode 已激活。

### 成本对比

| 模式 | 输入价格（百万 token） | 输出价格（百万 token） |
|------|----------------------|----------------------|
| 标准模式 | 更低 | 更低 |
| Fast Mode | $30 | $150 |

⚠️ Fast Mode 仅通过 Extra Usage（额外用量）计费，不包含在订阅套餐内。适合速度优先的场景，不适合成本敏感的任务。

### 什么时候用 Fast Mode？

✅ 适合：

- 快速迭代代码改动
- 实时调试
- 时间紧迫的紧急任务

❌ 不适合：

- 长时间自主任务
- 批量处理 / CI/CD
- 成本敏感的工作负载

💡 你还可以同时降低思考强度（Effort Level）来进一步加速——「Fast Mode + 低思考强度」组合适合简单的、快速的任务。

### 速率限制与回退

当 Fast Mode 的速率限制到达上限时：

1. 自动回退到标准 Opus 4.6
2. `↯` 图标变为灰色（表示冷却中）
3. 冷却结束后自动恢复 Fast Mode

## 状态栏配置

### 什么是状态栏？

状态栏是 Claude Code 底部的一个自定义信息条。它运行你配置的 shell 脚本，实时显示会话状态——上下文用量、费用、Git 分支等。

### 快速设置

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

Claude Code 通过 stdin 向你的脚本发送 JSON 数据，主要字段包括：

| 字段 | 说明 |
|------|------|
| `model.display_name` | 当前模型显示名 |
| `context_window.used_percentage` | 上下文窗口已用百分比 |
| `context_window.remaining_percentage` | 上下文窗口剩余百分比 |
| `cost.total_cost_usd` | 本次会话总费用（USD） |
| `cost.total_duration_ms` | 会话总时长（毫秒） |
| `cost.total_lines_added` | 新增代码行数 |
| `cost.total_lines_removed` | 删除代码行数 |
| `session_id` | 会话唯一标识 |
| `version` | Claude Code 版本号 |

💡 脚本输出支持多行、ANSI 颜色代码和 OSC 8 可点击链接。更新频率为每次 Claude 回复后触发，300ms 防抖。

## 环境变量

Claude Code 通过环境变量提供细粒度的控制能力。以下是几个最常用的环境变量：

### 常用环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `ANTHROPIC_API_KEY` | API 密钥 | `sk-ant-...` |
| `CLAUDE_CODE_USE_BEDROCK` | 使用 AWS Bedrock | `1` |
| `CLAUDE_CODE_USE_VERTEX` | 使用 Google Vertex AI | `1` |
| `DISABLE_AUTOUPDATER` | 禁用自动更新 | `1` |
| `CLAUDE_CODE_DISABLE_FAST_MODE` | 禁用 Fast Mode | `1` |
| `NODE_EXTRA_CA_CERTS` | 自定义 CA 证书路径 | `/path/to/ca.pem` |

💡 你可以在 `settings.json` 的 `env` 字段中设置环境变量，也可以在 shell 的 profile 文件（如 `.bashrc`）中设置。

## 企业网络配置

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

## 语音输入

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

## 配置最佳实践

### 推荐的项目级配置

``` json title=".claude/settings.json（提交到 git）"
{
  "permissions": {
    "allow": [
      "Read",
      "Edit",
      "Bash(git status)",
      "Bash(git diff*)",
      "Bash(git log*)",
      "Bash(mvn test*)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(sudo *)"
    ]
  }
}
```

### 推荐的 User 级配置

``` json title="~/.claude/settings.json"
{
  "language": "chinese",
  "permissions": {
    "allow": [
      "Read",
      "Write",
      "Edit"
    ]
  }
}
```

### 配置层级选择指南

| 你想配置什么？ | 放在哪一层？ | 原因 |
|---------------|-------------|------|
| 团队编码规范 | Project | 全员共享，通过 git 同步 |
| 个人语言偏好 | User | 所有项目通用 |
| 企业的安全策略 | Managed | 管理员强制执行 |
| 临时调试配置 | CLI 参数 | 仅本次会话生效 |
| 本地开发环境路径 | Local | 因机器而异，不提交 git |

📝 小结：Claude Code 的配置体系由五层优先级构成——Managed > CLI > Local > Project > User。理解这个层级关系后，你就能精确控制 Claude Code 在不同场景下的行为。权限系统保障安全，沙箱机制提供隔离，模型配置和输出风格让你按需定制 Claude 的能力与风格。
