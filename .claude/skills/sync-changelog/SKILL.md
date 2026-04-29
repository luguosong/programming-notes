---
name: sync-changelog
description: '一键并行更新所有 changelog 文件（从 GitHub Releases 拉取），再将新版本变更同步到主题笔记。触发词：同步 changelog、sync changelog、同步更新日志、changelog 同步笔记、sync notes。'
---

# Changelog 全量同步

一键从 GitHub Releases 拉取所有工具的最新版本，更新 changelog 文件，再将有价值的新变更并行同步到对应主题笔记。

## Changelog 源注册表

| ID | 名称 | GitHub 仓库 | changelog 路径 | 笔记根目录 | 映射文件 |
|----|------|-------------|---------------|-----------|---------|
| `claude-code` | Claude Code | `anthropics/claude-code` | `docs/topic/ai/claude-code/changelog/index.md` | `docs/topic/ai/claude-code/` | `mappings/claude-code.md` |
| `copilot-cli` | Copilot CLI | `github/copilot-cli` | `docs/topic/ai/github-copilot-cli/changelog/index.md` | `docs/topic/ai/github-copilot-cli/` | `mappings/copilot-cli.md` |
| `zensical` | Zensical | `zensical/zensical` | `docs/topic/zensical/changelog/index.md` | `docs/topic/zensical/` | `mappings/zensical.md` |

> **新增源**：在上表添加一行 + 在 `mappings/` 下新建 `{id}.md` 映射文件即可。

---

## 执行流程

### Phase 1：更新 Changelog 文件（并行 subagent）

为注册表中每个源创建一个 `general-purpose` subagent，**单条消息多个 Agent 调用**并行执行。

```markdown
Agent({
  description: "更新 Claude Code changelog",
  subagent_type: "general-purpose",
  prompt: "<Phase 1 prompt>"
})
Agent({
  description: "更新 Copilot CLI changelog",
  subagent_type: "general-purpose",
  prompt: "<Phase 1 prompt>"
})
Agent({
  description: "更新 Zensical changelog",
  subagent_type: "general-purpose",
  prompt: "<Phase 1 prompt>"
})
```

**等待所有 Phase 1 subagent 完成后**，收集各源的更新结果（新增版本列表）。

#### Phase 1 Subagent Prompt 模板

```
你负责更新 {名称} 的 changelog 文件。

## 任务

1. 读取当前 changelog 文件：{changelog_path}
   - 提取文件中已记录的最新版本号（第一个 `## 📦` 或 `## v` 标题中的版本号）
2. **获取最新版本列表**（双数据源策略，按顺序尝试）：
   - **方式 A（推荐）**：用 Bash 执行 `curl -sL "https://api.github.com/repos/{github_repo}/releases?per_page=15" | grep -E '"tag_name"|"published_at"|"prerelease"' | head -45` 获取 release 列表。注意 `rtk` 前缀不适用于 curl，直接使用 `curl`
   - **方式 B（备用）**：如果方式 A 因 API 速率限制失败，用 `mcp__web_reader__webReader` 访问 `https://github.com/{github_repo}/releases` 获取 release 列表
3. 筛选出比已记录版本更新的 **稳定版** release（排除 `prerelease: true` 的版本），按发布时间倒序
4. 对每个新版本，用 `mcp__web_reader__webReader` 访问 `https://github.com/{github_repo}/releases/tag/v{版本号}` 获取完整 release notes
5. 如果没有新版本，返回"无新版本"
6. 如果有新版本，按现有 changelog 格式追加到文件顶部（在 front matter 之后、第一个已有版本条目之前）

## 格式要求

严格参照当前 changelog 文件中已有条目的格式。典型格式：

  ## 📦 版本号（发布日期）

  ### ⚡ 性能
  - **条目**：说明

  ### ✨ 新功能
  - **条目**：说明

  ### 🔧 改进
  - 条目

  ### 🐛 Bug 修复
  - 条目

  ---

注意：
- 分类标题使用对应的 Emoji 前缀（⚡ 性能 / ✨ 新功能 / 🔧 改进 / 🐛 Bug 修复 / 💥 破坏性变更）
- 每个版本条目之间用 `---` 分隔
- 新增条目放在最上面（时间倒序）
- 从 GitHub release 中提取的条目，保留原始的技术细节和功能描述

## 返回

更新摘要，格式：
- 源：{名称}
- 新增版本：v1.0.0, v1.0.1（或"无新版本"）
- 新增条目数：N 条
```

### Phase 2：同步笔记（并行 subagent）

根据 Phase 1 结果，为每个**有新版本**的源创建一个 `general-purpose` subagent，**单条消息多个 Agent 调用**并行执行。

如果所有源都没有新版本，跳过 Phase 2，直接报告。

```markdown
Agent({
  description: "同步 Claude Code 笔记",
  subagent_type: "general-purpose",
  prompt: "<Phase 2 prompt>"
})
Agent({
  description: "同步 Copilot CLI 笔记",
  subagent_type: "general-purpose",
  prompt: "<Phase 2 prompt>"
})
...
```

#### Phase 2 Subagent Prompt 模板

```
你负责将 {名称} 的新版本 changelog 同步到主题笔记。

