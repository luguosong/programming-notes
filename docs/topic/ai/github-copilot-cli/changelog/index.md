---
title: 更新日志
description: GitHub Copilot CLI 各版本更新内容汇总，追踪新功能、改进和修复
---

# 更新日志

本页整理了 GitHub Copilot CLI 各版本的重要更新。数据来源于 [GitHub Releases](https://github.com/github/copilot-cli/releases)。

💡 建议保持 Copilot CLI 更新到最新版本，新版本通常包含性能改进和 Bug 修复：

``` bash
npm update -g @github/copilot
```

---

## 📦 1.0.24（2026-04-10）

> 📝 **笔记定位**：[preToolUse Hook 字段](../hooks/index.md#-hook-配置字段) · [Agent model 字段](../agents/index.md#-自定义-agent)

### 🔧 改进

- `preToolUse` Hook 现在遵循 `modifiedArgs`/`updatedInput` 和 `additionalContext` 字段
- 自定义 Agent 的 `model` 字段现在接受 VS Code 的显示名称和供应商后缀（如 "Claude Sonnet 4.5"、"GPT-5.4 (copilot)"）
- 重新设计退出界面，展示 Copilot 吉祥物和更清爽的使用摘要布局

### 🐛 修复

- CLI 崩溃（如 OOM 或 segfault）后终端状态（备用屏幕、光标、原始模式）正确恢复
- 首次运行在 GitHub 仓库中时，会话同步提示出现时正确遵循 `--remote` 标志

---

## 📦 1.0.23（2026-04-10）

> 📝 **笔记定位**：[CLI 模式标志](../modes/index.md#-programmatic-模式) · [快捷键](../basic/index.md#-快捷键)

### ✨ 新功能

- **`--mode`、`--autopilot` 和 `--plan` 标志**：直接以指定 Agent 模式启动 CLI
- ++ctrl+l++ 清除终端屏幕但不清除对话会话
- `/diff`、`/agent`、`/feedback`、`/ide` 和 `/tuikit` 在 Agent 运行时可用
- 在每模型 token 使用明细中显示 reasoning token 用量（非零时）

### 🔧 改进

- 斜杠命令选择器显示完整 Skill 描述和改进的滚动条
- Remote 标签页正确显示 Copilot coding agent 任务并支持通过 Tasks API 转向
- `.vscode/mcp.json` 迁移提示现在包含 `jq` 命令将配置迁移到 `.mcp.json`

### 🐛 修复

- 修复 memory 后端不可用时 Agent 在首轮挂起
- 修复 Bazel/Buck 构建目标标签（如 `//package:target`）被误识别为文件路径
- 修复包含 BEL 字符的 shell 输出导致终端反复蜂鸣

---

## 📦 1.0.22（2026-04-09）

> 📝 **笔记定位**：[Sub-agent 并发限制](../agents/index.md#内置-agent-的协作方式) · [Agent Skills 字段](../agents/index.md) · [MCP 配置源精简](../mcp/index.md#-配置文件层级) · [Hook 触发修复](../hooks/index.md#-生命周期事件)

### ✨ 新功能

- **Sub-agent 深度和并发限制**：防止 Agent 无限递归生成子 Agent
- **Agent Skills 字段**：自定义 Agent 可声明 `skills` 字段，启动时将 Skill 内容预加载到 Agent 上下文中
- **插件安装后提示**：插件安装完成后可显示设置说明消息
- **插件跨会话持久化**：插件在会话间保持启用状态，启动时根据用户配置自动安装
- 恢复会话时如果该会话已被其他 CLI 或应用使用，显示警告

### 🔧 改进

- MCP 工具的非标准 JSON Schema 现在会自动清理，兼容所有模型提供商
- 改进 MCP 和 Extension 工具返回的大图片处理
- 使用新的简化内联渲染器提升渲染性能
- 远程会话被组织策略阻止时，显示联系管理员的明确提示
- Sub-agent 活动不再显示重复的工具名称（如"view view the file..."）
- 斜杠命令选择器移至文本输入框上方，布局更稳定

### 🐛 修复

- 修复受 V8 引擎 grapheme 分段 bug 影响的系统上 CLI 崩溃
- 修复 `sessionStart` 和 `sessionEnd` Hook 在交互模式下每个提示触发一次，改为每个会话只触发一次
- 修复通过 BYOM/BYOK 配置使用 Anthropic 模型时，权限检查和其他 Hook 无法正常工作的问题
- 修复插件 Agent 未遵循 frontmatter 中指定的模型配置

### 🗑️ 移除

- 移除 `.vscode/mcp.json` 和 `.devcontainer/devcontainer.json` 作为 MCP 服务器配置源，CLI 现在只读取 `.mcp.json`。检测到 `.vscode/mcp.json` 但没有 `.mcp.json` 时显示迁移提示

---

## 📦 1.0.21（2026-04-07）

> 📝 **笔记定位**：[MCP 管理命令](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- **`copilot mcp` 命令**：新增 MCP 服务器管理命令
- Hook payload 格式兼容：使用 PascalCase 事件名配置的 Hook 现在接收 VS Code 兼容的 snake_case payload，包含 `hook_event_name`、`session_id` 和 ISO 8601 时间戳

### 🔧 改进

- 长时间运行的异步 shell 命令执行时 spinner 不再卡住
- Enterprise GitHub URL 输入框在登录流程中支持键盘输入和 Enter 提交
- 斜杠命令选择器过滤时不再闪烁或移动输入位置
- 时间线在内容缩减（如取消操作或工具完成）后不再变空白
- Plan 模式时间线显示用户文本时不再重复添加 "Plan" 前缀
- 自动关闭不再需要的 shell 会话，减少内存使用

---

## 📦 1.0.20（2026-04-07）

### ✨ 新功能

- **`copilot help monitoring`**：新增 OpenTelemetry 配置详情和示例的帮助主题

### 🔧 改进

- Spinner 持续显示直到后台 Agent 和 shell 命令完成，期间用户仍可输入
- Azure OpenAI BYOK 未配置 API 版本时默认使用 GA 无版本 v1 路由
- 减少实时响应流式传输时的 UI 卡顿
- `/yolo` 和 `--yolo` 行为统一，且 `/yolo` 状态跨 `/restart` 保持

---

## 📦 1.0.19（2026-04-06）

> 📝 **笔记定位**：[MCP 持久化](../mcp/index.md#-管理-mcp-服务器) · [Hook macOS 权限](../hooks/index.md#-实战示例) · [Agent 文件名映射](../agents/index.md#-agent-存放位置)

### 🔧 改进

- `/mcp enable` 和 `/mcp disable` 现在跨会话持久化
- OpenTelemetry 监控：子 Agent span 使用 `INTERNAL` span kind，聊天 span 新增 `github.copilot.time_to_first_chunk` 属性（仅流式传输）
- 会话已被其他客户端使用时跳过 IDE 自动连接
- 斜杠命令时间线条目现在包含命令名称（如 "Review"、"Plan"），提供更好的上下文

### 🐛 修复

- macOS 上缺少执行权限的 Plugin Hook 脚本现在能正确运行
- 恢复会话时，显示名称与文件名不同的自定义 Agent 能正确恢复

---

## 📦 1.0.18（2026-04-04）

> 📝 **笔记定位**：[Critic Agent](../agents/index.md#-内置-agent) · [Notification Hook](../hooks/index.md#-生命周期事件)

### ✨ 新功能

- **Critic Agent**：新增 Critic Agent，在执行计划和复杂实现时自动使用互补模型进行审查，提前捕获错误（Claude 模型实验性功能）
- **Notification Hook**：新增 `notification` Hook 事件，在 shell 命令完成、权限提示、对话弹窗和 Agent 完成时异步触发

### 🔧 改进

- `preToolUse` Hook 的 `permissionDecision` 设为 `allow` 时，不再弹出工具审批确认
- Session Resume 选择器首次使用时能正确按分支和仓库分组

---

## 📦 1.0.17（2026-04-03）

> 📝 **笔记定位**：[内置 Skills](../skills/index.md#-skills-存放位置) · [MCP OAuth HTTPS](../mcp/index.md#-mcp-oauth-认证)

### ✨ 新功能

- **内置 Skills**：CLI 开始内置 Skills，首批包含 Copilot Cloud Agent 环境定制指南
- **MCP OAuth HTTPS 支持**：MCP OAuth 流程支持通过自签名证书回退实现 HTTPS 重定向 URI，兼容要求 HTTPS 的 OAuth 提供商（如 Slack）

### ⚡ 性能

- `/resume` Session 选择器加载速度大幅提升，特别是在大量会话历史的情况下

---

## 📦 1.0.16（2026-04-02）

> 📝 **笔记定位**：[PermissionRequest Hook](../hooks/index.md#-生命周期事件) · [MCP 工具显示](../mcp/index.md#-管理-mcp-服务器)

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

> 📝 **笔记定位**：[MCP OAuth 认证](../mcp/index.md#-mcp-oauth-认证) · [postToolUseFailure Hook](../hooks/index.md#-生命周期事件) · [/share 导出](../workflows/index.md#导出会话share) · [配置 camelCase](../instructions/index.md#-配置文件补充)

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


---

## 📦 1.0.13（2026-03-30）

> 📝 **笔记定位**：[/rewind 时间线回滚](../context/index.md#undo-和-rewind撤销操作) · [MCP Sampling](../mcp/index.md#-mcp-sampling)

### ✨ 新功能

- **/rewind 时间线回滚**：`/rewind` 和双击 ++esc++ 打开时间线选择器，可回滚到对话历史中的任意节点
- **MCP Sampling 支持**：MCP 服务器可请求 LLM 推理（sampling），需用户通过审查提示批准

### 🔧 改进

- 使用 classic PAT 时显示清晰的错误提示信息

### 🐛 修复

- 会话结束时正确清理 shell 子进程
- MCP 服务器使用 Microsoft Entra ID 认证时不再每次登录都显示同意界面
- grep 工具处理大文件和长行时不再内存溢出
- 卸载 Marketplace 插件时同时清除磁盘上的缓存数据

---

## 📦 1.0.12（2026-03-26）

> 📝 **笔记定位**：[settings.json 配置源](../instructions/index.md#-配置文件补充) · [/rename 自动命名](../context/index.md#-会话管理) · [/allow-all 子命令](../basic/index.md#-常用斜杠命令速查)

### ✨ 新功能

- **Plugin Hook 环境变量**：Plugin Hook 接收 `CLAUDE_PROJECT_DIR` 和 `CLAUDE_PLUGIN_DATA` 环境变量，Hook 配置支持模板变量
- **模型选择器全屏视图**：模型选择器以全屏视图打开，支持方向键内联调整 reasoning effort
- **`/session rename` 自动命名**：无参数调用时自动根据对话历史生成会话名称
- **`/allow-all` 子命令**：`/allow-all`（`/yolo`）支持 `on`、`off` 和 `show` 子命令
- 读取 `.claude/settings.json` 和 `.claude/settings.local.json` 作为额外的仓库配置源
- OSC 8 超链接在 VS Code 终端中可点击
- ++ctrl+y++ 在计划模式下打开最近的研究报告（无计划时）

### 🔧 改进

- `/clear` 保留新会话中的 MCP 服务器
- 模型显示标题旁显示当前 reasoning effort 级别
- 恢复会话时还原之前选中的自定义 Agent
- 用户输入按 ++enter++ 后立即显示在对话中

### 🐛 修复

- `.mcp.json` 定义的 MCP 服务器在工作目录为 git 根目录时正确启动
- Windows 上当非系统 `clip.exe` 在 PATH 中优先时剪贴板复制正常工作
- `/diff` 在存在行内高亮时正确渲染所有行
- Workspace MCP 服务器正确加载并对 Agent 可见
- PowerShell `/flag` 参数不再被误识别为文件路径
- Windows OneDrive 路径不再错误触发受信任文件夹访问提示
- `@` 文件选择器不再显示 `.git` 目录内容
- 终端调整大小时滚动位置保持不变
- `/yolo` 路径权限在 `/clear` 后持久化
- Emoji 字符在终端文本选择中正确选中和高亮
- 有活跃工作的会话不再被过期会话回收器清理
- 运行产生大量输出的 shell 命令时 CLI 不再内存溢出崩溃
- Autopilot 取消时多次按 ++esc++ 不再导致会话卡死

### 🗑️ 移除

- 移除 `--alt-screen` 标志和 `alt_screen` 设置，备用屏幕缓冲区现在始终启用

---

## 📦 1.0.11（2026-03-23）

> 📝 **笔记定位**：[个人 Skills 目录](../skills/index.md#-skills-存放位置) · [/clear vs /new](../context/index.md#clear清除上下文) · [Monorepo 支持](../instructions/index.md#-仓库级指令) · [Hook 合并](../hooks/index.md#-创建-hook)

### ✨ 新功能

- **个人 Skills 目录**：新增 `~/.agents/skills/` 作为个人 Skill 发现目录
- **Monorepo 完整支持**：自定义指令、MCP 服务器、Skills 和 Agents 在从工作目录到 git 根目录的每个层级被发现
- **`/clear` 和 `/new` 语义分离**：`/clear` 完全放弃当前会话，`/new` 开始新对话
- `/cd` 为每个会话保持独立的工作目录，切换会话时自动恢复
- `/new` 和 `/clear` 命令支持可选的 prompt 参数

### 🔧 改进

- MCP 服务器被策略阻止时显示警告
- 组织级第三方 MCP 服务器策略对所有用户强制执行
- 多个扩展的 Hook 现在合并而非覆盖
- `sessionStart` Hook 的 `additionalContext` 注入到对话中
- GitHub MCP 服务器连接远程主机时遵守用户配置
- 启动消息显示已加载的 Hook 数量
- 后台 Agent 进度在 `read_agent` 和任务超时响应中展示
- `statusLine.command` 路径支持 `~` 和环境变量

### 🐛 修复

- 模型选择器正确显示模型，尽可能使用模型显示名称
- 终端在进程挂起和恢复（++ctrl+z++ / `fg`）后正确重绘
- MCP OAuth 兼容支持 Dynamic Client Registration 的服务器（如 Atlassian Rovo MCP Server）

---

## 📦 1.0.10（2026-03-20）

> 📝 **笔记定位**：[/undo 撤销](../context/index.md#undo-和-rewind撤销操作) · [/copy HTML 格式](../basic/index.md#-常用斜杠命令速查)

### ✨ 新功能

- **`/undo` 命令**：撤销最后一轮对话并回滚文件更改
- **`/copy` HTML 格式**：Windows 上 `/copy` 写入格式化 HTML 到剪贴板，可直接粘贴到 Word、Outlook 和 Teams
- **多会话实验性支持**：支持多个并发会话（实验性功能）
- **模型选择器分组视图**：模型重新组织为"可用"、"已阻止/禁用"和"升级"选项卡
- 新增 `--effort` 作为 `--reasoning-effort` 的简写别名
- 通过 `--plugin-dir` 加载的插件在 `/plugin list` 的"外部插件"分区中显示

### ⚡ 性能

- 查看大文件时减少内存使用

### 🔧 改进

- `/login` 设备流在 Codespaces 和远程终端环境中正常工作
- 方向键在使用 Application Keypad Mode 的终端中正确工作
- 仓库 Hook 在 prompt 模式（`-p` 标志）下正确触发
- 配置键名统一为 camelCase：`includeCoAuthoredBy`、`effortLevel`、`autoUpdatesChannel`、`statusLine`
- Workspace MCP 服务器在确认文件夹信任后才加载

### 🐛 修复

- `--server` 模式远程会话中正确检测工作目录
- 备用屏幕模式下 Markdown 无序列表正确渲染
- 通过 `/quit`、++ctrl+c++ 或重启退出时会话历史不再丢失
- Hook 嵌套结构中的 matcher 过滤器正确应用
- 使用 `.claude-plugin/` 或 `.plugin/` 清单目录的插件正确加载
- `/terminal-setup` 不再向 WSL 用户显示误导性错误

---

## 📦 1.0.9（2026-03-19）

### 🔧 改进

- 新增 `include_gitignored` 配置项，允许 `@` 文件搜索包含 gitignored 文件

### 🐛 修复

- SSH 断开或终端关闭时不再出现虚假的 I/O 错误消息
- WSL 上复制文本时正确保留 CJK 及其他非 ASCII 字符
- 从缩短 URL（如 `aka.ms` 链接）安装 Marketplace 和插件现在正常工作

---

## 📦 1.0.8（2026-03-18）

> 📝 **笔记定位**：[Hook 配置位置](../hooks/index.md#-创建-hook) · [MCP Allowlist](../mcp/index.md#-mcp-安全策略) · [Extension 模式](../plugins/index.md#-extensions实验性)

### ✨ 新功能

- **Extension 模式设置**：新增扩展模式设置控制可扩展性
- **MCP Allowlist 验证**：MCP 服务器可通过 `MCP_ALLOWLIST` 功能标志对配置的注册表进行验证
- `--resume` 支持接受 task ID（除 session ID 外）
- 支持在 `settings.json`、`settings.local.json` 和 `config.json` 中定义 Hook

### 🔧 改进

- Agent 模式标签和边框在非 truecolor 终端上显示正确颜色
- 备用屏幕缓冲区默认启用，提供更干净的终端体验
- 仓库级 Hook 在确认文件夹信任后才加载
- 空闲子 Agent 在 `/tasks` 视图中 2 分钟后自动隐藏
- prompt 模式下 ++ctrl+c++ 立即退出
- 对话框标题在所有对话框中一致显示

### 🐛 修复

- 扩展子进程加入活跃会话时退出计划模式工具保持可用
- macOS Terminal.app 中滚动正常工作
- tmux 中从外部编辑器返回后鼠标滚动正常
- Spinner 动画不再延迟时间线中的可见输出

---

## 📦 1.0.7（2026-03-17）

> 📝 **笔记定位**：[subagentStart Hook](../hooks/index.md#-生命周期事件) · [System Prompt 自定义](../instructions/index.md#-配置文件补充)

### ✨ 新功能

- **GPT-5.4 mini**：新增 `gpt-5.4-mini` 模型支持
- **System Prompt 自定义**：新增 `customize` 模式用于系统消息配置中的分区级 System Prompt 覆盖
- **`subagentStart` Hook**：新增子 Agent 启动时触发的 Hook 事件

### 🔧 改进

- 提升 CLI 各主题的颜色对比度，增强可读性和无障碍访问
- 用户消息以微妙的背景色显示，增强视觉区分
- Tab 栏选中标签使用紧凑的 `[label]` 样式
- 双击 ++esc++ 在有文本时清除输入，空白时触发撤销
- 分支指示器区分未暂存（`*`）、已暂存（`+`）和未跟踪（`%`）更改
- Pro 和试用用户在模型选择器中看到所有有权限的模型

### 🐛 修复

- 恢复 1.0.6 之前创建的会话不再失败
- CLI 重启不再重新发送 `-i`/`--interactive` 提示
- 修复自动更新在 Windows 上可能留下不完整包的边缘情况

---

## 📦 1.0.6（2026-03-16）

> 📝 **笔记定位**：[Hook 跨工具兼容](../hooks/index.md#-hook-配置字段) · [子 Agent 可读 ID](../agents/index.md#-使用-agent) · [applyTo 数组写法](../instructions/index.md#-路径特定指令) · [Extensions 权限](../plugins/index.md#-extensions实验性)

### ✨ 新功能

- **动态工具搜索**：模型可通过 Tool Search 动态发现和使用工具（Claude 模型）
- **Open Plugin 规范支持**：支持 Open Plugin 规范文件位置，提升兼容性
- Extension 工具接入权限系统
- 子 Agent 获得基于名称的可读 ID（如 `math-helper-0`）
- `create_pull_request` 工具输出中包含 PR URL

### ⚡ 性能

- 释放 shell 命令解析后的 tree-sitter WASM 对象，防止内存泄漏
- 消除冗余环境变量副本，减少内存使用
- 优化流式输出和工具输出的内存使用

### 🔧 改进

- Autopilot 模式下 `task_complete` 摘要为必填项，并以 Markdown 渲染
- 指令文件 frontmatter `applyTo` 同时接受字符串和数组值
- Hook 配置文件兼容 VS Code、Claude Code 和 CLI，无需修改
- Hook 配置文件支持 Claude Code 的嵌套 matcher/hooks 结构
- 自定义指令文件路径通过 `COPILOT_CUSTOM_INSTRUCTIONS_DIRS` 正确加载
- Agent 创建向导显示正确的用户 Agents 目录路径
- `/tasks` 视图中子 Agent 的耗时在空闲时冻结、活跃时恢复
- 通过 `--plugin-dir` 发现使用 `.claude-plugin/plugin.json` 的插件

### 🐛 修复

- Autopilot 在错误后不再永久阻塞
- 输入占位文本不再被屏幕阅读器朗读
- `/help` 对话框在备用屏幕模式下从顶部开始滚动
- Windows 上自动更新正确处理竞态条件
- Windows 上更新时另一实例运行不再导致 CLI 加载失败
- Copilot Free 用户的剩余请求控件不再显示不准确的配额数据
- 修复 HTTP/2 连接池竞态条件导致的会话崩溃
- 自动更新后 CLI 加载最新版本
- Kill 命令验证不再误阻合法命令
- 恢复会话时 Hook 正确触发
- 备用屏幕模式下 prompt 输入渲染所有行不被截断
- VS Code 终端中链接和右键粘贴不再触发两次
- 原生模块预构建在首次启动时可靠加载
- 命令导致 shell 退出时输出不再丢失

---

## 📦 1.0.5（2026-03-13）

> 📝 **笔记定位**：[preCompact Hook](../hooks/index.md#-生命周期事件) · [write_agent 工具](../agents/index.md#-使用-agent) · [/pr 命令](../workflows/index.md#pr-操作) · [新增命令](../basic/index.md#-常用斜杠命令速查)

### ✨ 新功能

- **`/pr` 命令**：创建和查看 PR、修复 CI 失败、处理 Review 反馈、解决合并冲突
- **`/extensions` 命令**：查看、启用和禁用 CLI 扩展
- **Diff 语法高亮**：`/diff` 支持 17 种编程语言的语法高亮
- **`/changelog` 增强**：支持 `last <N>`、`since <version>` 和 `summarize` 子命令
- **`preCompact` Hook**：Context Compaction 前运行自定义命令的新 Hook 事件
- **`write_agent` 工具**：向后台 Agent 发送跟进消息
- **动态指令检索**：实验性的基于 Embedding 的 MCP 和 Skill 指令动态检索（每轮）
- `@` 文件引用支持项目外路径：绝对路径、home 目录、相对父目录
- 新增 `/version` 命令显示 CLI 版本并检查更新

### 🔧 改进

- `/clear` 或 `/new` 后终端标题重置为默认
- `/experimental on|off` 切换实验模式后自动重启
- 右键粘贴定向到活跃的对话输入框
- 阻止网络（UNC）路径以防止通过 SMB 泄露凭据
- Memory 存储错误提示仓库不存在或缺少写权限
- 环境变量中使用 classic PAT 时显示清晰错误
- `claude-sonnet-4.6` 作为默认模型正确保留
- 错误重试耗尽后时间线中显示 Request ID

### 🐛 修复

- Windows CRLF 环境下 Diff 视图正确显示
- 修复关闭时的 Kitty 键盘协议转义序列问题
- 插件卸载可靠地删除所有文件
- Windows/PowerShell 上含反引号格式代码的 PR 描述正确渲染

---

## 📦 1.0.4（2026-03-11）

> 📝 **笔记定位**：[Hook 权限控制](../hooks/index.md#-hook-配置字段) · [configure-copilot Agent](../agents/index.md#-内置-agent) · [--reasoning-effort](../modes/index.md#-programmatic-模式)

### ✨ 新功能

- **自适应颜色引擎**：动态颜色模式和交互式主题选择器
- **OpenTelemetry 可观测性**：启用 OpenTelemetry 埋点
- **`--reasoning-effort` 标志**：通过命令行直接设置 reasoning effort
- **`configure-copilot` 子 Agent**：专用于管理 MCP 服务器、Agents 和 Skills 的子 Agent
- Extensions 支持以 CommonJS 模块编写
- 新增 `disableAllHooks` 标志禁用所有 Hook
- Hook 支持 `ask` 权限决策请求用户确认

### 🔧 改进

- `/pr open` 替换为 `/pr view [local|web]`
- 支持 Azure DevOps 仓库识别
- 会话导出头部每个字段独占一行
- `/instructions` 选择器中显示单个指令文件名
- 路径权限对话框提供一次性批准选项
- Windows 上跳过 PowerShell Profile 加载，加快 shell 命令执行
- 改进 CLI 帮助文档格式

### 🐛 修复

- MCP OAuth 重新认证稳定工作
- SAML 强制执行错误时自动更新不带认证令牌重试
- Autopilot 模式在 API 错误后停止继续而非循环
- 状态栏上下文窗口百分比不再跨轮次膨胀
- CLI 崩溃时终端正确重置
- `/update` 命令自动重启以应用更新
- OAuth 认证可靠处理 Microsoft Entra ID 和其他 OIDC 服务器

---

## 📦 1.0.3（2026-03-09）

> 📝 **笔记定位**：[DevContainer MCP](../mcp/index.md#-配置文件层级) · [Extensions 实验性](../plugins/index.md#-extensions实验性) · [/restart 命令](../basic/index.md#-常用斜杠命令速查)

### ✨ 新功能

- **Extensions 实验性功能**：Extensions 现已作为实验性功能可用
- **DevContainer MCP 配置**：从 `.devcontainer/devcontainer.json` 读取 MCP 服务器配置
- **`/restart` 命令**：热重启 CLI 同时保留会话
- 新增 `--binary-version` 标志
- 新增 `extraKnownMarketplaces` 设置
- `/terminal-setup` 新增 Windows Terminal 支持

### 🔧 改进

- 内部用户默认启用备用屏幕缓冲区
- 文档化 `GH_HOST`、`HTTP_PROXY`、`HTTPS_PROXY`、`NO_COLOR`、`NO_PROXY` 环境变量
- 后台任务通知在时间线中显示
- 输入 `quit` 退出 CLI
- `/reset-allowed-tools` 完全撤销 `/allow-all`
- 改进 SQL 工具中的批量查询
- `/add-dir` 目录在 `/clear` 和 `/resume` 后持久化
- 防止 `env` 命令被自动允许
- 信任安全的 `sed` 命令
- 配置键名 `merge_strategy` 重命名为 `mergeStrategy`

### 🐛 修复

- Ubuntu 上 keyring 无响应时登录流程不再挂起
- CLI 崩溃时终端正确重置
- 具有非标准 `outputSchema` 的 MCP 服务器可正常访问
- `/plugin update` 对 GitHub 安装的插件和项目设置中的 Marketplace 正常工作

---

## 📦 1.0.2（2026-03-06）— 🎉 GA 正式发布

> 📝 **笔记定位**：[Hook 配置别名](../hooks/index.md#-hook-配置字段)

!!! success "General Availability"
    此版本标志着 GitHub Copilot CLI 正式发布（GA），从 0.x 预览阶段进入稳定版本。

### 🔧 改进

- 输入 `exit` 关闭 CLI
- `ask_user` 表单支持 ++enter++ 键提交，枚举字段允许自定义输入
- Hook 配置支持 `command` 字段作为 `bash`/`powershell` 的跨平台别名
- Hook 配置接受 `timeout` 作为 `timeoutSec` 的别名

### 🐛 修复

- 修复 Meta 与控制键的组合处理（包括 `/terminal-setup` 的 ++shift+enter++）

---

## 📦 0.0.423（2026-03-06）— 最终预览版

> 📝 **笔记定位**：[MCP 带外交互](../mcp/index.md#-mcp-oauth-认证)

!!! info "最终 Pre-GA 版本"
    这是 GA 正式发布前的最后预览版本之一。

### ✨ 新功能

- **MCP 带外交互**：MCP 服务器可请求用户访问 URL 进行带外交互（OAuth、API Key 等）

### 🔧 改进

- 具有潜在危险扩展或替换的 shell 命令会提示用户确认
- 为 EMU 和 GHE Cloud 用户阻止 `/share gist`
- Elicitation 枚举和布尔字段需要按 ++enter++ 确认
- 提升 explore Agent 精度和大型仓库支持

### 🐛 修复

- Windows CRLF 环境下 Diff 模式正确显示

---

## 📦 0.0.422（2026-03-05）— 最终预览版

> 📝 **笔记定位**：[个人 Hook 目录](../hooks/index.md#-创建-hook) · [快捷键增强](../basic/index.md#-快捷键) · [enabledPlugins](../plugins/index.md#-extensions实验性)

!!! info "最终 Pre-GA 版本"
    这是 GA 正式发布前的最后预览版本之一。

### ✨ 新功能

- **GPT-5.4 模型**：新增 GPT-5.4 模型支持
- **`/copy` 命令**：新增 `/copy` 命令
- **历史搜索**：++ctrl+r++ 反向增量搜索命令历史
- **启动 Prompt Hook**：新增启动 Prompt Hook，会话开始时自动提交 prompt
- **自动插件安装**：配置中支持 `enabledPlugins` 自动安装插件
- 新增 `copy_on_select` 配置项
- `/diff` 在备用屏幕模式下支持鼠标滚轮
- prompt 模式下新增 `--output-format json` 标志输出 JSONL
- 后台 shell 命令和 Agent 完成时自动通知
- 插件贡献的 LSP 服务器在 `/lsp show` 中加载和显示

### ⚡ 性能

- 减少备用屏幕模式下的内存使用

### 🔧 改进

- 认证错误中显示 Request ID
- 从 `~/.copilot/hooks` 加载个人 Hook
- 时间线以框体显示问题并标注"Making best guess on autopilot"指示器
- `@` 文件补全反映工作目录的当前状态
- 备用屏幕模式下链接以下划线样式渲染
- `/delegate` 在多 remote 仓库中提示选择目标 remote
- GitHub MCP 服务器在同时存在 Azure DevOps 和 GitHub remote 时保持启用
- Markdown 表格内联代码中的冒号正确渲染
- 隐藏 todo 记录的冗余查询
- 支持从 `ssh://` URL 安装插件
- `launch_messages` 重命名为 `companyAnnouncements`
- 仓库配置从 `.github/copilot/config.json` 重命名为 `settings.json`

### 🐛 修复

- 插件缓存自动从损坏的克隆中恢复
- 未安装 git 时显示清晰错误
- CJK 输入时 IME 候选窗口在正确的光标位置显示
- `git color.diff=always` 设置下 Diff 模式正常工作
- Windows 上打开含 `&` 查询参数的 URL 正常处理
- tmux 中 ++esc++ 取消键正常工作
- prompt 输入中点击可重新定位文本光标
- 大量文件时 CLI 不再挂起数分钟
