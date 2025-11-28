# 5.一个Web应用的安全性始于过滤器

本章内容包括：

- 使用过滤器链
- 定义自定义过滤器
- 使用实现 Filter 接口的 Spring Security 类

在 Spring Security 中，HTTP 过滤器会将不同的职责委托给 HTTP
请求进行处理。此外，过滤器通常还负责管理每个需要应用到请求上的职责。这些过滤器因此形成了一条责任链。每个过滤器接收到请求后，会执行自身的逻辑，随后将请求传递给链中的下一个过滤器（见图
5.1）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101019498.png){ loading=lazy }
  <figcaption>图5.1 请求被传递到过滤器链。每个过滤器都会调用一个管理器，在请求上执行特定的逻辑，然后将请求继续传递给链中的下一个过滤器。</figcaption>
</figure>

我们可以用一个类比来说明这个问题。比如你去机场，从进入航站楼到登上飞机，你需要经过多道安检（见图5.2）。首先你要出示机票，然后核查护照，接着通过安检。在登机口，还可能会有更多的检查。例如，在某些情况下，在登机前还会再次核查你的护照和签证。这和
Spring Security 中的过滤器链非常相似。你可以像这样在 Spring Security 的过滤器链中自定义各种过滤器。Spring Security
提供了多种过滤器实现，你可以通过自定义将它们添加到过滤器链中，同时你也可以定义自己的自定义过滤器。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101021997.png){ loading=lazy }
  <figcaption>图5.2 在机场，你需要依次通过一系列安检点，最终才能登上飞机。同样地，Spring Security 也实现了一套过滤器链，对应用接收到的 HTTP 请求进行逐步处理。</figcaption>
</figure>

本章将讨论如何使用 Spring Security 自定义过滤器，这些过滤器是 Web
应用中认证与授权架构的一部分。例如，你可能希望在认证流程中为用户增加一个额外的步骤，比如验证邮箱地址或使用一次性密码。你还可以添加与认证事件审计相关的功能。应用程序在多种场景下都会用到认证审计，比如用于调试或分析用户行为。如今的技术和机器学习算法能够提升应用的能力，例如通过学习用户的行为模式，判断账户是否被黑客入侵或有人冒充用户操作。

了解如何自定义 HTTP 过滤器责任链是一项非常有价值的技能。在实际应用中，随着需求的多样化，默认配置往往无法满足所有场景。这时，你就需要对过滤器链中的组件进行添加或替换。默认实现中，通常采用
HTTP Basic 认证方式，也就是通过用户名和密码进行身份验证。但在实际项目中，往往会遇到更多复杂的需求。比如，你可能需要实现不同的认证策略，或者在授权事件发生时通知外部系统，亦或是记录认证成功或失败的日志，以便后续进行追踪和审计（见图
5.3）。无论你的具体需求是什么，Spring Security 都为你提供了高度灵活的过滤器链定制能力，让你可以根据实际情况精准地进行建模。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101025328.png){ loading=lazy }
  <figcaption>图 5.3 你可以通过在现有过滤器之前、之后或替换现有过滤器的方式，个性化定制过滤器链。这样不仅可以自定义认证流程，还能全面调整请求和响应的处理方式。</figcaption>
</figure>

## 在Spring Security架构中实现过滤器

本节将介绍 Spring Security 架构中过滤器及过滤器链的工作原理。你需要先了解这些基本概念，才能更好地理解后续章节中的实现示例。在第
2 章和第 3 章中，我们已经了解到，认证过滤器会拦截请求，并将认证的责任进一步委托给授权管理器。如果我们希望在认证之前执行某些逻辑，可以通过在认证过滤器之前插入自定义过滤器来实现。

在 Spring Security 架构中，过滤器本质上就是典型的 HTTP 过滤器。我们可以通过实现 jakarta.servlet 包下的 Filter
接口来自定义过滤器。和其他 HTTP 过滤器一样，你需要重写 doFilter() 方法来实现具体逻辑。该方法接收
ServletRequest、ServletResponse 和 FilterChain 作为参数：

