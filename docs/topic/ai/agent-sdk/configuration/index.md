---
title: 配置与权限
description: 自定义系统提示词与精细的权限控制
---

# 配置与权限

当你用 Agent SDK 启动一个 Agent 时，Claude 的行为完全由系统提示词决定——它用什么语气回答、遵循什么代码规范、遇到文件冲突怎么处理，都写在里面。但默认的系统提示词是"最小化"的，只包含工具使用的基础指令，不包含编码风格、项目约定等内容。与此同时，Claude 拥有读写文件、执行 Shell 命令的能力，你可能不希望它在生产环境中随意操作。

**本文你会学到**：

- 四种自定义系统提示词的方式及其适用场景
- 权限评估的完整流程（从 Hooks 到 `canUseTool`）
- 六种权限模式的区别与动态切换方法
- 声明式 Allow/Deny 规则的配置

## 自定义系统提示词

### 为什么需要自定义系统提示词？

假设你希望 Agent 生成的代码始终使用 TypeScript 严格模式、所有公共 API 都带 JSDoc 注释、提交前自动运行 lint——这些期望如果不告诉 Claude，它只会按自己的通用风格输出。系统提示词就是你告诉 Claude"怎么做事"的地方。

Agent SDK 默认使用**最小系统提示词**，只包含工具使用的基础指令。如果你需要 Claude Code 完整的系统提示词（编码规范、响应风格、安全策略等），需要显式指定 `systemPrompt: { type: "preset", preset: "claude_code" }`。

### 四种方式对比

Agent SDK 提供了四种自定义系统提示词的方式，各有适用场景：

| 特性 | `CLAUDE.md` | 输出风格 | `systemPrompt` + `append` | 自定义 `systemPrompt` |
|------|------------|---------|--------------------------|---------------------|
| 持久性 | 项目级文件 | 保存为文件 | 仅当次会话 | 仅当次会话 |
| 可复用性 | 仅限当前项目 | 跨项目复用 | 需在代码中重复 | 需在代码中重复 |
| 管理方式 | 文件系统 | CLI + 文件 | 代码中管理 | 代码中管理 |
| 默认工具指令 | 保留 | 保留 | 保留 | 丢失（除非手动包含） |
| 内置安全策略 | 维持 | 维持 | 维持 | 需自行添加 |
| 环境上下文 | 自动获取 | 自动获取 | 自动获取 | 需自行提供 |
| 定制程度 | 仅追加 | 替换默认风格 | 仅追加 | 完全控制 |
| 版本控制 | 随项目提交 | 可提交 | 随代码提交 | 随代码提交 |
| 作用范围 | 项目级别 | 用户或项目级别 | 当次会话 | 当次会话 |

### 通过 CLAUDE.md 注入项目规范

`CLAUDE.md` 是最"无感"的方式——Agent 运行在项目目录下时自动发现并加载，不需要改代码。

```markdown title="CLAUDE.md 示例"
# 项目规范

## 代码风格

- 使用 TypeScript 严格模式
- 优先使用函数式组件
- 公共 API 必须包含 JSDoc 注释

## 测试

- 提交前运行 `npm test`
- 代码覆盖率不低于 80%
- 单元测试用 Jest，E2E 测试用 Playwright

## 常用命令

- 构建：`npm run build`
- 开发：`npm run dev`
- 类型检查：`npm run typecheck`
```

在 SDK 中使用时，确保 `setting_sources` 包含 `"project"`（默认已包含）：

```python title="Python：加载 CLAUDE.md"
from claude_agent_sdk import query, ClaudeAgentOptions

async for message in query(
    prompt="添加一个用户资料组件",
    options=ClaudeAgentOptions(
        system_prompt={"type": "preset", "preset": "claude_code"},
        setting_sources=["project"],
    ),
):
    ...
```

```typescript title="TypeScript：加载 CLAUDE.md"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "添加一个用户资料组件",
  options: {
    systemPrompt: { type: "preset", preset: "claude_code" },
    settingSources: ["project"]
  }
})) {
  // ...
}
```

