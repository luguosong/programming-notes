# 一个网络应用的安全性始于过滤器

在 Spring Security 中，HTTP 过滤器将不同的职责委托给 HTTP
请求。此外，它们通常管理必须应用于请求的每项职责。因此，这些过滤器形成了一条职责链。一个过滤器接收到请求后，执行其逻辑，并最终将请求委托给链中的下一个过滤器（图
5.1）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409241809368.png){ loading=lazy }
  <figcaption>图5.1 请求被传递到过滤器链。每个过滤器都会调用一个管理器来对请求执行特定逻辑，然后将其传递给链中的下一个过滤器。</figcaption>
</figure>

让我们用一个比喻来说明。当你去机场时，从进入航站楼到登机，你需要经过多个筛选（图5.2）。首先，你需要出示机票，然后验证护照，接着通过安检。在登机口，可能还会有更多的筛选。例如，在某些情况下，登机前会再次验证护照和签证。这与Spring
Security中的过滤器链非常相似。同样，你可以在Spring Security中自定义过滤器链中的过滤器。Spring
Security提供了可以通过自定义添加到过滤器链中的过滤器实现，但你也可以定义自定义过滤器。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409241810796.png){ loading=lazy }
  <figcaption>图5.2 在机场，你需要经过一系列检查点，最终才能登上飞机。同样地，Spring Security 实现了一系列过滤器，用于处理应用程序接收到的 HTTP 请求。</figcaption>
</figure>

本章将讨论如何使用 Spring Security 自定义 Web 应用程序中`身份验证`和`授权`
架构的一部分过滤器。例如，您可能希望通过为用户增加一个步骤来增强身份验证，比如检查他们的电子邮件地址或使用一次性密码。您还可以添加与审计身份验证事件相关的功能。您会发现应用程序在各种场景中使用身份验证审计，从调试目的到识别用户行为。如今的技术和机器学习算法可以改进应用程序，例如，通过学习用户行为来判断是否有人入侵他们的账户或冒充用户。

了解如何自定义HTTP过滤器责任链是一项宝贵的技能。在实际应用中，应用程序通常有各种需求，默认配置可能不再适用。您需要添加或替换链中的现有组件。默认实现中使用的是HTTP基本身份验证方法，这允许您依赖用户名和密码。然而，在实际场景中，您可能需要更多功能。也许您需要实施不同的身份验证策略，通知外部系统关于授权事件，或者记录成功或失败的身份验证，以便后续进行追踪和审计（如图5.3所示）。无论您的场景如何，Spring
Security为您提供了灵活性，可以根据需要精确地建模过滤器链。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409241812148.png){ loading=lazy }
  <figcaption>图5.3 您可以通过在现有过滤器之前、之后或替换现有过滤器来个性化过滤器链。这样，您不仅可以定制认证过程，还可以定制请求和响应的整体处理。</figcaption>
</figure>

## 在 Spring Security 架构中实现过滤器

本节讨论了过滤器及过滤器链在 Spring Security 架构中的工作方式。首先需要了解这一概述，以便理解我们将在后续部分中进行的实现示例。在第
2 章和第 3 章中，我们了解到认证过滤器会拦截请求，并将认证责任进一步委托给`授权管理器`
。如果我们想在认证之前执行某些逻辑，可以通过在认证过滤器之前插入一个过滤器来实现。

在 Spring Security 架构中，过滤器是典型的 HTTP 过滤器。我们可以通过实现 jakarta.servlet 包中的 `Filter` 接口来创建过滤器。与其他
HTTP 过滤器一样，你需要重写 `doFilter()` 方法来实现其逻辑。此方法接收 `ServletRequest`、`ServletResponse` 和 `FilterChain`
作为参数：

- `ServletRequest`—表示HTTP请求。我们使用ServletRequest对象来获取有关请求的详细信息。
- `ServletResponse`—表示HTTP响应。我们使用ServletResponse对象在将响应发送回客户端或传递到过滤链的下一步之前对其进行修改。
- `FilterChain`—表示过滤器链。我们使用 FilterChain 对象将请求转发到链中的下一个过滤器。

!!! note

	从 Spring Boot 3 开始，Jakarta EE 取代了旧的 Java EE 规范。由于这一变化，您会注意到一些包的前缀从“javax”变为“jakarta”。例如，像 Filter、ServletRequest 和 ServletResponse 这样的类型，之前位于 javax.servlet 包中，现在则在 jakarta.servlet 包中。

