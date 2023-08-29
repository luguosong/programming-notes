---
layout: note
title: 概述
nav_order: 10
parent: JavaSE
create_time: 2023/8/7
---

# JDK和JRE

`JDK`(Java Development Kit)是Java开发工具包，包含了Java的开发工具，也包含了JRE。

`JRE`(Java Runtime Environment)是Java运行环境，包含了JVM和Java的核心类库。

`JDK = JRE + 开发工具(例如：编译器javac.exe等)`

`JRE = JVM + 核心类库`


# JDK安装环境变量配置

1. 新建系统变量`JAVA_HOME`，值为JDK的安装路径，例如：`D:\Program Files\Java\jdk1.8.0_291`
2. ~~新建系统变量`CLASSPATH`，值为`.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOME%\lib\tools.jar;`~~
3. 修改系统变量`Path`，在其值的最前面添加`%JAVA_HOME%\bin;%JAVA_HOME%\jre\bin;`

{: .warning-title}
> 不再需要配置CLASSPATH
> 
> JDK安装后，不需要配置CLASSPATH环境变量。在JDK1.5之前，是没有办法在当前目录下加载类的（找不到 JDK目录下lib文件夹中的.jar文件），所以我们需要通过配置classpath，但JDK1.5之后，JRE能自动搜索目录下类文件，并且加载dt.jar和tool.jar的类。

{: .note}
> JDK17会自动配置环境变量，不需要手动配置了

# Hello World

{% highlight java %}
{% include_relative HelloWorld.java %}
{% endhighlight %}

HelloWorld程序执行流程：
1. 编写源码
2. 编译：`javac HelloWorld.java`，生成字节码文件`HelloWorld.class`
3. 运行：`java HelloWorld`，JVM加载字节码文件，执行main方法

# 注释

```java
/**
 * 文档注释
 * @author luguosong
 * @version 1.0
 */
class Demo {
    public static void main(String[] args) {
        // 单行注释
        
        /*
            多行注释
            多行注释
            多行注释
        */
    }
}
```

# 面试题

<details markdown="block">
<summary>.java源文件中是否可以包括多个类？具体有什么限制？</summary>
是的，可以包含多个类。

但是只能有一个类是public的，且public的类名必须与文件名相同。
</details>
<hr>

<details markdown="block">
<summary>Java的优势</summary>
- 跨平台
- 安全性高
- 面向对象
- 健壮性
- 简单性（相对于C++）
- 社区繁荣
</details>
<hr>

<details markdown="block">
<summary>Java中是否存在内存溢出、内存泄露？如何解决？举例说明</summary>
Java中存在内存溢出、内存泄露。

内存溢出：OutOfMemoryError，例如：堆内存溢出、栈内存溢出、方法区内存溢出等。

内存泄露：内存泄露是指程序中己动态分配的堆内存由于某种原因程序未释放或无法释放，造成系统内存的浪费，导致程序运行速度减慢甚至系统崩溃等严重后果。

解决方法：及时释放不再使用的对象，避免对象引用存在循环引用的情况。
</details>
<hr>

<details markdown="block">
<summary>如何看待Java是一门半编译半解释型语言？</summary>
~~Java是一门半编译半解释型语言，Java源代码首先被编译成字节码文件，然后由JVM解释执行字节码文件。~~

Java字节码文件可能是解释执行的，也可能是JIT即时编译器编译执行的。
</details>
<hr>
