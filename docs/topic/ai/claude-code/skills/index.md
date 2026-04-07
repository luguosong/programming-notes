---
title: Skills 技能系统
description: 通过 Skills 为 Claude Code 添加可复用的知识、工作流和参考文档
---

**本文你会学到：**

- 🎯 什么是 Skill，它和 `CLAUDE.md` 有什么区别
- 🔧 参考型 Skill 和动作型 Skill 的不同用法
- 📦 如何从零创建一个自定义 Skill
- ⚙️ Front matter 各配置项的含义和作用
- 🚀 Skill 的自动触发和手动调用机制
- 💡 编写高质量 Skill 的最佳实践

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

```yaml title=".claude/skills/api-conventions/SKILL.md"
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

```yaml title=".claude/skills/deploy/SKILL.md"
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

各字段的含义：

| 字段 | 是否必填 | 说明 |
|------|---------|------|
| `name` | 否 | Skill 的显示名称，也是 `/` 命令的名字。省略时使用目录名。只能用小写字母、数字和连字符（最长 64 字符） |
| `description` | **推荐** | 描述 Skill 的功能和适用场景。Claude 根据它判断是否自动加载。超过 250 字符会被截断 |
| `argument-hint` | 否 | 自动补全时显示的参数提示，如 `[issue-number]` 或 `[filename] [format]` |
| `disable-model-invocation` | 否 | 设为 `true` 禁止 Claude 自动触发，只能手动 `/命令` 调用 |
| `user-invocable` | 否 | 设为 `false` 将 Skill 从 `/` 菜单隐藏（仅 Claude 可自动调用） |
| `allowed-tools` | 否 | Skill 激活时 Claude 可免确认使用的工具列表 |
| `model` | 否 | 指定 Skill 使用的模型 |
| `effort` | 否 | 思考力度：`low` / `medium` / `high` / `max`（仅 Opus 4.6） |
| `context` | 否 | 设为 `fork` 时，Skill 在独立的子代理中运行（v2.1.0 新增） |
| `agent` | 否 | 配合 `context: fork` 使用，指定子代理类型（如 `Explore`、`Plan`）（v2.1.0 新增） |
| `paths` | 否 | Glob 模式，限制 Skill 只在操作匹配的文件时才自动激活（v2.1.84 支持 YAML glob 列表） |
| `shell` | 否 | Shell 命令执行环境：`bash`（默认）或 `powershell` |
| `hooks` | 否 | 绑定到 Skill 生命周期的钩子（v2.1.0 新增） |

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

⚠️ 如果 Skill 描述在列表中被截断了（你有很多 Skill 时会发生），Claude 可能匹配不到。解决办法：把 description 的**关键用例前置**，或者调大 `SLASH_COMMAND_TOOL_CHAR_BUDGET` 环境变量。v2.1.32 起，Skill 描述预算自动按上下文窗口的 2% 缩放，大窗口用户能看到更多 Skill。

### 手动调用（/命令）

所有 `user-invocable` 为 `true`（默认）的 Skill 都可以通过 `/` 命令手动调用。在 Claude Code 中输入 `/` 即可看到所有可用的 Skill 和内置命令列表。

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

此外还有两个有用的内置变量：

- `${CLAUDE_SESSION_ID}` — 当前会话 ID
- `${CLAUDE_SKILL_DIR}` — Skill 所在目录的绝对路径（v2.1.69 新增）

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

## 🔧 自定义命令（Commands）

Claude Code 还有一个更早的机制叫 **Commands**，存放在 `.claude/commands/` 目录下。好消息是——**Commands 已经合并到 Skills 体系中**。

两者等价关系：

| Commands 写法 | Skills 写法 | 效果相同 |
|--------------|------------|---------|
| `.claude/commands/deploy.md` | `.claude/skills/deploy/SKILL.md` | 都会创建 `/deploy` 命令 |

✅ 如果你已经有 `.claude/commands/` 目录下的文件，它们**继续有效**，无需迁移。但新建 Skill 时推荐使用 `.claude/skills/` 目录，因为 Skills 支持更多功能（辅助文件、Front matter 配置等）。

⚠️ 如果同名 Skill 和 Command 都存在，**Skill 优先**。

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

⚠️ **每个启用的 Skill，description 都在偷你的上下文空间**。把描述写得短而精准，优化前后差距很大。

关键原则：**把最关键的用例放在前 250 字符内**，因为超出部分会被截断。

### 🏷️ 三种典型 Skill 类型

在实际使用中，Skill 通常可以分为三种类型，每种有不同的设计要点。

#### 类型一：检查清单型（质量门禁）

发布前或提交前跑一遍，确保不漏项：

```yaml title=".claude/skills/release-check/SKILL.md"
---
name: release-check
description: Use before cutting a release to verify build, version, and smoke test.
---

## Pre-flight（All must pass）

- [ ] `cargo build --release` passes
- [ ] `cargo clippy -- -D warnings` clean
- [ ] Version bumped in Cargo.toml
- [ ] CHANGELOG updated
- [ ] `kaku doctor` passes on clean env

## Output

Pass / Fail per item. Any Fail must be fixed before release.
```

#### 类型二：工作流型（标准化操作）

高风险操作显式调用 + 内置回滚步骤：

```yaml title=".claude/skills/config-migration/SKILL.md"
---
name: config-migration
description: Migrate config schema. Run only when explicitly requested.
disable-model-invocation: true
---

## Steps

1. Backup: `cp ~/.config/app/config.toml ~/.config/app/config.toml.bak`
2. Dry run: `app config migrate --dry-run`
3. Apply: remove `--dry-run` after confirming output
4. Verify: `app doctor` all pass

## Rollback

`cp ~/.config/app/config.toml.bak ~/.config/app/config.toml`
```

#### 类型三：领域专家型（封装决策框架）

运行时出问题时让 Claude 按固定路径收集证据，不要瞎猜：

```yaml title=".claude/skills/runtime-diagnosis/SKILL.md"
---
name: runtime-diagnosis
description: Use when app crashes, hangs, or behaves unexpectedly at runtime.
---

## Evidence Collection

1. Run `app doctor` and capture full output
2. Last 50 lines of `~/.local/share/app/logs/`
3. Plugin state: `app --list-plugins`

## Decision Matrix

| Symptom | First Check |
|---------|-------------|
| Crash on startup | doctor output → config syntax error |
| Rendering glitch | GPU backend / terminal capability |
| Config not applied | Config path + schema version |

## Output Format

Root cause / Blast radius / Fix steps / Verification command
```

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
| 个人全局 | `~/.claude/skills/<name>/SKILL.md` | 你所有项目 |
| 项目级 | `.claude/skills/<name>/SKILL.md` | 仅当前项目 |
| 子目录级 | `packages/frontend/.claude/skills/<name>/SKILL.md` | 操作该子目录文件时自动发现（monorepo 场景） |

优先级：企业级 > 个人全局 > 项目级 > 子目录级。

📝 **小结**：Skill 是 Claude Code 中性价比最高的扩展方式。一个几十行的 `SKILL.md` 文件，就能让 Claude 在你的项目中表现得像一个熟悉代码库、遵循团队规范的「老员工」。从编写项目的代码风格规范开始，逐步扩展到工作流自动化，你会发现 Skill 越写越多、Claude 越来越好用。
