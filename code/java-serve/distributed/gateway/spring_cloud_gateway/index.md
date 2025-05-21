# 网关

## 网关的作用

- 反向代理
- 统一认证鉴权
- 流量控制
- 熔断
- 日志监控

## 入门案例

引入相关依赖：

``` xml title="pom.xml"
--8<-- "code/java-serve/distributed/gateway/spring_cloud_gateway/gateway-hello/pom.xml"
```

配置路由和断言：

``` yaml title="application.yml"
--8<-- "code/java-serve/distributed/gateway/spring_cloud_gateway/gateway-hello/src/main/resources/application.yml"
```

## 路由

`路由`：网关的基本构建块。它由一个ID、一个目标URI、一组断言和一组过滤器定义。如果断言集合为真，则匹配该路由。

## url属性

### 配置地址

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: gateway-server1
          uri: https://example.org
          predicates:
            - Path=/**
```

### 负载均衡

需要引入`spring-cloud-starter-loadbalancer`,负载均衡到nacos服务。

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: gateway-server1
          uri: lb://gateway-server1
          predicates:
            - Path=/**
```

## 断言(Predicates)

这是一个 Java 8 函数断言。输入类型是 Spring Framework 的 ServerWebExchange。这允许您匹配 HTTP 请求中的任何内容，例如头信息或参数。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410300952320.png){ loading=lazy }
  <figcaption>路由断言</figcaption>
</figure>

## 过滤器(Filters)

这些是使用特定工厂构建的GatewayFilter实例。在这里，您可以在发送下游请求之前或之后修改请求和响应。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410300954344.png){ loading=lazy }
  <figcaption>网关过滤器</figcaption>
</figure>

### 过滤器对所有路由生效

`default-filters`配置表示过滤器对所有路由生效

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: gateway-server1
          uri: lb://gateway-server1
          predicates:
            - Path=/**
        - id: gateway-server2
          uri: lb://gateway-server2
          predicates:
            - Path=/**
      default-filters:
        - StripPrefix=1
```

## 自定义路由过滤器

创建路由过滤器工厂：

``` java title="PrintAnyGatewayFilterFactory.java"
--8<-- "code/java-serve/distributed/gateway/spring_cloud_gateway/gateway-custom-gateway-filter/src/main/java/com/upda/gatewaycustomgatewayfilter/filter/PrintAnyGatewayFilterFactory.java"
```

在配置文件中对指定路由配置过滤器：

``` yaml title="application.yml"
--8<-- "code/java-serve/distributed/gateway/spring_cloud_gateway/gateway-custom-gateway-filter/src/main/resources/application.yml"
```

## 自定义全局过滤器

定义全局过滤器：

``` java title="MyGlobalFilter.java"
--8<-- "code/java-serve/distributed/gateway/spring_cloud_gateway/gateway-custom-global-filter/src/main/java/com/upda/gatewaycustomglobalfilter/filter/MyGlobalFilter.java"
```

不需要在配置文件中进行配置，全局过滤器会直接生效。


