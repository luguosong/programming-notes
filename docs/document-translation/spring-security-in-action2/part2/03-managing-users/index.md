# 3.用户管理

本章内容包括：

- 使用 UserDetails 接口描述用户
- 在认证流程中使用 UserDetailsService
- 自定义实现 UserDetailsService
- 自定义实现 UserDetailsManager
- 在认证流程中使用 JdbcUserDetailsManager。

我有个大学同事，厨艺相当不错。虽然他不是高级餐厅的主厨，但对做饭非常有热情。有一次我们聊天时，我问他怎么能记住那么多菜谱。他说这很简单：“你不用记住完整的菜谱，只要记住基础食材之间的搭配方式就行了。这就像现实生活中的一些规则，告诉你哪些可以混合，哪些不能混合。至于每道菜，只需要记住一些小技巧就够了。”

这个比喻和架构的工作方式很相似。在任何健壮的框架中，我们都会通过契约来将框架的实现与其上构建的应用程序解耦。在 Java 里，我们用
`接口`来定义这些契约。程序员就像厨师一样，懂得如何将各种“食材”搭配在一起，从而选择最合适的实现。程序员熟悉框架的各种抽象，并利用这些抽象与框架进行集成。

本章将深入讲解你在第二章第一个示例中遇到的一个核心角色——`UserDetailsService`。除了UserDetailsService，我们还会讨论以下接口（契约）：

- `UserDetails`：用于描述Spring Security中的用户信息。
- `GrantedAuthority`：用于定义用户可以执行的操作权限。
- `UserDetailsManager`：继承自UserDetailsService契约，除了继承的行为外，还包括创建用户、修改或删除用户密码等操作。

在第二章中，你已经了解了UserDetailsService和PasswordEncoder在认证流程中的作用。但我们只讨论了如何使用你自定义的实例来替换Spring
Boot默认配置的实现。其实还有更多细节值得探讨，比如：

- Spring Security提供了哪些实现，以及如何使用它们
- 如何自定义契约的实现，以及在什么情况下需要这样做
- 在实际项目中如何实现这些接口
- 使用这些接口的最佳实践

我们的计划是先从Spring
Security如何理解用户定义开始，为此我们会先讲解UserDetails和GrantedAuthority这两个契约。接着，我们会详细介绍UserDetailsService，以及UserDetailsManager是如何扩展这一契约的。你将学习这些接口的具体实现方式（如InMemoryUserDetailsManager、JdbcUserDetailsManager和LdapUserDetailsManager）。如果这些实现不适合你的系统需求，我们还会讲解如何编写自定义实现。

## 在Spring Security中实现认证

在上一章中，我们初步了解了 Spring Security。在第一个例子里，我们讨论了 Spring Boot
如何设定一些默认配置，从而决定新应用最初的运行方式。你还学会了如何通过多种常见方式来覆盖这些默认设置，这些方式在实际应用中经常会用到。不过，我们之前只是简单介绍了一下这些内容，让你对接下来的学习有个大致的了解。在本章以及第
4 和第 5 章中，我们将更深入地探讨这些接口，介绍不同的实现方式，并说明它们在真实项目中的应用场景。

图3.1展示了Spring Security中的认证流程。这一架构是Spring Security实现认证过程的核心基础。理解它非常重要，因为无论你在任何Spring
Security的实现中都需要依赖它。你会发现，本书几乎每一章都会涉及到这部分架构。你会反复看到它，甚至可能会背下来——这其实是件好事。如果你掌握了这套架构，就像一位厨师熟悉自己的食材，能够随心所欲地烹制各种菜肴一样。

在图3.1中，阴影部分的方框代表我们最初使用的组件：UserDetailsService 和
PasswordEncoder。这两个组件主要负责流程中我常说的“用户管理部分”。在本章中，UserDetailsService 和 PasswordEncoder
这两个组件直接处理用户信息及其凭证。关于 PasswordEncoder 的详细内容，我们将在第4章中进行讨论。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508201646454.png){ loading=lazy }
  <figcaption>图 3.1 展示了 Spring Security 的认证流程。AuthenticationFilter 捕获传入的请求，并将认证任务交给 AuthenticationManager。AuthenticationManager 随后会调用认证提供者（AuthenticationProvider）来执行具体的认证操作。在校验用户名和密码时，AuthenticationProvider 会依赖 UserDetailsService 和 PasswordEncoder。</figcaption>
</figure>

