# 4.密码管理

本章内容包括：

- 实现并使用 PasswordEncoder
- 利用 Spring Security Crypto 模块提供的工具。

在第3章中，我们讨论了如何在使用 Spring Security 实现的应用程序中管理用户。那么，密码该如何处理呢？密码无疑是认证流程中至关重要的一环。本章将带你了解如何在基于
Spring Security 的应用中管理密码和密钥。我们将探讨 PasswordEncoder 接口，以及 Spring Security Crypto 模块（SSCM）为密码管理提供的相关工具。

## 使用密码编码器

通过第三章的学习，你现在应该已经清楚了解了 UserDetails
接口的作用，以及多种实现方式。正如你在第二章中所学，不同的组件在认证和授权过程中负责管理用户信息。你还了解到，其中一些组件有默认实现，比如
UserDetailsService 和 PasswordEncoder。现在你已经知道可以自定义这些默认实现。接下来，我们将深入探讨这些 Bean 及其实现方式，本节将重点分析
PasswordEncoder。图 4.1 展示了 PasswordEncoder 在认证流程中的位置。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508221359247.png){ loading=lazy }
  <figcaption>图4.1 Spring Security认证流程。AuthenticationProvider在认证过程中通过PasswordEncoder校验用户密码。</figcaption>
</figure>

通常情况下，系统不会以明文形式管理密码，而是会对密码进行某种转换，以增加其被读取和窃取的难度。针对这一职责，Spring Security
定义了一个独立的接口。为了便于理解，本节我将通过大量与 PasswordEncoder 实现相关的代码示例进行讲解。我们会先了解这个接口的基本内容，然后在项目中编写自己的实现。接下来，在
4.1.3 小节，我还会为你列出 Spring Security 提供的几种最常用、最广泛应用的 PasswordEncoder 实现。

### PasswordEncoder接口

在本节中，我们将讨论 PasswordEncoder 合约的定义。你需要实现这个合约，以告知 Spring Security
如何校验用户的密码。在认证过程中，PasswordEncoder 决定密码是否有效。每个系统都会以某种方式对密码进行编码，通常建议采用哈希存储，这样就不会有人能够直接读取密码。PasswordEncoder
也可以对密码进行编码。该合约声明的 encode() 和 matches()
方法，实际上就是它的核心职责。这两个方法属于同一个合约，因为它们紧密关联。应用程序对密码的编码方式，直接影响密码的校验方式。我们先来回顾一下
PasswordEncoder 接口的内容：

```java
public interface PasswordEncoder {

	String encode(CharSequence rawPassword);

	boolean matches(CharSequence rawPassword, String encodedPassword);

	default boolean upgradeEncoding(String encodedPassword) {
		return false;
	}
}
```

该接口定义了两个抽象方法和一个带有默认实现的方法。其中，抽象的 encode() 和 matches() 方法是你在使用 PasswordEncoder
实现时最常听到的两个方法。

encode(CharSequence rawPassword) 方法的作用是对传入的字符串进行转换。在 Spring Security 的功能中，它通常用于对密码进行加密或哈希处理。之后，你可以使用
matches(CharSequence rawPassword, String encodedPassword)
方法来判断一个原始密码与加密后的字符串是否匹配。在认证流程中，matches() 方法用于将用户输入的密码与系统保存的凭证进行比对。第三个方法
upgradeEncoding(CharSequence encodedPassword)，在接口默认返回 false。如果你重写该方法并返回 true，那么加密后的密码会再次进行编码，以提升安全性。

在某些情况下，对已编码的密码再次进行编码，可以增加从结果中获取明文密码的难度。总体来说，这属于一种安全上的“掩饰”手段，我个人并不推荐。不过，如果你认为适合你的场景，框架也提供了这种可能性。

### 实现自定义的PasswordEncoder

如你所见，matches() 和 encode() 这两个方法之间有着密切的关联。如果你重写它们，功能上必须保持一致：通过 encode()
方法返回的字符串，应该始终能够被同一个 PasswordEncoder 的 matches() 方法验证通过。在本节中，你将实现 PasswordEncoder
的约定，并定义该接口声明的两个抽象方法。掌握了如何实现 PasswordEncoder
后，你就可以自行决定应用程序在认证过程中如何管理密码。最简单的实现方式是使用一个将密码视为明文的密码编码器，也就是说，它不会对密码进行任何编码处理。

