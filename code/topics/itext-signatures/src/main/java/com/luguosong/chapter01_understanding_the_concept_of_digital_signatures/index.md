# 理解数字签名的概念

## 一个简单的PDF示例

下图展示了一个简单的PDF文档，里面只包含`Hello World`这几个词。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407051639656.png){ loading=lazy }
  <figcaption>图1.1：一个简单的Hello World文件</figcaption>
</figure>

如果我们查看图中展示的PDF文件的内部结构，我们会看到以下的PDF语法：

```text title="代码1.1：PDF文件的内部结构"
%PDF-1.4
%âãÏÓ
2 0 obj
<</Length 73 >>stream
BT
36 806 Td
0 -18 Td
/F1 12 Tf
(Hello World)Tj
0 0 Td
ET
Q
endstream
endobj
4 0 obj
<</Parent 3 0 R/Contents 2 0 R/Type/Page/Resources<</ProcSet [/PDF /Text /ImageB 
/ImageC /ImageI]/Font<</F1 1 0 R>>>>/MediaBox[0 0 595 842]>>
endobj
1 0 obj
<</BaseFont/Helvetica/Type/Font/Encoding/WinAnsiEncoding/Subtype/Type1>>
endobj
3 0 obj
<</ITXT(5.3.0)/Type/Pages/Count 1/Kids[4 0 R]>>
endobj
5 0 obj
<</Type/Catalog/Pages 3 0 R>>
endobj
6 0 obj
<</Producer(iText® 5.3.0 ©2000-2012 1T3XT 
BVBA)/ModDate(D:20120613102725+02'00')/CreationDate(D:20120613102725+02'00')>>
endobj
xref
0 7
0000000000 65535 f 
0000000311 00000 n 
0000000015 00000 n 
0000000399 00000 n 
0000000154 00000 n 
0000000462 00000 n 
0000000507 00000 n 
trailer
<</Root 5 0 R/ID 
[<0f6bb651c0480213fbbd13449b40fe8f><e77fb3c3c64c30ea2a908cd181c5f500>]/Info 6 0 
R/Size 7>>
startxref
643
%%EOF
```

每个PDF文件都以`%PDF-`开头，后跟`版本号`，并以`%%EOF`结尾。在此之间，可以找到不同的PDF对象，这些对象以某种方式定义文档的内容。解释代码示例中显示的所有对象的含义超出了本文的范围。

### 如何伪造PDF文档的内容

假设我们知道如何对文件进行一些小的更改。例如：让我们改变文档的内容、尺寸和元数据。请参见代码示例中标记为高亮的部分。

```text title="代码1.2" hl_lines="9 17 29"
%PDF-1.4
%âãÏÓ
2 0 obj
<</Length 73 >>stream
BT
36 806 Td
0 -18 Td
/F1 12 Tf
(Hello Bruno)Tj
0 0 Td
ET
Q
endstream
endobj
4 0 obj
<</Parent 3 0 R/Contents 2 0 R/Type/Page/Resources<</ProcSet [/PDF /Text /ImageB 
/ImageC /ImageI]/Font<</F1 1 0 R>>>>/MediaBox[0 0 120 806]>>
endobj
1 0 obj
<</BaseFont/Helvetica/Type/Font/Encoding/WinAnsiEncoding/Subtype/Type1>>
endobj
3 0 obj
<</ITXT(5.3.0)/Type/Pages/Count 1/Kids[4 0 R]>>
endobj
5 0 obj
<</Type/Catalog/Pages 3 0 R>>
endobj
6 0 obj
<</Producer(iText® 1.0.0 ©2000-2012 1T3XT 
BVBA)/ModDate(D:20120613102725+02'00')/CreationDate(D:20120613102725+02'00')>>
endobj
xref
0 7
0000000000 65535 f 
0000000311 00000 n 
0000000015 00000 n 
0000000399 00000 n 
0000000154 00000 n 
0000000462 00000 n 
0000000507 00000 n 
trailer
<</Root 5 0 R/ID 
[<0f6bb651c0480213fbbd13449b40fe8f><e77fb3c3c64c30ea2a908cd181c5f500>]/Info 6 0 
R/Size 7>>
startxref
643
%%EOF
```

