# 实现身份认证

第3章和第4章介绍了身份验证流程中的一些组件。我们讨论了`UserDetails`以及如何定义原型来描述Spring
Security中的用户。接下来，我们在示例中使用了`UserDetails`，展示了`UserDetailsService`和`UserDetailsManager`
契约的工作原理及其实现方法。我们还在示例中讨论并使用了这些接口的主要实现。最后，您学习了`PasswordEncoder`
如何管理密码及其使用方法，以及Spring Security`加密模块（SSCM）`中的加密器和密钥生成器。

然而，`AuthenticationProvider` 层负责认证的逻辑。在 `AuthenticationProvider` 中，你会找到决定是否认证请求的条件和指令。将这一责任委托给
`AuthenticationProvider` 的组件是 `AuthenticationManager`，它从 HTTP 过滤器层接收请求，这在第 5
章中已讨论过。在本章中，我们将探讨认证过程，该过程只有两种可能的结果：

- `请求的实体未经过身份验证`。用户未被识别，应用程序拒绝请求，而不将其委托给授权过程。通常，在这种情况下，返回给客户端的响应状态是HTTP
  401 未授权。
- `请求的实体已通过身份验证`。请求者的详细信息被存储，以便应用程序可以使用这些信息进行授权。正如您将在本章中了解到的，SecurityContext
  负责处理当前已认证请求的详细信息。

为了提醒您各个角色及其之间的关系，图6.1展示了我们在第2章中已经看到的图示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261737456.png){ loading=lazy }
  <figcaption>图 6.1 Spring Security 中的认证流程。此过程概述了应用程序识别提交请求的个人的方法。本章重点关注的元素已被突出显示。在此背景下，AuthenticationProvider 负责执行认证过程，而 SecurityContext 保留有关已认证请求的信息。</figcaption>
</figure>

本章将介绍认证流程的其余部分（图6.1中的阴影框）。接下来，在第7章和第8章中，您将学习授权的工作原理，这是HTTP请求中继认证之后的过程。首先，我们需要讨论如何实现
`AuthenticationProvider`接口。您需要了解Spring Security在认证过程中如何理解请求。

为了清晰描述如何表示认证请求，我们将从`Authentication`接口开始。一旦我们讨论完这个接口，就可以进一步观察在成功认证后请求的细节会发生什么。接下来，我们可以讨论
`SecurityContext`接口以及Spring Security如何管理它。在本章的结尾，你将学习如何自定义HTTP
Basic认证方法。我们还将讨论另一种可用于我们应用程序的认证选项——`基于表单的登录`。

## 理解AuthenticationProvider

在企业应用中，你可能会遇到这样的情况：基于用`户名和密码`
的默认身份验证方式不适用。此外，在身份验证方面，你的应用程序可能需要实现多种场景（如图6.2所示）。例如，你可能希望用户能够通过接收到的短
`信验证码`或特定应用程序显示的代码来证明身份。或者，你可能需要实现`用户必须提供存储在文件中`的某种密钥的身份验证场景。你甚至可能需要使用
`用户指纹`的表示来实现身份验证逻辑。框架的目的就是要足够灵活，以便让你能够实现这些场景中的任何一种。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409262059124.png){ loading=lazy }
  <figcaption>图6.2 一个应用程序可能需要多种身份验证方法。尽管用户名和密码在大多数情况下已足够，但在某些情况下，用户身份验证的过程可能会更加复杂。</figcaption>
</figure>

框架通常提供一组最常用的实现，但当然无法涵盖所有可能的选项。在 Spring Security 中，你可以使用 `AuthenticationProvider`
接口来定义任何自定义的认证逻辑。在本节中，你将学习通过实现 `Authentication` 接口来表示认证事件，然后使用
`AuthenticationProvider` 创建自定义的认证逻辑。为了实现我们的目标

- 在第6.1.1节中，我们分析了Spring Security如何表示认证事件。
- 在第6.1.2节中，我们讨论了负责认证逻辑的`AuthenticationProvider`合约。
- 在第6.1.3节中，您将通过实现`AuthenticationProvider`合约来编写自定义身份验证逻辑。

### 在身份验证过程中表示请求

本节讨论了 Spring Security 在认证过程中如何理解`请求`。在深入实现自定义认证逻辑之前，了解这一点非常重要。正如你将在第6.1.2节中了解到的，要实现自定义的
`AuthenticationProvider`，首先需要理解如何描述认证事件。在这里，我们将查看表示`认证`的契约，并讨论你需要了解的方法。

