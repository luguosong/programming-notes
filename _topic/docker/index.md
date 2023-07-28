---
layout: note
title: Docker
create_time: 2023/7/25
---

# 简介

Docker是一个开源的`容器化平台`，它可以帮助您在不同的环境中轻松地`构建`、`打包`、`部署`和`运行`应用程序。

Docker使用容器来封装`应用程序`及其`依赖项`，并提供了一种轻量级的虚拟化技术，使得应用程序可以在任何地方运行，而无需担心环境差异和依赖项冲突等问题。

Docker的核心组件包括Docker引擎、Docker Hub和Docker Compose等。

- `Docker引擎`是Docker的核心组件，它提供了一种轻量级的虚拟化技术，使得应用程序可以在任何地方运行。
- `Docker Hub`是一个公共的Docker镜像仓库，您可以在其中找到各种各样的镜像来构建和部署应用程序。
- `Docker Compose`是一个用于管理多个容器的工具，它可以帮助您在单个主机上运行多个微服务，并确保它们之间的通信是安全和可靠的。

# 运作原理

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307251051363-docker%E8%BF%90%E4%BD%9C%E5%8E%9F%E7%90%86%E8%AF%B4%E6%98%8E.png)

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307251050112-docker%E8%BF%90%E4%BD%9C%E5%8E%9F%E7%90%86.png)

# 安装

```shell
# 卸载旧版本
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

# 安装 yum-utils 软件包（该软件包提供 yum-config-manager 实用程序）
yum install -y yum-utils

# 使用以下命令设置稳定的存储库。之后，你可以从版本库中安装和更新 Docker。
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

# 安装 Docker Engine、containerd 和 Docker Compose
sudo yum install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 启动docker
sudo systemctl start docker

# 卸载docker
sudo yum remove docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin docker-ce-rootless-extras

# 主机上的映像、容器、卷或自定义配置文件不会自动删除。要删除所有映像、容器和卷，请执行以下操作
sudo rm -rf /var/lib/docker
sudo rm -rf /var/lib/containerd
```

# 配置阿里云镜像加速器

