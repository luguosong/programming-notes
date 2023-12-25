---
layout: note
title: File类和I/0
nav_order: 60
parent: JavaSE
latex: true
create_time: 2023/11/30
---

# File类相关操作

{: .note-title}
> 相对路径问题
>
> 如果使用`单元测试`，相对路径相对于当前module
>
> 如果使用`main方法`，相对路径相对于当前工程

{% highlight java %}
{% include_relative FileExample.java %}
{% endhighlight %}

# getAbsoluteFile用处

{% highlight java %}
{% include_relative GetAbsoluteFileExample.java %}
{% endhighlight %}

# 数据流分类

| 分类    | 字节输入流                | 字节输出流                 | 字符输入流             | 字符输出流              |
|-------|----------------------|-----------------------|-------------------|--------------------|
| 抽象基类  | InputStream          | OutputStream          | Reader            | Writer             |
| 访问文件  | FileInputStream      | FileOutputStream      | FileReader        | FileWriter         |
| 缓冲流   | BufferedInputStream  | BufferedOutputStream  | BufferedReader    | BufferedWriter     |
| 转换流   |                      |                       | InputStreamReader | OutputStreamWriter |
| 数据流   | DataInputStream      | DataOutputStream      |                   |                    |
| 对象流   | ObjectInputStream    | ObjectOutputStream    |                   |                    |
| 打印流   |                      | PrintStream           |                   | PrintWriter        |
| 访问数组  | ByteArrayInputStream | ByteArrayOutputStream | CharArrayReader   | CharArrayWriter    |
| 访问管道  | PipedInputStream     | PipedOutputStream     | PipedReader       | PipedWriter        |
| 推回输入流 | PushbackInputStream  |                       | PushbackReader    |                    |
| 访问字符串 |                      |                       | StringReader      | StringWriter       |

# 文件流

{: .note}
> 使用文件流的时候，如果文件不存在，会自动创建文件。
>
> 不使用手动通过`createNewFile`方法创建文件。

## 文件字符流

{% highlight java %}
{% include_relative FileReaderAndWriter.java %}
{% endhighlight %}

## 文件字节流

{% highlight java %}
{% include_relative FileInputStreamAndOutputStream.java %}
{% endhighlight %}

# 缓冲流

{: .note}
> 缓冲流的作用
>
> 当一定量的数据写入到缓冲区后，再一次性写入到磁盘，减少磁盘的交互次数，提高效率。

## 文件缓冲字符流

{% highlight java %}
{% include_relative BufferedReaderAndWriter.java %}
{% endhighlight %}

## 文件缓冲字节流

{% highlight java %}
{% include_relative BufferedInputStreamAndOutPutStream.java %}
{% endhighlight %}

# 转换流

作用：使用指定的字符集，将字节流转换为字符流。

`文件字符流`其实是`转换流`的子类，使用默认的字符集进行读写文件。

默认字符集在虚拟机启动时确定，通常取决于底层操作系统的语言和字符集。

{% highlight java %}
{% include_relative CharSetExample.java %}
{% endhighlight %}

使用转换流读取指定字符集的文件：

{% highlight java %}
{% include_relative InputStreamReaderExample.java %}
{% endhighlight %}

# 数据流和对象流

## 对象序列化

`对象序列化机制`允许把内存中的`Java对象`转换成平台无关的`二进制流`，从而允许把这种二进制流持久地保存在磁盘上，或通过网络将这种二进制流传输到另一个网络节点。

当其它程序获取了这种`二进制流`，就可以恢复成原来的`Java对象`。

## 数据流

处理`基本数据类型`、`String类型`。

{% highlight java %}
{% include_relative DataInputStreamAndOutputStream.java %}
{% endhighlight %}

## 对象流

可以处理`基本数据类型`和`对象类型`。

{: .note-title}
> 可序列化对象的要求
>
> 1. 实现`Serializable`接口
> 2. 所有属性必须是可序列化的
> 3. 声明serialVersionUID常量（非必须如果对象以后不再修改的话）
> 4. 使用transient修饰的属性不会被序列化

{% highlight java %}
{% include_relative ObjectInputStreamAndOutputStream.java %}
{% endhighlight %}

# 打印流

{% highlight java %}
{% include_relative PrintStreamExample.java %}
{% endhighlight %}

# 使用common io简化开发

{% highlight java %}
{% include_relative CommonIOExample.java %}
{% endhighlight %}
