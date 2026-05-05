---
title: GitLab CI/CD
description: 在 GitLab CI/CD 中集成 Claude Code 实现自动化工作流
---

**本文你会学到**：

- 如何在 GitLab CI/CD 中快速集成 Claude Code
- 使用 Amazon Bedrock 和 Google Vertex AI 的 OIDC 无密钥认证
- 常见用例的配置示例
- 安全治理和成本控制的实践建议

## 为什么在 GitLab 中用 Claude Code？

当你在 GitLab 上管理项目时，可能会遇到这样的问题：Issue 描述了明确的需求，但没人手实现；MR 等了很久没人审查；测试失败需要快速定位修复。Claude Code 集成到 GitLab CI/CD 后，这些问题可以自动解决。

核心价值：

- **即时 MR 创建**：描述需求，Claude 自动提议完整的 MR
- **自动化实现**：通过单个命令或 `@claude` 提及将 Issue 转化为可工作的代码
- **项目感知**：Claude 遵循 `CLAUDE.md` 中的指南和现有代码模式
- **简单设置**：一个 CI 作业 + 一个掩码变量即可启动
- **企业就绪**：支持 Claude API、Amazon Bedrock、Google Vertex AI
- **默认安全**：在你的 GitLab Runner 上运行，遵守分支保护和审批规则

