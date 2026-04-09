<h1 align="center">programming-notes</h1>

<p align="center">
  <span>学习编程过程中整理的笔记集合，涵盖 Java、前端、数学、英语等多个领域</span>
</p>

<p align="center">
  <img alt="GitHub repo size" src="https://img.shields.io/github/repo-size/luguosong/programming-notes">
  <img alt="GitHub Created At" src="https://img.shields.io/github/created-at/luguosong/programming-notes">
  <img alt="GitHub License" src="https://img.shields.io/github/license/luguosong/programming-notes">
</p>

## 本地运行

### 前置条件

- Python 3（用于运行 Zensical）
- Node.js（用于前端示例项目）
- Java 17 + Maven（用于 Java 示例项目）
- Docker（用于本地基础环境）

### 安装 Zensical

```shell
pip install zensical
```

### 启动文档站点

```shell
# 本地预览（默认 http://localhost:7000）
zensical serve

# 编译为静态网页（输出到 site/）
zensical build
```

> 具体操作请查看 [Zensical 文档](https://zensical.org/docs/get-started/)。

### 启动本地基础环境

基础环境配置位于 `environment/docker-compose.yml`，包含 MySQL、Redis、RabbitMQ、Nacos。

```shell
# 启动所有服务
docker compose -f environment/docker-compose.yml up -d

# 查看状态
docker compose -f environment/docker-compose.yml ps

# 停止
docker compose -f environment/docker-compose.yml down
```

| 服务 | 地址 | 账号 / 密码 |
|------|------|------------|
| MySQL 8.0 | `localhost:23306` | `root` / `12345678` |
| Redis 7 | `localhost:26379` | 无密码 |
| RabbitMQ 3 | `localhost:25672` / `http://localhost:35672` | `guest` / `guest` |
| Nacos 2.3 | `http://localhost:28848/nacos` | `nacos` / `nacos` |

## 项目结构

```
programming-notes/
├── docs/            # 文档内容（Markdown）
├── code/            # 与笔记对应的示例代码
├── environment/     # 本地开发基础环境（Docker Compose）
├── overrides/       # Zensical 主题覆盖文件
├── site/            # 构建产物（勿手动修改）
└── zensical.toml    # 站点配置（导航、主题、Markdown 扩展）
```

`docs/` 中的笔记通常对应 `code/` 中的示例代码。
