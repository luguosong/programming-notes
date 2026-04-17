---
title: 双工具协同实践
description: 在同一台电脑上同时使用 GitHub Copilot CLI 和 Claude Code 的最佳实践，涵盖扩展体系共享、上下文协同管理与防冗余策略
---

# 双工具协同实践

**本文你会学到**：

- 🤔 为什么两者可以在同一台电脑上协同工作
- 🗂️ 扩展体系（Skills / Hooks / MCP / Instructions）哪些可以共享、哪些要各自维护
- 📌 防止配置冗余的单一来源策略
- 🎯 不同任务场景下如何选择工具

---

## 🤔 为什么两者值得同时使用？

GitHub Copilot CLI 和 Claude Code 并不是互相替代的关系，它们在设计哲学上有明显分工：

- `Copilot CLI` 强调 AI 与人类的职责分工——每一步都可以审批确认，内置 GitHub MCP，对 Issue / PR / CI 的原生集成无需额外配置
- `Claude Code` 强调代理自主性——Sub-agents 并行执行、百万 token 上下文窗口，适合跨越多文件的复杂任务

两者共同遵循 MCP 协议、AGENTS.md 跨平台规范，以及部分相同的扩展目录格式。这意味着你不需要为两个工具各维护一套配置——大多数扩展只需写一份，两者都能读到。

---

## ⚙️ 扩展体系协同概览

| 扩展类型 | Claude Code | Copilot CLI | 可否共享 |
|---------|------------|------------|:-------:|
| **全局个人规范** | `~/.claude/CLAUDE.md` | `~/.copilot/copilot-instructions.md` | ❌ 各自维护 |
| **项目规范** | 根目录 `CLAUDE.md` | `.github/copilot-instructions.md` / `AGENTS.md` | ✅ 共用 `CLAUDE.md` |
| **Skills** | `.claude/skills/` | `.github/skills/` + `.claude/skills/`（兼容） | ✅ 共用 `.claude/skills/` |
| **Hooks** | `settings.json` hooks 字段 | `.github/hooks/*.json` + `settings.json`（兼容） | ✅ 共用 `settings.json` |
| **MCP** | `.mcp.json` / `.claude.json` | `.mcp.json` / `~/.copilot/mcp-config.json` | ✅ 共用 `.mcp.json` |
| **Agents / Sub-agents** | `.claude/agents/*.md` | `.github/*.agent.md` | ❌ 格式不兼容 |

### 配置文件位置速查

**全局层**

| 类型 | Claude Code | Copilot CLI |
|------|------------|------------|
| 配置目录 | `~/.claude/` | `~/.config/github-copilot/` |
| 全局指令文件 | `~/.claude/CLAUDE.md` | `~/.copilot/copilot-instructions.md` |
| MCP 配置 | `~/.claude.json`（`mcpServers` 字段） | `~/.copilot/mcp-config.json` |
| 会话/历史 | `~/.claude/projects/` | Copilot CLI 自有存储 |

**项目层**

| 类型 | Claude Code | Copilot CLI |
|------|------------|------------|
| 项目指令 | `./CLAUDE.md`（及子目录层叠） | `./AGENTS.md` / `.github/copilot-instructions.md` |
| 项目级 MCP | `./.mcp.json` | `./.mcp.json`（两者共用） |
| 忽略文件 | `.claudeignore` | `.gitignore`（直接读取） |

### 共享能力分级

**可直接共享（高价值）**

- **项目指令文件**：`CLAUDE.md` 同时被两个工具读取，维护一份即可
- **MCP 服务器配置**：`.mcp.json` 的 `mcpServers` 字段 schema 完全兼容
- **Skills**：`.claude/skills/` 目录两者均自动加载
- **Hooks**：`.claude/settings.json` 的 `hooks` 字段两者均识别

**部分可共享（需注意）**

- **忽略文件**：`.gitignore` 两者都参考；`.claudeignore` 格式与 gitignore 相同，可复制内容，但 Copilot CLI 通常直接读 `.gitignore`
- **自定义斜杠命令**：Claude Code 的 `~/.claude/commands/*.md` 是其专有机制，Copilot CLI 无完全对等方案，只能手动引用 prompt 片段
- **环境变量**：项目级 `.env`（非密钥部分）两者均可读取，凭证类环境变量各自独立

**不能共享**

| 内容 | 原因 |
|------|------|
| 认证凭证 | Claude Code 用 Anthropic API key，Copilot CLI 用 GitHub OAuth，机制不同 |
| 会话历史 | 格式与存储位置都不同，无法互通 |
| 模型选择 / Sub-agents | Claude Code 的子代理机制是专有的，Copilot CLI 模型切换也独立 |
| Claude Code Hooks | `PreToolUse`/`PostToolUse` 等事件类型在 Copilot CLI 中无对等概念 |

---

## 📋 指令层：消除冗余的单一来源策略

