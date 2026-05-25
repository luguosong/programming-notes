---
title: "概述"
---

## Java 技术体系

Java 技术既是一种编程语言，也是一个平台。

### Java 编程语言

Java 编程语言是一种高级语言。

在 Java 编程语言中，所有源代码首先以`纯文本文件`的形式编写，文件扩展名为 .java 。这些源文件随后由 javac 编译器`编译`成
.class 文件。 .class 文件不包含特定于你的处理器的本地代码，而是包含字节码（bytecode）— Java 虚拟机（Java VM）的机器语言。 java
启动工具随后使用 Java 虚拟机的一个实例来运行你的应用程序。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260524101942984.png){ loading=lazy }
  <figcaption>软件开发流程概览</figcaption>
</figure>

由于 Java VM 可在许多不同的操作系统上使用，相同的 .class 文件能够在 Microsoft Windows、Solaris™ 操作系统（Solaris OS）、Linux
或 Mac OS 上运行。某些虚拟机（如 Java SE HotSpot）在运行时执行额外的步骤以提升应用程序的性能。这包括各种任务，例如查找性能瓶颈和重新编译（编译为本地代码）经常使用的代码段。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260524102045930.png){ loading=lazy }
  <figcaption>通过 Java VM，同一个应用程序能够在多个平台上运行。</figcaption>
</figure>

### Java 平台

Java 平台有两个组件：

- Java 虚拟机
- Java 应用编程接口 (API)

API 是一个庞大的现成软件组件集合，提供许多有用的功能。它被组织成相关类和接口的库；这些库被称为包 (Package)。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260524102234733.png){ loading=lazy }
  <figcaption>API 和 Java 虚拟机将程序与底层硬件隔离开来</figcaption>
</figure>

作为一个平台无关的环境，Java 平台的运行速度可能比本地代码慢一些。然而，编译器和虚拟机技术的进步正在使性能接近本地代码的水平，同时不会牺牲可移植性。

## 核心特性

### 简单易学

C 语言功能强大但陷阱众多：手动管理内存、指针操作容易越界、没有内置字符串和集合类型。Java 的设计目标之一就是「去掉 C/C++
中容易出错的特性」：

- `没有指针运算`——你操作的是对象引用，而非裸内存地址
- `没有手动内存释放`——垃圾回收器自动处理
- `统一的字符串和数组`——`String` 是内置类型，数组自带长度信息
- `布尔类型独立`——`boolean` 不能与 `int` 互相转换，杜绝 `if (a = 0)` 这类拼写错误

``` java title="Java vs C 内存管理对比"
// ❌ C 语言：忘记 free 导致内存泄漏
int* arr = (int*) malloc(100 * sizeof(int));
// ... 使用 arr
// 如果忘记 free(arr)，内存永远不会被回收

// ✅ Java：无需手动释放，GC 自动回收
int[] arr = new int[100];
// 离开作用域后，GC 会自动回收
```

### 面向对象

Java 是「纯」面向对象语言——除基本类型（`int`、`boolean` 等）外，一切皆对象。与 C++ 不同，Java 不支持多重继承和全局函数，强制用类和接口组织代码。

三大核心机制：

- `封装`：通过访问修饰符（`private`/`protected`/`public`）控制内部状态的可见性
- `继承`：子类继承父类的属性和方法，实现代码复用（Java 只允许单继承）
- `多态`：同一接口的不同实现，运行时动态绑定

``` java title="多态示例"
Animal dog = new Dog();
Animal cat = new Cat();
dog.speak();  // "汪汪"
cat.speak();  // "喵喵"
```

### 平台无关性（Write Once, Run Anywhere）

这是 Java 最著名的卖点——`Write Once, Run Anywhere`（一次编译，到处运行）。

C/C++ 编译后生成的是特定平台的机器码，Windows 编译的程序无法直接在 Linux 上运行。而 Java 编译后生成的是`字节码`（`.class`
文件），由 JVM 在运行时解释或编译为本地代码。只要目标平台安装了 JVM，同一份字节码就能运行。

整个流程在「Java 编程语言」一节中已有图示，核心链路为：