`Authentication` 是同名过程中涉及的基本接口之一。`Authentication`
接口代表认证请求事件，并保存请求访问应用程序的实体的详细信息。在认证过程中及之后，您可以使用与认证请求事件相关的信息。
`请求访问应用程序的用户`称为
`主体`。如果您曾在任何应用中使用过 Java Security，您可能知道一个名为 `Principal` 的接口代表相同的概念。Spring Security 的
`Authentication` 接口扩展了这一契约（图 6.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409271621298.png){ loading=lazy }
  <figcaption>图6.3 Authentication协议扩展了Principal协议。它引入了额外的规定，例如需要密码或可以提供有关认证请求的更多细节。某些方面，如权限数组，是 Spring Security 特有的。</figcaption>
</figure>

在 Spring Security 中，`Authentication` 合约不仅代表一个主体，还包含了认证过程是否完成的信息以及权限的集合。这个合约被设计为扩展
Java Security 的 `Principal` 合约，这在与其他框架和应用程序的实现兼容性方面是一个优势。这种灵活性使得从其他方式实现认证的应用程序迁移到
Spring Security 更加容易。

让我们在下面的列表中进一步了解`Authentication`接口的设计。

``` java title="清单6.1 Spring Security中声明的Authentication接口"
public interface Authentication extends Principal, Serializable {

	Collection<? extends GrantedAuthority> getAuthorities();

	Object getCredentials();

	Object getDetails();

	Object getPrincipal();

	boolean isAuthenticated();

	void setAuthenticated(boolean isAuthenticated)
			throws IllegalArgumentException;
}
```

目前，您需要学习的接口方法只有以下这些：

- `isAuthenticated()`—如果认证过程结束则返回 true，如果认证过程仍在进行中则返回 false
- `getCredentials()`—返回用于认证过程的密码或任何秘密信息
- `getAuthorities()`—返回经过身份验证的请求所授予权限的集合

我们将在后续章节中讨论适用于所考虑实现的其他Authentication合约方法。

### 实现自定义认证逻辑

本节涉及实现自定义认证逻辑。我们将分析与此职责相关的Spring Security契约，以理解其定义。通过这些细节，您可以在第6.1.3节中通过代码示例实现自定义认证逻辑。

在 Spring Security 中，`AuthenticationProvider` 负责处理认证逻辑。`AuthenticationProvider` 接口的默认实现将查找系统用户的责任委托给
`UserDetailsService`。在认证过程中，它还使用 `PasswordEncoder` 进行密码管理。以下是 `AuthenticationProvider`
的定义，你需要为你的应用程序定义一个自定义认证提供者。

``` java title="清单 6.2 AuthenticationProvider 接口"
public interface AuthenticationProvider {

	Authentication authenticate(Authentication authentication)
			throws AuthenticationException;

	boolean supports(Class<?> authentication);
}
```

`AuthenticationProvider` 的职责与 `Authentication` 合约紧密相连。`authenticate()` 方法接收一个 `Authentication`
对象作为参数，并返回一个 `Authentication` 对象。我们通过实现 `authenticate()` 方法来定义认证逻辑。这里，我们快速总结一下实现
`authenticate()` 方法的方式：

- 如果认证失败，该方法应抛出一个`AuthenticationException`异常。
- 如果方法接收到一个不被您的`AuthenticationProvider`实现支持的认证对象，那么该方法应返回`null`。这样，我们就有可能在HTTP
  `过滤器级别`使用多种不同的认证类型。
- 该方法应返回一个表示完全认证对象的`Authentication`实例。对于此实例，`isAuthenticated()`
  方法返回true，并且包含有关认证实体的所有必要详细信息。通常，应用程序还会从该实例中移除敏感数据，例如密码。`在成功认证后，密码不再需要`
  ，保留这些详细信息可能会导致其暴露给不必要的目光。

在 `AuthenticationProvider` 接口中，第二个方法是 `supports(Class<?> authentication)`。如果当前的 `AuthenticationProvider`
支持作为 `Authentication` 对象提供的类型，你可以通过实现这个方法返回 true。请注意，即使这个方法对某个对象返回 true，
`authenticate()` 方法仍有可能通过返回 null 来拒绝请求。Spring Security 的设计更加灵活，允许用户实现一个
`AuthenticationProvider`，可以根据认证请求的详细信息而不仅仅是其类型来拒绝请求。

一个关于`authentication manager`和`authentication provider`如何协同工作以`验证`或`无效化身份验证`
请求的类比是为你的门配备一个更复杂的锁。你可以通过使用`卡片`或`传统的实体钥匙`来打开这个锁（图6.4）。锁本身就是
`authentication manager`，它决定是否打开门。为了做出这个决定，它委托给两个 `authentication providers`：一个知道如何验证`卡片`
，另一个知道如何验证`实体钥匙`。如果你用`卡片`来开门，只处理`实体钥匙`的`authentication provider`
会抱怨它不熟悉这种身份验证。然而，另一个provider支持这种身份验证，并验证卡片是否对这扇门有效。这就是`supports()`方法的目的。

