---
title: 扩展推荐
---

# 扩展推荐

## 内置 Skill 列表

| Skill                       | 用途                                        |
|-----------------------------|---------------------------------------------|
| `/deep-research`            | 深度研究某个主题，多源搜索 + 交叉验证       |
| `/code-review`              | 代码审查，可指定深度 low / medium / high    |
| `/security-review`          | 安全审查                                    |
| `/verify`                   | 验证代码改动是否生效                        |
| `/simplify`                 | 简化、优化已改动的代码（质量向，不查 bug）  |
| `/run`                      | 启动并运行项目应用                          |
| `/loop`                     | 定时循环执行某个任务，如 `/loop 5m /verify` |
| `/claude-api`               | 查询 Claude API 文档（模型、定价等）        |
| `/init`                     | 初始化项目 `CLAUDE.md` 文件                 |
| `/review`                   | 审查 Pull Request                           |
| `/update-config`            | 修改 Claude Code 配置（权限、环境变量等）   |
| `/fewer-permission-prompts` | 减少权限弹窗                                |
| `/keybindings-help`         | 自定义键盘快捷键                            |

---

## 推荐插件

通过 `/plugin install` 从 marketplace 安装的第三方插件。与 Copilot CLI 插件几乎都是 Skill 不同，Claude Code 插件形态多样——可能是
Skill、Agent、Command、Hooks、MCP、LSP 或 Output Style 之一，因此每个插件用「类型」标注其提供的能力。

以下插件来自三个 marketplace：

- `claude-plugins-official`（Anthropic 官方）
- `zai-coding-plugins`（智谱 AI）
- `claude-hud`（社区第三方）

### superpowers —— 核心工作流 Skill 库

> 来源：`claude-plugins-official` · 类型：Skill · 版本 `5.1.0`

把 TDD、系统化调试、计划评审等经过验证的工程实践封装成 Skill，在对应场景自动触发，强制执行方法论纪律（如「声称完成前必须验证」「写实现前先写测试」）。

```bash
/plugin install superpowers@claude-plugins-official
```

#### 会话入口

| Skill               | 功能说明                                  |
|---------------------|-------------------------------------------|
| `using-superpowers` | 会话入口：建立如何发现与使用 Skill 的规则 |

#### 构思与规划

| Skill           | 功能说明                                    |
|-----------------|---------------------------------------------|
| `brainstorming` | 任何创意工作前，先探索意图、需求与设计      |
| `writing-plans` | 有规格/需求时，先把多步骤任务写成可执行计划 |

#### 计划执行

| Skill                         | 功能说明                                                 |
|-------------------------------|----------------------------------------------------------|
| `executing-plans`             | 在独立会话执行已写好的计划，带审查检查点                 |
| `subagent-driven-development` | 当前会话执行含独立任务的计划                             |
| `dispatching-parallel-agents` | 面临 2+ 个无共享状态的独立任务时并行派发                 |
| `using-git-worktrees`         | 用 git worktree 隔离工作区，配合并行执行避免污染当前分支 |

#### 编码实践

| Skill                            | 功能说明                                             |
|----------------------------------|------------------------------------------------------|
| `test-driven-development`        | 实现任何功能/bugfix 前用 TDD（先写测试再写实现）     |
| `systematic-debugging`           | 遇到 bug、测试失败、异常行为时，系统性排查根因再修复 |
| `verification-before-completion` | 声称「完成/通过」前，必须运行验证命令并确认输出      |

#### 代码审查

| Skill                    | 功能说明                                 |
|--------------------------|------------------------------------------|
| `requesting-code-review` | 完成任务、实现大功能或合并前请求代码审查 |
| `receiving-code-review`  | 收到审查反馈时，先技术验证再决定是否采纳 |

#### 分支收尾

| Skill                            | 功能说明                                      |
|----------------------------------|-----------------------------------------------|
| `finishing-a-development-branch` | 开发完成后的分支收尾：合并、PR 还是清理的决策 |

#### Skill 创作

| Skill            | 功能说明                    |
|------------------|-----------------------------|
| `writing-skills` | 创建、编辑、验证 Skill 本身 |

---

### context7 —— 实时文档查询

> 来源：`claude-plugins-official` · 类型：MCP

