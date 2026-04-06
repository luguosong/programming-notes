---
title: 推荐扩展
description: 精选 MCP 服务器、插件、外部工具和 Skills 的推荐配置方案
---

# 推荐扩展

**本文你会学到**：
- 🔌 值得安装的 MCP 服务器和插件
- 📋 实用的 Skill 模板
- ⚙️ 适合全栈 Java/前端开发者的推荐配置
- 🏢 团队采纳 Copilot CLI 的渐进路径

前面的章节介绍了 Copilot CLI 的各种能力，本页则整理了一份`推荐清单`——就像新手机到手后"必装 App"列表，帮你快速搭建高效的 Copilot 工作环境。

---

## 🔌 用哪些 MCP 服务器？

MCP 服务器为 Copilot 连接外部数据源和工具。以下是按使用频率排序的推荐。

### GitHub MCP 服务器（内置）

内置于 Copilot CLI，`无需安装`。提供对 GitHub 平台的完整访问。

`提供的能力`：

- 搜索 Issue、PR、代码
- 查看仓库文件和 commit 历史
- 查看 CI/CD 工作流状态
- 管理 GitHub Actions

``` text
# 直接使用，无需配置
> 列出仓库中标记为 "bug" 的 Issue
> 查看 PR #42 的变更
```

---

### Context7

实时查询库和框架的最新文档，避免 AI 使用过时的 API 信息。支持 npm、PyPI 等生态。

``` json title="~/.copilot/mcp-config.json"
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

`使用场景`：

``` text
> 使用 Context7 查询 React 19 的 use() hook 用法
> 查询 Spring Boot 3.5 的最新配置属性
```

---

### Filesystem MCP

提供受控的文件系统访问，支持跨目录操作。

``` json title="~/.copilot/mcp-config.json"
{
  "mcpServers": {
    "filesystem": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/path/to/allowed/directory1",
        "/path/to/allowed/directory2"
      ]
    }
  }
}
```

!!! warning "安全提示"

    最后的路径参数指定了 MCP 服务器可以访问的目录范围，务必限制为必要的目录，避免暴露整个文件系统。

---

### Playwright MCP

浏览器自动化，支持 UI 测试、网页抓取和交互式操作。

``` json title="~/.copilot/mcp-config.json"
{
  "mcpServers": {
    "playwright": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@playwright/mcp@latest", "--browser", "chromium"]
    }
  }
}
```

`使用场景`：

``` text
> 打开 http://localhost:3000 并截图，检查页面布局
> 自动化测试登录流程
```

!!! tip "浏览器选项"

    `--browser` 参数支持 `chromium`、`firefox`、`msedge` 等值。

---

### Microsoft Learn MCP

访问 Microsoft 技术文档，获取 Azure、.NET、TypeScript 等技术的官方指南。

``` json title="~/.copilot/mcp-config.json"
{
  "mcpServers": {
    "microsoft-learn": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@nicobailon/mcp-microsoft-learn"]
    }
  }
}
```

---

### 数据库 MCP

连接数据库进行查询、迁移和数据分析。

=== "PostgreSQL"

    ``` json title="~/.copilot/mcp-config.json"
    {
      "mcpServers": {
        "postgres": {
          "type": "stdio",
          "command": "npx",
          "args": ["-y", "@modelcontextprotocol/server-postgres"],
          "env": {
            "POSTGRES_CONNECTION_STRING": "postgresql://user:pass@localhost:5432/dbname"
          }
        }
      }
    }
    ```

=== "MySQL"

    ``` json title="~/.copilot/mcp-config.json"
    {
      "mcpServers": {
        "mysql": {
          "type": "stdio",
          "command": "npx",
          "args": ["-y", "@benborla29/mcp-server-mysql"],
          "env": {
            "MYSQL_HOST": "localhost",
            "MYSQL_PORT": "3306",
            "MYSQL_USER": "root",
            "MYSQL_PASSWORD": "password",
            "MYSQL_DATABASE": "mydb"
          }
        }
      }
    }
    ```

---

### MCP 服务器推荐总览

| 服务器 | 功能 | 安装方式 |
|--------|------|---------|
| `GitHub`（内置） | GitHub API 完整访问 | 无需安装 |
| `Context7` | 实时库文档查询 | `npx @upstash/context7-mcp` |
| `Filesystem` | 受控文件系统访问 | `npx @modelcontextprotocol/server-filesystem` |
| `Playwright` | 浏览器自动化 | `npx @playwright/mcp@latest` |
| `Microsoft Learn` | 微软技术文档 | `npx @nicobailon/mcp-microsoft-learn` |
| `PostgreSQL` | PostgreSQL 数据库 | `npx @modelcontextprotocol/server-postgres` |

!!! tip "发现更多 MCP 服务器"

    - [GitHub MCP Registry](https://github.com/modelcontextprotocol/servers) — 官方 MCP 服务器目录
    - [MCP 服务器搜索](https://mcp.so/) — 社区 MCP 服务器聚合

---

## 🧩 用哪些插件？

插件通过市场安装，为 Copilot CLI 添加 Agent、Skill、Hook 等组合能力。

### 安装方式

``` text
# 从官方市场安装
/plugin install PLUGIN-NAME@copilot-plugins

