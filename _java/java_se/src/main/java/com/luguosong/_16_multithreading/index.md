---
layout: note
title: 多线程
nav_order: 160
parent: JavaSE
create_time: 2023/5/25
---

# 多线程优点

- 提高应用程序的响应，对图形化界面更有意义，可增强用户体验
- 提高计算机系统CPU的利用率
- 改善程序结构，将既长又复杂的进程分为多个线程，独立运行，利于理解和修改

# 创建线程

- 启动线程通过`java.lang.Thread.start`方法
- `java.lang.Thread.start`中会通过Java虚拟机调用这个线程的`java.lang.Thread.run`方法。
- `java.lang.Thread.run`方法中会调用`java.lang.Runnable.run`方法
- 因此创建多线程有两种方法
    - 继承`java.lang.Thread`类，直接从写`java.lang.Thread.run`方法，而不使用`java.lang.Runnable`
    - 在构造中传入`java.lang.Runnable`对象，调用`java.lang.Runnable.run`方法

{% highlight java %}
{% include_relative CreateThread1.java %}
{% endhighlight %}

# 设计模式分析

Runnable接口关键源码片段：

```java
package java.lang;

@FunctionalInterface
public interface Runnable {
    public abstract void run();
}

```

Thread关键源码片段：

```java
public class Thread implements Runnable {
    // 将要运行什么
    private Runnable target;

    // 无参构造
    public Thread() {
        //省略部分代码...
    }


    public Thread(Runnable target) {
        this.target = target;
        //省略部分代码...
    }

    // 线程要运行的代码
    @Override
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    // 启动线程
    public synchronized void start() {
        // 省略部分代码
        // 通过Java虚拟机调用这个线程的run方法
    }
}
```
