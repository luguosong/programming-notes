---
title: API 参考
description: TypeScript 和 Python SDK 的完整 API 参考
---

## TypeScript SDK

``` bash
npm install @anthropic-ai/claude-agent-sdk
```

### 核心函数

#### `query()`

与 Claude 交互的主要函数，创建异步生成器流式传输消息。

``` typescript
function query({
  prompt,
  options
}: {
  prompt: string | AsyncIterable<SDKUserMessage>;
  options?: Options;
}): Query;
```

| 参数 | 类型 | 说明 |
|:---|:---|:---|
| `prompt` | `string \| AsyncIterable<SDKUserMessage>` | 输入提示，字符串或异步可迭代对象（流式模式） |
| `options` | `Options` | 可选配置对象 |

返回 `Query` 对象（扩展 `AsyncGenerator<SDKMessage, void>`），支持 `close()`、`interrupt()`、`rewindFiles()`、`setModel()`、`setPermissionMode()` 等方法。

#### `startup()`

预热 CLI 子进程，将子进程生成和初始化移出关键路径。

``` typescript
function startup(params?: {
  options?: Options;
  initializeTimeoutMs?: number;
}): Promise<WarmQuery>;
```

返回 `WarmQuery`，其上的 `query()` 调用直接写入已就绪的进程，无需启动延迟。每个 `WarmQuery` 只能调用一次 `query()`。

#### `tool()`

创建类型安全的 MCP 工具定义，与 SDK MCP 服务器配合使用。

``` typescript
function tool<Schema extends AnyZodRawShape>(
  name: string,
  description: string,
  inputSchema: Schema,
  handler: (args, extra) => Promise<CallToolResult>,
  extras?: { annotations?: ToolAnnotations }
): SdkMcpToolDefinition<Schema>;
```

#### `createSdkMcpServer()`

创建进程内 MCP 服务器实例。

``` typescript
function createSdkMcpServer(options: {
  name: string;
  version?: string;
  tools?: Array<SdkMcpToolDefinition<any>>;
}): McpSdkServerConfigWithInstance;
```

#### `listSessions()` 和 `getSessionMessages()`

发现和读取历史会话。

``` typescript
function listSessions(options?: ListSessionsOptions): Promise<SDKSessionInfo[]>;
function getSessionMessages(sessionId: string, options?: GetSessionMessagesOptions): Promise<SessionMessage[]>;
```

### Options 类型

`query()` 和 `startup()` 的主要配置字段：

| 属性 | 类型 | 默认值 | 说明 |
|:---|:---|:---|:---|
| `prompt` | `string \| AsyncIterable<SDKUserMessage>` | - | 输入提示 |
| `model` | `string` | CLI 默认值 | 使用的 Claude 模型 |
| `systemPrompt` | `string \| { type: 'preset'; preset: 'claude_code'; append?: string }` | `undefined`（最小提示） | 系统提示配置 |
| `permissionMode` | `PermissionMode` | `'default'` | 权限模式 |
| `allowedTools` | `string[]` | `[]` | 自动批准的工具 |
| `disallowedTools` | `string[]` | `[]` | 始终拒绝的工具 |
| `canUseTool` | `CanUseTool` | `undefined` | 自定义权限函数 |
| `mcpServers` | `Record<string, McpServerConfig>` | `{}` | MCP 服务器配置 |
| `agents` | `Record<string, AgentDefinition>` | `undefined` | 子代理定义 |
| `maxTurns` | `number` | `undefined` | 最大代理轮次 |
| `maxBudgetUsd` | `number` | `undefined` | 成本上限（USD） |
| `cwd` | `string` | `process.cwd()` | 工作目录 |
| `env` | `Record<string, string \| undefined>` | `process.env` | 环境变量 |
| `tools` | `string[] \| { type: 'preset'; preset: 'claude_code' }` | `undefined` | 工具配置 |
| `thinking` | `ThinkingConfig` | `{ type: 'adaptive' }` | 思考/推理行为控制 |
| `effort` | `'low' \| 'medium' \| 'high' \| 'xhigh' \| 'max'` | `'high'` | 推理努力程度 |
| `resume` | `string` | `undefined` | 恢复的会话 ID |
| `continue` | `boolean` | `false` | 继续最近对话 |
| `settingSources` | `SettingSource[]` | CLI 默认值 | 文件系统设置来源 |
| `sandbox` | `SandboxSettings` | `undefined` | 沙箱配置 |
| `outputFormat` | `{ type: 'json_schema', schema: JSONSchema }` | `undefined` | 结构化输出格式 |
| `hooks` | `Partial<Record<HookEvent, HookCallbackMatcher[]>>` | `{}` | Hook 回调 |
| `persistSession` | `boolean` | `true` | 是否持久化会话到磁盘 |
| `enableFileCheckpointing` | `boolean` | `false` | 启用文件变更跟踪 |
| `includePartialMessages` | `boolean` | `false` | 包含流式部分消息 |

