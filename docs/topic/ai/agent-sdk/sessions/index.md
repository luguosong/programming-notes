---
title: 会话管理
description: 多轮对话、会话恢复与文件检查点
---

# 会话管理

你让 Agent 分析了一个认证模块，它花了三十秒读完所有相关文件、找出三个潜在问题。现在你想让它基于这些分析直接修复——如果重新来过，Agent 会再次读取所有文件、重新分析，浪费时间和 token。这就是会话管理要解决的问题：让 Agent 在多次交互之间保持上下文。

**本文你会学到**：

- 如何根据应用场景选择合适的会话管理方式
- Continue、Resume、Fork 的区别与适用场景
- 自动会话管理的使用方法
- 如何跨主机恢复会话
- 文件检查点（Checkpointing）的启用与回溯

## 会话管理方式对比

会话（Session）是 SDK 在 Agent 工作过程中积累的对话历史——包含你的提示、每个工具调用、每个工具结果和每次响应。SDK 会自动将对话历史写入磁盘，方便你之后恢复。

> 会话持久化的是**对话**，不是文件系统。如果你想快照和回退 Agent 对文件所做的更改，请参阅「文件检查点」部分。

根据你的应用场景，选择合适的会话管理方式：

| 场景 | 方案 |
|------|------|
| 一次性任务：单次提示，无后续 | 无需额外处理，一个 `query()` 搞定 |
| 同一进程内的多轮对话 | `ClaudeSDKClient`（Python）或 `continue: true`（TypeScript） |
| 进程重启后继续上次的对话 | `continue: true`（TypeScript）/ `continue_conversation=True`（Python） |
| 恢复某个特定的历史会话（而非最近的） | 捕获 session ID，传给 `resume` |
| 尝试不同方案而不丢失原始会话 | 使用 `fork` 分叉会话 |
| 无状态任务，不写磁盘（仅 TypeScript） | 设置 `persistSession: false` |

## Continue、Resume、Fork 的区别

这三个选项都是 `query()` 的参数字段，都用于恢复已有会话，但行为不同：

**Continue 和 Resume** 都会找到已有会话并追加内容，区别在于如何定位那个会话：

- **Continue** 自动找到当前目录下最近一次会话。你不需要跟踪任何 ID，适合一次只运行一个对话的应用
- **Resume** 接受一个特定的 session ID。你需要自己跟踪 ID，适合多会话（比如多用户应用中每个用户一个会话）或需要恢复到非最近会话的场景

**Fork** 不同：它创建一个新会话，以原始会话历史的副本作为起点。原始会话保持不变。适合你想尝试不同方向但保留回退选项的场景。

## 自动会话管理

### Python：ClaudeSDKClient

`ClaudeSDKClient` 在内部自动管理 session ID。每次调用 `client.query()` 都自动继续同一个会话，调用 `client.receive_response()` 来迭代当前查询的消息。Client 必须作为异步上下文管理器使用。

```python title="Python：ClaudeSDKClient 多轮对话"
import asyncio
from claude_agent_sdk import (
    ClaudeSDKClient,
    ClaudeAgentOptions,
    AssistantMessage,
    ResultMessage,
    TextBlock,
)


def print_response(message):
    """只打印人类可读的消息部分"""
    if isinstance(message, AssistantMessage):
        for block in message.content:
            if isinstance(block, TextBlock):
                print(block.text)
    elif isinstance(message, ResultMessage):
        cost = (
            f"${message.total_cost_usd:.4f}"
            if message.total_cost_usd is not None
            else "N/A"
        )
        print(f"[完成: {message.subtype}, 花费: {cost}]")


async def main():
    options = ClaudeAgentOptions(
        allowed_tools=["Read", "Edit", "Glob", "Grep"],
    )

    async with ClaudeSDKClient(options=options) as client:
        # 第一次查询：Client 内部自动捕获 session ID
        await client.query("分析认证模块")
        async for message in client.receive_response():
            print_response(message)

        # 第二次查询：自动继续同一个会话，无需传 ID
        await client.query("现在把它重构为使用 JWT")
        async for message in client.receive_response():
            print_response(message)


asyncio.run(main())
```