以明文方式管理密码，正是 NoOpPasswordEncoder 实例的作用。我们在第二章的第一个示例中就用到了这个类。如果你打算自己实现一个类似的类，大致可以参考下面的代码示例。

```java title="清单 4.1 最简单的 PasswordEncoder 实现"
public class PlainTextPasswordEncoder
		implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return rawPassword.toString();
	}

	@Override
	public boolean matches(
			CharSequence rawPassword, String encodedPassword) {
		return rawPassword.equals(encodedPassword);
	}
}
```

编码的结果始终与密码相同。因此，要判断两者是否匹配，只需使用 equals() 方法比较字符串即可。下面是一个使用 SHA-512 哈希算法实现的简单
PasswordEncoder 示例。

```java title="清单 4.2 实现一个使用 SHA-512 的 PasswordEncoder"
public class Sha512PasswordEncoder
		implements PasswordEncoder {

	@Override
	public String encode(CharSequence rawPassword) {
		return hashWithSHA512(rawPassword.toString());
	}

	@Override
	public boolean matches(
			CharSequence rawPassword, String encodedPassword) {
		String hashedPassword = encode(rawPassword);
		return encodedPassword.equals(hashedPassword);
	}

	// Omitted code

}
```

在代码清单4.2中，我们使用一个方法对提供的字符串值进行SHA-512哈希。我在清单4.2中省略了该方法的具体实现，你可以在清单4.3中找到它。我们在encode()
方法中调用了这个哈希方法，现在encode()会返回输入值的哈希结果。为了验证哈希值是否匹配输入，matches()
方法会对原始密码进行哈希处理，然后将其结果与用于校验的哈希值进行比较。

``` java title="代码清单4.3 使用 SHA-512 对输入进行哈希的方法实现"
private String hashWithSHA512(String input) {
	StringBuilder result = new StringBuilder();
	try {
		MessageDigest md = MessageDigest.getInstance("SHA-512");
		byte[] digested = md.digest(input.getBytes());
		for (int i = 0; i < digested.length; i++) {
			result.append(Integer.toHexString(0xFF & digested[i]));
		}
	} catch (NoSuchAlgorithmException e) {
		throw new RuntimeException("Bad algorithm");
	}
	return result.toString();
}
```

你将在下一节学到更好的实现方式，所以现在不用太在意这段代码。

### 从提供的PasswordEncoder实现中进行选择

虽然了解如何实现自己的 PasswordEncoder 很有用，但你也需要知道，Spring Security
已经为你提供了一些非常实用的实现。如果这些实现中有适合你应用场景的，你就无需重复造轮子。在本节中，我们将介绍 Spring Security
提供的几种 PasswordEncoder 实现选项，包括：

- NoOpPasswordEncoder —— 不对密码进行加密，直接以明文形式保存。我们只在示例中使用这种实现。由于它没有对密码进行哈希处理，绝不应该在实际项目中使用。
- StandardPasswordEncoder —— 使用 SHA-256
  算法对密码进行哈希处理。该实现目前已被弃用，不建议在新项目中使用。之所以弃用，是因为其采用的哈希算法安全性已不再足够强大，但你可能仍会在一些老项目中见到。如果遇到，建议尽快替换为更安全的密码加密器。
- Pbkdf2PasswordEncoder —— 基于 PBKDF2（Password-Based Key Derivation Function 2）算法实现。
- BCryptPasswordEncoder —— 使用 bcrypt 强哈希函数对密码进行加密。
- SCryptPasswordEncoder —— 使用 scrypt 哈希函数对密码进行加密。