我手动将单词`World`替换为`Bruno`，将页面尺寸从`595 x 842`改为`120 x 806`，并更改了生产者行中iText的`版本号`。下图展示了结果。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407051651262.png){ loading=lazy }
  <figcaption>图1.2：一个修改过的Hello World文件</figcaption>
</figure>

!!! warning

    千万不要自行进行这样的操作！在99.9%的情况下，手动更改PDF文件会损坏文件。我之所以这样做，只是为了证明，尽管PDF不是一种文字处理格式，尽管PDF不适合编辑文档，尽管不建议这样做，你仍然可以改变文档的内容。这正是引入数字签名所要避免的问题。

### 一个数字签名的PDF文档

图1.3展示了一个经过数字签名的Hello World文档。蓝色的横幅告诉我们这个文档是`已签名且所有签名都是有效的`
。签名面板告诉我们该文件是由`Bruno Specimen`签名的，并提供更多的签名细节。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407051705762.png){ loading=lazy }
  <figcaption>图1.3：一个已签名的Hello World文件</figcaption>
</figure>

绿色的勾号表示自签名应用以来，文档`没有被修改`，并且签名者的`身份是有效的`。

### 检查数字签名的语法

```text title="代码1.3：一个已签名的PDF文件的片段" hl_lines="3 4 5 6 7 8 9 10 11 27 28 29 30"
%PDF-1.4
%âãÏÓ
3 0 obj
<</F 132/Type/Annot/Subtype/Widget/Rect[0 0 0 0]/FT/Sig
/DR<<>>/T(signature)/V 1 0 R/P 4 0 R/AP<</N 2 0 R>>>>
endobj
1 0 obj
<</Contents <0481801e6d931d561563fb254e27c846e08325570847ed63d6f9e35 ... b2c8788a5>
/Type/Sig/SubFilter/adbe.pkcs7.detached/Location(Ghent)/M(D:20120928104114+02'00')
/ByteRange [0 160 16546 1745 ]/Filter/Adobe.PPKLite/Reason(Test)/ContactInfo()>>
endobj
...
9 0 obj
<</Length 63>>stream
q
BT
36 806 Td
0 -18 Td
/F1 12 Tf
(Hello World!)Tj
0 0 Td
ET
Q
endstream
endobj
...
11 0 obj
<</Type/Catalog/AcroForm<</Fields[3 0 R]/DR<</Font<</Helv 5 0 R
/ZaDb 6 0 R>>>>/DA(/Helv 0 Tf 0 g )/SigFlags 3>>/Pages 10 0 R>>
endobj
xref
0 12
0000000000 65535 f 
...
0000017736 00000 n 
trailer
<</Root 11 0 R/ID 
[<08ed1afb8ac41e841738c8b24d592465><bd91a30f9c94b8facf5673e7d7c998dc>]/Info 7 0 
R/Size 12>>
startxref
17879
%%EOF
```

请注意，我略微修改了文件，删除了在解释数字签名概念时不相关的字节。

首先让我们检查PDF的`根对象`（也称为`目录对象`）。在代码示例1.3中27到30行（编号为11）。`目录`总是表示为`PDF字典`。

在PDF文件中，`字典`可以很容易地识别出来。它们以`<<`开头，并以`>>`结尾。

在两者之间，你会找到一系列键值对。键总是一个`名称对象`。请注意，名称总是以`/`
开头。例如：如果PDF包含一个表单，你会在目录字典中找到一个名为`/AcroForm`的键。该值将是一个（指向）`字典`
。这个字典将包含一个`/SigFlags`值，如果表单包含数字签名的话。

表单中有一个字段。从`/Fields`数组引用到它：参见`对象3`(3到6行)。名为`signature`（/T(signature)）的字段是类型为`签名的字段`
（/FT/Sig）。我们在图1.3中没有看到签名的可视表示。这是因为Bruno Specimen决定使用`不可见的签名`。定义`小部件注释`
（/Type/Annot/SubType/Widget）的`矩形`（/Rect）具有`零宽度和零高度`（[0 0 0 0]）。

