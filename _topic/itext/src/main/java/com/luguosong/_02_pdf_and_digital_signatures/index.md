---
layout: note
title: PDF和数字签名
nav_order: 20
parent: iText 7 PDF文件的数字签名
create_time: 2023/11/15
---

# 在PDF中数字签名

## 签名处理器和子过滤器

在为PDF创建数字签名时，您需要定义一个首选的`签名处理器`（即/Filter条目）。在iText中，我们始终使用`/Adobe.PPKLite`过滤器。

虽然可以调整iText以使用另一个过滤器，但几乎没有必要这样做：只要处理器支持指定的/SubFilter格式，交互式PDF处理器可以使用其喜欢的任何处理器。

`子过滤器`指的是用于创建签名的编码或格式。例如：它是否使用PKCS#1、PKCS#7或CAdES？部分信息（例如公共证书）是存储在签名外部，还是嵌入在签名中？

{: .note-title}
> 分离的签名
>
> ❌根据`维基百科`的定义，分离的签名是一种数字签名，与其签名的数据`分开保存`，而不是`捆绑在单个文件中`。
>
> ✅在PDF的上下文中，这个定义并不完全正确：签名是包含在PDF文件中的，但签名的属性是`签名的一部分`，而`不是存储在签名字典中`。

在iText 5.3.0之前的版本中，您会选择`setCrypto()方法`的以下参数来对PDF文档进行签名：

- `PdfSignatureAppearance.WINCER_SIGNED`——这会创建一个使用子过滤器`/adbe.pkcs7.sha1`的签名。
- `PdfSignatureAppearance.SELF_SIGNED`——这会创建一个使用子过滤器`/adbe.x509.rsa_sha1`的签名。

这些选项在5.3.0中被移除，原因非常具体。

`/adbe.pkcs7.sha1`子过滤器将在PDF
2.0中被弃用。ISO-32000-2建议：`为了支持向后兼容性，PDF阅读器应该处理/SubFilter键的这个值，但PDF编写器不应再使用这个值作为该键的值。`
iText是一个PDF编写器，在iText 5.3.0之后，我们不再允许创建这种类型的签名。请不要再使用这个子过滤器对任何文档进行签名。

至于`/adbe.x509.rsa_sha1`，在PDF 2.0中仍然可用，但所使用的`基础标准（PKCS#1）在PAdES12中被明确禁止使用`
。也就是说，签名的/Contents条目的值不能是DER编码的PKCS#1二进制数据对象。我们已经停止支持纯PKCS#1签名的创建，以便iText创建的签名符合`PAdES规范`。

## 数字签名覆盖的字节范围

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311151312222.png)

签名字典包含一个`/ByteRange`条目。如果您阅读ISO-32000-1，您会发现这个字节范围可能包含间隙：PDF中未被签名覆盖的区域。理论上，这些间隙可能会使PDF存在漏洞。

PAdES对PDF数字签名规范引入了额外的限制，在ISO-32000-2中已针对ETSI子过滤器考虑到这一点：如果`/SubFilter`
是`/ETSI.CAdES.detached`或`/ETSI.RFC3161`，则/ByteRange应涵盖整个文件，`包括签名字典但不包括/Contents值`。此外，Adobe
Acrobat/Reader的最新版本已开始拒绝在字节范围中存在更大或更多空洞的签名。这就是为什么iText始终使用PDF文档的完整字节范围，无论选择了哪种子过滤器。

{: .warning}
> PDF中不存在`在文档页面上签字`的概念。

## 签名构成

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311151334876.png)

- 数字证书
    - 公钥
    - 身份信息的证书
- 数字证书对应私钥
- 使用私钥签署的原始文档的摘要
- 此外签名可以包含时间戳

## PDF支持的算法

{: .warning-title}
> `关于哈希的警告`：在某些国家，正在逐步淘汰使用SHA-1。建议使用更强大的哈希算法。
> 
> `关于加密的警告`：美国国家标准与技术研究院（NIST）建议自2010年起，1024位RSA密钥不再可行，建议迁移到2048位RSA密钥。其他国家的其他机构也提出了类似建议，例如在德国。

