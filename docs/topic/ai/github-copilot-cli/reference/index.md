---
title: 参考手册
description: CLI 命令速查、关键 Flag、环境变量和工具名称参考
---

# 参考手册

本文档是一个快速查阅手册，以表格为主，涵盖 CLI 启动命令、关键 Flag、Slash 命令、环境变量、内置工具、配置目录、MCP 服务器、Skills、Custom Agents 及 OpenTelemetry 监控等参考信息。

## CLI 启动命令

| 命令 | 说明 |
|------|------|
| `copilot` | 启动交互式会话 |
| `copilot -p "提示词"` | 编程式执行单条指令，完成后退出 |
| `copilot -i "提示词"` | 启动交互式会话并自动执行该提示词 |
| `copilot --resume[=VALUE]` | 恢复之前的会话（可指定会话 ID、前缀或名称） |
| `copilot --continue` | 恢复当前目录最近的会话 |
| `copilot --plan` | 以 Plan 模式启动 |
| `copilot --autopilot` | 以 Autopilot 模式启动 |
| `copilot --mode=MODE` | 指定启动模式（interactive / plan / autopilot） |
| `copilot --remote` | 启用远程访问（GitHub.com / Mobile） |
| `copilot --connect[=SESSION-ID]` | 直接连接到远程会话 |
| `copilot login` | 启动 OAuth 认证流程 |
| `copilot login --host HOST` | 认证到 GitHub Enterprise 实例 |
| `copilot update` | 下载并安装最新版本 |
| `copilot version` | 显示版本信息并检查更新 |
| `copilot init` | 为当前仓库初始化 Copilot 自定义指令 |
| `copilot completion SHELL` | 输出 Shell 自动补全脚本（bash / zsh / fish） |
| `copilot mcp` | 命令行管理 MCP 服务器配置 |
| `copilot plugin` | 管理插件和插件市场 |
| `copilot help [TOPIC]` | 显示帮助信息 |

## 关键 Flag 速查

### 通用选项

| Flag | 说明 |
|------|------|
| `-p` / `--prompt` | 编程式执行（非交互模式） |
| `-i` / `--interactive` | 启动交互式会话并自动执行提示词 |
| `-s` / `--silent` | 静默模式，仅输出 agent 回复 |
| `--model=MODEL` | 指定 AI 模型（`auto` 为自动选择） |
| `--mode=MODE` | 指定启动模式（interactive / plan / autopilot） |
| `--agent=AGENT` | 指定使用的自定义 Agent |
| `-n` / `--name=NAME` | 命名会话 |
| `--resume[=VALUE]` | 恢复会话（ID / 前缀 / 名称） |
| `--continue` | 从最近的会话继续 |
| `--remote` / `--no-remote` | 启用 / 禁用远程访问 |
| `--connect[=ID]` | 连接到远程会话 |
| `--experimental` / `--no-experimental` | 启用 / 禁用实验性功能 |
| `--effort=LEVEL` / `--reasoning-effort=LEVEL` | 推理努力级别（low / medium / high） |
| `--output-format=FORMAT` | 输出格式（text / json） |
| `--log-level=LEVEL` | 日志级别（none / error / warning / info / debug / all） |
| `--log-dir=DIRECTORY` | 日志文件目录 |
| `--banner` / `--no-banner` | 显示 / 隐藏启动 Banner |
| `--color-mode` | 颜色模式（default / dim / high-contrast / colorblind） |
| `--theme` | 终端主题（auto / dark / light） |
| `--screen-reader` | 启用屏幕阅读器优化 |

### 权限选项

| Flag | 说明 |
|------|------|
| `--allow-all` / `--yolo` | 跳过所有权限确认（工具 + 路径 + URL） |
| `--allow-all-tools` | 跳过工具审批 |
| `--allow-all-paths` | 禁用路径验证 |
| `--allow-all-urls` | 禁用 URL 验证 |
| `--allow-tool=TOOL ...` | 预批准特定工具（逗号分隔） |
| `--deny-tool=TOOL ...` | 禁止特定工具（逗号分隔） |
| `--allow-url=URL ...` | 预批准域名或 URL（逗号分隔） |
| `--deny-url=URL ...` | 禁止域名或 URL（逗号分隔） |
| `--available-tools=TOOL ...` | 仅暴露指定工具 |
| `--excluded-tools=TOOL ...` | 排除指定工具 |
| `--disallow-temp-dir` | 禁止临时目录访问 |
| `--no-ask-user` | 阻止 agent 提问 |
| `--add-dir=PATH` | 添加允许访问的目录 |