实际的签名可以在`签名字典`中找到（7到11行）。这个字典从`签名字段的值`（/V）引用(第5行)。`签名`
是/Contents条目的值。此签名覆盖PDF文件的所有字节，除了签名字节本身。

请看/ByteRange条目(第10行)：签名覆盖了从0到160字节和从16546到18291字节。签名本身占据了161到16545字节。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/java_serve/topics/itext-signatures/chapter01_understanding_the_concept_of_digital_signatures/%E6%95%B0%E5%AD%97%E7%AD%BE%E5%90%8D%E8%AF%AD%E6%B3%95%E7%BB%93%E6%9E%84.svg){ loading=lazy }
  <figcaption>大致结构</figcaption>
</figure>

!!! note

    在使用单词`signature`时，人们可能指的是不同的内容。

    在技术上纯粹的意义上，PDF文件中的签名指的是存储在/Contents条目中的字节。

    然而，当我们谈到页面上的签名表示时，甚至是文件中完整的签名基础设施（注释和签名字典）时，我们也会使用这个词。在本文中，我会尽量准确，但在某些情况下，确切的含义应根据上下文清楚理解。

### 使签名无效

现在，如果我更改签名覆盖的字节范围内的其中一个字节，Adobe Reader 将显示一个红色叉号，而不是绿色的勾号。图1.4
显示了如果我手动将 `World` 替换为 `Bruno` 会发生什么。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407051814004.png){ loading=lazy }
  <figcaption>图1.4：失效的签名</figcaption>
</figure>

在这种情况下，`签名者的身份`是有效的，但是会显示`文档自签名后已被更改或损坏`。 Adobe Reader
如何知道文档已被更改？要理解这一点，我们需要了解`哈希`的概念，以及需要了解加密的工作原理。

## 生成消息摘要

我记不住所有用来登录不同网站（如Twitter、Facebook、LinkedIn等）的密码。我经常使用`忘记密码`
功能。通常情况下，我会收到一个链接，允许我重置密码，但偶尔也会收到一封包含`明文原始密码`的邮件。

一个服务能够提供我的密码意味着我的实际密码可能存储在某个数据库或服务器上。这是一个危险的情况：这意味着任何黑客入侵系统后都可以获取所有用户的密码。

### 如何校验密码

一个简单的检查密码的方法是存储密码的摘要而不是实际的密码。让我们创建一个简单的类来演示这是如何实现的：

``` java title="代码1.4"
--8<-- "code/topics/itext-signatures/src/main/java/com/luguosong/chapter01_understanding_the_concept_of_digital_signatures/DigestDefault.java"
```

`摘要算法`是一种用于生成数据固定长度的唯一表示的算法。它将任意大小的数据（例如密码）转换为固定长度的哈希值或摘要。常见的摘要算法包括
SHA-256、MD5 等。

> 摘要算法的主要特性是它是单向的，即很难从摘要值反推出原始数据，并且不同的输入几乎总是生成不同的摘要值。因此，它们广泛用于密码存储和数据完整性验证。

### 什么是摘要算法

当我们创建摘要时，我们使用密码学`哈希函数`将任意数据块转换为`固定大小的位字符串`。这个数据块通常称为`消息`，而`哈希值`
被称为`消息摘要`。

在前面的例子中，`消息`是一个`密码`。只要消息没有被修改，这个哈希函数的结果总是相同的。任何对数据的意外或故意更改都会导致不同的哈希值。它也是一个单向算法。

当你创建密码的哈希值时，不应该可以根据消息摘要计算出密码。如果数据库被攻破，黑客得到的是一组消息摘要，但他无法将其转换为密码列表。

!!! warning

    如果黑客获得了包含密码哈希值的数据库，他仍然可以使用暴力破解来恢复密码。此外，一些较旧的哈希算法存在可以被利用的缺陷。如今，为了确保安全，至少需要对哈希进行加盐并应用多次迭代，但这超出了本文的讨论范围。

不同的密码学哈希算法的实现:

