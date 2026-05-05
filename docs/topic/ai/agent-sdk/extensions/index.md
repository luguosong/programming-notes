---
title: 扩展机制
description: Skills、Subagents、Hooks、Plugins 四大扩展方式
---

Agent SDK 提供了四种扩展 Agent 能力的方式：**Skills** 让 Claude 自主执行特定任务、**Subagents** 委派子任务给专门的 Agent、**Hooks** 在关键节点拦截和控制 Agent 行为、**Plugins** 打包并分发完整的扩展套件。它们各有适用场景，合理组合可以构建出能力丰富的 Agent 应用。

**本文你会学到**：

- Skills 的定义、自动发现和使用方式
- Subagents 的三种创建方式和上下文隔离机制
- Hooks 的事件系统、匹配器和回调模式
- Plugins 的加载方式和目录结构
- 什么时候用哪种扩展

## Skills

### 为什么需要 Skills

当你的 Agent 需要执行特定领域的任务时（比如处理 PDF、生成数据库迁移脚本），把所有指令塞进系统提示会让 prompt 臃肿且难以维护。Skills 让你把专门能力封装成独立的 `SKILL.md` 文件，Claude 根据上下文自主决定何时使用。

### Skills 如何工作

Skills 的工作流程分为五步：

1. **定义为文件系统产物**——在特定目录创建 `SKILL.md` 文件
2. **从文件系统加载**——根据 `settingSources` / `setting_sources` 配置发现 Skills
3. **自动发现**——SDK 启动时从用户和项目目录发现 Skills 元数据，触发时加载完整内容
4. **模型自主调用**——Claude 根据上下文判断是否使用，也可以通过 `/skill-name` 手动调用
5. **通过 `allowed_tools` 启用**——在 `allowed_tools` 中加入 `"Skill"` 才能使用 Skills

### 启用 Skills

```python title="启用 Skills"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions


async def main():
    options = ClaudeAgentOptions(
        cwd="/path/to/project",  # 包含 .claude/skills/ 的项目目录
        setting_sources=["user", "project"],  # 从文件系统加载 Skills
        allowed_tools=["Skill", "Read", "Write", "Bash"],  # 启用 Skill 工具
    )

    async for message in query(
        prompt="Help me process this PDF document", options=options
    ):
        print(message)


asyncio.run(main())
```

```typescript title="启用 Skills"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "Help me process this PDF document",
  options: {
    cwd: "/path/to/project", // 包含 .claude/skills/ 的项目目录
    settingSources: ["user", "project"], // 从文件系统加载 Skills
    allowedTools: ["Skill", "Read", "Write", "Bash"] // 启用 Skill 工具
  }
})) {
  console.log(message);
}
```

### Skills 存放位置

| 位置 | 路径 | 加载条件 | 用途 |
|------|------|----------|------|
| 项目 Skills | `.claude/skills/` | `setting_sources` 包含 `"project"` | 团队共享，提交 git |
| 用户 Skills | `~/.claude/skills/` | `setting_sources` 包含 `"user"` | 个人 Skills，所有项目通用 |
| Plugin Skills | 插件目录内 | 通过 `plugins` 选项加载 | 随插件分发 |

### 创建 Skills

每个 Skill 是一个包含 `SKILL.md` 的目录，`SKILL.md` 中有 YAML frontmatter（定义元数据）和 Markdown 正文（定义具体指令）：

```bash
# 目录结构
.claude/skills/processing-pdfs/
└── SKILL.md
```

`description` 字段决定了 Claude 何时调用你的 Skill——写得越清晰具体，匹配越准确。

### 工具访问控制

`SKILL.md` 中的 `allowed-tools` frontmatter 字段**仅在 Claude Code CLI 中生效**，通过 SDK 使用时不适用。SDK 中通过主配置的 `allowedTools` 统一控制工具访问：

```python title="SDK 中控制 Skills 的工具访问"
options = ClaudeAgentOptions(
    setting_sources=["user", "project"],
    allowed_tools=["Skill", "Read", "Grep", "Glob"],  # 只允许只读工具
)

async for message in query(prompt="Analyze the codebase structure", options=options):
    print(message)
```