关于哈希算法及其原理，你可以参考 David Wong 所著《Real-World Cryptography》（Manning,
2021）第二章，网址：[http://mng.bz/QRJw。](https://livebook.manning.com/book/real-world-cryptography/chapter-2/)

让我们来看一些如何创建这些类型的 PasswordEncoder 实现类实例的例子。NoOpPasswordEncoder 并不会对密码进行编码，它的实现方式类似于我们在示例
4.1 中提到的 PlainTextPasswordEncoder。因此，这种密码编码器只适用于理论上的示例。此外，NoOpPasswordEncoder
类被设计为单例模式，你无法在类外部直接调用它的构造方法，但可以通过 NoOpPasswordEncoder.getInstance() 方法来获取该类的实例，具体如下：

``` java
PasswordEncoder p = NoOpPasswordEncoder.getInstance();
```

Spring Security 提供的 StandardPasswordEncoder 实现采用 SHA-256 算法对密码进行哈希处理。对于
StandardPasswordEncoder，你可以在哈希过程中指定一个密钥，这个密钥通过构造方法的参数进行设置。如果你使用无参构造方法，默认会将密钥设置为空字符串。不过需要注意的是，StandardPasswordEncoder
目前已经被弃用，不建议在新的项目中继续使用它。当然，你可能会在一些老旧系统或遗留代码中看到它的身影，所以还是有必要了解一下。下面的代码片段演示了如何创建这个密码编码器的实例：

```java
PasswordEncoder p = new StandardPasswordEncoder();
PasswordEncoder p = new StandardPasswordEncoder("secret");
```

Spring Security 还提供了另一种选择，即 Pbkdf2PasswordEncoder 实现，它使用 PBKDF2 算法对密码进行加密。要创建
Pbkdf2PasswordEncoder 的实例，可以采用以下方式：

```java
PasswordEncoder p =
		new Pbkdf2PasswordEncoder("secret", 16, 310000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
```

PBKDF2 是一种相对简单的慢哈希函数，它会根据指定的迭代次数多次执行 HMAC
操作。最后一次调用时传入的前三个参数分别是用于编码过程的密钥值、用于编码密码的迭代次数以及哈希的长度。第二个和第三个参数会影响最终结果的强度。第四个参数用于指定哈希的宽度。你可以选择以下几种算法选项：

- PBKDF2WithHmacSHA1
- PBKDF2WithHmacSHA256
- PBKDF2WithHmacSHA512

你可以根据需要选择更多或更少的迭代次数，以及结果的长度。哈希值越长，密码的安全性就越高（哈希宽度同理）。不过需要注意的是，这些参数会影响性能：迭代次数越多，应用程序消耗的资源也越多。因此，你需要在生成哈希所消耗的资源和所需的加密强度之间做出合理的权衡。

!!! note

	在本书中，我提到了几个密码学相关的概念，如果你对此感兴趣，可以进一步了解。关于 HMAC 及其他密码学细节，我推荐阅读 David Wong 所著的《Real-World Cryptography》（Manning，2021）。该书的第三章对 HMAC 进行了详细介绍。你可以通过 [http://mng.bz/XqJG](https://livebook.manning.com/book/real-world-cryptography/chapter-3/) 获取这本书。

Spring Security 还提供了另一个非常优秀的选择——BCryptPasswordEncoder。它采用强大的 bcrypt 哈希算法对密码进行加密。你可以通过无参构造方法实例化
BCryptPasswordEncoder。当然，你也可以选择指定一个强度系数，用于表示编码过程中使用的对数轮数。此外，你还可以自定义用于加密的
SecureRandom 实例。

```java
PasswordEncoder p = new BCryptPasswordEncoder();
PasswordEncoder p = new BCryptPasswordEncoder(4);

SecureRandom s = SecureRandom.getInstanceStrong();
PasswordEncoder p = new BCryptPasswordEncoder(4, s);
```

您提供的 log rounds（对数轮次）值会影响哈希操作的迭代次数。实际使用的迭代次数为 2 的 log rounds 次方。在计算迭代次数时，log
rounds 的取值只能在 4 到 31 之间。您可以通过调用前面代码片段中展示的第二或第三个重载构造函数来指定这个值。

我为你介绍的最后一个选项是 SCryptPasswordEncoder（见图 4.2）。这种密码编码器采用 scrypt 哈希函数。对于
SCryptPasswordEncoder，你可以按照图 4.2 所示的方式创建其实例。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508241014200.png){ loading=lazy }
  <figcaption>图4.2 SCryptPasswordEncoder 构造函数接收五个参数，可用于配置 CPU 成本、内存成本、密钥长度和盐值长度。</figcaption>
</figure>

### 多种编码策略与DelegatingPasswordEncoder的结合使用

在本节中，我们将讨论在认证流程中需要采用不同密码匹配实现的场景。同时，你还将学习如何在应用中使用一个实用工具来充当
PasswordEncoder。这个工具本身并不实现密码编码逻辑，而是将具体操作委托给实现了 PasswordEncoder 接口的其他对象。

在某些应用中，你可能会觉得拥有多种密码编码器并根据特定配置进行选择非常有用。在实际生产环境中，我经常看到
DelegatingPasswordEncoder
的一个常见场景是：从某个特定版本开始，应用更换了密码加密算法。比如说，有人发现当前使用的算法存在安全漏洞，你希望为新注册用户更换加密算法，但又不想影响已有用户的凭证，这样一来系统中就会同时存在多种类型的哈希值。那该如何管理这种情况呢？虽然这并不是唯一的解决方案，但使用
DelegatingPasswordEncoder 对象是一个不错的选择。

DelegatingPasswordEncoder 是 PasswordEncoder
接口的一个实现类，它本身并不直接实现加密算法，而是将加密操作委托给其他实现了同一接口的实例。加密后的哈希值会以一个前缀开头，用于标识所采用的加密算法。DelegatingPasswordEncoder
会根据密码前缀，自动委托给对应的 PasswordEncoder 实现进行处理。

听起来似乎有些复杂，但通过一个例子你会发现其实很简单。图4.3展示了各个 PasswordEncoder 实例之间的关系。DelegatingPasswordEncoder
持有一个 PasswordEncoder 实现的列表，并将操作委托给它们。DelegatingPasswordEncoder 会将每个实例存储在一个映射（map）中。NoOpPasswordEncoder
被分配到键名为 noop，而 BCryptPasswordEncoder 实现则分配到键名为 bcrypt。当密码前缀为 {noop} 时，DelegatingPasswordEncoder
会将操作委托给 NoOpPasswordEncoder 实现。如果前缀为 {bcrypt}，则操作会委托给 BCryptPasswordEncoder 实现，如图4.4所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508242052927.png){ loading=lazy }
  <figcaption>图4.3 在此场景中，DelegatingPasswordEncoder 会根据密码前缀选择相应的编码器：对于以 {noop} 开头的密码，委托给 NoOpPasswordEncoder 处理；以 {bcrypt} 开头的密码，则由 BCryptPasswordEncoder 处理；而以 {scrypt} 开头的密码，则交由 SCryptPasswordEncoder 处理。当密码带有 {noop} 前缀时，DelegatingPasswordEncoder 会将处理任务转交给 NoOpPasswordEncoder。</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508242126136.png){ loading=lazy }
  <figcaption>图4.4 在这里，DelegatingPasswordEncoder 会将带有 {noop} 前缀的密码交由 NoOpPasswordEncoder 处理，将带有 {bcrypt} 前缀的密码交由 BCryptPasswordEncoder 处理，而带有 {scrypt} 前缀的密码则交由 SCryptPasswordEncoder 处理。如果密码带有 {bcrypt} 前缀，DelegatingPasswordEncoder 就会将处理流程转交给 BCryptPasswordEncoder 的机制。</figcaption>
