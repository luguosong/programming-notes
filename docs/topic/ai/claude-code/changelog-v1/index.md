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

- Set `CLAUDE_BASH_NO_LOGIN` environment variable to 1 or true to to skip login shell for BashTool
- No longer inform Claude of the list of allowed tools when permission is denied

### 🐛 修复

- 修复 Bedrock and Vertex environment variables evaluating all strings as truthy
- 修复 security vulnerability in Bash tool permission checks

---

## 📦 1.0.123

> 📝 **笔记定位**：[SlashCommand 工具](../skills/index.md#-自定义斜杠命令)

### ✨ 新功能

- 新增 SlashCommand tool, which enables Claude to invoke your slash commands. https://code.claude.com/docs/en/slash-commands#SlashCommand-tool

### 🔧 改进

- Bash permission rules now support output redirections when matching (e.g., `Bash(python:*)` matches `python script.py > output.txt`)
- Enhanced BashTool environment snapshot logging
- 迁移 --debug logging to a file, to enable easy tailing & filtering

### 🐛 修复

- 修复 thinking mode triggering on negation phrases like "don't think"
- 修复 rendering performance degradation during token streaming
- 修复 a bug where resuming a conversation in headless mode would sometimes enable thinking unnecessarily

---

## 📦 1.0.120

### 🔧 改进

- 改进 VSCode extension command registry and sessions dialog user experience
- Enhanced sessions dialog responsiveness and visual feedback

### 🐛 修复

- 修复 input lag during typing, especially noticeable with large prompts
- 修复 IDE compatibility issue by removing worktree support check
- 修复 security vulnerability where Bash tool permission checks could be bypassed using prefix matching

---

## 📦 1.0.119

### 🔧 改进

- Support dynamic headers for MCP servers via headersHelper configuration

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
- Type /t to temporarily disable thinking mode in your prompt
- 改进 path validation for glob and grep tools
- Show condensed output for post-tool hooks to reduce visual clutter
- 改进 UI consistency for permission request dialogs

### 🐛 修复

- 修复 visual feedback when loading state completes

---

## 📦 1.0.113

### 🔧 改进

- Move Ctrl+R keybinding for toggling transcript to Ctrl+O

### 🗑️ 移除

- 弃用 piped input in interactive mode

---

## 📦 1.0.112

### ✨ 新功能

- Hooks: 新增 systemMessage support for SessionEnd hooks
- 新增 `spinnerTipsEnabled` setting to disable spinner tips

### 🔧 改进

- Transcript mode (Ctrl+R): Added the model used to generate each assistant message
- 修复 issue where some Claude Max users were incorrectly recognized as Claude Pro users

### 🐛 修复

- IDE: Various improvements and bug fixes

---

## 📦 1.0.111

### 🔧 改进

- /model now validates provided model names

### 🐛 修复

- 修复 Bash tool crashes caused by malformed shell syntax parsing

---

## 📦 1.0.110

### 🔧 改进

- /terminal-setup command now supports WezTerm
- MCP: OAuth tokens now proactively refresh before expiration

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

- Settings: /doctor now validates permission rule syntax and suggests corrections

---

## 📦 1.0.94

### ✨ 新功能

- Vertex: add support for global endpoints for supported models
- SDK: 新增 custom tools as callbacks
- 新增 /todos command to list current todo items

### 🔧 改进

- /memory command now allows direct editing of all imported memory files

---

## 📦 1.0.93

### ✨ 新功能

- Windows: 新增 alt + v shortcut for pasting images from clipboard

### 🔧 改进

- Support NO_PROXY environment variable to bypass proxy for specified hostnames and IPs

---

## 📦 1.0.90

### 🔧 改进

- Settings file changes take effect immediately - no restart required

---

## 📦 1.0.88

> 📝 **笔记定位**：[模型别名环境变量](../how-it-works/index.md#-模型选择与配置)

### 🔧 改进

- Status line input now includes `exceeds_200k_tokens`
- Introduced `ANTHROPIC_DEFAULT_SONNET_MODEL` and `ANTHROPIC_DEFAULT_OPUS_MODEL` for controlling model aliases opusplan, opus, and sonnet.
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

> 📝 **笔记定位**：[SessionEnd 事件](../hooks/index.md#-hook-事件类型)

### 🔧 改进

- Status line input now includes session cost info
- Hooks: Introduced SessionEnd hook

---

## 📦 1.0.84

> 📝 **笔记定位**：[@-mention ~/.claude/ 文件](../skills/index.md#-mention-机制)

### 🔧 改进

- @-mention: Add ~/.claude/\* files to suggestions for easier agent, output style, and slash command editing
- Use built-in ripgrep by default; to opt out of this behavior, set USE_BUILTIN_RIPGREP=0

### 🐛 修复

- 修复 tool_use/tool_result id mismatch error when network is unstable
- 修复 Claude sometimes ignoring real-time steering when wrapping up a task

---

## 📦 1.0.83

### ✨ 新功能

- 新的 shimmering spinner

### 🔧 改进

- @-mention: Support files with spaces in path

---

## 📦 1.0.82

### ✨ 新功能

- SDK: 新增 request cancellation support
- SDK: 新的 additionalDirectories option to search custom paths, improved slash command processing

### 🔧 改进

- Settings: Validation prevents invalid fields in .claude/settings.json files
- MCP: 改进 tool name consistency

### 🐛 修复

- Bash: 修复 crash when Claude tries to automatically read large files

---

## 📦 1.0.81

### ✨ 新功能

- 发布 output styles, including new built-in educational output styles "Explanatory" and "Learning". Docs: https://code.claude.com/docs/en/output-styles

### 🐛 修复

- Agents: Fix custom agent loading when agent files are unparsable

---

## 📦 1.0.80

### 🐛 修复

- UI improvements: Fix text contrast for custom subagent colors and spinner rendering issues

---

## 📦 1.0.77

> 📝 **笔记定位**：[Opus Plan Mode](../how-it-works/index.md#-模型选择与配置)

### ✨ 新功能

- SDK: 新增 session support and permission denial tracking

### 🔧 改进

- Opus Plan Mode: New setting in `/model` to run Opus only in plan mode, Sonnet otherwise

### 🐛 修复

- Bash tool: Fix heredoc and multiline string escaping, improve stderr redirection handling
- 修复 token limit errors in conversation summarization

---

## 📦 1.0.73

> 📝 **笔记定位**：[MCP 多配置文件](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- Linux: 新增 support for Alpine and musl-based distributions (requires separate ripgrep installation)

### 🔧 改进

- MCP: Support multiple config files with `--mcp-config file1.json file2.json`
- MCP: Press Esc to cancel OAuth authentication flows
- Bash: 改进 command validation and reduced false security warnings
- UI: Enhanced spinner animations and status line visual hierarchy

---

## 📦 1.0.72

### 🔧 改进

- Ask permissions: have Claude Code always ask for confirmation to use specific tools with /permissions

---

## 📦 1.0.71

### 🔧 改进

- Background commands: (Ctrl-b) to run any Bash command in the background so Claude can keep working (great for dev servers, tailing logs, etc.)
- Customizable status line: add your terminal prompt to Claude Code with /statusline

---

## 📦 1.0.70

### ✨ 新功能

- 新增 support for @-mentions in slash command arguments

### ⚡ 性能

- Performance: Optimized message rendering for better performance with large contexts

### 🐛 修复

- Windows: 修复 native file search, ripgrep, and subagent functionality

---

## 📦 1.0.69

### 🔧 改进

- Upgraded Opus to version 4.1

---

## 📦 1.0.68

> 📝 **笔记定位**：[disableAllHooks 配置](../hooks/index.md#-hook-配置详解) · [/doctor 诊断 MCP](../mcp/index.md#-管理-mcp-服务器)

### ✨ 新功能

- SDK: 新增 canUseTool callback support for tool confirmation
- 新增 `disableAllHooks` setting

### ⚡ 性能

- 改进 file suggestions performance in large repos

### 🔧 改进

- Windows: improve permissions checks for allow / deny tools and project trust. This may create a new project entry in `.claude.json` - manually merge the history field if desired.
- Windows: improve sub-process spawning to eliminate "No such file or directory" when running commands like pnpm
- Enhanced /doctor command with CLAUDE.md and MCP tool context for self-serve debugging

### 🐛 修复

- 修复 incorrect model names being used for certain commands like `/pr-comments`

---

## 📦 1.0.65

### 🐛 修复

- IDE: 修复 connection stability issues and error handling for diagnostics
- Windows: 修复 shell environment setup for users without .bashrc files

---

## 📦 1.0.64

> 📝 **笔记定位**：[Agent 模型自定义](../sub-agents/index.md#-如何创建-sub-agents) · [Hook systemMessage 字段](../hooks/index.md#-hook-的输入与输出)

### ✨ 新功能

- Agents: Added model customization support - you can now specify which model an agent should use
- 新增 hidden files to file search and @-mention suggestions

### 🔧 改进

- Hooks: 新增 systemMessage field to hook JSON output for displaying warnings and context

### 🐛 修复

- Agents: Fixed unintended access to the recursive agent tool
- SDK: 修复 user input tracking across multi-turn conversations

---

## 📦 1.0.63

### 🐛 修复

- Windows: 修复 file search, @agent mentions, and custom slash commands functionality

---

## 📦 1.0.62

> 📝 **笔记定位**：[@-mention 调用 Agent](../sub-agents/index.md#-sub-agents-的协作模式) · [SessionStart 事件](../hooks/index.md#-hook-事件类型)

### ✨ 新功能

- 新增 @-mention support with typeahead for custom agents. @<your-custom-agent> to invoke it

### 🔧 改进

- Hooks: 新增 SessionStart hook for new session initialization
- /add-dir command now supports typeahead for directory paths
- 改进 network connectivity check reliability

---

## 📦 1.0.61

### ✨ 新功能

- IDE: 新增 support for pasting images in VSCode MacOS using ⌘+V
- IDE: 新增 `CLAUDE_CODE_AUTO_CONNECT_IDE=false` for disabling IDE auto-connection
- 新增 `CLAUDE_CODE_SHELL_PREFIX` for wrapping Claude and user-provided shell commands run by Claude Code

### 🔧 改进

- Transcript mode (Ctrl+R): Changed Esc to exit transcript mode rather than interrupt
- Settings: Added `--settings` flag to load settings from a JSON file

### 🐛 修复

- Settings: Fixed resolution of settings files paths that are symlinks
- OTEL: Fixed reporting of wrong organization after authentication changes
- Slash commands: Fixed permissions checking for allowed-tools with Bash

---

## 📦 1.0.60

> 📝 **笔记定位**：[自定义 Sub-agents 发布](../sub-agents/index.md#-为什么需要-sub-agents)

### ✨ 新功能

- You can now create custom subagents for specialized tasks! Run /agents to get started

---

## 📦 1.0.59

> 📝 **笔记定位**：[Hook PermissionDecision 输出](../hooks/index.md#-hook-的输入与输出)

### ✨ 新功能

- SDK: 新增 tool confirmation support with canUseTool callback

### 🔧 改进

- SDK: Allow specifying env for spawned process
- Hooks: Exposed PermissionDecision to hooks (including "ask")
- Hooks: UserPromptSubmit now supports additionalContext in advanced JSON output

### 🐛 修复

- 修复 issue where some Max users that specified Opus would still see fallback to Sonnet

---

## 📦 1.0.58

> 📝 **笔记定位**：[PreCompact 事件](../hooks/index.md#-hook-事件类型) · [CLAUDE_PROJECT_DIR 环境变量](../hooks/index.md#-hook-的输入与输出)

### ✨ 新功能

- 新增 support for reading PDFs

### 🔧 改进

- MCP: 改进 server health status display in 'claude mcp list'
- Hooks: 新增 CLAUDE_PROJECT_DIR env var for hook commands

---

## 📦 1.0.57

> 📝 **笔记定位**：[斜杠命令指定模型](../skills/index.md#-自定义斜杠命令)

### ✨ 新功能

- 新增 support for specifying a model in slash commands

### 🔧 改进

- 改进 permission messages to help Claude understand allowed tools

### 🐛 修复

- Fix: Remove trailing newlines from bash output in terminal wrapping

---

## 📦 1.0.56

### ✨ 新功能

- Windows: 启用 shift+tab for mode switching on versions of Node.js that support terminal VT mode

### 🐛 修复

- Fixes for WSL IDE detection
- 修复 an issue causing awsRefreshHelper changes to .aws directory not to be picked up

---

## 📦 1.0.55

### ✨ 新功能

- SDK: 新增 ability to capture error logging
- 新增 --system-prompt-file option to override system prompt in print mode

### 🔧 改进

- 明确 knowledge cutoff for Opus 4 and Sonnet 4 models

### 🐛 修复

- Windows: fixed Ctrl+Z crash

---

## 📦 1.0.54

> 📝 **笔记定位**：[UserPromptSubmit 事件](../hooks/index.md#-hook-事件类型) · [斜杠命令 argument-hint](../skills/index.md#-自定义斜杠命令)

### 🔧 改进

- Hooks: 新增 UserPromptSubmit hook and the current working directory to hook inputs
- Custom slash commands: Added argument-hint to frontmatter
- Windows: OAuth uses port 45454 and properly constructs browser URL
- Windows: mode switching now uses alt + m, and plan mode renders properly
- Shell: Switch to in-memory shell snapshot to fix file-related errors

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

> 📝 **笔记定位**：[Windows 原生支持](../platforms/index.md#-跨平台支持) · [Bedrock API Key](../platforms/index.md#-云平台bedrock-与-vertex)

### ✨ 新功能

- 新增 support for native Windows (requires Git for Windows)
- 新增 support for Bedrock API keys through environment variable AWS_BEARER_TOKEN_BEDROCK
- Settings: /doctor can now help you identify and fix invalid setting files
- `--append-system-prompt` can now be used in interactive mode, not just --print/-p.

### 🔧 改进

- 增加 auto-compact warning threshold from 60% to 80%
- OTEL resource now includes os.type, os.version, host.arch, and wsl.version (if running on Windows Subsystem for Linux)

### 🐛 修复

- 修复 an issue with handling user directories with spaces for shell snapshots
- Custom slash commands: Fixed user-level commands in subdirectories
- Plan mode: Fixed issue where rejected plan from sub-task would get discarded

---

## 📦 1.0.48

> 📝 **笔记定位**：[PreCompact 事件](../hooks/index.md#-hook-事件类型)

### ✨ 新功能

- 新增 progress messages to Bash tool based on the last 5 lines of command output
- 新增 expanding variables support for MCP server configuration

### 🔧 改进

- 移动 shell snapshots from /tmp to ~/.claude for more reliable Bash tool calls
- 改进 IDE extension path handling when Claude Code runs in WSL
- Hooks: 新增 a PreCompact hook
- Vim mode: Added c, f/F, t/T

### 🐛 修复

- 修复 a bug in v1.0.45 where the app would sometimes freeze on launch

---

## 📦 1.0.45

### 🔧 改进

- 重新设计 Search (Grep) tool with new tool input parameters and features
- 禁用 IDE diffs for notebook files, fixing "Timeout waiting after 1000ms" error
- 更新 prompt input undo to Ctrl+\_ to avoid breaking existing Ctrl+U behavior, matching zsh's undo shortcut
- Custom slash commands: Restored namespacing in command names based on subdirectories. For example, .claude/commands/frontend/component.md is now /frontend:component, not /component.

### 🐛 修复

- 修复 config file corruption issue by enforcing atomic writes
- Stop Hooks: Fixed transcript path after /clear and fixed triggering when loop ends with tool call

---

## 📦 1.0.44

### ✨ 新功能

- 新的 /export command lets you quickly export a conversation for sharing

### 🔧 改进

- MCP: resource_link tool results are now supported
- MCP: tool annotations and tool titles now display in /mcp view
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

> 📝 **笔记定位**：[Stop/SubagentStop 事件拆分](../hooks/index.md#-hook-事件类型) · [Hook 超时配置](../hooks/index.md#-hook-配置详解)

### ✨ 新功能

- 新的 tool parameters JSON for Bash tool in `tool_decision` event

### 🔧 改进

- Hooks: Split Stop hook triggering into Stop and SubagentStop
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

> 📝 **笔记定位**：[🎉 Hooks 正式发布](../hooks/index.md#-为什么需要-hooks)

### ✨ 新功能

- 发布 hooks. Special thanks to community input in https://github.com/anthropics/claude-code/issues/712. Docs: https://code.claude.com/docs/en/hooks

---

## 📦 1.0.37

### 🗑️ 移除

- 移除 ability to set `Proxy-Authorization` header via ANTHROPIC_AUTH_TOKEN or apiKeyHelper

---

## 📦 1.0.36

### 🔧 改进

- Web search now takes today's date into context

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
- Improvements to plan mode

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

> 📝 **笔记定位**：[斜杠命令增强](../skills/index.md#-自定义斜杠命令)

### ✨ 新功能

- 新增 timestamps in Ctrl-r mode and fixed Ctrl-c handling
- Enhanced jq regex support for complex filters with pipes and select

### 🔧 改进

- Custom slash commands: Run bash output, @-mention files, enable thinking with thinking keywords
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

- Performance optimizations for memory usage

### 🔧 改进

- Resizes images before upload to prevent API size limit errors

### 🐛 修复

- Slash commands: Fix selector display during history navigation

---

## 📦 1.0.27

> 📝 **笔记定位**：[Streamable HTTP 连接](../mcp/index.md#-连接方式) · [MCP OAuth 认证](../mcp/index.md#-认证方式)

### ✨ 新功能

- MCP resources can now be @-mentioned

### 🔧 改进

- Streamable HTTP MCP servers are now supported
- Remote MCP servers (SSE and HTTP) now support OAuth
- /resume slash command to switch conversations within Claude Code

---

## 📦 1.0.25

### 🔧 改进

- Slash commands: moved "project" and "user" prefixes to descriptions
- Slash commands: improved reliability for command discovery
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

- MCP authentication and permission improvements

### 🐛 修复

- 修复 issue where pasted content was lost when dialogs appeared

---

## 📦 1.0.17

### 🔧 改进

- We now emit messages from sub-tasks in -p mode (look for the parent_tool_use_id property)
- MCP server list UI improvements
- 更新 Claude Code process title to display "claude" instead of "node"

### 🐛 修复

- 修复 crashes when the VS Code diff tool is invoked multiple times quickly

---

## 📦 1.0.11

### ✨ 新功能

- Claude Code can now also be used with a Claude Pro subscription
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
- Bugfixes, UI polish, and tool reliability improvements

### 🗑️ 移除

- 弃用 claude config commands in favor of editing settings.json

---

## 📦 1.0.6

### ✨ 新功能

- 新增 support for symlinks in @file typeahead

### 🔧 改进

- 改进 edit reliability for tab-indented files
- Respect CLAUDE_CONFIG_DIR everywhere
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

> 📝 **笔记定位**：[Sonnet 4 / Opus 4 模型](../how-it-works/index.md#-模型选择与配置)

### ✨ 新功能

- 引入 Sonnet 4 and Opus 4 models

### 🔧 改进

- Claude Code is now generally available

---

## 📦 0.2.125

> 📝 **笔记定位**：[Bedrock ARN 格式变更](../platforms/index.md#-云平台bedrock-与-vertex)

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

> 📝 **笔记定位**：[CLAUDE.md 文件导入](../configuration/index.md#-claudemd项目级上下文)

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

> 📝 **笔记定位**：[@-mention 文件引用](../skills/index.md#-mention-机制) · [--mcp-config 启动参数](../mcp/index.md#-管理-mcp-服务器)

### ⚡ 性能

- 改进 performance for filename auto-complete

### 🔧 改进

- Hit Enter to queue up additional messages while Claude is working
- Drag in or copy/paste image files directly into the prompt
- @-mention files to directly add them to context
- Run one-off MCP servers with `claude --mcp-config <path-to-file>`

---

## 📦 0.2.74

> 📝 **笔记定位**：[Task 工具写入与 Bash](../sub-agents/index.md#-sub-agents-的协作模式)

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

> 📝 **笔记定位**：[共享项目权限规则](../enterprise/index.md#-安全与合规)

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

> 📝 **笔记定位**：[MCP SSE 传输协议](../mcp/index.md#-连接方式)

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

> 📝 **笔记定位**：[自定义斜杠命令首发](../skills/index.md#-自定义斜杠命令) · [MCP 调试模式](../mcp/index.md#-管理-mcp-服务器)

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