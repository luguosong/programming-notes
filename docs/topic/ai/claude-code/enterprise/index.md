---
title: 企业管理
description: 托管设置、认证方式、使用监控、安全架构、法律合规与零数据保留的完整指南
---

# 企业管理

个人开发者用 Claude Code，就像独自开车——想怎么开就怎么开。但在企业里，情况完全不同：你是车队管理员，需要统一设定车速上限、指定加油站、记录每辆车的行驶轨迹，还得确保油桶不会泄露。Claude Code 的企业级管理功能，就是为"车队"准备的。

企业管理本质上是一连串决策：选哪个 API 提供商（决定计费和合规体系）→ 策略怎么到达开发者设备（云端推送还是 MDM）→ 要强制执行什么规则（权限、沙箱、插件限制）→ 怎么追踪用量和成本。本文按此顺序组织，帮你逐步构建完整的企业治理体系。

**本文你会学到**：

- 四种托管设置的传递机制与平台路径
- 故障关闭行为、安全审批对话框与审计日志
- 策略执行的八大控制面——权限、沙箱、MCP、插件、Hook 等
- 企业环境中可用的认证方式与选型思路
- OpenTelemetry 监控体系与分析仪表盘的使用方法
- Claude Code 的安全架构——权限模型、沙箱、注入防御
- 企业网络配置——代理、CA 证书、mTLS 与域名白名单
- 法律合规与零数据保留——BAA/ZDR 关系、数据使用政策、安全漏洞报告
- GitHub Enterprise Server 集成的配置与限制

## 🏢 托管设置（Managed Settings）

企业里有几十甚至几百个开发者使用 Claude Code，总不能挨个去改配置。托管设置就是用来解决"统一管控"问题的——管理员在一个地方配置策略，所有开发者的 Claude Code 自动生效。现已支持 `managed-settings.d/` 目录，可将策略拆分为多个文件便于 MDM 管理（v2.1.83 新增）。

打个比方：托管设置就像公司的门禁系统。你不需要给每个人单独发钥匙（本地配置），而是在总控室统一设定谁能进哪扇门（托管设置），员工刷卡时自动匹配权限。

### Server-managed Settings：云端统一策略

Server-managed Settings 由 Anthropic 服务器在用户认证时自动下发，是**最常用**的托管方式。管理员在 Claude 组织后台配置，开发者完全无感。

核心工作流：

1. 管理员在 Claude 组织设置中编写 JSON 策略
2. 开发者通过 `claude auth login`（OAuth）登录
3. Anthropic 服务器在认证响应中下发策略
4. Claude Code 自动应用策略，开发者无法覆盖

??? tip "开发者能绕过吗？"

    不能。Server-managed Settings 的优先级**高于**用户本地配置和项目级 `CLAUDE.md`。开发者无法通过修改本地文件来绕过组织策略。不过部分设置标注为 `userOverridable`，这类设置允许开发者根据自己的需求微调。

### Endpoint-managed Settings：终端设备策略

Endpoint-managed Settings 通过**设备管理工具（MDM）** 将策略直接部署到开发者机器上，不经过 Anthropic 服务器，适合对安全控制有更严格要求的场景。Claude Code 在设备上查找托管设置时，按以下优先级使用找到的**第一个**：

| 优先级 | 机制 | 平台 | 策略位置 |
|--------|------|------|---------|
| 1（最高） | plist / 注册表策略 | macOS | `com.anthropic.claudecode` 托管偏好设置 |
| | | Windows | `HKLM\SOFTWARE\Policies\ClaudeCode` |
| 2 | 文件策略 | macOS | `/Library/Application Support/ClaudeCode/managed-settings.json` |
| | | Linux / WSL | `/etc/claude-code/managed-settings.json` |
| | | Windows | `C:\Program Files\ClaudeCode\managed-settings.json` |
| 3（最低） | Windows 用户注册表 | 仅 Windows | `HKCU\SOFTWARE\Policies\ClaudeCode` |

??? tip "各机制的安全性差异"

    - **plist / HKLM 注册表**：需要管理员权限才能写入，抗篡改能力强，适合作为主要执行通道
    - **文件策略**：同样需要管理员权限写入系统目录，通用性最好（支持所有平台）
    - **HKCU 注册表**：无需提升权限即可写入，仅作为便利默认值，不应作为强制执行通道

