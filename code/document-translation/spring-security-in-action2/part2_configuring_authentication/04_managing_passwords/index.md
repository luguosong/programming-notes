# 管理密码

在第三章中，我们讨论了如何在使用 Spring Security 实现的应用程序中`管理用户`。那么密码呢？它们无疑是认证流程中的重要组成部分。在本章中，您将学习如何在使用
Spring Security 实现的应用程序中管理密码和密钥。我们将讨论 `PasswordEncoder` 合约以及 Spring Security Crypto
模块（SSCM）提供的密码管理工具。

## 使用密码编码器

从第3章开始，你应该已经清楚了解了`UserDetails`接口是什么，以及多种使用其实现的方法。但正如你在第2章中了解到的，不同的参与者在认证和授权过程中管理用户表示。你还了解到，其中一些有默认设置，比如
`UserDetailsService`和`PasswordEncoder`。你现在知道可以覆盖这些默认设置。我们将继续深入理解这些bean及其实现方法，因此在本节中，我们将分析
`PasswordEncoder`。图4.1提醒你`PasswordEncoder`在认证过程中的位置。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409240923581.png){ loading=lazy }
  <figcaption>图4.1 Spring Security认证过程。AuthenticationProvider在认证过程中使用PasswordEncoder验证用户密码。</figcaption>
</figure>

通常情况下，系统不会以明文形式管理密码，这些密码通常会经过某种转换，使其更难以读取和窃取。为此，Spring Security
定义了一个独立的协议。为了简单解释这一点，我在本节中提供了许多与 `PasswordEncoder`
实现相关的代码示例。我们将从理解协议开始，然后在项目中编写我们的实现。接着，在第 4.1.3 节中，我将为您提供 Spring Security
提供的最知名和广泛使用的 `PasswordEncoder` 实现列表。

### PasswordEncoder 合约

在本节中，我们将讨论PasswordEncoder接口的定义。您需要实现这个接口，以告知Spring
Security如何验证用户的密码。在认证过程中，PasswordEncoder决定密码是否有效。每个系统都会以某种方式存储密码，最好是以哈希方式存储，以确保没有人能够读取它们。PasswordEncoder还可以对密码进行编码。接口中声明的encode()
和matches()方法实际上定义了其职责。这两个方法是同一接口的一部分，因为它们紧密相关。应用程序对密码进行编码的方式与密码的验证方式息息相关。首先，让我们回顾一下PasswordEncoder接口的内容：

``` java
public interface PasswordEncoder {

	String encode(CharSequence rawPassword);

	boolean matches(CharSequence rawPassword, String encodedPassword);

	default boolean upgradeEncoding(String encodedPassword) {
		return false;
	}
}
```

该接口定义了两个抽象方法和一个具有默认实现的方法。抽象的 `encode()` 和 `matches()` 方法也是在处理 `PasswordEncoder`
实现时最常听到的方法。

`encode(CharSequence rawPassword)` 方法的目的是对提供的字符串进行转换。在 Spring Security 的功能中，它用于对给定的密码进行加密或哈希处理。之后可以使用
`matches(CharSequence rawPassword, String encodedPassword)` 方法来检查编码后的字符串是否与原始密码匹配。在认证过程中，你可以使用
`matches()` 方法来验证提供的密码是否与已知凭据匹配。第三个方法是 `upgradeEncoding(CharSequence encodedPassword)`，在默认情况下返回
false。如果你重写它以返回 true，那么编码后的密码将再次编码以提高安全性。

在某些情况下，对已编码的密码进行再次编码可以增加从结果中获取明文密码的难度。总体来说，这是一种我个人不太喜欢的模糊处理。但如果你认为适用于你的情况，框架提供了这种可能性。

### 实现您的密码编码器

正如你所观察到的，方法`matches()`和`encode()`之间有着密切的关系。如果你重写它们，它们在功能上应该始终保持一致：由`encode()`
方法返回的字符串应该始终可以通过同一个`PasswordEncoder`的`matches()`方法进行验证。在本节中，你将实现`PasswordEncoder`
的契约，并定义接口声明的两个抽象方法。了解如何实现`PasswordEncoder`
后，你可以选择应用程序在认证过程中如何管理密码。最简单的实现是一个将密码视为纯文本的密码编码器：也就是说，它不会对密码进行任何编码。

以明文管理密码正是 `NoOpPasswordEncoder` 实例的作用。我们在第二章的第一个例子中使用了这个类。如果你要自己编写一个，它可能会像下面的代码一样。

