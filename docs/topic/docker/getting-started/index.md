# 基础入门

本节帮助你从零开始掌握 Docker 的核心概念与日常使用。

`Docker 基础`深入讲解镜像、容器、网络、数据持久化与 Dockerfile 编写——建议先完成下方快速上手，再系统阅读。

`Docker Compose`介绍多容器编排，用一个 YAML 文件管理完整应用栈。

---

## 五分钟快速上手

不需要任何 Docker 基础，跟着以下步骤亲手体验容器的完整生命周期。

### 第一步：确认环境

``` bash
# 验证 Docker 已安装并运行
docker version

# 应看到 Client 和 Server 两部分输出
# 若 Server 报错，说明 Docker daemon 未启动，需先启动 Docker Desktop（Windows/Mac）或 sudo systemctl start docker（Linux）
```

### 第二步：运行第一个容器

``` bash
# 一条命令启动 Nginx Web 服务器
docker run -d -p 8080:80 --name my-nginx nginx:latest

# 参数解读：
# -d          后台运行（detached 模式）
# -p 8080:80  将宿主机 8080 端口映射到容器 80 端口
# --name      给容器起一个名字，方便后续操作
# nginx:latest  使用的镜像名和标签
```

现在打开浏览器访问 `http://localhost:8080`，你将看到 Nginx 的欢迎页面——这就是一个运行中的容器。

### 第三步：修改容器并观察变化

``` bash
# 进入容器内部，替换欢迎页内容
docker exec -it my-nginx sh
echo "<h1>Hello from Docker!</h1>" > /usr/share/nginx/html/index.html
exit

# 刷新浏览器，页面已变为自定义内容
# 这说明容器是可写的：在镜像只读层之上有一个可写层
```

### 第四步：将修改保存为新镜像

``` bash
# 将容器的当前状态提交为新镜像
docker commit my-nginx my-nginx:custom

# 查看本地镜像列表，确认新镜像已创建
docker images
```

### 第五步：清理与停止

``` bash
# 停止并删除容器
docker stop my-nginx
docker rm my-nginx

# 从自定义镜像重新启动
docker run -d -p 8080:80 --name my-nginx-2 my-nginx:custom
# 再次访问 localhost:8080，自定义内容依然存在

# 最终清理
docker stop my-nginx-2 && docker rm my-nginx-2
```

???+ success "你刚才经历了什么？"
    1. `docker pull` — 从 Docker Hub 下载了一个镜像（`nginx:latest`）
    2. `docker run` — 基于镜像创建并启动了一个容器
    3. `docker exec` — 在运行中的容器里执行了命令
    4. `docker commit` — 把容器的修改保存成了新镜像
    5. `docker stop/rm` — 停止并删除了容器

    这就是 Docker 的核心工作流：**镜像 → 容器 → 修改 → 新镜像**，循环往复。更多细节详见「Docker 基础」。

---

## 学习路线图

``` mermaid
graph LR
    A[快速上手<br>5 分钟体验] --> B[Docker 基础<br>核心概念与命令]
    B --> C[Docker Compose<br>多容器编排]
    C --> D[生产实践<br>安全、CI/CD、集群]
    style A fill:#4A90D9,stroke:#6aaee8,color:#fff
    style B fill:#5B9F49,stroke:#7ab86a,color:#fff
    style C fill:#E09145,stroke:#eaaa6b,color:#fff
    style D fill:#C0392B,stroke:#d45d52,color:#fff
```

| 阶段 | 内容 | 适合人群 |
|------|------|---------|
| 快速上手 | 本页的五分钟教程 | 完全没有接触过 Docker |
| Docker 基础 | 架构原理、镜像、容器、网络、Dockerfile | 需要系统掌握日常操作 |
| Docker Compose | YAML 编排、多环境配置、Watch 模式 | 需要管理多服务应用 |
| 生产实践 | 安全扫描、CI/CD、Harbor、Swarm | 准备上生产环境 |
