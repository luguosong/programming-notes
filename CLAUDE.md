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
│   │   └── js/              # 自定义脚本（mathjax.js、mermaid.js 等）
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

文档写作风格（教学风格、Emoji、行内强调、标题架构）和文档结构规范（目录规则、nav 注册、交叉引用、图标、标题一致性、图片）已分别拆分到：

- `.claude/rules/writing-style.md` — 写作风格与标题架构
- `.claude/rules/doc-structure.md` — 文档结构与导航规则
- `.claude/rules/zensical.md` — Zensical 特有语法
