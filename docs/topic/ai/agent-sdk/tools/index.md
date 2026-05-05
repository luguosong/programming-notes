---
title: 工具与 MCP
description: 扩展 Agent 能力——自定义工具、MCP 集成与大规模工具搜索
---

Agent SDK 内置了一组强大的工具（文件操作、搜索、命令执行等），但实际应用中你往往需要更多——查询数据库、调用外部 API、执行领域特定逻辑。 Agent SDK 提供了三种扩展途径：自定义工具、MCP 服务器集成和工具搜索优化。

**本文你会学到**：

- 如何用 `@tool` / `tool()` 定义自定义工具并注册到 Agent
- 如何通过 MCP 协议连接外部工具服务器
- 如何用 Tool Search 优雅地管理大规模工具集

## 自定义工具

### 为什么需要自定义工具？

内置工具覆盖了通用的文件操作和命令执行场景，但当你需要让 Agent 访问数据库、调用公司内部 API 或执行特定业务逻辑时，就需要自定义工具。自定义工具通过 SDK 的进程内 MCP 服务器暴露给 Claude——不需要启动单独的进程，直接在你的应用代码中定义。

### 工具的四要素

一个工具由四个部分组成，分别传给 Python 的 `@tool` 装饰器或 TypeScript 的 `tool()` 函数：

| 要素 | 说明 |
|------|------|
| **Name** | 唯一标识符，Claude 用它来调用工具 |
| **Description** | 描述工具的功能，Claude 据此决定何时调用 |
| **Input Schema** | 参数定义。TypeScript 使用 Zod schema，Python 使用 `dict`（如 `{"latitude": float}`），也支持完整 JSON Schema |
| **Handler** | 异步函数，接收验证后的参数，返回包含 `content` 数组（和可选 `isError`）的对象 |

handler 返回的 `content` 数组中，每个元素可以是 `text`、`image` 或 `resource` 类型的块。

### 天气工具示例

下面是一个完整的天气查询工具，从定义到注册再到调用：

```python title="定义并注册天气工具"
from typing import Any
import httpx
from claude_agent_sdk import tool, create_sdk_mcp_server


# 定义工具：名称、描述、输入 schema、handler
@tool(
    "get_temperature",
    "Get the current temperature at a location",
    {"latitude": float, "longitude": float},
)
async def get_temperature(args: dict[str, Any]) -> dict[str, Any]:
    async with httpx.AsyncClient() as client:
        response = await client.get(
            "https://api.open-meteo.com/v1/forecast",
            params={
                "latitude": args["latitude"],
                "longitude": args["longitude"],
                "current": "temperature_2m",
                "temperature_unit": "fahrenheit",
            },
        )
        data = response.json()

    return {
        "content": [
            {
                "type": "text",
                "text": f"Temperature: {data['current']['temperature_2m']}°F",
            }
        ]
    }


# 将工具包装在进程内 MCP 服务器中
weather_server = create_sdk_mcp_server(
    name="weather",
    version="1.0.0",
    tools=[get_temperature],
)
```

```typescript title="定义并注册天气工具"
import { tool, createSdkMcpServer } from "@anthropic-ai/claude-agent-sdk";
import { z } from "zod";

const getTemperature = tool(
  "get_temperature",
  "Get the current temperature at a location",
  {
    latitude: z.number().describe("Latitude coordinate"),
    longitude: z.number().describe("Longitude coordinate")
  },
  async (args) => {
    const response = await fetch(
      `https://api.open-meteo.com/v1/forecast?latitude=${args.latitude}&longitude=${args.longitude}&current=temperature_2m&temperature_unit=fahrenheit`
    );
    const data: any = await response.json();

    return {
      content: [{ type: "text", text: `Temperature: ${data.current.temperature_2m}°F` }]
    };
  }
);

const weatherServer = createSdkMcpServer({
  name: "weather",
  version: "1.0.0",
  tools: [getTemperature]
});
```

然后将其传递给 `query`：

```python title="调用天气工具"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage


