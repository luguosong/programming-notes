---
title: 配置体系
description: 理解 Claude Code 的配置层级、权限系统、沙箱安全、模型选择、输出风格等核心配置能力
---

# 配置体系

**本文你会学到**：

- 🏗️ 理解 Claude Code 五层配置优先级的含义和作用范围
- 📝 配置文件的位置与冲突解决规则

→ 更多内容请查看子页面：「CLAUDE.md 记忆文件」「Settings 与权限」「模型与输出风格」「高级配置」

## 🏗️ 为什么 Claude Code 的配置很重要？

想象一下：你刚入职一家公司，有公司制度（所有员工都要遵守）、部门规章（你所在部门的额外规则）、还有你个人的工作习惯。这三层规则共同决定了你「能做什么」和「该怎么做」。

Claude Code 的配置体系也是同样的道理——它有五层配置，从「管理员强制执行的安全策略」到「你个人的偏好」，层层叠加、逐级覆盖。理解这个体系，你就掌握了让 Claude Code 按你的意愿高效工作的钥匙。

## 📂 配置文件有哪几层？

### 五层优先级

Claude Code 的所有配置都遵循相同的优先级规则：**数字越小优先级越高**。

| 优先级 | 层级 | 谁能修改 | 作用范围 | 典型用途 |
|--------|------|---------|---------|---------|
| 1（最高） | Managed（托管） | 系统管理员 | 整个组织 | 强制安全策略、合规要求（v2.1.83 新增 `managed-settings.d/` 分片配置目录） |
| 2 | CLI 参数 | 用户（启动时） | 单次会话 | 临时覆盖、调试 |
| 3 | Local（本地） | 用户 | 本机、本项目 | 个人偏好（不提交 git） |
| 4 | Project（项目） | 团队 | 当前项目 | 团队规范（提交 git 共享） |
| 5（最低） | User（用户） | 用户 | 所有项目 | 全局个人偏好 |

💡 把它想成 CSS 的优先级：`!important > inline style > ID > class > tag`。Managed 配置就像 `!important`，无论你怎么设置，它说了算。

📝 一些实用的配置项：

- `autoScrollEnabled`（v2.1.110 新增）：全屏模式下禁用对话自动滚动。当 Claude 输出很长而你正在回看上方内容时，关闭自动滚动可以防止页面自动跳到底部

### 配置文件位置

每一层都有对应的配置文件路径：

| 层级 | 配置文件路径 | 是否提交 git |
|------|-------------|-------------|
| Managed | 由系统管理器注入，路径因平台而异 | 不适用 |
| Local | `.claude/settings.local.json` | ❌ 不提交 |
| Project | `.claude/settings.json` | ✅ 提交共享 |
| User | `~/.claude/settings.json` | 不适用（全局） |

⚠️ 注意：`.claude/settings.local.json` 和 `.gitignore` 默认会忽略本地配置文件，所以你的个人偏好不会意外提交到仓库。

### 冲突解决规则

当多个层级对同一个配置项设置了不同的值时，**高优先级覆盖低优先级**。但有一个重要的例外：**权限规则（permissions）是追加的，不是覆盖的**。所有层级的 `allow` 规则会合并，而 `deny` 规则会从最严格的层级生效。v2.1.0 新增了通配符权限匹配（如 `Bash(npm *)`、`Bash(* install)`），让权限规则更灵活。

!!! warning "v2.1.113 安全收紧"

    从 v2.1.113 起，权限引擎对常见绕过手法做了关键收紧——这些是真实存在过的安全漏洞：

    - **`deny` 规则匹配 exec wrapper**：`Bash(rm:*)` 等 deny 规则现在会匹配被 `env`、`sudo`、`watch`、`ionice`、`setsid` 等 wrapper 包裹的命令，不能再通过 `sudo rm ...` 绕过
    - **`Bash(find:*)` allow 不再放行 `-exec` / `-delete`**：避免攻击者用 `find . -exec rm {} \;` 绕过 `Bash(rm:*)` 限制
    - **macOS `/private/{etc,var,tmp,home}` 视为危险删除目标**：在 `Bash(rm:*)` allow 规则下，删除这些路径会触发危险确认（macOS 上 `/etc` 实际指向 `/private/etc`）
    - **`dangerouslyDisableSandbox` 强制提示**：使用此参数运行命令时不再绕过权限提示（v2.1.113 修复）
    - **多行注释命令完整显示**：第一行是注释的多行 Bash 命令现在在 transcript 中显示完整命令，关闭 UI 欺骗向量（v2.1.113 修复）

    如果你之前依赖这些"漏洞"做某些自动化，升级后需要显式调整 `allow` 规则。

!!! tip "新增 sandbox 网络黑名单"

    v2.1.113 新增 `sandbox.network.deniedDomains` 设置——即使更广泛的 `allowedDomains` 通配符允许，仍可阻止特定域名。适合"允许 `*.example.com` 但禁止 `internal.example.com`"这类场景。

## ✅ 配置最佳实践

### 推荐的项目级配置

``` json title=".claude/settings.json（提交到 git）"
{
  "permissions": {
    "allow": [
      "Read",
      "Edit",
      "Bash(git status)",
      "Bash(git diff*)",
      "Bash(git log*)",
      "Bash(mvn test*)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(sudo *)"
    ]
  }
}
```

### 推荐的 User 级配置

``` json title="~/.claude/settings.json"
{
  "language": "chinese",
  "permissions": {
    "allow": [
      "Read",
      "Write",
      "Edit"
    ]
  }
}
```

💡 `language` 设置在 v2.1.0 中新增，用于配置 Claude 的回复语言。

主题方面，v2.1.111 新增了 "Auto (match terminal)" 主题选项，自动匹配终端的深色/浅色模式——你不再需要根据终端主题手动切换。从 `/theme` 命令中选择即可。

💡 命令行输入子命令时如果拼写接近但不正确，Claude Code 会自动建议最接近的子命令（v2.1.111 新增），比如输入 `claude udpate` 会提示 "你是想说 `claude update`？"。

### 配置层级选择指南

| 你想配置什么？ | 放在哪一层？ | 原因 |
|---------------|-------------|------|
| 团队编码规范 | Project | 全员共享，通过 git 同步 |
| 个人语言偏好 | User | 所有项目通用 |
| 企业的安全策略 | Managed | 管理员强制执行 |
| 临时调试配置 | CLI 参数 | 仅本次会话生效 |
| 本地开发环境路径 | Local | 因机器而异，不提交 git |

📝 小结：Claude Code 的配置体系由五层优先级构成——Managed > CLI > Local > Project > User。理解这个层级关系后，你就能精确控制 Claude Code 在不同场景下的行为。权限系统保障安全，沙箱机制提供隔离，模型配置和输出风格让你按需定制 Claude 的能力与风格。