### 项目级规范：只写一份 CLAUDE.md

最容易产生冗余的地方是项目规范。你可能有这样的疑问：

> 我用 Copilot CLI 时应该维护 `.github/copilot-instructions.md`，用 Claude Code 时应该维护 `CLAUDE.md`——两份文件要同步，太麻烦了。

实际上不需要两份。根据 Copilot CLI 的文档，仓库根目录的 `CLAUDE.md` 会被 Copilot CLI 自动读取，效果与 `AGENTS.md` 完全相同。

如果你更倾向于以 `AGENTS.md` 作为[开放规范](https://agents.md)的统一源（未来迁移其他 AI CLI 更方便），也可以反向建立符号链接：

```bash
# 以 AGENTS.md 为源，CLAUDE.md 作为符号链接
ln -s AGENTS.md CLAUDE.md

# 全局层同理
ln -s ~/.copilot/AGENTS.md ~/.claude/CLAUDE.md
```

两种方向都可行，关键是**只维护一份**，另一份用符号链接指向它。

**推荐结构（项目级）**：

```text
项目根目录/
├── CLAUDE.md            ← 单一来源，两个工具都读
└── .github/
    └── instructions/    ← 仅放路径特定指令（按文件类型/目录细化规范）
        ├── typescript.instructions.md
        └── tests.instructions.md
```

`不要同时维护` `CLAUDE.md` 和 `.github/copilot-instructions.md`，内容重复不仅需要人工同步，还会双倍消耗上下文 token。Copilot CLI 1.0.26 起已支持自动去重，但语义冗余仍然存在。

### 目录级规范：各有所长

| 层级 | Claude Code | Copilot CLI | 建议 |
|-----|------------|------------|-----|
| 子目录规范 | 子目录 `CLAUDE.md`（自动层叠） | `.github/instructions/*.instructions.md`（按 `applyTo` 匹配） | 优先用 Copilot 的路径匹配，Claude Code 也会读子目录的 `CLAUDE.md` |
| 全局个人偏好 | `~/.claude/CLAUDE.md` | `~/.copilot/copilot-instructions.md` | 两者分别维护，内容不同（下文说明） |

### 全局个人配置：各自有侧重

这两份全局文件不能共享，但写法应该互补而非重复：

```text
~/.claude/CLAUDE.md  → 给 Claude Code 的个人规范
    关注：角色定义、回复语言、思考风格、输出格式、会话持续规则

~/.copilot/copilot-instructions.md  → 给 Copilot CLI 的个人偏好
    关注：语言偏好、编码风格个人倾向（项目规范在项目级写）
```

---

## 🧩 Skills：共用一套工作流

### 共享目录结构

把所有 Skill 放在 `.claude/skills/`，两个工具都会自动加载：

```text
项目根目录/
└── .claude/
    └── skills/
        ├── code-review/
        │   └── SKILL.md        ← Claude Code 和 Copilot CLI 都自动加载
        ├── doc-generator/
        │   └── SKILL.md
        └── e2e-testing/
            ├── SKILL.md
            └── example.ts      ← 辅助文件，两者都能引用
```

个人级全局 Skill 放在 `~/.claude/skills/`，同样两者共享。

### Frontmatter 写法：核心字段通用，高级字段各自处理

两者的 SKILL.md 格式高度兼容，`name` 和 `description` 是通用字段：

``` markdown title=".claude/skills/code-review/SKILL.md"
---
name: code-review
description: 代码审查。当用户要求审查代码、检查代码质量或进行安全审计时自动激活。
disable-model-invocation: false
---

# 代码审查规范

## 审查维度
1. 类型安全与空值处理
2. 错误处理（禁止 catch 后静默忽略）
3. 安全性（SQL 注入、XSS、路径遍历）
4. 性能（N+1 查询、不必要的全表扫描）

## 输出格式
- 📍 位置（文件:行号）
- 🏷️ 类别
- 📝 问题描述
- ✅ 修复建议
```

`disable-model-invocation`、`model`、`effort`、`context: fork` 等 Claude Code 专有字段，Copilot CLI 会忽略但不报错，不影响使用。

---

## 🪝 Hooks：共用 settings.json

### 写在 `.claude/settings.json`，两者均读

`.claude/settings.json` 中的 `hooks` 字段被两个工具共同识别。推荐使用 Claude Code 的嵌套 `matcher/hooks` 格式——Copilot CLI 1.0.6 起完全兼容：

``` json title=".claude/settings.json"
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Edit|Write",
        "hooks": [
          {
            "type": "command",
            "command": "npx prettier --write \"$FILEPATH\" 2>/dev/null || true"
          }
        ]
      }
    ],
    "PreToolUse": [
      {
        "matcher": "Bash",
        "hooks": [
          {
            "type": "command",
            "command": "bash .claude/hooks/check-dangerous-cmd.sh"
          }
        ]
      }
    ]
  }
}
```

### Hook 配置分层

| 位置 | 作用范围 | 提交 Git | 适合内容 |
|------|---------|:-------:|---------|
| `.claude/settings.json` 的 hooks 字段 | 项目级，两工具共享 | ✅ | 格式化、Lint、安全校验 |
| `.github/hooks/*.json` | 项目级，Copilot CLI 专用 | ✅ | Copilot 特有的审计、通知 |
| `~/.claude/settings.json` 的 hooks 字段 | 个人全局，两工具共享 | ❌ | 个人通知偏好 |

> ⚠️ 不要在 `.claude/settings.json` 和 `.github/hooks/` 中写同一个逻辑，会触发两次。

---

## 🔌 MCP：共用 .mcp.json

### 项目级 MCP：写一份，两者共用

`.mcp.json`（项目根目录）是两个工具的共同 MCP 配置文件：

``` json title=".mcp.json"
{
  "mcpServers": {
    "context7": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp"]
    }
  }
}
```

### 全局 MCP：分别配置

| 工具 | 全局 MCP 配置文件 | 说明 |
|------|----------------|-----|
| Claude Code | `~/.claude.json` 中的 `mcpServers` | 支持 HTTP / SSE / stdio |
| Copilot CLI | `~/.copilot/mcp-config.json` | 支持 stdio |

需要某个 MCP 服务器只在一个工具中使用时，放到对应的全局配置中，不要放进 `.mcp.json`。

---

## 🗂️ 推荐共享策略总结

根据以上各节的分析，最终推荐三条落地原则：

1. **项目指令文件：选定一份，符号链接另一份**
   以 `CLAUDE.md` 或 `AGENTS.md` 二选一作为事实源（推荐 `CLAUDE.md`，Claude Code 和 Copilot CLI 均自动识别），另一侧用 `ln -s` 指向它。新团队成员无论用哪个工具都能读到统一规范。

2. **MCP 配置集中管理**
   把团队共用的 MCP 服务器（GitHub、文件系统、数据库等通用工具）统一放在项目根目录的 `.mcp.json` 并提交 Git。工具专有或含密钥的 MCP 单独放在各自的全局配置中（`~/.claude.json` / `~/.copilot/mcp-config.json`），通过环境变量引用 API key，不硬编码。

3. **全局个人偏好各自维护，内容互补而非重复**
   `~/.claude/CLAUDE.md` 侧重 Claude Code 专有特性（角色定义、Sub-agents 触发词、会话持续规则）；`~/.copilot/copilot-instructions.md` 侧重 Copilot CLI 特有约定。两份文件不求一致，但不要相互复制导致语义冗余。

---

## 🎯 任务分工：选对工具事半功倍

| 场景 | 推荐工具 | 理由 |
|------|---------|-----|
| 操作 GitHub Issue / PR / CI | Copilot CLI | 内置 GitHub MCP，零配置即可查询 PR、Issue、Workflow |
| 复杂多文件重构、架构级任务 | Claude Code | Sub-agents 并行 + 百万 token 上下文 |
| 需要每步审批的关键变更 | Copilot CLI | Interactive 模式，人类决策每一步 |
| 长时间自动化任务 | Claude Code | Autopilot + Sub-agents，自主性更强 |
| 快速问答、查文档 | 两者均可 | 用当前打开的工具即可 |
| 跨平台 CI/CD 脚本生成 | Claude Code | 非交互模式（`claude -p`）更方便集成 |

---

## 🚫 防冗余检查清单

在添加新配置前，先过一遍这个清单：

**指令类**

- [ ] 我是否同时维护了 `CLAUDE.md` 和 `.github/copilot-instructions.md`？→ 只保留 `CLAUDE.md`
- [ ] 我是否在多个层级（全局、项目、子目录）写了重复的规则？→ 合并到最高覆盖层级
- [ ] 路径特定规范是否已从 `CLAUDE.md` 拆到 `.github/instructions/*.instructions.md`？→ 减少两者的固定上下文占用

**Skills 类**

- [ ] Skills 是放在 `.github/skills/` 还是 `.claude/skills/`？→ 统一放 `.claude/skills/`（两者共享）
- [ ] 是否有两个 Skill 做同一件事？→ 合并为一个，`description` 覆盖两个触发场景

**Hooks 类**

- [ ] 同一逻辑是否同时出现在 `settings.json` 和 `.github/hooks/` 中？→ 删掉其中一份
- [ ] Hook 脚本是否产生了大量 stdout？→ 重定向到文件，避免污染上下文

**MCP 类**

- [ ] 团队共用的 MCP 是否放在 `.mcp.json` 并提交了 Git？→ 应该放在此处
- [ ] 含 API Key 的 MCP 是否误提交了 Git？→ 放到各自工具的全局配置中，或用环境变量替代
