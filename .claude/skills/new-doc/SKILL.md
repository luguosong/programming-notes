---
name: new-doc
description: 创建新的文档页面，包含完整的目录结构、front matter、nav 注册和写作规范检查。触发词：新建文档、new doc、创建页面、新增文档、add page。
---

# 新建文档页面

在 `docs/` 下创建新的文档页面，确保目录结构、front matter、nav 注册一步到位。

## 执行流程

### 第一步：确认页面信息

使用 `AskUserQuestion` 确认以下信息：

1. **页面所属分类**：`frontend/`、`java/`、`document-translation/`、`math/`、`english/`、`topic/` 等
2. **页面标题**：中文标题（将用于文件目录名、front matter title、nav 注册）
3. **文档类型**：教程型（自编笔记）还是翻译型（书籍配套）
4. **父级导航位置**：应该在 `zensical.toml` nav 的哪个分组下

### 第二步：创建目录和文件

1. 创建目录：`docs/{分类}/{目录名}/`
2. 创建 `index.md`，写入 front matter 和初始结构

**Front Matter 模板**：

```yaml
---
title: {页面标题}
---
```

> 如果是二级目录的 index.md，添加 `icon:` 字段（如 `icon: lucide/database`）。

**文档初始结构**（按文档类型选择）：

**教程型**（参考 `.claude/rules/writing-style.md`）：

```markdown
---
title: {页面标题}
---

# {页面标题}

**本文你会学到**：

- 知识点 1
- 知识点 2

## 为什么需要 X？

...

## 核心概念

### 概念 A

...

### 概念 B

...

## 基础用法

...

## 最佳实践

...
```

**翻译型**：

```markdown
---
title: {页面标题}
---

# {页面标题}

...
```

### 第三步：注册导航

在 `zensical.toml` 的 `nav` 部分注册新页面。

**规则**（来自 `.claude/rules/doc-structure.md`）：

- 目录规则：`docs/` 下所有页面必须是 `分类/文件夹名/index.md` 格式，**禁止**平级 `.md` 文件
- nav 格式：`{ "页面标题" = "分类/子目录/index.md" }`
- 标题一致性：nav 中的标题**必须**与 front matter `title:` 完全一致
- 分组规则：带子项的分组，第一项是匿名 index.md，禁止重复注册
- 交叉引用用「」括住标题名，禁止"第X章"形式

### 第四步：验证

1. 执行 `zensical build` 验证站点构建无错误
2. 检查 nav 中新页面是否正确显示

## 注意事项

- 严格遵循 `.claude/rules/doc-structure.md` 中的目录规则和 nav 注册规则
- 写作风格遵循 `.claude/rules/writing-style.md`
- Zensical 语法遵循 `.claude/rules/zensical.md`
- 使用 `AskUserQuestion` 在每个关键决策点确认