- `MD5` 是麻省理工学院的 Ron Rivest 教授设计的一系列消息摘要算法之一，于 1991 年设计。MD5 算法允许您为一条消息创建一个 128
  位（16 字节）的摘要。换句话说，对于无限数量的消息，存在着 2^128
  种可能的哈希值，这意味着两个不同的文档可能会得到相同的摘要。这被称为“哈希碰撞”。哈希函数的质量取决于创建碰撞的“容易程度”。尽管
  MD5 仍然被广泛使用，但现在不再被视为安全的算法。
- `SHA-1` 是由美国国家安全局（NSA）设计的。这种 160 位（20 字节）的哈希算法被认为比 MD5 更安全，但在 2005 年发现了一些安全漏洞。
- `SHA-2` 修复了这些漏洞。SHA-2 是一组哈希函数，包括 SHA-224、SHA-256、SHA-384 和 SHA-512。SHA-2 中消息摘要的长度可以是 224
  位（28 字节）、256 位（32 字节）、384 位（48 字节）或 512 位（64 字节）。
- `RIPEMD` 的全称是 RACE Integrity Primitives Evaluation Message Digest。它是在鲁汶大学（Katholieke Universiteit
  Leuven）开发的。

### BouncyCastle库

并非所有算法都受到java.security.MessageDigest类的实现支持。我们可以通过使用另一个安全提供者来解决这个问题。

Bouncy Castle 是一个用于密码学的 API 集合。它在 Java 和 C# 中都可用。我们可以通过在代码示例1.4中定义一个名为 "BC" 的
BouncyCastleProvider 实例作为安全提供者来扩展该类。这在代码示例1.5中实现。

``` java title="代码1.5"
--8<-- "code/topics/itext-signatures/src/main/java/com/luguosong/chapter01_understanding_the_concept_of_digital_signatures/DigestBC.java"
```

请注意，我们创建了一个`BouncyCastleProvider实例`，并将其添加到`Security类`
（位于java.security包中）。这个类集中了所有安全属性。其主要用途之一是管理`安全提供者`。

我们已经提到，消息可以是任何字节块，包括 PDF
文件特定字节范围内的字节。为了检测PDF文件是否被篡改，我们可以对这些字节创建一个`消息摘要`
，并将其存储在PDF文件内部。然后，当有人更改PDF时，从字节范围中更改的消息摘要将不再与存储在PDF中的消息摘要相对应。这解决了数据`完整性`、`真实性`
和`不可否认性`的问题吗？

还没有。某人可以轻易地修改PDF文件的字节，猜测使用的摘要算法，并将新的消息摘要存储在PDF中。为了避免这种情况，我们需要引入使用`非对称密钥算法`
进行加密的概念。

## 使用公钥加密加密消息

假设Bob想给Alice发送一条私密消息，但他不信任用于传输消息的通道。如果消息落入错误的手中，Bob希望避免给Alice的消息被其他人阅读。

Bob可以使用算法加密他的消息，然后Alice需要使用算法解密它。如果可以使用相同的密钥进行加密和解密（或者可以从用于加密的密钥派生解密密钥），那么Bob和Alice使用的是`对称密钥算法`
。这种算法的问题在于，Bob和Alice需要首先以安全的方式交换该算法。任何拥有密钥访问权限的人都可以解密你的消息。

为了避免这种情况，Bob 和 Alice 可以选择使用`非对称密钥算法`。在这种情况下，使用两个不同的密钥：一个用于`加密`
，另一个用于`解密`
消息。一个密钥不能从另一个密钥派生出来。现在，如果Bob想给Alice发送一条私密消息，Alice可以将她的公钥发送给Bob。任何人都可以使用这个公钥加密发给Alice的消息。当Bob发送这样加密的消息给Alice时，Alice将使用她的私钥来解密它。假设Chuck截获了消息，只要他没有访问Alice的私钥，他就无法读取消息。

最常用的公钥加密系统是`RSA`，以其发明者Ron Rivest、Adi Shamir和Leonard
Adleman的名字命名。创建密钥对时，可以指定密钥长度（以位为单位）。位数越高，加密强度越强。但也有一个缺点：每次RSA密钥长度加倍，解密速度会变慢6到7倍。

