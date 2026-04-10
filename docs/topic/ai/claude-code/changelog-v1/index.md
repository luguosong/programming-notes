---
title: 更新日志（1.x 及更早）
description: Claude Code 1.x 及更早版本的更新内容汇总
---

# 更新日志（1.x 及更早）

本页整理了 Claude Code 1.x 及更早版本的更新记录。当前版本（2.x）请查看[更新日志](../changelog/index.md)。

数据来源于 [GitHub Releases](https://github.com/anthropics/claude-code/releases)。

---


## 📦 1.0.126

### ✨ 新功能

- 启用 /context command for Bedrock and Vertex
- 新增 mTLS support for HTTP-based OpenTelemetry exporters

---

## 📦 1.0.124

### ⚡ 性能

- 改进 VSCode extension performance for large files

### 🔧 改进

- 新增 `CLAUDE_BASH_NO_LOGIN` 环境变量，设为 1 或 true 可跳过 BashTool 的 login shell
- 改进 权限被拒绝时不再将 allowed tools 列表通知 Claude

### 🐛 修复

- 修复 Bedrock and Vertex environment variables evaluating all strings as truthy
- 修复 security vulnerability in Bash tool permission checks

---

## 📦 1.0.123

> 📝 **笔记定位**：[SlashCommand 工具](../skills/index.md#-自定义命令commands)

### ✨ 新功能

- 新增 SlashCommand tool, which enables Claude to invoke your slash commands. https://code.claude.com/docs/en/slash-commands#SlashCommand-tool

### 🔧 改进

- 改进 Bash 权限规则现在支持匹配时的输出重定向（例如 `Bash(python:*)` 匹配 `python script.py > output.txt`）
- 改进 BashTool 环境快照日志
- 迁移 --debug logging to a file, to enable easy tailing & filtering

### 🐛 修复

- 修复 thinking mode triggering on negation phrases like "don't think"
- 修复 rendering performance degradation during token streaming
- 修复 a bug where resuming a conversation in headless mode would sometimes enable thinking unnecessarily

---

## 📦 1.0.120

### 🔧 改进

- 改进 VSCode extension command registry and sessions dialog user experience
- 改进 会话对话框的响应性和视觉反馈

### 🐛 修复

- 修复 input lag during typing, especially noticeable with large prompts
- 修复 IDE compatibility issue by removing worktree support check
- 修复 security vulnerability where Bash tool permission checks could be bypassed using prefix matching

---

## 📦 1.0.119

### 🔧 改进

- 新增 通过 headersHelper 配置为 MCP 服务器设置动态 headers

### 🐛 修复

- 修复 Windows issue where process visually freezes on entering interactive mode
- 修复 thinking mode not working in headless sessions
- 修复 slash commands now properly update allowed tools instead of replacing them

---

## 📦 1.0.117

### ✨ 新功能

- 新增 Ctrl-R history search to recall previous commands like bash/zsh
- 新增 sed command to auto-allowed commands in acceptEdits mode
- 新增 permissions management hint to /add-dir output

### 🐛 修复

- 修复 input lag while typing, especially on Windows
- 修复 Windows PATH comparison to be case-insensitive for drive letters

---

## 📦 1.0.115

### 🔧 改进

- 改进 thinking mode display with enhanced visual effects
- 新增 输入 /t 可临时禁用 thinking mode
- 改进 path validation for glob and grep tools
- 改进 post-tool hooks 显示精简输出以减少视觉干扰
- 改进 UI consistency for permission request dialogs

### 🐛 修复

- 修复 visual feedback when loading state completes

---

## 📦 1.0.113

### 🔧 改进

- 改进 将切换 transcript 的快捷键从 Ctrl+R 改为 Ctrl+O

### 🗑️ 移除

- 弃用 piped input in interactive mode

---

## 📦 1.0.112

### ✨ 新功能

- Hooks: 新增 systemMessage support for SessionEnd hooks
- 新增 `spinnerTipsEnabled` setting to disable spinner tips

### 🔧 改进

- Transcript mode (Ctrl+R): 新增 显示生成每条 assistant 消息所用的模型
- 修复 issue where some Claude Max users were incorrectly recognized as Claude Pro users

### 🐛 修复

- IDE: 改进 多项优化和 bug 修复

---

## 📦 1.0.111

### 🔧 改进

- /model 现在会验证提供的模型名称

### 🐛 修复

- 修复 Bash tool crashes caused by malformed shell syntax parsing

---

## 📦 1.0.110

### 🔧 改进

- /terminal-setup command 现在支持 WezTerm
- MCP: 改进 OAuth token 现在会在过期前主动刷新

### 🐛 修复

- 修复 reliability issues with background Bash processes

---

## 📦 1.0.109

### ✨ 新功能

- SDK: 新增 partial message streaming support via `--include-partial-messages` CLI flag

---

## 📦 1.0.106

### 🐛 修复

- Windows: 修复 path permission matching to consistently use POSIX format (e.g., `Read(//c/Users/...)`)

---

## 📦 1.0.97

### 🔧 改进

- Settings: 新增 /doctor 验证权限规则语法并建议修正

---

## 📦 1.0.94

### ✨ 新功能

- Vertex: 新增 支持受支持模型的 global endpoints
- SDK: 新增 custom tools as callbacks
- 新增 /todos command to list current todo items

### 🔧 改进

- /memory command 现在允许直接编辑所有导入的 memory 文件

---

## 📦 1.0.93

### ✨ 新功能

- Windows: 新增 alt + v shortcut for pasting images from clipboard

### 🔧 改进

- 新增 支持 NO_PROXY 环境变量以绕过指定主机名和 IP 的代理

---

## 📦 1.0.90

### 🔧 改进

- 改进 设置文件更改立即生效，无需重启

---

## 📦 1.0.88

### 🔧 改进

- 新增 status line 输入包含 `exceeds_200k_tokens`
- 引入 `ANTHROPIC_DEFAULT_SONNET_MODEL` 和 `ANTHROPIC_DEFAULT_OPUS_MODEL`，用于控制模型别名 opusplan、opus 和 sonnet
- Bedrock: 更新 default Sonnet model to Sonnet 4

### 🐛 修复

- 修复 issue causing "OAuth authentication is currently not supported"
- 修复 incorrect usage tracking in /cost.

---

## 📦 1.0.86

### ✨ 新功能

- 新增 /context to help users self-serve debug context issues
- SDK: 新增 UUID support for all SDK messages
- SDK: 新增 `--replay-user-messages` to replay user messages back to stdout

---

## 📦 1.0.85

> 📝 **笔记定位**：[SessionEnd 事件](../hooks/index.md#-hook-能在哪些时机触发)

### 🔧 改进

- 新增 status line 输入包含会话费用信息
- Hooks: 引入 SessionEnd hook

---

## 📦 1.0.84

> 📝 **笔记定位**：[@-mention ~/.claude/ 文件](../skills/index.md#-skill-的加载与触发)

### 🔧 改进

- @-mention: 新增 ~/.claude/* 文件到建议列表，便于编辑 agent、output style 和 slash command
- 改进 默认使用内置 ripgrep；如需退出此行为，设置 USE_BUILTIN_RIPGREP=0

### 🐛 修复

- 修复 tool_use/tool_result id mismatch error when network is unstable
- 修复 Claude sometimes ignoring real-time steering when wrapping up a task

---

## 📦 1.0.83

### ✨ 新功能

- 新的 shimmering spinner

### 🔧 改进

- @-mention: 新增 支持路径中包含空格的文件

---

## 📦 1.0.82

### ✨ 新功能

- SDK: 新增 request cancellation support
- SDK: 新的 additionalDirectories option to search custom paths, improved slash command processing

### 🔧 改进

- Settings: 改进 验证防止 .claude/settings.json 文件中的无效字段
- MCP: 改进 tool name consistency

### 🐛 修复

- Bash: 修复 crash when Claude tries to automatically read large files

---

## 📦 1.0.81

### ✨ 新功能

- 发布 output styles, including new built-in educational output styles "Explanatory" and "Learning". Docs: https://code.claude.com/docs/en/output-styles

### 🐛 修复

- Agents: 修复 agent 文件无法解析时的自定义 agent 加载问题

---

## 📦 1.0.80

### 🐛 修复

- 修复 自定义 subagent 颜色的文本对比度和 spinner 渲染问题

---

## 📦 1.0.77

### ✨ 新功能

- SDK: 新增 session support and permission denial tracking

### 🔧 改进

- Opus Plan Mode: 新增 在 `/model` 中设置仅在 plan mode 使用 Opus，其他情况使用 Sonnet

### 🐛 修复

- Bash tool: 修复 heredoc 和多行字符串转义，改进 stderr 重定向处理
- 修复 token limit errors in conversation summarization

---

## 📦 1.0.73

> 📝 **笔记定位**：[MCP 多配置文件](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- Linux: 新增 support for Alpine and musl-based distributions (requires separate ripgrep installation)

### 🔧 改进

- MCP: 新增 支持多配置文件 `--mcp-config file1.json file2.json`
- MCP: 新增 按 Esc 取消 OAuth 认证流程
- Bash: 改进 command validation and reduced false security warnings
- UI: 改进 spinner 动画和 status line 视觉层次

---

## 📦 1.0.72

### 🔧 改进

- 新增 Ask permissions：通过 /permissions 让 Claude Code 始终确认是否使用特定工具

---

## 📦 1.0.71

### 🔧 改进

- 新增 Background commands：按 Ctrl-b 在后台运行 Bash 命令，Claude 可继续工作（适用于 dev servers、tailing logs 等）
- 新增 可自定义 status line：通过 /statusline 将终端 prompt 添加到 Claude Code

---

## 📦 1.0.70

### ✨ 新功能

- 新增 support for @-mentions in slash command arguments

### ⚡ 性能

- 优化 消息渲染性能，提升大上下文场景下的表现

### 🐛 修复

- Windows: 修复 native file search, ripgrep, and subagent functionality

---

## 📦 1.0.69

### 🔧 改进

- 更新 Opus 升级至 4.1 版本

---

## 📦 1.0.68

> 📝 **笔记定位**：[disableAllHooks 配置](../hooks/index.md#-怎么配置-hook) · [/doctor 诊断 MCP](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- SDK: 新增 canUseTool callback support for tool confirmation
- 新增 `disableAllHooks` setting

### ⚡ 性能

- 改进 file suggestions performance in large repos

### 🔧 改进

- Windows: 改进 allow/deny 工具和项目信任的权限检查。可能会在 `.claude.json` 中创建新的项目条目——如需要请手动合并 history 字段
- Windows: 改进 子进程启动，消除运行 pnpm 等命令时的 "No such file or directory" 错误
- 改进 /doctor 命令，新增 CLAUDE.md 和 MCP 工具上下文用于自助调试

### 🐛 修复

- 修复 incorrect model names being used for certain commands like `/pr-comments`

---

## 📦 1.0.65

### 🐛 修复

- IDE: 修复 connection stability issues and error handling for diagnostics
- Windows: 修复 shell environment setup for users without .bashrc files

---

## 📦 1.0.64

> 📝 **笔记定位**：[Agent 模型自定义](../sub-agents/index.md#-sub-agent-工作原理) · [Hook systemMessage 字段](../hooks/index.md#-hook-能用来做什么实践指南)

### ✨ 新功能

- Agents: 新增 模型自定义支持，现在可以指定 agent 使用的模型
- 新增 hidden files to file search and @-mention suggestions

### 🔧 改进

- Hooks: 新增 systemMessage field to hook JSON output for displaying warnings and context

### 🐛 修复

- Agents: 修复 对 recursive agent tool 的非预期访问
- SDK: 修复 user input tracking across multi-turn conversations

---

## 📦 1.0.63

### 🐛 修复

- Windows: 修复 file search, @agent mentions, and custom slash commands functionality

---

## 📦 1.0.62

> 📝 **笔记定位**：[@-mention 调用 Agent](../sub-agents/index.md#-agent-teams) · [SessionStart 事件](../hooks/index.md#-hook-能在哪些时机触发)

### ✨ 新功能

- 新增 @-mention support with typeahead for custom agents. @<your-custom-agent> to invoke it

### 🔧 改进

- Hooks: 新增 SessionStart hook for new session initialization
- /add-dir command 现在支持目录路径的 typeahead
- 改进 network connectivity check reliability

---

## 📦 1.0.61

### ✨ 新功能

- IDE: 新增 support for pasting images in VSCode MacOS using ⌘+V
- IDE: 新增 `CLAUDE_CODE_AUTO_CONNECT_IDE=false` for disabling IDE auto-connection
- 新增 `CLAUDE_CODE_SHELL_PREFIX` for wrapping Claude and user-provided shell commands run by Claude Code

### 🔧 改进

- Transcript mode (Ctrl+R): 改进 将 Esc 改为退出 transcript mode 而非中断
- Settings: 新增 `--settings` 参数从 JSON 文件加载设置

### 🐛 修复

- Settings: 修复 settings 文件路径为 symlinks 时的解析问题
- OTEL: 修复 认证变更后报告错误 organization 的问题
- Slash commands: 修复 Bash 相关 allowed-tools 的权限检查

---

## 📦 1.0.60

> 📝 **笔记定位**：[自定义 Sub-agents 发布](../sub-agents/index.md#-为什么需要-sub-agent)

### ✨ 新功能

- 新增 自定义 subagents，用于专项任务！运行 /agents 开始使用

---

## 📦 1.0.59

> 📝 **笔记定位**：[Hook PermissionDecision 输出](../hooks/index.md#-hook-能用来做什么实践指南)

### ✨ 新功能

- SDK: 新增 tool confirmation support with canUseTool callback

### 🔧 改进

- SDK: 新增 为 spawned process 指定 env
- Hooks: 新增 向 hooks 暴露 PermissionDecision（包括 "ask"）
- Hooks: 新增 UserPromptSubmit 支持 advanced JSON output 中的 additionalContext

### 🐛 修复

- 修复 issue where some Max users that specified Opus would still see fallback to Sonnet

---

## 📦 1.0.58

> 📝 **笔记定位**：[PreCompact 事件](../hooks/index.md#-hook-能在哪些时机触发) · [CLAUDE_PROJECT_DIR 环境变量](../hooks/index.md#-hook-能用来做什么实践指南)

### ✨ 新功能

- 新增 support for reading PDFs

### 🔧 改进

- MCP: 改进 server health status display in 'claude mcp list'
- Hooks: 新增 CLAUDE_PROJECT_DIR env var for hook commands

---

## 📦 1.0.57

> 📝 **笔记定位**：[斜杠命令指定模型](../skills/index.md#-自定义命令commands)

### ✨ 新功能

- 新增 support for specifying a model in slash commands

### 🔧 改进

- 改进 permission messages to help Claude understand allowed tools

### 🐛 修复

- 修复 移除 bash 输出在 terminal wrapping 中的尾部换行符

---

## 📦 1.0.56

### ✨ 新功能

- Windows: 启用 shift+tab for mode switching on versions of Node.js that support terminal VT mode

### 🐛 修复

- 修复 WSL IDE 检测问题
- 修复 an issue causing awsRefreshHelper changes to .aws directory not to be picked up

---

## 📦 1.0.55

### ✨ 新功能

- SDK: 新增 ability to capture error logging
- 新增 --system-prompt-file option to override system prompt in print mode

### 🔧 改进

- 明确 knowledge cutoff for Opus 4 and Sonnet 4 models

### 🐛 修复

- Windows: 修复 Ctrl+Z 崩溃问题

---

## 📦 1.0.54

> 📝 **笔记定位**：[UserPromptSubmit 事件](../hooks/index.md#-hook-能在哪些时机触发) · [斜杠命令 argument-hint](../skills/index.md#-自定义命令commands)

### 🔧 改进

- Hooks: 新增 UserPromptSubmit hook and the current working directory to hook inputs
- Custom slash commands: 新增 frontmatter 中的 argument-hint
- Windows: 改进 OAuth 使用端口 45454 并正确构建浏览器 URL
- Windows: 改进 模式切换改为 alt + m，plan mode 正确渲染
- Shell: 改进 切换到内存 shell snapshot 以修复文件相关错误

---

## 📦 1.0.53

### ✨ 新功能

- 新增 helper script settings for AWS token refresh: awsAuthRefresh (for foreground operations like aws sso login) and awsCredentialExport (for background operation with STS-like response).

### 🔧 改进

- 更新 @-mention file truncation from 100 lines to 2000 lines

---

## 📦 1.0.52

> 📝 **笔记定位**：[MCP 服务器指令](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- 新增 support for MCP server instructions

---

## 📦 1.0.51

> 📝 **笔记定位**：[Windows 原生支持](../platforms/index.md#-终端-cli) · [Bedrock API Key](../platforms/index.md#-平台全景图)

### ✨ 新功能

- 新增 support for native Windows (requires Git for Windows)
- 新增 support for Bedrock API keys through environment variable AWS_BEARER_TOKEN_BEDROCK
- Settings: 新增 /doctor 可帮助识别和修复无效的 setting 文件
- `--append-system-prompt` 现在可在交互模式中使用，不仅限于 --print/-p

### 🔧 改进

- 增加 auto-compact warning threshold from 60% to 80%
- 新增 OTEL resource 包含 os.type、os.version、host.arch 和 wsl.version（如运行在 Windows Subsystem for Linux 上）

### 🐛 修复

- 修复 an issue with handling user directories with spaces for shell snapshots
- Custom slash commands: 修复 子目录中的 user-level 命令
- Plan mode: 修复 sub-task 中被拒绝的 plan 被丢弃的问题

---

## 📦 1.0.48

> 📝 **笔记定位**：[PreCompact 事件](../hooks/index.md#-hook-能在哪些时机触发)

### ✨ 新功能

- 新增 progress messages to Bash tool based on the last 5 lines of command output
- 新增 expanding variables support for MCP server configuration

### 🔧 改进

- 移动 shell snapshots from /tmp to ~/.claude for more reliable Bash tool calls
- 改进 IDE extension path handling when Claude Code runs in WSL
- Hooks: 新增 a PreCompact hook
- Vim mode: 新增 c、f/F、t/T 操作

### 🐛 修复

- 修复 a bug in v1.0.45 where the app would sometimes freeze on launch

---

## 📦 1.0.45

### 🔧 改进

- 重新设计 Search (Grep) tool with new tool input parameters and features
- 禁用 IDE diffs for notebook files, fixing "Timeout waiting after 1000ms" error
- 更新 prompt input undo to Ctrl+\_ to avoid breaking existing Ctrl+U behavior, matching zsh's undo shortcut
- Custom slash commands: 改进 恢复基于子目录的命令名称命名空间。例如 .claude/commands/frontend/component.md 现在是 /frontend:component，而非 /component

### 🐛 修复

- 修复 config file corruption issue by enforcing atomic writes
- Stop Hooks: 修复 /clear 后的 transcript 路径以及循环以 tool call 结束时的触发问题

---

## 📦 1.0.44

### ✨ 新功能

- 新的 /export command lets you quickly export a conversation for sharing

### 🔧 改进

- MCP: 新增 支持 resource_link tool 结果
- MCP: 新增 tool annotations 和 tool titles 在 /mcp view 中显示
- 变更 Ctrl+Z to suspend Claude Code. Resume by running `fg`. Prompt input undo is now Ctrl+U.

---

## 📦 1.0.43

### 🔧 改进

- Hooks: 新增 EPIPE system error handling

### 🐛 修复

- 修复 a bug where the theme selector was saving excessively

---

## 📦 1.0.42

### ✨ 新功能

- 新增 tilde (`~`) expansion support to `/add-dir` command

---

## 📦 1.0.41

> 📝 **笔记定位**：[Stop/SubagentStop 事件拆分](../hooks/index.md#-hook-能在哪些时机触发) · [Hook 超时配置](../hooks/index.md#-怎么配置-hook)

### ✨ 新功能

- 新的 tool parameters JSON for Bash tool in `tool_decision` event

### 🔧 改进

- Hooks: 改进 将 Stop hook 触发拆分为 Stop 和 SubagentStop
- Hooks: 启用 optional timeout configuration for each command
- Hooks: 新增 "hook_event_name" to hook input

### 🐛 修复

- 修复 a bug where MCP tools would display twice in tool list

---

## 📦 1.0.40

### 🐛 修复

- 修复 a bug causing API connection errors with UNABLE_TO_GET_ISSUER_CERT_LOCALLY if `NODE_EXTRA_CA_CERTS` was set

---

## 📦 1.0.39

### ✨ 新功能

- 新的 Active Time metric in OpenTelemetry logging

---

## 📦 1.0.38

> 📝 **笔记定位**：[🎉 Hooks 正式发布](../hooks/index.md#-什么时候需要-hook)

### ✨ 新功能

- 发布 hooks. Special thanks to community input in https://github.com/anthropics/claude-code/issues/712. Docs: https://code.claude.com/docs/en/hooks

---

## 📦 1.0.37

### 🗑️ 移除

- 移除 ability to set `Proxy-Authorization` header via ANTHROPIC_AUTH_TOKEN or apiKeyHelper

---

## 📦 1.0.36

### 🔧 改进

- 改进 Web search 现在将当天日期纳入上下文

### 🐛 修复

- 修复 a bug where stdio MCP servers were not terminating properly on exit

---

## 📦 1.0.35

> 📝 **笔记定位**：[MCP OAuth 认证发现](../mcp/index.md#-认证方式)

### ✨ 新功能

- 新增 support for MCP OAuth Authorization Server discovery

---

## 📦 1.0.34

### 🐛 修复

- 修复 a memory leak causing a MaxListenersExceededWarning message to appear

---

## 📦 1.0.33

### ✨ 新功能

- 新增 prompt input undo functionality (Ctrl+Z and vim 'u' command)

### 🔧 改进

- 改进 logging functionality with session ID support
- 改进 plan mode

---

## 📦 1.0.32

### ✨ 新功能

- 新增 forceLoginMethod setting to bypass login selection screen

### 🔧 改进

- 更新 loopback config for litellm

---

## 📦 1.0.31

### 🐛 修复

- 修复 a bug where ~/.claude.json would get reset when file contained invalid JSON

---

## 📦 1.0.30

> 📝 **笔记定位**：[斜杠命令增强](../skills/index.md#-自定义命令commands)

### ✨ 新功能

- 新增 timestamps in Ctrl-r mode and fixed Ctrl-c handling
- 改进 jq 正则表达式支持，适用于包含 pipes 和 select 的复杂过滤器

### 🔧 改进

- Custom slash commands: 新增 运行 bash 输出、@-mention 文件、通过 thinking 关键词启用 thinking
- 改进 file path autocomplete with filename matching

---

## 📦 1.0.29

### 🔧 改进

- 改进 CJK character support in cursor navigation and rendering

---

## 📦 1.0.28

### ✨ 新功能

- 新增 XDG_CONFIG_HOME support to configuration directory
- 新的 attributes (terminal.type, language) in OpenTelemetry logging

### ⚡ 性能

- 优化 内存使用性能

### 🔧 改进

- 改进 上传前调整图片尺寸以避免 API 大小限制错误

### 🐛 修复

- Slash commands: 修复 历史导航时的 selector 显示问题

---

## 📦 1.0.27

> 📝 **笔记定位**：[Streamable HTTP 连接](../mcp/index.md#-配置-mcp-服务器) · [MCP OAuth 认证](../mcp/index.md#-认证方式)

### ✨ 新功能

- 新增 MCP resources 支持 @-mention

### 🔧 改进

- 新增 支持 Streamable HTTP MCP 服务器
- 新增 Remote MCP 服务器（SSE 和 HTTP）支持 OAuth
- /resume slash command 用于在 Claude Code 内切换对话

---

## 📦 1.0.25

### 🔧 改进

- Slash commands: 改进 将 "project" 和 "user" 前缀移至描述中
- Slash commands: 改进 命令发现的可靠性
- 改进 support for Ghostty
- 改进 web search reliability

---

## 📦 1.0.24

### 🔧 改进

- 改进 /mcp output

### 🐛 修复

- 修复 a bug where settings arrays got overwritten instead of merged

---

## 📦 1.0.23

> 📝 **笔记定位**：[SDK 发布（TypeScript + Python）](../getting-started/index.md#-怎么安装-claude-code)

### ✨ 新功能

- 发布 TypeScript SDK: import @anthropic-ai/claude-code to get started
- 发布 Python SDK: pip install claude-code-sdk to get started

---

## 📦 1.0.22

### 🔧 改进

- SDK: 重命名 `total_cost` to `total_cost_usd`

---

## 📦 1.0.21

### 🔧 改进

- 改进 editing of files with tab-based indentation

### 🐛 修复

- 修复 for tool_use without matching tool_result errors
- 修复 a bug where stdio MCP server processes would linger after quitting Claude Code

---

## 📦 1.0.18

### ✨ 新功能

- 新增 --add-dir CLI argument for specifying additional working directories
- 新增 streaming input support without require -p flag
- 新增 CLAUDE_BASH_MAINTAIN_PROJECT_WORKING_DIR environment variable to freeze working directory for bash commands
- 新增 detailed MCP server tools display (/mcp)
- 新增 auto-reconnection for MCP SSE connections on disconnect

### ⚡ 性能

- 改进 startup performance and session storage performance

### 🔧 改进

- 改进 MCP 认证和权限

### 🐛 修复

- 修复 issue where pasted content was lost when dialogs appeared

---

## 📦 1.0.17

### 🔧 改进

- 改进 在 -p mode 中输出 sub-tasks 的消息（查找 parent_tool_use_id 属性）
- 改进 MCP 服务器列表 UI
- 更新 Claude Code process title to display "claude" instead of "node"

### 🐛 修复

- 修复 crashes when the VS Code diff tool is invoked multiple times quickly

---

## 📦 1.0.11

### ✨ 新功能

- 新增 Claude Code 现在支持 Claude Pro 订阅
- 新增 /upgrade for smoother switching to Claude Max plans

### 🔧 改进

- 改进 UI for authentication from API keys and Bedrock/Vertex/external auth tokens
- 改进 shell configuration error handling
- 改进 todo list handling during compaction

---

## 📦 1.0.10

### ✨ 新功能

- 新增 markdown table support

### ⚡ 性能

- 改进 streaming performance

---

## 📦 1.0.8

### ✨ 新功能

- 新增 support for triggering thinking non-English languages

### 🔧 改进

- 增加 default otel interval from 1s -> 5s
- 改进 compacting UI

### 🐛 修复

- 修复 Vertex AI region fallback when using CLOUD_ML_REGION
- 修复 edge cases where MCP_TIMEOUT and MCP_TOOL_TIMEOUT weren't being respected
- 修复 a regression where search tools unnecessarily asked for permissions

---

## 📦 1.0.7

### 🔧 改进

- 重命名 /allowed-tools -> /permissions
- 迁移 allowedTools and ignorePatterns from .claude.json -> settings.json
- 改进 error handling for /install-github-app

### 🐛 修复

- 修复 a bug where --dangerously-skip-permissions sometimes didn't work in --print mode
- 修复 Bug 修复、UI 优化和工具可靠性改进

### 🗑️ 移除

- 弃用 claude config commands in favor of editing settings.json

---

## 📦 1.0.6

### ✨ 新功能

- 新增 support for symlinks in @file typeahead

### 🔧 改进

- 改进 edit reliability for tab-indented files
- 改进 全局统一遵循 CLAUDE_CONFIG_DIR
- 减少 unnecessary tool permission prompts

### 🐛 修复

- Bugfixes, UI polish, and tool reliability improvements

---

## 📦 1.0.4

### 🐛 修复

- 修复 a bug where MCP tool errors weren't being parsed correctly

---

## 📦 1.0.1

### ✨ 新功能

- 新增 `DISABLE_INTERLEAVED_THINKING` to give users the option to opt out of interleaved thinking.

### 🔧 改进

- 改进 model references to show provider-specific names (Sonnet 3.7 for Bedrock, Sonnet 4 for Console)
- 更新 documentation links and OAuth process descriptions

---

## 📦 1.0.0

### ✨ 新功能

- 引入 Sonnet 4 and Opus 4 models

### 🔧 改进

- Claude Code is now generally available

---

## 📦 0.2.125

> 📝 **笔记定位**：[Bedrock ARN 格式变更](../platforms/index.md#-平台全景图)

### 🔧 改进

- Breaking change: Bedrock ARN passed to `ANTHROPIC_MODEL` or `ANTHROPIC_SMALL_FAST_MODEL` should no longer contain an escaped slash (specify `/` instead of `%2F`)

### 🗑️ 移除

- 移除 `DEBUG=true` in favor of `ANTHROPIC_LOG=debug`, to log all requests

---

## 📦 0.2.117

### 🔧 改进

- Breaking change: --print JSON output now returns nested message objects, for forwards-compatibility as we introduce new metadata fields
- Introduced settings.cleanupPeriodDays
- Introduced CLAUDE_CODE_API_KEY_HELPER_TTL_MS env var
- Introduced --debug mode

---

## 📦 0.2.108

### ✨ 新功能

- You can now send messages to Claude while it works to steer Claude in real-time

### 🔧 改进

- Introduced BASH_DEFAULT_TIMEOUT_MS and BASH_MAX_TIMEOUT_MS env vars

### 🐛 修复

- 修复 a bug where thinking was not working in -p mode
- 修复 a regression in /cost reporting
- Lots of other bugfixes and improvements

### 🗑️ 移除

- 弃用 MCP wizard interface in favor of other MCP commands

---

## 📦 0.2.107

> 📝 **笔记定位**：[CLAUDE.md 文件导入](../configuration/index.md#-配置文件有哪几层)

### ✨ 新功能

- CLAUDE.md files can now import other files. Add @path/to/file.md to ./CLAUDE.md to load additional files on launch

---

## 📦 0.2.106

### ✨ 新功能

- MCP SSE server configs can now specify custom headers

### 🐛 修复

- 修复 a bug where MCP permission prompt didn't always show correctly

---

## 📦 0.2.105

### ✨ 新功能

- Claude can now search the web
- 新增 word movement keybindings for Vim

### ⚡ 性能

- 改进 latency for startup, todo tool, and file edits

### 🔧 改进

- 移动 system & account status to /status

---

## 📦 0.2.102

### ✨ 新功能

- You can now paste multiple large chunks into one prompt

### 🔧 改进

- 改进 thinking triggering reliability
- 改进 @mention reliability for images and folders

---

## 📦 0.2.100

### 🔧 改进

- Made db storage optional; missing db support disables --continue and --resume

### 🐛 修复

- 修复 a crash caused by a stack overflow error

---

## 📦 0.2.98

### 🐛 修复

- 修复 an issue where auto-compact was running twice

---

## 📦 0.2.96

### ✨ 新功能

- Claude Code can now also be used with a Claude Max subscription (https://claude.ai/upgrade)

---

## 📦 0.2.93

### 🔧 改进

- Resume conversations from where you left off from with "claude --continue" and "claude --resume"
- Claude now has access to a Todo list that helps it stay on track and be more organized

---

## 📦 0.2.82

### ✨ 新功能

- 新增 support for --disallowedTools

### 🔧 改进

- 重命名 tools for consistency: LSTool -> LS, View -> Read, etc.

---

## 📦 0.2.75

> 📝 **笔记定位**：[@-mention 文件引用](../skills/index.md#-skill-的加载与触发) · [--mcp-config 启动参数](../mcp/index.md#-管理-mcp-服务器)

### ⚡ 性能

- 改进 performance for filename auto-complete

### 🔧 改进

- Hit Enter to queue up additional messages while Claude is working
- Drag in or copy/paste image files directly into the prompt
- @-mention files to directly add them to context
- Run one-off MCP servers with `claude --mcp-config <path-to-file>`

---

## 📦 0.2.74

> 📝 **笔记定位**：[Task 工具写入与 Bash](../sub-agents/index.md#-agent-teams)

### ✨ 新功能

- 新增 support for refreshing dynamically generated API keys (via apiKeyHelper), with a 5 minute TTL
- Task tool can now perform writes and run bash commands

---

## 📦 0.2.72

### 🔧 改进

- 更新 spinner to indicate tokens loaded and tool usage

---

## 📦 0.2.70

### ✨ 新功能

- Network commands like curl are now available for Claude to use
- Claude can now run multiple web queries in parallel

### 🔧 改进

- Pressing ESC once immediately interrupts Claude in Auto-accept mode

---

## 📦 0.2.69

### 🔧 改进

- Enhanced terminal output display with better text truncation logic

### 🐛 修复

- 修复 UI glitches with improved Select component behavior

---

## 📦 0.2.67

> 📝 **笔记定位**：[共享项目权限规则](../enterprise/index.md#-安全架构)

### 🔧 改进

- Shared project permission rules can be saved in .claude/settings.json

---

## 📦 0.2.66

### 🔧 改进

- Print mode (-p) now supports streaming output via --output-format=stream-json

### 🐛 修复

- 修复 issue where pasting could trigger memory or bash mode unexpectedly

---

## 📦 0.2.63

### 🐛 修复

- 修复 an issue where MCP tools were loaded twice, which caused tool call errors

---

## 📦 0.2.61

### 🔧 改进

- 导航 menus with vim-style keys (j/k) or bash/emacs shortcuts (Ctrl+n/p) for faster interaction
- Enhanced image detection for more reliable clipboard paste functionality

### 🐛 修复

- 修复 an issue where ESC key could crash the conversation history selector

---

## 📦 0.2.59

### 🔧 改进

- Copy+paste images directly into your prompt
- 改进 progress indicators for bash and fetch tools

### 🐛 修复

- Bugfixes for non-interactive mode (-p)

---

## 📦 0.2.54

> 📝 **笔记定位**：[MCP SSE 传输协议](../mcp/index.md#-配置-mcp-服务器)

### ✨ 新功能

- 新增 support for MCP SSE transport

### 🔧 改进

- Quickly add to Memory by starting your message with '#'
- Press ctrl+r to see full output for long tool results

---

## 📦 0.2.53

### ✨ 新功能

- 新的 web fetch tool lets Claude view URLs that you paste in

### 🐛 修复

- 修复 a bug with JPEG detection

---

## 📦 0.2.50

> 📝 **笔记定位**：[MCP 项目级作用域 .mcp.json](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- 新的 MCP "project" scope now allows you to add MCP servers to .mcp.json files and commit them to your repository

---

## 📦 0.2.49

### 🔧 改进

- Previous MCP server scopes have been renamed: previous "project" scope is now "local" and "global" scope is now "user"

---

## 📦 0.2.47

> 📝 **笔记定位**：[自动压缩机制](../context-engineering/index.md#-压缩机制的陷阱)

### 🔧 改进

- Press Tab to auto-complete file and folder names
- Press Shift + Tab to toggle auto-accept for file edits
- Automatic conversation compaction for infinite conversation length (toggle with /config)

---

## 📦 0.2.44

### 🔧 改进

- Ask Claude to make a plan with thinking mode: just say 'think' or 'think harder' or even 'ultrathink'

---

## 📦 0.2.41

### ✨ 新功能

- MCP server startup timeout can now be configured via MCP_TIMEOUT environment variable

### 🔧 改进

- MCP server startup no longer blocks the app from starting up

---

## 📦 0.2.37

### ✨ 新功能

- 新的 /release-notes command lets you view release notes at any time

### 🔧 改进

- `claude config add/remove` commands now accept multiple values separated by commas or spaces

---

## 📦 0.2.36

> 📝 **笔记定位**：[MCP 服务器管理命令](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- 新增 MCP servers as JSON strings with `claude mcp add-json <n> <json>`

### 🔧 改进

- Import MCP servers from Claude Desktop with `claude mcp add-from-claude-desktop`

---

## 📦 0.2.34

### 🔧 改进

- Vim bindings for text input - enable with /vim or /config

---

## 📦 0.2.32

> 📝 **笔记定位**：[MCP 交互式配置向导](../mcp/index.md#-管理-mcp-服务器)

### 🔧 改进

- Interactive MCP setup wizard: Run "claude mcp add" to add MCP servers with a step-by-step interface

### 🐛 修复

- 修复 for some PersistentShell issues

---

## 📦 0.2.31

> 📝 **笔记定位**：[自定义斜杠命令首发](../skills/index.md#-自定义命令commands) · [MCP 调试模式](../mcp/index.md#-管理-mcp-服务器)

### 🔧 改进

- Custom slash commands: Markdown files in .claude/commands/ directories now appear as custom slash commands to insert prompts into your conversation
- MCP debug mode: Run with --mcp-debug flag to get more information about MCP server errors

---

## 📦 0.2.30

> 📝 **笔记定位**：[macOS Keychain 密钥存储](../getting-started/index.md#-怎么安装-claude-code)

### ✨ 新功能

- 新增 ANSI color theme for better terminal compatibility

### 🔧 改进

- (Mac-only) API keys are now stored in macOS Keychain

### 🐛 修复

- 修复 issue where slash command arguments weren't being sent properly

---

## 📦 0.2.26

### ✨ 新功能

- 新的 /approved-tools command for managing tool permissions

### 🔧 改进

- Word-level diff display for improved code readability
- Fuzzy matching for slash commands

---

## 📦 0.2.21

### 🔧 改进

- Fuzzy matching for /commands

---