- ServletRequest —— 代表 HTTP 请求。我们可以通过 ServletRequest 对象获取请求的详细信息。
- ServletResponse —— 代表 HTTP 响应。我们可以通过 ServletResponse 对象在响应返回给客户端或传递到下一个过滤器之前对其进行修改。
- FilterChain —— 代表过滤器链。我们可以通过 FilterChain 对象将请求传递给链中的下一个过滤器。

!!! note

	从 Spring Boot 3 开始，Jakarta EE 取代了旧的 Java EE 规范。由于这一变更，你会发现部分包的前缀从“javax”变成了“jakarta”。例如，像 Filter、ServletRequest 和 ServletResponse 这样的类型，原本位于 javax.servlet 包下，现在则被移到了 jakarta.servlet 包中。

过滤器链代表了一组按照特定顺序执行的过滤器。Spring Security 为我们提供了一些过滤器的实现以及它们的执行顺序。以下是其中的一些过滤器：

- BasicAuthenticationFilter 负责处理 HTTP Basic 认证（如果存在的话）。
- CsrfFilter 负责防止跨站请求伪造（CSRF）攻击，这部分内容我们会在第9章详细讨论。
- CorsFilter 负责处理跨域资源共享（CORS）的授权规则，这部分内容我们也会在第10章介绍。

你不需要了解所有的过滤器，因为在实际开发中你很可能不会直接操作它们，但你需要理解过滤器链的工作原理，并且对其中的一些实现有所了解。在本书中，我只会讲解与我们讨论的主题密切相关的核心过滤器。

需要理解的是，一个应用程序的过滤器链中并不一定包含所有这些过滤器的实例。过滤器链的长短取决于你对应用的具体配置。例如，在第2章和第3章中你已经了解到，如果你想使用HTTP
Basic认证方式，就需要调用HttpSecurity类的httpBasic()方法。实际上，当你调用httpBasic()
方法时，系统会在过滤器链中添加一个BasicAuthenticationFilter实例。同理，根据你编写的配置，过滤器链的定义也会随之发生变化。

你可以在过滤器链中相对于另一个过滤器添加新的过滤器（见图
5.4）。你也可以选择在某个已知过滤器之前、之后或直接在其位置上添加过滤器。实际上，每个位置都是一个索引（即一个数字），有时也会被称为“顺序”。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101046662.png){ loading=lazy }
  <figcaption>图5.4 每个过滤器都有一个顺序编号，用于决定过滤器在处理请求时的执行顺序。除了Spring Security自带的过滤器之外，你还可以添加自定义过滤器。</figcaption>
</figure>

如果你想进一步了解 Spring Security 提供的过滤器及其配置顺序，可以查阅枚举类
SecurityWebFiltersOrder，详见：[http://mng.bz/yZEG](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/web/server/SecurityWebFiltersOrder.html)。

你可以在同一个位置添加两个或更多的过滤器（见图5.5）。在第5.4节中，我们将遇到一个常见的情形，这种情况通常会让开发者感到困惑。

!!! note

	如果多个过滤器具有相同的位置，它们的调用顺序是不确定的。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101100038.png){ loading=lazy }
  <figcaption>图 5.5 在过滤器链中，你可能会有多个过滤器设置了相同的顺序值。这种情况下，Spring Security 并不保证它们的调用顺序。</figcaption>
</figure>

## 在过滤器链中，在已有过滤器之前添加一个新过滤器

本节将介绍如何在过滤器链中某个已有过滤器之前应用自定义 HTTP
过滤器。在实际开发中，你可能会遇到需要这样做的场景。为了解决这个问题，我们将以一个项目为例，带你一步步实现自定义过滤器，并将其应用到指定过滤器之前。掌握了这个方法后，你可以根据实际需求，将其灵活应用到生产环境中的类似场景。

对于我们的第一个自定义过滤器实现，让我们来看一个简单的场景。我们希望确保每个请求都包含一个名为 Request-Id 的请求头（参见项目
ssia-ch5-ex1）。我们假设应用程序会用这个请求头来追踪请求，并且这个请求头是必需的。同时，我们希望在应用程序执行认证之前先验证这些假设。认证过程可能涉及查询数据库或其他资源消耗较大的操作，如果请求格式不合法，我们不希望应用程序去执行这些操作。那么该如何实现呢？满足当前需求只需要两个步骤，最终过滤器链会像图
5.6 所示：

