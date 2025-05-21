# 控制反转

## 不使用Spring存在的问题

- 使用`new`创建对象时，用到了`具体的`
  实现类。当业务需求反复变化，实现类变更为其它类。需要反复修改创建对象处的代码。不满足`开闭原则`。
- 使用`new`创建对象。依赖了具体实现类，不符合`依赖倒置原则`。

## Spring概述

`Spring框架`是 Java 平台的一个开源的全栈（full-stack）应用程序框架和控制反转容器实现，一般被直接称为
Spring。该框架的一些核心功能理论上可用于任何 Java 应用，但 Spring 还为基于Java企业版平台构建的 Web 应用提供了大量的拓展支持。Spring
没有直接实现任何的编程模型，但它已经在 Java 社区中广为流行，基本上完全代替了`企业级JavaBeans（EJB）模型`。

Spring框架以 Apache License 2.0 开源许可协议的形式发布，该框架最初由 `Rod Johnson` 以及 Juergen Hoeller 等人开发。

## 版本说明

!!! warning

    从`Spring Framework 6.0`开始，Spring需要`Java 17+`

| Spring版本 | Jakarta EE版本                  | JDK版本     |
|----------|-------------------------------|-----------|
| 5.3.x    | Java EE 7-8 (javax namespace) | 8-21      |
| 6.0.x    | 9-10                          | 17-21     |
| 6.1.x    | 9-10                          | 17-23     |
| 6.2.x    | 9-11                          | 17-25(预计) |

## ICO入门案例

- 引入依赖：

``` xml
--8<-- "code/java-serve/spring/basic/spring-hello/pom.xml"
```

> `spring-context`依赖内部会引用其它核心模块：

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202405301128137.png){ loading=lazy }
  <figcaption>spring-context依赖内部引入其它核心模块</figcaption>
</figure>

- 创建User对象：

``` java title="User.java"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/User.java"
```

- 编写Spring配置文件：

``` xml title="spring_config_hello.xml"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/spring_config_hello.xml"
```

- 编写测试类：

``` java title="SpringHello.java"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/hello/SpringHello.java"
```

## IOC原理分析

`初始化阶段`：Spring读取配置文件中的bean标签，根据其中的`class属性`(类的全限定类名)，默认通过`反射`
调用类的无参构造创建对象，并将对象实例跟`id属性`绑定封装成Map集合。

`使用阶段`：Spring根据`id名称`可以获取到对应的`对象实例`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/java-serve/spring/basic/Spring%20IOC%E5%8E%9F%E7%90%86.svg){ loading=lazy }
  <figcaption>IOC原理图</figcaption>
</figure>

## 配置日志

``` xml title="引入依赖"
<dependencies>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-core</artifactId>
        <version>2.20.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.logging.log4j</groupId>
        <artifactId>log4j-slf4j2-impl</artifactId>
        <version>2.20.0</version>
    </dependency>
</dependencies>
```

``` xml title="log4j2.xml:日志配置文件"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/log4j2.xml"
```

## bean的实例化方式

### 构造方法

直接配置文件中配置类的全路径，Spring会调用`构造方法`创建对象。

```xml
<!--Spring会根据class类路径，通过构造方法创建对象-->
<bean id="user" class="com.luguosong.ioc.User"/>
```

### 简单工厂

编写简单工厂：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/create_bean/SimpleFactory.java"
```

编写配置文件，配置`简单工厂类`：

``` xml
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_create_simple_factory.xml"
```

通过简单工厂创建对象：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/create_bean/SimpleFactoryTest.java"
```

### 工厂方法

实现`FactoryBean<T>`接口的对象本身就是用于`创建单个对象的工厂`(工厂方法模式)。如果一个bean实现了这个接口，它将作为一个`工厂`来暴露对象，而不是直接作为一个bean实例暴露自身。

!!! warning

    实现此接口的bean不能用作普通bean。一个FactoryBean以bean的风格定义，但对于bean引用暴露的对象（通过getObject()）总是它创建的对象。

容器只负责管理FactoryBean实例的生命周期，而不负责FactoryBean创建的对象的生命周期。因此，暴露的bean对象上的销毁方法（如java.io.Closeable.close()）不会自动调用。相反，FactoryBean应该实现`DisposableBean接口`并将任何此类关闭调用委托给底层对象。


#### 通过配置文件实现

编写工厂对象：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/create_bean/FactoryMethod2.java"
```

编写配置文件：

``` xml title="ioc_create_factory_method2.xml"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_create_factory_method2.xml"
```

测试：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/create_bean/FactoryMethod2Test.java"
```

#### 直接继承FactoryBean接口

编写工厂类，继承`FactoryBean<T>`接口：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/create_bean/FactoryMethod1.java"
```

编写配置文件：

``` xml title="ioc_create_factory_method1.xml"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_create_factory_method1.xml"
```

测试：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/create_bean/FactoryMethod1Test.java"
```

## 依赖注入

### set注入

`<property>标签`通过`set方法`进行依赖注入。

> 可以使用`p命名空间`简化set注入

设置配置文件：

