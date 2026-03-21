# 7.配置端点级授权：限制访问

本章内容包括：

- 定义权限和角色
- 在端点上应用授权规则

几年前，我在美丽的喀尔巴阡山滑雪时，目睹了一幕有趣的场景。大约有十几个人正在排队，准备进入缆车前往滑雪道顶端。这时，一位知名流行歌手在两名保镖的陪同下出现了。他自信满满地走上前，显然觉得自己很有名，可以直接插队。可当他走到队伍最前面时，却被工作人员拦住了。“请出示您的票！”工作人员说道，随后又解释道：“首先，您需要有票；其次，这里没有优先通道，抱歉。队伍的末尾在那边。”他指了指队尾。生活中，很多时候你是谁其实并不重要。对于软件应用来说也是如此——当你试图访问某个功能或数据时，你是谁并不会带来任何特权！

到目前为止，我们只讨论了认证，也就是你已经了解的，应用程序识别资源调用者的过程。在之前的示例中，我们并没有实现任何用来决定是否批准请求的规则，我们只关心系统是否认识这个用户。在大多数应用中，系统识别的所有用户并不一定都能访问所有资源。本章我们将讨论授权。授权是指系统在识别出客户端身份后，决定其是否有权限访问所请求资源的过程（见图7.1）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201602129.png){ loading=lazy }
  <figcaption>图 7.1 授权是应用程序在经过身份验证后，决定某个实体是否有权限访问某项资源的过程。授权总是在认证之后进行。</figcaption>
</figure>

在 Spring Security 中，当应用程序完成认证流程后，会将请求交由授权过滤器处理。该过滤器会根据已配置的授权规则决定是否允许或拒绝该请求（见图
7.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201611656.png){ loading=lazy }
  <figcaption>图 7.2 当客户端发起请求时，认证过滤器会先验证用户身份。验证通过后，认证过滤器会将用户信息存入SecurityContext，并将请求传递给授权过滤器。授权过滤器随后会根据SecurityContext中的用户信息，判断该请求是否被允许。</figcaption>
</figure>

为了全面涵盖授权的核心内容，本章将包括以下内容：

- 了解什么是权限，并根据用户的权限为所有接口设置访问规则；
- 学习如何将权限分组为角色，以及如何根据用户的角色应用授权规则。

在第8章中，我们将继续选择需要应用授权规则的端点。现在，让我们先了解一下权限和角色，以及它们如何限制对我们应用程序的访问。

## 基于权限和角色限制访问

在本节中，您将学习授权和角色的相关概念。通过这些机制，您可以为应用程序的所有接口提供安全保护。在实际应用中，不同用户拥有不同的权限，因此在掌握这些概念后，才能灵活应用于真实场景。用户根据自身的权限，只能执行特定的操作。应用程序通过权限和角色来分配这些操作权限。

在第3章中，你实现了GrantedAuthority接口。我们在讨论另一个重要组件——UserDetails接口时，首次介绍了这个接口。当时我们没有深入使用GrantedAuthority，因为正如你将在本章中了解到的，这个接口主要与授权过程相关。现在我们可以回过头来，进一步探讨GrantedAuthority的作用。图7.3展示了UserDetails契约与GrantedAuthority接口之间的关系。在我们讲解完这个契约后，你将学会如何单独或针对特定请求使用这些规则。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201631841.png){ loading=lazy }
  <figcaption>图 7.3 用户拥有一个或多个权限（允许的操作）。在认证阶段，UserDetailsService 会获取关于用户的详细信息，包括其权限。认证成功后，应用程序会利用这些权限（通过 GrantedAuthority 接口表示）来执行授权操作。</figcaption>
</figure>

