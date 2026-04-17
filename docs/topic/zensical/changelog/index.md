---
title: 更新日志
---

# 更新日志

本文记录 Zensical 每个版本的变更内容，按发布时间倒序排列。数据来源于 [GitHub Releases](https://github.com/zensical/zensical/releases)。

## v0.0.33（2026-04-14）

### 破坏性变更

- Docker 镜像基础镜像切换为 Alpine Linux，提升兼容性和易用性

### 新功能

- `zensical new` 生成的 `zensical.toml` 现在默认包含所有推荐的 Markdown Extensions 配置

### Bug 修复

- UI 更新至 v0.0.13，修复目录中锚点链接的两个 Bug
- 修复过时的帮助信息
- 补充默认 Markdown Extensions 的缺失配置

## v0.0.32（2026-04-07）

### Bug 修复

- 修复当站点 URL 包含路径组件时，base URL 前缀剥离不正确的问题
- 更新 Pygments 依赖以缓解安全漏洞
- 修复作为代码片段引用的 Markdown 文件被错误纳入自动生成导航的问题

## v0.0.31（2026-04-01）

### Bug 修复

- UI 更新至 v0.0.12：因 Lucide 升级至 v1，移除了 19 个品牌图标，新增 166 个图标（主要在 SimpleIcons 和 FontAwesome 中）
- 修复 modern 主题中目录相关的最新 Bug
- 修复即时导航（Instant Navigation）中锚点链接的问题

## v0.0.30（2026-03-28）

### 新功能

- 添加对 `mike` 的支持，用于在 GitHub Pages 上管理多版本文档

### Bug 修复

- UI 更新至 v0.0.11：modern 主题新增移动端浮动目录菜单
- 修复 `docs-directory` 和 `custom-theme-directory` 的验证问题

> `mike` 是临时方案，Zensical 将在未来提供原生的版本管理支持，不受 GitHub Pages 限制。

## v0.0.29（2026-03-24）

### Bug 修复

- 修复绝对路径链接被错误处理的问题
- 修复 Windows 11 上文件变更未被 Zensical 检测到的问题（对监听路径进行规范化）

## v0.0.28（2026-03-19）

### 新功能

- 新增文件监听器配置选项，支持轮询模式，适用于 Docker on Windows 等文件系统事件受限的环境
- modern 主题新增版本选择器支持

### Bug 修复

- 修复引导生成的 `zensical.toml` 中 MathJax 配置无效的问题
- UI 更新至 v0.0.10，修复搜索和代码注释渲染相关的 Bug
- 修复 Docker on Windows 下自动重载不生效的问题

### 配置参考

```bash
# 启用轮询监听器（适用于 Docker on Windows）
export ZENSICAL_POLL_WATCHER=1

# 设置轮询间隔（默认 500ms）
export ZENSICAL_POLL_INTERVAL=500
```

## v0.0.27（2026-03-13）

### Bug 修复

- 修复自动追加的代码片段位于 `docs` 目录内时导致的重载循环
- 修复包含中文路径段的页面自动重载不生效的问题
- 构建输出的 URL 不再进行 percent 编码

## v0.0.26（2026-03-11）

### Bug 修复

- 修复 manylinux x86 架构上 Python 3.8 替代 3.10 构建 wheel 导致 Zensical 不可用的问题（上游 `maturin` 依赖的 Bug）
- 修复 Python 3.14 下使用 emoji 扩展时的废弃警告

## v0.0.25（2026-03-10）

### Bug 修复

- UI 更新至 v0.0.9，改善无障碍体验，修复渲染问题
- `zensical serve` 在配置解析错误时不再终止运行
- 自动追加的 `pymdownx.snippets` 文件现在会被监听变更
- 插件配置哈希化，变更后触发页面重建
- 修复引导生成的 `zensical.toml` 中的示例说明

## v0.0.24（2026-02-26）

### Bug 修复

- UI 更新至 v0.0.8，修复中文等非 ASCII 语言的即时预览问题，以及 modern 主题中页面切换时的布局偏移
- 修复 `mkdocstrings` 源码中虚拟环境被错误包含的问题
- 修复禁用目录 URL 时同页面链接解析不正确的问题（回归 Bug）

## v0.0.23（2026-02-11）

### Bug 修复

- 修复 v0.0.22 引入的回归问题：未配置 `mkdocstrings` 时构建报错（改为延迟导入）

## v0.0.22（2026-02-11）

### 新功能

- 添加对 `autorefs` 插件的支持

### Bug 修复

- 大幅提升大型 `mkdocstrings` 项目的性能
- UI 更新至 v0.0.7，修复移动端浏览体验问题
- 每次重建时重置兼容模块中的全局数据
- 修复 `mkdocstrings` 路径设为根目录时的监听问题
- 未安装 `mkdocstrings` 但启用时提前报错
- 修复包含 `:` 的相对 URL 需要以 `./` 开头的问题

## v0.0.21（2026-02-04）

### Bug 修复

- UI 更新至 v0.0.6，修复 `data-preview` 标记的大量链接导致的内存占用过高问题
- modern 主题中回到顶部按钮移至底部
- 修复 `extra_css` 变更后不自动重载的问题
- 修复目标为当前页面时预览未生成的问题

## v0.0.20（2026-01-29）

### Bug 修复

- 修复大型 `mkdocstrings` 项目构建时内存占用过高的问题
- 修复构建有时提前终止的问题

### 性能优化

- 移除导航不必要的 clone 操作，降低内存消耗

## v0.0.19（2026-01-24）

### 新功能

- 支持生成 `objects.inv` 文件，允许外部工具发现和链接 API 文档（需配合 `mkdocstrings` v1.0.2+）

### Bug 修复

- 修复外部源文件被错误监听的问题

## v0.0.18（2026-01-23）

### Bug 修复

- 修复默认导航处理中的回归问题
- 修复 `mkdocstrings` `paths` 设为 `.` 时的重载循环（v0.0.17 回归）
- 修复构建时出现 "trailing characters" 错误的问题
- 放宽导航模板中的 meta key 限制，允许使用任意 meta key

### 重构

- 切换为保守的源文件自动重载策略

## v0.0.17（2026-01-19）

### 新功能

- 支持自动和手动的 API 交叉引用（Cross-references）：自动生成的 API 文档中的符号名自动链接到相关章节

```md
<!-- 手动交叉引用语法 -->
See [the FastAPI class][fastapi.FastAPI] for reference.
```

### Bug 修复

- UI 更新至 v0.0.5
- 修复使用 `--open` 时每次配置变更都打开浏览器的问题
- 允许页面在 `nav` 中多次出现
- 监听 `mkdocstrings` 源文件变更以自动重载

## v0.0.16（2026-01-15）

### 新功能

- 允许在主题配置中使用任意 admonition 图标 key

### Bug 修复

- UI 更新至 v0.0.4，修复搜索 `&` 字符、页脚 Lucide 图标使用等问题

## v0.0.15（2025-12-24）

### 新功能

- UI 更新至 v0.0.3：新增模糊搜索支持，改善触摸设备上的 tooltip 行为

## v0.0.14（2025-12-21）

### 新功能

- 发布官方 Docker 镜像

```sh
docker run --rm -it -p 8000:8000 -v ${PWD}:/docs zensical/zensical
```

### Bug 修复

- 修复 Linux 和 Windows 上构建挂起的问题
- 修复模板变更后构建缓存未失效的问题
- 修复缺少 `custom_dir` 设置导致构建崩溃的问题

## v0.0.13（2025-12-18）

### 破坏性变更

- UI 更新至 v0.0.2，Simple Icons 移除了 44 个图标，新增 132 个图标

### Bug 修复

- 修复 `repo_name` 被主机名替换的问题

## v0.0.12（2025-12-18）

### Bug 修复

- 改善 `mkdocs.yml` 解析的健壮性
- 修复 `zensical new` 的多个问题
- 修复 `mkdocs.yml` 在 Windows 上的编码错误
- 修复 `.mjs` 文件的 MIME 类型和 `type="module"` 自动添加
- 修复 `pymdownx.superfences` 自定义 fence 验证器解析问题
- 修复 content.action 按钮不显示的问题
- 修复文件名包含 `index` 被错误识别为索引文件的问题

## v0.0.11（2025-12-03）

### 新功能

- 添加对 `mkdocstrings` 的支持，可为 Python 项目生成 API 参考文档（交叉引用和反向链接暂未支持）

### Bug 修复

- 修复包含空格的链接无法解析的问题
- 修复滚动条出现/消失时的布局偏移
- 修复 classic 主题下搜索显示 `⌘K` 快捷键的兼容性问题

## v0.0.10（2025-11-25）

### 性能优化

- Disco 搜索引擎重大性能改进：分页不再重新执行查询（延迟降至 <1ms）
- 搜索高亮改为懒加载，仅对可见结果执行（25MB 数据集最坏情况从 150ms 降至 60ms）

### Bug 修复

- 修复 `zensical.toml` 不识别 preview 扩展的问题
- 修复 FontAwesome 图标在导航中的对齐问题

## v0.0.9（2025-11-20）

### Bug 修复

- 修复 Markdown Extensions 配置变更后未被检测到的问题
- 修复配置解析器要求文件名必须为 `zensical.toml` 的问题

## v0.0.8（2025-11-15）

### Bug 修复

- 修复 `pymdownx.blocks` 导致构建崩溃的问题
- 修复禁用目录 URL 时同页面链接 `.` 不工作的问题
- 修复页面以 `index.md` 结尾时总被当作索引页面的问题
- 修复 `slugify` 函数不可配置的问题
- 修复 `zensical serve` 在连接关闭时写入挂起的问题
- 修复中文路径处理崩溃的问题

## v0.0.7（2025-11-12）

### Bug 修复

- 修复自定义 fence 格式化函数未被正确解析的问题
- 修复离线模式下 `iframe-worker` 未加载的问题
- 修复 BetterEm 扩展配置问题
- 修复中文路径处理崩溃的问题
- 改善任务列表复选框的可识别性

## v0.0.6（2025-11-11）

### Bug 修复

- 修复 Windows 上 base URL 使用反斜杠的问题
- 修复 `edit_uri` 不使用 `docs_dir` 作为默认值的问题
- 修复浏览器在服务启动前就打开的问题
- 修复 modern 主题公告栏文字颜色、搜索栏、图标填充、标签页控件等多个 UI 问题
- 修复 Chrome 移动端搜索无法打开的问题

## v0.0.5（2025-11-07）

### Bug 修复

- 默认禁用 tracing
- 修复 `extra templates` 处理错误
- 为 `zensical serve` 输出的 URL 添加协议前缀
- Python < 3.11 使用 `tomli` 解析 TOML
- 修复离线插件导致配置解析器崩溃的问题

## v0.0.4（2025-11-06）

### 新功能

- 支持通过 `python -m zensical` 作为脚本运行

### Bug 修复

- 修复发布包中缺少 `LICENSE.md` 的问题
- 修复 `extra_css`/`extra_javascript` 位置错误的问题
- 修复 `zensical new` 未创建 GitHub Action 的问题

## v0.0.3（2025-11-05）

- 首次发布