async def main():
    options = ClaudeAgentOptions(
        mcp_servers={"weather": weather_server},
        allowed_tools=["mcp__weather__get_temperature"],
    )

    async for message in query(
        prompt="旧金山的温度是多少？",
        options=options,
    ):
        if isinstance(message, ResultMessage) and message.subtype == "success":
            print(message.result)


asyncio.run(main())
```

```typescript title="调用天气工具"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "旧金山的温度是多少？",
  options: {
    mcpServers: { weather: weatherServer },
    allowedTools: ["mcp__weather__get_temperature"]
  }
})) {
  if (message.type === "result" && message.subtype === "success") {
    console.log(message.result);
  }
}
```

> 工具的完整限定名格式为 `mcp__{server_name}__{tool_name}`。`mcpServers` 中的 key 成为 `server_name` 部分。多个工具可以用通配符 `mcp__weather__*` 一次性批准。

### 工具注解

[Tool Annotations](https://modelcontextprotocol.io/docs/concepts/tools#tool-annotations) 是可选的元数据，描述工具的行为特征。所有 hint 字段都是布尔值：

| 字段 | 默认值 | 含义 |
|------|--------|------|
| `readOnlyHint` | `false` | 工具不修改环境。控制是否可以与其他只读工具并行调用 |
| `destructiveHint` | `true` | 工具可能执行破坏性更新。仅作信息提示 |
| `idempotentHint` | `false` | 相同参数的重复调用没有额外效果。仅作信息提示 |
| `openWorldHint` | `true` | 工具触及进程外的系统。仅作信息提示 |

注解是元数据而非强制措施。标记为 `readOnlyHint: true` 的工具仍然可以在 handler 中写磁盘——保持注解与实际行为一致即可。

```python title="为只读工具添加注解"
from claude_agent_sdk import tool, ToolAnnotations


@tool(
    "get_temperature",
    "Get the current temperature at a location",
    {"latitude": float, "longitude": float},
    annotations=ToolAnnotations(
        readOnlyHint=True  # 允许 Claude 将此工具与其他只读调用批量执行
    ),
)
async def get_temperature(args):
    return {"content": [{"type": "text", "text": "..."}]}
```

```typescript title="为只读工具添加注解"
tool(
  "get_temperature",
  "Get the current temperature at a location",
  { latitude: z.number(), longitude: z.number() },
  async (args) => ({ content: [{ type: "text", text: `...` }] }),
  { annotations: { readOnlyHint: true } } // 允许 Claude 批量并行调用
);
```

### 错误处理

handler 如何报告错误决定了 Agent 循环是继续还是停止：

| 情况 | 结果 |
|------|------|
| handler 抛出未捕获异常 | Agent 循环停止，Claude 看不到错误 |
| handler 捕获错误并返回 `isError: true` | Agent 循环继续，Claude 看到错误并可以重试或换方法 |

```python title="优雅的错误处理"
import httpx
from typing import Any


@tool(
    "fetch_data",
    "Fetch data from an API",
    {"endpoint": str},
)
async def fetch_data(args: dict[str, Any]) -> dict[str, Any]:
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(args["endpoint"])
            if response.status_code != 200:
                # 返回错误结果让 Claude 能做出反应，而非直接抛出异常
                return {
                    "content": [
                        {
                            "type": "text",
                            "text": f"API error: {response.status_code} {response.reason_phrase}",
                        }
                    ],
                    "is_error": True,
                }

            data = response.json()
            return {"content": [{"type": "text", "text": str(data)}]}
    except Exception as e:
        # 捕获异常保持循环存活，未捕获异常会终止整个 query()
        return {
            "content": [{"type": "text", "text": f"Failed to fetch data: {str(e)}"}],
            "is_error": True,
        }