让我们创建这样一个公钥和私钥对。

### 创建一个密钥库

Java SDK 包含一个名为 keytool 的工具。您可以使用此工具创建一个密钥库。这个密钥库将包含您的`私钥`，以及一个包含关于`您`
以及`您的公钥`信息的`数字证书`。请参考代码示例1.7，了解如何使用 keytool。

```shell title="使用 keytool 创建密钥库"
C:\Users\10545>keytool -genkey -alias demo -keyalg RSA -keysize 2048 -keystore ks
输入密钥库口令:

再次输入新口令:

您的名字与姓氏是什么?
  [Unknown]:  lu
您的组织单位名称是什么?
  [Unknown]:  IT
您的组织名称是什么?
  [Unknown]:  iText Software
您所在的城市或区域名称是什么?
  [Unknown]:  Ghent
您所在的省/市/自治区名称是什么?
  [Unknown]:  OVL
该单位的双字母国家/地区代码是什么?
  [Unknown]:  BE
CN=lu, OU=IT, O=iText Software, L=Ghent, ST=OVL, C=BE是否正确?
  [否]:  是

正在为以下对象生成 2,048 位RSA密钥对和自签名证书 (SHA256withRSA) (有效期为 90 天):
         CN=lu, OU=IT, O=iText Software, L=Ghent, ST=OVL, C=BE
```

一个密钥库可以包含多个私钥，我们可以为每个密钥定义一个与密钥库密码不同的密码。为了简单起见，我们只使用了一个密码：“password”。我们可以在Java程序中使用这个密钥库来`加密`
和`解密`消息。

### 加密和解密消息

``` java title="代码1.8"
--8<-- "code/topics/itext-signatures/src/main/java/com/luguosong/chapter01_understanding_the_concept_of_digital_signatures/EncryptDecrypt.java"
```

### 检查我们的自签名证书

如你所见，我们创建了一个`KeyStore对象`。通过这个密钥库，我们可以获得包含公钥的公共`证书`。

公用证书还包含关于`密钥所有者`、`其有效期`等信息。证书中的信息由`颁发者`
签名。在这种情况下，我们自己创建了证书，所以我们既是`所有者`又是`颁发者`，因此我们称这个证书为`自签名证书`
。我们将在第3章中了解更多关于其他证书的内容。

代码示例1.9显示了 Certificate 对象的 toString() 方法的结果：