v2.1.83 新增 `managed-settings.d/` 分片配置目录，可以将策略拆分为多个文件便于 MDM 管理。

### 故障关闭与强制刷新

默认情况下，如果 Claude Code 启动时无法从 Anthropic 服务器获取托管设置，CLI 会继续运行，只是暂时不执行策略。这个短暂的"无策略窗口"在大多数场景下可以接受，但对于合规要求严格的企业来说可能是个问题。

在托管设置中启用 `forceRemoteSettingsRefresh: true` 后，行为发生根本变化：

```json
{
  "forceRemoteSettingsRefresh": true
}
```

启用后的行为：CLI 启动时会阻塞等待远程设置获取完成。如果获取失败，CLI 直接退出，而不是在没有策略保护的状态下继续运行。这个设置是"自我延续"的——一旦从服务器下发，它也会被本地缓存，后续启动即使在新会话首次成功获取之前，也会强制执行同样的行为。

??? warning "启用前确认网络可达"

    启用 `forceRemoteSettingsRefresh` 前，必须确保你的网络策略允许访问 `api.anthropic.com`。如果该端点不可达，CLI 在启动时直接退出，开发者将完全无法使用 Claude Code。

### 安全审批对话框

管理员通过 Server-managed Settings 下发的某些设置可能带来安全风险，Claude Code 在应用这些设置前会要求开发者明确批准。触发安全审批对话框的设置类型包括：

| 设置类型 | 示例 |
|---------|------|
| Shell 命令设置 | 执行任意 shell 命令的配置 |
| 自定义环境变量 | 不在已知安全白名单中的变量 |
| Hook 配置 | 任何 hook 定义 |

当这些设置存在时，用户会看到一个安全对话框，解释正在配置的内容。用户必须批准才能继续；如果拒绝，Claude Code 会退出。

??? note "非交互模式下的行为"

    在使用 `-p` 标志的非交互模式下，Claude Code 跳过安全对话框，直接应用设置而无需用户批准。这个设计是为了保证 CI/CD 等自动化场景不会因审批流程而中断。

### 审计日志

设置变更的审计日志可通过合规 API 或审计日志导出获得。审计事件记录了执行的操作类型、执行操作的账户和设备，以及对先前值和新值的引用。如果你的企业需要追踪"谁在什么时候改了什么策略"，审计日志是不可缺少的能力。

??? tip "如何获取审计日志"

    联系你的 Anthropic 账户团队以获取审计日志的访问权限。审计日志适用于 Claude for Teams 和 Enterprise 计划。

### 混合提供商的配置策略

如果组织内混合使用多个 API 提供商（部分人用 Claude.ai OAuth，部分人用 Bedrock），建议：

- Claude.ai 用户配置 **Server-managed Settings**
- 其他提供商用户配置 **File-based 或 plist/registry 回退**

这样无论开发者使用哪个提供商，都能收到组织策略。

### 权限规则合并规则

无论策略通过哪种机制传递，托管值都**优先于**用户和项目设置。但数组设置（如 `permissions.allow` 和 `permissions.deny`）会**合并**来自所有源的条目——开发者可以扩展托管列表，但不能从中删除。

### 常用策略配置示例

以下是一些典型的企业策略场景：

```json
{
  "allowedTools": ["Read", "Edit", "Bash", "Glob"],
  "disallowedTools": ["Write"],
  "maxTurns": 50,
  "contextWindow": "128k"
}
```

💡 这段策略做了三件事：白名单限制可用工具、禁止直接写入新文件、限制单次对话轮数。企业可以根据自身安全要求灵活组合。

### 策略优先级总览

当同一设置在多个来源中都有定义时，Claude Code 按以下优先级决定最终值：