在用户管理中，我们会用到 UserDetailsService 和 UserDetailsManager 接口。UserDetailsService
只负责通过用户名获取用户信息，这也是框架完成认证所需的唯一操作。而 UserDetailsManager
则增加了添加、修改和删除用户的相关功能，这些通常是大多数应用所必需的。将这两个接口分离，正好体现了接口隔离原则。接口分离带来了更高的灵活性，因为框架不会强制你实现应用不需要的功能。如果应用只需要用户认证，实现
UserDetailsService 接口就足够了。若需要管理用户，则 UserDetailsService 和 UserDetailsManager 组件还需要有一种方式来表示用户。

Spring Security 提供了 `UserDetails` 接口，你需要实现它，以便用框架能够理解的方式描述用户。正如你将在本章中了解到的，在
Spring Security 中，用户拥有一组权限，也就是用户被允许执行的操作。在第 7 到第 12 章讨论授权时，我们会大量涉及这些权限。但目前，Spring
Security 通过 GrantedAuthority 接口来表示用户可以执行的操作。我们通常称这些为“权限”，而每个用户可以拥有一个或多个权限。在图
3.2 中，你可以看到用户管理部分在认证流程中各组件之间的关系示意图。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508201658867.png){ loading=lazy }
  <figcaption>图3.2 展示了用户管理相关组件之间的依赖关系。UserDetailsService 通过用户名查找用户，从而获取用户的详细信息。用户由 UserDetails 接口定义，每个用户拥有一个或多个权限，这些权限通过 GrantedAuthority 接口表示。如果需要实现如创建、删除用户或修改密码等操作，则可以使用 UserDetailsManager 接口。该接口在 UserDetailsService 的基础上扩展了这些功能。</figcaption>
</figure>

理解 Spring Security
架构中这些对象之间的关联，以及实现它们的方法，可以为你在开发应用时提供丰富的选择。每一种方案都有可能成为你正在开发的应用中恰到好处的拼图，因此你需要谨慎做出决定。但要做出选择，首先你得清楚有哪些选项可以选。

## 描述用户

在本节中，你将学习如何描述应用程序的用户，以便 Spring Security
能够识别他们。学会如何表示用户并让框架感知用户，是构建认证流程的关键一步。应用程序会根据用户信息来决定是否允许访问某项功能。要管理用户，首先需要了解如何在应用中定义用户的原型。本节将通过示例，讲解如何在
Spring Security 应用中为用户建立蓝图。

在 Spring Security 中，用户定义需要满足 UserDetails 接口的规范。UserDetails 代表了 Spring Security
所理解的用户。你在应用中用于描述用户的类必须实现这个接口，这样框架才能识别并处理你的用户信息。

### 使用UserDetails接口描述用户

在本节中，你将学习如何实现 UserDetails 接口，以描述你应用中的用户。我们会讨论 UserDetails
规范中声明的方法，以理解我们为何以及如何实现它们。首先，让我们来看一下下面展示的这个接口。

```java title="清单 3.1 UserDetails 接口"
public interface UserDetails extends Serializable {
	String getUsername();

	String getPassword();

	Collection<? extends GrantedAuthority> getAuthorities();

	boolean isAccountNonExpired();

	boolean isAccountNonLocked();

	boolean isCredentialsNonExpired();

	boolean isEnabled();
}

```

getUsername() 和 getPassword() 方法分别返回用户名和密码，正如你所预期的那样。应用程序会在认证过程中使用这两个值，而它们也是这个接口中唯一与认证相关的信息。其余五个方法则都与授权用户访问应用资源有关。

通常来说，应用程序应允许用户在其业务场景下执行一些有意义的操作。例如，用户应该能够读取、写入或删除数据。我们通常说用户是否有权限执行某个操作，而“权限”则代表了用户所拥有的操作特权。我们通过实现
getAuthorities() 方法，返回授予用户的一组权限。

!!! note

	正如你将在第6章中了解到的，Spring Security 使用“权限”（authorities）来指代细粒度的权限或角色（角色是权限的集合）。为了让你阅读起来更加轻松，本书将细粒度的权限称为“权限”（authorities）。

此外，正如在 UserDetails 接口中所看到的，用户可以进行以下操作：

- 让账户过期
- 锁定账户
- 让凭证过期
- 禁用账户

假设你决定在应用逻辑中实现这些用户限制，那么你需要实现 isAccountNonExpired()、isAccountNonLocked()
、isCredentialsNonExpired() 和 isEnabled() 这几个方法，并确保需要启用的功能返回
true。并不是所有应用都需要账户过期或在特定条件下被锁定。如果你的应用不需要这些功能，可以让这四个方法直接返回 true 即可。