清单7.1展示了GrantedAuthority接口的定义。权限（authority）指的是用户可以对系统资源执行的某项操作。每个权限都有一个名称，可以通过对象的getAuthority()
方法以字符串形式获取。在定义自定义授权规则时，我们会用到权限的名称。通常，授权规则可能像这样：“Jane被允许删除产品记录”，或者“John被允许读取文档记录”。在这些例子中，delete和read就是被授予的权限。应用程序允许Jane和John执行这些操作，这些操作通常被命名为read、write或delete等。

```java title="清单 7.1 GrantedAuthority 接口规范"
public interface GrantedAuthority extends Serializable {
	String getAuthority();
}
```

在 Spring Security 中，UserDetails 是用于描述用户的接口，其中包含了一组 GrantedAuthority 实例，如图 7.3
所示。你可以为一个用户分配一个或多个权限。getAuthorities() 方法会返回这些 GrantedAuthority 实例的集合。在代码清单 7.2
中，你可以看到 UserDetails 接口中的这个方法。我们需要实现该方法，以便返回分配给用户的所有权限。认证完成后，这些权限会成为已登录用户的详细信息的一部分，应用程序可以据此授予相应的访问权限。

```java title="清单 7.2 UserDetails 接口中的 getAuthorities() 方法"
public interface UserDetails extends Serializable {
	Collection<? extends GrantedAuthority> getAuthorities();

	// Omitted code
}
```

### 基于用户权限限制所有端点的访问

本节将介绍如何为特定用户限制对接口的访问。在之前的示例中，任何已认证用户都可以调用应用程序的任意接口。接下来，你将学习如何自定义这些访问权限。在实际生产环境中的应用程序里，有些接口即使未登录也可以访问，而另一些则需要特殊权限（见图7.4）。我们会通过多个示例，帮助你掌握在Spring
Security中实现这些访问限制的不同方法。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511201644163.png){ loading=lazy }
  <figcaption>图7.4 权限定义了用户在应用程序中可执行的操作。这些操作为授权协议的制定提供了依据，确保只有拥有特定权限的用户才能访问某些接口。例如，Jane 仅限于在该接口进行读取和写入操作，而 John 则拥有读取、写入、删除和修改的全部权限。</figcaption>
</figure>

现在你已经了解了 UserDetails 和 GrantedAuthority 的接口及它们之间的关系，是时候编写一个小型应用来实现授权规则了。通过这个示例，你将学习几种根据用户权限配置端点访问的方法。我们将启动一个名为
ssia-ch7-ex1 的新项目，并向你展示三种可以实现访问控制的配置方式：

- `hasAuthority()` —— 该方法只接收一个权限参数，应用程序会根据这个权限配置访问限制。只有拥有该权限的用户才能调用该接口。
- `hasAnyAuthority()` —— 该方法可以接收多个权限参数，应用程序会根据这些权限配置访问限制。我通常把这个方法理解为“拥有任意一个指定权限即可”。用户只需具备其中至少一个权限，就可以发起请求。
  我建议根据你分配的权限数量，优先使用 hasAuthority() 或 hasAnyAuthority() 方法，因为它们简单易懂，配置时也很直观，有助于提升代码的可读性。
- `access()` —— 该方法为访问控制提供了无限的配置可能性，因为应用程序可以基于自定义的 AuthorizationManager
  对象来构建授权规则。你可以根据实际需求自行实现 AuthorizationManager 接口，Spring Security 也提供了一些现成的实现。其中最常用的是
  WebExpressionAuthorizationManager，它允许你基于 Spring 表达式语言（SpEL）来定义授权规则。但需要注意的是，使用 access()
  方法可能会让授权规则变得难以阅读和理解。因此，除非无法通过 hasAnyAuthority() 或 hasAuthority()
  实现需求，否则我建议优先使用前两种方法，access() 作为次选方案。

在你的 pom.xml 文件中，只需引入 spring-boot-starter-web 和 spring-boot-starter-security
这两个依赖。这两项依赖已经足以实现前面提到的三种解决方案。你可以在项目 ssia-ch7-ex1 中找到该示例：

