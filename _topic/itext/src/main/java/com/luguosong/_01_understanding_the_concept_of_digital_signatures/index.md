---
layout: note
title: 了解数字签名的概念
nav_order: 10
parent: iText 7 PDF文件的数字签名
create_time: 2023/11/13
---

# pdf中的数字签名

- 根对象（以PDF字典的形式表示）
  - 表单
    - 引用签名类型的字段
      - 引用实际签名

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202311140937241.png)

# 计算摘要

{% highlight java %}
{% include_relative DigestDefault.java %}
{% endhighlight %}

{: .warning}
> 仅仅使用摘要，无法保证文档的完整性。
> 
> 因为摘要是以明文的形式存储在pdf中的，可以随时被篡改。

# 创建密钥库

Java SDK(JAVA 8)包含一个名为keytool的工具。您可以使用此工具创建一个密钥库。该密钥库将包含您的私钥，以及包含有关您信息和您的公钥的数字证书。

- genkey： 表示生成密钥对的操作。
- alias demo： 指定要生成的密钥对的别名，这里设置为“demo”。
- keyalg RSA： 指定密钥的算法为 RSA。
- keysize 2048： 指定密钥的长度为 2048 比特。
- keystore ks： 指定要创建的密钥库的文件名为“ks”。

```shell
keytool -genkey -alias demo -keyalg RSA -keysize 2048 -keystore ks
输入密钥库口令:
再次输入新口令:
您的名字与姓氏是什么?
  [Unknown]:  lu
您的组织单位名称是什么?
  [Unknown]:  guosong
您的组织名称是什么?
  [Unknown]:  lgs
您所在的城市或区域名称是什么?
  [Unknown]:  suzhou
您所在的省/市/自治区名称是什么?
  [Unknown]:  jiangsu
该单位的双字母国家/地区代码是什么?
  [Unknown]:  china
CN=lu, OU=guosong, O=lgs, L=suzhou, ST=jiangsu, C=china是否正确?
  [否]:  y

输入 <demo> 的密钥口令
        (如果和密钥库口令相同, 按回车):

Warning:
JKS 密钥库使用专用格式。建议使用 "keytool -importkeystore -srckeystore ks -destkeystore ks -deststoretype pkcs12"  迁移到行业标准格式 PKCS12。
```

# 非对称加密和解密

{% highlight java %}
{% include_relative EncryptDecrypt.java %}
{% endhighlight %}

# 签名与验签

发送者可以用自己的私钥对消息进行签名，并将消息的摘要（digest）与实际消息一起发送。接收者可以使用发送者的公钥解密摘要，然后对收到的消息进行哈希运算，以验证消息的完整性和真实性。如果哈希值匹配，则消息未被篡改，否则则存在篡改的可能性。

{% highlight java %}
{% include_relative SignVerify.java %}
{% endhighlight %}

# 相关标准

## 名词缩写

- `PKI`：公钥基础设施（Public Key Infrastructure）。它是一组硬件、软件、人员、政策和程序，用于创建、管理、分发、使用、存储和撤销数字证书。
- `ASN.1`：抽象语法标记一（Abstract Syntax Notation One）。它是一种标准且灵活的表示法，描述了在电信和计算机网络中表示、编码、传输和解码数据的规则和结构。
- `BER`：基本编码规则（Basic Encoding Rules）。它是ASN.1标准制定的最初规则，用于将抽象信息编码成特定数据流。
- `DER`：可辨识编码规则（Distinguished Encoding Rules）。DER是BER的一个子集，提供了对ASN.1值进行编码的唯一方法。它受X.509对BER编码施加的约束所影响。
- `X.509`：公钥基础设施（PKI）和权限管理基础设施的标准。它规定了公钥证书和证书吊销列表等内容的标准格式。
- `IETF`：互联网工程任务组（Internet Engineering Task Force）。他们制定并推广互联网标准，与万维网联盟（W3C）、国际标准化组织（ISO）和国际电工委员会（IEC）密切合作。
- `RFC`：请求评论（Request for Comments）。RFC是由互联网工程任务组（IETF）发布的备忘录，描述了适用于互联网和互联网连接系统运作的方法、行为、研究或创新。
- `FIPS`：联邦信息处理标准（Federal Information Processing Standard）。它是美国联邦政府为计算机系统开发的公开宣布的标准化。

## 公钥密码学标准（PKCS）

