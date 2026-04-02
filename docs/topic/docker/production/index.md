# 生产环境最佳实践

将 Docker 应用部署到生产环境前，需要从镜像优化、安全加固、日志管理、资源限制等维度逐一审查。本页提供一份系统化的 Checklist，可作为上线前的审查清单。

---

## 镜像优化

### 使用多阶段构建

构建阶段包含编译工具和完整 SDK（漏洞多、体积大），运行阶段只保留最小运行时：

``` dockerfile title="Dockerfile"
# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# 运行阶段（最终镜像）
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 选择最小化基础镜像

| 基础镜像 | 大小 | 安全性 | 兼容性 |
|---------|------|-------|--------|
| `ubuntu:22.04` | ~77 MB | 漏洞多 | 最好 |
| `eclipse-temurin:17-jre-alpine` | ~85 MB | 较好 | 良好（部分 glibc 依赖可能不兼容） |
| `gcr.io/distroless/java17` | ~100 MB | 最好（无 shell） | 需适应无 shell 调试 |

???+ tip "distroless 注意事项"
    `distroless` 镜像没有 `sh`/`bash`，无法 `docker exec` 进入。调试时可临时使用 `debug` 变体：
    ``` bash
    # 调试时使用带 shell 的变体
    gcr.io/distroless/java17-debian12:debug
    ```

### 优化构建缓存

``` dockerfile
# ✅ 先复制依赖文件（变化少，缓存命中率高）
COPY pom.xml .
RUN mvn dependency:go-offline

# 再复制源码（变化频繁，放在最后）
COPY src ./src
RUN mvn package -DskipTests
```

### 固定镜像版本标签

``` dockerfile
# ❌ 使用 latest，构建不可复现
FROM nginx:latest

# ✅ 使用精确版本号
FROM nginx:1.25.4-alpine
```

---

## 安全加固

### 以非 root 用户运行

``` dockerfile
# 创建专用用户和组
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

### 限制容器能力（Capabilities）

Linux 内核默认赋予容器大量能力，生产环境应`最小化授权`：

``` yaml title="compose.yaml"
services:
  app:
    image: myapp:1.0
    # 丢弃所有能力
    cap_drop:
      - ALL
    # 只添加必要的能力
    cap_add:
      - NET_BIND_SERVICE  # 绑定 1024 以下端口
    # 禁止提权
    security_opt:
      - no-new-privileges:true
    # 只读根文件系统（配合 tmpfs 写入临时文件）
    read_only: true
    tmpfs:
      - /tmp
```

### 使用 Secret 管理敏感数据

``` bash
# ❌ 禁止：在镜像或环境变量中硬编码密码
# -e MYSQL_ROOT_PASSWORD=secret

# ✅ 使用 Docker Secret（Swarm 模式）
echo "mysecretpassword" | docker secret create db_password -
```

在 Compose 文件中使用 Secret：

``` yaml
services:
  db:
    image: mysql:8.0
    secrets:
      - db_password
    environment:
      MYSQL_ROOT_PASSWORD_FILE: /run/secrets/db_password

secrets:
  db_password:
    file: ./secrets/db_password.txt
```

### 启用镜像签名与验证

``` bash
# 启用 Docker Content Trust（签名验证）
export DOCKER_CONTENT_TRUST=1

# 推送时自动签名
docker push myorg/myapp:1.0

# 拉取时验证签名（未签名镜像会被拒绝）
docker pull myorg/myapp:1.0
```

---

## 日志管理

### 配置日志轮转

容器默认使用 `json-file` 日志驱动，`不配置轮转会撑满磁盘`：

``` json title="/etc/docker/daemon.json"
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "10m",
    "max-file": "3"
  }
}
```

或在 Compose 文件中按服务配置：

``` yaml
services:
  app:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "5"
```

### 集中式日志收集

生产环境推荐将容器日志统一收集到日志平台：

``` yaml
services:
  app:
    logging:
      driver: "syslog"
      options:
        syslog-address: "tcp://log.example.com:514"
        tag: "myapp"
```

| 方案 | 适用场景 |
|------|---------|
| `json-file` + Filebeat | 简单场景，宿主机 Agent 采集 |
| `syslog` | 已有 Syslog 基础设施 |
| `fluentd` / `fluent-bit` | Kubernetes / 大规模容器部署 |
| Loki + Grafana | 轻量级，适合 Prometheus 生态 |

