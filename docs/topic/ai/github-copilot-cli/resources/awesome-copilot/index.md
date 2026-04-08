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

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `java-development` | Java 开发最佳实践 | 含 4 个 Skill。对话涉及 Javadoc、JUnit 5、Spring Boot 时**自动推荐**，也可 `/java-development:java-docs` 等**手动调用** |
| `java-mcp-development` | 用 Java 构建 MCP 服务器 | Skill + Agent。Skill **手动** `/` 调用生成项目；Agent `java-mcp-expert` 处理复杂 MCP 任务时**自动委派** |
| `csharp-dotnet-development` | C# / .NET 开发 | 多 Skill + Agent。Skill 涉及 C# 测试、异步编程等话题时**自动推荐**；Agent `expert-dotnet-software-engineer` 复杂任务时**自动委派** |
| `csharp-mcp-development` | 用 C# 构建 MCP 服务器 | Skill + Agent。模式同 `java-mcp-development`，含生成器 Skill 和专家 Agent |
| `frontend-web-dev` | 前端 Web 开发（React、Angular、Electron） | Skill + Agent。Playwright 测试 Skill **手动** `/` 调用；Agent `expert-react-frontend-engineer` / `electron-angular-native` 前端任务时**自动委派** |
| `python-mcp-development` | 用 Python 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件，含生成器 Skill 和专家 Agent |
| `go-mcp-development` | 用 Go 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件 |
| `rust-mcp-development` | 用 Rust 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件 |
| `typescript-mcp-development` | 用 TypeScript 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件 |
| `kotlin-mcp-development` | 用 Kotlin 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件 |
| `ruby-mcp-development` | 用 Ruby 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件 |
| `swift-mcp-development` | 用 Swift 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件 |
| `php-mcp-development` | 用 PHP 构建 MCP 服务器 | Skill + Agent。模式同其他 MCP 开发插件 |
| `clojure-interactive-programming` | Clojure 交互式开发（REPL 驱动） | Skill + Agent。含提醒 Skill（手动 `/` 调用）和 REPL 驱动 Agent，Clojure 开发任务时**自动委派** |

### OpenAPI 代码生成

从 OpenAPI 规范自动生成完整的、可运行的后端应用：

| 插件名 | 目标框架 | 使用方式 |
|-------|---------|---------|
| `openapi-to-application-java-spring-boot` | Java + Spring Boot | Skill + Agent。Skill 在提到"从 OpenAPI 生成应用"时**自动推荐**，也可 `/` 手动调用；Agent 处理完整应用生成时**自动委派** |
| `openapi-to-application-csharp-dotnet` | C# + .NET | 同上模式，目标框架为 C# + .NET |
| `openapi-to-application-python-fastapi` | Python + FastAPI | 同上模式，目标框架为 Python + FastAPI |
| `openapi-to-application-nodejs-nestjs` | Node.js + NestJS | 同上模式，目标框架为 Node.js + NestJS |
| `openapi-to-application-go` | Go | 同上模式，目标框架为 Go |

### 测试与质量

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `testing-automation` | 测试自动化（Playwright、TDD 红绿重构） | 多 Skill + 4 Agent。Playwright/JUnit/NUnit Skill **自动推荐**；TDD Agent（`tdd-red` → `tdd-green` → `tdd-refactor`）按 TDD 流程**手动指定**或匹配任务时**自动委派** |
| `polyglot-test-agent` | 多语言通用测试生成（自动发现语言和框架） | Skill + 8 Agent 流水线。提到"生成测试、写单元测试、提高覆盖率"时 Skill **自动推荐**；Agent 按 Research → Plan → Implement 流水线**自动编排** |
| `doublecheck` | 代码双重检查 | Skill + Agent。说 `use doublecheck` **自动激活**持续验证模式（每条回复附验证摘要）；`@doublecheck` **手动**启动交互式深度验证 |

### 数据库与数据

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `database-data-management` | 数据库管理（PostgreSQL DBA Agent + MS SQL Agent） | 多 Skill + 2 Agent。SQL 优化/审查 Skill 涉及数据库话题时**自动推荐**；Agent `postgresql-dba` / `ms-sql-dba` 数据库管理任务时**自动委派** |
| `oracle-to-postgres-migration-expert` | Oracle 到 PostgreSQL 迁移 | 7 Skill + Agent。Agent 采用引导式迁移（教育 → 建议 → 确认 → 执行），需用户逐步确认后**手动推进**，不会自主链式执行 |
| `power-bi-development` | Power BI 报表开发 | 4 Skill + 4 Agent。Skill 涉及 DAX 优化、模型设计等时**自动推荐**；Agent 按专业方向（数据建模/DAX/性能/可视化）**自动委派** |

