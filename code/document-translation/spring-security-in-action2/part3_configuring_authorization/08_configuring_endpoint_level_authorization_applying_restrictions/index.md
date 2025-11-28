# 8.配置端点级授权：应用访问限制

本章内容包括：

- 使用匹配器方法选择需要施加限制的请求
- 了解每种匹配器方法的最佳应用场景

在第7章中，你学习了如何基于权限和角色进行访问配置。但之前我们只是将这些配置应用到了所有端点上。在本章中，你将学习如何将授权约束应用到特定的一组请求上。在实际生产环境的应用中，很少会对所有请求都采用相同的规则。通常会有一些端点只能被特定用户访问，而其他端点则可能对所有人开放。根据业务需求，每个应用都会有自己定制的授权配置。接下来，我们将讨论在编写访问配置时，如何针对不同的请求进行区分和配置的各种方式。

即使我们之前没有特别注意，你最先用到的匹配器方法其实是 anyRequest()。正如前几章中所用到的那样，你现在应该知道，它指的是所有请求，无论路径或
HTTP 方法如何。它的作用就是表示“任何请求”，有时也可以理解为“所有其他请求”。

首先，我们先来说说如何通过路径选择请求；接下来，我们还可以将 HTTP 方法加入到场景中。要选择需要应用授权配置的请求，可以使用
requestMatchers() 方法。

## 使用requestMatchers()方法选择端点

在本节中，你将学习如何通用地使用 requestMatchers() 方法，这样在第 8.2 至 8.4 节中，我们可以继续介绍针对不同 HTTP
请求选择方式的多种授权限制策略。到本章结束时，你将能够根据应用需求，在任何授权配置中灵活运用 requestMatchers()
方法。我们先从一个简单的例子开始。

我们创建了一个应用程序，暴露了两个接口：/hello 和 /ciao。我们希望只有拥有 ADMIN 角色的用户才能访问 /hello 接口，同样，只有拥有
MANAGER 角色的用户才能访问 /ciao 接口。你可以在项目 ssia-ch8-ex1 中找到这个示例。下面的代码展示了控制器类的定义。

```java title="清单8.1 控制器类的定义"

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}

	@GetMapping("/ciao")
	public String ciao() {
		return "Ciao!";
	}
}
```

在配置类中，我们声明了一个 InMemoryUserDetailsManager 作为 UserDetailsService 实例，并添加了两个拥有不同角色的用户。用户
John 拥有 ADMIN 角色，而 Jane 拥有 MANAGER 角色。为了指定只有拥有 ADMIN 角色的用户才能访问 /hello 端点，我们使用了
requestMatchers() 方法来进行请求授权。下面的代码展示了该配置类的定义。

``` java title="代码清单 8.2 配置类的定义"

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

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.requestMatchers("/hello").hasRole("ADMIN")
						.requestMatchers("/ciao").hasRole("MANAGER")
		);

		return http.build();
	}
}

```

你可以运行并测试这个应用程序。当你以用户 John 调用 /hello 接口时，会收到成功的响应。但如果用用户 Jane 调用同一个接口，响应状态会返回
HTTP 403 Forbidden。同样地，/ciao 接口只能用 Jane 用户获取成功结果，对于 John 用户，响应状态也会返回 HTTP 403
Forbidden。你可以在下面的代码片段中看到使用 cURL 的示例调用。要以 John 用户调用 /hello 接口，请使用：

```shell
curl -u john:12345 http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

要为用户 Jane 调用 /hello 接口，请使用

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

要为用户 Jane 调用 /ciao 接口，请使用

```shell
curl -u jane:12345 http://localhost:8080/ciao
```

响应体是

```shell
Ciao!
```

要为用户 John 调用 /ciao 接口，请使用

```shell
curl -u john:12345 http://localhost:8080/ciao
```

响应体是

```shell
{
   "status":403,
   "error":"Forbidden",
   "message":"Forbidden",
   "path":"/ciao"
}
```

如果你现在在应用中添加任何其他端点，这些端点默认对所有人开放，甚至包括未认证的用户。假设你按照下一个代码示例添加了一个新的端点
/hola。

```java title="清单 8.3 为应用程序新增 /hola 路径的端点"

@RestController
public class HelloController {

	// Omitted code

