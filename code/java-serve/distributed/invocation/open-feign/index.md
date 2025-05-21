# Spring Cloud OpenFeign

## 入门案例

服务消费者引入依赖：

``` xml title="pom.xml"
--8<-- "code/java-serve/distributed/invocation/open-feign/open-feign-consumer/pom.xml"
```

服务消费者启动类配置`@EnableFeignClients`:

``` java title="OpenFeignConsumerApplication.java"
--8<-- "code/java-serve/distributed/invocation/open-feign/open-feign-consumer/src/main/java/com/luguosong/openfeignconsumer/OpenFeignConsumerApplication.java"
```

定义接口：

``` java title="ProducerClient.java"
--8<-- "code/java-serve/distributed/invocation/open-feign/open-feign-consumer/src/main/java/com/luguosong/openfeignconsumer/client/ProducerClient.java"
```

调用：

``` java title="TestController.java"
--8<-- "code/java-serve/distributed/invocation/open-feign/open-feign-consumer/src/main/java/com/luguosong/openfeignconsumer/controller/TestController.java"
```

## 连接池

OpenFeign底层默认使用`HttpURlConnection`进行网络请求，不支持连接池。

可以自定义配置`Apache HttpClient`或`OKHttp`进行网络请求，这两种都支持连接池。

以`OKHttp`为例，先引入依赖：

```xml

<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-okhttp</artifactId>
</dependency>
```

在配置文件中进行配置：

```yaml
spring:
  cloud:
    openfeign:
      okhttp:
        enabled: true
```

## 最佳实践

### 方案一

将服务生产者拆分为`service`(业务代码)、`dto`(存放数据传输对象)、`api`(存放Feign接口)模块。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410211012434.png){ loading=lazy }
  <figcaption>OpenFeign拆分方式一</figcaption>
</figure>

### 方案二

将Feign接口全部存放在同一个接口中。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410211126986.png){ loading=lazy }
  <figcaption>OpenFeign拆分方式二</figcaption>
</figure>

!!! note

	当Open Feign 接口所在包名与服务消费者模块包名不一致时，需要在启动类中手动指定包名：

	`@EnableFeignClients(basePackages = "com.luguosong.api")`

## 日志记录

1. 配置FeignClient所在包的日志级别为`DEBUG`。
2. 设置OpenFeign的日志级别。

OpenFeign有如下日志级别：

| 日志级别    | 日志内容                     |
|---------|--------------------------|
| NONE    | 默认值，不记录日志                |
| BASIC   | 仅记录请求的方法、URL以及响应状态码和执行时间 |
| HEADERS | 在BASIC的基础上，额外记录了请求和响应头消息 |
| FULL    | 记录请求和响应的头信息、请求体、元数据      |

``` java title="DefaultFeignConfig.java"
--8<-- "code/java-serve/distributed/invocation/open-feign/open-feign-consumer/src/main/java/com/luguosong/openfeignconsumer/config/DefaultFeignConfig.java"
```

局部生效：

``` java
@FeignClient(value = "open-feign-producer", configuration = DefaultFeignConfig.class)
```

全局生效：

``` java
@EnableFeignClients(defaultConfiguration = DefaultFeignConfig.class)
```