除了测试`认证类型`之外，Spring Security 还增加了一层灵活性。`门锁`可以识别多种类型的`卡`。在这种情况下，当你出示一张卡时，其中一个认证提供者可能会说：
`我理解这是一张卡。但这不是我能验证的卡类型！` 这种情况发生在 `supports()` 返回 true 但 `authenticate()` 返回 null 时。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409272224557.png){ loading=lazy }
  <figcaption>图6.4 AuthenticationManager委托给其中一个可用的认证提供者。AuthenticationProvider可能不支持提供的认证类型。然而，如果它支持该对象类型，它可能不知道如何认证该特定对象。认证过程会进行评估，能够判断请求是否正确的AuthenticationProvider会响应AuthenticationManager。</figcaption>
</figure>

图6.5展示了另一种情景，其中一个`AuthenticationProvider`对象识别了`Authentication`，但判断其无效。在这种情况下，结果将是一个
`AuthenticationException`，最终在Web应用的HTTP响应中表现为`401 Unauthorized`状态。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409282031020.png){ loading=lazy }
  <figcaption>图6.5 如果没有任何一个 AuthenticationProvider 对象识别出 Authentication，或者其中任何一个拒绝它，则结果为 AuthenticationException。</figcaption>
</figure>

### 应用自定义认证逻辑

在本节中，我们实现自定义认证逻辑。您可以在项目 ssia-ch6-ex1 中找到此示例。通过这个示例，您可以应用在 6.1.1 和 6.1.2 节中学到的关于
`Authentication` 和 `AuthenticationProvider` 接口的知识。在清单 6.3 和 6.4 中，我们构建了一个如何实现自定义
`AuthenticationProvider` 的示例。这些步骤也在图 6.5 中展示，具体如下：

1. 声明一个实现 `AuthenticationProvider` 合约的类。
2. 确定新的 `AuthenticationProvider` 支持哪些类型的 `Authentication` 对象。
3. 实现`supports(Class<?> c)`方法，以指定我们定义的`AuthenticationProvider`支持哪种类型的身份验证。
4. 实现 `authenticate(Authentication a)` 方法以实现认证逻辑
5. 将新的 `AuthenticationProvider` 实现实例注册到 Spring Security 中。

``` java title="清单6.3 重写AuthenticationProvider的supports()方法"

@Component
public class CustomAuthenticationProvider
		implements AuthenticationProvider {

	// Omitted code

	@Override
	public boolean supports(Class<?> authenticationType) {
		return authenticationType
				.equals(UsernamePasswordAuthenticationToken.class);
	}
}
```

在代码清单6.3中，我们定义了一个实现`AuthenticationProvider`接口的新类。我们使用`@Component`
注解标记该类，以便在Spring管理的上下文中拥有其类型的实例。接下来，我们必须决定这个`AuthenticationProvider`支持哪种
`Authentication`接口的实现。这取决于我们期望在`authenticate()`方法中提供哪种类型的参数。如果我们没有在认证过滤器级别进行任何自定义（如第5章所述），那么类
`UsernamePasswordAuthenticationToken`定义了该类型。这个类是`Authentication`接口的一个实现，代表了使用`用户名和密码`
的标准认证请求。

根据这个定义，我们让 `AuthenticationProvider` 支持特定类型的密钥。一旦我们确定了 `AuthenticationProvider` 的范围，就可以通过重写
`authenticate()` 方法来实现认证逻辑，如下所示。

``` java title="清单 6.4 实现认证逻辑"

@Component
public class CustomAuthenticationProvider
		implements AuthenticationProvider {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	// Omitted constructor

	@Override
	public Authentication authenticate(Authentication authentication) {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		UserDetails u = userDetailsService.loadUserByUsername(username);

		if (passwordEncoder.matches(password, u.getPassword())) {
			return new UsernamePasswordAuthenticationToken(
					username,
					password,
					u.getAuthorities());
		} else {
			throw new BadCredentialsException
					("Something went wrong!");
		}
	}

	// Omitted code
}

```

清单6.4中的逻辑很简单，图6.6对此逻辑进行了直观展示。我们使用`UserDetailsService`实现来获取`UserDetails`。如果用户不存在，
`loadUserByUsername()`方法应抛出`AuthenticationException`。在这种情况下，认证过程停止，HTTP过滤器将响应状态设置为
`HTTP 401 Unauthorized`。如果用户名存在，我们可以进一步使用上下文中的`PasswordEncoder的matches()`方法检查用户的密码。如果密码不匹配，同样应抛出
`AuthenticationException`。如果密码正确，`AuthenticationProvider`返回一个标记为`authenticated`的`Authentication`
实例，其中包含请求的详细信息。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410272058959.png){ loading=lazy }
  <figcaption>图6.6 AuthenticationProvider执行定制的认证过程。它通过特定的UserDetailsService实现来获取用户详细信息以确认认证请求，并使用PasswordEncoder验证密码是否正确。如果未找到用户或密码不正确，AuthenticationProvider将抛出AuthenticationException。</figcaption>