# 从社区市场安装
/plugin install PLUGIN-NAME@awesome-copilot

# 从 GitHub 仓库安装
/plugin install OWNER/REPO
```

### 官方插件市场（copilot-plugins）

| 插件 | 功能 | 安装命令 |
|------|------|---------|
| `advanced-security` | GitHub Advanced Security（GHAS），密钥扫描、代码安全分析 | `/plugin install advanced-security@copilot-plugins` |
| `java-springboot` | Spring Boot 开发最佳实践、Agent 和 Skill | `/plugin install java-springboot@copilot-plugins` |
| `java-junit` | JUnit 5 单元测试最佳实践 | `/plugin install java-junit@copilot-plugins` |
| `java-docs` | Java 文档（Javadoc）生成 | `/plugin install java-docs@copilot-plugins` |
| `csharp-nunit` | NUnit 测试最佳实践 | `/plugin install csharp-nunit@copilot-plugins` |
| `javascript-typescript-jest` | Jest 测试最佳实践 | `/plugin install javascript-typescript-jest@copilot-plugins` |

### 社区插件市场（awesome-copilot）

| 插件 | 功能 | 安装命令 |
|------|------|---------|
| `context-engineering` | 上下文工程，提升 Copilot 上下文感知能力 | `/plugin install context-engineering@awesome-copilot` |
| `database-data-management` | 数据库管理（PostgreSQL、MySQL、SQL Server） | `/plugin install database-data-management@awesome-copilot` |
| `java-development` | Java 开发最佳实践：Javadoc、JUnit 5、Spring Boot、项目脚手架 | `/plugin install java-development@awesome-copilot` |
| `openapi-to-application-java-spring-boot` | 从 OpenAPI 规范生成 Spring Boot 完整应用 | `/plugin install openapi-to-application-java-spring-boot@awesome-copilot` |
| `testing-automation` | TDD 红绿重构循环、Playwright 测试自动化 | `/plugin install testing-automation@awesome-copilot` |
| `frontend-web-dev` | Electron+Angular 代码审查、React 19 前端工程 | `/plugin install frontend-web-dev@awesome-copilot` |
| `polyglot-test-agent` | 多语言测试生成管线（Python、Java、Go、Rust 等） | `/plugin install polyglot-test-agent@awesome-copilot` |
| `project-planning` | PRD、实现计划、技术 Spike、任务分解、GitHub Issues 生成 | `/plugin install project-planning@awesome-copilot` |
| `software-engineering-team` | 完整工程团队：安全审查、架构评审、DevOps、PM、技术写作 | `/plugin install software-engineering-team@awesome-copilot` |
| `cloud-design-patterns` | 42 种云架构设计模式参考 | `/plugin install cloud-design-patterns@awesome-copilot` |
| `documentation-writer` | 基于 Diátaxis 框架的技术文档写作 | `/plugin install documentation-writer@awesome-copilot` |
| `quality-playbook` | 质量体系：测试协议、代码审查、规范审计 | `/plugin install quality-playbook@awesome-copilot` |
| `multi-stage-dockerfile` | 优化的多阶段 Dockerfile 生成 | `/plugin install multi-stage-dockerfile@awesome-copilot` |
| `secret-scanning` | 密钥扫描和推送保护配置 | `/plugin install secret-scanning@awesome-copilot` |

!!! tip "浏览更多插件"

    ``` text
    # 浏览官方市场
    /plugin marketplace browse copilot-plugins

    # 浏览社区市场
    /plugin marketplace browse awesome-copilot
    ```

    也可以访问 [awesome-copilot.github.com/plugins](https://awesome-copilot.github.com/plugins/) 在线浏览。

---

## 🛠️ 搭配哪些外部工具？

### CodeRabbit

[CodeRabbit](https://coderabbit.ai/) 是 AI 驱动的 PR 审查工具，与 Copilot CLI 配合可实现"编写-审查"闭环。

`核心能力`：

- 自动 PR 总结与三级风险评估（Critical / Major / Minor）
- 可直接提交的修复建议（Committable Suggestions）
- 安全防御：检测硬编码凭据、SQL 注入风险及输入验证缺失

`集成方式`：

``` bash
# 方式一：通过 npm 安装 CLI
npm install -g coderabbit