``` xml
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

我们还在应用中添加了一个接口，用于测试我们的授权配置：

```java

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

在一个配置类中，我们将 InMemoryUserDetailsManager 声明为我们的 UserDetailsService，并添加了两个用户，John 和
Jane，由其进行管理。每个用户都拥有不同的权限。具体实现方式可以参考下面的代码示例。

``` java title="代码清单 7.3 声明 UserDetailsService 并分配用户"

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var manager = new InMemoryUserDetailsManager();

		var user1 = User.withUsername("john")
				.password("12345")
				.authorities("READ")
				.build();

		var user2 = User.withUsername("jane")
				.password("12345")
				.authorities("WRITE")
				.build();

		manager.createUser(user1);
		manager.createUser(user2);

		return manager;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}

```

接下来，我们要添加授权配置。在第二章中，当我们完成第一个示例时，你已经看到如何让所有端点对所有用户开放。为此，我们在应用的上下文中创建了一个
SecurityFilterChain Bean，方式与下面的代码示例类似。

```java title="清单 7.4 让所有端点对所有用户开放，无需身份验证"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().permitAll()
		);

		return http.build();
	}
}

```

authorizeHttpRequests() 方法允许我们继续为各个端点指定授权规则。anyRequest() 方法表示该规则适用于所有请求，无论其 URL 或
HTTP 方法为何。permitAll() 方法则允许所有匹配的请求访问，无论用户是否经过认证。

假设我们希望只有拥有WRITE权限的用户才能访问所有接口。在我们的例子中，这意味着只有Jane可以访问。我们可以通过基于用户权限来实现这个目标并限制访问。请看下面的代码示例。

```java title="代码清单 7.5 仅允许具有 WRITE 权限的用户访问"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest()
						.hasAuthority("WRITE")
		);

		return http.build();
	}
}

```

你可以看到，permitAll() 方法被 hasAuthority() 方法所取代。你需要将允许用户访问的权限名称作为 hasAuthority()
方法的参数传入。应用程序会先对请求进行认证，然后根据用户所拥有的权限来决定是否允许该请求。

现在我们可以开始测试这个应用了，分别用两个用户去调用这个接口。当用用户 Jane 调用时，HTTP 响应状态是 200 OK，响应体内容为
“Hello!”；而用用户 John 调用时，HTTP 响应状态则是 403 Forbidden，响应体为空。例如，使用 Jane 用户调用该接口时：

```shell
curl -u jane:12345 http://localhost:8080/hello
```

我们收到如下响应：

```shell
Hello!
```

调用端点，用户为 John

```shell
curl -u john:12345 http://localhost:8080/hello
```

我们收到如下响应：

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/hello"
}
```

同样地，我们也可以使用 hasAnyAuthority() 方法。该方法支持可变参数，因此可以接收多个权限名称。如果用户拥有方法参数中任意一个权限，应用程序就会允许该请求。你可以将前面代码中的
hasAuthority() 替换为 hasAnyAuthority("WRITE")，此时应用程序的行为完全相同。但如果你将 hasAuthority() 替换为
hasAnyAuthority("WRITE", "READ")，那么拥有任意一个权限的用户请求都会被接受。在我们的例子中，John 和 Jane
的请求都会被允许。下面的代码展示了如何使用 hasAnyAuthority() 方法。

``` java title="清单 7.6 应用 hasAnyAuthority() 方法"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest()
						.hasAnyAuthority("WRITE", "READ");     
    );

		return http.build();
	}
}

```

现在，您可以使用我们的任意一位用户成功调用该接口。以下是以 John 用户为例的调用方式：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

对Jane的呼叫是

```shell
curl -u jane:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

