# 配置端点级授权：实施访问限制

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

```java title="代码清单 8.2 配置类的定义"

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

如果你将某个端点设计为对所有人开放，那么你可以在不提供用户名和密码的情况下访问它，此时 Spring Security 不会进行身份认证。但如果你提供了用户名和密码，Spring Security 会在认证过程中对其进行校验。如果用户名或密码错误（即系统中不存在），认证就会失败，响应状态码将是 401 Unauthorized（未授权）。更具体地说，如果你按照清单 8.4 的配置访问 /hola 端点，应用会如预期返回 Hola! 作为响应内容，状态码为 200 OK。例如：

```shell
curl http://localhost:8080/hola
```
