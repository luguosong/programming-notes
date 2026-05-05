---
title: GitHub Actions
description: 在 GitHub Actions 中集成 Claude Code 实现自动化代码审查和修复
---

**本文你会学到**：

- 如何在几分钟内完成 Claude Code GitHub Actions 的基本配置
- 从 Beta 升级到 GA v1.0 的迁移要点
- 使用 Amazon Bedrock 和 Google Vertex AI 的企业级配置方法
- 安全、成本控制和故障排除的最佳实践

## 为什么在 GitHub Actions 中用 Claude Code？

当你需要在每次 PR 创建时自动审查代码、在 Issue 被标记时自动实现功能、或者在评论中提到 AI 助手时让它帮忙修复 Bug——这些场景下手动操作既低效又容易遗漏。Claude Code GitHub Actions 把 AI 能力直接嵌入你的 CI 流水线，让它像一个永远在线的团队成员一样自动响应。

核心价值：

- **即时 PR 创建**：描述需求，Claude 自动创建包含完整变更的 PR
- **自动化代码实现**：通过 `@claude` 提及，将 Issue 转化为可工作的代码
- **遵循项目标准**：Claude 读取仓库中的 `CLAUDE.md`，遵守你的编码规范
- **简单设置**：一条命令完成 GitHub App 安装和密钥配置
- **默认安全**：代码始终留在 GitHub 托管的 Runner 上

!!! info "底层技术"

    Claude Code GitHub Actions 基于 [Claude Agent SDK](../agent-sdk/index.md) 构建，支持以编程方式将 Claude Code 集成到应用程序中。你可以用该 SDK 构建超越 GitHub Actions 的自定义自动化工作流。

## Claude 能做什么？

Claude Code 提供了一个功能强大的 GitHub Action（`anthropics/claude-code-action@v1`），支持以下场景：

- **审查代码**：在 PR 评论中触发代码质量、安全性和正确性审查
- **修复 Bug**：在 Issue 或 PR 评论中描述问题，Claude 自动定位并修复
- **实现功能**：基于 Issue 描述直接在分支中编写代码并打开 PR
- **回答问题**：在代码审查讨论中向 Claude 询问实现建议

你可以在 Issue 或 PR 评论中这样使用：

```text
@claude implement this feature based on the issue description
@claude how should I implement user authentication for this endpoint?
@claude fix the TypeError in the user dashboard component
```

## 快速设置

最快的方式是通过终端中的 Claude Code 运行安装命令：

```bash
claude
```

然后在 Claude Code 会话中执行：

```text
/install-github-app
```

这条命令会引导你完成 GitHub App 安装和 API 密钥配置。

