# 镜像安全扫描

## 📖 概述

容器镜像可能包含操作系统包、应用依赖中的已知安全漏洞（CVE）。在 CI/CD 流水线中集成镜像扫描，可在部署前发现并修复高危漏洞。

常用工具：

| 工具 | 特点 |
|------|------|
| `Trivy` | CNCF 项目，扫描速度快，支持 OS 包、语言依赖、IaC、Secret |
| `Docker Scout` | Docker 官方内置，与 Docker Desktop / Hub 集成 |
| `Grype` | Anchore 出品，支持多种格式 |
| `Snyk` | SaaS + CLI，商业支持好 |

本文重点介绍 `Trivy` 和 `Docker Scout`。

---

## 🔍 Trivy

### 安装

=== "Linux"

    ``` bash
    # 通过包管理器安装（以 Ubuntu 为例）
    sudo apt-get install wget apt-transport-https gnupg lsb-release
    wget -qO - https://aquasecurity.github.io/trivy-repo/deb/public.key | sudo apt-key add -
    echo "deb https://aquasecurity.github.io/trivy-repo/deb $(lsb_release -sc) main" | \
      sudo tee -a /etc/apt/sources.list.d/trivy.list
    sudo apt-get update
    sudo apt-get install trivy
    ```

=== "macOS"

    ``` bash
    brew install trivy
    ```

=== "Docker（无需安装）"

    ``` bash
    # 直接用 Docker 运行 Trivy
    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
      aquasec/trivy image nginx:latest
    ```

=== "Windows"

    从 [GitHub Releases](https://github.com/aquasecurity/trivy/releases) 下载 Windows 可执行文件，加入 PATH。

### 扫描镜像

``` bash
# 扫描本地镜像
trivy image nginx:latest

# 扫描远程镜像（自动拉取）
trivy image python:3.11-slim

# 只报告高危和严重漏洞
trivy image --severity HIGH,CRITICAL nginx:latest

# 忽略未修复的漏洞（减少噪音）
trivy image --ignore-unfixed nginx:latest

# 输出为 JSON（便于程序解析）
trivy image --format json --output result.json nginx:latest

# 输出为 SARIF（GitHub Code Scanning 格式）
trivy image --format sarif --output result.sarif nginx:latest

# 扫描本地 tar 格式镜像
trivy image --input myapp.tar
```

### 扫描结果示例

```
nginx:latest (debian 12.4)
==========================
Total: 142 (UNKNOWN: 0, LOW: 94, MEDIUM: 35, HIGH: 11, CRITICAL: 2)

┌───────────────┬────────────────┬──────────┬───────────────────┬──────────────────┬────────────────────────────────────────┐
│    Library    │ Vulnerability  │ Severity │ Installed Version │  Fixed Version   │                 Title                  │
├───────────────┼────────────────┼──────────┼───────────────────┼──────────────────┼────────────────────────────────────────┤
│ openssl       │ CVE-2024-xxxx  │ CRITICAL │ 3.0.11-1          │ 3.0.13-1         │ OpenSSL: ...                           │
└───────────────┴────────────────┴──────────┴───────────────────┴──────────────────┴────────────────────────────────────────┘
```

### 扫描 Dockerfile（构建前检测）

``` bash
# 扫描 Dockerfile 中的配置问题（Misconfig）
trivy config Dockerfile

# 扫描整个项目目录（Dockerfile + IaC + 依赖文件）
trivy fs .

# 扫描 Git 仓库中的 Secret（API Key、密码等）
trivy fs --scanners secret .
```

### 扫描语言依赖

``` bash
# Java（扫描 pom.xml / build.gradle）
trivy fs --scanners vuln ./pom.xml

# Node.js（扫描 package-lock.json）
trivy fs --scanners vuln ./package-lock.json

# Python（扫描 requirements.txt）
trivy fs --scanners vuln ./requirements.txt
```

### 设置漏洞阈值（CI 中使用）

``` bash
# 发现 CRITICAL 漏洞时以非零状态码退出（触发 CI 失败）
trivy image --exit-code 1 --severity CRITICAL myapp:latest

# 发现 HIGH 或 CRITICAL 时失败，同时忽略未修复
trivy image \
  --exit-code 1 \
  --severity HIGH,CRITICAL \
  --ignore-unfixed \
  myapp:latest
```

### 忽略特定 CVE

创建 `.trivyignore` 文件：

``` text title=".trivyignore"
# 格式：CVE-ID  # 可选的注释
CVE-2023-12345  # 已评估，不影响我们的使用场景
CVE-2023-67890
```

---

## 🛡️ Docker Scout

Docker Scout 是 Docker 官方提供的镜像分析工具，已内置于 Docker Desktop 4.17+。

### 基本使用

``` bash
# 查看镜像漏洞概览
docker scout cves nginx:latest

# 只显示严重和高危漏洞
docker scout cves --only-severity critical,high nginx:latest

# 与基础镜像对比，查看新增漏洞
docker scout compare myapp:2.0 --to myapp:1.0

# 给出修复建议（升级到哪个基础镜像可解决更多漏洞）
docker scout recommendations myapp:latest

# 查看 SBOM（软件物料清单）
docker scout sbom nginx:latest
```

### 在 Docker Desktop 中使用

Docker Desktop 的镜像列表页面会自动显示漏洞数量，点击镜像可查看详细的 CVE 报告和修复建议。

---

## ✅ 最佳实践

### 使用最小化基础镜像

``` dockerfile
# ❌ 使用完整发行版，漏洞面大
FROM ubuntu:22.04

# ✅ 使用 slim 版本
FROM python:3.11-slim

# ✅ 使用 alpine（更小，但注意兼容性）
FROM node:20-alpine

# ✅ 使用 distroless（无 shell，攻击面最小）
FROM gcr.io/distroless/java17-debian12
```

### 及时更新基础镜像

``` bash
# 定期重新构建镜像以获取最新安全补丁
docker build --no-cache -t myapp:latest .
```

### 以非 root 用户运行

``` dockerfile
# 创建非 root 用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

### 多阶段构建减少攻击面

``` dockerfile
# 构建阶段包含编译工具（漏洞多）
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

# 运行阶段只包含 JRE（漏洞少）
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### SBOM 与供应链安全

``` bash
# 生成 SBOM（软件物料清单）
trivy image --format cyclonedx --output sbom.json myapp:1.0

# 验证镜像完整性（结合 cosign 签名）
cosign sign --key cosign.key registry.example.com/myapp:1.0
cosign verify --key cosign.pub registry.example.com/myapp:1.0
```
