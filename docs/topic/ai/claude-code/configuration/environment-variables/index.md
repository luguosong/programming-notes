---
title: 环境变量参考
description: Claude Code 全部环境变量速查（按类别分组）
---

# 环境变量参考

**本文你会学到**：

- Claude Code 支持哪些环境变量
- 如何按类别快速查找所需变量（完整参考列表）

Claude Code 支持通过环境变量精细控制其行为。你可以：

- 在启动 `claude` 前在 shell 中设置
- 在 `settings.json` 的 `env` 键中配置（跨会话持久生效）
- 团队管理员通过托管设置统一部署

## API 认证

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `ANTHROPIC_API_KEY` | — | 作为 `X-Api-Key` 标头发送的 API 密钥。设置后替代 Claude Pro/Max/Team/Enterprise 订阅。在非交互模式（`-p`）中始终优先使用；交互模式下会提示确认。要恢复订阅，执行 `unset ANTHROPIC_API_KEY` |
| `ANTHROPIC_AUTH_TOKEN` | — | `Authorization` 标头的自定义值（自动添加 `Bearer ` 前缀） |
| `CLAUDE_CODE_OAUTH_TOKEN` | — | Claude.ai 身份验证的 OAuth 访问令牌，SDK 和自动化环境的 `/login` 替代方案。优先于钥匙链存储的凭证。通过 `claude setup-token` 生成 |
| `CLAUDE_CODE_OAUTH_REFRESH_TOKEN` | — | OAuth 刷新令牌，设置后 `claude auth login` 直接交换此令牌而不打开浏览器。需要同时设置 `CLAUDE_CODE_OAUTH_SCOPES` |
| `CLAUDE_CODE_OAUTH_SCOPES` | — | 刷新令牌颁发时的 OAuth 作用域，空格分隔，如 `"user:profile user:inference user:sessions:claude_code"`。设置刷新令牌时必需 |
| `CLAUDE_CODE_API_KEY_HELPER_TTL_MS` | — | 使用 `apiKeyHelper` 时刷新凭证的间隔（毫秒） |

## API 端点与代理

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `ANTHROPIC_BASE_URL` | — | 覆盖 API 端点，用于通过代理或网关路由请求。设置为非第一方主机时，MCP 工具搜索默认禁用，需手动设置 `ENABLE_TOOL_SEARCH=true` |
| `ANTHROPIC_CUSTOM_HEADERS` | — | 添加到请求的自定义标头（`Name: Value` 格式，多个标头用换行符分隔） |
| `ANTHROPIC_BETAS` | — | 逗号分隔的 `anthropic-beta` 标头值。适用于所有认证方式，包括 Claude.ai 订阅（`--betas` CLI 标志仅支持 API 密钥） |
| `ANTHROPIC_CUSTOM_MODEL_OPTION` | — | 在 `/model` 选择器中添加的自定义模型 ID |
| `ANTHROPIC_CUSTOM_MODEL_OPTION_NAME` | — | 自定义模型条目的显示名称，未设置时默认为模型 ID |
| `ANTHROPIC_CUSTOM_MODEL_OPTION_DESCRIPTION` | — | 自定义模型条目的显示描述 |
| `ANTHROPIC_CUSTOM_MODEL_OPTION_SUPPORTED_CAPABILITIES` | — | 自定义模型的支持能力声明 |
| `CLAUDE_CODE_EXTRA_BODY` | — | JSON 对象，合并到每个 API 请求体的顶级。用于传递 Claude Code 不直接公开的提供商特定参数 |
| `API_TIMEOUT_MS` | `600000`（10 分钟） | API 请求超时时间（毫秒）。最大值 2147483647，超过会导致计时器溢出使请求立即失败 |
| `HTTP_PROXY` | — | HTTP 代理服务器 |
| `HTTPS_PROXY` | — | HTTPS 代理服务器 |
| `NO_PROXY` | — | 绕过代理的域名和 IP 列表 |
| `CLAUDE_CODE_PROXY_RESOLVES_HOSTS` | — | 设为 `1` 允许代理执行 DNS 解析 |

## TLS 与证书

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_CERT_STORE` | `bundled,system` | TLS 连接的 CA 证书源，逗号分隔。`bundled` 是 Claude Code 自带的 Mozilla CA 集，`system` 是操作系统信任存储。Node.js 运行时上仅使用捆绑集 |

当企业使用私有 CA 或需要 mTLS 双向认证时，以下变量派上用场：

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_CLIENT_CERT` | mTLS 客户端证书文件路径 |
| `CLAUDE_CODE_CLIENT_KEY` | mTLS 客户端私钥文件路径 |
| `CLAUDE_CODE_CLIENT_KEY_PASSPHRASE` | 加密私钥的密码短语（可选） |

