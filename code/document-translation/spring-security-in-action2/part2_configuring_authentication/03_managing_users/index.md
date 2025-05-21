# 管理用户

我大学的一位同事厨艺相当不错。他不是高档餐厅的厨师，但对烹饪充满热情。有一天，在一次讨论中分享想法时，我问他是如何记住那么多食谱的。他告诉我，这很简单。
`你不需要记住整个食谱，只要记住基本食材如何搭配就行了。这就像一些现实世界的契约，告诉你什么可以混合，什么不应该混合。然后对于每个食谱，你只需要记住一些小技巧。`

这种类比类似于架构的运作方式。在任何稳健的框架中，我们使用契约来将框架的实现与其上构建的应用程序解耦。在 Java
中，我们使用接口来定义契约。程序员就像厨师，了解各种材料如何协同工作，从而选择最合适的实现。程序员了解框架的抽象，并利用这些抽象与框架进行集成。

本章将详细介绍您在第2章的第一个示例中遇到的一个基本角色——UserDetailsService。除了UserDetailsService，我们还将讨论以下接口（契约）：

- `UserDetails`，用于描述 Spring Security 的用户。
- `GrantedAuthority`，允许我们定义用户可以执行的操作。
- `UserDetailsManager`，扩展了 UserDetailsService 的契约。除了继承的行为外，它还描述了创建用户以及修改或删除用户密码等操作。

从第二章开始，您已经对`UserDetailsService`和`PasswordEncoder`在认证过程中所扮演的角色有了一定的了解。但我们只讨论了如何插入您自定义的实例，而不是使用Spring
Boot配置的默认实例。我们还有更多细节需要讨论，例如：

- Spring Security 提供的实现及其使用方法
- 如何定义合约的自定义实现以及何时进行
- 在实际应用中实现接口的方法
- 使用这些接口的最佳实践

计划首先介绍 Spring Security 如何理解`用户定义`。为此，我们将讨论 `UserDetails` 和 `GrantedAuthority` 接口。接下来，我们将详细介绍
`UserDetailsService` 以及 `UserDetailsManager` 如何扩展该接口。您将应用这些接口的实现（例如，`InMemoryUserDetailsManager`、
`JdbcUserDetailsManager` 和 `LdapUserDetailsManager`）。当这些实现不适合您的系统时，您将编写一个自定义实现。

## 思维导图

<figure markdown="span">
  ![](https://edrawcloudpubliccn.oss-cn-shenzhen.aliyuncs.com/viewer/self/1059758/share/2024-9-24/1727141086/main.svg){ loading=lazy }
  <figcaption>用户管理思维导图</figcaption>
</figure>

## 在Spring Security中实现身份验证

在上一章中，我们开始学习Spring Security。在第一个例子中，我们讨论了Spring
Boot如何指定一些默认设置来定义新应用程序的初始工作方式。你还了解到如何使用我们常在应用中找到的各种替代方案来覆盖这些默认设置。然而，我们只是粗略地介绍了一下这些内容，以便你对我们将要做的事情有个大概的了解。在本章以及第4章和第5章中，我们将更详细地讨论这些接口，以及不同的实现方式，并探讨在实际应用中可能会在哪里找到它们。

图3.1展示了Spring Security中的认证流程。这个架构是Spring Security实现的认证过程的核心。理解它很重要，因为在任何Spring
Security的实现中你都会依赖它。你会注意到，我们几乎在本书的每一章中都会讨论这个架构的部分内容。你会如此频繁地看到它，以至于可能会背下来，这是一件好事。如果你了解这个架构，就像一个厨师了解自己的食材，可以组合出任何食谱。

在图3.1中，阴影框表示我们开始使用的组件：`UserDetailsService`和`PasswordEncoder`。这两个组件专注于我常称为`用户管理部分`
的流程。在本章中，`UserDetailsService`和`PasswordEncoder`是直接处理用户详细信息及其凭据的组件。我们将在第4章详细讨论PasswordEncoder。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409131639582.png){ loading=lazy }
  <figcaption>图3.1 Spring Security的认证流程。AuthenticationFilter捕获传入请求，并将认证任务交给AuthenticationManager。AuthenticationManager则使用认证提供者来执行认证过程。为了验证用户名和密码，认证提供者依赖于UserDetailsService和PasswordEncoder。</figcaption>
</figure>

在用户管理中，我们使用了 `UserDetailsService` 和 `UserDetailsManager` 接口。`UserDetailsService`
仅负责通过用户名检索用户，这是框架完成身份验证所需的唯一操作。`UserDetailsManager` 则增加了`添加`、`修改`或`删除用户`
的功能，这在大多数应用程序中是必需的功能。将这两个接口分开是接口隔离原则的一个优秀例子。接口的分离提供了更好的灵活性，因为框架不会强迫你在应用程序不需要的情况下实现某些行为。如果应用程序只需要进行
`用户身份验证`，那么实现 `UserDetailsService` 接口就足够满足所需功能。为了管理用户，`UserDetailsService` 和
`UserDetailsManager` 组件需要一种方式来`表示用户`。

