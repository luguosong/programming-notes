# 6.实现身份认证

本章内容包括：

- 使用自定义 AuthenticationProvider 实现认证逻辑
- 采用 HTTP Basic 和表单登录方式进行身份验证
- 理解并管理 Security-Context 组件

---

第3章和第4章介绍了在认证流程中参与的一些组件。我们讨论了 UserDetails 以及如何定义原型来描述 Spring Security
中的用户。接着，我们在示例中使用了 UserDetails，展示了 UserDetailsService 和 UserDetailsManager
的接口规范及其实现方式。我们还讨论并演示了这些接口的主流实现。最后，你学习了 PasswordEncoder 如何管理密码及其使用方法，以及
Spring Security 加密模块（SSCM）中的加密器和密钥生成器的用法。

然而，AuthenticationProvider 层负责具体的认证逻辑。AuthenticationProvider 中定义了判断和处理是否认证请求的条件与指令。将这一职责委托给
AuthenticationProvider 的组件是 AuthenticationManager，它从 HTTP 过滤器层接收请求，这部分内容在第 5
章已经讨论过。在本章中，我们将深入了解认证流程，其结果只有两种可能：

- 发起请求的实体未通过认证。用户身份无法识别，应用会直接拒绝该请求，而不会进入授权流程。通常，这种情况下返回给客户端的响应状态码是
  HTTP 401 Unauthorized（未授权）。
- 发起请求的实体已通过认证。应用会保存请求者的详细信息，以便后续进行授权处理。正如你将在本章了解到的，SecurityContext
  负责管理当前已认证请求的相关信息。

为了帮助你回忆相关角色及其之间的关系，图6.1展示了我们在第2章已经见过的那张示意图。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511191600914.png){ loading=lazy }
  <figcaption>图 6.1 展示了 Spring Security 中的认证流程。该流程描述了应用程序识别提交请求用户的方式。本章重点关注的部分已在图中突出显示。在此流程中，AuthenticationProvider 负责执行认证操作，而 SecurityContext 用于保存已认证请求的相关信息。</figcaption>
</figure>

本章将介绍认证流程中剩余的部分（见图6.1中阴影框部分）。接下来的第7章和第8章，你将学习授权的相关内容，也就是在HTTP请求中紧随认证之后的处理流程。首先，我们需要讨论如何实现AuthenticationProvider接口。你需要了解Spring
Security在认证过程中是如何解析和处理请求的。

为了清晰地说明如何表示一个认证请求，我们将从Authentication接口开始讲解。在了解了它之后，我们会进一步探讨认证成功后，请求的详细信息会发生什么变化。接下来，我们将讨论SecurityContext接口，以及Spring
Security是如何管理它的。在本章的后半部分，你还将学习如何自定义HTTP Basic认证方式。此外，我们还会介绍另一种可用于应用程序的认证方式——基于表单的登录。

## 理解AuthenticationProvider

在企业级应用中，你可能会遇到这样一种情况：基于用户名和密码的默认认证实现并不适用。此外，在认证方面，你的应用可能还需要实现多种不同的场景（见图6.2）。例如，你可能希望用户能够通过短信收到的验证码，或者通过某个特定应用显示的验证码来证明身份。又或者，你需要实现这样的认证场景：用户必须提供存储在文件中的某种密钥。甚至有时候，你还需要利用用户的指纹信息来完成认证逻辑。一个框架的目标，就是要足够灵活，能够让你实现上述任意一种认证场景。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511191611109.png){ loading=lazy }
  <figcaption>图6.2 某些应用可能需要多种身份验证方式。虽然在大多数情况下，用户名和密码已经足够，但在某些场景下，用户认证过程可能会更加复杂。</figcaption>
</figure>

一个框架通常会提供一套最常用的实现方式，但当然无法覆盖所有可能的选项。以 Spring Security 为例，你可以通过实现
AuthenticationProvider 接口来定义任何自定义的认证逻辑。在本节中，你将学习如何通过实现 Authentication 接口来表示认证事件，并使用
AuthenticationProvider 创建自定义的认证逻辑。为实现这一目标：

- 在 6.1.1 节，我们将分析 Spring Security 是如何表示认证事件的。
- 在 6.1.2 节，我们会讨论负责认证逻辑的 AuthenticationProvider 接口。
- 在 6.1.3 节，你将通过一个示例，编写实现 AuthenticationProvider 接口的自定义认证逻辑。

### 在认证过程中表示请求

本节将讨论 Spring Security 在认证过程中是如何理解请求的。在深入实现自定义认证逻辑之前，了解这一点非常重要。正如你将在 6.1.2
节中了解到的，要实现自定义的 AuthenticationProvider，首先需要明白如何描述认证事件。在这里，我们将介绍用于表示认证的接口，并讲解你需要掌握的方法。

认证（Authentication）是同名流程中涉及的核心接口之一。Authentication
接口代表一次认证请求事件，并保存了请求访问应用程序实体的详细信息。在认证过程中以及认证完成后，你都可以使用与该认证请求事件相关的信息。请求访问应用程序的用户被称为主体（principal）。如果你曾在任何应用中使用过
Java Security，应该知道有一个名为 Principal 的接口，它代表的正是同样的概念。而 Spring Security 的 Authentication
接口则扩展了这一契约（见图 6.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511191635018.png){ loading=lazy }
  <figcaption>图 6.3 认证协议扩展了主体协议，并引入了更多规定，比如必须提供密码，或者可以进一步补充认证请求的相关信息。其中一些内容，如权限数组，是 Spring Security 特有的。</figcaption>
</figure>

在 Spring Security 中，Authentication 合约不仅代表一个主体（principal），还包含了认证过程是否完成的信息，以及一组权限（authorities）。该合约设计为扩展自
Java Security 的 Principal 合约，这在与其他框架和应用的实现兼容性方面是一个优势。这种灵活性使得从其他认证方式迁移到 Spring
Security 变得更加容易。

让我们通过下面的代码示例，进一步了解 Authentication 接口的设计。