# 方式二：GitHub App（推荐，自动集成到 PR 流程）
# 访问 https://github.com/apps/coderabbitai 安装
```

`与 Copilot CLI 协同`：

``` bash
# CodeRabbit 分析 PR 并输出修复建议
code-rabbit --prompt-only > /tmp/fixes.txt

# Copilot CLI 执行修复
copilot -p "$(cat /tmp/fixes.txt) 请修复上述问题"
```

---

## 🎯 用哪些 Skills？

以下是一些实用的 Skill 模板，可以直接在项目中使用或作为参考。

### 代码审查 Skill

``` markdown title=".github/skills/code-review/SKILL.md"
---
name: code-review
description: 全面的代码审查，当用户要求审查代码、检查代码质量或安全审计时自动激活。
---

# 代码审查

## 审查维度
1. **安全性**：注入攻击、认证绕过、敏感数据暴露
2. **正确性**：逻辑错误、边界条件、空指针
3. **性能**：N+1 查询、不必要的循环、内存泄漏
4. **可维护性**：命名规范、函数复杂度、重复代码

## 输出格式
每个问题包含：位置、严重程度、问题描述、修复建议。
只报告真正需要修复的问题，不评论代码风格。
```

### 测试生成 Skill

``` markdown title=".github/skills/test-generator/SKILL.md"
---
name: test-generator
description: 生成全面的单元测试和集成测试。当用户要求生成测试、写测试、提高覆盖率时自动激活。
---

# 测试生成

## 测试策略
- 正常路径 + 边界条件 + 异常场景
- 使用 AAA 模式（Arrange, Act, Assert）
- 外部依赖使用 Mock 隔离

## 语言适配
- Java：JUnit 5 + Mockito + AssertJ
- Python：pytest + unittest.mock
- TypeScript：Jest / Vitest
- 自动检测项目使用的测试框架
```

### Git Commit 规范 Skill

``` markdown title=".github/skills/git-commit/SKILL.md"
---
name: git-commit
description: 生成符合 Conventional Commit 规范的提交信息。当用户要求生成 commit message 或提交代码时自动激活。
---

# Git Commit 规范

## 格式
type(scope): description

## 类型
- feat: 新功能
- fix: 修复 Bug
- docs: 文档变更
- style: 代码格式（不影响功能）
- refactor: 重构（非新功能、非修复）
- perf: 性能优化
- test: 测试相关
- chore: 构建/工具变更

