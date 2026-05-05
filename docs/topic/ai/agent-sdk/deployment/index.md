---
title: 部署与安全
description: 生产级 Agent 托管部署与安全架构
---

你把 Agent SDK 写好了，本地跑得好好的。但生产环境不是你的开发机——Agent 可以执行任意命令、访问文件、调用外部 API。如果没有适当的隔离和防护，一个处理恶意内容的 Agent 可能泄露用户数据、消耗系统资源、甚至被利用来攻击其他服务。如何让 Agent 在生产环境安全高效地运行，是这篇文章要解决的核心问题。

**本文你会学到**：

- Agent SDK 的架构特点（长运行进程）对部署的影响
- 四种部署模式及各自的适用场景
- 沙箱提供商的选择与对比
- 安全威胁模型和纵深防御策略
- 隔离技术（Sandbox、Docker、gVisor、Firecracker）的对比
- 凭证管理和文件系统配置的最佳实践

## 理解 SDK 架构对部署的影响

Agent SDK 和传统的无状态 LLM API 有本质区别。传统 API 是"请求-响应"模式，每次调用互不相关。而 Agent SDK 作为**长运行进程**运行，它会在持久化的 shell 环境中执行命令、在工作目录中管理文件、并携带先前交互的上下文来处理工具调用。

这意味着你不能像部署普通 Web API 那样简单地水平扩容——每个 Agent 实例是有状态的，需要持久化的工作目录和 shell 环境。

## 系统要求

每个 SDK 实例需要：

| 项目 | 要求 |
|------|------|
| 运行时 | Python 3.10+（Python SDK）或 Node.js 18+（TypeScript SDK） |
| CLI 二进制 | SDK 包自带，无需单独安装 Claude Code |
| 资源（推荐） | 1 GiB RAM、5 GiB 磁盘、1 CPU（根据任务调整） |
| 网络 | 出站 HTTPS 到 `api.anthropic.com`，可选访问 MCP 服务器或外部工具 |

## 沙箱提供商选项

几个云服务商专门为 AI 代码执行提供了安全容器环境：

