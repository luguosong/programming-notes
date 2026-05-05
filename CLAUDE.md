# CLAUDE.md

## 仓库简介

**陆国松的编程笔记**（programming-notes）— 多语言、多模块的学习笔记集合：
- **文档站点**：基于 [zensical](https://zensical.org/) 构建的静态文档站点（`docs/` 目录）
- **书籍配套代码**：Spring Security / OAuth2 等模块（`code/document-translation/`）
- **前端示例代码**：React + Vite 独立小项目（`code/frontend/`）

## 常用命令

### 本地基础环境（Docker Compose）

配置位于 `environment/docker-compose.yml`（MySQL 8.0 / Redis 7 / RabbitMQ 3 / Nacos 2.3）：

```shell
docker compose -f environment/docker-compose.yml up -d    # 启动
docker compose -f environment/docker-compose.yml ps        # 状态
docker compose -f environment/docker-compose.yml down      # 停止
```

服务连接信息见 `README.md`。

### 文档站点（zensical）

```shell
pip install zensical          # 安装
zensical serve                # 预览（http://localhost:7000）
zensical build                # 编译到 site/
```

### Java / Spring Boot 示例

独立 Maven 模块（Java 17，Spring Boot 3.5.0），进入对应目录后：

```shell
mvn clean package -DskipTests  # 构建
mvn test                        # 全部测试
mvn test -Dtest=ClassName#methodName  # 单个测试方法
mvn spring-boot:run            # 启动应用
```

### 前端 React 示例

独立 Vite 项目，进入对应目录后：

```shell
npm install    # 安装依赖
npm run dev    # 开发模式
npm run build  # 生产构建
```

### HTML 示例引用规范

HTML 代码示例放在 `docs/frontend/html/*/demo/` 目录。文档中引用时：
- `--8<--` 使用项目根目录的完整路径：`--8<-- "docs/frontend/html/SECTION/demo/FILE.html"`
- `<iframe>` 的 `src` 使用相对路径：`src="demo/FILE.html"`

## 架构约定

### docs/ 与 code/ 的对应关系

`docs/` 笔记对应 `code/` 示例代码：
- `docs/document-translation/.../02-hello/` ↔ `code/document-translation/.../ssia-ch2-ex1/`
- `docs/java/javase/file/` ↔ `code/java/javase/file/`（file-basic、nio-file）

### zensical 站点

配置集中在 `zensical.toml`。**新增页面必须在 `nav` 部分注册**，否则不显示在导航中。

### Java 模块约定

`code/java/` 采用两级聚合 POM 组织（顶层 `code/java/pom.xml` → 子聚合 POM）。

- 新建子模块后**必须**在对应聚合 POM 的 `<modules>` 中添加
- **一个模块只演示一个功能维度**（如 `jdbc-connection`、`jdbc-statement`，而非 `jdbc-demo`）
- 模块名称自描述功能边界

### 前端技术栈

React 19.2 + Vite 7 + TypeScript · Ant Design 6 · MobX 6 · React Router 7

## 写作规范

写作风格、文档结构、Zensical 语法已拆分到 `.claude/rules/`：
- `writing-style.md` — 写作风格与标题架构
- `doc-structure.md` — 文档结构与导航规则
- `zensical.md` — Zensical 特有语法