[配置地址](https://cr.console.aliyun.com/cn-shanghai/instances/mirrors)

```shell
# 创建配置文件夹
sudo mkdir -p /etc/docker

# 创建daemon.json文件
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": ["登录阿里云后获取的地址，每个人都不一样"]
}
EOF

# 重新加载配置文件
sudo systemctl daemon-reload

# 重启docker
sudo systemctl restart docker
```

# 基本命令

## 启动docker

```shell
sudo systemctl start docker
```

## 停止docker

```shell
sudo systemctl stop docker
```

## 重启docker

```shell
sudo systemctl restart docker
```

## 查看docker状态

```shell
sudo systemctl status docker
```

## 开机启动docker

```shell
sudo systemctl enable docker
```

## 查看docker概要信息

```shell
docker info
```

## 查看docker总体帮助文档

```shell
docker --help
```

## 查看docker子命令帮助文档

```shell
docker 子命令 --help
```

## 📌

## 列出主机上的镜像

```shell
# docker images
# -q 只显示镜像ID
# -a 显示所有镜像
# -f 过滤镜像

docker images
```

## 镜像搜索

```shell
# docker search 镜像名称
# --limit 限制返回的结果数量

docker search 镜像名称
```

## 拉取镜像

```shell
# docker pull 镜像名称:版本号

docker pull 镜像名称
```

## 删除镜像

```shell
# docker rmi
# -f 强制删除

docker rmi 镜像名称:版本号

# 强制删除全部镜像
docker rmi -f $(docker images -q)
```

## 虚悬镜像

`虚悬镜像（Dangling Image）`
是指仓库名和标签均为none的镜像。一般来说，虚悬镜像已经失去了存在的价值，是可以随意删除的。出现虚悬镜像的原因一般是在`docker pull **:latest`
时产生，或者在构建新镜像的时候，为这个镜像打了一个已经存在的tag，此时Docker会移除旧镜像上的tag,将这个tag用在新的镜像上，此时旧镜像就变成了悬虚镜像。构建新镜像报错时，也会生成一个悬虚镜像。

```shell
# 批量删除虚悬镜像
docker rmi $(docker images -f "dangling=true" -q)

# 或
docker image prune -f --filter "dangling=true"
```

## 查看docker空间占用

```shell
docker system df
```

## 📌

## 新建并启动容器

```shell
# docker run
# -d 后台运行
# --name 容器名称
# -i,--interactive 交互式操作
# -t,--tty 分配一个终端
# -p 端口映射
# -P 随机端口映射
# -v 数据卷映射
# --privileged=true 特权模式
# --volume-from 容器名称 从容器中挂载数据卷
# --restart=always 自动重启

# 后台运行
docker run -d --name 容器名称 镜像名称:版本号

# 交互式运行
docker run -it --name 容器名称 镜像名称:版本号

# Docker容器数据卷,将容器中的数据进行备份
docker run -it --name 容器名称 -v 主机路径:容器内路径 --privileged=true 镜像名称:版本号

# 让容器内只读
docker run -it --name 容器名称 -v 主机路径:容器内路径:ro --privileged=true 镜像名称:版本号
```

## 交互式运行退出

- `exit` 退出容器，容器停止运行
- `ctrl + p + q` 退出容器，容器继续运行

## 进入容器并以命令行交互

```shell
# docker exec
# -i,--interactive 交互式操作
# -t,--tty 分配一个终端

docker exec -it 容器ID或容器名称 /bin/bash
```

```shell
docker attach 容器ID或容器名称
```

{: .warning-title}
> exec和attach的区别
> 
> - exec进入容器后，是在容器中打开新的终端，并且可以启动新的进程，退出容器后，容器继续运行
> - attach进入容器后，不会启动新的进程，退出容器后，容器停止运行

## 查看正在运行的容器

```shell
# docker ps
# -a 显示所有容器
# -l 显示最近创建的容器
# -n 显示最近n个创建的容器
# -q 只显示容器ID

docker ps -a
```

## 启动停止的容器

```shell
docker start 容器ID或容器名称
```

## 停止运行的容器

```shell
docker stop 容器ID或容器名称
```

## 强制停止容器

```shell
docker kill 容器ID或容器名称
```

## 重启容器

```shell
docker restart 容器ID或容器名称
```

## 删除容器

```shell
# docker rm
# -f 强制删除

docker rm 容器ID或容器名称

# 强制删除全部容器
docker rm -f $(docker ps -a -q)
#或
docker ps -a -q | xargs docker rm
```

## 容器日志查看

```shell
# docker logs
# -f 跟踪日志输出
# -t 显示时间戳
# --tail n 显示最后n条日志

docker logs 容器ID或容器名称
```

## 查看容器内运行进程

```shell
docker top 容器ID或容器名称
```

## 查看容器内部细节

```shell
docker inspect 容器ID或容器名称
```

## 容器内拷贝内容到主机

```shell
docker cp 容器ID或容器名称:容器内路径 主机路径
```

## 容器备份

```shell
# 导出
docker export 容器ID或容器名称 > 备份文件名.tar

# 导入
cat 备份文件名.tar | docker import - 镜像名称:版本号
```

# 镜像发布

## 打包容器为新镜像

```shell
# docker commit
# -a 作者
# -m 提交信息

docker commit 容器ID或容器名称 镜像名称:版本号
```

## 镜像创建新标签

```shell
docker tag 镜像名称:版本号 用户名/新镜像名称:版本号
```

## 镜像发布到dockerhub

```shell
# 登录
docker login

# 发布
docker push 用户名/镜像名称:版本号
```

## 本地镜像发布到阿里云

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307251709613-%E6%8E%A8%E9%80%81%E9%95%9C%E5%83%8F%E5%88%B0%E9%98%BF%E9%87%8C%E4%BA%91.png)

## 本地镜像发布到私有库

```shell
# 拉取registry
docker pull registry

# 启动registry
# -p 端口映射
# -v 数据卷映射
# --restart=always 自动重启
# --privileged=true 特权模式
# --name 容器名称
docker run -d -p 5000:5000 -v /opt/data/registry:/var/lib/registry --restart=always --privileged=true --name registry registry

# 查看私有库已上传的镜像
curl -XGET http://127.0.0.1:5000/v2/_catalog

# 创建新标签
docker tag 旧镜像名称:版本号 私有库ip:私有库端口/新镜像名称:版本号

# 推送镜像到私有库
docker push 私有库ip:私有库端口/新镜像名称:版本号
```


# 常规安装简介

## 总体步骤

