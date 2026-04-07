---
name: sync-changelog
description: '将 changelog 中的新版本变更同步到对应主题笔记页面，并标注版本号。支持 Copilot CLI 和 Claude Code 两套笔记。触发词：同步 changelog、sync changelog、同步更新日志、changelog 同步笔记、sync notes。'
---

# Changelog → 主题笔记同步

读取 changelog 中的新版本条目，将有价值的变更同步到对应主题笔记页面，并标注版本来源。

## 适用工具与路径映射

### Copilot CLI

| 路径 | 说明 |
|------|------|
| **changelog** | `docs/topic/ai/github-copilot-cli/changelog/index.md` |
| **笔记根目录** | `docs/topic/ai/github-copilot-cli/` |

主题页面映射：

| 关键词/领域 | 目标文件 |
|------------|---------|
| Hook、preToolUse、postToolUse、notification hook | `hooks/index.md` |
| MCP、mcp enable、mcp disable、mcp auth、OAuth | `mcp/index.md` |
| Skill、内置 skill、skill 目录 | `skills/index.md` |
| Agent、explore、task、code-review、Critic Agent | `agents/index.md` |
| Plugin、marketplace、插件 | `plugins/index.md` |
| 上下文、session、resume、compact、rewind | `context/index.md` |
| 模式、plan、autopilot、interactive | `modes/index.md` |
| 安装、更新、启动、认证 | `basic/index.md` |
| 工作流、diff、pr、commit、delegate | `workflows/index.md` |
| 自定义指令、CLAUDE.md、instructions | `instructions/index.md` |
| 推荐扩展、最佳实践 | `practices/index.md` |

### Claude Code

| 路径 | 说明 |
|------|------|
| **changelog** | `docs/topic/ai/claude-code/changelog/index.md` |
| **笔记根目录** | `docs/topic/ai/claude-code/` |

主题页面映射：

| 关键词/领域 | 目标文件 |
|------------|---------|
| Hook、preToolUse、postToolUse、CwdChanged、FileChanged | `hooks/index.md` |
| MCP、mcp enable、OAuth、elicitation、SSE | `mcp/index.md` |
| Skill、skill 目录、skillDirectories | `skills/index.md` |
| Sub-agent、background agent、task、fleet | `sub-agents/index.md` |
| Plugin、marketplace、plugin install | `plugins/index.md` |
| 上下文、session、resume、compact、context | `context-engineering/index.md` |
| 平台、Windows、macOS、Linux、WSL、sandbox | `platforms/index.md` |
| 安装、更新、启动、认证、login | `getting-started/index.md` |
| 工作原理、工具、Bash、Edit、Write | `how-it-works/index.md` |
| 配置、CLAUDE.md、settings、permissions | `configuration/` 下的子页面 |
| 自动化、-p、headless、SDK、CI | `automation/index.md` |
| 企业、managed settings、BYOK | `enterprise/index.md` |
| 集成、IDE、VSCode、Bedrock、Vertex | `integrations/index.md` |
| 最佳实践 | `best-practices/index.md` |

## 执行流程

### 第一步：确认同步范围

使用 `ask_user` 确认：

1. **同步哪个工具**（如未从对话上下文推断出）：Copilot CLI 还是 Claude Code
2. **同步哪些版本**：
   - 「最新版本」——只同步 changelog 中最顶部的版本
   - 「指定版本」——如 `1.0.19` 或 `2.1.92`
   - 「自某版本起」——如 `1.0.17 之后的所有版本`

### 第二步：读取 changelog 并提取变更

1. 读取对应的 changelog 文件
2. 提取目标版本的所有条目
3. **过滤**：只保留对主题笔记有价值的条目（跳过纯 Bug 修复中的边缘情况和 UI 微调）
4. 按主题映射表将条目分组到目标文件

### 第三步：检查目标页面现有内容

对每个需要更新的目标页面：

1. 读取目标文件全文
2. 搜索是否已包含该版本号的标注（避免重复同步）
3. 确定新内容应插入的位置（找到最相关的 H2/H3 章节）

### 第四步：更新主题笔记

对每个目标页面执行编辑：

#### 版本标注格式

**行内标注**——在新增/修改的内容末尾或段落开头标注：

```markdown
`/mcp enable` 和 `/mcp disable` 的配置现在跨会话持久化（1.0.19 新增）。
```

对于新增的独立段落或列表项：

```markdown
- 自动模式下被拒绝的命令会显示通知，并出现在 `/permissions` → Recent 标签页（2.1.89 新增）
```

对于新增的独立 H3 小节：

```markdown
### Critic Agent

> 📦 1.0.18 新增

在执行计划和复杂实现时，自动使用互补模型进行审查，提前捕获错误。目前为 Claude 模型的实验性功能。
```

#### 编辑原则

1. **不要逐条搬运 changelog**——将同一主题的多个 changelog 条目合并为连贯的说明段落
2. **保持笔记的教学风格**——changelog 是事实列表，笔记要解释"这个功能有什么用"
3. **遵循项目的教学风格约束**——问题驱动、类比优先、循序渐进（参见 CLAUDE.md）
4. **版本标注位置**——紧跟功能描述，使用半角括号 `（版本号 新增）` 或 `（版本号 改进）`
5. **不修改已有内容的措辞**——只新增或扩展，不重写现有段落
6. **技术术语使用反引号**——如 `PreToolUse`、`/mcp enable`

### 第五步：输出同步报告

完成所有编辑后，输出摘要：

```
## 同步报告

**工具**：Copilot CLI
**版本**：1.0.19

| 目标文件 | 操作 | 内容摘要 |
|---------|------|---------|
| mcp/index.md | 新增段落 | MCP 配置持久化说明 |
| context/index.md | 更新列表 | 时间线命令名称改进 |

共更新 2 个文件，新增 N 行。
```

然后使用 `ask_user` 询问是否还需要调整。

## 注意事项

- **幂等性**：同步前检查版本标注是否已存在，避免重复添加
- **最小改动**：每次只添加必要内容，不重构现有结构
- **保持一致**：新增内容的语气、格式与同一页面已有内容保持一致
- 如果某个 changelog 条目不明确属于哪个主题页面，跳过并在报告中说明
- 如果目标文件不存在或为空，在报告中说明，不创建新文件