## 模型配置

### 模型别名覆盖

以下变量用于覆盖内置模型别名指向的实际模型：

| 变量 | 说明 |
|------|------|
| `ANTHROPIC_DEFAULT_OPUS_MODEL` | 覆盖 `opus` 别名的模型 |
| `ANTHROPIC_DEFAULT_SONNET_MODEL` | 覆盖 `sonnet` 别名的模型 |
| `ANTHROPIC_DEFAULT_HAIKU_MODEL` | 覆盖 `haiku` 别名的模型（已弃用） |
| `ANTHROPIC_MODEL` | 按名称选择模型配置（而非直接指定模型 ID） |
| `ANTHROPIC_SMALL_FAST_MODEL` | 后台任务的 Haiku 级模型（已弃用） |
| `ANTHROPIC_SMALL_FAST_MODEL_AWS_REGION` | Bedrock/Mantle 下 Haiku 级模型的 AWS 区域覆盖 |

每个别名还有配套的 `_NAME`、`_DESCRIPTION`、`_SUPPORTED_CAPABILITIES` 后缀变量，用于自定义模型选择器中的显示信息。

### 思考与推理

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MAX_THINKING_TOKENS` | — | 覆盖扩展思考 token 预算，上限为模型最大输出 token 减一。设为 `0` 完全禁用思考。在支持自适应推理的模型上，除非禁用自适应推理，否则此预算被忽略 |
| `CLAUDE_CODE_DISABLE_THINKING` | — | 设为 `1` 强制禁用扩展思考，比 `MAX_THINKING_TOKENS=0` 更直接 |
| `CLAUDE_CODE_DISABLE_ADAPTIVE_THINKING` | — | 设为 `1` 禁用 Opus 4.6 和 Sonnet 4.6 的自适应推理，回退到 `MAX_THINKING_TOKENS` 控制的固定预算。对 Opus 4.7 无效 |
| `CLAUDE_CODE_EFFORT_LEVEL` | — | 设置思考强度。值：`low`、`medium`、`high`、`xhigh`、`max`、`auto`。优先于 `/effort` 和 `effortLevel` 设置 |
| `DISABLE_INTERLEAVED_THINKING` | — | 设为 `1` 禁用交错思考 beta 标头，适用于不支持的 LLM 网关或提供商 |
| `CLAUDE_CODE_MAX_OUTPUT_TOKENS` | — | 设置最大输出 token 数。增加此值会减少自动压缩触发前的有效上下文窗口 |

### 上下文与压缩

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DISABLE_COMPACT` | — | 设为 `1` 禁用所有压缩（自动和手动 `/compact`） |
| `DISABLE_AUTO_COMPACT` | — | 设为 `1` 仅禁用自动压缩，手动 `/compact` 仍可用 |
| `CLAUDE_AUTOCOMPACT_PCT_OVERRIDE` | `95` | 触发自动压缩的上下文容量百分比（1-100）。低于默认值可更早压缩 |
| `CLAUDE_CODE_AUTO_COMPACT_WINDOW` | — | 自动压缩的上下文容量（token）。默认为模型上下文窗口（200K 或 1M）。可与百分比变量配合使用 |
| `CLAUDE_CODE_MAX_CONTEXT_TOKENS` | — | 覆盖 Claude Code 假设的上下文窗口大小。仅在与 `DISABLE_COMPACT` 同时设置时生效 |
| `CLAUDE_CODE_DISABLE_1M_CONTEXT` | — | 设为 `1` 禁用 1M 上下文窗口支持。适用于有合规要求的企业环境 |

### 旧模型映射

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_DISABLE_LEGACY_MODEL_REMAP` | 设为 `1` 阻止 Opus 4.0 和 4.1 自动重映射到当前 Opus 版本。仅在 Anthropic API 上生效，Bedrock/Vertex/Foundry 不运行重映射 |

## Fast Mode

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_DISABLE_FAST_MODE` | 设为 `1` 彻底禁用 Fast Mode。适用于组织需要统一管控成本的场景 |

