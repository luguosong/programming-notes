---
layout: note
title: Spring
nav_order: 1000
create_time: 2023/12/26
---

# Spring容器接口和实现

- `BeanFactory接口`:容器最基本的接口
    - `ApplicationContext接口`:增加了更多功能
        - `ClassPathXmlApplicationContext实现类`:从类路径下加载配置文件
        - `FileSystemXmlApplicationContext实现类`:从文件系统中加载配置文件
        - `AnnotationConfigApplicationContext实现类`:从注解中加载配置类

# Spring容器的作用

- `控制反转（Ioc, Inversion of Control）`:将对象的创建和对象之间的调用过程交给Spring容器来管理,当需要某个对象时,只需要从容器中取出即可,而不用自己创建。
- `依赖注入（DI, Dependency Injection）`:对象之间的依赖关系由Spring容器在运行期间动态的确定,并将依赖关系注入到对象之中。

# XML控制反转和依赖注入

{: .warning}
> 配置Bean后，并不意味着程序一启动就会创建Bean，只有当程序需要使用Bean时，才会创建Bean。

- 引入依赖：

```xml

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-context</artifactId>
    <version>6.1.2</version>
</dependency>
```

- 创建组件

{% highlight java %}
{% include_relative ioc-example/src/main/java/com/luguosong/xml_ioc/Component.java %}
{% endhighlight %}

- 组件工厂

{% highlight java %}
{% include_relative ioc-example/src/main/java/com/luguosong/xml_ioc/ComponentFactory.java %}
{% endhighlight %}

- 创建xml配置文件

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202312271608652.png)

{% highlight xml %}
{% include_relative ioc-example/src/main/resources/spring-config1.xml %}
{% endhighlight %}

调用Bean：

{% highlight java %}
{% include_relative ioc-example/src/main/java/com/luguosong/xml_ioc/IocTest.java %}
{% endhighlight %}




