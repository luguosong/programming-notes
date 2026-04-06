# CLAUDE.md

## 仓库简介

**陆国松的编程笔记**（programming-notes）是一个多语言、多模块的学习笔记集合，包含：

- **文档站点**：基于 [zensical](https://zensical.org/) 构建的静态文档站点，内容在 `docs/` 目录
- **书籍配套代码**：Spring Security / OAuth2 / Spring Authorization Server 等独立模块，在 `code/document-translation/` 下按书籍章节组织（含 Java Maven 和 Node.js 项目）
- **前端示例代码**：React + Vite 独立小项目，在 `code/frontend/` 下按主题组织

## 常用命令

### 本地基础环境（Docker Compose）

基础环境配置位于 `environment/docker-compose.yml`，包含 MySQL、Redis、RabbitMQ、Nacos。

```shell
# 启动所有服务（后台运行）
docker compose -f environment/docker-compose.yml up -d

# 查看服务状态
docker compose -f environment/docker-compose.yml ps

# 停止并保留数据
docker compose -f environment/docker-compose.yml down

# 停止并清除数据卷（慎用）
docker compose -f environment/docker-compose.yml down -v
```

各服务连接信息：

| 服务 | 地址 | 账号 / 密码 |
|------|------|------------|
| MySQL 8.0 | `localhost:23306` | `root` / `12345678` |
| Redis 7 | `localhost:26379` | 无密码 |
| RabbitMQ 3 | `localhost:25672`（AMQP）<br>`http://localhost:35672`（管理台） | `guest` / `guest` |
| Nacos 2.3 | `http://localhost:28848/nacos` | `nacos` / `nacos` |

> 数据持久化目录：`environment/data/`（已加入 `.gitignore`，不提交）

### 文档站点（zensical）

```shell
# 安装 zensical（首次使用）
pip install zensical

# 本地预览站点（默认 http://localhost:7000）
zensical serve

# 编译为静态网页（输出到 site/）
zensical build
```

### Java / Spring Boot 示例

每个示例是独立的 Maven 模块（Java 17，Spring Boot 3.5.0），进入对应目录后执行：

```shell
# 构建
mvn clean package

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassName

# 运行单个测试方法
mvn test -Dtest=ClassName#methodName

# 启动 Spring Boot 应用
mvn spring-boot:run
```

### 前端 React 示例

每个示例是独立的 Vite 项目，进入对应目录后执行：

```shell
# 安装依赖
npm install

# 开发模式启动
npm run dev

# 生产构建
npm run build
```

### 前端 HTML 示例

HTML 笔记的代码示例放在 `docs/frontend/html/*/demo/` 目录（与 `index.md` 同级的 `demo/` 文件夹），以完整 HTML 文档形式存在，同时用于代码引用和效果预览。

**文档中的引用规范**：
- `--8<--` 使用从项目根目录算起的完整路径：`--8<-- "docs/frontend/html/SECTION/demo/FILE.html"`
- `<iframe>` 的 `src` 使用相对路径：`src="demo/FILE.html"`（iframe 通过浏览器加载，相对路径即可）

**示例**：
````markdown
``` html title="标题标签演示"
--8<-- "docs/frontend/html/text-content/demo/headings.html"
```

<iframe class="html-demo" loading="lazy" src="demo/headings.html"></iframe>
````

## 架构说明

### 整体目录结构

```
programming-notes/
├── docs/                    # 文档内容（Markdown），站点导航由 zensical.toml 的 nav 配置控制
│   ├── custom/              # 自定义资源（不参与 nav 路由）
│   │   ├── css/             # 自定义样式（custom.css、font.css、sidebar-width.css 等）
│   │   └── js/              # 自定义脚本（mathjax.js、viewer-js-init.js 等）
│   ├── assert/              # 静态资源（logo、favicon 等）
│   ├── frontend/            # 前端相关笔记（HTML、CSS、React）
│   ├── java/                # Java 笔记
│   ├── document-translation/# 书籍翻译（Spring Security、OAuth2、算法等）
│   ├── math/                # 数学笔记
│   ├── english/             # 英语笔记
│   ├── roadmap/             # 学习路线
│   ├── superpowers/         # 规划文档、设计文档（仅本地，已 .gitignore）
│   └── topic/               # 专题研究（含 Zensical 使用说明）
├── code/                    # 与笔记对应的示例代码
│   ├── java/                # Java 知识点演示（聚合 POM：code/java/pom.xml）
│   │   ├── database/        # 数据库相关模块（聚合 POM：code/java/database/pom.xml）
│   │   │   ├── jdbc-connection/        # 连接管理
│   │   │   ├── jdbc-statement/         # Statement + ResultSet
│   │   │   ├── jdbc-preparedstatement/ # PreparedStatement
│   │   │   ├── jdbc-transaction-batch/ # 事务 + 批处理
│   │   │   ├── jdbc-pool/              # 连接池（HikariCP + Druid）
│   │   │   ├── jdbc-metadata/          # 元数据
│   │   │   ├── jdbc-advanced-types/    # 高级数据类型
│   │   │   ├── jdbc-stored-procedure/  # 存储过程
│   │   │   ├── jdbc-clob/              # CLOB/BLOB 大对象
│   │   │   └── jdbc-exception/         # 异常处理
│   │   └── javase/          # JavaSE 相关模块（聚合 POM：code/java/javase/pom.xml）
│   │       ├── file/        # 文件操作模块（聚合 POM：code/java/javase/file/pom.xml）
│   │       │   ├── file-basic/        # java.io.File 类操作
│   │       │   └── nio-file/          # NIO.2 Path + Files 工具类
│   │       └── io/          # IO 流模块（聚合 POM：code/java/javase/io/pom.xml）
│   │           ├── io-node-stream/     # 节点流
│   │           └── io-wrapper-stream/  # 包装流
│   ├── document-translation/# 书籍配套示例（按书籍/章节组织）
│   │   ├── oauth2-in-action/            # OAuth 2 in Action（Node.js/Express，12 模块）
│   │   ├── spring-authorization-server-153/ # Spring Authorization Server 实战（Spring Boot 3.5.8）
│   │   └── spring-security-in-action2/  # Spring Security in Action 2nd（44 子模块）
│   ├── topic/               # 专题研究配套代码
│   │   └── oauth/diagrams/  # OAuth 专题图表资源
│   └── frontend/            # 前端示例
│       └── react/           # React 示例
│           ├── basic/basic-syntax/react-basic-demo/     # 基础语法演示
│           └── global-state-management/mobx/mobx-demo/  # MobX 状态管理演示
├── environment/             # 本地开发基础环境
│   ├── docker-compose.yml   # 一键启动 MySQL/Redis/RabbitMQ/Nacos
│   ├── data/                # 各服务数据持久化目录（不提交 git）
│   └── init/                # 初始化脚本（如 MySQL 建库 SQL，按需放置）
│       └── mysql/           # MySQL 初始化 SQL（docker 启动时自动执行）
├── overrides/               # zensical 主题自定义覆盖文件
│   ├── home.html            # 首页模板覆盖
│   ├── main.html            # 主模板覆盖
│   └── partials/            # 局部模板覆盖（如 header.html 等）
├── site/                    # zensical build 的输出目录（勿手动修改）
└── zensical.toml            # 站点配置（导航、主题、Markdown 扩展）
```

### docs/ 与 code/ 的对应关系

`docs/` 中的每篇笔记通常对应 `code/` 中的示例代码。例如：
- `docs/document-translation/spring-security-in-action2/part1/02-hello/` ↔ `code/document-translation/spring-security-in-action2/ssia-ch2-ex1/`
- `docs/frontend/react/global-state-management/mobx/` ↔ `code/frontend/react/global-state-management/mobx/mobx-demo/`
- `docs/java/javase/file/` ↔ `code/java/javase/file/`（file-basic、nio-file）
- `docs/java/javase/io/` ↔ `code/java/javase/io/`（io-node-stream、io-wrapper-stream）

### zensical 站点配置

站点配置集中在 `zensical.toml`：
- `docs_dir = "docs"` — 文档根目录
- `dev_addr = "localhost:7000"` — 本地开发地址
- `nav` — 完整导航树，新增页面需要在此注册
- `[project.markdown_extensions]` — 启用了数学公式（MathJax）、代码高亮、任务列表、选项卡等扩展

#### nav 注册格式速查

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

### Java 示例模块结构

`code/java/` 下采用两级聚合 POM 组织：

- **`code/java/pom.xml`** — 顶层聚合 POM，在 IntelliJ IDEA 中打开此文件可一次性导入全部 Java 子模块
- **`code/java/database/pom.xml`** — database 子聚合 POM，管理所有数据库相关演示模块

每个 Java 演示模块遵循标准 Maven 目录结构（Java 17，Spring Boot 3.5.0），以测试类为主体，**只包含与其主题相关的依赖**（基础依赖：`H2` + `spring-boot-starter-test`，部分模块按需额外添加如 `HikariCP`、`Druid` 等）。

**新建子模块后**：必须在对应聚合 POM 的 `<modules>` 中添加模块目录名，否则 IDEA 不会感知新模块。

### Java 示例模块拆分原则

**一个模块只演示一个功能维度**，不要把多个不相关的技术点混在同一模块中。

- ✅ 正确：`jdbc-connection`（只放连接相关）、`jdbc-statement`（只放 Statement+ResultSet）
- ❌ 错误：`jdbc-demo`（把连接、查询、事务、连接池全部堆在一起）

模块名称应自描述功能边界（如 `jdbc-transaction-batch` 而非 `jdbc-advanced`）；测试类间无共享依赖时应拆分。

### 前端示例技术栈

- React 19.2 + Vite 7 + TypeScript
- UI 组件库：Ant Design 6 (antd)
- 状态管理示例：MobX 6 + mobx-react
- 路由：React Router 7（集成在各示例中，无独立路由示例项目）
- 无专项测试配置

## 新增内容规范

- 新增文档页面后，必须在 `zensical.toml` 的 `nav` 部分注册，否则页面不会出现在导航中
- `site/` 目录为构建产物，不应提交到版本库（已在 `.gitignore` 中）

### 文档目录规则（强制）

`docs/` 下所有 Markdown 页面均采用**独立文件夹形式**，即 `分类/文件夹名/index.md`，**禁止**直接创建 `分类/文件名.md` 平级文件。

- ✅ 正确：`docs/topic/oauth/core-concepts/index.md`
- ❌ 错误：`docs/topic/oauth/core-concepts.md`

此规则适用于 `docs/` 下所有自编笔记、专题研究、书籍翻译等所有内容，无例外。

### 文档内交叉引用规范

引用同一书籍其他章节时，**禁止"第X章"形式**，统一用「」括住章节标题名。章节标题以 `zensical.toml` 的 `nav` 配置为准。

| ✅ 正确 | ❌ 禁止 |
|--------|--------|
| 详见「配置CSRF防护」 | 详见第9章 |
| 参考「用户管理」中的介绍 | 参考第3章中的介绍 |
| 从「Spring Security 入门」开始学习 | 从第二章开始学习 |

### 导航图标约定

- **只有二级目录的 `index.md` 在左侧导航中展示图标**，一级目录和三级及以下不设置
- front matter `icon:` 路径用 `/`（如 `lucide/database`），正文图标短码用 `-`（如 `:lucide-database:`）——勿混淆

### 图片统一格式

带图注统一用 `<figure>` 格式（见下方「Zensical 特有语法」），图片托管在 `https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/`，图注格式为"图 章节号.图号 说明"。

### 行内强调与说明约定

- **强调 / 重点内容**：使用反引号 `` ` `` 包裹，而非加粗或斜体。例如：`RegisteredClient`、`Authorization Code`
- **补充说明**：在被说明内容后紧跟半角括号 `(说明文字)`。例如：JDBC（Java Database Connectivity）、H2（内存数据库）

| ✅ 正确 | ❌ 避免 |
|--------|--------|
| `Authorization Code` 是最常用的授权方式 | **Authorization Code** 是最常用的授权方式 |
| JDBC（Java Database Connectivity） | JDBC - Java Database Connectivity |

### Emoji 表情使用规范（仅自编笔记）

> 以下规范仅适用于 `docs/` 下非翻译类内容。翻译类内容（`docs/document-translation/`）忠于原书风格，不适用。

**原则**：Emoji 是调味料，不是主菜——适度使用让笔记更生动，但不要过度堆砌。

**核心规则**：
1. **每段 / 每条最多 1 个 Emoji**，禁止连续堆叠（如 `🎯💡🚀`）
2. **优先在视觉分界处使用**：H2/H3 标题前、要点列表开头、段落首句、对比表格
3. **语义匹配优先**：Emoji 含义应与上下文一致
4. **保持克制**：一篇文章中 Emoji 总数控制在 10-20 个以内
5. **技术名词不加 Emoji**：`Authorization Code`、`PreparedStatement` 等术语本身不需要修饰

**常用 Emoji 参考**：标题 → ⚙️🗂️🧱🔍🧪 | 要点 → 🎯💡🔧🚀📦 | 语义 → ✅❌⚠️💡📌 | 过渡 → ➡️↪️→

**禁止**：标题中连续多个 Emoji、正文每句都加 Emoji、代码块内部使用 Emoji

### 教学风格约束（仅自编笔记）

> 仅适用于 `docs/` 下非翻译类内容。翻译类忠于原书风格，不适用。

**设计原则——问题驱动**：每个技术决策都从一个具体问题出发。不要先讲理论再讲实现，而是先抛出「你会遇到什么问题」，再引出解决方案。读者带着问题读，吸收效率远高于被动接收知识。

- 一个 H2/H3 的开头应该是「当你需要 X 时，你会发现 Y 问题…」而非「X 是一种 Y 技术」
- 代码示例先展示「不用这个方案会怎样」（痛点），再给出解决方案（对比更鲜明）

**语言风格**：类比优先（先用生活常识类比，再给精确定义）→ 循序渐进（先够用版本，再完整细节）→ 口语化但术语准确 → 主动语态 → 术语零门槛（不假设前置知识）

**知识点结构**（每个 H3 按此脉络）：遇到什么问题（Problem）→ 是什么（What）→ 怎么用（How）→ 注意点（Pitfalls，可选）

**代码示例**：标注语言、关键行加注释、最小可运行、错误标 `// ❌` 正确标 `// ✅`、复杂示例前先用自然语言说明

**节奏控制**：每个 H3 控制在 5 分钟内可读完；每 2-3 个知识点后加小结或对比表格；长文开头加「本文你会学到」要点列表

**禁止**：上来就贴大段代码没解释、堆砌官方文档原文、虎头蛇尾、假设读者已知前置概念

### 标题架构规范（TOC 知识大纲化）

**核心原则**：标题层级 = 知识脉络。读者**只读右侧 TOC** 就能还原文章的推进逻辑。

#### 修改已有文章时的全局审视原则（强制）

对已有 TOC 结构的文章进行新增或修改时，**禁止简单地在某个位置插入内容**。必须先通读完整标题结构，评估新内容在知识脉络中的位置，必要时重构章节结构，确保 TOC 从上到下仍是一条完整的知识路径。

> **豁免**：`docs/document-translation/` 下的文章忠于原书，不适用重构要求。

#### 标题层级职责

- **H2**：划分知识维度，从上到下体现渐进逻辑（教程型：为什么→是什么→怎么做→进阶→最佳实践 | 翻译型：按原书顺序，标题补充语境 | 参考型：概述→分组→配置→FAQ）
- **H3**：拆分具体知识点，H2 下 ≥3 个子概念时必须拆出 H3

#### 命名风格

| 推荐 | 避免 |
|------|------|
| `### 为什么需要连接池？` | `### 连接池概述` |
| `### 手动事务提交` | `### 事务（二）` |
| `### 连接池 vs 直连对比` | `### 对比` |
| `## RegisteredClient：客户端注册信息` | `## RegisteredClient`（孤立术语） |
| 用动作描述：`## 配置数据源` | 编号代替逻辑：`## 第一步` |

#### 反模式

- **扁平清单**：≥5 个连续 H2 且无 H3 → 按逻辑分组降级为 H3
- **万能标题**：反复出现 `## 概述`、`## 其他`、`## 总结` → 用具体内容替换

#### 骨架模板

- **教程型**：`# 主题` → `## 为什么需要 X` → `## 核心概念（### A / ### B）` → `## 基础用法（### 最小示例 / ### 常用 API）` → `## 进阶用法` → `## 最佳实践`
- **翻译型**：`# 章节号. 标题` → `## 主题 A（### 知识点 1/2）` → `## 主题 B` → `## 总结（可选）`
- **参考型**：`# 模块名` → `## 概述` → `## 分组 A（### 组件 1/2）` → `## 配置参考` → `## 常见问题`

## Zensical 特有语法与项目约定

> Zensical 基于 Material for MkDocs，支持 Admonitions、Content Tabs、代码块增强、文本格式化扩展（高亮/下划线/上下标/键位）、图标 Emoji、网格布局等标准功能。以下仅记录**项目特有约定**和 **AI 易错点**。

### 代码块空格规则（易错）

带 `title=` 等属性时，`` ``` `` 与语言标识符之间**必须有空格**：

- ✅ `` ``` java title="示例" ``
- ❌ `` ```java title="示例" ``

### 图片统一格式

```markdown
<!-- 带图注（项目统一格式） -->
<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/xxx.png){ loading=lazy }
  <figcaption>图 1.1 图注说明文字</figcaption>