### 项目规划与工程管理

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `project-planning` | 项目规划（PRD、架构设计、实施计划） | 8 Skill + 7 Agent。Skill 涉及 PRD、实施计划、技术预研等时**自动推荐**；Agent（`plan` / `prd` / `planner` 等）复杂规划任务时**自动委派** |
| `software-engineering-team` | 软件工程团队角色模拟（安全审查、架构评审、技术写作等） | 纯 Agent（7 个角色）。匹配对应角色任务时**自动委派**（如安全审查 → `se-security-reviewer`，架构评审 → `se-system-architecture-reviewer`） |
| `technical-spike` | 技术预研（时间盒式技术调研） | Skill + Agent。Skill **手动** `/` 创建预研文档；Agent `research-technical-spike` 系统性调研验证时**自动委派** |
| `edge-ai-tasks` | Edge AI 任务规划 | 纯 Agent（2 个）。`task-researcher` 项目分析 + `task-planner` 计划制定，复杂任务拆解时**自动委派** |

### DevOps 与云

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `azure-cloud-development` | Azure 云开发 | 2 Skill + 7 Agent。Skill **手动** `/` 调用（资源诊断、成本优化）；Agent 覆盖架构师、IaC（Bicep/Terraform）、Logic Apps 等，按任务**自动委派** |
| `devops-oncall` | DevOps 值班诊断 | Skill + Agent。Skill **手动** `/` 调用（Azure 资源诊断、多阶段 Dockerfile 生成）；Agent `azure-principal-architect` 架构咨询时**自动委派** |

### Microsoft 生态

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `mcp-m365-copilot` | Microsoft 365 Copilot 集成 | Skill + Agent。Skill **手动** `/` 调用（创建声明式代理、自适应卡片、部署管理）；Agent `mcp-m365-agent-expert` 专家指导时**自动委派** |
| `flowstudio-power-automate` | Power Automate 流程开发 | 3 个 Skill。涉及 Power Automate 流程管理、调试、构建时**自动推荐**（需 FlowStudio MCP 订阅） |
| `power-platform-mcp-connector-development` | Power Platform 连接器开发 | Skill + Agent。Skill **手动** `/` 调用生成连接器和 MCP 服务器；Agent 集成专家按需**自动委派** |
| `power-apps-code-apps` | Power Apps 代码组件开发 | Skill + Agent。Skill **手动** `/` 调用脚手架项目；Agent `power-platform-expert` 平台咨询时**自动委派** |
| `pcf-development` | PowerApps Component Framework 开发 | 安装后按 `/pcf-development:` 查看可用命令，**手动** `/` 调用 |
| `typespec-m365-copilot` | TypeSpec M365 Copilot 开发 | 3 个 Skill。**手动** `/` 调用创建 TypeSpec 声明式代理、API 插件、REST 操作 |
| `dataverse-sdk-for-python` | Dataverse Python SDK | 4 个 Skill。**手动** `/` 调用生成 Dataverse Python 代码（快速入门、高级模式、生产代码、用例方案） |
| `winui3-development` | WinUI 3 桌面应用开发 | Skill + Agent。迁移指南 Skill **手动** `/` 调用；Agent `winui3-expert` 防止 UWP→WinUI 3 API 误用，桌面开发时**自动委派** |

### AI 与 Agent 工具链

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `context-engineering` | 上下文工程（多文件变更前的依赖分析） | 3 Skill + Agent。Skill 涉及上下文映射、重构计划时**自动推荐**；Agent `context-architect` 多文件变更前**自动委派**分析依赖 |
| `structured-autonomy` | 结构化自主工作流 | 3 个 Skill。**手动** `/` 调用（Plan → Generate → Implement 三阶段工作流） |
| `rug-agentic-workflow` | Agent 工作流编排 | 纯 Agent（3 个）。编排器 `rug-orchestrator` 拆解任务，自动分配给 `swe-subagent`（实现）和 `qa-subagent`（质检），复杂交付任务时**自动委派** |
| `copilot-sdk` | Copilot SDK 开发 | 1 个 Skill。提到"Copilot SDK、嵌入 AI Agent、可编程代理"等关键词时**自动推荐** |
| `arize-ax` | Arize AI 可观测性 | 9 个 Skill。涉及 Arize 追踪、评估、数据集、实验等关键词时**自动推荐**，也可 `/arize-ax:arize-trace` 等**手动调用** |
| `phoenix` | Phoenix AI 追踪 | 3 个 Skill。涉及 Phoenix CLI 调试、LLM 评估、OpenInference 追踪时**自动推荐** |
| `napkin` | 快速原型/草稿工具 | 1 个 Skill。对话中说 `let's napkin` 或"打开白板"时**自动触发**，在浏览器中打开可视化白板，画完后说 `check the napkin` 让 Copilot 分析 |

### 安全

| 插件名 | 说明 | 使用方式 |
|-------|------|---------|
| `security-best-practices` | 安全最佳实践（OWASP、零信任等） | 1 个 Skill。涉及 AI 提示词安全审查时**自动推荐**，也可 `/security-best-practices:ai-prompt-engineering-safety-review` **手动调用** |

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