两个查询通过同一个 `client` 实例运行，第二次查询自动拥有第一次的完整上下文。

### TypeScript：continue: true

TypeScript 的稳定版 SDK（`query()` 函数）没有类似 Python `ClaudeSDKClient` 的会话持有对象。取而代之的是在每次后续 `query()` 调用中传入 `continue: true`，SDK 自动找到磁盘上最近的会话并恢复。

```typescript title="TypeScript：continue: true 多轮对话"
import { query } from "@anthropic-ai/claude-agent-sdk";

// 第一次查询：创建新会话
for await (const message of query({
  prompt: "分析认证模块",
  options: { allowedTools: ["Read", "Glob", "Grep"] }
})) {
  if (message.type === "result" && message.subtype === "success") {
    console.log(message.result);
  }
}

// 第二次查询：continue: true 自动恢复最近的会话
for await (const message of query({
  prompt: "现在把它重构为使用 JWT",
  options: {
    continue: true,
    allowedTools: ["Read", "Edit", "Write", "Glob", "Grep"]
  }
})) {
  if (message.type === "result" && message.subtype === "success") {
    console.log(message.result);
  }
}
```

## 使用 query() 的会话选项

### 捕获 Session ID

Resume 和 Fork 都需要 session ID。从 `ResultMessage` 的 `session_id` 字段读取——无论会话成功还是失败，此字段都会存在。在 TypeScript 中，ID 还可以更早地从 `SystemMessage` 直接读取。

```python title="Python：捕获 session ID"
import asyncio
from claude_agent_sdk import query, ClaudeAgentOptions, ResultMessage

async def main():
    session_id = None

    async for message in query(
        prompt="分析认证模块并建议改进",
        options=ClaudeAgentOptions(
            allowed_tools=["Read", "Glob", "Grep"],
        ),
    ):
        if isinstance(message, ResultMessage):
            session_id = message.session_id
            if message.subtype == "success":
                print(message.result)

    print(f"Session ID: {session_id}")
    return session_id


session_id = asyncio.run(main())
```

```typescript title="TypeScript：捕获 session ID"
import { query } from "@anthropic-ai/claude-agent-sdk";

let sessionId: string | undefined;

for await (const message of query({
  prompt: "分析认证模块并建议改进",
  options: { allowedTools: ["Read", "Glob", "Grep"] }
})) {
  if (message.type === "result") {
    sessionId = message.session_id;
    if (message.subtype === "success") {
      console.log(message.result);
    }
  }
}

console.log(`Session ID: ${sessionId}`);
```

### 按 ID 恢复会话

将 session ID 传给 `resume` 参数，Agent 会从该会话中断的地方继续，拥有完整的上下文。常见用途：

- **跟进已完成任务**：Agent 已分析完毕，现在基于分析结果直接行动，无需重新读取文件
- **从限制中恢复**：第一次运行因 `error_max_turns` 或 `error_max_budget_usd` 结束，用更高的限制恢复
- **重启进程后恢复**：关机前捕获了 ID，重启后恢复对话

```python title="Python：按 ID 恢复会话"
# 之前的会话已经做了分析，现在直接基于分析结果行动
async for message in query(
    prompt="现在实现你建议的重构方案",
    options=ClaudeAgentOptions(
        resume=session_id,
        allowed_tools=["Read", "Edit", "Write", "Glob", "Grep"],
    ),
):
    if isinstance(message, ResultMessage) and message.subtype == "success":
        print(message.result)
```

> 如果 `resume` 返回了一个全新的会话而非预期的历史上下文，最常见的原因是 `cwd` 不匹配。会话文件存储在 `~/.claude/projects/<encoded-cwd>/*.jsonl` 下，其中 `<encoded-cwd>` 是绝对工作目录中所有非字母数字字符被替换为 `-` 的结果。如果恢复调用运行在不同的目录，SDK 会在错误的位置查找。

### Fork 探索不同方案

Fork 创建一个新会话，以原始会话历史的副本作为起点，之后独立发展。Fork 获得自己的 session ID，原始会话的 ID 和历史保持不变。你最终拥有两个独立的会话，可以分别恢复。