### 编程式选项

| Flag | 说明 |
|------|------|
| `--share=PATH` | 导出会话到 Markdown 文件 |
| `--share-gist` | 导出会话到 GitHub Gist |
| `--secret-env-vars=VAR ...` | 在输出中脱敏指定环境变量 |

## 工具权限模式

`--allow-tool` 和 `--deny-tool` 支持 `Kind(argument)` 格式的权限模式，`argument` 可省略（匹配该类型所有工具）。

| Kind | 说明 | 示例 |
|------|------|------|
| `memory` | Agent 记忆写入 | `memory` |
| `read` | 文件或目录读取 | `read`, `read(.env)` |
| `shell` | Shell 命令执行 | `shell(git push)`, `shell(git:*)`, `shell` |
| `url` | URL 访问 | `url(github.com)`, `url(https://*.api.com)` |
| `write` | 文件创建或修改 | `write`, `write(src/*.ts)` |
| `SERVER-NAME` | MCP 服务器工具调用 | `MyMCP(create_issue)`, `MyMCP` |

!!! tip

- `shell` 的 `:*` 后缀匹配命令词干 + 空格，如 `shell(git:*)` 匹配 `git push` 但不匹配 `gitea`
- Deny 规则始终优先于 Allow 规则，即使设置了 `--allow-all`

## Slash 命令完整列表

### 通用命令

| 命令 | 功能 |
|------|------|
| `/help` | 查看帮助 |
| `/clear` / `/new` / `/reset` | 放弃当前会话，开始新对话 |
| `/exit` / `/quit` | 退出 |
| `/compact` | 压缩上下文，减少 token 占用 |
| `/context` | 查看 context window 使用情况 |
| `/undo` / `/rewind` | 撤销最后一轮并恢复文件变更 |
| `/copy` | 复制最近回复到剪贴板 |
| `/ask QUESTION` | 快速提问（不加入对话历史，实验性） |
| `/session` | 查看 / 管理会话信息 |
| `/resume` / `/continue` | 切换到其他会话 |
| `/rename [NAME]` | 重命名会话 |
| `/version` | 查看版本 |
| `/restart` | 热重启（保留会话） |
| `/changelog` | 查看更新日志 |
| `/usage` | 查看使用统计 |
| `/tasks` | 查看后台任务（子代理和 Shell 会话） |

### 模式与权限

| 命令 | 功能 |
|------|------|
| `/plan [PROMPT]` | 进入 Plan 模式 |
| `/experimental [on\|off\|show]` | 切换实验性功能 |
| `/allow-all [on\|off\|show]` / `/yolo` | 启用所有权限 |
| `/model` / `/models [MODEL]` | 切换模型 |
| `/theme` | 切换颜色模式 |
| `/statusline` / `/footer` | 自定义状态栏 |
| `/remote [on\|off]` | 远程访问控制 |
| `/reset-allowed-tools` | 重置已允许的工具列表 |
| `/add-dir PATH` | 添加允许访问的目录 |
| `/list-dirs` | 查看已允许的目录 |

### 开发工作流

| 命令 | 功能 |
|------|------|
| `/review [PROMPT]` | 代码审查 |
| `/diff` | 查看工作区变更 |
| `/pr [view\|create\|fix\|auto]` | PR 管理 |
| `/research TOPIC` | 深度研究 |
| `/delegate [PROMPT]` | 委派到远程仓库，生成 PR |
| `/fleet [PROMPT]` | 并行子代理执行 |
| `/cwd` / `/cd [PATH]` | 切换或查看工作目录 |

### 导出与共享

| 命令 | 功能 |
|------|------|
| `/share [file\|html\|gist] [session\|research] [PATH]` | 导出会话（Markdown / HTML / Gist） |

### 扩展管理

| 命令 | 功能 |
|------|------|
| `/agent` | 浏览和选择 Agent |
| `/skills [list\|info\|add\|remove\|reload]` | 管理 Skills |
| `/mcp [show\|add\|edit\|delete\|disable\|enable\|auth\|reload]` | 管理 MCP 服务器 |
| `/plugin [marketplace\|install\|uninstall\|update\|list]` | 管理插件 |
| `/env` | 查看已加载的扩展环境 |
| `/instructions` | 查看和切换自定义指令文件 |
| `/lsp [show\|test\|reload\|help]` | 管理 LSP 服务器 |