```text title="代码1.9"
[
[
 Version: V3
 Subject: CN=Bruno Specimen, OU=IT, O=iText Software, L=Ghent, ST=OVL, C=BE
 Signature Algorithm: SHA1withRSA, OID = 1.2.840.113549.1.1.5
 Key: Sun RSA public key, 2048 bits
 modulus: 
27706646249437583578501322921252037659324960984454438650274096621513733947318221232
90092536075175589409888251417041849614639606544370595805501222639942552792696182924
19557917502293557528812483868420880765808333319067679184013346901221838396913865166
99015383461952441725262486245434952426855074038516834028858534816117097190264270919
71970499616689684012198665415564791592761123642686002605100319784405598279465396131
52730660815729426764990600604032553721917074418187300648866487699179740248069790221
86704383972995455717886346330217224211116969013795163606127880980836981725138593346
185822803712134120722258642329810193
 public exponent: 65537
 Validity: [From: Sat Aug 04 15:40:30 CEST 2012,
 To: Tue Nov 17 14:40:30 CET 2015]
 Issuer: CN=Bruno Specimen, OU=IT, O=iText Software, L=Ghent, ST=OVL, C=BE
 SerialNumber: [ 501d264e]
]
 Algorithm: [SHA1withRSA]
 Signature:
0000: 12 ED EA 66 FE 6C 2C FC 0F F4 59 19 44 40 FE BF ...f.l,...Y.D@..
0010: CF 9E 66 D3 DC 62 85 F1 D5 62 76 07 F6 F2 67 04 ..f..b...bv...g.
0020: E8 F6 61 42 02 F9 36 A9 8B 12 6F 8B 4B B6 14 9B ..aB..6...o.K...
0030: 78 2F CA F0 53 76 41 F4 47 B7 5A 2B F7 A1 A9 73 x/..SvA.G.Z+...s
0040: 8E 44 55 31 14 D3 AB 3F 59 6C 53 E7 04 C4 2E 36 .DU1...?YlS....6
0050: DE 2C 1E F5 F9 E3 19 EF 7D 92 67 66 56 73 22 18 .,........gfVs".
0060: EC AF CB 86 22 B8 F0 D0 94 EC 37 97 D1 23 DA 43 ....".....7..#.C
0070: 98 8E 37 34 7E AD 76 78 99 63 21 0D 06 C3 1D 47 ..74..vx.c!....G
0080: 5D 21 0A A6 CD 57 70 C1 A4 23 5E 85 1E B9 80 DC ]!...Wp..#^.....
0090: A1 BF 61 02 12 D1 3D 5F D8 2E C5 5C 16 A2 6D 8D ..a...=_...\..m.
00A0: E1 0B 3C 1F 22 CF 11 18 AA 2D CF 75 C1 F6 C2 E8 ..<."....-.u....
00B0: 40 C2 59 C1 19 8B 86 61 79 12 4F F2 3E EC 61 1B @.Y....ay.O.>.a.
00C0: D3 CF FD 8C 3B F4 6D 1D F2 25 C3 7F 69 B1 B7 D2 ....;.m..%..i...
00D0: 49 96 61 B2 50 2B 70 74 AA 8E DC 18 6A 22 FC 00 I.a.P+pt....j"..
00E0: 96 67 A4 0B 70 62 63 A8 49 D6 E1 36 92 60 FF 0C .g..pbc.I..6.`..
00F0: 0E 72 55 0B A2 EC 3F CE 2E B4 BE E8 42 2C F0 73 .rU...?.....B,.s
]
```

在代码示例1.8中，我们从 Certificate 对象中获取公钥，并从 KeyStore 对象中获取私钥。公钥和私钥对象可以用作 encrypt() 和
decrypt() 方法的参数。这些方法几乎是相同的。唯一的区别是 Cipher 模式：我们使用的是 `ENCRYPT_MODE` 或 `DECRYPT_MODE`。

### 使用公钥算法进行认证和不可否认性

使用`公钥加密私钥解密`，是使用加密机制来确保第三方无法读取你的消息。

而当使用`私钥加密公钥解密`
，是为了明确你是消息的作者。如果我能使用你的公钥解密消息，我可以百分之百确定它是使用你的私钥加密的。由于只有你能访问你的私钥，我可以百分之百确定你是消息的作者。这就是为什么使用非对称密钥算法的加密概念可以用于`数字签名`。

图1.5显示了这两种不同的概念。对于`加密`，使用公钥（绿色的钥匙）加密，使用私钥（红色的钥匙）解密。对于创建`签名`，则反过来。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202407081517802.png){ loading=lazy }
  <figcaption>图1.5：非对称密钥算法，绿色代表公钥，红色代表私钥</figcaption>
</figure>

这解决了我们的数据完整性、真实性和不可否认性的问题吗？

是的，因为一旦有人篡改了我发给你的加密消息，你将无法使用我的公钥解密它。另一方面，如果你成功解密了它，你就确定我是作者。我不能一方面声称我没有签署消息，另一方面又声称我的私钥没有被泄露。

!!! note

    然而，实际操作中不是这样进行的：加密和解密大消息需要大量资源。如果我的目标不是保护消息不被他人阅读，我们可以通过仅加密`消息的摘要`，并将这个`签名的摘要`附加到实际消息上来节省我们机器上的宝贵时间和CPU。当你收到我的消息时，你可以`解密摘要`并对`消息进行哈希`以进行比较。如果匹配，则消息是真实的。如果不匹配，则消息被篡改。

不过，我们还没有解决所有可能的问题。还有许多问题需要回答。例如：

- 你如何确保你使用的公钥是我的，而不是某个冒充我的人的公钥？
- 如果我的私钥被泄露，会发生什么？

我们将在后续章节中回答这些问题。首先，我们需要熟悉一些缩写和标准。

## 密码学缩写词和标准概述

理解术语是我首次接触密码学和数字签名时遇到的最重要的门槛之一。以下是一些缩写词和标准的列表，你将在接下来的几章中遇到。在使用PDF库iText添加数字签名到PDF文档时，你也需要理解它们的含义。

### 缩写

- `PKI` 表示公钥基础设施。它是一组硬件、软件、人员、政策和程序，用于创建、管理、分发、使用、存储和撤销数字证书。
- `ASN.1` 表示抽象语法表示一。它是一种描述规则和结构的标准和灵活表示法，用于在电信和计算机网络中表示、编码、传输和解码数据。
- `BER` 表示基本编码规则。它们是ASN.1标准中为将抽象信息编码为特定数据流而制定的原始规则。
- `DER` 表示专有编码规则。DER是BER的子集，提供了一种精确编码ASN.1值的方法。它是基于X.509对BER编码的约束而制定的。
- `X.509` 是公钥基础设施（PKI）和特权管理基础设施的标准。它指定了公钥证书和证书吊销列表等内容的标准格式。
- `IETF`表示互联网工程任务组。他们开发和推广互联网标准，并与万维网联盟（W3C）、国际标准化组织（ISO）和国际电工委员会（IEC）密切合作。
- `RFC` 表示请求评论。RFC是由互联网工程任务组（IETF）发布的备忘录，描述适用于互联网和互联网连接系统运行的方法、行为、研究或创新。
- `FIPS` 表示联邦信息处理标准。这是美国联邦政府开发的公开公告的标准化，用于计算机系统中的使用。

### 公钥密码学标准 (PKCS)

除了加密算法外，RSA还是一家美国计算机和网络安全公司的名称。就像算法一样，它的名字也是根据其联合创始人 Rivest、Shamir 和 Adleman 的姓名首字母命名的。RSA 公司制定并发布了一组从#1到#15的公钥密码学标准。其中一些标准已不再使用。在本文的背景下，我们主要关注 `PKCS#7`，即加密消息语法 (CMS)，但我们也会看一些其他标准。