```python title="Python：Fork 探索不同方案"
# Fork：从 session_id 分叉出新会话
forked_id = None
async for message in query(
    prompt="不用 JWT，改用 OAuth2 实现认证模块",
    options=ClaudeAgentOptions(
        resume=session_id,
        fork_session=True,
    ),
):
    if isinstance(message, ResultMessage):
        forked_id = message.session_id  # Fork 的新 ID，与 session_id 不同
        if message.subtype == "success":
            print(message.result)

print(f"分叉会话: {forked_id}")

# 原始会话完好无损，恢复它继续 JWT 方案
async for message in query(
    prompt="继续 JWT 方案",
    options=ClaudeAgentOptions(resume=session_id),
):
    if isinstance(message, ResultMessage) and message.subtype == "success":
        print(message.result)
```

> Fork 分叉的是**对话历史**，不是文件系统。如果分叉后的 Agent 编辑了文件，这些更改是真实的，同一目录下的所有会话都能看到。如果要同时分叉和回退文件更改，使用「文件检查点」。

## 跨主机恢复

会话文件是本机的。要在不同主机之间恢复会话（CI Worker、临时容器、Serverless），有两种方式：

- **迁移会话文件**：将 `~/.claude/projects/<encoded-cwd>/<session-id>.jsonl` 从第一次运行中持久化，并在新主机上恢复到相同路径后再调用 `resume`。`cwd` 必须匹配
- **不依赖会话恢复**：将需要的结果（分析输出、决策、文件 diff）捕获为应用状态，传入新会话的 prompt。这通常比到处搬运会话文件更健壮

两个 SDK 都提供了在磁盘上枚举会话和读取消息的函数：

| Python | TypeScript | 用途 |
|--------|-----------|------|
| `list_sessions()` | `listSessions()` | 枚举会话列表 |
| `get_session_messages()` | `getSessionMessages()` | 读取会话消息 |
| `get_session_info()` | `getSessionInfo()` | 查看会话元信息 |
| `rename_session()` | `renameSession()` | 重命名会话 |
| `tag_session()` | `tagSession()` | 为会话添加标签 |

这些函数适合构建自定义的会话选择器、清理逻辑或对话查看器。

## 文件检查点（Checkpointing）

### 为什么需要文件检查点？

Agent 在会话中会频繁修改文件——创建新文件、编辑已有文件、修改 Notebook 单元格。如果 Agent 的某次修改出了问题，你想回退到之前的状态怎么办？普通的 `git checkout` 可能不够——Agent 可能还没有提交，或者你只是想临时回退到某个中间状态看看。文件检查点就是为此设计的：它自动跟踪 Agent 通过 `Write`、`Edit`、`NotebookEdit` 工具所做的文件修改，让你可以回退到任意一个检查点。

> 只有通过 `Write`、`Edit`、`NotebookEdit` 工具的修改会被跟踪。通过 Bash 命令（如 `echo > file.txt` 或 `sed -i`）的修改不会被捕获。

### 启用检查点

两个关键配置：

| 配置项 | Python | TypeScript | 说明 |
|--------|--------|-----------|------|
| 启用检查点 | `enable_file_checkpointing=True` | `enableFileCheckpointing: true` | 跟踪文件变更以支持回退 |
| 接收检查点 UUID | `extra_args={"replay-user-messages": None}` | `extraArgs: { 'replay-user-messages': null }` | 在响应流中接收用户消息 UUID |

```python title="Python：启用检查点"
from claude_agent_sdk import ClaudeSDKClient, ClaudeAgentOptions

options = ClaudeAgentOptions(
    enable_file_checkpointing=True,
    permission_mode="acceptEdits",
    extra_args={"replay-user-messages": None},
)

async with ClaudeSDKClient(options) as client:
    await client.query("重构认证模块")
```

### 捕获检查点 UUID

启用 `replay-user-messages` 后，响应流中的每条用户消息都有一个 UUID，作为检查点标识。通常捕获第一个用户消息的 UUID 即可——回退到它将所有文件恢复到原始状态。

