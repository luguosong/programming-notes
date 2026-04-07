---
title: 插件系统
description: 通过插件打包和分发 Skills、Hooks、MCP 服务器等扩展功能
---

**本文你会学到**：

- 🎯 什么是插件，以及它和「独立配置」的区别
- 🔧 如何安装、启用、禁用和卸载插件
- 📦 插件市场的概念和工作原理
- ⚙️ 如何从零开发一个插件（含目录结构和 manifest 配置）
- 🏷️ 插件的命名空间机制，以及为什么要做命名隔离

## 🔌 什么是插件

想象一下你的手机：出厂时系统功能有限，但通过应用商店安装 App 后，它能变成导航仪、支付工具、社交平台。Claude Code 的插件系统做的就是类似的事（v2.0.12 发布） —— 让你把 `Skills`、`Hooks`、`MCP 服务器`、`LSP 服务器` 等扩展功能打包成一个可分发、可版本管理的「扩展包」，一键安装到 Claude Code 中。

💡 简单来说：**插件就是一个自包含的目录，里面装着各种扩展组件，附带一份描述文件（manifest）告诉 Claude Code 怎么加载它们。**

### 插件能装什么

一个插件可以包含以下任意组合的组件：

| 组件 | 存放位置 | 作用 |
|------|---------|------|
| `Skills` | `skills/` 目录 | 给 Claude 新增可调用的技能（自动或手动触发） |
| `Commands` | `commands/` 目录 | 斜杠命令（遗留方式，新插件推荐用 `skills/`） |
| `Agents` | `agents/` 目录 | 专用的子代理（Subagent），Claude 会根据任务自动调用 |
| `Hooks` | `hooks/hooks.json` | 事件处理器，在特定生命周期事件触发时自动执行 |
| `MCP 服务器` | `.mcp.json` | MCP 服务器配置，连接外部工具和服务 |
| `LSP 服务器` | `.lsp.json` | 语言服务器配置，为 Claude 提供实时代码智能 |
| 可执行文件 | `bin/` 目录 | 添加到 Bash 工具 `PATH` 的可执行脚本 |
| 输出风格 | `output-styles/` 目录 | 自定义 Claude 的回复风格 |

### 插件 vs 独立配置

你可能会问：这些组件我也可以直接放在项目的 `.claude/` 目录下，为什么要打包成插件？两者的区别如下：

| 对比维度 | 独立配置（`.claude/`） | 插件（Plugin） |
|---------|---------------------|---------------|
| 适用范围 | 仅当前项目 | 可跨项目复用 |
| 分发方式 | 手动复制文件 | 通过市场一键安装 |
| 命名 | `/hello`（短名） | `/my-plugin:hello`（带命名空间前缀） |
| 版本管理 | 无 | 支持 Semantic Versioning |
| 团队共享 | 通过 Git 仓库共享 | 通过插件市场分发 |
| 更新方式 | 手动同步 | 自动更新 |

📌 **选择建议**：个人快速实验用独立配置；需要分享给团队、社区，或者要在多个项目间复用，就用插件。

## 📥 安装与管理插件

### 插件的安装范围（Scope）

安装插件时，Claude Code 让你选择一个**安装范围**，决定插件在哪些地方生效、谁能使用：

| 范围 | 配置文件 | 典型场景 |
|------|---------|---------|
| `user`（默认） | `~/.claude/settings.json` | 个人插件，所有项目通用 |
| `project` | `.claude/settings.json` | 团队插件，通过 Git 共享给协作者 |
| `local` | `.claude/settings.local.json` | 项目专属但不上传 Git |
| `managed` | 托管设置 | 管理员统一部署，只读 |

### 安装插件

从市场安装插件最简单的方式：

```bash
# 从官方市场安装 GitHub 集成插件
/plugin install github@claude-plugins-official

# 通过 CLI 安装（指定范围）
claude plugin install formatter@my-marketplace --scope project
```

你也可以在交互界面操作：运行 `/plugin`，进入 **Discover** 标签页，选中插件后选择安装范围。

