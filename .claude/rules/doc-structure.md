# 文档结构与导航规范

## 文档目录规则（强制）

`docs/` 下所有 Markdown 页面均采用**独立文件夹形式**，即 `分类/文件夹名/index.md`，**禁止**直接创建 `分类/文件名.md` 平级文件。

- ✅ 正确：`docs/topic/oauth/core-concepts/index.md`
- ❌ 错误：`docs/topic/oauth/core-concepts.md`

此规则适用于 `docs/` 下所有自编笔记、专题研究、书籍翻译等所有内容，无例外。

## nav 注册格式速查

站点配置集中在 `zensical.toml` 的 `nav` 部分，新增页面必须在此注册。

```toml
# 叶子页面（无子项）
{ "页面标题" = "分类/子目录/index.md" }

# 带子项的目录节点（第一项通常是该目录的 index.md）
{ "目录标题" = [
    "分类/index.md",
    { "子页面" = "分类/子目录/index.md" }
] }
```

**注意**：`docs/custom/`、`docs/assert/` 等资源目录无需注册 nav。

## 分组 index 页面规则（重要）

带子项的分组中，第一项 `"分类/index.md"` 作为分组的父页面（匿名项），**禁止**在列表中再以命名方式重复注册同一路径：

```toml
# ✅ 正确：index.md 仅作为匿名父页面出现一次
{ "用户与权限" = [
    "topic/linux/user-management/index.md",
    { "sudo 与 PAM" = "topic/linux/user-management/sudo-pam/index.md" },
    { "ACL 与特殊权限" = "topic/linux/user-management/acl/index.md" }
] }

# ❌ 错误：index.md 被重复注册为命名子页面
{ "用户与权限" = [
    "topic/linux/user-management/index.md",
    { "用户与组管理" = "topic/linux/user-management/index.md" },  # 与父页面重复！
    { "sudo 与 PAM" = "topic/linux/user-management/sudo-pam/index.md" }
] }
```

## 文档标题一致性（强制）

每篇文档 front matter 中的 `title:` 必须与 `zensical.toml` nav 中对应条目的标题**完全一致**。Zensical 会优先显示 nav 配置的标题，不一致会导致 SEO 标题与页面标题不同、搜索索引混乱。

- ✅ nav: `{ "自定义指令" = "..." }` → front matter: `title: 自定义指令`
- ❌ nav: `{ "插件" = "..." }` → front matter: `title: 插件系统`（多了"系统"二字）

修改任一侧标题时，务必同步更新另一侧。

## 文档内交叉引用规范

引用同一书籍其他章节时，**禁止"第X章"形式**，统一用「」括住章节标题名。章节标题以 `zensical.toml` 的 `nav` 配置为准。

| ✅ 正确 | ❌ 禁止 |
|--------|--------|
| 详见「配置CSRF防护」 | 详见第9章 |
| 参考「用户管理」中的介绍 | 参考第3章中的介绍 |
| 从「Spring Security 入门」开始学习 | 从第二章开始学习 |

## 导航图标约定

- **只有二级目录的 `index.md` 在左侧导航中展示图标**，一级目录和三级及以下不设置
- front matter `icon:` 路径用 `/`（如 `lucide/database`），正文图标短码用 `-`（如 `:lucide-database:`）——勿混淆

## 图片统一格式

带图注统一用 `<figure>` 格式，图片托管在 `https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/`，图注格式为"图 章节号.图号 说明"。

```markdown
<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/xxx.png){ loading=lazy }
  <figcaption>图 1.1 图注说明文字</figcaption>
</figure>
```
