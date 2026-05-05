---
title: Agent 系统
description: 理解 Copilot CLI 的 Agent 架构，掌握内置 Agent 使用和自定义 Agent 创建
---

# Agent 系统

**本文你会学到**：

- 🤖 Agent 是什么，和 Skill、自定义指令有什么区别
- 📦 内置 Agent 及其适用场景
- 🎭 如何创建和使用自定义 Agent
- 💼 实战示例：安全审查 Agent、测试助手 Agent

打个比方：如果 Copilot 默认状态是一个`全栈工程师`，那 Agent 就是让他戴上`专业帽子`——戴上安全审查帽子时，他会用安全专家的视角看代码；戴上测试助手帽子时，他会专注于测试策略。

---

## ❓ 为什么需要 Agent？

默认的 Copilot 是"通用型选手"，什么都能做但未必做得最精。当你的任务需要特定领域的深度专业视角时——比如安全审计、性能优化、数据库设计——激活对应的 Agent 能获得更精准、更专业的输出。

---

## 📦 内置 Agent

Copilot CLI 自带以下内置 Agent，无需配置即可使用。主 Agent 会根据你的 prompt 和当前上下文自动选择合适的内置 Agent 作为子代理执行任务。

### 功能总览

| Agent | 功能 | 适用场景 |
|-------|------|---------|
| `explore` | 快速代码搜索和分析 | 理解代码库结构、查找文件、回答代码问题 |
| `task` | 执行命令并报告结果 | 运行测试、构建项目、安装依赖 |
| `code-review` | 高信噪比的代码审查 | 发现真正重要的问题：Bug、安全漏洞、逻辑错误 |
| `general-purpose` | 完整能力的通用 Agent | 复杂多步骤任务 |
| `init` | 项目初始化 | 分析项目并生成指令文件 |
| `configure-copilot` | MCP/Agent/Skill 配置助手（1.0.4 新增） | 交互式管理 MCP 服务器、Agents 和 Skills |
| `critic` | 互补模型审查（1.0.18 新增，实验性） | 执行计划和复杂实现时自动审查，提前捕获错误 |

### 详细特性

| Agent | 用途 | 特殊行为 |
|-------|------|---------|
| **Research** | 深度研究分析，提供详尽的技术调研报告 | 使用硬编码模型（不可通过 `/model` 切换），只能通过 `/research` 斜杠命令触发，主 Agent 不会自动调用 |
| **Task** | 执行开发命令并汇报结果 | 成功时只返回简报摘要，保持主上下文整洁；失败时返回完整输出便于排查 |
| **Explore** | 快速代码库分析 | 只读操作，使用代码智能工具（代码导航、grep、glob），拥有 GitHub MCP server 的只读访问权限，可与其他子代理并行运行 |
| **General-purpose** | 复杂多步骤任务 | 功能最全的通用 Agent，拥有与主 Agent 几乎相同的能力，可在独立上下文中运行，适合需要单独上下文窗口或并行执行的任务 |
| **Code-review** | 代码审查 | 极高信噪比——只报告真正重要的问题（Bug、安全漏洞、竞态条件、内存泄漏、逻辑错误），不评论代码风格和格式，不修改任何文件 |
| **Init** | 项目初始化 | 分析项目结构并生成 `CLAUDE.md` / `copilot-instructions.md` 指令文件 |
| **Configure-copilot** | 辅助配置管理 | 1.0.4 新增，交互式引导完成 MCP 服务器、Agent 和 Skill 的配置 |
| **Critic** | 批评性审查 | 1.0.18 实验性功能，使用与主模型不同的互补 LLM 来审查代码，在执行复杂实现时自动激活 |

!!! info "Critic Agent"

    Critic Agent 在你执行复杂实现时自动激活，使用一个互补模型（与主模型不同的 LLM）来审查代码——就像请了一位"第二意见"专家。目前为 Claude 模型的实验性功能（1.0.18 新增）。

!!! info "/research 深度研究改进（1.0.40）"

    `/research` 命令现在使用编排器/子 Agent 模型——主 Agent 负责规划和协调，多个子 Agent 并行执行不同方面的研究，最后汇总成更全面、可靠的深度研究报告。相当于从"一个人查资料"升级为"一个小团队分工调研再汇总"。