## Prompt Caching

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `DISABLE_PROMPT_CACHING` | — | 设为 `1` 禁用所有模型的 prompt caching |
| `DISABLE_PROMPT_CACHING_OPUS` | — | 仅禁用 Opus 模型 |
| `DISABLE_PROMPT_CACHING_SONNET` | — | 仅禁用 Sonnet 模型 |
| `DISABLE_PROMPT_CACHING_HAIKU` | — | 仅禁用 Haiku 模型 |
| `ENABLE_PROMPT_CACHING_1H` | — | 设为 `1` 请求 1 小时缓存 TTL（默认 5 分钟）。适用于 API Key、Bedrock、Vertex 和 Foundry。订阅用户自动获得 1 小时 TTL，1 小时写入按更高费率计费 |
| `ENABLE_PROMPT_CACHING_1H_BEDROCK` | — | 已弃用，改用 `ENABLE_PROMPT_CACHING_1H` |
| `FORCE_PROMPT_CACHING_5M` | — | 设为 `1` 强制 5 分钟 TTL，覆盖 `ENABLE_PROMPT_CACHING_1H` |
| `CLAUDE_CODE_ATTRIBUTION_HEADER` | — | 设为 `0` 省略系统提示开头的归属块（版本和提示指纹），可改善通过 LLM 网关路由时的缓存命中率 |

## 流式传输与回退

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_DISABLE_NONSTREAMING_FALLBACK` | — | 设为 `1` 禁用流式请求中途失败时的非流式回退。适用于网关导致回退重复执行工具的场景 |
| `CLAUDE_CODE_ENABLE_FINE_GRAINED_TOOL_STREAMING` | — | 设为 `1` 强制启用细粒度工具输入流式传输。避免 API 完全缓冲工具输入参数造成的延迟。仅限 Anthropic API |
| `CLAUDE_ENABLE_BYTE_WATCHDOG` | — | 设为 `1` 强制启用字节级流式空闲监视器，设为 `0` 强制禁用。未设置时对 Anthropic API 默认启用 |
| `CLAUDE_ENABLE_STREAM_WATCHDOG` | — | 设为 `1` 启用事件级流式空闲监视器。Bedrock/Vertex/Foundry 上唯一可用的空闲监视器 |
| `CLAUDE_STREAM_IDLE_TIMEOUT_MS` | `300000`（5 分钟） | 流式空闲超时。字节级和事件级监视器的默认和最小值，较低值被静默限制以吸收思考暂停和代理缓冲 |

## Bedrock 专用

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_USE_BEDROCK` | 设为 `1` 使用 Amazon Bedrock 作为提供商 |
| `ANTHROPIC_BEDROCK_BASE_URL` | 覆盖 Bedrock 端点 URL，用于自定义端点或 LLM 网关路由 |
| `ANTHROPIC_BEDROCK_MANTLE_BASE_URL` | 覆盖 Bedrock Mantle 端点 URL |
| `CLAUDE_CODE_USE_MANTLE` | 设为 `1` 使用 Bedrock Mantle 端点 |
| `ANTHROPIC_BEDROCK_SERVICE_TIER` | Bedrock 服务层级（`default`、`flex` 或 `priority`） |
| `AWS_BEARER_TOKEN_BEDROCK` | Bedrock API 密钥，用于身份验证 |
| `CLAUDE_CODE_SKIP_BEDROCK_AUTH` | 跳过 Bedrock 的 AWS 身份验证（如使用 LLM 网关时） |
| `CLAUDE_CODE_SKIP_MANTLE_AUTH` | 跳过 Bedrock Mantle 的 AWS 身份验证 |