!!! info "研究预览"

    Claude Code for GitLab CI/CD 目前处于测试阶段，功能和特性可能随版本更新变化。此集成由 GitLab 维护，如需支持请参阅 [GitLab issue](https://gitlab.com/gitlab-org/gitlab/-/issues/573776)。

!!! note "底层技术"

    此集成基于 [Claude Code CLI and Agent SDK](../agent-sdk/index.md) 构建，可以在 CI/CD 作业和自定义自动化工作流中以编程方式使用 Claude。

## 工作原理

Claude Code 使用 GitLab CI/CD 在隔离的容器中运行 AI 任务，结果通过 MR 提交回来。整个过程分为三个阶段：

### 事件驱动的编排

GitLab 监听你选择的触发器（例如评论中的 `@claude`、MR 事件、手动触发）。作业从线程和仓库收集上下文，构建提示，运行 Claude Code。

### 提供商抽象

选择适合你环境的 AI 提供商：

| 提供商 | 认证方式 | 适用场景 |
|--------|---------|---------|
| Claude API (SaaS) | `ANTHROPIC_API_KEY` | 快速上手，无需云基础设施 |
| Amazon Bedrock | OIDC + IAM 角色 | AWS 生态，数据驻留需求 |
| Google Vertex AI | Workload Identity Federation | GCP 生态，数据驻留需求 |

### 沙箱执行

每次交互都在具有严格网络和文件系统规则的容器中运行。Claude Code 强制执行工作区范围的权限限制写入操作。每项变更都通过 MR 流动，审查者可以看到完整的 diff，现有的分支保护和审批规则仍然有效。

## Claude 能做什么？

Claude Code 支持以下 CI/CD 工作流：

- 从问题描述或评论创建和更新 MR
- 分析性能回归并提议优化
- 直接在分支中实现功能，然后打开 MR
- 修复测试或评论中识别的 Bug 和回归
- 响应后续评论以迭代修改

实际使用示例：

```text
@claude implement this feature based on the issue description
```

Claude 分析 Issue 和代码库，在分支中编写变更，并打开 MR 供审查。

```text
@claude suggest a concrete approach to cache the results of this API call
```

Claude 提议更改、添加缓存实现代码，并更新 MR。

```text
@claude fix the TypeError in the user dashboard component
```

Claude 定位错误、实现修复，并更新分支或打开新 MR。

## 快速设置

最快的入门方式是向 `.gitlab-ci.yml` 添加一个最小作业，并将 API 密钥设置为掩码变量。

### 添加掩码 CI/CD 变量

1. 进入 **Settings** -> **CI/CD** -> **Variables**
2. 添加 `ANTHROPIC_API_KEY`（勾选 Mask，按需勾选 Protected）

### 添加 Claude 作业

```yaml title=".gitlab-ci.yml"
stages:
  - ai

claude:
  stage: ai
  image: node:24-alpine3.21
  rules:
    - if: '$CI_PIPELINE_SOURCE == "web"'
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
  variables:
    GIT_STRATEGY: fetch
  before_script:
    - apk update
    - apk add --no-cache git curl bash
    - curl -fsSL https://claude.ai/install.sh | bash
  script:
    # 启动内置 GitLab MCP 服务器（可选）
    - /bin/gitlab-mcp-server || true
    # Claude Code 执行 AI 任务
    - >
      claude
      -p "${AI_FLOW_INPUT:-'Review this MR and implement the requested changes'}"
      --permission-mode acceptEdits
      --allowedTools "Bash Read Edit Write mcp__gitlab"
      --debug
```

添加后，从 **CI/CD** -> **Pipelines** 手动运行作业进行测试，或从 MR 事件触发。

!!! note "企业提供商"

    如果需要使用 Amazon Bedrock 或 Google Vertex AI，请参考后文的[企业提供商配置](#使用-amazon-bedrock-和-google-vertex-ai)部分。

## 手动设置（建议用于生产）

### 配置提供商访问

| 提供商 | 认证配置 |
|--------|---------|
| Claude API | 将 `ANTHROPIC_API_KEY` 存储为掩码 CI/CD 变量 |
| Amazon Bedrock | 配置 GitLab -> AWS OIDC，创建具有 Bedrock 权限的 IAM 角色 |
| Google Vertex AI | 配置 GitLab -> GCP Workload Identity Federation |

### 添加 GitLab API 凭证

- 默认使用 `CI_JOB_TOKEN`（自动注入）
- 如需更广泛的权限，创建具有 `api` 范围的项目访问令牌，存储为 `GITLAB_ACCESS_TOKEN`（掩码）

### 可选：启用提及驱动的触发器

1. 为 "Comments (notes)" 添加项目 webhook 到你的事件监听器
2. 当评论包含 `@claude` 时，让监听器使用 `AI_FLOW_INPUT` 和 `AI_FLOW_CONTEXT` 变量调用 Pipeline 触发 API

## 配置示例

### 基本 CI/CD 配置（Claude API）

最简单的配置，适用于 Claude API 直连：

```yaml title=".gitlab-ci.yml"
stages:
  - ai

claude:
  stage: ai
  image: node:24-alpine3.21
  rules:
    - if: '$CI_PIPELINE_SOURCE == "web"'
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
  variables:
    GIT_STRATEGY: fetch
  before_script:
    - apk update
    - apk add --no-cache git curl bash
    - curl -fsSL https://claude.ai/install.sh | bash
  script:
    - /bin/gitlab-mcp-server || true
    - >
      claude
      -p "${AI_FLOW_INPUT:-'Summarize recent changes and suggest improvements'}"
      --permission-mode acceptEdits
      --allowedTools "Bash Read Edit Write mcp__gitlab"
      --debug
  # Claude Code 会自动使用 CI/CD 变量中的 ANTHROPIC_API_KEY
```

### Amazon Bedrock 作业（OIDC）

通过 OIDC 交换 GitLab JWT 令牌获取临时 AWS 凭证，无需存储静态密钥：

```yaml title=".gitlab-ci.yml（Bedrock 片段）"
claude-bedrock:
  stage: ai
  image: node:24-alpine3.21
  rules:
    - if: '$CI_PIPELINE_SOURCE == "web"'
  before_script:
    - apk add --no-cache bash curl jq git python3 py3-pip
    - pip install --no-cache-dir awscli
    - curl -fsSL https://claude.ai/install.sh | bash
    # 交换 GitLab OIDC 令牌获取 AWS 凭证
    - export AWS_WEB_IDENTITY_TOKEN_FILE="${CI_JOB_JWT_FILE:-/tmp/oidc_token}"
    - if [ -n "${CI_JOB_JWT_V2}" ]; then printf "%s" "$CI_JOB_JWT_V2" > "$AWS_WEB_IDENTITY_TOKEN_FILE"; fi
    - >
      aws sts assume-role-with-web-identity
      --role-arn "$AWS_ROLE_TO_ASSUME"
      --role-session-name "gitlab-claude-$(date +%s)"
      --web-identity-token "file://$AWS_WEB_IDENTITY_TOKEN_FILE"
      --duration-seconds 3600 > /tmp/aws_creds.json
    - export AWS_ACCESS_KEY_ID="$(jq -r .Credentials.AccessKeyId /tmp/aws_creds.json)"
    - export AWS_SECRET_ACCESS_KEY="$(jq -r .Credentials.SecretAccessKey /tmp/aws_creds.json)"
    - export AWS_SESSION_TOKEN="$(jq -r .Credentials.SessionToken /tmp/aws_creds.json)"
  script:
    - /bin/gitlab-mcp-server || true
    - >
      claude
      -p "${AI_FLOW_INPUT:-'Implement the requested changes and open an MR'}"
      --permission-mode acceptEdits
      --allowedTools "Bash Read Edit Write mcp__gitlab"
      --debug
  variables:
    AWS_REGION: "us-west-2"
```

需要的 CI/CD 变量：

| 变量 | 说明 |
|------|------|
| `AWS_ROLE_TO_ASSUME` | Bedrock 访问的 IAM 角色 ARN |
| `AWS_REGION` | Bedrock 区域（如 `us-west-2`） |

!!! tip

    Bedrock 的模型 ID 包含区域前缀，例如 `us.anthropic.claude-sonnet-4-6`。

### Google Vertex AI 作业（Workload Identity Federation）

通过 WIF 向 Google Cloud 认证，无需下载服务账户密钥：

```yaml title=".gitlab-ci.yml（Vertex AI 片段）"
claude-vertex:
  stage: ai
  image: gcr.io/google.com/cloudsdktool/google-cloud-cli:slim
  rules:
    - if: '$CI_PIPELINE_SOURCE == "web"'
  before_script:
    - apt-get update && apt-get install -y git && apt-get clean
    - curl -fsSL https://claude.ai/install.sh | bash
    # 通过 WIF 向 Google Cloud 认证
    - >
      gcloud auth login --cred-file=<(cat <<EOF
      {
        "type": "external_account",
        "audience": "${GCP_WORKLOAD_IDENTITY_PROVIDER}",
        "subject_token_type": "urn:ietf:params:oauth:token-type:jwt",
        "service_account_impersonation_url": "https://iamcredentials.googleapis.com/v1/projects/-/serviceAccounts/${GCP_SERVICE_ACCOUNT}:generateAccessToken",
        "token_url": "https://sts.googleapis.com/v1/token"
      }
      EOF
      )
    - gcloud config set project "$(gcloud projects list --format='value(projectId)' --filter="name:${CI_PROJECT_NAMESPACE}" | head -n1)" || true
  script:
    - /bin/gitlab-mcp-server || true
    - >
      CLOUD_ML_REGION="${CLOUD_ML_REGION:-us-east5}"
      claude
      -p "${AI_FLOW_INPUT:-'Review and update code as requested'}"
      --permission-mode acceptEdits
      --allowedTools "Bash Read Edit Write mcp__gitlab"
      --debug
  variables:
    CLOUD_ML_REGION: "us-east5"
```

需要的 CI/CD 变量：

| 变量 | 说明 |
|------|------|
| `GCP_WORKLOAD_IDENTITY_PROVIDER` | 完整的提供商资源名称 |
| `GCP_SERVICE_ACCOUNT` | 具有 Vertex AI 权限的服务账户 |
| `CLOUD_ML_REGION` | Vertex 区域（如 `us-east5`） |

## 使用 Amazon Bedrock 和 Google Vertex AI

### Amazon Bedrock OIDC 配置

配置 AWS 允许 GitLab CI 作业通过 OIDC 假设 IAM 角色：

1. 启用 Amazon Bedrock 并请求访问目标 Claude 模型
2. 为 GitLab 创建 IAM OIDC 提供商
3. 创建由 GitLab OIDC 提供商信任的 IAM 角色，限制为你的项目和受保护的 refs
4. 为 Bedrock API 调用附加最小权限策略

存储为 CI/CD 变量：`AWS_ROLE_TO_ASSUME`、`AWS_REGION`

### Google Vertex AI OIDC 配置

配置 Google Cloud 允许 GitLab CI 作业通过 Workload Identity Federation 模拟服务账户：

1. 启用 IAM Credentials API、STS API 和 Vertex AI API
2. 为 GitLab OIDC 创建 Workload Identity Pool 和提供商
3. 创建仅具有 Vertex AI 角色的专用服务账户
4. 授予 WIF 主体权限以模拟服务账户

存储为 CI/CD 变量：`GCP_WORKLOAD_IDENTITY_PROVIDER`、`GCP_SERVICE_ACCOUNT`、`CLOUD_ML_REGION`

## 常见参数和变量

Claude Code 在 CI/CD 中支持以下常用输入：

| 参数 / 变量 | 说明 |
|------------|------|
| `prompt` / `prompt_file` | 内联提供指令（`-p`）或通过文件 |
| `max_turns` | 限制来回迭代次数 |
| `timeout_minutes` | 限制总执行时间 |
| `ANTHROPIC_API_KEY` | Claude API 所需（Bedrock/Vertex 不需要） |
| `AI_FLOW_INPUT` | 从 webhook/API 触发器传入的上下文提示 |
| `AI_FLOW_CONTEXT` | 触发上下文信息 |
| `AI_FLOW_EVENT` | 事件类型 |

## 自定义 Claude 的行为

通过两种方式指导 Claude：

### CLAUDE.md

在仓库根目录创建 `CLAUDE.md` 文件，定义编码标准、安全要求和项目约定。Claude 在运行期间读取此文件并遵循你的规则。

### 自定义提示

通过作业中的 `-p` 参数传递任务特定的指令。不同作业使用不同提示，例如：

- 审查作业：`-p "Review this MR for security issues"`
- 实现作业：`-p "Implement the feature described in the issue"`
- 重构作业：`-p "Refactor this module to improve testability"`

## 安全和治理

### 安全保障

- 每个作业在具有受限网络访问的隔离容器中运行
- Claude 的变更通过 MR 流动，审查者可以看到每个 diff
- 分支保护和审批规则对 AI 生成的代码同样有效
- Claude Code 使用工作区范围的权限限制写入范围
- 成本在你的控制下——你提供自己的提供商凭证

### 密钥安全

!!! warning "永远不要将 API 密钥或云凭证提交到仓库"

    始终使用 GitLab CI/CD 变量：

    - 将 `ANTHROPIC_API_KEY` 添加为掩码变量（需要时勾选 Protected）
    - 优先使用提供商 OIDC 认证（无长期密钥）
    - 限制作业权限和网络出口
    - 像审查任何其他贡献者一样审查 Claude 的 MR

### CLAUDE.md 配置

在仓库根目录创建 `CLAUDE.md` 文件，定义编码标准和项目特定规则。Claude 在运行期间读取此文件，并在提议变更时遵循你的约定。

### 成本控制

使用 Claude Code in GitLab CI/CD 涉及两方面成本：

**GitLab Runner 时间**：Claude 在你的 Runner 上运行并消耗计算分钟数。

**API 成本**：每次交互根据提示和响应大小消耗 Token。

成本优化建议：

- 使用明确的 `@claude` 命令减少不必要的轮次
- 设置适当的 `max_turns` 和作业超时
- 限制并发以控制并行运行
- 在 Runner 中缓存 npm 和包安装

## 故障排除

### Claude 不响应 @claude 命令

- 验证 Pipeline 是否被触发（手动、MR 事件或通过注释事件监听器）
- 确保 CI/CD 变量（`ANTHROPIC_API_KEY` 或云提供商设置）存在且未掩码
- 检查评论是否包含 `@claude`（不是 `/claude`）以及你的提及触发器是否已配置

### 作业无法写入评论或打开 MR

- 确保 `CI_JOB_TOKEN` 对项目具有足够的权限，或使用具有 `api` 范围的项目访问令牌
- 检查 `mcp__gitlab` 工具是否在 `--allowedTools` 中启用
- 确认作业在 MR 的上下文中运行，或通过 `AI_FLOW_*` 变量有足够的上下文

### 身份验证错误

- **Claude API**：确认 `ANTHROPIC_API_KEY` 有效且未过期
- **Bedrock / Vertex**：验证 OIDC / WIF 配置、角色模拟和密钥名称；确认区域和模型可用性
