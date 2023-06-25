---
layout: note
title: 环境安装
nav_order: 10
parent: OpenCloud开发日志
create_time: 2023/5/9
---

# 安装.NET环境

.NET环境安装：

[官网地址](https://dotnet.microsoft.com/download)

# 安装MongoDB

- 对于 Windows，您可以使用此[安装程序](https://www.mongodb.com/try/download/community)
- 要在 Linux 上安装它，请使用以下[说明](https://docs.mongodb.com/manual/administration/install-on-linux/)

{: .warning}
> 在Windowsn中，MongoDB将以服务的形式存在

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202306251731635-MongoDB.png)

# 安装OpenCloudServer

1. 下载最新的OpenCloudServer压缩包

2. 配置`./appsettings.json`

    - `DatabaseUrl`:MongoDB 服务器的 URL，如果安装在其他服务器实例上，请进行更改
    - `StoragePath`:用于存储用户文件的文件夹路径
    - `RegistrationToken`:用于 JobRunner 的令牌

   ```json
   {
    "OdWebSettings": {
     "DatabaseUrl": "mongodb://localhost:27017", 
     "StoragePath": "/home/clouduser/storage",
     "RegistrationToken": "df1dceb8e2d6f6b0894363b085801e36"
    }
   }
   ```

3. 启动服务

   ```shell
   # 其中--urls是服务器将侦听的端点。
   # 要检查服务器是否正在运行，请在浏览器中打开以下 URL：http://127.0.0.1:8080/version。
   dotnet OpenCloudServer.dll --urls http://127.0.0.1:8080
   ```

# 安装JobRunner

