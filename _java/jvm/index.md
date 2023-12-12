---
layout: note
title: JVM
nav_order: 20
create_time: 2023/5/4
---

# 相关工具

## Bytecode-Viewer

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202310101701385.png)

该工具的相关配置信息会保存到`~/.Bytecode-Viewer`目录下。删除该文件夹即可清除缓存配置：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202310101702988.png)

## jclasslib Bytecode Viewer

安装IDEA插件jclasslib Bytecode Viewer。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202310111038998.png)

# 🍪类加载子系统

负责将.class字节码文件加载到内存（运行时数据区）中，并对数据进行校验、转换解析和初始化，最终形成可以被虚拟机直接使用的Java类型。

# 加载（Loading）

## 加载过程

加载步骤：

1. 通过一个类的全限定名来获取定义此类的二进制字节流。
2. 将这个字节流所代表的静态存储结构转化为`方法区`的运行时数据结构。
3. 在内存中生成一个代表这个类的`java.lang.Class对象`，作为方法区这个类的各种数据的`访问入口`。

## 类加载器分类

- `Bootstrap ClassLoader`:引导类加载器,负责加载Java核心库
    - 使用C/C++语言实现，嵌套在JVM内部。不继承自`java.lang.ClassLoader`，没有父加载器
    - 加载`<JAVA_HOME>/lib`目录下的类库。只加载包名为`java`、`javax`、`sun`等开头的类
- `Extension ClassLoader`:扩展类加载器,责加载JRE扩展目录中的类
    - 使用Java语言实现，是`java.lang.ClassLoader`的子类
    - 加载`<JAVA_HOME>/lib/ext`目录下的类库
    - 如果用户自定义的类放在`<JAVA_HOME>/lib/ext`目录下，也会由扩展类加载器加载
- `System ClassLoader`:系统类加载器,也称为应用类加载器,负责加载应用程序类路径上指定的类。
    - 使用Java语言实现，是`java.lang.ClassLoader`的子类
- 用户自定义类加载器

{% highlight java %}
{% include_relative jvm_demo/src/main/java/com/luguosong/ClassLoaderPrintDemo.java %}
{% endhighlight %}

{% highlight java %}
{% include_relative jvm_demo/src/main/java/com/luguosong/ClassLoaderDemo.java %}
{% endhighlight %}

## 自定义类加载器

什么情况使用类加载器：

1. 隔离加载类
2. 修改类加载的方式
3. 扩展加载源
4. 防止源码泄露

## 双亲委派机制

双亲委派过程：

1. 当一个类加载器收到类加载请求时，首先将加载任务委托给父加载器。
2. 如果父加载器仍然存在父加载器，则继续向上委托，直至Bootstrap ClassLoader。
3. 如果父加载器可以完成类加载任务，则返回父加载器加载的类。如果父加载器无法完成此加载任务，则子加载器才会尝试自己去加载。

双亲委派机制的好处：

1. 避免类的重复加载：当父加载器已经加载了该类时，就没有必要子加载器再加载一次。
2. 保护程序安全，防止核心API被随意篡改：当用户自己编写一个java.lang.String类时，程序会优先使用Bootstrap
   ClassLoader加载器加载核心库中的String类，而不是使用用户编写的String类。

# 链接（Linking）

## 验证（Verification）

为了确保Class文件的字节流中包含的信息符合当前虚拟机的要求，并且不会危害虚拟机自身的安全。

包含以下验证：

- 文件格式验证
- 元数据验证
- 字节码验证
- 符号引用验证

## 准备（Preparation）

为`类变量`分配内存并设置该类变量的默认`初始默认值`。比如int类型赋值为0。

{: .warning}
> 这里`并非初始化`，而是为类变量分配内存并设置初始值的阶段，这些变量所使用的内存都将在方法区中进行分配。

{: .warning}
> 这里`不包含常量`，只有静态变量。应该常量在`编译阶段`就已经分配值了。

## 解析（Resolution）

将常量池内的符号引用替换为`直接引用`的过程。

# 初始化（Initialization）

执行类构造器方法`<clinit>()`的过程。用于初始化类变量。

{: .warning}
> 这里的`类构造器方法`是指`<clinit>()`方法，不是`构造函数`。

javac构造器自动搜集类中的所有`类变量的赋值动作`和`静态代码块`中的语句合并产生的。

如果类中不存在类变量的赋值动作和静态代码块，那么编译器也不会为该类生成`<clinit>()`方法。

```java
public class ClinitTest {

    //这段代码会被收集到<clinit>类构造器中
    public static int num1 = 10;

    static {
        //这段代码也会被收集到<clinit>类构造器中
        num1 = 11;
        num2 = 22;
    }

    //这段代码会被收集到<clinit>类构造器中
    //因为<clinit>类构造器是按照源代码顺序收集的
    //因此这段代码会覆盖上面的num2 = 22
    public static int num2 = 20;

    public static void main(String[] args) {
        System.out.println(num1); //11
        System.out.println(num2); //22
    }
}
```

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202310110930403.png)

# 🍪运行时数据区概述

- 多线程共享
    - 方法区
    - 堆
- 线程私有
    - PC寄存器
    - 虚拟机栈
    - 本地方法栈

{: .note-title}
> java.lang.Runtime
> 
> 一个JVM实例对应一个Runtime实例，该实例是单例的。
> 
> Runtime实例就相当于运行时数据区，可以通过该实例获取运行时数据区的相关信息和操作运行时数据区。

{% highlight java %}
{% include_relative jvm_demo/src/main/java/com/luguosong/RunTimeDemo.java %}
{% endhighlight %}

# PC寄存器

`PC寄存器（Program Counter Register）`也叫程序计数器，用来存储指向下一条指令的地址，也即将要执行的指令代码。由执行引擎读取下一条指令。



# 虚拟机栈

# 本地方法栈

# 堆

# 方法区

# 执行引擎


