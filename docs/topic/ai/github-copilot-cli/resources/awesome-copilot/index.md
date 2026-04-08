---
title: 社区市场 awesome-copilot
description: 社区驱动的 Copilot CLI 插件、Agent、Skill、Hook 等资源集合
---

# 社区市场 awesome-copilot

📦 仓库地址：[github/awesome-copilot](https://github.com/github/awesome-copilot) · 🌐 网站：[awesome-copilot.github.com](https://awesome-copilot.github.com)

`awesome-copilot` 是一个社区驱动的 Copilot 资源集合——如果说官方市场是"精品店"，那社区市场就是"大型超市"。它不仅包含插件，还汇集了 Agent、Instruction、Skill、Hook、Workflow 等各类扩展资源，覆盖了几乎所有主流语言和开发场景。

## 📥 安装方式

``` text
/plugin install <插件名>@awesome-copilot
```

`awesome-copilot` 同样是默认注册的市场，直接安装即可。如果遇到市场未注册的错误（旧版本 CLI），先注册再安装：

``` text
/plugin marketplace add github/awesome-copilot
/plugin install <插件名>@awesome-copilot
```

## 📚 仓库资源总览

awesome-copilot 不仅有插件，还提供了其他类型的共享资源：

| 资源类型 | 说明 | 浏览 |
|---------|------|------|
| 🤖 Agent | 集成 MCP 服务器的专业角色 | [所有 Agent →](https://awesome-copilot.github.com/agents) |
| 📋 Instruction | 按文件模式自动应用的编码规范 | [所有 Instruction →](https://awesome-copilot.github.com/instructions) |
| 🎯 Skill | 包含指令和资源的独立技能包 | [所有 Skill →](https://awesome-copilot.github.com/skills) |
| 🔌 Plugin | Agent + Skill 的策划组合 | [所有 Plugin →](https://awesome-copilot.github.com/plugins) |
| 🪝 Hook | Agent 会话中自动触发的钩子 | [所有 Hook →](https://awesome-copilot.github.com/hooks) |
| ⚡ Workflow | 用 Markdown 编写的 AI 驱动 GitHub Actions | [所有 Workflow →](https://awesome-copilot.github.com/workflows) |
| 🍳 Cookbook | 可直接复制使用的 Copilot API 配方 | — |

## 🔌 插件分类速查

!!! tip "在线浏览"
    以下仅列出核心插件。完整列表和全文搜索请访问 [awesome-copilot.github.com/plugins](https://awesome-copilot.github.com/plugins)。

### 语言开发与 MCP 服务器开发

为特定语言提供开发最佳实践、代码审查、测试生成等能力。MCP 开发类插件则专注于用该语言构建 MCP 服务器。

| 插件名 | 说明 |
|-------|------|
| `java-development` | Java 开发最佳实践 |
| `java-mcp-development` | 用 Java 构建 MCP 服务器 |
| `csharp-dotnet-development` | C# / .NET 开发 |
| `csharp-mcp-development` | 用 C# 构建 MCP 服务器 |
| `frontend-web-dev` | 前端 Web 开发（React、Angular、Electron） |
| `python-mcp-development` | 用 Python 构建 MCP 服务器 |
| `go-mcp-development` | 用 Go 构建 MCP 服务器 |
| `rust-mcp-development` | 用 Rust 构建 MCP 服务器 |
| `typescript-mcp-development` | 用 TypeScript 构建 MCP 服务器 |
| `kotlin-mcp-development` | 用 Kotlin 构建 MCP 服务器 |
| `ruby-mcp-development` | 用 Ruby 构建 MCP 服务器 |
| `swift-mcp-development` | 用 Swift 构建 MCP 服务器 |
| `php-mcp-development` | 用 PHP 构建 MCP 服务器 |
| `clojure-interactive-programming` | Clojure 交互式开发（REPL 驱动） |

### OpenAPI 代码生成

从 OpenAPI 规范自动生成完整的、可运行的后端应用：

| 插件名 | 目标框架 |
|-------|---------|
| `openapi-to-application-java-spring-boot` | Java + Spring Boot |
| `openapi-to-application-csharp-dotnet` | C# + .NET |
| `openapi-to-application-python-fastapi` | Python + FastAPI |
| `openapi-to-application-nodejs-nestjs` | Node.js + NestJS |
| `openapi-to-application-go` | Go |

### 测试与质量

| 插件名 | 说明 |
|-------|------|
| `testing-automation` | 测试自动化（Playwright、TDD 红绿重构） |
| `polyglot-test-agent` | 多语言通用测试生成（自动发现语言和框架） |
| `doublecheck` | 代码双重检查 |

### 数据库与数据

| 插件名 | 说明 |
|-------|------|
| `database-data-management` | 数据库管理（PostgreSQL DBA Agent + MS SQL Agent） |
| `oracle-to-postgres-migration-expert` | Oracle 到 PostgreSQL 迁移 |
| `power-bi-development` | Power BI 报表开发 |

### 项目规划与工程管理

| 插件名 | 说明 |
|-------|------|
| `project-planning` | 项目规划（PRD、架构设计、实施计划） |
| `software-engineering-team` | 软件工程团队角色模拟（安全审查、架构评审、技术写作等） |
| `technical-spike` | 技术预研（时间盒式技术调研） |
| `edge-ai-tasks` | Edge AI 任务规划 |

### DevOps 与云

| 插件名 | 说明 |
|-------|------|
| `azure-cloud-development` | Azure 云开发 |
| `devops-oncall` | DevOps 值班诊断 |

### Microsoft 生态

| 插件名 | 说明 |
|-------|------|
| `mcp-m365-copilot` | Microsoft 365 Copilot 集成 |
| `flowstudio-power-automate` | Power Automate 流程开发 |
| `power-platform-mcp-connector-development` | Power Platform 连接器开发 |
| `power-apps-code-apps` | Power Apps 代码组件开发 |
| `pcf-development` | PowerApps Component Framework 开发 |
| `typespec-m365-copilot` | TypeSpec M365 Copilot 开发 |
| `dataverse-sdk-for-python` | Dataverse Python SDK |
| `winui3-development` | WinUI 3 桌面应用开发 |

### AI 与 Agent 工具链

| 插件名 | 说明 |
|-------|------|
| `context-engineering` | 上下文工程（多文件变更前的依赖分析） |
| `structured-autonomy` | 结构化自主工作流 |
| `rug-agentic-workflow` | Agent 工作流编排 |
| `copilot-sdk` | Copilot SDK 开发 |
| `arize-ax` | Arize AI 可观测性 |
| `phoenix` | Phoenix AI 追踪 |
| `napkin` | 快速原型/草稿工具 |

### 安全

| 插件名 | 说明 |
|-------|------|
| `security-best-practices` | 安全最佳实践（OWASP、零信任等） |

## 🤖 Agent 速查

!!! tip "在线浏览"
    以下仅列出各分类代表性 Agent（共 170+）。完整列表请访问 [awesome-copilot.github.com/agents](https://awesome-copilot.github.com/agents)。

**使用方式**：将 Agent 文件（`.agent.md`）复制到项目的 `.github/agents/` 或 `.copilot/agents/` 目录，即可在 Copilot CLI 中通过 `@agent-name` 调用。

### 语言与框架专家

| Agent | 说明 |
|-------|------|
| `expert-react-frontend-engineer` | React 19 前端开发专家 |
| `expert-nextjs-developer` | Next.js 全栈开发专家 |
| `expert-dotnet-software-engineer` | .NET 资深工程师 |
| `expert-cpp-software-engineer` | C++ 资深工程师 |
| `CSharpExpert` | C# 语言专家 |
| `vuejs-expert` | Vue.js 开发专家 |
| `nuxt-expert` | Nuxt.js 开发专家 |
| `laravel-expert-agent` | Laravel PHP 框架专家 |
| `shopify-expert` | Shopify 电商开发专家 |
| `drupal-expert` | Drupal CMS 专家 |
| `electron-angular-native` | Electron + Angular 桌面应用 |
| `clojure-interactive-programming` | Clojure 交互式开发（REPL 驱动） |
| `winui3-expert` | WinUI 3 桌面应用专家 |
| `dotnet-maui` | .NET MAUI 跨平台应用 |

### MCP 服务器开发

| Agent | 说明 |
|-------|------|
| `java-mcp-expert` | 用 Java 构建 MCP 服务器 |
| `python-mcp-expert` | 用 Python 构建 MCP 服务器 |
| `typescript-mcp-expert` | 用 TypeScript 构建 MCP 服务器 |
| `go-mcp-expert` | 用 Go 构建 MCP 服务器 |
| `rust-mcp-expert` | 用 Rust 构建 MCP 服务器 |
| `csharp-mcp-expert` | 用 C# 构建 MCP 服务器 |
| `kotlin-mcp-expert` | 用 Kotlin 构建 MCP 服务器 |
| `ruby-mcp-expert` | 用 Ruby 构建 MCP 服务器 |
| `swift-mcp-expert` | 用 Swift 构建 MCP 服务器 |
| `php-mcp-expert` | 用 PHP 构建 MCP 服务器 |

### 数据库

| Agent | 说明 |
|-------|------|
| `postgresql-dba` | PostgreSQL 数据库管理 |
| `ms-sql-dba` | Microsoft SQL Server 管理 |
| `mongodb-performance-advisor` | MongoDB 性能优化 |
| `neo4j-docker-client-generator` | Neo4j 图数据库客户端生成 |

### 测试与质量

| Agent | 说明 |
|-------|------|
| `polyglot-test-generator` | 多语言通用测试生成（编排全流程） |
| `playwright-tester` | Playwright E2E 测试 |
| `tdd-red` | TDD 红灯：先写失败测试 |
| `tdd-green` | TDD 绿灯：最小实现通过测试 |
| `tdd-refactor` | TDD 重构：保持绿灯优化代码 |
| `doublecheck` | 代码双重检查与交叉验证 |
| `debug` | 调试助手 |

### 项目规划与管理

| Agent | 说明 |
|-------|------|
| `plan` / `planner` | 战略规划与架构分析 |
| `prd` | 生成产品需求文档 |
| `implementation-plan` | 生成实施计划 |
| `task-planner` / `task-researcher` | 任务拆解与研究 |
| `context-architect` | 多文件变更前的上下文依赖分析 |
| `repo-architect` | 仓库架构分析 |
| `refine-issue` | Issue 细化与完善 |

### DevOps 与基础设施

| Agent | 说明 |
|-------|------|
| `github-actions-expert` | GitHub Actions CI/CD 专家 |
| `devops-expert` | DevOps 综合专家 |
| `platform-sre-kubernetes` | Kubernetes SRE 平台工程 |
| `terraform` | Terraform IaC 通用 |
| `terraform-azure-implement` | Terraform Azure 实施 |
| `azure-principal-architect` | Azure 首席架构师 |
| `azure-iac-generator` | Azure 基础设施即代码生成 |

### 软件工程团队角色

| Agent | 说明 |
|-------|------|
| `se-security-reviewer` | 安全审查（OWASP、零信任） |
| `se-system-architecture-reviewer` | 系统架构评审 |
| `se-technical-writer` | 技术文档写作 |
| `se-ux-ui-designer` | UX/UI 设计评审 |
| `se-product-manager-advisor` | 产品管理顾问 |
| `se-gitops-ci-specialist` | GitOps CI/CD 专家 |
| `se-responsible-ai-code` | 负责任 AI 编码 |

### "Beast Mode" 增强模式

| Agent | 说明 |
|-------|------|
| `4.1-Beast` | GPT-4.1 深度思考模式 |
| `Thinking-Beast-Mode` | 强制深度推理 |
| `gpt-5-beast-mode` | GPT-5 增强模式 |
| `rust-gpt-4.1-beast-mode` | Rust + GPT-4.1 增强 |

---

## 📋 Instruction 速查

!!! tip "在线浏览"
    以下仅列出各分类代表性 Instruction（共 200+）。完整列表请访问 [awesome-copilot.github.com/instructions](https://awesome-copilot.github.com/instructions)。

**使用方式**：将 Instruction 文件（`.instructions.md`）复制到项目的 `.github/instructions/` 或 `.copilot/instructions/` 目录，Copilot 会按文件 Glob 模式自动应用对应的编码规范。

### 语言与框架

| Instruction | 说明 |
|-------------|------|
| `csharp` | C# 编码规范 |
| `go` | Go 编码规范 |
| `rust` | Rust 编码规范 |
| `springboot` | Spring Boot 最佳实践 |
| `nextjs` | Next.js 开发规范 |
| `svelte` | Svelte 框架规范 |
| `quarkus` | Quarkus 框架规范 |
| `laravel-expert-agent` | Laravel 框架规范 |
| `blazor` | Blazor 组件开发 |
| `dotnet-maui` | .NET MAUI 跨平台 |
| `ruby-on-rails` | Ruby on Rails 规范 |
| `php-symfony` | PHP Symfony 规范 |
| `dart-n-flutter` | Dart & Flutter 规范 |
| `r` | R 语言规范 |

### 代码质量与工程实践

| Instruction | 说明 |
|-------------|------|
| `code-review-generic` | 通用代码审查规范 |
| `performance-optimization` | 性能优化指南 |
| `security-and-owasp` | 安全与 OWASP 规范 |
| `self-explanatory-code-commenting` | 自解释代码注释 |
| `object-calisthenics` | 面向对象健身操（简洁 OOP） |
| `oop-design-patterns` | 设计模式应用 |
| `context-engineering` | 上下文工程 |
| `memory-bank` | 项目记忆库管理 |

### DevOps 与基础设施

| Instruction | 说明 |
|-------------|------|
| `github-actions-ci-cd-best-practices` | GitHub Actions CI/CD 最佳实践 |
| `kubernetes-deployment-best-practices` | Kubernetes 部署最佳实践 |
| `containerization-docker-best-practices` | Docker 容器化最佳实践 |
| `terraform` | Terraform 基础规范 |
| `terraform-azure` | Terraform Azure 特定规范 |
| `ansible` | Ansible 自动化规范 |
| `powershell` | PowerShell 脚本规范 |
| `shell` | Shell 脚本规范 |

### MCP 服务器开发

| Instruction | 说明 |
|-------------|------|
| `java-mcp-server` | Java MCP 服务器开发规范 |
| `python-mcp-server` | Python MCP 服务器规范 |
| `csharp-mcp-server` | C# MCP 服务器规范 |
| `go-mcp-server` | Go MCP 服务器规范 |
| `rust-mcp-server` | Rust MCP 服务器规范 |
| `typescript-mcp-server` | TypeScript MCP 服务器规范 |

---

## 🎯 Skill 速查

!!! tip "在线浏览"
    以下仅列出各分类代表性 Skill（共 300+）。完整列表请访问 [awesome-copilot.github.com/skills](https://awesome-copilot.github.com/skills)。

**使用方式**：将 Skill 文件夹复制到项目的 `.github/skills/` 或 `.copilot/skills/` 目录，在对话中满足触发条件时自动激活。也可通过插件安装。

### 代码生成与工程

| Skill | 说明 |
|-------|------|
| `openapi-to-application-code` | 从 OpenAPI 规范生成完整应用 |
| `create-spring-boot-java-project` | 创建 Spring Boot Java 项目骨架 |
| `create-spring-boot-kotlin-project` | 创建 Spring Boot Kotlin 项目骨架 |
| `create-implementation-plan` | 生成实施计划 |
| `update-implementation-plan` | 更新已有实施计划 |
| `create-specification` | 生成技术规格文档 |
| `create-technical-spike` | 创建技术预研文档 |
| `refactor-plan` | 多文件重构计划 |
| `refactor` | 代码重构执行 |

### 测试

| Skill | 说明 |
|-------|------|
| `polyglot-test-agent` | 多语言测试生成全流程 |
| `playwright-generate-test` | 生成 Playwright E2E 测试 |
| `playwright-explore-website` | 探索网站结构用于测试 |
| `java-junit` | Java JUnit 5 测试最佳实践 |
| `csharp-nunit` / `csharp-xunit` / `csharp-mstest` | C# 测试框架 |
| `javascript-typescript-jest` | Jest 测试 |
| `spring-boot-testing` | Spring Boot 测试 |

### 代码审查与质量

| Skill | 说明 |
|-------|------|
| `sql-optimization` | SQL 性能优化 |
| `sql-code-review` | SQL 代码审查 |
| `postgresql-optimization` | PostgreSQL 优化 |
| `postgresql-code-review` | PostgreSQL 代码审查 |
| `security-review` | 安全审查 |
| `ai-prompt-engineering-safety-review` | AI Prompt 安全审查 |
| `doublecheck` | 双重检查 |
| `code-exemplars-blueprint-generator` | 代码示例蓝图生成 |

### 文档与 GitHub

| Skill | 说明 |
|-------|------|
| `java-docs` | Java 文档注释 |
| `csharp-docs` | C# 文档注释 |
| `create-readme` | 生成 README |
| `create-llms` | 生成 llms.txt |
| `documentation-writer` | 通用文档写作 |
| `prd` | 产品需求文档 |
| `github-issues` | GitHub Issue 管理 |
| `create-github-issues-feature-from-implementation-plan` | 从实施计划创建 Issue |
| `git-commit` | 生成规范的 Git 提交信息 |
| `conventional-commit` | 约定式提交 |

### MCP 服务器开发

| Skill | 说明 |
|-------|------|
| `java-mcp-server-generator` | 生成 Java MCP 服务器 |
| `python-mcp-server-generator` | 生成 Python MCP 服务器 |
| `csharp-mcp-server-generator` | 生成 C# MCP 服务器 |
| `go-mcp-server-generator` | 生成 Go MCP 服务器 |
| `typescript-mcp-server-generator` | 生成 TypeScript MCP 服务器 |
| `rust-mcp-server-generator` | 生成 Rust MCP 服务器 |
| `kotlin-mcp-server-generator` | 生成 Kotlin MCP 服务器 |

### 上下文与提示工程

| Skill | 说明 |
|-------|------|
| `context-map` | 任务相关文件映射 |
| `what-context-needed` | 分析需要哪些上下文 |
| `prompt-builder` | 提示词构建 |
| `boost-prompt` | 提示词增强 |
| `noob-mode` | 新手友好模式 |
| `first-ask` | 先提问再行动 |

---

## 🪝 Hook 速查

Hook 是在 Agent 会话期间自动触发的钩子脚本，用于检查和保护。完整列表请访问 [awesome-copilot.github.com/hooks](https://awesome-copilot.github.com/hooks)。

| Hook | 说明 |
|------|------|
| `secrets-scanner` | 提交前扫描代码中的密钥泄露 |
| `dependency-license-checker` | 检查依赖的开源许可证合规性 |
| `governance-audit` | 代码治理审计 |
| `session-auto-commit` | 会话结束时自动提交 |
| `session-logger` | 记录会话日志 |
| `tool-guardian` | 工具调用守护（防止危险操作） |

---

## ⚡ Workflow 速查

Workflow 是用 Markdown 编写的 AI 驱动 GitHub Actions 自动化。完整列表请访问 [awesome-copilot.github.com/workflows](https://awesome-copilot.github.com/workflows)。

| Workflow | 说明 |
|----------|------|
| `daily-issues-report` | 每日 Issue 报告 |
| `ospo-contributors-report` | 开源贡献者报告 |
| `ospo-org-health` | 组织健康度检查 |
| `ospo-release-compliance-checker` | 发布合规性检查 |
| `ospo-stale-repos` | 过期仓库检测 |
| `relevance-check` | 内容相关性检查 |
| `relevance-summary` | 相关性摘要 |

| 维度 | awesome-copilot（社区） | copilot-plugins（官方） |
|------|----------------------|----------------------|
| 维护者 | 社区贡献者 | GitHub 团队 |
| 审核标准 | 社区评审 | 严格审核 |
| 插件数量 | 种类丰富（57+） | 少而精（3 个） |
| 资源类型 | 插件 + Agent + Skill + Hook + Workflow | 仅插件 |
| 适用场景 | 特定语言、框架、工作流 | 通用、安全相关 |

!!! warning "使用须知"
    社区市场的资源来自第三方开发者。安装前请检查插件的文档和源码，评估其质量和安全性。

想查看官方精选插件？查看 → [官方市场 copilot-plugins](../copilot-plugins/index.md)