</figure>


接下来，让我们看看如何定义一个 DelegatingPasswordEncoder。首先，你需要创建一组你想要使用的 PasswordEncoder 实例，然后将它们组合到一个
DelegatingPasswordEncoder 中，具体实现如下所示。

```java title="清单 4.4 创建 DelegatingPasswordEncoder 实例"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public PasswordEncoder passwordEncoder() {
		Map<String, PasswordEncoder> encoders = new HashMap<>();

		encoders.put("noop", NoOpPasswordEncoder.getInstance());
		encoders.put("bcrypt", new BCryptPasswordEncoder());
		encoders.put("scrypt", new SCryptPasswordEncoder());

		// 参数一为默认密码编码器
		// 如果没有前缀，则使用默认的密码编码器
		return new DelegatingPasswordEncoder("bcrypt", encoders);
	}
}
```

DelegatingPasswordEncoder 只是一个充当 PasswordEncoder 的工具，因此当你需要从多个实现中进行选择时，可以使用它。在代码清单
4.4 中，声明的 DelegatingPasswordEncoder 实例包含了对 NoOpPasswordEncoder、BCryptPasswordEncoder 和 SCryptPasswordEncoder
的引用，并且默认委托给 BCryptPasswordEncoder 实现。根据哈希值的前缀，DelegatingPasswordEncoder 会使用正确的 PasswordEncoder
实现来校验密码。这个前缀是用来标识应该从编码器映射中选用哪个密码编码器的关键。如果没有前缀，DelegatingPasswordEncoder
会使用默认的编码器。默认的 PasswordEncoder 是在构造 DelegatingPasswordEncoder 实例时作为第一个参数传入的那个编码器。对于清单
4.4 中的代码，默认的 PasswordEncoder 就是 bcrypt。