### 管理已安装的插件

```bash
# 禁用插件（不卸载，随时可重新启用）
/plugin disable plugin-name@marketplace-name

# 重新启用
/plugin enable plugin-name@marketplace-name

# 完全卸载
/plugin uninstall plugin-name@marketplace-name

# 保留数据卸载（适合测试新版本后重装）
claude plugin uninstall formatter@my-marketplace --keep-data
```

### 会话中热重载

安装、启用或禁用插件后，不需要重启 Claude Code。运行以下命令即可让所有改动立即生效（v2.1.69 新增 `/reload-plugins` 命令）：

```
/reload-plugins
```

Claude Code 会重新加载所有活跃的插件，并显示插件、Skills、Agents、Hooks、MCP 服务器和 LSP 服务器的数量统计。

## 🏪 插件市场

### 🔌 什么是插件市场

如果说插件是「App」，那插件市场就是「应用商店」。它本质上是一个**插件目录清单**，描述了有哪些插件可用、从哪里下载。使用插件市场的过程分两步：

1. **添加市场** —— 类似于「在手机上添加一个应用商店的源」
2. **从市场安装插件** —— 类似于「从商店里下载一个 App」

### 官方市场

Anthropic 维护了一个官方市场（名称为 `claude-plugins-official`），它在 Claude Code 启动时自动可用。你可以直接运行 `/plugin` 进入 **Discover** 标签页浏览，或者访问 claude.com/plugins 查看在线目录。

官方市场包含以下类别的插件：

#### 代码智能（Code Intelligence）

这些插件为 Claude 接入语言服务器（Language Server），让它获得和 VS Code 一样的代码分析能力 —— 编辑后立即看到类型错误、跳转定义、查找引用。

| 语言 | 插件名 | 需要安装的二进制 |
|------|--------|----------------|
| Java | `jdtls-lsp` | `jdtls` |
| Python | `pyright-lsp` | `pyright-langserver` |
| TypeScript | `typescript-lsp` | `typescript-language-server` |
| Go | `gopls-lsp` | `gopls` |
| Rust | `rust-analyzer-lsp` | `rust-analyzer` |
| C/C++ | `clangd-lsp` | `clangd` |
| Kotlin | `kotlin-lsp` | `kotlin-language-server` |
| C# | `csharp-lsp` | `csharp-ls` |
| PHP | `php-lsp` | `intelephense` |
| Lua | `lua-lsp` | `lua-language-server` |
| Swift | `swift-lsp` | `sourcekit-lsp` |

⚠️ 安装 LSP 插件前，必须先确保对应的语言服务器二进制已安装并可用。

#### 外部集成

打包了预配置的 MCP 服务器，让 Claude 能直接连接外部服务：

- **源码管理**：`github`、`gitlab`
- **项目管理**：`atlassian`（Jira/Confluence）、`asana`、`linear`、`notion`
- **设计**：`figma`
- **基础设施**：`vercel`、`firebase`、`supabase`
- **通讯**：`slack`
- **监控**：`sentry`

#### 开发工作流

为常见开发任务提供命令和代理：

- `commit-commands`：Git 提交工作流（commit、push、创建 PR）
- `pr-review-toolkit`：PR 审查专用代理
- `plugin-dev`：插件开发工具包

### 添加第三方市场

除了官方市场，任何人都可以创建和发布自己的市场。添加方式如下：

```bash
# 从 GitHub 仓库添加
/plugin marketplace add owner/repo

# 从其他 Git 托管平台添加
/plugin marketplace add https://gitlab.com/company/plugins.git

# 指定分支或标签
/plugin marketplace add https://gitlab.com/company/plugins.git#v1.0.0

# 从本地目录添加（适合开发测试）
/plugin marketplace add ./my-local-marketplace

# 从远程 URL 添加
/plugin marketplace add https://example.com/marketplace.json
```

### 管理市场

```bash
# 刷新市场列表（获取最新插件信息）
/plugin marketplace update marketplace-name

# 移除市场
/plugin marketplace remove marketplace-name

# 通过 CLI 查看所有已添加的市场
claude plugin marketplace list
```