### IDE 与其他

| 命令 | 功能 |
|------|------|
| `/ide` | 连接到 IDE 工作区 |
| `/terminal-setup` | 配置终端多行输入支持 |
| `/login` / `/logout` | 登录 / 登出 |
| `/user [show\|list\|switch]` | 管理当前 GitHub 用户 |
| `/feedback` / `/bug` | 提交反馈 |
| `/init` | 初始化仓库 Copilot 指令 |
| `/keep-alive [on\|busy\|NUMBERm\|NUMBERh]` | 防止机器休眠（实验性） |
| `/chronicle <standup\|tips\|improve\|reindex>` | 会话历史工具（实验性） |
| `/downgrade <VERSION>` | 降级到指定版本 |

## 交互界面快捷键

### 全局快捷键

| 快捷键 | 功能 |
|--------|------|
| `@ FILENAME` | 引入文件内容到上下文 |
| `# NUMBER` | 引入 GitHub Issue 或 PR |
| `! COMMAND` | 绕过 Copilot 执行本地 Shell 命令 |
| `?` | 打开快速帮助（空提示时） |
| `Esc` | 取消当前操作 |
| `Ctrl+C` | 取消操作 / 清除输入；按两次退出 |
| `Ctrl+D` | 关闭 |
| `Ctrl+G` | 用外部编辑器编辑提示词（`$EDITOR`） |
| `Ctrl+L` | 清屏 |
| `Ctrl+Enter` / `Ctrl+Q` | Agent 忙碌时排队发送消息 |
| `Ctrl+R` | 反向搜索命令历史 |
| `Ctrl+V` | 从剪贴板粘贴为附件 |
| `Ctrl+X` 然后 `/` | 在输入过程中执行 Slash 命令 |
| `Ctrl+X` 然后 `e` | 用外部编辑器编辑提示词 |
| `Ctrl+X` 然后 `o` | 打开时间线中最近的链接 |
| `Ctrl+Z` | 挂起到后台（Unix） |
| `Shift+Enter` / `Alt+Enter` | 输入中插入换行 |
| `Shift+Tab` | 在 interactive / plan / autopilot 模式间切换 |

### 时间线快捷键

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+O` | 展开时间线中最近的项目详情 |
| `Ctrl+E` | 展开时间线中所有项目 |
| `Ctrl+T` | 展开 / 折叠推理过程显示 |
| `Page Up` / `Page Down` | 上下翻页时间线 |

### 导航快捷键

| 快捷键 | 功能 |
|--------|------|
| `Ctrl+A` | 移到行首 |
| `Ctrl+E` | 移到行尾 |
| `Ctrl+B` / `Ctrl+F` | 前 / 后移动一个字符 |
| `Ctrl+H` | 删除前一个字符 |
| `Ctrl+K` | 删除到行尾 |
| `Ctrl+U` | 删除到行首 |
| `Ctrl+W` | 删除前一个单词 |
| `Alt+←` / `Alt+→` | 按单词移动光标 |
| `↑` / `↓` | 浏览命令历史 |
| `Tab` / `Ctrl+Y` | 接受内联补全建议 |

## 权限审批响应

当 CLI 提示权限确认时，可使用以下按键：

| 按键 | 效果 |
|------|------|
| `y` | 允许本次请求 |
| `n` | 拒绝本次请求 |
| `!` | 允许本次及后续所有同类请求（当前会话） |
| `#` | 拒绝本次及后续所有同类请求（当前会话） |
| `?` | 显示请求详细信息 |

## 环境变量参考

### 认证相关

| 变量 | 说明 |
|------|------|
| `COPILOT_GITHUB_TOKEN` | GitHub 认证 Token（最高优先级） |
| `GH_TOKEN` | GitHub CLI Token（第二优先级） |
| `GITHUB_TOKEN` | GitHub Token（第三优先级） |
| `COPILOT_GH_HOST` | Copilot 专用 GitHub 主机名，覆盖 `GH_HOST` |

### 模型与行为