- `PKCS#1: RSA密码学标准`: 这一标准以RFC 3447发布。它定义了RSA公钥和私钥的数学属性和格式（以明文编码的ASN.1格式），以及执行RSA加密、解密、生成和验证签名的基本算法和编码/填充方案。我们在讨论子过滤器/adbe.x509.rsa_sha1时会简要遇到PKCS#1。
- `PKCS#7: 加密消息语法标准` :这一标准以RFC 2315发布。它用于对任何形式的数字数据进行数字签名、摘要、认证或加密。加密消息语法（CMS）在RFC 5652中进行了更新。其架构围绕基于证书的密钥管理构建，例如PKIX（PKI X.509）工作组定义的配置文件。
- `PKCS#11: 加密令牌接口` :这一标准也被称为“Cryptoki”，是“cryptographic token interface”（加密令牌接口）的合成词，发音为“crypto-key”。它是一个API，定义了与加密令牌（如硬件安全模块（HSM）、USB密钥和智能卡）的通用接口。我们将在第4章中遇到PKCS#11，我们将使用Luna SA HSM和iKey 4000 USB密钥对PDF进行签名。
- `PKCS#12: 个人信息交换语法标准` :这一标准定义了一种文件格式，用于存储带有相应公钥证书的私钥，通过基于密码的对称密钥进行保护。在实践中，这些文件通常具有.p12扩展名，可用作Java密钥存储的格式。注意，您也可能会遇到.pfx文件。PFX是PKCS#12的前身。
- `PKCS#13: 椭圆曲线密码学标准` :这一标准基于有限域上椭圆曲线的代数结构，是一种公钥密码学的方法。在本文的背景下，我们不会使用PKCS#13，但在第2.1.4节中，当我们查看PDF支持的不同加密算法时，我们会简要提到它。

### PDF ISO标准

`便携式文档格式（Portable Document Format，PDF）`最初是Adobe专有的规范。第一版发布于1993年，由Adobe拥有版权，但很快以开放的方式发布，允许开发人员使用该规范创建PDF编写器（如iText）和/或PDF查看器（如JPedal）。