Spring Security 提供了 `UserDetails` 接口，你必须实现它以便用框架能够理解的方式描述用户。正如你将在本章中了解到的，在
Spring Security 中，用户拥有一组权限，即用户被允许执行的操作。在第 7 到第 12 章中讨论授权时，我们将大量涉及这些权限。但目前，Spring
Security 使用 `GrantedAuthority` 接口来表示用户`可以执行的操作`。我们通常称这些为`权限`，一个用户可以拥有一个或多个。在图
3.2 中，你可以看到用户管理部分在认证流程中各组件之间关系的表示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409131647193.png){ loading=lazy }
  <figcaption>图3.2 用户管理中各组件之间的依赖关系。UserDetailsService通过用户名查找用户来获取用户详细信息。用户由UserDetails契约描述。每个用户拥有一个或多个权限，这些权限由GrantedAuthority接口表示。为了实现创建、删除或修改用户密码等操作，使用扩展了UserDetailsService的UserDetailsManager契约来包含这些功能。</figcaption>
</figure>

在Spring
Security架构中理解这些对象之间的联系以及实现它们的方法，可以为您在开发应用程序时提供多种选择。这些选项中的任何一个都可能是您正在开发的应用程序中合适的拼图，因此您需要明智地做出选择。但要能够做出选择，首先需要了解有哪些可供选择的选项。

## 描述用户

在本节中，您将学习如何描述应用程序的`用户`，以便 Spring Security 能够理解他们。学习如何表示`用户`并让框架`识别他们`
是构建认证流程的关键步骤。应用程序会根据用户做出决策——`是否允许调用某个功能`。要处理用户，首先需要了解如何在应用程序中定义用户的原型。本节通过示例介绍如何在
Spring Security 应用程序中为用户建立一个蓝图。

对于 Spring Security，用户定义应满足 `UserDetails` 合约。`UserDetails` 合约代表 Spring Security
所理解的用户。应用程序中描述用户的类必须实现此接口，这样框架才能理解它。

### 使用UserDetails合约描述用户

在本节中，您将学习如何实现UserDetails接口，以描述应用程序中的用户。我们将讨论UserDetails契约中声明的方法，以理解我们如何以及为何实现每一个方法。首先，让我们来看一下下面列出的接口。

``` java title="清单 3.1 UserDetails 接口"

package org.springframework.security.core.userdetails;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public interface UserDetails extends Serializable {

	Collection<? extends GrantedAuthority> getAuthorities();

	String getPassword();

	String getUsername();

	default boolean isAccountNonExpired() {
		return true;
	}

	default boolean isAccountNonLocked() {
		return true;
	}

	default boolean isCredentialsNonExpired() {
		return true;
	}

	default boolean isEnabled() {
		return true;
	}

}
```

`getUsername()` 和 `getPassword()` 方法如预期地返回用户名和密码。应用程序在身份验证过程中使用这些值，并且这些是此合同中唯一与身份验证相关的详细信息。其他五个方法都与授权用户访问应用程序的资源有关。

通常，应用程序应允许`用户`执行一些在应用程序上下文中有意义的操作。例如，用户应该能够`读取、写入或删除数据`
。我们说用户是否有权限执行某个操作，而权限代表了用户拥有的特权。我们实现了`getAuthorities()`
方法来返回授予用户的一组权限。

!!! note

	正如您将在第6章中了解到的，Spring Security使用权限来指代细粒度的特权或角色（即特权的集合）。为了让您的阅读更加轻松，在本书中，我将细粒度的特权称为`权限`。

此外，如在UserDetails合约中所见，用户可以:

- 让账户过期
- 锁定账户
- 让凭证过期
- 禁用账户

假设您选择在应用程序的逻辑中实施这些用户限制。在这种情况下，您需要实现方法 `isAccountNonExpired()`、`isAccountNonLocked()`、
`isCredentialsNonExpired()` 和 `isEnabled()`，以便那些需要启用的返回
true。并不是所有应用程序的账户都会在某些条件下过期或被锁定。如果您不需要在应用程序中实现这些功能，可以简单地让这四个方法返回
true。

!!! note

	`UserDetails`接口中最后四个方法的名称可能听起来有些奇怪。有人可能会认为，从代码整洁性和可维护性的角度来看，这些名称选择得不够明智。例如，`isAccountNonExpired()`这个名称看起来像是双重否定，乍一看可能会引起混淆。但让我们仔细分析这四个方法的名称。它们被命名为在授权失败时返回false，否则返回true。这是一种正确的方法，因为人们通常会将“false”与负面联系在一起，而将“true”与正面情境联系在一起。