### 常见问题排查

**Skills 未找到**：检查 `setting_sources` 是否包含 `"user"` 或 `"project"`；检查 `cwd` 是否指向包含 `.claude/skills/` 的目录。

**Skill 未被使用**：确认 `"Skill"` 在 `allowed_tools` 中；检查 `SKILL.md` 的 `description` 是否足够清晰，包含相关关键词。

## Subagents

### 为什么需要 Subagents

当一个任务涉及多个子领域时，全部塞进一个 Agent 的上下文会导致 prompt 膨胀、工具误用。Subagents 让你把任务委派给专门的子 Agent——每个子 Agent 拥有独立的上下文、专门的指令和受限的工具集。

### 三种创建方式

| 方式 | 说明 | 适用场景 |
|------|------|----------|
| **程序化定义**（推荐） | 在代码中通过 `agents` 参数定义 | SDK 应用、动态配置 |
| **文件系统定义** | 在 `.claude/agents/` 下创建 Markdown 文件 | 与 Claude Code CLI 共享配置 |
| **内置通用型** | 无需定义，Claude 自动使用 `general-purpose` 子 Agent | 快速探索和研究任务 |

程序化定义的 Agent 优先级高于同名的文件系统 Agent。

### 程序化定义 Subagents

通过 `agents` 参数定义，`Agent` 工具必须在 `allowed_tools` 中（因为 Claude 通过 Agent 工具调用子 Agent）：

```python title="定义和使用 Subagents"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, AgentDefinition


async def main():
    async for message in query(
        prompt="Review the authentication module for security issues",
        options=ClaudeAgentOptions(
            # Agent 工具是调用子 Agent 的必需条件
            allowed_tools=["Read", "Grep", "Glob", "Agent"],
            agents={
                "code-reviewer": AgentDefinition(
                    # description 告诉 Claude 何时使用这个子 Agent
                    description="Expert code review specialist. Use for quality, security, and maintainability reviews.",
                    # prompt 定义子 Agent 的行为和专业能力
                    prompt="""You are a code review specialist with expertise in security, performance, and best practices.

When reviewing code:
- Identify security vulnerabilities
- Check for performance issues
- Verify adherence to coding standards
- Suggest specific improvements

Be thorough but concise in your feedback.""",
                    # tools 限制子 Agent 只能使用指定工具（此处为只读）
                    tools=["Read", "Grep", "Glob"],
                    # model 覆盖子 Agent 使用的模型
                    model="sonnet",
                ),
                "test-runner": AgentDefinition(
                    description="Runs and analyzes test suites. Use for test execution and coverage analysis.",
                    prompt="""You are a test execution specialist. Run tests and provide clear analysis of results.

Focus on:
- Running test commands
- Analyzing test output
- Identifying failing tests
- Suggesting fixes for failures""",
                    # Bash 访问让子 Agent 能运行测试命令
                    tools=["Bash", "Read", "Grep"],
                ),
            },
        ),
    ):
        if hasattr(message, "result"):
            print(message.result)


asyncio.run(main())
```

```typescript title="定义和使用 Subagents"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "Review the authentication module for security issues",
  options: {
    // Agent 工具是调用子 Agent 的必需条件
    allowedTools: ["Read", "Grep", "Glob", "Agent"],
    agents: {
      "code-reviewer": {
        description:
          "Expert code review specialist. Use for quality, security, and maintainability reviews.",
        prompt: `You are a code review specialist with expertise in security, performance, and best practices.

When reviewing code:
- Identify security vulnerabilities
- Check for performance issues
- Verify adherence to coding standards
- Suggest specific improvements

Be thorough but concise in your feedback.`,
        tools: ["Read", "Grep", "Glob"],
        model: "sonnet"
      },
      "test-runner": {
        description:
          "Runs and analyzes test suites. Use for test execution and coverage analysis.",
        prompt: `You are a test execution specialist. Run tests and provide clear analysis of results.

