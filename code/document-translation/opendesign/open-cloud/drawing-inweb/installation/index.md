# 安装

## 系统要求

浏览器：

支持具备 WebAssembly 与 WebGL 的现代浏览器。

ProtectionServer 要求： .NET Core 8.0+ 或兼容环境。

## 安装

- 准备受保护的服务器
	- 安装 .NET Core 8.0+
	- 按照 Microsoft 指南操作：[https://learn.microsoft.com/en-us/dotnet/core/install/](https://learn.microsoft.com/en-us/dotnet/core/install/)
- 准备运行示例
	- 安装 Node.js 18+
	- `cd examples`
	- `npm install`
	- `npm start`

## 运行

要启动服务器，请执行以下命令：

``` shell
dotnet ProtectionServer.dll --urls "http://localhost:8080"
```

为了让浏览器下载该库，你必须指定指向该服务器的链接以及脚本名称。随后浏览器会下载主库文件 DrawingWeb.wasm。

例如：

``` html
<script src="http://127.0.0.1:8080/DrawingWeb.js"></script>
```