```java title="清单6.1 Spring Security中声明的Authentication接口"
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

目前，你只需要了解这个接口中的以下几个方法：

- isAuthenticated() —— 如果认证流程已经结束，则返回 true；如果认证流程仍在进行中，则返回 false。
- getCredentials() —— 返回在认证过程中使用的密码或其他密钥信息。
- getAuthorities() —— 返回已认证请求所拥有的权限集合。

我们将在后续章节中，根据具体实现的需要，讨论 Authentication 合约中的其他方法。

### 实现自定义认证逻辑

本节将介绍如何实现自定义认证逻辑。我们会分析与此职责相关的 Spring Security 合约，以便深入理解其定义。掌握这些细节后，你可以参考
6.1.3 节中的代码示例，完成自定义认证逻辑的实现。

在 Spring Security 中，AuthenticationProvider 负责处理认证逻辑。AuthenticationProvider 接口的默认实现会将查找系统用户的任务委托给
UserDetailsService，同时在认证过程中还会使用 PasswordEncoder 进行密码管理。下面的代码展示了 AuthenticationProvider
的定义，如果你需要为自己的应用程序自定义认证提供者，可以参考这个定义。

```java title="清单 6.2 AuthenticationProvider 接口"
public interface AuthenticationProvider {

	Authentication authenticate(Authentication authentication)
			throws AuthenticationException;

	boolean supports(Class<?> authentication);
}
```

AuthenticationProvider 的职责与 Authentication 接口紧密相关。authenticate() 方法接收一个 Authentication 对象作为参数，并返回一个
Authentication 对象。我们通过实现 authenticate() 方法来定义认证逻辑。下面简要总结一下实现 authenticate() 方法的方式：

- 方法在认证失败时应抛出 AuthenticationException 异常。
- 如果方法接收到的认证对象不是你的 AuthenticationProvider 实现所支持的类型，则应返回 null。这样，我们就可以在 HTTP
  过滤器层面使用多种不同的认证类型。
- 方法应返回一个代表已完全认证对象的 Authentication 实例。对于该实例，isAuthenticated() 方法会返回
  true，并且包含所有关于已认证实体的必要信息。通常，应用程序还会从该实例中移除敏感数据，比如密码。认证成功后，密码已不再需要，保留这些信息可能会导致敏感数据泄露。

AuthenticationProvider 接口中的第二个方法是 supports(Class<?> authentication)。你可以通过实现这个方法，在当前
AuthenticationProvider 支持传入的 Authentication 对象类型时返回 true。需要注意的是，即使 supports 方法对某个对象返回了
true，authenticate() 方法仍然有可能通过返回 null 来拒绝该请求。Spring Security 的设计更加灵活，允许用户实现一个
AuthenticationProvider，不仅可以根据类型判断是否支持认证请求，还可以根据认证请求的具体细节来决定是否拒绝。

一个形象的比喻可以帮助理解认证管理器（authentication manager）和认证提供者（authentication
provider）如何协作来验证或拒绝一次认证请求：就像你的门上装了一个更复杂的锁。这把锁可以通过刷卡或者用传统的物理钥匙来打开（见图6.4）。锁本身就相当于认证管理器，负责决定是否开门。为了做出这个决定，它会把请求委托给两个认证提供者：一个负责验证门卡，另一个负责验证物理钥匙。如果你用门卡开门，那个只懂物理钥匙的认证提供者会表示它不支持这种认证方式。而另一个支持门卡认证的提供者则会检查这张卡是否有效。这正是
supports() 方法存在的意义。

除了测试认证类型之外，Spring Security 还增加了一层灵活性。就像门锁可以识别多种类型的门卡一样。当你刷卡时，其中一个认证提供者可能会说：“我能识别这是一张卡，但这不是我能验证的卡类型！”这种情况发生在
supports() 方法返回 true，而 authenticate() 方法返回 null 的时候。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511191723548.png){ loading=lazy }
  <figcaption>图6.4 AuthenticationManager 会将认证请求委托给可用的认证提供者之一。某些 AuthenticationProvider 可能并不支持当前的认证类型。不过，即使支持该对象类型，也可能无法对具体的对象进行认证。认证请求会被逐一评估，能够判断请求是否有效的 AuthenticationProvider 会向 AuthenticationManager 返回结果。</figcaption>
</figure>

图6.5展示了另一种情况：某个 AuthenticationProvider 对象识别出了 Authentication，但判定其无效。在这种情况下，系统会抛出一个
AuthenticationException，最终在 Web 应用的 HTTP 响应中返回 401 Unauthorized 状态码。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511191725784.png){ loading=lazy }
  <figcaption>图6.5 如果所有 AuthenticationProvider 对象都无法识别该 Authentication，或者其中任何一个拒绝它，最终将抛出 AuthenticationException 异常。</figcaption>
</figure>

### 应用自定义认证逻辑

在本节中，我们将实现自定义认证逻辑。你可以在项目 ssia-ch6-ex1 中找到这个示例。通过这个例子，你可以应用在第 6.1.1 和 6.1.2
节中学到的关于 Authentication 和 AuthenticationProvider 接口的知识。在代码清单 6.3 和 6.4 中，我们展示了如何实现一个自定义的
AuthenticationProvider。具体步骤如图 6.5 所示，主要包括以下内容：

1. 声明一个实现 AuthenticationProvider 接口的类。
2. 确定该 AuthenticationProvider 支持哪些类型的 Authentication 对象。
3. 实现 supports(Class<?> c) 方法，用于指定我们定义的 AuthenticationProvider 支持哪种类型的认证。
4. 实现 authenticate(Authentication a) 方法，编写具体的认证逻辑。
5. 最后，将新实现的 AuthenticationProvider 实例注册到 Spring Security 中。

```java title="清单6.3 重写 AuthenticationProvider 的 supports() 方法"

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

在代码清单6.3中，我们定义了一个实现了AuthenticationProvider接口的新类。我们使用@Component注解标记该类，以便Spring能够在其管理的上下文中创建该类型的实例。接下来，我们需要决定这个AuthenticationProvider支持哪种Authentication接口的实现类型。这取决于我们期望在authenticate()
方法中接收到的参数类型。如果我们没有在认证过滤器层面做任何自定义（正如第5章所讨论的），那么UsernamePasswordAuthenticationToken类就定义了这个类型。该类是Authentication接口的一个实现，代表了一个包含用户名和密码的标准认证请求。

通过这样的定义，我们让 AuthenticationProvider 支持特定类型的凭证。一旦明确了 AuthenticationProvider 的适用范围，我们只需重写
authenticate() 方法来实现具体的认证逻辑，如下所示。