```python title="Python：捕获检查点 UUID 和 session ID"
checkpoint_id = None
session_id = None

async for message in client.receive_response():
    # 每条用户消息更新检查点（只保留最新的）
    if isinstance(message, UserMessage) and message.uuid:
        checkpoint_id = message.uuid
    # 从 ResultMessage 捕获 session ID
    if isinstance(message, ResultMessage):
        session_id = message.session_id
```

```typescript title="TypeScript：捕获检查点 UUID 和 session ID"
let checkpointId: string | undefined;
let sessionId: string | undefined;

for await (const message of response) {
  if (message.type === "user" && message.uuid) {
    checkpointId = message.uuid;
  }
  if ("session_id" in message) {
    sessionId = message.session_id;
  }
}
```

### 回退文件

回退分两种情况：**在流处理中立即回退**，以及**流结束后回退**。

流结束后回退需要先恢复会话，再用空 prompt 建立连接，然后调用 `rewind_files()` / `rewindFiles()`：

```python title="Python：流结束后回退文件"
async with ClaudeSDKClient(
    ClaudeAgentOptions(enable_file_checkpointing=True, resume=session_id)
) as client:
    await client.query("")  # 空 prompt 建立连接
    async for message in client.receive_response():
        await client.rewind_files(checkpoint_id)
        break
```

```typescript title="TypeScript：流结束后回退文件"
const rewindQuery = query({
  prompt: "",  // 空 prompt 建立连接
  options: { ...opts, resume: sessionId }
});

for await (const msg of rewindQuery) {
  await rewindQuery.rewindFiles(checkpointId);
  break;
}
```

如果你捕获了 session ID 和 checkpoint UUID，也可以通过 CLI 回退：

``` bash
claude -p --resume <session-id> --rewind-files <checkpoint-uuid>
```

### 常见模式

#### 在 risky 操作前保存检查点

在每次 Agent 回合前更新检查点，覆盖前一个。如果出问题立即回退并退出循环：

```python title="Python：在 risky 操作前保存检查点"
safe_checkpoint = None

async with ClaudeSDKClient(options) as client:
    await client.query("重构认证模块")

    async for message in client.receive_response():
        # 每次回合前更新检查点（只保留最新的）
        if isinstance(message, UserMessage) and message.uuid:
            safe_checkpoint = message.uuid

        # 根据你的逻辑决定是否回退
        # 例如：错误检测、验证失败、用户输入等
        if your_revert_condition and safe_checkpoint:
            await client.rewind_files(safe_checkpoint)
            break
```

#### 多个恢复点

如果 Agent 跨多个回合修改文件，你可能想回退到某个中间状态而非全部回退。将所有检查点 UUID 存入数组，之后选择任意一个恢复：

```python title="Python：多个恢复点"
from dataclasses import dataclass
from datetime import datetime


@dataclass
class Checkpoint:
    id: str
    description: str
    timestamp: datetime


checkpoints = []
session_id = None

async with ClaudeSDKClient(options) as client:
    await client.query("重构认证模块")

    async for message in client.receive_response():
        if isinstance(message, UserMessage) and message.uuid:
            checkpoints.append(
                Checkpoint(
                    id=message.uuid,
                    description=f"第 {len(checkpoints) + 1} 轮之后",
                    timestamp=datetime.now(),
                )
            )
        if isinstance(message, ResultMessage) and not session_id:
            session_id = message.session_id

# 之后：恢复到任意检查点
if checkpoints and session_id:
    target = checkpoints[0]  # 选择任意检查点
    async with ClaudeSDKClient(
        ClaudeAgentOptions(enable_file_checkpointing=True, resume=session_id)
    ) as client:
        await client.query("")
        async for message in client.receive_response():
            await client.rewind_files(target.id)
            break
    print(f"已回退到: {target.description}")
```

### 检查点的局限性

| 局限性 | 说明 |
|--------|------|
| 仅限 Write/Edit/NotebookEdit | Bash 命令的文件修改不会被跟踪 |
| 绑定会话 | 检查点与创建它的会话绑定 |
| 仅文件内容 | 创建、移动、删除目录不会被回退操作撤销 |
| 仅本地文件 | 远程或网络文件不会被跟踪 |
