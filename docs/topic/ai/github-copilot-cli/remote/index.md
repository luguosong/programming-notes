---
title: 远程访问
description: 从 GitHub.com 或 GitHub Mobile 远程访问和控制 CLI 会话
---

# 远程访问

**本文你会学到**：

- 如何从 GitHub.com 或 GitHub Mobile 连接到正在运行的 CLI 会话
- 远程访问的前提条件与启用方式
- 远程能做哪些操作、有哪些限制
- `/keep-alive` 防休眠与会话重连机制

打个比方：远程访问就像手机远程控制——你在手机上可以看到并操控办公室电脑上运行的 Copilot 会话，离开工位也不耽误审批权限请求或查看任务进度。

## 概念与使用场景

远程访问让你从 GitHub.com 或 GitHub Mobile 查看和控制正在运行的 CLI 会话。

典型使用场景：

- **离开工位后继续交互** — 在笔记本上启动了会话，但临时被叫走或已下班，不想回到机器前也能继续与 Copilot 交互
- **监控长时间运行的任务** — 启动了复杂任务，但没给 Copilot 完全权限，需要定期审批权限请求
- **手机快速查看进度** — 正在做其他事情，用 GitHub Mobile 快速扫一眼 Copilot 的工作状态

!!! info "公开预览中"

    远程访问目前处于公开预览阶段，功能可能变化。移动端访问需要 GitHub Mobile 最新 Beta 版本：
    - [iOS TestFlight](https://testflight.apple.com/join/NLskzwi5)
    - [Android Google Play](https://play.google.com/apps/testing/com.github.android)

## 前提条件

| 条件 | 说明 |
|------|------|
| **策略已启用** | 企业/组织的 "Remote Control" 策略必须开启（默认关闭） |
| **GitHub 仓库** | 工作目录必须是 GitHub.com 上的 Git 仓库，否则提示 "Remote session disabled: not in a GitHub repository" |
| **机器在线** | CLI 会话必须在联网机器上运行，休眠或断网后远程不可用 |
| **交互式会话** | 仅支持交互式会话，`-p`/`--prompt` 编程式模式不支持 |

## 启用方式

有三种方式启用远程访问：

=== "斜杠命令"

    在交互式会话中输入：

    ```text
    /remote
    ```

    CLI 会连接 GitHub.com 并显示远程访问详情。再次输入 `/remote` 可重新查看详情，`/remote off` 可关闭。

=== "命令行标志"

    启动 CLI 时直接带 `--remote` 参数：

    ```bash
    copilot --remote
    ```

    适合一开始就知道可能需要远程访问的场景，避免忘记手动启用。

=== "配置文件"

    在 `~/.copilot/settings.json` 中设置默认启用：

    ```json
    {
      "remoteSessions": true
    }
    ```

    所有交互式会话自动开启远程访问。如需单次禁用，使用 `--no-remote`。

!!! note "优先级"

    命令行标志 `--remote` / `--no-remote` 始终优先于配置文件中的 `remoteSessions` 设置。

## 从 GitHub.com 访问

启用后，CLI 会显示一个会话链接，格式为：

```text
https://github.com/OWNER/REPO/tasks/TASK_ID
```

用同一账号登录 GitHub.com 后即可通过该链接访问会话。也可以从最近代理会话列表中找到它：左上角菜单 → **Copilot** → **Recent agent sessions** → 点击你的 CLI 会话。

本地终端和远程界面**同时活跃**，两边都能输入。Copilot CLI 以先收到的响应为准。会话本身始终运行在本地机器上，远程界面只是交互通道。

## 从 GitHub Mobile 访问

1. 打开 GitHub Mobile，点击右下角的 **Copilot** 按钮
2. 在 "Agent sessions" 列表中找到你的会话并点击打开

### QR 码快速访问

1. 在交互式会话中输入 `/remote` 重新显示远程会话详情
2. 按 ++ctrl+e++ 切换 QR 码显示（注意：输入框必须为空时此快捷键才生效）
3. 用手机扫描 QR 码直接跳转到 GitHub Mobile 中的会话

## 远程能力

| 操作 | 说明 |
|------|------|
| 响应权限请求 | 批准或拒绝工具、文件路径、URL 权限请求 |
| 回答问题 | 当 Copilot 需要更多信息或需要你做决策时 |
| 审批计划 | Plan 模式下的计划审批 |
| 提交新提示 | 输入新的指令或问题，和在终端中一样 |
| 切换模式 | 在 Interactive 和 Plan 模式之间切换 |
| 取消操作 | 停止代理当前正在做的工作 |

!!! warning "斜杠命令不可用"

    远程界面目前**不支持**斜杠命令（如 `/allow-all`）。

## /keep-alive 防休眠

使用 `/keep-alive` 防止机器在长时间任务中休眠，保持远程连接畅通。

| 用法 | 说明 |
|------|------|
| `/keep-alive on` | CLI 会话运行期间持续保持唤醒 |
| `/keep-alive off` | 允许机器正常休眠 |
| `/keep-alive busy` | 仅在 Copilot 正在执行任务或等待你输入时保持唤醒，空闲后可正常休眠 |
| `/keep-alive 30m` | 保持唤醒 30 分钟 |
| `/keep-alive 2h` | 保持唤醒 2 小时 |
| `/keep-alive 1d` | 保持唤醒 1 天 |

不带参数时显示当前 keep-alive 状态。

## 恢复带远程访问的会话

关闭一个启用了远程访问的会话后，CLI 会显示包含 `--remote` 的恢复命令：

```bash
copilot --resume=SESSION_ID --remote
```

同样，`copilot --continue --remote` 可以恢复最近的会话并自动启用远程访问。如果配置文件中已设置 `"remoteSessions": true`，则恢复时自动启用，无需额外加 `--remote`。

## 重连与可见性

- **自动重连** — 本地机器与 GitHub 之间的网络暂时中断后，连接恢复即可继续远程使用
- **仅账户所有者可访问** — 只有启动 CLI 会话的同一 GitHub 账号才能远程访问，其他人无法查看或交互
- **会话数据传输** — 会话事件（对话消息、工具执行、权限请求）从本地发送到 GitHub，远程命令从 GitHub 轮询并由 CLI 注入本地会话
- **60MB 输出限制** — 为保证远程功能稳定性，会话输出超过 60MB 后远程界面性能可能下降，本地终端不受影响
- **本地执行不变** — 所有 Shell 命令、文件操作、工具执行都在本地机器上进行，远程访问不授予对本地机器的任何直接访问权限

## 管理策略

企业/组织所有者可通过 AI 控制设置管理远程访问策略：

- "Remote Control" 策略**默认关闭**，需手动启用
- 策略在组织或企业级别设置，用户个人无法绕过
- 该策略关闭时，组织成员无法使用远程访问功能

如需禁用某次会话的远程访问，使用 `copilot --no-remote` 启动，或从配置文件中移除 `remoteSessions` 设置。