!!! note

	UserDetails 接口中最后四个方法的命名可能听起来有些奇怪。有人可能会认为，这些名字在代码整洁性和可维护性方面并不理想。比如，isAccountNonExpired() 这个名字看起来像是双重否定，乍一看可能会让人困惑。但我们不妨仔细分析一下这四个方法的命名。它们之所以这样命名，是为了在授权失败时都返回 false，其他情况下则返回 true。这种做法其实是合理的，因为人们通常会把 “false” 与负面情况联系起来，而 “true” 则代表积极的场景。

### GrantedAuthority合同详解

正如你在第3.2.1节中看到的，UserDetails接口的定义中，将授予用户的操作称为“权限”（authorities）。在第7到第12章中，我们会基于这些用户权限编写授权配置。因此，了解如何定义权限非常重要。

权限代表了用户在你的应用中可以执行的操作。如果没有权限，所有用户都将是平等的。虽然有些简单的应用确实让所有用户权限相同，但在大多数实际场景中，应用通常会区分不同类型的用户。有些用户可能只能查看特定信息，而其他用户则可以修改数据。你需要根据应用的功能需求，让系统能够区分这些用户，也就是确定用户所需的权限。在
Spring Security 中，权限是通过 GrantedAuthority 接口来描述的。

在讨论如何实现 UserDetails 之前，我们先来了解一下 GrantedAuthority 接口。这个接口在用户详情的定义中会用到，它表示授予用户的某项权限。每个用户至少要拥有一个权限。下面是
GrantedAuthority 接口的实现定义：

```java
public interface GrantedAuthority extends Serializable {
	String getAuthority();
}
```

要创建一个权限，你只需要为该权限取一个名字，这样在编写授权规则时就可以引用它。例如，用户可以读取应用程序管理的记录，或者删除这些记录。你可以根据为这些操作命名的方式来编写授权规则。

在本章中，我们将实现 getAuthority() 方法，使其返回权限名称的字符串。GrantedAuthority 接口只有一个抽象方法，在本书中，你会经常看到我们用
lambda 表达式来实现它。另一种方式是使用 SimpleGrantedAuthority 类来创建权限实例。SimpleGrantedAuthority 类提供了一种创建不可变
GrantedAuthority 实例的方法，你只需在构建实例时传入权限名称。下面的代码片段展示了两种实现 GrantedAuthority 的方式：一种是使用
lambda 表达式，另一种是通过 SimpleGrantedAuthority 类。

```java
GrantedAuthority g1 = () -> "READ";
GrantedAuthority g2 = new SimpleGrantedAuthority("READ");
```

### 编写一个最简化的UserDetails实现

在本节中，你将编写第一个 UserDetails
接口的实现。我们首先会给出一个基础实现，每个方法都返回固定的值。随后，我们会将其改造成更贴近实际应用场景的版本，使其能够支持多个不同的用户实例。现在，你已经了解了如何实现
UserDetails 和 GrantedAuthority 接口，我们可以为应用程序编写一个最简单的用户定义。

我们创建一个名为 DummyUser 的类，用于实现一个用户的最简描述，如下所示。我主要用这个类来演示如何实现 UserDetails
接口中的方法。这个类的实例始终只代表一个用户——用户名为 "bill"，密码为 "12345"，并且拥有名为 "READ" 的权限。

```java title="代码清单 3.2 DummyUser 类"
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

清单3.2中的这个类实现了UserDetails接口，因此需要实现该接口的所有方法。在这里，你可以看到getUsername()和getPassword()
方法的实现。在这个示例中，这两个方法仅返回各自属性的固定值。

接下来，我们为权限列表添加一个定义。下面的代码展示了 getAuthorities() 方法的实现。该方法返回一个只包含单一 GrantedAuthority
接口实现的集合。

```java title="代码清单 3.3 getAuthorities() 方法的实现"
public class DummyUser implements UserDetails {

	// Omitted code

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> "READ");
	}

	// Omitted code

}
```

最后，您还需要为 UserDetails 接口的最后四个方法添加实现。在 DummyUser 类中，这些方法始终返回
true，表示该用户始终处于激活和可用状态。相关示例请参见下方代码。

```java title="代码清单 3.4 UserDetails 接口最后四个方法的实现"
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

当然，这种最简化的实现意味着该类的所有实例都代表同一个用户。虽然这有助于理解接口的基本要求，但在实际应用中并不可取。对于真实的应用场景，你应该创建一个可以生成不同用户实例的类。此时，类的定义至少需要包含用户名和密码这两个属性，具体可以参考下一个代码示例。

