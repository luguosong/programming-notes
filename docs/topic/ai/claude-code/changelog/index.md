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

## 📦 2.1.92（2026-04-04）

### ✨ 新功能

- **`forceRemoteSettingsRefresh` 策略**：启用后 CLI 在启动时阻塞直到远程托管设置刷新完成，获取失败则退出（fail-closed）
- **Bedrock 交互式设置向导**：在登录界面选择 "3rd-party platform" 时可进入 Bedrock 配置向导
- `/cost` 现在为订阅用户显示按模型和缓存命中的分项明细
- `/release-notes` 改为交互式版本选择器
- Remote Control 会话名称默认使用主机名作为前缀，可通过 `--remote-control-session-name-prefix` 覆盖
- Pro 用户在 prompt 缓存过期后返回会话时看到底部提示

### ⚡ 性能

- Write 工具在大文件上的 diff 计算速度提升 60%（含 Tab/`&`/`$` 的文件）

### 🔧 改进

- Linux 沙箱在 npm 和 native 构建中均包含 `apply-seccomp` 辅助程序

### 🐛 修复

- 修复 tmux 窗口被关闭或重新编号后子 Agent 永久无法生成（报错 "Could not determine pane count"）
- 修复 prompt 类型 Stop Hook 在小型快速模型返回 `ok:false` 时错误失败
- 修复工具输入验证在流式传输将数组/对象字段作为 JSON 编码字符串发送时失败
- 修复扩展思考产生纯空白文本块时导致的 API 400 错误
- 修复自动驾驶按键意外提交反馈调查
- 修复 Homebrew 安装的更新提示未使用 cask 发布渠道
- 修复多行提示中 `ctrl+e` 在行尾时跳转到下一行末尾
- 修复全屏模式向上滚动时同一消息出现在两个位置
- 修复空闲返回提示 "/clear to save X tokens" 显示累计会话 token 而非当前上下文大小
- 修复插件 MCP 服务器与 claude.ai connector 重复时启动卡在 "connecting"

### 🗑️ 移除

- 移除 `/tag` 命令
- 移除 `/vim` 命令（通过 `/config` → Editor mode 切换 vim 模式）

---

## 📦 2.1.91（2026-04-02）

### ✨ 新功能

- MCP 工具结果持久化：通过 `_meta["anthropic/maxResultSizeChars"]` 注解覆盖上限（最高 500K）
- **`disableSkillShellExecution` 设置**：禁用 Skills、自定义斜杠命令和插件命令中的内联 shell 执行
- `claude-cli://open?q=` 深度链接支持多行提示
- 插件可在 `bin/` 下附带可执行文件，并从 Bash 工具中直接调用

### 🔧 改进

- `/feedback` 不可用时显示原因说明，而非从斜杠菜单中消失
- 改进 `/claude-api` Skill 的 Agent 设计模式指导
- Edit 工具使用更短的 `old_string` 锚点，减少输出 token

### ⚡ 性能

- Bun 环境下 `stripAnsi` 通过 `Bun.stripANSI` 路由执行，性能更佳

### 🐛 修复

- 修复 `--resume` 导致的 transcript 链断裂，可能丢失对话历史
- 修复 `cmd+delete` 在 iTerm2、kitty、WezTerm、Ghostty 和 Windows Terminal 上不能删除到行首
- 修复远程会话中计划模式在容器重启后丢失计划文件
- 修复 `permissions.defaultMode: "auto"` 在 settings.json 中的 JSON schema 验证
- 修复 Windows 版本清理未保护活跃版本的回滚副本

---

## 📦 2.1.90（2026-04-01）

### ✨ 新功能

- **`/powerup` 命令**：交互式课程，通过动画演示教授 Claude Code 功能
- 新增 `CLAUDE_CODE_PLUGIN_KEEP_MARKETPLACE_ON_FAILURE` 环境变量，`git pull` 失败时保留 marketplace 缓存
- `.husky` 加入受保护目录列表（acceptEdits 模式）

### ⚡ 性能

- SSE 传输层以线性时间处理大型流式帧（此前为二次方）
- 长对话 SDK 会话的 transcript 写入不再呈二次方减速

### 🔧 改进

- `/resume` 全项目视图改为并行加载项目会话
- `--resume` 选择器不再显示 `claude -p` 或 SDK 调用创建的会话

### 🐛 修复

- 修复达到用量限制后速率限制选项对话框无限自动弹出
- 修复 `--resume` 在配置了延迟工具或 MCP 服务器时首次请求完全 prompt 缓存未命中（v2.1.69 引入的回退）
- 修复 PostToolUse format-on-save Hook 重写文件后 `Edit`/`Write` 报错 "File content has changed"
- 修复输出 JSON 到 stdout 并以退出码 2 结束的 `PreToolUse` Hook 未正确阻止工具调用
- 修复自动模式不尊重用户明确限制（如 "don't push"、"wait for X before Y"）
- 加固 PowerShell 工具权限检查：修复尾部 `&` 后台作业绕过、`-ErrorAction Break` 调试器挂起、压缩包提取 TOCTOU

### 🗑️ 移除

- 移除 `Get-DnsClientCache` 和 `ipconfig /displaydns` 的自动允许（DNS 缓存隐私）

---

## 📦 2.1.89（2026-04-01）

### ✨ 新功能

- **`"defer"` 权限决策**：`PreToolUse` Hook 返回 `"defer"` 可暂停 headless 会话的工具调用，稍后通过 `-p --resume` 恢复
- **`CLAUDE_CODE_NO_FLICKER=1` 环境变量**：无闪烁备用屏幕渲染
- **`PermissionDenied` Hook**：在自动模式分类器拒绝后触发
- 命名子 Agent 出现在 `@` 提及的自动补全建议中
- `MCP_CONNECTION_NONBLOCKING=true` 用于 `-p` 模式跳过 MCP 连接等待
- 自动模式：被拒绝的命令显示通知并出现在 `/permissions` → Recent 标签
- `/buddy` 愚人节彩蛋——孵化一个观看你编码的小生物 🐣

### 🔧 改进

- `@` 提及自动补全优先排列源文件，高于同名 MCP 资源
- 交互式会话中默认不再生成思考摘要
- `/env` 现在同时应用于 PowerShell 工具命令（此前仅影响 Bash）

### 🐛 修复

- 修复 Edit/Write 工具在 Windows 上将 CRLF 加倍并剥离 Markdown 硬换行
- 修复长运行会话中大型 JSON 输入作为 LRU 缓存键导致的内存泄漏
- 修复 LSP 服务器崩溃后进入僵尸状态——现在在下次请求时重启
- 修复包含 CJK 或 Emoji 的提示历史条目被静默丢弃
- 修复 autocompact 抖动循环——连续 3 次失败后停止，不再消耗 API 调用
- 修复嵌套 CLAUDE.md 文件在长会话中被重复注入数十次
- 修复 API 返回权限错误时误显示 "Rate limit reached"
- 修复 Windows Terminal Preview 1.25 上 Shift+Enter 提交而非插入换行
- 修复 PowerShell 工具在命令向 stderr 写入进度信息时错误报告失败

---

## 📦 2.1.87（2026-03-29）

### 🐛 修复

- 修复 Cowork Dispatch 中消息无法送达

---

## 📦 2.1.86（2026-03-27）

### ✨ 新功能

- API 请求新增 `X-Claude-Code-Session-Id` 头，用于代理端会话聚合
- `.jj` 和 `.sl` 加入 VCS 目录排除列表（Jujutsu/Sapling 支持）

### ⚡ 性能

- 减少启动阶段配置大量 claude.ai MCP connector 时的事件循环阻塞
- 减少 `@` 提及文件时的 token 开销
- Read 工具使用紧凑行号格式并去重未变更的重复读取，降低 token 用量
- 提升 Bedrock、Vertex 和 Foundry 用户的 prompt 缓存命中率

### 🐛 修复

- 修复 `--resume` 在 v2.1.85 之前创建的会话上报错 "tool_use ids were found without tool_result blocks"
- 修复配置了条件 Skills 或 Rules 时 Write/Edit/Read 在项目根目录外的文件上失败
- 修复每次 Skill 调用时不必要的配置磁盘写入（Windows 上导致性能问题/文件损坏）
- 修复非常长的会话使用 `/feedback` 时可能的内存溢出崩溃
- 修复 `--bare` 模式在交互式会话中丢弃 MCP 工具
- 修复 v2.1.83 以来官方 marketplace 插件脚本在 macOS/Linux 上报 "Permission denied"
- 修复多实例运行时状态栏显示其他会话的模型
- 修复长会话中 markdown/highlight 渲染缓存导致的内存增长

---

## 📦 2.1.85（2026-03-26）

### ✨ 新功能

- MCP `headersHelper` 脚本新增 `CLAUDE_CODE_MCP_SERVER_NAME` 和 `CLAUDE_CODE_MCP_SERVER_URL` 环境变量
- Hook 新增条件 `if` 字段，使用权限规则语法过滤触发时机
- Transcript 中新增定时任务触发的时间戳标记
- MCP OAuth 遵循 RFC 9728 Protected Resource Metadata 发现规范
- `PreToolUse` Hook 可通过返回 `updatedInput` + `permissionDecision: "allow"` 来满足 `AskUserQuestion`
- 插件选项支持外部配置——插件可在启用时提示用户配置

### 🔧 改进

- 组织策略阻止的插件无法再被安装或启用
- `--bare -p`（SDK 模式）到 API 请求快约 14%

### ⚡ 性能

- 提升大型仓库中 `@` 提及文件自动补全的性能
- 滚动大型 transcript 的性能改进（WASM yoga-layout 替换为纯 TypeScript）

### 🐛 修复

- 修复对话过长时 `/compact` 报错 "context exceeded"
- 修复 `--worktree` 在非 git 仓库中退出报错
- 修复 `deniedMcpServers` 设置未阻止 claude.ai MCP 服务器
- 修复存在 refresh token 时 MCP 升级授权失败
- 修复边缘连接变动期间持续的 ECONNRESET 错误
- 修复退出后终端在 Ghostty、Kitty、WezTerm 中残留增强键盘模式

---

## 📦 2.1.84（2026-03-26）