`CLAUDE.md` 适合团队共享的项目规范、构建命令、目录结构说明等需要长期存在且随版本控制的内容。如果你手动设置了 `setting_sources`，记得包含 `"project"`，否则 `CLAUDE.md` 不会被加载。

### 通过输出风格切换角色

输出风格（Output Styles）是保存为 Markdown 文件的预设配置，适合定义不同的"角色"——比如代码审查员、安全审计员、SQL 优化专家。它们存储在 `~/.claude/output-styles/`（用户级）或 `.claude/output-styles/`（项目级）目录下。

```python title="Python：创建输出风格"
from pathlib import Path

output_styles_dir = Path.home() / ".claude" / "output-styles"
output_styles_dir.mkdir(parents=True, exist_ok=True)

content = """---
name: Code Reviewer
description: 严格的代码审查助手
---

你是一个资深代码审查员。
对每份代码提交：
1. 检查 Bug 和安全问题
2. 评估性能
3. 提出改进建议
4. 代码质量评分（1-10）"""

(output_styles_dir / "code-reviewer.md").write_text(content, encoding="utf-8")
```

输出风格通过 CLI 的 `/output-style [name]` 命令激活，SDK 在 `setting_sources` 包含 `"user"` 或 `"project"` 时自动加载。

### 通过 systemPrompt append 追加指令

当你需要在保留 Claude Code 全部内置能力的基础上，添加一些会话级别的指令时，`append` 是最直接的方式：

```python title="Python：使用 append 追加指令"
from claude_agent_sdk import query, ClaudeAgentOptions

async for message in query(
    prompt="编写一个计算斐波那契数列的函数",
    options=ClaudeAgentOptions(
        system_prompt={
            "type": "preset",
            "preset": "claude_code",
            "append": "所有 Python 代码必须包含详细的 docstring 和类型注解。",
        },
    ),
):
    ...
```

```typescript title="TypeScript：使用 append 追加指令"
import { query } from "@anthropic-ai/claude-agent-sdk";

for await (const message of query({
  prompt: "编写一个计算斐波那契数列的函数",
  options: {
    systemPrompt: {
      type: "preset",
      preset: "claude_code",
      append: "所有 Python 代码必须包含详细的 docstring 和类型注解。"
    }
  }
})) {
  // ...
}
```

**提示缓存优化**：默认情况下，不同工作目录的会话即使使用相同的 preset 和 append 文本，也无法共享 prompt 缓存——因为 preset 会在系统提示词中嵌入工作目录、平台版本、当前日期、git 状态等上下文信息。如果需要跨用户、跨机器共享缓存，可以设置 `exclude_dynamic_sections: True`（Python）/ `excludeDynamicSections: true`（TypeScript），将动态上下文移到第一条用户消息中：

```python title="Python：启用跨会话缓存共享"
options=ClaudeAgentOptions(
    system_prompt={
        "type": "preset",
        "preset": "claude_code",
        "append": "你负责 Acme 的内部工单分类流程。",
        "exclude_dynamic_sections": True,
    },
)
```

注意：启用此选项后，工作目录等上下文在用户消息中的权重略低于系统提示词，Claude 对这些信息的依赖程度可能稍有降低。

### 通过自定义系统提示词完全控制行为

当你不需要 Claude Code 的内置行为，而是要构建一个高度专业化的 Agent（比如只做数据分析、只做翻译），直接传入自定义字符串作为 `systemPrompt` 是最干净的方式。代价是丢失所有默认工具指令和安全策略，需要自行编写。

```python title="Python：自定义系统提示词"
from claude_agent_sdk import query, ClaudeAgentOptions

custom_prompt = """你是一个 Python 数据分析专家。
遵循以下规则：
- 代码干净、有充分注释
- 所有函数使用类型注解
- 包含完整的 docstring
- 优先使用函数式编程风格
- 每次决策都要解释理由"""

async for message in query(
    prompt="构建一个数据处理管道",
    options=ClaudeAgentOptions(system_prompt=custom_prompt),
):
    ...
```