- `PKCS#1`：`RSA密码学标准`，该标准以RFC 3447的形式发布。它定义了RSA公钥和私钥的数学属性和格式（以明文的ASN.1编码形式），以及执行RSA加密、解密、生成和验证签名的基本算法、编码/填充方案。在讨论子过滤器 /adbe.x509.rsa_sha1 时，我们会简要提到PKCS#1。
- `PKCS#7`：`密码消息语法标准`,该标准以RFC 2315的形式发布。它用于数字签名、摘要、认证或加密任何形式的数字数据。`密码消息语法（CMS）`在RFC 5652中进行了更新。其架构建立在基于证书的密钥管理之上，例如PKIX（PKI X.509）工作组定义的档案。
- `PKCS#11`：`密码令牌接口`,该标准也被称为`Cryptoki`，是`cryptographic token interface`（密码令牌接口）的合成词，发音为`crypto-key`。它是一个API，定义了对加密令牌（如硬件安全模块（HSM）、USB密钥和智能卡）的通用接口。我们会在第四章中遇到PKCS#11，在那里我们将使用Luna SA HSM和iKey 4000 USB密钥对PDF进行签名。
- `PKCS#12`：`个人信息交换语法标准`,该标准定义了一种用于存储带有相应公钥证书的私钥的文件格式，并用基于密码的对称密钥进行保护。在实践中，这些文件的扩展名通常为 .p12。它可作为Java密钥库的格式。注意，您可能也会遇到 .pfx 文件。PFX是PKCS#12的前身。
- `PKCS#13`：`椭圆曲线密码学标准`,该标准是基于有限域上椭圆曲线的代数结构的一种公钥密码学方法。在本文的背景下，我们不会使用PKCS#13，但在查看PDF支持的不同加密算法时，我们会在第2.1.4节中简要提到它。

## PDF ISO标准

`便携式文档格式（Portable Document Format，PDF）`曾是Adobe公司专有的规范。首个版本于1993年发布，由Adobe拥有版权，但很快以一种开放的方式发布，即Adobe允许开发人员使用规范来创建PDF编写器（如iText6）和/或PDF阅读器（如JPedal7）。

自2001年以来，国际标准化组织（ISO）已经发布了PDF规范的一些子集作为ISO标准：ISO-15930（也称为PDF/X）针对印前领域，ISO-19005（也称为PDF/A）是用于归档PDF的标准。

2007年，Adobe决定将PDF规范（当时是PDF 1.7）提交给企业内容管理协会（AIIM），以便作为ISO标准出版。该标准于2008年7月1日发布为`ISO-32000-1（ISO, 2008）`。

其他PDF标准已经或将来发布，例如针对工程领域的`ISO 24517（也称为PDF/E）`、可变数据打印和交易打印的`ISO 16612（也称为PDF/VT）`，以及通用可访问性的`ISO-14289（也称为PDF/UA）`。同时，我们期待ISO-32000-2，这是ISO-32000-1的后续标准,也被称为 `PDF 2.0`。

## CAdES, XAdES和PAdES

欧洲电信标准协会（ETSI）是电信行业中的独立、非营利性的标准化组织（设备制造商和网络运营商）。该组织发布技术标准（TS），其中对于本白皮书的背景而言，最重要的是关于高级电子签名的解释。

高级电子签名是一种电子签名，具有以下特点：
- 唯一地与签署者关联；
- 能够识别签署者；
- 使用签署者能够单独控制的手段创建；
- 与其相关数据相连，使得对数据的任何后续更改可被检测到。

ETSI发布了三套规范：
- `CMS高级电子签名（CAdES）`：CAdES是对CMS的一组扩展，使其适用于高级电子签名。它在ETSI TS 101 733中进行了描述。CAdES的一个重要优点是，即使底层的加密算法被破解，电子签名的文档也可以在很长一段时间内保持有效。
- `XML高级电子签名（XAdES）`：XAdES是XML数字签名标准XML-DSig11的扩展。它在ETSI TS 101 903中进行了描述。iText不支持XAdES的使用。我在此提及它只是因为它是PAdES Part 5 中使用的技术。
- `PDF高级电子签名（PAdES）`：PAdES是对PDF和ISO-32000-1的一组限制和扩展，使其适用于高级电子签名。它在TS 102 778（ETSI，2009）中进行了描述，并将在ISO-32000-2中实施。PAdES由六部分组成：
  - `Part 1`:第一部分是对PDF文档中签名支持的概述，并列出了其他文档中PDF配置文件的特性。
  - `Part 2`:PAdES基本部分基于ISO-32000-1。如果您想了解PDF中的数字签名更多信息，您应该在开始研究PDF参考文档之前阅读此规范。PAdES第2部分自iText 5.0.0版本起得到支持。
  - `Part 3`:PAdES增强部分描述了基于CAdES的配置文件：PAdES基本电子签名（BES）和显式策略电子签名（EPES）。PAdES第3部分自iText 5.3.0版本起得到支持。
  - `Part 4`:PAdES长期验证（LTV）是关于在用户签名证书到期后保护数据的机制。此机制需要文档安全存储（DSS）。PAdES第4部分自iText 5.1.3版本起得到支持。
  - `Part 5`:PAdES用于XML内容的部分描述了XAdES签名的配置文件。例如，在填充嵌入PDF文件中的XML内容的XFA表单后，用户可以签署表单的选定部分。iText目前尚不支持此功能。
  - `Part 6`:电子签名的视觉表示部分。这在iText中得到支持，但也取决于其他因素。例如：您的证书是否包含足够的信息？
