---
title: 推荐扩展
---

## java-development — Java / Spring Boot 开发助手

> 来源：`awesome-copilot`

```bash
copilot plugin install java-development@awesome-copilot
```

| Skill                             | 功能说明                                   |
|-----------------------------------|--------------------------------------------|
| `create-spring-boot-java-project` | 快速生成 Spring Boot 项目骨架              |
| `java-docs`                       | 为 Java 类型生成规范 Javadoc 注释          |
| `java-junit`                      | JUnit 5 单元测试最佳实践（含数据驱动测试） |
| `java-springboot`                 | Spring Boot 开发最佳实践指导               |

---

## testing-automation — 多语言自动化测试

> 来源：`awesome-copilot`

```bash
copilot plugin install testing-automation@awesome-copilot
```

#### 单元测试

| Skill | 功能说明 |
|-------|----------|
| `java-junit` | JUnit 5 单元测试最佳实践（含数据驱动测试） |
| `csharp-nunit` | C# NUnit 测试最佳实践（含数据驱动测试） |

#### E2E 测试（Playwright）

| Skill | 功能说明 |
|-------|----------|
| `playwright-explore-website` | 用 Playwright MCP 探索网站结构，辅助编写测试 |
| `playwright-generate-test` | 根据场景描述生成 Playwright 端到端测试 |

#### AI Prompt 安全审查

| Skill | 功能说明 |
|-------|----------|
| `ai-prompt-engineering-safety-review` | 对 AI Prompt 进行全面安全性审查 |

---

## frontend-web-dev — 前端 E2E 测试

> 来源：`awesome-copilot`

```bash
copilot plugin install frontend-web-dev@awesome-copilot
```

| Skill                        | 功能说明                                     |
|------------------------------|----------------------------------------------|
| `playwright-explore-website` | 用 Playwright MCP 探索网站结构，辅助编写测试 |
| `playwright-generate-test`   | 根据场景描述生成 Playwright 端到端测试       |

---

## context-engineering — 上下文整理与重构规划

> 来源：`awesome-copilot`

```bash
copilot plugin install context-engineering@awesome-copilot
```

| Skill                 | 功能说明                                 |
|-----------------------|------------------------------------------|
| `context-map`         | 在修改前生成与任务相关的所有文件映射     |
| `refactor-plan`       | 多文件重构前制定具体计划（避免盲目改动） |
| `what-context-needed` | 询问 Copilot 在回答问题前需要哪些文件    |

---

## database-data-management — SQL / PostgreSQL 审查与优化

> 来源：`awesome-copilot`

```bash
copilot plugin install database-data-management@awesome-copilot
```

#### 通用 SQL

| Skill | 功能说明 |
|-------|----------|
| `sql-code-review` | 通用 SQL 安全性、可维护性及代码质量审查 |
| `sql-optimization` | 通用 SQL 性能调优、索引策略优化 |

#### PostgreSQL 专项

| Skill | 功能说明 |
|-------|----------|
| `postgresql-code-review` | PostgreSQL 专项代码审查（反模式、最佳实践） |
| `postgresql-optimization` | PostgreSQL 高级特性与查询性能优化 |

---

## project-documenter — draw.io 图表与 Word 文档生成

> 来源：`awesome-copilot`

```bash
copilot plugin install project-documenter@awesome-copilot
```

| Skill        | 功能说明                                     |
|--------------|----------------------------------------------|
| `drawio`     | 生成 `.drawio` 图表并导出为 PNG / SVG / PDF  |
| `md-to-docx` | 将 Markdown 转为专业格式的 Word (.docx) 文档 |

---

## doublecheck — AI 输出三层验证

> 来源：`awesome-copilot`

```bash
copilot plugin install doublecheck@awesome-copilot
```

| Skill         | 功能说明                                           |
|---------------|----------------------------------------------------|
| `doublecheck` | 提取 AI 输出中的可验证声明，逐一寻找支持或反驳证据 |

---

## security-best-practices — AI Prompt 安全审查

> 来源：`awesome-copilot`

```bash
copilot plugin install security-best-practices@awesome-copilot
```

| Skill                                 | 功能说明                                       |
|---------------------------------------|------------------------------------------------|
| `ai-prompt-engineering-safety-review` | 分析 Prompt 的安全性，发现注入攻击、越权等风险 |

---

## react19-upgrade — React 18 → 19 迁移指南

> 来源：`awesome-copilot`

```bash
copilot plugin install react19-upgrade@awesome-copilot
```

| Skill                         | 功能说明                                                        |
|-------------------------------|-----------------------------------------------------------------|
| `react19-source-patterns`     | React 19 源文件迁移模式（API 变更、ref 处理、context）          |
| `react19-concurrent-patterns` | 保留并升级并发模式（useTransition、useDeferredValue、Suspense） |
| `react19-test-patterns`       | 测试文件迁移到 React 19（act()、renderHook 等变更）             |

---

## structured-autonomy — 结构化规划与分步实施

> 来源：`awesome-copilot`

```bash
copilot plugin install structured-autonomy@awesome-copilot
```

| Skill                           | 功能说明                             |
|---------------------------------|--------------------------------------|
| `structured-autonomy-plan`      | 结构化规划：将任务拆解为可验证的步骤 |
| `structured-autonomy-generate`  | 根据计划生成结构化实现方案           |
| `structured-autonomy-implement` | 执行结构化实现，步步验证             |

---

## software-engineering-team — 7 个专业角色 Agent

> 来源：`awesome-copilot`（通过 `/agent` 命令调用，无 Skill）

```bash
copilot plugin install software-engineering-team@awesome-copilot
```

#### 产品与设计

| Agent | 角色说明 |
|-------|----------|
| `se-product-manager-advisor` | 产品经理顾问：GitHub Issues 编写、业务价值对齐、数据驱动产品决策 |
| `se-ux-ui-designer` | UX/UI 设计师：用户旅程映射、Jobs-to-be-Done 分析、Figma 设计产出 |

#### 技术实现

| Agent | 角色说明 |
|-------|----------|
| `se-system-architecture-reviewer` | 系统架构审查师：Well-Architected 框架、可扩展性分析、AI 与分布式系统设计 |
| `se-gitops-ci-specialist` | DevOps 专家：CI/CD 流水线、GitOps 工作流、部署问题排查 |
| `se-technical-writer` | 技术写作师：开发者文档、技术博客、教程与培训内容 |

#### 质量与合规

| Agent | 角色说明 |
|-------|----------|
| `se-security-reviewer` | 安全审查专家：OWASP Top 10、Zero Trust、LLM 安全、企业安全标准 |
| `se-responsible-ai-code` | 负责任 AI 专家：偏见预防、无障碍合规、伦理开发与包容性设计 |

---

## advanced-security — 依赖漏洞与密钥扫描

> 来源：`copilot-plugins`（需 GitHub MCP Server）

```bash
copilot plugin install advanced-security@copilot-plugins
```

| Skill                 | 功能说明                                              |
|-----------------------|-------------------------------------------------------|
| `dependency-scanning` | 扫描依赖项中的已知漏洞（通过 GitHub Dependabot 数据） |
| `secret-scanning`     | 扫描文件或近期变更中的密钥、API Key、Token 等敏感信息 |