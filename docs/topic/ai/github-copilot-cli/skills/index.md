---
title: Skills 技能系统
description: 创建和管理 Skill 文件，让 Copilot CLI 按需加载专业知识模块
---

# Skills 技能系统

**本文你会学到**：

- 🧩 Skills 解决什么问题，和自定义指令有什么区别
- ⚡ Skills 的自动触发机制
- 🛠️ 如何创建和管理 Skill
- 💼 实战示例：代码审查 Skill、文档生成 Skill

打个比方：如果自定义指令是给 Copilot 的`入职手册`（始终生效的规则），那 Skill 就像是`岗位操作指南`——只有当 Copilot 被安排做特定工作时才拿出来参考。比如"生成测试"时参考测试指南，"写文档"时参考文档指南。

---

## ⚖️ Skills 与指令的区别

!!! quote "一句话判断"

    - `自定义指令`：几乎每次对话都需要的规则（如编码规范、项目结构）
    - `Skills`：只在相关任务出现时才需要的详细工作流（如"生成 Playwright 测试"）

为什么两者都需要？因为指令文件会`始终占用上下文窗口`——如果你把所有操作指南都塞进指令文件，上下文窗口会被挤满，Copilot 的响应质量反而下降。Skill 只在需要时加载，更节省上下文空间。

## 🎯 自动触发机制

Skill 的核心特性是`自动触发`——你不需要手动激活它，Copilot 会根据你的 prompt 自动判断是否加载。判断依据是 Skill 的 `description` 字段。

``` text
# SKILL.md 中 description: "为 Web 应用生成端到端测试，使用 Playwright 框架"

# 以下 prompt 会自动触发该 Skill：
> 为登录页面生成 Playwright 测试
> 写一个端到端测试覆盖购物车流程

# 以下 prompt 不会触发：
> 修复 CSS 样式问题
> 优化数据库查询
```

!!! tip "description 是关键"

    Skill 是否被触发完全取决于 `description` 字段的描述质量。写清楚 Skill 的功能和适用场景，能显著提高自动匹配的准确性。

---

## 🛠️ 创建 Skill

### 目录结构

每个 Skill 拥有独立目录，目录名使用`小写字母加连字符`：

``` text
.github/skills/webapp-testing/
├── SKILL.md          # 必须命名为 SKILL.md（大写）
├── example-test.ts   # 可选：示例文件
├── templates/        # 可选：模板目录
└── config.json       # 可选：配置文件
```

### SKILL.md 格式

由 YAML Frontmatter 和 Markdown 正文两部分组成：

``` markdown title=".github/skills/webapp-testing/SKILL.md"
---
name: webapp-testing
description: 为 Web 应用生成端到端测试，使用 Playwright 框架。当用户要求生成 E2E 测试、集成测试或 UI 测试时使用此技能。
---

# Web 应用端到端测试

## 测试结构
- 使用 Page Object 模式组织测试
- 每个页面对应一个 Page Object 类
- 测试文件放在 `tests/e2e/` 目录

## 测试规范
- 每个测试用例独立运行，不依赖其他测试的状态
- 使用 `test.describe` 按功能分组
- 使用 `test.beforeEach` 做通用初始化

## 示例
参考 `example-test.ts` 中的写法。
```

### Frontmatter 字段

| 字段 | 必需 | 说明 |
|------|:----:|------|
| `name` | ✅ | 唯一标识符，小写字母加连字符，通常与目录名一致 |
| `description` | ✅ | 描述功能和适用场景，用于自动匹配 |
| `license` | ❌ | 许可证说明 |

---

## 📂 Skills 存放位置

=== "项目级"

    团队共享，提交到 Git：

    - `.github/skills/`
    - `.claude/skills/`（兼容 Claude Code）
    - `.agents/skills/`

=== "个人级"

    仅本机可用：

    - `~/.copilot/skills/`
    - `~/.claude/skills/`（兼容 Claude Code）
    - `~/.agents/skills/`（1.0.11 新增）

!!! info "内置 Skills"

    Copilot CLI 内置部分 Skills（如 Copilot Cloud Agent 环境定制指南），无需手动安装即可使用（1.0.17 新增）。使用 `/skills list` 可查看所有可用 Skills（含内置和自定义）。

!!! warning "Copilot CLI 不加载 ~/.claude/ 目录（1.0.36）"

    从 1.0.36 起，`~/.claude/skills/` 目录中的 Skills 不再被 Copilot CLI 加载。如果你之前将个人 Skill 放在了 `~/.claude/skills/`，需要迁移到 `~/.copilot/skills/` 或 `~/.agents/skills/`。

