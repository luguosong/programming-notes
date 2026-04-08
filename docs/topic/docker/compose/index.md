---
title: Docker Compose
---

# Docker Compose

## 📖 一条命令管好多个容器——Docker Compose 是什么？

当你的应用需要同时运行数据库、缓存、消息队列等多个服务时，逐个 `docker run` 不仅繁琐还容易出错。Docker Compose 就是为这种场景设计的：通过一个 `docker-compose.yml` 文件，将所有服务、网络、卷统一配置，一条命令启动整个环境。

???+ note "版本说明"
    Docker Compose V2 已内置于 Docker Desktop 和新版 Docker Engine，命令从 `docker-compose`（V1，需单独安装）改为 `docker compose`（V2，空格而非连字符）。本文统一使用 V2 语法。

---

## 🚀 快速入门

以一个 `Spring Boot + MySQL + Redis` 应用为例：

``` yaml title="docker-compose.yml"
services:
  # 应用服务
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/mydb?useSSL=false
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: secret
      SPRING_REDIS_HOST: cache
    depends_on:
      db:
        condition: service_healthy
      cache:
        condition: service_started
    networks:
      - backend

  # 数据库服务
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: mydb
    volumes:
      - mysql-data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - backend

  # 缓存服务
  cache:
    image: redis:7-alpine
    volumes:
      - redis-data:/data
    networks:
      - backend

# 具名卷
volumes:
  mysql-data:
  redis-data:

# 自定义网络
networks:
  backend:
    driver: bridge
```

``` bash
# 启动所有服务（后台运行）
docker compose up -d

# 查看服务状态
docker compose ps

# 停止并删除所有容器
docker compose down
```

---

## ⌨️ 核心命令

### 生命周期

``` bash
# 启动（后台运行，自动重建有变更的镜像）
docker compose up -d --build

# 只启动指定服务
docker compose up -d app db

# 停止所有服务（保留容器和卷）
docker compose stop

# 停止并删除容器、网络（保留卷）
docker compose down

# 停止并删除容器、网络、卷
docker compose down -v

# 停止并删除容器、网络、卷及本地镜像
docker compose down -v --rmi local
```

### 查看状态

``` bash
# 查看所有服务状态
docker compose ps

# 查看服务日志
docker compose logs

# 实时跟踪指定服务日志
docker compose logs -f app

# 查看资源占用
docker compose top
```

### 调试与操作

``` bash
# 进入指定服务的容器
docker compose exec app bash

# 在指定服务容器中运行命令（不进入交互式）
docker compose exec db mysqladmin -uroot -psecret status

# 单独重启某个服务
docker compose restart app

# 扩容：将 app 服务扩展到 3 个实例（需配置 load balancer）
docker compose up -d --scale app=3
```

### 镜像管理

``` bash
# 构建所有服务的镜像（不启动）
docker compose build

# 强制重新构建（忽略缓存）
docker compose build --no-cache

# 拉取所有服务用到的外部镜像
docker compose pull

# 删除构建产生的镜像
docker compose down --rmi local
```

---

## 📝 配置文件详解

### 顶层结构

``` yaml
# compose 文件版本（V2 可省略，建议省略）
# version: "3.9"

services:    # 定义各个服务（必填）
  ...

networks:    # 定义网络（可选，不定义则使用默认网络）
  ...

volumes:     # 定义卷（可选）
  ...

configs:     # 定义配置文件（Swarm 模式使用）
  ...

secrets:     # 定义密钥（Swarm 模式使用）
  ...
```

### services 配置

#### 镜像来源

``` yaml
services:
  # 方式一：直接使用已有镜像
  db:
    image: mysql:8.0

  # 方式二：基于 Dockerfile 构建
  app:
    build:
      context: .          # 构建上下文（Dockerfile 所在目录）
      dockerfile: Dockerfile.prod   # 指定 Dockerfile 文件名（默认 Dockerfile）
      args:               # 传递 ARG 构建参数
        APP_ENV: production
      target: runner      # 多阶段构建时指定目标阶段
      cache_from:         # 构建缓存来源
        - myapp:latest

  # 方式三：构建 + 指定最终镜像名
  api:
    build: ./api
    image: myorg/api:1.0  # 构建后使用此名称
```

#### 端口映射

``` yaml
services:
  web:
    ports:
      - "8080:80"           # 宿主机:容器
      - "443:443"
      - "127.0.0.1:9229:9229"  # 仅绑定本地回环地址（更安全）
      - target: 80          # 对象语法
        host_ip: "127.0.0.1"
        published: "8080"
        protocol: tcp
```

#### 环境变量

``` yaml
services:
  app:
    # 方式一：直接写入
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: db
      DB_PORT: "3306"

    # 方式二：引用宿主机环境变量（值为空时从宿主机继承）
    environment:
      - SPRING_PROFILES_ACTIVE
      - DB_PASSWORD       # 从宿主机 $DB_PASSWORD 继承

    # 方式三：从 .env 文件加载
    env_file:
      - .env
      - .env.local        # 会覆盖 .env 中的同名变量
```

``` text title=".env"
DB_PASSWORD=supersecret
REDIS_URL=redis://cache:6379
```

