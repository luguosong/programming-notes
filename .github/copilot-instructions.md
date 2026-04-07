# 编程笔记 Copilot 指引

> 📖 项目完整上下文见 `CLAUDE.md`（架构、规范、常用命令等）。本文件仅包含 Copilot 工具特有配置。

## 快速导航

| 需求 | 文件 |
|------|------|
| 仓库简介 & 目录结构 | `CLAUDE.md` §仓库简介 / §架构说明 |
| 常用命令（Docker / zensical / Maven / npm） | `CLAUDE.md` §常用命令 |
| 新增内容规范 & 文档目录规则 | `CLAUDE.md` §新增内容规范 |
| Zensical Markdown 语法速查 | `CLAUDE.md` §Zensical 特有语法与项目约定 |
| Git 提交规范 | `.github/git-commit-instructions.md` |

## Copilot CLI Skills（`.claude/skills/`）

| Skill | 适用场景 |
|-------|---------|
| `word-pronunciation` | 在 Zensical 文档页面中添加点击发音按钮 |
| `doc-quality-review` | 对 docs/ 下的文档进行内容质量审查（准确性、教学风格、TOC 结构、代码引用等） |
| `sync-changelog` | 将 changelog 新版本变更同步到对应主题笔记页面并标注版本号（Copilot CLI + Claude Code） |

## Copilot Custom Agents（`.github/agents/`）

> 暂无自定义 agent，后续新增时在此登记。

| Agent | 适用场景 |
|-------|---------|
| — | — |
