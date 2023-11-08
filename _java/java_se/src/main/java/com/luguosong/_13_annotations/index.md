---
layout: note
title: 注解
nav_order: 130
parent: JavaSE
latex: true
create_time: 2023/9/15
---

# 概述

{: .note-title}
> 注解
>
> `注解`是`Java 5`的一项重要语言更新。
>
> 又叫作`元数据`,使我们可以用正式的方式为代码添加信息，这样就可以在将来方便地使用这些数据。

可以使用注解的地方：

- `声明`：注解可以应用于类、字段、方法和其他程序元素的声明。
- `类型使用`：截至`Java SE 8`发布版本，注解也可以应用于类型的使用。

作用：

- `编译器的信息`：注解可用于由编译器检测错误或抑制警告。
- `编译时和部署时处理`：软件工具可以处理注解信息以生成代码、XML文件等。
- `运行时处理`：一些注解可在运行时进行检查。

# 常见注解

`Java 5`引入了第一批定义在java.lang中的3个通用内建注解:

- `@Deprecated`：标记过时的方法。
- `@Override`：标记方法覆盖。
- `@SuppressWarnings`：抑制编译器警告。

Java 7引入:

- `@SafeVarargs`:用于在使用泛型作为可变参数的方法或构造器中关闭对调用者的警告。

Java 8引入:

- `@FunctionalInterface`：用于表明类型声明是函数式接口。

# 自定义注解

{: .note}
> 没有任何元素的注解（如上面的@Test）称为`标记注解`。

`元注解`（定义注解的注解）：

- `@Target`:定义了该注解可应用的地方。
    - `ElementType.CONSTRUCTOR`——构造器声明
    - `ElementType.FIELD`——字段声明（包括枚举常量）
    - `ElementType.LOCAL_VARIABLE`——本地变量声明
    - `ElementType.METHOD`——方法声明
    - `ElementType.PACKAGE`——包声明
    - `ElementType.PARAMETRE`——参数声明
    - `ElementType.TYPE`——类、接口（包括注解类型）或枚举的声明
- `@Retention`:注解信息可以保存多久。
    - `SOURCE`——注解会被编译器丢弃
    - `CLASS`——注解在类文件中可被编译器使用，但会被虚拟机丢弃
    - `RUNTIME`——注解在运行时仍被虚拟机保留，因此可以通过反射读取到注解信息
- `@Documented`:在Javadoc中引入该注解
- `@Inherited`:允许子类继承父类中的注解
- `@Repeatable`:可以多次应用于同一个声明（Java 8）

定义注解：

{% highlight java %}
{% include_relative custom_annotation/CustomAnnotations.java %}
{% endhighlight %}

使用自定义注解：

{% highlight java %}
{% include_relative custom_annotation/Test.java %}
{% endhighlight %}