## Vertex AI 专用

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_USE_VERTEX` | 设为 `1` 使用 Google Vertex AI 作为提供商 |
| `ANTHROPIC_VERTEX_PROJECT_ID` | Vertex AI 的 GCP 项目 ID（使用 Vertex 时必需） |
| `ANTHROPIC_VERTEX_BASE_URL` | 覆盖 Vertex AI 端点 URL |
| `CLAUDE_CODE_SKIP_VERTEX_AUTH` | 跳过 Vertex 的 Google 身份验证 |

### Vertex 区域覆盖

以下变量用于覆盖各模型在 Vertex AI 上的部署区域：

| 变量 | 覆盖模型 |
|------|---------|
| `VERTEX_REGION_CLAUDE_3_5_HAIKU` | Claude 3.5 Haiku |
| `VERTEX_REGION_CLAUDE_3_5_SONNET` | Claude 3.5 Sonnet |
| `VERTEX_REGION_CLAUDE_3_7_SONNET` | Claude 3.7 Sonnet |
| `VERTEX_REGION_CLAUDE_4_0_OPUS` | Claude 4.0 Opus |
| `VERTEX_REGION_CLAUDE_4_0_SONNET` | Claude 4.0 Sonnet |
| `VERTEX_REGION_CLAUDE_4_1_OPUS` | Claude 4.1 Opus |
| `VERTEX_REGION_CLAUDE_4_5_OPUS` | Claude Opus 4.5 |
| `VERTEX_REGION_CLAUDE_4_5_SONNET` | Claude Sonnet 4.5 |
| `VERTEX_REGION_CLAUDE_4_6_OPUS` | Claude Opus 4.6 |
| `VERTEX_REGION_CLAUDE_4_6_SONNET` | Claude Sonnet 4.6 |
| `VERTEX_REGION_CLAUDE_4_7_OPUS` | Claude Opus 4.7 |
| `VERTEX_REGION_CLAUDE_HAIKU_4_5` | Claude Haiku 4.5 |

## Microsoft Foundry 专用

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_USE_FOUNDRY` | 设为 `1` 使用 Microsoft Foundry 作为提供商 |
| `ANTHROPIC_FOUNDRY_API_KEY` | Foundry 身份验证的 API 密钥 |
| `ANTHROPIC_FOUNDRY_BASE_URL` | Foundry 资源的完整基础 URL（如 `https://my-resource.services.ai.azure.com/anthropic`） |
| `ANTHROPIC_FOUNDRY_RESOURCE` | Foundry 资源名称（如 `my-resource`）。未设置 `BASE_URL` 时必需 |
| `CLAUDE_CODE_SKIP_FOUNDRY_AUTH` | 跳过 Foundry 的 Azure 身份验证 |

## MCP 服务器

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MCP_TIMEOUT` | `30000`（30 秒） | MCP 服务器启动超时（毫秒） |
| `MCP_TOOL_TIMEOUT` | `100000000`（约 28 小时） | MCP 工具执行超时（毫秒） |
| `MCP_CONNECTION_NONBLOCKING` | — | 设为 `true` 在非交互模式中完全跳过 MCP 连接等待 |
| `MCP_SERVER_CONNECTION_BATCH_SIZE` | `3` | 启动时并行连接的本地 MCP 服务器（stdio）最大数量 |
| `MCP_REMOTE_SERVER_CONNECTION_BATCH_SIZE` | `20` | 启动时并行连接的远程 MCP 服务器（HTTP/SSE）最大数量 |
| `CLAUDE_CODE_MCP_ALLOWLIST_ENV` | — | 设为 `1` 使用仅安全基线环境生成 stdio MCP 服务器，而非继承 shell 环境 |
| `MCP_CLIENT_SECRET` | — | 预配置凭证 MCP 服务器的 OAuth 客户端密钥，避免交互式提示 |
| `MCP_OAUTH_CALLBACK_PORT` | — | OAuth 重定向回调的固定端口 |
| `ENABLE_TOOL_SEARCH` | — | MCP 工具搜索模式。`true`（始终延迟）、`auto`（10% 阈值）、`auto:N`（自定义阈值）、`false`（提前加载） |
| `MAX_MCP_OUTPUT_TOKENS` | `25000` | MCP 工具响应最大 token 数。超过 10,000 时显示警告 |

## 插件系统

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_PLUGIN_CACHE_DIR` | `~/.claude/plugins` | 插件根目录。市场和插件缓存位于此路径下的子目录 |
| `CLAUDE_CODE_PLUGIN_GIT_TIMEOUT_MS` | `120000`（2 分钟） | 插件 git 操作超时（安装或更新时） |
| `CLAUDE_CODE_PLUGIN_KEEP_MARKETPLACE_ON_FAILURE` | — | 设为 `1` 在 `git pull` 失败时保留现有市场缓存而非擦除重克隆。适用于离线或隔离环境 |
| `CLAUDE_CODE_PLUGIN_SEED_DIR` | — | 只读插件种子目录路径（Unix 用 `:` 分隔，Windows 用 `;` 分隔）。用于容器镜像预填充 |
| `CLAUDE_CODE_DISABLE_OFFICIAL_MARKETPLACE_AUTOINSTALL` | — | 设为 `1` 跳过首次运行时官方插件市场的自动添加 |
| `CLAUDE_CODE_ENABLE_BACKGROUND_PLUGIN_REFRESH` | — | 设为 `1` 在非交互模式中转换边界处刷新插件状态 |
| `CLAUDE_CODE_SYNC_PLUGIN_INSTALL` | — | 设为 `1` 在非交互模式中等待插件安装完成后再查询 |
| `CLAUDE_CODE_SYNC_PLUGIN_INSTALL_TIMEOUT_MS` | — | 同步插件安装超时。超时后继续执行并记录错误 |
| `FORCE_AUTOUPDATE_PLUGINS` | — | 设为 `1` 强制插件自动更新，即使主自动更新程序被禁用 |

