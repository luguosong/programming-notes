# Tomcat服务器

## Java服务器软件种类

- `Web 服务器`：只实现了`Servlet`和`JSP`等部分规范
    - [Apache Tomcat](https://tomcat.apache.org/)
    - [jetty](https://eclipse.dev/jetty/):Jetty提供了一个Web服务器和Servlet容器，此外还提供了对HTTP/2、WebSocket、OSGi、JMX、JNDI、JAAS等许多集成的支持。这些组件是开源的，可供商业用途和分发免费使用。
- `应用服务器`:实现了JavaEE所有规范
    - JBOSS:内嵌Apache Tomcat，2014年11月20日，JBoss更名为WildFly。
    - WebLogic
    - WebSphere

## Tomcat版本说明

- `Apache Tomcat 8.0.x`实现了Servlet 3.1、JSP 2.3、EL 3.0和WebSocket 1.1规范。
- `Apache Tomcat 9.x`实现了Servlet 4.0、JSP 2.3、EL 3.0、WebSocket 1.1 和 JASPIC 1.1 规范（Java EE 8 平台所需的版本）。
- `Apache Tomcat 10.1.x`实现了 Servlet 6.0、JSP 3.1、EL 5.0、WebSocket 2.1 和 Authentication 3.0 规范（这些是 Jakarta EE 10 平台所需的版本）。
- `Apache Tomcat 11.0.x`实现了 Servlet 6.1、JSP 4.0、EL 6.0、WebSocket 2.2 和 Authentication 3.1 规范（这些是 Jakarta EE 11 平台所需的版本）。

## 下载安装Tomcat

!!! 安装前提条件

    需要先安装java环境，并配有`JAVA_HOME`环境变量。

- [下载](https://tomcat.apache.org/download-80.cgi)压缩包，并解压。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405102214744.png){ loading=lazy }
  <figcaption>下载Tomcat压缩包</figcaption>
</figure>

- 通过bin目录下的`startup.bat`，`startup.bat`内部会调用同目录下的`catalina.bat`启动Tomcat。

!!! warning

    `catalina.bat`内部会使用到`JAVA_HOME`环境变量。

```shell
# 启动服务
startup.bat

# 关闭服务
shutdown.bat
```

- 打开浏览器，输入`localhost:8080`,测试服务是否启动成功

## 配置

### Tomcat控制台乱码问题

!!! 原因

    cmd控制台使用的是GBK编码。

- 打开tomcat目录下的`/conf/logging.properties`

```properties
#java.util.logging.ConsoleHandler.encoding = UTF-8

#改为

java.util.logging.ConsoleHandler.encoding = GBK
```

## Hello World

- 在Tomcat主目录`webapp`目录下新建文件夹`test`。
- 在其中创建test.html文件，并添加如下内容：

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Tomcat入门案例</title>
</head>
<body>
Hello Tomcat
</body>
</html>
```

- 启动Tomcat
- 访问`http://127.0.0.1:8080/test/hello.html` 访问应用