过滤器链表示一组具有特定执行顺序的过滤器。Spring Security 为我们提供了一些过滤器实现及其顺序。以下是其中的一些过滤器：

- `BasicAuthenticationFilter` 负责处理 HTTP 基本身份验证（如果存在）。
- `CsrfFilter` 负责处理跨站请求伪造（CSRF）保护，我们将在第9章中讨论。
- `CorsFilter` 负责处理跨域资源共享 (CORS) 授权规则，我们将在第10章中讨论这一点。

你不需要了解所有的过滤器，因为你可能不会直接在代码中使用它们，但你需要理解过滤器链的工作原理，并了解一些实现。在本书中，我只解释对我们讨论的各种主题至关重要的过滤器。

重要的是要理解，一个应用程序的过滤器链中不一定包含所有这些过滤器的实例。链的长短取决于你如何配置应用程序。例如，在第2章和第3章中，你了解到如果想使用HTTP基本认证方法，就需要调用
`HttpSecurity`类的`httpBasic()`方法。调用`httpBasic()`方法后，`BasicAuthenticationFilter`
的一个实例会被添加到链中。同样，根据你编写的配置，过滤器链的定义也会受到影响。

您可以在链中相对于另一个过滤器添加一个新过滤器（图5.4）。或者，您可以在已知过滤器之前、之后或其位置添加一个过滤器。每个位置实际上是一个索引（一个数字），您可能还会看到它被称为
`顺序`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409242155498.png){ loading=lazy }
  <figcaption>图5.4 每个过滤器都有一个序号，决定了过滤器在请求中应用的顺序。你可以在 Spring Security 提供的过滤器基础上添加自定义过滤器。</figcaption>
</figure>

如果您想了解更多关于 Spring Security 提供的过滤器及其配置顺序的信息，可以查看枚举
SecurityWebFiltersOrder，访问[地址](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/config/web/server/SecurityWebFiltersOrder.html)。

您可以在同一位置添加两个或多个过滤器（图5.5）。在第5.4节中，我们将遇到一个常见的情况，这种情况通常会让开发人员感到困惑。

!!! note

	如果多个过滤器具有相同的位置，则它们的调用顺序未定义。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409242157306.png){ loading=lazy }
  <figcaption>图5.5 在过滤器链中，您可能会有多个具有相同顺序值的过滤器。在这种情况下，Spring Security 不保证它们的调用顺序。</figcaption>
</figure>

## 在链中现有过滤器之前添加一个过滤器

本节讨论如何在过滤器链中将自定义HTTP过滤器应用于现有过滤器之前。你可能会遇到这种情况，在这种情况下，这会很有用。为了以实用的方式解决这个问题，我们将以一个项目为例，你将学习如何轻松实现一个自定义过滤器，并在过滤器链中将其应用于现有过滤器之前。然后，你可以将这个示例调整为在生产应用中遇到的任何类似需求。

对于我们的第一个自定义过滤器实现，让我们考虑一个简单的场景。我们希望确保每个请求都有一个名为 `Request-Id` 的头（参见项目
ssia-ch5-ex1[^1]
）。我们假设我们的应用程序使用这个头来跟踪请求，并且这个头是必需的。同时，我们希望在应用程序执行身份验证之前验证这些假设。身份验证过程可能涉及查询数据库或其他消耗资源的操作，如果请求格式无效，我们不希望应用程序执行这些操作。我们该如何做到这一点呢？解决当前需求只需两个步骤，最后过滤器链如图
5.6 所示。

[^1]:ssia-ch5-ex1:在现有过滤器前后添加自定义过滤器

1. 实现过滤器。创建一个`RequestValidationFilter`类，用于检查请求中是否存在所需的头信息。
2. 将过滤器添加到过滤器链中。在配置类中使用 SecurityFilterChain bean 来完成此操作。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409242338197.png){ loading=lazy }
  <figcaption>图5.6 在我们的示例中，我们添加了一个RequestValidationFilter，它在身份验证过滤器之前起作用。RequestValidationFilter确保如果请求验证失败，则不会进行身份验证。在我们的情况下，请求必须包含一个名为Request-Id的必填头。</figcaption>
</figure>