``` xml title="ioc_set.xml"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_set.xml"
```

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/dependency_injection/IocSetterTest.java"
```

!!! property标签的name属性

    `name属性`对应set方法方法名去除set，并且首字母小写。

### 构造方法注入

`<constructor-arg>标签`通过`构造方法`进行依赖注入。

- 默认通过参数的类型推断指定参数(❗这种方式构造函数中的参数类型不能重复)
- 通过`index属性`指定构造函数参数位置设置参数
- 通过`name属性`指定构造函数参数名称设置参数

> 可以使用`c命名空间`简化构造方法注入

设置配置文件：

``` xml title="ioc_constructor.xml"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_constructor.xml"
```

测试：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/dependency_injection/IocConstructorTest.java"
```


### util命名空间

util命名空间针对与集合的复用场景。

``` xml
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_util.xml"
```

### 自动注入

依赖可以根据`名字`或`类型`自动注入。

基于的还是`set方法`。

``` xml
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_auto.xml"
```

### 加载外部配置文件

``` properties title="jdbc.properties"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/jdbc.properties"
```

``` xml
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_jdbc.xml"
```

## bean作用域

- `单例（singleton）`:将单个bean定义范围限定为每个Spring IoC容器的单个对象实例。
- `原型（prototype）`:将单个bean定义范围限定为任意数量的对象实例。

仅在支持web的Spring ApplicationContext上下文中有效：

- `请求（request）`:将单个bean定义范围限定为单个HTTP请求的生命周期。也就是说，每个HTTP请求都有自己的bean实例，该实例是基于单个bean定义创建的。
- `会话（session）`:将单个bean定义范围限定为HTTP会话的生命周期。
- `应用程序（application）`:将单个bean定义范围限定为ServletContext的生命周期。
- `WebSocket（websocket）`:将单个bean定义范围限定为WebSocket的生命周期。

``` xml
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_scope.xml"
```

## bean生命周期

1. `实例化Bean`
2. `调用setter方法属性赋值`
3. `实现Aware相关接口，执行对应方法`:BeanNameAware接口、BeanClassLoaderAware接口、BeanFactoryAware接口
4. `初始化Bean之前执行`:自定义处理器，实现BeanPostProcessor接口，重写postProcessBeforeInitialization方法
5. `实现InitializingBean接口执行对应方法`
6. `初始化Bean`：配置文件bean配置init-method属性
7. `初始化Bean之后执行`：自定义处理器，实现BeanPostProcessor接口，重写postProcessAfterInitialization方法
8. `使用Bean`
9. `实现DisposableBean接口，执行对应方法`
10. `销毁Bean`:配置文件bean配置destroy-method属性

!!! warning

    Spring容器只对作用域`scope="singleton"`的bean进行完整的生命周期管理。

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/life_cycle/Dog.java"
```

配置文件，指定`init-method`和`destroy-method`:

``` xml
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_life_cycle.xml"
```

配置处理器，其方法在初始化前后执行：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/life_cycle/MyBeanPostProcessor.java"
```

测试：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/life_cycle/LifeCycleTest.java"
```

## 将自己new的对象添加到Bean

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/other/MyObjectToApplication.java"
```

## 注解开发

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202406121824480.png){ loading=lazy }
  <figcaption>注解开发依赖于aop包，context包中包含aop了</figcaption>
</figure>

### 入门示例

通过注解的方式引入依赖：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/annotation/hello/User.java"
```

!!! note

    如果不设置`@Component`注解的value属性，Spring会默认使用类名首字母小写作为bean的id

配置文件配置包扫描：

``` xml title="ioc_annotation_hello.xml"
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/resources/ioc_annotation_hello.xml"
```

测试：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/annotation/hello/Test.java"
```

### @Component别名

一下别名功能与`@Component`相同，只是为了区分不同使用场景：

- `@Controller`：表示层bean
- `@Service`：业务逻辑层bean
- `@Repository`：持久化层bean

### 依赖注入

使用注解方式就行依赖注入可以分为以下几种方式：

- `@Value`:注入基本数据类型
- `@Autowired`:根据类型注入（接口只能有一个实现类）
- `@Autowired+@Qualifier`：根据bean名称注入
- `@Resource`：根据bean名称注入

!!! warning

    如果`@Resource`根据名称没有找到对应的类，则会根据`类型`注入。

    此时也会要求接口只有唯一的实现类。

要使用`@Resource`注解需要依赖以下包：

```xml
<dependency>
    <groupId>jakarta.annotation</groupId>
    <artifactId>jakarta.annotation-api</artifactId>
    <version>3.0.0</version>
</dependency>
```

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/annotation/dependency_injection/User.java"
```

### 注解配置文件

可以使用`@Configuration`注解替代xml方式的配置文件。做到`全注解无xml`开发。

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/annotation/config/SpringConfig.java"
```

编写测试类,将`ClassPathXmlApplicationContext`更改为`AnnotationConfigApplicationContext`，从`配置类`读取配置：

``` java
--8<-- "code/java-serve/spring/basic/spring-hello/src/main/java/com/luguosong/ioc/annotation/config/Test.java"
```

