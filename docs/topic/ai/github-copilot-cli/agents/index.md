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

Copilot CLI 自带以下内置 Agent，无需配置即可使用：

| Agent | 功能 | 适用场景 |
|-------|------|---------|
| `explore` | 快速代码搜索和分析 | 理解代码库结构、查找文件、回答代码问题 |
| `task` | 执行命令并报告结果 | 运行测试、构建项目、安装依赖 |
| `code-review` | 高信噪比的代码审查 | 发现真正重要的问题：Bug、安全漏洞、逻辑错误 |
| `general-purpose` | 完整能力的通用 Agent | 复杂多步骤任务 |
| `init` | 项目初始化 | 分析项目并生成指令文件 |

---

## 🎮 使用 Agent

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