### 消息类型层级

`SDKMessage` 是所有消息的联合类型：

| 类型 | 说明 |
|:---|:---|
| `SDKAssistantMessage` | 助手响应，`message` 字段包含 `BetaMessage`（含 `content`、`model`、`stop_reason`、`usage`） |
| `SDKUserMessage` | 用户输入消息，支持 `shouldQuery: false` 注入上下文而不触发轮次 |
| `SDKResultMessage` | 最终结果，包含 `subtype`（`success` 或错误类型）、`total_cost_usd`、`usage`、`result` |
| `SDKSystemMessage` | 系统初始化消息，包含可用工具、模型、权限模式等元数据 |
| `SDKPartialAssistantMessage` | 流式部分消息（需启用 `includePartialMessages`） |
| `SDKCompactBoundaryMessage` | 对话压缩边界指示 |
| `SDKStatusMessage` | 状态更新（如压缩中） |
| `SDKTaskNotificationMessage` | 后台任务完成/失败通知 |
| `SDKToolProgressMessage` | 工具执行进度 |
| `SDKPromptSuggestionMessage` | 预测的下一个用户提示 |

### 权限与沙箱类型

#### `PermissionMode`

``` typescript
type PermissionMode =
  | "default"           // 标准权限行为
  | "acceptEdits"       // 自动接受文件编辑
  | "bypassPermissions" // 绕过所有权限检查
  | "plan"              // 规划模式，无执行
  | "dontAsk"           // 不提示，未预批准则拒绝
  | "auto";             // 使用模型分类器自动批准或拒绝
```

#### `ThinkingConfig`

``` typescript
type ThinkingConfig =
  | { type: "adaptive" }                      // 模型自适应决定
  | { type: "enabled"; budgetTokens?: number } // 固定思考令牌预算
  | { type: "disabled" };                     // 禁用扩展思考
```

#### `SandboxSettings`

| 属性 | 类型 | 默认值 | 说明 |
|:---|:---|:---|:---|
| `enabled` | `boolean` | `false` | 启用命令沙箱 |
| `autoAllowBashIfSandboxed` | `boolean` | `true` | 启用沙箱时自动批准 bash |
| `excludedCommands` | `string[]` | `[]` | 始终绕过沙箱的命令 |
| `network` | `SandboxNetworkConfig` | `undefined` | 网络限制配置 |
| `filesystem` | `SandboxFilesystemConfig` | `undefined` | 文件系统读写限制 |

---

## Python SDK

``` bash
pip install claude-agent-sdk
```

### 核心函数

#### `query()`

每次创建新会话的查询函数，返回异步迭代器。

``` python
async def query(
    *,
    prompt: str | AsyncIterable[dict[str, Any]],
    options: ClaudeAgentOptions | None = None,
    transport: Transport | None = None
) -> AsyncIterator[Message]
```

#### `ClaudeSDKClient`

维持对话会话的客户端类，支持多轮对话和中断。

``` python
class ClaudeSDKClient:
    async def connect(self, prompt=None) -> None
    async def query(self, prompt, session_id="default") -> None
    async def receive_messages(self) -> AsyncIterator[Message]
    async def receive_response(self) -> AsyncIterator[Message]
    async def interrupt(self) -> None
    async def set_permission_mode(self, mode: str) -> None
    async def set_model(self, model: str | None = None) -> None
    async def rewind_files(self, user_message_id: str) -> None
    async def get_mcp_status(self) -> McpStatusResponse
    async def disconnect(self) -> None
```

支持 `async with` 上下文管理器自动管理连接。`query()` 每次创建新会话，`ClaudeSDKClient` 在同一上下文中维持多轮对话。

#### `tool()` 装饰器

创建类型安全的 MCP 工具。

``` python
@tool("name", "description", {"param": str})
async def handler(args: dict[str, Any]) -> dict[str, Any]:
    return {"content": [{"type": "text", "text": "..."}]}
```

#### `create_sdk_mcp_server()`

``` python
def create_sdk_mcp_server(
    name: str,
    version: str = "1.0.0",
    tools: list[SdkMcpTool[Any]] | None = None
) -> McpSdkServerConfig
```

