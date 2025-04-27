# MinIO

## 安装

### 免费版与收费版

去[仓库](https://github.com/minio/minio)下的README文件中获取地址：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412191647917.png){ loading=lazy }
  <figcaption>github</figcaption>
</figure>

官网好像找不到免费地址了，这个地址下载需要许可证才能运行：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412191648961.png){ loading=lazy }
  <figcaption>官网提供的地址下载，需要许可证才能使用</figcaption>
</figure>

### Centos下安装

```shell title="下载和启动"
# 下载文件
wget https://dl.min.io/server/minio/release/linux-amd64/minio

# 为文件提供可执行权限
chmod +x minio

# MINIO_ROOT_USER 指定用户名
# MINIO_ROOT_PASSWORD 指定密码
# /mnt/data 指定数据存储目录
# --console-address ":9001" 指定网页控制台端口
# & 后台运行
MINIO_ROOT_USER=admin MINIO_ROOT_PASSWORD=12345678 ./minio server /mnt/data --console-address ":9001" &
```

```shell title="关闭服务"
# 查询服务
ps -ef | grep minio

# 根据id关闭服务
kill xxxx
```

### Docker下安装

[镜像地址](https://hub.docker.com/r/minio/minio)

```shell
# 拉取镜像
docker pull minio/minio

# 启动容器
docker run -p 9000:9000 -p 9001:9001 \
minio/minio server /data --console-address ":9001"

# 自定义参数启动
docker run -p 9000:9000 -p 9090:9090 \
 --name minio \
 -d --restart=always \
 -e "MINIO_ACCESS_KEY=admin" \       #自定义MINIO_ACCESS_KEY
 -e "MINIO_SECRET_KEY=admin123456" \   #自定义MINIO_SECRET_KEY
 -v /root/docker/minio/data:/data  \
 -v /root/docker/minio/config:/root/.minio \
 minio/minio  server\
 /data --console-address ":9090" -address ":9000"    #9000 服务端口； 9090 web端控制台端口（两个端口都可以自定义）
```

### Windows下安装

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202412192105650.png){ loading=lazy }
  <figcaption>github README文档</figcaption>
</figure>

要在 64 位 Windows 主机上运行 MinIO，请从以下网址下载 MinIO 可执行文件：

[https://dl.min.io/server/minio/release/windows-amd64/minio.exe](https://dl.min.io/server/minio/release/windows-amd64/minio.exe)

在 Windows 主机上运行独立 MinIO 服务器，请使用以下命令。将 D:\ 替换为您希望 MinIO 存储数据的驱动器或目录路径。您必须将终端或 PowerShell 的目录更改为 minio.exe 可执行文件所在的位置，或者将该目录的路径添加到系统 $PATH：

```shell
minio.exe server D:\
```

MinIO 部署开始时使用默认的根凭证 `minioadmin:minioadmin`。您可以通过 MinIO 控制台测试部署，这是一个嵌入在 MinIO 服务器中的基于网页的对象浏览器。在主机上运行的网络浏览器中输入 `http://127.0.0.1:9000`，并使用根凭证登录。您可以使用浏览器创建存储桶、上传对象以及浏览 MinIO 服务器的内容。

您还可以使用任何兼容 S3 的工具进行连接，例如 MinIO 客户端 mc 命令行工具。有关使用 mc 命令行工具的更多信息，请参阅[使用 MinIO 客户端 mc 进行测试](https://github.com/minio/minio?tab=readme-ov-file#test-using-minio-client-mc)。对于应用程序开发人员，请访问 https://min.io/docs/minio/linux/developers/minio-drivers.html 查看支持语言的 MinIO SDK。

!!! note

	独立的 MinIO 服务器最适合早期开发和评估。某些功能如版本控制、对象锁定和存储桶复制需要通过分布式部署 MinIO 并启用纠删码来实现。对于扩展开发和生产环境，建议部署启用纠删码的 MinIO，每台 MinIO 服务器至少需要 4 个驱动器。有关更完整的文档，请参阅 MinIO 纠删码概述。

## 入门案例

- 创建springboot项目，引入minio依赖。

``` xml title="pom.xml"
--8<-- "code/topics/object-store/minio/minio-demo/pom.xml"
```

- 创建`MinioClient`对象

``` java title="Config.java"
--8<-- "code/topics/object-store/minio/minio-demo/src/main/java/com/upda/miniodemo/config/Config.java"
```

## Bucket操作

``` java title="BucketTest.java"
--8<-- "code/topics/object-store/minio/minio-demo/src/test/java/com/upda/miniodemo/BucketTest.java"
```

## 对象操作

``` java title="ObjectTest.java"
--8<-- "code/topics/object-store/minio/minio-demo/src/test/java/com/upda/miniodemo/ObjectTest.java"
```