## Shell 与 Bash

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_SHELL` | — | 覆盖自动 shell 检测。适用于登录 shell 与工作 shell 不同的情况（如 `bash` vs `zsh`） |
| `CLAUDE_CODE_SHELL_PREFIX` | — | 命令前缀，包装所有 bash 命令（Bash 工具、hooks、MCP stdio 服务器启动）。用于日志或审计 |
| `BASH_DEFAULT_TIMEOUT_MS` | `120000`（2 分钟） | Bash 命令默认超时 |
| `BASH_MAX_TIMEOUT_MS` | `600000`（10 分钟） | Bash 命令最大可设置超时 |
| `BASH_MAX_OUTPUT_LENGTH` | — | Bash 输出最大字符数，超过后中间截断 |
| `CLAUDE_CODE_BASH_MAINTAIN_PROJECT_WORKING_DIR` | — | 设为 `1` 在每个 Bash/PowerShell 命令后返回原始工作目录 |
| `CLAUDE_ENV_FILE` | — | 每个 Bash 命令前运行的 shell 脚本路径。用于在命令间保持 virtualenv 或 conda 激活状态 |
| `CLAUDE_CODE_USE_POWERSHELL_TOOL` | — | 控制 PowerShell 工具。无 Git Bash 的 Windows 自动启用；有 Git Bash 时 `1` 选择加入、`0` 退出 |

### Windows 专用

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_GIT_BASH_PATH` | Git Bash 可执行文件（`bash.exe`）路径。适用于已安装但不在 PATH 中的情况 |

## 安全与沙箱

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_SUBPROCESS_ENV_SCRUB` | 设为 `1` 从子进程环境中删除 Anthropic 和云提供商凭证。Linux 上还在隔离 PID 命名空间中运行 Bash 子进程。配置 `allowed_non_write_users` 时 `claude-code-action` 自动设置 |
| `CLAUDE_CODE_SCRIPT_CAPS` | JSON 对象，限制特定脚本在会话中的调用次数。如 `{"deploy.sh": 2}`。基于子字符串匹配 |
| `CLAUDE_CODE_DISABLE_ATTACHMENTS` | 设为 `1` 禁用附件处理。`@` 文件提及作为纯文本发送 |

## 文件与搜索

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_GLOB_HIDDEN` | — | 设为 `false` 在 Glob 工具结果中排除点文件。默认包含。不影响 `@` 文件自动完成、`ls`、Grep 或 Read |
| `CLAUDE_CODE_GLOB_NO_IGNORE` | — | 设为 `false` 使 Glob 工具尊重 `.gitignore`。默认不尊重。不影响 `@` 文件自动完成 |
| `CLAUDE_CODE_GLOB_TIMEOUT_SECONDS` | `20`（WSL 默认 60） | Glob 文件发现超时（秒） |
| `CLAUDE_CODE_FILE_READ_MAX_OUTPUT_TOKENS` | — | 覆盖文件读取默认 token 限制 |
| `USE_BUILTIN_RIPGREP` | — | 设为 `0` 使用系统安装的 `rg` 而非内置版本 |
| `CLAUDE_CODE_USE_NATIVE_FILE_SEARCH` | — | 设为 `1` 使用 Node.js 文件 API 发现命令/subagent/输出样式。适用于内置 ripgrep 不可用的情况 |

## Subagent 与任务

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_FORK_SUBAGENT` | — | 设为 `1` 启用分叉 subagent（继承完整对话上下文而非从头开始）。交互模式和 SDK 均支持 |
| `CLAUDE_AGENT_SDK_DISABLE_BUILTIN_AGENTS` | — | 设为 `1` 禁用所有内置 subagent 类型（Explore、Plan 等）。仅限非交互模式 |
| `CLAUDE_AGENT_SDK_MCP_NO_PREFIX` | — | 设为 `1` 跳过 SDK 创建的 MCP 服务器工具名称上的 `mcp__` 前缀 |
| `CLAUDE_CODE_SUBAGENT_MODEL` | — | Subagent 使用的模型，详见「模型与输出风格」中的模型别名配置 |
| `CLAUDE_CODE_MAX_TOOL_USE_CONCURRENCY` | `10` | 可并行执行的只读工具和 subagent 最大数量 |
| `TASK_MAX_OUTPUT_LENGTH` | `32000`（最大 160000） | Subagent 输出最大字符数，超过后截断（完整输出保存到磁盘） |
| `SLASH_COMMAND_TOOL_CHAR_BUDGET` | 动态（回退 8000） | Skill 元数据的字符预算，动态扩展为上下文窗口的 1% |
| `CLAUDE_CODE_ENABLE_TASKS` | — | 设为 `1` 在非交互模式中启用任务跟踪 |
| `CLAUDE_CODE_TASK_LIST_ID` | — | 跨会话共享任务列表的 ID。多实例设置相同 ID 即可协调 |

## Hooks 与会话

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_SESSIONEND_HOOKS_TIMEOUT_MS` | `1500`（最高 60000） | SessionEnd hooks 时间预算。自动提高到配置的最高 hook timeout |
| `CLAUDE_CODE_DISABLE_CRON` | — | 设为 `1` 禁用计划任务。`/loop` skill 和 cron 工具不可用 |
| `CLAUDE_CODE_RESUME_INTERRUPTED_TURN` | — | 设为 `1` 在上一个会话中途结束时自动恢复。适用于 SDK 模式 |

