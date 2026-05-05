---
title: 模型与输出风格
description: 模型别名、思考强度、扩展上下文、输出风格定制、Fast Mode
---

# 模型与输出风格

**本文你会学到**：

- 🧠 模型别名与思考强度的配置方式
- 📏 扩展上下文（从 200K 到 1M tokens）与模型固定
- 🎨 三种内置输出风格与自定义风格创建
- ⚡ Fast Mode 的原理、成本、限制与适用场景

## 🧠 怎么切换不同的模型？

### 模型别名

Claude Code 内置了一组简洁的模型别名，方便快速切换：

| 别名 | 实际模型 | 特点 |
|------|---------|------|
| `sonnet` | Claude Sonnet 4 | 平衡性能与成本 |
| `opus` | Claude Opus 4.6 | 最强能力，速度较慢 |
| `haiku` | Claude Haiku 3.5 | 最快速度，成本最低 |
| `opusplan` | Opus（用于规划）+ Sonnet（用于执行） | 规划用强模型，执行用快模型 |

💡 在会话中输入 `/model` 即可查看和切换模型。

### 思考强度（Effort Level）

思考强度控制 Claude 在回答前「思考多久」。你可以把它理解为考试时「打草稿的时间」：

| 级别 | 行为 | 适用场景 |
|------|------|---------|
| 高 | 深度推理，适合复杂问题 | 架构设计、复杂 Bug 排查 |
| 中 | 适中的推理 | 日常开发 |
| 低 | 快速响应，减少思考时间 | 简单修改、代码格式化 |

> 💡 **默认等级变更**（v2.1.94）：API Key、Bedrock/Vertex/Foundry、Team 和 Enterprise 用户的默认 effort 等级已从「中」提升为「高」。Pro 个人用户仍默认为「中」。在会话中输入 `/effort` 可以动态调整。

### 扩展上下文

当你在一个有 50+ 文件的项目中做跨文件重构，或者对话已经很长导致 Claude 开始「遗忘」早期内容时——这就意味着上下文窗口快满了。Claude Code 的默认上下文窗口为 200K tokens，对于这种场景可能不够用，可以启用扩展上下文，将窗口扩展到 **1M tokens**（约 5 倍）。

``` bash
# 启动时指定扩展上下文
claude --model claude-sonnet-4-20250514 --max-tokens 1000000
```

⚠️ 扩展上下文会增加输入 token 的成本。如果你的对话未超过 200K tokens，启用扩展上下文并不会带来额外好处。

### 模型固定（Model Pinning）

你可以在配置中固定使用特定版本的模型，避免 Claude Code 自动升级到新版模型：

``` json title="settings.json"
{
  "model": "claude-sonnet-4-20250514"
}
```

这对需要稳定行为的 CI/CD 环境很有用——确保每次运行的模型行为一致。

## 🎨 怎么控制 Claude 的输出风格？

### 三种内置风格

输出风格（Output Style）控制 Claude 的回复方式和语气。它直接修改 Claude Code 的系统提示词：

| 风格 | 行为 | 适用场景 |
|------|------|---------|
| `Default` | 高效完成软件工程任务 | 日常开发（默认） |
| `Explanatory` | 在完成任务的同时提供教学性的 Insight | 学习新代码库 |
| `Learning` | 协作模式，会让你自己动手写关键代码 | 深度学习、实践练习 |

💡 你可以把它想成三种「老师风格」：Default 是「安静高效的上司」，Explanatory 是「边做边讲解的导师」，Learning 是「让你自己动手的教练」。

### 切换输出风格

``` bash
# 通过 /config 交互式选择
/config

# 或直接在 settings.json 中设置
```

``` json title="settings.json"
{
  "outputStyle": "Explanatory"
}
```

⚠️ 输出风格在会话启动时加载到系统提示词中，因此**修改后需要重新启动会话才能生效**。这是为了保持系统提示词的稳定，以便利用 Prompt Caching 降低延迟和成本。

### 创建自定义输出风格

自定义输出风格是一个带 front matter 的 Markdown 文件，存放在 `~/.claude/output-styles/`（用户级）或 `.claude/output-styles/`（项目级）：

``` markdown title="~/.claude/output-styles/reviewer.md"
---
name: Code Reviewer
description: 以代码审查者的角色审查代码
keep-coding-instructions: true
---

# 审查模式

你是一个严格的代码审查者。对每个变更：
1. 检查是否有潜在的 Bug
2. 评估代码可读性
3. 提出改进建议

## 格式要求

- 先总结变更内容
- 按严重程度排列问题
- 每个建议附带具体的修改方案
```

| front matter 字段 | 说明 | 默认值 |
|-------------------|------|--------|
| `name` | 风格名称（显示在 /config 菜单中） | 文件名 |
| `description` | 风格描述 | 无 |
| `keep-coding-instructions` | 是否保留默认的编码相关系统提示词 | `false` |