</figure>

要插入新的`AuthenticationProvider`实现，我们定义了一个`SecurityFilterChain bean`。以下示例展示了这一点。

``` java title="清单 6.5 在配置类中注册 AuthenticationProvider"

@Configuration
public class ProjectConfig {

	private final AuthenticationProvider authenticationProvider;

	// Omitted constructor

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authenticationProvider(authenticationProvider);

		http.authorizeHttpRequests(c -> c.anyRequest().authenticated());

		return http.build();
	}

	// Omitted code
}
```

!!! note

    在代码清单6.5中，依赖注入被应用于一个使用`AuthenticationProvider`接口声明的字段。Spring识别`AuthenticationProvider`为一个接口（即一种抽象）。然而，Spring知道需要在其上下文中找到该接口的一个实现实例。在我们的例子中，这个实现是`CustomAuthenticationProvider`的实例，这是我们声明并使用`@Component`注解添加到Spring上下文中的唯一一个这种类型的实现。如需回顾依赖注入的相关知识，我推荐阅读我写的另一本书《Spring Start Here》（Manning, 2021）。

就是这样！您已成功自定义了 `AuthenticationProvider` 的实现。现在，您可以在需要的地方为您的应用程序自定义身份验证逻辑。

!!! note "如何在应用设计中失败"

    错误地应用框架会导致应用程序难以维护。更糟糕的是，有时那些未能正确使用框架的人会认为这是框架的问题。让我给你讲个故事。

    某年冬天，我作为顾问与一家公司合作时，他们的开发负责人打电话给我，请我协助实现一个新功能。他们需要在系统的一个组件中应用一种自定义的身份验证方法，该组件是在 Spring 的早期版本中开发的。不幸的是，在实现应用程序的类设计时，开发人员没有正确依赖 Spring Security 的核心架构。

    他们只使用了过滤器链，将 Spring Security 的整个功能重新实现为自定义代码。

    开发人员发现，随着时间的推移，定制变得越来越困难。然而，没有人采取行动来正确地重新设计组件并按照 Spring Security 的预期使用合同。许多困难来自于对 Spring 功能的不熟悉。一位主要开发人员说：“这都是 Spring Security 的错！这个框架很难应用，任何定制都很难实现。”他的看法让我有些震惊。我知道 Spring Security 有时确实难以理解，而且这个框架以学习曲线陡峭而闻名。但我从未遇到过无法用 Spring Security 设计出易于定制的类的情况！

    我们一起调查了这个问题，我发现应用程序开发人员可能只利用了 Spring Security 的 10% 功能。随后，我举办了一个为期两天的 Spring Security 研讨会，重点讲解我们可以为他们需要更改的特定系统组件做些什么以及如何去做。

    一切都以决定完全重写大量自定义代码而告终，以正确依赖于 Spring Security，从而使应用程序更易于扩展，以满足他们对安全实施的关注。我们还发现了一些与 Spring Security 无关的其他问题，但那是另一个故事。

    从这个故事中你可以学到以下几点：

    - 一个框架，尤其是广泛应用于各种应用程序的框架，是由许多聪明的人编写的，很难相信它会被糟糕地实现。在得出任何问题可能是框架导致的结论之前，请始终先分析你的应用程序。
    - 在决定使用某个框架时，至少要确保你了解其基础知识。
    - 请注意您用来学习该框架的资源。有时，您在网上找到的文章可能只是展示如何快速解决问题，而不一定是如何正确实现类设计。
    - 在研究中使用多个来源。为了解释你的误解，当不确定如何使用某个东西时，写一个概念验证。
    - 如果你决定使用一个框架，就尽可能地按照其预期用途来使用它。例如，假设你使用了 Spring Security，但你发现，在进行安全性实现时，你倾向于编写更多的自定义代码，而不是依赖框架提供的功能。你应该思考为什么会出现这种情况。

    如果你决定使用一个框架，就尽可能地按照其预期用途来使用它。例如，假设你使用了 Spring Security，但你发现，在进行安全实现时，你倾向于编写更多的自定义代码，而不是依赖框架提供的功能。你应该思考为什么会出现这种情况。

## 使用 SecurityContext

本节讨论安全上下文。我们分析其工作原理、如何访问数据，以及应用程序在不同线程相关场景中如何管理它。完成本节后，您将了解如何为各种情况配置安全上下文。这样，您可以利用安全上下文中存储的已认证用户信息，在第7章和第8章中配置授权。

