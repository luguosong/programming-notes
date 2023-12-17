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

# 流

| 分类    | 字节输入流                | 字节输出流                 | 字符输入流             | 字符输出流              |
|-------|----------------------|-----------------------|-------------------|--------------------|
| 抽象基类  | InputStream          | OutputStream          | Reader            | Writer             |
| 访问文件  | FileInputStream      | FileOutputStream      | FileReader        | FileWriter         |
| 访问数组  | ByteArrayInputStream | ByteArrayOutputStream | CharArrayReader   | CharArrayWriter    |
| 访问管道  | PipedInputStream     | PipedOutputStream     | PipedReader       | PipedWriter        |
| 访问字符串 |                      |                       | StringReader      | StringWriter       |
| 缓冲流   | BufferedInputStream  | BufferedOutputStream  | BufferedReader    | BufferedWriter     |
| 转换流   |                      |                       | InputStreamReader | OutputStreamWriter |
| 对象流   | ObjectInputStream    | ObjectOutputStream    |                   |                    |
| 打印流   |                      | PrintStream           |                   | PrintWriter        |
| 推回输入流 | PushbackInputStream  |                       | PushbackReader    |                    |
| 特殊流   | DataInputStream      | DataOutputStream      |                   |                    |

# 文件流

{: .note}
> 使用文件流的时候，如果文件不存在，会自动创建文件。
>
> 不使用手动通过`createNewFile`方法创建文件。

## 字符流

{% highlight java %}
{% include_relative FileReaderAndWriter.java %}
{% endhighlight %}

## 字节流

{% highlight java %}
{% include_relative FileInputStreamAndOutputStream.java %}
{% endhighlight %}

# 缓冲流


