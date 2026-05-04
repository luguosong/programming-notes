---
title: 自动化
description: 定时任务、远程控制、消息通道、CI/CD 集成与自动代码审查
---

**本文你会学到**：

- 🎯 三种调度方式对比（`/loop`、Desktop、云端），选择最适合你的方案
- ⏰ 用 `/loop` 命令设置本地定时任务：固定间隔、动态间隔、内置维护提示词
- 📋 管理计划任务、设置一次性提醒、Cron 表达式参考
- 🎮 通过 Remote Control 远程操控 Claude Code 会话
- 📡 使用 Channels 将外部消息源接入 Claude Code 对话
- ⚙️ 在 GitHub Actions 和 GitLab CI/CD 中集成 Claude Code
- 🔍 配置自动代码审查（Code Review）托管服务

## ⏰ 定时任务

想象一下，你有一个实习生，你让他每天早上 9 点检查一遍代码有没有报错，每周五下午跑一次测试——你不需要每天手动提醒他，只要设定好时间表，他就会准时执行。Claude Code 的定时任务就是这样一个「定时闹钟 + 自动执行」的机制。

### 三种调度方式对比

Claude Code 提供三种调度方式，适用于不同场景：

| 维度 | 云端（Routines） | Desktop 计划任务 | `/loop`（会话内） |
|------|----------------|----------------|------------------|
| 运行位置 | Anthropic 云端 | 你的电脑 | 你的电脑 |
| 需要电脑在线 | 否 | 是 | 是 |
| 需要会话打开 | 否 | 否 | 是 |
| 跨重启持久化 | 是 | 是 | `--resume` 恢复未过期任务 |
| 访问本地文件 | 否（全新 clone） | 是 | 是 |
| MCP 服务器 | 按任务配置 Connector | 配置文件 + Connector | 继承当前会话 |
| 权限提示 | 否（自主运行） | 按任务配置 | 继承当前会话 |
| 最小间隔 | 1 小时 | 1 分钟 | 1 分钟 |

💡 **选型建议**：需要不依赖本机、可靠运行的任务用**云端**；需要访问本地文件但不一定有活跃会话的用 **Desktop**；开发阶段想快速轮询某个状态用 **`/loop`**。

### `/loop` 命令详解

