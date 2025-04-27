# 概述

## 关于 Java 技术

Java 技术既是一种编程语言，也是一种平台。

### Java编程语言

Java 编程语言是一种高级语言，可以用以下所有流行词来描述：

- Simple：简单
- Object oriented：面向对象
- Distributed：分布式
- Multithreaded：多线程
- Dynamic：动态
- Architecture neutral：架构中立
- Portable：可移植
- High performance：高性能
- Robust：健壮
- Secure：安全

上述每个流行词汇都在詹姆斯·高斯林和亨利·麦吉尔顿撰写的白皮书[《Java语言环境》](https://www.oracle.com/java/technologies/language-environment.html)中进行了详细解释。

在 Java 编程语言中，所有源代码首先写在以 `.java 扩展名`结尾的纯文本文件中。然后，这些源文件通过 `javac 编译器`编译成 `.class 文件`。`.class 文件`不包含与处理器本地的代码；相反，它包含`字节码`——Java 虚拟机（Java VM）的机器语言。接着，java 启动工具使用 Java 虚拟机的一个实例运行你的应用程序。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408281530619.png){ loading=lazy }
  <figcaption>软件开发过程概述。</figcaption>
</figure>

由于 Java 虚拟机可在多种操作系统上使用，相同的 `.class 文件`能够在 Microsoft Windows、Solaris™ 操作系统（Solaris OS）、Linux 或 Mac OS 上运行。一些虚拟机，例如 Java SE HotSpot at a Glance，会在运行时执行额外的步骤，以提升应用程序的性能。这包括查找性能瓶颈和重新编译（为本地代码）经常使用的代码段等各种任务。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408281531430.png){ loading=lazy }
  <figcaption>通过 Java 虚拟机，应用程序能够在多个平台上运行。</figcaption>
</figure>

### Java平台

平台是程序运行的硬件或软件环境。我们已经提到了一些最受欢迎的平台，如微软Windows、Linux、Solaris操作系统和Mac OS。大多数平台可以被描述为操作系统和底层硬件的组合。`Java平台`与大多数其他平台不同，因为它是一个仅基于软件的平台，运行在其他基于硬件的平台之上。

Java 平台有两个组成部分：

- Java虚拟机
- Java应用程序编程接口 (API)

您已经了解了Java虚拟机；它是Java平台的基础，并已移植到各种基于硬件的平台上。

API 是一个大型的现成软件组件集合，提供了许多实用功能。它被分组为相关类和接口的库，这些库被称为包。下一节“Java 技术能做什么？”将重点介绍 API 提供的一些功能。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408281533258.png){ loading=lazy }
  <figcaption>API 和 Java 虚拟机将程序与底层硬件隔离开来。</figcaption>
</figure>

作为一个跨平台的环境，Java平台可能比本地代码稍慢。然而，编译器和虚拟机技术的进步正在使其性能接近本地代码，同时不影响可移植性。

“Java虚拟机”和“JVM”指的是Java平台的虚拟机。

## Java 技术能做什么

通用的高级Java编程语言是一个强大的软件平台。Java平台的每个完整实现都为您提供以下功能：

- `开发工具`：开发工具提供了编译、运行、监控、调试和记录应用程序所需的一切。作为一名新开发者，您将主要使用的工具是 javac 编译器、java 启动器和 javadoc 文档工具。
- `应用程序编程接口（API）`：API 提供了 Java 编程语言的核心功能。它提供了一系列实用的类，供您在自己的应用程序中使用。API 涵盖了从基本对象到网络和安全、XML 生成和数据库访问等各个方面。核心 API 非常庞大；要了解其包含的内容，请查阅 Java 平台标准版 8 文档。
- `部署技术`：JDK 软件提供了标准机制，如 Java Web Start 软件和 Java 插件软件，用于将您的应用程序部署给最终用户。
- `用户界面工具包`：JavaFX、Swing 和 Java 2D 工具包使创建复杂的图形用户界面 (GUI) 成为可能。
- `集成库`：集成库如Java IDL API、JDBC API、Java命名和目录接口（JNDI）API、Java RMI，以及基于互联网ORB协议技术的Java远程方法调用（Java RMI-IIOP技术）等，支持数据库访问和远程对象的操作。

## Java 技术将如何改变我的生活

我们不能保证学习 Java 编程语言会带给你名声、财富，甚至是一份工作。然而，它可能会让你的程序更出色，并且比其他语言需要更少的努力。我们相信，Java 技术将帮助你做到以下几点：