### 💀adbe.pkcs7.sha1子过滤器

支持的消息摘要：SHA1（其他摘要可用于摘要签名数据字段，但SHA1用于摘要正在签名的PDF文档数据）。

RSA算法：最多1024位（自PDF 1.3起），2048位（自PDF 1.5起），4096位（自PDF 1.7起）。

DSA算法：最多4096位（自PDF 1.6起）。

{: .warning}
> 请注意，此子过滤器在PDF 2.0（ISO-32000-2）中将不再支持。自iText版本5.3.0起不再支持。

### 💀adbe.x509.rsa_sha1子过滤器

支持的消息摘要：SHA1（自PDF 1.3起），SHA256（自PDF 1.6起），以及SHA384、SHA512、RIPEMD160（自1.7起）。

RSA算法：最多1024位（自PDF 1.3起），2048位（自PDF 1.5起），4096位（自PDF 1.7起）。

DSA算法：不支持。

{: .warning}
> 请注意，尽管名称中引用了SHA1，但支持其他摘要算法。由于PAdES标准禁止纯PKCS＃1，自iText版本5.3.0起不再支持此子过滤器。

### adbe.pkcs7.detached，ETSI.CAdES.detached和ETSI.RFC3161

支持的消息摘要：SHA1（自PDF 1.3起），SHA256（自PDF 1.6起），以及SHA384、SHA512、RIPEMD160（自1.7起）。

RSA算法：最多1024位（自PDF 1.3起），2048位（自PDF 1.5起），4096位（自PDF 1.7起）。

DSA算法：最多4096位（自PDF 1.6起）。

ECDSA：椭圆曲线数字签名算法将在PDF 2.0中支持。

自iText版本5.3.0起，默认完全支持分离式签名。

- `adbe.pkcs7.detached`： 这是用于创建`CMS（Cryptographic Message Syntax）`或`PKCS#7`格式的数字签名的一种方式。它创建的数字签名是“分离式”的，即签名是单独的文件，与签名的数据不直接捆绑在一起。这种签名方式在PDF文档中经常使用。
- `ETSI.CAdES.detached`： 这是遵循欧洲电信标准协会（ETSI）定义的`高级电子签名（CAdES）`规范的一种子过滤器。CAdES是针对数字签名的一组标准，用于增强数字签名的安全性和长期有效性。`ETSI.CAdES.detached`创建的签名也是`分离式`的，它将签名和签名的数据分开。
- `ETSI.RFC3161`： 这是一种数字签名时间戳的方式，遵循RFC 3161标准，允许向已存在的签名添加时间戳。这样做可以证明签名在特定时间之前是有效的，并且签名的时间戳可被认证。这种方式不会直接创建签名，而是添加一个与签名相关的时间戳信息。

# 数字签名示例

## 添加可见签名到文档


iText在创建PDF字节的哈希值时使用与签名创建(IExternalSignature对象创建)定义相同的摘要算法。

签名算法（RSA，DSA或ECDSA）和密钥大小将从私钥对象中获取。

{% highlight java %}
{% include_relative SignHelloWorld.java %}
{% endhighlight %}

## 签署大文件

将 PDF 内容保存在磁盘的临时文件中。这对于大型 PDF 文件是更优选的方式，因为将整个 PDF 内容保存在内存中可能会导致内存不足的异常。保存在磁盘上的临时文件可以更好地处理大型文件。

```java
public class SignHelloWorld {
  public void sign(){
      //...
      //PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());
      //将临时文件的路径传递给PdfSigner构造函数
      PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), temp, new StampingProperties());
      //...
  }
}
```

# 创建和签署签名字段

## 使用Adobe Acrobat添加签名字段

1. 菜单
2. 添加或编辑字段
3. 添加新字段
4. 选择`数字签名`
5. 绘制一个矩形
6. 设置字段名称

## 对字段进行签名

{% highlight java %}
{% include_relative SignEmptyField.java %}
{% endhighlight %}