自2001年以来，国际标准化组织（ISO）已经发布了PDF规范的几个子集作为ISO标准：`ISO-15930（也称为PDF/X）`用于印前行业，以及`ISO-19005（也称为PDF/A）`用于PDF的归档标准。

2007年，Adobe决定将当时的PDF规范（`PDF 1.7版本`）提交给企业内容管理协会（AIIM），以便将其作为ISO标准发布。该标准于2008年7月1日发布为`ISO-32000-1`。

随后或将来，其他PDF标准也会出台，如`ISO 24517（也称为PDF/E）`用于工程领域，`ISO 16612（也称为PDF/VT）`用于变量数据打印和交易打印，以及`ISO 14289（也称为PDF/UA）`用于通用无障碍访问。

同时，我们期待`ISO-32000-2`，这是`ISO-32000-1`的继任者。预计将在2013年推出，届时将引入PDF 2.0。本文已经考虑了PDF 2.0中将引入的一些变化。

### CAdES, XAdES和PAdES

`欧洲电信标准化协会（ETSI）`是电信行业（设备制造商和网络运营商）中的独立非营利性标准化组织。该组织发布技术标准（TS），其中解释`高级电子签名`的标准对于本白皮书的背景非常重要。

`高级电子签名`是指以下特点的电子签名：

- 与签署者唯一相关联，
- 能够识别签署者，
- 使用签署者能够单独控制的手段创建，
- 与相关数据关联，从而可以检测到数据的任何后续更改

ETSI发布了三套规范:

!!! note "CMS高级电子签名（CAdES）"

    CAdES是对CMS的一组扩展，使其适用于`高级电子签名`。它在ETSI TS 101 733中进行了描述。CAdES的一个重要优点是，即使底层的加密算法被破解，电子签名文档也可以在长时间内保持有效。

!!! note "XML高级电子签名（XAdES）"

    XAdES是对XML-DSig的扩展，XML-DSig是定义数字签名XML语法的标准。XAdES在ETSI TS 101 903中进行了描述。iText不支持使用XAdES，我在这里提到它只是因为它是PAdES第5部分中使用的技术。

!!! note "PDF高级电子签名（PAdES）"

    PAdES是对PDF和`ISO-32000-1`的一组限制和扩展，使其适用于`高级电子签名`。它在TS 102 778（ETSI，2009）中进行了描述，并将在`ISO-32000-2`中实施。PAdES由六部分组成：

    - 第1部分 - 第一部分概述了PDF文档中签名支持的情况，并列出了其他文档中PDF配置文件的特性。
    - 第2部分 - PAdES基础部分基于`ISO-32000-1`。如果您想了解PDF中的数字签名更多信息，在深入研究PDF参考之前，应阅读此规范。自iText 5.0.0版本起支持PAdES第2部分。
    - 第3部分 - PAdES增强描述了基于CAdES的配置文件：PAdES基本电子签名（BES）和显式策略电子签名（EPES）。自iText 5.3.0版本起支持PAdES第3部分。
    - 第4部分 - PAdES长期验证（LTV）涉及在用户签名证书到期后保护数据的机制。此机制需要文档安全存储（DSS）。自iText 5.1.3版本起支持PAdES第4部分。
    - 第5部分 - 用于XML内容的PAdES描述了XAdES签名的配置文件。例如，在填写嵌入PDF文件中的XML内容（XFA表单）后，用户可以对表单的选定部分进行签名。目前iText尚不支持此功能。
    - 第6部分 - 电子签名的可视表示。这在iText中得到支持，但也取决于其他因素。例如：您的证书是否包含足够的信息？

## 总结

在本章中，我们探讨了`数字签名`的不同方面。首先，我们查看了一个PDF文件，并了解了如何伪造现有文档。我们学习了`哈希算法`和`加密技术`，并了解了如何结合这些概念来保护PDF文件。最后，我们对`术语`和`标准`进行了概述。

现在，我们准备深入研究PDF文件内部的数字签名。在接下来的章节中，我们将通过一系列示例将理论付诸实践。