``` java title="清单 4.1 最简单的 PasswordEncoder 实现"
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

编码的结果总是与密码相同。因此，要检查它们是否匹配，只需使用`equals()`比较字符串即可。下面的示例展示了一个简单的
`PasswordEncoder`实现，它使用`SHA-512`哈希算法。

``` java title="清单 4.2 实现使用 SHA-512 的 PasswordEncoder"
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

在代码清单4.2中，我们使用一种方法对提供的字符串值进行`SHA-512`哈希。我在代码清单4.2中省略了该方法的实现，但你可以在代码清单4.3中找到。我们从
`encode()`方法中调用此方法，该方法现在返回其输入的哈希值。为了验证输入与哈希的匹配，`matches()`
方法会对其输入中的原始密码进行哈希处理，并将其与用于验证的哈希进行比较。

``` java title="清单 4.3 使用 SHA-512 对输入进行哈希的方法实现"
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

在下一节中，您将学习到更好的方法来实现这一点，所以现在不用太在意这段代码。

### PasswordEncoder的实现

虽然了解如何实现自己的 `PasswordEncoder` 很有用，但你也应该知道，Spring Security
已经为你提供了一些有利的实现。如果其中一个适合你的应用程序，你就不需要重新编写。在本节中，我们将讨论 Spring Security 提供的
`PasswordEncoder` 实现选项。这些选项包括：

- `NoOpPasswordEncoder`—不对密码进行编码，而是以明文形式保存。我们仅在示例中使用此实现。由于它不对密码进行哈希处理，因此绝不应在实际场景中使用。
- ❌`StandardPasswordEncoder`—使用 `SHA-256` 对密码进行哈希处理。`此实现现已被弃用`
  ，不应在新的实现中使用。之所以弃用，是因为它使用的哈希算法已不再被认为足够强大，但在现有应用中可能仍会发现此实现。最好在现有应用中发现时，用其他更强大的密码编码器替换它。
- `Pbkdf2PasswordEncoder`—使用基于密码的密钥派生函数2（`PBKDF2`）。
- `BCryptPasswordEncoder`—使用`bcrypt`强哈希函数对密码进行编码。
- `SCryptPasswordEncoder`—使用`scrypt`哈希函数对密码进行编码。

让我们来看一些如何创建这些类型的`PasswordEncoder`实现实例的例子。`NoOpPasswordEncoder`不对密码进行编码。它的实现类似于我们在示例4.1中提到的
`PlainTextPasswordEncoder`。因此，我们仅在理论示例中使用这种密码编码器。此外，`NoOpPasswordEncoder`
类被设计为单例模式。你不能从类外直接调用它的构造函数，但可以使用`NoOpPasswordEncoder.getInstance()`方法来获取类的实例，如下所示：

``` java
PasswordEncoder p = NoOpPasswordEncoder.getInstance();
```

Spring Security 提供的 `StandardPasswordEncoder` 实现使用 `SHA-256` 对密码进行哈希处理。对于 `StandardPasswordEncoder`
，你可以提供一个用于哈希过程的密钥。你可以通过构造函数的参数来设置这个密钥的值。如果选择调用无参数的构造函数，默认会使用空字符串作为密钥的值。然而，
`StandardPasswordEncoder` 现在`已经被弃用`，我不建议在新的实现中使用它。你可能会在旧应用程序或遗留代码中发现它的使用，因此需要对此有所了解。下面的代码片段展示了如何创建这个密码编码器的实例：

``` java
PasswordEncoder p = new StandardPasswordEncoder();
PasswordEncoder p = new StandardPasswordEncoder("secret");
```

Spring Security 提供的另一个选项是使用 `PBKDF2` 进行密码编码的 `Pbkdf2PasswordEncoder` 实现。要创建
`Pbkdf2PasswordEncoder` 的实例，您可以选择以下方法：

``` java
PasswordEncoder p =
		new Pbkdf2PasswordEncoder("secret", 16, 310000, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);
