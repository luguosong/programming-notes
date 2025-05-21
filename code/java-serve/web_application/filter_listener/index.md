# 过滤器和监听器

## 过滤器概述

`过滤器`（Filter）是一种用于拦截和处理客户端请求和响应的组件。它们可以在`请求到达Servlet之前`或`响应返回客户端之前`
，对请求和响应进行修改或处理。

## 过滤器入门案例

- 编写一个Servlet

``` java
--8<-- "code/java-serve/web_application/filter_listener/filter-hello/src/main/java/com/luguosong/Servlet1.java"
```

- 编写过滤器

``` java
--8<-- "code/java-serve/web_application/filter_listener/filter-hello/src/main/java/com/luguosong/Filter1.java"
```

- 在`web.xml`中配置过滤器

``` xml
--8<-- "code/java-serve/web_application/filter_listener/filter-hello/src/main/webapp/WEB-INF/web.xml"
```

!!! warning

    web.xml中<filter-mapping>的顺序决定了过滤器的执行顺序。

    如果使用的是注解开发，默认执行顺序取决于过滤器类的字母顺序。

``` shell title="执行结果"
过滤器1请求执行
过滤器2请求执行
Servlet执行
过滤器2响应执行
过滤器1响应执行
```

## 过滤器生命周期

- 与Servlet不同，过滤器对象会在服务`启动时`创建,并执行`init()`方法。过滤器也是`单例`的，只会创建一次。
- 每次客户端请求与过滤器映射的资源时，执行`doFilter()`方法。
- 当容器卸载过滤器或者Web应用程序关闭时，容器会调用过滤器的`destroy()`方法。

## 监听器概述

`监听器`（Listener）是一种特殊的组件，用于监视和响应特定事件或状态变化。它们在Servlet生命周期的不同阶段起作用，通过实现特定的接口来完成其功能。

## 监听器入门案例

- 应用启动和销毁时，监听创建和销毁ServletContext对象：

``` java
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/java/com/luguosong/MyServletContextListener.java"
```

- 每次客户端发送http请求，监听ServletRequest对象被创建和销毁：

``` java
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/java/com/luguosong/MyServletRequestListener.java"
```

- 监听session对象被创建和销毁：

``` java
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/java/com/luguosong/MyHttpSessionListener.java"
```

- 监听request域内容修改：

``` java
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/java/com/luguosong/MyServletRequestAttributeListener.java"
```

- 监听Session域内容修改：

``` java
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/java/com/luguosong/MyHttpSessionAttributeListener.java"
```

- 监听ServletContext域内容修改：

``` java
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/java/com/luguosong/MyServletContextAttributeListener.java"
```

- 监听指定对象放入Session中：

``` java
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/java/com/luguosong/User.java"
```

- web.xml中配置监听器：

``` xml
--8<-- "code/java-serve/web_application/filter_listener/listener-hello/src/main/webapp/WEB-INF/web.xml"
```