!!! note

	花括号是哈希前缀的一部分，应该包裹在密钥名称的两侧。例如，如果提供的哈希值是 {noop}12345，DelegatingPasswordEncoder 就会将其委托给我们为前缀 noop 注册的 NoOpPasswordEncoder。再次提醒，前缀中的花括号是必须的。

如果哈希值看起来像下面的代码片段，那么密码编码器就是我们为前缀 {bcrypt} 指定的
BCryptPasswordEncoder。由于我们将其设置为默认实现，所以如果没有任何前缀，应用也会默认使用这个编码器：

```
{bcrypt}$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG
```

为方便起见，Spring Security 提供了一种创建 DelegatingPasswordEncoder 的方式，该方式会将所有标准的 PasswordEncoder
实现映射到一个集合中。PasswordEncoderFactories 类提供了一个静态方法 createDelegatingPasswordEncoder()，该方法会返回一个包含完整
PasswordEncoder 映射，并以 bcrypt 作为默认编码器的 DelegatingPasswordEncoder 实现：

``` java
PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
```

!!! note "编码 vs. 加密 vs. 哈希"

	在前面的章节中，我经常使用编码、加密和哈希这几个术语。这里我想简单澄清一下这些术语的含义，以及它们在本书中的具体用法。

	编码指的是对给定输入进行的任何转换。例如，如果我们有一个将字符串反转的函数 x，那么函数 x 作用于 ABCD 时，会得到 DCBA。

	加密是一种特殊类型的编码方式，在这种方式中，我们需要同时提供输入值和密钥才能得到输出。密钥的作用在于，之后可以根据密钥来决定谁有权限将该函数反向运算（即通过输出还原出输入）。用函数的形式来表示加密，最简单的方式如下：

	```text
	(x, k) -> y 
	```

	其中，x 是输入，k 是密钥，y 是加密后的结果。这样，知道密钥的人就可以通过已知的函数，根据输出（y, k）推算出输入 x。我们把这个反向的过程称为解密。如果加密和解密使用的是同一个密钥，通常称为对称密钥。

	如果我们在加密和解密过程中分别使用了不同的密钥（加密：(x, k1) -> y，解密：(y, k2) -> x），那么这种加密方式就被称为非对称加密。此时，(k1, k2) 被称为密钥对。用于加密的 k1 通常被称为公钥，而用于解密的 k2 则称为私钥。这样，只有私钥的持有者才能解密数据。

	哈希是一种特殊的编码方式，不过它的函数是单向的。也就是说，从哈希函数的输出 y，无法还原出输入 x。然而，我们始终需要一种方法来判断某个输出 y 是否对应某个输入 x，因此可以把哈希理解为一对用于编码和匹配的函数。如果哈希过程是 x -> y，那么还应该有一个匹配函数 (x, y) -> boolean。

	有时候，哈希函数还会在输入中加入一个随机值：(x, k) -> y。我们把这个随机值称为“盐值”。盐值能够增强哈希函数的安全性，使得通过结果反推出原始输入变得更加困难。

总结一下我们在本书中迄今为止讨论和应用过的合约，表4.1对每个组件做了简要说明。