| 变量 | 说明 |
|------|------|
| `COPILOT_MODEL` | 默认 AI 模型 |
| `COPILOT_HOME` | 配置目录位置（默认 `~/.copilot`） |
| `COPILOT_CACHE_HOME` | 缓存目录覆盖 |
| `COPILOT_ALLOW_ALL` | 设为 `true` 允许所有权限 |
| `COPILOT_AUTO_UPDATE` | 设为 `false` 禁用自动更新 |
| `COPILOT_EDITOR` | 编辑器命令（检查顺序：`COPILOT_EDITOR` → `$VISUAL` → `$EDITOR` → `vi`） |
| `COPILOT_CUSTOM_INSTRUCTIONS_DIRS` | 自定义指令附加目录（逗号分隔） |
| `COPILOT_SKILLS_DIRS` | Skills 附加目录（逗号分隔） |
| `COPILOT_PROMPT_FRAME` | 设为 `1` 启用装饰性 UI 框架 |

### 子代理限制

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `COPILOT_SUBAGENT_MAX_DEPTH` | `6` | 子代理最大嵌套深度（1–256） |
| `COPILOT_SUBAGENT_MAX_CONCURRENT` | `32` | 最大并发子代理数（1–256） |

### 终端与显示

| 变量 | 说明 |
|------|------|
| `COLORFGBG` | 深色 / 浅色终端背景检测回退 |
| `PLAIN_DIFF` | 设为 `true` 禁用富 diff 渲染 |
| `USE_BUILTIN_RIPGREP` | 设为 `false` 使用系统 ripgrep |

## 内置工具速查

### Shell 工具

| 工具 | 说明 |
|------|------|
| `bash` | 执行 Bash 命令 |
| `powershell` | 执行 PowerShell 命令 |
| `list_bash` / `list_powershell` | 列出活跃的 Shell 会话 |
| `read_bash` / `read_powershell` | 读取 Shell 会话输出 |
| `stop_bash` / `stop_powershell` | 终止 Shell 会话 |
| `write_bash` / `write_powershell` | 向 Shell 会话发送输入 |

### 文件操作工具

| 工具 | 说明 |
|------|------|
| `create` | 创建新文件 |
| `edit` | 通过字符串替换编辑文件 |
| `view` | 读取文件或目录内容 |
| `apply_patch` | 应用补丁（部分模型替代 edit/create） |

### Agent 与任务工具

| 工具 | 说明 |
|------|------|
| `task` | 运行子代理 |
| `list_agents` | 列出可用 Agent |
| `read_agent` | 检查后台 Agent 状态 |
| `ask_user` | 向用户提问 |
| `skill` | 调用自定义 Skill |

### 搜索与网络工具

| 工具 | 说明 |
|------|------|
| `glob` | 文件模式匹配搜索 |
| `grep` / `rg` | 文件内容搜索 |
| `web_fetch` | 获取并解析网页内容 |

## 配置目录结构

默认路径：`~/.copilot`（可通过 `COPILOT_HOME` 覆盖）

### 用户可编辑文件

| 路径 | 说明 |
|------|------|
| `settings.json` | 个人配置（JSONC 格式，支持注释） |
| `copilot-instructions.md` | 全局自定义指令（应用于所有会话） |
| `instructions/` | 额外指令文件目录（`*.instructions.md`） |
| `mcp-config.json` | 用户级 MCP 服务器定义 |
| `lsp-config.json` | 用户级 LSP 服务器定义 |
| `agents/` | 个人自定义 Agent（`.agent.md` 文件） |
| `skills/` | 个人自定义 Skill（子目录 + `SKILL.md`） |
| `hooks/` | 用户级 Hook 脚本 |

### 自动管理文件

| 路径 | 说明 |
|------|------|
| `config.json` | 应用内部状态（认证、插件元数据等） |
| `permissions-config.json` | 已保存的工具和目录权限 |
| `session-state/` | 会话历史数据（按会话 ID 组织） |
| `session-store.db` | 跨会话数据的 SQLite 数据库 |
| `logs/` | 会话日志文件 |
| `installed-plugins/` | 已安装的插件文件 |
| `plugin-data/` | 插件持久化数据 |
| `ide/` | IDE 集成状态 |

## 配置文件设置

配置层级：用户 → 仓库 → 本地，更具体的层级覆盖更通用的层级。

### 配置层级

