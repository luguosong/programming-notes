---
title: 更新日志
description: GitHub Copilot CLI 各版本更新内容汇总，追踪新功能、改进和修复
---

# 更新日志

本页整理了 GitHub Copilot CLI 各版本的重要更新。数据来源于 [GitHub Releases](https://github.com/github/copilot-cli/releases)。

💡 建议保持 Copilot CLI 更新到最新版本，新版本通常包含性能改进和 Bug 修复：

``` bash
npm update -g @anthropic-ai/claude-code
```

---

## 📦 1.0.18（2026-04-04）

### ✨ 新功能

- **Critic Agent**：新增 Critic Agent，在执行计划和复杂实现时自动使用互补模型进行审查，提前捕获错误（Claude 模型实验性功能）
- **Notification Hook**：新增 `notification` Hook 事件，在 shell 命令完成、权限提示、对话弹窗和 Agent 完成时异步触发

### 🔧 改进

- `preToolUse` Hook 的 `permissionDecision` 设为 `allow` 时，不再弹出工具审批确认
- Session Resume 选择器首次使用时能正确按分支和仓库分组

---

## 📦 1.0.17（2026-04-03）

### ✨ 新功能

- **内置 Skills**：CLI 开始内置 Skills，首批包含 Copilot Cloud Agent 环境定制指南
- **MCP OAuth HTTPS 支持**：MCP OAuth 流程支持通过自签名证书回退实现 HTTPS 重定向 URI，兼容要求 HTTPS 的 OAuth 提供商（如 Slack）

### ⚡ 性能

- `/resume` Session 选择器加载速度大幅提升，特别是在大量会话历史的情况下

---

## 📦 1.0.16（2026-04-02）

### ✨ 新功能

- **PermissionRequest Hook**：新增 `PermissionRequest` Hook，允许脚本以编程方式批准或拒绝工具权限请求
- MCP 工具调用在时间线中显示工具名称和参数摘要

### 🔧 改进

- SQL 工具被 `excludedTools` 或 `availableTools` 排除时，不再显示 SQL prompt 标签
- MCP 服务器在工作目录变更后能正确使用有效认证重新连接

### 🐛 修复

- MCP 服务器在登录、用户切换和 `/mcp reload` 后能正确加载
- BYOK（自带密钥）Anthropic 提供商现在正确遵守配置的 `maxOutputTokens` 限制

### 🗑️ 移除

- 移除已废弃的 `marketplaces` 仓库设置（使用 `extraKnownMarketplaces` 替代）

---

## 📦 1.0.15（2026-04-01）

### ✨ 新功能

- **MCP OAuth 设备码流程**：新增 Device Code Flow（RFC 8628）作为无头环境和 CI 中 MCP OAuth 的回退方案
- **`/mcp auth` 命令**：新增 MCP OAuth 服务器的重新认证 UI，支持账号切换
- **`postToolUseFailure` Hook**：针对工具执行错误的新 Hook 事件，`postToolUse` 现在仅在工具调用成功后运行
- **`/share html` 命令**：将会话和研究报告导出为自包含的交互式 HTML 文件
- **MCP 配置 RPC**：新增 `mcp.config.list`、`mcp.config.add`、`mcp.config.update`、`mcp.config.remove` 服务端 RPC，用于管理持久化 MCP 服务器配置
- Copilot 吉祥物现在在交互模式下会有微妙的眨眼动画 👀

### ⚡ 性能

- CLI 退出会话后立即退出，不再等待最多 10 秒
- Diff 查看器新增 ++home++ / ++end++ 和 ++page-up++ / ++page-down++ 导航

### 🔧 改进

- Autopilot 模式下按 ++esc++ 或 ++ctrl+c++ 取消后不再继续执行
- 配置键名切换为 camelCase（`askUser`、`autoUpdate`、`storeTokenPlaintext`、`logLevel`、`skillDirectories`、`disabledSkills`），snake_case 仍可使用
- ++ctrl+d++ 不再排队消息，改用 ++ctrl+q++ 或 ++ctrl+enter++ 排队
- 大工具输出预览显示正确的字符计数，最多 500 字符

### 🐛 修复

- CLI 加载时输入的按键不再丢失
- MCP 连接慢的服务器不再阻止 Agent 启动
- Windows 剪贴板粘贴图片在 WSL 环境下正常工作

### 🗑️ 移除

- 移除 `gpt-5.1-codex`、`gpt-5.1-codex-mini` 和 `gpt-5.1-codex-max` 模型支持

---

## 📦 1.0.14（2026-03-31）

### ⚡ 性能

- 减少流式输出时的 CPU 使用率，优化 spinner 渲染和任务轮询
- 加快 CLI 启动速度：并行运行终端检测、认证和 git 操作
- 利用 V8 编译缓存减少重复调用时的解析和编译时间
- MCP 注册表查找更可靠，支持自动重试和请求超时

### 🔧 改进

- 允许 SDK 会话参与者通过 `handlePendingElicitation` API 响应 elicitation 请求
- SDK `exit_plan_mode.requested` 事件现在始终被触发，无论是否配置了直接回调
- ++shift+enter++ 在支持 Kitty 键盘协议的终端中正确插入换行
- Grep 和 glob 搜索在超时后及时返回结果

### 🐛 修复

- BYOM 模式下图片能正确发送到 Anthropic 模型
- 模型选择器的选择正确覆盖当前会话的 `--model` 标志
- 终端输出不再在错误退出时清屏或跳动
- `--config-dir` 在恢复会话时不再被忽略
- 被 allowlist 策略阻止的 MCP 服务器从 `/mcp show` 中隐藏
- Reasoning effort 设置在 BYOM 提供商中正确应用
- Windows 原生剪贴板复制不再包含多余的 U+FEFF 字符
- elicitation 对话中快速输入时按键不再丢失
- 鼠标支持激活时粘贴文本不再损坏
- MCP 服务器 OAuth 认证在 ACP 模式下正常工作
- Git 市场 URL 克隆失败时显示底层错误详情

### 🗑️ 移除

- 移除 `gemini-3-pro-preview` 模型支持
