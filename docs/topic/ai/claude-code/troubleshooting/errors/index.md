---
title: 常见错误
description: Claude Code 常见错误信息速查与解决方案
---

Claude Code 的运行时错误大多映射到底层 Claude API 错误码。本页按错误类型分类，列出常见错误信息、含义和解决方案，方便快速定位和修复。

> Claude Code 在展示错误前会自动重试瞬时故障（指数退避，最多 10 次）。当看到下方错误时，说明重试已耗尽。可通过 `CLAUDE_CODE_MAX_RETRIES` 调整重试次数，通过 `API_TIMEOUT_MS`（默认 600000ms）调整请求超时。

## 认证错误

认证错误意味着 Claude Code 无法向 API 证明你的身份。随时运行 `/status` 查看当前活跃凭证。

| 错误信息 | 含义 | 解决方案 |
|---------|------|---------|
| `Not logged in · Please run /login` | 当前会话没有有效凭证 | 运行 `/login`；若使用环境变量认证，确认 `ANTHROPIC_API_KEY` 已正确设置和导出；CI 环境需配置 `apiKeyHelper` 脚本 |
| `Invalid API key · Fix external API key` | API 密钥被拒绝，拼写错误或已被撤销 | 检查密钥拼写，确认在 [Console](https://platform.claude.com/settings/keys) 未被撤销；运行 `env \| grep ANTHROPIC` 检查是否有过时的 `.env` 文件覆盖；也可取消设置后运行 `/login` 使用订阅认证 |
| `This organization has been disabled` | 环境变量中的旧密钥属于已禁用的组织，覆盖了订阅登录 | 在当前 shell 取消设置 `ANTHROPIC_API_KEY` 并从 shell 配置文件中删除，重启 Claude Code 后运行 `/status` 确认凭证 |
| `OAuth token revoked / expired · Please run /login` | 保存的登录令牌已失效或过期 | 运行 `/login`；若同一会话仍报错，先 `/logout` 再 `/login`；跨启动重复提示需检查系统时钟和 macOS Keychain |
| `OAuth token does not meet scope requirement: user:profile` | 存储的令牌缺少新功能所需的权限范围 | 运行 `/login` 获取新令牌，无需先登出 |

## API 服务端错误

服务端错误来自 Anthropic 基础设施，与你的账户或请求无关。

| 错误信息 | 含义 | 解决方案 |
|---------|------|---------|
| `API Error: 500 ... Internal server error` | API 内部意外故障 | 检查 [status.claude.com](https://status.claude.com)；等待一分钟重试（输入 `try again` 即可）；持续无公告则运行 `/feedback` 提交反馈 |
| `API Error: Repeated 529 Overloaded errors` | API 暂时达到容量限制（非个人配额） | 检查 [status.claude.com](https://status.claude.com)；几分钟后重试；运行 `/model` 切换到其他模型（容量按模型分别计算） |
| `Request timed out` | API 在超时时间内未响应 | 重试请求；将任务拆分为较小的提示；慢速网络或代理环境可提高 `API_TIMEOUT_MS` |
| `<model> is temporarily unavailable, so auto mode cannot determine the safety of...` | Auto mode 的分类模型过载，操作被阻止 | 几秒后重试（通常自动重试）；可先进行只读任务；这是临时问题，无需更改设置 |

## 使用限制

使用限制错误意味着你的账户或计划配额已用尽。

| 错误信息 | 含义 | 解决方案 |
|---------|------|---------|
| `You've hit your session limit / weekly limit / Opus limit` | 订阅计划的滚动使用额度已耗尽 | 等待消息中显示的重置时间；运行 `/usage` 查看限额；运行 `/extra-usage` 购买额外用量（Pro/Max）或联系管理员（Team/Enterprise） |
| `Server is temporarily limiting requests` | API 施加了与计划配额无关的短期限流 | 等待片刻重试；持续存在则检查 [status.claude.com](https://status.claude.com) |
| `Request rejected (429)` | 达到 API 密钥 / Bedrock / Vertex AI 的速率限制 | 运行 `/status` 确认活跃凭证；检查提供商控制台的限制配置；降低 `CLAUDE_CODE_MAX_TOOL_USE_CONCURRENCY` 或切换到较小模型 |
| `Credit balance is too low` | Console 组织的预付信用余额不足 | 在 [platform.claude.com/settings/billing](https://platform.claude.com/settings/billing) 充值并开启自动充值；Pro/Max 用户可运行 `/login` 切换到订阅认证 |

## 网络与连接错误

网络错误通常源于本地网络、代理或防火墙，而非 Anthropic 基础设施。

| 错误信息 | 含义 | 解决方案 |
|---------|------|---------|
| `Unable to connect to API` / `fetch failed` | 无法建立到 API 的 TCP 连接 | 运行 `curl -I https://api.anthropic.com` 确认连通性；公司代理需设置 `HTTPS_PROXY`；LLM 网关需设置 `ANTHROPIC_BASE_URL`；确保防火墙放行相关主机 |
| `Unable to connect to API (ECONNREFUSED / ECONNRESET / ETIMEDOUT)` | 连接被拒绝、重置或超时 | 同上；WSL 环境检查 `/etc/resolv.conf`；macOS 检查是否有残留 VPN 隧道接口；退出 Docker Desktop 排除干扰 |
| `SSL certificate verification failed` / `Self-signed certificate detected` | 代理或安全设备使用自签证书，Node.js 不信任 | 导出组织 CA 包并设置 `NODE_EXTRA_CA_CERTS=/path/to/ca-bundle.pem`；**禁止**使用 `NODE_TLS_REJECT_UNAUTHORIZED=0` |

## 请求内容错误

请求内容错误意味着 API 收到了请求但拒绝了其内容。

| 错误信息 | 含义 | 解决方案 |
|---------|------|---------|
| `Prompt is too long` | 对话加附件超出模型上下文窗口 | 运行 `/compact` 压缩历史或 `/clear` 重新开始；运行 `/context` 查看占用详情；禁用未使用的 MCP 服务器（`/mcp disable <name>`）；精简 `CLAUDE.md` 内存文件 |
| `Error during compaction: Conversation too long` | `/compact` 本身失败，上下文已满到无法生成摘要 | 按 Esc 两次回退几轮消息，再运行 `/compact`；仍不行则 `/clear` 开始新会话（历史可通过 `/resume` 恢复） |
| `Request too large` | 请求体超过 API 字节限制（通常因大文件粘贴） | 按 Esc 两次回退；用路径引用大文件而非粘贴内容；让 Claude 分块读取 |
| `Image was too large` | 图片超过大小或尺寸限制 | 按 Esc 两次回退；调整图片大小（单图最长边不超过 8000px，多图场景 2000px）；截取相关区域而非全屏 |
| `PDF too large` / `PDF is password protected` / `PDF was not valid` | PDF 无法处理 | 超大 PDF 用 Read 工具分页读取或用 `pdftotext` 提取文本；受保护或无效 PDF 需先解除保护或重新导出 |
| `Extra inputs are not permitted` | 代理或 LLM 网关删除了 `anthropic-beta` 请求头 | 配置网关转发 `anthropic-beta` 头；或设置 `CLAUDE_CODE_DISABLE_EXPERIMENTAL_BETAS=1` 禁用 beta 功能 |
| `There's an issue with the selected model` | 模型名称未识别或账户无权访问 | 运行 `/model` 选择可用模型；使用别名（`sonnet`、`opus`）代替版本化 ID；检查 `--model`、`ANTHROPIC_MODEL`、设置文件中是否有过时的模型 ID |
| `Claude Opus is not available with the Claude Pro plan` | 当前订阅计划不包含所选模型 | 运行 `/model` 选择计划内的模型；升级计划后需 `/logout` 再 `/login` |
| `thinking.type.enabled is not supported for this model` | Claude Code 版本过旧，不支持新模型的思考配置 | 运行 `claude update` 升级到 v2.1.111+；或运行 `/model` 切换到 Opus 4.6 或 Sonnet |
| `max_tokens must be greater than thinking.budget_tokens` | 扩展思考预算超过最大响应长度 | 降低 `MAX_THINKING_TOKENS` 或提高 `CLAUDE_CODE_MAX_OUTPUT_TOKENS` |
| `API Error: 400 due to tool use concurrency issues` | 对话历史中工具调用和思考块序列不一致 | 运行 `/rewind` 或按 Esc 两次回退到损坏轮次之前，继续对话 |

## 响应质量异常

没有显示错误但 Claude 回答质量明显下降时，排查以下方面：

| 排查方向 | 操作 |
|---------|------|
| 模型选择 | 运行 `/model` 确认当前模型，环境变量或之前的切换可能导致使用了较小模型 |
| 推理强度 | 运行 `/effort` 检查并提高推理级别（`ultrathink` 为最高） |
| 上下文压力 | 运行 `/context` 查看窗口占用，接近满时运行 `/compact` 或 `/clear` |
| 指令过时 | 运行 `/doctor` 检查超大 `CLAUDE.md` 文件和 MCP 工具定义占用 |

响应出错时，用 `/rewind` 或 Esc 两次回退到错误轮次之前，重新表述提示比在对话中直接纠正效果更好。

## 其他错误排查

遇到本页未列出的错误时：

- 运行 `/feedback` 提交反馈（包含对话记录，便于诊断）
- 运行 `/doctor` 检查本地配置
- 检查 [status.claude.com](https://status.claude.com) 是否有活跃事件
- 在 [GitHub Issues](https://github.com/anthropics/claude-code/issues) 搜索已有问题

MCP 服务器、Hook 脚本、安装相关的错误请参阅对应页面：详见「MCP 配置」、「Hook 调试」和「安装故障排除」。