### 关于GrantedAuthority合同的详细说明

正如您在第3.2.1节中看到的`UserDetails`接口定义中，授予用户的操作被称为`权限`
。在第7到第12章中，我们将基于这些用户权限编写授权配置。因此，了解如何定义它们是至关重要的。

`权限`
代表用户在您的应用程序中可以执行的操作。没有权限，所有用户将是平等的。虽然有些简单的应用程序中用户是平等的，但在大多数实际情况下，一个应用程序会定义多种类型的用户。有些用户可能只能读取特定信息，而其他用户则可以修改数据。您需要根据应用程序的功能需求来区分这些用户，这些需求就是用户所需的权限。在
Spring Security 中，您可以使用 `GrantedAuthority` 接口来描述权限。

在讨论实现 `UserDetails` 之前，让我们先了解一下 `GrantedAuthority` 接口。我们在定义用户详细信息时使用这个接口，它代表授予用户的
`权限`。用户必须`至少拥有一个权限`。以下是 `GrantedAuthority` 定义的实现：

``` java
public interface GrantedAuthority extends Serializable {

	/*
	 * 如果GrantedAuthority可以表示为String，
	 * 并且该String的精度足以供AccessDecisionManager（或委托人）在访问控制决策中依赖，
	 * 则该方法应返回这样的String。
	 *
	 * 如果GrantedAuthority不能以足够的精度表示为String，
	 * 则应返回null。返回null将要求AccessDecisionManager（或委托人）专门支持GrantedAuthority的实现，
	 * 因此除非确实需要，否则应避免返回null。
	 *
	 * 返回值: 授予权限的表示（如果授予权限无法以足够的精度表示为String，则为null）
	 * */
	String getAuthority();

}
```

要创建一个`权限`，您只需为该特权找到一个`名称`，以便在编写授权规则时可以引用它。例如，用户可以读取或删除应用程序管理的记录。您根据为这些操作指定的名称来编写授权规则。

在本章中，我们将实现 `getAuthority()` 方法，以返回权限名称作为字符串。`GrantedAuthority` 接口只有一个抽象方法，在本书中，你会经常看到我们使用
lambda 表达式来实现它的例子。另一种方法是使用 `SimpleGrantedAuthority` 类来创建权限实例。`SimpleGrantedAuthority`
类提供了一种创建不可变 `GrantedAuthority` 实例的方法。构建实例时，你需要提供权限名称。在接下来的代码片段中，你会看到两个实现
`GrantedAuthority` 的例子。我们首先使用 lambda 表达式，然后使用 `SimpleGrantedAuthority` 类：

``` java
GrantedAuthority g1 = () -> "READ";
GrantedAuthority g2 = new SimpleGrantedAuthority("READ");
```

### 编写一个简化版的UserDetails实现

在本节中，您将编写`UserDetails`合约的第一个实现。我们从一个基本实现开始，其中每个方法返回一个静态值。然后，我们将其更改为一个更实用的版本，该版本允许您拥有多个不同的用户实例。现在您已经知道如何实现
`UserDetails`和`GrantedAuthority`接口，我们可以为应用程序编写最简单的用户定义。

使用一个名为 `DummyUser` 的类，我们来实现一个用户的简要描述，如下所示。我主要使用这个类来演示如何实现 UserDetails
合约的方法。这个类的实例总是指向一个用户，即`bill`，他的密码是 `12345`，并拥有一个名为 `READ` 的权限。

``` java title="代码清单 3.2 DummyUser 类"
public class DummyUser implements UserDetails {

	@Override
	public String getUsername() {
		return "bill";
	}

	@Override
	public String getPassword() {
		return "12345";
	}

	// Omitted code

}
```

清单3.2中的类实现了`UserDetails`接口，并需要实现其所有方法。在这里，你会看到`getUsername()`和`getPassword()`
方法的实现。在这个例子中，这些方法仅为每个属性返回一个固定值。

接下来，我们为权限列表添加一个定义。下面的代码展示了`getAuthorities()`方法的实现。该方法返回一个仅包含`GrantedAuthority`
接口单一实现的集合。

``` java title="清单 3.3 getAuthorities() 方法的实现"
public class DummyUser implements UserDetails {

	// Omitted code

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> "READ");
	}

	// Omitted code

}
```

最后，您需要为 UserDetails 接口的最后四个方法添加实现。对于 DummyUser 类，这些方法始终返回
true，表示用户始终处于活跃状态且可用。您可以在以下列表中找到示例。

``` java
public class DummyUser implements UserDetails {

	// Omitted code

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	// Omitted code

}
```

当然，这种最小化的实现意味着该类的所有实例都代表同一个用户。这是理解契约的一个良好开端，但在实际应用中你不会这样做。对于实际应用，你应该创建一个类，用于生成可以代表不同用户的实例。在这种情况下，你的定义至少应该在类中包含用户名和密码作为属性，如下一个列表所示。

