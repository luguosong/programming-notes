# 代理(Proxy)🔥

## 意图

为目标对象提供一种代理以控制对这个对象的访问。

## 结构

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202406172054353.png){ loading=lazy }
  <figcaption>代理模式结构</figcaption>
</figure>

- `服务接口（Service Interface）`声明了服务接口。代理必须遵循该接口才能伪装成服务对象。
- `服务（Service）`类提供了一些实用的业务逻辑。
- `代理（Proxy）`类包含一个指向服务对象的引用成员变量。代理完成其任务（例如延迟初始化、记录日志、访问控制和缓存等）后会将请求传递给服务对象。下，代理会对其服务对象的整个生命周期进行管理。
- `客户端（Client）` 能通过同一接口与服务或代理进行交互，所以你可在一切需要服务对象的代码中使用代理。

## 静态代理

### 代码实现

服务接口：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/static_proxy/UserService.java"
```

服务具体实现：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/static_proxy/UserServiceImpl.java"
```

静态代理：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/static_proxy/UserServiceProxy.java"
```

测试代码：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/static_proxy/Test.java"
```



### 静态代理存在的问题

- 需要为所有想要被代理的`服务（Service）`编写`代理类（Proxy）`，会导致代理类爆炸式增多。

## 动态代理

### JDK动态代理

服务接口：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/dynamic_proxy_jdk/UserService.java"
```

服务具体实现：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/dynamic_proxy_jdk/UserServiceImpl.java"
```

在测试类中直接生成代理对象：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/dynamic_proxy_jdk/Test.java"
```

### cglib动态代理

使用cglib动态代理需要导入相关依赖：

```xml

<dependencies>
    <!--❌这个依赖已经不维护了，JDK17调用会报错-->
    <dependency>
        <groupId>cglib</groupId>
        <artifactId>cglib</artifactId>
        <version>3.3.0</version>
    </dependency>

    <!--可以使用Spring内部的cglib实现，支持JDK17-->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.1.8</version>
    </dependency>
</dependencies>
```

没有接口的服务类：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/dynamic_proxy_cglib/UserServiceImpl.java"
```

在测试类中动态创建代理对象：

``` java
--8<-- "code/topics/design_pattern/src/main/java/com/luguosong/structural/proxy/dynamic_proxy_cglib/Test.java"
```