| Contract           | Description                                                          |
|--------------------|----------------------------------------------------------------------|
| UserDetails        | 表示 Spring Security 所识别的用户。                                           |
| GrantedAuthority   | 定义了应用程序中允许用户执行的操作（例如，读取、写入、删除等）。                                     |
| UserDetailsService | 表示用于通过用户名获取用户详细信息的对象。                                                |
| UserDetailsManager | 这是一个更为专门的 UserDetailsService 合约。除了可以通过用户名检索用户之外，还可以用于修改用户集合或特定用户的信息。 |
| PasswordEncoder    | 指定密码的加密或哈希方式，以及如何校验给定的编码字符串是否与明文密码匹配。                                |

## 利用Spring Security Crypto模块

在本节中，我们将讨论 Spring Security 的加密模块（SSCM），这是 Spring Security 中专门处理加密相关功能的部分。Java
语言本身并未原生提供加解密功能和密钥生成，这在开发过程中为开发者带来了一定的限制，需要额外引入依赖来实现更便捷的加密操作。

为了让我们的开发更加便捷，Spring Security 也提供了自己的解决方案，这样你就无需引入额外的库，从而减少了项目的依赖。密码编码器其实也是
SSCM（Spring Security 加密模块）的一部分，尽管我们在前面的章节中是单独讲解的。在本节中，我们将探讨 SSCM
在加密相关方面还提供了哪些其他选项，并通过示例介绍如何使用 SSCM 的两个核心功能：

- `密钥生成器（Key generators）`——用于为哈希和加密算法生成密钥的对象
- `加密器（Encryptors）`——用于加密和解密数据的对象

### 使用密钥生成器

在本节中，我们将讨论密钥生成器。密钥生成器是一种用于生成特定类型密钥的对象，通常用于加密或哈希算法。Spring Security
提供的密钥生成器实现是非常实用的工具。你会更倾向于使用这些实现，而不是为你的应用程序引入额外的依赖库，这也是我建议你熟悉这些工具的原因。接下来，我们来看一些创建和使用密钥生成器的代码示例。

有两种主要类型的密钥生成器接口：BytesKeyGenerator 和 StringKeyGenerator。我们可以直接通过工厂类 KeyGenerators
来构建它们。你可以使用字符串密钥生成器（由 StringKeyGenerator 接口表示）来获取一个字符串形式的密钥。通常，我们会将这个密钥用作哈希或加密算法的盐值。你可以在下面的代码片段中找到
StringKeyGenerator 接口的定义：

```java
public interface StringKeyGenerator {
	String generateKey();
}
```

该生成器仅包含一个 generateKey() 方法，用于返回表示键值的字符串。下面的代码片段展示了如何获取一个 StringKeyGenerator
实例，并利用它生成一个盐值：

``` java
// 通过工厂类KeyGenerators构建StringKeyGenerator对象
StringKeyGenerator keyGenerator = KeyGenerators.string();
String salt = keyGenerator.generateKey();
```

生成器会创建一个8字节的密钥，并将其编码为十六进制字符串。该方法会将上述操作的结果作为字符串返回。第二个用于描述密钥生成器的接口是 BytesKeyGenerator，其定义如下：

```java
public interface BytesKeyGenerator {

	int getKeyLength();

	byte[] generateKey();

}
```

除了返回字节数组（byte[]）形式密钥的 generateKey() 方法之外，该接口还定义了另一个方法，用于返回密钥的字节长度。默认的 BytesKeyGenerator 生成的密钥长度为 8 字节：

```java
BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom();
byte[] key = keyGenerator.generateKey();
int keyLength = keyGenerator.getKeyLength();
```

在前面的代码片段中，密钥生成器生成的是长度为8字节的密钥。如果你希望指定不同的密钥长度，可以在获取密钥生成器实例时，通过为 KeyGenerators.secureRandom() 方法传入所需的长度值来实现：

```java
BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom(16);
```

通过 KeyGenerators.secureRandom() 方法创建的 BytesKeyGenerator，每次调用 generateKey() 方法时都会生成唯一的密钥。在某些情况下，我们可能更希望每次调用同一个密钥生成器时都返回相同的密钥值。此时，可以使用 KeyGenerators.shared(int length) 方法创建一个 BytesKeyGenerator。在下面的代码片段中，key1 和 key2 的值是相同的：