``` java title="清单 3.5 UserDetails 接口的更实用实现"
public class SimpleUser implements UserDetails {

	private final String username;
	private final String password;

	public SimpleUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	// Omitted code

}
```

### 使用构建器创建UserDetails类型的实例

某些应用程序比较简单，不需要自定义实现 UserDetails 接口。在本节中，我们将了解如何使用 Spring Security
提供的构建器类来创建简单的用户实例。通过使用 User 构建器类(建造者设计模式)，您可以快速获得一个代表用户的实例，而无需在应用程序中声明更多的类。

`org.springframework.security.core.userdetails` 包中的 `User` 类是构建 `UserDetails` 类型实例的一种简单方式。使用这个类，你可以创建
`不可变的 UserDetails 实例`。你至少需要提供一个`用户名`和`密码`，并且用户名不能为空字符串。以下示例展示了如何使用这个构建器。通过这种方式构建用户，你不需要自定义实现
`UserDetails` 合约。

``` java title="清单 3.6 使用 User 构建器类构建用户"
UserDetails u = User.withUsername("bill")
		.password("12345")
		.authorities("read", "write")
		.accountExpired(false)
		.disabled(true)
		.build();
```

以之前的示例为例，让我们更深入地了解 `User` 构建器类的结构。`User.withUsername(String username)` 方法返回一个嵌套在 User
类中的 `UserBuilder` 类的实例。创建构建器的另一种方法是从另一个 `UserDetails` 实例开始。在示例 3.7 中，第一行通过给定的字符串用户名构建了一个
`UserBuilder`。随后，我们展示了如何从一个已存在的 `UserDetails` 实例开始创建构建器。

``` java title="清单 3.7 创建 User.UserBuilder 实例"
User.UserBuilder builder1 = User.withUsername("bill");

UserDetails u1 = builder1
		.password("12345")
		.authorities("read", "write")
		.passwordEncoder(p -> encode(p))
		.accountExpired(false)
		.disabled(true)
		.build();

User.UserBuilder builder2 = User.withUserDetails(u);

UserDetails u2 = builder2.build();

```

在列表3.7中定义的任何构建器中，你都可以使用构建器来获取由`UserDetails`契约表示的用户。在构建流程的最后，你调用`build()`
方法。如果你提供了密码，它会应用定义的函数来对密码进行编码，构建`UserDetails`实例并返回它。

!!! note

	请注意，这里的密码编码器是作为一个 `Function<String, String> `提供的，而不是以 Spring Security 提供的 `PasswordEncoder` 接口的形式提供的。这个函数的唯一职责是将密码转换为给定的编码。在下一节中，我们将详细讨论我们在第2章中使用的 Spring Security 的 `PasswordEncoder` 合约。我们将在第4章中更详细地讨论 `PasswordEncoder` 合约。

### 结合与用户相关的多项职责

在上一节中，您学习了如何实现 `UserDetails`
接口。在实际场景中，这通常更为复杂。在大多数情况下，用户会涉及多个职责。如果您将用户存储在数据库中，那么在应用程序中，您还需要一个类来表示持久化实体。或者，如果您通过网络服务从其他系统中获取用户，那么您可能需要一个数据传输对象来表示用户实例。假设是第一种情况，一个简单但也典型的例子，我们在一个
SQL 数据库中有一个表来存储用户。为了简化示例，我们给每个用户只分配一个权限。以下是映射该表的实体类。

``` java title="清单 3.8 定义 JPA 用户实体类"

@Entity
public class User {

	@Id
	private Long id;
	private String username;
	private String password;
	private String authority;

	// Omitted getters and setters

}
```

如果让同一个类也实现 Spring Security 的用户详情契约，这个类就会变得更加复杂。你觉得下面的代码看起来怎么样？在我看来，这是一团糟，我会在其中迷失。

``` java title="❌清单 3.9 User 类具有两个职责"

@Entity
public class User implements UserDetails {

	@Id
	private int id;
	private String username;
	private String password;
	private String authority;

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	public String getAuthority() {
		return this.authority;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> authority);
	}

	// Omitted code

}
```

该类包含`JPA注解`、`getter`和`setter`，其中`getUsername()`和`getPassword()`都重写了`UserDetails`契约中的方法。它有一个返回字符串的
`getAuthority()`方法，以及一个返回集合的`getAuthorities()`方法。`getAuthority()`方法只是类中的一个`getter`，而
`getAuthorities()`实现了`UserDetails`接口中的方法。当添加与其他实体的关系时，情况变得更加复杂。总之，这段代码一点也不友好！

我们如何编写更简洁的代码？之前代码示例中混乱的根源在于混合了两种职责。虽然在应用程序中确实需要这两者，但没有人说必须将它们放在同一个类中。让我们尝试通过定义一个名为
`SecurityUser` 的独立类来分离这些职责，该类适配 User 类。正如下一个列表所示，`SecurityUser` 类实现了 `UserDetails`
接口，并利用它将我们的用户接入 Spring Security 架构。`User` 类则仅保留其 JPA 实体的职责。

