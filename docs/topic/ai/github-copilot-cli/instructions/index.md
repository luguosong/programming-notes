# 自定义指令

**本文你会学到**：
- 🤔 自定义指令解决什么问题（为什么要用它）
- 📋 四种指令类型各自的适用场景
- ✍️ 如何编写高质量的指令内容
- 🎯 路径特定指令的匹配规则

打个比方：自定义指令就像给新入职的同事写一份`入职手册`——告诉他项目用什么技术栈、编码有什么规范、测试怎么跑。有了这份手册，他不用每次都来问你同样的问题。

!!! info "即时生效"

    修改指令文件后`无需重启 CLI`，保存后即可在下次提交提示时生效。

---

## 🤔 为什么要用自定义指令？

没有自定义指令时，Copilot 只能根据你的 prompt 和当前文件来工作。这意味着：
- 它不知道你的项目用什么编码规范（camelCase 还是 snake_case？）
- 它不知道测试该用什么框架（JUnit 5 还是 TestNG？）
- 它不知道提交信息该用什么格式

每次都要在 prompt 中重复这些信息，既低效又不稳定。自定义指令让这些信息`始终生效`，Copilot 每次响应时都会参考。

---

## 📋 指令类型总览

| 类型 | 文件位置 | 作用范围 | 提交到 Git |
|------|---------|---------|:----------:|
| 仓库级指令 | `.github/copilot-instructions.md` | 仓库内所有请求 | ✅ |
| 路径特定指令 | `.github/instructions/*.instructions.md` | 匹配路径的请求 | ✅ |
| Agent 指令 | `AGENTS.md`（仓库根 / 子目录） | 跨平台 AI Agent | ✅ |
| 本地指令 | `~/.copilot/copilot-instructions.md` | 仅本机所有仓库 | ❌ |

---

## 📁 仓库级指令

适用于在仓库上下文中发起的`所有请求`，是最常用的指令类型。

`位置`：仓库根目录下 `.github/copilot-instructions.md`

``` markdown title=".github/copilot-instructions.md"
# 项目约定

## 技术栈
- Python 3.12 + FastAPI
- PostgreSQL + SQLAlchemy
- pytest 用于测试

## 编码规范
- 使用 type hints
- 函数文档使用 Google 风格的 docstring
- 变量命名使用 snake_case

## 项目结构
- `src/api/` - API 路由层
- `src/services/` - 业务逻辑层
- `src/models/` - 数据模型层
- `tests/` - 测试，镜像 src 结构
```

---

## 🎯 路径特定指令

有时候不同目录的代码需要不同的规范——比如 `tests/` 目录下的代码和 `src/` 目录的代码风格可能不同。路径特定指令就是为此设计的：当 prompt 涉及与指定路径匹配的文件时，对应的指令会自动加载。

`位置`：`.github/instructions/` 目录下，文件名必须以 `.instructions.md` 结尾。

### applyTo 匹配规则

文件开头使用 YAML frontmatter 的 `applyTo` 关键字指定作用范围：

``` yaml title=".github/instructions/typescript.instructions.md"
---
applyTo: "**/*.ts,**/*.tsx"
---
# TypeScript 编码规范

- 优先使用 interface 而非 type alias
- 使用 ESLint + Prettier 格式化
- 组件 props 使用独立的 interface 定义
- 避免使用 any，必要时使用 unknown
```

`applyTo` 支持 glob 模式，多个模式用逗号分隔。

### excludeAgent 排除规则

可选的 `excludeAgent` 字段排除特定 Agent 使用这些指令：

``` yaml title=".github/instructions/test-style.instructions.md"
---
applyTo: "tests/**/*.py"
excludeAgent: "code-review"
---
# 测试编写规范

- 使用 pytest 框架
- 每个测试函数以 test_ 开头
- 使用 @pytest.fixture 管理测试数据
```

---

## 🤖 Agent 指令（AGENTS.md）

`AGENTS.md` 是一种跨平台标准，被 Copilot CLI、Claude Code、Gemini 等多种 AI Agent 共同支持。可以把它理解为"AI Agent 通用语言"——写一份，所有 AI 工具都能读懂。

### 查找位置（按优先级）

1. 仓库根目录 `AGENTS.md`
2. 当前工作目录 `AGENTS.md`
3. `COPILOT_CUSTOM_INSTRUCTIONS_DIRS` 环境变量指定目录中的 `AGENTS.md`

!!! tip "兼容其他 Agent"

    仓库根目录的 `CLAUDE.md` 和 `GEMINI.md` 也会被 Copilot CLI 读取，内容效果与 `AGENTS.md` 相同。

### 分层放置

大型项目可以在子目录中放置 `AGENTS.md`，当 Copilot 操作对应目录的文件时，会自动加载该目录的指令：

``` text
project-root/
├── AGENTS.md                # 全局规范
├── frontend/
│   └── AGENTS.md            # 前端特有规范
└── backend/
    └── AGENTS.md            # 后端特有规范
```

---

## 🏠 本地指令

本地指令适合存放`个人偏好`和`本地环境特有信息`——比如你习惯用中文回答、本地数据库的端口是 19002 而非默认的 3306。这些信息不适合提交到 Git（每个人的环境不同），所以放在本地配置中。

`位置`：`$HOME/.copilot/copilot-instructions.md`

``` markdown title="~/.copilot/copilot-instructions.md"
# 个人偏好

- 使用简体中文回答
- 代码注释使用中文
- 优先使用函数式编程风格
- 日志使用 loguru 而非 logging
```

---

## 🔍 扩展指令搜索路径

设置 `COPILOT_CUSTOM_INSTRUCTIONS_DIRS` 环境变量可以让 Copilot 在额外的目录中查找指令文件：

=== "Linux / macOS"

    ``` bash
    export COPILOT_CUSTOM_INSTRUCTIONS_DIRS="/path/to/team-instructions,/path/to/org-instructions"
    ```

=== "Windows（PowerShell）"

    ``` powershell
    $env:COPILOT_CUSTOM_INSTRUCTIONS_DIRS = "C:\team-instructions;C:\org-instructions"
    ```

Copilot CLI 会在这些目录中查找 `AGENTS.md` 和 `.github/instructions/**/*.instructions.md` 文件。

---

## ✨ /init 命令

`/init` 命令让 Copilot 自动分析当前项目并生成指令文件：

``` text
/init
```

Copilot 会扫描项目结构、依赖文件、已有配置等，然后提议创建或更新：

- `.github/copilot-instructions.md`
- `AGENTS.md`
- 其他建议的指令文件

!!! tip "新项目最佳实践"

    在新项目中首先运行 `/init`，让 Copilot 了解项目并生成基础指令文件。然后在此基础上手动调整和完善。

---

## 📝 指令编写最佳实践

### 内容建议

- `简洁具体`：明确说明规则和偏好，避免模糊描述。"使用 camelCase 命名" 比 "命名要规范" 有效得多
- `结构化`：使用标题和列表组织内容，方便 Copilot 解析
- `包含示例`：展示正确和错误的做法
- `列出命令`：包含构建、测试、部署等常用命令——Copilot 可以直接调用它们

### 常见误区

| 问题 | 说明 |
|------|------|
| 指令过长 | 指令会消耗上下文窗口的 token，过长会挤压实际对话空间 |
| 内容重复 | 不同层级的指令文件之间不要重复同一信息 |
| 过于泛化 | 如"写好代码"之类无法执行的指令没有意义 |
| 相互矛盾 | 不同指令文件之间的规则不应冲突 |