#### 卷挂载

``` yaml
services:
  app:
    volumes:
      # 具名卷（需在顶层 volumes 声明）
      - app-data:/app/data

      # bind mount：将宿主机目录挂载到容器（绝对或相对路径）
      - ./config:/app/config:ro   # :ro 只读挂载

      # 匿名卷（容器删除后自动清理）
      - /app/tmp

      # 对象语法（更清晰）
      - type: bind
        source: ./logs
        target: /app/logs
```

#### 依赖关系

``` yaml
services:
  app:
    depends_on:
      # 简写：db 容器启动后再启动 app（不等待服务就绪）
      # db:
      #   condition: service_started

      # 等待健康检查通过后再启动
      db:
        condition: service_healthy
      cache:
        condition: service_started
      # service_completed_successfully：等待一次性任务容器执行成功
      migration:
        condition: service_completed_successfully
```

#### 重启策略

``` yaml
services:
  app:
    restart: always          # 总是重启
    # restart: "no"          # 不重启（默认）
    # restart: on-failure    # 退出码非 0 时重启
    # restart: unless-stopped  # 除非手动停止，否则总是重启
```

#### 健康检查

``` yaml
services:
  db:
    image: mysql:8.0
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p$$MYSQL_ROOT_PASSWORD"]
      interval: 10s       # 检查间隔
      timeout: 5s         # 单次超时
      retries: 5          # 失败重试次数
      start_period: 30s   # 启动宽限期（此期间失败不计入 retries）
```

???+ tip "环境变量转义"
    在 `healthcheck.test` 中引用环境变量时，需要用 `$$` 转义（避免 Compose 提前解析），实际传给 shell 时为 `$`。

#### 资源限制

``` yaml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: "0.50"     # 最多使用 0.5 个 CPU
          memory: 512M     # 最多使用 512 MB 内存
        reservations:
          cpus: "0.25"     # 保留 0.25 个 CPU
          memory: 256M
```

#### 网络配置

``` yaml
services:
  app:
    networks:
      - frontend
      - backend
    # 指定容器在网络内的别名
    networks:
      backend:
        aliases:
          - api-service
```

#### 其他常用配置

``` yaml
services:
  app:
    # 容器名称（不指定则自动生成）
    container_name: myapp

    # 工作目录
    working_dir: /app

    # 覆盖镜像的 ENTRYPOINT
    entrypoint: ["/bin/sh", "-c"]

    # 覆盖镜像的 CMD
    command: ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]

    # 共享宿主机的 IPC 命名空间（高性能场景）
    ipc: host

    # 特权模式（谨慎使用）
    privileged: false

    # 映射宿主机端口（不对外暴露，仅服务发现用）
    expose:
      - "8080"

    # 自定义 hosts（写入容器 /etc/hosts）
    extra_hosts:
      - "host.docker.internal:host-gateway"

    # 日志配置
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### volumes 配置

``` yaml
volumes:
  # 默认本地驱动（Docker 自动管理路径）
  mysql-data:

  # 显式指定驱动和选项
  nfs-data:
    driver: local
    driver_opts:
      type: nfs
      o: "addr=192.168.1.100,rw"
      device: ":/exports/data"

  # 引用外部已存在的卷（不会被 compose down 删除）
  legacy-data:
    external: true
    name: old-volume-name
```

### networks 配置

``` yaml
networks:
  # 默认 bridge 网络
  backend:
    driver: bridge
    # 自定义子网
    ipam:
      config:
        - subnet: "172.20.0.0/16"

  # 引用外部已存在的网络
  existing-net:
    external: true

  # 使用宿主机网络
  host-network:
    driver: host
```

---

## 🌍 多环境配置

### 使用多个 Compose 文件叠加

``` bash
# 基础配置 + 开发环境覆盖
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# 基础配置 + 生产环境覆盖
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

``` yaml title="docker-compose.yml（基础配置）"
services:
  app:
    image: myapp:latest
    environment:
      SPRING_PROFILES_ACTIVE: default
```

``` yaml title="docker-compose.dev.yml（开发覆盖）"
services:
  app:
    build: .              # 开发时从源码构建
    volumes:
      - .:/app            # 挂载源码热更新
    environment:
      SPRING_PROFILES_ACTIVE: dev
    ports:
      - "5005:5005"       # 开启远程调试端口
```

``` yaml title="docker-compose.prod.yml（生产覆盖）"
services:
  app:
    restart: always
    environment:
      SPRING_PROFILES_ACTIVE: prod
    deploy:
      resources:
        limits:
          memory: 1G
```

### 使用 .env 文件注入变量

``` text title=".env"
APP_VERSION=1.0.0
DB_PASSWORD=prod-secret
EXPOSE_PORT=80
```

``` yaml title="docker-compose.yml"
services:
  app:
    image: myapp:${APP_VERSION}   # 引用 .env 变量
    ports:
      - "${EXPOSE_PORT}:8080"
  db:
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
```

---

## 🏗️ 完整项目示例

以下是一个包含 `Nginx 反向代理 + Spring Boot + MySQL + Redis` 的完整配置：