| 优先级 | 来源 | 传递方式 |
|--------|------|---------|
| 1（最高） | Server-managed | Anthropic 服务器认证时下发，活跃会话中每小时刷新 |
| 2 | plist / HKLM 注册表 | 通过 MDM 部署，需管理员权限 |
| 3 | File-based managed | 系统目录下的 JSON 文件 |
| 4 | Windows 用户注册表 | HKCU，无需管理员权限 |
| 5 | CLI 参数 | 用户启动时临时指定 |
| 6 | 本地设置 | `.claude/settings.local.json` |
| 7 | 项目设置 | `.claude/settings.json` |
| 8（最低） | 用户设置 | `~/.claude/settings.json` |

Server-managed Settings 需要 Claude for Teams 或 Enterprise 计划。使用其他提供商（Bedrock、Vertex、Foundry）的部署需要改用 Endpoint-managed 机制。

## 🔒 策略执行控制面

托管设置确定了策略"怎么传到设备"，接下来要决定"管什么"。Claude Code 提供了八大控制面，每一行对应一组设置键。

| 控制面 | 作用 | 关键设置键 |
|--------|------|-----------|
| [权限规则](../configuration/settings-permissions/index.md) | 允许、询问或拒绝特定工具和命令 | `permissions.allow`、`permissions.deny` |
| 权限锁定 | 仅托管权限规则生效；禁用 `--dangerously-skip-permissions` | `allowManagedPermissionRulesOnly`、`permissions.disableBypassPermissionsMode` |
| [沙箱隔离](../configuration/settings-permissions/index.md#_6) | 操作系统级文件系统和网络隔离 | `sandbox.enabled`、`sandbox.network.allowedDomains`、`sandbox.network.deniedDomains` |
| [托管 CLAUDE.md](../configuration/claude-md/index.md) | 每个会话加载的组织级指令，无法排除 | 托管策略路径下的文件 |
| [MCP 服务器控制](../mcp/index.md) | 限制用户可连接的 MCP 服务器 | `allowedMcpServers`、`deniedMcpServers`、`allowManagedMcpServersOnly` |
| [插件市场控制](../plugins/index.md) | 限制用户可安装的市场来源 | `strictKnownMarketplaces`、`blockedMarketplaces` |
| Hook 限制 | 仅托管 Hook 加载；限制 HTTP Hook URL | `allowManagedHooksOnly`、`allowedHttpHookUrls` |
| 版本底线 | 阻止低于组织最低版本号的自动更新 | `minimumVersion` |

??? tip "权限规则 vs 沙箱：两层防护各管什么？"

    它们覆盖不同的层。拒绝 `WebFetch` 会阻止 Claude 的 fetch 工具，但如果 `Bash` 是允许的，`curl` 和 `wget` 仍然可以访问任何 URL。沙箱通过操作系统级别的网络域允许列表弥补这一差距。两层配合形成纵深防御。

    ```json
    {
      "permissions": {
        "deny": ["WebFetch"]
      },
      "sandbox": {
        "enabled": true,
        "network": {
          "allowedDomains": ["api.example.com", "registry.npmjs.org"]
        }
      }
    }
    ```

    这段策略同时从应用层和 OS 层限制了网络访问。

## 🔐 认证方式

企业使用 Claude Code 的第一步，是让开发者能安全、合规地登录。Claude Code 支持多种认证方式，不同方式的安全等级和使用体验各异。

### OAuth 认证（推荐企业使用）

OAuth 是企业环境下的推荐方式，类似你用"微信登录"第三方应用——开发者跳转到 Anthropic 的登录页面，输入企业账号密码，授权后 Claude Code 自动获取访问令牌。

```bash
# 启动 OAuth 登录流程
claude auth login
```

优势：令牌自动刷新、支持 MFA（多因素认证）、管理员可集中管理团队成员的访问权限。

### API Key

API Key 适合自动化场景或 CI/CD 流水线。开发者从 Anthropic Console 生成一个密钥，直接配置给 Claude Code。

```bash
# 设置 API Key
export ANTHROPIC_API_KEY=sk-ant-xxxxx
```

⚠️ 注意：API Key 是长期凭证，**不要提交到版本库**。生产环境中建议通过密钥管理服务注入，而非硬编码在脚本中。

### 云服务商认证

对于已在 AWS、GCP 等云平台上使用 Claude 模型的企业，可以通过云服务商的认证体系直接使用 Claude Code：

| 云服务商 | 认证方式 | 适用场景 |
|---------|---------|---------|
| Amazon Bedrock | AWS IAM 凭证 | AWS 原生企业 |
| Google Vertex AI | Google Cloud 凭证 | GCP 原生企业 |
| Anthropic Foundry | Foundry OAuth | 需要定制模型部署 |

### apiKeyHelper：凭证自动化获取

`apiKeyHelper` 是一个灵活的认证机制，让 Claude Code 从**外部命令**动态获取 API Key，而不是将密钥静态配置在环境变量中。

```json
{
  "apiKeyHelper": "aws secretsmanager get-secret-value --secret-id claude-api-key --query SecretString --output text"
}
```

💡 这就好比把钥匙放在保险柜里，每次需要时用指纹解锁取出，而不是把钥匙直接挂在门口。密钥轮换时只需更新保险柜里的内容，所有使用方自动生效。

## 📊 使用监控

企业需要回答两个关键问题：团队花了多少钱？谁在用、用了多少？Claude Code 通过 OpenTelemetry 协议提供了可观测性能力。

### OpenTelemetry 集成（Beta）

OpenTelemetry（简称 OTel）是云原生领域的标准可观测性框架。Claude Code 支持导出三类遥测数据：

| 数据类型 | 说明 | 示例 |
|---------|------|------|
| Metrics（指标） | 可聚合的数值度量 | Token 消耗量、请求数、延迟 |
| Events/Logs（事件日志） | 离散的事件记录 | 工具调用、错误发生、会话开始 |
| Traces（分布式追踪） | 请求的完整链路 | 一次对话从发起到完成的全过程 |

通过环境变量配置 OTel 导出目标：

```bash
# 指定 OTel 导出端点
export OTEL_EXPORTER_OTLP_ENDPOINT="http://otel-collector:4318"

# 启用所有遥测类型
export OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf"
export CLAUDE_CODE_USE_BETAS="otel"
```

💡 OTel 的好处在于**标准化**。企业已有的 Grafana、Datadog、Jaeger 等监控工具都能直接消费这些数据，无需为 Claude Code 搭建专用监控系统。

### 调试 API 请求体

在排查 Claude Code 的 API 调用问题时，通常只能看到摘要信息。设置 `OTEL_LOG_RAW_API_BODIES` 环境变量后（v2.1.111 新增），完整的 API 请求和响应体会作为 OpenTelemetry 日志事件输出，方便你在 OTel Collector 或日志分析工具中查看原始的请求/响应内容：

```bash
export OTEL_LOG_RAW_API_BODIES=1
```

⚠️ 这会显著增加日志量，建议仅在调试时开启，排查完毕后关闭。

### SDK 分布式追踪

当 Claude Code 通过 SDK 或 headless 模式在微服务架构中使用时，追踪请求链路至关重要。SDK/headless 会话现在会自动从环境变量读取 `TRACEPARENT` 和 `TRACESTATE` 用于分布式追踪链接（v2.1.110 改进），可以无缝融入现有的 OpenTelemetry 追踪体系。

### OTel 事件持续增强

v2.1.122 对 OpenTelemetry 事件做了多项增强：

- **数值属性类型修正**：`api_request`/`api_error` 日志事件的数值属性现在以数字类型而非字符串输出，方便在 Grafana 等工具中直接做数值聚合和告警
- **`@`-mention 事件**：新增 `claude_code.at_mention` 日志事件，追踪 `@`-mention 解析行为
- **`tool_use_id` 关联**：`tool_result` 和 `tool_decision` 事件新增 `tool_use_id`，可以将工具决策与工具结果精确关联
- **`tool_input_size_bytes`**：`tool_result` 事件新增输入大小字段，帮助分析工具调用对上下文的压力

v2.1.121 进一步增强了 LLM 请求 span，新增 `stop_reason`、`gen_ai.response.finish_reasons` 和 `user_system_prompt` 属性，让你对每次 API 请求的终止原因和系统提示有更完整的可见性。

v2.1.126 对 `skill_activated` 事件做了增强：现在对用户输入的斜杠命令也会触发该事件，并新增 `invocation_trigger` 属性来区分触发来源——`"user-slash"`（用户手动输入斜杠命令）、`"claude-proactive"`（Claude 主动调用）或 `"nested-skill"`（嵌套调用）。这让企业对 Skill 的使用模式有更精确的审计能力（v2.1.126 改进）。

### OTel Collector 架构

实际部署中，通常不会让 Claude Code 直接将数据发送给最终存储后端，而是在中间放一个 OTel Collector 做中转：

```mermaid
graph LR
    A[Claude Code] -->|OTLP| B[OTel Collector]
    B --> C[Grafana]
    B --> D[Datadog]
    B --> E[Elasticsearch]
```

??? tip "为什么需要 Collector？"

    Collector 可以做数据过滤、采样、格式转换，避免把原始数据直接灌入后端。比如你只想保留每天的 Token 汇总，不想逐条记录每次工具调用，就可以在 Collector 层做聚合。

## 📋 分析与审计

除了底层的 OTel 遥测，Claude Code 还提供了更直观的分析能力，帮助企业管理者从宏观角度了解团队使用情况。

### 使用指标（Usage Metrics）

使用指标仪表盘回答"整体用了多少"的问题。企业管理者可以在 Claude 后台查看：

- 团队 Token 消耗趋势
- 按项目/团队/个人的使用分布
- 高频使用的工具和功能
- 成本趋势与预算对比

### 贡献指标（Contribution Metrics）

贡献指标回答"AI 辅助到底产出了多少"的问题，与 GitHub PR 紧密关联。当 Claude Code 辅助开发者提交代码时，系统会自动标记这些贡献。

| 指标 | 说明 |
|------|------|
| PR 数量 | Claude Code 参与的 PR 数 |
| 代码行数 | Claude Code 生成/修改的代码行 |
| 审查通过率 | AI 辅助代码的审查结果 |
| 团队排名 | 基于贡献量的排行榜 |

这些指标通过 GitHub Webhook 传送到分析仪表盘，让管理者能够量化 AI 辅助开发带来的实际产出。

### 成本追踪（Cost Tracking）

成本追踪回答"花了多少钱、能不能控"的问题，仅在 Anthropic 直连（Claude for Teams / Enterprise / Console）下可用。云提供商用户通过各自的计费平台（AWS Cost Explorer、GCP Billing、Azure Cost Management）查看支出。

| 能力 | 说明 |
|------|------|
| 支出限制 | 为团队或个人设置消费上限 |
| 速率限制 | 控制单位时间内的请求量 |
| 使用归属 | 按用户、项目、团队归集成本 |

Claude for Teams 和 Enterprise 计划在 [claude.ai/analytics/claude-code](https://claude.ai/analytics/claude-code) 提供内置使用情况仪表盘。如果需要请求级别的审计日志或按数据敏感度路由流量，可以在开发者和提供商之间部署 [LLM Gateway](../integrations/index.md#llm-gateway)。

## 🛡️ 安全架构

Claude Code 的安全设计遵循一个核心原则：**最小权限（Least Privilege）**。简单说就是：默认什么权限都不给，需要什么再明确授予。

### 权限模型

Claude Code 在执行任何操作前，都需要获得用户的明确授权。权限通过工具级别进行控制：

- **文件读取（Read）**：默认允许
- **文件编辑（Edit/Write）**：需要用户确认
- **命令执行（Bash）**：需要用户确认，管理员可进一步限制
- **网络访问**：受网络策略管控

💡 这种"默认只读"的设计很像数据库的 `SELECT` 权限——查询数据是安全的，但修改数据需要额外授权。

### 沙箱机制

沙箱（Sandbox）为 Claude Code 的命令执行提供了一个隔离环境，防止意外或恶意的操作影响宿主系统。后台 agent 还支持 worktree 隔离——在独立的 git worktree 中执行操作，结合 `disableAllHooks` 策略确保后台 agent 无法绕过安全限制（v2.1.49 新增）。

沙箱的限制包括：

| 限制维度 | 具体约束 |
|---------|---------|
| 文件系统 | 只能访问工作目录内的文件 |
| 网络 | 限制出站网络连接 |
| 进程 | 限制可执行的命令 |
| 资源 | 限制 CPU 和内存使用 |

??? note "沙箱 vs Docker"

    沙箱是轻量级的进程级隔离，不依赖容器运行时。如果你需要更强的隔离，可以将 Claude Code 运行在 Docker 容器中，沙箱 + 容器形成双层防护。

### Prompt 注入防御

Prompt 注入是 LLM 应用面临的核心安全威胁之一——攻击者试图通过精心构造的输入，让模型执行非预期的操作。Claude Code 在多个层面进行了防御：

1. **指令边界保护**：系统提示与用户输入之间有明确的分隔，减少用户输入"污染"系统指令的可能
2. **工具调用验证**：每次工具调用都需要用户确认，即使模型被注入也无法自动执行敏感操作
3. **权限分层**：只读操作自动通过，写操作必须审批，减少注入攻击的攻击面

⚠️ 注意：没有任何防御是 100% 完美的。Prompt 注入防御是一个持续演进的领域，企业应结合代码审查、输出验证等手段形成纵深防御体系。

v2.1.126 修复了 `allowManagedDomainsOnly` / `allowManagedReadPathsOnly` 在更高优先级的托管设置源缺少 `sandbox` 块时被忽略的安全问题。如果你使用了这些策略来限制网络或文件访问，建议升级到 v2.1.126 或更高版本以确保策略正确生效。

对于使用 `CLAUDE_CODE_PROVIDER_MANAGED_BY_HOST` 进行主机托管部署（Bedrock/Vertex/Foundry）的用户，v2.1.126 修复了分析功能被自动禁用的问题。之前该环境变量会错误地关闭使用量分析，导致企业无法追踪这些部署的实际使用情况（v2.1.126 修复）。

## 开发容器（DevContainer）

企业开发环境需要一致性和隔离性。开发容器将 Claude Code 运行在容器化环境中，命令执行与文件系统都被隔离，而项目文件的编辑实时同步到本地仓库。

核心优势：每个开发者面对同一个操作系统、同一套工具链、同一个 Claude Code 版本。容器还是应用组织策略的天然场所——相同的镜像和配置在所有开发者的机器上运行。

详见「[开发容器（DevContainer）](devcontainer/index.md)」，涵盖安装配置、认证持久化、容器内策略执行、网络出站限制和 Codespaces 集成。

## 🌐 企业网络配置

企业网络通常不直接访问互联网——流量需要经过代理服务器、防火墙，可能还要处理企业 TLS 检查。Claude Code 通过环境变量支持这些企业网络场景，包括代理配置、自定义 CA 证书信任和 mTLS 身份验证。

### 代理配置

Claude Code 遵守标准的代理环境变量。大多数企业只需要配置 `HTTPS_PROXY`：

```bash
# HTTPS 代理（推荐）
export HTTPS_PROXY=https://proxy.example.com:8080

# HTTP 代理（如果 HTTPS 不可用）
export HTTP_PROXY=http://proxy.example.com:8080

# 绕过特定请求的代理——空格或逗号分隔
export NO_PROXY="localhost,192.168.1.1,example.com,.example.com"
```

如果代理需要基本认证，在 URL 中包含凭证：

```bash
export HTTPS_PROXY=http://username:password@proxy.example.com:8080
```

??? tip "高级认证场景"

    对于需要 NTLM、Kerberos 等高级认证方式的代理，Claude Code 不直接支持。可以考虑在 Claude Code 和上游 API 之间部署 LLM Gateway 来处理这些认证需求。

### CA 证书信任

企业 TLS 检查代理（如 CrowdStrike Falcon、Zscaler）会用自己的根证书替换原始证书。Claude Code 默认信任其捆绑的 Mozilla CA 证书和操作系统证书存储，因此只要企业根 CA 安装在操作系统信任存储中，TLS 检查代理无需额外配置即可工作。

`CLAUDE_CODE_CERT_STORE` 控制信任哪些 CA 源，接受逗号分隔的列表：

```bash
# 默认值：信任捆绑证书和系统证书
export CLAUDE_CODE_CERT_STORE=bundled,system

# 仅信任捆绑的 Mozilla CA（忽略系统证书）
export CLAUDE_CODE_CERT_STORE=bundled

# 仅信任系统证书存储
export CLAUDE_CODE_CERT_STORE=system
```

如果企业使用自定义 CA 且不在系统证书存储中，使用 `NODE_EXTRA_CA_CERTS` 指定：

```bash
export NODE_EXTRA_CA_CERTS=/path/to/enterprise-ca-cert.pem
```

??? note "Node.js 运行时的限制"

    系统 CA 存储集成需要原生 Claude Code 二进制分发。在 Node.js 运行时上运行时，系统 CA 存储不会自动合并，必须通过 `NODE_EXTRA_CA_CERTS` 显式指定。

### mTLS 身份验证

某些企业环境要求双向 TLS 认证（mTLS）——不仅客户端验证服务端证书，服务端也要验证客户端证书。Claude Code 通过以下环境变量支持 mTLS：

```bash
# 客户端证书
export CLAUDE_CODE_CLIENT_CERT=/path/to/client-cert.pem

# 客户端私钥
export CLAUDE_CODE_CLIENT_KEY=/path/to/client-key.pem

# 可选：私钥密码
export CLAUDE_CODE_CLIENT_KEY_PASSPHRASE="your-passphrase"
```

### 域名白名单

在配置代理和防火墙规则时，需要将 Claude Code 依赖的域名加入白名单：

| URL | 用途 |
|-----|------|
| `api.anthropic.com` | Claude API 请求 |
| `claude.ai` | Claude.ai 账户认证 |
| `platform.claude.com` | Anthropic Console 账户认证 |
| `downloads.claude.ai` | 插件可执行文件下载、原生安装程序和更新 |
| `bridge.claudeusercontent.com` | Chrome 中的 Claude 扩展 WebSocket 桥接 |

如果通过 npm 安装 Claude Code 或自行管理二进制分发，可能不需要访问 `downloads.claude.ai`。使用 Bedrock、Vertex 或 Foundry 时，模型流量和认证会转到对应的云提供商，而非 `api.anthropic.com`。

### LLM Gateway

对于需要集中控制 API 流量的企业，可以在开发者和 Claude API 之间部署 LLM Gateway。Gateway 可以统一处理认证、速率限制、请求审计等横切关注点，而无需在每个开发者设备上单独配置。具体配置方式参见 LLM Gateway 集成文档。

## 法律合规与零数据保留

企业使用 AI 工具时，法务和合规团队最关心的问题是：**我们的代码会被拿去训练 AI 吗？数据会泄露吗？** 企业版的 API 请求不会被用于模型训练，且可通过零数据保留（ZDR）策略实现推理数据的即时删除。

详见「[法律合规与零数据保留](legal/index.md)」，涵盖数据使用政策、BAA/ZDR 合规关系、认证方式使用政策、安全漏洞报告和 ZDR 的完整配置指南。

## 🏗️ GitHub Enterprise Server 集成

很多企业的代码不是托管在 github.com 上，而是运行在自建的 GitHub Enterprise Server（GHES）实例中。Claude Code 支持与 GHES 集成，让企业可以在自托管环境中使用完整的 Claude Code 功能。

### 支持的功能一览

| 功能 | GHES 支持情况 | 说明 |
|------|-------------|------|
| Web 会话（`claude --remote`） | ✅ 支持 | 管理员连接一次，开发者直接使用 |
| 自动代码审查（Code Review） | ✅ 支持 | 与 github.com 行为一致 |
| Teleport 会话转移 | ✅ 支持 | 在 Web 和终端之间切换 |
| 插件市场 | ✅ 支持 | 需使用完整 git URL |
| 贡献指标 | ✅ 支持 | 通过 Webhook 传递 |
| GitHub Actions | ✅ 支持 | 需手动配置 Workflow |
| GitHub MCP Server | ❌ 不支持 | 需改用 `gh` CLI 替代 |

### 管理员配置流程

GHES 集成由管理员一次性配置完成，开发者无需做任何额外设置。

核心步骤：

1. 在 Claude 组织后台启动引导设置
2. 系统生成 GitHub App Manifest
3. 在 GHES 上创建对应的 GitHub App（授予必要权限）
4. 配置 Webhook 以接收事件通知

GitHub App 需要的权限：

| 权限 | 级别 | 用途 |
|------|------|------|
| Contents | 读写 | 克隆仓库、推送分支 |
| Pull requests | 读写 | 创建 PR、发布审查评论 |
| Issues | 读写 | 响应 Issue 提及 |
| Checks | 读写 | 发布 Code Review 状态检查 |
| Actions | 只读 | 读取 CI 状态 |
| Metadata | 只读 | GitHub 基础要求 |

### 网络要求

⚠️ GHES 实例必须能从 Anthropic 基础设施访问。如果你的 GHES 部署在防火墙后面，需要将 Anthropic API 的 IP 地址加入白名单。

### 开发者工作流

管理员配置完成后，开发者的使用体验与 github.com 完全一致。Claude Code 会自动从当前工作目录的 `git remote` 中检测 GHES 主机名：

```bash
# 克隆 GHES 仓库（正常操作）
git clone https://github.example.com/my-org/my-project.git
cd my-project

# 启动 Web 会话（Claude 自动检测 GHES 并路由）
claude --remote "为支付回调添加重试逻辑"
```

### 插件市场在 GHES 上的差异

GHES 上的插件市场与 github.com 的结构完全相同，唯一的区别是引用方式。因为 `owner/repo` 短格式默认解析到 github.com，GHES 必须使用完整 git URL：

```bash
# ❌ github.com 短格式（不会解析到 GHES）
/plugin marketplace add my-org/claude-plugins

# ✅ 使用完整 git URL
/plugin marketplace add https://github.example.com/my-org/claude-plugins.git
```

如果企业通过托管设置限制了可用的插件市场来源，管理员可以用 `hostPattern` 批量放行整个 GHES 主机名下的所有市场：

```json
{
  "strictKnownMarketplaces": [
    {
      "source": "hostPattern",
      "hostPattern": "^github\\.example\\.com$"
    }
  ]
}
```

## ✅ 验证与入职

### 验证托管设置是否生效

配置完成后，让开发者在 Claude Code 中运行 `/status`。输出中会包含以 `Enterprise managed settings` 开头的一行，后跟括号标注的来源：

| 来源标识 | 含义 |
|---------|------|
| `(remote)` | Server-managed Settings（从 Anthropic 服务器下发） |
| `(plist)` | macOS 托管偏好设置 |
| `(HKLM)` | Windows 系统注册表 |
| `(HKCU)` | Windows 用户注册表 |
| `(file)` | File-based managed（系统目录 JSON 文件） |

如果输出中**没有** `Enterprise managed settings` 行，说明当前设备未收到任何托管策略，需要排查传递机制是否正确配置。

### 常见入职问题

| 问题 | 解决方案 |
|------|---------|
| 登录后看不到企业选项 | 运行 `claude update` 更新到最新版本，然后重启终端 |
| "你还没有被添加到组织" | 座位不包含 Claude Code 访问权限，需在管理控制台中更新 |
| 需要切换账户 | 运行 `/logout` 然后 `/login` |
| GHES 仓库未识别 | 确认 `git remote` 指向 GHES 主机名 |

### 开发者入门资源

配置验证通过后，可以分享以下资源帮助团队快速上手：

- [快速入门](../getting-started/index.md)：从安装到首次使用的完整流程
- [常见工作流](../best-practices/index.md)：代码审查、重构、调试等日常任务模式
- [Claude Academy 课程](https://anthropic.skilljar.com/)：Anthropic 官方自定进度学习课程

**小结**：Claude Code 的企业管理体系围绕"集中管控 + 最小权限 + 透明可审计"三个原则构建。托管设置通过四种传递机制统一策略下发，故障关闭和审计日志确保策略不被绕过，八大控制面精细管控权限和隔离，认证方式灵活适配企业基础设施，OTel 监控与分析仪表盘让用量透明可量化，安全架构从权限、沙箱、注入防御三层构筑防线，企业网络配置覆盖代理、证书和 mTLS 等复杂场景。开发容器、法律合规与零数据保留的详细内容已拆分为独立子页面，配置完成后通过 `/status` 验证生效，即可让团队安全高效地使用 Claude Code。