- `快速入门`：尽管 Java 编程语言是一种强大的面向对象语言，但它易于学习，特别是对于已经熟悉 C 或 C++ 的程序员来说。
- `编写更少的代码`：对程序指标（如类数量、方法数量等）的比较表明，用Java编写的程序可能比用C++编写的同一程序小四倍。
- `编写更好的代码`：Java 编程语言鼓励良好的编码实践，自动垃圾回收帮助您避免内存泄漏。其面向对象的特性、JavaBeans™ 组件架构以及广泛且易于扩展的 API 使您能够重用现有的经过测试的代码，并减少引入错误的可能性。
- `更快速地开发程序`：Java 编程语言比 C++ 更简单，因此使用 Java 编写程序的开发时间可能会快一倍。此外，程序所需的代码行数也会更少。
- `避免平台依赖`：通过避免使用其他语言编写的库，您可以保持程序的可移植性。
- `一次编写，到处运行`：由于用 Java 编程语言编写的应用程序被编译成与机器无关的字节码，因此它们可以在任何 Java 平台上稳定运行。
- `更轻松地分发软件`：使用 Java Web Start 软件，用户只需点击鼠标即可启动您的应用程序。启动时的自动版本检查确保用户始终使用您的软件的最新版本。如果有更新可用，Java Web Start 软件会自动更新他们的安装。

## Windows的Hello World!

### 准备

要编写你的第一个程序，你需要：

- `Java SE 开发工具包 8 (JDK 8)`：你现在可以[下载](https://www.oracle.com/java/technologies/downloads/?er=221886) Windows 版本。（请确保下载的是 JDK，而不是 JRE。）请参考[安装说明](https://docs.oracle.com/javase/8/docs/technotes/guides/install/install_overview.html)。
- `文本编辑器`：在这个例子中，我们将使用记事本，这是Windows平台自带的一个简单编辑器。如果你使用其他文本编辑器，可以轻松调整这些说明。

这两个项目就是你编写第一个应用程序所需的一切。

### 创建您的第一个应用程序

您的第一个应用程序 HelloWorldApp 将简单地显示问候语“Hello world!”。要创建这个程序，您将：

- `创建源文件`:源文件包含用Java编程语言编写的代码，您和其他程序员都可以理解。您可以使用任何文本编辑器来创建和编辑源文件。

启动您的编辑器。您可以通过开始菜单选择程序 > 附件 > 记事本来启动记事本编辑器。在新文档中输入以下代码：

``` java
/**
 * The HelloWorldApp class implements an application that
 * simply prints "Hello World!" to standard output.
 */
class HelloWorldApp {
    public static void main(String[] args) {
        System.out.println("Hello World!"); // Display the string.
    }
}
```

!!! note "请注意输入时的细节"

    请准确输入所有代码、命令和文件名。编译器（javac）和启动器（java）对大小写敏感，因此必须保持一致的大小写格式。

    HelloWorldApp 与 helloworldapp 不同。

将代码保存到名为 HelloWorldApp.java 的文件中。

- `将源文件编译为.class文件`:Java 编程语言编译器（javac）将你的源文件翻译成 Java 虚拟机可以理解的指令。这些指令被称为字节码。

打开一个命令窗口。你可以通过开始菜单选择“运行...”，然后输入cmd来实现。命令窗口应类似于下图所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202408281610785.png){ loading=lazy }
  <figcaption>一个命令行窗口。</figcaption>
</figure>

提示符显示您当前的目录。当您调出提示符时，当前目录通常是您的Windows XP主目录（如上图所示）。

要编译源文件，请将当前目录切换到文件所在的目录。例如，如果你的源目录是C盘上的myapplication，请在提示符下输入以下命令并按回车键：

```shell
cd C:\myapplication
```

现在提示符应该更改为 `C:\myapplication>`。

!!! note

    要切换到不同驱动器上的目录，您需要输入一个额外的命令：驱动器的名称。例如，要切换到 D 盘上的 myapplication 目录，您必须输入 D:，如下所示：

    C:\>D:

    D:\>cd myapplication

    D:\myapplication>

如果在提示符下输入 dir，您应该会看到您的源文件，如下所示：

```shell
C:\>cd myapplication

C:\myapplication>dir
 Volume in drive C is System
 Volume Serial Number is F2E8-C8CC

 Directory of C:\myapplication

2014-04-24  01:34 PM    <DIR>          .
2014-04-24  01:34 PM    <DIR>          ..
2014-04-24  01:34 PM               267 HelloWorldApp.java
               1 File(s)            267 bytes
               2 Dir(s)  93,297,991,680 bytes free

C:\myapplication>
```

现在您可以准备编译了。在提示符下输入以下命令并按回车键。

```shell
javac HelloWorldApp.java
```

编译器已生成了一个字节码文件，HelloWorldApp.class。在命令提示符下，输入 dir 查看生成的新文件，如下所示：