``` yaml title="docker-compose.yml"
services:
  # Nginx 反向代理
  nginx:
    image: nginx:1.25-alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - ./nginx/certs:/etc/nginx/certs:ro
    depends_on:
      - app
    networks:
      - frontend
    restart: unless-stopped

  # Spring Boot 应用（两个实例）
  app:
    build:
      context: .
      target: runner
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/${DB_NAME}
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_REDIS_HOST: cache
      SPRING_REDIS_PORT: 6379
    depends_on:
      db:
        condition: service_healthy
      cache:
        condition: service_started
    networks:
      - frontend
      - backend
    restart: unless-stopped
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 512M
    logging:
      driver: json-file
      options:
        max-size: "10m"
        max-file: "5"

  # MySQL 数据库
  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    networks:
      - backend
    restart: unless-stopped

  # Redis 缓存
  cache:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD} --maxmemory 256mb --maxmemory-policy allkeys-lru
    volumes:
      - redis-data:/data
    networks:
      - backend
    restart: unless-stopped

volumes:
  mysql-data:
  redis-data:

networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge
```

``` text title=".env"
DB_NAME=myapp
DB_USER=appuser
DB_PASSWORD=your-password
DB_ROOT_PASSWORD=your-root-password
REDIS_PASSWORD=your-redis-password
```

---

## 👀 Watch 模式（实时开发）

Docker Compose V2.22+ 引入了 `watch` 功能，监听本地文件变更后自动同步或重建容器，无需手动重启。

### 基本配置

在服务的 `develop.watch` 中定义监听规则：

``` yaml title="compose.yaml"
services:
  web:
    build: .
    ports:
      - "8080:8080"
    develop:
      watch:
        # 同步文件变更（无需重建，适合前端资源、配置文件）
        - action: sync
          path: ./src/main/resources/static
          target: /app/static

        # 同步并触发容器内命令（如热重载）
        - action: sync+restart
          path: ./src/main/resources/application.yml
          target: /app/config/application.yml

        # 重建镜像（依赖变化时才需要）
        - action: rebuild
          path: ./pom.xml
```

| action | 行为 | 适用场景 |
|--------|------|---------|
| `sync` | 将变更文件直接复制到运行中的容器 | 静态资源、模板文件 |
| `sync+restart` | 复制文件后重启容器进程 | 配置文件变更 |
| `rebuild` | 重新构建镜像并替换容器 | 依赖变更（`pom.xml`、`package.json`） |

### 启动 Watch

``` bash
# 启动服务并开启文件监听
docker compose watch

# 仅监听指定服务
docker compose watch web

# 服务已在运行时，仅开启监听（不重新启动）
docker compose watch --no-up

# 静默模式（不显示构建输出）
docker compose watch --quiet
```

???+ tip "Watch vs volume bind mount"
    | 方式 | 优势 | 劣势 |
    |------|------|------|
    | `bind mount`（`-v ./src:/app/src`） | 简单，即时生效 | 无法按文件类型区分行为、无法自动重建 |
    | `watch` | 声明式配置，区分 sync/rebuild | 需要 Compose V2.22+，初次配置稍复杂 |

    开发环境推荐使用 `watch`，精确控制每种文件变更的处理方式。

---

## 👤 Profiles（按环境选择性启动）

当不同环境需要不同的服务集合时，可以用 `profiles` 给服务打标签，按需启动。

### 基本用法

``` yaml title="compose.yaml"
services:
  # 核心服务（始终启动）
  app:
    build: .
    ports:
      - "8080:8080"

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: secret

  # 调试工具（仅 debug profile 时启动）
  adminer:
    image: adminer
    ports:
      - "8888:8080"
    profiles:
      - debug

  # Redis（仅 cache profile 时启动）
  redis:
    image: redis:7-alpine
    profiles:
      - cache

  # 监控栈（仅 monitoring profile 时启动）
  prometheus:
    image: prom/prometheus
    profiles:
      - monitoring
  grafana:
    image: grafana/grafana
    profiles:
      - monitoring
```

``` bash
# 默认启动：只运行 app 和 db（无 profile 标签的服务）
docker compose up -d

# 同时启动 debug profile 的服务
docker compose --profile debug up -d
# → app + db + adminer

# 启动多个 profile
docker compose --profile debug --profile cache up -d
# → app + db + adminer + redis

# 只启动某个 profile 的服务（不含无标签服务）
docker compose --profile monitoring up -d --no-deps prometheus grafana
```

### 自动激活 Profile

也可以通过环境变量自动激活：

``` bash
# 设置默认激活的 profile
export COMPOSE_PROFILES=debug,cache
docker compose up -d
# 等同于 docker compose --profile debug --profile cache up -d
```

???+ tip "Profiles vs 多个 Compose 文件"
    - `Profiles`：适合少量服务的开关（如调试工具、监控组件），一个文件搞定
    - `多个 Compose 文件叠加`：适合整个服务配置的大幅差异（如开发 vs 生产的不同端口、资源限制）

    两者可以组合使用：基础配置 + Profile 切换调试工具 + 文件叠加覆盖环境参数。
