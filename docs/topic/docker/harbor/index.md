# 私有镜像仓库 Harbor

## 概述

[Harbor](https://goharbor.io/) 是由 VMware 开源、CNCF 毕业的企业级容器镜像仓库，在 Docker Registry 基础上增加了：

- `访问控制`：基于角色（RBAC）的权限管理
- `镜像扫描`：集成 Trivy 等漏洞扫描工具
- `镜像签名`：基于 Notary 的镜像内容信任
- `镜像复制`：跨仓库自动同步
- `WebUI`：可视化管理界面
- `Webhook`：事件通知

---

## 安装 Harbor

Harbor 通过 Docker Compose 部署，需要 Docker Engine 20.10+ 和 Docker Compose 1.18+。

### 下载安装包

``` bash
# 下载最新在线安装版
wget https://github.com/goharbor/harbor/releases/download/v2.10.0/harbor-online-installer-v2.10.0.tgz

# 解压
tar xzvf harbor-online-installer-v2.10.0.tgz
cd harbor
```

### 配置 harbor.yml

``` bash
# 复制配置模板
cp harbor.yml.tmpl harbor.yml
```

``` yaml title="harbor.yml（关键配置）"
# Harbor 对外访问的地址（域名或 IP）
hostname: registry.example.com

# HTTP 配置（生产环境建议改用 HTTPS）
http:
  port: 80

# HTTPS 配置（推荐）
https:
  port: 443
  certificate: /your/certificate/path
  private_key: /your/private/key/path

# Harbor 管理员初始密码
harbor_admin_password: Harbor12345

# 数据库配置
database:
  password: root123
  max_idle_conns: 100
  max_open_conns: 900

# 数据持久化路径
data_volume: /data
```

### 安装与启动

``` bash
# 执行安装脚本（自动生成 docker-compose.yml 并启动）
sudo ./install.sh

# 如需启用镜像扫描（Trivy）
sudo ./install.sh --with-trivy

# 查看运行状态
docker compose ps

# 停止 Harbor
docker compose down -v

# 启动 Harbor
docker compose up -d
```

安装完成后访问 `http://registry.example.com`，使用 `admin / Harbor12345` 登录。

---

## 配置 Docker 客户端

### HTTP 仓库（开发环境）

在 Docker 客户端的 `/etc/docker/daemon.json` 中添加非安全仓库：

``` json title="/etc/docker/daemon.json"
{
  "insecure-registries": ["registry.example.com"]
}
```

``` bash
sudo systemctl restart docker
```

### HTTPS 仓库（推荐）

将 Harbor 的 CA 证书添加到系统信任：

``` bash
# 将 CA 证书复制到 Docker 证书目录
sudo mkdir -p /etc/docker/certs.d/registry.example.com
sudo cp ca.crt /etc/docker/certs.d/registry.example.com/ca.crt
# 无需重启 Docker
```

---

## 基本操作

### 登录与推送镜像

``` bash
# 登录
docker login registry.example.com
# 输入用户名和密码

# 给本地镜像打 Harbor 标签
# 格式：registry地址/项目名/镜像名:标签
docker tag myapp:1.0 registry.example.com/myproject/myapp:1.0

# 推送到 Harbor
docker push registry.example.com/myproject/myapp:1.0

# 拉取镜像
docker pull registry.example.com/myproject/myapp:1.0

# 登出
docker logout registry.example.com
```

### 项目管理

Harbor 通过`项目（Project）`隔离不同团队或应用的镜像：

| 操作 | 路径 |
|------|------|
| 创建项目 | 管理员后台 → 项目 → 新建项目 |
| 设置公开/私有 | 项目设置 → 访问级别 |
| 添加成员 | 项目 → 成员 → 添加成员（指定角色） |

`内置角色权限：`

| 角色 | 权限 |
|------|------|
| 访客（Guest） | 只读（拉取镜像、查看制品） |
| 开发者（Developer） | 拉取 + 推送 |
| 维护者（Maintainer） | 开发者权限 + 删除镜像、管理 Webhook |
| 项目管理员（Admin） | 完整项目管理权限 |

---

## 镜像扫描

Harbor 集成 `Trivy` 自动扫描镜像漏洞。

### 手动扫描

在 Harbor WebUI 中：项目 → 镜像列表 → 选择镜像 → 点击"扫描"。

或通过命令行触发（需要 Harbor API）：

``` bash
# 触发扫描（Harbor API v2）
curl -u admin:Harbor12345 -X POST \
  "https://registry.example.com/api/v2.0/projects/myproject/repositories/myapp/artifacts/1.0/scan"
```

### 自动扫描策略

项目设置 → 配置 → 自动扫描：

- `推送时扫描`：每次镜像推送后自动触发扫描
- `阻止有漏洞的镜像拉取`：配置阻止策略（如拦截 Critical 级别漏洞）

``` bash
# 在项目中设置镜像安全策略（禁止拉取含 Critical 漏洞的镜像）
# 通过 WebUI：项目 → 配置 → 防止镜像被拉取（根据严重度）
```

---

## 镜像复制

Harbor 支持在多个仓库之间自动同步镜像，适用于多地域部署。

### 创建复制规则

管理员后台 → 仓库管理 → 新建目标仓库（填写目标 Harbor 地址和凭据）。

管理员后台 → 复制管理 → 新建规则：

| 字段 | 说明 |
|------|------|
| 复制模式 | Push（本地推送到远端） / Pull（从远端拉取到本地） |
| 源资源过滤 | 按项目/镜像名/标签过滤 |
| 触发模式 | 手动 / 定时 / 事件驱动（推送时自动复制） |
| 覆盖 | 是否覆盖目标仓库已有的同名标签 |

---

## Webhook 集成

项目 → Webhook → 新建 Webhook：

``` json
{
  "auth_header": "Bearer token123",
  "enabled": true,
  "event_types": ["PUSH_ARTIFACT", "SCANNING_COMPLETED"],
  "name": "ci-notify",
  "targets": [
    {
      "address": "https://ci.example.com/harbor-webhook",
      "type": "http"
    }
  ]
}
```

`支持的事件类型：`

- `PUSH_ARTIFACT` — 镜像推送
- `DELETE_ARTIFACT` — 镜像删除
- `SCANNING_COMPLETED` — 扫描完成
- `SCANNING_FAILED` — 扫描失败
- `QUOTA_EXCEED` — 配额超出

---

## 垃圾回收

定期清理未被引用的镜像层，释放磁盘空间：

管理员后台 → 系统管理 → 垃圾清理：

``` bash
# 通过 API 触发垃圾回收
curl -u admin:Harbor12345 -X POST \
  "https://registry.example.com/api/v2.0/system/gc/schedule" \
  -H "Content-Type: application/json" \
  -d '{"schedule": {"type": "Manual"}}'
```

???+ warning "执行垃圾回收前"
    建议先进入维护模式（只读模式），避免清理过程中有新的推送操作导致数据损坏。