`.java` → `javac` → `.class`（字节码）→ JVM → 任意操作系统

### 安全性

Java 从四个层面保障安全：

- `字节码校验器`：加载 `.class` 文件时检查字节码是否合法，防止篡改
- `沙箱模型`：不受信任代码运行在受限环境中，无法随意访问本地文件系统
- `无指针运算`：程序无法直接操作内存地址，杜绝缓冲区溢出攻击
- `数组边界检查`：每次访问都会验证索引范围，越界时抛出异常

### 高性能（JIT 即时编译）

Java 的「解释型语言一定慢」的印象早已过时。现代 JVM 的 HotSpot 编译器在运行时将热点代码（被频繁执行的代码段）`即时编译`
（JIT，Just-In-Time Compilation）为本地机器码，使其性能接近 C/C++。

``` text
字节码 ──解释执行──→ 首次运行（较慢）
    │
    └──热点检测──→ JIT 编译为本地码 ──→ 后续运行（接近原生速度）
```

此外，JVM 还会持续进行`内联优化`、`逃逸分析`、`循环展开`等编译优化，程序运行时间越长，优化效果越好。

### 多线程支持

C 语言的多线程依赖操作系统 API（如 POSIX `pthread`），不同平台写法不同，调试困难。Java 在`语言级别`内置多线程支持：

- `Thread` 类和 `Runnable` 接口：创建线程的基础 API
- `synchronized` 关键字：语言级互斥锁
- `java.util.concurrent` 包：线程池、并发集合、原子操作等高级工具
- `虚拟线程`（Java 21+）：轻量级线程，可轻松创建百万级并发任务

``` java title="线程创建对比"
// ❌ C 语言（POSIX）
pthread_t thread;

pthread_create(&thread, NULL, &my_function, NULL);

pthread_join(thread, NULL);

// ✅ Java
Thread thread = new Thread(() -> System.out.println("Hello"));
thread.start();
```

### 自动垃圾回收（GC）

C/C++ 中，程序员必须手动 `malloc`/`free` 或 `new`/`delete` 来管理内存。忘记释放会`内存泄漏`，重复释放会`程序崩溃`
——据统计，C/C++ 程序中约 40% 的 bug 与内存管理有关。

Java 的垃圾回收器（GC，Garbage Collector）自动追踪对象的引用关系，回收不再被引用的对象所占内存：

``` java title="GC 自动回收示例"
public void processData() {
    // 创建大对象
    byte[] data = new byte[1024 * 1024 * 100];  // 100MB

    // 使用 data 处理业务...

} // ← 方法返回后，data 不再被引用，GC 会在适当时机自动回收这 100MB
```

开发者无需（也无法）手动释放内存，从根源上消除了内存泄漏和悬垂指针问题。

## 发展史

### 诞生：从嵌入式到互联网

Java 诞生于 `1990` 年 Sun Microsystems 的一个内部项目。工程师 James Gosling 起初为嵌入式设备开发一种 C++ 的替代语言，命名为
Oak（源自他窗外的橡树）。

嵌入式项目未能商业化，团队在 `1994` 年将方向转向万维网——用 Java 编写的 `applet` 可以在浏览器中运行，这在当时是革命性的。
`1995 年 5 月 23 日`，Sun 在 SunWorld 大会上正式发布 Java，Netscape 同日宣布浏览器支持 Java，语言迅速引发行业关注。

发布后的头两年，Java 的普及速度惊人：`1996 年 4 月`，10 家主要操作系统供应商声明将嵌入 Java 技术；`1996 年 9 月`，已有约 8.3
万个网页采用 Java 技术制作；`1997 年 4 月`首届 JavaOne 大会吸引逾万人参会，创当时全球同类会议规模纪录；`2001 年` Nokia 宣布到
2003 年将售出 1 亿部支持 Java 的手机。

### 版本演进

