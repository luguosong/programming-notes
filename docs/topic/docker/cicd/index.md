---
title: CI/CD 流水线
---

# Docker CI/CD 流水线

## 📖 为什么要把 Docker 接入流水线？

手动构建镜像、手动推送、手动部署——环节一多就容易出错。将 Docker 镜像构建、扫描、推送集成到 CI/CD 流水线，实现：

- 🏗️ `代码合并时自动构建镜像`
- 🔍 `自动扫描漏洞，阻断不安全的镜像`
- 📤 `自动推送到镜像仓库`
- 🚀 `自动部署到测试/生产环境`

本文介绍 `GitHub Actions` 和 `Jenkins` 两种主流方案。

---

## ⚙️ GitHub Actions

### 基础：构建并推送到 Docker Hub

``` yaml title=".github/workflows/docker-build.yml"
name: 构建并推送 Docker 镜像

on:
  push:
    branches: [main]
    tags: ["v*.*.*"]   # 打 tag 时触发
  pull_request:
    branches: [main]

env:
  REGISTRY: docker.io
  IMAGE_NAME: ${{ github.repository }}   # 格式：owner/repo

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write   # 推送到 GHCR 需要此权限

    steps:
      # 1. 检出代码
      - name: 检出代码
        uses: actions/checkout@v4

      # 2. 设置 QEMU（支持多架构构建）
      - name: 设置 QEMU
        uses: docker/setup-qemu-action@v3

      # 3. 设置 Docker Buildx
      - name: 设置 Docker Buildx
        uses: docker/setup-buildx-action@v3

      # 4. 登录 Docker Hub
      - name: 登录 Docker Hub
        if: github.event_name != 'pull_request'
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      # 5. 生成镜像标签（自动从 branch/tag/PR 生成）
      - name: 生成镜像元数据
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=ref,event=pr
            type=semver,pattern={{version}}
            type=semver,pattern={{major}}.{{minor}}
            type=sha,prefix=sha-

      # 6. 构建并推送
      - name: 构建并推送镜像
        uses: docker/build-push-action@v5
        with:
          context: .
          platforms: linux/amd64,linux/arm64   # 多架构
          push: ${{ github.event_name != 'pull_request' }}
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha          # 使用 GitHub Actions 缓存
          cache-to: type=gha,mode=max
```

### 进阶：包含 Trivy 安全扫描

``` yaml title=".github/workflows/docker-secure.yml"
name: 安全构建流水线

on:
  push:
    branches: [main]

jobs:
  build-scan-push:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      security-events: write   # 上传 SARIF 报告到 GitHub Security 需要

    steps:
      - name: 检出代码
        uses: actions/checkout@v4

      - name: 设置 Docker Buildx
        uses: docker/setup-buildx-action@v3

      # 构建镜像（先不推送，扫描通过后再推送）
      - name: 构建镜像（本地）
        uses: docker/build-push-action@v5
        with:
          context: .
          load: true   # 加载到本地 Docker daemon
          tags: myapp:${{ github.sha }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

      # Trivy 漏洞扫描
      - name: Trivy 漏洞扫描
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: myapp:${{ github.sha }}
          format: sarif
          output: trivy-results.sarif
          severity: CRITICAL,HIGH
          exit-code: "1"          # 发现高危漏洞时失败
          ignore-unfixed: true    # 忽略未修复的漏洞

      # 将扫描结果上传到 GitHub Security 页面
      - name: 上传扫描结果到 GitHub Security
        if: always()   # 即使扫描失败也上传结果
        uses: github/codeql-action/upload-sarif@v3
        with:
          sarif_file: trivy-results.sarif

      # 扫描通过后登录并推送
      - name: 登录 Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKERHUB_USERNAME }}
          password: ${{ secrets.DOCKERHUB_TOKEN }}

      - name: 推送镜像
        run: |
          docker tag myapp:${{ github.sha }} myorg/myapp:latest
          docker tag myapp:${{ github.sha }} myorg/myapp:${{ github.sha }}
          docker push myorg/myapp:latest
          docker push myorg/myapp:${{ github.sha }}
```

### 推送到 GitHub Container Registry (GHCR)

``` yaml
# 登录 GHCR（使用自动提供的 GITHUB_TOKEN）
- name: 登录 GHCR
  uses: docker/login-action@v3
  with:
    registry: ghcr.io
    username: ${{ github.actor }}
    password: ${{ secrets.GITHUB_TOKEN }}

# 镜像名使用 ghcr.io/owner/repo 格式
- name: 构建并推送到 GHCR
  uses: docker/build-push-action@v5
  with:
    push: true
    tags: ghcr.io/${{ github.repository }}:latest
```

### 推送到私有 Harbor

``` yaml
- name: 登录私有 Harbor
  uses: docker/login-action@v3
  with:
    registry: registry.example.com
    username: ${{ secrets.HARBOR_USERNAME }}
    password: ${{ secrets.HARBOR_PASSWORD }}

- name: 构建并推送到 Harbor
  uses: docker/build-push-action@v5
  with:
    push: true
    tags: registry.example.com/myproject/myapp:${{ github.sha }}
```

### 构建后自动部署

``` yaml
  deploy:
    needs: build-scan-push   # 等待构建扫描推送完成
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'

    steps:
      # 通过 SSH 连接服务器执行部署
      - name: SSH 部署到生产服务器
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            docker pull myorg/myapp:${{ github.sha }}
            docker stop myapp || true
            docker rm myapp || true
            docker run -d \
              --name myapp \
              --restart always \
              -p 8080:8080 \
              myorg/myapp:${{ github.sha }}
```

