---
title: 上下文工程
description: 理解 Claude Code 的上下文成本构成、分层策略、噪声治理和 Prompt Caching 架构
---

**本文你会学到**：

- 🎯 Claude Code 上下文的真实成本构成——你的可用空间比想象中少
- 🔄 每轮结束后的 5 个决策选项——Continue / Rewind / Compact / Clear / Subagent
- 🔙 Rewind vs Correcting——管理上下文质量的关键习惯
- 🗜️ 压缩机制的陷阱与 Compact Instructions——控制压缩时保留什么
- 📐 上下文分层策略——什么该常驻、什么该按需、什么该隔离
- 🔇 Tool Output 噪声——另一个容易被忽视的上下文杀手
- 📋 HANDOFF.md 模式——跨会话传递进度的工程化方案
- ⚡ Prompt Caching 架构——Claude Code 成本优化的底层秘密

## 🧠 上下文不是容量问题，是噪声问题

很多人第一次遇到上下文窗口满的情况，本能反应是"窗口不够大"。但用久了你会发现，真正卡住的地方通常不是上下文不够长，而是**太吵了**——有用的信息被大量无关内容淹没。

打个比方：上下文窗口就像你的办公桌。问题往往不是桌子太小，而是上面堆满了你根本不需要的东西——过期的报纸、上个月的会议记录、看了一半就没碰过的文件——它们占着位置，让你找不到真正需要的资料。

上下文工程的核心就是：**让每一寸空间都放有价值的东西**。

## 📊 上下文成本构成

### 上下文窗口大小