要完成步骤1——实现过滤器，我们需要定义一个自定义过滤器。下面的列表展示了具体的实现。

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

在 `doFilter()` 方法中，我们编写过滤器的逻辑。在我们的例子中，我们检查 `Request-Id` 头是否存在。如果存在，我们通过调用
`doFilter()` 方法将请求转发到链中的下一个过滤器。如果头不存在，我们在响应中设置 HTTP 状态 400 Bad
Request，而不将其转发到链中的下一个过滤器（图 5.7）。代码清单 5.2 展示了该逻辑。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409250918097.png){ loading=lazy }
  <figcaption>图5.7 我们在认证之前添加的自定义过滤器会检查请求中是否存在Request-Id头。如果请求中存在该头，应用程序将转发请求进行认证。如果该头不存在，应用程序将设置HTTP状态为400 Bad Request并返回给客户端。</figcaption>
</figure>

``` java title="清单5.2 在 doFilter() 方法中实现逻辑"

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

要实现步骤2，在配置类中应用过滤器，我们使用`HttpSecurity`对象的`addFilterBefore()`方法，因为我们希望应用程序在认证之前执行这个自定义过滤器。此方法接收两个参数：

- 我们想要添加到链中的`自定义过滤器实例`——在我们的例子中，这是清单5.1中展示的`RequestValidationFilter`类的一个实例。
- 在添加新实例之前的`过滤器类型`——在这个例子中，由于要求在认证之前执行过滤器逻辑，我们需要在认证过滤器之前添加自定义过滤器实例。
  `BasicAuthenticationFilter` 类定义了认证过滤器的默认类型。

到目前为止，我们通常将处理身份验证的过滤器称为`身份验证过滤器`。你将在接下来的章节中了解到，Spring Security
还配置了其他过滤器。在第9章中，我们将讨论`跨站请求伪造（CSRF）保护`，而在第10章中，我们将讨论`跨域资源共享（CORS）`
。这两种功能也依赖于过滤器。

下面的示例展示了如何在配置类中将自定义过滤器添加到认证过滤器之前。为了简化示例，我们使用了`permitAll()`方法来允许所有未经认证的请求。

``` java title="清单 5.3 在认证之前配置自定义过滤器"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/05_begins_with_filters/ssia-ch5-ex1/src/main/java/com/luguosong/ssiach5ex1/config/ProjectConfig.java"
```

我们还需要一个控制器类和一个端点来测试功能。下面的列表定义了控制器类。

``` java title="清单 5.4 控制器类"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/05_begins_with_filters/ssia-ch5-ex1/src/main/java/com/luguosong/ssiach5ex1/controllers/HelloController.java"
```

您现在可以运行和测试该应用程序了。如果在没有添加请求头的情况下调用端点，会生成一个HTTP状态为400 Bad
Request的响应。如果您在请求中添加了请求头，响应状态将变为HTTP 200 OK，并且您还会看到响应主体“Hello!
”。要在没有Request-Id请求头的情况下调用端点，我们使用以下cURL命令：

```shell
curl -v http://localhost:8080/hello
```

此调用生成以下（截断的）响应：

```shell
...
< HTTP/1.1 400
...
```

要调用端点并提供 `Request-Id` 头信息，我们使用以下 cURL 命令：

```shell
curl -H "Request-Id:12345" http://localhost:8080/hello
```

此调用生成以下响应正文：

```shell
Hello!
```

## 在链中现有过滤器之后添加一个过滤器

本节说明如何在过滤器链中现有过滤器之后添加一个过滤器。当你希望在过滤器链中已有的某些逻辑之后执行其他操作时，可以使用这种方法。假设你需要在认证过程之后执行一些逻辑。这可能包括在某些认证事件后
`通知其他系统`，或者仅仅用于`记录和追踪`目的（如图5.8所示）。与第5.1节类似，我们将通过一个示例来展示如何实现这一点。你可以根据实际情况进行调整。

在我们的示例中，我们通过在`身份验证过滤器`之后添加一个过滤器来`记录所有成功的身份验证事件`
（图5.8）。我们认为，绕过身份验证过滤器的事件代表成功的身份验证事件，并且我们希望对此进行记录。延续第5.1节的示例，我们还记录通过HTTP头接收到的请求ID。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261401487.png){ loading=lazy }
  <figcaption>图5.8 我们在 BasicAuthenticationFilter 之后添加 AuthenticationLoggingFilter，以记录应用程序认证的请求。</figcaption>
</figure>

以下列表展示了一个过滤器的定义，该过滤器记录通过身份验证过滤器的请求。

``` java title="清单 5.5 定义用于记录请求的过滤器"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/05_begins_with_filters/ssia-ch5-ex1/src/main/java/com/luguosong/ssiach5ex1/filters/AuthenticationLoggingFilter.java"
```

要在身份验证过滤器之后的链中添加自定义过滤器，可以调用 `HttpSecurity` 的 `addFilterAfter()` 方法。下面的列表展示了具体实现。

``` java title="清单5.6 在过滤器链中在现有过滤器之后添加自定义过滤器"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/05_begins_with_filters/ssia-ch5-ex1/src/main/java/com/luguosong/ssiach5ex1/config/ProjectConfig.java"
```

在运行应用程序并调用端点后，我们观察到每次成功调用端点时，应用程序都会在控制台中打印一行日志。对于该调用，

````shell
curl -H "Request-Id:12345" http://localhost:8080/hello
````

响应正文是:

```shell
Hello!
```

在控制台中，你可以看到类似于以下的行

```shell
请求已成功通过身份验证，ID为 12345
```

## 在链中某个位置添加一个过滤器

本节讨论在过滤器链中某个位置添加过滤器。这种方法尤其适用于为Spring Security已知的某个过滤器所承担的职责提供不同的实现。一个典型的场景是
`身份验证`。

假设您不想使用HTTP基本身份验证流程，而是想实施其他方法。与其使用用户名和密码作为应用程序验证用户的输入凭据，您需要采用另一种方法。您可能遇到的一些场景示例包括：

- 基于静态头部值的身份验证
- 使用对称密钥对请求进行身份验证签名
- 在认证过程中使用一次性密码 (OTP)

在我们的第一个场景中（基于`静态密钥`
进行身份验证），客户端在HTTP请求的头部发送一个字符串，该字符串始终相同。应用程序将这些值存储在某个地方，最有可能是在数据库或密钥库中。应用程序根据这个静态值识别客户端。

这种方法（图5.9）在身份验证方面提供的安全性较弱，但由于其简单性，架构师和开发人员在后端应用程序之间的调用中经常选择这种方法。由于不需要进行复杂的计算，例如应用加密签名，这种实现的执行速度也很快。通过这种方式，用于身份验证的
`静态密钥`代表了一种折中方案，开发人员在安全性方面更多地依赖于基础设施层，同时也不会让端点完全没有保护。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261435106.png){ loading=lazy }
  <figcaption>图5.9 请求包含一个具有静态密钥值的头部。如果该值与应用程序已知的值匹配，则接受该请求。</figcaption>
</figure>

在我们的第二种场景中，使用`对称密钥`
来签署和验证请求，客户端和服务器都知道密钥的值（客户端和服务器共享该密钥）。客户端使用这个密钥对请求的一部分进行签名（例如，对特定头部的值进行签名），服务器则使用相同的密钥检查签名是否有效（图5.10）。服务器可以在数据库或秘密存储中为每个客户端存储单独的密钥。同样，你也可以使用一对
`非对称密钥`。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261439917.png){ loading=lazy }
  <figcaption>图 5.10 Authorization 头包含一个使用客户端和服务器之间共享的密钥加密的值（或使用服务器拥有对应公钥的私钥加密的值）。如果应用程序验证签名有效，则允许请求继续进行。</figcaption>
</figure>

最后，在我们的第三种情境中，用户在认证过程中使用`一次性密码（OTP）`，可以通过`短信`接收OTP，或者使用像`Google Authenticator`
这样的认证应用程序（见图5.11）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261442048.png){ loading=lazy }
  <figcaption>图5.11 为了访问资源，客户端必须使用一次性密码（OTP）。此OTP从外部认证服务器获取。通常，应用程序在需要多因素认证的登录过程中会采用这种方法。</figcaption>
</figure>

让我们通过一个示例来演示如何应用自定义过滤器。为了保持案例的相关性和简洁性，我们专注于配置，并考虑一个简单的认证逻辑。在我们的场景中，我们有一个
`静态密钥`的值，这个值对所有请求都是相同的。要进行认证，用户必须在授权头中添加正确的静态密钥值，如图5.12所示。你可以在项目ssia-ch5-ex2[^2]中找到这个示例的代码。

[^2]:ssia-ch5-ex2：在指定位置添加自定义过滤器

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261445936.png){ loading=lazy }
  <figcaption>图 5.12 客户端在 HTTP 请求的 Authorization 头中添加一个静态密钥。服务器在授权请求之前会检查是否识别该密钥。</figcaption>
</figure>

我们首先实现一个名为`StaticKeyAuthenticationFilter`的过滤器类。这个类从属性文件中读取`静态密钥`的值，并验证
`Authorization头`的值是否与之相等。如果值相同，过滤器将请求转发给过滤器链中的下一个组件。如果不相同，过滤器将HTTP响应状态设置为
`401 Unauthorized`，而不在过滤器链中转发请求。以下是`StaticKeyAuthenticationFilter`类的定义。

``` java title="清单5.7 StaticKeyAuthenticationFilter类的定义"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/05_begins_with_filters/ssia-ch5-ex2/src/main/java/com/luguosong/ssiach5ex2/filters/StaticKeyAuthenticationFilter.java"
```

一旦我们定义了过滤器，就可以使用 `addFilterAt()` 方法将其添加到过滤器链中 `BasicAuthenticationFilter` 类的位置（图 5.13）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202409261504458.png){ loading=lazy }
  <figcaption>图5.13 我们在原本使用HTTP Basic作为认证方法时BasicAuthenticationFilter类所在的位置添加了自定义认证过滤器。这意味着我们的自定义过滤器具有相同的排序值。</figcaption>
</figure>

请记住我们在第5.1节讨论的内容。当在特定位置添加过滤器时，Spring Security并不认为它是该位置唯一的过滤器。你可能会在链中的同一位置添加更多过滤器。在这种情况下，
`Spring Security不保证这些过滤器的执行顺序`
。我重复这一点是因为我见过很多人对其工作方式感到困惑。一些开发人员认为，当你在已知位置应用过滤器时，它会被替换。事实并非如此！我们必须确保不添加不需要的过滤器。

!!! note

	我建议你不要在链中的同一位置添加多个过滤器。当你在同一位置添加更多过滤器时，它们的使用顺序是不确定的。拥有一个明确的调用顺序是有意义的，已知的顺序使你的应用程序更易于理解和维护。

在代码清单5.8中，你可以找到添加过滤器的配置类定义。注意，我们在这里没有调用HttpSecurity类的`httpBasic()`方法，因为我们不希望将
`BasicAuthenticationFilter`实例添加到过滤器链中。

``` java title="清单 5.8 在配置类中添加过滤器"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/05_begins_with_filters/ssia-ch5-ex2/src/main/java/com/luguosong/ssiach5ex2/config/ProjectConfig.java"
```

要测试应用程序，我们还需要一个端点。为此，我们定义一个控制器，如清单5.4所示。你应该在`application.properties`
文件中为服务器的静态密钥添加一个值，如下所示。

```properties
authorization.key=SD9cICjl1e
```

!!! note

	将密码、密钥或任何不应被所有人看到的数据存储在属性文件中，对于生产应用程序来说从来都不是一个好主意。在我们的示例中，我们采用这种方法是为了简化操作，并让您专注于我们所做的 Spring Security 配置。但在实际场景中，请务必使用秘密库来存储此类信息。

我们现在可以测试这个应用程序。预计该应用程序将允许具有正确授权头部值的请求，并拒绝其他请求，返回HTTP
401未授权状态作为响应。以下代码片段展示了用于测试应用程序的curl调用。如果您在服务器端为授权头部使用相同的值，调用将成功，您将看到响应主体：Hello!
调用

```shell
curl -H "Authorization:SD9cICjl1e" http://localhost:8080/hello
```

返回此响应主体：

```shell
Hello!
```

在以下调用中，如果缺少或错误填写了Authorization头，响应状态将是HTTP 401 Unauthorized：

```shell
curl -v http://localhost:8080/hello
```

响应状态是:

```shell
...
< HTTP/1.1 401
...
```

在这种情况下，因为我们没有配置 `UserDetailsService`，所以 Spring Boot 会自动配置一个，如你在第 2 章中所学。但在我们的场景中，你根本不需要
`UserDetailsService`，因为用户的概念并不存在。我们只需验证请求调用服务器端点的用户是否知道某个特定值。应用场景通常不会如此简单，通常需要一个
`UserDetailsService`。然而，如果你预见到或遇到不需要这个组件的情况，可以禁用自动配置。要禁用默认 `UserDetailsService`
的配置，可以在主类的 `@SpringBootApplication` 注解中使用 `exclude` 属性。

``` java
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class })
```

## Spring Security 提供的过滤器实现

本节讨论由 Spring Security 提供的实现 `Filter` 接口的类。在示例中，我们通过直接实现该接口来定义过滤器。

Spring Security 提供了一些实现了 Filter 接口的抽象类，您可以通过扩展这些类来定义自己的过滤器。这些类还增加了一些功能，您的实现可以从中受益。例如，您可以扩展
`GenericFilterBean` 类，该类允许您使用在 web.xml 描述文件中定义的初始化参数。一个更有用的扩展自 `GenericFilterBean` 的类是
`OncePerRequestFilter`。在将过滤器添加到链中时，框架并不保证每个请求只调用一次。顾名思义，`OncePerRequestFilter` 实现了确保过滤器的
`doFilter()` 方法每个请求仅执行一次的逻辑。

如果你的应用程序需要这样的功能，可以使用 Spring 提供的类。然而，如果不需要这些功能，我总是建议尽可能简化你的实现。我经常看到开发人员在不需要
`GenericFilterBean` 类所添加的自定义逻辑的功能中，扩展了 `GenericFilterBean` 类，而不是实现 `Filter`
接口。当被问及原因时，他们似乎并不知道。可能是因为他们在网上的示例中看到了这样的实现，就直接复制了。

为了清楚地说明如何使用这样的类，让我们写一个例子。我们在第5.3节中实现的日志功能非常适合使用`OncePerRequestFilter`
。我们希望避免对同一请求进行多次日志记录。Spring Security并不保证过滤器不会被多次调用，因此我们需要自己处理这个问题。最简单的方法是使用
`OncePerRequestFilter`类来实现过滤器。我在一个名为ssia-ch5-ex3的独立项目中写了这个。

在代码清单5.9中，你会看到我对`AuthenticationLoggingFilter`类所做的更改。与5.3节中的示例直接实现Filter接口不同，现在它扩展了
`OncePerRequestFilter`类。我们在这里重写的方法是`doFilterInternal()`。你可以在项目ssia-ch5-ex3[^3]中找到这段代码。

[^3]:ssia-ch5-ex3:自定义过滤器继承OncePerRequestFilter，达到同一请求不被执行多次的效果

``` java title="清单 5.9 扩展 OncePerRequestFilter 类"
--8<-- "code/java-serve/authentication/spring-security/part2_configuring_authentication/05_begins_with_filters/ssia-ch5-ex3/src/main/java/com/luguosong/ssiach5ex3/filters/AuthenticationLoggingFilter.java"
```

以下是关于OncePerRequestFilter类的一些简短观察，可能对您有所帮助：

- 它仅支持 HTTP 请求，但这实际上是我们一直使用的。其优势在于它会进行类型转换，我们可以直接将请求接收为 `HttpServletRequest`
  和 `HttpServletResponse`。记住，在使用 Filter 接口时，我们必须对请求和响应进行类型转换。
- 您可以实现逻辑来决定是否应用过滤器。即使您已将过滤器添加到链中，您也可能决定它不适用于某些请求。您可以通过重写
  `shouldNotFilter(HttpServletRequest)` 方法来设置这一点。默认情况下，过滤器适用于所有请求。
- 默认情况下，`OncePerRequestFilter` 不适用于`异步请求`或`错误调度请求`。您可以通过重写方法
  `shouldNotFilterAsyncDispatch()` 和 `shouldNotFilterErrorDispatch()` 来更改此行为。

如果您在实现中发现 `OncePerRequestFilter` 的这些特性有用，我建议使用这个类来定义您的过滤器。

## 小结

- Web应用程序架构的第一层是拦截HTTP请求的过滤器链。至于Spring Security架构中的其他组件，您可以根据需求进行自定义。
- 您可以通过在现有过滤器之前、之后或当前位置添加新过滤器来自定义过滤链。
- 您可以在现有过滤器的同一位置添加多个过滤器。在这种情况下，过滤器的执行顺序未定义。
- 更改过滤器链可以帮助您定制身份验证和授权，以满足应用程序的需求。