1. 实现过滤器。创建一个 RequestValidationFilter 类，用于检查请求中是否存在所需的请求头。
2. 将过滤器添加到过滤器链。在配置类中，通过 SecurityFilterChain bean 完成此操作。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101121590.png){ loading=lazy }
  <figcaption>图 5.6 在我们的示例中，我们添加了一个 RequestValidationFilter，它会在认证过滤器之前执行。RequestValidationFilter 用于确保如果请求校验失败，则不会进行认证。在本例中，请求必须包含一个名为 Request-Id 的必填请求头。</figcaption>
</figure>

要完成第1步——实现过滤器，我们需要自定义一个过滤器。下面的代码展示了具体的实现方式。

``` java title="清单 5.1 实现自定义过滤器"
public class RequestValidationFilter 
  implements Filter {

  @Override
  public void doFilter(
     ServletRequest servletRequest, 
     ServletResponse servletResponse, 
     FilterChain filterChain) 
     throws IOException, ServletException {
     // ...
  }
}

```

在 doFilter() 方法内部，我们编写过滤器的具体逻辑。在本例中，我们会检查请求头中是否存在 Request-Id。如果存在，则通过调用
doFilter() 方法将请求传递给链中的下一个过滤器。如果不存在该请求头，则直接在响应中设置 HTTP 400 Bad Request
状态码，而不再继续传递给后续过滤器（见图 5.7）。代码逻辑如清单 5.2 所示。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101128849.png){ loading=lazy }
  <figcaption>图 5.7 我们在认证之前添加的自定义过滤器会检查请求中是否包含 Request-Id 头。如果该头存在，应用程序会将请求转发进行认证；如果该头不存在，应用程序会返回 HTTP 400 Bad Request 状态码，并将响应返回给客户端。</figcaption>
</figure>

``` java title="清单 5.2 在 doFilter() 方法中实现逻辑"

@Override
public void doFilter(
		ServletRequest request,
		ServletResponse response,
		FilterChain filterChain)
		throws IOException,
		ServletException {
	var httpRequest = (HttpServletRequest) request;
	var httpResponse = (HttpServletResponse) response;

	String requestId = httpRequest.getHeader("Request-Id");

	if (requestId == null || requestId.isBlank()) {
		httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		return;
	}

	filterChain.doFilter(request, response);

}

```

要实现第2步，即在配置类中应用过滤器，我们使用 HttpSecurity 对象的 addFilterBefore()
方法，因为我们希望应用程序在认证之前执行这个自定义过滤器。该方法接收两个参数：

- 我们要添加到过滤器链中的自定义过滤器实例——在本例中，就是 RequestValidationFilter 类的一个实例（见代码清单 5.1）。
- 新实例要添加到哪个过滤器之前——在本例中，由于需求是在认证之前执行过滤器逻辑，所以需要将自定义过滤器实例添加到认证过滤器之前。BasicAuthenticationFilter
  类定义了默认的认证过滤器类型。

到目前为止，我们一直将处理认证的过滤器统称为认证过滤器。你将在后续章节了解到，Spring Security
还会配置其他类型的过滤器。在第9章，我们会讨论跨站请求伪造（CSRF）防护；在第10章，我们会讨论跨域资源共享（CORS）。这两项功能同样依赖于过滤器的实现。

下面的代码示例展示了如何在配置类中将自定义过滤器添加到认证过滤器之前。为了简化示例，我们使用 permitAll() 方法，允许所有未认证的请求通过。

```java title="清单 5.3 在认证之前配置自定义过滤器"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.addFilterBefore(
						new RequestValidationFilter(), BasicAuthenticationFilter.class)
				.authorizeRequests(c -> c.anyRequest().permitAll());

		return http.build();
	}
}

```

我们还需要一个控制器类和一个端点，用于测试功能。下面的代码展示了控制器类的定义。

```java title="清单 5.4 控制器类"

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String hello() {
		return "Hello!";
	}
}
```