`/loop` 是一个[内置 Skill](../skills/index.md#官方内置-skills)，让 Claude Code 在当前会话中周期性重复执行任务（v2.1.71 新增）。`/proactive` 是 `/loop` 的别名（v2.1.105 新增），效果完全相同。从 v2.1.113 起，按 `Esc` 可取消待执行的唤醒；唤醒触发时会清晰显示为 "Claude resuming /loop wakeup"，便于识别是定时任务还是普通对话。

你提供给 `/loop` 的内容决定了它的行为模式：

| 你提供的内容 | 示例 | 行为 |
|-------------|------|------|
| 间隔 + 提示词 | `/loop 5m check the deploy` | 按[固定计划](#固定间隔运行)运行 |
| 仅提示词 | `/loop check the deploy` | Claude [动态选择间隔](#动态间隔运行) |
| 仅间隔或无 | `/loop` | 运行[内置维护提示词](#内置维护提示词) |

你也可以将另一个斜杠命令作为提示词传递，例如 `/loop 20m /review-pr 1234`，在每次迭代时重新运行打包的工作流。

#### 固定间隔运行

当你同时提供间隔和提示词时，Claude 将间隔转换为 cron 表达式，按固定计划执行：

```text
/loop 5m check if the deployment finished and tell me what happened
```

间隔可以写在提示词前面（裸格式如 `30m`），也可以写在后面（从句如 `every 2 hours`）。支持单位：`s`（秒）、`m`（分钟）、`h`（小时）、`d`（天）。

⚠️ 秒数向上舍入到最近的分钟（cron 最小粒度为 1 分钟）。不能均匀映射到 cron 步长的间隔（如 `7m`、`90m`）会舍入到最近的间隔，Claude 会告诉你实际选择了什么。

#### 动态间隔运行

当你省略间隔时，Claude 在每次迭代后根据观察到的情况**动态选择延迟**（1 分钟到 1 小时之间）：构建完成时等待较短，没有待处理项时等待较长。

```text
/loop check whether CI passed and address any review comments
```

选择的延迟和原因会在每次迭代结束时打印。这种模式比固定间隔更节省 token——Claude 会在任务变得安静后自动降低检查频率。

动态计划的循环也出现在[计划任务列表](#管理计划任务)中，可以像其他任务一样列出或取消。[七天过期](#七天过期)同样适用。

!!! info "Monitor tool"

    当你请求动态间隔时，Claude 可能会直接使用 Monitor tool 而非 cron 调度。Monitor 运行后台脚本并流式传输每个输出行，完全避免了轮询，通常比按间隔重新运行提示词更节省 token 且响应更快。

#### 内置维护提示词

当你不带任何参数运行 `/loop` 时，Claude 使用内置维护提示词，依次处理：

1. 继续对话中未完成的工作
2. 照顾当前分支的 PR：审查评论、失败的 CI、合并冲突
3. 运行清理（如错误搜索、代码简化）

Claude 不会启动该范围之外的新举措，不可逆操作（如推送或删除）仅在继续已授权的内容时进行。

```text
/loop
```

裸 `/loop` 在动态选择的间隔上运行。添加间隔（如 `/loop 15m`）则在固定计划上运行。要用你自己的默认值替换内置提示词，创建 `loop.md` 文件。

#### 自定义默认提示词：loop.md

`loop.md` 文件替换内置维护提示词，只对裸 `/loop` 生效（你提供了提示词时它被忽略）。Claude 在两个位置查找，使用找到的第一个：

| 路径 | 作用范围 |
|------|---------|
| `.claude/loop.md` | 项目级（优先） |
| `~/.claude/loop.md` | 用户级（适用于未定义 `loop.md` 的项目） |

```markdown title=".claude/loop.md"
Check the `release/next` PR. If CI is red, pull the failing job log,
diagnose, and push a minimal fix. If new review comments have arrived,
address each one and resolve the thread. If everything is green and
quiet, say so in one line.
```

对 `loop.md` 的编辑在下一次迭代时生效。文件大小超过 25,000 字节会被截断。

#### 停止循环

在 `/loop` 等待下一次迭代时按 `Esc` 即可停止——这会清除待处理的唤醒。但你通过自然语言或 `CronCreate` 计划的任务不受 `Esc` 影响，需要通过[管理计划任务](#管理计划任务)手动删除。

### 一次性提醒

对于一次性提醒，用自然语言描述即可。Claude 计划一个单次触发的任务，运行后自动删除：

```text
remind me at 3pm to push the release branch
```

```text
in 45 minutes, check whether the integration tests passed
```

Claude 使用 cron 表达式将触发时间固定到特定的分钟和小时，并确认何时触发。

### 管理计划任务

用自然语言要求 Claude 列出或取消任务：

```text
what scheduled tasks do I have?
```

```text
cancel the deploy check job
```

在幕后，Claude 使用三个工具：

| 工具 | 用途 |
|------|------|
| `CronCreate` | 计划新任务（5 字段 cron 表达式 + 提示词 + 重复/单次） |
| `CronList` | 列出所有计划任务（ID、计划、提示词） |
| `CronDelete` | 按 ID 取消任务 |

每个任务有一个 8 字符 ID，一个会话最多同时保存 50 个计划任务。

### 计划任务的运行机制

#### 调度与排队

调度程序每秒检查一次到期的任务，以低优先级将其加入队列。计划的提示词在你的回合之间触发——如果 Claude 正忙，提示词会等到当前回合结束。所有时间都使用你的本地时区。

#### 抖动

为了避免所有会话在同一时刻请求 API，调度程序添加确定性偏移：

- 重复任务：最多晚触发其周期的 10%，上限 15 分钟
- 一次性任务：为整点或半点计划的任务最多提前 90 秒触发

偏移从任务 ID 派生，相同任务总是获得相同偏移。如果你在意精确时间，选择不是整点或半点的分钟（如 `3 9 * * *` 而非 `0 9 * * *`），一次性抖动将不适用。

#### 七天过期

重复任务在创建后 **7 天自动过期**——任务最后触发一次，然后自行删除。如果需要更长时间的调度，请在过期前取消并重新创建，或使用云端定时任务 / Desktop 计划任务。

### Cron 表达式参考

`CronCreate` 接受标准 5 字段 cron 表达式：`minute hour day-of-month month day-of-week`。所有字段都支持通配符（`*`）、单个值、步长（`*/15`）、范围（`1-5`）和逗号分隔列表。

| 示例 | 含义 |
|------|------|
| `*/5 * * * *` | 每 5 分钟 |
| `0 * * * *` | 每小时整点 |
| `7 * * * *` | 每小时的第 7 分钟 |
| `0 9 * * *` | 每天本地时间 9am |
| `0 9 * * 1-5` | 工作日每天 9am |
| `30 14 15 3 *` | 3 月 15 日下午 2:30 |

星期几使用 `0` 或 `7` 表示周日，`6` 表示周六。不支持 `L`、`W`、`?` 和名称别名（如 `MON`、`JAN`）。

当月份日期和星期几都受限时，任一字段匹配即算匹配（标准 vixie-cron 语义）。

### 禁用计划任务

设置环境变量 `CLAUDE_CODE_DISABLE_CRON=1` 可完全禁用调度程序——cron 工具和 `/loop` 变得不可用，已计划的任务也停止触发。

### 局限性

会话范围的调度有固有限制：

- 任务仅在 Claude Code 运行且空闲时触发——关闭终端或退出会话会停止触发
- 没有错过触发的追赶——如果计划时间在 Claude 忙碌时经过，它只会在空闲时触发一次
- 启动新对话会清除所有会话范围的任务——使用 `--resume` 或 `--continue` 恢复会话可找回未过期的任务
- 后台 Bash 和 Monitor 任务在恢复时不会被恢复

需要无人值守的定时自动化，请使用云端定时任务、Desktop 计划任务或 [GitHub Actions](#github-actions-集成)。

### 云端定时任务

云端定时任务运行在 Anthropic 管理的云端基础设施上，**不依赖你的本机**——即使你关机了，任务依然准时执行。详细的三种调度方式对比见[上文](#三种调度方式对比)。

#### 核心特性

| 特性 | 说明 |
|------|------|
| 运行环境 | Anthropic 云端基础设施，无需本机在线 |
| 任务持久性 | 即使你的电脑关机，任务依然按计划执行 |
| 频率选项 | 每小时、每天、每周、自定义 cron |
| 分支控制 | 可指定在哪个 Git 分支上执行 |
| Connector 支持 | 任务可通过 Anthropic Connector 访问你的代码仓库 |

#### 配置方式

云端定时任务通过 Anthropic Console 的 Claude Code 页面进行管理。配置时需要指定：

1. **任务描述**：告诉 Claude Code 每次要做什么
2. **执行频率**：支持预置选项和自定义 cron
3. **目标仓库与分支**：通过 Connector 关联到你的代码仓库
4. **权限范围**：控制在仓库中可执行的操作

## 🎮 远程控制

Remote Control 让你从外部**操控一个正在运行的 Claude Code 会话**——就像远程桌面一样，只不过你控制的是一个 AI 助手的对话。

### Server Mode

Server Mode 把 Claude Code 变成一个「服务端」，监听来自客户端的连接请求：

```bash
# 启动 server mode
claude --server
```

启动后，Claude Code 会在后台运行，等待外部连接。

### Interactive Mode

Interactive Mode 是更完整的交互体验——它不仅允许远程控制，还提供了完整的终端界面：

```bash
# 启动 interactive mode（默认监听本机）
claude -i

# 指定监听地址
claude -i --host 0.0.0.0 --port 8080
```

### 连接到已有会话

如果你已经有一个 Claude Code 会话在运行，可以通过 `/rc`（Remote Control）命令从另一个终端连接过去：

```
> /rc
```

Claude Code 会显示可连接的会话列表，选择后即可在新终端中操控原有会话。

⚠️ **安全提醒**：Remote Control 会暴露你的 Claude Code 会话给网络上的其他客户端。在生产环境中使用时，务必配合网络隔离和认证机制，避免未授权访问。

### Remote Control 命令扩展

Remote Control 的可用命令持续扩展中（v2.1.110 改进）。除了基本的对话交互，以下命令现在也可以在 Remote Control 客户端（移动端/Web）中使用：

- `/autocompact`：自动压缩上下文
- `/context`：查看当前上下文
- `/exit`：退出会话
- `/reload-plugins`：重新加载插件
- `/extra-usage`：从远程客户端管理额外用量额度（v2.1.113 新增）

从 v2.1.113 起，Remote Control 客户端还可以查询 `@`-file 自动补全建议，并能正常流式接收 subagent 的 transcript（之前会丢失）；会话在 Claude Code 退出时也会被正确归档。

这意味着你通过手机远程控制 Claude Code 时，能做的事和坐在电脑前几乎一样了。

### 推送通知

Claude Code 支持发送移动端推送通知（v2.1.110 新增）。当 Remote Control 配置启用时，Claude 可以在需要你关注时（比如任务完成、等待权限确认）向手机发送推送通知。你不需要一直盯着屏幕——去做别的事，Claude 会主动通知你。

### 适用场景

| 场景 | 说明 |
|------|------|
| 多终端协作 | 在办公室电脑启动会话，回家后从笔记本连接继续 |
| 团队协作 | 多人连接同一个 Claude Code 会话，协同处理任务 |
| 无头环境 | 在服务器上以 server mode 运行，从本地客户端操控 |

## 📡 Channels 消息通道

想象 Claude Code 是你的私人助理。平时你只能亲自去他的办公室（终端）找他。但如果助理有一部手机（Channel），你就可以通过微信、钉钉等工具给他发消息，他收到后会直接处理并回复——你甚至不需要打开终端。

**Channels 就是 Claude Code 的「通讯录」**——它让外部消息源能把事件推送到 Claude Code 的会话中，让 Claude 像处理普通对话一样处理这些外部输入。

### 支持的消息源

Claude Code 目前支持以下消息源（v2.1.0 引入 Research Preview）：

| 消息源 | 说明 |
|--------|------|
| Telegram | 通过 Telegram Bot 接收消息 |
| Discord | 通过 Discord Bot 接收频道消息 |
| iMessage | 通过 iMessage 接收消息（macOS 专属） |

> 💡 **Research Preview**：Channels 目前处于研究预览阶段，API 可能发生变化。建议在非关键场景中使用，并关注官方更新。

### Channel 的工作原理

一个 Channel 本质上是一个 **Webhook 接收器 + 消息转发器**：

```mermaid
graph LR
    A[外部消息源<br>Telegram / Discord] -->|Webhook| B[Channel 接收器]
    B -->|转发事件| C[Claude Code 会话]
    C -->|回复| B
    B -->|发送回复| A
```

1. 外部消息源（如 Telegram）通过 Webhook 将消息发送到你的 Channel
2. Channel 将消息作为事件注入到 Claude Code 会话中
3. Claude Code 处理事件并生成回复
4. Channel 将回复发送回原始消息源

### 构建自定义 Channel

你可以通过 `claude/channel` capability 来声明一个自定义 Channel。Channel 需要实现以下核心能力：

- **接收外部事件**：通过 Webhook 或其他方式获取外部消息
- **向 Claude Code 注入消息**：将事件转化为 Claude 可理解的对话输入
- **权限中继（Permission Relay）**：将 Claude 的工具调用请求转发给用户授权
- **回复投递**：将 Claude 的响应发送回消息源

⚠️ 构建自定义 Channel 需要一定的开发能力——你需要部署一个 Webhook 接收服务，并处理消息格式的转换。如果你只是想快速体验，建议先使用官方支持的 Telegram / Discord Channel。

## 🔄 GitHub Actions 集成

如果你用过 GitHub Actions，一定知道它可以在代码提交、PR 创建等事件发生时自动执行流水线。把 Claude Code 嵌入这个流水线，就等于让一个 AI 助手在每次 CI 触发时自动干活——比如自动修复 lint 错误、生成测试、审查代码。

### 基本配置

Claude Code 提供官方 Action：`anthropics/claude-code-action@v1`。以下是基本用法：

``` yaml title=".github/workflows/claude.yml"
name: Claude Code
on:
  issues:
    types: [labeled]

jobs:
  claude:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Run Claude Code
        uses: anthropics/claude-code-action@v1
        with:
          anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
          prompt: |
            修复这个 issue 中描述的问题。
            完成后创建一个 PR。
```

### 通过 @claude 触发

除了通过 workflow 事件触发外，你还可以在 GitHub Issue、PR 评论、代码审查评论中使用 `@claude` 来触发 Claude Code：

```markdown
@claude 请帮我修复这个测试失败的问题
```

Claude Code 会读取上下文（Issue 内容、PR diff 等），然后执行相应操作。

v2.1.119 扩展了 `--from-pr` 参数的支持范围：现在除了 GitHub PR URL 外，还接受 GitLab merge-request、Bitbucket pull-request 和 GitHub Enterprise PR URL。结合 `owner/repo#N` 链接主机检测的改进（现在使用 git remote 的主机而非总是指向 github.com），在多平台 Git 环境中使用更加顺畅。

### 常用配置参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `anthropic_api_key` | Anthropic API 密钥 | `${{ secrets.ANTHROPIC_API_KEY }}` |
| `prompt` | 给 Claude 的指令 | `"修复 lint 错误"` |
| `model` | 使用的模型 | `claude-sonnet-4-20250514` |
| `allowed_tools` | 允许 Claude 使用的工具列表 | `"Bash(git*)" "Edit"` |
| `timeout_minutes` | 超时时间（分钟） | `10` |
| `directives` | 额外的行为指令文件路径 | `.claude/commands/fix.md` |

### 实际示例：自动修复 lint

``` yaml title=".github/workflows/auto-fix.yml"
name: Auto Fix Lint
on:
  pull_request:
    types: [opened, synchronize]

jobs:
  auto-fix:
    runs-on: ubuntu-latest
    permissions:
      contents: write
    steps:
      - uses: actions/checkout@v4

      - name: Install dependencies
        run: npm ci

      - name: Run lint fix with Claude
        uses: anthropics/claude-code-action@v1
        with:
          anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
          prompt: |
            运行 npm run lint 查看错误，
            然后修复所有 lint 错误。
            如果有修改，直接提交到当前分支。
```

💡 **`allowed_tools` 的重要性**：在 CI 环境中，建议通过 `allowed_tools` 限制 Claude 可执行的操作范围，避免它执行不必要（甚至危险）的命令。比如只允许 `Read`、`Edit`、`Bash(npm*)` 等。

## 🚀 GitLab CI/CD 集成

GitLab CI/CD 的集成方式和 GitHub Actions 类似——通过在 `.gitlab-ci.yml` 中配置 Claude Code 的运行步骤，让 AI 助手参与到你的 CI 流水线中。

### 基本配置

``` yaml title=".gitlab-ci.yml"
stages:
  - review

claude-review:
  stage: review
  image: node:20
  before_script:
    - npm install -g @anthropic-ai/claude-code
  script:
    - claude -p "审查当前分支的代码变更，指出潜在问题并给出修复建议"
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
```

### 使用 OIDC 认证

如果你使用 AWS Bedrock 或 Google Vertex AI 作为 Claude 的模型提供方，可以通过 OIDC（OpenID Connect）让 GitLab Runner 无需硬编码 API 密钥即可安全访问：

``` yaml title=".gitlab-ci.yml"
claude-review:
  stage: review
  image: node:20
  id_tokens:
    CLAUDE_OIDC_TOKEN:
      aud: https://claude.ai
  script:
    - npm install -g @anthropic-ai/claude-code
    # 使用 OIDC Token 认证，无需 API Key
    - claude -p "审查代码变更"
  variables:
    ANTHROPIC_AUTH_TOKEN: $CLAUDE_OIDC_TOKEN
```

💡 **OIDC 的优势**：传统方式需要在 CI/CD 中存储 API 密钥（secrets），存在泄露风险。OIDC 通过短期的令牌交换实现认证，无需长期密钥，安全性更高。

### GitHub Actions vs GitLab CI/CD

| 维度 | GitHub Actions | GitLab CI/CD |
|------|---------------|-------------|
| 配置文件 | `.github/workflows/*.yml` | `.gitlab-ci.yml` |
| 官方 Action | `anthropics/claude-code-action@v1` | 直接调用 `claude` CLI |
| @claude 触发 | 支持（Issue/PR 评论） | 不支持 |
| OIDC 认证 | 通过 `actions/setup-oidc` | 通过 `id_tokens` 关键字 |
| 触发方式 | workflow events + @claude | pipeline rules |

## 🔍 自动代码审查

### `/ultrareview`：终端触发的并行多 Agent 审查

`/ultrareview` 是一个强大的代码审查命令（v2.1.111 新增），它利用 Anthropic 云端的多 Agent 协作能力，对代码进行并行分析。与下面介绍的托管式自动代码审查不同，`/ultrareview` 是**直接在终端中触发的**：

```bash
# 审查当前分支的所有变更
/ultrareview

# 审查指定 GitHub PR
/ultrareview 123
```

v2.1.113 进一步优化了 `/ultrareview`：并行检查启动更快、启动对话框直接显示 diffstat 让你预览改动规模、启动状态有动画反馈。如果你需要进一步打磨结果，"Refine with Ultraplan" 现在会在 transcript 中显示远程会话 URL（v2.1.113 修复），方便分享和追溯。

它会在云端启动多个 Agent，分别从不同维度（代码质量、安全、最佳实践等）同时审查代码，然后汇总结果。适合在提交 PR 前做一次全面审查。

### 托管式自动代码审查

托管式自动代码审查是 Anthropic 提供的**托管服务**——你不需要自己搭建 CI 流水线，只需要在 Anthropic Console 中配置，就能让多 Agent 协作系统自动分析你仓库中的 PR。

打个比方：你自己搭建 GitHub Actions + Claude Code 做审查，就像自己装修房子；而使用自动代码审查托管服务，就像请了一个精装修团队——他们带着专业工具直接上门服务。

#### 工作原理

自动代码审查服务采用**多 Agent 协作**的方式：

```mermaid
graph LR
    A[PR 创建/更新] -->|Webhook| B[代码审查服务]
    B --> C[Agent 1<br>代码分析]
    B --> D[Agent 2<br>安全检查]
    B --> E[Agent 3<br>最佳实践检查]
    C --> F[汇总审查意见]
    D --> F
    E --> F
    F -->|发布评论| G[PR 页面]
```

每个 Agent 专注于不同维度的审查，最终将所有意见汇总为一条或多条 PR 评论。

#### 审查严重级别

审查结果会标记不同的严重级别，帮助你快速判断哪些问题需要优先处理：

| 级别 | 含义 | 建议处理方式 |
|------|------|-------------|
| 🔴 Critical | 严重问题（安全漏洞、数据丢失风险等） | 必须立即修复 |
| 🟠 Warning | 警告（潜在 Bug、性能问题等） | 强烈建议修复 |
| 🟡 Info | 建议（代码风格、可读性改进等） | 可选改进 |
| 🟢 Nit | 细微问题（命名、格式等） | 可忽略 |

#### 自定义审查行为

你可以通过以下方式自定义审查服务的偏好：

**`CLAUDE.md`**：在仓库根目录放置 `CLAUDE.md` 文件，审查 Agent 会自动读取并遵循其中的规范：

``` markdown title="CLAUDE.md"
# 项目规范

- 本项目使用 Java 17 + Spring Boot 3.x
- 所有 public 方法必须有 Javadoc
- 禁止使用 `@Autowired` 字段注入，统一使用构造器注入
- 异常处理不允许 catch 后静默忽略
```

**`REVIEW.md`**：专门用于代码审查的指令文件，优先级高于 `CLAUDE.md`：

``` markdown title="REVIEW.md"
# 审查重点

- 关注并发安全性：检查共享可变状态是否正确同步
- 关注资源泄露：确保流、连接等资源在 finally 中关闭
- 审查意见请用中文
```

💡 **`CLAUDE.md` vs `REVIEW.md`**：`CLAUDE.md` 是项目通用规范，影响所有 Claude Code 使用场景；`REVIEW.md` 是审查专属指令，只影响自动代码审查服务。如果两者同时存在且内容冲突，`REVIEW.md` 优先。

#### 配置入口

自动代码审查服务通过 Anthropic Console 的 Claude Code 页面进行配置：

1. 关联你的 GitHub/GitLab 仓库
2. 设置触发条件（哪些分支、哪些事件触发审查）
3. 配置审查偏好（严重级别阈值、关注的代码区域等）
4. （可选）在仓库中添加 `CLAUDE.md` 和 `REVIEW.md` 自定义审查行为

📝 **小结**：自动代码审查服务适合不想自己维护 CI 流水线的团队。如果你需要更细粒度的控制（比如只在特定文件变更时审查），或者想和现有的 CI 流程深度集成，建议使用前面介绍的 GitHub Actions / GitLab CI/CD 方式自己搭建。

## ⚡ 其他自动化改进

从 v2.1.111 起，Auto 模式不再需要 `--enable-auto-mode` 参数即可使用。你可以直接切换到 Auto 权限模式，让 Claude 在无需逐次确认的情况下执行操作——适合在可信环境中使用。