```shell
C:\myapplication>javac HelloWorldApp.java

C:\myapplication>dir
 Volume in drive C is System
 Volume Serial Number is F2E8-C8CC

 Directory of C:\myapplication

2014-04-24  02:07 PM    <DIR>          .
2014-04-24  02:07 PM    <DIR>          ..
2014-04-24  02:07 PM               432 HelloWorldApp.class
2014-04-24  01:34 PM               267 HelloWorldApp.java
               2 File(s)            699 bytes
               2 Dir(s)  93,298,032,640 bytes free

C:\myapplication>
```

现在你有了一个 .class 文件，可以运行你的程序了。

- `运行程序`:Java 应用程序启动工具 (java) 使用 Java 虚拟机来运行您的应用程序。

在同一目录下，在提示符中输入以下命令：

```shell
java -cp . HelloWorldApp
```

您应该在屏幕上看到以下内容：

```shell
C:\myapplication>java -cp . HelloWorldApp
Hello World!

C:\myapplication>
```

恭喜！你的程序运行成功！

## 深入探讨Hello World!应用程序

现在您已经看过“Hello World!”应用程序（也许还编译并运行过它），您可能会想知道它是如何工作的。以下是它的代码：

``` java

class HelloWorldApp {
    public static void main(String[] args) {
        System.out.println("Hello World!"); // Display the string.
    }
}
```

"Hello World!" 应用程序主要由三个部分组成：`源代码注释`、`HelloWorldApp 类定义`和`main方法`。以下解释将为您提供对代码的基本理解，但更深层次的含义只有在您阅读完教程的其余部分后才会显现。

### 源代码注释

以下高亮文本定义了“Hello World!”应用程序的注释：

``` java hl_lines="1-4"
/**
 * The HelloWorldApp class implements an application that
 * simply prints "Hello World!" to standard output.
 */
class HelloWorldApp {
    public static void main(String[] args) {
        System.out.println("Hello World!"); // Display the string.
    }
}
```

注释不会被编译器处理，但对其他程序员很有用。Java 编程语言支持三种类型的注释：

- `/* text */` 编译器会忽略从 `/*` 到 `*/` 之间的所有内容。
- `/** documentation */` 这表示一个文档注释（简称为 doc 注释）。编译器会忽略这种注释，就像它忽略使用 `/*` 和 `*/` 的注释一样。javadoc 工具在准备自动生成的文档时会使用 doc 注释。
- `// text` 编译器会忽略从 `//` 到行尾的所有内容。

### HelloWorldApp 类定义

以下高亮文本开始了“Hello World!”应用程序的类定义块：

``` java hl_lines="5 9"
/**
 * The HelloWorldApp class implements an application that
 * simply displays "Hello World!" to the standard output.
 */
class HelloWorldApp {
    public static void main(String[] args) {
        System.out.println("Hello World!"); // Display the string.
    }
}
```

如上所示，类定义的最基本形式是：

``` java
class name {
    //. . .
}
```

关键字`class`用于开始定义一个名为name的类，每个类的代码都位于上面加粗的花括号之间。第二章概述了类的基本概念，第四章则详细讨论了类。目前只需知道每个应用程序都以类定义开始即可。

### main方法

以下高亮文本开始定义main方法：

``` java hl_lines="6 8"
/**
 * The HelloWorldApp class implements an application that
 * simply displays "Hello World!" to the standard output.
 */
class HelloWorldApp {
    public static void main(String[] args) {
        System.out.println("Hello World!"); // Display the string.
    }
}
```

在 Java 编程语言中，每个应用程序都必须包含一个主方法，其签名为：

``` java
public static void main(String[] args)
```

修饰符 `public` 和 `static` 可以按任意顺序书写（public static 或 static public），但惯例是使用如上所示的 `public static`。你可以随意命名参数，但大多数程序员选择使用 "args" 或 "argv"。

主要方法类似于 C 和 C++ 中的主函数；它是应用程序的入口点，随后会调用程序所需的所有其他方法。

主方法接受一个参数：一个由字符串类型元素组成的数组。

public static void main(`String[] args`)

这个数组是运行时系统向您的应用程序传递信息的机制。例如：

```shell
java MyApp arg1 arg2
```

数组中的每个字符串称为命令行参数。命令行参数允许用户在不重新编译应用程序的情况下影响其操作。例如，一个排序程序可能允许用户通过以下命令行参数指定数据按降序排序：

```shell
-descending
```

"Hello World!" 应用程序忽略了其命令行参数，但你应该意识到这些参数的存在。

最后，这一行：

``` java
System.out.println("Hello World!");
```

使用核心库中的 System 类将 "Hello World!" 消息打印到标准输出。这个库的部分内容（也称为 "应用程序编程接口" 或 "API"）将在本教程的其余部分进行讨论。