---

## 资源限制

### CPU 和内存限制

`不设资源限制的容器可能耗尽宿主机资源，导致雪崩`：

``` yaml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: "1.0"       # 最多使用 1 个 CPU 核心
          memory: 512M      # 最多使用 512 MB 内存
        reservations:
          cpus: "0.25"      # 保底资源
          memory: 128M
```

`docker run` 等效参数：

``` bash
docker run -d \
  --memory="512m" \
  --cpus="1.0" \
  myapp:1.0
```

### OOM（Out of Memory）策略

``` yaml
services:
  app:
    # 容器内存超限时的行为
    # - true：容器被 OOM Killer 杀掉（默认）
    # - false：内核优先杀掉其他非关键进程
    # 注意：生产环境应合理设置 memory limit，而非依赖 oom_kill_disable
    deploy:
      resources:
        limits:
          memory: 1G
```

---

## 健康检查

### 配置 HEALTHCHECK

``` dockerfile title="Dockerfile"
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
```

| 参数 | 说明 |
|------|------|
| `--interval` | 检查间隔（默认 30s） |
| `--timeout` | 单次超时时间（默认 30s） |
| `--start-period` | 启动宽限期，不计入失败次数（适合启动慢的应用） |
| `--retries` | 连续失败多少次标记为 unhealthy（默认 3） |

### 健康状态查看

``` bash
# 查看容器健康状态
docker inspect --format='{{.State.Health.Status}}' myapp

# 查看健康检查历史
docker inspect --format='{{json .State.Health}}' myapp | python -m json.tool
```

---

## 重启策略

| 策略 | 行为 | 适用场景 |
|------|------|---------|
| `no` | 不自动重启（默认） | 一次性任务 |
| `always` | 总是重启，包括守护进程重启后 | 核心服务 |
| `unless-stopped` | 同 always，但手动停止后不重启 | 一般服务 |
| `on-failure` | 仅退出码非 0 时重启 | 批处理任务 |

``` yaml
services:
  app:
    restart: unless-stopped
```

???+ warning "restart: always 的注意事项"
    手动 `docker stop` 的容器，在 Docker daemon 重启后**不会**被自动拉起（与 `always` 不同）。如果希望 daemon 重启后也自动拉起，使用 `always`。

---

## 生产环境 Checklist

上线前逐项核对：

### 镜像安全

- [ ] 使用多阶段构建，最终镜像不含编译工具
- [ ] 基础镜像使用精简版（`alpine` / `slim` / `distroless`）
- [ ] 镜像版本标签精确到具体版本号（非 `latest`）
- [ ] 以非 root 用户运行（`USER appuser`）
- [ ] 已通过 Trivy / Docker Scout 扫描，无 Critical 漏洞

### 运行时安全

- [ ] 已配置 `cap_drop: ALL`，只添加必要能力
- [ ] 启用 `no-new-privileges`
- [ ] 敏感数据通过 Secret 或外部密钥管理（非环境变量明文）
- [ ] 只读根文件系统（`read_only: true`）

### 资源与稳定性

- [ ] 设置了 CPU 和内存限制（`deploy.resources.limits`）
- [ ] 配置了健康检查（`HEALTHCHECK`）
- [ ] 设置了重启策略（`restart: unless-stopped`）
- [ ] 配置了日志轮转（`logging.options.max-size`）

### 可观测性

- [ ] 日志已接入集中收集系统
- [ ] 关键指标已暴露（Prometheus `/actuator/prometheus` 等）
- [ ] 健康检查端点可用于负载均衡器探活

### 网络

- [ ] 服务间通信使用自定义网络（非默认 bridge）
- [ ] 对外端口绑定到指定接口（如 `127.0.0.1:8080:8080`，而非 `0.0.0.0`）
- [ ] 使用 HTTPS（配置 TLS 证书）

???+ tip "自动化 Checklist"
    可以在 CI/CD 中使用 `hadolint`（Dockerfile Linter）和 `docker-compose config` 验证配置，自动拦截不合规项：
    ``` bash
    # Dockerfile 静态检查
    hadolint Dockerfile

    # Compose 文件语法验证
    docker compose config -q
    ```
