# 1.入门概览

本章介绍 Java 中加密 API 的架构，以及建立在这些加密 API 之上的 Bouncy Castle CMS/SMIME/OpenPGP API 架构。还会提供基本的安装说明，并讨论
BC FIPS 认证 API 与普通发行版之间的差异。同时会给出适用于所有这些 API 的随机数使用注意事项，并探讨“安全位数”对应用与算法的意义。

## Java提供者架构

与 Java 加密架构（JCA）相关的提供者架构是 JVM 中各类安全服务的基础。它现在包括 `Java 安全套接字扩展（JSSE）`
，用于访问安全套接字层（SSL）和传输层安全（TLS）实现、`Java 通用安全服务（JGSS）API` 以及`简单认证与安全层（SASL）API`。你也会看到对
`Java 加密扩展（JCE）`的引用；在早期（Java 1.4 之前）它是与 JCA 分开发布的，不过除了少数情况外，现在通常更倾向于把它整体视为
JCA 的一部分。其中一个 JCA 与 JCE 区分仍然有意义的场景是提供者签名，这部分内容我们稍后会进一步讨论。

在提供者架构中，基础层通过定义一组`服务类`向开发者暴露，以便提供一个统一的接口来与已安装的服务提供者交互。这些`服务类`
通过调用在不同`服务提供者接口（SPI）`中定义的方法来提供功能，而这些 SPI 方法则由实际提供者中定义的对象实现，这些提供者要么随
JVM 一起提供，要么由如 Bouncy Castle 这样的厂商（例如 Legion）提供。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125160256052.png){ loading=lazy }
  <figcaption>通过 Java 加密架构调用的 Provider 的区块视图</figcaption>
</figure>

提供者架构首先在应用开发者通常调用的内容（例如属于 `java.security.Signature` 或 `javax.crypto.Cipher` 的方法）与真正由加密服务提供者（如
Bouncy Castle）实现的内容之间建立了分离。比如从开发者角度创建一个加密器的过程将类似于：

```java
Cipher c = Cipher.getInstance("Blowfish/CBC/PKCS5Padding");
```

但是，Blowfish 密码服务的实现者实际上会继承 javax.crypto.CipherSpi 类，而正是这个服务提供者接口（SPI）对象被封装在 Cipher
对象内部，承担着该密码算法的实际运作部分。

提供程序架构的第二个作用是让应用程序的部署者可以决定使用某个特定算法的哪个厂商实现。这可以通过依赖提供程序的优先级来完成，例如上面例子中调用特定服务的
`getInstance()` 方法时的做法。你也可以通过指定提供程序的名称，明确要求你通过 `getInstance()` 创建的加密服务对象所包含的
SPI 必须来自某个特定提供程序：

```java
Cipher c = Cipher.getInstance("Blowfish/CBC/PKCS5Padding", "BC");
```

或者，从 Java 1.5 及以后版本起，也可以通过传入一个实际的提供者对象来实现：

``` java
Cipherc =Cipher.getInstance( "Blowfish/CBC/PKCS5Padding",newBouncyCastleProvider());
```

### 提供者优先级的工作原理

在未明确指定提供者的情况下，JCA 会依靠提供者的优先级来决定创建哪个 SPI。

你可以通过以下例子看到这一点：

``` java
/**
 * 一个简单的示例应用，展示了服务提供者优先级如何影响 JVM 的返回结果。
 */
public class PrecedenceDemo
{
	public static void main(String[] args)
			throws Exception
	{
		// adds BC to the end of the precedence list
		Security.addProvider(new BouncyCastleProvider());

		System.out.println(MessageDigest.getInstance("SHA1")
				.getProvider().getName());

		System.out.println(MessageDigest.getInstance("SHA1", "BC")
				.getProvider().getName());
	}
}
```

如果你编译并运行这个例子，在普通的 Java 安装环境下，应该会看到如下输出：

```shell
SUN 
BC
```

正如代码中的注释所示，addProvider() 方法会将提供者添加到优先级列表的末尾，因此默认的 SHA-1 实现会来自 SUN 提供者，而不是 BouncyCastle。

当JVM启动时，已安装的提供者的优先级由随JVM提供的 java.security 文件中的优先级表决定。如果你使用的是 Java 1.8 及更早版本，可以在 `$JAVA_HOME/jre/lib/security` 路径下找到该文件；从 Java 1.9 起，文件位于 `$JAVA_HOME/conf/security`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125161554964.png){ loading=lazy }
  <figcaption>优先级配置文件</figcaption>
</figure>


### 提供者签名是什么意思？

你可能也见过对“签名提供者”的提及。许多 JVM 要求任何提供加密或密钥交换服务的提供者都必须由 JCE 签名证书签名。有些 JVM（比如 OpenJDK 项目产出的）则没有此要求，但像 Oracle、IBM 这样的机构发布的 JVM 仍然要求。

如果提供者没有正确签名，在需要提供者签名的 JVM 中，JCE 将拒绝该提供者用于 JCE 功能（注意：这不包括签名和消息摘要）。大多数情况下这不会成为问题，因为自己构建提供者并不常见。但如果你确实要这么做，就需要申请一个 JCE 签名证书。就我们在 Bouncy Castle 的情况而言，我们已经申请了签名证书，并使用 Oracle 授予的证书，使得普通的 Bouncy Castle 提供者和 Bouncy Castle FIPS 提供者都能在标准 JVM 中运行。

注意：JCE 签名证书与普通代码签名证书是完全不同的。如果需要使用 JCE 签名证书，请按照随 Java 发布版文档附带的《如何在 Java 密码架构中实现提供程序》文档中“获取代码签名证书”一节所述的流程申请。

### 管辖区策略文件