### 自动更新

官方市场默认开启自动更新。你也可以为第三方市场手动开启：运行 `/plugin` -> **Marketplaces** -> 选择市场 -> 启用自动更新。

启动时如果检测到插件有更新，Claude Code 会提示你运行 `/reload-plugins` 来加载新版本。

⚠️ 安全提示：插件具有和你相同的用户权限，可以执行任意代码。请只安装来自可信来源的插件和市场。

### 团队市场配置

团队管理员可以在项目的 `.claude/settings.json` 中预配置市场，让团队成员在信任项目文件夹时自动提示安装：

```json
{
  "extraKnownMarketplaces": {
    "my-team-tools": {
      "source": {
        "source": "github",
        "repo": "your-org/claude-plugins"
      }
    }
  }
}
```

## 🛠️ 插件开发基础

### 创建第一个插件

让我们从最简单的情况开始 —— 创建一个只有一个 Skill 的插件。

**第 1 步**：创建目录结构

```
my-plugin/
├── .claude-plugin/
│   └── plugin.json        # 插件清单文件
└── skills/
    └── hello/
        └── SKILL.md       # 技能定义
```

**第 2 步**：编写 `plugin.json`（清单文件）

```json
{
  "name": "my-plugin",
  "version": "1.0.0",
  "description": "我的第一个 Claude Code 插件"
}
```

**第 3 步**：编写 `SKILL.md`

```markdown
---
name: hello
description: 向用户打招呼，接受一个名字参数
---

根据用户提供的名字，生成一段友好的问候语。

如果用户传入 $ARGUMENTS，使用该名字；否则使用"世界"。
```

💡 `$ARGUMENTS` 是一个特殊变量，用于捕获用户调用技能时传入的参数。

**第 4 步**：本地测试

```bash
# 使用 --plugin-dir 加载本地插件进行测试
claude --plugin-dir ./my-plugin

# 测试技能
/my-plugin:hello Claude
```

### 完整的插件目录结构

当你的插件包含更多组件时，目录结构会更丰富：

```
enterprise-plugin/
├── .claude-plugin/           # 元数据目录
│   └── plugin.json           # 插件清单（可选，无则自动发现）
├── commands/                 # 斜杠命令（Markdown 文件）
│   ├── status.md
│   └── logs.md
├── agents/                   # 子代理定义
│   ├── security-reviewer.md
│   └── compliance-checker.md
├── skills/                   # Agent Skills
│   ├── code-review/
│   │   └── SKILL.md
│   └── pdf-processor/
│       ├── SKILL.md
│       └── scripts/
├── hooks/                    # Hook 配置
│   └── hooks.json
├── bin/                      # 可执行文件（添加到 PATH）
├── .mcp.json                # MCP 服务器配置
├── .lsp.json                # LSP 服务器配置
├── settings.json            # 默认设置
└── scripts/                 # 工具脚本
```

⚠️ **常见错误**：把组件目录（如 `commands/`、`agents/`）放到 `.claude-plugin/` 里面。正确做法是放在插件根目录，只有 `plugin.json` 属于 `.claude-plugin/`。

### plugin.json 详解

清单文件是可选的 —— 如果不提供，Claude Code 会自动从默认目录发现组件，并用目录名作为插件名。但推荐总是提供一份 `plugin.json`，因为它携带了版本号、描述等元数据。

```json
{
  "name": "my-plugin",          // 必填：唯一标识符（kebab-case）
  "version": "1.2.0",           // 语义化版本号
  "description": "插件用途简述",
  "author": {
    "name": "作者名",
    "email": "author@example.com"
  },
  "homepage": "https://docs.example.com",
  "repository": "https://github.com/user/plugin",
  "license": "MIT",
  "keywords": ["keyword1", "keyword2"],
  "commands": ["./custom/commands/special.md"],
  "agents": "./custom/agents/",
  "skills": "./custom/skills/",
  "hooks": "./config/hooks.json",
  "mcpServers": "./mcp-config.json",
  "lspServers": "./.lsp.json"
}
```