!!! warning "Research Agent 的模型限制"

    Research Agent 使用硬编码模型而非当前会话选择的模型，因此无法通过 `/model` 命令切换其使用的 LLM。这是刻意设计——确保研究质量的一致性。

---

## 🎮 使用 Agent

### 调用方式详解

Copilot 提供四种方式调用自定义 Agent，灵活度从低到高：

#### 在提示词中直接提及

在 prompt 中使用 `@CUSTOM-AGENT-NAME` 语法直接引用 Agent，类似在群聊中 @某人：

``` text
> 使用 @security-reviewer 检查 src/auth/ 目录下的所有文件
```

Copilot 会识别 `@` 引用并激活对应的自定义 Agent。

#### 触发词推断

不显式提及 Agent 名称，Copilot 根据 Agent 的 `description` 字段自动匹配最合适的 Agent。比如你的 Agent 描述中包含"安全审查"关键词，当你说"帮我做一次安全检查"时，Copilot 会自动推断应该使用该 Agent：

``` text
# 假设 security-reviewer 的 description 包含"安全审查/安全检查/安全审计"
> 检查所有 TypeScript 文件中的潜在安全问题
```

也可以在 Agent 描述中定义触发词（trigger word），让匹配更精准：

``` text
# "seccheck" 是在 Agent profile 中定义的触发词
> seccheck src/app/validator.go
```

#### 命令行标志编程式调用

通过 `--agent` 参数在启动时指定 Agent，适合脚本化或 CI/CD 场景：

``` bash
copilot --agent security-auditor --prompt "检查 src/app/validator.go"
```

其中 `security-auditor` 是 Agent profile 文件名去掉 `.agent.md` 后的部分，通常与 frontmatter 中的 `name` 字段一致。

#### 斜杠命令交互式选择

在交互模式中输入 `/agent`，从可用 Agent 列表中选择：

``` text
> /agent
# 弹出可选 Agent 列表，选择后输入任务 prompt
```

!!! note

    内置 Agent（explore、task、general-purpose、code-review）不会出现在 `/agent` 的列表中。它们由主 Agent 自动调度，不需要手动选择。

### 优先级规则

当多个层级存在同名自定义 Agent 时，Copilot 按以下优先级加载：

``` mermaid
graph LR
    A["system-level<br/>用户级 ~/.copilot/agents/"] -->|最高优先级| B[生效的 Agent]
    C["repository-level<br/>项目级 .github/agents/"] -->|次优先级| B
    D["organization-level<br/>组织级 .github-private/agents/"] -->|最低优先级| B
```

| 层级 | 存放位置 | 作用范围 | 优先级 |
|------|---------|---------|:------:|
| 用户级（system-level） | `~/.copilot/agents/` | 当前用户的所有项目 | 最高 |
| 项目级（repository-level） | `.github/agents/` | 当前仓库 | 中 |
| 组织级（organization-level） | `.github-private/agents/` | 组织/企业下所有项目 | 最低 |

!!! warning "文件名 vs frontmatter name"

    自定义 Agent 的**文件名**（如 `security-reviewer.agent.md`）和 frontmatter 中的 **`name` 字段**可以不同。文件名是 Agent 的标识符（`--agent` 参数使用文件名），而 `name` 字段是显示名称。建议保持两者一致以避免混淆。恢复会话时，Copilot CLI 会正确识别两者的映射关系（1.0.19 修复）。

### 交互式选择

``` text
# 打开 Agent 选择菜单
/agent

# 从列表中选择要使用的 Agent
```

### 命令行指定

``` bash
# 启动时指定 Agent
copilot --agent code-review

# 在 Programmatic 模式中使用
copilot --agent explore -p "找出所有未使用的导入"
```

### 在会话中使用

``` text
# 激活 Agent 后输入任务
> /agent
# 选择 code-review
> @src/auth.py 审查认证逻辑中的安全漏洞
```

### 向后台 Agent 发送消息

使用 `write_agent` 工具可以向后台运行的 Agent 发送跟进消息（1.0.5 新增），无需等待它完成：