1. 搜索镜像
2. 拉取镜像
3. 查看镜像
4. 启动镜像
5. 停止容器
6. 移除容器

## Tomcat安装

```shell
# 拉取镜像
docker pull tomcat

# 启动
docker run -d -v /opt/data/tomcat/webapps:/usr/local/tomcat/webapps -p 8080:8080 --name tomcat tomcat
```

## mysql

```shell
# 拉取镜像
docker pull mysql

# 启动
docker run \
-d --privileged=true \
-v /opt/data/mysql/data:/var/lib/mysql \
-v /opt/data/mysql/conf:/etc/mysql/conf.d \
-v /opt/data/mysql/logs:/logs \
-p 3306:3306 \
-e MYSQL_ROOT_PASSWORD=12345678 \
--name mysql \
mysql
```

解决中文乱码问题，在`/opt/data/mysql/conf`目录下创建`my.cnf`文件，内容如下：

```
[client]
default-character-set=utf8
[mysqld]
character-set-server=utf8
collation-server=utf8_general_ci
```

## redis

配置文件修改：
- 注释`bind 127.0.0.1`,允许远程访问
- 设置`daemonize no`,因为该配置和`docker run`中的`-d`冲突

```shell
# 拉取镜像
docker pull redis

# 启动
# redis-server /etc/redis/redis.conf表示使用配置文件启动redis
docker run \
-d --privileged=true \
-v /opt/data/redis/data:/data \
-v /opt/data/redis/conf/redis.conf:/etc/redis/redis.conf \
-p 6379:6379 \
--name redis \
redis redis-server /etc/redis/redis.conf
```

# 复杂安装

## 安装mysql主从复制

- 启动主库

```shell
# 拉取镜像
docker pull mysql

# 启动
docker run \
-d --privileged=true \
-v /opt/data/mysql-master/data:/var/lib/mysql \
-v /opt/data/mysql-master/conf:/etc/mysql \
-v /opt/data/mysql-master/logs:/logs \
-v /opt/data/mysql-master/mysql-files:/var/lib/mysql-files \
-p 3307:3306 \
-e MYSQL_ROOT_PASSWORD=12345678 \
--name mysql-master \
mysql
```

在目录`/opt/data/mysql-master/conf`下创建`my.cnf`文件，内容如下：

```
[mysqld]
## 设置server_id,同一局域网中需要唯一,一般设置为IP
server-id=101
## 指定不需要同步的数据库名称
binlog-ignore-db=mysql
## 开启二进制日志功能,可以随便取(服务器唯一)
log-bin=mysql-bin
## 设置使用的二进制日志格式（mixed,statement,row）
binlog_format=mixed
## 二进制日志过期时间，默认为0不清理。单位：天
expire_logs_days=7
## 跳过主从复制错误，1062错误是主键冲突，1032错误是唯一索引冲突
slave-skip-errors=1062
```

修改完成，重启容器

创建数据同步用户，并授权：

```shell
# 创建用户
CREATE USER 'slave'@'%' IDENTIFIED BY '12345678';

# 授权
GRANT REPLICATION SLAVE,REPLICATION CLIENT ON *.* TO 'slave'@'%';
```

- 启动从库

```shell
# 启动
docker run \
-d --privileged=true \
-v /opt/data/mysql-slave/data:/var/lib/mysql \
-v /opt/data/mysql-slave/conf:/etc/mysql \
-v /opt/data/mysql-slave/logs:/logs \
-v /opt/data/mysql-slave/mysql-files:/var/lib/mysql-files \
-p 3308:3306 \
-e MYSQL_ROOT_PASSWORD=12345678 \
--name mysql-slave \
mysql
```

在目录`/opt/data/mysql-slave/conf`下创建`my.cnf`文件，内容如下：

```
[mysqld]
## 设置server_id,同一局域网中需要唯一,一般设置为IP
server-id=102
## 指定不需要同步的数据库名称
binlog-ignore-db=mysql
## 开启二进制日志功能,可以随便取(服务器唯一)
log-bin=mysql-bin
## 设置二进制日志使用内存大小
binlog_cache_size=1M
## 设置使用的二进制日志格式（mixed,statement,row）
binlog_format=mixed
## 二进制日志过期时间，默认为0不清理。单位：天
expire_logs_days=7
## 跳过主从复制错误，1062错误是主键冲突，1032错误是唯一索引冲突
slave-skip-errors=1062
## relay_log配置中继日志
relay_log=edu-mysql-relay-bin
## 复制事件写进自己的二进制日志
log_slave_updates=1
## 从库只读
read_only=1
```