Focus on:
- Running test commands
- Analyzing test output
- Identifying failing tests
- Suggesting fixes for failures`,
        tools: ["Bash", "Read", "Grep"]
      }
    }
  }
})) {
  if ("result" in message) console.log(message.result);
}
```

### AgentDefinition 配置参考

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `description` | `string` | 是 | 自然语言描述，告诉 Claude 何时使用这个子 Agent |
| `prompt` | `string` | 是 | 子 Agent 的系统提示，定义其角色和行为 |
| `tools` | `string[]` | 否 | 允许的工具列表，省略则继承所有工具 |
| `disallowedTools` | `string[]` | 否 | 从工具集中移除的工具列表 |
| `model` | `string` | 否 | 模型覆盖，接受别名（`sonnet`、`opus`、`haiku`）或完整模型 ID |
| `skills` | `string[]` | 否 | 该子 Agent 可用的 Skill 名称列表 |
| `memory` | `'user' \| 'project' \| 'local'` | 否 | Memory 来源 |
| `mcpServers` | `(string \| object)[]` | 否 | 可用的 MCP 服务器 |
| `maxTurns` | `number` | 否 | 最大循环次数 |
| `background` | `boolean` | 否 | 是否作为非阻塞后台任务运行 |
| `effort` | `'low' \| 'medium' \| 'high' \| 'xhigh' \| 'max' \| number` | 否 | 推理投入程度 |
| `permissionMode` | `PermissionMode` | 否 | 工具执行的权限模式 |

### 上下文继承规则

子 Agent 的上下文窗口是全新的（不包含父 Agent 的对话历史），但并非完全空白：

| 子 Agent 收到的 | 子 Agent 收不到的 |
|-----------------|-------------------|
| 自己的系统提示和 Agent 工具的 prompt | 父 Agent 的对话历史或工具结果 |
| 项目 `CLAUDE.md`（通过 `settingSources` 加载） | Skills（除非在 `AgentDefinition.skills` 中列出） |
| 工具定义（继承父 Agent 或 `tools` 子集） | 父 Agent 的系统提示 |

父子 Agent 之间的唯一通信通道是 Agent 工具的 prompt 字符串——如果你希望子 Agent 知道文件路径、错误消息等上下文，必须在 prompt 中显式包含。

### 工具限制

通过 `tools` 字段限制子 Agent 的能力，这是安全防护的核心手段：

| 用例 | 工具组合 | 说明 |
|------|----------|------|
| 只读分析 | `Read`、`Grep`、`Glob` | 可检查代码但不能修改或执行 |
| 测试执行 | `Bash`、`Read`、`Grep` | 可运行命令并分析输出 |
| 代码修改 | `Read`、`Edit`、`Write`、`Grep`、`Glob` | 完整读写但没有命令执行权限 |
| 完全访问 | 所有工具 | 省略 `tools` 字段即继承父 Agent 全部工具 |

### 调用和检测子 Agent

**自动调用**：Claude 根据 `description` 字段自动判断是否委派任务给子 Agent。

**显式调用**：在 prompt 中直接指定名称：

```text
Use the code-reviewer agent to check the authentication module
```

**检测子 Agent 调用**：检查 `tool_use` 块中 `name` 为 `"Agent"`（或旧版 `"Task"`）的工具调用，以及 `parent_tool_use_id` 字段：

```python title="检测子 Agent 调用"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, AgentDefinition


async def main():
    async for message in query(
        prompt="Use the code-reviewer agent to review this codebase",
        options=ClaudeAgentOptions(
            allowed_tools=["Read", "Glob", "Grep", "Agent"],
            agents={
                "code-reviewer": AgentDefinition(
                    description="Expert code reviewer.",
                    prompt="Analyze code quality and suggest improvements.",
                    tools=["Read", "Glob", "Grep"],
                )
            },
        ),
    ):
        # 检查子 Agent 调用（兼容旧版 "Task" 和新版 "Agent"）
        if hasattr(message, "content") and message.content:
            for block in message.content:
                if getattr(block, "type", None) == "tool_use" and block.name in ("Task", "Agent"):
                    print(f"Subagent invoked: {block.input.get('subagent_type')}")

        # 检查消息是否来自子 Agent 上下文
        if hasattr(message, "parent_tool_use_id") and message.parent_tool_use_id:
            print("  (running inside subagent)")


asyncio.run(main())
```