> 📝 **笔记定位**：[内置工具](../how-it-works/index.md#-内置工具) · [Hook 触发时机](../hooks/index.md#-hook-能在哪些时机触发) · [压缩机制](../context-engineering/index.md#-压缩机制的陷阱) · [安全架构](../enterprise/index.md#-安全架构) · [MCP 认证](../mcp/index.md#-认证方式)

### ✨ 新功能

- **PowerShell 工具**：Windows 上新增 PowerShell 工具（opt-in 预览）
- 新增 `ANTHROPIC_DEFAULT_{OPUS,SONNET,HAIKU}_MODEL_SUPPORTS` 环境变量用于第三方模型能力覆盖
- 新增 `TaskCreated` Hook，在 `TaskCreate` 创建任务时触发
- 新增 `WorktreeCreate` Hook 的 `type: "http"` 支持
- 新增 `allowedChannelPlugins` 托管设置供团队/企业管理员使用
- 新增空闲返回提示：离开 75 分钟以上返回时建议 `/clear`
- 深度链接（`claude-cli://`）在首选终端中打开
- Rules 和 Skills 的 `paths:` frontmatter 支持 YAML glob 列表

### ⚡ 性能

- 交互式启动提速约 30ms（`setup()` 并行化）
- 提升 p90 prompt 缓存命中率

### 🔧 改进

- MCP 工具描述和服务器指令上限为 2KB
- `ToolSearch` 启用时全局系统提示缓存正常工作
- Issue/PR 引用仅在写成 `owner/repo#123` 格式时变为可点击链接

### 🐛 修复

- 修复语音按住说话（push-to-talk）时按键字符泄漏到文本输入
- 修复使用 `--json-schema` 时工作流子 Agent 报 API 400
- 修复部分克隆仓库在启动时触发大量 blob 下载的性能问题
- 修复原生终端光标不跟随文本输入插入符（CJK 输入法组合现在内联渲染）

---

## 📦 2.1.83（2026-03-25）

> 📝 **笔记定位**：[托管设置](../enterprise/index.md#-托管设置managed-settings) · [安装 Claude Code](../getting-started/index.md#-怎么安装-claude-code) · [Sub-agent 工作原理](../sub-agents/index.md#sub-agent-工作原理) · [配置文件层级](../configuration/index.md#-配置文件有哪几层)

### ✨ 新功能

- **`managed-settings.d/` 分片配置目录**：不同团队可部署独立的策略片段
- **`CwdChanged` 和 `FileChanged` Hook 事件**：用于响应式环境管理
- 新增 `sandbox.failIfUnavailable` 设置，沙箱无法启动时退出并报错
- 新增 `disableDeepLinkRegistration` 设置
- 新增 `CLAUDE_CODE_SUBPROCESS_ENV_SCRUB=1` 环境变量，从子进程环境中剥离凭据
- **Transcript 搜索**：在 transcript 模式下按 `/` 进行搜索
- 新增 `Ctrl+X Ctrl+E` 作为外部编辑器的快捷键别名
- 粘贴的图片在光标处插入 `[Image #N]` 标签
- Agent 可在 frontmatter 中声明 `initialPrompt` 自动提交首轮对话

### ⚡ 性能

- 改进 `--resume` 在大型会话上的内存使用和启动延迟
- 改进插件启动——命令、Skills 和 Agents 从磁盘缓存加载，无需重新获取
- 非流式回退 token 上限提升（21k → 64k），超时延长（120s → 300s）

### 🔧 改进

- 改进 `WebFetch` 以 `Claude-User` 身份标识，遵守 `robots.txt`
- `/status` 在 Claude 响应时也可使用
- "停止所有后台 Agent" 快捷键从 `Ctrl+F` 改为 `Ctrl+X Ctrl+K`

### 🐛 修复

- 修复 macOS 上 Claude Code 退出时挂起
- 修复空闲时屏幕闪烁空白
- 修复 diff 非常大的文件时挂起（现在 5 秒后超时）
- 修复启用语音输入时启动 UI 冻结 1-8 秒
- 修复 `--mcp-config` CLI 标志绕过托管策略强制执行
- 修复 `caffeinate` 进程在退出时未终止（阻止 Mac 进入睡眠）
- 修复上下文压缩后后台子 Agent 变为不可见
- 修复 SDK 会话在 resume 时历史丢失
- 修复 Remote Control 会话在活跃运行时显示为 Idle

---

## 📦 2.1.81（2026-03-20）

### ✨ 新功能

- **`--bare` 标志**：用于脚本化 `-p` 调用——跳过 Hook、LSP、插件同步和 Skill 目录扫描
- **`--channels` 权限中继**：channel 服务器可将工具审批提示转发到手机

### 🔧 改进

- 恢复 worktree 中创建的会话时自动切换回该 worktree
- MCP 读取/搜索工具调用折叠为一行 "Queried {server}"
- MCP OAuth 支持 Client ID Metadata Document（CIMD / SEP-991）
- 计划模式默认隐藏 "clear context" 选项

### 🐛 修复

- 修复多个并发 Claude Code 会话需要重复重新认证
- 修复语音模式静默吞噬重试失败
- 修复 `--channels` 对 Team/Enterprise 组织的绕过
- 修复插件目录在会话中被删除时插件 Hook 阻塞提示提交

---

## 📦 2.1.80（2026-03-19）

### ✨ 新功能

- statusline 脚本新增 `rate_limits` 字段用于显示速率限制使用情况
- 新增 `source: 'settings'` 插件 marketplace 来源
- Skills 和斜杠命令新增 `effort` frontmatter 支持
- **`--channels`（研究预览）**：允许 MCP 服务器向会话推送消息

### ⚡ 性能

- 大型仓库启动内存减少约 80 MB（250k 文件仓库）

### 🔧 改进

- 大型 git 仓库中 `@` 文件自动补全响应更快
- 简化插件安装提示为单个 `/plugin install` 命令

### 🐛 修复

- 修复 `--resume` 丢弃并行工具结果——含并行工具调用的会话现在正确恢复
- 修复语音模式因 Cloudflare 机器人检测导致 WebSocket 失败
- 修复通过 API 代理、Bedrock 或 Vertex 使用细粒度工具流时的 400 错误

---

## 📦 2.1.79（2026-03-18）

### ✨ 新功能

- `claude auth login` 新增 `--console` 标志用于 Anthropic Console（API 计费）认证
- `/config` 菜单新增 "Show turn duration" 切换
- [VSCode] 新增 `/remote-control`——将会话桥接到 claude.ai/code，可从浏览器或手机继续

### ⚡ 性能

- 启动内存使用减少约 18MB

### 🔧 改进

- `CLAUDE_CODE_PLUGIN_SEED_DIR` 支持多个种子目录

### 🐛 修复

- 修复 `claude -p` 作为子进程在无显式 stdin 时挂起
- 修复 `-p`（print）模式下 Ctrl+C 不工作
- 修复 `/btw` 返回主 Agent 输出而非回答旁支问题
- 修复企业用户无法在速率限制（429）错误时重试
- 修复切换会话时 `SessionEnd` Hook 未触发

---

## 📦 2.1.78（2026-03-17）

### ✨ 新功能

- 新增 `StopFailure` Hook 事件，在回合因 API 错误结束时触发
- 新增 `${CLAUDE_PLUGIN_DATA}` 变量用于插件持久化状态
- 插件附带的 Agent 支持 `effort`、`maxTurns` 和 `disallowedTools` frontmatter
- 终端通知在 tmux 内运行时能到达外层终端
- 响应文本现在按行流式输出

### 🔧 改进

- `deny: ["mcp__servername"]` 正确在发送到模型前移除 MCP 服务器工具
- `--worktree` 标志现在从 worktree 目录加载 Skills 和 Hook

### 🐛 修复

- 修复 Linux 沙箱内 `git log HEAD` 失败
- 修复 API 错误触发 Stop Hook 时的无限循环
- 修复 `bypassPermissions` 模式下 `.git`、`.claude` 等受保护目录无需提示即可写入
- **安全修复**：`sandbox.enabled: true` 但依赖缺失时不再静默禁用沙箱
- 修复语音模式在 WSL2 + WSLg 上不工作
- 修复 Bash 工具在 VS Code 从 Dock/Spotlight 启动时找不到 Homebrew 二进制文件

---

## 📦 2.1.77（2026-03-17）

### ✨ 新功能

- Claude Opus 4.6 默认最大输出 token 提升至 64k，上限 128k
- 沙箱文件系统新增 `allowRead` 设置
- `/copy` 支持可选索引参数：`/copy N`

### ⚡ 性能

- macOS 启动加速约 60ms（并行读取 keychain 凭据）
- `--resume` 在 fork 密集型和超大会话上快 45%，峰值内存减少约 100-150MB

### 🔧 改进

- Agent 工具不再接受 `resume` 参数——改用 `SendMessage({to: agentId})`
- `/fork` 重命名为 `/branch`（`/fork` 仍可作为别名使用）

### 🐛 修复

- 修复复合 bash 命令 "Always Allow" 保存为单条规则而非按子命令分别保存
- 修复自动更新器启动重叠的二进制下载
- 修复 `--resume` 静默截断最近的对话历史
- 修复 `PreToolUse` Hook 返回 `"allow"` 绕过 `deny` 权限规则（包括企业托管设置）
- 修复 Write 工具在覆写 CRLF 文件时静默转换行尾
- 修复长运行会话中进度消息未被压缩导致的内存增长

---

## 📦 2.1.76（2026-03-14）

### ✨ 新功能

- **MCP elicitation 支持**：MCP 服务器可在任务中请求用户结构化输入
- 新增 `Elicitation` 和 `ElicitationResult` Hook
- 新增 `-n` / `--name` CLI 标志设置会话显示名称
- 新增 `worktree.sparsePaths` 设置用于大型 monorepo 的 git sparse-checkout
- 新增 `PostCompact` Hook，在压缩完成后触发
- 新增 `/effort` 斜杠命令设置模型努力级别

### 🔧 改进

- 改进 `--worktree` 启动性能
- 终止后台 Agent 现在保留部分结果
- 深色终端主题下引用块可读性改善

### 🐛 修复

- 修复延迟工具在对话压缩后丢失输入 schema
- 修复计划已被接受后计划模式仍要求重新审批
- 修复 `/voice` 在 Windows 上不工作
- 修复自动压缩在连续失败后无限重试（3 次后触发熔断）

---

## 📦 2.1.75（2026-03-13）

> 📝 **笔记定位**：[上下文窗口](../how-it-works/index.md#-上下文窗口) · [第三方 LLM](../integrations/index.md#-第三方-llm-提供商)

### ✨ 新功能

- Opus 4.6 默认支持 1M 上下文窗口（Max、Team 和 Enterprise 计划）
- 新增 `/color` 命令供所有用户设置提示栏颜色
- 使用 `/rename` 时提示栏显示会话名称
- 记忆文件新增最后修改时间戳

### ⚡ 性能

- 改进 macOS 非 MDM 设备上的启动性能

### 🐛 修复

- 修复 Bash 工具在管道命令中破坏 `!`
- 修复 token 估算过度计数导致过早触发上下文压缩
- 修复 `/resume` 在恢复 fork 或 continued 会话后丢失会话名称

### 🗑️ 移除

- 移除已废弃的 Windows 托管设置回退路径 `C:\ProgramData\ClaudeCode\`（破坏性变更）

---

## 📦 2.1.74（2026-03-12）

### ✨ 新功能

- `/context` 命令新增可操作建议
- 新增 `autoMemoryDirectory` 设置配置自动记忆存储目录

### 🔧 改进

- `--plugin-dir` 的本地开发副本现在覆盖已安装的 marketplace 插件

### 🐛 修复

- 修复流式 API 响应缓冲区未释放导致的内存泄漏
- 修复托管策略 `ask` 规则被用户 `allow` 规则或 Skill `allowed-tools` 绕过
- 修复 MCP OAuth 认证回调端口被占用时挂起
- 修复 Windows Terminal 中希伯来语、阿拉伯语等 RTL 文本渲染不正确
- 修复 Windows 上 LSP 服务器因文件 URI 格式错误不工作

---

## 📦 2.1.73（2026-03-11）

### ✨ 新功能

- 新增 `modelOverrides` 设置，将模型选择器条目映射到自定义 provider 模型 ID
- OAuth 登录因 SSL 证书错误失败时新增可操作指引

### 🔧 改进

- 中断 Claude 后按 Up 键恢复被中断的提示并回退
- Bedrock、Vertex 和 Foundry 上的默认 Opus 模型改为 Opus 4.6

### 🐛 修复

- 修复复杂 bash 命令权限提示触发的冻结和 100% CPU 死循环
- 修复多个 Claude Code 会话在同一项目中运行时 Bash 工具输出丢失
- 修复 `model: opus`/`sonnet`/`haiku` 的子 Agent 在 Bedrock、Vertex 和 Foundry 上被静默降级
- 修复 `/resume` 在选择器中显示当前会话
- 修复 Linux 沙箱在 native 构建中报 "ripgrep (rg) not found" 无法启动
- 修复 `--resume` 在 transcript 包含旧版 CLI 工具结果时崩溃

---

## 📦 2.1.72（2026-03-10）

### ✨ 新功能

- `/copy` 新增 `w` 键将聚焦选择直接写入文件
- `/plan` 支持可选描述参数（如 `/plan fix the auth bug`）
- 新增 `ExitWorktree` 工具离开 `EnterWorktree` 会话
- 新增 `CLAUDE_CODE_DISABLE_CRON` 环境变量

### ⚡ 性能

- 包体积减小约 510 KB
- 长会话中 CPU 利用率改善

### 🔧 改进

- 努力级别简化为 low/medium/high（移除 max），新符号 ○ ◐ ●
- `/config` 改进——Escape 取消更改，Enter 保存并关闭
- 语音输入对仓库名称和常见开发术语的转录准确度提升
- CLAUDE.md 中的 HTML 注释在自动注入时对 Claude 隐藏

### 🐛 修复

- 修复后台任务或 Hook 响应慢时退出缓慢
- 修复 Skill Hook 每次事件触发两次
- 修复 Windows 上插件安装失败和 marketplace 阻止用户范围安装等多个插件问题
- 修复 `/clear` 终止后台 Agent/Bash 任务——现在仅清除前台任务


---

## 📦 2.1.71（2026-03-07）

> 📝 **笔记定位**：[定时任务](../automation/index.md#-定时任务) · [配置文件层级](../configuration/index.md#-配置文件有哪几层)

### ✨ 新功能

- 新增 `/loop` command to run a prompt or slash command on a recurring interval (e.g. `/loop 5m check the deploy`)
- 新增 cron scheduling tools for recurring prompts within a session
- 新增 `voice:pushToTalk` keybinding to make the voice activation key rebindable in `keybindings.json` (default: space) — modifier+letter combos like `meta+k` have zero typing interference
- 新增 `fmt`, `comm`, `cmp`, `numfmt`, `expr`, `test`, `printf`, `getconf`, `seq`, `tsort`, and `pr` to the bash auto-approval allowlist

### ⚡ 性能

- 改进 startup time by deferring native image processor loading to first use

### 🔧 改进

- 改进 bridge session reconnection to complete within seconds after laptop wake from sleep, instead of waiting up to 10 minutes
- 改进 `/plugin uninstall` to disable project-scoped plugins in `.claude/settings.local.json` instead of modifying `.claude/settings.json`, so changes don't affect teammates
- 改进 plugin-provided MCP server deduplication — servers that duplicate a manually-configured server (same command/URL) are now skipped, preventing duplicate connections and tool sets. Suppressions are shown in the `/plugin` menu.
- 更新 `/debug` to toggle debug logging on mid-session, since debug logs are no longer written by default

### 🐛 修复

- 修复 stdin freeze in long-running sessions where keystrokes stop being processed but the process stays alive
- 修复 a 5–8 second startup freeze for users with voice mode enabled, caused by CoreAudio initialization blocking the main thread after system wake
- 修复 startup UI freeze when many claude.ai proxy connectors refresh an expired OAuth token simultaneously
- 修复 forked conversations (`/fork`) sharing the same plan file, which caused plan edits in one fork to overwrite the other
- 修复 the Read tool putting oversized images into context when image processing failed, breaking subsequent turns in long image-heavy sessions
- 修复 false-positive permission prompts for compound bash commands containing heredoc commit messages
- 修复 plugin installations being lost when running multiple Claude Code instances
- 修复 claude.ai connectors failing to reconnect after OAuth token refresh
- 修复 claude.ai MCP connector startup notifications appearing for every org-configured connector instead of only previously connected ones
- 修复 background agent completion notifications missing the output file path, which made it difficult for parent agents to recover agent results after context compaction
- 修复 duplicate output in Bash tool error messages when commands exit with non-zero status
- 修复 Chrome extension auto-detection getting permanently stuck on "not installed" after running on a machine without local Chrome
- 修复 `/plugin marketplace update` failing with merge conflicts when the marketplace is pinned to a branch/tag ref
- 修复 `/plugin marketplace add owner/repo@ref` incorrectly parsing `@` — previously only `#` worked as a ref separator, causing undiagnosable errors with `strictKnownMarketplaces`
- 修复 duplicate entries in `/permissions` Workspace tab when the same directory is added with and without a trailing slash
- 修复 `--print` hanging forever when team agents are configured — the exit loop no longer waits on long-lived `in_process_teammate` tasks
- 修复 "❯ Tool loaded." appearing in the REPL after every `ToolSearch` call
- 修复 prompting for `cd <cwd> && git ...` on Windows when the model uses a mingw-style path

### 🗑️ 移除

- 移除 startup notification noise for unauthenticated org-registered claude.ai connectors

---

## 📦 2.1.70（2026-03-06）

### ✨ 新功能

- [VSCode] 新增 spark icon in VS Code activity bar that lists all Claude Code sessions, with sessions opening as full editors
- [VSCode] 新增 full markdown document view for plans in VS Code, with support for adding comments to provide feedback
- [VSCode] 新增 native MCP server management dialog — use `/mcp` in the chat panel to enable/disable servers, reconnect, and manage OAuth authentication without switching to the terminal

### ⚡ 性能

- 改进 compaction to preserve images in the summarizer request, allowing prompt cache reuse for faster and cheaper compaction
- 减少 startup memory by ~426KB for users without custom CA certificates

### 🔧 改进

- 改进 error message when microphone captures silence to distinguish from "no speech detected"
- 改进 `/rename` to work while Claude is processing, instead of being silently queued
- 减少 prompt input re-renders during turns by ~74%
- 减少 Remote Control `/poll` rate to once per 10 minutes while connected (was 1–2s), cutting server load ~300×. Reconnection is unaffected — transport loss immediately wakes fast polling.

### 🐛 修复

- 修复 API 400 errors when using `ANTHROPIC_BASE_URL` with a third-party gateway — tool search now correctly detects proxy endpoints and disables `tool_reference` blocks
- 修复 `API Error: 400 This model does not support the effort parameter` when using custom Bedrock inference profiles or other model identifiers not matching standard Claude naming patterns
- 修复 empty model responses immediately after `ToolSearch` — the server renders tool schemas with system-prompt-style tags at the prompt tail, which could confuse models into stopping early
- 修复 prompt-cache bust when an MCP server with `instructions` connects after the first turn
- 修复 Enter inserting a newline instead of submitting when typing over a slow SSH connection
- 修复 clipboard corrupting non-ASCII text (CJK, emoji) on Windows/WSL by using PowerShell `Set-Clipboard`
- 修复 extra VS Code windows opening at startup on Windows when running from the VS Code integrated terminal
- 修复 voice mode failing on Windows native binary with "native audio module could not be loaded"
- 修复 push-to-talk not activating on session start when `voiceEnabled: true` was set in settings
- 修复 markdown links containing `#NNN` references incorrectly pointing to the current repository instead of the linked URL
- 修复 repeated "Model updated to Opus 4.6" notification when a project's `.claude/settings.json` has a legacy Opus model string pinned
- 修复 plugins showing as inaccurately installed in `/plugin`
- 修复 plugins showing "not found in marketplace" errors on fresh startup by auto-refreshing after marketplace installation
- 修复 `/security-review` command failing with `unknown option merge-base` on older git versions
- 修复 `/color` command having no way to reset back to the default color — `/color default`, `/color gray`, `/color reset`, and `/color none` now restore the default
- 修复 a performance regression in the `AskUserQuestion` preview dialog that re-ran markdown rendering on every keystroke in the notes input
- 修复 feature flags read during early startup never refreshing their disk cache, causing stale values to persist across sessions
- 修复 `permissions.defaultMode` settings values other than `acceptEdits` or `plan` being applied in Claude Code Remote environments — they are now ignored
- 修复 skill listing being re-injected on every `--resume` (~600 tokens saved per resume)
- 修复 teleport marker not rendering in VS Code teleported sessions

---

## 📦 2.1.69（2026-03-05）

> 📝 **笔记定位**：[Skill 加载](../skills/index.md#-skill-的加载与触发) · [安装 Claude Code](../getting-started/index.md#-怎么安装-claude-code) · [插件管理](../plugins/index.md#-安装与管理插件) · [平台全景](../platforms/index.md#-平台全景图)

### ✨ 新功能

- 新增 the `/claude-api` skill for building applications with the Claude API and Anthropic SDK
- 新增 Ctrl+U on an empty bash prompt (`!`) to exit bash mode, matching `escape` and `backspace`
- 新增 numeric keypad support for selecting options in Claude's interview questions (previously only the number row above QWERTY worked)
- 新增 optional name argument to `/remote-control` and `claude remote-control` (`/remote-control My Project` or `--name "My Project"`) to set a custom session title visible in claude.ai/code
- 新增 Voice STT support for 10 new languages (20 total) — Russian, Polish, Turkish, Dutch, Ukrainian, Greek, Czech, Danish, Swedish, Norwegian
- 新增 effort level display (e.g., "with low effort") to the logo and spinner, making it easier to see which effort setting is active
- 新增 agent name display in terminal title when using `claude --agent`
- 新增 `sandbox.enableWeakerNetworkIsolation` setting (macOS only) to allow Go programs like `gh`, `gcloud`, and `terraform` to verify TLS certificates when using a custom MITM proxy with `httpProxyPort`
- 新增 `includeGitInstructions` setting (and `CLAUDE_CODE_DISABLE_GIT_INSTRUCTIONS` env var) to remove built-in commit and PR workflow instructions from Claude's system prompt
- 新增 `/reload-plugins` command to activate pending plugin changes without restarting
- 新增 a one-time startup prompt suggesting Claude Code Desktop on macOS and Windows (max 3 showings, dismissible)
- 新增 `${CLAUDE_SKILL_DIR}` variable for skills to reference their own directory in SKILL.md content
- 新增 `InstructionsLoaded` hook event that fires when CLAUDE.md or `.claude/rules/*.md` files are loaded into context
- 新增 `agent_id` (for subagents) and `agent_type` (for subagents and `--agent`) to hook events
- 新增 `worktree` field to status line hook commands with name, path, branch, and original repo directory when running in a `--worktree` session
- 新增 `pluginTrustMessage` in managed settings to append organization-specific context to the plugin trust warning shown before installation
- 新增 policy limit fetching (e.g., remote control restrictions) for Team plan OAuth users, not just Enterprise
- 新增 `pathPattern` to `strictKnownMarketplaces` for regex-matching file/directory marketplace sources alongside `hostPattern` restrictions
- 新增 plugin source type `git-subdir` to point to a subdirectory within a git repo
- 新增 `oauth.authServerMetadataUrl` config option for MCP servers to specify a custom OAuth metadata discovery URL when standard discovery fails
- [VSCode] 新增 compaction display as a collapsible "Compacted chat" card with the summary inside

### ⚡ 性能

- 改进 spinner performance by isolating the 50ms animation loop from the surrounding shell, reducing render and CPU overhead during turns
- 改进 UI rendering performance in native binaries with React Compiler
- 改进 memory usage in long sessions by stabilizing `onSubmit` across message updates
- 改进 file operation performance by avoiding reading file contents for existence checks (6 sites)
- 减少 baseline memory by ~16MB by deferring Yoga WASM preloading
- 减少 memory footprint for SDK and CCR sessions using stream-json output
- 减少 memory usage when resuming large sessions (including compacted history)
- 减少 token usage on multi-agent tasks with more concise subagent final reports

### 🔧 改进

- 改进 `--worktree` startup by eliminating a git subprocess on the startup path
- 改进 macOS startup by eliminating redundant settings-file reloads when managed settings resolve
- 改进 macOS startup for Claude.ai enterprise/team users by skipping an unnecessary keychain lookup
- 改进 MCP `-p` startup by pipelining claude.ai config fetch with local connections and using a concurrency pool instead of sequential batching
- 改进 voice startup by removing imperceptible warmup pulse animations that were causing re-render stutter
- 改进 MCP binary content handling: tools returning PDFs, Office documents, or audio now save decoded bytes to disk with the correct file extension instead of dumping raw base64 into the conversation context. WebFetch also saves binary responses alongside its summary.
- 改进 LSP tool rendering and memory context building to no longer read entire files
- 改进 session upload and memory sync to avoid reading large files into memory before size/binary checks
- 改进 documentation to clarify that `--append-system-prompt-file` and `--system-prompt-file` work in interactive mode (the docs previously said print mode only)
- 变更 Sonnet 4.5 users on Pro/Max/Team Premium to be automatically migrated to Sonnet 4.6
- 变更 the `/resume` picker to show your most recent prompt instead of the first one. This also resolves some titles appearing as `(session)`.
- 变更 claude.ai MCP connector failures to show a notification instead of silently disappearing from the tool list
- 变更 example command suggestions to be generated deterministically instead of calling Haiku
- 变更 resuming after compaction to no longer produce a preamble recap before continuing
- [SDK] 变更 task creation to no longer require the `activeForm` field — the spinner falls back to the task subject
- [VSCode] The permission mode picker now respects `permissions.disableBypassPermissionsMode` from your effective Claude Code settings (including managed/policy settings) — when set to `disable`, bypass permissions mode is hidden from the picker

### 🐛 修复

- 修复 a security issue where nested skill discovery could load skills from gitignored directories like `node_modules`
- 修复 trust dialog silently enabling all `.mcp.json` servers on first run. You'll now see the per-server approval dialog as expected
- 修复 `claude remote-control` crashing immediately on npm installs with "bad option: --sdk-url" (anthropics/claude-code#28334)
- 修复 `--model claude-opus-4-0` and `--model claude-opus-4-1` resolving to deprecated Opus versions instead of current
- 修复 macOS keychain corruption when using multiple OAuth MCP servers. Large OAuth metadata blobs could overflow the `security -i` stdin buffer, silently leaving stale credentials behind and causing repeated `/login` prompts.
- 修复 `.credentials.json` losing `subscriptionType` (showing "Claude API" instead of "Claude Pro"/"Claude Max") when the profile endpoint transiently fails during token refresh (anthropics/claude-code#30185)
- 修复 ghost dotfiles (`.bashrc`, `HEAD`, etc.) appearing as untracked files in the working directory after sandboxed Bash commands on Linux
- 修复 Shift+Enter printing `[27;2;13~` instead of inserting a newline in Ghostty over SSH
- 修复 stash (Ctrl+S) being cleared when submitting a message while Claude is working
- 修复 ctrl+o (transcript toggle) freezing for many seconds in long sessions with lots of file edits
- 修复 plan mode feedback input not supporting multi-line text entry (backslash+Enter and Shift+Enter now insert newlines)
- 修复 cursor not moving down into blank lines at the top of the input box
- 修复 `/stats` crash when transcript files contain entries with missing or malformed timestamps
- 修复 a brief hang after a streaming error on long sessions (the transcript was being fully rewritten to drop one line; it is now truncated in place)
- 修复 `--setting-sources user` not blocking dynamically discovered project skills
- 修复 duplicate CLAUDE.md, slash commands, agents, and rules when running from a worktree nested inside its main repo (e.g. `claude -w`)
- 修复 plugin Stop/SessionEnd/etc hooks not firing after any `/plugin` operation
- 修复 plugin hooks being silently dropped when two plugins use the same `${CLAUDE_PLUGIN_ROOT}/...` command template
- 修复 memory leak in long-running SDK/CCR sessions where conversation messages were retained unnecessarily
- 修复 API 400 errors in forked agents (autocompact, summarization) when resuming sessions that were interrupted mid-tool-batch
- 修复 "unexpected tool_use_id found in tool_result blocks" error when resuming conversations that start with an orphaned tool result
- 修复 teammates accidentally spawning nested teammates via the Agent tool's `name` parameter
- 修复 `CLAUDE_CODE_MAX_OUTPUT_TOKENS` being ignored during conversation compaction
- 修复 `/compact` summary rendering as a user bubble in SDK consumers (Claude Code Remote web UI, VSCode extension)
- 修复 voice space bar getting stuck after a failed voice activation (module loading race, cold GrowthBook)
- 修复 worktree file copy on Windows
- 修复 global `.claude` folder detection on Windows
- 修复 symlink bypass where writing new files through a symlinked parent directory could escape the working directory in `acceptEdits` mode
- 修复 sandbox prompting users to approve non-allowed domains when `allowManagedDomainsOnly` is enabled in managed settings — non-allowed domains are now blocked automatically with no bypass
- 修复 interactive tools (e.g., `AskUserQuestion`) being silently auto-allowed when listed in a skill's allowed-tools, bypassing the permission prompt and running with empty answers
- 修复 multi-GB memory spike when committing with large untracked binary files in the working tree
- 修复 Escape not interrupting a running turn when the input box has draft text. Use Up arrow to pull queued messages back for editing, or Ctrl+U to clear the input line.
- 修复 Android app crash when running local slash commands (`/voice`, `/cost`) in Remote Control sessions
- 修复 a memory leak where old message array versions accumulated in React Compiler `memoCache` over long sessions
- 修复 a memory leak where REPL render scopes accumulated over long sessions (~35MB over 1000 turns)
- 修复 memory retention in in-process teammates where the parent's full conversation history was pinned for the teammate's lifetime, preventing GC after `/clear` or auto-compact
- 修复 a memory leak in interactive mode where hook events could accumulate unboundedly during long sessions
- 修复 hang when `--mcp-config` points to a corrupted file
- 修复 slow startup when many skills/plugins are installed
- 修复 `cd <outside-dir> && <cmd>` permission prompt to surface the chained command instead of only showing "Yes, allow reading from <dir>/"
- 修复 conditional `.claude/rules/*.md` files (with `paths:` frontmatter) and nested CLAUDE.md files not loading in print mode (`claude -p`)
- 修复 `/clear` not fully clearing all session caches, reducing memory retention in long sessions
- 修复 terminal flicker caused by animated elements at the scrollback boundary
- 修复 UI frame drops on macOS when using MCP servers with OAuth (regression from 2.1.x)
- 修复 occasional frame stalls during typing caused by synchronous debug log flushes
- 修复 `TeammateIdle` and `TaskCompleted` hooks to support `{"continue": false, "stopReason": "..."}` to stop the teammate, matching `Stop` hook behavior
- 修复 `WorktreeCreate` and `WorktreeRemove` plugin hooks being silently ignored
- 修复 skill descriptions with colons (e.g., "Triggers include: X, Y, Z") failing to load from SKILL.md frontmatter
- 修复 project skills without a `description:` frontmatter field not appearing in Claude's available skills list
- 修复 `/context` showing identical token counts for all MCP tools from a server
- 修复 literal `nul` file creation on Windows when the model uses CMD-style `2>nul` redirection in Git Bash
- 修复 extra blank lines appearing below each tool call in the expanded subagent transcript view (Ctrl+O)
- 修复 Tab/arrow keys not cycling Settings tabs when `/config` search box is focused but empty
- 修复 service key OAuth sessions (CCR containers) spamming `[ERROR]` logs with 403s from profile-scoped endpoints
- 修复 inconsistent color for "Remote Control active" status indicator
- 修复 Voice waveform cursor covering the first suffix letter when dictating mid-input
- 修复 Voice input showing all 5 spaces during warmup instead of capping at ~2 (aligning with the "keep holding…" hint)
- [VSCode] 修复 RTL text (Arabic, Hebrew, Persian) rendering reversed in the chat panel (regression in v2.1.63)

---

## 📦 2.1.68（2026-03-04）

### 🔧 改进

- Opus 4.6 now defaults to medium effort for Max and Team subscribers. Medium effort works well for most tasks — it's the sweet spot between speed and thoroughness. You can change this anytime with `/model`
- 重新引入 the "ultrathink" keyword to enable high effort for the next turn

### 🗑️ 移除

- 移除 Opus 4 and 4.1 from Claude Code on the first-party API — users with these models pinned are automatically moved to Opus 4.6

---

## 📦 2.1.66（2026-03-04）

### 🔧 改进

- 减少 spurious error logging

---

## 📦 2.1.63（2026-02-28）

### ✨ 新功能

- 新增 `/simplify` and `/batch` bundled slash commands
- 新增 `ENABLE_CLAUDEAI_MCP_SERVERS=false` env var to opt out from making claude.ai MCP servers available
- 新增 HTTP hooks, which can POST JSON to a URL and receive JSON instead of running a shell command
- 新增 manual URL paste fallback during MCP OAuth authentication. If the automatic localhost redirect doesn't work, you can paste the callback URL to complete authentication.
- 新增 "Always copy full response" option to the `/copy` picker. When selected, future `/copy` commands will skip the code block picker and copy the full response directly.

### ⚡ 性能

- 改进 memory usage in long sessions with subagents by stripping heavy progress message payloads during context compaction

### 🔧 改进

- Project configs & auto memory now shared across git worktrees of the same repository
- 改进 `/model` command to show the currently active model in the slash command menu
- VSCode: 新增 session rename and remove actions to the sessions list

### 🐛 修复

- 修复 local slash command output like /cost appearing as user-sent messages instead of system messages in the UI
- 修复 listener leak in bridge polling loop
- 修复 listener leak in MCP OAuth flow cleanup
- 修复 memory leak when navigating hooks configuration menu
- 修复 listener leak in interactive permission handler during auto-approvals
- 修复 file count cache ignoring glob ignore patterns
- 修复 memory leak in bash command prefix cache
- 修复 MCP tool/resource cache leak on server reconnect
- 修复 IDE host IP detection cache incorrectly sharing results across ports
- 修复 WebSocket listener leak on transport reconnect
- 修复 memory leak in git root detection cache that could cause unbounded growth in long-running sessions
- 修复 memory leak in JSON parsing cache that grew unbounded over long sessions
- VSCode: 修复 remote sessions not appearing in conversation history
- 修复 a race condition in the REPL bridge where new messages could arrive at the server interleaved with historical messages during the initial connection flush, causing message ordering issues.
- 修复 memory leak where long-running teammates retained all messages in AppState even after conversation compaction
- 修复 a memory leak where MCP server fetch caches were not cleared on disconnect, causing growing memory usage with servers that reconnect frequently
- 修复 `/clear` not resetting cached skills, which could cause stale skill content to persist in the new conversation

---

## 📦 2.1.62（2026-02-27）

### 🐛 修复

- 修复 prompt suggestion cache regression that reduced cache hit rates

---

## 📦 2.1.61（2026-02-26）

### 🐛 修复

- 修复 concurrent writes corrupting config file on Windows

---

## 📦 2.1.59（2026-02-26）

### ✨ 新功能

- 新增 `/copy` command to show an interactive picker when code blocks are present, allowing selection of individual code blocks or the full response.

### ⚡ 性能

- 改进 memory usage in multi-agent sessions by releasing completed subagent task state

### 🔧 改进

- Claude automatically saves useful context to auto-memory. Manage with /memory
- 改进 "always allow" prefix suggestions for compound bash commands (e.g. `cd /tmp && git fetch && git push`) to compute smarter per-subcommand prefixes instead of treating the whole command as one
- 改进 ordering of short task lists

### 🐛 修复

- 修复 MCP OAuth token refresh race condition when running multiple Claude Code instances simultaneously
- 修复 shell commands not showing a clear error message when the working directory has been deleted
- 修复 config file corruption that could wipe authentication when multiple Claude Code instances ran simultaneously

---

## 📦 2.1.58（2026-02-25）

### 🔧 改进

- Expand Remote Control to more users

---

## 📦 2.1.56（2026-02-25）

### 🐛 修复

- VS Code: 修复 another cause of "command 'claude-vscode.editor.openLast' not found" crashes

---

## 📦 2.1.55（2026-02-25）

### 🐛 修复

- 修复 BashTool failing on Windows with EINVAL error

---

## 📦 2.1.53（2026-02-25）

### 🐛 修复

- 修复 a UI flicker where user input would briefly disappear after submission before the message rendered
- 修复 bulk agent kill (ctrl+f) to send a single aggregate notification instead of one per agent, and to properly clear the command queue
- 修复 graceful shutdown sometimes leaving stale sessions when using Remote Control by parallelizing teardown network calls
- 修复 `--worktree` sometimes being ignored on first launch
- 修复 a panic ("switch on corrupted value") on Windows
- 修复 a crash that could occur when spawning many processes on Windows
- 修复 a crash in the WebAssembly interpreter on Linux x64 & Windows x64
- 修复 a crash that sometimes occurred after 2 minutes on Windows ARM64

---

## 📦 2.1.52（2026-02-24）

### 🐛 修复

- VS Code: 修复 extension crash on Windows ("command 'claude-vscode.editor.openLast' not found")

---

## 📦 2.1.51（2026-02-24）

### ✨ 新功能

- 新增 `claude remote-control` subcommand for external builds, enabling local environment serving for all users.
- 新增 support for custom npm registries and specific version pinning when installing plugins from npm sources
- 新增 `CLAUDE_CODE_ACCOUNT_UUID`, `CLAUDE_CODE_USER_EMAIL`, and `CLAUDE_CODE_ORGANIZATION_UUID` environment variables for SDK callers to provide account info synchronously, eliminating a race condition where early telemetry events lacked account metadata.
- Managed settings can now be set via macOS plist or Windows Registry. Learn more at https://code.claude.com/docs/en/settings#settings-files

### ⚡ 性能

- BashTool now skips login shell (`-l` flag) by default when a shell snapshot is available, improving command execution performance. Previously this required setting `CLAUDE_BASH_NO_LOGIN=true`.

### 🔧 改进

- 更新 plugin marketplace default git timeout from 30s to 120s and added `CLAUDE_CODE_PLUGIN_GIT_TIMEOUT_MS` to configure.
- Tool results larger than 50K characters are now persisted to disk (previously 100K). This reduces context window usage and improves conversation longevity.
- The `/model` picker now shows human-readable labels (e.g., "Sonnet 4.5") instead of raw model IDs for pinned model versions, with an upgrade hint when a newer version is available.

### 🐛 修复

- 修复 a security issue where `statusLine` and `fileSuggestion` hook commands could execute without workspace trust acceptance in interactive mode.
- 修复 a bug where duplicate `control_response` messages (e.g. from WebSocket reconnects) could cause API 400 errors by pushing duplicate assistant messages into the conversation.
- 修复 slash command autocomplete crashing when a plugin's SKILL.md description is a YAML array or other non-string type

---

## 📦 2.1.50（2026-02-20）

### ✨ 新功能

- 新增 support for `startupTimeout` configuration for LSP servers
- 新增 `WorktreeCreate` and `WorktreeRemove` hook events, enabling custom VCS setup and teardown when agent worktree isolation creates or removes worktrees.
- 新增 support for `isolation: worktree` in agent definitions, allowing agents to declaratively run in isolated git worktrees.
- 新增 `claude agents` CLI command to list all configured agents
- 新增 `CLAUDE_CODE_DISABLE_1M_CONTEXT` environment variable to disable 1M context window support

### ⚡ 性能

- 改进 memory usage during long sessions by clearing internal caches after compaction
- 改进 memory usage during long sessions by clearing large tool results after they have been processed
- 改进 startup performance for headless mode (`-p` flag) by deferring Yoga WASM and UI component imports

### 🔧 改进

- `CLAUDE_CODE_SIMPLE` mode now also disables MCP tools, attachments, hooks, and CLAUDE.md file loading for a fully minimal experience.
- Opus 4.6 (fast mode) now includes the full 1M context window
- VSCode: 新增 `/extra-usage` command support in VS Code sessions

### 🐛 修复

- 修复 a bug where resumed sessions could be invisible when the working directory involved symlinks, because the session storage path was resolved at different times during startup. Also fixed session data loss on SSH disconnect by flushing session data before hooks and analytics in the graceful shutdown sequence.
- Linux: 修复 native modules not loading on systems with glibc older than 2.30 (e.g., RHEL 8)
- 修复 memory leak in agent teams where completed teammate tasks were never garbage collected from session state
- 修复 `CLAUDE_CODE_SIMPLE` to fully strip down skills, session memory, custom agents, and CLAUDE.md token counting
- 修复 `/mcp reconnect` freezing the CLI when given a server name that doesn't exist
- 修复 memory leak where completed task state objects were never removed from AppState
- 修复 bug where MCP tools were not discovered when tool search is enabled and a prompt is passed in as a launch argument
- 修复 a memory leak where LSP diagnostic data was never cleaned up after delivery, causing unbounded memory growth in long sessions
- 修复 a memory leak where completed task output was not freed from memory, reducing memory usage in long sessions with many tasks
- 修复 prompt suggestion cache regression that reduced cache hit rates
- 修复 unbounded memory growth in long sessions by capping file history snapshots
- 修复 memory leak where TaskOutput retained recent lines after cleanup
- 修复 memory leak in CircularBuffer where cleared items were retained in the backing array
- 修复 memory leak in shell command execution where ChildProcess and AbortController references were retained after cleanup

---

## 📦 2.1.49（2026-02-19）

> 📝 **笔记定位**：[Sub-agent 工作原理](../sub-agents/index.md#sub-agent-工作原理) · [Hook 实践](../hooks/index.md#-hook-能用来做什么实践指南) · [安全架构](../enterprise/index.md#-安全架构)

### ✨ 新功能

- 新增 `--worktree` (`-w`) flag to start Claude in an isolated git worktree
- 新增 Ctrl+F keybinding to kill background agents (two-press confirmation)
- 新增 `ConfigChange` hook event that fires when configuration files change during a session, enabling enterprise security auditing and optional blocking of settings changes.

### ⚡ 性能

- 改进 performance in non-interactive mode (`-p`) by skipping unnecessary API calls during startup
- 改进 performance by caching authentication failures for HTTP and SSE MCP servers, avoiding repeated connection attempts to servers requiring auth
- 改进 startup performance by caching MCP auth failures to avoid redundant connection attempts
- 改进 startup performance by reducing HTTP calls for analytics token counting
- 改进 startup performance by batching MCP tool token counting into a single API call

### 🔧 改进

- 改进 MCP OAuth authentication with step-up auth support and discovery caching, reducing redundant network requests during server connections
- Subagents support `isolation: "worktree"` for working in a temporary git worktree
- Agent definitions support `background: true` to always run as a background task
- Plugins can ship `settings.json` for default configuration
- Simple mode (`CLAUDE_CODE_SIMPLE`) now includes the file edit tool in addition to the Bash tool, allowing direct file editing in simple mode.
- Permission suggestions are now populated when safety checks trigger an ask response, enabling SDK consumers to display permission options
- Sonnet 4.5 with 1M context is being removed from the Max plan in favor of our frontier Sonnet 4.6 model, which now has 1M context. Please switch in /model.
- SDK model info now includes `supportsEffort`, `supportedEffortLevels`, and `supportsAdaptiveThinking` fields so consumers can discover model capabilities.
- 改进 permission prompts for path safety and working directory blocks to show the reason for the restriction instead of a bare prompt with no context

### 🐛 修复

- 修复 file-not-found errors to suggest corrected paths when the model drops the repo folder
- 修复 Ctrl+C and ESC being silently ignored when background agents are running and the main thread is idle. Pressing twice within 3 seconds now kills all background agents.
- 修复 prompt suggestion cache regression that reduced cache hit rates.
- 修复 `plugin enable` and `plugin disable` to auto-detect the correct scope when `--scope` is not specified, instead of always defaulting to user scope
- 修复 verbose mode not updating thinking block display when toggled via `/config` — memo comparators now correctly detect verbose changes
- 修复 unbounded WASM memory growth during long sessions by periodically resetting the tree-sitter parser
- 修复 potential rendering issues caused by stale yoga layout references
- 修复 unbounded memory growth during long-running sessions caused by Yoga WASM linear memory never shrinking
- 修复 `disableAllHooks` setting to respect managed settings hierarchy — non-managed settings can no longer disable managed hooks set by policy (#26637)
- 修复 `--resume` session picker showing raw XML tags for sessions that start with commands like `/clear`. Now correctly falls through to the session ID fallback.

---

## 📦 2.1.47（2026-02-18）

### ✨ 新功能

- 新增 `last_assistant_message` field to Stop and SubagentStop hook inputs, providing the final assistant response text so hooks can access it without parsing transcript files.
- 新增 `chat:newline` keybinding action for configurable multi-line input (anthropics/claude-code#26075)
- 新增 `added_dirs` to the statusline JSON `workspace` section, exposing directories added via `/add-dir` to external scripts (anthropics/claude-code#26096)

### ⚡ 性能

- 改进 memory usage in long-running sessions by releasing API stream buffers, agent context, and skill state after use
- 改进 startup performance by deferring SessionStart hook execution, reducing time-to-interactive by ~500ms.
- 改进 performance of `@` file mentions - file suggestions now appear faster by pre-warming the index on startup and using session-based caching with background refresh.
- 改进 memory usage by trimming agent task message history after tasks complete
- 改进 memory usage during long agent sessions by eliminating O(n²) message accumulation in progress updates

### 🔧 改进

- 改进 VS Code plan preview: auto-updates as Claude iterates, enables commenting only when the plan is ready for review, and keeps the preview open when rejecting so Claude can revise.
- Search patterns in collapsed tool results are now displayed in quotes for clarity
- Use `ctrl+f` to kill all background agents instead of double-pressing ESC. Background agents now continue running when you press ESC to cancel the main thread, giving you more control over agent lifecycle.
- 简化 teammate navigation to use only Shift+Down (with wrapping) instead of both Shift+Up and Shift+Down.
- 增加 initial session count in resume picker from 10 to 50 for faster session discovery (anthropics/claude-code#26123)
- 移动 config backup files from home directory root to `~/.claude/backups/` to reduce home directory clutter (anthropics/claude-code#26130)
- The `/rename` command now updates the terminal tab title by default (anthropics/claude-code#25789)

### 🐛 修复

- 修复 FileWriteTool line counting to preserve intentional trailing blank lines instead of stripping them with `trimEnd()`.
- 修复 Windows terminal rendering bugs caused by `os.EOL` (`\r\n`) in display code — line counts now show correct values instead of always showing 1 on Windows.
- 修复 a bug where bold and colored text in markdown output could shift to the wrong characters on Windows due to `\r\n` line endings.
- 修复 compaction failing when conversation contains many PDF documents by stripping document blocks alongside images before sending to the compaction API (anthropics/claude-code#26188)
- 修复 an issue where bash tool output was silently discarded on Windows when using MSYS2 or Cygwin shells.
- 修复 the bash permission classifier to validate that returned match descriptions correspond to actual input rules, preventing hallucinated descriptions from incorrectly granting permissions
- 修复 user-defined agents only loading one file on NFS/FUSE filesystems that report zero inodes (anthropics/claude-code#26044)
- 修复 plugin agent skills silently failing to load when referenced by bare name instead of fully-qualified plugin name (anthropics/claude-code#25834)
- Windows: 修复 CWD tracking temp files never being cleaned up, causing them to accumulate indefinitely (anthropics/claude-code#17600)
- 修复 API 400 errors ("thinking blocks cannot be modified") that occurred in sessions with concurrent agents, caused by interleaved streaming content blocks preventing proper message merging.
- 修复 an issue where a single file write/edit error would abort all other parallel file write/edit operations. Independent file mutations now complete even when a sibling fails.
- 修复 custom session titles set via `/rename` being lost after resuming a conversation (anthropics/claude-code#23610)
- 修复 collapsed read/search hint text overflowing on narrow terminals by truncating from the start.
- 修复 an issue where bash commands with backslash-newline continuation lines (e.g., long commands split across multiple lines with `\`) would produce spurious empty arguments, potentially breaking command execution.
- 修复 built-in slash commands (`/help`, `/model`, `/compact`, etc.) being hidden from the autocomplete dropdown when many user skills are installed (anthropics/claude-code#22020)
- 修复 MCP servers not appearing in the MCP Management Dialog after deferred loading
- 修复 session name persisting in status bar after `/clear` command (anthropics/claude-code#26082)
- 修复 crash when a skill's `name` or `description` in SKILL.md frontmatter is a bare number (e.g., `name: 3000`) — the value is now properly coerced to a string (anthropics/claude-code#25837)
- 修复 /resume silently dropping sessions when the first message exceeds 16KB or uses array-format content (anthropics/claude-code#25721)
- 修复 `claude doctor` misclassifying mise and asdf-managed installations as native installs (anthropics/claude-code#26033)
- 修复 zsh heredoc failing with "read-only file system" error in sandboxed commands (anthropics/claude-code#25990)
- 修复 agent progress indicator showing inflated tool use count (anthropics/claude-code#26023)
- 修复 image pasting not working on WSL2 systems where Windows copies images as BMP format (anthropics/claude-code#25935)
- 修复 background agent results returning raw transcript data instead of the agent's final answer (anthropics/claude-code#26012)
- 修复 Warp terminal incorrectly prompting for Shift+Enter setup when it supports it natively (anthropics/claude-code#25957)
- 修复 CJK wide characters causing misaligned timestamps and layout elements in the TUI (anthropics/claude-code#26084)
- 修复 custom agent `model` field in `.claude/agents/*.md` being ignored when spawning team teammates (anthropics/claude-code#26064)
- 修复 plan mode being lost after context compaction, causing the model to switch from planning to implementation mode (anthropics/claude-code#26061)
- 修复 `alwaysThinkingEnabled: true` in settings.json not enabling thinking mode on Bedrock and Vertex providers (anthropics/claude-code#26074)
- 修复 `tool_decision` OTel telemetry event not being emitted in headless/SDK mode (anthropics/claude-code#26059)
- 修复 session name being lost after context compaction — renamed sessions now preserve their custom title through compaction (anthropics/claude-code#26121)
- Windows: fixed worktree session matching when drive letter casing differs (anthropics/claude-code#26123)
- 修复 `/resume <session-id>` failing to find sessions whose first message exceeds 16KB (anthropics/claude-code#25920)
- 修复 "Always allow" on multiline bash commands creating invalid permission patterns that corrupt settings (anthropics/claude-code#25909)
- 修复 React crash (error #31) when a skill's `argument-hint` in SKILL.md frontmatter uses YAML sequence syntax (e.g., `[topic: foo | bar]`) — the value is now properly coerced to a string (anthropics/claude-code#25826)
- 修复 crash when using `/fork` on sessions that used web search — null entries in search results from transcript deserialization are now handled gracefully (anthropics/claude-code#25811)
- 修复 read-only git commands triggering FSEvents file watcher loops on macOS by adding --no-optional-locks flag (anthropics/claude-code#25750)
- 修复 custom agents and skills not being discovered when running from a git worktree — project-level `.claude/agents/` and `.claude/skills/` from the main repository are now included (anthropics/claude-code#25816)
- 修复 non-interactive subcommands like `claude doctor` and `claude plugin validate` being blocked inside nested Claude sessions (anthropics/claude-code#25803)
- Windows: 修复 the same CLAUDE.md file being loaded twice when drive letter casing differs between paths (anthropics/claude-code#25756)
- 修复 inline code spans in markdown being incorrectly parsed as bash commands (anthropics/claude-code#25792)
- 修复 teammate spinners not respecting custom spinnerVerbs from settings (anthropics/claude-code#25748)
- 修复 shell commands permanently failing after a command deletes its own working directory (anthropics/claude-code#26136)
- 修复 hooks (PreToolUse, PostToolUse) silently failing to execute on Windows by using Git Bash instead of cmd.exe (anthropics/claude-code#25981)
- 修复 LSP `findReferences` and other location-based operations returning results from gitignored files (e.g., `node_modules/`, `venv/`) (anthropics/claude-code#26051)
- 修复 sessions with large first prompts (>16KB) disappearing from the /resume list (anthropics/claude-code#26140)
- 修复 shell functions with double-underscore prefixes (e.g., `__git_ps1`) not being preserved across shell sessions (anthropics/claude-code#25824)
- 修复 spinner showing "0 tokens" counter before any tokens have been received (anthropics/claude-code#26105)
- VSCode: 修复 conversation messages appearing dimmed while the AskUserQuestion dialog is open (anthropics/claude-code#26078)
- 修复 background tasks failing in git worktrees due to remote URL resolution reading from worktree-specific gitdir instead of the main repository config (anthropics/claude-code#26065)
- 修复 Right Alt key leaving visible `[25~` escape sequence residue in the input field on Windows/Git Bash terminals (anthropics/claude-code#25943)
- 修复 Edit tool silently corrupting Unicode curly quotes (\u201c\u201d \u2018\u2019) by replacing them with straight quotes when making edits (anthropics/claude-code#26141)
- 修复 OSC 8 hyperlinks only being clickable on the first line when link text wraps across multiple terminal lines.

---

## 📦 2.1.46

### ✨ 新功能

- 新增 support for using claude.ai MCP connectors in Claude Code

### 🐛 修复

- 修复 orphaned CC processes after terminal disconnect on macOS

---

## 📦 2.1.45（2026-02-17）

### ✨ 新功能

- 新增 support for Claude Sonnet 4.6
- 新增 support for reading `enabledPlugins` and `extraKnownMarketplaces` from `--add-dir` directories
- 新增 `spinnerTipsOverride` setting to customize spinner tips — configure `tips` with an array of custom tip strings, and optionally set `excludeDefault: true` to show only your custom tips instead of the built-in ones
- 新增 `SDKRateLimitInfo` and `SDKRateLimitEvent` types to the SDK, enabling consumers to receive rate limit status updates including utilization, reset times, and overage information

### ⚡ 性能

- 改进 startup performance by removing eager loading of session history for stats caching
- 改进 memory usage for shell commands that produce large output — RSS no longer grows unboundedly with command output size

### 🔧 改进

- 改进 collapsed read/search groups to show the current file or search pattern being processed beneath the summary line while active
- [VSCode] 改进 permission destination choice (project/user/session) to persist across sessions

### 🐛 修复

- 修复 Agent Teams teammates failing on Bedrock, Vertex, and Foundry by propagating API provider environment variables to tmux-spawned processes (anthropics/claude-code#23561)
- 修复 sandbox "operation not permitted" errors when writing temporary files on macOS by using the correct per-user temp directory (anthropics/claude-code#21654)
- 修复 Task tool (backgrounded agents) crashing with a `ReferenceError` on completion (anthropics/claude-code#22087)
- 修复 autocomplete suggestions not being accepted on Enter when images are pasted in the input
- 修复 skills invoked by subagents incorrectly appearing in main session context after compaction
- 修复 excessive `.claude.json.backup` files accumulating on every startup
- 修复 plugin-provided commands, agents, and hooks not being available immediately after installation without requiring a restart

---

## 📦 2.1.44（2026-02-16）

### 🐛 修复

- 修复 ENAMETOOLONG errors for deeply-nested directory paths
- 修复 auth refresh errors

---

## 📦 2.1.43

### 🐛 修复

- 修复 AWS auth refresh hanging indefinitely by adding a 3-minute timeout
- 修复 spurious warnings for non-agent markdown files in `.claude/agents/` directory
- 修复 structured-outputs beta header being sent unconditionally on Vertex/Bedrock

---

## 📦 2.1.42（2026-02-13）

### ✨ 新功能

- 新增 one-time Opus 4.6 effort callout for eligible users

### ⚡ 性能

- 改进 startup performance by deferring Zod schema construction

### 🔧 改进

- 改进 prompt cache hit rates by moving date out of system prompt

### 🐛 修复

- 修复 /resume showing interrupt messages as session titles
- 修复 image dimension limit errors to suggest /compact

---

## 📦 2.1.41（2026-02-13）

### ✨ 新功能

- 新增 guard against launching Claude Code inside another Claude Code session
- 新增 `speed` attribute to OTel events and trace spans for fast mode visibility
- 新增 `claude auth login`, `claude auth status`, and `claude auth logout` CLI subcommands
- 新增 Windows ARM64 (win32-arm64) native binary support

### 🔧 改进

- 改进 model error messages for Bedrock/Vertex/Foundry users with fallback suggestions
- 改进 `/rename` to auto-generate session name from conversation context when called without arguments
- 改进 narrow terminal layout for prompt footer

### 🐛 修复

- 修复 Agent Teams using wrong model identifier for Bedrock, Vertex, and Foundry customers
- 修复 a crash when MCP tools return image content during streaming
- 修复 /resume session previews showing raw XML tags instead of readable command names
- 修复 plugin browse showing misleading "Space to Toggle" hint for already-installed plugins
- 修复 hook blocking errors (exit code 2) not showing stderr to the user
- 修复 file resolution failing for @-mentions with anchor fragments (e.g., `@README.md#installation`)
- 修复 FileReadTool blocking the process on FIFOs, `/dev/stdin`, and large files
- 修复 background task notifications not being delivered in streaming Agent SDK mode
- 修复 cursor jumping to end on each keystroke in classifier rule input
- 修复 markdown link display text being dropped for raw URL
- 修复 auto-compact failure error notifications being shown to users
- 修复 permission wait time being included in subagent elapsed time display
- 修复 proactive ticks firing while in plan mode
- 修复 clear stale permission rules when settings change on disk
- 修复 hook blocking errors showing stderr content in UI

---

## 📦 2.1.39（2026-02-10）

### ⚡ 性能

- 改进 terminal rendering performance

### 🐛 修复

- 修复 fatal errors being swallowed instead of displayed
- 修复 process hanging after session close
- 修复 character loss at terminal screen boundary
- 修复 blank lines in verbose transcript view

---

## 📦 2.1.38（2026-02-10）

### 🔧 改进

- 改进 heredoc delimiter parsing to prevent command smuggling
- 阻止 writes to `.claude/skills` directory in sandbox mode

### 🐛 修复

- 修复 VS Code terminal scroll-to-top regression introduced in 2.1.37
- 修复 Tab key queueing slash commands instead of autocompleting
- 修复 bash permission matching for commands using environment variable wrappers
- 修复 text between tool uses disappearing when not using streaming
- 修复 duplicate sessions when resuming in VS Code extension

---

## 📦 2.1.37（2026-02-07）

### 🐛 修复

- 修复 an issue where /fast was not immediately available after enabling /extra-usage

---

## 📦 2.1.36（2026-02-07）

### ✨ 新功能

- Fast mode is now available for Opus 4.6. Learn more at https://code.claude.com/docs/en/fast-mode

---

## 📦 2.1.34（2026-02-06）

### 🐛 修复

- 修复 a crash when agent teams setting changed between renders
- 修复 a bug where commands excluded from sandboxing (via `sandbox.excludedCommands` or `dangerouslyDisableSandbox`) could bypass the Bash ask permission rule when `autoAllowBashIfSandboxed` was enabled

---

## 📦 2.1.33（2026-02-06）

### ✨ 新功能

- 新增 `TeammateIdle` and `TaskCompleted` hook events for multi-agent workflows
- 新增 support for restricting which sub-agents can be spawned via `Task(agent_type)` syntax in agent "tools" frontmatter
- 新增 `memory` frontmatter field support for agents, enabling persistent memory with `user`, `project`, or `local` scope
- 新增 plugin name to skill descriptions and `/skills` menu for better discoverability
- VSCode: 新增 support for remote sessions, allowing OAuth users to browse and resume sessions from claude.ai
- VSCode: 新增 git branch and message count to the session picker, with support for searching by branch name

### 🔧 改进

- 改进 error messages for API connection failures — now shows specific cause (e.g., ECONNREFUSED, SSL errors) instead of generic "Connection error"
- Errors from invalid managed settings are now surfaced

### 🐛 修复

- 修复 agent teammate sessions in tmux to send and receive messages
- 修复 warnings about agent teams not being available on your current plan
- 修复 an issue where submitting a new message while the model was in extended thinking would interrupt the thinking phase
- 修复 an API error that could occur when aborting mid-stream, where whitespace text combined with a thinking block would bypass normalization and produce an invalid request
- 修复 API proxy compatibility issue where 404 errors on streaming endpoints no longer triggered non-streaming fallback
- 修复 an issue where proxy settings configured via `settings.json` environment variables were not applied to WebFetch and other HTTP requests on the Node.js build
- 修复 `/resume` session picker showing raw XML markup instead of clean titles for sessions started with slash commands
- VSCode: 修复 scroll-to-bottom under-scrolling on initial session load and session switch

---

## 📦 2.1.32（2026-02-05）

> 📝 **笔记定位**：[Agent Teams](../sub-agents/index.md#agent-teams) · [第三方 LLM](../integrations/index.md#-第三方-llm-提供商) · [上下文分层](../context-engineering/index.md#-上下文分层策略) · [Agentic Loop](../how-it-works/index.md#-agentic-loopclaude-code-的核心循环)

### ✨ 新功能

- Claude Opus 4.6 is now available!
- 新增 research preview agent teams feature for multi-agent collaboration (token-intensive feature, requires setting CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1)
- 新增 "Summarize from here" to the message selector, allowing partial conversation summarization.

### 🔧 改进

- Claude now automatically records and recalls memories as it works
- Skills defined in `.claude/skills/` within additional directories (`--add-dir`) are now loaded automatically.
- 更新 --resume to re-use --agent value specified in previous conversation by default.
- Skill character budget now scales with context window (2% of context), so users with larger context windows can see more skill descriptions without truncation
- VSCode: 新增 spinner when loading past conversations list

### 🐛 修复

- 修复 `@` file completion showing incorrect relative paths when running from a subdirectory
- Fixed: Bash tool no longer throws "Bad substitution" errors when heredocs contain JavaScript template literals like `${index + 1}`, which previously interrupted tool execution
- 修复 Thai/Lao spacing vowels (สระ า, ำ) not rendering correctly in the input field
- VSCode: 修复 slash commands incorrectly being executed when pressing Enter with preceding text in the input field

---

## 📦 2.1.31（2026-02-04）

### ✨ 新功能

- 新增 session resume hint on exit, showing how to continue your conversation later
- 新增 support for full-width (zenkaku) space input from Japanese IME in checkbox selection

### ⚡ 性能

- 减少 layout jitter in the terminal when the spinner appears and disappears during streaming

### 🔧 改进

- 改进 system prompts to more clearly guide the model toward using dedicated tools (Read, Edit, Glob, Grep) instead of bash equivalents (`cat`, `sed`, `grep`, `find`), reducing unnecessary bash command usage
- 改进 PDF and request size error messages to show actual limits (100 pages, 20MB)

### 🐛 修复

- 修复 PDF too large errors permanently locking up sessions, requiring users to start a new conversation
- 修复 bash commands incorrectly reporting failure with "Read-only file system" errors when sandbox mode was enabled
- 修复 a crash that made sessions unusable after entering plan mode when project config in `~/.claude.json` was missing default fields
- 修复 `temperatureOverride` being silently ignored in the streaming API path, causing all streaming requests to use the default temperature (1) regardless of the configured override
- 修复 LSP shutdown/exit compatibility with strict language servers that reject null params

### 🗑️ 移除

- 移除 misleading Anthropic API pricing from model selector for third-party provider (Bedrock, Vertex, Foundry) users

---

## 📦 2.1.30（2026-02-03）

### ✨ 新功能

- 新增 `pages` parameter to the Read tool for PDFs, allowing specific page ranges to be read (e.g., `pages: "1-5"`). Large PDFs (>10 pages) now return a lightweight reference when `@` mentioned instead of being inlined into context.
- 新增 pre-configured OAuth client credentials for MCP servers that don't support Dynamic Client Registration (e.g., Slack). Use `--client-id` and `--client-secret` with `claude mcp add`.
- 新增 `/debug` for Claude to help troubleshoot the current session
- 新增 support for additional `git log` and `git show` flags in read-only mode (e.g., `--topo-order`, `--cherry-pick`, `--format`, `--raw`)
- 新增 token count, tool uses, and duration metrics to Task tool results
- 新增 reduced motion mode to the config
- [VSCode] 新增 multiline input support to the "Other" text input in question dialogs (use Shift+Enter for new lines)

### ⚡ 性能

- 改进 memory usage for `--resume` (68% reduction for users with many sessions) by replacing the session index with lightweight stat-based loading and progressive enrichment

### 🔧 改进

- 改进 `TaskStop` tool to display the stopped command/task description in the result line instead of a generic "Task stopped" message
- 变更 `/model` to execute immediately instead of being queued

### 🐛 修复

- 修复 phantom "(no content)" text blocks appearing in API conversation history, reducing token waste and potential model confusion
- 修复 prompt cache not correctly invalidating when tool descriptions or input schemas changed, only when tool names changed
- 修复 400 errors that could occur after running `/login` when the conversation contained thinking blocks
- 修复 a hang when resuming sessions with corrupted transcript files containing `parentUuid` cycles
- 修复 rate limit message showing incorrect "/upgrade" suggestion for Max 20x users when extra-usage is unavailable
- 修复 permission dialogs stealing focus while actively typing
- 修复 subagents not being able to access SDK-provided MCP tools because they were not synced to the shared application state
- 修复 a regression where Windows users with a `.bashrc` file could not run bash commands
- [VSCode] 修复 duplicate sessions appearing in the session list when starting a new conversation

---

## 📦 2.1.29（2026-01-31）

### 🐛 修复

- 修复 startup performance issues when resuming sessions that have `saved_hook_context`

---

## 📦 2.1.27（2026-01-30）

### ✨ 新功能

- 新增 tool call failures and denials to debug logs
- 新增 `--from-pr` flag to resume sessions linked to a specific GitHub PR number or URL

### 🔧 改进

- Sessions are now automatically linked to PRs when created via `gh pr create`

### 🐛 修复

- 修复 context management validation error for gateway users, ensuring `CLAUDE_CODE_DISABLE_EXPERIMENTAL_BETAS=1` avoids the error
- 修复 /context command not displaying colored output
- 修复 status bar duplicating background task indicator when PR status was shown
- Windows: 修复 bash command execution failing for users with `.bashrc` files
- Windows: 修复 console windows flashing when spawning child processes
- VSCode: 修复 OAuth token expiration causing 401 errors after extended sessions

---

## 📦 2.1.25（2026-01-29）

### 🐛 修复

- 修复 beta header validation error for gateway users on Bedrock and Vertex, ensuring `CLAUDE_CODE_DISABLE_EXPERIMENTAL_BETAS=1` avoids the error

---

## 📦 2.1.23（2026-01-29）

### ✨ 新功能

- 新增 customizable spinner verbs setting (`spinnerVerbs`)

### ⚡ 性能

- 改进 terminal rendering performance with optimized screen data layout

### 🔧 改进

- 变更 Bash commands to show timeout duration alongside elapsed time
- 变更 merged pull requests to show a purple status indicator in the prompt footer

### 🐛 修复

- 修复 mTLS and proxy connectivity for users behind corporate proxies or using client certificates
- 修复 per-user temp directory isolation to prevent permission conflicts on shared systems
- 修复 a race condition that could cause 400 errors when prompt caching scope was enabled
- 修复 pending async hooks not being cancelled when headless streaming sessions ended
- 修复 tab completion not updating the input field when accepting a suggestion
- 修复 ripgrep search timeouts silently returning empty results instead of reporting errors
- [IDE] 修复 model options displaying incorrect region strings for Bedrock users in headless mode

---

## 📦 2.1.22（2026-01-28）

### 🐛 修复

- 修复 structured outputs for non-interactive (-p) mode

---

## 📦 2.1.21（2026-01-28）

### ✨ 新功能

- 新增 support for full-width (zenkaku) number input from Japanese IME in option selection prompts
- [VSCode] 新增 automatic Python virtual environment activation, ensuring `python` and `pip` commands use the correct interpreter (configurable via `claudeCode.usePythonEnvironment` setting)

### 🔧 改进

- 改进 read/search progress indicators to show "Reading…" while in progress and "Read" when complete
- 改进 Claude to prefer file operation tools (Read, Edit, Write) over bash equivalents (cat, sed, awk)

### 🐛 修复

- 修复 shell completion cache files being truncated on exit
- 修复 API errors when resuming sessions that were interrupted during tool execution
- 修复 auto-compact triggering too early on models with large output token limits
- 修复 task IDs potentially being reused after deletion
- 修复 file search not working in VS Code extension on Windows
- [VSCode] 修复 message action buttons having incorrect background colors

---

## 📦 2.1.20（2026-01-27）

### ✨ 新功能

- 新增 arrow key history navigation in vim normal mode when cursor cannot move further
- 新增 external editor shortcut (Ctrl+G) to the help menu for better discoverability
- 新增 PR review status indicator to the prompt footer, showing the current branch's PR state (approved, changes requested, pending, or draft) as a colored dot with a clickable link
- 新增 support for loading `CLAUDE.md` files from additional directories specified via `--add-dir` flag (requires setting `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1`)
- 新增 ability to delete tasks via the `TaskUpdate` tool

### 🔧 改进

- 改进 `/sandbox` command UI to show dependency status with installation instructions when dependencies are missing
- 改进 thinking status text with a subtle shimmer animation
- 改进 task list to dynamically adjust visible items based on terminal height
- 改进 fork conversation hint to show how to resume the original session
- 变更 collapsed read/search groups to show present tense ("Reading", "Searching for") while in progress, and past tense ("Read", "Searched for") when complete
- 变更 `ToolSearch` results to appear as a brief notification instead of inline in the conversation
- 变更 the `/commit-push-pr` skill to automatically post PR URLs to Slack channels when configured via MCP tools
- 变更 the `/copy` command to be available to all users
- 变更 background agents to prompt for tool permissions before launching
- 变更 permission rules like `Bash(*)` to be accepted and treated as equivalent to `Bash`
- 变更 config backups to be timestamped and rotated (keeping 5 most recent) to prevent data loss

### 🐛 修复

- 修复 session compaction issues that could cause resume to load full history instead of the compact summary
- 修复 agents sometimes ignoring user messages sent while actively working on a task
- 修复 wide character (emoji, CJK) rendering artifacts where trailing columns were not cleared when replaced by narrower characters
- 修复 JSON parsing errors when MCP tool responses contain special Unicode characters
- 修复 up/down arrow keys in multi-line and wrapped text input to prioritize cursor movement over history navigation
- 修复 draft prompt being lost when pressing UP arrow to navigate command history
- 修复 ghost text flickering when typing slash commands mid-input
- 修复 marketplace source removal not properly deleting settings
- 修复 duplicate output in some commands like `/context`
- 修复 task list sometimes showing outside the main conversation view
- 修复 syntax highlighting for diffs occurring within multiline constructs like Python docstrings
- 修复 crashes when cancelling tool use

---

## 📦 2.1.19（2026-01-23）

### ✨ 新功能

- 新增 env var `CLAUDE_CODE_ENABLE_TASKS`, set to `false` to keep the old system temporarily
- 新增 shorthand `$0`, `$1`, etc. for accessing individual arguments in custom commands
- [SDK] 新增 replay of `queued_command` attachment messages as `SDKUserMessageReplay` events when `replayUserMessages` is enabled

### 🔧 改进

- 变更 skills without additional permissions or hooks to be allowed without requiring approval
- 变更 indexed argument syntax from `$ARGUMENTS.0` to `$ARGUMENTS[0]` (bracket syntax)
- [VSCode] 启用 session forking and rewind functionality for all users

### 🐛 修复

- 修复 crashes on processors without AVX instruction support
- 修复 dangling Claude Code processes when terminal is closed by catching EIO errors from `process.exit()` and using SIGKILL as fallback
- 修复 `/rename` and `/tag` not updating the correct session when resuming from a different directory (e.g., git worktrees)
- 修复 resuming sessions by custom title not working when run from a different directory
- 修复 pasted text content being lost when using prompt stash (Ctrl+S) and restore
- 修复 agent list displaying "Sonnet (default)" instead of "Inherit (default)" for agents without an explicit model setting
- 修复 backgrounded hook commands not returning early, potentially causing the session to wait on a process that was intentionally backgrounded
- 修复 file write preview omitting empty lines

---

## 📦 2.1.18

### ✨ 新功能

- 新增 customizable keyboard shortcuts. Configure keybindings per context, create chord sequences, and personalize your workflow. Run `/keybindings` to get started. Learn more at https://code.claude.com/docs/en/keybindings

---

## 📦 2.1.17（2026-01-22）

### 🐛 修复

- 修复 crashes on processors without AVX instruction support

---

## 📦 2.1.16（2026-01-22）

> 📝 **笔记定位**：[Agentic Loop](../how-it-works/index.md#-agentic-loopclaude-code-的核心循环) · [平台全景](../platforms/index.md#-平台全景图)

### ✨ 新功能

- 新增 new task management system, including new capabilities like dependency tracking
- [VSCode] 新增 native plugin management support
- [VSCode] 新增 ability for OAuth users to browse and resume remote Claude sessions from the Sessions dialog

### 🐛 修复

- 修复 out-of-memory crashes when resuming sessions with heavy subagent usage
- 修复 an issue where the "context remaining" warning was not hidden after running `/compact`
- 修复 session titles on the resume screen not respecting the user's language setting
- [IDE] 修复 a race condition on Windows where the Claude Code sidebar view container would not appear on start

---

## 📦 2.1.15（2026-01-21）

### ✨ 新功能

- 新增 deprecation notification for npm installations - run `claude install` or see https://docs.anthropic.com/en/docs/claude-code/getting-started for more options

### ⚡ 性能

- 改进 UI rendering performance with React Compiler

### 🐛 修复

- 修复 the "Context left until auto-compact" warning not disappearing after running `/compact`
- 修复 MCP stdio server timeout not killing child process, which could cause UI freezes

---

## 📦 2.1.14（2026-01-20）

### ✨ 新功能

- 新增 history-based autocomplete in bash mode (`!`) - type a partial command and press Tab to complete from your bash command history
- 新增 search to installed plugins list - type to filter by name or description
- 新增 support for pinning plugins to specific git commit SHAs, allowing marketplace entries to install exact versions
- [VSCode] 新增 `/usage` command to display current plan usage

### 🔧 改进

- 改进 backspace to delete pasted text as a single token instead of one character at a time

### 🐛 修复

- 修复 a regression where the context window blocking limit was calculated too aggressively, blocking users at ~65% context usage instead of the intended ~98%
- 修复 memory issues that could cause crashes when running parallel subagents
- 修复 memory leak in long-running sessions where stream resources were not cleaned up after shell commands completed
- 修复 `@` symbol incorrectly triggering file autocomplete suggestions in bash mode
- 修复 `@`-mention menu folder click behavior to navigate into directories instead of selecting them
- 修复 `/feedback` command generating invalid GitHub issue URLs when description is very long
- 修复 `/context` command to show the same token count and percentage as the status line in verbose mode
- 修复 an issue where `/config`, `/context`, `/model`, and `/todos` command overlays could close unexpectedly
- 修复 slash command autocomplete selecting wrong command when typing similar commands (e.g., `/context` vs `/compact`)
- 修复 inconsistent back navigation in plugin marketplace when only one marketplace is configured
- 修复 iTerm2 progress bar not clearing properly on exit, preventing lingering indicators and bell sounds

---

## 📦 2.1.12（2026-01-17）

### 🐛 修复

- 修复 message rendering bug

---

## 📦 2.1.11（2026-01-17）

### 🐛 修复

- 修复 excessive MCP connection requests for HTTP/SSE transports

---

## 📦 2.1.10

### ✨ 新功能

- 新增 new `Setup` hook event that can be triggered via `--init`, `--init-only`, or `--maintenance` CLI flags for repository setup and maintenance operations
- 新增 keyboard shortcut 'c' to copy OAuth URL when browser doesn't open automatically during login
- [VSCode] 新增 install count display to plugin listings
- [VSCode] 新增 trust warning when installing plugins

### 🔧 改进

- 改进 startup to capture keystrokes typed before the REPL is fully ready
- 改进 file suggestions to show as removable attachments instead of inserting text when accepted

### 🐛 修复

- 修复 a crash when running bash commands containing heredocs with JavaScript template literals like `${index + 1}`

---

## 📦 2.1.9（2026-01-16）

### ✨ 新功能

- 新增 `auto:N` syntax for configuring the MCP tool search auto-enable threshold, where N is the context window percentage (0-100)
- 新增 `plansDirectory` setting to customize where plan files are stored
- 新增 external editor support (Ctrl+G) in AskUserQuestion "Other" input field
- 新增 session URL attribution to commits and PRs created from web sessions
- 新增 support for `PreToolUse` hooks to return `additionalContext` to the model
- 新增 `${CLAUDE_SESSION_ID}` string substitution for skills to access the current session ID

### 🐛 修复

- 修复 long sessions with parallel tool calls failing with an API error about orphan tool_result blocks
- 修复 MCP server reconnection hanging when cached connection promise never resolves
- 修复 Ctrl+Z suspend not working in terminals using Kitty keyboard protocol (Ghostty, iTerm2, kitty, WezTerm)

---

## 📦 2.1.7（2026-01-14）

### ✨ 新功能

- 新增 `showTurnDuration` setting to hide turn duration messages (e.g., "Cooked for 1m 6s")
- 新增 ability to provide feedback when accepting permission prompts
- 新增 inline display of agent's final response in task notifications, making it easier to see results without reading the full transcript file
- 启用 MCP tool search auto mode by default for all users. When MCP tool descriptions exceed 10% of the context window, they are automatically deferred and discovered via the MCPSearch tool instead of being loaded upfront. This reduces context usage for users with many MCP tools configured. Users can disable this by adding `MCPSearch` to `disallowedTools` in their settings.

### 🔧 改进

- 改进 typing responsiveness by reducing memory allocation overhead in terminal rendering
- 变更 OAuth and API Console URLs from console.anthropic.com to platform.claude.com

### 🐛 修复

- 修复 security vulnerability where wildcard permission rules could match compound commands containing shell operators
- 修复 false "file modified" errors on Windows when cloud sync tools, antivirus scanners, or Git touch file timestamps without changing content
- 修复 orphaned tool_result errors when sibling tools fail during streaming execution
- 修复 context window blocking limit being calculated using the full context window instead of the effective context window (which reserves space for max output tokens)
- 修复 spinner briefly flashing when running local slash commands like `/model` or `/theme`
- 修复 terminal title animation jitter by using fixed-width braille characters
- 修复 plugins with git submodules not being fully initialized when installed
- 修复 bash commands failing on Windows when temp directory paths contained characters like `t` or `n` that were misinterpreted as escape sequences
- [VSCode] 修复 `claudeProcessWrapper` setting passing the wrapper path instead of the Claude binary path

---

## 📦 2.1.6（2026-01-13）

### ✨ 新功能

- 新增 search functionality to `/config` command for quickly filtering settings
- 新增 Updates section to `/doctor` showing auto-update channel and available npm versions (stable/latest)
- 新增 date range filtering to `/stats` command - press `r` to cycle between Last 7 days, Last 30 days, and All time
- 新增 automatic discovery of skills from nested `.claude/skills` directories when working with files in subdirectories
- 新增 `context_window.used_percentage` and `context_window.remaining_percentage` fields to status line input for easier context window display
- 新增 an error display when the editor fails during Ctrl+G

### 🔧 改进

- 改进 the external CLAUDE.md imports approval dialog to show which files are being imported and from where
- 改进 the `/tasks` dialog to go directly to task details when there's only one background task running
- 改进 @ autocomplete with icons for different suggestion types and single-line formatting
- 更新 "Help improve Claude" setting fetch to refresh OAuth and retry when it fails due to a stale OAuth token
- 变更 task notification display to cap at 3 lines with overflow summary when multiple background tasks complete simultaneously
- 变更 terminal title to "Claude Code" on startup for better window identification

### 🐛 修复

- 修复 permission bypass via shell line continuation that could allow blocked commands to execute
- 修复 false "File has been unexpectedly modified" errors when file watchers touch files without changing content
- 修复 text styling (bold, colors) getting progressively misaligned in multi-line responses
- 修复 the feedback panel closing unexpectedly when typing 'n' in the description field
- 修复 rate limit warning appearing at low usage after weekly reset (now requires 70% usage)
- 修复 rate limit options menu incorrectly auto-opening when resuming a previous session
- 修复 numpad keys outputting escape sequences instead of characters in Kitty keyboard protocol terminals
- 修复 Option+Return not inserting newlines in Kitty keyboard protocol terminals
- 修复 corrupted config backup files accumulating in the home directory (now only one backup is created per config file)
- 修复 `mcp list` and `mcp get` commands leaving orphaned MCP server processes
- 修复 visual artifacts in ink2 mode when nodes become hidden via `display:none`
- [VSCode] 修复 usage indicator not updating after manual compact

### 🗑️ 移除

- 移除 ability to @-mention MCP servers to enable/disable - use `/mcp enable <name>` instead

---

## 📦 2.1.5（2026-01-12）

### ✨ 新功能

- 新增 `CLAUDE_CODE_TMPDIR` environment variable to override the temp directory used for internal temp files, useful for environments with custom temp directory requirements

---

## 📦 2.1.4（2026-01-11）

### ✨ 新功能

- 新增 `CLAUDE_CODE_DISABLE_BACKGROUND_TASKS` environment variable to disable all background task functionality including auto-backgrounding and the Ctrl+B shortcut

### 🐛 修复

- 修复 "Help improve Claude" setting fetch to refresh OAuth and retry when it fails due to a stale OAuth token

---

## 📦 2.1.3（2026-01-09）

### ✨ 新功能

- 新增 release channel (`stable` or `latest`) toggle to `/config`
- 新增 detection and warnings for unreachable permission rules, with warnings in `/doctor` and after saving rules that include the source of each rule and actionable fix guidance
- [VSCode] 新增 clickable destination selector for permission requests, allowing you to choose where settings are saved (this project, all projects, shared with team, or session only)

### 🔧 改进

- 合并 slash commands and skills, simplifying the mental model with no change in behavior
- 改进 terminal rendering stability by preventing uncontrolled writes from corrupting cursor state
- 改进 slash command suggestion readability by truncating long descriptions to 2 lines
- 变更 tool hook execution timeout from 60 seconds to 10 minutes

### 🐛 修复

- 修复 plan files persisting across `/clear` commands, now ensuring a fresh plan file is used after clearing a conversation
- 修复 false skill duplicate detection on filesystems with large inodes (e.g., ExFAT) by using 64-bit precision for inode values
- 修复 mismatch between background task count in status bar and items shown in tasks dialog
- 修复 sub-agents using the wrong model during conversation compaction
- 修复 web search in sub-agents using incorrect model
- 修复 trust dialog acceptance when running from the home directory not enabling trust-requiring features like hooks during the session

---

## 📦 2.1.2（2026-01-09）

### ✨ 新功能

- 新增 source path metadata to images dragged onto the terminal, helping Claude understand where images originated
- 新增 clickable hyperlinks for file paths in tool output in terminals that support OSC 8 (like iTerm)
- 新增 support for Windows Package Manager (winget) installations with automatic detection and update instructions
- 新增 Shift+Tab keyboard shortcut in plan mode to quickly select "auto-accept edits" option
- 新增 `FORCE_AUTOUPDATE_PLUGINS` environment variable to allow plugin autoupdate even when the main auto-updater is disabled
- 新增 `agent_type` to SessionStart hook input, populated if `--agent` is specified

### 🔧 改进

- 改进 Option-as-Meta hint on macOS to show terminal-specific instructions for native CSIu terminals like iTerm2, Kitty, and WezTerm
- 改进 error message when pasting images over SSH to suggest using `scp` instead of the unhelpful clipboard shortcut hint
- 改进 permission explainer to not flag routine dev workflows (git fetch/rebase, npm install, tests, PRs) as medium risk
- 变更 large bash command outputs to be saved to disk instead of truncated, allowing Claude to read the full content
- 变更 large tool outputs to be persisted to disk instead of truncated, providing full output access via file references
- 变更 `/plugins` installed tab to unify plugins and MCPs with scope-based grouping
- [SDK] 变更 minimum zod peer dependency to ^4.0.0

### 🐛 修复

- 修复 a command injection vulnerability in bash command processing where malformed input could execute arbitrary commands
- 修复 a memory leak where tree-sitter parse trees were not being freed, causing WASM memory to grow unbounded over long sessions
- 修复 binary files (images, PDFs, etc.) being accidentally included in memory when using `@include` directives in CLAUDE.md files
- 修复 updates incorrectly claiming another installation is in progress
- 修复 crash when socket files exist in watched directories (defense-in-depth for EOPNOTSUPP errors)
- 修复 remote session URL and teleport being broken when using `/tasks` command
- 修复 MCP tool names being exposed in analytics events by sanitizing user-specific server configurations
- [VSCode] 修复 usage display not updating after manual compact

### 🗑️ 移除

- 弃用 Windows managed settings path `C:\ProgramData\ClaudeCode\managed-settings.json` - administrators should migrate to `C:\Program Files\ClaudeCode\managed-settings.json`

---

## 📦 2.1.0

> 📝 **笔记定位**：[Skill 类型](../skills/index.md#-skill-的两种类型) · [配置层级](../configuration/index.md#-配置文件有哪几层) · [Sub-agent](../sub-agents/index.md#sub-agent-工作原理) · [MCP 工具发现](../mcp/index.md#-mcp-工具的发现与调用) · [平台全景](../platforms/index.md#-平台全景图) · [Hook 触发](../hooks/index.md#-hook-能在哪些时机触发) · [Channels](../automation/index.md#-channels-消息通道)

### ✨ 新功能

- 新增 automatic skill hot-reload - skills created or modified in `~/.claude/skills` or `.claude/skills` are now immediately available without restarting the session
- 新增 support for running skills and slash commands in a forked sub-agent context using `context: fork` in skill frontmatter
- 新增 support for `agent` field in skills to specify agent type for execution
- 新增 `language` setting to configure Claude's response language (e.g., language: "japanese")
- 新增 `respectGitignore` support in `settings.json` for per-project control over @-mention file picker behavior
- 新增 `IS_DEMO` environment variable to hide email and organization from the UI, useful for streaming or recording sessions
- 新增 wildcard pattern matching for Bash tool permissions using `*` at any position in rules (e.g., `Bash(npm *)`, `Bash(* install)`, `Bash(git * main)`)
- 新增 unified Ctrl+B backgrounding for both bash commands and agents - pressing Ctrl+B now backgrounds all running foreground tasks simultaneously
- 新增 support for MCP `list_changed` notifications, allowing MCP servers to dynamically update their available tools, prompts, and resources without requiring reconnection
- 新增 `/teleport` and `/remote-env` slash commands for claude.ai subscribers, allowing them to resume and configure remote sessions
- 新增 support for disabling specific agents using `Task(AgentName)` syntax in settings.json permissions or the `--disallowedTools` CLI flag
- 新增 hooks support to agent frontmatter, allowing agents to define PreToolUse, PostToolUse, and Stop hooks scoped to the agent's lifecycle
- 新增 hooks support for skill and slash command frontmatter
- 新增 new Vim motions: `;` and `,` to repeat f/F/t/T motions, `y` operator for yank with `yy`/`Y`, `p`/`P` for paste, text objects (`iw`, `aw`, `iW`, `aW`, `i"`, `a"`, `i'`, `a'`, `i(`, `a(`, `i[`, `a[`, `i{`, `a{`), `>>` and `<<` for indent/dedent, and `J` to join lines
- 新增 `/plan` command shortcut to enable plan mode directly from the prompt
- 新增 slash command autocomplete support when `/` appears anywhere in input, not just at the beginning
- 新增 `--tools` flag support in interactive mode to restrict which built-in tools Claude can use during interactive sessions
- 新增 `CLAUDE_CODE_FILE_READ_MAX_OUTPUT_TOKENS` environment variable to override the default file read token limit
- 新增 support for `once: true` config for hooks
- 新增 support for YAML-style lists in frontmatter `allowed-tools` field for cleaner skill declarations
- 新增 support for prompt and agent hook types from plugins (previously only command hooks were supported)
- 新增 Cmd+V support for image paste in iTerm2 (maps to Ctrl+V)
- 新增 left/right arrow key navigation for cycling through tabs in dialogs
- 新增 real-time thinking block display in Ctrl+O transcript mode
- 新增 filepath to full output in background bash task details dialog
- 新增 Skills as a separate category in the context visualization
- [VSCode] 新增 currently selected model name to the context menu
- [VSCode] 新增 descriptive labels on auto-accept permission button (e.g., "Yes, allow npm for this project" instead of "Yes, and don't ask again")

### ⚡ 性能

- Multiple optimizations to improve startup performance
- 改进 terminal rendering performance when using native installer or Bun, especially for text with emoji, ANSI codes, and Unicode characters
- 改进 performance when reading Jupyter notebooks with many cells

### 🔧 改进

- 变更 Shift+Enter to work out of the box in iTerm2, WezTerm, Ghostty, and Kitty without modifying terminal configs
- 减少 permission prompts for complex bash commands
- 改进 CLI help output to display options and subcommands in alphabetical order for easier navigation
- 改进 reliability for piped input like `cat refactor.md | claude`
- 改进 reliability for AskQuestion tool
- 改进 sed in-place edit commands to render as file edits with diff preview
- 改进 Claude to automatically continue when response is cut off due to output token limit, instead of showing an error message
- 改进 compaction reliability
- 改进 subagents (Task tool) to continue working after permission denial, allowing them to try alternative approaches
- 改进 skills to show progress while executing, displaying tool uses as they happen
- 改进 skills from `/skills/` directories to be visible in the slash command menu by default (opt-out with `user-invocable: false` in frontmatter)
- 改进 skill suggestions to prioritize recently and frequently used skills
- 改进 spinner feedback when waiting for the first response token
- 改进 token count display in spinner to include tokens from background agents
- 改进 incremental output for async agents to give the main thread more control and visibility
- 改进 permission prompt UX with Tab hint moved to footer, cleaner Yes/No input labels with contextual placeholders
- 改进 the Claude in Chrome notification with shortened help text and persistent display until dismissed
- 改进 macOS screenshot paste reliability with TIFF format support
- 改进 `/stats` output
- 更新 Atlassian MCP integration to use a more reliable default configuration (streamable HTTP)
- 变更 "Interrupted" message color from red to grey for a less alarming appearance
- [SDK] 变更 minimum zod peer dependency to ^4.0.0

### 🐛 修复

- 修复 security issue where sensitive data (OAuth tokens, API keys, passwords) could be exposed in debug logs
- 修复 files and skills not being properly discovered when resuming sessions with `-c` or `--resume`
- 修复 pasted content being lost when replaying prompts from history using up arrow or Ctrl+R search
- 修复 Esc key with queued prompts to only move them to input without canceling the running task
- 修复 command search to prioritize exact and prefix matches on command names over fuzzy matches in descriptions
- 修复 PreToolUse hooks to allow `updatedInput` when returning `ask` permission decision, enabling hooks to act as middleware while still requesting user consent
- 修复 plugin path resolution for file-based marketplace sources
- 修复 LSP tool being incorrectly enabled when no LSP servers were configured
- 修复 background tasks failing with "git repository not found" error for repositories with dots in their names
- 修复 Claude in Chrome support for WSL environments
- 修复 Windows native installer silently failing when executable creation fails
- 修复 OAuth token refresh not triggering when server reports token expired but local expiration check disagrees
- 修复 session persistence getting stuck after transient server errors by recovering from 409 conflicts when the entry was actually stored
- 修复 session resume failures caused by orphaned tool results during concurrent tool execution
- 修复 a race condition where stale OAuth tokens could be read from the keychain cache during concurrent token refresh attempts
- 修复 AWS Bedrock subagents not inheriting EU/APAC cross-region inference model configuration, causing 403 errors when IAM permissions are scoped to specific regions
- 修复 API context overflow when background tasks produce large output by truncating to 30K chars with file path reference
- 修复 a hang when reading FIFO files by skipping symlink resolution for special file types
- 修复 terminal keyboard mode not being reset on exit in Ghostty, iTerm2, Kitty, and WezTerm
- 修复 Alt+B and Alt+F (word navigation) not working in iTerm2, Ghostty, Kitty, and WezTerm
- 修复 `${CLAUDE_PLUGIN_ROOT}` not being substituted in plugin `allowed-tools` frontmatter, which caused tools to incorrectly require approval
- 修复 files created by the Write tool using hardcoded 0o600 permissions instead of respecting the system umask
- 修复 commands with `$()` command substitution failing with parse errors
- 修复 multi-line bash commands with backslash continuations being incorrectly split and flagged for permissions
- 修复 bash command prefix extraction to correctly identify subcommands after global options (e.g., `git -C /path log` now correctly matches `Bash(git log:*)` rules)
- 修复 slash commands passed as CLI arguments (e.g., `claude /context`) not being executed properly
- 修复 pressing Enter after Tab-completing a slash command selecting a different command instead of submitting the completed one
- 修复 slash command argument hint flickering and inconsistent display when typing commands with arguments
- 修复 Claude sometimes redundantly invoking the Skill tool when running slash commands directly
- 修复 skill token estimates in `/context` to accurately reflect frontmatter-only loading
- 修复 subagents sometimes not inheriting the parent's model by default
- 修复 model picker showing incorrect selection for Bedrock/Vertex users using `--model haiku`
- 修复 duplicate Bash commands appearing in permission request option labels
- 修复 noisy output when background tasks complete - now shows clean completion message instead of raw output
- 修复 background task completion notifications to appear proactively with bullet point
- 修复 forked slash commands showing "AbortError" instead of "Interrupted" message when cancelled
- 修复 cursor disappearing after dismissing permission dialogs
- 修复 `/hooks` menu selecting wrong hook type when scrolling to a different option
- 修复 images in queued prompts showing as "[object Object]" when pressing Esc to cancel
- 修复 images being silently dropped when queueing messages while backgrounding a task
- 修复 large pasted images failing with "Image was too large" error
- 修复 extra blank lines in multiline prompts containing CJK characters (Japanese, Chinese, Korean)
- 修复 ultrathink keyword highlighting being applied to wrong characters when user prompt text wraps to multiple lines
- 修复 collapsed "Reading X files…" indicator incorrectly switching to past tense when thinking blocks appear mid-stream
- 修复 Bash read commands (like `ls` and `cat`) not being counted in collapsed read/search groups, causing groups to incorrectly show "Read 0 files"
- 修复 spinner token counter to properly accumulate tokens from subagents during execution
- 修复 memory leak in git diff parsing where sliced strings retained large parent strings
- 修复 race condition where LSP tool could return "no server available" during startup
- 修复 feedback submission hanging indefinitely when network requests timeout
- 修复 search mode in plugin discovery and log selector views exiting when pressing up arrow
- 修复 hook success message showing trailing colon when hook has no output
- [VSCode] 修复 paragraph breaks not rendering in markdown content
- [VSCode] 修复 scrolling in the extension inadvertently scrolling the parent iframe
- [Windows] 修复 issue with improper rendering

### 🗑️ 移除

- 移除 permission prompt when entering plan mode - users can now enter plan mode without approval
- 移除 underline styling from image reference links

---

## 📦 2.0.76（2026-01-07）

### 🐛 修复

- 修复 issue with macOS code-sign warning when using Claude in Chrome integration

---

## 📦 2.0.75

### 🐛 修复

- Minor bugfixes

---

## 📦 2.0.74（2025-12-19）

### ✨ 新功能

- 新增 LSP (Language Server Protocol) tool for code intelligence features like go-to-definition, find references, and hover documentation
- 新增 `/terminal-setup` support for Kitty, Alacritty, Zed, and Warp terminals
- 新增 ctrl+t shortcut in `/theme` to toggle syntax highlighting on/off
- 新增 syntax highlighting info to theme picker
- 新增 guidance for macOS users when Alt shortcuts fail due to terminal configuration
- [VSCode] 新增 gift tag pictogram for year-end promotion message

### 🔧 改进

- 改进 `/context` command visualization with grouped skills and agents by source, slash commands, and sorted token count

### 🐛 修复

- 修复 skill `allowed-tools` not being applied to tools invoked by the skill
- 修复 Opus 4.5 tip incorrectly showing when user was already using Opus
- 修复 a potential crash when syntax highlighting isn't initialized correctly
- 修复 visual bug in `/plugins discover` where list selection indicator showed while search box was focused
- 修复 macOS keyboard shortcuts to display 'opt' instead of 'alt'
- [Windows] 修复 issue with improper rendering

---

## 📦 2.0.73（2025-12-19）

### ✨ 新功能

- 新增 clickable `[Image #N]` links that open attached images in the default viewer
- 新增 alt-y yank-pop to cycle through kill ring history after ctrl-y yank
- 新增 search filtering to the plugin discover screen (type to filter by name, description, or marketplace)
- 新增 support for custom session IDs when forking sessions with `--session-id` combined with `--resume` or `--continue` and `--fork-session`
- [VSCode] 新增 tab icon badges showing pending permissions (blue) and unread completions (orange)

### 🔧 改进

- 改进 `/theme` command to open theme picker directly
- 改进 theme picker UI
- 改进 search UX across resume session, permissions, and plugins screens with a unified SearchBox component

### 🐛 修复

- 修复 slow input history cycling and race condition that could overwrite text after message submission

---

## 📦 2.0.72

> 📝 **笔记定位**：[Chrome 扩展](../platforms/index.md#-chrome-扩展)

### ✨ 新功能

- 新增 Claude in Chrome (Beta) feature that works with the Chrome extension (https://claude.ai/chrome) to let you control your browser directly from Claude Code
- 新增 scannable QR code to mobile app tip for quick app downloads
- 新增 loading indicator when resuming conversations for better feedback

### ⚡ 性能

- 减少 terminal flickering
- 改进 @ mention file suggestion speed (~3× faster in git repositories)
- 改进 file suggestion performance in repos with `.ignore` or `.rgignore` files

### 🔧 改进

- 改进 settings validation errors to be more prominent
- 变更 thinking toggle from Tab to Alt+T to avoid accidental triggers

### 🐛 修复

- 修复 `/context` command not respecting custom system prompts in non-interactive mode
- 修复 order of consecutive Ctrl+K lines when pasting with Ctrl+Y

---

## 📦 2.0.71

### ✨ 新功能

- 新增 /config toggle to enable/disable prompt suggestions
- 新增 `/settings` as an alias for the `/config` command
- 新的 syntax highlighting engine for native build

### 🔧 改进

- Bedrock: Environment variable `ANTHROPIC_BEDROCK_BASE_URL` is now respected for token counting and inference profile listing

### 🐛 修复

- 修复 @ file reference suggestions incorrectly triggering when cursor is in the middle of a path
- 修复 MCP servers from `.mcp.json` not loading when using `--dangerously-skip-permissions`
- 修复 permission rules incorrectly rejecting valid bash commands containing shell glob patterns (e.g., `ls *.txt`, `for f in *.png`)

---

## 📦 2.0.70

### ✨ 新功能

- 新增 Enter key to accept and submit prompt suggestions immediately (tab still accepts for editing)
- 新增 wildcard syntax `mcp__server__*` for MCP tool permissions to allow or deny all tools from a server
- 新增 auto-update toggle for plugin marketplaces, allowing per-marketplace control over automatic updates
- 新增 `current_usage` field to status line input, enabling accurate context window percentage calculations

### ⚡ 性能

- 改进 memory usage by 3x for large conversations

### 🔧 改进

- 改进 resolution of stats screenshots copied to clipboard (Ctrl+S) for crisper images
- 改进 UI for file creation permission dialog

### 🐛 修复

- 修复 input being cleared when processing queued commands while the user was typing
- 修复 prompt suggestions replacing typed input when pressing Tab
- 修复 diff view not updating when terminal is resized
- 修复 thinking mode toggle in /config not persisting correctly

### 🗑️ 移除

- 移除 # shortcut for quick memory entry (tell Claude to edit your CLAUDE.md instead)

---

## 📦 2.0.69

### 🐛 修复

- Minor bugfixes

---

## 📦 2.0.68

### ✨ 新功能

- 新增 support for enterprise managed settings. Contact your Anthropic account team to enable this feature.

### 🔧 改进

- 改进 plan mode exit UX: show simplified yes/no dialog when exiting with empty or missing plan instead of throwing an error

### 🐛 修复

- 修复 IME (Input Method Editor) support for languages like Chinese, Japanese, and Korean by correctly positioning the composition window at the cursor
- 修复 a bug where disallowed MCP tools were visible to the model
- 修复 an issue where steering messages could be lost while a subagent is working
- 修复 Option+Arrow word navigation treating entire CJK (Chinese, Japanese, Korean) text sequences as a single word instead of navigating by word boundaries

---

## 📦 2.0.67

### ✨ 新功能

- 新增 search functionality to `/permissions` command with `/` keyboard shortcut for filtering rules by tool name

### 🔧 改进

- Thinking mode is now enabled by default for Opus 4.5
- Thinking mode configuration has moved to /config
- Show reason why autoupdater is disabled in `/doctor`

### 🐛 修复

- 修复 false "Another process is currently updating Claude" error when running `claude update` while another instance is already on the latest version
- 修复 MCP servers from `.mcp.json` being stuck in pending state when running in non-interactive mode (`-p` flag or piped input)
- 修复 scroll position resetting after deleting a permission rule in `/permissions`
- 修复 word deletion (opt+delete) and word navigation (opt+arrow) not working correctly with non-Latin text such as Cyrillic, Greek, Arabic, Hebrew, Thai, and Chinese
- 修复 `claude install --force` not bypassing stale lock files
- 修复 consecutive @~/ file references in CLAUDE.md being incorrectly parsed due to markdown strikethrough interference
- Windows: 修复 plugin MCP servers failing due to colons in log directory paths

---

## 📦 2.0.65

### ✨ 新功能

- 新增 ability to switch models while writing a prompt using alt+p (linux, windows), option+p (macos).
- 新增 context window information to status line input
- 新增 `fileSuggestion` setting for custom `@` file search commands
- 新增 `CLAUDE_CODE_SHELL` environment variable to override automatic shell detection (useful when login shell differs from actual working shell)

### 🐛 修复

- 修复 prompt not being saved to history when aborting a query with Escape
- 修复 Read tool image handling to identify format from bytes instead of file extension

---

## 📦 2.0.64

> 📝 **笔记定位**：[安装](../getting-started/index.md#-怎么安装-claude-code) · [上下文分层](../context-engineering/index.md#-上下文分层策略) · [Sub-agent](../sub-agents/index.md#sub-agent-工作原理)

### ✨ 新功能

- 新增 named session support: use `/rename` to name sessions, `/resume <name>` in REPL or `claude --resume <name>` from the terminal to resume them
- 新增 support for .claude/rules/`.  See https://code.claude.com/docs/en/memory for details.
- 新增 image dimension metadata when images are resized, enabling accurate coordinate mappings for large images
- Bedrock: 新增 support for `aws login` AWS Management Console credentials

### 🔧 改进

- Made auto-compacting instant
- Agents and bash commands can run asynchronously and send messages to wake up the main agent
- /stats now provides users with interesting CC stats, such as favorite model, usage graph, usage streak
- 改进 `/resume` screen with grouped forked sessions and keyboard shortcuts for preview (P) and rename (R)
- VSCode: 新增 copy-to-clipboard button on code blocks and bash tool inputs
- Bedrock: 改进 efficiency of token counting
- Unshipped AgentOutputTool and BashOutputTool, in favor of a new unified TaskOutputTool

### 🐛 修复

- 修复 auto-loading .env when using native installer
- 修复 `--system-prompt` being ignored when using `--continue` or `--resume` flags
- VSCode: 修复 extension not working on Windows ARM64 by falling back to x64 binary via emulation

---

## 📦 2.0.62

### ✨ 新功能

- 新增 "(Recommended)" indicator for multiple-choice questions, with the recommended option moved to the top of the list
- 新增 `attribution` setting to customize commit and PR bylines (deprecates `includeCoAuthoredBy`)

### 🐛 修复

- 修复 duplicate slash commands appearing when ~/.claude is symlinked to a project directory
- 修复 slash command selection not working when multiple commands share the same name
- 修复 an issue where skill files inside symlinked skill directories could become circular symlinks
- 修复 running versions getting removed because lock file incorrectly going stale
- 修复 IDE diff tab not closing when rejecting file changes

---

## 📦 2.0.61

### ✨ 新功能

- Reverted VSCode support for multiple terminal clients due to responsiveness issues.

---

## 📦 2.0.60

> 📝 **笔记定位**：[Sub-agent](../sub-agents/index.md#sub-agent-工作原理) · [MCP 工具发现](../mcp/index.md#-mcp-工具的发现与调用)

### ✨ 新功能

- 新增 background agent support. Agents run in the background while you work
- 新增 --disable-slash-commands CLI flag to disable all slash commands
- 新增 model name to "Co-Authored-By" commit messages
- 启用 "/mcp enable [server-name]" or "/mcp disable [server-name]" to quickly toggle all servers
- VSCode: 新增 support for multiple terminal clients connecting to the IDE server simultaneously

### 🔧 改进

- 更新 Fetch to skip summarization for pre-approved websites

---

## 📦 2.0.59

### ✨ 新功能

- 新增 --agent CLI flag to override the agent setting for the current session
- 新增 `agent` setting to configure main thread with a specific agent's system prompt, tool restrictions, and model

### 🐛 修复

- VS Code: 修复 .claude.json config file being read from incorrect location

---

## 📦 2.0.58

### ✨ 新功能

- Windows: Managed settings now prefer `C:\Program Files\ClaudeCode` if it exists. Support for `C:\ProgramData\ClaudeCode` will be removed in a future version.

### 🔧 改进

- Pro users now have access to Opus 4.5 as part of their subscription!

### 🐛 修复

- 修复 timer duration showing "11m 60s" instead of "12m 0s"

---

## 📦 2.0.57

### ✨ 新功能

- 新增 feedback input when rejecting plans, allowing users to tell Claude what to change
- VSCode: 新增 streaming message support for real-time response display

---

## 📦 2.0.56

### ✨ 新功能

- 新增 setting to enable/disable terminal progress bar (OSC 9;4)
- VSCode Extension: Added support for VS Code's secondary sidebar (VS Code 1.97+), allowing Claude Code to be displayed in the right sidebar while keeping the file explorer on the left. Requires setting sidebar as Preferred Location in the config.

---

## 📦 2.0.55

### ⚡ 性能

- 改进 fuzzy matching for `@` file suggestions with faster, more accurate results

### 🔧 改进

- 改进 AskUserQuestion tool to auto-submit single-select questions on the last question, eliminating the extra review screen for simple question flows

### 🐛 修复

- 修复 proxy DNS resolution being forced on by default. Now opt-in via `CLAUDE_CODE_PROXY_RESOLVES_HOSTS=true` environment variable
- 修复 keyboard navigation becoming unresponsive when holding down arrow keys in memory location selector

---

## 📦 2.0.54

### 🔧 改进

- Hooks: 启用 PermissionRequest hooks to process 'always allow' suggestions and apply permission updates

### 🐛 修复

- 修复 issue with excessive iTerm notifications

---

## 📦 2.0.52

### 🔧 改进

- 允许 some uses of `$!` in bash commands

### 🐛 修复

- 修复 duplicate message display when starting Claude with a command line argument
- 修复 `/usage` command progress bars to fill up as usage increases (instead of showing remaining percentage)
- 修复 image pasting not working on Linux systems running Wayland (now falls back to wl-paste when xclip is unavailable)

---

## 📦 2.0.51

> 📝 **笔记定位**：[第三方 LLM](../integrations/index.md#-第三方-llm-提供商) · [桌面应用](../platforms/index.md#-桌面应用)

### ✨ 新功能

- 新增 Opus 4.5! https://www.anthropic.com/news/claude-opus-4-5
- 引入 Claude Code for Desktop: https://claude.com/download
- Pro users can now purchase extra usage for access to Opus 4.5 in Claude Code

### 🔧 改进

- To give you room to try out our new model, we've updated usage limits for Claude Code users. See the Claude Opus 4.5 blog for full details
- Plan Mode now builds more precise plans and executes more thoroughly
- Usage limit notifications now easier to understand
- Switched `/usage` back to "% used"

### 🐛 修复

- 修复 handling of thinking errors
- 修复 performance regression

---

## 📦 2.0.50

### 🔧 改进

- 静默 a noisy but harmless error during upgrades
- 改进 ultrathink text display
- 改进 clarity of 5-hour session limit warning message

### 🐛 修复

- 修复 bug preventing calling MCP tools that have nested references in their input schemas

---

## 📦 2.0.49

### ✨ 新功能

- 新增 readline-style ctrl-y for pasting deleted text

### 🔧 改进

- 改进 clarity of usage limit warning message

### 🐛 修复

- 修复 handling of subagent permissions

---

## 📦 2.0.47

### 🔧 改进

- 改进 error messages and validation for `claude --teleport`
- 改进 error handling in `/usage`

### 🐛 修复

- 修复 race condition with history entry not getting logged at exit
- 修复 Vertex AI configuration not being applied from `settings.json`

---

## 📦 2.0.46

### 🐛 修复

- 修复 image files being reported with incorrect media type when format cannot be detected from metadata

---

## 📦 2.0.45

> 📝 **笔记定位**：[第三方 LLM](../integrations/index.md#-第三方-llm-提供商) · [Hook 触发](../hooks/index.md#-hook-能在哪些时机触发)

### ✨ 新功能

- 新增 support for Microsoft Foundry! See https://code.claude.com/docs/en/azure-ai-foundry
- 新增 `PermissionRequest` hook to automatically approve or deny tool permission requests with custom logic

### 🔧 改进

- Send background tasks to Claude Code on the web by starting a message with `&`

---

## 📦 2.0.43

### ✨ 新功能

- 新增 `permissionMode` field for custom agents
- 新增 `tool_use_id` field to `PreToolUseHookInput` and `PostToolUseHookInput` types
- 新增 skills frontmatter field to declare skills to auto-load for subagents
- 新增 the `SubagentStart` hook event

### 🐛 修复

- 修复 nested `CLAUDE.md` files not loading when @-mentioning files
- 修复 duplicate rendering of some messages in the UI
- 修复 some visual flickers
- 修复 NotebookEdit tool inserting cells at incorrect positions when cell IDs matched the pattern `cell-N`

---

## 📦 2.0.42

### ✨ 新功能

- 新增 `agent_id` and `agent_transcript_path` fields to `SubagentStop` hooks.

---

## 📦 2.0.41

### ✨ 新功能

- 新增 `model` parameter to prompt-based stop hooks, allowing users to specify a custom model for hook evaluation
- SDK: Support custom timeouts for hooks
- Plugins: Added support for sharing and installing output styles

### 🔧 改进

- ctrl-r history search landing on a slash command no longer cancels the search
- Allow more safe git commands to run without approval
- Teleporting a session from web will automatically set the upstream branch

### 🐛 修复

- 修复 slash commands from user settings being loaded twice, which could cause rendering issues
- 修复 incorrect labeling of user settings vs project settings in command descriptions
- 修复 crash when plugin command hooks timeout during execution
- Fixed: Bedrock users no longer see duplicate Opus entries in the /model picker when using `--model haiku`
- 修复 broken security documentation links in trust dialogs and onboarding
- 修复 issue where pressing ESC to close the diff modal would also interrupt the model

---

## 📦 2.0.37

### 🔧 改进

- Hooks: 新增 matcher values for Notification hook events
- Output Styles: Added `keep-coding-instructions` option to frontmatter

### 🐛 修复

- 修复 how idleness is computed for notifications

---

## 📦 2.0.36

### 🐛 修复

- Fixed: DISABLE_AUTOUPDATER environment variable now properly disables package manager update notifications
- 修复 queued messages being incorrectly executed as bash commands
- 修复 input being lost when typing while a queued message is processed

---

## 📦 2.0.35

### ✨ 新功能

- 新增 `CLAUDE_CODE_EXIT_AFTER_STOP_DELAY` environment variable to automatically exit SDK mode after a specified idle duration, useful for automated workflows and scripts

### 🔧 改进

- 改进 fuzzy search results when searching commands
- 改进 VS Code extension to respect `chat.fontSize` and `chat.fontFamily` settings throughout the entire UI, and apply font changes immediately without requiring reload
- 迁移 `ignorePatterns` from project config to deny permissions in the localSettings.

### 🐛 修复

- 修复 menu navigation getting stuck on items with empty string or other falsy values (e.g., in the `/hooks` menu)

---

## 📦 2.0.34

### ⚡ 性能

- 改进 file path suggestion performance with native Rust-based fuzzy finder

### 🔧 改进

- VSCode Extension: Added setting to configure the initial permission mode for new conversations

### 🐛 修复

- 修复 infinite token refresh loop that caused MCP servers with OAuth (e.g., Slack) to hang during connection
- 修复 memory crash when reading or writing large files (especially base64-encoded images)

---

## 📦 2.0.33

### 🔧 改进

- Native binary installs now launch quicker.

### 🐛 修复

- 修复 `claude doctor` incorrectly detecting Homebrew vs npm-global installations by properly resolving symlinks
- 修复 `claude mcp serve` exposing tools with incompatible outputSchemas

---

## 📦 2.0.32

### ✨ 新功能

- 新增 `companyAnnouncements` setting for displaying announcements on startup

### 🔧 改进

- Un-deprecate output styles based on community feedback

### 🐛 修复

- 修复 hook progress messages not updating correctly during PostToolUse hook execution

---

## 📦 2.0.31

### ✨ 新功能

- Vertex: add support for Web Search on supported models

### 🔧 改进

- Windows: native installation uses shift+tab as shortcut for mode switching, instead of alt+m
- VSCode: Adding the respectGitIgnore configuration to include .gitignored files in file searches (defaults to true)

### 🐛 修复

- 修复 a bug with subagents and MCP servers related to "Tool names must be unique" error
- 修复 issue causing `/compact` to fail with `prompt_too_long` by making it respect existing compact boundaries
- 修复 plugin uninstall not removing plugins

---

## 📦 2.0.30

### ✨ 新功能

- 新增 helpful hint to run `security unlock-keychain` when encountering API key errors on macOS with locked keychain
- 新增 `allowUnsandboxedCommands` sandbox setting to disable the dangerouslyDisableSandbox escape hatch at policy level
- 新增 `disallowedTools` field to custom agent definitions for explicit tool blocking
- 新增 prompt-based stop hooks
- 启用 SSE MCP servers on native build

### 🔧 改进

- VSCode: 新增 respectGitIgnore configuration to include .gitignored files in file searches (defaults to true)
- VSCode: 恢复 selection indicator in input footer showing current file or code selection status

### 🐛 修复

- 修复 Explore agent creating unwanted .md investigation files during codebase exploration
- 修复 a bug where `/context` would sometimes fail with "max_tokens must be greater than thinking.budget_tokens" error message
- 修复 `--mcp-config` flag to correctly override file-based MCP configurations
- 修复 bug that saved session permissions to local settings
- 修复 MCP tools not being available to sub-agents
- 修复 hooks and plugins not executing when using --dangerously-skip-permissions flag
- 修复 delay when navigating through typeahead suggestions with arrow keys

### 🗑️ 移除

- 弃用 output styles. Review options in `/output-style` and use --system-prompt-file, --system-prompt, --append-system-prompt, CLAUDE.md, or plugins instead
- 移除 support for custom ripgrep configuration, resolving an issue where Search returns no results and config discovery fails

---

## 📦 2.0.28

### ✨ 新功能

- Subagents: claude can now choose to resume subagents
- SDK: added --max-budget-usd flag
- 新增 branch and tag support for git-based plugins and marketplaces using fragment syntax (e.g., `owner/repo#branch`)

### 🔧 改进

- Plan mode: introduced new Plan subagent
- Subagents: claude can dynamically choose the model used by its subagents
- Discovery of custom slash commands, subagents, and output styles no longer respects .gitignore
- Stop `/terminal-setup` from adding backslash to `Shift + Enter` in VS Code

### 🐛 修复

- 修复 a bug where macOS permission prompts would show up upon initial launch when launching from home directory
- Various other bug fixes

---

## 📦 2.0.27

### ✨ 新功能

- 新的 UI for permission prompts
- 新增 current branch filtering and search to session resume screen for easier navigation

### 🔧 改进

- VSCode Extension: Add config setting to include .gitignored files in file searches

### 🐛 修复

- 修复 directory @-mention causing "No assistant message found" error
- VSCode Extension: Bug fixes for unrelated 'Warmup' conversations, and configuration/settings occasionally being reset to defaults

---

## 📦 2.0.25

### 🗑️ 移除

- 移除 legacy SDK entrypoint. Please migrate to @anthropic-ai/claude-agent-sdk for future SDK updates: https://platform.claude.com/docs/en/agent-sdk/migration-guide

---

## 📦 2.0.24

> 📝 **笔记定位**：[平台全景](../platforms/index.md#-平台全景图) · [配置层级](../configuration/index.md#-配置文件有哪几层)

### ✨ 新功能

- Claude Code Web: Support for Web -> CLI teleport

### 🔧 改进

- Sandbox: Releasing a sandbox mode for the BashTool on Linux & Mac
- Bedrock: Display awsAuthRefresh output when auth is required

### 🐛 修复

- 修复 a bug where project-level skills were not loading when --setting-sources 'project' was specified

---

## 📦 2.0.22

### ✨ 新功能

- IDE: 新增 toggle to enable/disable thinking.
- 新增 support for enterprise managed MCP allowlist and denylist

### 🐛 修复

- 修复 content layout shift when scrolling through slash commands
- 修复 bug causing duplicate permission prompts with parallel tool calls

---

## 📦 2.0.21

### ✨ 新功能

- 新增 an interactive question tool
- 新增 Haiku 4.5 as a model option for Pro users

### 🔧 改进

- Support MCP `structuredContent` field in tool responses
- Claude will now ask you questions more often in plan mode

### 🐛 修复

- 修复 an issue where queued commands don't have access to previous messages' output

---

## 📦 2.0.20

> 📝 **笔记定位**：[什么是 Skill](../skills/index.md#-什么是-skill)

### ✨ 新功能

- 新增 support for Claude Skills

---

## 📦 2.0.19

### 🔧 改进

- Auto-background long-running bash commands instead of killing them. Customize with BASH_DEFAULT_TIMEOUT_MS

### 🐛 修复

- 修复 a bug where Haiku was unnecessarily called in print mode

---

## 📦 2.0.17

> 📝 **笔记定位**：[第三方 LLM](../integrations/index.md#-第三方-llm-提供商) · [Sub-agent](../sub-agents/index.md#为什么需要-sub-agent)

### ✨ 新功能

- 新增 Haiku 4.5 to model selector!
- 引入 the Explore subagent. Powered by Haiku it'll search through your codebase efficiently to save context!

### 🔧 改进

- Haiku 4.5 automatically uses Sonnet in plan mode, and Haiku for execution (i.e. SonnetPlan by default)
- 3P (Bedrock and Vertex) are not automatically upgraded yet. Manual upgrading can be done through setting `ANTHROPIC_DEFAULT_HAIKU_MODEL`
- OTEL: support HTTP_PROXY and HTTPS_PROXY
- `CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC` now disables release notes fetching

---

## 📦 2.0.15

### 🐛 修复

- 修复 bug with resuming where previously created files needed to be read again before writing
- 修复 bug with `-p` mode where @-mentioned files needed to be read again before writing

---

## 📦 2.0.14

### 🔧 改进

- 改进 permission checks for bash with inline env vars
- 减少 unnecessary logins
- Document --system-prompt
- Several improvements to rendering
- Plugins UI polish

### 🐛 修复

- 修复 @-mentioning MCP servers to toggle them on/off
- 修复 ultrathink + thinking toggle

---

## 📦 2.0.13

### 🐛 修复

- 修复 `/plugin` not working on native build

---

## 📦 2.0.12

> 📝 **笔记定位**：[什么是插件](../plugins/index.md#-什么是插件)

### 🔧 改进

- **Plugin System Released**: Extend Claude Code with custom commands, agents, hooks, and MCP servers from marketplaces
- `/plugin install`, `/plugin enable/disable`, `/plugin marketplace` commands for plugin management
- Repository-level plugin configuration via `extraKnownMarketplaces` for team collaboration
- `/plugin validate` command for validating plugin structure and configuration
- Plugin announcement blog post at https://www.anthropic.com/news/claude-code-plugins
- Plugin documentation available at https://code.claude.com/docs/en/plugins
- Comprehensive error messages and diagnostics via `/doctor` command
- Avoid flickering in `/model` selector
- Improvements to `/help`
- Avoid mentioning hooks in `/resume` summaries
- Changes to the "verbose" setting in `/config` now persist across sessions

---

## 📦 2.0.11

### ⚡ 性能

- 减少 system prompt size by 1.4k tokens

### 🐛 修复

- IDE: 修复 keyboard shortcuts and focus issues for smoother interaction
- 修复 Opus fallback rate limit errors appearing incorrectly
- 修复 /add-dir command selecting wrong default tab

---

## 📦 2.0.10

### ✨ 新功能

- 新增 tab completion for shell commands in bash mode
- PreToolUse hooks can now modify tool inputs

### 🔧 改进

- 重写 terminal renderer for buttery smooth UI
- Enable/disable MCP servers by @mentioning, or in /mcp
- Press Ctrl-G to edit your prompt in your system's configured text editor

### 🐛 修复

- Fixes for bash permission checks with environment variables in the command

---

## 📦 2.0.9

### 🐛 修复

- 修复 regression where bash backgrounding stopped working

---

## 📦 2.0.8

### ✨ 新功能

- IDE: 新增 drag-and-drop support for files and folders in chat

### 🔧 改进

- 更新 Bedrock default Sonnet model to `global.anthropic.claude-sonnet-4-5-20250929-v1:0`
- 改进 message rendering for users with light themes on dark terminals

### 🐛 修复

- /context: Fix counting for thinking blocks

### 🗑️ 移除

- 移除 deprecated .claude.json allowedTools, ignorePatterns, env, and todoFeatureEnabled config options (instead, configure these in your settings.json)

---

## 📦 2.0.5

### ✨ 新功能

- IDE: 新增 "Open in Terminal" link in login screen
- SDK: 新增 SDKUserMessageReplay.isReplay to prevent duplicate messages

### 🐛 修复

- IDE: 修复 IME unintended message submission with Enter and Tab
- 修复 unhandled OAuth expiration 401 API errors

---

## 📦 2.0.1

### 🔧 改进

- Skip Sonnet 4.5 default model setting change for Bedrock and Vertex

### 🐛 修复

- Various bug fixes and presentation improvements

---

## 📦 2.0.0

> 📝 **笔记定位**：[平台全景](../platforms/index.md#-平台全景图) · [Sub-agent](../sub-agents/index.md#sub-agent-工作原理) · [Agentic Loop](../how-it-works/index.md#-agentic-loopclaude-code-的核心循环) · [安装](../getting-started/index.md#-怎么安装-claude-code)

### ✨ 新功能

- 新的 native VS Code extension
- 新增 subagents dynamically with `--agents` flag

### 🔧 改进

- Fresh coat of paint throughout the whole app
- /rewind a conversation to undo code changes
- /usage command to see plan limits
- Tab to toggle thinking (sticky across sessions)
- Ctrl-R to search history
- Unshipped claude config command
- Hooks: 减少 PostToolUse 'tool_use' ids were found without 'tool_result' blocks errors
- SDK: The Claude Code SDK is now the Claude Agent SDK

---

---

> 📖 更早版本（1.x 及之前）请查看[更新日志（1.x 及更早）](../changelog-v1/index.md)。