#### `list_sessions()` 和 `get_session_messages()`

Python 版本为同步函数，立即返回。

``` python
def list_sessions(directory=None, limit=None, include_worktrees=True) -> list[SDKSessionInfo]
def get_session_messages(session_id, directory=None, limit=None, offset=0) -> list[SessionMessage]
```

### ClaudeAgentOptions 类型

主要配置字段：

| 属性 | 类型 | 默认值 | 说明 |
|:---|:---|:---|:---|
| `system_prompt` | `str \| SystemPromptPreset \| None` | `None` | 系统提示配置 |
| `permission_mode` | `PermissionMode \| None` | `None` | 权限模式 |
| `allowed_tools` | `list[str]` | `[]` | 自动批准的工具 |
| `disallowed_tools` | `list[str]` | `[]` | 始终拒绝的工具 |
| `can_use_tool` | `CanUseTool \| None` | `None` | 自定义权限回调 |
| `model` | `str \| None` | `None` | Claude 模型 |
| `mcp_servers` | `dict[str, McpServerConfig] \| str \| Path` | `{}` | MCP 服务器配置 |
| `agents` | `dict[str, AgentDefinition] \| None` | `None` | 子代理定义 |
| `max_turns` | `int \| None` | `None` | 最大代理轮次 |
| `cwd` | `str \| Path \| None` | `None` | 工作目录 |
| `resume` | `str \| None` | `None` | 恢复会话 ID |
| `thinking` | `ThinkingConfig \| None` | `None` | 思考行为控制 |
| `effort` | `Literal["low", "medium", "high", "max"] \| None` | `None` | 推理努力级别 |
| `setting_sources` | `list[SettingSource] \| None` | `None` | 设置来源 |
| `sandbox` | `SandboxSettings \| None` | `None` | 沙箱配置 |
| `tools` | `list[str] \| ToolsPreset \| None` | `None` | 工具配置 |
| `hooks` | `dict[HookEvent, list[HookMatcher]] \| None` | `None` | Hook 配置 |

### 消息类型

Python `Message` 联合类型：

| 类型 | 说明 |
|:---|:---|
| `AssistantMessage` | 助手响应，包含 `content`（内容块列表）、`model`、`usage`、`error` |
| `UserMessage` | 用户输入，包含 `content`、`uuid`、`tool_use_result` |
| `ResultMessage` | 最终结果，包含 `subtype`、`total_cost_usd`、`usage`、`result`、`model_usage` |
| `SystemMessage` | 系统消息，包含 `subtype` 和 `data` 字典 |
| `StreamEvent` | 流式部分消息（需 `include_partial_messages=True`） |
| `RateLimitEvent` | 速率限制事件 |

### 权限类型

| 类型 | 说明 |
|:---|:---|
| `PermissionMode` | `Literal["default", "acceptEdits", "plan", "dontAsk", "bypassPermissions"]` |
| `PermissionResultAllow` | 允许工具调用，可包含 `updated_input` 和 `updated_permissions` |
| `PermissionResultDeny` | 拒绝工具调用，包含 `message` 和 `interrupt` 标志 |
| `PermissionUpdate` | 权限更新操作（`addRules`、`replaceRules`、`removeRules`、`setMode` 等） |

---

## TypeScript 与 Python 的差异

| 维度 | TypeScript | Python |
|:---|:---|:---|
| **会话管理** | `query()` 内部维持会话，支持 `streamInput()` 多轮 | `query()` 每次新建会话；`ClaudeSDKClient` 维持多轮对话 |
| **消息接收** | `for await (const msg of query(...))` 统一迭代 | `async for msg in query(...)` 或 `client.receive_response()` |
| **会话发现** | 异步（`Promise`） | 同步（立即返回） |
| **选项命名** | camelCase（`allowedTools`、`permissionMode`） | snake_case（`allowed_tools`、`permission_mode`） |
| **子代理字段** | camelCase（`AgentDefinition`） | `AgentDefinition` 字段仍用 camelCase（映射到线路格式） |
| **工具定义** | `tool()` 函数 + Zod schema | `@tool` 装饰器 + Python 类型或 JSON Schema |
| **中断支持** | `query()` 流式模式下支持 | 仅 `ClaudeSDKClient` 支持，`query()` 不支持 |
| **类型系统** | TypeScript 接口/类型别名 | `@dataclass`（属性访问）和 `TypedDict`（键访问）混合使用 |
| **`auto` 权限模式** | 支持 | 不支持 |