现在你可以运行并测试这个应用了。如果在调用接口时没有添加请求头，会返回 HTTP 400 Bad Request 响应。如果你在请求中加入该请求头，响应状态就会变为
HTTP 200 OK，并且你还能看到响应体内容 "Hello!"。如果要在不带 Request-Id 请求头的情况下调用接口，可以使用以下 cURL 命令：

```shell
curl -v http://localhost:8080/hello
```

此调用会生成如下（已截断）响应：

```shell
...
< HTTP/1.1 400
...
```

要调用该接口并添加 Request-Id 请求头，可以使用以下 cURL 命令：

```shell
curl -H "Request-Id:12345" http://localhost:8080/hello
```

此调用会生成如下响应体：

```shell
Hello!
```

## 在过滤器链中，在已有过滤器之后添加新过滤器

本节将介绍如何在过滤器链中某个已有过滤器之后添加新的过滤器。当你希望在过滤器链中某个已有逻辑执行完毕后，再执行自定义逻辑时，可以采用这种方式。比如，你可能需要在认证流程完成后执行一些操作，例如在特定认证事件发生后通知其他系统，或者仅用于日志记录和追踪（见图5.8）。和5.1节类似，我们会通过一个示例来演示具体做法，你可以根据实际需求进行调整和应用。

在我们的示例中，我们通过在认证过滤器之后添加一个过滤器，来记录所有成功的认证事件（见图5.8）。我们认为，凡是能够通过认证过滤器的请求都代表一次成功的认证事件，因此需要将其记录下来。延续第5.1节的示例，我们还会记录通过HTTP请求头接收到的请求ID。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101416821.png){ loading=lazy }
  <figcaption>图 5.8 我们在 BasicAuthenticationFilter 之后添加了 AuthenticationLoggingFilter，用于记录应用程序认证的请求。</figcaption>
</figure>

下面的代码展示了一个过滤器的定义，用于记录通过认证过滤器的请求日志。

``` java title="清单 5.5 定义用于记录请求日志的过滤器"
public class AuthenticationLoggingFilter implements Filter {

  private final Logger logger =
          Logger.getLogger(
          AuthenticationLoggingFilter.class.getName());

  @Override
  public void doFilter(
    ServletRequest request, 
    ServletResponse response, 
    FilterChain filterChain) 
      throws IOException, ServletException {

      var httpRequest = (HttpServletRequest) request;

      var requestId = 
        httpRequest.getHeader("Request-Id");

      logger.info("Successfully authenticated
                   request with id " +  requestId);

      filterChain.doFilter(request, response);
  }
}

```

要在认证过滤器之后将自定义过滤器添加到过滤器链中，可以调用 HttpSecurity 的 addFilterAfter() 方法。下面的代码展示了具体实现。

``` java title="清单 5.6 在过滤器链中，在已有过滤器之后添加自定义过滤器"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.addFilterBefore(
						new RequestValidationFilter(),
						BasicAuthenticationFilter.class)
				.addFilterAfter(
						new AuthenticationLoggingFilter(),
						BasicAuthenticationFilter.class)
				.authorizeRequests(c -> c.anyRequest().permitAll());

		return http.build();
	}
}

```

运行应用程序并调用端点后，我们发现每次成功调用端点时，应用程序都会在控制台打印一条日志。对于该调用

```shell
curl -H "Request-Id:12345" http://localhost:8080/hello
```

响应体是

```shell
Hello!
```

在控制台中，你会看到类似这样的一行信息：

```shell
INFO 5876 --- [nio-8080-exec-2] 
[CA]c.l.s.f.AuthenticationLoggingFilter: 
[CA]Successfully authenticated request with id 12345
```

## 在过滤器链中某个位置添加过滤器

本节将讨论如何在过滤器链中某个已有过滤器的位置添加新的过滤器。这种方法尤其适用于为 Spring Security
已经内置的某个过滤器所承担的职责，提供不同的实现方式。一个典型的场景就是身份认证。

假设你不想使用 HTTP Basic 认证流程，而是希望实现一种不同的认证方式。你不再用用户名和密码作为输入凭证来验证用户身份，而是需要采用其他方法。你可能会遇到以下几种场景：

- 基于静态请求头值进行身份认证
- 使用对称密钥对请求进行签名以实现认证
- 在认证流程中使用一次性密码（OTP）

