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