### 恢复子 Agent

子 Agent 可以恢复以继续之前的工作。恢复时子 Agent 保留完整的对话历史：

1. 第一次查询时捕获 `session_id`
2. 从消息内容中提取 `agentId`
3. 第二次查询时传入 `resume: sessionId` 并在 prompt 中引用 agent ID

```python title="恢复子 Agent"
import asyncio
import json
import re
from claude_agent_sdk import query, ClaudeAgentOptions


def extract_agent_id(text: str) -> str | None:
    """从 Agent 工具结果文本中提取 agentId。"""
    match = re.search(r"agentId:\s*([a-f0-9-]+)", text)
    return match.group(1) if match else None


async def main():
    agent_id = None
    session_id = None

    # 第一次调用——使用 Explore 子 Agent
    async for message in query(
        prompt="Use the Explore agent to find all API endpoints in this codebase",
        options=ClaudeAgentOptions(allowed_tools=["Read", "Grep", "Glob", "Agent"]),
    ):
        if hasattr(message, "session_id"):
            session_id = message.session_id
        if hasattr(message, "content"):
            content_str = json.dumps(message.content, default=str)
            extracted = extract_agent_id(content_str)
            if extracted:
                agent_id = extracted
        if hasattr(message, "result"):
            print(message.result)

    # 第二次调用——恢复会话，继续追问
    if agent_id and session_id:
        async for message in query(
            prompt=f"Resume agent {agent_id} and list the top 3 most complex endpoints",
            options=ClaudeAgentOptions(
                allowed_tools=["Read", "Grep", "Glob", "Agent"], resume=session_id
            ),
        ):
            if hasattr(message, "result"):
                print(message.result)


asyncio.run(main())
```

## Hooks

### 为什么需要 Hooks

你需要在不修改 Agent 核心逻辑的情况下控制其行为——比如阻止对 `.env` 文件的写入、记录所有工具调用、自动批准只读操作。Hooks 就是在 Agent 执行的关键节点运行的回调函数，让你拦截、修改或观察 Agent 行为。

### Hooks 如何工作

Hooks 的执行流程分为五步：

1. **事件触发**——Agent 执行过程中发生特定事件（工具即将被调用、工具返回结果、子 Agent 启动等）
2. **SDK 收集已注册的 Hooks**——包括你在 `options.hooks` 中传入的回调 Hooks 和设置文件中的 shell 命令 Hooks
3. **匹配器过滤**——如果 Hook 配置了 `matcher` 正则模式，SDK 针对事件目标（如工具名称）进行匹配
4. **回调函数执行**——每个匹配的 Hook 接收事件详细信息作为输入
5. **返回决策**——你的回调返回输出对象，告诉 Agent 允许、阻止或修改操作

### 可用的 Hook 事件

SDK 提供了丰富的 Hook 事件，覆盖 Agent 生命周期的各个阶段：

| Hook 事件 | Python | TypeScript | 触发条件 | 典型用途 |
|-----------|--------|------------|----------|----------|
| `PreToolUse` | 是 | 是 | 工具调用请求（可阻止或修改） | 阻止危险命令 |
| `PostToolUse` | 是 | 是 | 工具执行完成 | 记录审计日志 |
| `PostToolUseFailure` | 是 | 是 | 工具执行失败 | 处理工具错误 |
| `UserPromptSubmit` | 是 | 是 | 用户提示提交 | 注入额外上下文 |
| `Stop` | 是 | 是 | Agent 执行停止 | 保存会话状态 |
| `SubagentStart` | 是 | 是 | 子 Agent 初始化 | 跟踪并行任务 |
| `SubagentStop` | 是 | 是 | 子 Agent 完成 | 聚合并行结果 |
| `PreCompact` | 是 | 是 | 对话压缩请求 | 压缩前存档完整记录 |
| `PermissionRequest` | 是 | 是 | 权限对话将显示 | 自定义权限处理 |
| `Notification` | 是 | 是 | Agent 状态消息 | 转发到 Slack/PagerDuty |
| `PostToolBatch` | 否 | 是 | 一整批工具调用解决 | 注入批次约定 |
| `SessionStart` | 否 | 是 | 会话初始化 | 初始化日志 |
| `SessionEnd` | 否 | 是 | 会话终止 | 清理资源 |
| `TaskCompleted` | 否 | 是 | 后台任务完成 | 聚合结果 |