💡 `commands`、`agents`、`skills` 等路径字段可以**替换**默认目录位置。如果指定了这些字段，对应的默认目录就不会被扫描。想同时保留默认目录并添加额外路径，可以用数组形式：`"commands": ["./commands/", "./extras/deploy.md"]`。

### 两个重要的环境变量

Claude Code 为插件提供了两个特殊的环境变量，在 Hook 命令、MCP/LSP 配置、Skill 和 Agent 内容中都可以使用：

| 变量 | 含义 | 用途 |
|------|------|------|
| `${CLAUDE_PLUGIN_ROOT}` | 插件安装目录的绝对路径 | 引用插件自带的脚本、配置文件 |
| `${CLAUDE_PLUGIN_DATA}` | 插件持久化数据目录 | 存放 `node_modules`、缓存、生成文件等 |

⚠️ `${CLAUDE_PLUGIN_ROOT}` 在插件更新后会变化，不要在这里存放需要持久化的数据。需要跨版本保留的文件请用 `${CLAUDE_PLUGIN_DATA}`。

### 给插件添加 Hook

在 `hooks/hooks.json` 中定义事件处理器：

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": "${CLAUDE_PLUGIN_ROOT}/scripts/format-code.sh"
          }
        ]
      }
    ]
  }
}
```

这会在 Claude 每次使用 `Write` 或 `Edit` 工具后自动运行格式化脚本。

### 给插件添加 MCP 服务器

在 `.mcp.json` 中配置 MCP 服务器：

```json
{
  "mcpServers": {
    "plugin-database": {
      "command": "${CLAUDE_PLUGIN_ROOT}/servers/db-server",
      "args": ["--config", "${CLAUDE_PLUGIN_ROOT}/config.json"],
      "env": {
        "DB_PATH": "${CLAUDE_PLUGIN_ROOT}/data"
      }
    }
  }
}
```

### 发布到官方市场

当你的插件足够成熟，想分享给所有 Claude Code 用户时，可以通过以下入口提交：

- **Claude.ai**：claude.ai/settings/plugins/submit
- **Console**：platform.claude.com/plugins/submit

### 从独立配置迁移到插件

如果你已经在 `.claude/` 目录下积累了 Skills 或 Hooks，可以轻松迁移到插件：

| 独立配置（`.claude/`） | 插件 |
|---------------------|------|
| `.claude/commands/` | `plugin-name/commands/` |
| `.claude/skills/` | `plugin-name/skills/` |
| `settings.json` 中的 Hooks | `hooks/hooks.json` |
| 只在当前项目可用 | 通过市场安装，跨项目复用 |

## 📛 插件的命名空间

### 为什么需要命名空间

Claude Code 可能同时安装多个插件，如果两个插件都定义了一个叫 `deploy` 的 Skill，该怎么区分？答案就是命名空间。

每个插件的组件都会自动加上**插件名前缀**，格式为 `插件名:组件名`。例如：

- 插件 `team-a-tools` 中的 `deploy` 技能 -> `/team-a-tools:deploy`
- 插件 `team-b-tools` 中的 `deploy` 技能 -> `/team-b-tools:deploy`

这样即使两个插件有同名组件也不会冲突。这也是为什么插件名必须使用 kebab-case（如 `my-plugin`）—— 它会作为命名空间的前缀。

### 命名空间的具体体现

| 场景 | 独立配置 | 插件 |
|------|---------|------|
| 调用技能 | `/hello` | `/my-plugin:hello` |
| 查看代理 | `/agents` 中显示 `hello` | `/agents` 中显示 `my-plugin:hello` |
| 命名冲突 | 后加载的覆盖先加载的 | 通过前缀区分，永不冲突 |

📌 **总结**：命名空间就是给每个插件的组件加了一个「姓氏」，确保全局唯一。虽然名字变长了，但换来的是多插件共存的稳定性 —— 这和 Java 的包名、Docker 的容器名是同样的设计思路。