要根据用户权限指定访问控制，实践中你会遇到的第三种方式是使用 access() 方法。不过，access() 方法更加通用。它接收一个
AuthorizationManager
实现作为参数。你可以为这个对象提供任何实现，用以应用各种定义授权规则的逻辑。这个方法非常强大，并不仅限于基于权限的控制。然而，使用这种方式会让代码变得更难阅读和理解。因此，我建议将其作为最后的选择，仅当你无法使用本节前面介绍的
hasAuthority() 或 hasAnyAuthority() 方法时再考虑使用。

为了让这个方法更易于理解，我首先将其作为指定权限（使用 hasAuthority() 和 hasAnyAuthority() 方法）的另一种选择来介绍。在这个例子中，你将使用一个
AuthorizationManager 实现，并且需要提供一个 `SpEL表达式`
作为参数。我们定义的授权规则会变得更难阅读，因此我并不推荐在简单规则中使用这种方式。不过，access() 方法的优势在于，你可以通过自定义的
AuthorizationManager 实现来灵活定制授权规则，这一点非常强大！和 SpEL 表达式一样，你几乎可以定义任何条件。

!!! note

	在大多数情况下，所需的权限限制可以通过 hasAuthority() 和 hasAnyAuthority() 方法实现，我建议优先使用这两种方法。只有在这两种方式不适用，且你需要实现更通用的授权规则时，才建议使用 access() 方法。

我们先来看一个简单的例子，实现与前面案例相同的需求。如果你只需要判断用户是否拥有特定的权限，可以在 access() 方法中使用如下表达式：

- hasAuthority('WRITE') —— 要求用户必须具备 WRITE 权限才能访问该接口。
- hasAnyAuthority('READ', 'WRITE') —— 要求用户至少拥有 READ 或 WRITE 权限中的一个。通过这种表达式，你可以枚举所有允许访问的权限。

请注意，这些表达式与本节前面介绍的方法同名。下面的代码示例演示了如何使用 access() 方法。

```java title="代码清单 7.7 使用 access() 方法配置端点访问权限"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest()
						.access("hasAuthority('WRITE')")
		);

		return http.build();
	}
}

```

清单7.7中的示例表明，如果你只是满足简单需求，使用access()方法会让语法变得复杂。在这种情况下，你应该直接使用hasAuthority()
或hasAnyAuthority()方法。不过，access()
方法并非一无是处。正如之前提到的，它提供了更高的灵活性。在实际项目中，你会遇到一些需要根据更复杂的表达式来授予访问权限的场景，这时就可以用access()
方法来实现。如果没有access()方法，这些需求是无法实现的。

在代码清单7.8中，你可以看到 access() 方法被用于一个用其他方式不太容易表达的权限校验。具体来说，清单7.8中的配置定义了两个用户：John
和 Jane，他们拥有不同的权限。John 只拥有读取权限，而 Jane 拥有读取、写入和删除权限。该接口应该允许拥有读取权限的用户访问，但不允许拥有删除权限的用户访问。

!!! note

	在 Spring 应用中，你会发现权限命名有各种风格和习惯。有些开发者喜欢用全大写字母，而有些则倾向于全小写。在我看来，只要在你的应用中保持一致，这些做法都是可以接受的。在本书中，我在示例中采用了不同的命名风格，目的是让你了解在实际开发中可能遇到的多种方式。

这当然只是一个假设的例子，不过它足够简单，便于理解，同时也足够复杂，可以很好地说明 access() 方法为何更为强大。要用 access()
方法实现这一点，你可以使用一个接收 SpEL 表达式的 AuthorizationManager 实现。SpEL 表达式需要准确反映你的需求。例如：

``` text
"hasAuthority('read') and !hasAuthority('delete')"
```

下面的代码示例展示了如何使用更复杂的表达式来应用 access() 方法。你可以在名为 ssia-ch7-ex2 的项目中找到这个示例。