### 组合使用

这四种方式可以组合使用。例如，激活了"Code Reviewer"输出风格后，再通过 append 添加当次审查的重点方向：

```python title="Python：输出风格 + append 组合"
async for message in query(
    prompt="审查这个认证模块",
    options=ClaudeAgentOptions(
        system_prompt={
            "type": "preset",
            "preset": "claude_code",
            "append": """
            本次审查重点关注：
            - OAuth 2.0 合规性
            - Token 存储安全
            - 会话管理机制
            """,
        },
    ),
):
    ...
```

## 权限控制

### 为什么需要权限控制？

Agent 拥有强大的工具能力——它可以读写文件、执行 Shell 命令、搜索代码。在开发环境中这些能力很方便，但在 CI/CD 流水线、生产部署脚本或多租户 SaaS 中，你可能希望精确控制 Agent 能做什么、不能做什么。Agent SDK 提供了多层权限机制来实现这种控制。

### 权限评估流程

当 Claude 请求使用某个工具时，SDK 按以下顺序评估权限：

```mermaid
graph LR
    A[Hooks] --> B[Deny 规则]
    B --> C[权限模式]
    C --> D[Allow 规则]
    D --> E[canUseTool 回调]
```

具体来说：

- **Hooks**：首先运行 [Hooks](../hooks/)，可以允许、拒绝或继续到下一步
- **Deny 规则**：检查 `disallowed_tools` 和 `settings.json` 中的拒绝规则。匹配则直接阻止，即使在 `bypassPermissions` 模式下也生效
- **权限模式**：应用当前权限模式。`bypassPermissions` 批准到达此步骤的所有请求；`acceptEdits` 自动批准文件操作；其他模式继续向下
- **Allow 规则**：检查 `allowed_tools` 和 `settings.json` 中的允许规则。匹配则批准
- **canUseTool 回调**：如果以上都未解决，调用你的 `canUseTool` 回调做决策。在 `dontAsk` 模式下此步骤被跳过，工具直接被拒绝

### Allow/Deny 规则

`allowed_tools` 和 `disallowed_tools`（TypeScript 中为 `allowedTools` / `disallowedTools`）用于控制工具是否被自动批准或始终拒绝：

| 配置 | 效果 |
|------|------|
| `allowed_tools=["Read", "Grep"]` | `Read` 和 `Grep` 被自动批准。未列出的工具仍然存在，但会落入权限模式和 `canUseTool` 评估 |
| `disallowed_tools=["Bash"]` | `Bash` 始终被拒绝。Deny 规则最先被检查，在任何权限模式下都生效 |

如果你要构建一个严格锁定的 Agent，将 `allowed_tools` 与 `dontAsk` 模式配合使用。列出的工具自动批准，其他一律拒绝：

```typescript title="TypeScript：严格锁定工具集"
const options = {
  allowedTools: ["Read", "Glob", "Grep"],
  permissionMode: "dontAsk"
};
```

> `allowed_tools` 不会约束 `bypassPermissions` 模式。`allowed_tools` 只是预批准列出的工具，未列出的工具虽然不匹配任何 Allow 规则，但会落入权限模式判断——而 `bypassPermissions` 会批准一切。如果你需要 `bypassPermissions` 但又想阻止特定工具，使用 `disallowed_tools`。

### 声明式权限规则

你还可以在 `.claude/settings.json` 中声明式地配置 allow、deny 和 ask 规则。这些规则在 `setting_sources` 包含 `"project"` 时被加载（默认已包含）。