!!! note "前提条件"

    - 你必须是仓库管理员才能安装 GitHub App 和添加密钥
    - GitHub App 会请求对 Contents、Issues、Pull requests 的读写权限
    - 快速设置仅适用于 Claude API 直连用户。Bedrock / Vertex AI 用户请参考[手动设置](#手动设置)和[企业提供商配置](#使用-amazon-bedrock-和-google-vertex-ai)

完成后，在任意 Issue 或 PR 评论中标记 `@claude` 即可测试。

## 手动设置

如果 `/install-github-app` 命令失败，或者你更喜欢手动控制每个步骤：

### 安装 Claude GitHub App

访问 [https://github.com/apps/claude](https://github.com/apps/claude) 安装官方 Claude GitHub App。该 App 需要以下仓库权限：

| 权限 | 范围 | 用途 |
|------|------|------|
| Contents | 读写 | 修改仓库文件 |
| Issues | 读写 | 响应 Issue |
| Pull requests | 读写 | 创建 PR、推送更改 |

### 添加 API 密钥

在仓库的 **Settings** -> **Secrets and variables** -> **Actions** 中添加：

- `ANTHROPIC_API_KEY`：你的 Claude API 密钥（从 [console.anthropic.com](https://console.anthropic.com) 获取）

### 创建工作流文件

从官方示例仓库复制工作流模板到你的 `.github/workflows/` 目录：

```yaml title=".github/workflows/claude.yml"
name: Claude Code
on:
  issue_comment:
    types: [created]
  pull_request_review_comment:
    types: [created]
jobs:
  claude:
    runs-on: ubuntu-latest
    steps:
      - uses: anthropics/claude-code-action@v1
        with:
          anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
```

这个工作流监听 Issue 和 PR 评论事件，当评论中包含 `@claude` 时自动触发 Claude Code。

## 从 Beta 升级到 GA v1.0

如果你之前使用的是 Beta 版本（`@beta`），升级到 v1.0 需要修改工作流文件——新版本简化了配置并引入了自动模式检测。

!!! warning "重大变更"

    v1.0 引入了多项重大变更，必须更新工作流文件才能正常工作。

### 必要的迁移变更

所有 Beta 用户都需要完成以下四项修改：

1. **更新 action 版本**：`@beta` -> `@v1`
2. **删除 `mode` 配置**：`mode: "tag"` 或 `mode: "agent"` 已移除，现在自动检测
3. **替换提示输入**：`direct_prompt` -> `prompt`
4. **迁移 CLI 选项**：`max_turns`、`model`、`custom_instructions` 等移入 `claude_args`

### 迁移对照表

| 旧 Beta 输入 | 新 v1.0 输入 |
|-------------|-------------|
| `mode` | 已删除（自动检测） |
| `direct_prompt` | `prompt` |
| `override_prompt` | `prompt`（带 GitHub 变量） |
| `custom_instructions` | `claude_args: --append-system-prompt` |
| `max_turns` | `claude_args: --max-turns` |
| `model` | `claude_args: --model` |
| `allowed_tools` | `claude_args: --allowedTools` |
| `disallowed_tools` | `claude_args: --disallowedTools` |
| `claude_env` | `settings`（JSON 格式） |

### 迁移示例

**Beta 版本（旧）：**

```yaml
- uses: anthropics/claude-code-action@beta
  with:
    mode: "tag"
    direct_prompt: "Review this PR for security issues"
    anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
    custom_instructions: "Follow our coding standards"
    max_turns: "10"
    model: "claude-sonnet-4-6"
```

**GA v1.0（新）：**

```yaml
- uses: anthropics/claude-code-action@v1
  with:
    prompt: "Review this PR for security issues"
    anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
    claude_args: |
      --append-system-prompt "Follow our coding standards"
      --max-turns 10
      --model claude-sonnet-4-6
```

!!! tip

    Action 现在根据配置自动检测运行模式：响应 `@claude` 提及时运行**交互模式**，直接使用 `prompt` 时运行**自动化模式**。

## 配置示例

### 基本工作流：响应 @claude 提及

最简单的用法，监听评论中的 `@claude` 并自动响应：

```yaml
name: Claude Code
on:
  issue_comment:
    types: [created]
  pull_request_review_comment:
    types: [created]
jobs:
  claude:
    runs-on: ubuntu-latest
    steps:
      - uses: anthropics/claude-code-action@v1
        with:
          anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
```

### 自定义自动化：代码审查

在每次 PR 打开或更新时自动审查代码质量：

```yaml
name: Code Review
on:
  pull_request:
    types: [opened, synchronize]
jobs:
  review:
    runs-on: ubuntu-latest
    steps:
      - uses: anthropics/claude-code-action@v1
        with:
          anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
          prompt: "Review this pull request for code quality, correctness, and security. Analyze the diff, then post your findings as review comments."
          claude_args: "--max-turns 5"
```

### 日程触发：每日报告

每天早上 9 点自动生成昨日提交和 Issue 的汇总：

```yaml
name: Daily Report
on:
  schedule:
    - cron: "0 9 * * *"
jobs:
  report:
    runs-on: ubuntu-latest
    steps:
      - uses: anthropics/claude-code-action@v1
        with:
          anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
          prompt: "Generate a summary of yesterday's commits and open issues"
          claude_args: "--model opus"
```

### 统一配置格式

所有场景下，Action 的核心配置结构相同：

```yaml
- uses: anthropics/claude-code-action@v1
  with:
    anthropic_api_key: ${{ secrets.ANTHROPIC_API_KEY }}
    prompt: "Your instructions here"       # 可选，省略时响应 @claude
    claude_args: "--max-turns 5"           # 可选，CLI 参数
```

关键特性：

- **统一提示接口**：所有指令通过 `prompt` 传递
- **Skills 支持**：可以直接在提示中调用已安装的 [Skills](../skills/index.md)
- **CLI 参数透传**：通过 `claude_args` 传递任意 Claude Code CLI 参数
- **灵活触发器**：适用于任何 GitHub 事件

## Action 参数参考

| 参数 | 说明 | 必需 |
|------|------|------|
| `prompt` | Claude 的指令（纯文本或 skill 名称） | 否* |
| `claude_args` | 传递给 Claude Code 的 CLI 参数 | 否 |
| `anthropic_api_key` | Claude API 密钥 | 是** |
| `github_token` | 用于 API 访问的 GitHub 令牌 | 否 |
| `trigger_phrase` | 自定义触发短语（默认 `@claude`） | 否 |
| `use_bedrock` | 使用 Amazon Bedrock 代替 Claude API | 否 |
| `use_vertex` | 使用 Google Vertex AI 代替 Claude API | 否 |

\* `prompt` 可选——省略时 Claude 响应触发短语（如 `@claude`）
\*\* Claude API 直连时必需，Bedrock/Vertex 时不需

### 常用 CLI 参数

通过 `claude_args` 传递：

```yaml
claude_args: "--max-turns 5 --model claude-sonnet-4-6 --mcp-config /path/to/config.json"
```

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `--max-turns` | 最大对话轮数 | 10 |
| `--model` | 使用的模型 | `claude-sonnet-4-6` |
| `--mcp-config` | MCP 配置文件路径 | - |
| `--allowedTools` | 允许的工具列表（逗号分隔） | - |
| `--debug` | 启用调试输出 | 关闭 |

## 使用 Amazon Bedrock 和 Google Vertex AI

如果你的企业需要控制数据驻留和计费，可以将 Claude Code GitHub Actions 与自有云基础设施配合使用。

!!! note "前置条件"

    - **Amazon Bedrock**：已启用 Bedrock 并可访问 Claude 模型、配置了 GitHub OIDC、创建了具有 Bedrock 权限的 IAM 角色
    - **Google Vertex AI**：已启用 Vertex AI API、配置了 Workload Identity Federation、创建了具有 Vertex AI 权限的服务账户

### 创建自定义 GitHub App

使用第三方提供商时，建议创建自定义 GitHub App 以获得最佳控制：

1. 访问 [https://github.com/settings/apps/new](https://github.com/settings/apps/new)
2. 填写基本信息（名称、主页 URL）
3. **Webhooks** 取消勾选 "Active"（此集成不需要 Webhook）
4. 配置仓库权限：Contents 读写、Issues 读写、Pull requests 读写
5. 创建后，点击 **Generate a private key**，保存下载的 `.pem` 文件
6. 记下 App ID
7. 将 App 安装到目标仓库
8. 在仓库 Secrets 中添加 `APP_PRIVATE_KEY`（`.pem` 文件内容）和 `APP_ID`

!!! tip

    自定义 App 通过 `actions/create-github-app-token` action 在工作流中生成身份验证令牌。如果你不想自定义 App，也可以直接使用官方 Anthropic App。

### Amazon Bedrock 工作流

配置 AWS OIDC 让 GitHub Actions 无需存储静态密钥即可安全认证：

```yaml title=".github/workflows/claude-bedrock.yml"
name: Claude PR Action

permissions:
  contents: write
  pull-requests: write
  issues: write
  id-token: write

on:
  issue_comment:
    types: [created]
  pull_request_review_comment:
    types: [created]
  issues:
    types: [opened, assigned]

jobs:
  claude-pr:
    if: |
      (github.event_name == 'issue_comment' && contains(github.event.comment.body, '@claude')) ||
      (github.event_name == 'pull_request_review_comment' && contains(github.event.comment.body, '@claude')) ||
      (github.event_name == 'issues' && contains(github.event.issue.body, '@claude'))
    runs-on: ubuntu-latest
    env:
      AWS_REGION: us-west-2
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Generate GitHub App token
        id: app-token
        uses: actions/create-github-app-token@v2
        with:
          app-id: ${{ secrets.APP_ID }}
          private-key: ${{ secrets.APP_PRIVATE_KEY }}

      - name: Configure AWS Credentials (OIDC)
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_TO_ASSUME }}
          aws-region: us-west-2

      - uses: anthropics/claude-code-action@v1
        with:
          github_token: ${{ steps.app-token.outputs.token }}
          use_bedrock: "true"
          claude_args: '--model us.anthropic.claude-sonnet-4-6 --max-turns 10'
```

需要的 GitHub Secrets：

| 密钥名称 | 说明 |
|---------|------|
| `AWS_ROLE_TO_ASSUME` | Bedrock 访问的 IAM 角色 ARN |
| `APP_ID` | 你的 GitHub App ID |
| `APP_PRIVATE_KEY` | GitHub App 的私钥 |

!!! tip

    Bedrock 的模型 ID 包含区域前缀，例如 `us.anthropic.claude-sonnet-4-6`。

### Google Vertex AI 工作流

通过 Workload Identity Federation 实现 GCP 无密钥认证：

```yaml title=".github/workflows/claude-vertex.yml"
name: Claude PR Action

permissions:
  contents: write
  pull-requests: write
  issues: write
  id-token: write

on:
  issue_comment:
    types: [created]
  pull_request_review_comment:
    types: [created]
  issues:
    types: [opened, assigned]

jobs:
  claude-pr:
    if: |
      (github.event_name == 'issue_comment' && contains(github.event.comment.body, '@claude')) ||
      (github.event_name == 'pull_request_review_comment' && contains(github.event.comment.body, '@claude')) ||
      (github.event_name == 'issues' && contains(github.event.issue.body, '@claude'))
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Generate GitHub App token
        id: app-token
        uses: actions/create-github-app-token@v2
        with:
          app-id: ${{ secrets.APP_ID }}
          private-key: ${{ secrets.APP_PRIVATE_KEY }}

      - name: Authenticate to Google Cloud
        id: auth
        uses: google-github-actions/auth@v2
        with:
          workload_identity_provider: ${{ secrets.GCP_WORKLOAD_IDENTITY_PROVIDER }}
          service_account: ${{ secrets.GCP_SERVICE_ACCOUNT }}

      - uses: anthropics/claude-code-action@v1
        with:
          github_token: ${{ steps.app-token.outputs.token }}
          use_vertex: "true"
          claude_args: '--model claude-sonnet-4-5@20250929 --max-turns 10'
        env:
          ANTHROPIC_VERTEX_PROJECT_ID: ${{ steps.auth.outputs.project_id }}
          CLOUD_ML_REGION: us-east5
```

需要的 GitHub Secrets：

| 密钥名称 | 说明 |
|---------|------|
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | 工作负载身份提供商资源名称 |
| `GCP_SERVICE_ACCOUNT` | 具有 Vertex AI 权限的服务账户 |
| `APP_ID` | 你的 GitHub App ID |
| `APP_PRIVATE_KEY` | GitHub App 的私钥 |

## 安全考虑和最佳实践

### 密钥管理

!!! warning "永远不要将 API 密钥提交到仓库"

    始终使用 GitHub Secrets：

    - 将 API 密钥添加为名为 `ANTHROPIC_API_KEY` 的仓库密钥
    - 在工作流中通过 `${{ secrets.ANTHROPIC_API_KEY }}` 引用
    - 将 Action 权限限制为仅必要的范围
    - 合并前审查 Claude 的所有建议

### CLAUDE.md 配置

在仓库根目录创建 `CLAUDE.md` 文件，定义代码风格、审查标准、项目规则和首选模式。Claude 在创建 PR 和响应请求时会遵循这些指南。

### 优化性能

- 使用 Issue 模板提供充分的上下文
- 保持 `CLAUDE.md` 简洁聚焦
- 为工作流配置合理的超时时间
- 通过 `--max-turns` 防止过度迭代

### CI 成本控制

使用 Claude Code GitHub Actions 涉及两方面成本：

**GitHub Actions 成本**：Claude Code 运行在 GitHub 托管的 Runner 上，消耗 Actions 分钟数。

**API 成本**：每次交互根据提示和响应长度消耗 API Token。

成本优化建议：

- 使用明确的 `@claude` 指令减少不必要的 API 调用
- 配置适当的 `--max-turns` 防止失控迭代
- 设置工作流级超时
- 利用 GitHub 的并发控制限制并行运行

## 故障排除

### Claude 不响应 @claude 命令

检查以下几点：

- GitHub App 是否已正确安装到目标仓库
- 工作流是否已启用（仓库 Settings -> Actions）
- `ANTHROPIC_API_KEY` 是否已正确设置在仓库 Secrets 中
- 评论中是否包含 `@claude`（注意是 `@` 而不是 `/`）

### CI 不在 Claude 的提交上运行

- 确认使用的是 GitHub App 或自定义 App 令牌，而不是 Actions 默认用户
- 检查工作流触发器是否包含 `push` 等必要事件
- 验证 App 权限是否包含触发 CI 的能力

### 身份验证错误

- **Claude API**：确认 `ANTHROPIC_API_KEY` 有效且未过期
- **Bedrock / Vertex**：检查 OIDC / WIF 配置、角色模拟权限、密钥名称是否正确