在认证过程结束后，您可能需要获取有关已认证实体的详细信息。例如，您可能需要引用当前已认证用户的用户名或权限。在认证过程完成后，这些信息是否仍然可访问？一旦
`AuthenticationManager`成功完成认证过程，它会在请求的剩余时间内存储 `Authentication`实例（见图6.7）。存储认证对象的实例被称为
`security context`(安全上下文)。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410281421306.png){ loading=lazy }
  <figcaption>图6.7 认证成功后，认证过滤器将已认证实体的详细信息存储在安全上下文中。这样，映射到请求的动作控制器在需要时可以访问这些详细信息。</figcaption>
</figure>

Spring Security 的安全上下文由 `SecurityContext` 接口描述，并在以下列表中定义。

``` java title="清单 6.6 SecurityContext 接口"
public interface SecurityContext extends Serializable {
	Authentication getAuthentication();

	void setAuthentication(Authentication authentication);
}
```

从合同定义中可以看出，`SecurityContext` 的主要职责是存储 `Authentication` 对象。那么 `SecurityContext` 本身是如何管理的呢？Spring
Security 提供了三种策略，通过一个管理者角色的对象来管理`SecurityContext`，这个对象被称为 `SecurityContextHolder`。

- `MODE_THREADLOCAL`—允许每个线程在安全上下文中存储自己的详细信息。在每个请求对应一个线程的Web应用程序中，这是一种常见的方法，因为每个请求都有一个独立的线程。
- `MODE_INHERITABLETHREADLOCAL`——与`MODE_THREADLOCAL`类似，但它还指示Spring Security在异步方法的情况下将安全上下文复制到下一个线程。这样，我们可以说运行
  `@Async`方法的新线程继承了安全上下文。@Async注解用于方法，以指示Spring在单独的线程上调用被注解的方法。
- `MODE_GLOBAL`—使应用程序的所有线程共享相同的安全上下文实例。

除了这三种管理由 Spring Security 提供的安全上下文的策略外，本节还说明了当您定义 Spring
未知的线程时会发生什么。如您所见，对于这些情况，您需要显式地将安全上下文中的详细信息复制到新线程中。Spring Security 无法自动管理不在
Spring 上下文中的对象，但它提供了一些非常有用的实用类。

### 在安全上下文中使用持有策略

管理安全上下文的第一种策略是 `MODE_THREADLOCAL` 策略，这也是 Spring Security 用于管理安全上下文的默认策略。使用这种策略时，Spring
Security 使用 `ThreadLocal` 来管理上下文。`ThreadLocal` 是 JDK
提供的一种实现，它作为数据集合工作，但确保应用程序的每个线程只能看到存储在其专用部分的数据。这样，每个请求都可以访问其自己的安全上下文。没有线程可以访问其他线程的
`ThreadLocal`。这意味着在一个 Web 应用程序中，每个请求只能看到其自己的安全上下文。可以说，这也是您通常希望在后端 Web
应用程序中实现的。

图6.8概述了此功能。每个请求（A、B 和 C）都有其分配的线程（T1、T2 和
T3），因此每个请求只能看到存储在其自身安全上下文中的详细信息。然而，这也意味着如果创建了一个新线程（例如，当调用异步方法时），新线程也将拥有其自己的安全上下文。父线程（请求的原始线程）的详细信息不会被复制到新线程的安全上下文中。

!!! note

    在这里，我们讨论一种传统的 servlet 应用程序，其中每个请求都与一个线程绑定。这种架构仅适用于传统的 servlet 应用程序，每个请求都有自己的线程分配。这不适用于响应式应用程序。我们将在第 17 章详细讨论响应式方法的安全性。

作为管理安全上下文的默认策略，这个过程不需要显式配置。在身份验证过程结束后，只需在需要的地方使用静态方法`getContext()`
从持有者那里获取安全上下文。在代码清单6.7中，你可以看到在应用程序的一个端点获取安全上下文的示例。从安全上下文中，你可以进一步获取存储已认证实体详细信息的
`Authentication`对象。本节讨论的示例可以在项目ssia-ch6-ex2中找到。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410281600238.png){ loading=lazy }
  <figcaption>图6.8 每个请求都有自己的线程，用箭头表示。每个线程只能访问其自身的安全上下文信息。当创建新线程时（例如，通过@Async方法），父线程的详细信息不会被复制。</figcaption>
</figure>

``` java title="清单 6.7 从 SecurityContextHolder 获取 SecurityContext"

@GetMapping("/hello")
public String hello() {
	SecurityContext context = SecurityContextHolder.getContext();
	Authentication a = context.getAuthentication();
	return "Hello, " + a.getName() + "!";
}
```

在端点级别获取上下文中的认证信息更加方便，因为Spring会直接将其注入到方法参数中。你不需要每次都显式引用SecurityContextHolder类。如下所示的方法更佳。

``` java title="清单6.8 Spring在方法参数中注入Authentication值"

/*
 * Spring Boot 会在方法参数中注入当前的身份验证信息。
 * */

@GetMapping("/hello")
public String hello(Authentication a) {
	return "Hello, " + a.getName() + "!";
}
```