</figure>
```

### 图标路径写法差异

- Front matter `icon:` 字段用 `/`：`icon: lucide/database`
- 正文 Markdown 图标短码用 `-`：`:lucide-database:`

### 嵌入外部文件

```markdown
--8<-- "相对路径/文件名"
```

### Front Matter 常用字段

```markdown
---
icon: lucide/book          # 仅二级目录 index.md 设置
status: new                # 需在 zensical.toml extra.status 中定义
hide: [navigation, toc]    # 可选：隐藏左侧导航或右侧目录
---
```

### Mermaid 图表样式规范

Mermaid 在 Shadow DOM 内渲染，外部 CSS 无法穿透，只能通过 CSS 变量继承。深色模式适配已在 `docs/custom/css/custom.css` 的 `[data-md-color-scheme="slate"]` 中配置 `--md-mermaid-*` 变量，**勿删除**。

**样式生成规则**：
1. 节点一律 `fill:transparent`，通过 `stroke` 颜色和粗细区分类别
2. 使用 `classDef` 定义样式类，避免逐个节点写 `style`
3. 使用 `style` 指令时必须同时指定 `stroke`，否则深色模式下边框模糊

```
classDef regular fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
classDef lts fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
```

**禁止**：通过 `extra_javascript` 调用 `mermaid.run()` 或 `mermaid.initialize()` 试图修复样式——Shadow DOM 关闭后外部无法操作，唯一有效方式是 CSS 变量继承。