``` java title="代码清单 7.8 使用更复杂的表达式调用 access() 方法"

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var manager = new InMemoryUserDetailsManager();

		var user1 = User.withUsername("john")
				.password("12345")
				.authorities("read")
				.build();

		var user2 = User.withUsername("jane")
				.password("12345")
				.authorities("read", "write", "delete")
				.build();

		manager.createUser(user1);
		manager.createUser(user2);

		return manager;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		String expression =
				"""hasAuthority('read') and
						 !hasAuthority('delete')
						""";

		http.authorizeHttpRequests(
				c -> c.anyRequest()
						.access(new WebExpressionAuthorizationManager(expression));

    );

		return http.build();
	}
}

```

现在让我们测试一下应用程序，调用 /hello 接口，用户为 John：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应主体是

```shell
Hello!
```

当使用用户 Jane 调用该接口时

```shell
curl -u jane:12345 http://localhost:8080/hello
```

响应的主体是

```shell
{
   "status":403,
   "error":"Forbidden",
   "message":"Forbidden",
   "path":"/hello"
}
```

用户 John 只有读取权限，可以成功调用该接口。然而，Jane 拥有删除权限，却没有被授权调用该接口。Jane 调用接口时返回的 HTTP 状态码为
403 Forbidden（禁止访问）。

通过这些示例，你可以了解到如何为用户访问特定端点设置权限约束。当然，我们还没有讨论如何根据路径或 HTTP
方法来选择需要保护的请求。目前，我们是将这些规则应用于所有请求，而不考虑应用程序暴露的具体端点。等我们完成对用户角色的相同配置后，会进一步讲解如何选择需要应用授权配置的端点。

### 基于用户角色限制所有端点的访问权限

在本节中，我们将讨论如何根据角色来限制对端点的访问。角色是描述用户可以执行哪些操作的另一种方式（见图7.5）。在实际应用中，你也会经常遇到角色的概念，因此理解角色以及角色与权限之间的区别非常重要。本节我们会通过多个示例来演示如何使用角色，这样你就能了解应用在各种实际场景下如何使用角色，以及如何为这些场景编写相应的配置。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511202048124.png){ loading=lazy }
  <figcaption>图 7.5 角色是粗粒度的。每个拥有特定角色的用户只能执行该角色所授权的操作。在授权时采用这种理念，系统会根据用户的用途来决定是否允许请求。只有拥有特定角色的用户才能访问某些接口。</figcaption>
</figure>

Spring Security
将权限理解为我们可以施加限制的细粒度特权。角色则更像是用户的徽章，为用户赋予一组操作的权限。有些应用总是为特定用户分配相同的权限组。比如，在你的应用中，用户要么只有读取权限，要么拥有全部权限（读取、写入和删除）。在这种情况下，把只能读取的用户视为拥有
READER 角色，而其他用户则拥有 ADMIN 角色，可能会更容易理解。拥有 ADMIN
角色意味着应用会授予你读取、写入、更新和删除的权限。当然，你也可以设置更多的角色。例如，如果有需求规定还需要一个只能读取和写入的用户，你就可以为应用新增一个名为
MANAGER 的第三种角色。

!!! note

	当在应用程序中采用角色管理的方式时，你无需再单独定义权限。权限在这种情况下仅作为一个概念存在，可能会在具体实现需求中体现。但在应用层面，你只需定义一个角色，就可以涵盖用户有权执行的一个或多个操作。

你为角色起的名字其实和权限的名字类似——完全由你自己决定。可以说，角色相比权限来说是更粗粒度的。在底层实现上，Spring Security
也是用同一个接口 GrantedAuthority 来表示角色和权限。在定义角色时，角色名应该以 ROLE_ 前缀开头。从实现层面来看，这个前缀就是用来区分角色和权限的。你可以在项目
ssia-ch7-ex3 中找到本节讨论的示例。接下来，我们来看一下我对前一个示例所做的修改。