	@GetMapping("/hola")
	public String hola() {
		return "Hola!";
	}
}
```

当你访问这个新接口时，你会发现无论是否拥有有效用户权限都可以访问。下面的代码片段展示了这一行为。要在未进行身份验证的情况下调用
/hola 接口，请使用：

```shell
curl http://localhost:8080/hola
```

响应体是

```shell
Hola!
```

要为用户 John 调用 /hola 接口，请使用

```shell
curl -u john:12345 http://localhost:8080/hola
```

响应体是

```shell
Hola!
```

如果你希望让这种行为更加明显，可以使用 permitAll() 方法。具体做法是在请求授权的配置链末尾，使用 anyRequest() 匹配器方法，如清单
8.4 所示。

!!! note

	将所有规则明确列出是一种良好的实践。代码清单8.4清晰且毫不含糊地表明，除了 /hello 和 /ciao 这两个端点外，允许所有人访问其他端点。

```java title="代码清单 8.4 明确标记其他请求为无需认证即可访问"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.requestMatchers("/hello").hasRole("ADMIN")
						.requestMatchers("/ciao").hasRole("MANAGER")
						.anyRequest().permitAll()
		);

		return http.build();
	}
}

```

!!! note

    当你使用匹配器来指定请求时，规则的顺序应当是从具体到泛泛。这也是为什么不能在更具体的 requestMatchers() 方法之前调用 anyRequest() 方法。

#### 未认证vs认证失败

如果你将某个端点设计为对所有人开放，那么你可以在不提供用户名和密码的情况下访问它，此时 Spring Security
不会进行身份认证。但如果你提供了用户名和密码，Spring Security 会在认证过程中对其进行校验。如果用户名或密码错误（即系统中不存在），认证就会失败，响应状态码将是
401 Unauthorized（未授权）。更具体地说，如果你按照清单 8.4 的配置访问 /hola 端点，应用会如预期返回 Hola! 作为响应内容，状态码为
200 OK。例如：

```shell
curl http://localhost:8080/hola
```

响应体是

```shell
Hola!
```

但是，如果你使用无效的凭证调用该接口，响应的状态码将是401 Unauthorized（未授权）。在下一个请求中，我使用了一个错误的密码：

```shell
curl -u bill:abcde http://localhost:8080/hola
```

响应体是

```shell
{
   "status":401,
   "error":"Unauthorized",
   "message":"Unauthorized",
   "path":"/hola"
}
```

这种行为看起来可能有些奇怪，但其实是有道理的，因为只要你在请求中提供了用户名和密码，框架都会对其进行校验。正如你在第7章中学到的，应用程序总是在进行授权之前先进行认证，正如下图所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511210917920.png){ loading=lazy }
  <figcaption>授权过滤器允许任何请求访问 /hola 路径。然而，由于应用程序会先执行认证逻辑，所以请求根本不会被转发到授权过滤器。相反，认证过滤器会直接返回一个 HTTP 401 未授权响应。</figcaption>
</figure>

总之，任何身份验证失败的情况都会返回一个 401 Unauthorized（未授权）状态码，应用程序也不会将请求转发到对应的接口。permitAll()
方法仅用于授权配置，如果身份验证未通过，请求同样不会被继续处理。

---

当然，您也可以决定让所有其他端点仅对已认证用户开放。为此，您只需将 permitAll() 方法替换为 authenticated()，如下所示。同样，您还可以通过使用
denyAll() 方法来拒绝所有其他请求。

```java title="清单 8.5 让所有已认证用户都能访问其他请求"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.requestMatchers("/hello").hasRole("ADMIN")
						.requestMatchers("/ciao").hasRole("MANAGER")
						.anyRequest().authenticated()
		);

		return http.build();
	}
}