```

```typescript title="优雅的错误处理"
tool(
  "fetch_data",
  "Fetch data from an API",
  {
    endpoint: z.string().url().describe("API endpoint URL")
  },
  async (args) => {
    try {
      const response = await fetch(args.endpoint);

      if (!response.ok) {
        return {
          content: [
            {
              type: "text",
              text: `API error: ${response.status} ${response.statusText}`
            }
          ],
          isError: true
        };
      }

      const data = await response.json();
      return {
        content: [{ type: "text", text: JSON.stringify(data, null, 2) }]
      };
    } catch (error) {
      return {
        content: [
          {
            type: "text",
            text: `Failed to fetch data: ${error instanceof Error ? error.message : String(error)}`
          }
        ],
        isError: true
      };
    }
  }
);
```

### 返回图片和资源

`content` 数组不仅支持 `text`，还支持 `image` 和 `resource` 块，可以在同一个响应中混合使用。

#### 图片

图片块以 base64 编码的方式内联传输。没有 URL 字段——如果你的图片在某个 URL 上，需要在 handler 中 fetch、读取字节并 base64 编码后返回。

| 字段 | 类型 | 说明 |
|------|------|------|
| `type` | `"image"` | |
| `data` | `string` | base64 编码的字节（纯 base64，不要 `data:image/...;base64,` 前缀） |
| `mimeType` | `string` | 必填，如 `image/png`、`image/jpeg`、`image/webp`、`image/gif` |

```python title="返回图片的工具"
import base64
import httpx


@tool("fetch_image", "Fetch an image from a URL and return it to Claude", {"url": str})
async def fetch_image(args):
    async with httpx.AsyncClient() as client:
        response = await client.get(args["url"])

    return {
        "content": [
            {
                "type": "image",
                "data": base64.b64encode(response.content).decode("ascii"),
                "mimeType": response.headers.get("content-type", "image/png"),
            }
        ]
    }
```

#### 资源

资源块嵌入一个由 URI 标识的内容片段。URI 是供 Claude 引用的标签，实际内容在 `text` 或 `blob` 字段中。适用于工具生成了某个可以被命名引用的内容（如生成的文件或外部系统记录）。

```python title="返回资源块
return {
    "content": [
        {
            "type": "resource",
            "resource": {
                "uri": "file:///tmp/report.md",  # 供 Claude 引用的标签，SDK 不会读取该路径
                "mimeType": "text/markdown",
                "text": "# Report\n...",  # 实际内容，内联
            },
        }
    ]
}
```

### 控制内置工具可见性

`tools` 选项和 allowed/disallowed 列表作用于不同层面：

| 选项 | 层面 | 效果 |
|------|------|------|
| `tools: ["Read", "Grep"]` | 可用性 | 只有列出的内置工具出现在 Claude 上下文中。MCP 工具不受影响 |
| `tools: []` | 可用性 | 移除所有内置工具，Claude 只能使用你的 MCP 工具 |
| allowed tools | 权限 | 列出的工具无需权限提示即可运行。未列出的工具仍可用，但走权限流程 |
| disallowed tools | 权限 | 对列出的工具的每次调用都被拒绝。工具仍在上下文中，Claude 可能仍会尝试调用 |

优先用 `tools` 而非 `disallowed tools` 来限制内置工具——从 `tools` 中省略一个工具会让 Claude 完全不知道它的存在，避免浪费 turn 去尝试一个注定被拒绝的调用。

## MCP 集成

### MCP 是什么？

[MCP（Model Context Protocol）](https://modelcontextprotocol.io/docs/getting-started/intro) 是一个开放标准，用于连接 AI Agent 到外部工具和数据源。通过 MCP，你的 Agent 可以查询数据库、集成 Slack 和 GitHub 等 API、连接其他服务——而无需编写自定义工具实现。

MCP 服务器可以作为本地进程运行、通过 HTTP 连接、或直接在你的 SDK 应用内执行。

### 添加 MCP 服务器

你可以在 `query()` 的代码中配置 MCP 服务器，也可以通过 `.mcp.json` 配置文件加载。

#### 在代码中配置

```python title="在代码中添加 MCP 服务器"
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage

options = ClaudeAgentOptions(
    mcp_servers={
        "filesystem": {
            "command": "npx",
            "args": [
                "-y",
                "@modelcontextprotocol/server-filesystem",
                "/Users/me/projects",
            ],
        }
    },
    allowed_tools=["mcp__filesystem__*"],
)

