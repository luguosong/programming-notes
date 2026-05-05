---
title: 开发容器（DevContainer）
description: 在开发容器中运行 Claude Code，实现一致的环境隔离与企业级策略执行
---

# 开发容器（DevContainer）

企业开发环境需要一致性和隔离性——你希望每个开发者在相同的环境中运行工具，而不是各自维护一套本地配置。开发容器（Dev Container）完美解决了这个问题：定义一个容器化环境，Claude Code 在容器内运行，命令执行与文件系统都被隔离，而项目文件的编辑会实时同步到本地仓库。

## 为什么用开发容器跑 Claude Code？

直接在主机上运行 Claude Code 时，所有命令都以开发者的本地用户身份执行。这意味着：

- 不同开发者的工具链版本可能不一致
- 恶意或意外操作可能影响宿主系统
- 重建环境时认证状态和设置会丢失

开发容器把这些变量全部统一：每个开发者打开容器后，面对的是同一个操作系统、同一套工具链、同一个 Claude Code 版本。

??? warning "安全提醒"

    开发容器提供了实质性保护，但并非万能。使用 `--dangerously-skip-permissions` 时，容器无法阻止恶意项目泄露容器内可访问的任何内容，包括 `~/.claude` 中的凭证。仅在受信任的仓库中使用开发容器，并避免将主机密钥（如 `~/.ssh` 或云凭证文件）挂载到容器中。

## 在开发容器中安装 Claude Code

Claude Code 通过 Dev Container Feature 安装，适用于任何支持 Dev Containers 规范的工具（VS Code、GitHub Codespaces、JetBrains IDE 等）。

在 `.devcontainer/devcontainer.json` 中添加 Feature：

```json
{
  "image": "mcr.microsoft.com/devcontainers/base:ubuntu",
  "features": {
    "ghcr.io/anthropics/devcontainer-features/claude-code:1.0": {}
  }
}
```

Feature 会安装最新版本的 Claude Code，CLI 默认在容器内自动更新。版本标签（如 `:1.0`）固定的是 Feature 的安装脚本版本，而非 Claude Code 本身的版本。

## 认证状态持久化

容器重建时，主目录默认被丢弃，开发者每次都要重新登录。Claude Code 将认证令牌、用户设置和会话历史存储在 `~/.claude` 下。通过挂载命名卷可以在重建过程中保持这些状态：

```json
"mounts": [
  "source=claude-code-config,target=/home/node/.claude,type=volume"
]
```

如果容器的 `remoteUser` 不是 `node`，需要将 `/home/node` 替换为实际用户的家目录。若将卷挂载到 `~/.claude` 以外的位置，还需设置 `CLAUDE_CONFIG_DIR` 环境变量指向挂载路径。

要为每个项目隔离状态（而非所有仓库共享一个卷），在源名称中包含 `${devcontainerId}` 变量：

```json
"mounts": [
  "source=claude-code-config-${devcontainerId},target=/home/node/.claude,type=volume"
]
```

## 容器内策略执行

开发容器是应用组织策略的天然场所——相同的镜像和配置在每个开发者的机器上运行。Claude Code 在 Linux 上读取 `/etc/claude-code/managed-settings.json`，并以最高优先级应用其中的设置。

在 Dockerfile 中复制策略文件：

```dockerfile
RUN mkdir -p /etc/claude-code
COPY managed-settings.json /etc/claude-code/managed-settings.json
```

不过，因为 Dockerfile 存在于仓库中，有写入权限的开发者可以修改或删除这一步。如果需要防止开发者通过编辑仓库文件来绕过策略，应通过 Server-managed Settings 或 MDM 提供托管设置。

要通过环境变量为容器内的所有 Claude Code 会话设置通用配置，在 `devcontainer.json` 的 `containerEnv` 中添加：

```json
"containerEnv": {
  "CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC": "1",
  "DISABLE_AUTOUPDATER": "1"
}
```

`CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC` 是一个关键变量——设置为 `1` 后，Claude Code 会禁用所有非必要的网络流量（如遥测和崩溃报告），适用于对网络出站有严格限制的环境。配合 `DISABLE_AUTOUPDATER` 可以固定 Claude Code 版本，确保所有开发者使用一致的 CLI 版本。

### 策略传递机制的选择

在开发容器环境中，托管设置有三种典型传递路径：

| 路径 | 机制 | 安全强度 | 适用场景 |
|------|------|---------|---------|
| Server-managed | Anthropic 服务器认证时下发 | 最高 | 所有开发者使用 Anthropic 直连 |
| Dockerfile 内嵌 | 构建时复制到 `/etc/claude-code/` | 中等 | 需要快速部署，接受开发者可修改的限制 |
| MDM 注入 | 通过 `init` 脚本从外部挂载 | 高 | 有完善 MDM 基础设施的企业 |

对于安全要求最高的场景，推荐 Server-managed + Dockerfile 双重策略：Server-managed 提供不可篡改的强制策略，Dockerfile 中的配置作为容器重建后的兜底。

## 网络出站限制

企业可能希望将容器的出站流量限制为仅 Claude Code 和开发工具需要的域名。Claude Code 官方参考容器包含一个 `init-firewall.sh` 脚本，该脚本阻止除白名单域名之外的所有出站流量。

在容器内运行防火墙需要额外的 Linux capabilities，因此参考配置通过 `runArgs` 添加了 `NET_ADMIN` 和 `NET_RAW`：

```json
"runArgs": ["--cap-add=NET_ADMIN", "--cap-add=NET_RAW"]
```

防火墙脚本和这些 capabilities 对 Claude Code 本身并非必需——如果你的组织已有网络控制手段（如企业代理或防火墙），可以省略这部分配置。

??? tip "防火墙与企业代理的取舍"

    如果企业已有全局代理（如 Squid、Zscaler），在容器层面再添加防火墙可能是冗余的。建议选择其中一个层做网络控制，而不是多层叠加——多层控制会增加排错难度，且可能出现规则冲突。

## 参考配置

Anthropic 在 `anthropics/claude-code` 仓库的 `.devcontainer/` 目录下提供了一个完整的参考容器，组合了 CLI、出站防火墙、持久卷和基于 Zsh 的 shell。它作为工作示例提供，而非维护的基础镜像——你可以参考它的结构来构建适合自己项目的开发容器配置。

| 文件 | 用途 |
|------|------|
| `devcontainer.json` | 卷挂载、`runArgs` capabilities、VS Code 扩展和 `containerEnv` |
| `Dockerfile` | 基础镜像、开发工具和 Claude Code 安装 |
| `init-firewall.sh` | 阻止除白名单域名之外的所有出站网络流量 |

## Codespaces 集成

GitHub Codespaces 本质上就是云端的 Dev Container。如果你的团队使用 Codespaces，Dev Container Feature 同样适用——开发者打开 Codespace 后自动获得预配置的 Claude Code 环境，无需任何本地安装。

与本地 Dev Container 的关键差异：

| 差异点 | 本地 Dev Container | GitHub Codespaces |
|--------|-------------------|-------------------|
| 计算资源 | 使用宿主机资源 | 使用云端分配的资源 |
| 网络环境 | 继承宿主机网络 | 独立的云端网络，需额外配置代理 |
| 认证持久化 | 命名卷即可 | 需配合 Codespaces Secrets 管理凭证 |
| 成本 | 仅消耗宿主机资源 | 按 Codespaces 用量计费 |

??? note "Codespaces 中的认证管理"

    在 Codespaces 中，建议通过 GitHub Secrets 注入 `ANTHROPIC_API_KEY`，而非使用 OAuth 登录。这样每个 Codespace 实例启动时自动获取凭证，无需开发者手动操作。