```

PBKDF2 是一种相对简单的慢速哈希函数，它根据迭代次数参数执行多次 HMAC。最后一次调用接收的前三个参数分别是
`用于编码过程的密钥值`、`用于编码密码的迭代次数`和`哈希的大小`。第二和第三个参数可以影响结果的强度。第四个参数决定哈希的宽度。您可以选择以下选项：

- PBKDF2WithHmacSHA1
- PBKDF2WithHmacSHA256
- PBKDF2WithHmacSHA512

可以选择更多或更少的迭代次数，以及结果的长度。哈希越长，密码越强大（哈希宽度也是如此）。然而，请注意性能会受到这些值的影响：迭代次数越多，应用程序消耗的资源就越多。你应该在生成哈希所消耗的资源和编码所需的强度之间做出明智的权衡。

Spring Security 提供的另一个优秀选项是 `BCryptPasswordEncoder`，它使用 bcrypt 强哈希函数来编码密码。你可以通过调用无参构造函数来实例化
`BCryptPasswordEncoder`。不过，你也可以选择指定一个`强度系数`，该系数代表编码过程中使用的对数轮数。此外，你还可以更改用于编码的
`SecureRandom` 实例：

``` java
PasswordEncoder p = new BCryptPasswordEncoder();
PasswordEncoder p = new BCryptPasswordEncoder(4);