```java title="清单 6.4 实现认证逻辑"

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

清单6.4中的逻辑非常简单，图6.6则直观地展示了这一逻辑。我们通过 UserDetailsService 的实现类获取
UserDetails。如果用户不存在，loadUserByUsername() 方法会抛出 AuthenticationException。此时，认证流程会终止，HTTP 过滤器会将响应状态设置为
HTTP 401 Unauthorized。如果用户名存在，我们会进一步使用上下文中的 PasswordEncoder 的 matches() 方法校验用户密码。如果密码不匹配，同样会抛出
AuthenticationException。如果密码正确，AuthenticationProvider 会返回一个标记为“已认证”的 Authentication 实例，其中包含了请求的详细信息。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511200930543.png){ loading=lazy }
  <figcaption>图6.6 AuthenticationProvider 执行定制化的认证流程。它通过特定的 UserDetailsService 实现类获取用户信息来确认认证请求，并使用 PasswordEncoder 校验密码是否正确。如果未找到用户或密码不正确，AuthenticationProvider 会抛出 AuthenticationException。</figcaption>
</figure>

为了接入新的 AuthenticationProvider 实现，我们需要定义一个 SecurityFilterChain Bean。具体示例如下：

```java title="代码清单 6.5 在配置类中注册 AuthenticationProvider"

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

	在代码清单6.5中，依赖注入用于一个通过 AuthenticationProvider 接口声明的字段。Spring 会识别 AuthenticationProvider 是一个接口（也就是一种抽象类型），但它也知道需要在自己的上下文中找到该接口的具体实现实例。在我们的例子中，实现类就是 CustomAuthenticationProvider，这是我们唯一声明并通过 @Component 注解添加到 Spring 上下文中的同类型实例。如果你需要回顾一下依赖注入的相关知识，我推荐阅读我写的另一本书《Spring 从零开始》（Manning, 2021）。

就是这样！你已经成功自定义了 AuthenticationProvider 的实现。现在，你可以根据需要自定义应用程序的认证逻辑了。

#### 应用设计如何走向失败

错误地应用框架会导致应用程序难以维护。更糟糕的是，有时候那些没有正确使用框架的人还会认为是框架本身的问题。让我给你讲个故事。

有一年冬天，我曾作为顾问与一家公司合作，期间他们的开发主管打电话请我协助实现一个新功能。他们需要在系统的某个组件中应用自定义的认证方式，而这个组件是用早期版本的
Spring 开发的。不幸的是，在进行应用类设计时，开发人员并没有很好地依赖 Spring Security 的核心架构。

他们只用了过滤器链，将 Spring Security 的整个功能都重新用自定义代码实现了一遍。

开发人员发现，随着时间的推移，组件的定制变得越来越困难。然而，没有人真正采取行动，去重新设计组件并按照 Spring Security
的预期契约来使用。造成困难的很大一部分原因，是大家对 Spring 的能力了解不够。某位主要开发者甚至说：“这都是 Spring Security
的错！这个框架太难用了，稍微想做点定制就特别麻烦。”听到他这么说，我有些吃惊。我知道 Spring Security
有时候确实不太容易理解，而且这个框架的学习曲线也不算平缓。但我从没遇到过用 Spring Security 设计一个易于定制的类却无从下手的情况！

我们一起调查了这个问题，我发现应用开发人员可能只用了 Spring Security 功能的十分之一。随后，我举办了为期两天的 Spring
Security 专题培训，重点讲解了针对他们需要修改的系统组件，我们可以做些什么以及具体如何实现。

最终，我们决定彻底重写大量自定义代码，改为正确地依赖 Spring Security，从而让应用更易于扩展，以满足他们对安全实现的需求。我们还发现了一些与
Spring Security 无关的其他问题，不过那就是另一个话题了。

以下是这个故事带给你的几点启示：

- 一个框架，尤其是那些在实际应用中被广泛使用的框架，往往是由许多聪明的人共同开发的，很难相信它会被糟糕地实现。在归咎于框架之前，务必先分析你的应用程序，确认问题是否真的源自框架本身。
- 在决定使用某个框架时，至少要确保你已经掌握了它的基本原理。
- 注意你用来学习框架的资源。有时候，网上的文章只是教你如何快速绕过问题，并不一定是正确的类设计实现方式。
- 研究时要参考多个信息来源。如果遇到疑惑，不妨自己写一个概念验证（Proof of Concept），以便理清思路。
- 如果你决定采用某个框架，尽量充分发挥它的原生功能。例如，如果你在使用 Spring Security
  时发现安全相关的实现大多是自己写的定制代码，而不是利用框架本身的能力，那你就应该思考一下原因。

当我们依赖框架所实现的功能时，可以享受到诸多优势。首先，这些功能经过充分测试，更新时引入漏洞的可能性较低。此外，优秀的框架通常基于抽象设计，有助于我们构建易于维护的应用程序。请记住，如果你选择自行实现这些功能，往往更容易引入安全漏洞。

## 使用SecurityContext

本节将讨论SecurityContext。我们会分析其工作原理、数据访问方式，以及应用在不同线程相关场景下如何管理SecurityContext。完成本节学习后，你将掌握在各种情况下配置SecurityContext的方法。这样，你就能在第7章和第8章配置授权时，灵活运用SecurityContext中存储的已认证用户信息。

在认证过程完成后，你很可能需要获取已认证实体的相关信息。例如，你可能需要访问当前已认证用户的用户名或权限。那么，这些信息在认证流程结束后还能获取吗？一旦
AuthenticationManager 成功完成认证，它会在本次请求的后续处理中保存 Authentication 实例（见图 6.7）。用于存储 Authentication
对象的实例被称为SecurityContext（security context）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201022276.png){ loading=lazy }
  <figcaption>图6.7 认证成功后，认证过滤器会将已认证实体的详细信息存储到SecurityContext中。之后，映射到该请求的控制器在需要时即可访问这些信息。</figcaption>
</figure>

Spring Security 的SecurityContext由 SecurityContext 接口描述，具体定义如下所示。

```java title="清单6.6 SecurityContext接口"
public interface SecurityContext extends Serializable {

	Authentication getAuthentication();

	void setAuthentication(Authentication authentication);
}
```

正如你从契约定义中可以看出，SecurityContext 的主要职责是存储 Authentication 对象。那么，SecurityContext 本身是如何被管理的呢？Spring
Security 提供了三种策略，通过一个管理者角色的对象来管理 SecurityContext，这个对象被称为 SecurityContextHolder：

- `MODE_THREADLOCAL`——允许每个线程在SecurityContext中存储自己的信息。在每个请求对应一个线程的 Web
  应用中，这是一种常见的做法，因为每个请求都会分配一个独立的线程。
- `MODE_INHERITABLETHREADLOCAL`——与 MODE_THREADLOCAL 类似，但它还会在异步方法的情况下，将SecurityContext复制到下一个线程。这样，运行
  @Async 方法的新线程就可以继承原有的SecurityContext。@Async 注解用于方法上，指示 Spring 在单独的线程中调用被注解的方法。
- `MODE_GLOBAL`——让应用中的所有线程都共享同一个SecurityContext实例。