```java title="清单3.5 更为实用的 UserDetails 接口实现"
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

有些应用程序比较简单，并不需要自定义实现 UserDetails 接口。在本节中，我们将介绍如何使用 Spring Security
提供的构建器类来创建简单的用户实例。你无需在应用中额外声明一个类，只需通过 User 构建器类即可快速获得一个代表用户的实例。

`org.springframework.security.core.userdetails` 包中的 User 类是一种简单的方式，用于创建 UserDetails
类型的实例。通过这个类，你可以方便地构建不可变的 UserDetails
实例。你至少需要提供用户名和密码，并且用户名不能为空字符串。下面的示例展示了如何使用这个构建器。采用这种方式创建用户，无需自定义实现
UserDetails 接口。

``` java
UserDetails u = User.withUsername("bill")
               .password("12345")
               .authorities("read", "write")
               .accountExpired(false)
               .disabled(true)
               .build();
```

以前面的代码为例，我们来深入解析一下 User 构建器类的结构。User.withUsername(String username) 方法会返回一个嵌套在 User 类中的
UserBuilder 构建器实例。另一种创建构建器的方式是从另一个 UserDetails 实例开始。在代码清单 3.7 中，第一行通过传入一个字符串形式的用户名来构建
UserBuilder。随后，我们还演示了如何以一个已存在的 UserDetails 实例为起点来创建构建器。

```java
User.UserBuilder builder1 = User.withUsername("bill"); //使用用户名创建用户

UserDetails u1 = builder1
		.password("12345")
		.authorities("read", "write")
		.passwordEncoder(p -> encode(p)) // 密码编码器只是一个用于编码的函数。
		.accountExpired(false)
		.disabled(true)
		.build(); //在构建流程的最后，调用 build() 方法。

User.UserBuilder builder2 = User.withUserDetails(u1); //你也可以通过已有的 UserDetails 实例来创建用户。

UserDetails u2 = builder2.build();

```

你可以从代码清单3.7中定义的任意一个构建器看到，可以通过该构建器获取一个由UserDetails接口表示的用户。在构建流程的最后，调用build()
方法。如果你提供了密码，它会应用定义好的函数对密码进行加密，然后构建UserDetails实例并返回。

!!! note

	请注意，这里的密码编码器是以 Function<String, String> 的形式提供的，而不是 Spring Security 提供的 PasswordEncoder 接口。这个函数的唯一职责就是对给定编码格式的密码进行转换。在下一节中，我们将详细讨论 Spring Security 的 PasswordEncoder 合约（我们在第2章已经用过），并会在第4章进一步深入探讨 PasswordEncoder 的相关内容。

### 结合与用户相关的多项职责

在上一节中，你已经学会了如何实现 UserDetails 接口。在实际场景中，情况往往更加复杂。大多数情况下，用户会关联到多个职责。如果你将用户信息存储在数据库中，那么在应用程序中就需要一个类来表示
`持久化实体`。或者，如果你是通过 Web 服务从其他系统获取用户信息，那么通常还需要一个`数据传输对象`
来表示用户实例。假设我们采用第一种方式，也就是一个简单但常见的场景，我们在 SQL
数据库中有一个用于存储用户信息的表。为了简化示例，我们假设每个用户只拥有一个权限。下面的代码展示了用于映射该表的实体类。

```java title="清单3.8 定义JPA User实体类"

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

如果你让同一个类同时实现 Spring Security 的用户详情接口，这个类就会变得更加复杂。你觉得下面的代码看起来怎么样？在我看来，这简直是一团糟，我会在其中迷失方向。

```java title="清单3.9 User类承担了两个职责"

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

该类包含了JPA注解、getter和setter方法，其中 getUsername() 和 getPassword() 都重写了 UserDetails 接口中的方法。它还包含一个返回字符串的
getAuthority() 方法，以及一个返回集合的 getAuthorities() 方法。getAuthority() 只是类中的一个普通 getter，而
getAuthorities() 则实现了 UserDetails 接口中的方法。情况在为其他实体添加关联关系时会变得更加复杂。总的来说，这段代码非常不易理解！

我们如何让这段代码写得更简洁？前面代码示例显得混乱，根本原因在于混合了两种不同的职责。虽然在应用中确实需要这两部分功能，但并没有人规定必须把它们放在同一个类里。我们可以尝试将它们分离，定义一个名为
SecurityUser 的独立类，用于适配 User 类。如下所示，SecurityUser 类实现了 UserDetails 接口，并借此将我们的用户集成到 Spring
Security 架构中。而 User 类则只保留了其作为 JPA 实体的职责。

```java title="清单 3.10 仅将 User 类实现为一个 JPA 实体"
public class User {

