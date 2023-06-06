---
layout: note
title: PDF和数字签名
nav_order: 20
has_children: true
parent: iText 7 PDF文件的数字签名
---
# 签名Hello World

{% highlight java %}
{% include_relative C2_01_SignHelloWorld.java %}
{% endhighlight %}

# 签署大型PDF文件

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230406143257.png)

``` java
PdfSigner pdfSigner = new PdfSigner(pdfReader,
                    // 创建输出流，用于将签署后的PDF文档写入文件
                    Files.newOutputStream(Paths.get("_topic/itext/src/test/resources/02_pdf_and_digital_signatures/02_SignHelloWorldWithTempFile/hello_signed_with_temp.pdf")),
                    //*****增加临时写入的文件*****
                    "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/02_SignHelloWorldWithTempFile/",
                    // 创建StampingProperties对象，用于设置签名属性
                    new StampingProperties());
```

{% highlight java %}
{% include_relative C2_02_SignHelloWorldWithTempFile.java %}
{% endhighlight %}

# 创建和签署签名字段

{% highlight java %}
{% include_relative C2_03_CreateFileWithEmptyField.java %}
{% endhighlight %}

# 创建不同的签名外观

## 自定义PdfSignatureAppearance

- `n0`-背景层。
- ~~`n1`-有效性层，用于未知和有效状态。(不再使用)~~
- `n2`-签名外观，包含关于签名的信息。
- ~~`n3`-有效性层，用于无效状态。(不再使用)~~
- ~~`n4`-文本层，用于签名状态的文本表示。(不再使用)~~

{: .warning}

> 自Acrobat 6 (2003)以来，不再推荐使用层 `n1`、`n3`和 `n4`。

{% highlight java %}
{% include_relative C2_04_CustomAppearance.java %}
{% endhighlight %}

## 使用便捷的方法创建数字签名的外观

上边通过修改 `n0`和 `n2`属于从底层对签名外观进行修改。iText还提供了一些便捷方法，用于快速创建数字签名的外观。通过使用这些方法，可以避免使用底层API，更加方便地自定义数字签名的外观。

{% highlight java %}
{% include_relative C2_05_SignatureAppearance.java %}
{% endhighlight %}

## 自定义渲染模式

{% highlight java %}
{% include_relative C2_06_SignatureAppearances.java %}
{% endhighlight %}

## 向签名字典添加元数据

PDF规范允许将以下元数据添加到签名字典中：

- `Name（名称）`：签署文档的人或机构的名称。只有在无法从签名中提取名称时，才应使用此值。
- `M`：签署时间。根据签名处理程序的不同，这可能是普通未经验证的计算机时间或从安全服务器以可验证方式生成的时间。ISO-32000-1 告诉我们，只有在签名中没有可用签署时间时才应使用此选项，但 iText 无论如何都会添加此条目，这并不会有害。
- `Location（位置）`：签名的 CPU 主机名称或实际签名位置。
- `Reason（原因）`：签名的原因，例如“我同意”。
- `ContactInfo（联系信息）`：签署者提供的信息，以使接收者可以联系签署者验证签名，例如电话号码。

{% highlight java %}
{% include_relative C2_07_SignatureMetadata.java %}
{% endhighlight %}

# 在PDF和工作流程中签名

## 普通签名和认证签名

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230411142621.png)

`证书级别`是 `Certification Signature`中的一个重要概念。它用于定义签名者为文档添加的权限范围。在 `iText 7`中，setCertificationLevel方法允许我们设置文档的证书级别。

- `普通签名(Approval Signature)`是一种简单的签名，仅表明签名者已查看并批准了文档内容。
  - `NO_CERTIFICATION`：批准签字。没有证书。也就是 `普通签名(Approval Signature)`
- `认证签名(Certification Signature)`则是一种更高级别的签名，除了具有Approval Signature的所有功能外，还允许签名者定义文档的权限。
  - `CERTIFIED_NO_CHANGES_ALLOWED`：作者签名，不允许更改。
  - `CERTIFIED_FORM_FILLING`：作者签名，允许填写表格。
  - `CERTIFIED_FORM_FILLING_AND_ANNOTATIONS`：允许作者签名、表格填写和注释。

{% highlight java %}
{% include_relative C2_08_SignatureTypes.java %}
{% endhighlight %}

## 顺序签名

{: .warning}

> 签名必须按顺序逐个应用，`无法并行签名`。
>
> 例如：出版商无法同时将他的合同发送给两个作者进行批准，然后在合并签署的文档（与打包在文件夹中不同）。其中一名作者必须首先签署合同，然后由另一名作者签署。

Alice采用 `CERTIFIED_FORM_FILLING`认证签名，Bob和Carol采用 `NOT_CERTIFIED`批准签名：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230413093238.png)

Alice和Bob采用 `NOT_CERTIFIED`批准签名，Carol采用 `CERTIFIED_FORM_FILLING`认证签名：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230413102607.png)

Alice和Bob采用 `NOT_CERTIFIED`批准签名，Carol采用 `CERTIFIED_NO_CHANGES_ALLOWED`认证签名：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230413131955.png)

Alice采用 `CERTIFIED_FORM_FILLING`认证签名，Bob采用 `NOT_CERTIFIED`批准签名，Carol采用 `CERTIFIED_FORM_FILLING`认证签名：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230413133745.png)

代码如下：

{% highlight java %}
{% include_relative C2_09_SequentialSignatures.java %}
{% endhighlight %}

## 多次签署和填写字段

Alice将是该文档的作者。她将首先使用 `认证签名`进行签署，以允许表格填写。接下来，Bob将填写一个字段，并写下 `Read and approved by Bob`，之后他使用 `批准签名`签署该文档。第三步，Carol需要做同样的事情。最后，作为工作流程中的第四方，Dave `批准`该文档。以下是具体步骤：

- 创建一个带有文本域和签名域的pdf：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230413165722.png)

- Alice使用 `CERTIFIED_FORM_FILLING`等级进行认证签署：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230414163342.png)

- Bob添加文本，对文本进行了Alice允许的修改

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230414175453.png)

- Bob进行批准签名

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230414180437.png)

- Carol添加文本

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230414180259.png)

- Carole进行批准签名

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230414180551.png)

- Dave添加文本并进行批准签名

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20230414182603.png)

{: .warning}
> 不管是添加文本还是签名，都需要在`new StampingProperties().useAppendMode()`模式下进行，以免破坏Alice的签名

{% highlight java %}
{% include_relative C2_10_SignatureWorkflow.java %}
{% endhighlight %}

## 签署后锁定字段和文件

`com.itextpdf.forms.PdfSigFieldLock.LockAction`表示签名对应的签名字段时应该锁定的字段:

- `ALL`:文档中的所有字段
- `INCLUDE`:/Fields 数组中指定的所有字段
- `EXCLUDE`:/Fields 数组中指定的字段除外的所有字段

`com.itextpdf.forms.PdfSigFieldLock.LockPermissions`表示签名字段被签名时授予文档的不同级别的访问权限：

- `NO_CHANGES_ALLOWED`：不允许对文档进行任何更改；对文档的任何更改都会使签名无效，
- `FORM_FILLING`：允许的更改是填写表格、实例化页面模板和签名；其他更改使签名无效
- `FORM_FILLING_AND_ANNOTATION`：允许的更改与之前相同，以及注释创建、删除和修改；其他更改会使签名无效。

{% highlight java %}
{% include_relative C2_11_LockFields.java %}
{% endhighlight %}