SecureRandom s = SecureRandom.getInstanceStrong();
PasswordEncoder p = new BCryptPasswordEncoder(4, s);
```

您提供的日志轮次值会影响哈希操作使用的迭代次数。使用的迭代次数为2的日志轮次次方。在计算迭代次数时，日志轮次的值只能在4到31之间。您可以通过调用第二或第三个重载构造函数来指定这一点，如前面的代码片段所示。

我向您介绍的最后一个选项是 `SCryptPasswordEncoder`（图 4.2）。这种密码编码器使用 `scrypt` 哈希函数。对于
`SCryptPasswordEncoder`，您可以按照图 4.2 所示创建其实例。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409241536421.png){ loading=lazy }
  <figcaption>图4.2 SCryptPasswordEncoder构造函数接受五个参数，允许您配置CPU成本、内存成本、密钥长度和盐值长度。</figcaption>
</figure>

### DelegatingPasswordEncoder多重编码策略

在本节中，我们将讨论在身份验证流程中必须应用各种实现来匹配密码的情况。您还将学习如何在应用程序中应用一个有用的工具，该工具充当
`PasswordEncoder`。这个工具并没有自己的实现，而是委托给其他实现了`PasswordEncoder`接口的对象。

在某些应用中，你可能会发现使用多种密码编码器并根据特定配置进行选择是很有用的。在生产应用中，我常见到使用
`DelegatingPasswordEncoder`
的场景是，当编码算法随着应用的某个版本更新而改变时。想象一下，如果有人发现当前使用的算法存在漏洞，你希望为新注册用户更改算法，但不想更改现有凭据。这样一来，你就会有多种不同的哈希。你该如何管理这种情况呢？虽然这不是唯一的解决方案，但一个不错的选择是使用
`DelegatingPasswordEncoder`对象。

`DelegatingPasswordEncoder` 是 `PasswordEncoder` 接口的一种实现。它并不直接实现编码算法，而是委托给另一个实现相同接口的实例。哈希值以一个前缀开头，该前缀用于指明定义该哈希值的算法。
`DelegatingPasswordEncoder` 根据密码的前缀将任务委托给正确的 `PasswordEncoder` 实现。

这听起来很复杂，但通过一个例子，你会发现其实很简单。图4.3展示了`PasswordEncoder`实例之间的关系。`DelegatingPasswordEncoder`
有一个`PasswordEncoder`实现的列表，它会将操作委托给这些实现。`DelegatingPasswordEncoder`将每个实例存储在一个映射中。
`NoOpPasswordEncoder`被分配了键`noop`，而`BCryptPasswordEncoder`实现被分配了键`bcrypt`。当密码前缀为`{noop}`时，
`DelegatingPasswordEncoder`会将操作委托给`NoOpPasswordEncoder`实现。如果前缀是`{bcrypt}`，那么操作就会委托给
`BCryptPasswordEncoder`实现，如图4.4所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409241600344.png){ loading=lazy }
  <figcaption>在此场景中，DelegatingPasswordEncoder 使用 NoOpPasswordEncoder 处理以 {noop} 为前缀的密码，使用 BCryptPasswordEncoder 处理以 {bcrypt} 开头的密码，使用 SCryptPasswordEncoder 处理以 {scrypt} 开头的密码。当密码带有 {noop} 前缀时，DelegatingPasswordEncoder 将任务交给 NoOpPasswordEncoder 版本。</figcaption>
</figure>

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409241609188.png){ loading=lazy }
  <figcaption>图4.4 在这里，DelegatingPasswordEncoder 将处理以 {noop} 为前缀的密码任务分配给 NoOpPasswordEncoder，将以 {bcrypt} 为前缀的密码任务分配给 BCryptPasswordEncoder，将以 {scrypt} 为前缀的密码任务分配给 SCryptPasswordEncoder。如果密码带有 {bcrypt} 前缀，DelegatingPasswordEncoder 会将处理过程交由 BCryptPasswordEncoder 的机制。</figcaption>
</figure>

接下来，让我们了解如何定义一个 `DelegatingPasswordEncoder`。首先，创建一个包含所需 `PasswordEncoder` 实现实例的集合，然后将这些实例组合到一个
`DelegatingPasswordEncoder` 中，如下所示。

``` java title="清单 4.4 创建 DelegatingPasswordEncoder 实例"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public PasswordEncoder passwordEncoder() {
		Map<String, PasswordEncoder> encoders = new HashMap<>();

		encoders.put("noop", NoOpPasswordEncoder.getInstance());
		encoders.put("bcrypt", new BCryptPasswordEncoder());
		encoders.put("scrypt", new SCryptPasswordEncoder());

		return new DelegatingPasswordEncoder("bcrypt", encoders);
	}
}
```

`DelegatingPasswordEncoder` 只是一个充当 `PasswordEncoder` 的工具，因此当你需要从多个实现中进行选择时可以使用它。在代码清单4.4中，声明的
`DelegatingPasswordEncoder` 实例包含对 `NoOpPasswordEncoder`、`BCryptPasswordEncoder` 和 `SCryptPasswordEncoder`
的引用，并将默认实现委托给 `BCryptPasswordEncoder`。根据哈希的前缀，`DelegatingPasswordEncoder` 使用正确的
`PasswordEncoder` 实现来匹配密码。这个前缀包含用于从编码器映射中识别要使用的密码编码器的键。如果没有前缀，
`DelegatingPasswordEncoder` 使用默认编码器。默认的 `PasswordEncoder` 是在构造 `DelegatingPasswordEncoder`
实例时作为第一个参数提供的。在代码清单4.4中，默认的 `PasswordEncoder` 是 bcrypt。

!!! note

	花括号是哈希前缀的一部分，应该围绕键的名称。例如，如果提供的哈希是{noop}12345，`DelegatingPasswordEncoder`会委托给我们为前缀noop注册的`NoOpPasswordEncoder`。再次提醒，`前缀中的花括号是必需的`。

如果哈希值看起来像下面的代码片段，那么密码编码器就是我们分配给前缀 {bcrypt} 的那个，即 `BCryptPasswordEncoder`
。如果根本没有前缀，应用程序也会委托给它，因为我们将其定义为默认实现：

```text
{bcrypt}$2a$10$xn3LI/AjqicFYZFruSwve.681477XaVNaUQbr1gioaWPn4t1KsnmG
```

为了方便起见，Spring Security 提供了一种创建 DelegatingPasswordEncoder 的方式，该方式包含一个映射到所有标准提供的
`PasswordEncoder` 实现的映射。`PasswordEncoderFactories` 类提供了一个 `createDelegatingPasswordEncoder()`
静态方法，该方法返回一个包含完整 `PasswordEncoder` 映射集的 `DelegatingPasswordEncoder` 实现，并将 `bcrypt` 作为默认编码器。

``` java
PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
```

!!! note "编码 vs. 加密 vs. 哈希"

	在前面的章节中，我经常使用`编码`、`加密`和`哈希`这些术语。我想简要澄清一下这些术语及其使用方式。

	`编码`指的是对给定输入的任何转换。例如，如果我们有一个函数 x 用于反转字符串，那么函数 x -> y 应用于 ABCD 会产生 DCBA。

	`加密`是一种特殊类型的编码，为了获得输出，我们需要提供输入值和一个密钥。密钥使我们能够选择之后谁可以逆转该函数（从输出中获取输入）。将加密表示为函数的最简单形式是。

	```text
	(x, k) -> y 
	```

	其中 x 是输入，k 是密钥，y 是加密结果。这样，知道密钥的人可以使用已知函数从输出中获得输入 (y, k) -> x。我们称这个反向函数为解密。如果用于加密的密钥与用于解密的密钥相同，我们通常称之为对称密钥。	

	如果我们有两个不同的密钥用于加密和解密（(x, k1) -> y 和 (y, k2) -> x），那么我们称这种加密为非对称密钥加密。此时，(k1, k2) 被称为密钥对。用于加密的密钥 k1 也被称为公钥，而 k2 则称为私钥。这样，只有私钥的持有者才能解密数据。

	`哈希`是一种特殊类型的编码，但其函数是单向的。也就是说，从哈希函数的输出 y 无法还原输入 x。然而，应该始终有一种方法来检查输出 y 是否对应于输入 x，因此我们可以将哈希理解为一对用于编码和匹配的函数。如果哈希是 x -> y，那么我们也应该有一个匹配函数 (x, y) -> 布尔值。

	有时候，哈希函数也可能会在输入中加入一个随机值：(x, k) -> y。我们称这个值为“盐”。盐使得函数更强大，增加了通过反向函数从结果中获取输入的难度。

总结到目前为止我们在本书中讨论和应用的合同，表4.1简要描述了每个组成部分。

`表4.1 代表Spring Security中认证流程主要契约的接口（查看表格图）`

| 合同                 | 描述                                                          |
|--------------------|-------------------------------------------------------------|
| UserDetails        | 表示 Spring Security 所看到的用户。                                  |
| GrantedAuthority   | 定义应用程序目的内用户允许执行的操作（例如，读取、写入、删除等）                            |
| UserDetailsService | 表示用于通过用户名获取用户详细信息的对象。                                       |
| UserDetailsManager | UserDetailsService 的一个更具体的合同。除了通过用户名检索用户外，还可以用于修改用户集合或特定用户。 |
| PasswordEncoder    | 指定密码的加密或哈希方式，以及如何检查给定的编码字符串是否与明文密码匹配。                       |

## Spring Security Crypto 模块

在本节中，我们将讨论 Spring Security 加密模块（`SSCM`），这是 Spring Security 处理加密的部分。Java
语言本身并不提供加密和解密功能以及生成密钥的功能，这限制了开发人员在添加依赖项时，无法更轻松地实现这些功能。

为了让我们的生活更轻松，Spring Security 还提供了自己的解决方案，使您无需使用单独的库即可减少项目的依赖性。即使在之前的部分中我们将其单独处理，密码编码器也是
`SSCM` 的一部分。在本节中，我们将讨论 `SSCM` 提供的与加密相关的其他选项。您将看到如何使用 `SSCM` 中两个基本功能的示例：

- 密钥生成器——用于生成哈希和加密算法密钥的对象
- 加密器——用于加密和解密数据的对象

### 使用密钥生成器

在本节中，我们讨论密钥生成器。密钥生成器是一种用于生成特定类型密钥的对象，通常用于加密或哈希算法。Spring Security
提供的密钥生成器实现是非常实用的工具。你会更愿意使用这些实现，而不是为你的应用程序添加其他依赖项，这也是我建议你熟悉它们的原因。让我们来看一些如何创建和应用密钥生成器的代码示例。

两个接口代表了两种主要类型的密钥生成器：`BytesKeyGenerator` 和 `StringKeyGenerator`。我们可以通过使用工厂类
`KeyGenerators` 直接构建它们。你可以使用由 `StringKeyGenerator` 合约表示的字符串密钥生成器来获取一个字符串形式的密钥。通常，我们将这个密钥用作
`哈希或加密算法的盐值`。你可以在以下代码片段中找到 `StringKeyGenerator` 合约的定义：

``` java
public interface StringKeyGenerator {