```

你已经熟悉了使用匹配器方法来指定需要配置授权限制的请求。接下来，我们将深入探讨可以使用的各种语法。

在大多数实际场景中，多个端点通常会共享相同的授权规则，因此你无需为每个端点单独配置。此外，有时你还需要指定 HTTP
方法，而不仅仅是像之前那样只设置路径。

有时候，你只需要为某个端点在通过 HTTP GET 请求时配置规则。这种情况下，你还需要为 HTTP POST 和 HTTP DELETE
定义不同的规则。在接下来的部分，我们将分别介绍每种匹配器方法，并详细讨论这些内容。

## 选择需实施授权限制的请求

在本节中，我们将深入探讨请求匹配器的配置。使用 requestMatchers() 方法是应用授权配置时常用的方式之一。因此，我相信你在开发应用时，会有很多机会用到这个方法来指定请求。

该匹配器采用标准的 ANT 语法（见表 8.1）来指定路径。这种语法与使用 @RequestMapping、@GetMapping、@PostMapping
等注解编写端点映射时所用的语法完全一致。你可以通过以下两种方法来声明 MVC 匹配器：

- `requestMatchers(HttpMethod method, String... patterns)` —— 允许你同时指定要应用限制的 HTTP 方法和路径。如果你希望针对同一路径的不同
  HTTP 方法设置不同的访问限制，这个方法会非常有用。
- `requestMatchers(String... patterns)` —— 如果你只需要根据路径进行权限控制，这个方法更简单易用。无论使用哪种 HTTP
  方法，只要匹配到指定路径，限制都会自动生效。

在本节中，我们将探讨多种使用 requestMatchers() 方法的方式。为此，我们首先编写一个应用程序，开放多个接口以进行演示。

这是我们第一次编写可以使用除 GET 以外的其他 HTTP 方法调用的端点。你可能已经注意到，直到现在我一直避免使用其他 HTTP
方法。原因在于，Spring Security 默认会针对跨站请求伪造（CSRF）进行防护。在第 9 章，我们会详细讨论 Spring Security 如何通过
CSRF 令牌来防止这种安全漏洞。但为了让当前的示例更简单，并且能够调用所有端点，包括通过 POST、PUT 或 DELETE 暴露的接口，我们需要在
securityFilterChain() 方法中禁用 CSRF 防护：

``` java
http.csrf(
 c -> c.disable()
);
```

!!! note

	我们现在暂时关闭了 CSRF 防护，只是为了让你能够专注于我们正在讨论的主题：matcher 方法。但请不要急于认为这种做法是好的。在第 9 章，我们会详细讲解 Spring Security 提供的 CSRF 防护机制。

我们首先定义四个用于测试的接口端点：

- /a，使用 HTTP GET 方法
- /a，使用 HTTP POST 方法
- /a/b，使用 HTTP GET 方法
- /a/b/c，使用 HTTP GET 方法

通过这些端点，我们可以针对不同的授权配置场景进行考虑。下面的代码清单展示了这些端点的定义。你可以在项目 ssia-ch8-ex2
中找到这个示例。

```java title="清单8.6 配置授权的四个端点定义"

@RestController
public class TestController {

	@PostMapping("/a")
	public String postEndpointA() {
		return "Works!";
	}

	@GetMapping("/a")
	public String getEndpointA() {
		return "Works!";
	}

	@GetMapping("/a/b")
	public String getEndpointB() {
		return "Works!";
	}

	@GetMapping("/a/b/c")
	public String getEndpointC() {
		return "Works!";
	}
}
```

我们还需要创建几个拥有不同角色的用户。为简化操作，我们继续使用 InMemoryUserDetailsManager。下面的代码展示了在配置类中定义
UserDetailsService 的方式。

``` java title="清单8.7 UserDetailsService 的定义"

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

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}

```

让我们从第一个场景开始。对于使用 HTTP GET 方法访问 /a 路径的请求，应用需要对用户进行身份认证。而对于同一路径下使用 HTTP
POST 方法的请求，则不需要认证。除此之外，应用会拒绝所有其他请求。下面的配置代码展示了如何实现这一需求。

```java title="清单 8.8 第一个场景 /a 的授权配置"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.requestMatchers(HttpMethod.GET, "/a")
						.authenticated()
						.requestMatchers(HttpMethod.POST, "/a")
						.permitAll()
						.anyRequest()
						.denyAll()
		);

		http.csrf(
				c -> c.disable()
		);

		return http.build();
	}
}

```

在接下来的代码片段中，我们将分析针对清单8.8中所示配置，调用各个端点的结果。对于未进行身份验证的情况下，使用POST方法请求路径/a，可以使用以下cURL命令：

```shell
curl -XPOST http://localhost:8080/a   
```

响应体是

```shell
Works!
```

在未进行身份验证的情况下，通过 HTTP GET 请求调用路径 /a 时，使用

```shell
curl -XGET http://localhost:8080/a   
```

回复是

```shell
{
 "status":401,
 "error":"Unauthorized",
 "message":"Unauthorized",
 "path":"/a"
}
```

如果你想让响应变为成功状态，需要使用有效用户进行身份验证。对于以下调用：

```shell
curl -u john:12345 -XGET http://localhost:8080/a   
```

响应体是

```shell
Works!
```

然而，用户 John 无权访问路径 /a/b，因此使用他的凭证进行该请求时会返回 403 Forbidden（禁止访问）：

```shell
curl -u john:12345 -XGET http://localhost:8080/a/b  
```

回复是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/a/b"
}
```