``` text
# 启动后台任务
> 在后台分析整个项目的测试覆盖率

# 发送补充指令
> 写入后台 Agent：重点关注 src/auth/ 目录
```

子 Agent 会获得基于名称的可读 ID（如 `math-helper-0`）而非随机字符串（1.0.6 新增），方便在 `/tasks` 视图中识别。空闲子 Agent 在 `/tasks` 视图中 2 分钟后自动隐藏（1.0.8 改进）。

---

## 🔄 子代理行为

子代理（subagent）是主 Agent 为了完成特定子任务而临时启动的独立 Agent 实例。理解子代理的运行机制有助于更好地规划复杂任务。

### 独立上下文窗口

每个子代理拥有自己的上下文窗口，与主 Agent 的上下文相互隔离。这意味着：

- 子代理中填充的详细信息（搜索结果、文件内容等）不会占用主 Agent 的上下文空间
- 主 Agent 可以专注于高层规划和协调，将具体执行工作下放给子代理
- 尤其在大型任务中，这种隔离能有效避免上下文窗口溢出

打个比方：主 Agent 是项目经理，子代理是团队成员——每个人有自己的笔记本（上下文），做完工作后只把结论汇报给项目经理，而不是把所有草稿都塞到经理桌上。

### 并发与深度限制

子代理可以并行运行，但受到以下限制以防止失控（1.0.22 新增）：

| 限制类型 | 说明 |
|---------|------|
| **并发限制** | 同时运行的子代理数量有上限，防止资源耗尽 |
| **深度限制** | 子代理不能再生成子代理的子代理（禁止无限递归），确保调用链有界 |
| **只读代理可并行** | 像 Explore 这样只读的子代理可以安全地与其他子代理并行运行，因为它不会修改任何文件 |

### 会话标识

Shell 命令和 MCP server 运行时会自动接收 `COPILOT_AGENT_SESSION_ID` 环境变量（1.0.29 新增），其中包含当前 Agent 会话的唯一标识：

``` bash
# 在 Shell 脚本或 MCP server 中使用
echo "当前会话 ID: $COPILOT_AGENT_SESSION_ID"
```

当你需要在外部脚本中追踪是哪个 Agent 会话触发了操作时（如日志关联、审计追踪），这个变量非常有用。

### 时间线折叠

子代理的运行过程在时间线中以可折叠的方式展示（1.0.35 改进）：

- 子代理的思考过程（thinking）默认隐藏，减少视觉噪音
- 你可以展开查看详细的推理过程
- 只关注实际的工具调用和最终结果，让交互体验更清爽

### ACP 客户端支持

ACP 客户端（如 VS Code 中的 Copilot 扩展）现在可通过 `agent` 配置选项列出和切换自定义 Agent（1.0.40 新增），让你在非终端环境中也能方便地使用项目定义的专业角色。

---

## 🎭 自定义 Agent

通过 `.agent.md` 文件定义自定义 Agent，让 Copilot 扮演特定领域专家。

### 文件格式

``` markdown title=".github/agents/security-reviewer.agent.md"
---
name: security-reviewer
description: 安全审查专家，专注于 OWASP Top 10 和常见安全漏洞
tools:
  - read_file
  - search
  - grep
---

# 安全审查 Agent

你是一名高级安全工程师。审查代码时关注以下方面：

## 审查重点
- SQL 注入和参数化查询
- XSS 防护和输出编码
- 认证和授权绕过
- 敏感数据暴露
- CSRF 防护

## 审查标准
- 严重：必须立即修复（阻断发布）
- 高危：下一版本前修复
- 中危：纳入技术债务清单
- 低危：建议性改进

## 输出格式
每个问题包含：位置、严重程度、问题描述、修复建议。
```

### Frontmatter 字段

| 字段 | 必需 | 说明 |
|------|:----:|------|
| `name` | ✅ | Agent 标识符，使用小写字母加连字符 |
| `description` | ✅ | 简述 Agent 用途和激活时机 |
| `tools` | ❌ | Agent 可使用的工具列表 |
| `skills` | ❌ | 启动时预加载到 Agent 上下文中的 Skill 列表（1.0.22 新增） |

### 命名约定