	String generateKey();

}
```

生成器只有一个generateKey()方法，该方法返回一个表示键值的字符串。下面的代码片段展示了如何获取一个`StringKeyGenerator`
实例以及如何使用它来获取盐值：

``` java
StringKeyGenerator keyGenerator = KeyGenerators.string();
String salt = keyGenerator.generateKey();
```

生成器创建一个8字节的密钥，并将其编码为十六进制字符串。该方法将这些操作的结果作为字符串返回。
`描述密钥生成器的第二个接口是BytesKeyGenerator`，其定义如下：

``` java
public interface BytesKeyGenerator {

	int getKeyLength();

	byte[] generateKey();

}
```

除了返回字节数组形式密钥的 `generateKey()`方法外，该接口还定义了另一个方法，用于返回密钥的字节长度。默认的
`BytesKeyGenerator` 生成的密钥长度为 8 字节：

``` java
BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom();
byte[] key = keyGenerator.generateKey();
int keyLength = keyGenerator.getKeyLength();
```

在之前的代码片段中，密钥生成器生成的是8字节长度的密钥。如果你想指定不同的密钥长度，可以在获取密钥生成器实例时，通过向
`KeyGenerators.secureRandom()`方法提供所需的值来实现。

``` java
BytesKeyGenerator keyGenerator = KeyGenerators.secureRandom(16);
```

通过使用 `KeyGenerators.secureRandom()` 方法创建的 `BytesKeyGenerator` 所生成的密钥在每次调用 `generateKey()`
方法时都是唯一的。在某些情况下，我们更倾向于实现一个在每次调用同一个密钥生成器时返回相同密钥值的方案。在这种情况下，我们可以使用
`KeyGenerators.shared(int length)` 方法创建一个 `BytesKeyGenerator`。在这个代码片段中，key1 和 key2 具有相同的值：

``` java
BytesKeyGenerator keyGenerator = KeyGenerators.shared(16);
byte[] key1 = keyGenerator.generateKey();
byte[] key2 = keyGenerator.generateKey();
```

### 使用加密器加密和解密机密信息

在本节中，我们通过代码示例应用了 Spring Security 提供的加密器实现。加密器是实现加密算法的对象。在谈到安全性时，加密和解密是常见的操作，因此可以预期在您的应用程序中需要这些功能。

我们经常需要在系统组件之间传输数据或保存数据时对其进行加密。加密器提供的操作包括加密和解密。`SSCM`定义了两种类型的加密器：
`BytesEncryptor`和`TextEncryptor`。虽然它们的职责相似，但处理的数据类型不同。`TextEncryptor`
将数据作为字符串处理。其方法接收字符串作为输入，并返回字符串作为输出，正如您可以从其接口定义中看到的那样：

``` java
public interface TextEncryptor {