async for message in query(prompt="列出项目中的文件", options=options):
    if isinstance(message, ResultMessage) and message.subtype == "success":
        print(message.result)
```

```typescript title="在代码中添加 MCP 服务器"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "列出项目中的文件",
  options: {
    mcpServers: {
      filesystem: {
        command: "npx",
        args: ["-y", "@modelcontextprotocol/server-filesystem", "/Users/me/projects"]
      }
    },
    allowedTools: ["mcp__filesystem__*"]
  }
})) {
  if (message.type === "result" && message.subtype === "success") {
    console.log(message.result);
  }
}
```

#### 通过配置文件

在项目根目录创建 `.mcp.json`，SDK 会在启用 `project` 设置源时自动加载（默认即启用）：

```json title=".mcp.json"
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-filesystem", "/Users/me/projects"]
    }
  }
}
```

### 批准 MCP 工具

MCP 工具需要显式授权才能被 Claude 调用。没有权限时，Claude 能看到工具存在但无法调用。

工具命名格式为 `mcp__{server_name}__{tool_name}`。例如名为 `"github"` 的服务器中的 `list_issues` 工具，全名为 `mcp__github__list_issues`。

```typescript title="用通配符批准 MCP 工具"
const _ = {
  options: {
    mcpServers: { /* 你的服务器 */ },
    allowedTools: [
      "mcp__github__*",        // github 服务器的所有工具
      "mcp__db__query",        // db 服务器的 query 工具
      "mcp__slack__send_message" // slack 服务器的 send_message 工具
    ]
  }
};
```

> **优先使用 `allowedTools` 而非权限模式来管理 MCP 访问。** `permissionMode: "acceptEdits"` 不会自动批准 MCP 工具，`permissionMode: "bypassPermissions"` 虽然会但同时也禁用了所有其他安全提示。`allowedTools` 中的通配符精确授权你想要的 MCP 服务器，不多不少。

### 传输类型

MCP 服务器使用不同的传输协议与 Agent 通信。如何选择取决于服务器文档提供的信息：

| 服务器文档提供 | 使用方式 |
|--------------|---------|
| 一个要运行的**命令**（如 `npx @modelcontextprotocol/server-github`） | stdio |
| 一个 **URL** | HTTP 或 SSE |
| 你在代码中构建工具 | SDK MCP 服务器 |

#### stdio 服务器

本地进程通过 stdin/stdout 通信，适合在本机运行的 MCP 服务器：

```python title="stdio 传输的 MCP 服务器"
options = ClaudeAgentOptions(
    mcp_servers={
        "github": {
            "command": "npx",
            "args": ["-y", "@modelcontextprotocol/server-github"],
            "env": {"GITHUB_TOKEN": os.environ["GITHUB_TOKEN"]},
        }
    },
    allowed_tools=["mcp__github__list_issues", "mcp__github__search_issues"],
)
```

对应的 `.mcp.json` 配置：

```json title="stdio 服务器的 .mcp.json"
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "${GITHUB_TOKEN}"
      }
    }
  }
}
```

`${GITHUB_TOKEN}` 语法会在运行时展开环境变量。

#### HTTP/SSE 服务器

用于云端托管的 MCP 服务器和远程 API：

```python title="HTTP/SSE 传输的 MCP 服务器"
options = ClaudeAgentOptions(
    mcp_servers={
        "remote-api": {
            "type": "sse",
            "url": "https://api.example.com/mcp/sse",
            "headers": {"Authorization": f"Bearer {os.environ['API_TOKEN']}"},
        }
    },
    allowed_tools=["mcp__remote-api__*"],
)
```

对于 HTTP（非流式），使用 `"type": "http"` 而非 `"type": "sse"`。

### 认证

大多数 MCP 服务器需要认证。传递凭据的方式取决于传输类型：

- **stdio 服务器**：通过 `env` 字段传递环境变量
- **HTTP/SSE 服务器**：通过 `headers` 字段直接传递认证头
- **OAuth2**：SDK 不自动处理 OAuth 流程，但在你的应用中完成 OAuth 流程后，可以通过 headers 传递 access token

```python title="OAuth2 认证的 MCP 服务器
# 在应用中完成 OAuth 流程后
access_token = await get_access_token_from_oauth_flow()