### 匹配器

匹配器用正则表达式过滤 Hook 的触发条件。对于工具类 Hook，匹配器按工具名称过滤：

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `matcher` | `string` | `undefined` | 按工具名称匹配的正则。省略则匹配所有 |
| `hooks` | `HookCallback[]` | - | 匹配时执行的回调函数数组 |
| `timeout` | `number` | `60` | 超时时间（秒） |

```python title="匹配器示例"
options = ClaudeAgentOptions(
    hooks={
        "PreToolUse": [
            # 只匹配文件修改工具
            HookMatcher(matcher="Write|Edit", hooks=[file_security_hook]),
            # 匹配所有 MCP 工具
            HookMatcher(matcher="^mcp__", hooks=[mcp_audit_hook]),
            # 无匹配器——对所有 PreToolUse 事件触发
            HookMatcher(hooks=[global_logger]),
        ]
    }
)
```

```typescript title="匹配器示例"
const options = {
  hooks: {
    PreToolUse: [
      { matcher: "Write|Edit", hooks: [fileSecurityHook] }, // 只匹配文件修改工具
      { matcher: "^mcp__", hooks: [mcpAuditHook] }, // 匹配所有 MCP 工具
      { hooks: [globalLogger] } // 无匹配器——对所有 PreToolUse 事件触发
    ]
  }
};
```

注意：匹配器只按**工具名称**过滤，不按文件路径或其他参数。要按文件路径过滤，在回调内部检查 `tool_input.file_path`。

### 回调输入和输出

每个 Hook 回调接收三个参数：

- **输入数据**——包含事件详情的类型化对象（所有 Hook 共享 `session_id`、`cwd`、`hook_event_name`）
- **工具使用 ID**——用于关联同一工具调用的 `PreToolUse` 和 `PostToolUse` 事件
- **上下文**——TypeScript 中包含 `AbortSignal`，Python 中保留供将来使用

回调返回的输出对象分两类字段：

- **顶级字段**——`systemMessage`（注入对话上下文）、`continue`（控制 Agent 是否继续）
- **`hookSpecificOutput`**——控制当前操作，字段取决于 Hook 类型。`PreToolUse` 中可设置 `permissionDecision`（`allow`、`deny`、`ask`）、`permissionDecisionReason` 和 `updatedInput`

返回 `{}` 表示不做任何修改，允许操作继续。

### 阻止工具调用

下面的示例拦截 `Write` 和 `Edit` 工具调用，阻止对 `.env` 文件的修改：

```python title="阻止特定工具调用"
import asyncio
from claude_agent_sdk import (
    AssistantMessage,
    ClaudeSDKClient,
    ClaudeAgentOptions,
    HookMatcher,
    ResultMessage,
)


async def protect_env_files(input_data, tool_use_id, context):
    # 从工具输入参数中提取文件路径
    file_path = input_data["tool_input"].get("file_path", "")
    file_name = file_path.split("/")[-1]

    # 如果针对 .env 文件，阻止操作
    if file_name == ".env":
        return {
            "hookSpecificOutput": {
                "hookEventName": input_data["hook_event_name"],
                "permissionDecision": "deny",
                "permissionDecisionReason": "Cannot modify .env files",
            }
        }

    return {}


async def main():
    options = ClaudeAgentOptions(
        hooks={
            "PreToolUse": [HookMatcher(matcher="Write|Edit", hooks=[protect_env_files])]
        }
    )

    async with ClaudeSDKClient(options=options) as client:
        await client.query("Update the database configuration")
        async for message in client.receive_response():
            if isinstance(message, (AssistantMessage, ResultMessage)):
                print(message)


asyncio.run(main())
```