通过这个例子，你已经了解了如何根据 HTTP
方法区分请求。那么，如果有多个路径需要应用相同的授权规则怎么办？当然，我们可以枚举所有需要授权规则的路径，但如果路径太多，代码的可读性就会变得很差。此外，我们可能一开始就知道某一组具有相同前缀的路径总是遵循相同的授权规则。我们希望确保在同一组中新增路径时，不需要额外修改授权配置。为了解决这些情况，我们可以使用路径表达式。下面我们通过一个例子来说明这一点。

对于当前项目，我们希望所有以 /a/b 开头的路径请求都遵循相同的规则。在我们的场景中，这些路径包括 /a/b 和
/a/b/c。为此，我们使用 ** 运算符。你可以在项目 ssia-ch8-ex3 中找到相关示例。

``` java title="代码清单 8.9 配置类中针对多路径的修改"
@Configuration
public class ProjectConfig { 

  // Omitted code

  @Bean
  public void configure(HttpSecurity http) 
    throws Exception {

    http.httpBasic(Customizer.withDefaults());

    http.authorizeHttpRequests(
      c -> c.requestMatchers( "/a/b/**").authenticated()
            .anyRequest().permitAll();
    );
          
    http.csrf(
      c -> c.disable()
    );

    return http.build();
  }
}

```

根据清单8.9中的配置，你可以在未认证的情况下访问路径 /a，但对于所有以 /a/b 开头的路径，应用程序都需要对用户进行认证。下面的代码片段展示了分别调用
/a、/a/b 和 /a/b/c 端点时的结果。首先，如果你想在未认证的情况下访问 /a 路径，可以使用以下方式：

```shell
curl http://localhost:8080/a
```

响应体是

```shell
Works!
```

要在无需身份验证的情况下调用 /a/b 路径，请使用

```shell
curl http://localhost:8080/a/b
```

回复是

```shell
{ 
 "status":401,
 "error":"Unauthorized",
 "message":"Unauthorized",
 "path":"/a/b"
}
```

要在无需认证的情况下调用 /a/b/c 路径，请使用

```shell
curl http://localhost:8080/a/b/c
```

回复是

```shell
{
 "status":401,
 "error":"Unauthorized",
 "message":"Unauthorized",
 "path":"/a/b/c"
}
```