除了 Spring Security 提供的这三种管理SecurityContext的策略之外，本节还介绍了当你自己定义的线程不被 Spring
所识别时会发生什么情况。你会了解到，在这种情况下，需要手动将SecurityContext中的信息复制到新线程中。Spring Security 无法自动管理那些不在
Spring 上下文中的对象，但它也提供了一些非常实用的工具类。

### 为SecurityContext采用持有策略

管理SecurityContext的第一种策略是 MODE_THREADLOCAL 策略，这也是 Spring Security 默认采用的SecurityContext管理方式。在这种策略下，Spring
Security 通过 ThreadLocal 来管理上下文。ThreadLocal 是 JDK
提供的一种实现方式，它本质上是一个数据集合，但能够确保应用中的每个线程只能访问属于自己那一部分的数据。这样，每个请求都能访问到自己的SecurityContext，线程之间无法访问彼此的
ThreadLocal 数据。也就是说，在 Web 应用中，每个请求只能看到自己的SecurityContext。对于后端 Web 应用来说，这通常也是我们希望实现的效果。

图 6.8 展示了该功能的整体概览。每个请求（A、B 和 C）都有自己分配的线程（T1、T2 和
T3），因此每个请求只能访问存储在其自身SecurityContext中的信息。然而，这也意味着如果创建了一个新线程（例如调用异步方法时），新线程也会拥有自己的SecurityContext。父线程（即请求的原始线程）中的信息不会被复制到新线程的SecurityContext中。

!!! note

	这里我们讨论的是传统的 Servlet 应用，每个请求都对应一个线程。这种架构仅适用于每个请求分配独立线程的传统 Servlet 应用，不适用于响应式应用。关于响应式架构的安全性，我们将在第 17 章进行详细探讨。

作为管理SecurityContext的默认策略，这一过程无需显式配置。在认证流程结束后，只需在需要的地方通过静态的 getContext()
方法从持有者处获取SecurityContext即可。在代码清单6.7中，你可以看到在某个应用端点获取SecurityContext的示例。通过SecurityContext，你还可以进一步获取
Authentication 对象，该对象用于存储已认证实体的详细信息。本节讨论的示例可以在项目 ssia-ch6-ex2 中找到。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201037267.png){ loading=lazy }
  <figcaption>图 6.8 每个请求都有自己独立的线程，用箭头表示。每个线程只能访问属于自己的SecurityContext信息。当创建新线程时（例如通过 @Async 方法），父线程中的SecurityContext信息不会被复制到新线程中。</figcaption>
</figure>

```java title="清单 6.7 从 SecurityContextHolder 获取 SecurityContext"

@GetMapping("/hello")
public String hello() {
	SecurityContext context = SecurityContextHolder.getContext();
	Authentication a = context.getAuthentication();

	return "Hello, " + a.getName() + "!";
}
```

在端点层面获取认证信息更加方便，因为Spring会直接将其注入到方法参数中，无需每次都显式引用SecurityContextHolder类。如下所示，这种方式更加优雅。

```java title="清单6.8 Spring在方法参数中注入Authentication值"

@GetMapping("/hello")
public String hello(Authentication a) {
	return "Hello, " + a.getName() + "!";
}

```

当使用正确的用户调用该接口时，响应体中会包含用户名。例如：

```shell
curl -u user:99ff79e3-8ca0-401c-a396-0a8625ab3bad http://localhost:8080/hello
Hello, user!
```

### 对异步调用采用持有策略

在大多数情况下，采用默认的SecurityContext管理策略非常方便，而且通常已经足够用了。MODE_THREADLOCAL
能够为每个线程隔离SecurityContext，使得SecurityContext的理解和管理更加直观。不过，也存在一些场景，这种方式并不适用。

如果每个请求需要处理多个线程，情况就会变得更加复杂。假如你把接口改成异步的，执行方法的线程就不再是处理请求的那个线程了。可以看看下面这个代码示例中的接口，思考一下会发生什么。

```java title="清单6.9 由不同线程执行的 @Async 方法"

@GetMapping("/bye")
@Async
public void goodbye() {
	SecurityContext context = SecurityContextHolder.getContext();
	String username = context.getAuthentication().getName();

	// do something with the username
}

```

为了启用 @Async 注解的功能，我还创建了一个配置类，并在其上添加了 @EnableAsync 注解：

```java

@Configuration
@EnableAsync
public class ProjectConfig {
}
```

!!! note

	有时候在文章或论坛中，你会看到一些配置注解直接写在主类上。比如，有些示例会把 @EnableAsync 注解直接加在主类上。从技术角度来说，这种做法没错，因为我们通常会在 Spring Boot 应用的主类上加上 @SpringBootApplication 注解，而这个注解本身就包含了 @Configuration 的特性。不过，在实际项目中，我们更倾向于将各自的职责分开，主类一般不会作为配置类来使用。为了让本书中的示例更加清晰，我会把这些注解放在专门的 @Configuration 类上，这也是实际开发中更常见的做法。

如果你现在直接运行这段代码，会在获取 authentication 的 name 时抛出 NullPointerException，具体是在这一行：

``` java
String username = context.getAuthentication().getName()
```

这是因为该方法现在在另一个线程上执行，而该线程并未继承SecurityContext。因此，Authorization对象为null，在当前代码环境下会导致NullPointerException。在这种情况下，你可以通过使用MODE_INHERITABLETHREADLOCAL策略来解决这个问题。可以通过调用SecurityContextHolder.setStrategyName()
方法，或者设置系统属性spring.security.strategy来指定该策略。设置此策略后，框架会将请求原始线程的SecurityContext信息复制到异步方法新创建的线程中（见图6.9）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201357522.png){ loading=lazy }
  <figcaption>图 6.9 当使用 MODE_INHERITABLETHREADLOCAL 时，框架会将请求原始线程中的SecurityContext信息复制到新线程的SecurityContext中。</figcaption>
</figure>

下面的代码展示了如何通过调用 setStrategyName() 方法来设置SecurityContext管理策略。