```typescript title="阻止特定工具调用"
import { query, HookCallback, PreToolUseHookInput } from "@anthropic-ai/claude-agent-sdk";

const protectEnvFiles: HookCallback = async (input, toolUseID, { signal }) => {
  const preInput = input as PreToolUseHookInput;
  const toolInput = preInput.tool_input as Record<string, unknown>;
  const filePath = toolInput?.file_path as string;
  const fileName = filePath?.split("/").pop();

  if (fileName === ".env") {
    return {
      hookSpecificOutput: {
        hookEventName: preInput.hook_event_name,
        permissionDecision: "deny",
        permissionDecisionReason: "Cannot modify .env files"
      }
    };
  }
  return {};
};

for await (const message of query({
  prompt: "Update the database configuration",
  options: {
    hooks: {
      PreToolUse: [{ matcher: "Write|Edit", hooks: [protectEnvFiles] }]
    }
  }
})) {
  if (message.type === "assistant" || message.type === "result") {
    console.log(message);
  }
}
```

### 修改工具输入

通过 `updatedInput` 修改工具的输入参数。下面的示例拦截 `Write` 工具，将所有文件路径重定向到 `/sandbox` 目录：

```python title="修改工具输入"
async def redirect_to_sandbox(input_data, tool_use_id, context):
    if input_data["hook_event_name"] != "PreToolUse":
        return {}

    if input_data["tool_name"] == "Write":
        original_path = input_data["tool_input"].get("file_path", "")
        return {
            "hookSpecificOutput": {
                "hookEventName": input_data["hook_event_name"],
                "permissionDecision": "allow",
                "updatedInput": {
                    **input_data["tool_input"],
                    "file_path": f"/sandbox{original_path}",
                },
            }
        }
    return {}
```

使用 `updatedInput` 时必须同时返回 `permissionDecision: 'allow'`，且始终返回新对象而非修改原始 `tool_input`。

### 自动批准工具

通过 `permissionDecision: 'allow'` 自动批准特定工具，让它们无需用户确认即可执行：

```python title="自动批准只读工具"
async def auto_approve_read_only(input_data, tool_use_id, context):
    if input_data["hook_event_name"] != "PreToolUse":
        return {}

    read_only_tools = ["Read", "Glob", "Grep"]
    if input_data["tool_name"] in read_only_tools:
        return {
            "hookSpecificOutput": {
                "hookEventName": input_data["hook_event_name"],
                "permissionDecision": "allow",
                "permissionDecisionReason": "Read-only tool auto-approved",
            }
        }
    return {}
```

### 异步 Hooks

如果 Hook 只需要执行副作用（日志、发送 webhook）而不影响 Agent 行为，可以返回异步输出让 Agent 不必等待：

```python title="异步 Hook"
async def async_hook(input_data, tool_use_id, context):
    # 启动后台任务，然后立即返回
    asyncio.create_task(send_to_logging_service(input_data))
    return {"async_": True, "asyncTimeout": 30000}
```

异步 Hook 无法阻止、修改或注入上下文——因为 Agent 已经继续执行。

### 链接多个 Hooks

Hooks 按数组中的顺序依次执行，保持每个 Hook 职责单一：

```python title="链接多个 Hooks"
options = ClaudeAgentOptions(
    hooks={
        "PreToolUse": [
            HookMatcher(hooks=[rate_limiter]),      # 首先检查速率限制
            HookMatcher(hooks=[authorization_check]), # 其次验证权限
            HookMatcher(hooks=[input_sanitizer]),     # 第三清理输入
            HookMatcher(hooks=[audit_logger]),        # 最后记录操作
        ]
    }
)
```

当多个 Hooks 同时适用时，**deny** 优先于 **ask**，**ask** 优先于 **allow**——只要有任何 Hook 返回 `deny`，操作就会被阻止。

## Plugins

### 为什么需要 Plugins

当你需要把 Skills、Agents、Hooks 和 MCP 服务器打包成一个可复用、可分发的单元时，Plugins 就是答案。它让团队共享扩展变得简单——一个 Plugin 目录包含了所有相关配置。

