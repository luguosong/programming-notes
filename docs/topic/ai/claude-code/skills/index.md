---
title: Skills
description: 通过 Skills 为 Claude Code 添加可复用的知识、工作流和参考文档
---

**本文你会学到**：

- 🎯 什么是 Skill，它和 `CLAUDE.md` 有什么区别
- 🔧 参考型 Skill 和动作型 Skill 的不同用法
- 📦 如何从零创建一个自定义 Skill
- ⚙️ Front matter 各配置项的含义和作用
- 🚀 Skill 的自动触发和手动调用机制
- 🔧 自定义命令（Commands）与 Skill 的关系
- ⏳ Skill 内容在会话中的生命周期管理
- 🔒 如何限制 Claude 的 Skill 访问权限
- ✅ 编写高质量 Skill 的最佳实践
- 📦 官方内置 Skills 开箱即用
- 🔍 常见 Skill 问题的故障排除

## 🎯 什么是 Skill

想象一下，你有一个新同事入职了。你除了给他一本《公司规范手册》让他随时翻阅（这就是 `CLAUDE.md`），还可以给他准备一些**可复用的 SOP 手册**——比如「部署流程」「代码审查清单」「API 设计规范」。这些手册不用他刻意去翻，遇到相关任务时他会**自动拿出来用**，或者你明确跟他说「按部署手册来」时他就照做。

**Skill 就是这样的 SOP 手册**（v2.0.20 首次引入）。它是 Claude Code 的扩展机制——你写一个 `SKILL.md` 文件，里面放 instructions（指令），Claude 就会把它加入自己的工具箱。当对话上下文匹配时，Claude 会自动加载对应 Skill；你也可以随时通过 `/skill-name` 手动调用。v2.1.0 新增了 Skill 热重载——修改 `SKILL.md` 后无需重启会话即可生效。

💡 **Skill 和 `CLAUDE.md` 的区别**：`CLAUDE.md` 是全局上下文，Claude 每次对话都会读取；Skill 是按需加载的专项知识，只在相关时才会激活。你可以把 `CLAUDE.md` 理解为「入职手册」，Skill 理解为「岗位 SOP」。