```java title="清单6.10 使用 InitializingBean 设置 SecurityContextHolder 模式"

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

调用该端点后，您会发现 Spring 能够正确地将SecurityContext传递到下一个线程。此外，Authentication 也不再为 null。

!!! note

 	这只有在框架自行创建线程时才有效（比如使用 @Async 方法的情况）。如果你的代码自己创建线程，即使采用 MODE_INHERITABLETHREADLOCAL 策略，也会遇到同样的问题。原因在于，这种情况下框架并不了解你代码所创建的线程。我们将在第 6.2.4 和 6.2.5 节讨论如何解决这些问题。

### 为独立应用采用持有策略

如果你需要的是一个在所有应用线程间共享的SecurityContext，可以将策略更改为 MODE_GLOBAL（见图 6.10）。这种策略并不适用于 Web
服务器，因为它不符合 Web 应用的整体架构。后端 Web
应用会独立处理每个收到的请求，因此将SecurityContext按请求分离，比为所有请求共用一个上下文更合理。不过，对于独立应用来说，这种策略依然是一个不错的选择。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201400647.png){ loading=lazy }
  <figcaption>图6.10 当使用 MODE_GLOBAL 作为SecurityContext管理策略时，所有线程访问的是同一个SecurityContext。这意味着所有线程都能访问相同的数据，并且可以修改这些信息。因此，可能会出现竞态条件，你必须注意同步问题。</figcaption>
</figure>


如下代码片段所示，您可以像使用 MODE_INHERITABLETHREADLOCAL 一样切换策略。您可以通过调用
SecurityContextHolder.setStrategyName() 方法，或设置系统属性 spring.security.strategy 来实现。

```java

@Bean
public InitializingBean initializingBean() {
	return () -> SecurityContextHolder.setStrategyName(
			SecurityContextHolder.MODE_GLOBAL);
}
```

另外，需要注意的是，SecurityContext 并不是线程安全的。因此，在这种所有应用线程都可以访问 SecurityContext
对象的策略下，你需要特别注意并发访问的问题。

### 使用DelegatingSecurityContextRunnable转发SecurityContext

你已经了解到，Spring Security 提供了三种模式来管理SecurityContext：`MODE_THREADLOCAL`、`MODE_INHERITEDTHREADLOCAL` 和
`MODE_GLOBAL`
。默认情况下，框架只会为请求线程提供SecurityContext，并且这个SecurityContext仅对该线程可访问。然而，框架并不会自动处理新创建的线程（比如异步方法中的线程）。因此，在这种情况下，你需要显式地设置不同的SecurityContext管理模式。不过，这里还有一个特殊情况：如果你的代码在没有被框架感知的情况下启动了新线程，会发生什么？有时候我们把这些线程称为
`自管理线程`，因为它们是由我们自己管理的，而不是由框架管理的。在本节中，我们将介绍 Spring Security
提供的一些实用工具，帮助你将SecurityContext传递到新创建的线程中。

SecurityContextHolder 的任何特定策略都无法为你提供自管理线程的解决方案。在这种情况下，你需要自行处理SecurityContext的传递。一个解决办法是使用
DelegatingSecurityContextRunnable 来包装你希望在独立线程上执行的任务。DelegatingSecurityContextRunnable 继承自
Runnable，可以在不需要返回值的任务执行时使用。如果你的任务有返回值，则可以使用 Callable<T>
的替代方案——DelegatingSecurityContextCallable<T>。这两个类都代表异步执行的任务，与普通的 Runnable 或 Callable
类似。此外，它们能够确保将当前的SecurityContext复制到执行任务的线程上。正如图 6.11 所示，这些对象会包装原始任务，并将SecurityContext复制到新线程中。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201409149.png){ loading=lazy }
  <figcaption>图 6.11 DelegatingSecurityContextCallable 被设计为 Callable 对象的装饰器。在构建该对象时，你需要提供应用程序异步执行的 callable 任务。DelegatingSecurityContextCallable 会将SecurityContext中的相关信息复制到新线程，然后再执行任务。</figcaption>
</figure>

下面的代码示例展示了如何使用 DelegatingSecurityContextCallable。我们先定义一个简单的接口方法，该方法声明了一个 Callable
对象。这个 Callable 任务会从当前的SecurityContext中返回用户名。

```java title="代码清单6.11 定义一个 Callable 对象并在独立线程中作为任务执行"

@GetMapping("/ciao")
public String ciao() throws Exception {
	Callable<String> task = () -> {
		SecurityContext context = SecurityContextHolder.getContext();
		return context.getAuthentication().getName();
	};

	// Omitted code
}
```

我们继续之前的示例，将任务提交给 ExecutorService。执行结果会被获取，并作为响应体由接口返回。

```java title="代码清单6.12 定义 ExecutorService 并提交任务"

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

如果你直接运行这个应用程序，只会得到一个 NullPointerException。在新创建的线程中执行 callable
任务时，认证信息已经不存在，SecurityContext也是空的。为了解决这个问题，我们可以用 DelegatingSecurityContextCallable
对任务进行包装，这样就能把当前的SecurityContext传递到新线程中，具体实现如下所示。

```java title="清单 6.13 运行由 DelegatingSecurityContextCallable 装饰的任务"

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

现在调用该端点时，你会发现 Spring 已经将SecurityContext传递到了任务执行的线程中：

```shell
curl -u user:2eb3f2e8-debd-420c-9680-48159b2ff905 http://localhost:8080/ciao
```

本次调用的响应体为

```shell
Ciao, user!
```

### 使用DelegatingSecurityContextExecutorService转发SecurityContext

当我们在代码中自行启动线程，而没有让框架知晓这些线程时，就需要手动管理SecurityContext信息在不同线程之间的传递。在第6.2.4节中，你已经通过任务本身实现了从SecurityContext复制相关信息的技巧。Spring
Security 提供了一些非常实用的工具类，比如 DelegatingSecurityContextRunnable 和
DelegatingSecurityContextCallable。这些类可以包装你异步执行的任务，并负责将SecurityContext中的信息复制到新创建的线程中，从而让你的实现能够在新线程中访问这些信息。

不过，除了通过任务本身来管理SecurityContext的传递之外，还有另一种选择，就是从线程池的角度来处理SecurityContext的传播。在本节中，你将学习如何利用
Spring Security 提供的更多实用工具类，通过线程池来实现SecurityContext的传递。

另一种为任务添加装饰的方法是使用特定类型的 Executor。在下一个示例中，你会发现任务依然只是一个简单的 Callable<T>
，但线程依然能够管理SecurityContext。这是因为有一个名为 DelegatingSecurityContextExecutorService 的实现对 ExecutorService
进行了装饰，从而实现了SecurityContext的传递。DelegatingSecurityContextExecutorService 还负责SecurityContext的传播，如图 6.12 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201438228.png){ loading=lazy }
  <figcaption>图 6.12：DelegatingSecurityContextExecutorService 对 ExecutorService 进行包装，在提交任务前将SecurityContext信息传递到下一个线程。</figcaption>
</figure>

下面的代码示例演示了如何使用 DelegatingSecurityContextExecutorService 对 ExecutorService
进行装饰，这样在提交任务时，SecurityContext的信息会自动进行传递和处理。

```java title="清单 6.14 传递 SecurityContext"

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

