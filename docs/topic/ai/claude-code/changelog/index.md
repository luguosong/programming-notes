---
title: 更新日志
description: Claude Code 各版本更新内容汇总，追踪新功能、改进和修复
---

# 更新日志

本页整理了 Claude Code 各版本的重要更新。数据来源于 [GitHub Releases](https://github.com/anthropics/claude-code/releases)。

💡 建议保持 Claude Code 更新到最新版本，新版本通常包含性能改进和 Bug 修复：

``` bash
npm update -g @anthropic-ai/claude-code
```

---

## 📦 2.1.117（2026-04-22）

> 📝 **笔记定位**：[Forked Subagent](../sub-agents/index.md) · [Agent MCP 加载](../mcp/index.md) · [/model 持久化](../configuration/index.md) · [/resume 智能摘要](../context-engineering/index.md) · [MCP 并发连接](../mcp/index.md) · [插件依赖改进](../plugins/index.md) · [原生构建搜索](../how-it-works/index.md) · [Opus 4.7 上下文窗口](../how-it-works/index.md) · [清理周期扩展](../configuration/index.md) · [OpenTelemetry 增强](../enterprise/index.md)

### ⚡ 性能

- **原生构建内嵌搜索工具**：macOS/Linux 原生构建中 `Glob` 和 `Grep` 工具被内嵌的 `bfs` 和 `ugrep` 替代，通过 Bash tool 直接调用，无需单独工具往返（Windows 和 npm 安装版本不变）
- **MCP 并发连接默认启用**：同时配置本地和 claude.ai MCP 服务器时启动更快
- **Windows `where.exe` 缓存**：按进程缓存可执行文件查找结果，加速子进程启动

### ✨ 新功能

- **Forked Subagent 外部构建支持**：通过 `CLAUDE_CODE_FORK_SUBAGENT=1` 在外部构建中启用 forked subagent
- **Agent frontmatter `mcpServers` 加载**：通过 `--agent` 运行主线程代理时加载 agent frontmatter 中声明的 MCP 服务器
- **Advisor Tool 改进（实验性）**：对话框增加"实验性"标签和学习链接；启动时通知；修复会话卡在"Advisor tool result content could not be processed"错误
- **`cleanupPeriodDays` 清理范围扩展**：现在也清理 `~/.claude/tasks/`、`~/.claude/shell-snapshots/` 和 `~/.claude/backups/`

### 🔧 改进

- **`/model` 选择持久化**：模型选择跨重启保留，即使项目固定了不同模型；启动头信息显示活动模型来源（项目或托管设置固定）
- **`/resume` 智能摘要**：恢复过期的大型会话前提供摘要选项，与 `--resume` 行为一致
- **插件依赖改进**：`plugin install` 对已安装插件现在安装缺失依赖而非停在"already installed"；依赖错误提示"not installed"并给出安装提示；`plugin marketplace add` 自动从已配置市场解析缺失依赖
- **托管设置市场管控**：`blockedMarketplaces` 和 `strictKnownMarketplaces` 现在在插件安装、更新、刷新和自动更新时强制执行
- **OpenTelemetry 增强**：`user_prompt` 事件新增 `command_name` 和 `command_source`（斜杠命令）；`cost.usage`、`token.usage`、`api_request` 和 `api_error` 新增 `effort` 属性；自定义/MCP 命令名默认脱敏（需 `OTEL_LOG_TOOL_DETAILS=1` 显示）
- **Opus/Sonnet 4.6 默认 effort 调整**：Pro/Max 订阅用户使用 Opus 4.6 和 Sonnet 4.6 时默认 effort 从 `medium` 提升至 `high`

### 🐛 修复

- 修复 Plain-CLI OAuth 会话在访问令牌过期时报"Please run /login"——现在在 401 时自动刷新令牌
- 修复 `WebFetch` 在超大 HTML 页面上挂起——在 HTML 转 Markdown 前截断输入
- 修复代理返回 HTTP 204 No Content 时崩溃——现在显示清晰错误而非 `TypeError`
- 修复 `CLAUDE_CODE_OAUTH_TOKEN` 环境变量启动时 `/login` 无效（令牌过期场景）
- 修复 prompt 输入撤销（`Ctrl+_`）在刚输入后无反应，以及每步撤销跳过一个状态
- 修复 `NO_PROXY` 在 Bun 运行时不被远程 API 请求尊重
- 修复慢速连接下按键名以合并文本到达时的虚假 escape/return 触发
- 修复 SDK `reload_plugins` 串行重连所有用户 MCP 服务器
- 修复 Bedrock application-inference-profile 在 Opus 4.7 且禁用思考时 400 错误
- 修复 MCP `elicitation/create` 请求在 print/SDK 模式下服务器连接完成后自动取消
- 修复 subagent 运行不同模型时错误触发文件读取恶意软件警告
- 修复有后台任务时空闲重渲染循环，减少 Linux 内存增长
- 修复 VS Code "Manage Plugins" 面板在配置多个大型市场时崩溃
- 修复 Opus 4.7 会话显示虚高的 `/context` 百分比并过早自动压缩——按 200K 上下文窗口计算而非 Opus 4.7 原生 1M

---

## 📦 2.1.116（2026-04-20）

> 📝 **笔记定位**：[/resume 性能](../context-engineering/index.md) · [MCP 启动](../mcp/index.md) · [思考进度](../how-it-works/index.md) · [/config 搜索](../configuration/index.md) · [插件依赖](../plugins/index.md) · [沙箱安全](../configuration/index.md#-配置层级与优先级)

### ⚡ 性能

- **`/resume` 速度提升 66%+**：大型会话（40MB+）加载更快，更高效处理含多个死分支的会话
- **MCP 启动加速**：配置多个 stdio 服务器时启动更快；`resources/templates/list` 延迟到首次 `@` 提及时加载
- **终端滚动优化**：VS Code、Cursor 和 Windsurf 中全屏滚动更流畅，`/terminal-setup` 可配置编辑器滚动灵敏度

### ✨ 新功能

- **思考进度内联显示**：思考旋转器内联显示进度（"still thinking"、"thinking more"、"almost done thinking"）
- **`/config` 搜索增强**：可匹配选项值（如搜索 "vim" 可找到编辑器模式设置）
- **`/doctor` 异步打开**：无需等待当前轮次完成，可在 Claude 响应时打开
- **插件依赖自动安装**：`/reload-plugins` 和后台插件自动更新可从已添加的市场自动安装缺失的依赖
- **GitHub API 限流提示**：`gh` 命令触发 GitHub API 限流时显示提示，便于 Agent 主动退避
- **Usage 标签页即时显示**：Settings 中的 Usage 标签页立即显示 5 小时和每周用量，限流时不再失败
- **Agent 前置事项钩子**：通过 `--agent` 运行主线程代理时，`hooks:` 前置事项可触发
- **斜杠命令无匹配提示**：筛选结果为零时显示 "No commands match"

### 🔒 安全

- **沙箱 `rm`/`rmdir` 加固**：针对 `/`、`$HOME` 或其他关键系统目录不再绕过危险路径安全检查

### 🐛 修复

- 修复天城文字和印度文字在终端 UI 中列对齐错误
- 修复 VS Code 集成终端滚动时的空白单元格和界面消失问题
- 修复短终端高度时模态搜索对话框溢出屏幕、遮盖搜索框和键盘提示
- 修复 Ctrl+- 在 Kitty 协议终端（iTerm2、Ghostty、WezTerm 等）中不触发撤销
- 修复 Cmd+Left/Right 在 Kitty 协议终端（Warp、kitty、Ghostty、WezTerm）中不跳转行首/行尾
- 修复通过包装进程启动（`npx`、`bun run`）时 Ctrl+Z 挂起终端
- 修复内联模式中终端调整大小或大量输出时的回滚重复问题
- 修复并行请求期间的间歇性 API 400 错误（缓存控制 TTL 顺序）
- 修复 `/branch` 拒绝大于 50MB 的会话文本记录
- 修复 `/resume` 在大型会话文件上无声显示空对话
- 修复 `/plugin` Installed 标签页重复显示条目
- 修复 `/update` 和 `/tui` 在进入工作树后不工作

---

## 📦 2.1.114（2026-04-17）

### 🐛 修复

- 修复 Agent Teams 队友请求工具权限时权限对话框崩溃

---

## 📦 2.1.113（2026-04-17）

> 📝 **笔记定位**：[原生二进制 / Windows 特性](../platforms/index.md#-终端-cli) · [Remote Control 扩展](../automation/index.md#remote-control-命令扩展) · [`/loop` & `/ultrareview`](../automation/index.md#loop云端定时任务) · [Subagent 卡死处理](../sub-agents/index.md) · [Sandbox 安全收紧](../configuration/index.md#-配置层级与优先级) · [MCP 并发与 ToolSearch](../mcp/index.md#动态工具更新) · [插件依赖冲突](../plugins/index.md#插件错误处理与诊断) · [长上下文恢复 compact](../context-engineering/index.md) · [终端快捷键](../how-it-works/index.md#-终端界面与快捷键)

### ✨ 新功能

- **原生二进制分发**：CLI 现在通过每平台可选依赖生成原生 Claude Code 二进制，而非 bundled JavaScript
- **`sandbox.network.deniedDomains` 设置**：即使更广泛的 `allowedDomains` 通配符允许时，仍可阻止特定域名

### 🔧 改进

- **Fullscreen Shift+↑/↓ 滚动**：扩展选择超出可视边缘时滚动视口
- **`Ctrl+A` / `Ctrl+E` readline 行为**：在多行输入中移到当前逻辑行的开头/结尾
- **Windows `Ctrl+Backspace`**：删除前一个单词
- **长 URL 跨行换行可点击**：在支持 OSC 8 hyperlinks 的终端中保持可点击
- **`/loop` 改进**：Esc 取消待执行唤醒，唤醒显示为 "Claude resuming /loop wakeup"
- **`/extra-usage` Remote Control 支持**：现在可在移动/Web 客户端使用
- **Remote Control `@`-file 自动补全**：客户端可查询 `@`-file 补全建议
- **`/ultrareview` 改进**：并行检查更快启动、启动对话框显示 diffstat、启动状态动画化
- **Subagent 卡死检测**：mid-stream 卡死的 subagent 在 10 分钟后明确报错而非静默挂起
- **Bash 多行注释命令**：首行为注释的多行命令现在在 transcript 中显示完整命令，关闭 UI 欺骗向量
- **`cd <current-dir> && git …`**：当 `cd` 是 no-op 时不再触发权限提示
- **macOS 安全加固**：`/private/{etc,var,tmp,home}` 路径在 `Bash(rm:*)` allow 规则下视为危险删除目标
- **Bash deny 规则匹配 wrappers**：现在匹配被 `env`/`sudo`/`watch`/`ionice`/`setsid` 等 exec wrapper 包裹的命令
- **`Bash(find:*)` 安全收紧**：allow 规则不再自动批准 `find -exec` / `-delete`

### 🐛 修复

- 修复 MCP 并发调用超时处理（一个工具调用的消息可能默默撤销另一个调用的看门狗）
- 修复 Cmd-backspace / `Ctrl+U` 重新支持从光标删除到行首
- 修复 markdown 表格在单元格包含含管道字符的行内代码 span 时断裂
- 修复 session recap 在编写未发送的 prompt 文本时自动触发
- 修复 `/copy` "Full response" 未为粘贴到 GitHub/Notion/Slack 对齐 markdown 表格列
- 修复在查看运行中的 subagent 时输入的消息被隐藏并误归属于父 AI
- 修复 Bash `dangerouslyDisableSandbox` 在 sandbox 外运行命令时未触发权限提示
- 修复 `/effort auto` 确认信息（现在显示 "Effort level set to max" 与状态栏一致）
- 修复"已复制 N 字符"提示对 emoji 等多 code-unit 字符过度计数
- 修复 `/insights` 在 Windows 上以 `EBUSY` 崩溃
- 修复退出确认对话框将一次性计划任务误标为重复任务（现显示倒计时）
- 修复 fullscreen 模式下斜杠/@ 补全菜单未紧贴 prompt 边框
- 修复 `CLAUDE_CODE_EXTRA_BODY` `output_config.effort` 在 subagent 调用不支持 effort 的模型和 Vertex AI 上引发 400 错误
- 修复 `NO_COLOR` 设置时 prompt 光标消失
- 修复 `ToolSearch` 排名（粘贴的 MCP 工具名现在返回实际工具而非描述匹配的兄弟工具）
- 修复 compact 已恢复的长上下文会话失败（"Extra usage is required for long context requests"）
- 修复 `plugin install` 在依赖版本与已安装插件冲突时仍成功——现在报告 `range-conflict`
- 修复 "Refine with Ultraplan" 未在 transcript 显示远程会话 URL
- 修复 SDK 图像内容块处理失败时崩溃会话（现在降级为文本占位符）
- 修复 Remote Control 会话不流式传输 subagent transcripts
- 修复 Remote Control 会话在 Claude Code 退出时未归档
- 修复通过 Bedrock Application Inference Profile ARN 使用 Opus 4.7 时 `thinking.type.enabled is not supported` 400 错误

---

## 📦 2.1.112（2026-04-16）

### 🐛 修复

- 修复 auto mode 下 "claude-opus-4-7 is temporarily unavailable" 错误

---

## 📦 2.1.111（2026-04-16）

> 📝 **笔记定位**：[思考力度控制](../how-it-works/index.md) · [/effort 滑块](../skills/index.md) · [/ultrareview](../automation/index.md) · [PowerShell 工具](../platforms/index.md) · [Bash 免确认](../how-it-works/index.md) · [/less-permission-prompts](../skills/index.md) · [API 调试](../enterprise/index.md) · [插件诊断](../plugins/index.md) · [会话恢复](../context-engineering/index.md)

### ✨ 新功能

- **`/effort` 交互式滑块**：不带参数调用 `/effort` 时打开交互式滑块，支持方向键在等级间导航，Enter 确认
- **Opus 4.7 xhigh 推理等级**：为 Opus 4.7 新增 `xhigh` 推理等级，介于 `high` 和 `max` 之间，可通过 `/effort`、`--effort` 和模型选择器使用
- **Auto（匹配终端）主题**：新增 "Auto (match terminal)" 主题选项，自动匹配终端的深色/浅色模式，从 `/theme` 中选择
- **`/less-permission-prompts` skill**：扫描 transcript 中常见的只读 Bash 和 MCP tool 调用，为 `.claude/settings.json` 生成优先级排序的允许列表建议
- **`/ultrareview`**：使用云端并行多 Agent 分析和评审运行全面代码审查，不带参数审查当前分支，或 `/ultrareview <PR#>` 获取并审查指定 GitHub PR
- **Auto mode 免 Flag**：auto mode 不再需要 `--enable-auto-mode` 参数
- **Windows PowerShell tool**：PowerShell 工具逐步推出中，通过 `CLAUDE_CODE_USE_POWERSHELL_TOOL` 选择加入或退出
- **只读 Bash 命令免确认**：带 glob 模式的只读 Bash 命令（如 `ls *.ts`）以及以 `cd <project-dir> &&` 开头的命令不再触发权限确认
- **子命令拼写建议**：输入 `claude <word>` 拼写接近但不对时，建议最接近的子命令（如 `claude udpate` → "你是想说 `claude update`？"）
- **Plan 文件命名改进**：Plan 文件现在根据 prompt 命名（如 `fix-auth-race-snug-otter.md`），而非纯随机词
- **`Ctrl+U` 清空输入**：`Ctrl+U` 现在清空整个输入缓冲区（之前：删除到行首），按 `Ctrl+Y` 恢复
- **`Ctrl+L` 全屏重绘**：`Ctrl+L` 在清空 prompt 输入的同时强制全屏重绘
- **`/skills` 按 Token 数排序**：`/skills` 菜单现在支持按估算 token 数排序，按 `t` 键切换
- **`OTEL_LOG_RAW_API_BODIES` 环境变量**：将完整的 API 请求和响应体作为 OpenTelemetry 日志事件输出，用于调试
- **`/setup-vertex` 和 `/setup-bedrock` 改进**：当 `CLAUDE_CONFIG_DIR` 设置时显示实际的 `settings.json` 路径；重新运行时从已有 pin 中获取候选模型
- **Transcript 底部快捷键提示**：Transcript 视图底部现在显示 `[`（导出到终端回滚）和 `v`（在编辑器中打开）快捷键
- **长粘贴截断标记改进**：截断长粘贴的 "+N lines" 标记改为全宽分隔线，更易扫描
- **`plugin_errors` 事件字段**：headless `--output-format stream-json` 在插件因未满足依赖而被降级时，init 事件中包含 `plugin_errors`

### 🔧 改进

- 抑制 TUI 在正常操作期间可能出现的虚假解压、网络和瞬态错误信息
- 撤销 v2.1.110 中对非流式回退重试的上限设置——该上限在 API 过载时以更多直接失败替代了长时间等待

### 🐛 修复

- 修复 iTerm2 + tmux 环境下发送终端通知时的显示撕裂问题（随机字符、输入漂移）
- 修复 `@` 文件建议在非 git 工作目录中每轮都重新扫描整个项目
- 修复编辑前的 LSP 诊断信息在编辑后出现，导致模型重新读取刚编辑的文件
- 修复 Tab 补全 `/resume` 时立即恢复任意标题的 session 而非显示 session 选择器
- 修复 `/context` 网格渲染在行间多出空行
- 修复 `/clear` 丢失通过 `/rename` 设置的 session 名称
- 修复 Claude 调用不存在的 `commit` skill 并显示 "Unknown skill: commit" 的问题
- 修复 Bedrock/Vertex/Foundry 上的 429 限流错误引用 status.claude.com（该站点仅涵盖 Anthropic 运营的提供商）
- 修复关闭反馈调查后连续弹出的问题
- 修复 bash/PowerShell/MCP tool 输出中的裸 URL 在终端跨行换行时不可点击
- Windows：`CLAUDE_ENV_FILE` 和 SessionStart hook 环境文件现在生效（之前无效）
- Windows：带盘符路径的权限规则现在正确根锚定，仅盘符大小写不同的路径被识别为同一路径
- 插件依赖错误现在区分冲突、无效和过于复杂的版本要求；修复 `plugin update` 后解析版本过期的问题

---

## 📦 2.1.110（2026-04-15）

> 📝 **笔记定位**：[/tui 与 /focus](../how-it-works/index.md) · [推送通知](../automation/index.md) · [Remote Control 扩展](../automation/index.md) · [Windows 特性](../platforms/index.md) · [插件管理](../plugins/index.md) · [Hook 修复](../hooks/index.md) · [SDK 追踪](../enterprise/index.md) · [会话恢复任务](../context-engineering/index.md) · [自动滚动配置](../configuration/index.md)

### ✨ 新功能

- **`/tui` 命令**：新增 `/tui` 命令和 `tui` 设置，运行 `/tui fullscreen` 在同一会话中切换到无闪烁渲染
- **`/focus` 命令**：`Ctrl+O` 改为仅在普通和详细 transcript 之间切换；专注视图现在通过新的 `/focus` 命令单独切换
- **推送通知 tool**：Claude 可在 Remote Control 和 "Push when Claude decides" 配置启用时发送移动端推送通知
- **`autoScrollEnabled` 配置**：新增配置项，可在全屏模式下禁用对话自动滚动
- **`Ctrl+G` 编辑器显示上下文**：新增选项，在 `Ctrl+G` 外部编辑器中以注释形式显示 Claude 的上一条回复（通过 `/config` 启用）
- **`/resume` 恢复计划任务**：`--resume`/`--continue` 现在恢复未过期的计划任务

### 🔧 改进

- `/plugin` Installed 标签页改进：需要关注的项和收藏项置顶，禁用项折叠隐藏，按 `f` 收藏选中项
- `/doctor` 当 MCP server 在多个配置作用域中以不同端点定义时发出警告
- Remote Control 命令扩展：`/autocompact`、`/context`、`/exit` 和 `/reload-plugins` 现在可在 Remote Control（移动端/Web）客户端中使用
- Write tool 现在会在 IDE diff 中编辑了建议内容后再接受时通知模型
- Bash tool 现在强制执行文档中规定的最大超时时间，不再接受任意大的值
- SDK/headless 会话现在从环境变量读取 `TRACEPARENT`/`TRACESTATE` 用于分布式追踪链接
- Session recap 现在对禁用遥测的用户（Bedrock、Vertex、Foundry、`DISABLE_TELEMETRY`）启用，可通过 `/config` 或 `CLAUDE_CODE_ENABLE_AWAY_SUMMARY=0` 退出

### 🐛 修复

- 修复 MCP server 连接在 SSE/HTTP 传输中途断开时 tool 调用无限挂起
- 修复 API 不可达时非流式回退重试导致数分钟挂起
- 修复 session recap、本地斜杠命令输出和其他系统状态行在专注模式下不显示
- 修复全屏模式下 tool 运行时选中文本导致高 CPU 占用
- 修复插件安装不遵守 `plugin.json` 中声明的依赖；`/plugin` 安装现在列出自动安装的依赖
- 修复 `disable-model-invocation: true` 的 skill 通过 `/<skill>` 在消息中间调用时失败
- 修复 `--resume` 有时显示第一个 prompt 而非 `/rename` 设置的名称
- 修复排队消息在多 tool 调用轮次中短暂出现两次
- 修复 session 清理未移除完整 session 目录（包括 subagent transcript）
- 修复 CLI 重启后（如 `/tui`、provider 设置向导）按键丢失
- 修复 macOS Terminal.app 和其他不支持同步输出的终端中启动渲染乱码
- 安全加固："在编辑器中打开"操作防止不受信任的文件名注入命令
- 修复 `PermissionRequest` hook 返回 `updatedInput` 时未重新检查 `permissions.deny` 规则
- 修复 `PreToolUse` hook 的 `additionalContext` 在 tool 调用失败时被丢弃
- 修复向 stdout 输出非 JSON 行的 stdio MCP server 在第一行杂散输出时被断开（v2.1.105 回归）
- 修复 headless/SDK session 自动标题在设置 `CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC` 时触发额外 Haiku 请求
- 修复管道（非 TTY）Ink 输出包含单行超宽行时潜在的大量内存分配
- 修复全屏模式下 `/skills` 菜单在列表超出模态框时不滚动
- 修复 Remote Control session 过期时显示通用错误而非提示重新登录
- 修复从 claude.ai 的 Remote Control session 重命名未持久化标题到本地 CLI session

---

## 📦 2.1.109（2026-04-15）

### 🔧 改进

- 改进 extended-thinking 指示器，新增旋转进度提示

---

## 📦 2.1.108（2026-04-14）

> 📝 **笔记定位**：[缓存 TTL 控制](../context-engineering/index.md) · [Session recap](../context-engineering/index.md) · [/undo 别名](../how-it-works/index.md) · [内置命令发现](../skills/index.md) · [内存优化](../best-practices/index.md) · [会话恢复改进](../context-engineering/index.md) · [模型切换警告](../how-it-works/index.md)

### ✨ 新功能

- **1 小时 Prompt 缓存 TTL**：新增 `ENABLE_PROMPT_CACHING_1H` 环境变量，在 API key、Bedrock、Vertex 和 Foundry 上选择 1 小时缓存 TTL；新增 `FORCE_PROMPT_CACHING_5M` 强制 5 分钟 TTL
- **Session recap**：新增 recap 功能，在返回 session 时提供上下文摘要，可在 `/config` 中配置，手动调用 `/recap`
- **内置斜杠命令发现**：模型现在可以通过 Skill tool 发现和调用内置斜杠命令（如 `/init`、`/review`、`/security-review`）
- **`/undo` 别名**：`/undo` 现在是 `/rewind` 的别名

### 🔧 改进

- `/model` 在对话中途切换模型前发出警告，因为下一条响应会以未缓存方式重新读取完整历史
- `/resume` 选择器默认显示当前目录的 session，按 `Ctrl+A` 显示所有项目
- 服务端限流与计划用量限制现在区分显示；5xx/529 错误显示 status.claude.com 链接
- 通过按需加载语言语法，减少文件读取、编辑和语法高亮的内存占用
- 查看详细 transcript（`Ctrl+O`）时新增 "verbose" 指示器
- 启动时当 `DISABLE_PROMPT_CACHING*` 环境变量禁用了 prompt 缓存时显示警告

### 🐛 修复

- 修复 `/login` 代码输入中粘贴不工作（v2.1.105 回归）
- 修复设置了 `DISABLE_TELEMETRY` 的订阅用户回退到 5 分钟缓存 TTL 而非 1 小时
- 修复 Agent tool 在 auto mode 下当安全分类器的 transcript 超出上下文窗口时仍提示权限
- 修复 `CLAUDE_ENV_FILE` 以 `#` 注释行结尾时 Bash tool 无输出
- 修复 `--resume` 丢失通过 `/rename` 设置的 session 自定义名称和颜色
- 修复首条消息为简短问候时 session 标题显示占位示例文本
- 修复 `--teleport` 后终端转义码在 prompt 输入中显示为乱码
- 修复 `/feedback` 重试：失败后按 Enter 重新提交现在无需先编辑描述即可工作
- 修复 `--teleport` 和 `--resume <id>` 前置条件错误静默退出而非显示错误信息
- 修复 Remote Control session 在 Web UI 中设置的标题在第三条消息后被自动生成标题覆盖
- 修复 `--resume` 在 transcript 包含自引用消息时截断 session
- 修复 transcript 写入失败（如磁盘满）被静默丢弃而非记录日志
- 修复变音符号在配置了 `language` 设置时从响应中丢失
- 修复策略管理的插件在从非首次安装的项目运行时从不自动更新

---

## 📦 2.1.107（2026-04-14）

### 🔧 改进

- 在长时间操作期间更早显示 thinking 提示

---

## 📦 2.1.105（2026-04-13）

> 📝 **笔记定位**：[PreCompact 可阻断](../hooks/index.md#其他事件) · [插件 monitors 键](../plugins/index.md#pluginjson-详解) · [Skill 描述上限提升](../skills/index.md#front-matter-配置项说明) · [/proactive 别名](../automation/index.md#本地定时任务)

### ✨ 新功能

- **`EnterWorktree` 工具新增 `path` 参数**：可在现有 worktree 之间直接切换，无需重新创建
- **PreCompact Hook 支持阻断**：exit code 2 可阻止自动压缩执行
- **插件后台监控**：插件 manifest 新增 `monitors` 键，支持挂载后台监控进程
- **`/proactive` 命令**：作为 `/loop` 的别名使用

### 🔧 改进

- API 流式传输卡住超过 5 分钟后自动中止并切换到非流式重试
- 网络错误消息立即显示重试提示，不再等待
- 超长单行文件写入在 UI 中截断显示，而非分页翻页
- `/doctor` 界面重新设计，加入状态图标
- `/config` 菜单标签和说明更清晰
- Skill 描述上限从 250 提升至 1,536 字符
- `WebFetch` 工具自动过滤页面中的 `<style>` 和 `<script>` 标签
- 改进合并了 squash-merged PR 的过期 Agent worktree 清理
- MCP 大体量输出截断时提供格式化相关的具体指引

### 🐛 修复

- 修复排队消息附带的图片被丢弃
- 修复长对话中提示词换行时屏幕变空白
- 修复助手消息中前导空白被丢失
- 修复 Bash 输出在带可点击文件链接时显示乱码
- 修复 Alt+Enter 和 Ctrl+J 在多种终端中换行插入失效
- 修复 "Creating worktree" 文字重复出现
- 修复焦点模式下已排队的用户提示词消失
- 修复单次定时任务被反复触发
- 修复 Team/Enterprise 入站频道通知被静默丢弃
- 修复市场插件安装时未自动安装依赖
- 修复自动更新后官方市场状态丢失
- 修复 `/resume` 会话提示未正确打印
- 修复反馈调查快捷键失效
- 修复输出格式不规范的 stdio MCP 服务器导致卡住，现在快速失败
- 修复 headless 会话第一轮无法使用 MCP 工具
- 修复 `/model` 选择器在 AWS Bedrock 非 US 区域无法使用
- 修复多个提供商的 429 速率限制错误未显示清晰提示
- 修复 `/resume` 在遇到格式错误的 text block 时崩溃
- 修复终端高度较短时 `/help` 布局错乱
- 修复格式错误的键绑定条目不再被静默接受，改为报错
- 修复 `CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC` 在单项目持久化场景下不生效
- 修复通过 SSH/mosh 连接时 16 色调色板显示偏淡
- 修复模式降级时 Bash 工具权限提示行为不一致

---

## 📦 2.1.101（2026-04-11）

> 📝 **笔记定位**：[企业网络配置](../configuration/advanced/index.md) · [Settings 与权限](../configuration/settings-permissions/index.md)

### ✨ 新功能

- **`/team-onboarding` 命令**：根据本地 Claude Code 使用记录生成团队新人上手指南
- **OS CA 证书存储默认信任**：企业 TLS 代理无需额外配置即可工作（设置 `CLAUDE_CODE_CERT_STORE=bundled` 仅使用内置 CA）
- **`/ultraplan` 自动创建云环境**：`/ultraplan` 及其他远程会话功能现在自动创建默认云环境，无需先在网页端配置

### 🔧 改进

- brief mode 现在会在 Claude 返回纯文本而非结构化消息时自动重试一次
- focus mode 中 Claude 编写更自包含的摘要，因为用户只能看到最终消息
- 工具不可用错误现在解释原因和后续步骤，当模型调用的工具存在但当前上下文不可用时
- rate-limit 重试消息现在显示命中的限制和重置时间，而非模糊的秒数倒计时
- 拒绝错误消息现在包含 API 提供的解释（如果可用）
- `claude -p --resume <name>` 现在接受通过 `/rename` 或 `--name` 设置的会话标题
- settings 韧性改进：`settings.json` 中无法识别的 Hook 事件名称不再导致整个文件被忽略
- 通过托管设置强制启用的插件 Hook 现在在设置 `allowManagedHooksOnly` 时正常运行
- `/plugin` 和 `claude plugin update` 在 marketplace 无法刷新时显示警告，而非静默报告过时版本
- plan mode 在用户组织或认证设置无法访问 Claude Code 网页端时，隐藏「Refine with Ultraplan」选项
- beta tracing 现在遵循 `OTEL_LOG_USER_PROMPTS`、`OTEL_LOG_TOOL_DETAILS` 和 `OTEL_LOG_TOOL_CONTENT` 设置
- SDK `query()` 在消费者从 `for await` 中 `break` 或使用 `await using` 时正确清理子进程和临时文件

### 🐛 修复

- **修复 POSIX `which` 回退中的命令注入漏洞**（LSP 二进制检测使用）
- 修复长会话中虚拟滚动器保留大量消息列表历史副本导致内存泄漏
- 修复 `--resume`/`--continue` 在大会话上丢失对话上下文（加载器锚定到死胡同分支而非活跃对话）
- 修复 `--resume` 链恢复桥接到无关子 Agent 对话（当子 Agent 消息位于主链写入间隙附近时）
- 修复 `--resume` 在持久化的 Edit/Write 工具结果缺少 `file_path` 时崩溃
- 修复硬编码的 5 分钟请求超时中止慢速后端（本地 LLM、扩展思考、慢网关），忽略 `API_TIMEOUT_MS`
- 修复 `permissions.deny` 规则不覆盖 PreToolUse Hook 的 `permissionDecision: "ask"`（之前 Hook 可将拒绝降级为提示）
- 修复 `--setting-sources` 未包含 `user` 时，后台清理忽略 `cleanupPeriodDays` 并删除 30 天前的对话历史
- 修复 Bedrock SigV4 认证在设置 `ANTHROPIC_AUTH_TOKEN`、`apiKeyHelper` 或 `ANTHROPIC_CUSTOM_HEADERS` 的 Authorization 头时返回 403
- 修复 `claude -w <name>` 在之前会话的 worktree 清理留下过期目录时报「already exists」
- 修复子 Agent 不继承动态注入的 MCP 服务器工具
- 修复在隔离 worktree 中运行的子 Agent 被拒绝访问其 worktree 内的文件
- 修复沙箱 Bash 命令在全新启动后因 `mktemp: No such file or directory` 失败
- 修复 `claude mcp serve` 工具调用在验证 `outputSchema` 的 MCP 客户端中报「Tool execution failed」
- 修复 `RemoteTrigger` 工具的 `run` 操作发送空请求体被服务器拒绝
- 修复多个 `/resume` 选择器问题：窄默认视图隐藏其他项目会话、Windows Terminal 无法预览、worktree 中 cwd 不正确、会话未找到错误未输出到 stderr、终端标题未设置、恢复提示与输入重叠
- 修复 Grep 工具在嵌入的 ripgrep 二进制路径过期时 ENOENT（VS Code 扩展自动更新、macOS App Translocation）；现在回退到系统 `rg` 并在会话中自修复
- 修复 `/btw` 每次使用时将整个对话写入磁盘
- 修复 `/context` 的 Free space 和 Messages breakdown 与头部百分比不一致
- 修复多个插件问题：斜杠命令解析到具有重复 `name:` frontmatter 的错误插件、`/plugin update` 失败报 `ENAMETOOLONG`、Discover 显示已安装的插件、目录源插件从过期版本缓存加载、skill 不遵循 `context: fork` 和 `agent` frontmatter 字段
- 修复 `/mcp` 菜单对使用 `headersHelper` 配置的 MCP 服务器提供 OAuth 特定操作；现在改为提供 Reconnect 以重新调用辅助脚本
- 修复 `ctrl+]`、`ctrl+\` 和 `ctrl+^` 键绑定在发送原始 C0 控制字节的终端中不触发（Terminal.app、默认 iTerm2、xterm）
- 修复 `/login` OAuth URL 渲染时带填充，影响鼠标选择
- 修复渲染问题：非全屏模式下可见区域上方内容变化时闪烁、长会话中终端滚动回溯被清除、鼠标滚动转义序列偶尔泄漏到输入中作为文本
- 修复 `settings.json` 的 env 值为数字而非字符串时崩溃
- 修复应用内设置写入（如 `/add-dir --remember`、`/config`）不刷新内存快照，阻止已移除目录在会话中被撤销
- 修复自定义键绑定（`~/.claude/keybindings.json`）在 Bedrock、Vertex 和其他第三方提供商上不加载
- 修复 `claude --continue -p` 未正确继续由 `-p` 或 SDK 创建的会话
- 修复多个 Remote Control 问题：会话崩溃时 worktree 被移除、连接失败未持久化到记录、brief mode 中本地会话显示虚假「Disconnected」指示器、`/remote-control` 在仅设置 `CLAUDE_CODE_ORGANIZATION_UUID` 时通过 SSH 失败
- 修复 `/insights` 有时从响应中省略报告文件链接
- [VSCode] 修复关闭最后一个编辑器标签时聊天输入下方的文件附件未清除

---

> 📖 更早版本（2.1.98 及之前）请查看[更新日志归档](../changelog-archive/index.md)。
>
> 更早版本（1.x 及之前）请查看[更新日志（1.x 及更早）](../changelog-v1/index.md)。
