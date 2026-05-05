---
title: 官方插件目录
description: claude-plugins-official 市场全部插件分类速查
---

Claude Code 内置的官方市场 `claude-plugins-official`（[GitHub 仓库](https://github.com/anthropics/claude-plugins-official) | [在线目录](https://claude.com/plugins)）提供了 **33 个内部插件** 和 **15 个外部集成**（截至 2025 年 5 月，数据可能随版本更新变动，建议通过 `/plugin search` 获取最新列表）。本页按功能分类列出所有插件，方便快速查找和安装。

💡 安装任意插件：

```bash
/plugin install 插件名@claude-plugins-official
```

## 🔬 代码智能（LSP）

这些插件为 Claude 接入语言服务器，提供实时类型检查、跳转定义、查找引用等能力——和 IDE 一样的代码分析体验。

⚠️ 安装前需确保对应语言服务器二进制已安装，以 TypeScript 为例：

```bash
# 1. 先安装语言服务器二进制
npm install -g typescript-language-server typescript

# 2. 验证安装
typescript-language-server --version

# 3. 再安装 LSP 插件
/plugin install typescript-lsp@claude-plugins-official
```

| 语言 | 插件名 | 需要的二进制 | 说明 |
|------|--------|------------|------|
| Java | `jdtls-lsp` | `jdtls` | 代码智能与重构 |
| Python | `pyright-lsp` | `pyright-langserver` | 静态类型检查与代码智能 |
| TypeScript / JavaScript | `typescript-lsp` | `typescript-language-server` | 代码智能特性 |
| Go | `gopls-lsp` | `gopls` | 代码智能、重构与分析 |
| Rust | `rust-analyzer-lsp` | `rust-analyzer` | 代码智能与分析 |
| C / C++ | `clangd-lsp` | `clangd` | 代码智能、诊断与格式化 |
| Kotlin | `kotlin-lsp` | `kotlin-language-server` | 代码智能、重构与分析 |
| C# | `csharp-lsp` | `csharp-ls` | 代码智能与诊断 |
| Ruby | `ruby-lsp` | `ruby-lsp` | 代码智能与分析 |
| PHP | `php-lsp` | `intelephense` | 代码智能与诊断 |
| Lua | `lua-lsp` | `lua-language-server` | 代码智能与诊断 |
| Swift | `swift-lsp` | `sourcekit-lsp` | Swift 项目代码智能 |

## 🔧 开发工作流

为常见开发任务提供结构化命令和专用代理。

| 插件名 | 提供 | 说明 |
|--------|------|------|
| `commit-commands` | Commands | Git 工作流简化，提供 commit、push、创建 PR 等一站式命令 |
| `code-review` | Commands、Agents | 自动化代码审查，多个专用代理对 PR 审查并给出置信度评分 |
| `feature-dev` | Commands、Agents | 结构化功能开发工作流，7 个阶段 + 专用代理，从需求到实现全覆盖 |
| `pr-review-toolkit` | Agents | PR 审查工具集，6 个代理覆盖评论、测试、错误、类型和代码质量 |
| `hookify` | Commands | 分析你的行为模式或显式指令，自动生成 Hooks 来阻止不期望的操作（如防止 Claude 自动提交代码） |
| `ralph-loop` | Commands、Hooks | 基于 Ralph Wiggum 技术的迭代式开发循环——让 Claude 反复自我检查和修正，直到输出质量达标 |
| `code-simplifier` | Commands | 对修改过的代码进行复用、质量和效率三维检查，发现问题直接修复 |
| `security-guidance` | Commands、Skills | 安全审查与修复建议，识别代码中的安全隐患并提供修复方案 |

## 🏗️ 插件与扩展开发

帮助你构建 Claude Code 生态的插件、Skills、MCP 服务器等。

| 插件名 | 提供 | 说明 |
|--------|------|------|
| `plugin-dev` | Skills、Commands、Agents | 插件开发综合工具包，7 个专用 Skills + 引导式工作流 |
| `agent-sdk-dev` | Commands、Agents | Claude Agent SDK 应用开发和验证，支持 Python / TypeScript |
| `mcp-server-dev` | Skills、MCP | MCP 服务器设计与构建，支持交互式 UI 和 bundling |
| `skill-creator` | Skills | Skills 创建、改进和性能评估（evals） |
| `example-plugin` | Skills、Commands、MCP | 官方示例插件，演示所有扩展选项，适合学习插件结构 |

## 📋 项目配置

帮助分析和优化 Claude Code 的项目配置。

| 插件名 | 提供 | 说明 |
|--------|------|------|
| `claude-code-setup` | Skills | 分析代码库结构，推荐适合的自动化配置（Hooks、Skills、MCP） |
| `claude-md-management` | Skills、Commands | `CLAUDE.md` 维护工具，支持质量审计和会话学习成果捕获 |
| `session-report` | Skills | 分析 Claude Code 会话用量，生成可交互 HTML 仪表盘（Token、缓存、子代理、Skill 统计及优化建议） |

## 🎨 输出风格

自定义 Claude 的回复风格和交互模式。

| 插件名 | 提供 | 说明 |
|--------|------|------|
| `explanatory-output-style` | Hooks | `SessionStart` Hook，让 Claude 在每次实现选择时提供教学性说明 |
| `learning-output-style` | Hooks | 融合学习模式和解释功能的交互式教育风格 |
| `playground` | Skills | 创建交互式 HTML playground，带可视化控件和实时预览 |

## 🎯 专项能力

面向特定领域的专用插件。

| 插件名 | 提供 | 说明 |
|--------|------|------|
| `frontend-design` | Skills | 生成有辨识度、生产级的前端界面，避免千篇一律的 AI 美学 |
| `math-olympiad` | Skills | 竞赛数学求解器，具备对抗验证和基于置信度的弃权机制 |

## 🌐 外部集成

打包了预配置的 MCP 服务器，让 Claude 能直接连接外部服务。

| 插件名 | 类别 | 平台 | 说明 |
|--------|------|------|------|
| `github` | 源码管理 | GitHub | GitHub 仓库、Issues、PR 集成 |
| `gitlab` | 源码管理 | GitLab | GitLab 项目管理集成 |
| `asana` | 项目管理 | Asana | 任务和项目管理集成 |
| `linear` | 项目管理 | Linear | 问题跟踪和项目管理集成 |
| `firebase` | 基础设施 | Firebase | Google Firebase 服务集成 |
| `terraform` | 基础设施 | Terraform | 基础设施即代码管理 |
| `discord` | 通讯 | Discord | 连接 Discord Bot，支持回复、反应和编辑消息 |
| `telegram` | 通讯 | Telegram | 连接 Telegram Bot，支持回复、反应和编辑消息 |
| `imessage` | 通讯 | iMessage | 读取 `chat.db` 历史并通过 Messages.app 发送消息 |
| `greptile` | 代码分析 | Greptile | AI 代码审查代理集成 |
| `context7` | 代码分析 | Context7 | 上下文感知的代码搜索 |
| `serena` | 代码分析 | Serena | 代码分析平台集成 |
| `laravel-boost` | 框架 | Laravel | Laravel 框架增强集成 |
| `playwright` | 框架 | Playwright | 浏览器自动化与测试集成 |
| `fakechat` | 测试 | — | 简易 UI，用于测试 channel 契约，无需外部服务 |

## 📊 插件分类速查

| 类别 | 插件数量 | 提供的能力 |
|------|---------|-----------|
| 代码智能（LSP） | 12 | 实时类型检查、跳转定义、查找引用 |
| 开发工作流 | 8 | Git 命令、PR 审查、功能开发流程 |
| 插件与扩展开发 | 5 | 插件/SDK/MCP/Skill 开发工具 |
| 项目配置 | 3 | CLAUDE.md 管理、自动化推荐、会话用量报告 |
| 输出风格 | 3 | 教学模式、学习模式、playground |
| 专项能力 | 2 | 前端设计、竞赛数学 |
| 外部集成 | 15 | 源码/项目管理/基础设施/通讯等 |