调用该端点以测试 DelegatingSecurityContextExecutorService 是否正确地委托了SecurityContext：

```shell
curl -u user:5a5124cc-060d-40b1-8aad-753d3da28dca http://localhost:8080/hola
```

本次调用的响应体为

```shell
Hola, user!
```

!!! note

	与SecurityContext并发支持相关的类中，表6.1中列出的那些类值得重点关注。

Spring 提供了多种实用类的实现，可以在你的应用中用于管理SecurityContext，尤其是在你自己创建线程时。在第 6.2.4 节中，你已经实现了
DelegatingSecurityContextCallable。本节我们将使用
DelegatingSecurityContextExecutorService。如果你需要为定时任务实现SecurityContext的传递，那么你会很高兴地发现，Spring Security
还为你提供了一个名为 DelegatingSecurityContextScheduledExecutorService 的装饰器。它的机制与本节介绍的
DelegatingSecurityContextExecutorService 类似，不同之处在于它是对 ScheduledExecutorService 进行装饰，使你能够方便地处理定时任务。

此外，为了提供更高的灵活性，Spring Security 还为你提供了一个更抽象的装饰器——DelegatingSecurityContextExecutor。这个类直接装饰了
Executor，这是线程池层级中最抽象的接口。当你希望能够根据实际需求，随时替换为任何语言支持的线程池实现时，可以选择使用它来设计你的应用程序。

| Class                                             | 描述                                                                                          |
|---------------------------------------------------|---------------------------------------------------------------------------------------------|
| DelegatingSecurityContextExecutor                 | 实现了 Executor 接口，旨在为 Executor 对象添加装饰功能，使其能够将SecurityContext传递到线程池中创建的线程。                               |
| DelegatingSecurityContextExecutorService          | 实现了ExecutorService接口，旨在为ExecutorService对象增加功能，使其能够将SecurityContext传递到线程池中创建的线程。                       |
| DelegatingSecurityContextScheduledExecutorService | 实现了 ScheduledExecutorService 接口，旨在为 ScheduledExecutorService 对象增加功能，使其能够将SecurityContext传递到线程池中创建的线程。 |
| DelegatingSecurityContextRunnable                 | 实现了 Runnable 接口，表示一个在不同线程上执行的任务，但不会返回结果。相比普通的 Runnable，它还可以将SecurityContext传递到新线程中使用。                 |
| DelegatingSecurityContextCallable                 | 实现了 Callable 接口，表示一个在不同线程上执行并最终返回结果的任务。相比普通的 Callable，它还可以将SecurityContext传递到新线程中使用。                  |

## 理解HTTP Basic认证与表单登录认证

到目前为止，我们只使用了 HTTP Basic 作为认证方式，但在本书的后续内容中，你会了解到还有其他可选方案。HTTP Basic
认证方法非常简单，因此非常适合用作示例、演示或概念验证。但也正因为它的简单性，在实际应用场景中，可能并不总是最合适的选择。

在本节中，您将进一步了解与 HTTP Basic
相关的更多配置。此外，我们还会介绍一种新的认证方式——formLogin。在本书接下来的内容中，我们还会探讨其他认证方法，这些方法适用于不同类型的架构。我们将对它们进行比较，帮助您掌握最佳实践以及认证过程中的一些反模式。

### 使用和配置HTTP Basic

你已经知道，HTTP Basic 是默认的认证方式，我们在第三章的多个示例中已经见识过它的工作原理。本节将进一步详细介绍这种认证方式的配置方法。

对于理论场景来说，HTTP Basic
认证自带的默认设置已经非常不错了。然而，在更复杂的应用中，你可能会发现需要对这些设置进行一些自定义。例如，你可能希望在认证失败时实现特定的处理逻辑，甚至需要在返回给客户端的响应中设置一些特定的值。下面我们通过实际示例来探讨这些情况，帮助你了解如何实现这些功能。我还想再次强调，你可以像下面的代码所示那样，显式地设置这个方法。你可以在项目
ssia-ch6-ex3 中找到这个示例。

```java title="清单6.15 设置 HTTP Basic 认证方式"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		return http.build();
	}
}
```