???+ note "版本命名变迁"

    Java 的版本命名经历过几次调整：

    - `JDK 1.0 ~ 1.1`：早期直接叫 JDK
    - `J2SE 1.2 ~ 5.0`：`1998` 年进入 Java 2 时代；`1999 年 6 月` Sun 将 Java 划分为三个版本：标准版（J2SE）、企业版（J2EE）和微型版（J2ME）
    - `J2SE 1.5 → Java SE 5.0`：`2004` 年为强调版本重要性，首次去掉"1."前缀
    - `Java SE 6 起`：`2005 年 6 月` JavaOne 大会上，Sun 宣布统一去掉命名中的"2"（J2SE → Java SE、J2EE → Java EE、J2ME → Java ME）
    - `Java 9 起`：采用每 6 个月发布一个新版本的快速发布节奏，每 2 年推出一个长期支持（LTS）版本

以下仅列出对语言和平台有`重大影响`的里程碑版本：

| 版本               | 时间      | 关键特性                                             |
|------------------|---------|--------------------------------------------------|
| JDK 1.0          | 1996-01 | 语言诞生，Applet 模型                                   |
| JDK 1.1          | 1997-02 | 内部类、JavaBeans、JDBC                               |
| J2SE 1.2         | 1998-12 | 集合框架、`反射`、Swing、JIT 编译器                          |
| J2SE 1.3         | 2000-05 | `HotSpot JVM` 捆绑、JNDI、JavaSound                  |
| J2SE 1.4         | 2002-02 | 正则表达式、异常链、NIO、XML 解析器                            |
| J2SE 5.0         | 2004-09 | `泛型`、for-each、自动装箱、`枚举`、可变参数                     |
| Java SE 6        | 2006-12 | 脚本语言支持（JSR 223）、可插拔注解                            |
| Java SE 7        | 2011-07 | try-with-resources、switch 字符串、`fork/join 框架`     |
| Java SE 8 (LTS)  | 2014-03 | `Lambda 表达式`、Stream API、`Optional`、新日期时间 API     |
| Java SE 11 (LTS) | 2018-09 | HTTP Client、Flight Recorder、移除 Java EE 模块        |
| Java SE 17 (LTS) | 2021-09 | 密封类（预览）、模式匹配（预览）、移除实验性 AOT                       |
| Java SE 21 (LTS) | 2023-09 | `虚拟线程`、模式匹配 switch、记录模式                          |
| Java SE 22       | 2024-03 | `Foreign Function & Memory API`（正式版）、多文件源码直接运行（预览）   |
| Java SE 23       | 2024-09 | ZGC 分代模式默认启用、Markdown 文档注释                       |
| Java SE 24       | 2025-03 | `永久禁用 Security Manager`、抗量子加密模块、分代 Shenandoah GC |
| Java SE 25 (LTS) | 2025-09 | `结构化并发`（第五次预览）、`Scoped Values`（正式版）、紧凑源文件、灵活构造器    |
| Java SE 26       | 2026-03 | 最新非 LTS 版本，下一个 LTS 为 Java 29（2027-09）            |

!!! tip "选择哪个版本学习？"

    当前推荐以 `Java 25 LTS` 作为学习和开发基准。它是最新长期支持版本（2025-09 发布），`虚拟线程`已全面成熟，`Scoped Values` 正式落地。`结构化并发`仍在预览阶段（API 持续演进中），主流框架已跟进支持。下一个 LTS 将是 2027 年 9 月的 Java 29。

### 从 Sun 到 Oracle

`2010` 年 Oracle 收购 Sun Microsystems，Java 的开发和维护由 Oracle 接手。收购后 Oracle 对 Java 社群的态度引发争议——
`2010 年 11 月`，Apache 威胁退出 JCP（Java Community Process），抗议 Oracle 对 TCK（技术兼容性包）授权的限制。

自 Java 11 起，Oracle JDK 采用商业许可证，同时社区驱动的 OpenJDK 成为免费开源的替代方案。两个发行版在代码上完全一致，区别仅在于许可协议和少量商业特性。

## 从零编写第一个 Java 程序

### JDK 下载与环境配置

Java 开发工具包（JDK）包含了编译器、运行时环境和各种开发工具。你可以从 Oracle 官方网站或 OpenJDK 社区下载适合你操作系统的版本。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260524153305118.png){ loading=lazy }
  <figcaption>选择安装位置</figcaption>