---

## 🔧 Jenkins

### Jenkinsfile（声明式流水线）

``` groovy title="Jenkinsfile"
pipeline {
    agent any

    environment {
        // Harbor 仓库地址
        REGISTRY      = 'registry.example.com'
        // Harbor 项目/镜像名
        IMAGE_NAME    = 'myproject/myapp'
        // Jenkins Credentials 中配置的 Harbor 凭据 ID
        REGISTRY_CRED = 'harbor-credentials'
        // 镜像标签：使用 Git commit SHA
        IMAGE_TAG     = "${env.GIT_COMMIT?.take(8) ?: 'latest'}"
        FULL_IMAGE    = "${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
    }

    stages {
        stage('检出代码') {
            steps {
                checkout scm
            }
        }

        stage('构建镜像') {
            steps {
                sh """
                    docker build \
                      -t ${FULL_IMAGE} \
                      --label "git.commit=${env.GIT_COMMIT}" \
                      --label "build.number=${env.BUILD_NUMBER}" \
                      .
                """
            }
        }

        stage('安全扫描') {
            steps {
                // 使用 Trivy 扫描，CRITICAL 漏洞时失败
                sh """
                    trivy image \
                      --exit-code 1 \
                      --severity CRITICAL \
                      --ignore-unfixed \
                      --no-progress \
                      ${FULL_IMAGE}
                """
            }
            post {
                always {
                    // 保存扫描报告
                    sh """
                        trivy image \
                          --format json \
                          --output trivy-report.json \
                          ${FULL_IMAGE}
                    """
                    archiveArtifacts artifacts: 'trivy-report.json', allowEmptyArchive: true
                }
            }
        }

        stage('推送镜像') {
            when {
                branch 'main'   // 只有 main 分支才推送
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${REGISTRY_CRED}",
                    usernameVariable: 'REGISTRY_USER',
                    passwordVariable: 'REGISTRY_PASS'
                )]) {
                    sh """
                        echo "${REGISTRY_PASS}" | docker login ${REGISTRY} -u "${REGISTRY_USER}" --password-stdin
                        docker push ${FULL_IMAGE}
                        # 同时打 latest 标签
                        docker tag ${FULL_IMAGE} ${REGISTRY}/${IMAGE_NAME}:latest
                        docker push ${REGISTRY}/${IMAGE_NAME}:latest
                    """
                }
            }
        }

        stage('部署到测试环境') {
            when {
                branch 'main'
            }
            steps {
                sshagent(['deploy-server-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no deploy@test.example.com \
                          "docker pull ${FULL_IMAGE} && \
                           docker stop myapp || true && \
                           docker rm myapp || true && \
                           docker run -d --name myapp --restart always \
                             -p 8080:8080 ${FULL_IMAGE}"
                    """
                }
            }
        }
    }

    post {
        always {
            // 清理本地构建的镜像（节省磁盘）
            sh "docker rmi ${FULL_IMAGE} || true"
            // 清理 Jenkins workspace
            cleanWs()
        }
        success {
            echo "✅ 流水线成功：${FULL_IMAGE}"
        }
        failure {
            echo "❌ 流水线失败，请检查日志"
        }
    }
}
```

### Jenkins 配置要求

1. `安装插件`：
   - Docker Pipeline
   - SSH Agent
   - Credentials Binding

2. `配置 Credentials`：
   Jenkins → Manage Jenkins → Credentials → 新建 "Username with password"，ID 填 `harbor-credentials`。

3. `Jenkins 节点需要`：
   - 安装 Docker CLI（与 Jenkins 进程在同一节点）
   - Jenkins 用户加入 `docker` 组：`sudo usermod -aG docker jenkins`
   - 安装 Trivy：`sudo apt-get install trivy`

---

## 🏗️ 一套镜像怎么同时跑 x86 和 ARM？

在 GitHub Actions 中构建同时支持 `linux/amd64`（x86）和 `linux/arm64`（Apple M系列、ARM服务器）的镜像：

``` yaml
- name: 设置 QEMU（支持 ARM 模拟）
  uses: docker/setup-qemu-action@v3

- name: 设置 Buildx
  uses: docker/setup-buildx-action@v3

- name: 构建多架构镜像
  uses: docker/build-push-action@v5
  with:
    platforms: linux/amd64,linux/arm64
    push: true
    tags: myorg/myapp:latest
```

---

## ⚡ 构建太慢怎么办？——缓存优化

### 利用层缓存加速构建

在 Dockerfile 中，`将变化频率低的层放在前面`，充分利用构建缓存：

``` dockerfile
# ✅ 先复制依赖文件（变化少），再复制源码（变化多）
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .                    # 依赖文件先复制
RUN mvn dependency:go-offline     # 下载依赖（可缓存）
COPY src ./src                    # 源码后复制
RUN mvn package -DskipTests

# ❌ 直接复制全部，pom.xml 不变时也会因源码变化导致缓存失效
COPY . .
RUN mvn package -DskipTests
```

### GitHub Actions 缓存

``` yaml
- name: 构建（使用 GitHub Actions 缓存）
  uses: docker/build-push-action@v5
  with:
    cache-from: type=gha
    cache-to: type=gha,mode=max   # mode=max 缓存所有中间层
    ...
```

### Registry 缓存（多机器共享）

``` yaml
- name: 构建（使用 Registry 缓存）
  uses: docker/build-push-action@v5
  with:
    cache-from: type=registry,ref=myorg/myapp:buildcache
    cache-to: type=registry,ref=myorg/myapp:buildcache,mode=max
    ...
```