如前面的例子所示，** 运算符用于匹配任意数量的路径名。你可以像我们在上一个例子中那样使用它，以匹配具有已知前缀的请求路径。你也可以将它用在路径的中间位置，用来匹配任意数量的路径名，或者匹配以特定模式结尾的路径，比如
/a/**/c。因此，/a/**/c 不仅可以匹配 /a/b/c，还可以匹配 /a/b/d/c、a/b/c/d/e/c 等等。如果你只想匹配一个路径名，可以使用单个
*。例如，a/*/c 可以匹配 a/b/c 和 a/d/c，但不会匹配 a/b/d/c。

由于你通常会使用路径变量，这些变量在为此类请求设置授权规则时非常有用。你甚至可以根据路径变量的值来制定规则。还记得第8.1节关于
denyAll() 方法以及如何限制所有请求的讨论吗？

现在让我们来看一个更贴合本节所学内容的示例。假设我们有一个带有路径变量的接口端点，并且我们希望拒绝所有路径变量值中包含非数字字符的请求。你可以在项目
ssia-ch8-ex4 中找到这个示例。下面是该控制器的代码。

```java title="代码清单8.10 控制器类中带有路径变量的端点定义"

@RestController
public class ProductController {

	@GetMapping("/product/{code}")
	public String productCode(@PathVariable String code) {
		return code;
	}
}
```

下面的代码示例展示了如何配置授权，使得只有参数值全为数字的请求始终被允许，其他所有请求都被拒绝。

```java title="代码清单8.11 配置授权以仅允许特定数字"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.httpBasic(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.requestMatchers("/product/{code:^[0-9]*$}")
						.permitAll()
						.anyRequest()
						.denyAll()
		);

		return http.build();
	}
}

```

!!! note

	在使用参数表达式和正则表达式时，请确保参数名、冒号（:）以及正则表达式之间没有空格，如示例所示。

运行此示例后，您可以看到如下代码片段所展示的结果。应用程序只会在路径变量的值为纯数字时才接受请求。如果你尝试使用值 1234a
调用该接口，请使用：

```shell
curl http://localhost:8080/product/1234a
```

回复是

```shell
{
 "status":401,
 "error":"Unauthorized",
 "message":"Unauthorized",
 "path":"/product/1234a"
}
```

要调用该接口并传递值 12345，请使用

```shell
curl http://localhost:8080/product/12345
```

回复是

```shell
12345
```

我们已经详细讨论过，并举了许多关于如何使用 requestMatchers() 方法来引用请求的例子。表 8.1
总结了本节中使用的路径表达式，之后如果需要回顾其中的内容，可以随时查阅该表。

表8.1 MVC匹配器中用于路径匹配的常用表达式

| 表达式              | 描述                                                    |
|------------------|-------------------------------------------------------|
| /a               | 仅限路径 /a。                                              |
| /a/*             | * 操作符可以替换一个路径名。在这种情况下，它可以匹配 /a/b 或 /a/c，但不会匹配 /a/b/c。 |
| /a/**            | ** 操作符可以匹配多个路径名。在这种情况下，/a、/a/b 和 /a/b/c 都符合这个表达式。     |
| /a/{param}       | 此表达式适用于带有指定路径参数的 /a 路径。                               |
| /a/{param:regex} | 此表达式仅在路径参数的值符合指定正则表达式时，应用于路径 /a。                      |

## 使用正则表达式进行请求匹配

本节将讨论正则表达式（regex）。你应该已经了解什么是正则表达式，但不需要对此非常精通。你可以参考 [https://www.regular-expressions.info/books.html](https://www.regular-expressions.info/books.html)
上推荐的任何一本书，这些都是深入学习正则表达式的优秀资源。编写正则表达式时，我也经常使用像 [https://regexr.com/](https://regexr.com/)
这样的在线生成器（见图 8.1）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211319829.png){ loading=lazy }
  <figcaption>图8.1 让你的猫在键盘上玩耍并不是生成正则表达式（regex）的最佳方法。要学习如何生成正则表达式，你可以使用像 https://regexr.com/ 这样的在线生成器。</figcaption>
</figure>

第8.2节和第8.3节展示了，在大多数情况下，可以使用路径表达式语法来指定需要应用授权配置的请求。不过，有些情况下你可能有更特殊的需求，而路径表达式无法满足。例如，
`当路径中包含特定符号或字符时，拒绝所有请求`。针对这种场景，你就需要使用更强大的表达式，比如正则表达式。

你可以使用正则表达式来表示任意格式的字符串，因此在这方面几乎没有限制。不过，正则表达式的缺点是，即使在简单场景下也不太容易阅读。因此，你可能更倾向于优先使用路径表达式，只有在别无选择时才考虑正则表达式。如果需要实现基于正则的请求匹配器，可以通过
requestMatchers() 方法，并传入 RegexRequestMatcher 实现类作为参数来完成。

为了演示正则表达式匹配器的工作原理，我们将通过构建一个为用户提供视频内容的应用程序来实际操作一下。这个应用程序通过调用
`/video/{country}/{language}`
这个接口来获取视频内容。举个例子，应用会从用户发起请求的路径变量中接收国家和语言信息。我们假设，只要用户已通过身份验证，如果请求来自美国、加拿大或英国，或者使用的是英语，他们都可以观看视频内容。

你可以在项目 ssia-ch8-ex5 中找到这个示例的实现。我们需要保护的接口包含两个路径变量，如下所示。这使得使用请求匹配器来实现这一需求变得较为复杂。

```java title="代码清单8.12 控制器类端点的定义"

@RestController
public class VideoController {

	@GetMapping("/video/{country}/{language}")
	public String video(@PathVariable String country,
						@PathVariable String language) {
		return "Video allowed for " + country + " " + language;
	}
}
```

对于单一路径变量的条件，我们可以直接在路径表达式中编写正则表达式。在第8.2节中我们提到过这样的例子，但当时没有详细展开，因为那时我们还没有讨论正则表达式。

假设你有一个端点 /email/{email}，你希望只对那些 email 参数以 .com 结尾的请求应用某个规则。针对这种情况，你可以按照下面的代码片段编写一个请求匹配器。完整示例可以在项目
ssia-ch8-ex6 中找到：

``` java
http.authorizeHttpRequests(
  c -> c.requestMatchers("/email/{email:.*(?:.+@.+\\.com)}" ).permitAll()
        .anyRequest().denyAll();
);
   
```

如果你测试这样的限制，会发现应用只接受以 .com 结尾的邮箱地址。例如，要向 jane@example.com 调用该接口，你可以使用：

```shell
curl http://localhost:8080/email/jane@example.com
```

响应正文是

```shell
Allowed for email jane@example.com
```

要调用 jane@example.net 的接口，你可以这样做：

```shell
curl http://localhost:8080/email/jane@example.net
```

响应体是

```shell
{
 "status":401,
 "error":"Unauthorized",
 "message":"Unauthorized",
 "path":/email/jane@example.net
}
```

这其实很简单，也进一步说明了为什么我们不常用正则表达式匹配器。不过，正如我之前提到的，有时候需求会比较复杂。当你遇到以下情况时，使用正则表达式匹配器会更加方便：

- 针对所有包含电话号码或电子邮件地址的路径进行特定配置
- 针对所有具有特定格式的路径进行特定配置，包括所有路径变量中传递的内容

回到我们的正则表达式匹配器示例（ssia-ch8-ex6）：当你需要编写更复杂的规则，涉及更多路径模式和多个路径变量值时，使用正则表达式匹配器会更加方便。代码清单8.13展示了一个配置类的定义，该类通过正则表达式匹配器来满足对
/video/{country}/{language} 路径的需求。我们还添加了两个具有不同权限的用户，用于测试该实现。

``` java title="代码清单 8.13 使用正则表达式匹配器的配置类"
@Configuration
public class ProjectConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    var uds = new InMemoryUserDetailsManager();

    var u1 = User.withUsername("john")
                 .password("12345")
                 .authorities("read")
                 .build();

    var u2 = User.withUsername("jane")
                .password("12345")
                .authorities("read", "premium")
                .build();

    uds.createUser(u1);
    uds.createUser(u2);

    return uds;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return NoOpPasswordEncoder.getInstance();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) 
    throws Exception {

    http.httpBasic(Customizer.withDefaults());

    http.authorizeHttpRequests(
 
      c -> c.regexMatchers(".*/(us|uk|ca)+/(en|fr).*")
                .authenticated()    
            .anyRequest()           
                .hasAuthority("premium");
  
    );  
    
  }
}

