---
layout: note
title: 异常
nav_order: 100
parent: JavaSE
create_time: 2023/5/24
---

# 错误和异常分类

- `java.lang.Throwable`：错误和异常的超类，表示可以被throw和catch
    - `java.lang.Error`：错误，程序无法处理，比如内存溢出和栈溢出
        - `java.lang.VirtualMachineError`：虚拟机错误，比如栈溢出
            - `java.lang.StackOverflowError`：栈溢出
            - `java.lang.OutOfMemoryError`：内存溢出
    - `java.lang.Exception`：异常，程序可以处理，分为两类
        - `java.lang.RuntimeException`：运行时异常，比如空指针异常、数组下标越界异常
            - `java.lang.ArrayIndexOutOfBoundsException`：数组下标越界异常
            - `java.lang.NullPointerException`：空指针异常
            - `java.lang.ClassCastException`：将对象转换为它不是其实例的子类
        - 非运行时异常，比如IO异常、SQL异常
          - `java.lang.IOException`：IO异常
          - `java.lang.ClassNotFoundException`：找不到类异常
    

# 运行时异常

{% highlight java %}
{% include_relative RuntimeExceptionExample.java %}
{% endhighlight %}

# 编译时异常

{% highlight java %}
{% include_relative CheckedExceptionExample.java %}
{% endhighlight %}

# 自定义异常

{% highlight java %}
{% include_relative CustomException.java %}
{% endhighlight %}