```java
BytesKeyGenerator keyGenerator = KeyGenerators.shared(16);
byte [] key1 = keyGenerator.generateKey();
byte [] key2 = keyGenerator.generateKey();
```

### 使用加密器进行机密信息的加密与解密

在本节中，我们将结合代码示例，介绍 Spring Security 提供的加密器实现。加密器是一种实现加密算法的对象。在安全领域，加密和解密是常见的操作，因此你很可能会在应用中用到这些功能。

我们经常需要在系统各组件之间传输数据或将数据持久化时对其进行加密。加密器通常提供加密和解密两种操作。SSCM 定义了两种类型的加密器：BytesEncryptor 和 TextEncryptor。虽然它们的职责类似，但处理的数据类型不同。TextEncryptor 以字符串的形式管理数据，其方法接收字符串作为输入，并返回字符串作为输出，正如其接口定义所示：

```java
public interface TextEncryptor {

 String encrypt(String text);
 String decrypt(String encryptedText);

}
```

BytesEncryptor 更加通用，您可以将输入数据以字节数组的形式提供给它：

```java
public interface BytesEncryptor {

 byte[] encrypt(byte[] byteArray);
 byte[] decrypt(byte[] encryptedByteArray);

}
```

让我们来看看有哪些方式可以创建和使用加密器。Encryptors 工厂类为我们提供了多种选择。对于 BytesEncryptor，我们可以像下面这样使用 Encryptors.standard() 或 Encryptors.stronger() 方法：

``` java
String salt = KeyGenerators.string().generateKey();
String password = "secret";
String valueToEncrypt = "HELLO";

BytesEncryptor e = Encryptors.standard(password, salt);
byte[] encrypted = e.encrypt(valueToEncrypt.getBytes());
byte[] decrypted = e.decrypt(encrypted); 
```

在幕后，标准字节加密器使用256位AES加密算法对输入内容进行加密。若需构建更强大的字节加密器实例，可以调用 Encryptors.stronger() 方法。

``` java
BytesEncryptor e = Encryptors.stronger(password, salt);
```

区别很小，并且是在幕后发生的。256位的AES加密采用了Galois/Counter Mode（GCM）作为操作模式，而标准模式则使用了分组密码链接（CBC）模式，后者被认为相对较弱。

TextEncryptors 主要有三种类型。你可以通过调用 Encryptors.text() 或 Encryptors.delux() 来创建这三种类型的加密器。除了这些用于创建加密器的方法外，还有一个方法可以返回一个虚拟的 TextEncryptor，这种加密器不会对值进行加密。你可以在演示示例中使用这个虚拟的 TextEncryptor，或者在你想测试应用性能但又不希望花时间在加密上的场景下使用。返回这种无操作加密器的方法是 Encryptors.noOpText()。在下面的代码片段中，你可以看到一个使用 TextEncryptor 的示例。即使调用了加密器，在这个例子中，encrypted 和 valueToEncrypt 实际上是相同的：

```java
String valueToEncrypt = "HELLO";
TextEncryptor e = Encryptors.noOpText();
String encrypted = e.encrypt(valueToEncrypt);
```

Encryptors.text() 加密器通过调用 Encryptors.standard() 方法来处理加密操作，而 Encryptors.delux() 方法则使用类似于 Encryptors.stronger() 实例的方式。

```java
String salt = KeyGenerators.string().generateKey();
String password = "secret";
String valueToEncrypt = "HELLO";

TextEncryptor e = Encryptors.text(password, salt);
String encrypted = e.encrypt(valueToEncrypt);
String decrypted = e.decrypt(encrypted);

```

## 总结

- PasswordEncoder 在认证逻辑中承担着至关重要的职责——处理密码。
- Spring Security 提供了多种哈希算法可供选择，实现方式只需根据需求进行选择即可。
- Spring Security Crypto 模块（SSCM）为密钥生成器和加密器的实现提供了多种选择。
- 密钥生成器是用于生成可与加密算法配合使用的密钥的工具对象。
- 加密器是用于实现数据加密和解密的工具对象。