在第一个场景（基于静态密钥进行身份认证）中，客户端会在 HTTP
请求的头部发送一个字符串，这个字符串始终保持不变。应用程序会在某处（通常是数据库或密钥库）保存这些静态值。应用程序会根据这个静态值来识别客户端身份。

这种方式（见图5.9）在身份认证方面的安全性较弱，但由于其实现简单，架构师和开发人员在后端应用之间的调用中经常选择这种方案。此外，这种实现方式执行速度很快，因为它不像应用加密签名那样需要进行复杂的计算。因此，用于认证的静态密钥实际上是一种折中做法，开发者更多地依赖基础设施层面的安全措施，同时也不会让接口完全暴露在无保护的状态下。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101455341.png){ loading=lazy }
  <figcaption>图5.9 请求中包含一个带有静态密钥值的请求头。如果该值与应用程序已知的密钥匹配，应用程序就会接受该请求。</figcaption>
</figure>

在我们的第二种场景中，使用对称密钥对请求进行签名和验证时，客户端和服务器都知道密钥的值（即双方共享同一个密钥）。客户端使用该密钥对请求的一部分内容进行签名（例如，对特定请求头的值进行签名），而服务器则使用同样的密钥来验证签名是否有效（见图5.10）。服务器可以在数据库或密钥库中为每个客户端分别存储密钥。同样地，你也可以使用一对非对称密钥来实现类似的功能。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101500684.png){ loading=lazy }
  <figcaption>图 5.10 Authorization 请求头中包含一个使用客户端与服务器之间共享的密钥加密的值（或者使用私钥加密，服务器持有相应的公钥）。如果应用程序验证签名有效，则允许该请求继续执行。</figcaption>
</figure>

最后，在第三种场景中，用户会通过短信或使用诸如 Google Authenticator 这样的身份验证器应用收到一次性密码（OTP），并在认证过程中使用该密码（见图
5.11）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101506192.png){ loading=lazy }
  <figcaption>图 5.11 资源访问时，客户端必须使用一次性密码（OTP）。该 OTP 由外部认证服务器生成获取。通常，应用程序会在需要多因素认证的登录流程中采用这种方式。</figcaption>
</figure>


让我们通过一个示例来演示如何应用自定义过滤器。为了让案例既贴合实际又简明易懂，我们将重点放在配置上，并采用一个简单的认证逻辑。在本例中，我们有一个静态密钥的值，对所有请求都是相同的。用户只有在
Authorization 请求头中添加了正确的静态密钥值，才能通过认证，如图 5.12 所示。该示例的代码可以在项目 ssia-ch5-ex2 中找到。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101508501.png){ loading=lazy }
  <figcaption>图 5.12 客户端在 HTTP 请求的 Authorization 头中添加一个静态密钥，服务器在授权请求前会先检查是否识别该密钥。</figcaption>
</figure>

我们首先实现一个名为 StaticKeyAuthenticationFilter 的过滤器类。该类会从属性文件中读取静态密钥的值，并校验 Authorization
头中的值是否与之相同。如果两者一致，过滤器会将请求转发给过滤器链中的下一个组件；如果不一致，过滤器会将响应的 HTTP 状态码设置为
401 Unauthorized，并且不会继续向下转发请求。下面的代码定义了 StaticKeyAuthenticationFilter 类。

```java title="代码清单 5.7 StaticKeyAuthenticationFilter 类的定义"
// 为了让我们能够从属性文件中注入值，它会在 Spring 上下文中添加该类的一个实例。
@Component
// 通过实现 Filter 接口并重写 doFilter() 方法，定义认证逻辑。
public class StaticKeyAuthenticationFilter
		implements Filter {

	// 使用 @Value 注解从属性文件中获取静态键的值
	@Value("${authorization.key}")
	private String authorizationKey;

	@Override
	public void doFilter(ServletRequest request,
						 ServletResponse response,
						 FilterChain filterChain)
			throws IOException, ServletException {

		var httpRequest = (HttpServletRequest) request;
		var httpResponse = (HttpServletResponse) response;

		// 从请求中获取 Authorization 头的值，并与静态密钥进行比对。
		String authentication =
				httpRequest.getHeader("Authorization");

		if (authorizationKey.equals(authentication)) {
			filterChain.doFilter(request, response);
		} else {
			httpResponse.setStatus(
					HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}

```

