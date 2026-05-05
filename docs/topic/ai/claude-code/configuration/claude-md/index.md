---
title: CLAUDE.md 记忆文件
description: 理解 CLAUDE.md 的作用域、自动记忆机制，以及如何写出高质量的协作契约
---

# CLAUDE.md 记忆文件

**本文你会学到**：

- 📝 CLAUDE.md 的作用域体系、加载顺序与目录组织方式
- 🧠 自动记忆（Auto Memory）的工作原理
- 📋 如何写出高质量的 CLAUDE.md——模板与最佳实践

## 📝 CLAUDE.md：Claude 的「长期记忆」

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

### 符号链接跨项目共享规则

当你维护多个项目，并且希望它们共享同一套规则时，不必在每份仓库里重复维护。`.claude/rules/` 目录支持符号链接（symlink），你可以把一份共享规则链接到多个项目中：

``` bash
# 链接整个共享目录
ln -s ~/shared-claude-rules .claude/rules/shared

# 链接单个文件
ln -s ~/company-standards/security.md .claude/rules/security.md
```

Claude Code 会正常解析和加载符号链接指向的文件，循环链接也会被检测并优雅跳过，不会造成死循环。

### 从额外目录加载记忆文件

使用 `--add-dir` 标志可以让 Claude 访问主工作目录之外的其他目录。但默认情况下，这些额外目录中的 `CLAUDE.md` 文件**不会被加载**——这是一个有意的设计，防止不相关的指令污染上下文。

如果你确实需要加载额外目录中的记忆文件，可以设置 `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD` 环境变量：

``` bash
CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1 claude --add-dir ../shared-config
```

设置后，Claude 会从额外目录中加载以下文件：

- `CLAUDE.md`
- `.claude/CLAUDE.md`
- `.claude/rules/*.md`
- `CLAUDE.local.md`（如果你没有在 `--setting-sources` 中排除 `local`）

这个机制在 monorepo 或需要引用共享配置目录的场景中很实用，但要注意控制加载范围——越多不相关的内容进入上下文，Claude 遵守核心指令的可靠性就越低。

### CLAUDE.md 文件加载顺序

你有没有遇到过这种情况：明明在 `CLAUDE.md` 里写了「使用 2 空格缩进」，Claude 却一直在用 4 空格？或者改了配置但 Claude 的行为完全没有变化？这类问题的根因往往是加载顺序——多个 `CLAUDE.md` 文件同时存在时，谁先谁后决定了最终效果。

理解加载顺序有助于排查「为什么我的指令没生效」的问题。Claude Code 从当前工作目录**向上遍历**目录树，沿途检查每个目录是否有 `CLAUDE.md` 和 `CLAUDE.local.md`。

假设你在 `foo/bar/` 目录下启动 Claude Code，加载顺序如下：

1. 从文件系统根目录开始，逐级向下，直到工作目录
2. 在每一级目录中，先加载 `CLAUDE.md`，再加载 `CLAUDE.local.md`
3. 工作目录中的文件**最后被读取**，因此优先级最高

所有发现的文件是**拼接**到上下文中的，而非相互覆盖。这意味着越接近工作目录的指令越晚被 Claude 看到，在实际效果上拥有更高的优先级。

子目录中的 `CLAUDE.md` 文件不会在启动时加载——它们在 Claude 读取该子目录中的文件时才按需加载。

### 用户级规则与项目级规则的优先级

当用户级规则（`~/.claude/rules/`）和项目级规则（`.claude/rules/`）存在冲突时，**项目规则优先**。这是因为用户级规则在项目规则之前加载，后加载的项目规则会"覆盖"前面的同主题指令。

这个设计是合理的：项目约定应该优先于个人偏好。如果你在 `~/.claude/rules/preferences.md` 中写了「使用 4 空格缩进」，但项目 `.claude/rules/code-style.md` 中写了「使用 2 空格缩进」，Claude 会遵循项目的 2 空格规则。

### 排除不相关的 CLAUDE.md 文件

在大型 monorepo 中，你可能只负责其中一个子项目，但目录树上游有其他团队写的 `CLAUDE.md`——这些指令与你的工作无关，还会浪费上下文空间。此时可以用 `claudeMdExcludes` 设置来跳过特定文件：

``` json
{
  "claudeMdExcludes": [
    "**/monorepo/CLAUDE.md",
    "/home/user/monorepo/other-team/.claude/rules/**"
  ]
}
```

把这个配置加到 `.claude/settings.local.json` 中，排除规则就只在你的机器上生效，不会影响其他团队成员。

需要注意两点：

- 托管策略（Managed）级别的 `CLAUDE.md` **不能被排除**——组织范围的指令始终生效
- 排除模式使用 glob 语法与绝对文件路径匹配，支持在用户、项目、本地、托管策略任意层级配置，各层配置会合并

### 自动记忆（Auto Memory）

Claude Code 还有一个更智能的机制：**自动记忆**。当 Claude 在对话中觉得某些信息对未来的会话有用时，它会自动写入 CLAUDE.md。