``` java title="清单3.10 仅将User类实现为JPA实体"

@Entity
public class User {

	@Id
	private int id;
	private String username;
	private String password;
	private String authority;

	// Omitted getters and setters

}
```

在代码清单3.10中，User类仅保留了其JPA实体的职责，因此变得更加易读。阅读这段代码时，您现在可以专注于与持久化相关的细节，而这些细节从Spring
Security的角度来看并不重要。在下一个清单中，我们将实现SecurityUser类来封装User实体。

``` java title="代码清单 3.11 实现 UserDetails 合约的 SecurityUser 类"
public class SecurityUser implements UserDetails {

	private final User user;

	public SecurityUser(User user) {
		this.user = user;
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> user.getAuthority());
	}

	// Omitted code

}
```

正如您所看到的，我们仅使用 `SecurityUser` 类将系统中的用户详细信息映射到 Spring Security 理解的 `UserDetails` 合约。为了表明没有
`User` 实体，`SecurityUser` 就没有意义，我们将该字段设为 `final`。您必须通过构造函数提供用户。SecurityUser 类适配了 User
实体类，并添加了与 Spring Security 合约相关的必要代码，而不将代码混入 JPA 实体中，从而实现多种不同的任务。

!!! note

	您可以找到不同的方法来分离这两种职责。我并不想说我在本节中介绍的方法是最好的或唯一的方法。通常，您选择实现类设计的方式会因具体情况而有所不同。但主要思想是相同的：避免混合职责，尽量编写尽可能解耦的代码，以提高应用程序的可维护性。

## 指导 Spring Security 如何管理用户

在上一节中，您实现了`UserDetails`合约来描述用户，以便Spring Security能够理解他们。但Spring
Security是如何管理用户的呢？在比较凭证时，它们是从哪里获取的，又如何添加新用户或更改现有用户？在第二章中，您了解到框架定义了一个特定组件，身份验证过程将用户管理委托给这个组件：
`UserDetailsService`实例。我们甚至定义了一个`UserDetailsService`来覆盖Spring Boot提供的默认实现。

在本节中，我们将尝试多种实现 `UserDetailsService` 类的方法。通过实现 `UserDetailsService` 合约中描述的职责，您将了解用户管理的工作原理。之后，您将了解到
`UserDetailsManager` 接口如何为 `UserDetailsService` 定义的合约增加更多功能。在本节的最后，我们将使用 Spring Security 提供的
`UserDetailsManager` 接口的实现。我们将编写一个示例项目，使用 Spring Security 提供的最知名实现之一——
`JdbcUserDetailsManager` 类。学习完这些后，您将知道如何告诉 Spring Security `在哪里查找用户`，这在认证流程中至关重要。

### 理解 UserDetailsService 合约

在本节中，您将了解`UserDetailsService`接口的定义。在理解如何以及为何实现它之前，您必须首先了解其约定。现在是深入探讨
`UserDetailsService`以及如何使用该组件的实现的时候了。`UserDetailsService`接口仅包含一个方法，如下所示：

``` java
public interface UserDetailsService {

	UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException;
}
```

身份验证实现通过调用`loadUserByUsername(String username)`方法来获取具有给定用户名的用户详细信息（图3.3）。当然，用户名被视为唯一。该方法返回的用户是
`UserDetails`契约的一个实现。如果用户名不存在，该方法会抛出`UsernameNotFoundException`异常。

!!! note

	`UsernameNotFoundException` 是一种运行时异常。`UserDetailsService` 接口中的 throws 子句仅用于文档目的。`UsernameNotFoundException` 直接继承自 `AuthenticationException` 类型，而 `AuthenticationException` 是所有与身份验证过程相关异常的父类。`AuthenticationException` 进一步继承自 `RuntimeException` 类。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409221633179.png){ loading=lazy }
  <figcaption>图 3.3 AuthenticationProvider 是负责执行认证过程的元素，并利用 UserDetailsService 来收集用户详细信息。它通过调用 loadUserByUsername(String username) 方法，根据用户名查找用户。</figcaption>
</figure>

### 实现UserDetailsService契约

在本节中，我们通过 ssia-ch3-ex1[^1] 实际示例来演示如何实现`UserDetailsService`。您的应用程序管理有关`凭证`
和其他用户方面的详细信息。这些信息可能存储在数据库中，或者通过您通过网络服务或其他方式访问的系统进行处理（图3.3）。无论在您的系统中如何实现，Spring
Security唯一需要您提供的就是一个`通过用户名检索用户的实现`。

[^1]: ssia-ch3-ex1:自己实现UserDetailsService