Upstash Context7 MCP，从源仓库实时拉取版本相关的文档与代码示例注入上下文，避免 LLM 训练数据过时导致的 API 用法错误。

```bash
/plugin install context7@claude-plugins-official
```

| 工具                 | 功能说明                                            |
|----------------------|-----------------------------------------------------|
| `resolve-library-id` | 将库名解析为 Context7 库 ID（如 `/vercel/next.js`） |
| `query-docs`         | 按问题拉取该库的文档片段与代码示例                  |

---

### code-simplifier —— 代码精简

> 来源：`claude-plugins-official` · 类型：Agent

简化并精炼代码——提升清晰度、一致性、可维护性，同时完整保留功能。默认聚焦最近改动的代码。

```bash
/plugin install code-simplifier@claude-plugins-official
```

| Agent             | 功能说明                            |
|-------------------|-------------------------------------|
| `code-simplifier` | 作为子 agent 被派发执行代码简化任务 |

---

### code-review —— 自动代码审查

> 来源：`claude-plugins-official` · 类型：Command

多个专门 agent 协作 + 置信度评分的自动化代码审查，可按深度（`low`/`medium`/`high`）调节覆盖面。

```bash
/plugin install code-review@claude-plugins-official
```

| 命令           | 功能说明                                                              |
|----------------|-----------------------------------------------------------------------|
| `/code-review` | 审查当前 diff；`--comment` 发为 PR 行内评论，`--fix` 直接应用修复建议 |

---

### frontend-design —— 前端设计

> 来源：`claude-plugins-official` · 类型：Skill

生成独特、生产级、刻意规避通用 AI 审美的前端界面。在用户要求构建 Web 组件、页面或应用时触发。

```bash
/plugin install frontend-design@claude-plugins-official
```

| Skill             | 功能说明                                 |
|-------------------|------------------------------------------|
| `frontend-design` | 生成有设计质感的前端代码，避免模板化产出 |

---

### skill-creator —— Skill 创建与评测

> 来源：`claude-plugins-official` · 类型：Skill

创建新 Skill、改进已有 Skill、衡量 Skill 性能的完整工具链，含 eval、基准测试、方差分析与触发准确率优化。

```bash
/plugin install skill-creator@claude-plugins-official
```

| Skill           | 功能说明                                                           |
|-----------------|--------------------------------------------------------------------|
| `skill-creator` | 从零创建 / 优化 / 跑 eval / 基准测试 / 优化 description 触发准确率 |

---

### claude-md-management —— CLAUDE.md 维护

> 来源：`claude-plugins-official` · 类型：Command + Skill

维护和改进 `CLAUDE.md`——审计质量、捕获会话学习、保持项目记忆不过时。

```bash
/plugin install claude-md-management@claude-plugins-official
```

| Skill / Command      | 功能说明                                                |
|----------------------|---------------------------------------------------------|
| `/revise-claude-md`  | 用本会话的学习更新 `CLAUDE.md`                          |
| `claude-md-improver` | 扫描所有 `CLAUDE.md`，按模板评估质量并定向更新（Skill） |

---

### learning-output-style —— 学习输出风格

> 来源：`claude-plugins-official` · 类型：Output Style（Hooks）

交互式学习模式：不全程代写，而是在关键决策点请求 5-10 行有意义的代码贡献（业务逻辑、错误处理策略等），让用户在真正重要的取舍上参与。模仿未正式发布的
Learning 输出风格。

```bash
/plugin install learning-output-style@claude-plugins-official
```

> 该插件通过 hooks 注入输出风格，无 Skill / Command 表格。通过 `/output-style` 切换启用。

---

### security-guidance —— 代码安全审查

> 来源：`claude-plugins-official` · 类型：Hooks

对 Claude 生成代码的多层安全审查：编辑时基于模式告警、Stop 时 LLM 驱动的 diff 审查、提交时 agent 审查，覆盖注入、XSS、SSRF、硬编码密钥等
25+ 类漏洞。

```bash
/plugin install security-guidance@claude-plugins-official
```

> 该插件通过 `PreToolUse` / `Stop` hooks 自动生效，无需手动调用，无 Skill / Command 表格。

---

### jdtls-lsp —— Java 语言服务器

> 来源：`claude-plugins-official` · 类型：LSP