```java title="代码清单 7.9 为用户分配角色"

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var manager = new InMemoryUserDetailsManager();

		var user1 = User.withUsername("john")
				.password("12345")
				.authorities("ROLE_ADMIN")
				.build();

		var user2 = User.withUsername("jane")
				.password("12345")
				.authorities("ROLE_MANAGER")
				.build();

		manager.createUser(user1);
		manager.createUser(user2);

		return manager;
	}

	// Omitted code

}

```

要为用户角色设置约束，可以采用以下几种方法：

- `hasRole()` —— 接收一个角色名称作为参数，应用程序会根据该角色授权请求。
- `hasAnyRole()` —— 接收多个角色名称作为参数，应用程序会根据这些角色批准请求。
- `access() `—— 使用 AuthorizationManager 来指定应用程序授权请求的角色或角色集合。在角色控制方面，你可以将 hasRole() 或
  hasAnyRole() 作为 SpEL 表达式，与 WebExpressionAuthorizationManager 实现结合使用。

正如你所看到的，这些名称与第7.1.1节中介绍的方法非常相似。我们以同样的方式使用它们，不过这里是针对角色进行配置，而不是权限。我的建议也类似：优先使用
hasRole() 或 hasAnyRole() 方法，只有在这两种方法不适用时，再考虑使用 access()。下面的代码展示了现在的 securityFilterChain()
方法的样子。

```java title="清单 7.10 配置应用仅允许管理员请求"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().hasRole("ADMIN")
		);

		return http.build();
	}
}

```

!!! note

    需要特别注意的一点是，ROLE_ 前缀只在声明角色时使用，而在实际使用角色时，只需用角色名称即可。

在测试应用程序时，你应该注意到用户 John 能够访问该接口，而 Jane 会收到 HTTP 403 Forbidden 错误。要使用用户 John
调用该接口，请执行以下操作：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

要以用户 Jane 调用该接口，请使用

```shell
curl -u jane:12345 http://localhost:8080/hello
```

响应体是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/hello"
}
```

在使用 User 构建器类创建用户时，就像本节示例中所做的那样，你可以通过 roles() 方法来指定角色。该方法会创建 GrantedAuthority
对象，并自动为你提供的名称添加 ROLE_ 前缀。

!!! note

	请确保在为 roles() 方法提供参数时，不要包含 ROLE_ 前缀。如果在 roles() 方法的参数中不小心加上了该前缀，方法会抛出异常。简而言之，使用 authorities() 方法时需要加上 ROLE_ 前缀，而使用 roles() 方法时则不要加上 ROLE_ 前缀。

在代码清单7.11中，你可以看到在基于角色设计访问控制时，正确使用roles()方法而不是authorities()
方法的方式。你还可以将本清单与清单7.9进行对比，观察使用authorities和roles之间的区别。

```java title="代码清单 7.11 使用 roles() 方法配置角色"

@Configuration
public class ProjectConfig {

	@Bean
	public UserDetailsService userDetailsService() {
		var manager = new InMemoryUserDetailsManager();

		var user1 = User.withUsername("john")
				.password("12345")
				.roles("ADMIN")
				.build();

		var user2 = User.withUsername("jane")
				.password("12345")
				.roles("MANAGER")
				.build();

		manager.createUser(user1);
		manager.createUser(user2);

		return manager;
	}

	// Omitted code
}