	@Id
	private int id;
	private String username;
	private String password;
	private String authority;

	// Omitted getters and setters

}
```

在代码清单3.10中，User类只保留了其JPA实体的职责，因此代码变得更加易读。阅读这段代码时，你现在可以专注于与持久化相关的细节，而这些细节对于Spring
Security来说并不重要。在下一个代码清单中，我们将实现SecurityUser类，用于包装User实体。

```java title="清单 3.11 实现 UserDetails 接口的 SecurityUser 类"
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

正如你所见，我们仅使用 SecurityUser 类将系统中的用户信息映射为 Spring Security 能够识别的 UserDetails 接口。为了强调
SecurityUser 离开 User 实体毫无意义，我们将该字段声明为 final，必须通过构造方法传入用户对象。SecurityUser 类对 User
实体进行了适配，并补充了与 Spring Security 协议相关的必要代码，而没有将这些代码混入 JPA 实体中，从而实现了多种不同的功能分离。

!!! note

	你可以找到多种方法来分离这两项职责。我并不是说本节介绍的方法是最优或唯一的选择。通常，具体采用哪种类设计方式会因实际情况而异。但核心思想是一致的：避免职责混杂，尽量让代码实现解耦，从而提升应用程序的可维护性。

## 指导Spring Security如何管理用户

在上一节中，你实现了 UserDetails 接口，用于描述用户信息，以便 Spring Security 能够识别和处理用户。但 Spring Security
是如何管理用户的呢？在进行凭证比对时，这些用户信息是从哪里获取的？又该如何添加新用户或修改已有用户？在第二章中，你已经了解到，框架为用户管理专门定义了一个组件，认证流程会将用户管理的相关操作委托给
UserDetailsService 实例。我们甚至自定义了一个 UserDetailsService，以覆盖 Spring Boot 默认提供的实现。

在本节中，我们将尝试多种实现 UserDetailsService 类的方法。通过实现示例中 UserDetailsService
合约所描述的职责，你将深入理解用户管理的工作原理。随后，你会了解到 UserDetailsManager 接口是如何在 UserDetailsService
定义的合约基础上扩展更多功能的。最后，我们将使用 Spring Security 提供的 UserDetailsManager 接口的现成实现。我们会编写一个示例项目，采用
Spring Security 最常用的实现之一——JdbcUserDetailsManager 类。掌握这些内容后，你就能告诉 Spring Security
到哪里查找用户，这对于认证流程来说至关重要。

### 理解UserDetailsService接口规范

在本节中，您将学习 UserDetailsService 接口的定义。在了解如何以及为何实现它之前，您需要先理解它的约定。现在，是时候深入探讨
UserDetailsService 以及如何使用其实现类了。UserDetailsService 接口只包含一个方法，如下所示：

```java
public interface UserDetailsService {

	UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException;
}
```

认证实现会调用 loadUserByUsername(String username) 方法，根据给定的用户名获取用户详情（见图
3.3）。用户名在系统中当然是唯一的。该方法返回的用户对象实现了 UserDetails 接口。如果用户名不存在，则会抛出
UsernameNotFoundException 异常。

!!! note

	UsernameNotFoundException 是一种运行时异常。UserDetailsService 接口中的 throws 子句仅用于文档说明。UsernameNotFoundException 直接继承自 AuthenticationException 类型，而 AuthenticationException 是所有与认证过程相关异常的父类。AuthenticationException 又进一步继承自 RuntimeException 类。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508211748748.png){ loading=lazy }
  <figcaption>图 3.3 AuthenticationProvider 负责执行认证流程，并通过 UserDetailsService 获取用户信息。它会调用 loadUserByUsername(String username) 方法，根据用户名查找用户。</figcaption>
</figure>

### 实现UserDetailsService接口

在本节中，我们将通过一个实际示例来演示如何实现 UserDetailsService。你的应用程序负责管理凭证信息以及其他用户相关的内容。这些信息可能存储在数据库中，也可能由你通过
Web 服务或其他方式访问的其他系统来处理（见图 3.3）。无论你的系统采用哪种方式，Spring Security
唯一需要你做的，就是提供一个能够根据用户名检索用户信息的实现。