## 规则
- description 使用英文，首字母小写，不加句号
- scope 可选，描述影响范围
- 破坏性变更在 footer 中添加 BREAKING CHANGE
```

### API 文档生成 Skill

``` markdown title=".github/skills/api-docs/SKILL.md"
---
name: api-docs
description: 生成 REST API 文档。当用户要求生成 API 文档、接口文档、OpenAPI 规范时自动激活。
---

# API 文档生成

## 每个端点包含
- HTTP 方法 + 路径
- 请求参数（路径、查询、请求体）
- 响应格式和状态码
- 认证要求
- curl 调用示例

## 输出格式
- Markdown 格式
- 参数用表格展示
- 按功能模块分组
```

---

## 🏗️ 我的推荐配置

以下是一份综合配置示例，适合全栈 Java/前端开发者：

### MCP 配置

``` json title="~/.copilot/mcp-config.json"
{
  "mcpServers": {
    "context7": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@upstash/context7-mcp"]
    },
    "playwright": {
      "type": "stdio",
      "command": "npx",
      "args": ["-y", "@playwright/mcp@latest", "--browser", "chromium"]
    },
    "filesystem": {
      "type": "stdio",
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "C:\\Users\\USERNAME\\projects"
      ]
    }
  }
}
```

### 推荐安装的插件

``` text
# Java 全栈开发
/plugin install java-development@awesome-copilot
/plugin install openapi-to-application-java-spring-boot@awesome-copilot

# 测试与质量
/plugin install testing-automation@awesome-copilot
/plugin install polyglot-test-agent@awesome-copilot

# 前端开发
/plugin install frontend-web-dev@awesome-copilot

# 工程效能
/plugin install context-engineering@awesome-copilot
/plugin install project-planning@awesome-copilot
/plugin install software-engineering-team@awesome-copilot

# 数据库
/plugin install database-data-management@awesome-copilot
```

### 推荐创建的 Skills

``` text
.github/skills/
├── code-review/SKILL.md       # 代码审查
├── test-generator/SKILL.md    # 测试生成
├── git-commit/SKILL.md        # Commit 规范
└── api-docs/SKILL.md          # API 文档
```

---

## 🏢 团队怎么推行 Copilot CLI？

### 效能监控

如何判断团队是否真正用好了 Copilot CLI？以下指标可以帮助评估：

| 指标 | 含义 | 健康标准 |
|------|------|---------|
| `单回合会话比例` | 开发者是否充分利用多轮对话能力 | 过高说明仅当搜索引擎用，需引导深度使用 |
| `文件提及成功率` | 上下文感知的准确性 | 低成功率说明需优化项目指令文件 |
| `Plan 模式使用率` | 复杂任务是否经过规划 | 过低说明跳过了规划直接编码 |

!!! tip "会话分析"

    使用 `/session` 查看当前会话的 Token 使用情况，使用 `/context` 监控上下文消耗。定期分析这些数据有助于优化指令文件和工作流。

### 团队采纳路径

不要指望团队一夜之间就熟练使用 Copilot CLI——就像任何新工具一样，需要分阶段逐步推进。以下是经过实践验证的三阶段采纳路径：

**阶段一：启蒙期**

- 团队成员在终端使用 `Interactive` 模式取代传统的 Web 搜索和文档查阅
- 建立 `.github/copilot-instructions.md`，统一基础编码规范
- 目标：让每个人习惯与 AI 对话

**阶段二：规范期**

- 要求所有非琐碎任务必须经过 `Plan` 模式审核
- 定期审查和优化指令文件，确保上下文质量
- 创建团队共享的 Skills 和 Agents，沉淀最佳实践
- 目标：将 AI 融入开发流程

**阶段三：自动化期**

- 推广 `Autopilot` 模式处理重构和批量任务
- 集成 AI 审查工具（如 CodeRabbit）到 CI 流程
- 持续监控团队效能指标，优化工具链
- 目标：实现 AI 驱动的开发闭环