在下一个例子中，我们编写了一个包含内存用户列表的`UserDetailsService`。在第2章中，你使用了一个提供的实现——
`InMemoryUserDetailsManager`，它实现了相同的功能。由于你已经熟悉这个实现的工作原理，我选择了类似的功能，但这次我们自己来实现。当我们创建
`UserDetailsService`类的实例时，我们提供一个用户列表。你可以在项目`ssia-ch3-ex1`中找到这个例子。在名为model的包中，我们定义了如下所示的
`UserDetails`。

``` java title="清单 3.12 UserDetails 接口的实现"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/03_managing_users/ssia-ch3-ex1/src/main/java/com/luguosong/ssiach3ex1/model/User.java"
```

在名为 services 的包中，我们创建了一个名为 `InMemoryUserDetailsService` 的类。以下是我们实现该类的方法。

``` java title="清单 3.13 UserDetailsService 接口的实现"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/03_managing_users/ssia-ch3-ex1/src/main/java/com/luguosong/ssiach3ex1/services/InMemoryUserDetailsService.java"
```

`loadUserByUsername(String username)` 方法在用户列表中搜索给定的用户名，并返回所需的 `UserDetails` 实例。如果没有找到该用户名的实例，则抛出
`UsernameNotFoundException`。我们现在可以将此实现用作我们的 `UserDetailsService`。下面的代码展示了如何在配置类中将其添加为一个
bean，并在其中注册一个用户。

``` java title="代码清单 3.14 在配置类中将 UserDetailsService 注册为一个 Bean"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/03_managing_users/ssia-ch3-ex1/src/main/java/com/luguosong/ssiach3ex1/config/ProjectConfig.java"
```

最后，我们创建一个简单的端点并测试其实现。以下列表定义了该端点。

``` java title="清单 3.15 用于测试实现的端点定义"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/03_managing_users/ssia-ch3-ex1/src/main/java/com/luguosong/ssiach3ex1/controller/HelloController.java"
```

使用 cURL 调用端点时，我们注意到，对于用户名为 john、密码为 12345 的用户，我们收到 HTTP 200 OK 的响应。如果使用其他信息，应用程序则返回
401 未授权：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应正文是

```shell
Hello!
```

### 实现UserDetailsManager合约

在本节中，我们将讨论如何使用和实现`UserDetailsManager`接口。该接口扩展了`UserDetailsService`合同，并增加了更多方法。Spring
Security需要`UserDetailsService`合同来进行身份验证。但通常在应用程序中，也需要管理用户。大多数情况下，应用程序应该能够添加新用户或删除现有用户。在这种情况下，我们实现了由Spring
Security定义的更具体的接口`UserDetailsManager`。它扩展了`UserDetailsService`，并增加了我们需要实现的更多操作：

``` java
public interface UserDetailsManager extends UserDetailsService {
	void createUser(UserDetails user);

	void updateUser(UserDetails user);

	void deleteUser(String username);

	void changePassword(String oldPassword, String newPassword);

	boolean userExists(String username);
}
```

我们在第2章中使用的`InMemoryUserDetailsManager`对象实际上是一个`UserDetailsManager`。当时，我们只考虑了它的
`UserDetailsService`特性。项目`ssia-ch2-ex2`与本节的示例相配套。

#### JDBCUserDetailsManager用户管理

除了 `InMemoryUserDetailsManager`，我们经常使用另一个 `UserDetailsManager` 的实现，即 `JdbcUserDetailsManager`。
`JdbcUserDetailsManager` 类在 SQL 数据库中管理用户。它通过 JDBC 直接连接到数据库。这样，`JdbcUserDetailsManager`
就不依赖于任何其他与数据库连接相关的框架或规范。

要了解 `JdbcUserDetailsManager` 的工作原理，最好通过一个示例来实践。在下面的示例中，您将实现一个应用程序，该应用程序使用
`JdbcUserDetailsManager` 管理 MySQL 数据库中的用户。图 3.4 概述了 `JdbcUserDetailsManager` 实现在认证流程中的位置。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409231409182.png){ loading=lazy }
  <figcaption>图 3.4 Spring Security 认证流程。这里我们使用 JdbcUserDetailsManager 作为我们的 UserDetailsService 组件。JdbcUserDetailsManager 使用数据库来管理用户。</figcaption>
</figure>

您将开始使用我们的演示应用程序，该应用程序通过创建一个数据库和两个表来使用`JdbcUserDetailsManager`。在我们的例子中，我们将数据库命名为
`spring`，其中一个表命名为`users`，另一个命名为`authorities`。这些名称是`JdbcUserDetailsManager`默认识别的表名。正如您将在本节末尾了解到的，
`JdbcUserDetailsManager`的实现是灵活的，您可以根据需要覆盖这些默认名称。`users`表的目的是存储用户记录。
`JdbcUserDetailsManager`实现期望users表中有三列——`用户名`、`密码`和`启用状态`，您可以利用这些列来停用用户。