options = ClaudeAgentOptions(
    mcp_servers={
        "oauth-api": {
            "type": "http",
            "url": "https://api.example.com/mcp",
            "headers": {"Authorization": f"Bearer {access_token}"},
        }
    },
    allowed_tools=["mcp__oauth-api__*"],
)
```

### GitHub Issues 示例

下面的示例连接 GitHub MCP 服务器来列出最近的 issues，包含调试日志验证 MCP 连接和工具调用：

```python title="列出 GitHub Issues"
import asyncio
import os
from claude_agent_sdk import (
    query,
    ClaudeAgentOptions,
    ResultMessage,
    SystemMessage,
    AssistantMessage,
)


async def main():
    options = ClaudeAgentOptions(
        mcp_servers={
            "github": {
                "command": "npx",
                "args": ["-y", "@modelcontextprotocol/server-github"],
                "env": {"GITHUB_TOKEN": os.environ["GITHUB_TOKEN"]},
            }
        },
        allowed_tools=["mcp__github__list_issues"],
    )

    async for message in query(
        prompt="列出 anthropics/claude-code 中最近的 3 个 issue",
        options=options,
    ):
        # 验证 MCP 服务器是否连接成功
        if isinstance(message, SystemMessage) and message.subtype == "init":
            print("MCP servers:", message.data.get("mcp_servers"))

        # 记录 Claude 何时调用 MCP 工具
        if isinstance(message, AssistantMessage):
            for block in message.content:
                if hasattr(block, "name") and block.name.startswith("mcp__"):
                    print("MCP tool called:", block.name)

        # 打印最终结果
        if isinstance(message, ResultMessage) and message.subtype == "success":
            print(message.result)


asyncio.run(main())
```

### 数据库查询示例

使用 Postgres MCP 服务器查询数据库。连接字符串作为参数传递给服务器，Agent 自动发现数据库 schema、编写 SQL 并返回结果：

```python title="自然语言查询数据库"
import asyncio
import os
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage


async def main():
    connection_string = os.environ["DATABASE_URL"]

    options = ClaudeAgentOptions(
        mcp_servers={
            "postgres": {
                "command": "npx",
                "args": [
                    "-y",
                    "@modelcontextprotocol/server-postgres",
                    connection_string,
                ],
            }
        },
        # 只允许读查询，不允许写
        allowed_tools=["mcp__postgres__query"],
    )

    # 自然语言查询 — Claude 自动编写 SQL
    async for message in query(
        prompt="上周有多少用户注册？按天分组。",
        options=options,
    ):
        if isinstance(message, ResultMessage) and message.subtype == "success":
            print(message.result)


asyncio.run(main())
```

### 错误处理

MCP 服务器可能因多种原因连接失败——进程未安装、凭据无效、远程服务器不可达等。

SDK 在每次 `query` 开始时产出一个 `subtype` 为 `"init"` 的 `system` 消息，其中包含每个 MCP 服务器的连接状态。在 Agent 开始工作前检查 `status` 字段可以提前发现连接失败：

```python title="检查 MCP 服务器连接状态"
from claude_agent_sdk import query, ClaudeAgentOptions, SystemMessage, ResultMessage


async for message in query(prompt="处理数据", options=options):
    if isinstance(message, SystemMessage) and message.subtype == "init":
        failed_servers = [
            s
            for s in message.data.get("mcp_servers", [])
            if s.get("status") != "connected"
        ]

        if failed_servers:
            print(f"连接失败: {failed_servers}")

    if isinstance(message, ResultMessage) and message.subtype == "error_during_execution":
        print("执行失败")