```

#### access() 方法详解

在第7.1.1和7.1.2节中，你已经学会了如何使用 access()
方法，结合权限和角色来设置授权规则。通常情况下，应用程序中的授权限制都与权限和角色相关。不过，需要注意的是，access()
方法本身是通用的，它的行为完全取决于你作为参数传入的 AuthorizationManager 实现。此外，在我们的示例中，我们只使用了
WebExpressionAuthorizationManager 这个实现，它是基于 SpEL
表达式来应用授权限制的。通过这些示例，我主要是教你如何针对权限和角色进行配置，但实际上，WebExpressionAuthorizationManager
可以接收任何 SpEL 表达式，并不一定非要和权限或角色相关。

一个简单的例子是，将接口的访问权限配置为仅在下午12点之后才允许访问。要实现这样的需求，你可以使用如下的 SpEL 表达式：

``` java
T(java.time.LocalTime).now().isAfter(T(java.time.LocalTime).of(12, 0))
```

有关 SpEL 表达式的更多信息，请参阅 Spring Framework 官方文档：

[http://mng.bz/M9J7](https://docs.spring.io/spring-framework/reference/core/expressions.html)

我们可以说，借助 access() 方法，你几乎可以实现任何类型的规则，可能性无限。不过请记住，在实际应用中，我们始终追求尽可能简洁的语法。只有在别无选择的情况下，才去复杂化你的配置。你可以在项目
ssia-ch7-ex4 中看到这个示例的实际应用。

### 限制对所有端点的访问

在本节中，我们将讨论如何限制所有请求的访问权限。在第5.2节中，你已经了解到，可以通过 permitAll()
方法允许所有请求访问。你还学会了如何根据权限和角色来设置访问规则。但实际上，你也可以选择拒绝所有请求。denyAll() 方法的作用正好与
permitAll() 方法相反。下面的代码示例展示了如何使用 denyAll() 方法。

```java title="代码清单 7.12 使用 denyAll() 方法限制端点访问"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().denyAll()
		);

		return http.basic();
	}
}

```

这种限制可以用在哪里呢？虽然它不像其他方法那样常见，但在某些情况下，需求确实会让你不得不采用这种方式。让我举几个例子来说明这一点。

假设你有一个接口，它通过路径变量接收一个邮箱地址。你希望只允许那些以 .com 结尾的邮箱地址作为变量值的请求，其他格式的邮箱地址都不被接受。（在下一章你会学习如何根据路径和
HTTP 方法，甚至针对路径变量，对一组请求应用限制。）针对这个需求，你可以使用正则表达式来筛选符合规则的请求，然后通过 denyAll()
方法，指示应用拒绝所有这些请求（见图 7.6）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511202238824.png){ loading=lazy }
  <figcaption>图 7.6 当用户调用接口并传入以 .com 结尾的参数值时，应用程序会接受该请求。而当用户调用接口并提供以 .net 结尾的邮箱地址时，应用程序会拒绝该请求。要实现这样的行为，你可以对所有参数值不是以 .com 结尾的接口使用 denyAll() 方法。</figcaption>
</figure>

你也可以想象一个如图 7.7
所示设计的应用程序。几个服务实现了应用的用例，客户端可以通过不同路径下的端点来访问这些服务。但要调用某个端点，客户端需要请求另一个我们称之为网关的服务。在这种架构中，存在两个独立的网关服务。在图
7.7 中，我将它们称为网关 A 和网关 B。如果客户端想访问 /products 路径，就会请求网关 A；而访问 /articles 路径，则需要请求网关
B。每个网关服务都被设计为拒绝所有它们不负责的其他路径的请求。这个简化的场景可以帮助你更容易理解 denyAll()
方法。在实际生产应用中，你也可能会在更复杂的架构中遇到类似的情况。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511202241163.png){ loading=lazy }
  <figcaption>图7.7 访问通过网关A和B进行。每个网关只转发特定路径的请求，拒绝所有其他请求。</figcaption>
</figure>

生产环境中的应用会面临各种架构需求，有时这些需求可能看起来很奇怪。一个框架必须具备足够的灵活性，以应对你可能遇到的任何情况。因此，denyAll()
方法和本章中你学到的其他选项一样重要。

## 总结

- 授权是应用程序在已认证请求到来后，决定是否允许该请求的过程。授权总是在认证之后进行。
- 你可以根据已认证用户的权限和角色，配置应用程序如何进行请求授权。
- 在你的应用中，也可以指定某些请求允许未认证用户访问。
- 你可以通过 denyAll() 方法配置应用拒绝所有请求，或通过 permitAll() 方法允许所有请求。