## 内存与上下文

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_DISABLE_AUTO_MEMORY` | 设为 `1` 禁用自动内存。设为 `0` 在逐步推出期间强制启用 |
| `CLAUDE_CODE_DISABLE_CLAUDE_MDS` | 设为 `1` 阻止加载所有 CLAUDE.md 内存文件（用户、项目、自动内存） |
| `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD` | 设为 `1` 从 `--add-dir` 指定的目录加载内存文件 |
| `CLAUDECODE` | Claude Code 生成的 shell 中自动设为 `1`。hooks 或状态行命令中未设置。用于检测是否在 Claude Code shell 内运行 |

## UI 与终端

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_NO_FLICKER` | 设为 `1` 启用全屏渲染（减少闪烁，保持内存平坦）。等同于 `tui` 设置 |
| `CLAUDE_CODE_DISABLE_MOUSE` | 设为 `1` 禁用全屏渲染中的鼠标跟踪 |
| `CLAUDE_CODE_DISABLE_VIRTUAL_SCROLL` | 设为 `1` 禁用全屏渲染中的虚拟滚动，渲染所有消息 |
| `CLAUDE_CODE_SCROLL_SPEED` | — | 全屏渲染中鼠标滚轮滚动倍数（1-20） |
| `CLAUDE_CODE_ACCESSIBILITY` | 设为 `1` 保持原生终端光标可见，允许屏幕放大器跟踪 |
| `CLAUDE_CODE_SYNTAX_HIGHLIGHT` | 设为 `false` 禁用 diff 输出中的语法高亮 |
| `CLAUDE_CODE_DISABLE_TERMINAL_TITLE` | 设为 `1` 禁用基于对话上下文的自动终端标题更新 |
| `CLAUDE_CODE_HIDE_CWD` | 设为 `1` 在启动徽标中隐藏工作目录。适用于屏幕共享或录制 |
| `CLAUDE_CODE_TMUX_TRUECOLOR` | 设为 `1` 允许 tmux 内 24 位真彩色输出。需配合 `~/.tmux.conf` 中的 `set -ga terminal-overrides ',*:Tc'` |

## IDE 集成

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_AUTO_CONNECT_IDE` | 覆盖自动 IDE 连接。设为 `false` 阻止自动连接，设为 `true` 强制尝试连接（如 tmux 遮挡父终端时） |
| `CLAUDE_CODE_IDE_HOST_OVERRIDE` | 覆盖 IDE 扩展连接的主机地址 |
| `CLAUDE_CODE_IDE_SKIP_AUTO_INSTALL` | 跳过 IDE 扩展自动安装 |
| `CLAUDE_CODE_IDE_SKIP_VALID_CHECK` | 设为 `1` 跳过连接期间 IDE 锁定文件验证 |

## 远程控制与云会话

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_REMOTE` | 云会话中自动设为 `true`。hooks 或设置脚本中读取此值检测云环境 |
| `CLAUDE_CODE_REMOTE_SESSION_ID` | 云会话中自动设为当前会话 ID，用于构造会话转录链接 |
| `CCR_FORCE_BUNDLE` | 设为 `1` 强制 `claude --remote` 捆绑上传本地仓库（即使 GitHub 可用） |
| `CLAUDE_REMOTE_CONTROL_SESSION_NAME_PREFIX` | 远程控制会话自动名称的前缀。默认为主机名 |

## 监控与遥测（OpenTelemetry）

### 遥测开关