	String encrypt(String text);

	String decrypt(String encryptedText);

}
```

`BytesEncryptor` 更加通用。您可以将输入数据以字节数组的形式提供。

``` java
public interface BytesEncryptor {

	byte[] encrypt(byte[] byteArray);

	byte[] decrypt(byte[] encryptedByteArray);

}
```

让我们来看看有哪些选项可以用来构建和使用加密器。`Encryptors` 工厂类为我们提供了多种可能性。对于 `BytesEncryptor`，我们可以使用
`Encryptors.standard()` 或 `Encryptors.stronger()` 方法，如下所示：

``` java
String salt = KeyGenerators.string().generateKey();
String password = "secret";
String valueToEncrypt = "HELLO";

BytesEncryptor e = Encryptors.standard(password, salt);
byte[] encrypted = e.encrypt(valueToEncrypt.getBytes());
byte[] decrypted = e.decrypt(encrypted); 
```

在幕后，标准字节加密器使用`256字节的AES加密`来加密输入。要构建一个更强大的字节加密器实例，可以调用`Encryptors.stronger()`
方法：

``` java
BytesEncryptor e = Encryptors.stronger(password, salt);
```

区别在于细节，AES 256位加密在幕后使用了Galois/计数器模式（GCM）作为操作模式。标准模式使用的是密码分组链接（CBC），被认为是一种较弱的方法。

文本加密器主要有三种类型。您可以通过调用`Encryptors.text()`或`Encryptors.delux()`
来创建这三种类型的加密器。除了这些创建加密器的方法之外，还有一种方法可以返回一个不进行加密的虚拟文本加密器。您可以在演示示例中使用这个虚拟文本加密器，或者在您希望测试应用程序性能而不想花时间进行加密的情况下使用。返回这种无操作加密器的方法是
`Encryptors.noOpText()`。在下面的代码片段中，您将看到使用文本加密器的示例。即使在示例中调用了加密器，`encrypted`和
`valueToEncrypt`的值仍然相同：

``` java
String valueToEncrypt = "HELLO";
TextEncryptor e = Encryptors.noOpText();
String encrypted = e.encrypt(valueToEncrypt);
```

`Encryptors.text()` 加密器使用 `Encryptors.standard()` 方法来管理加密操作，而 `Encryptors.delux()` 方法则使用
`Encryptors.stronger()` 实例，如下所示：

``` java
String salt = KeyGenerators.string().generateKey();
String password = "secret";
String valueToEncrypt = "HELLO";

TextEncryptor e = Encryptors.text(password, salt);
String encrypted = e.encrypt(valueToEncrypt);
String decrypted = e.decrypt(encrypted);
```

## 小结

- PasswordEncoder在认证逻辑中承担着最关键的职责之一——处理密码。
- Spring Security 提供了多种哈希算法选择，使得实现变得只是一个选择的问题。
- Spring Security Crypto模块（SSCM）为密钥生成器和加密器的实现提供了多种选择。
- 密钥生成器是用于帮助生成加密算法所需密钥的实用工具。
- 加密器是实用工具对象，帮助您进行数据加密和解密。