📌 **Output Style vs CLAUDE.md vs Skills**：
- **Output Style** 直接修改系统提示词，改变 Claude 的「角色设定」
- **CLAUDE.md** 作为用户消息追加在系统提示词之后，不改变 Claude 的角色
- **Skills** 是任务特定的提示词，通过 `/skill-name` 手动触发

## ⚡ Fast Mode：速度优先

### 什么是 Fast Mode？

Fast Mode 是 Claude Opus 4.6 的高速度配置——**不是换了模型，而是换了 API 参数**。你可以把它理解成「同一位厨师，从正常烹饪模式切到快炒模式」：菜品质量不变，但上菜速度提升约 2.5 倍。

### 开启方式

``` bash
# 在会话中切换
/fast

# 或在 settings.json 中设置
```

``` json title="settings.json"
{
  "fastMode": true
}
```

开启后，提示符旁会出现 `↯` 图标，表示 Fast Mode 已激活。

### 成本对比

Fast Mode 的每 token 定价高于标准 Opus 4.6，并且根据上下文长度分为两个档位：

| 模式 | 上下文范围 | 输入价格（百万 token） | 输出价格（百万 token） |
|------|-----------|----------------------|----------------------|
| 标准 Opus 4.6 | — | 更低 | 更低 |
| Fast Mode | < 200K tokens | $30 | $150 |
| Fast Mode | > 200K tokens | $60 | $225 |

⚠️ Fast Mode 仅通过 Extra Usage（额外用量）计费，不包含在订阅套餐内。即使你的套餐还有剩余用量，Fast Mode 也会从第一个 token 开始按 Fast Mode 费率单独收费。

💡 如果你计划使用 Fast Mode，**在会话开始时启用**比在对话中途切换更划算——中途切换时，整个对话上下文都需要按 Fast Mode 的未缓存输入 token 价格重新计费。

### 可用性与限制

Fast Mode 目前的可用范围有限制：

- **需要 Extra Usage**：个人账户在控制台计费设置中启用，团队和企业由管理员统一启用
- **第三方云提供商不可用**：Fast Mode 不支持 Amazon Bedrock、Google Vertex AI、Microsoft Azure Foundry，仅通过 Anthropic 控制台 API 和订阅计划的 Extra Usage 提供
- **团队和企业默认禁用**：管理员需要手动启用后，用户才能使用 `/fast` 命令。未启用时 `/fast` 会提示 "Fast mode has been disabled by your organization."
- **与扩展上下文兼容**：Fast Mode 支持 1M tokens 的扩展上下文窗口
- **速率限制与自动回退**：当 Fast Mode 的速率限制到达上限时，会自动回退到标准 Opus 4.6，`↯` 图标变为灰色（表示冷却中），冷却结束后自动恢复 Fast Mode

管理员可以在以下位置启用 Fast Mode：
- **控制台**（API 客户）：Claude Code 偏好设置页面
- **Claude AI**（团队和企业）：管理员设置 > Claude Code

### 环境变量与组织级控制

除了在会话中用 `/fast` 或在 `settings.json` 中设置 `"fastMode": true` 之外，还有两种机制可以控制 Fast Mode：

**完全禁用 Fast Mode**：设置环境变量 `CLAUDE_CODE_DISABLE_FAST_MODE=1`，这将彻底关闭 Fast Mode 功能。详见环境变量文档。

**要求每会话手动启用**：团队和企业管理员可以在托管设置（参见「Settings 与权限」）或服务器托管设置中将 `fastModePerSessionOptIn` 设为 `true`：

``` json title="托管设置"
{
  "fastModePerSessionOptIn": true
}
```

启用后，每个新会话都以 Fast Mode 关闭状态开始，用户需要用 `/fast` 手动开启。这在用户运行多个并发会话的组织中有助于控制成本。删除该设置后，用户的偏好会恢复为默认的跨会话持久行为。

### 什么时候用 Fast Mode？

✅ 适合：

- 快速迭代代码改动
- 实时调试
- 时间紧迫的紧急任务

❌ 不适合：

- 长时间自主任务
- 批量处理 / CI/CD
- 成本敏感的工作负载

### Fast Mode 与思考强度（Effort Level）

Fast Mode 和 Effort Level 都能影响响应速度，但作用机制完全不同：

| 设置 | 效果 |
|------|------|
| **Fast Mode** | 相同的模型质量，更低的延迟，更高的成本 |
| **较低的 Effort Level** | 更少的思考时间，更快的响应，复杂任务上可能质量下降 |

两者可以叠加使用——对于简单的直接任务，「Fast Mode + 低 Effort Level」组合能获得最大速度。