在下一个示例中，我们将编写一个
UserDetailsService，它包含一个内存中的用户列表。在第2章中，你已经使用过一个提供的实现——InMemoryUserDetailsManager，它实现了相同的功能。由于你已经熟悉了这个实现的工作方式，这里我选择实现一个类似的功能，但这次我们自己来实现。当我们创建
UserDetailsService 类的实例时，会传入一个用户列表。你可以在项目 ssia-ch3-ex1 中找到这个示例。在名为 model 的包中，我们按照下面的代码定义了
UserDetails。

```java title="清单 3.12 UserDetails 接口的实现"
public class User implements UserDetails {

	private final String username;
	private final String password;
	private final String authority;

	public User(String username, String password, String authority) {
		this.username = username;
		this.password = password;
		this.authority = authority;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(() -> authority);
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

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
}

```

在名为 services 的包中，我们创建了一个名为 InMemoryUserDetailsService 的类。下面的代码展示了该类的具体实现方式。

```java title="代码清单 3.13 UserDetailsService 接口的实现"
public class InMemoryUserDetailsService implements UserDetailsService {

	private final List<UserDetails> users;

	public InMemoryUserDetailsService(List<UserDetails> users) {
		this.users = users;
	}

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {

		return users.stream()
				.filter(
						u -> u.getUsername().equals(username)
				)
				.findFirst()
				.orElseThrow(
						() -> new UsernameNotFoundException("User not found")
				);
	}
}

```

loadUserByUsername(String username) 方法会在用户列表中查找指定的用户名，并返回对应的 UserDetails 实例。如果找不到该用户名的实例，则会抛出
UsernameNotFoundException。现在我们可以将这个实现作为我们的 UserDetailsService 使用。下面的代码展示了如何在配置类中将其注册为一个
bean，并在其中添加一个用户。

```java title="代码清单 3.14 在配置类中将 UserDetailsService 注册为 Bean"

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails u = new User("john", "12345", "read");
		List<UserDetails> users = List.of(u);
		return new InMemoryUserDetailsService(users);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
```

最后，我们创建一个简单的接口，并对实现进行测试。下面的代码展示了该接口的定义。

```java title="代码清单 3.15 用于测试实现的端点定义"

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

使用 cURL 调用该接口时，我们发现当用户名为 john，密码为 12345 时，会返回 HTTP 200 OK。如果使用其他用户名或密码，应用则会返回
401 Unauthorized（未授权）。

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

### 实现UserDetailsManager接口

在本节中，我们将讨论如何使用和实现 UserDetailsManager 接口。该接口在 UserDetailsService 合同的基础上进行了扩展，增加了更多方法。Spring
Security 需要 UserDetailsService 合同来完成认证操作。但在实际应用中，通常还需要对用户进行管理。大多数情况下，应用程序应该能够添加新用户或删除现有用户。此时，我们可以实现
Spring Security 定义的更为具体的接口——UserDetailsManager。它继承了 UserDetailsService，并新增了一些我们需要实现的操作：

```java
public interface UserDetailsManager extends UserDetailsService {
	void createUser(UserDetails user);

	void updateUser(UserDetails user);

	void deleteUser(String username);

	void changePassword(String oldPassword, String newPassword);