您可以选择通过使用`数据库管理系统（DBMS）`的命令行工具或客户端应用程序自行创建数据库及其结构。例如，对于 MySQL，您可以选择使用
MySQL Workbench 来完成这项工作。但最简单的方法是让 Spring Boot 自行运行脚本。为此，只需在项目的 resources 文件夹中添加两个文件：
`schema.sql` 和 `data.sql`。在 `schema.sql` 文件中，添加与`数据库结构相关`的查询，例如创建、修改或删除表。在 `data.sql`
文件中，添加`处理表内数据`的查询，例如 INSERT、UPDATE 或 DELETE。Spring Boot 会在您启动应用程序时自动为您运行这些文件。对于需要数据库的示例，使用
`H2 内存数据库`是一个更简单的解决方案。这样，您无需安装单独的 DBMS 解决方案。

!!! note

	如果你愿意，在开发本书中介绍的应用程序时，也可以选择使用 H2（就像我在 ssia-ch3-ex2 项目中所做的那样）。但在大多数情况下，我选择使用外部 DBMS 来实现这些示例，以明确它是系统的一个外部组件，从而避免混淆。

您可以使用下面的代码在 MySQL 服务器中创建用户表。您可以将此脚本添加到 Spring Boot 项目的 `schema.sql` 文件中。

```sql title="清单 3.16 创建用户表的 SQL 查询"
CREATE TABLE IF NOT EXISTS `spring`.`users`
(
    `id`       INT         NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(45) NOT NULL,
    `password` VARCHAR(45) NOT NULL,
    `enabled`  INT         NOT NULL,
    PRIMARY KEY (`id`)
);
```

权限表存储每个用户的权限。每条记录存储一个用户名以及授予该用户名用户的权限。

```sql title="清单 3.17 创建 authorities 表的 SQL 查询"
CREATE TABLE IF NOT EXISTS `spring`.`authorities`
(
    `id`        INT         NOT NULL AUTO_INCREMENT,
    `username`  VARCHAR(45) NOT NULL,
    `authority` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`id`)
);
```

!!! note

	为了简化并让您专注于我们讨论的 Spring Security 配置，在本书提供的示例中，我省略了索引或外键的定义。

为了确保您有一个用于测试的用户，请在每个表中插入一条记录。您可以将这些查询添加到 Spring Boot 项目资源文件夹中的 `data.sql`
文件中：

```sql
INSERT INTO `spring`.`authorities`
    (username, authority)
VALUES ('john', 'write');

INSERT INTO `spring`.`users`
    (username, password, enabled)
VALUES ('john', '12345', '1');
```

对于您的项目，您需要至少添加以下列表中所述的依赖项。请检查您的 pom.xml 文件以确保已添加这些依赖项。

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
    </dependency>
</dependencies>
```

!!! note

	在您的示例中，您可以使用任何 SQL 数据库技术，只需将正确的 JDBC 驱动程序添加到依赖项中即可。

请记住，您需要根据所使用的数据库技术添加相应的JDBC驱动程序。例如，如果您使用MySQL，则需要添加如下代码片段中所示的MySQL驱动程序依赖项。

```xml

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

您可以在项目的application.properties文件中配置数据源，也可以将其配置为一个独立的bean。如果选择使用application.properties文件，您需要在该文件中添加以下几行：

```properties
spring.datasource.url=jdbc:h2:mem:ssia
spring.datasource.username=sa
spring.datasource.password=
spring.sql.init.mode=always
```

在项目的配置类中，你需要定义 `UserDetailsService` 和 `PasswordEncoder`。`JdbcUserDetailsManager` 需要 `DataSource`
来连接数据库。数据源可以通过方法的参数自动装配（如下所示），也可以通过类的属性进行装配。

``` java title="清单 3.19 在配置类中注册 JdbcUserDetailsManager"

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService(DataSource dataSource) {
		return new JdbcUserDetailsManager(dataSource);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
```

要访问应用程序的任何端点，现在需要使用存储在数据库中的用户进行HTTP基本身份验证。为了证明这一点，我们创建了一个新的端点，如下所示，然后使用cURL调用它。

``` java title="清单 3.20 用于检查实现的测试端点"

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

在下一个代码片段中，您将看到使用正确的用户名和密码调用端点时的结果：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应为：

```shell
Hello!
```

`JdbcUserDetailsManager` 还允许您配置所使用的查询。在前面的例子中，我们确保使用了表和列的确切名称，因为
JdbcUserDetailsManager 的实现需要这些名称。但这些名称可能并不是您应用程序的最佳选择。下面的示例展示了如何覆盖
JdbcUserDetailsManager 的查询。

``` java