当使用正确的用户调用端点时，响应主体包含用户名。例如：

```shell
curl -u user:99ff79e3-8ca0-401c-a396-0a8625ab3bad http://localhost:8080/hello
Hello, user!
```

### 使用持有策略进行异步调用

坚持使用默认策略来管理安全上下文是很容易的。在很多情况下，这就是你所需要的全部。`MODE_THREADLOCAL`
提供了为每个线程隔离安全上下文的能力，使得安全上下文更容易理解和管理。然而，也有一些情况不适用这种方法。

如果我们必须处理每个请求的多个线程，情况会变得更加复杂。看看当你将端点设为异步时会发生什么。执行该方法的线程不再是处理请求的同一个线程。请考虑下一个列表中展示的端点。

``` java title="清单6.9 由不同线程提供服务的@Async方法"

/*
 * 由于使用了@Async，该方法在单独的线程上执行。
 * */

@GetMapping("/bye")
@Async
public void goodbye() {
	SecurityContext context = SecurityContextHolder.getContext();
	String username = context.getAuthentication().getName();
	// do something with the username
}
```

为了启用`@Async`注解的功能，我还创建了一个配置类，并使用`@EnableAsync`注解进行了标注：

``` java

@Configuration
@EnableAsync
public class ProjectConfig {
}
```

!!! note

    有时候在文章或论坛中，配置注解会放在`主类`上。例如，你可能会发现某些示例直接在主类上使用`@EnableAsync`注解。这种做法在技术上是正确的，因为我们在Spring Boot应用程序的主类上使用`@SpringBootApplication`注解，而这个注解包含`@Configuration`的特性。然而，在实际应用中，我们更倾向于将职责分开，从不将主类用作配置类。为了让本书中的示例尽可能清晰，我更喜欢将这些注解放在`@Configuration`类上，这与实际场景中的做法类似。

如果你现在尝试运行这段代码，它将在获取认证名称的那一行抛出一个`NullPointerException`异常，该行是

`String username = context.getAuthentication().getName()`

这是因为该方法现在在另一个线程上执行，而该线程没有继承安全上下文。因此，`Authorization` 对象为空，并在所示代码的上下文中导致
`NullPointerException`。在这种情况下，可以通过使用 `MODE_INHERITABLETHREADLOCAL` 策略来解决问题。这可以通过调用
`SecurityContextHolder.setStrategyName() `方法或使用系统属性 `spring.security.strategy`
来设置。通过设置此策略，框架会将请求的原始线程的详细信息复制到新创建的异步方法线程中（图 6.9）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410281657035.png){ loading=lazy }
  <figcaption>图6.9 使用 MODE_INHERITABLETHREADLOCAL 时，框架会将请求的原始线程中的安全上下文详细信息复制到新线程的安全上下文中。</figcaption>
</figure>

以下列表展示了一种通过调用 `setStrategyName()` 方法来设置安全上下文管理策略的方法。

``` java

@Configuration
@EnableAsync
public class ProjectConfig {
	@Bean
	public InitializingBean initializingBean() {
		return () -> SecurityContextHolder.setStrategyName(
				SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}
}
```

调用端点后，您会发现安全上下文已由 Spring 正确传递到下一个线程。此外，身份验证不再为 null。

!!! note

    这仅在框架自行创建线程时有效（例如，在@Async方法的情况下）。如果是你的代码创建线程，即使使用MODE_INHERITABLETHREADLOCAL策略，你也会遇到同样的问题。这是因为在这种情况下，框架并不知道你的代码创建的线程。我们将在第6.2.4和6.2.5节讨论如何解决这些问题。

### 对独立应用程序使用持有策略

如果你需要一个由所有应用程序线程共享的安全上下文，请将策略更改为`MODE_GLOBAL`
（图6.10）。对于一个不适合应用程序整体情况的Web服务器，你不会使用这种策略。后端Web应用程序独立管理其接收到的请求，因此为每个请求分开安全上下文比为所有请求使用一个上下文更有意义。不过，这对于独立应用程序来说可能是一个不错的选择。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410281721304.png){ loading=lazy }
  <figcaption>图6.10 当使用 MODE_GLOBAL 作为安全上下文管理策略时，所有线程访问相同的安全上下文。这意味着它们都可以访问相同的数据并可以更改这些信息。因此，可能会发生竞争条件，必须注意同步。</figcaption>
</figure>

如下代码片段所示，您可以像我们对 `MODE_INHERITABLETHREADLOCAL` 所做的那样更改策略。您可以使用
`SecurityContextHolder.setStrategyName()` 方法或系统属性 `spring.security.strategy`：

``` java

@Bean
public InitializingBean initializingBean() {
	return () -> SecurityContextHolder.setStrategyName(
			SecurityContextHolder.MODE_GLOBAL);
}
```