</figure>

安装目录说明：

``` text
$JAVA_HOME/
├── bin/        # 可执行工具目录（java/javac/jstack 等命令均在此）
├── conf/       # 运行时配置文件（安全策略/网络/日志等）
├── include/    # C/C++ 头文件（JNI/JVMTI 开发时 #include 此处）
├── jmods/      # 模块化 JDK 包（.jmod 文件，jlink 构建自定义 JRE 的原料）
├── legal/      # 各模块开源协议声明（LICENSE/NOTICE，发布合规用）
├── lib/        # JVM 核心库(JDK类库)与内部数据（非用户 classpath，勿混淆）
├── man/        # 命令手册页（man java 等，部分发行版/平台才有）
└── release     # 纯文本版本描述文件（JAVA_VERSION/OS_ARCH 等 KV 信息）
```

将 `bin` 目录配置到环境变量：

- Windows：系统属性 → 高级系统设置 → 环境变量 → Path → 新建 `%JAVA_HOME%\bin`

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260524164028808.png){ loading=lazy }
  <figcaption>先配置JAVA_HOME</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20260524164114732.png){ loading=lazy }
  <figcaption>再基于JAVA_HOME配置path</figcaption>
</figure>

!!! warning

    `JAVA_HOME` 是给 Maven/Tomcat 等工具定位 JDK 根目录用的，PATH 只是让 shell 能执行 `java` 命令，两者解决不同层次的问题。

### Hello World

编写 `HelloWorld` 源码

``` java title="HelloWorld.java"
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

编译源码

```shell
# 在命令行中，进入 HelloWorld.java 所在目录，执行：
javac HelloWorld.java
```

运行编译后会生成一个 `HelloWorld.class` 文件，包含 Java 字节码。执行以下命令运行程序：

```shell
java HelloWorld
```

!!! warning

    注意，`java` 命令后面跟的不是文件名，而是类名

```shell
# 命令行输出
Hello, World!
```

#### public class 与 class 的区别

一个 Java 源文件中可以定义多个 `class`。编译之后，一个 `class` 就会对应生成一个 `class` 字节码文件。

如果一个类是 `public` 的，类名必须和源文件名保持一致。`public` 的类可以没有。如果有的话，也只能有一个。

### 加载与执行原理

- 编译阶段
    - Java 源代码（.java）通过 `javac` 命令编译成字节码（.class）
- 运行阶段
    - 通过 `java` 命令启动 JVM，JVM 加载 .class 文件，执行字节码
        - JVM 包含类加载器（ClassLoader）负责加载 .class 文件。类加载器根据CLASSPATH环境变量寻找.class文件。
        - 字节码通过解释器或 JIT 编译器转换为本地机器码执行。

!!! note "Java即是编译型语言又是解释型语言"

    源码先由javac`编译`成平台无关的字节码（`.class`），再由 JVM `解释执行`，同时 JIT 会将热点字节码`编译`成本地机器码。

#### CLASSPATH 加载机制

JVM 启动时，ClassLoader 按以下顺序加载 .class 文件：

```text
Bootstrap ClassLoader   → JDK核心库 (rt.jar, $JAVA_HOME/lib)
    ↓
Extension ClassLoader   → 扩展库 ($JAVA_HOME/lib/ext)
    ↓
Application ClassLoader → CLASSPATH 指定路径
```

!!! note "当前目录 ."

    - 未设置 CLASSPATH 环境变量时，默认 CLASSPATH = .（当前目录）
    - 一旦你手动设置了 CLASSPATH，. 就不再自动包含，需显式加上

    ```bash
    # 手动设置时必须带上 .，否则当前目录失效
    CLASSPATH=.;C:\libs\foo.jar
    ```

### Java 注释类型

```java
// 单行注释

/*
多行注释
多行注释
 */

/**
 * 文档注释（JavaDoc）
 * 用于生成 API 文档，支持 HTML 标签和特殊标记（@param/@return 等）
 * javadoc -d doc/ HelloWorld.java
 */
```