### Plugin 结构

```text
my-plugin/
├── .claude-plugin/
│   └── plugin.json          # 必需：Plugin 清单
├── skills/                   # Agent Skills（自主调用或 /skill-name）
│   └── my-skill/
│       └── SKILL.md
├── agents/                   # 自定义 Agents
│   └── specialist.md
├── hooks/                    # 事件处理器
│   └── hooks.json
└── .mcp.json                # MCP 服务器定义
```

Plugin 目录必须包含 `.claude-plugin/plugin.json` 清单文件。

### 加载 Plugins

在 `options.plugins` 中提供本地文件系统路径。`type` 字段必须是 `"local"`：

```python title="加载 Plugins"
import asyncio
from claude_agent_sdk import query


async def main():
    async for message in query(
        prompt="Hello",
        options={
            "plugins": [
                {"type": "local", "path": "./my-plugin"},
                {"type": "local", "path": "/absolute/path/to/another-plugin"},
            ]
        },
    ):
        pass


asyncio.run(main())
```

```typescript title="加载 Plugins"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "Hello",
  options: {
    plugins: [
      { type: "local", path: "./my-plugin" },
      { type: "local", path: "/absolute/path/to/another-plugin" }
    ]
  }
})) {
  // Plugin 的 commands、agents 等功能现在可用
}
```

路径可以是相对路径（相对于当前工作目录）或绝对路径，必须指向包含 `.claude-plugin/` 的 Plugin 根目录。

### 验证 Plugin 安装

Plugin 成功加载后会出现在系统初始化消息中：

```typescript title="验证 Plugin 安装"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "Hello",
  options: {
    plugins: [{ type: "local", path: "./my-plugin" }]
  }
})) {
  if (message.type === "system" && message.subtype === "init") {
    console.log("Plugins:", message.plugins);
    // [{ name: "my-plugin", path: "./my-plugin" }]
    console.log("Commands:", message.slash_commands);
    // ["/help", "/compact", "my-plugin:custom-command"]
  }
}
```

### 使用 Plugin Skills

Plugin 中的 Skills 自动以 Plugin 名称作为命名空间，调用格式为 `plugin-name:skill-name`：

```python title="使用 Plugin Skills"
import asyncio
from claude_agent_sdk import query, AssistantMessage, TextBlock


async def main():
    async for message in query(
        prompt="/demo-plugin:greet",  # 使用 Plugin 命名空间的 Skill
        options={"plugins": [{"type": "local", "path": "./plugins/demo-plugin"}]},
    ):
        if isinstance(message, AssistantMessage):
            for block in message.content:
                if isinstance(block, TextBlock):
                    print(f"Claude: {block.text}")


asyncio.run(main())
```

### Plugin 未加载的排查

如果 Plugin 没有出现在初始化消息中，检查以下几点：

1. **路径是否正确**——确保指向 Plugin 根目录（包含 `.claude-plugin/`）
2. **plugin.json 是否有效**——清单文件必须包含合法的 JSON 语法
3. **文件权限**——确保 Plugin 目录可读

## 选择指南

四种扩展机制各有侧重，选择时根据核心需求判断：

| 需求 | 推荐方式 | 原因 |
|------|----------|------|
| 封装可复用的专门能力 | Skills | Claude 自主调用，`SKILL.md` 易于维护 |
| 隔离上下文、并行处理子任务 | Subagents | 独立上下文、可限制工具、可并行运行 |
| 拦截和控制 Agent 行为 | Hooks | 在关键节点插入自定义逻辑，支持阻止和修改 |
| 打包分发完整扩展套件 | Plugins | 包含 Skills、Agents、Hooks、MCP 的完整单元 |

实际项目中，它们经常配合使用：

- 一个**Plugin**打包了你的团队扩展，其中包含多个 **Skills** 和一个安全审计 **Hook**
- 主 Agent 通过 **Subagents** 并行执行代码审查和测试运行，每个子 Agent 有独立的工具限制
- **Hooks** 在全局层面拦截危险操作、记录审计日志、自动批准只读工具