基于 Eclipse JDT.LS 的 Java 语言服务器，经 `LSP` 工具提供 `goToDefinition`、`findReferences`、`hover`、`incomingCalls` 等
Java 代码智能。

```bash
/plugin install jdtls-lsp@claude-plugins-official
```

> 该插件提供语言服务器后端，由 LSP 工具按需调用，无 Skill / Command 表格。

---

### typescript-lsp —— TypeScript 语言服务器

> 来源：`claude-plugins-official` · 类型：LSP

TypeScript 语言服务器，经 `LSP` 工具提供 TS / JS 的定义跳转、引用查找、悬停信息等代码智能。

```bash
/plugin install typescript-lsp@claude-plugins-official
```

> 该插件提供语言服务器后端，由 LSP 工具按需调用，无 Skill / Command 表格。

---

### glm-plan-usage —— GLM 用量查询

> 来源：`zai-coding-plugins` · 类型：Command + Agent

查询 GLM Coding Plan 服务的配额与用量统计（智谱 AI 编程套餐）。

```bash
/plugin install glm-plan-usage@zai-coding-plugins
```

| Command / Agent     | 功能说明                   |
|---------------------|----------------------------|
| `/usage-query`      | 查询当前账户的用量信息     |
| `usage-query-agent` | 执行用量查询脚本的子 agent |

---

### claude-hud —— 实时状态栏

> 来源：`claude-hud`（jarrodwatts/claude-hud）· 类型：Command（statusLine）

Claude Code 实时状态栏 HUD：上下文健康度、工具活动、agent 追踪、todo 进度一目了然。

```bash
/plugin install claude-hud@claude-hud
```

| Command      | 功能说明                                                    |
|--------------|-------------------------------------------------------------|
| `/setup`     | 将 claude-hud 配置为状态栏                                  |
| `/configure` | 配置 HUD 显示项（布局、语言、预设、显示元素），保留手动覆盖 |

---

## MCP 服务器

除插件内置的 `context7` 外，以下 MCP 可独立配置或由 coding-helper 包装层注入使用：

| MCP              | 来源                                | 主要能力                                                               |
|------------------|-------------------------------------|------------------------------------------------------------------------|
| `zai-mcp-server` | 独立配置（智谱 `@z_ai/mcp-server`） | 图像/视频分析、截图 OCR、错误诊断、UI 转代码、技术图与数据可视化解读等 |
| `context7`       | `context7` 插件内置                 | 库 ID 解析 + 版本相关文档查询（见上文）                                |
| `4_5v_mcp`       | coding-helper 包装层注入            | 远程图像分析（`analyze_image`）                                        |
| `web_reader`     | coding-helper 包装层注入            | 抓取 URL 转为模型友好的 Markdown（`webReader`）                        |

> `4_5v_mcp` 与 `web_reader` 不通过 `mcpServers` 配置注册，由智谱 coding-helper 包装层在启动时自动注入。

---

## 自定义 Skill（非插件）

直接放在 skills 目录、不经 marketplace 分发的 Skill。

### gstack —— 用户级工具集

> 位置：`~/.claude/skills/` · 非插件，独立安装

GStack 是一套覆盖浏览器自动化、规划评审、发布部署、文档流程、安全纪律、iOS 开发等场景的大型工具集，通过 `/skill-name`
调用。以下按工作流顺序归类：

#### 📋 规划与咨询

| Skill          | 功能说明                   |
|----------------|----------------------------|
| `office-hours` | 开放式技术咨询与讨论       |
| `spec`         | 编写功能规格文档           |
| `autoplan`     | 自动生成多步骤任务执行计划 |

#### 🔍 计划评审

| Skill                | 功能说明                               |
|----------------------|----------------------------------------|
| `plan-ceo-review`    | 以 CEO 视角评审计划（商业价值、风险）  |
| `plan-eng-review`    | 以工程视角评审计划（技术可行性、架构） |
| `plan-design-review` | 以设计视角评审计划（用户体验、一致性） |
| `plan-devex-review`  | 以开发者体验视角评审计划               |

#### 🎨 设计