Claude Code 目前支持最高 **100 万 tokens（1M）** 的上下文窗口（截至写作时，Opus 4.7、Sonnet 4.5 等新模型默认开启 1M；此数字随模型更新可能变化，以[官方文档](https://docs.anthropic.com/en/docs/about-claude/models)为准），但这并非全部可用。在你的对话开始之前，大量空间就已经被占用了：

```
1M 总上下文
├── 固定开销（~15-20K）    ← 每次会话都一样
│   ├── 系统指令：~2K
│   ├── 所有启用的 Skill 描述符：~1-5K
│   ├── MCP Server 工具定义：~10-20K  ← 最大隐形杀手
│   └── LSP 状态：~2-5K
│
├── 半固定（~5-10K）        ← 项目不同而不同
│   ├── CLAUDE.md：~2-5K
│   └── Memory：~1-2K
│
└── 动态可用（~970K+）      ← 你和 Claude 真正"呼吸"的空间
    ├── 对话历史
    ├── 文件内容
    └── 工具调用结果
```

⚠️ **MCP 工具定义是最大的隐形杀手**。一个典型 MCP Server（如 GitHub）包含 20-30 个工具定义，每个约 200 tokens，合计 4,000-6,000 tokens。接 5 个 Server，光这部分固定开销就到了 25,000 tokens。在需要读取大量代码的场景下，这部分开销非常关键。

💡 用 `/context` 命令可以实时查看当前上下文的占用结构，帮你定位哪些部分吃掉了太多空间。

### 上下文腐化（Context Rot）

拥有 1M 上下文并不意味着可以无限放东西进去。**上下文腐化（Context Rot）**是指：随着上下文增长，模型性能会逐渐下降——注意力分散到更多 tokens 上，陈旧无关的内容开始干扰当前任务。

对于 1M 上下文模型，上下文腐化通常在 **约 300-400K tokens** 时开始显现，但具体表现高度依赖任务性质，不是一个硬性规则。

这意味着："上下文没满"不等于"可以继续往里塞"。**主动管理上下文质量**比被动等待窗口满更重要。

## 📐 上下文分层策略

了解了成本构成后，关键问题是：**哪些东西应该待在上下文里，哪些不该？**

推荐的分层方式是按照加载频率和必要性来划分：

| 层级 | 加载方式 | 放什么 | 示例 |
|------|---------|--------|------|
| **始终常驻** | 每次自动加载 | 项目契约、构建命令、禁止事项 | `CLAUDE.md`（支持通过 `--add-dir` 加载额外目录的 CLAUDE.md，v2.1.20 新增） |
| **按路径加载** | 匹配到对应文件时加载 | 语言/目录/文件类型特定规则 | `.claude/rules/`（v2.0.64 新增，v2.1.84 支持 `paths:` YAML glob 列表） |
| **按需加载** | 触发相关场景时加载 | 工作流、领域知识 | `Skills` |
| **隔离加载** | 独立上下文，只返回摘要 | 大量探索、并行研究 | `Subagents` |
| **不进上下文** | 确定性脚本，不占用空间 | 审计、阻断、校验 | `Hooks` |

说白了：**偶尔用的东西就不要每次都加载进来**。

### 各扩展机制的上下文成本对比

| 机制 | 上下文占用 | 说明 |
|------|-----------|------|
| `CLAUDE.md` | 每次加载，2-5K tokens | 越短越好，只放最关键的 |
| `.claude/rules/` | 匹配到路径时加载 | 按需加载，比全放 CLAUDE.md 省空间（v2.0.64 新增，v2.1.84 支持 YAML glob 路径匹配） |
| `Skills`（auto-invoke） | description 常驻，完整内容按需加载 | description 预算为上下文窗口的 2%（v2.1.32 改进） |
| `Skills`（disabled） | 零占用 | 只有你调用时才加载，最适合低频 Skill |
| `Hooks` | 零成本（除非返回内容） | 正常放行时不输出内容 = 不占空间 |
| `MCP`（defer_loading） | 工具名常驻，完整定义按需加载 | Claude 需要时才加载工具 schema |
| `Subagents` | 主上下文中只有摘要 | 探索过程不污染主对话 |

## 🔇 Tool Output 噪声：另一个隐形杀手

前面算的是固定开销，但动态部分同样有个坑容易被忽视：**Tool Output**。

`cargo test` 一次完整输出动辄几千行，`git log`、`find`、`grep` 在稍大的仓库里也能轻松塞满屏幕。这些输出 Claude 并不需要全看——它真正需要知道的就是「过了还是挂了，挂在哪里」。但只要输出出现在上下文里，就是实实在在的 token 消耗。

v2.1.108 通过按需加载语言语法，进一步降低了文件读取、编辑和语法高亮操作的内存占用——这对处理大型仓库的长会话尤为明显。

### 解决方案

**手动截断**：在命令后面加 `| head -30` 限制输出行数。

```bash
# ❌ 完整输出，可能几千行
cargo test

# ✅ 只看前 30 行，够判断通过/失败
# 注意：head 命令仅用于文档示例，实际使用时可用 RTK 自动截断
cargo test 2>&1 | head -30
```

**系统化过滤**：使用 [RTK（Rust Token Killer）](https://github.com/nicepkg/rtk) 在命令输出到 Claude 之前自动过滤，只留决策需要的核心信息。它通过 Hook 透明重写命令，对 Claude Code 完全无感。

## 🔄 每轮结束后的 5 个选择

每次 Claude 完成一轮回复后，你都处于一个决策节点。这 5 个选项不是等价的——选对了，后续工作顺畅；选错了，你可能在一个正在腐化的上下文里越陷越深。

| 选项 | 何时使用 | 操作 |
|------|---------|------|
| **Continue（继续）** | 回复质量高，任务推进顺利 | 直接输入下一个问题或指令 |
| **Rewind（回退）** | 回复跑偏或触发了错误的工具调用链 | 按两次 `Esc` 返回到上一节点，重新输入 |
| **Compact（压缩）** | 同一任务中途、历史对话量大但要继续工作 | 输入 `/compact` |
| **Clear（清空）**  | 准备开始全新任务，或上下文已经"脏了" | 输入 `/clear`，自己写一段摘要交给新会话 |
| **Subagent（子代理）** | 下一步需要大量探索性操作，不想污染主上下文 | 把探索任务发给子代理 |

### Rewind vs Correcting：回退而非纠错

这是最容易被忽视的习惯差异。

当 Claude 给出一个跑偏的回答，很多人会直接在同一上下文里追加"不对，你应该……"来纠错。问题是：**那个错误的回答已经写进上下文了**——包括它触发的错误工具调用、失败的尝试、误导性的推理链。在这个受污染的上下文上继续，等于带着一堆垃圾在开车。

**Rewind（双击 `Esc`）** 会把对话回退到这次回复之前的状态，就像那次错误回答从未发生过。回退后换一个更清晰的提示重新问，往往能直接得到正确答案——而不是在错误路径上来回挣扎。

**什么时候用 Rewind，什么时候接受并继续？**

- 回复方向完全跑偏 → Rewind
- 触发了一长串错误工具调用 → Rewind
- 小小的措辞问题或你可以直接利用的内容 → 继续（追加说明）
- 已经有副作用（文件被改了、命令运行了）→ 需要先处理副作用，再 Rewind

### Compact vs Clear：什么时候彻底清空

`/compact` 和 `/clear` 都能降低上下文负担，但适用场景不同：

| 场景 | 推荐操作 | 理由 |
|------|---------|------|
| 同一复杂任务的中途休整 | `/compact` | 保留工作记忆，避免重新描述背景 |
| 当前任务完成，开始全新任务 | `/clear` + 自己写摘要 | 干净的上下文，不带历史包袱 |
| 高风险下一步（如大规模重构）开始前 | `/clear` + 自己写摘要 | 摘要由你控制，质量优于算法压缩 |
| 上下文已经"腐化"，模型开始乱做事 | `/clear` | 纠错成本已经高于重新开始 |

⚠️ **坏的 Compact**：如果上下文里已经有很多错误尝试、废弃的推理路径，`/compact` 可能会把这些垃圾浓缩进去——你得到一个"高效传播误导信息"的摘要。这种情况下 `/clear` 加上自己写的精准摘要效果更好。

## 🗜️ 压缩机制的陷阱

当上下文接近上限时，Claude Code 会自动触发压缩（Compaction）。v2.0.64 起 auto-compacting 变为即时完成，不再阻塞交互。默认压缩算法按"可重新读取"判断——早期的 Tool Output 和文件内容会被优先删掉，但**顺带把架构决策和约束理由也一起扔了**。

两小时后再改代码，Claude 可能根本不记得两小时前定了什么。莫名其妙的 Bug 就是这么来的。

### Compact Instructions：告诉压缩算法该保留什么

解决方案是在 `CLAUDE.md` 里写明压缩时必须保留什么：

```markdown
## Compact Instructions

When compressing, preserve in priority order:

1. Architecture decisions (NEVER summarize)
2. Modified files and their key changes
3. Current verification status (pass/fail)
4. Open TODOs and rollback notes
5. Tool outputs (can delete, keep pass/fail only)
```

这样压缩后，关键决策不会丢失，Claude 能继续基于之前的上下文工作。

### 长会话管理的最佳节奏

| 情况 | 操作 | 说明 |
|------|------|------|
| 同一任务，进入新阶段 | `/compact` | 压缩但保留重点（v2.1.32 新增"Summarize from here"支持局部压缩） |
| 被纠偏两次以上 | `/clear` | 开新会话，别反复调 prompt |
| 长时间工作，需要换人接手 | 先写 HANDOFF.md，再开新会话 | 见下方 HANDOFF.md 模式 |

💡 **自动记忆**（v2.1.32 新增）：Claude 现在会在工作过程中自动记录和回忆有价值的上下文，v2.1.59 进一步强化为 auto-memory 自动保存。你可以用 `/memory` 管理这些记忆。

### Session recap：不在场时发生了什么

当你离开一段时间再回到会话，Claude 可能已经处理了大量内容。**Session recap**（v2.1.108 新增）就是为此设计的——返回 session 时提供上下文摘要，让你快速了解"我不在的时候发生了什么"。

- 可在 `/config` 中配置是否启用
- 手动调用 `/recap` 查看摘要
- 对禁用遥测的用户（Bedrock、Vertex、Foundry、`DISABLE_TELEMETRY`）同样可用（v2.1.110 改进）

### 恢复会话（`/resume`）

`/resume` 用于恢复之前的会话。选择器默认显示当前目录的 session，按 `Ctrl+A` 切换为显示所有项目（v2.1.108 改进）。此外，`--resume` / `--continue` 现在还会恢复未过期的计划任务（v2.1.110 新增），让你继续上次中断的定时任务。

v2.1.122 进一步增强了 `/resume` 的搜索能力：在搜索框中粘贴 PR URL 现在可以找到创建该 PR 的会话（支持 GitHub、GitHub Enterprise、GitLab 和 Bitbucket）。v2.1.119 则扩展了 `--continue`/`--resume` 的匹配范围，现在也能找到通过 `/add-dir` 添加当前目录的会话。

!!! tip "长上下文会话压缩修复"

    从 v2.1.113 起，对已恢复的长上下文会话执行 compact 不再失败（之前会报 "Extra usage is required for long context requests"）——长会话现在可以放心地恢复后再压缩。

## 📋 HANDOFF.md：跨会话传递进度

Compact Instructions 能缓解压缩丢失信息的问题，但还有一种更主动的方案：**在开新会话前，让 Claude 写一份 HANDOFF.md**。

HANDOFF.md 把当前的进度、尝试过什么、哪些走通了、哪些是死路、下一步该做什么写清楚。下一个 Claude 实例只读这个文件就能接着做，不依赖压缩算法的摘要质量。

你可以这样指示 Claude：

> 在 HANDOFF.md 里写清楚现在的进展。解释你试了什么、什么有效、什么没用，让下一个拿到新鲜上下文的 agent 只看这个文件就能继续完成任务。

写完后快速扫一眼，有缺漏直接让它补，然后开新会话，把 HANDOFF.md 的路径发给新会话即可。

### HANDOFF.md 内容示例

```markdown title="HANDOFF.md（示例）"
# 会话交接文档

## 当前任务
将认证模块从 Session 迁移到 JWT。

## 已完成
- [x] 添加 `spring-boot-starter-security` 和 `jjwt` 依赖
- [x] 实现 `JwtAuthenticationFilter`（位于 `src/main/java/com/example/security/`）
- [x] 配置 `SecurityFilterChain`，禁用 CSRF，启用无状态会话

## 已尝试但失败的方案
- ❌ 在 `application.yml` 中配置 `server.servlet.session.timeout: 0` — 无法完全禁用 Session
- ❌ 直接修改原有 `AuthController` — 与前端登录页耦合太紧，改动范围过大

## 当前卡点
`JwtAuthenticationFilter` 能正确解析 token，但 `/api/auth/refresh` 端点返回 401。
原因是刷新端点本身也需要经过 JWT filter，但刷新 token 尚未实现。

## 下一步
1. 在 `AuthController` 中添加 `/api/auth/refresh` 端点，从数据库验证刷新 token
2. 在 `SecurityFilterChain` 中将 `/api/auth/refresh` 加入白名单
3. 编写刷新 token 的集成测试

## 关键文件
- `src/main/java/com/example/security/JwtAuthenticationFilter.java`
- `src/main/java/com/example/config/SecurityConfig.java`
- `src/main/java/com/example/controller/AuthController.java`
```

## ⚙️ Plan Mode 的工程价值

> Plan Mode 是 Claude Code 的「安全沙盒」——它可以在不修改任何文件的前提下，帮你分析代码、评估影响、规划方案。按 `Shift+Tab` 切换。

Plan Mode 的核心价值是把**探索和执行拆开**：

- **探索阶段**以只读操作为主——Claude 先澄清目标和边界，再提交具体方案
- **执行成本**在方案确认之后才发生
- 对于复杂重构、迁移、跨模块改动，这比"急着出代码"有用得多

### Plan Mode 如何节省上下文

Plan Mode 的上下文效益体现在两个方面：

1. **零副作用探索**：Plan Mode 下所有文件操作都是虚拟的——不会真正写入磁盘，也不会触发构建、测试等工具链。即使探索了 50 个文件，退出 Plan Mode 后这些中间过程不会留在上下文里（只保留最终方案）
2. **提前验证方案可行性**：在投入大量 token 执行之前，先用低成本的只读探索确认方向正确。一次 Plan 花几百 token，避免了执行到一半发现方向错误后 Rewind 浪费的数千 token

💡 **进阶玩法**：开一个 Claude 写计划，再开另一个以"高级工程师"身份审这个计划，让 AI 审 AI。

⚠️ **直觉上的误解**：Plan Mode 似乎应该切换成只读工具集，但这会破坏 Prompt Caching（见下方）。实际实现中，`EnterPlanMode` 是模型可以自己调用的工具，工具集不变，缓存不受影响。

## ⚡ Prompt Caching 架构

### 为什么缓存如此重要

工程界有句话 "Cache Rules Everything Around Me"，对 Claude Code 同样如此。整个架构都是围绕 Prompt 缓存构建的——**高命中率不光省钱，速率限制也会松很多**。

### 缓存工作原理：前缀匹配

Prompt 缓存按**前缀匹配**工作：从请求开头到每个 `cache_control` 断点之前的内容都会被缓存。所以 Prompt 中各部分的**顺序非常重要**：

```
Claude Code 的 Prompt 布局（从上到下）：
1. System Prompt    → 静态，锁定
2. Tool Definitions → 静态，锁定
3. Chat History     → 动态，在后面
4. 当前用户输入     → 最后
```

静态部分在前面，动态部分在后面——这样无论对话怎么推进，前面的缓存始终有效。

### 破坏缓存的常见陷阱

| 陷阱 | 为什么会破坏缓存 | 解决方案 |
|------|----------------|---------|
| 在 System Prompt 中放入带时间戳的内容 | 每次请求前缀都变了 | 时间信息放在用户消息的 `<system-reminder>` 标签中 |
| 非确定性地打乱工具定义顺序 | 前缀变化导致缓存失效 | 保持工具定义顺序稳定 |
| 会话中途增删工具 | 前缀结构改变 | 避免中途修改 MCP 配置 |

💡 Claude Code 自己也是这么做的：当前时间等动态信息通过 `<system-reminder>` 标签放在用户消息里，而不是 System Prompt 中。

### 缓存 TTL 控制

默认的 Prompt 缓存有效期为 5 分钟。v2.1.108 新增了 `ENABLE_PROMPT_CACHING_1H` 环境变量，可将缓存 TTL 延长到 1 小时（适用于 API key、Bedrock、Vertex 和 Foundry），大幅提升长会话的成本效益。同时提供 `FORCE_PROMPT_CACHING_5M` 强制使用 5 分钟 TTL（适用于需要频繁刷新前缀的场景）。

⚠️ 缓存 TTL 越长，Prompt 前缀必须越稳定。启用 1 小时 TTL 后，更要注意上方提到的缓存陷阱——中途增删工具或放入时间戳内容会导致缓存无法命中。

### 会话中途不要切换模型

Prompt 缓存是**模型唯一**的。假如你已经和 Opus 对话了 100K tokens，想问个简单问题切换到 Haiku——实际上比继续用 Opus **更贵**，因为要为 Haiku 重建整个缓存。

确实需要切换时，用 Subagent 交接：Opus 准备一条"交接消息"给另一个模型，说明需要完成的任务。

### defer_loading：工具的延迟加载

Claude Code 有数十个内置工具和 MCP 工具，每次请求全量包含会很贵。但中途移除工具又会破坏缓存（见上方陷阱）。解决方案是发送轻量级 **stub**——只有工具名，标记 `defer_loading: true`。模型通过 `ToolSearch` 工具"发现"它们，完整的工具 schema 只在模型选择后才加载。v2.1.84 修复了 `ToolSearch` 启用时全局系统提示缓存的兼容问题，同时改进了 p90 prompt 缓存命中率。

这样，缓存前缀保持稳定，同时避免了加载全部工具定义的开销。

## 🎯 小结

| 概念 | 一句话理解 | 关键行动 |
|------|-----------|---------|
| **1M 上下文** | 窗口大了，但噪声问题更严重 | 主动管理质量，别等满了再处理 |
| **上下文腐化** | 300-400K tokens 后性能下降 | 提前用 Compact/Clear/Subagent |
| **Rewind** | 双击 Esc 抹掉错误回答 | 跑偏时回退比纠错更干净 |
| **Compact vs Clear** | Compact 保留记忆；Clear 彻底清空 | 高风险下一步前选 Clear |
| **上下文成本** | 固定开销占 15-20K | 用 `/context` 监控占用 |
| **分层策略** | 偶尔用的别常驻 | 用 Skills/Hooks 替代长 CLAUDE.md |
| **Tool Output 噪声** | 命令输出也是 token | 加 `\| head -30` 或用 RTK |
| **压缩陷阱** | 算法会丢决策和理由 | 写 Compact Instructions |
| **HANDOFF.md** | 换会话前写交接文档 | 长任务的工程化最佳实践 |
| **Prompt Caching** | 缓存命中率直接决定成本 | 别中途切换模型、别改 System Prompt |
