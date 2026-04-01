# 推荐扩展

Copilot CLI 的能力可以通过 MCP 服务器、插件和 Skills 进行大幅扩展。本页整理了实用的推荐扩展及其安装方法。

---

## 推荐 MCP 服务器

MCP 服务器为 Copilot 连接外部数据源和工具。以下是常用的 MCP 服务器推荐。

### GitHub MCP 服务器（内置）

内置于 Copilot CLI，**无需安装**。提供对 GitHub 平台的完整访问。

**提供的能力**：

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

**使用场景**：

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

**使用场景**：

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
| **GitHub**（内置） | GitHub API 完整访问 | 无需安装 |
| **Context7** | 实时库文档查询 | `npx @upstash/context7-mcp` |
| **Filesystem** | 受控文件系统访问 | `npx @modelcontextprotocol/server-filesystem` |
| **Playwright** | 浏览器自动化 | `npx @playwright/mcp@latest` |
| **Microsoft Learn** | 微软技术文档 | `npx @nicobailon/mcp-microsoft-learn` |
| **PostgreSQL** | PostgreSQL 数据库 | `npx @modelcontextprotocol/server-postgres` |

!!! tip "发现更多 MCP 服务器"

    - [GitHub MCP Registry](https://github.com/modelcontextprotocol/servers) — 官方 MCP 服务器目录
    - [MCP 服务器搜索](https://mcp.so/) — 社区 MCP 服务器聚合

---

## 推荐插件

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
| **advanced-security** | GitHub Advanced Security（GHAS），密钥扫描、代码安全分析 | `/plugin install advanced-security@copilot-plugins` |
| **java-springboot** | Spring Boot 开发最佳实践、Agent 和 Skill | `/plugin install java-springboot@copilot-plugins` |
| **java-junit** | JUnit 5 单元测试最佳实践 | `/plugin install java-junit@copilot-plugins` |
| **java-docs** | Java 文档（Javadoc）生成 | `/plugin install java-docs@copilot-plugins` |
| **csharp-nunit** | NUnit 测试最佳实践 | `/plugin install csharp-nunit@copilot-plugins` |
| **javascript-typescript-jest** | Jest 测试最佳实践 | `/plugin install javascript-typescript-jest@copilot-plugins` |

### 社区插件市场（awesome-copilot）

| 插件 | 功能 | 安装命令 |
|------|------|---------|
| **context-engineering** | 上下文工程，提升 Copilot 上下文感知能力 | `/plugin install context-engineering@awesome-copilot` |
| **database-data-management** | 数据库管理（PostgreSQL、MySQL、SQL Server） | `/plugin install database-data-management@awesome-copilot` |
| **cloud-design-patterns** | 42 种云架构设计模式参考 | `/plugin install cloud-design-patterns@awesome-copilot` |
| **documentation-writer** | 基于 Diátaxis 框架的技术文档写作 | `/plugin install documentation-writer@awesome-copilot` |
| **quality-playbook** | 质量体系：测试协议、代码审查、规范审计 | `/plugin install quality-playbook@awesome-copilot` |
| **multi-stage-dockerfile** | 优化的多阶段 Dockerfile 生成 | `/plugin install multi-stage-dockerfile@awesome-copilot` |
| **polyglot-test-agent** | 多语言测试生成（Python、Java、Go、Rust 等） | `/plugin install polyglot-test-agent@awesome-copilot` |
| **secret-scanning** | 密钥扫描和推送保护配置 | `/plugin install secret-scanning@awesome-copilot` |

!!! tip "浏览更多插件"

    ``` text
    # 浏览官方市场
    /plugin marketplace browse copilot-plugins

    # 浏览社区市场
    /plugin marketplace browse awesome-copilot
    ```

    也可以访问 [awesome-copilot.github.com/plugins](https://awesome-copilot.github.com/plugins/) 在线浏览。

---

## 推荐 Skills

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

## 我的推荐配置

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
# 官方插件
/plugin install java-springboot@copilot-plugins
/plugin install java-junit@copilot-plugins
/plugin install advanced-security@copilot-plugins

# 社区插件
/plugin install context-engineering@awesome-copilot
/plugin install database-data-management@awesome-copilot
/plugin install polyglot-test-agent@awesome-copilot
```

### 推荐创建的 Skills

``` text
.github/skills/
├── code-review/SKILL.md       # 代码审查
├── test-generator/SKILL.md    # 测试生成
├── git-commit/SKILL.md        # Commit 规范
└── api-docs/SKILL.md          # API 文档
```