一旦我们定义好过滤器，就可以通过 addFilterAt() 方法将其添加到过滤器链中，放置在 BasicAuthenticationFilter 这个类的位置（见图
5.13）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202509101513496.png){ loading=lazy }
  <figcaption>图 5.13 我们将自定义认证过滤器添加到原本用于 HTTP Basic 认证时 BasicAuthenticationFilter 所在的位置。这意味着我们的自定义过滤器拥有与其相同的排序值。</figcaption>
</figure>

但请记住我们在第5.1节讨论过的内容：当你在某个特定位置添加过滤器时，Spring Security
并不会假设该位置只有你添加的这一个过滤器。你可能会在同一个链的位置添加多个过滤器。在这种情况下，Spring Security
并不保证这些过滤器的执行顺序。我之所以再次强调这一点，是因为很多人对它的工作方式感到困惑。有些开发者认为，当你在某个已知过滤器的位置添加新的过滤器时，原有的会被替换。其实并不是这样！我们必须确保不要添加那些不需要的过滤器。

!!! note

	我建议你不要在过滤器链的同一位置添加多个过滤器。如果在同一个位置添加多个过滤器，它们的执行顺序是未定义的。明确的调用顺序对于过滤器来说非常重要，已知的顺序能够让你的应用更易于理解和维护。

在代码清单5.8中，你可以看到用于添加过滤器的配置类的定义。请注意，这里我们没有调用 HttpSecurity 类的 httpBasic()
方法，因为我们并不希望将 BasicAuthenticationFilter 实例添加到过滤器链中。

```java title="代码清单 5.8 在配置类中添加过滤器"

@Configuration
public class ProjectConfig {

	private final StaticKeyAuthenticationFilter filter;

	// 省略了构造函数

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {
		http.addFilterAt(filter,
						BasicAuthenticationFilter.class)
				.authorizeRequests(c -> c.anyRequest().permitAll());

		return http.build();
	}
}

```

为了测试应用程序，我们还需要一个接口。为此，我们可以像清单5.4那样定义一个控制器。同时，你需要在 application.properties
文件中为服务器端的静态密钥添加一个值，如下所示。

```properties
authorization.key=SD9cICjl1e
```

!!! note

	在生产环境的应用中，将密码、密钥或任何不应被所有人看到的数据存储在属性文件中绝不是一个好主意。在我们的示例中，为了简化流程并让你专注于 Spring Security 的配置，我们采用了这种方式。但在实际项目中，务必使用专门的密钥管理系统来存储这类敏感信息。

现在我们可以测试这个应用了。预期情况下，应用会允许带有正确 Authorization 请求头的请求通过，并拒绝其他请求，返回 HTTP 401
Unauthorized 状态码。下面的代码片段展示了用于测试应用的 curl 命令。如果你在请求中使用了和服务器端相同的 Authorization
值，调用就会成功，你会看到响应内容 “Hello!”。

```shell
curl -H "Authorization:SD9cICjl1e" http://localhost:8080/hello
```

返回此响应体：

```shell
Hello!
```

通过以下调用，如果缺少 Authorization 头或其内容不正确，响应状态将为 HTTP 401 Unauthorized（未授权）：

```shell
curl -v http://localhost:8080/hello
```

响应状态为

```shell
...
< HTTP/1.1 401
...
```

在这种情况下，由于我们没有配置 UserDetailsService，Spring Boot 会自动为我们配置一个，正如你在第二章中学到的那样。但在我们的场景中，根本不需要
UserDetailsService，因为这里并不存在用户的概念。我们只需要验证请求调用服务器端某个接口的用户是否知道某个特定的值。实际应用场景通常不会这么简单，通常还是需要
UserDetailsService。不过，如果你预见到或者确实遇到不需要这个组件的情况，可以禁用自动配置。要禁用默认 UserDetailsService
的配置，只需在主类的 @SpringBootApplication 注解中使用 exclude 属性即可：