- 使用`小写字母加连字符`：`security-reviewer`，不用 `SecurityReviewer`
- 名称应自描述功能：`pytest-helper` 而非 `test-agent`

---

## 📂 Agent 存放位置

=== "项目级"

    ``` text
    .github/agents/
    ├── security-reviewer.agent.md
    ├── doc-writer.agent.md
    └── pytest-helper.agent.md
    ```

    提交到 Git，团队共享。

=== "个人级"

    ``` text
    ~/.copilot/agents/
    ├── my-reviewer.agent.md
    └── daily-helper.agent.md
    ```

    仅本机可用，不提交到 Git。

    自定义 Agent 名称现在在状态栏底部可见（1.0.35 新增），可通过 `/statusline` 切换显示。这让多 Agent 切换场景下更容易确认当前使用的是哪个 Agent。

!!! warning "Copilot CLI 不加载 ~/.claude/ 目录（1.0.36）"

    从 1.0.36 起，`~/.claude/` 目录中的自定义 Agents、Skills 和 Commands 不再被 Copilot CLI 加载——它们是 Claude Code 专属目录。Copilot CLI 的个人级扩展应放在 `~/.copilot/` 目录下。此前版本会错误加载 `~/.claude/` 中的内容，可能导致意外行为。

=== "组织级"

    通过 GitHub 组织设置共享 Agent，所有组织成员自动获得。

!!! warning "文件名与显示名称"

    自定义 Agent 的文件名（如 `security-reviewer.agent.md`）和 frontmatter 中的 `name` 字段可以不同。恢复会话时，Copilot CLI 会正确识别两者的映射关系（1.0.19 修复）。建议保持文件名和 `name` 字段一致，避免混淆。

---

## 💼 实战示例

### 创建测试助手 Agent

``` markdown title=".github/agents/pytest-helper.agent.md"
---
name: pytest-helper
description: Python 测试专家，使用 pytest 框架生成全面的测试用例
tools:
  - read_file
  - write_file
  - execute_command
---

# Pytest 测试助手

你是一名 Python 测试工程师，擅长使用 pytest 编写高质量测试。

## 测试编写原则
- 使用 AAA 模式：Arrange, Act, Assert
- 每个测试只验证一个行为
- 使用描述性的测试名称：test_should_xxx_when_yyy
- 使用 @pytest.fixture 管理测试数据
- 使用 @pytest.mark.parametrize 做数据驱动测试

## 覆盖策略
1. 正常路径（Happy Path）
2. 边界条件（空值、极值、类型错误）
3. 异常场景（网络超时、文件不存在）
4. 并发场景（如适用）

## Mock 策略
- 外部服务调用使用 unittest.mock
- 数据库操作使用 fixture + 内存数据库
- 文件操作使用 tmp_path fixture
```

### 使用自定义 Agent

``` text
# 选择自定义 Agent
> /agent
# 从列表中选择 pytest-helper

# 给 Agent 分配任务
> @src/user_service.py 为 UserService 类生成完整的测试套件
```

---

## ⚖️ Agent vs Skill vs 指令

三者容易混淆，用一张表理清：

!!! quote "一句话区分"

    - `指令`：背景知识（项目规范）——始终生效
    - `Skill`：操作手册（怎么生成测试）——相关任务时自动触发
    - `Agent`：专业角色（安全专家）——需要时手动切换

| 维度 | Agent | Skill | Custom Instructions |
|------|-------|-------|---------------------|
| `激活方式` | 手动：`/agent` 或 `--agent` | 自动：根据 prompt 匹配 `description` | 始终生效 |
| `定义文件` | `.agent.md` | `SKILL.md` + 资源文件夹 | `.instructions.md` / `AGENTS.md` |
| `适用场景` | 专业角色审查/分析 | 特定任务的工作流（如测试生成） | 全局规范和偏好 |
| `能否生成代码` | ✅ | ✅ | 间接影响（引导 Copilot 行为） |
| `团队共享` | ✅（提交 `.github/agents/`） | ✅（提交 `.github/skills/`） | ✅（提交 `.github/`） |

!!! tip "选择建议"

    - `每次都需要的规则` → Custom Instructions
    - `特定任务自动触发` → Skill
    - `需要手动切换专家视角` → Agent