你可以调用 HttpSecurity 实例的 httpBasic() 方法，并传入一个 Customizer 类型的参数。通过这个参数，你可以配置与认证方式相关的一些设置，比如设置
realm 名称，如代码清单 6.16 所示。你可以将 realm 理解为使用特定认证方式的保护空间。关于 realm 的详细说明，可以参考 RFC
2617：[https://tools.ietf.org/html/rfc2617](https://datatracker.ietf.org/doc/html/rfc2617)。

```java title="清单6.16 配置认证失败响应的领域名称"

@Bean
public SecurityFilterChain configure(HttpSecurity http)
		throws Exception {

	http.httpBasic(c -> {
		c.realmName("OTHER");
		c.authenticationEntryPoint(new CustomEntryPoint());
	});

	http.authorizeHttpRequests(c -> c.anyRequest().authenticated());

	return http.build();
}
```

清单6.16展示了一个修改领域名称的示例。这里使用的lambda表达式实际上是一个类型为 Customizer<
HttpBasicConfigurer<HttpSecurity>> 的对象。参数类型为 HttpBasicConfigurer<HttpSecurity>，允许我们调用 realmName()
方法来重命名领域。你可以使用 cURL 命令并加上 -v 参数，以获取详细的 HTTP 响应，其中领域名称确实已经被修改。不过需要注意的是，只有当
HTTP 响应状态为 401 Unauthorized 时，响应中才会包含 WWW-Authenticate 头部，而当响应状态为 200 OK 时则不会出现该头部。以下是调用
cURL 的示例：

```shell
curl -v http://localhost:8080/hello
```

调用的响应是

```shell
/
...
< WWW-Authenticate: Basic realm="OTHER"
...
```

此外，通过使用定制器（Customizer），我们可以自定义认证失败时的响应。如果你的系统客户端在认证失败时对响应内容有特定要求，就需要这样做。你可能需要添加或移除某些响应头，或者可以通过一些逻辑过滤响应体，确保应用不会向客户端泄露任何敏感数据。

!!! note

	始终要谨慎对待向系统外部暴露的数据。最常见的错误之一（这也是 OWASP 十大安全漏洞之一，详见 [https://owasp.org/www-project-top-ten/](https://owasp.org/www-project-top-ten/)）就是泄露敏感数据。在处理应用程序因身份验证失败而返回给客户端的详细信息时，始终存在泄露机密信息的风险。

要自定义认证失败时的响应，我们可以实现一个 AuthenticationEntryPoint。它的 commence() 方法会接收
HttpServletRequest、HttpServletResponse 以及导致认证失败的 AuthenticationException。代码清单 6.17 展示了一种实现
AuthenticationEntryPoint 的方式，该实现会在响应中添加一个 header，并将 HTTP 状态码设置为 401 Unauthorized（未授权）。

!!! note

	AuthenticationEntryPoint 接口的名称有些模糊，并没有直接体现它在认证失败时的用途。在 Spring Security 架构中，这个接口实际上是由一个名为 ExceptionTranslationManager 的组件直接使用的。ExceptionTranslationManager 负责处理在过滤器链中抛出的 AccessDeniedException 和 AuthenticationException。你可以把 ExceptionTranslationManager 看作是 Java 异常与 HTTP 响应之间的桥梁。

```java title="清单6.17 实现 AuthenticationEntryPoint"
public class CustomEntryPoint
		implements AuthenticationEntryPoint {

	@Override
	public void commence(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			AuthenticationException e)
			throws IOException, ServletException {

		httpServletResponse
				.addHeader("message", "Luke, I am your father!");
		httpServletResponse
				.sendError(HttpStatus.UNAUTHORIZED.value());

	}
}
```

然后，您可以在配置类中将 CustomEntryPoint 注册到 HTTP Basic 认证方式。下面展示了用于自定义入口点的配置类示例。

```java title="清单 6.18 设置自定义 AuthenticationEntryPoint"

@Bean
public SecurityFilterChain configure(HttpSecurity http)
		throws Exception {

	http.httpBasic(c -> {
		c.realmName("OTHER");
		c.authenticationEntryPoint(new CustomEntryPoint());
	});

	http.authorizeHttpRequests().anyRequest().authenticated();

	return http.build();
}
```

如果你现在调用某个接口并使认证失败，你应该能在响应中看到新添加的请求头：

```shell
curl -v http://localhost:8080/hello
```

调用的响应是

```shell
...
< HTTP/1.1 401
< Set-Cookie: JSESSIONID=459BAFA7E0E6246A463AD19B07569C7B; Path=/; HttpOnly
< message: Luke, I am your father!
...
```

### 使用表单登录实现身份认证

在开发 Web 应用程序时，你可能希望为用户提供一个友好的登录表单，让他们输入自己的凭证。此外，你还可能希望经过身份验证的用户在登录后能够自由浏览各个网页，并且可以随时注销。对于一个小型
Web 应用，可以采用基于表单的登录方式。在本节中，你将学习如何为你的应用配置并应用这种认证方式。为此，我们将编写一个使用表单登录的小型
Web 应用。图 6.13 展示了我们将要实现的流程。本节中的示例均来自项目 ssia-ch6-ex4。

!!! note

	我把这种方法与一个小型 Web 应用联系在一起，因为这样我们可以使用服务端会话来管理SecurityContext。但对于需要横向扩展的大型应用来说，使用服务端会话来管理SecurityContext并不可取。在第12到第15章中，我们在讲解 OAuth 2 时会更详细地讨论这些内容。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201527849.png){ loading=lazy }
  <figcaption>图 6.13 使用基于表单的登录。尚未通过身份验证的用户会被重定向到登录表单，输入凭证进行登录。应用程序在验证其身份后，会将用户引导至主页面。</figcaption>
</figure>

要将认证方式切换为表单登录，可以在 SecurityFilterChain Bean 的 HttpSecurity 对象上调用 formLogin() 方法，而不是使用
httpBasic()。下面的代码展示了这一修改。

```java title="清单6.19 将认证方式更改为表单登录"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.formLogin(Customizer.withDefaults());

		http.authorizeHttpRequests(c -> c.anyRequest().authenticated());

		return http.build();
	}
}
```

即使只进行了最基本的配置，Spring Security 也已经为你的项目自动设置好了登录表单和注销页面。启动应用并用浏览器访问时，系统会自动将你重定向到登录页面（见图
6.14）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201528840.png){ loading=lazy }
  <figcaption>图 6.14 使用 formLogin() 方法时，Spring Security 自动配置的默认登录页面</figcaption>
</figure>

只要你没有注册自己的 UserDetailsService，就可以使用默认提供的账号登录。正如我们在第二章中了解到的，默认用户名是
user，密码则是应用启动时在控制台打印出来的 UUID。由于没有定义其他页面，登录成功后会被重定向到一个默认的错误页面。该应用在认证方面依然采用了我们之前示例中用到的架构。因此，如图
6.14 所示，你需要为应用首页实现一个控制器。不同之处在于，这次我们希望接口返回的是 HTML 页面，而不是简单的 JSON
格式响应，这样浏览器才能将其作为网页进行解析。基于这个需求，我们选择遵循 Spring MVC 的流程，在控制器执行完相关操作后，从文件中渲染视图。图
6.15 展示了 Spring MVC 渲染应用首页的流程。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201535434.png){ loading=lazy }
  <figcaption>图6.15 展示了Spring MVC流程的简要示意图。调度器会根据给定的路径（本例中为 /home）找到对应的控制器方法。控制器方法执行完毕后，视图被渲染，最终响应返回给客户端。</figcaption>
</figure>

要在应用程序中添加一个简单的页面，首先需要在项目的 resources/static 文件夹下创建一个 HTML 文件。我将这个文件命名为
home.html。在文件中输入一些你之后可以在浏览器中看到的文本，比如添加一个标题（例如：`<h1>Welcome</h1>`）。创建好 HTML
页面后，还需要在控制器中定义路径到视图的映射。下面的代码展示了在控制器类中为 home.html 页面定义的处理方法。

```java title="代码清单6.20 为 home.html 页面定义控制器的 action 方法"

@Controller
public class HelloController {

	@GetMapping("/home")
	public String home() {
		return "home.html";
	}
}
```

请注意，这里使用的是 @Controller 而不是 @RestController。因此，Spring 并不会将方法返回值直接作为 HTTP
响应发送回去，而是会根据返回值查找并渲染名为 home.html 的视图。

现在尝试访问 /home 路径时，系统会首先询问你是否要登录。登录成功后，你会被重定向到主页，并看到欢迎信息。此时你可以访问 /logout
路径，这会将你重定向到登出页面（见图 6.16）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201537212.png){ loading=lazy }
  <figcaption>图6.16 由Spring Security为表单登录认证方式配置的注销页面</figcaption>
</figure>

当用户在未登录的情况下尝试访问某个路径时，系统会自动将其重定向到登录页面。登录成功后，应用会将用户重新导向到其最初尝试访问的路径。如果该路径不存在，系统则会显示默认的错误页面。formLogin()
方法会返回一个类型为 FormLoginConfigurer<HttpSecurity> 的对象，方便我们进行自定义配置。例如，你可以通过调用
defaultSuccessUrl() 方法来实现，如下所示。