另外，请注意，SecurityContext 不是线程安全的。因此，在这种应用程序的所有线程都可以访问 SecurityContext 对象的策略中，您需要注意并发访问。

### 使用DelegatingSecurityContextRunnable转发安全上下文

您已经了解到，可以通过Spring
Security提供的三种模式来管理安全上下文：`MODE_THREADLOCAL`、`MODE_INHERITEDTHREADLOCAL`和`MODE_GLOBAL`
。默认情况下，框架仅确保为请求线程提供安全上下文，并且该安全上下文仅对该线程可访问。然而，框架并不负责处理新创建的线程（例如，在异步方法的情况下）。此外，您了解到在这种情况下，必须明确设置不同的模式来管理安全上下文。但我们仍然面临一个特殊情况：当您的代码启动新线程而框架并不知情时会发生什么？有时我们称这些为
`自我管理线程`，因为是我们在管理它们，而不是框架。在本节中，我们将应用Spring
Security提供的一些实用工具，帮助您将安全上下文传播到新创建的线程。

没有特定的 `SecurityContextHolder` 策略可以为`自管理线程`提供解决方案。在这种情况下，你需要负责安全上下文的传播。一个解决方案是使用
`DelegatingSecurityContextRunnable` 来装饰你想在单独线程上执行的任务。`DelegatingSecurityContextRunnable` 扩展了
`Runnable`。当任务执行后没有返回值时，你可以使用它。如果有返回值，则可以使用 `Callable<T>` 的替代方案，即
`DelegatingSecurityContextCallable<T>`。这两个类都表示异步执行的任务，与其他 `Runnable` 或 `Callable`
一样。此外，它们确保为执行任务的线程复制当前的安全上下文。如图 6.11 所示，这些对象装饰原始任务并将安全上下文复制到新线程。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410281726867.png){ loading=lazy }
  <figcaption>图6.11 DelegatingSecurityContextCallable 被设计为 Callable 对象的装饰器。在构建这样的对象时，你需要提供应用程序异步执行的可调用任务。DelegatingSecurityContextCallable 会将安全上下文中的详细信息复制到新线程中，然后执行该任务。</figcaption>
</figure>

下面的示例展示了如何使用 `DelegatingSecurityContextCallable`。首先，我们定义一个简单的端点方法，该方法声明了一个 `Callable`
对象。这个 `Callable` 任务将返回当前安全上下文中的用户名。

``` java title="清单6.11 定义一个可调用对象并在单独的线程上执行它作为任务"

@GetMapping("/ciao")
public String ciao() throws Exception {
	Callable<String> task = () -> {
		SecurityContext context = SecurityContextHolder.getContext();
		return context.getAuthentication().getName();
	};
	// Omitted code
}
```

我们继续这个例子，将任务提交给`ExecutorService`。执行结果被获取，并作为响应主体由端点返回。

``` java

@GetMapping("/ciao")
public String ciao() throws Exception {
	Callable<String> task = () -> {
		SecurityContext context = SecurityContextHolder.getContext();
		return context.getAuthentication().getName();
	};
	ExecutorService e = Executors.newCachedThreadPool();
	try {
		return "Ciao, " + e.submit(task).get() + "!";
	} finally {
		e.shutdown();
	}
}
```

如果直接运行该应用程序，只会得到一个`NullPointerException`。在新创建的线程中运行可调用任务时，身份验证已不存在，安全上下文为空。为了解决这个问题，我们使用
`DelegatingSecurityContextCallable`装饰任务，它为新线程提供当前上下文，如下所示。

``` java

@GetMapping("/ciao")
public String ciao() throws Exception {
	Callable<String> task = () -> {
		SecurityContext context = SecurityContextHolder.getContext();
		return context.getAuthentication().getName();
	};
	ExecutorService e = Executors.newCachedThreadPool();
	try {
		var contextTask = new DelegatingSecurityContextCallable<>(task);
		return "Ciao, " + e.submit(contextTask).get() + "!";
	} finally {
		e.shutdown();
	}
}
```

现在调用该端点，您可以观察到 Spring 将安全上下文传播到了执行任务的线程中：

```shell
curl -u user:2eb3f2e8-debd-420c-9680-48159b2ff905 http://localhost:8080/ciao
```

此调用的响应主体为:

```shell
Ciao, user!
```

### 使用DelegatingSecurityContextExecutorService转发安全上下文

当我们处理代码启动的线程而没有让框架知晓时，必须管理从安全上下文到下一个线程的详细信息传播。在第6.2.4节中，你使用了一种技术，通过任务本身来复制安全上下文中的详细信息。Spring
Security 提供了一些很棒的实用类，比如 `DelegatingSecurityContextRunnable` 和 `DelegatingSecurityContextCallable`
。这些类装饰了你异步执行的任务，并负责从安全上下文中复制详细信息，以便你的实现可以从新创建的线程中访问这些信息。然而，我们还有另一种选择来处理安全上下文向新线程的传播，即从线程池而不是任务本身管理传播。在本节中，你将学习如何使用
Spring Security 提供的更多优秀实用类来应用这种技术。