| 提供商 | 特点 |
|--------|------|
| [Modal Sandbox](https://modal.com/docs/guide/sandbox) | Serverless，按需启动，适合临时任务 |
| [Cloudflare Sandboxes](https://github.com/cloudflare/sandbox-sdk) | 边缘计算，低延迟 |
| [E2B](https://e2b.dev/) | 专为 AI Agent 设计的沙箱环境 |
| [Fly Machines](https://fly.io/docs/machines/) | 容器化，支持持久化实例 |
| [Vercel Sandbox](https://vercel.com/docs/functions/sandbox) | 与 Vercel 生态集成 |
| [Daytona](https://www.daytona.io/) | 开发环境管理 |

对于自托管场景，可以使用 Docker、gVisor 或 Firecracker 进行隔离（见下文「隔离技术对比」）。

## 四种部署模式

根据你的应用场景，选择合适的部署模式：

### 临时会话

为每个用户任务创建一个新容器，任务完成后销毁。

适合一次性任务，用户可能在任务进行中与 AI 交互，但完成后容器即销毁：

- Bug 调查和修复
- 发票处理和数据提取
- 文档翻译
- 图像/视频处理

### 长运行会话

为长期任务维护持久化容器实例。通常在容器内按需运行**多个** Agent 进程。

适合主动型 Agent（无用户输入也能自主行动）或高频交互场景：

- 邮件代理：自动监控、分类、响应
- 网站构建器：每个用户独立容器，支持实时编辑
- 高频聊天机器人：处理 Slack 等平台的连续消息流

### 混合会话

临时容器 + 状态补充（来自数据库或 SDK 的会话恢复功能）。

适合间歇性交互场景——启动工作、完成后关闭，但可以恢复继续：

- 个人项目管理器
- 深度研究任务（跨多小时）
- 客户支持代理（跨多轮交互的工单）

### 单容器多进程

在一个全局容器中运行多个 Agent SDK 进程。

适合 Agent 之间需要紧密协作的场景，但需要防止相互覆盖，通常是**最不推荐**的模式：

- 多 Agent 模拟（如视频游戏中的 NPC）

## 安全威胁模型

Agent 的行为可能被它处理的内容所影响——仓库中的 README、网页内容、用户输入中的恶意指令，这就是所谓的 **Prompt Injection**。例如，如果某个项目的 README 包含异常指令，Agent 可能会将这些指令纳入其操作中，以操作者未预期的方式行动。

Claude 模型本身设计为能够抵御此类攻击，但纵深防御仍然是良好的实践。即使 Agent 被诱导执行恶意操作，网络控制也可以完全阻断对外通信。

### 内置安全功能

Claude Code 自带几项安全特性：

- **权限系统**：每个工具和 bash 命令都可以配置为 allow、block 或需要用户批准。支持 glob 模式匹配规则（如"允许所有 npm 命令"或"禁止任何带 sudo 的命令"）
- **命令解析**：执行 bash 命令前，Claude Code 会将其解析为 AST 并与权限规则匹配。无法解析的命令或不在 allow 规则中的命令需要显式批准。`eval` 等危险构造始终需要批准
- **网页搜索摘要**：搜索结果以摘要形式传递而非原始内容，降低恶意网页内容注入的风险
- **Sandbox 模式**：bash 命令可以在受限制的沙箱环境中运行

## 安全原则

对于需要超越默认安全配置的部署，以下三个原则指导所有可用选项。

### 最小权限

限制 Agent 只拥有完成特定任务所需的最小能力：

| 资源 | 限制方式 |
|------|---------|
| 文件系统 | 只挂载必要的目录，优先只读 |
| 网络 | 通过代理限制到特定端点 |
| 凭证 | 通过代理注入，不直接暴露 |
| 系统能力 | 在容器中 drop Linux capabilities |

### 安全边界

安全边界将不同信任级别的组件隔离开。对于高安全场景，将敏感资源（如凭证）放在 Agent 边界之外。即使 Agent 环境出了问题，边界外的资源仍然受保护。

例如，不给 Agent 直接访问 API key，而是在 Agent 环境外部运行一个代理，由代理在转发请求时注入 key。Agent 可以发 API 请求，但永远看不到凭证本身。

### 纵深防御

对于高安全环境，分层叠加多种控制提供额外保护：

- 容器隔离
- 网络限制
- 文件系统控制
- 代理层请求验证

## 隔离技术对比

不同隔离技术在安全性、性能和运维复杂度之间有不同的权衡：

| 技术 | 隔离强度 | 性能开销 | 运维复杂度 |
|------|---------|---------|-----------|
| Sandbox runtime | 良好（安全默认值） | 非常低 | 低 |
| Docker 容器 | 取决于配置 | 低 | 中 |
| gVisor | 优秀（正确配置时） | 中/高 | 中 |
| 虚拟机（Firecracker、QEMU） | 优秀（正确配置时） | 高 | 中/高 |

### Sandbox runtime

轻量级隔离，无需容器。使用 OS 原语（Linux 上是 `bubblewrap`，macOS 上是 `sandbox-exec`）在操作系统层面限制文件系统和网络访问。

配置方式：

```bash title="安装 sandbox-runtime"
npm install @anthropic-ai/sandbox-runtime
```

通过 JSON 配置文件指定允许的路径和域名。优点是无需 Docker 配置、容器镜像或网络设置，代理和文件系统限制内置。

安全注意事项：

- 与 VM 不同，沙箱进程共享宿主机内核。内核漏洞理论上可能被利用来逃逸。如果需要内核级隔离，使用 gVisor 或 VM
- 代理基于客户端提供的 hostname 做域名白名单，不终止或检查加密流量。沙箱内的代码理论上可以通过 domain fronting 等技术访问白名单外的主机。如果需要更强的保证，配置 TLS 终止代理

### Docker 容器

容器通过 Linux namespace 提供隔离。每个容器有独立的文件系统、进程树和网络栈视图，但共享宿主机内核。

一个安全加固的容器配置：

```bash title="Docker 安全加固配置"
docker run \
  --cap-drop ALL \
  --security-opt no-new-privileges \
  --security-opt seccomp=/path/to/seccomp-profile.json \
  --read-only \
  --tmpfs /tmp:rw,noexec,nosuid,size=100m \
  --tmpfs /home/agent:rw,noexec,nosuid,size=500m \
  --network none \
  --memory 2g \
  --cpus 2 \
  --pids-limit 100 \
  --user 1000:1000 \
  -v /path/to/code:/workspace:ro \
  -v /var/run/proxy.sock:/var/run/proxy.sock:ro \
  agent-image
```

各选项的作用：

| 选项 | 作用 |
|------|------|
| `--cap-drop ALL` | 移除所有 Linux capabilities（如 `NET_ADMIN`、`SYS_ADMIN`），防止权限提升 |
| `--security-opt no-new-privileges` | 阻止进程通过 setuid 二进制获取额外权限 |
| `--security-opt seccomp=...` | 限制可用系统调用，Docker 默认阻止约 44 个，自定义 profile 可以阻止更多 |
| `--read-only` | 容器根文件系统不可变，Agent 无法持久化修改 |
| `--tmpfs /tmp:...` | 提供可写的临时目录，容器停止后自动清除 |
| `--network none` | 移除所有网络接口，Agent 只能通过挂载的 Unix socket 通信 |
| `--memory 2g` | 限制内存使用，防止资源耗尽 |
| `--pids-limit 100` | 限制进程数量，防止 fork 炸弹 |
| `--user 1000:1000` | 以非 root 用户运行 |
| `-v ...:/workspace:ro` | 代码目录只读挂载，Agent 可以分析但不能修改 |
| `-v .../proxy.sock:...` | 挂载连接到宿主机代理的 Unix socket |

`--network none` 是关键：容器没有任何网络接口，唯一与外部通信的方式是通过挂载的 Unix socket 连接到宿主机上的代理。代理可以执行域名白名单、注入凭证、记录所有流量。即使 Agent 被 prompt injection 攻陷，也无法将数据泄露到任意服务器。

### gVisor

标准容器的系统调用直接到达宿主机内核，内核漏洞可能导致容器逃逸。gVisor 在用户空间拦截系统调用，实现自己的兼容层处理大多数系统调用，减少对真实内核的访问。

使用 gVisor 需要安装 `runsc` 运行时：

```json title="/etc/docker/daemon.json"
{
  "runtimes": {
    "runsc": {
      "path": "/usr/local/bin/runsc"
    }
  }
}
```

```bash title="使用 gVisor 运行容器"
docker run --runtime=runsc agent-image
```

性能开销取决于工作负载类型：

| 工作负载 | 开销 |
|---------|------|
| CPU 密集计算 | 约 0%（无系统调用拦截） |
| 简单系统调用 | 约 2 倍变慢 |
| 文件 I/O 密集 | 最高可达 10-200 倍变慢（大量 open/close 操作） |

对于多租户环境或处理不可信内容，额外隔离通常值得这个开销。

### 虚拟机

VM 通过 CPU 虚拟化扩展提供硬件级隔离。每个 VM 运行自己的内核，创建强边界。Firecracker 专为轻量级微 VM 设计，启动时间低于 125ms，内存开销不到 5 MiB。

VM 没有外部网络接口，而是通过 `vsock`（虚拟 socket）通信。所有流量通过 vsock 路由到宿主机上的代理，代理执行白名单并注入凭证后再转发。

## 凭证管理

Agent 经常需要凭证来调用 API、访问仓库或交互云服务。核心挑战是：提供访问能力但不暴露凭证本身。

### Proxy 模式

推荐方式：在 Agent 安全边界**外部**运行一个代理，由代理将凭证注入出站请求。

这个模式的好处：

1. Agent 永远看不到实际凭证
2. 代理可以执行允许访问的端点白名单
3. 代理可以记录所有请求用于审计
4. 凭证集中存储，而非分发到每个 Agent

### 配置 Agent 使用代理

Claude Code 支持两种代理配置方式：

**方式一：`ANTHROPIC_BASE_URL`**（简单，仅适用于采样 API 请求）

```bash title="通过 ANTHROPIC_BASE_URL 路由采样请求"
export ANTHROPIC_BASE_URL="http://localhost:8080"
```

Claude Code 和 Agent SDK 会将采样请求发送到你的代理而非直接发送到 Claude API。代理接收明文 HTTP 请求，可以检查和修改（包括注入凭证），然后转发到真实 API。

**方式二：`HTTP_PROXY` / `HTTPS_PROXY`**（系统级，所有流量）

```bash title="通过 HTTP_PROXY 路由所有流量"
export HTTP_PROXY="http://localhost:8080"
export HTTPS_PROXY="http://localhost:8080"
```

所有 HTTP 流量都会通过代理。但对于 HTTPS 流量，代理只能创建加密的 CONNECT 隧道，无法查看或修改请求内容（除非配置 TLS 终止）。

### 其他服务的凭证访问

Agent 除了调用 Claude API，通常还需要访问 Git 仓库、数据库和内部 API 等。两种主要方式：

**自定义工具**：通过 MCP server 或自定义工具提供访问。Agent 调用工具，但实际的认证请求在 Agent 安全边界外执行。优点是无需 TLS 中间人拦截，凭证完全在 Agent 外部。

**流量转发**：对于 HTTPS 服务，通过 TLS 终止代理解密、检查/修改、再加密转发。需要在 Agent 的信任存储中安装代理的 CA 证书，并配置 `HTTP_PROXY`/`HTTPS_PROXY`。注意不是所有程序都尊重这些变量（如 Node.js 的 `fetch()` 默认忽略，Node 24+ 可设置 `NODE_USE_ENV_PROXY=1`）。

## 文件系统配置

文件系统控制决定 Agent 可以读写哪些文件。

### 只读挂载

当 Agent 需要分析代码但不能修改时，使用只读挂载：

```bash title="只读挂载代码目录"
docker run -v /path/to/code:/workspace:ro agent-image
```

> 即使是只读访问，也可能暴露凭证。以下文件在挂载前应排除或清理：

| 文件 | 风险 |
|------|------|
| `.env`、`.env.local` | API key、数据库密码等敏感信息 |
| `~/.git-credentials` | 明文 Git 凭证 |
| `~/.aws/credentials` | AWS 访问密钥 |
| `~/.config/gcloud/application_default_credentials.json` | Google Cloud 凭证 |
| `~/.azure/` | Azure CLI 凭证 |
| `~/.docker/config.json` | Docker 镜像仓库认证 token |
| `~/.kube/config` | Kubernetes 集群凭证 |
| `.npmrc`、`.pypirc` | 包仓库 token |
| `*-service-account.json` | GCP 服务账号密钥 |
| `*.pem`、`*.key` | 私钥 |

### 可写位置

如果 Agent 需要写入文件，根据持久化需求选择方案：

- **临时工作区**：使用 `tmpfs` 挂载，仅存在于内存中，容器停止后清除
- **需要审查的修改**：使用 overlay 文件系统，Agent 的写入存储在独立层，可以检查、应用或丢弃
- **完全持久化**：挂载专用 volume，与敏感目录分离

```bash title="临时可写目录配置"
docker run \
  --read-only \
  --tmpfs /tmp:rw,noexec,nosuid,size=100m \
  --tmpfs /workspace:rw,noexec,size=500m \
  agent-image
```

## 常见问题

**Agent 会话在超时前可以运行多久？**
Agent 会话本身不会超时，但建议设置 `maxTurns` 属性防止 Agent 陷入无限循环。

**多久更新一次 CLI？**
CLI 使用 semver 版本控制，破坏性变更会有版本号变更。关注 release notes 即可。

**空闲容器应该保持还是关闭？**
取决于你的用户交互模式。不同沙箱提供商支持不同的空闲超时配置，根据用户响应频率调整。容器最低运行成本约每小时 5 美分。

**如何与容器内的 Agent 通信？**
在容器中暴露端口，应用为外部客户端提供 HTTP/WebSocket 端点，SDK 在容器内部运行。

**如何监控容器健康和 Agent 性能？**
容器本质上就是服务器，你现有的日志基础设施可以直接使用。结合「监控与成本」中的 OpenTelemetry 集成可以获得更详细的洞察。
