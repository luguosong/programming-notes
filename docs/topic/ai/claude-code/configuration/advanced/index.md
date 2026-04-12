---
title: 高级配置
description: 状态栏定制、环境变量、企业网络代理、语音输入
---

# 高级配置

**🎯 本文你会学到**：

- 📊 自定义状态栏（StatusLine）的配置与可用数据字段
- 🔑 常用环境变量速查
- 🌐 企业网络代理、自定义 CA 证书与 mTLS 配置
- 🎙️ 语音输入的启用与按键绑定

## 📊 怎么自定义状态栏？

### 什么是状态栏？

状态栏是 Claude Code 底部的一个自定义信息条。它运行你配置的 shell 脚本，实时显示会话状态——上下文用量、费用、Git 分支等。

### 快速设置

``` bash
# 用自然语言描述你想要的效果
/statusline show model name and context percentage with a progress bar
```

Claude Code 会自动生成脚本并配置好 settings.json。

### 手动配置

``` json title="settings.json"
{
  "statusLine": {
    "type": "command",
    "command": "~/.claude/statusline.sh",
    "padding": 2
  }
}
```

也可以使用内联命令（用 `jq` 解析 JSON）：

``` json title="内联命令示例"
{
  "statusLine": {
    "type": "command",
    "command": "jq -r '\"[\\(.model.display_name)] \\(.context_window.used_percentage // 0)% context\"'"
  }
}
```

### 可用的数据字段

Claude Code 通过 stdin 向你的脚本发送 JSON 数据，主要字段包括：

| 字段 | 说明 |
|------|------|
| `model.display_name` | 当前模型显示名 |
| `context_window.used_percentage` | 上下文窗口已用百分比 |
| `context_window.remaining_percentage` | 上下文窗口剩余百分比 |
| `cost.total_cost_usd` | 本次会话总费用（USD） |
| `cost.total_duration_ms` | 会话总时长（毫秒） |
| `cost.total_lines_added` | 新增代码行数 |
| `cost.total_lines_removed` | 删除代码行数 |
| `session_id` | 会话唯一标识 |
| `version` | Claude Code 版本号 |
| `workspace.git_worktree` | 当前目录所在的 git worktree 名称（v2.1.97 新增） |

💡 脚本输出支持多行、ANSI 颜色代码和 OSC 8 可点击链接。更新频率为每次 Claude 回复后触发，300ms 防抖。你也可以通过 `refreshInterval` 设置自动刷新间隔（单位秒），让状态栏定期更新而不必等待 Claude 回复（v2.1.97 新增）：

``` json title="带自动刷新的状态栏配置"
{
  "statusLine": {
    "type": "command",
    "command": "~/.claude/statusline.sh",
    "refreshInterval": 30
  }
}
```

## 🔑 有哪些环境变量可以设置？

Claude Code 通过环境变量提供细粒度的控制能力。以下是几个最常用的环境变量：

### 常用环境变量

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `ANTHROPIC_API_KEY` | API 密钥 | `sk-ant-...` |
| `CLAUDE_CODE_USE_BEDROCK` | 使用 AWS Bedrock | `1` |
| `CLAUDE_CODE_USE_VERTEX` | 使用 Google Vertex AI | `1` |
| `DISABLE_AUTOUPDATER` | 禁用自动更新 | `1` |
| `CLAUDE_CODE_DISABLE_FAST_MODE` | 禁用 Fast Mode | `1` |
| `CLAUDE_CODE_PERFORCE_MODE` | Perforce 模式：只读文件操作提示 `p4 edit` 而非静默覆盖（v2.1.98 新增） | `1` |
| `CLAUDE_CODE_SUBPROCESS_ENV_SCRUB` | 启用子进程沙箱（PID 命名空间隔离，仅 Linux）（v2.1.98 新增） | `1` |
| `CLAUDE_CODE_SCRIPT_CAPS` | 限制每会话脚本调用次数（v2.1.98 新增） | `10` |
| `NODE_EXTRA_CA_CERTS` | 自定义 CA 证书路径 | `/path/to/ca.pem` |

💡 你可以在 `settings.json` 的 `env` 字段中设置环境变量，也可以在 shell 的 profile 文件（如 `.bashrc`）中设置。

## 🌐 公司有代理/防火墙怎么办？——企业网络配置

### 代理配置

在企业环境中，Claude Code 需要通过代理服务器访问外部 API。它尊重标准的代理环境变量：

``` bash
# HTTPS 代理（推荐）
export HTTPS_PROXY=https://proxy.example.com:8080

# 代理绕过列表
export NO_PROXY="localhost,192.168.1.1,example.com"
```

如果代理需要认证，直接在 URL 中包含凭证：

``` bash
export HTTPS_PROXY=http://username:password@proxy.example.com:8080
```

### 自定义 CA 证书

如果你的企业使用自签名证书：

``` bash
export NODE_EXTRA_CA_CERTS=/path/to/enterprise-ca.pem
```

> 💡 **v2.1.101 起**：Claude Code 默认信任操作系统的 CA 证书存储，企业 TLS 代理通常无需额外配置。如果只需要内置 CA，设置 `CLAUDE_CODE_CERT_STORE=bundled` 即可。

### mTLS 双向认证

对安全性要求极高的环境可以启用 mTLS：

``` bash
# 客户端证书
export CLAUDE_CODE_CLIENT_CERT=/path/to/client-cert.pem

# 客户端私钥
export CLAUDE_CODE_CLIENT_KEY=/path/to/client-key.pem

# 私钥密码（如果加密了）
export CLAUDE_CODE_CLIENT_KEY_PASSPHRASE="your-passphrase"
```

### 网络访问要求

确保防火墙允许访问以下域名：

| 域名 | 用途 |
|------|------|
| `api.anthropic.com` | Claude API 端点 |
| `claude.ai` | Claude.ai 账户认证 |
| `platform.claude.com` | Anthropic Console 认证 |

## 🎙️ 能用语音跟 Claude 对话吗？

### 什么是语音输入？

语音输入让你可以**按住一个键、说话、松开**——Claude Code 会把你的语音实时转写成文字，插入到提示框中。你可以混合使用语音和键盘输入。

### 启用方式

``` bash
/voice
# 输出：Voice mode enabled. Hold Space to record.
```

⚠️ 语音输入**仅支持 Claude.ai 账户认证**，不支持 API Key 或第三方云服务（Bedrock、Vertex AI 等）。此外，语音需要在本地录音，因此不支持 SSH 会话或 Web 版 Claude Code。

### 支持的语言

| 语言 | 代码 | 语言 | 代码 |
|------|------|------|------|
| 英语 | `en` | 法语 | `fr` |
| 德语 | `de` | 日语 | `ja` |
| 西班牙语 | `es` | 韩语 | `ko` |
| 中文（未列出） | — | — | — |

⚠️ 目前中文不在官方支持列表中。如果 `language` 设置为不支持的值，语音输入会回退到英语。

### 按键绑定

默认按住 `Space` 开始录音。你可以通过 `~/.claude/keybindings.json` 重新绑定：

``` json title="~/.claude/keybindings.json"
{
  "bindings": [
    {
      "context": "Chat",
      "bindings": {
        "meta+k": "voice:pushToTalk",
        "space": null
      }
    }
  ]
}
```

💡 使用修饰键组合（如 `meta+k`）可以在第一次按下时立即开始录音，无需等待长按检测。