| 变量 | 说明 |
|------|------|
| `DISABLE_TELEMETRY` | 设为 `1` 退出 Statsig 遥测（不含用户数据） |
| `CLAUDE_CODE_ENABLE_TELEMETRY` | 设为 `1` 启用 OpenTelemetry 数据收集 |
| `DISABLE_ERROR_REPORTING` | 设为 `1` 退出 Sentry 错误报告 |
| `CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC` | 等同于同时设置 `DISABLE_AUTOUPDATER`、`DISABLE_FEEDBACK_COMMAND`、`DISABLE_ERROR_REPORTING`、`DISABLE_TELEMETRY` |
| `CLAUDE_CODE_PROVIDER_MANAGED_BY_HOST` | 由嵌入 Claude Code 的主机平台设置，管理提供商路由和遥测行为 |

### OTel 配置

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `OTEL_LOG_TOOL_CONTENT` | — | 设为 `1` 在 OTel span 中包含工具输入输出（默认禁用以保护敏感数据） |
| `OTEL_LOG_TOOL_DETAILS` | — | 设为 `1` 包含工具参数、MCP 服务器名、原始错误等详情 |
| `OTEL_LOG_USER_PROMPTS` | — | 设为 `1` 包含用户提示文本（默认编辑隐藏） |
| `OTEL_LOG_RAW_API_BODIES` | — | 设为 `1` 发出完整 API 请求/响应 JSON；设为 `file:<dir>` 写入磁盘 |
| `OTEL_METRICS_INCLUDE_ACCOUNT_UUID` | 包含 | 设为 `false` 从指标中排除账户 UUID |
| `OTEL_METRICS_INCLUDE_SESSION_ID` | 包含 | 设为 `false` 从指标中排除会话 ID |
| `OTEL_METRICS_INCLUDE_VERSION` | 排除 | 设为 `true` 在指标中包含 Claude Code 版本 |
| `CLAUDE_CODE_OTEL_FLUSH_TIMEOUT_MS` | `5000` | 刷新待处理 OTel spans 超时 |
| `CLAUDE_CODE_OTEL_HEADERS_HELPER_DEBOUNCE_MS` | `1740000`（29 分钟） | 刷新动态 OTel 标头间隔 |
| `CLAUDE_CODE_OTEL_SHUTDOWN_TIMEOUT_MS` | `2000` | OTel 导出器关闭超时 |

标准 OpenTelemetry 导出器变量同样受支持：`OTEL_METRICS_EXPORTER`、`OTEL_LOGS_EXPORTER`、`OTEL_EXPORTER_OTLP_ENDPOINT`、`OTEL_EXPORTER_OTLP_PROTOCOL`、`OTEL_EXPORTER_OTLP_HEADERS`、`OTEL_METRIC_EXPORT_INTERVAL`、`OTEL_RESOURCE_ATTRIBUTES` 及信号特定变体。

## 更新与安装

| 变量 | 说明 |
|------|------|
| `DISABLE_AUTOUPDATER` | 设为 `1` 禁用自动后台更新，手动 `claude update` 仍有效 |
| `DISABLE_UPDATES` | 设为 `1` 阻止所有更新（包括手动），比 `DISABLE_AUTOUPDATER` 更严格 |
| `DISABLE_INSTALLATION_CHECKS` | 设为 `1` 禁用安装警告 |
| `DISABLE_GROWTHBOOK` | 设为 `1` 禁用功能标志获取，使用代码默认值 |

## 命令与功能开关

| 变量 | 说明 |
|------|------|
| `DISABLE_FEEDBACK_COMMAND` | 设为 `1` 禁用 `/feedback` 命令（旧名 `DISABLE_BUG_COMMAND`） |
| `DISABLE_FEEDBACK_SURVEY` | 别名，与 `CLAUDE_CODE_DISABLE_FEEDBACK_SURVEY` 同义 |
| `DISABLE_LOGIN_COMMAND` | 设为 `1` 隐藏 `/login` 命令 |
| `DISABLE_LOGOUT_COMMAND` | 设为 `1` 隐藏 `/logout` 命令 |
| `DISABLE_UPGRADE_COMMAND` | 设为 `1` 隐藏 `/upgrade` 命令 |
| `DISABLE_DOCTOR_COMMAND` | 设为 `1` 隐藏 `/doctor` 命令 |
| `DISABLE_EXTRA_USAGE_COMMAND` | 设为 `1` 隐藏 `/extra-usage` 命令 |
| `DISABLE_COST_WARNINGS` | 设为 `1` 禁用成本警告消息 |
| `DISABLE_INSTALL_GITHUB_APP_COMMAND` | 设为 `1` 隐藏 `/install-github-app` 命令 |
| `ENABLE_CLAUDEAI_MCP_SERVERS` | 设为 `false` 禁用 claude.ai MCP 服务器（已登录用户默认启用） |
| `CLAUDE_CODE_DISABLE_PROMPT_SUGGESTION` | 设为 `false` 禁用提示建议 |
| `CLAUDE_CODE_ENABLE_AWAY_SUMMARY` | 覆盖会话回顾可用性。`0` 强制关闭，`1` 强制启用 |
| `CLAUDE_CODE_DISABLE_POLICY_SKILLS` | 设为 `1` 跳过系统范围的托管 skills 目录 |
| `IS_DEMO` | 设为 `1` 启用演示模式：隐藏邮箱/组织名，跳过入门 |