一种替代装饰任务的方法是使用特定类型的`Executor`。在下一个例子中，你可以看到任务仍然是一个简单的 `Callable<T>`
，但线程仍然管理安全上下文。安全上下文的传播是因为一个名为 `DelegatingSecurityContextExecutorService` 的实现装饰了
`ExecutorService`。`DelegatingSecurityContextExecutorService` 还负责安全上下文的传播，如图 6.12 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202410282220623.png){ loading=lazy }
  <figcaption>图6.12：DelegatingSecurityContextExecutorService 装饰了一个 ExecutorService，并在提交任务之前将安全上下文信息传递给下一个线程。</figcaption>
</figure>

以下代码示例展示了如何使用 `DelegatingSecurityContextExecutorService` 来装饰 `ExecutorService`
，这样在提交任务时，它会负责传播安全上下文的详细信息。

``` java

@GetMapping("/hola")
public String hola() throws Exception {
	Callable<String> task = () -> {
		SecurityContext context = SecurityContextHolder.getContext();
		return context.getAuthentication().getName();
	};
	ExecutorService e = Executors.newCachedThreadPool();
	e = new DelegatingSecurityContextExecutorService(e);
	try {
		return "Hola, " + e.submit(task).get() + "!";
	} finally {
		e.shutdown();
	}
}
```

调用端点都用于测试 `DelegatingSecurityContextExecutorService` 是否正确委托了安全上下文：

```shell
curl -u user:5a5124cc-060d-40b1-8aad-753d3da28dca http://localhost:8080/hola
```

此调用的响应主体是:

```shell
Hola,user!
```

!!! note

    在与安全上下文并发支持相关的类中，您应记住表6.1中介绍的那些。

Spring提供了多种实用类的实现，可以在应用程序中用于管理安全上下文，尤其是在创建自定义线程时。在第6.2.4节中，你实现了
`DelegatingSecurityContextCallable`。在本节中，我们使用`DelegatingSecurityContextExecutorService`
。如果你需要为计划任务实现安全上下文传播，那么你会很高兴地知道，Spring Security还为你提供了一个名为
`DelegatingSecurityContextScheduledExecutorService`的装饰器。这个机制与本节介绍的
`DelegatingSecurityContextExecutorService`类似，不同之处在于它装饰了一个`ScheduledExecutorService`，使你能够处理计划任务。

此外，为了提供更大的灵活性，Spring Security 为您提供了一种更抽象的装饰器版本，称为 `DelegatingSecurityContextExecutor`
。这个类直接装饰了一个 `Executor`，它是线程池层次结构中最抽象的契约。当您希望能够用语言提供的任何选项替换线程池的实现时，可以选择在应用程序设计中使用它。

表6.1 负责将安全上下文委托给独立线程的对象（查看表格图）

| 类                                                 | 描述                                                                                      |
|---------------------------------------------------|-----------------------------------------------------------------------------------------|
| DelegatingSecurityContextExecutor                 | 实现了Executor接口，旨在为Executor对象添加功能，使其能够将安全上下文转发到其池创建的线程中。                                  |
| DelegatingSecurityContextExecutorService          | 实现了ExecutorService接口，旨在为ExecutorService对象添加功能，将安全上下文传递给其线程池创建的线程。                       |
| DelegatingSecurityContextScheduledExecutorService | 实现了 ScheduledExecutorService 接口，旨在为 ScheduledExecutorService 对象添加功能，将安全上下文传递给其线程池创建的线程。 |
| DelegatingSecurityContextRunnable                 | 实现了Runnable接口，表示在不同线程上执行的任务，不返回响应。除了普通的Runnable之外，它还可以传播安全上下文，以便在新线程上使用。                |
| DelegatingSecurityContextCallable                 | 实现了Callable接口，表示一个在不同线程上执行并最终返回响应的任务。除了普通的Callable之外，它还可以在新线程上传递安全上下文以供使用。              |

## 理解 HTTP 基本认证和基于表单的登录认证

到目前为止，我们只使用了HTTP Basic作为认证方法，但在本书中，你将了解到还有其他可能性。HTTP
Basic认证方法简单明了，非常适合用于示例、演示或概念验证。然而，正因为其简单性，它可能并不适合你需要实现的所有实际场景。

在本节中，您将学习更多与 HTTP Basic 相关的配置。此外，我们将讨论一种新的身份验证方法，称为 `formLogin`
。在本书的其余部分，我们将讨论其他身份验证方法，这些方法与不同类型的架构非常契合。我们将对它们进行比较，以便您了解最佳实践以及身份验证的反模式。

