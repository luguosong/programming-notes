# 面向切面编程

## 名词解释

- `连接点（JoinPoint）`：程序执行过程中，可以织入切面的`位置`。比如方法执行前后或异常抛出之后。
- `切点（Pointcut）`：我们配置的满足条件的目标`方法`。
- `通知（Advice）`：具体增强的`方法`。
- `切面（Aspect）`：`切点`+`通知`
- `织入（Weaving）`：把通知应用到目标对象上的过程。
- `代理对象（proxy）`：目标对象被织入后产生的新对象
- `目标对象（Target）`：被织入的对象

## AspectJ XML方式开发

目标对象：

``` java
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/java/com/luguosong/xml/UserServiceImpl.java"
```

切面类：

``` java
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/java/com/luguosong/xml/UserAspect.java"
```

Spring配置文件：

``` xml
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/resources/aspect.xml"
```

测试：

``` java
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/java/com/luguosong/xml/Test.java"
```

## AspectJ注解方式开发🔥

目标对象:

``` java
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/java/com/luguosong/anno/UserServiceImpl.java"
```

Spring配置类,配置组件扫描和启用基于AspectJ的`自动代理`：

``` java
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/java/com/luguosong/anno/SpringConfig.java"
```

切面类：

``` java
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/java/com/luguosong/anno/UserAspect.java"
```

测试：

``` java
--8<-- "code/java-serve/spring/aop/aop-aspect/src/main/java/com/luguosong/anno/Test.java"
```