	boolean userExists(String username);
}
```

我们在第2章中使用的 InMemoryUserDetailsManager 实际上是一个 UserDetailsManager。当时我们只关注了它作为 UserDetailsService
的特性。本节所用的示例代码可参考项目 ssia-ch3-ex2。

#### 使用JDBCUserDetailsManager进行用户管理

除了 InMemoryUserDetailsManager 之外，我们还经常使用另一种 UserDetailsManager
实现——JdbcUserDetailsManager。JdbcUserDetailsManager 类用于在 SQL 数据库中管理用户，并通过 JDBC
直接连接数据库。因此，JdbcUserDetailsManager 不依赖于任何其他与数据库连接相关的框架或规范。

要了解 JdbcUserDetailsManager 的工作原理，最好的方式就是通过实际例子来演示。在下面的示例中，你将实现一个应用程序，利用
JdbcUserDetailsManager 管理 MySQL 数据库中的用户。图 3.4 展示了 JdbcUserDetailsManager 在整个认证流程中的位置和作用。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202508220900642.png){ loading=lazy }
  <figcaption>图 3.4 Spring Security 认证流程。本例中，我们将 JdbcUserDetailsManager 作为 UserDetailsService 组件使用。JdbcUserDetailsManager 通过数据库来管理用户。</figcaption>
</figure>

你将开始开发我们的演示应用，该应用通过 JdbcUserDetailsManager 实现用户管理。首先需要创建一个数据库和两张数据表。在本例中，我们将数据库命名为
spring，其中一张表命名为 users，另一张表命名为 authorities。这两个表名是 JdbcUserDetailsManager
默认识别的表名。正如你将在本节结尾了解到的，JdbcUserDetailsManager 的实现非常灵活，如果需要，你可以自定义这些默认表名。users
表的作用是存储用户信息。JdbcUserDetailsManager 要求 users 表中包含三个字段：username、password 和 enabled，你可以通过
enabled 字段来禁用或启用用户。

你可以选择自己创建数据库及其结构，可以使用你的数据库管理系统（DBMS）的命令行工具，或者使用客户端应用程序。例如，对于
MySQL，你可以选择使用 MySQL Workbench 来完成这项工作。但最简单的方法是让 Spring Boot 自动为你执行这些脚本。为此，只需在项目的
resources 文件夹下添加两个文件：schema.sql 和 data.sql。在 schema.sql 文件中，编写与数据库结构相关的 SQL 语句，比如创建、修改或删除表；而在
data.sql 文件中，编写针对表中数据的操作语句，比如 INSERT、UPDATE 或 DELETE。当你启动应用程序时，Spring Boot
会自动执行这两个文件中的脚本。对于需要数据库的示例项目，更简单的解决方案是使用 H2 内存数据库，这样你就无需单独安装数据库管理系统了。

!!! note

	如果你愿意，也可以选择使用 H2（就像我在 ssia-ch3-ex2 项目中做的那样）来开发本书中的应用程序。不过在大多数情况下，我还是选择用外部数据库管理系统来实现示例，这样可以明确地将其作为系统的外部组件，从而避免产生混淆。

您可以使用下面的代码在 MySQL 服务器上创建 users 表。只需将该脚本添加到 Spring Boot 项目的 schema.sql 文件中即可。

``` sql
CREATE TABLE IF NOT EXISTS `spring`.`users` (
 `id` INT NOT NULL AUTO_INCREMENT,
 `username` VARCHAR(45) NOT NULL,
 `password` VARCHAR(45) NOT NULL,
 `enabled` INT NOT NULL,
 PRIMARY KEY (`id`));
```

authorities 表用于存储每个用户的权限。每条记录包含一个用户名，以及授予该用户的具体权限。

``` sql
CREATE TABLE IF NOT EXISTS `spring`.`authorities`
(
    `id`        INT         NOT NULL AUTO_INCREMENT,
    `username`  VARCHAR(45) NOT NULL,
    `authority` VARCHAR(45) NOT NULL,
    PRIMARY KEY (`id`)
);
```

!!! note

	为了简化操作，并让你专注于我们讨论的 Spring Security 配置，本书中的示例省略了索引和外键的定义。

为了确保有用户用于测试，请分别在每个表中插入一条记录。你可以将这些 SQL 语句添加到 Spring Boot 项目的 resources 文件夹下的
data.sql 文件中。

``` sql
INSERT INTO `spring`.`authorities`
    (username, authority)
VALUES ('john', 'write');

INSERT INTO `spring`.`users`
    (username, password, enabled)
VALUES ('john', '12345', '1');
```

对于您的项目，您至少需要添加下列依赖项。请检查您的 pom.xml 文件，确保已包含这些依赖。

``` xml
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
```

!!! note

	在你的示例中，只要在依赖项中添加正确的 JDBC 驱动，就可以使用任何 SQL 数据库技术。

请注意，您需要根据所使用的数据库类型添加相应的 JDBC 驱动。例如，如果您使用的是 MySQL，则需要像下面的代码片段中所示，添加
MySQL 驱动依赖。

```xml

<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

你可以在项目的 application.properties 文件中配置数据源，也可以将其作为一个独立的 Bean。如果选择使用 application.properties
文件，只需在该文件中添加以下内容：

```properties
spring.datasource.url=jdbc:h2:mem:ssia
spring.datasource.username=sa
spring.datasource.password=
spring.sql.init.mode=always
```

在项目的配置类中，你需要定义 UserDetailsService 和 PasswordEncoder。JdbcUserDetailsManager 需要 DataSource
来连接数据库。数据源可以通过方法参数自动注入（如下例所示），也可以通过类的属性进行注入。

```java

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

现在，访问应用程序的任何端点都需要使用数据库中存储的用户进行 HTTP Basic 认证。为了验证这一点，我们创建了一个新的端点（如下所示），然后使用
cURL 进行调用。

```java

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