| 层级 | 位置 | 用途 |
|------|------|------|
| 用户 | `~/.copilot/settings.json` | 所有仓库的全局默认值 |
| 仓库 | `.github/copilot/settings.json` | 共享仓库配置（提交到仓库） |
| 本地 | `.github/copilot/settings.local.json` | 个人覆盖（加入 `.gitignore`） |

### 常用 settings.json 键

| 键 | 类型 | 默认值 | 说明 |
|----|------|--------|------|
| `model` | string | varies | AI 模型，设 `auto` 自动选择 |
| `effortLevel` | string | `"medium"` | 推理努力级别（low / medium / high / xhigh） |
| `experimental` | boolean | `false` | 启用实验性功能 |
| `askUser` | boolean | `true` | 允许 agent 提问 |
| `autoUpdate` | boolean | `true` | 自动下载 CLI 更新 |
| `theme` | string | `"auto"` | 终端主题（auto / dark / light） |
| `colorMode` | string | `"default"` | 颜色模式 |
| `renderMarkdown` | boolean | `true` | 渲染 Markdown 输出 |
| `stream` | boolean | `true` | 启用流式响应 |
| `includeCoAuthoredBy` | boolean | `true` | Git commit 添加 Co-authored-by |
| `updateTerminalTitle` | boolean | `true` | 在终端标题显示当前意图 |
| `logLevel` | string | `"default"` | 日志级别 |
| `beep` | boolean | `true` | 需要关注时发出提示音 |
| `screenReader` | boolean | `false` | 屏幕阅读器优化 |
| `mouse` | boolean | `true` | Alt screen 模式鼠标支持 |
| `respectGitignore` | boolean | `true` | `@` 文件提及器排除 gitignore 文件 |
| `compactPaste` | boolean | `true` | 大粘贴（>10 行）压缩显示 |

## MCP 服务器参考

### `copilot mcp` 子命令

| 子命令 | 说明 |
|--------|------|
| `list [--json]` | 列出所有已配置的 MCP 服务器 |
| `get <name> [--json]` | 查看特定服务器配置和工具 |
| `add <name>` | 添加服务器到用户配置 |
| `remove <name>` | 移除用户级服务器 |

### 传输类型

| 类型 | 说明 | 必填字段 |
|------|------|----------|
| `local` / `stdio` | 本地进程（stdin/stdout 通信） | `command`, `args` |
| `http` | 远程服务器（可流式 HTTP 传输） | `url` |
| `sse` | 远程服务器（Server-Sent Events） | `url` |

### 内置 MCP 服务器

| 服务器 | 说明 |
|--------|------|
| `github-mcp-server` | GitHub API 集成（Issues、PR、Commits、Code Search、Actions） |
| `playwright` | 浏览器自动化（导航、点击、输入、截图、表单） |
| `fetch` | HTTP 请求（`fetch` 工具） |
| `time` | 时间工具（`get_current_time`、`convert_time`） |

## Skills 参考

### Skill frontmatter 字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 唯一标识符（字母、数字、连字符，最长 64 字符） |
| `description` | string | 是 | 功能描述和使用时机（最长 1024 字符） |
| `allowed-tools` | string/string[] | 否 | 自动允许的工具列表，`"*"` 表示全部 |
| `user-invocable` | boolean | 否 | 用户是否可通过 `/SKILL-NAME` 调用（默认 `true`） |
| `disable-model-invocation` | boolean | 否 | 阻止 agent 自动调用（默认 `false`） |

### Skill 加载位置（优先级从高到低）

| 位置 | 范围 | 说明 |
|------|------|------|
| `.github/skills/` | 项目 | 项目专属 Skill |
| `.agents/skills/` | 项目 | 替代项目位置 |
| `.claude/skills/` | 项目 | Claude 兼容位置 |
| 父目录 `.github/skills/` | 继承 | Monorepo 父目录支持 |
| `~/.copilot/skills/` | 个人 | 个人 Skill |
| `~/.agents/skills/` | 个人 | 跨项目共享 Agent Skill |
| 插件目录 | 插件 | 已安装插件提供的 Skill |
| `COPILOT_SKILLS_DIRS` | 自定义 | 附加目录（逗号分隔） |
| CLI 内置 | 内置 | 随 CLI 分发，最低优先级 |

## Custom Agents 参考

### 内置 Agent