```

常见失败原因：

- **缺少环境变量**：检查 `env` 字段是否与服务器预期匹配
- **服务器未安装**：对于 `npx` 命令，验证包是否存在且 Node.js 在 PATH 中
- **连接字符串无效**：对于数据库服务器，验证格式和数据库可访问性
- **网络问题**：对于远程 HTTP/SSE 服务器，检查 URL 可达性和防火墙设置
- **连接超时**：MCP SDK 默认 60 秒超时，如果服务器启动较慢可以考虑预热

## Tool Search

### 为什么需要 Tool Search？

当你的 Agent 连接了很多 MCP 服务器时，工具定义会占据大量上下文窗口。50 个工具可能消耗 10-20K token，留给实际工作的空间就少了。而且工具选择准确率在加载超过 30-50 个工具时会明显下降。

Tool Search 通过按需发现和加载工具来解决这两个问题——不在上下文中预加载所有工具定义，而是让 Agent 在需要时搜索并加载相关的工具。

### 工作原理

当 Tool Search 激活时，工具定义不会进入上下文窗口。Agent 收到可用工具的摘要，在需要某个能力时搜索相关工具，加载最相关的 3-5 个到上下文中。这些工具在后续 turn 中保持可用。如果对话足够长，SDK 压缩了早期消息，之前发现的工具可能被移除，Agent 会再次搜索。

Tool Search 在首次发现工具时增加一次额外的往返（搜索步骤），但对于大型工具集，每个 turn 的上下文更小，总体上更优。工具少于约 10 个时，预加载通常更快。

> Tool Search 需要 Claude Sonnet 4 或更高版本、Claude Opus 4 或更高版本。Haiku 模型不支持。

### 配置 Tool Search

通过 `ENABLE_TOOL_SEARCH` 环境变量配置，设置在 `query()` 的 `env` 选项中：

| 值 | 行为 |
|------|------|
| （未设置） | Tool Search 始终开启。工具定义从不进入上下文。这是默认值 |
| `true` | 与未设置相同 |
| `auto` | 检查所有工具定义的 token 总量是否超过模型上下文窗口的 10%，超过则激活 |
| `auto:N` | 与 `auto` 相同但使用自定义百分比。`auto:5` 表示超过 5% 时激活 |
| `false` | 关闭 Tool Search。所有工具定义在每个 turn 中加载到上下文 |

```python title="使用 auto:5 配置 Tool Search"
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage

options = ClaudeAgentOptions(
    mcp_servers={
        "enterprise-tools": {
            "type": "http",
            "url": "https://tools.example.com/mcp",
        }
    },
    allowed_tools=["mcp__enterprise-tools__*"],  # 通配符预批准该服务器所有工具
    env={
        "ENABLE_TOOL_SEARCH": "auto:5"  # 工具定义超过上下文 5% 时激活
    },
)

async for message in query(
    prompt="查找并运行适当的数据库查询",
    options=options,
):
    if isinstance(message, ResultMessage) and message.subtype == "success":
        print(message.result)
```

```typescript title="使用 auto:5 配置 Tool Search"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "查找并运行适当的数据库查询",
  options: {
    mcpServers: {
      "enterprise-tools": {
        type: "http",
        url: "https://tools.example.com/mcp"
      }
    },
    allowedTools: ["mcp__enterprise-tools__*"],
    env: {
      ENABLE_TOOL_SEARCH: "auto:5" // 工具定义超过上下文 5% 时激活
    }
  }
})) {
  if (message.type === "result" && message.subtype === "success") {
    console.log(message.result);
  }
}
```

### 优化工具发现

搜索机制通过匹配查询与工具名称和描述来工作。好的命名和描述能让工具更容易被找到：

- **名称**：`search_slack_messages` 比 `query_slack` 能匹配更广范围的请求
- **描述**：`"Search Slack messages by keyword, channel, or date range"` 比 `"Query Slack"` 能匹配更多查询

你也可以在系统提示中添加一段列出可用工具类别的信息，让 Agent 知道有哪些工具可以搜索：

```text title="在系统提示中列出工具类别
You can search for tools to interact with Slack, GitHub, and Jira.
```

### Tool Search 的限制

| 限制项 | 值 |
|--------|---|
| 最大工具数 | 10,000 个 |
| 每次搜索返回 | 3-5 个最相关工具 |
| 支持的模型 | Claude Sonnet 4 及更高、Claude Opus 4 及更高（不支持 Haiku） |
