---
title: CLAUDE.md 记忆文件
description: 理解 CLAUDE.md 的作用域、自动记忆机制，以及如何写出高质量的协作契约
---

# CLAUDE.md 记忆文件

**🎯 本文你会学到**：

- 📝 CLAUDE.md 的四个作用域与优先级
- 🧠 自动记忆（Auto Memory）的工作原理
- 📋 高质量 CLAUDE.md 模板与最佳实践
- 🔧 `.claude/rules/` 目录组织方式

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

### 自动记忆（Auto Memory）

Claude Code 还有一个更智能的机制：**自动记忆**。当 Claude 在对话中觉得某些信息对未来的会话有用时，它会自动写入 CLAUDE.md。

- 写入位置：User 级别的 `~/.claude/CLAUDE.md`（追加到末尾）
- 触发条件：Claude 判断这些信息值得记住（如项目架构偏好、常用命令等）
- 用户可控：你可以随时手动编辑或删除自动写入的内容

⚠️ 自动记忆默认开启。如果不想让 Claude 自动修改你的 CLAUDE.md，可以在设置中关闭。

## 🤝 把 CLAUDE.md 用成「协作契约」

CLAUDE.md 不只是"项目文档"，更像是你和 Claude 之间的**协作契约**。里面只放那些**每次会话都得成立的事**——不是知识库，不是团队文档。

💡 **起步建议**：一开始甚至可以什么都不写。先用起来，等你发现自己老是在重复同一件事，再把它补进去。用 `#` 可以把当前对话里的内容直接追加进 CLAUDE.md。

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