| Skill                 | 功能说明                         |
|-----------------------|----------------------------------|
| `design-consultation` | 设计方向咨询，探讨交互与视觉策略 |
| `design-shotgun`      | 快速生成多个差异化设计方案       |
| `design-html`         | 生成可预览的 HTML 原型           |
| `design-review`       | 对已有设计方案进行专业评审       |
| `devex-review`        | 以开发者体验视角评审设计与工具链 |

#### 🌐 浏览器与抓取

| Skill                   | 功能说明                               |
|-------------------------|----------------------------------------|
| `browse`                | 控制 Chrome 浏览器访问网页、截图与交互 |
| `scrape`                | 抓取网页内容并返回结构化数据           |
| `connect-chrome`        | 连接已打开的 Chrome 实例进行自动化     |
| `setup-browser-cookies` | 配置浏览器 cookies，用于登录态测试     |

#### ✅ QA 测试

| Skill        | 功能说明                           |
|--------------|------------------------------------|
| `qa`         | 执行完整 QA 测试流程（含代码改动） |
| `qa-only`    | 仅执行 QA，不修改任何代码          |
| `pair-agent` | 派发浏览器 QA 子 agent 并行执行    |

#### 🔒 开发纪律与安全

| Skill         | 功能说明                         |
|---------------|----------------------------------|
| `careful`     | 谨慎模式：每步操作前请求用户确认 |
| `guard`       | 防护模式：阻止破坏性或不可逆操作 |
| `freeze`      | 冻结指定文件，防止意外修改       |
| `unfreeze`    | 解除文件冻结状态                 |
| `investigate` | 系统性排查问题根因               |
| `health`      | 检查项目整体健康状况             |
| `cso`         | 以首席安全官视角审查代码与架构   |

#### 🚀 发布与部署

| Skill              | 功能说明                             |
|--------------------|--------------------------------------|
| `benchmark`        | 执行性能基准测试并输出报告           |
| `benchmark-models` | 对比多个 AI 模型在任务上的性能       |
| `ship`             | 完整发布流程：代码审查 + 测试 + 部署 |
| `canary`           | 金丝雀发布（小流量灰度验证）         |
| `land-and-deploy`  | 合并 PR 并触发部署                   |
| `setup-deploy`     | 初始化配置部署流程                   |
| `landing-report`   | 生成发布完成报告                     |

#### 📄 文档与复盘

| Skill               | 功能说明                 |
|---------------------|--------------------------|
| `document-generate` | 从代码或需求生成技术文档 |
| `document-release`  | 生成版本发布说明         |
| `make-pdf`          | 将文档转换为 PDF 格式    |
| `retro`             | 复盘总结本次开发周期     |
| `learn`             | 从代码库提取并沉淀知识   |

#### 🔧 工具与知识管理

| Skill             | 功能说明                       |
|-------------------|--------------------------------|
| `context-save`    | 保存当前会话上下文快照         |
| `context-restore` | 恢复之前保存的上下文           |
| `sync-gbrain`     | 将知识同步到 GBrain 知识库     |
| `setup-gbrain`    | 初始化配置 GBrain 集成         |
| `codex`           | 通过 OpenAI Codex 执行代码任务 |
| `gstack-upgrade`  | 升级 gstack 工具集到最新版本   |
| `skillify`        | 将当前工作流提炼为可复用 Skill |

#### 📱 iOS 专项

| Skill               | 功能说明                            |
|---------------------|-------------------------------------|
| `ios-qa`            | iOS 应用端到端 QA 测试              |
| `ios-fix`           | 修复 iOS 特定问题（布局、兼容性等） |
| `ios-design-review` | iOS 界面与交互设计评审              |
| `ios-clean`         | 清理 iOS 构建缓存与临时文件         |
| `ios-sync`          | 同步 iOS 代码与相关配置             |

---

### doc-quality-review —— 项目级文档审查

> 位置：`.claude/skills/`（本项目）· 非插件

将粗略学习笔记转化为结构清晰的文档，覆盖 7 个维度：技术准确性、内容清晰度、TOC 结构、学习目标完整性、代码引用有效性、格式与元数据合规、代码增强。

| Skill                | 触发词                                                                       |
|----------------------|------------------------------------------------------------------------------|
| `doc-quality-review` | `审查文档`、`review doc`、`质量检查`、`doc audit`、`文档审查`、`检查笔记` 等 |