## 输入信息

- 映射文件：.claude/skills/sync-changelog/mappings/{id}.md
- Changelog 路径：{changelog_path}
- 笔记根目录：{notes_root}
- 新版本列表：{从 Phase 1 获取的具体版本号}

## 执行步骤

### 步骤一：读取映射和 changelog

1. 读取映射文件，获取关键词→笔记文件的映射关系
2. 读取 changelog 文件中新版本的所有条目
3. **过滤**：只保留对主题笔记有价值的条目（跳过纯边缘 Bug 修复和 UI 微调）
4. 按映射表将条目分组到目标文件

### 步骤二：更新目标笔记

对每个需要更新的目标页面：

1. 读取目标文件全文
2. **幂等检查**：搜索是否已包含该版本号的标注（如"1.0.19 新增"），跳过已标注的内容
3. 找到最相关的 H2/H3 章节作为插入位置
4. 执行编辑

#### 版本标注格式

**行内标注**——在功能描述末尾标注：

    `功能说明`（版本号 新增）

**列表项**：

    - 功能描述（版本号 新增）

**新增 H3 小节**：

    ### 小节标题

    > 📦 版本号 新增

    内容说明。

#### 编辑原则

1. **合并同主题条目**——不逐条搬运，将同一主题的多个 changelog 条目合并为连贯的说明段落
2. **保持教学风格**——解释"这个功能有什么用"，而非罗列 changelog 原文
3. **问题驱动**——用"当你需要 X 时"开头，说明功能价值
4. **不修改已有措辞**——只新增或扩展，不重写现有段落
5. **技术术语用反引号**——如 `PreToolUse`、`/mcp enable`

### 步骤三：添加 changelog 导航链接

对每个被同步的版本，在版本标题下方添加导航链接。

#### 格式

    ## 📦 版本号（日期）

    > 📝 **笔记定位**：[链接文字](../目标文件/index.md#锚点) · [链接文字](../目标文件/index.md#锚点)

    ### ⚡ 性能
    ...

#### 规则

1. **位置**：紧跟版本标题的下一行空行后，在分类标题之前
2. **链接文字**：2-6 字，概括同步到该页面的内容主题
3. **锚点**：项目使用 `pymdownx.slugs.slugify(case='lower')`，中文保留、Emoji 剥离、标点删除
   - 示例：`## 🔧 管理 MCP 服务器` → `#-管理-mcp-服务器`
4. **多个链接**：用 ` · `（空格+中点+空格）分隔
5. **相对路径**：从 changelog 文件出发的相对路径（如 `../mcp/index.md`）
6. **无同步则不加**：某版本没有内容被同步到笔记，不添加导航行
7. **幂等更新**：已有 `📝 **笔记定位**` 行则替换内容

## 返回

同步报告，格式：

| 目标文件 | 操作 | 内容摘要 |
|---------|------|---------|
| mcp/index.md | 新增段落 | MCP 配置持久化说明 |
| hooks/index.md | 更新列表 | Hook 生命周期扩展 |

共更新 N 个文件。
Changelog 已添加 N 条笔记导航链接。
```

### 汇总报告

Phase 2 全部完成后，输出汇总报告：

```markdown
## 同步报告

### Changelog 更新

| 源 | 新增版本 | 状态 |
|----|---------|------|
| Claude Code | v2.1.118, v2.1.119 | 已更新 |
| Copilot CLI | 无新版本 | 跳过 |
| Zensical | v0.0.34 | 已更新 |

### 笔记同步

| 源 | 更新文件数 | 导航链接数 |
|----|-----------|-----------|
| Claude Code | 5 | 2 |
| Zensical | 1 | 1 |

共更新 X 个 changelog 文件，同步 Y 个笔记文件。
```

然后使用 `AskUserQuestion` 询问是否需要调整。

---

## 注意事项

- **幂等性**：同步前检查版本标注是否已存在，避免重复添加
- **最小改动**：每次只添加必要内容，不重构现有结构
- **保持一致**：新增内容的语气、格式与同一页面已有内容保持一致
- **无映射则跳过**：如果某个 changelog 条目不明确属于哪个主题页面，跳过并在报告中说明
- **目标文件不存在则跳过**：不创建新文件，在报告中说明
- **Phase 1 失败不影响其他源**：某个源的 GitHub 拉取失败时，继续处理其他源