```

运行并测试这些端点可以确认应用程序已经正确应用了授权配置。用户 John 可以访问国家代码为 US、语言为 en
的端点，但由于我们设置的限制，他无法访问国家代码为 FR、语言为 fr 的端点。以 John 用户在美国地区、使用英语身份验证并调用
/video 端点为例，流程如下：

```shell
curl -u john:12345 http://localhost:8080/video/us/en
```

响应体是

```shell
Video allowed for us en
```

调用 /video 接口，并以法国地区和法语为参数对用户 John 进行身份验证的请求如下：

```shell
curl -u john:12345 http://localhost:8080/video/fr/fr
```

响应体是

```shell
{
 "status":403,
 "error":"Forbidden",
 "message":"Forbidden",
 "path":"/video/fr/fr"
}
```

拥有premium权限的用户 Jane 两次调用均成功。对于第一次调用

```shell
curl -u jane:12345 http://localhost:8080/video/us/en
```

响应体是

```shell
Video allowed for us en
```

对于第二次调用

```shell
curl -u jane:12345 http://localhost:8080/video/fr/fr
```

响应体是

```shell
Video allowed for fr fr
```

正则表达式是非常强大的工具，可以用来根据各种需求匹配路径。然而，由于正则表达式难以阅读且可能变得非常冗长，因此应将其作为最后的选择。只有在路径表达式无法满足你的需求时，才建议使用正则表达式。

在本节中，我采用了我能想到的最简单的例子，这样所需的正则表达式就会很短。但在更复杂的场景下，正则表达式可能会变得非常冗长。当然，你也会遇到一些专家声称任何正则表达式都很容易读懂。比如，用于匹配电子邮件地址的正则表达式可能就像下面代码片段中的那样。你能轻松读懂并理解它吗？

```shell
(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-
]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-
\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0
-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0
-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0
-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-
\x09\x0b\x0c\x0e-\x7f])+)\])
```

## 总结

- 在实际场景中，不同的请求会应用不同的授权规则。
- 需要配置授权规则的请求通常根据路径和 HTTP 方法来指定，可以通过 requestMatchers() 方法实现。
- 当需求过于复杂，路径表达式无法满足时，可以使用更强大的正则表达式来实现。