@Bean
public UserDetailsService userDetailsService(DataSource dataSource) {
	String usersByUsernameQuery =
			"select username, password, enabled from users where username = ?";
	String authsByUserQuery =
			"select username, authority from spring.authorities where username = ?";

	var userDetailsManager = new JdbcUserDetailsManager(dataSource);
	userDetailsManager.setUsersByUsernameQuery(usersByUsernameQuery);
	userDetailsManager.setAuthoritiesByUsernameQuery(authsByUserQuery);
	return userDetailsManager;
}
```

同样，我们可以更改 JdbcUserDetailsManager 实现中使用的所有查询。

!!! note "练习"

	为数据库中的表和列命名不同，编写一个类似的应用程序。重写JdbcUserDetailsManager实现的查询（例如，身份验证使用新的表结构）。项目ssia-ch3-ex2[^2]提供了一个可能的解决方案。

[^2]:ssia-ch3-ex2:JDBCUserDetailsManager用户管理

#### LDAPUserDetailsManager用户管理

LDAP（轻量级目录访问协议）通常被组织用作用户信息的中央存储库和身份验证服务。它还可以用于存储应用程序用户的角色信息。

Spring Security 还提供了一个用于 LDAP 的 `UserDetailsManager` 实现。即使它不如 `JdbcUserDetailsManager` 流行，但如果需要与
LDAP 系统集成进行用户管理，您可以依赖它。在项目 `ssia-ch3-ex3`[^3] 中，您可以找到使用 `LdapUserDetailsManager`
的简单示例。由于无法在此示例中使用真实的 `LDAP` 服务器，我在 Spring Boot 应用程序中设置了一个嵌入式的。为了设置嵌入式 LDAP
服务器，我定义了一个简单的 LDAP 数据交换格式`LDIF文件`。以下是我的 `LDIF` 文件的内容。

[^3]: ssia-ch3-ex3:LDAPUserDetailsManager用户管理

```ldif
dn: dc=springframework,dc=org
objectclass: top
objectclass: domain
objectclass: extensibleObject
dc: springframework

dn: ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: uid=john,ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
cn: John
sn: John
uid: john
userPassword: 12345
```

在LDIF文件中，我只添加了一个用户，以便在这个示例结束时测试应用程序的行为。我们可以将LDIF文件直接添加到资源文件夹中。这样，它会自动在类路径中，方便我们后续引用。我将LDIF文件命名为
`server.ldif`。要使用LDAP并允许Spring Boot启动嵌入式LDAP服务器，你需要在pom.xml中添加依赖项：

```xml

<dependencies>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-ldap</artifactId>
    </dependency>
    <dependency>
        <groupId>com.unboundid</groupId>
        <artifactId>unboundid-ldapsdk</artifactId>
    </dependency>
</dependencies>
```

在 `application.properties` 文件中，您还需要添加嵌入式 LDAP 服务器的配置，如以下代码片段所示。应用程序启动嵌入式 LDAP
服务器所需的值包括 LDIF 文件的位置、LDAP 服务器的端口以及基础域组件（DN）标签值：

```properties
spring.ldap.embedded.ldif=classpath:server.ldif
spring.ldap.embedded.base-dn=dc=springframework,dc=org
spring.ldap.embedded.port=33389
```

一旦拥有用于身份验证的LDAP服务器，就可以配置您的应用程序使用它。下面的列表展示了如何配置LdapUserDetailsManager，以使您的应用能够通过LDAP服务器进行用户身份验证。

``` java title="清单 3.23 配置文件中 LdapUserDetailsManager 的定义"

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var cs = new DefaultSpringSecurityContextSource(
				"ldap://127.0.0.1:33389/dc=springframework,dc=org");
		cs.afterPropertiesSet();

		var manager = new LdapUserDetailsManager(cs);

		manager.setUsernameMapper(
				new DefaultLdapUsernameToDnMapper("ou=groups", "uid"));

		manager.setGroupSearchBase("ou=groups");

		return manager;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}

```

让我们创建一个简单的端点来测试安全配置。我添加了一个控制器类，如下代码片段所示：

``` java

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

现在启动应用程序，并调用 /hello 端点。如果你希望应用程序允许你调用该端点，你需要使用用户 john 进行身份验证。下面的代码片段展示了使用
cURL 调用该端点的结果：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应结果为：

```shell
Hello!
```

## 小结

- UserDetails接口是用于在Spring Security中描述用户的契约。
- UserDetailsService接口是Spring Security期望您在认证架构中实现的契约，用于描述应用程序获取用户详细信息的方式。
- UserDetailsManager接口扩展了UserDetailsService，并增加了与创建、更改或删除用户相关的行为。
- Spring
  Security提供了一些UserDetailsManager契约的实现，其中包括InMemoryUserDetailsManager、JdbcUserDetailsManager和LdapUserDetailsManager。
- JdbcUserDetailsManager类的优势在于直接使用JDBC，不会将应用程序锁定在其他框架中。