```java title="清单6.21 为登录表单设置默认成功跳转URL"

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
		throws Exception {

	http.formLogin(c -> c.defaultSuccessUrl("/home", true));

	http.authorizeHttpRequests(c -> c.anyRequest().authenticated());

	return http.build();
}
```

如果你需要更深入地定制认证流程，可以使用 AuthenticationSuccessHandler 和 AuthenticationFailureHandler
对象，这两种方式能够为你提供更细致的自定义能力。这些接口允许你实现一个对象，通过它可以编写自定义的认证逻辑。如果你想自定义认证成功后的处理逻辑，可以定义一个
AuthenticationSuccessHandler。其 onAuthenticationSuccess() 方法会接收 servlet 请求、servlet 响应以及 Authentication
对象作为参数。下面的代码示例展示了如何实现 onAuthenticationSuccess() 方法，根据已登录用户所拥有的权限进行不同的重定向操作。

```java title="清单6.22 实现 AuthenticationSuccessHandler"

@Component
public class CustomAuthenticationSuccessHandler
		implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			Authentication authentication)
			throws IOException {

		var authorities = authentication.getAuthorities();

		var auth =
				authorities.stream()
						.filter(a -> a.getAuthority().equals("read"))
						.findFirst();

		if (auth.isPresent()) {
			httpServletResponse
					.sendRedirect("/home");
		} else {
			httpServletResponse
					.sendRedirect("/error");
		}
	}
}

```

在实际场景中，有时客户端在认证失败时会期望响应采用特定的格式。他们可能希望收到的 HTTP 状态码不是 401
Unauthorized，而是其他状态码，或者希望响应体中包含额外的信息。最常见的情况是应用程序会发送一个请求标识符。这个请求标识符具有唯一值，用于在多个系统之间追踪请求，应用程序可以在认证失败时将其包含在响应体中。还有一种情况是你希望对响应进行处理，确保应用不会向系统外泄露敏感数据。你也可能希望为认证失败定义自定义逻辑，比如仅记录事件以便后续调查。

如果你希望自定义应用在认证失败时执行的逻辑，可以通过实现 AuthenticationFailureHandler
来实现，方式类似。例如，如果你想在认证失败时添加一个特定的响应头，可以参考代码清单 6.23 的做法。当然，你也可以在这里实现任何你需要的逻辑。对于
AuthenticationFailureHandler，onAuthenticationFailure() 方法会接收请求、响应以及 Authentication 对象。

```java title="清单6.23 实现 AuthenticationFailureHandler"

@Component
public class CustomAuthenticationFailureHandler
		implements AuthenticationFailureHandler {

	@Override
	public void onAuthenticationFailure(
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse,
			AuthenticationException e) {

		try {

			httpServletResponse.setHeader("failed",
					LocalDateTime.now().toString());
			httpServletResponse.sendRedirect("/error");

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
```

要使用这两个对象，需要在 formLogin() 方法返回的 FormLoginConfigurer 对象上的 securityFilterChain()
方法中进行注册。下面的代码示例展示了具体操作方法。

```java title="清单6.24 在配置类中注册处理器对象"

@Configuration
public class ProjectConfig {

	private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
	private final CustomAuthenticationFailureHandler authenticationFailureHandler;

// Omitted constructor

	@Bean
	public UserDetailsService uds() {
		var uds = new InMemoryUserDetailsManager();

		uds.createUser(
				User.withDefaultPasswordEncoder()
						.username("john")
						.password("12345")
						.authorities("read")
						.build()
		);

		uds.createUser(
				User.withDefaultPasswordEncoder()
						.username("bill")
						.password("12345")
						.authorities("write")
						.build()
		);

		return uds;
	}

	@Bean
	public SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.formLogin(c ->
				c.successHandler(authenticationSuccessHandler)
						.failureHandler(authenticationFailureHandler)
		);

		http.authorizeHttpRequests(c -> c.anyRequest().authenticated());

		return http.build();
	}
}
```

目前，如果你尝试使用正确的用户名和密码通过 HTTP Basic 方式访问 /home 路径，应用会返回一个 HTTP 302 Found
状态的响应。这表示应用正在进行重定向。即使你输入了正确的用户名和密码，系统也不会认可，而是会按照 formLogin
方法的要求，将你重定向到登录表单。不过，你可以通过修改配置，让应用同时支持 HTTP Basic 和基于表单的登录方式，如下所示。

```java title="清单6.25 同时使用基于表单的登录和HTTP Basic认证"

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
		throws Exception {

	http.formLogin(c ->

			c.successHandler(authenticationSuccessHandler)
					.failureHandler(authenticationFailureHandler)
	);

	http.httpBasic(Customizer.withDefaults());

	http.authorizeHttpRequests(c -> c.anyRequest().authenticated());

	return http.build();
}
```

现在，无论是通过表单登录还是 HTTP Basic 认证方式，都可以正常访问 /home 路径。

```shell
curl -u user:cdd430f6-8ebc-49a6-9769-b0f3ce571d19 http://localhost:8080/home
```

调用的响应是

```shell
<h1>Welcome</h1>
```

## 总结

- AuthenticationProvider 是用于实现自定义认证逻辑的组件。
- 在实现自定义认证逻辑时，保持各项职责的解耦是一种良好的实践。对于用户管理，AuthenticationProvider 会委托给
  UserDetailsService；而密码校验的职责，则委托给 PasswordEncoder。
- SecurityContext 在认证成功后会保存已认证实体的相关信息。
- 管理SecurityContext有三种策略可选：MODE_THREADLOCAL、MODE_INHERITABLETHREADLOCAL 和 MODE_GLOBAL。不同模式下，跨线程访问SecurityContext的方式也有所不同。
- 需要注意的是，共享线程本地模式仅适用于由 Spring 管理的线程。对于非 Spring 管理的线程，框架不会自动复制SecurityContext。
- Spring Security 提供了实用的工具类，方便你管理由自己代码创建的线程（这些线程 Spring 框架本身并不知晓）。你可以使用以下类来管理你自定义线程的
  SecurityContext：
	- DelegatingSecurityContextRunnable
	- DelegatingSecurityContextCallable
	- DelegatingSecurityContextExecutor
- Spring Security 会自动配置登录表单以及基于表单的登录认证方法 formLogin()，同时也支持登出选项。对于开发小型 Web
  应用来说，这种方式非常简单易用。
- formLogin 认证方法高度可定制，并且可以与 HTTP Basic 认证方式结合使用。