| Agent | 默认模型 | 说明 |
|-------|----------|------|
| `code-review` | claude-sonnet-4.5 | 高信噪比代码审查，分析 diff 中的 bug、安全和逻辑问题 |
| `configure-copilot` | varies | 通过自然语言管理 Copilot CLI 配置（实验性） |
| `explore` | claude-haiku-4.5 | 快速代码库探索，聚焦回答（<300 字） |
| `general-purpose` | claude-sonnet-4.5 | 全功能 Agent，适用于复杂多步骤任务 |
| `research` | claude-sonnet-4.6 | 深度研究 Agent，生成基于代码库和网络的报告 |
| `rubber-duck` | 互补模型 | 构建性审查，识别弱点并建议改进（实验性） |
| `task` | claude-haiku-4.5 | 命令执行（测试、构建、lint），成功返回摘要 |

### Custom Agent frontmatter 字段

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `description` | string | 是 | Agent 描述（显示在列表和 `task` 工具中） |
| `infer` | boolean | 否 | 允许主 Agent 自动委派（默认 `true`） |
| `model` | string | 否 | AI 模型，未设置则继承外部 Agent 模型 |
| `name` | string | 否 | 显示名称，默认为文件名 |
| `tools` | string[] | 否 | 可用工具（默认 `["*"]`） |
| `mcp-servers` | object | 否 | MCP 服务器配置 |

### Agent 存放位置

| 范围 | 位置 |
|------|------|
| 项目 | `.github/agents/` 或 `.claude/agents/` |
| 用户 | `~/.copilot/agents/` |
| 插件 | `<plugin>/agents/` |

## OpenTelemetry 监控

OTel 默认关闭，零开销。满足以下任一条件时自动激活：

- `COPILOT_OTEL_ENABLED=true`
- `OTEL_EXPORTER_OTLP_ENDPOINT` 已设置
- `COPILOT_OTEL_FILE_EXPORTER_PATH` 已设置

### OTel 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `COPILOT_OTEL_ENABLED` | `false` | 显式启用 OTel |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | — | OTLP 端点 URL（设置即启用 OTel） |
| `COPILOT_OTEL_EXPORTER_TYPE` | `otlp-http` | 导出器类型（`otlp-http` / `file`） |
| `OTEL_SERVICE_NAME` | `github-copilot` | 服务名称 |
| `OTEL_RESOURCE_ATTRIBUTES` | — | 额外资源属性（`key=value` 逗号分隔） |
| `OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT` | `false` | 捕获完整 prompt 和响应内容 |
| `OTEL_LOG_LEVEL` | — | OTel 日志级别 |
| `COPILOT_OTEL_FILE_EXPORTER_PATH` | — | 将所有信号写入此文件（JSONL） |
| `OTEL_EXPORTER_OTLP_HEADERS` | — | OTLP 导出器认证头 |

!!! warning

启用内容捕获（`OTEL_INSTRUMENTATION_GENAI_CAPTURE_MESSAGE_CONTENT=true`）可能包含敏感信息（代码、文件内容、用户提示词），仅在受信任环境中使用。

### 导出的指标

| 指标 | 类型 | 单位 | 说明 |
|------|------|------|------|
| `gen_ai.client.operation.duration` | Histogram | s | LLM 调用和 Agent 调用耗时 |
| `gen_ai.client.token.usage` | Histogram | tokens | 按 input/output 分类的 token 计数 |
| `gen_ai.client.operation.time_to_first_chunk` | Histogram | s | 首个流式 chunk 到达时间 |
| `gen_ai.client.operation.time_per_output_chunk` | Histogram | s | 首个 chunk 之后的 chunk 间延迟 |
| `github.copilot.tool.call.count` | Counter | calls | 按 `gen_ai.tool.name` 和 `success` 分类的工具调用 |
| `github.copilot.tool.call.duration` | Histogram | s | 按 `gen_ai.tool.name` 分类的工具执行延迟 |
| `github.copilot.agent.turn.count` | Histogram | turns | 每次 Agent 调用的 LLM 轮次 |

## 模型优先级

当 CLI 确定使用哪个模型时，按以下优先级检查（从高到低）：

1. 自定义 Agent 定义中指定的模型（如有）
2. `--model` 命令行选项
3. `COPILOT_MODEL` 环境变量
4. 配置文件中的 `model` 键
5. CLI 默认模型
