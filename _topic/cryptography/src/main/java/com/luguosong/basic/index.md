---
layout: note
title: 密码学基础
nav_order: 10
parent: 密码学
create_time: 2023/10/30
---

# JCA简介

`JCA（Java Cryptography Architecture,Java 密码体系结构）`是平台的重要组成部分，包括`提供者`
体系结构和一组用于`数字签名`、`消息摘要`（哈希）、`证书`和`证书验证`、`加密`（对称/非对称块/流密码）、`密钥生成和管理`
以及安全`随机数生成`等的API。

# 加密服务提供者(CSP)

加密服务提供者（Cryptographic Service Provider，CSP）指的是实现一个或多个加密服务的包或一组包。

JCA提供了一组API，允许用户查询已安装的提供程序以及它们支持哪些服务：

{% highlight java %}
{% include_relative ShowProviders.java %}
{% endhighlight %}

# CSP优先权

当多个加密服务提供者对指定算法进行实现时，会优先选择优先级高的提供者，当然也可以`指定服务提供者`：

{% highlight java %}
{% include_relative PrecedenceDemo.java %}
{% endhighlight %}

# 引擎类

引擎类与加密服务提供者之间的关系，操作的是引擎类，而真正的实现是由加密服务提供者提供的：

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/jca/Java-Cryptography.svg)

引擎类相当于一个工厂，用于创建`密钥`、`签名`、`消息摘要`、`加密`等对象，而这些对象的具体实现是由`加密服务提供者`提供的。

常用引擎类：
- MessageDigest
- Signature
- KeyFactory
- KeyPairGenerator
- Cipher

# BC库和JCA关系

首先BC库可以作为加密服务提供者，提供了相应算法的具体实现，可以通过JCA引擎类进行相关算法操作。

同时，BC库还通过了自带的一套轻量级API，可以直接进行相关算法的计算。

![](https://cdn.jsdelivr.net/gh/luguosong/images@master/diagrams/jca/BC%E5%BA%93%E4%B8%8EJCA.drawio.svg)