修改完成，重启容器

在主库中查询主从同步状态：

```shell
show master status;
```

在从数据库中配置主从复制：

```shell
# 配置主从复制
change master to \
master_host='宿主机ip',\
master_user='主库中配置的从表访问用户',\
master_password='主库中配置的从表访问用户密码',\
master_port=主表端口,\
master_log_file='主库中查询到的File',\
master_log_pos=主库中查询到的Position,\
master_connect_retry=30;
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307261818320-mysql%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6%E4%BB%8E%E8%A1%A8%E8%AF%B4%E6%98%8E.png)

查询从库中查询主从同步状态：

```shell
show slave status\G;
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307261828980-%E4%BB%8E%E5%BA%93%E5%BC%80%E5%90%AF%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6%E5%89%8D%E7%8A%B6%E6%80%81.png)

在从数据库中开启主从复制：

```shell
start slave;
```

查询从库中查询主从同步状态：

```shell
show slave status\G;
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307261829961-%E4%BB%8E%E8%A1%A8%E5%BC%80%E5%90%AF%E4%B8%BB%E4%BB%8E%E5%A4%8D%E5%88%B6%E5%90%8E%E7%8A%B6%E6%80%81.png)

到此，主库修改，从库自动同步。

## redis集群

分区方式：
- 哈希取余分区
- 一致性哈希算法分区
- 哈希槽分区

创建6个redis容器：

```shell
# 拉取镜像
docker pull redis

# 启动第一个redis容器
docker run \
-d --privileged=true \
--net host \
-v /opt/data/redis-cluster/7001/data:/data \
--name redis-cluster-7001 \
redis \
--cluster-enabled yes \
--appendonly yes \
--port 7001

# 启动第二个redis容器
docker run \
-d --privileged=true \
--net host \
-v /opt/data/redis-cluster/7002/data:/data \
--name redis-cluster-7002 \
redis \
--cluster-enabled yes \
--appendonly yes \
--port 7002

# 启动第三个redis容器
docker run \
-d --privileged=true \
--net host \
-v /opt/data/redis-cluster/7003/data:/data \
--name redis-cluster-7003 \
redis \
--cluster-enabled yes \
--appendonly yes \
--port 7003

# 启动第二个redis容器
docker run \
-d --privileged=true \
--net host \
-v /opt/data/redis-cluster/7004/data:/data \
--name redis-cluster-7004 \
redis \
--cluster-enabled yes \
--appendonly yes \
--port 7004

# 启动第二个redis容器
docker run \
-d --privileged=true \
--net host \
-v /opt/data/redis-cluster/7005/data:/data \
--name redis-cluster-7005 \
redis \
--cluster-enabled yes \
--appendonly yes \
--port 7005

# 启动第二个redis容器
docker run \
-d --privileged=true \
--net host \
-v /opt/data/redis-cluster/7006/data:/data \
--name redis-cluster-7006 \
redis \
--cluster-enabled yes \
--appendonly yes \
--port 7006
```

进入其中一个容器，创建集群：

```shell
# 进入容器
docker exec -it redis-cluster-7001 /bin/bash

# 创建集群
redis-cli --cluster create 宿主机ip:7001 宿主机ip:7002 宿主机ip:7003 宿主机ip:7004 宿主机ip:7005 宿主机ip:7006 --cluster-replicas 1
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307271838522-%E9%9B%86%E7%BE%A4%E5%88%9B%E5%BB%BA.png)

查看集群状态：

```shell
# 连接到其中一个redis
redis-cli -p 7001
```

```shell
# 查看集群状态
cluster info
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307271850293-redis%E9%9B%86%E7%BE%A4%E7%8A%B6%E6%80%81.png)

```shell
# 查看集群节点,可以查看主从映射关系
cluster nodes
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202307281410641-%E4%B8%BB%E4%BB%8E%E6%98%A0%E5%B0%84%E5%85%B3%E7%B3%BB.png)



# DockerFile解析

# 微服务实战

# Docker网络

# Docker Compose容器编排

# Portainer容器管理

# CAdvisor+InfluxDB+Grafana监控