``` java
@SpringBootApplication(exclude = 
 {UserDetailsServiceAutoConfiguration.class })
```

## Spring Security提供的过滤器实现

本节将介绍由 Spring Security 提供的实现了 Filter 接口的相关类。在示例中，我们通过直接实现该接口来定义过滤器。

Spring Security 提供了一些实现了 Filter 接口的抽象类，您可以在自定义过滤器时继承这些类。这些抽象类还为您的实现增加了一些实用功能，继承它们可以带来不少便利。例如，您可以继承
GenericFilterBean 类，这样就可以使用在 web.xml 配置文件中定义的初始化参数（如果适用）。在 GenericFilterBean 的基础上，更常用的一个扩展类是
OncePerRequestFilter。因为当您将过滤器添加到过滤器链时，框架并不保证每个请求只会调用一次过滤器。而顾名思义，OncePerRequestFilter
实现了相关逻辑，确保 doFilter() 方法在每个请求中只会被执行一次。

如果你的应用需要这样的功能，建议直接使用 Spring 提供的相关类。不过，如果你并不需要这些功能，我始终建议尽量保持实现的简单。很多时候，我看到开发者在实现一些并不需要
GenericFilterBean 类自带自定义逻辑的功能时，还是选择继承了 GenericFilterBean，而不是直接实现 Filter
接口。问及原因，他们往往也说不上来，可能只是照搬了网上的示例代码。

为了让大家更清楚地了解如何使用这样的类，我们来写一个示例。在第5.3节中实现的日志功能，就是使用 OncePerRequestFilter
的绝佳场景。我们希望避免对同一个请求进行多次日志记录。由于 Spring Security 并不保证过滤器只会被调用一次，因此我们需要自己处理这个问题。最简单的方法就是基于
OncePerRequestFilter 类来实现过滤器。我在一个名为 ssia-ch5-ex3 的独立项目中完成了这个实现。

在代码清单5.9中，你会看到我对 AuthenticationLoggingFilter 类所做的修改。与第5.3节中的示例不同，这次它没有直接实现 Filter
接口，而是继承了 OncePerRequestFilter 类。我们在这里重写的方法是 doFilterInternal()。你可以在项目 ssia-ch5-ex3 中找到这段代码。

```java title="代码清单 5.9 继承 OncePerRequestFilter 类"
public class AuthenticationLoggingFilter
		extends OncePerRequestFilter {

	private final Logger logger =
			Logger.getLogger(
					AuthenticationLoggingFilter.class.getName());

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws
			ServletException, IOException {

		String requestId = request.getHeader("Request-Id");

		logger.info("Successfully authenticated request with id " +
				requestId);

		filterChain.doFilter(request, response);
	}
}

```

以下是关于 OncePerRequestFilter 类的一些简要观察，或许对你有用：

- 它只支持 HTTP 请求，但实际上我们平时也都是用的 HTTP。好处在于它会自动进行类型转换，我们可以直接拿到 HttpServletRequest 和
  HttpServletResponse 类型的请求和响应。要知道，使用 Filter 接口时，我们还需要手动进行类型转换。
- 你可以实现逻辑来决定是否应用该过滤器。即使你已经把过滤器加到了链中，也可以选择对某些请求不生效。你可以通过重写
  shouldNotFilter(HttpServletRequest) 方法来设置。默认情况下，过滤器会应用到所有请求上。
- 默认情况下，OncePerRequestFilter 不会应用于异步请求或错误分发请求。如果你需要改变这一行为，可以通过重写
  shouldNotFilterAsyncDispatch() 和 shouldNotFilterErrorDispatch() 方法来实现。

如果你觉得 OncePerRequestFilter 的这些特性对你的实现有帮助，建议你使用这个类来定义你的过滤器。

## 总结

- Web应用架构的第一层是过滤器链，它负责拦截HTTP请求。至于Spring Security架构中的其他组件，你也可以根据需求进行自定义。
- 你可以通过在现有过滤器之前、之后或相同位置添加新过滤器，来自定义过滤器链。
- 在同一个过滤器位置可以有多个过滤器，这种情况下，过滤器的执行顺序是不确定的。
- 修改过滤器链可以帮助你根据应用需求自定义认证和授权流程。
