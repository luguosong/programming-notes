---
layout: note
title: FastDFS
create_time: 2023/9/2
---

# 服务端

- 跟踪器
- 存储器

# 安装

安装前的依赖环境：

```shell
# centos系
yum install gcc libevent libevent-devel -y

# debian系
apt install gcc libevent-dev -y
```

解压`libfastcommon-1.0.36.tar.gz`，并安装：

```shell
# 解压
tar -zxvf libfastcommon-1.0.36.tar.gz

# 进入目录
cd libfastcommon-1.0.36

# 编译
./make.sh

# 安装
./make.sh install
```

解压`fastdfs-5.11.tar.gz`，并安装：

```shell
# 解压
tar -zxvf fastdfs-5.11.tar.gz

# 进入目录
cd fastdfs-5.11

# 编译
./make.sh

# 安装
./make.sh install
```

安装成功后，`/usr/bin`目录下会有`fdfs_trackerd`和`fdfs_storaged`等命令：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309040955712.png)

`/etc/fdfs`目录下会有`client.conf`、`storage.conf`和`tracker.conf`等配置文件：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202309041011345.png)

将fastdfs-5.11中conf目录下的配置文件`http.conf`、`mime.types`拷贝到`/etc/fdfs`目录下：

```shell
cp http.conf /etc/fdfs/

cp mime.types /etc/fdfs/
```

配置`tracker.conf`

```shell
#用于存储数据和日志文档的基本路径,目录必须存在
base_path=/opt/fastdfs/tracker
```

配置`storage.conf`

```shell
#用于存储数据和日志文档的基本路径,目录必须存在
base_path=/opt/fastdfs/storage

#tracker服务器的IP地址或者域名，必须配置
tracker_server=xxx.xxx.xxx.xxx:22122

#如果 store_path0 不存在，它的值就是 base_path
store_path0=/opt/fastdfs/storage/files
```

启动服务：

```shell
fdfs_trackerd /etc/fdfs/tracker.conf

fdfs_storaged /etc/fdfs/storage.conf

# 观察服务是否启动成功
ps -ef | grep fdfs
```

关闭服务

```shell
# 关闭tracker
fdfs_trackerd /etc/fdfs/tracker.conf stop

# 关闭storage
fdfs_storaged /etc/fdfs/storage.conf stop
```

# 客户端测试

修改`/etc/fdfs/client.conf`配置文件：

```shell
# 修改客户端基础路径
base_path=/opt/fastdfs/client

# 修改tracker服务器的IP地址或者域名
tracker_server=xxx.xxx.xxx.xxx:22122
```

上传文件：

```shell
# 上传文件
fdfs_test /etc/fdfs/client.conf upload testfile
```

下载文件：

```shell
# 下载文件
fdfs_test /etc/fdfs/client.conf download group1 M00/00/00/wKgBZl9Z1Z2Ae3ZzAAABZ0Z1Z2A000.txt
```

# nginx安装配置

```shell
# 解压
unzip fastdfs-nginx-module-master.zip
```

其中src目录下的common.c文件可能需要修改，开头添加以下内容：

```shell
#pragma GCC diagnostic 
#pragma GCC diagnostic ignored "-Wformat-truncation"
```

解压安装nginx：

```shell
# 解压nginx-1.14.2.tar.gz
tar -zxvf nginx-1.14.2.tar.gz

# 进入目录
cd nginx-1.14.2

# 配置，其中--add-module是刚刚解压的fastdfs-nginx-module-master/src目录
./configure --prefix=/usr/local/nginx_fdfs --add-module=/root/fastdfs-nginx-module-master/src

# 编译
make

# 安装
make install
```

配置`mod_fastdfs.conf`文件（来自fastdfs-nginx-module-master/src目录）:

```
base_path=/opt/fastdfs/nginx_mod

tracker_server=1.116.34.196:22122

url_have_group_name = true

store_path0=/opt/fastdfs/storage/files
```

将配置完的文件拷贝到`/etc/fdfs`目录下。

配置`nginx.conf`文件（来自nginx-1.14.2/conf目录）：

```
location ~ /group[1-9]/M0[0-9] {	
            ngx_fastdfs_module;  
        }
```

启动nginx：

```shell
# 启动
/usr/local/nginx_fdfs/sbin/nginx -c /usr/local/nginx_fdfs/conf/nginx.conf
```