Skills 遵循 [Agent Skills 开放标准](https://github.com/anthropics/agent-skills)，这意味着你写的 Skill 不仅能在 Claude Code 中使用，也可以跨其他支持该标准的 AI 工具复用。Claude Code 在此基础上做了扩展，增加了调用控制、子代理执行、动态上下文注入等能力。

## 📂 Skill 的两种类型

根据 Skill 的用途，可以把它分为两大类：**参考型**和**动作型**。理解这个分类很重要——它决定了你该怎么设计 Skill 的内容和触发方式。

### 参考型 Skill（给 Claude 看的知识库）

参考型 Skill 本质上是在给 Claude 补充知识——约定、规范、风格指南、领域知识等。它不会主动执行某个动作，而是让 Claude 在做其他事情时「心里有数」。

举个例子，你可以在项目中放一个 API 设计规范的 Skill：

``` yaml title=".claude/skills/api-conventions/SKILL.md"
---
name: api-conventions
description: 本项目的 API 设计规范。编写或审查 API 接口时自动加载
---

编写 API 接口时，遵循以下约定：

- 使用 RESTful 命名风格：资源用名词复数（`/users`、`/orders`）
- 统一错误响应格式：`{ "code": "ERROR_CODE", "message": "描述" }`
- 所有接口必须做参数校验，不合法的请求返回 `400 Bad Request`
- 分页接口统一使用 `page` 和 `size` 参数
```

参考型 Skill 的特点是：**Claude 会在相关上下文中自动加载它**，把它作为「背景知识」来辅助当前工作。你通常不需要（也不应该）手动用 `/` 命令去调用它。

### 动作型 Skill（用 /命令 触发的工作流）

动作型 Skill 是一个明确的**工作流程**——比如「部署应用」「提交代码」「创建 PR」。这种 Skill 你通常希望**只有你手动触发时才执行**，而不想让 Claude 自作主张地运行。

典型示例——部署 Skill：

``` yaml title=".claude/skills/deploy/SKILL.md"
---
name: deploy
description: 将应用部署到生产环境
disable-model-invocation: true
---

部署 $ARGUMENTS 到生产环境，依次执行以下步骤：

1. 运行完整测试套件，确保所有测试通过
2. 执行生产构建
3. 推送到部署目标
4. 验证部署是否成功
```

注意上面的 `disable-model-invocation: true`——这告诉 Claude「不要自动触发这个 Skill」。你绝对不会希望 Claude 看到你的代码改好了就自作主张去部署。

📝 **小结**：两种类型的对比：

| 维度 | 参考型 Skill | 动作型 Skill |
|------|-------------|-------------|
| **本质** | 知识补充 | 工作流程 |
| **触发方式** | Claude 自动匹配 | 用户手动 `/命令` |
| **典型场景** | API 规范、代码风格、领域知识 | 部署、提交、代码生成 |
| **`disable-model-invocation`** | `false`（默认） | `true` |
| **`user-invocable`** | `true`（默认） | `true`（默认） |

## ✏️ 创建自定义 Skill

### Front matter 配置项说明

每个 `SKILL.md` 文件由两部分组成：**YAML Front Matter**（`---` 包裹的配置区）和 **Markdown 正文**（Claude 执行 Skill 时遵循的指令）。

一个完整的 Front matter 示例：

```yaml
---
name: my-skill
description: 这个 Skill 做什么，以及什么时候该用它
argument-hint: "[issue-number]"
disable-model-invocation: true
user-invocable: true
allowed-tools: Read Grep Glob
model: sonnet
effort: high
context: fork
agent: Explore
paths: "src/**/*.ts"
shell: bash
---
```

各字段的含义（分为基础字段和高级字段两组）：

**基础字段**——大多数 Skill 只需配置这些：

| 字段 | 是否必填 | 说明 |
|------|---------|------|
| `name` | 否 | Skill 的显示名称，也是 `/` 命令的名字。省略时使用目录名。只能用小写字母、数字和连字符（最长 64 字符） |
| `description` | **推荐** | 描述 Skill 的功能和适用场景。Claude 根据它判断是否自动加载。与 `when_to_use` 共享 1,536 字符预算（2.1.105 起） |
| `when_to_use` | 否 | 补充说明"何时触发"——列举触发短语和示例请求。与 `description` 拼接后共享 1,536 字符预算 |
| `argument-hint` | 否 | 自动补全时显示的参数提示，如 `[issue-number]` 或 `[filename] [format]` |
| `arguments` | 否 | 命名参数列表，用于 `$name` 替换。接受空格分隔的字符串或 YAML 列表，名称按顺序映射到参数位置 |
| `disable-model-invocation` | 否 | 设为 `true` 禁止 Claude 自动触发，只能手动 `/命令` 调用。同时阻止 Skill 被预加载到子代理中 |
| `user-invocable` | 否 | 设为 `false` 将 Skill 从 `/` 菜单隐藏（仅 Claude 可自动调用） |

**高级字段**——需要精细控制时使用：

| 字段 | 是否必填 | 说明 |
|------|---------|------|
| `allowed-tools` | 否 | Skill 激活时 Claude 可免确认使用的工具列表 |
| `model` | 否 | 指定 Skill 使用的模型 |
| `effort` | 否 | 思考力度：`low` / `medium` / `high` / `max`（仅 Opus 4.6） / `xhigh`（仅 Opus 4.7，v2.1.111 新增） |
| `context` | 否 | 设为 `fork` 时，Skill 在独立的子代理中运行（v2.1.0 新增） |
| `agent` | 否 | 配合 `context: fork` 使用，指定子代理类型（如 `Explore`、`Plan`）（v2.1.0 新增） |
| `paths` | 否 | Glob 模式，限制 Skill 只在操作匹配的文件时才自动激活（v2.1.84 支持 YAML glob 列表） |
| `shell` | 否 | Shell 命令执行环境：`bash`（默认）或 `powershell` |
| `hooks` | 否 | 绑定到 Skill 生命周期的钩子（v2.1.0 新增） |

`when_to_use` 是 `description` 的补充，两者拼接后计入 1,536 字符预算。最佳实践是：`description` 写简短的触发关键词（<50 字符），`when_to_use` 补充更详细的"什么时候用/不用"说明：

```yaml
---
name: code-review
description: Use for PR reviews with focus on correctness.
when_to_use: |
  Use when reviewing TypeScript PRs or when explicitly asked.
  Avoid for large architectural reviews — use /design-review instead.
---
```

💡 **两个关键字段的组合效果**：

| 配置 | 你能调用 | Claude 能调用 | 上下文加载时机 |
|------|---------|-------------|-------------|
| 默认（都不设置） | ✅ | ✅ | description 始终在上下文中，完整内容在触发时加载 |
| `disable-model-invocation: true` | ✅ | ❌ | description 不在上下文中，你调用时才加载 |
| `user-invocable: false` | ❌ | ✅ | description 始终在上下文中，Claude 调用时加载完整内容 |

### Skill 的目录结构

每个 Skill 是一个**独立目录**，核心文件是 `SKILL.md`，还可以附带辅助文件：

```
my-skill/
├── SKILL.md           # 主指令文件（必填）
├── reference.md       # 详细参考文档（按需加载）
├── template.md        # 模板文件（Claude 填充用）
├── examples/
│   └── sample.md      # 输出示例
└── scripts/
    └── validate.sh    # 可执行脚本
```

⚠️ **建议 `SKILL.md` 控制在 500 行以内**。详细内容拆分到辅助文件中，然后在 `SKILL.md` 里引用它们，让 Claude 知道每个文件是什么、什么时候该读。

引用辅助文件的写法：

```markdown
## 附加资源

- 完整 API 文档见 [reference.md](reference.md)
- 更多使用示例见 [examples/sample.md](examples/sample.md)
```

## ⚡ Skill 的加载与触发

### 模型自动触发

Claude 并不是把所有 Skill 全部加载进上下文——那样太浪费 token 了。实际的机制是：

1. **Skill 的 `description` 始终在上下文中**（除非设了 `disable-model-invocation: true`），Claude 知道「有哪些 Skill 可用」
2. 当你的对话内容与某个 Skill 的 description 匹配时，Claude **自动加载完整 Skill 内容**
3. 如果 Skill 设了 `paths` 字段，则只有当你操作匹配的文件时才会触发

举个例子，你有一个 description 为「本项目的 API 设计规范」的 Skill。当你跟 Claude 说「帮我写一个查询用户的接口」时，Claude 会自动加载这个 Skill，按规范来写接口。

⚠️ 如果 Skill 描述在列表中被截断了（你有很多 Skill 时会发生），Claude 可能匹配不到。解决办法：把 description 的**关键用例前置**，或者调大 `SLASH_COMMAND_TOOL_CHAR_BUDGET` 环境变量。v2.1.32 起，Skill 描述预算自动按上下文窗口的一定比例缩放，大窗口用户能看到更多 Skill。

### 手动调用（/命令）

所有 `user-invocable` 为 `true`（默认）的 Skill 都可以通过 `/` 命令手动调用。在 Claude Code 中输入 `/` 即可看到所有可用的 Skill 和内置命令列表（v2.1.111 改进：`/skills` 菜单现在支持按 `t` 键按估算 token 数排序，帮你快速定位最耗上下文的 Skill）。v2.1.121 进一步优化了 `/skills` 界面，新增输入搜索框，在列表很长时可以快速定位目标 Skill。

除了自定义 Skill，内置斜杠命令（如 `/init`、`/review`、`/security-review`）也可以被模型通过 Skill tool 发现和调用（v2.1.108 新增）——这意味着模型能自主选择合适的内置命令来完成任务。

Skill 支持传递参数，通过 `$ARGUMENTS` 变量接收：

```markdown
---
name: fix-issue
description: 修复 GitHub Issue
disable-model-invocation: true
---

修复 GitHub Issue $ARGUMENTS，按以下流程执行：

1. 读取 Issue 描述
2. 理解需求
3. 实现修复
4. 编写测试
5. 创建提交
```

调用方式：`/fix-issue 123`，Claude 收到的指令就是「修复 GitHub Issue 123...」。

还支持按位置访问参数：

| 语法 | 说明 | 示例 |
|------|------|------|
| `$ARGUMENTS` | 所有参数（整体） | `/deploy staging` → `staging` |
| `$ARGUMENTS[N]` | 第 N 个参数（从 0 开始） | `$ARGUMENTS[0]` |
| `$N` | 上面语法简写 | `$0`、`$1` |
| `$name` | 命名参数，在 `arguments` 中声明后使用 | `arguments: [issue, branch]` → `$issue`、`$branch` |

命名参数通过 `arguments` 字段声明，名称按顺序映射到位置参数：

```yaml
---
name: migrate-component
description: 将组件从一个框架迁移到另一个
arguments: [component, from, to]
---

将 $component 组件从 $from 迁移到 $to，保留所有现有行为和测试。
```

调用 `/migrate-component SearchBar React Vue`，`$component` → `SearchBar`，`$from` → `React`，`$to` → `Vue`。

此外还有三个有用的内置变量：

- `${CLAUDE_SESSION_ID}` — 当前会话 ID（适合日志记录和会话关联）
- `${CLAUDE_SKILL_DIR}` — Skill 所在目录的绝对路径（v2.1.69 新增，用于引用捆绑脚本）
- `${CLAUDE_EFFORT}` — 当前思考力度：`low` / `medium` / `high` / `xhigh` / `max`（v2.1.111 新增）

### 动态上下文注入

Skill 支持 `` !`<command>` `` 语法，在发送给 Claude **之前**执行 shell 命令，用命令输出替换占位符。这是预处理，不是 Claude 执行的。

```markdown
---
name: pr-summary
description: 总结 Pull Request 的变更
---

## PR 上下文

- PR diff: !`gh pr diff`
- PR 评论: !`gh pr view --comments`
- 变更文件: !`gh pr diff --name-only`

## 任务

总结这个 PR 的主要变更...
```

运行时，Claude 看到的是命令的**输出结果**，而不是命令本身。

对于多行命令，使用以 ` ```! ` 开头的围栏代码块：

````
## 环境信息
```!
node --version
npm --version
git status --short
```
````

⚠️ 要禁用来自用户、项目、插件或 `--add-dir` 目录源的 Skill 中的 shell 命令执行，在[设置](/zh-CN/settings)中添加 `"disableSkillShellExecution": true`。启用后，每个命令被替换为 `[shell command execution disabled by policy]`，不再执行。**捆绑和托管 Skill 不受影响**——此设置在[托管设置](/zh-CN/permissions#managed-settings)中最有用。

💡 要在 Skill 中启用[扩展思考](/zh-CN/common-workflows#use-extended-thinking-thinking-mode)，在 Skill 内容的任意位置包含单词 `ultrathink` 即可。

### 自定义命令（Commands）

Claude Code 还有一个更早的机制叫 **Commands**，存放在 `.claude/commands/` 目录下。好消息是——**Commands 已经合并到 Skills 体系中**。

两者等价关系：

| Commands 写法 | Skills 写法 | 效果相同 |
|--------------|------------|---------|
| `.claude/commands/deploy.md` | `.claude/skills/deploy/SKILL.md` | 都会创建 `/deploy` 命令 |

✅ 如果你已经有 `.claude/commands/` 目录下的文件，它们**继续有效**，无需迁移。但新建 Skill 时推荐使用 `.claude/skills/` 目录，因为 Skills 支持更多功能（辅助文件、Front matter 配置等）。

⚠️ 如果同名 Skill 和 Command 都存在，**Skill 优先**。

## ⏳ Skill 内容生命周期

当你或 Claude 调用一个 Skill 时，`SKILL.md` 的内容作为单条消息进入对话，并在会话剩余部分一直存在。Claude Code **不会**在后续轮次重新读取 Skill 文件——因此，需要贯穿整个任务的指导应该写成**常设指令**，而不是一次性步骤。

**自动压缩行为**：当对话被压缩以释放上下文时，Claude Code 会保留每个已调用 Skill 的前 5,000 个 token，从最近调用的 Skill 开始填充（总预算 25,000 token）。如果你在一个会话中调用了多个 Skill，较旧的 Skill 可能在压缩后完全丢失。

💡 如果 Skill 似乎在第一个响应后就不再影响行为，内容通常还在，只是模型选择了其他方法。加强 Skill 的 `description` 和指令可以让模型持续偏好它。如果 Skill 很大或之后又调用了其他 Skill，在压缩后**重新调用**它以恢复完整内容。

## 🔒 限制 Claude 的 Skill 访问

默认情况下，Claude 可以调用任何没有设置 `disable-model-invocation: true` 的 Skill。有三种方式可以精确控制 Claude 的 Skill 访问：

**通过 `/permissions` 拒绝 Skill 工具（禁用所有 Skill）**：

```text
# 添加到拒绝规则：
Skill
```

**使用权限规则允许或拒绝特定 Skill**：

```text
# 只允许特定 Skill
Skill(commit)
Skill(review-pr *)

# 拒绝特定 Skill
Skill(deploy *)
```

权限语法：`Skill(name)` 精确匹配，`Skill(name *)` 前缀匹配（带任意参数）。

**在 Front matter 中隐藏单个 Skill**：添加 `disable-model-invocation: true` 会从 Claude 的上下文中完全删除该 Skill。

完整的 `.claude/settings.json` 配置示例：

``` json title=".claude/settings.json — Skill 权限配置"
{
  "permissions": {
    "allow": [
      "Skill(commit)",
      "Skill(review-pr *)"
    ],
    "deny": [
      "Skill(deploy *)"
    ]
  }
}
```

⚠️ `user-invocable` 字段仅控制菜单可见性，不控制 Skill 工具访问。要用编程方式阻止调用，请使用 `disable-model-invocation: true`。

## ✅ Skill 最佳实践

### 🎯 写好 description：让模型知道"何时该用我"

description 的核心作用不是告诉模型"我是干什么的"，而是让它知道"**什么时候该用我**"。这两者差很多——前者触发精准，后者触发模糊。

```yaml
# ❌ 描述"做什么"——任何后端工作都可能触发（~45 tokens）
description: |
  This skill helps you review code changes.
  It checks for common issues like unsafe code, error handling...
  Use this when you want to ensure code quality before merging.

# ✅ 描述"何时用"——精准触发（~9 tokens）
description: Use for PR reviews with focus on correctness.
```

⚠️ **每个启用的 Skill，description 都会持续占用上下文空间**。把描述写得短而精准，优化前后差距很大。

关键原则：**把最关键的用例前置**，超出 1,536 字符的部分会被截断（2.1.105 起；此前为 250 字符）。

### 🏷️ 9 种 Skill 类型

Anthropic 内部实践总结出了 9 种常见 Skill 类别（来自 Thariq, 2026-3-17）。最好的 Skill 只属于其中一类，跨多类的往往边界模糊、难以维护。

| 类别 | 核心目的 | 典型示例 |
|------|---------|---------|
| **Library & API Reference** | 教 Claude 正确使用某个库/CLI/SDK，内部库或有"Claude 常踩坑"问题的库尤为适合 | `billing-lib`、`internal-cli` |
| **Product Verification** | 验证产品功能是否工作——连接 Playwright、tmux 等工具，提供端到端验证流程 | `signup-flow-driver`、`checkout-verifier` |
| **Data Fetching & Analysis** | 连接数据和监控栈，包含凭证、Dashboard ID、常用查询模式 | `funnel-query`、`grafana` |
| **Business Process** | 把重复性工作流自动化为一条命令，可保存日志帮助模型保持一致性 | `standup-post`、`weekly-recap` |
| **Code Scaffolding** | 为特定框架或模块生成样板代码，尤其适合有自然语言要求的脚手架 | `new-migration`、`create-app` |
| **Code Quality & Review** | 执行代码审查和质量规范，可在 Hooks 或 CI 中自动触发 | `adversarial-review`、`testing-practices` |
| **CI/CD & Deployment** | 拉取、推送、部署代码，通常依赖其他 Skill 采集数据 | `deploy-<service>`、`babysit-pr` |
| **Runbooks** | 接收症状（Slack 告警、错误签名），引导多工具调查，输出结构化报告 | `<service>-debugging`、`oncall-runner` |
| **Infrastructure Operations** | 常规运维和基础设施操作，涉及破坏性操作时需要内置护栏 | `<resource>-orphans`、`cost-investigation` |

⚠️ **一个 Skill 跨多类时是警告信号**——意味着它承担了太多责任，考虑拆分。

### 编写高质量 Skill 的 9 个技巧

以下是 Anthropic 内部从大量 Skills 实践中总结的技巧（来自 Thariq）：

1. **不要写废话** — Claude 已经熟悉通用编码模式，只写它不知道的东西。Skill 的最高价值在于"非常规知识"，不是重复文档

2. **必写 Gotchas 章节** — 把 Claude 踩过的坑汇集到一个 `## Gotchas` 章节。这是整个 Skill 信噪比最高的部分，随着使用不断补充

3. **善用文件系统做渐进式披露** — Skill 是文件夹，不只是一个 markdown 文件。把详细的函数签名放 `references/api.md`，示例放 `examples/`，脚本放 `scripts/`。在 `SKILL.md` 里告诉 Claude 每个文件是什么、何时该读

4. **不要太规定步骤** — 过于手把手的 step-by-step 会导致 Claude 死板执行、丧失适应能力。给出目标和约束，让 Claude 自己决定路径

5. **想清楚 Setup 环节** — 如果 Skill 需要用户提供配置信息，设计一个 `config.json` 文件模式：没有配置时让 Claude 用 `AskUserQuestion` 工具收集配置，然后存到 `config.json`

6. **description 是写给模型看的触发器** — 不是人类读的摘要。写"何时触发这个 Skill"而不是"这个 Skill 做什么"

7. **用 Skill 目录存状态** — 可以在 Skill 目录里存 JSON、文本日志甚至 SQLite。用 `${CLAUDE_PLUGIN_DATA}` 存需要跨升级保留的数据（Skill 目录升级时可能被删除）

8. **给 Claude 预制脚本** — 把常用的操作封装成脚本放进 Skill 目录，让 Claude 组合调用，而不是每次从头生成。Claude 的时间应该花在"决定做什么"上，而不是"重写样板代码"

9. **用按需 Hooks 增强高风险 Skill** — Skill 可以注册只在该 Skill 运行期间生效的 Hooks。例如：`/careful` Skill 激活时阻断 `rm -rf`、`DROP TABLE` 等危险命令；`/freeze` 激活时阻止在特定目录外写文件

### 📊 频率策略：auto-invoke 还是手动触发

根据 Skill 的使用频率决定触发方式，避免浪费上下文：

| 频率 | 策略 | 说明 |
|------|------|------|
| **高频**（>1 次/会话） | 保持 auto-invoke | 优化 description，让它精准触发 |
| **低频**（<1 次/会话） | `disable-model-invocation: true` | 手动触发，description 完全脱离上下文 |
| **极低频**（<1 次/月） | 移除 Skill，改为文档 | 放进 CLAUDE.md 或项目文档中 |

💡 **判断标准**：如果一个 Skill 的 description 大部分时候只是白白占着上下文、偶尔才触发一次，那就设为 `disable-model-invocation`。

### 🧩 保持 SKILL.md 精简

- `SKILL.md` 控制在 500 行以内
- 详细参考文档拆分到辅助文件
- 在 `SKILL.md` 里告诉 Claude 每个辅助文件是什么、什么时候该读

### ⚙️ 合理控制触发权限

| 场景 | 推荐配置 |
|------|---------|
| 知识补充类（API 规范、代码风格） | 默认即可，让 Claude 自动触发 |
| 有副作用的工作流（部署、发消息） | `disable-model-invocation: true` |
| 纯后台知识（旧系统架构说明） | `user-invocable: false` |

### 📁 善用路径限定

如果 Skill 只在特定目录的文件上生效，用 `paths` 字段限制触发范围，避免在不相关上下文中误触发：

```yaml
paths: "src/api/**/*.ts"
```

### 🔧 复杂任务用子代理

对于需要大量探索或独立执行的任务，使用 `context: fork` 让 Skill 在子代理中运行。子代理有自己的上下文，不会污染主对话：

```yaml
---
name: deep-research
description: 深入研究某个主题
context: fork
agent: Explore
---
```

### 🏗️ 项目级 vs 全局级 Skill

| 存放位置 | 路径 | 作用范围 |
|---------|------|---------|
| 企业 | 参见[托管设置](/zh-CN/settings#settings-files) | 组织内所有用户 |
| 个人全局 | `~/.claude/skills/<name>/SKILL.md` | 你所有项目 |
| 项目级 | `.claude/skills/<name>/SKILL.md` | 仅当前项目 |
| 插件 | `<plugin>/skills/<name>/SKILL.md` | 启用插件的地方 |
| 子目录级 | `packages/frontend/.claude/skills/<name>/SKILL.md` | 操作该子目录文件时自动发现（monorepo 场景） |

优先级：企业级 > 个人全局 > 项目级。插件 Skill 使用 `plugin-name:skill-name` 命名空间，因此不会与其他级别冲突。

💡 **实时变更检测**：Claude Code 监视 Skill 目录的文件变更。在 `~/.claude/skills/`、项目 `.claude/skills/` 或 `--add-dir` 目录内的 `.claude/skills/` 中添加、编辑或删除 Skill，会在当前会话中**立即生效**，无需重启。但创建在会话启动时不存在的顶级 Skills 目录需要重启 Claude Code。

### 📂 来自其他目录的 Skills

`--add-dir` 标志主要用于授予文件访问权限，但 Skills 是一个例外：添加目录中的 `.claude/skills/` 会自动加载。其他 `.claude/` 配置（如 subagents、命令和输出样式）**不会**从其他目录加载。

⚠️ 来自 `--add-dir` 目录的 `CLAUDE.md` 文件默认不加载。要加载它们，设置 `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1`。

### 🔑 减少权限提示：`/less-permission-prompts`

权限确认弹窗频繁出现会打断工作节奏。v2.1.111 新增的 `/less-permission-prompts` 内置 Skill 可以自动解决这个问题——它会扫描你的 transcript，找出常见的只读 Bash 和 MCP 工具调用，然后为 `.claude/settings.json` 生成一个优先级排序的允许列表建议。运行一次就能显著减少重复授权。

📝 **小结**：Skill 是 Claude Code 中性价比最高的扩展方式。一个几十行的 `SKILL.md` 文件，就能让 Claude 在你的项目中表现得像一个熟悉代码库、遵循团队规范的「老员工」。从编写项目的代码风格规范开始，逐步扩展到工作流自动化，你会发现 Skill 越写越多、Claude 越来越好用。

## 📦 官方内置 Skills

Anthropic 随 Claude Code 附带了 5 个开箱即用的内置 Skill（无需安装）：

| Skill | 功能说明 | 使用示例 |
|-------|---------|---------|
| `simplify` | 审查改动后的代码，消除重复、改进质量和效率 | 完成功能开发后运行 `/simplify`，自动检查最近改动的代码质量 |
| `batch` | 在多个文件或 Git Worktree 上批量并行执行操作 | 「把所有 `*.controller.ts` 文件中的 `any` 类型替换为具体类型」 |
| `debug` | 系统化调试失败的命令或代码 | 当 `npm test` 失败时，Claude 会自动启动调试流程定位根因 |
| `loop` | 按设定间隔反复执行某个 prompt 或斜杠命令 | `/loop 5m /check-deploy` 每 5 分钟检查部署状态（重复任务 7 天后自动过期） |
| `claude-api` | 使用 Claude API 或 Anthropic SDK 构建应用 | 当代码中检测到 `anthropic` 或 `@anthropic-ai/sdk` 导入时自动触发，提供 API 最佳实践 |

💡 社区和官方维护的可安装 Skill 集合见 [Skills Repository](https://github.com/anthropics/skills)。

## 🔍 故障排除

### Skill 未触发

如果 Claude 在预期时不使用你的 Skill：

1. 检查 `description` 是否包含用户会自然说的关键字
2. 验证 Skill 是否出现在 `What skills are available?` 回答中
3. 尝试重新表述你的请求，使其更接近 `description`
4. 如果 Skill 是用户可调用的，使用 `/skill-name` 直接调用

### Skill 触发过于频繁

如果 Claude 在你不想要时使用了 Skill：

1. 使 `description` 更具体，缩小触发范围
2. 如果你只想手动调用，添加 `disable-model-invocation: true`

### Skill 描述被截断

Skill 描述被加载到上下文中，让 Claude 知道什么可用。如果你有很多 Skill，描述会被缩短以适应字符预算，这可能删除 Claude 需要匹配的关键字。

- 预算按上下文窗口的一定比例动态扩展，回退为 8,000 个字符
- 要提高限制，设置 `SLASH_COMMAND_TOOL_CHAR_BUDGET` 环境变量
- 或在源处修剪 `description` 和 `when_to_use`——前置关键用例，因为组合文本被限制为 1,536 字符

### 调试配置

使用 `/debug-config` 命令（参见「调试你的配置」）诊断为什么 Skill 没有出现或触发。