## 调试与日志

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CODE_DEBUG_LOGS_DIR` | `~/.claude/debug/<id>.txt` | 调试日志文件路径（需通过 `--debug` 或 `/debug` 启用调试模式） |
| `CLAUDE_CODE_DEBUG_LOG_LEVEL` | `debug` | 调试日志最小级别：`verbose`、`debug`、`info`、`warn`、`error` |
| `CLAUDE_CODE_SKIP_PROMPT_HISTORY` | — | 设为 `1` 跳过将提示历史和会话转录写入磁盘。会话不出现在 `--resume`、`--continue` 或历史中 |

## 配置与路径

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `CLAUDE_CONFIG_DIR` | `~/.claude` | 覆盖配置目录。所有设置、凭证、会话历史和插件都存储在此路径下。适用于并行运行多个账户 |
| `CLAUDE_CODE_TMPDIR` | 平台默认临时目录 | 覆盖内部临时文件目录。Claude Code 在此路径下创建 `/claude-{uid}/` 子目录 |
| `FALLBACK_FOR_ALL_PRIMARY_MODELS` | — | 设为任意非空值使所有主模型过载时触发 fallback model |

## 简化与精简模式

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_SIMPLE` | 设为 `1` 使用最小系统提示和仅 Bash/Read/Edit 工具运行。禁用 hooks、skills、plugins、MCP servers、自动内存和 CLAUDE.md 自动发现。等同于 `--bare` CLI 标志 |
| `CLAUDE_CODE_SIMPLE_SYSTEM_PROMPT` | 设为 `1` 在 Opus 4.7 上使用较短系统提示。保持完整工具集、hooks、MCP 和 CLAUDE.md 发现 |

## 结构化输出

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MAX_STRUCTURED_OUTPUT_RETRIES` | `5` | 非交互模式中 `--json-schema` 验证失败时的重试次数 |

## Git 与 Perforce

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_DISABLE_GIT_INSTRUCTIONS` | 设为 `1` 从系统提示中删除内置 git 工作流说明和状态快照。适用于使用自定义 git 工作流 skills 的场景 |
| `CLAUDE_CODE_DISABLE_FILE_CHECKPOINTING` | 设为 `1` 禁用文件 checkpointing，`/rewind` 无法恢复代码更改 |
| `CLAUDE_CODE_PERFORCE_MODE` | 设为 `1` 启用 Perforce 感知的写入保护。Edit/Write 在目标文件缺少写权限时失败并提示 `p4 edit` |

## 实验性功能

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS` | 设为 `1` 启用代理团队功能（默认禁用） |
| `CLAUDE_CODE_DISABLE_EXPERIMENTAL_BETAS` | 设为 `1` 从 API 请求中移除 `anthropic-beta` 标头和 beta 工具字段。适用于网关拒绝这些字段的情况 |
| `CLAUDE_CODE_MAX_RETRIES` | `10` | 覆盖失败 API 请求的重试次数 |
| `CLAUDE_CODE_EXIT_AFTER_STOP_DELAY` | — | 查询循环空闲后自动退出前的等待时间（毫秒）。适用于 SDK 自动化工作流 |
| `CLAUDE_CODE_AUTO_BACKGROUND_TASKS` | 设为 `1` 强制启用长时间运行代理任务的自动后台处理 |

## 提供商托管模式

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_PROVIDER_MANAGED_BY_HOST` | 由嵌入 Claude Code 的主机平台设置。设置后，提供商选择、端点和认证变量在设置文件中被忽略，防止用户设置覆盖主机路由 |

## 其他

| 变量 | 说明 |
|------|------|
| `CLAUDE_CODE_TEAM_NAME` | 代理团队中此成员所属的团队名称。代理团队自动设置 |
| `DISABLE_INTERLEAVED_THINKING` | 设为 `1` 防止发送交错思考 beta 标头 |