---

## 🎮 使用 Skill

### 自动触发

大多数情况下，只需要正常写 prompt，Copilot 会自动判断是否需要加载某个 Skill：

``` text
# 如果你有一个 webapp-testing Skill
> 为用户注册流程生成端到端测试
# Copilot 自动加载 webapp-testing Skill 并按照其规范生成测试
```

Skill 指令（SKILL.md 正文）在对话的多个轮次间持久保留（1.0.25 修复）。此前存在一个 bug：Skill 在首轮加载后，后续轮次中指令内容会丢失，导致 Copilot 在长对话中忘记 Skill 的约束。

通过 `-i`（`--interactive`）启动时，Skill 斜杠命令（如 `/skill-name`）现在能在启动时正确识别（1.0.35 修复），此前在非交互模式启动后再切换到交互模式时，Skill 命令可能无法被发现。

### 显式调用

也可以通过 `/skill-name` 语法显式调用：

``` text
> 使用 /webapp-testing 为登录页面生成测试
```

---

## 🔧 Skill 管理命令

| 命令 | 功能 |
|------|------|
| `/skills list` | 列出当前可用的所有技能 |
| `/skills info <name>` | 查看某个技能的详细信息 |
| `/skills add <path>` | 添加一个额外的技能搜索路径 |
| `/skills reload <name>` | 重新加载技能（修改后无需重启 CLI） |
| `/skills remove <dir>` | 移除技能目录 |

!!! tip "超大 Skill 也可调用"

    如果某个 Skill 的内容超过了 token 限制，它仍然会出现在 `/skills list` 中并可以通过名称显式调用（1.0.32 改进），只是不参与自动触发。这避免了大型 Skill 因体积问题被完全屏蔽。

!!! tip "禁用的 Skill 不再显示"

    被禁用的 Skill 不再出现在斜杠命令列表中（1.0.36 改进），减少了列表中的噪音。如需重新使用，先通过 `/skills list` 确认状态再启用。

!!! tip "ACP 客户端中的 Skill 斜杠命令（1.0.40 新增）"

    Skills 现在在 ACP 客户端（如 VS Code 中的 Copilot）中也可用作斜杠命令，与 CLI 中的使用体验保持一致。这意味着你在 VS Code 中也能通过 `/skill-name` 的方式显式调用项目定义的 Skill。

---

## 💼 实战示例

### 创建代码审查 Skill

``` markdown title=".github/skills/python-review/SKILL.md"
---
name: python-review
description: Python 代码审查技能。当用户要求审查 Python 代码、检查代码质量或进行安全审计时自动激活。
---

# Python 代码审查

## 审查清单
1. **类型安全**：检查类型注解是否完整和正确
2. **错误处理**：是否有未处理的异常、空的 except 块
3. **安全性**：SQL 注入、路径遍历、不安全的反序列化
4. **性能**：N+1 查询、不必要的循环、内存泄漏
5. **可维护性**：函数长度、圈复杂度、命名规范

## 输出格式
对每个问题，说明：
- 📍 位置（文件:行号）
- 🏷️ 类别（类型安全/错误处理/安全性/性能/可维护性）
- 📝 问题描述
- ✅ 修复建议
```

### 创建文档生成 Skill

``` markdown title=".github/skills/doc-generator/SKILL.md"
---
name: doc-generator
description: API 文档生成器。当用户要求生成 API 文档、写 README 或创建使用说明时自动激活。
---

# API 文档生成

## 文档结构
每个 API 端点的文档包含：
- 端点路径和 HTTP 方法
- 请求参数（路径参数、查询参数、请求体）
- 响应格式和状态码
- 使用示例（curl 命令）
- 错误处理说明

## 风格要求
- 使用 Markdown 格式
- 代码示例使用代码块并标注语言
- 参数说明使用表格
```

---

## ⚖️ Skills vs Agent vs 指令 vs MCP

| 维度 | Skills | Agent | Instructions | MCP |
|------|--------|-------|-------------|-----|
| `核心功能` | 任务增强工作流 | 专业角色扮演 | 全局规则 | 外部数据源 |
| `激活方式` | 自动（prompt 匹配） | 手动（`/agent`） | 始终生效 | 自动（需要时调用） |
| `定义方式` | `SKILL.md` + 资源文件 | `.agent.md` | `.instructions.md` | JSON 配置 |
| `包含资源` | ✅ 示例、模板、配置 | ❌ 仅 Markdown | ❌ 仅 Markdown | ✅ 外部工具 |
| `典型用例` | 测试生成、代码审查 | 安全专家、DBA | 编码规范 | GitHub API、数据库 |