规则语法详见 [Permission settings](/en/settings#permission-settings)。

### 权限模式详解

权限模式提供全局级别的工具使用控制。你可以在调用 `query()` 时设置，也可以在流式会话中动态切换。

#### 模式总览

| 模式 | 行为 | 适用场景 |
|------|------|---------|
| `default` | 标准权限行为，未匹配的工具触发 `canUseTool` 回调 | 通用开发，需要审批时 |
| `dontAsk` | 未被预批准的工具直接拒绝，不调用 `canUseTool` | 无头 Agent，需要固定工具集 |
| `acceptEdits` | 自动批准文件编辑和文件系统操作 | 信任编辑能力，加速迭代 |
| `bypassPermissions` | 跳过所有权限检查 | 受控环境，极度信任 |
| `plan` | 禁止工具执行，Claude 只做规划 | 代码审查、变更预览 |
| `auto` | 模型分类器逐个批准或拒绝工具调用（仅 TypeScript） | 减少交互提示 |

> **子代理继承**：当父 Agent 使用 `bypassPermissions`、`acceptEdits` 或 `auto` 时，所有子代理继承该模式且不可覆盖。子代理可能有不同的系统提示词和更宽松的行为，继承 `bypassPermissions` 意味着它们拥有完全自主的系统访问权限。

#### 设置权限模式

你可以在创建查询时设置模式，也可以在会话进行中动态切换：

```python title="Python：在创建查询时设置模式"
from claude_agent_sdk import query, ClaudeAgentOptions

async for message in query(
    prompt="帮我重构这段代码",
    options=ClaudeAgentOptions(permission_mode="default"),
):
    ...
```

```python title="Python：会话中途动态切换模式"
from claude_agent_sdk import query, ClaudeAgentOptions

q = query(
    prompt="帮我重构这段代码",
    options=ClaudeAgentOptions(permission_mode="default"),
)

# 在会话过程中将模式切换为 acceptEdits
await q.set_permission_mode("acceptEdits")

async for message in q:
    ...
```

```typescript title="TypeScript：会话中途动态切换模式"
import { query } from "@anthropic-ai/claude-agent-sdk";

const q = query({
  prompt: "帮我重构这段代码",
  options: { permissionMode: "default" }
});

// 在会话过程中将模式切换为 acceptEdits
await q.setPermissionMode("acceptEdits");

for await (const message of q) {
  // ...
}
```

动态切换让你可以先从严格模式开始，审查 Claude 的初始方案后再放宽权限。

#### acceptEdits 模式

自动批准文件操作，让 Claude 可以不经确认地编辑代码。其他工具（如非文件系统操作的 Bash 命令）仍需正常权限。

自动批准的操作包括：

- 文件编辑（`Edit`、`Write` 工具）
- 文件系统命令：`mkdir`、`touch`、`rm`、`rmdir`、`mv`、`cp`、`sed`

这些操作仅限于工作目录和 `additionalDirectories` 内的路径。超出范围的路径和受保护路径的写入仍会触发确认。

适合在原型开发或隔离目录中工作时使用——你信任 Claude 的编辑能力，希望更快的迭代速度。

#### dontAsk 模式

将任何权限提示转化为拒绝。只有被 `allowed_tools`、`settings.json` 的 allow 规则或 Hook 预批准的工具能正常运行，其他一律拒绝且不调用 `canUseTool`。

适合需要明确、固定工具表面的无头 Agent——相比静默依赖 `canUseTool` 不存在的情况，`dontAsk` 模式给出的是硬性拒绝，更安全。

#### bypassPermissions 模式

自动批准所有工具使用，不弹出任何提示。Hooks 仍然执行，必要时可以阻止操作。

> 极度谨慎使用。Claude 在此模式下拥有完全的系统访问权限。`allowed_tools` 不会约束此模式——每个工具都会被批准，而不仅仅是列出的那些。但 Deny 规则、显式 ask 规则和 Hooks 仍然生效。

#### plan 模式

完全禁止工具执行。Claude 只能分析代码、制定计划，不能做任何更改。Claude 可能会使用 `AskUserQuestion` 来澄清需求。

适合代码审查或需要在执行前审批变更的场景——让 Claude 提出方案，你审批后再切换到其他模式执行。