在下一个代码片段中，你会看到使用正确的用户名和密码调用该端点时的结果：

```shell
curl -u john:12345 http://localhost:8080/hello
```

对呼叫的响应是

```shell
Hello!
```

JdbcUserDetailsManager 还允许你自定义所使用的查询语句。在前面的示例中，我们确保使用了 JdbcUserDetailsManager
实现所要求的表名和列名。但这些名称未必适合你的应用场景。下面的示例展示了如何重写 JdbcUserDetailsManager 的查询语句。

```java title="清单 3.21 修改 JdbcUserDetailsManager 的查询以查找用户"

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

同样地，我们也可以修改 JdbcUserDetailsManager 实现中使用的所有查询语句。

!!! note "练习"

	编写一个类似的应用程序，但在数据库中为表和列使用不同的命名。重写 JdbcUserDetailsManager 实现中的查询（例如，使认证能够适配新的表结构）。项目 ssia-ch3-ex2 提供了一个可能的解决方案。

#### 使用LDAPUserDetailsManager进行用户管理

Spring Security 还为 LDAP 提供了 UserDetailsManager 的实现。虽然它没有 JdbcUserDetailsManager 那么常用，但如果你需要与
LDAP 系统集成进行用户管理，依然可以依赖它。在项目 ssia-ch3-ex3 中，你可以看到一个关于 LdapUserDetailsManager
的简单演示。由于这个演示无法使用真实的 LDAP 服务器，我在 Spring Boot 应用中搭建了一个嵌入式 LDAP 服务器。为了配置嵌入式
LDAP 服务器，我定义了一个简单的 LDAP 数据交换格式（LDIF）文件。下面是我的 LDIF 文件的内容。

``` ldif title="清单 3.22 LDIF 文件的定义"
// 定义基础实体
dn: dc=springframework,dc=org
objectclass: top
objectclass: domain
objectclass: extensibleObject
dc: springframework

// 定义一个群组实体
dn: ou=groups,dc=springframework,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

// 定义用户
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

在 LDIF 文件中，我只添加了一个用户，目的是在本示例的最后用于测试应用的行为。我们可以将 LDIF 文件直接放到 resources
文件夹下，这样它会自动被加入到 classpath 中，方便后续引用。我将这个 LDIF 文件命名为 server.ldif。为了使用 LDAP 并让 Spring
Boot 能够启动内嵌的 LDAP 服务器，你需要在 pom.xml 中添加相关依赖：

``` xml

<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-ldap</artifactId>
</dependency>
<dependency>
	<groupId>com.unboundid</groupId>
	<artifactId>unboundid-ldapsdk</artifactId>
</dependency>
```

在 application.properties 文件中，你还需要添加嵌入式 LDAP 服务器的相关配置，如下代码片段所示。应用启动嵌入式 LDAP
服务器所需的参数包括：LDIF 文件的位置、LDAP 服务器的端口号，以及基础域组件（DN）的标签值。

```properties
spring.ldap.embedded.ldif=classpath:server.ldif
spring.ldap.embedded.base-dn=dc=springframework,dc=org
spring.ldap.embedded.port=33389
```

一旦你拥有了用于身份验证的LDAP服务器，就可以将其集成到你的应用中。下面的示例展示了如何配置
LdapUserDetailsManager，使你的应用能够通过LDAP服务器进行用户认证。

```java title="清单3.23 配置文件中 LdapUserDetailsManager 的定义"

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

我们还可以创建一个简单的接口，用于测试安全配置。为此，我添加了一个控制器类，代码如下：

```java

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

现在启动应用程序，并调用 /hello 接口。如果你希望应用程序允许你访问该接口，需要使用用户 john 进行身份验证。下面的代码片段展示了使用
cURL 调用该接口的结果：

```shell
curl -u john:12345 http://localhost:8080/hello
```

对该调用的响应是

```shell
Hello!
```

## 总结

- UserDetails 接口是用于在 Spring Security 中描述用户的契约。
- UserDetailsService 接口是 Spring Security 期望你在认证架构中实现的契约，用于定义应用程序获取用户信息的方式。
- UserDetailsManager 接口继承自 UserDetailsService，并增加了与创建、修改或删除用户相关的操作。
- Spring Security 提供了多种 UserDetailsManager 的实现，包括 InMemoryUserDetailsManager、JdbcUserDetailsManager 和
  LdapUserDetailsManager。
- JdbcUserDetailsManager 类的优势在于它直接使用 JDBC，不会让应用程序受限于其他框架。