- 写入位置：User 级别的 `~/.claude/CLAUDE.md`（追加到末尾）
- 触发条件：Claude 判断这些信息值得记住（如项目架构偏好、常用命令等）
- 用户可控：你可以随时手动编辑或删除自动写入的内容

⚠️ 自动记忆默认开启。如果不想让 Claude 自动修改你的 CLAUDE.md，可以设置环境变量关闭：

``` bash
export CLAUDE_CODE_DISABLE_AUTO_MEMORY=1
```

或者在 `settings.json` 的 `env` 字段中持久配置：

``` json title="settings.json"
{
  "env": {
    "CLAUDE_CODE_DISABLE_AUTO_MEMORY": "1"
  }
}
```

## 🤝 把 CLAUDE.md 用成「协作契约」

CLAUDE.md 不只是"项目文档"，更像是你和 Claude 之间的**协作契约**。里面只放那些**每次会话都得成立的事**——不是知识库，不是团队文档。

💡 **起步建议**：一开始甚至可以什么都不写。先用起来，等你发现自己老是在重复同一件事，再把它补进去。用 `#` 可以把当前对话里的内容直接追加进 CLAUDE.md。

### 用 `/init` 快速生成初始 CLAUDE.md

手动从零写 CLAUDE.md 很容易不知道该放什么。`/init` 命令可以帮你自动生成一份起点——它会分析你的代码库，把构建命令、测试指令和项目约定整理成一个初始文件。如果 `CLAUDE.md` 已经存在，`/init` 不会覆盖，而是建议改进。

默认的 `/init` 流程是一次性生成的。如果你想更精细地控制生成内容，可以设置 `CLAUDE_CODE_NEW_INIT=1` 环境变量来启用**交互式多阶段流程**：

``` bash
CLAUDE_CODE_NEW_INIT=1 claude
```

启用后，`/init` 会引导你逐步选择要设置的工件：

- `CLAUDE.md` 文件（项目指令）
- Skills（可复用的工作流）
- Hooks（自动化钩子）

确认选择后，Claude 会派出 subagent 探索你的代码库，通过后续问题填补信息空白，最后在写入任何文件之前**呈现完整提案供你审查**。这种「先审查再写入」的方式比一次性生成更可控，也更容易发现遗漏。

需要注意的是，在 `/init` 的初始化过程中，`PreToolUse` 等 hooks 会被正常触发——这意味着如果你之前配置了文件写入权限的钩子，它们在初始化阶段也会生效。

### 应该放什么

| 类别 | 示例 |
|------|------|
| **构建和测试命令**（最核心） | `mvn clean package -DskipTests`、`npm run test` |
| **关键目录结构与模块边界** | `HTTP handlers live in src/http/handlers/` |
| **代码风格和命名约束** | `Service 层不允许注入 HttpServletRequest` |
| **环境陷阱** | `本项目使用 Bun 而非 npm` |
| **绝对禁止的事**（NEVER 列表） | `禁止修改 .env`、`禁止删除 feature flags` |
| **压缩时必须保留的信息** | Compact Instructions（详见「上下文工程」） |

### 不该放什么

| ❌ 别放 | 原因 | ✅ 放哪里 |
|--------|------|---------|
| 大段背景介绍和项目历史 | Claude 可以通过读代码推断 | 不需要放 |
| 完整 API 文档 | 每次加载都费 token | Skills 的辅助文件 |
| 空泛原则如「写高质量代码」 | 模糊指令等于没有指令 | 具体可执行的约束 |
| 大量背景资料和低频任务知识 | 浪费固定上下文空间 | Skills 按需加载 |

### 高质量 CLAUDE.md 模板

``` markdown
# Project Contract

## Build And Test

- Install: `pnpm install`
- Dev: `pnpm dev`
- Test: `pnpm test`
- Typecheck: `pnpm typecheck`
- Lint: `pnpm lint`

## Architecture Boundaries

- HTTP handlers live in `src/http/handlers/`
- Domain logic lives in `src/domain/`
- Do not put persistence logic in handlers
- Shared types live in `src/contracts/`

## Coding Conventions

- Prefer pure functions in domain layer
- Do not introduce new global state without explicit justification
- Reuse existing error types from `src/errors/`

## Safety Rails

## NEVER

- Modify `.env`, lockfiles, or CI secrets without explicit approval
- Remove feature flags without searching all call sites
- Commit without running tests

## ALWAYS

- Show diff before committing
- Update CHANGELOG for user-facing changes

## Compact Instructions

When compressing, preserve in priority order:
1. Architecture decisions (NEVER summarize)
2. Modified files and their key changes
3. Current verification status (pass/fail)
4. Open risks, TODOs, rollback notes
```

### 让 Claude 维护自己的 CLAUDE.md

一个很实用的技巧：每次纠正 Claude 的错误后，让它自己更新 CLAUDE.md：

> Update your CLAUDE.md so you don't make that mistake again.

Claude 在给自己补这类规则时效果不错，用久了确实越来越少犯同样的错。不过也要**定期 review**——时间一长，当初有用的限制现在未必还适合。
