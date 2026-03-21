# 9.配置CSRF防护

本章内容包括：

- 了解 CSRF 攻击
- 实现 CSRF 防护
- 定制 CSRF 防护

你已经了解了过滤器链及其在 Spring Security 架构中的作用。在第 5 章中，我们通过多个示例对过滤器链进行了定制。不过，Spring
Security 也会向过滤器链中添加自己的过滤器。本章将重点介绍用于配置 CSRF（跨站请求伪造）防护的过滤器，并带你学习如何根据实际场景灵活定制这些过滤器，使其更加契合你的需求。

你可能已经注意到，到目前为止的大多数示例中，我们只用 HTTP GET 实现了接口。而当我们需要配置 HTTP POST 时，还必须在配置中额外添加一条指令来关闭
CSRF 保护。这是因为 Spring Security 默认启用了 CSRF 保护，所以你无法直接通过 HTTP POST 调用接口。

接下来我们将讨论 CSRF 防护以及在应用程序中何时使用它。CSRF
是一种常见的攻击方式，存在漏洞的应用可能会在用户认证后，强迫用户在网页应用上执行一些不想要的操作。你肯定不希望自己开发的应用存在
CSRF 漏洞，让攻击者有机会诱导用户执行未经授权的操作。

由于了解如何防范这些漏洞至关重要，我们首先回顾一下什么是CSRF以及它的工作原理。接着，我们将讨论Spring
Security用来防御CSRF漏洞的令牌机制。随后，我们会介绍如何获取CSRF令牌，并利用它通过HTTP
POST方法调用接口。我们将通过一个包含REST接口的小型应用来进行演示。在掌握了Spring
Security实现CSRF令牌机制的原理后，我们还会探讨如何在实际应用场景中使用这一机制。最后，你还将了解在Spring
Security中对CSRF令牌机制进行自定义的可能方式。

## Spring Security中的CSRF防护机制是如何工作的

本节将介绍 Spring Security 如何实现 CSRF 防护。在此之前，理解 CSRF 防护的基本机制至关重要。我经常遇到一些情况，由于对 CSRF
防护原理的误解，导致在本应启用的场景下被禁用，或者反之。和框架中的其他功能一样，只有正确使用，才能真正为你的应用程序带来价值。

例如，假设有这样一个场景（见图9.1）：你正在公司使用一个网页工具来存储和管理你的文件。通过这个工具的网页界面，你可以添加新文件、为已有记录上传新版本，甚至可以删除文件。此时你收到一封邮件，邀请你打开一个网页，理由可能是你最喜欢的商店正在搞促销。你点开了这个页面，但页面要么是空白的，要么直接跳转到了你熟悉的网站（比如你常逛的网店）。当你回到工作界面时，却发现所有文件都不见了！

发生了什么？你当时已经登录了工作应用，以便管理你的文件。当你添加、修改或删除文件时，网页会向服务器请求相关接口来执行这些操作。而当你点击邮件中的未知链接，打开了外部页面时，那个页面就调用了你应用的后端，并以你的身份执行了一些操作（比如删除了你的文件）。

之所以会这样，是因为你之前已经登录过，所以服务器信任这些操作是由你发起的。你可能觉得，别人不可能那么轻易就让你点击陌生邮件或消息里的链接，但相信我，这种情况真的经常发生在很多人身上。大多数网页应用的用户其实并不了解安全风险。所以，作为懂得这些“套路”的开发者，你更应该主动保护用户，构建安全的应用，而不是指望用户自己去防范风险。

CSRF攻击假设用户已经登录了某个Web应用。攻击者会诱导用户打开一个包含恶意脚本的页面，这些脚本会在用户正在使用的同一个应用中执行操作。由于用户已经登录（这是我们的前提），伪造的代码就能够冒充用户，代表用户执行各种操作。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211431016.png){ loading=lazy }
  <figcaption>图9.1 用户登录账户后，访问了一个包含伪造代码的页面。这段代码会冒充用户身份，代表用户执行一些不希望发生的操作。</figcaption>
</figure>

我们如何保护用户免受此类情况的影响？CSRF防护的目标是确保只有网页应用的前端能够执行变更操作（通常指除了GET、HEAD、TRACE或OPTIONS之外的HTTP方法）。这样，像我们示例中的外部页面就无法代表用户进行操作。

我们该如何实现这一点呢？可以确定的是，在用户进行任何可能修改数据的操作之前，必须先通过 HTTP GET
请求至少访问一次网页。此时，应用程序会生成一个唯一的令牌。之后，应用程序只接受在请求头中包含该唯一令牌的变更操作请求（如
POST、PUT、DELETE 等）。

应用认为，知晓令牌的值就足以证明是应用自身在发起变更请求，而不是其他系统。任何包含变更操作（如 POST、PUT、DELETE 等）的页面，都应通过
CSRF 令牌接收响应，并且在进行变更请求时必须使用该令牌。

CSRF防护的起点是过滤器链中的一个名为 CsrfFilter 的过滤器。CsrfFilter 会拦截请求，并允许所有使用以下 HTTP
方法的请求：GET、HEAD、TRACE 和 OPTIONS。对于其他类型的请求，过滤器则要求请求头中携带一个令牌。如果请求头不存在或令牌值不正确，应用程序会拒绝该请求，并将响应状态设置为
HTTP 403 Forbidden（禁止访问）。

这个令牌是什么，它是从哪里来的？这些令牌其实就是字符串值。当你使用除 GET、HEAD、TRACE 或 OPTIONS
之外的其他请求方法时，必须在请求头中添加令牌。如果没有这样做，应用程序就不会接受该请求，如图 9.2 所示。

<figure markdown="span">
 ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211458595.png){ loading=lazy }
  <figcaption>图 9.2 要发起 POST 请求，客户端需要在请求头中添加 CSRF 令牌。应用程序会在页面加载时（通过 GET 请求）生成一个 CSRF 令牌，并将该令牌添加到所有可以从已加载页面发起的请求中。这样，只有当前加载的页面才能进行更改数据的请求。</figcaption>
</figure>


CsrfFilter（见图9.3）使用一个名为 CsrfTokenRepository 的组件来管理 CSRF
令牌的值，包括生成新令牌、存储令牌以及最终使令牌失效。默认情况下，CsrfTokenRepository 会将令牌存储在 HTTP 会话(session)
中，并将令牌生成为随机字符串。在大多数情况下，这样已经足够了，但正如你将在第9.3节中了解到的，如果默认实现无法满足你的需求，你也可以自定义
CsrfTokenRepository 的实现。

在本节中，我通过大量文字和图示详细讲解了Spring
Security中CSRF防护的工作原理。为了加深你的理解，我还准备了一个简单的代码示例。你可以在名为ssia-ch9-ex1的项目中找到这段代码。接下来，我们将创建一个应用程序，暴露两个接口：其中一个通过HTTP
GET请求访问，另一个通过HTTP POST请求访问。

正如你现在已经知道的，不能在不禁用 CSRF 保护的情况下直接调用 POST 接口。在本示例中，你将学习如何在不关闭 CSRF 保护的前提下调用
POST 接口。你需要先获取 CSRF Token，然后在发起 HTTP POST 请求时，将其放在请求头中使用。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211502529.png){ loading=lazy }
  <figcaption>图9.3 CsrfFilter 是过滤器链中的一个过滤器。它接收请求，并最终将其转发给链中的下一个过滤器。为了管理 CSRF 令牌，CsrfFilter 使用了 CsrfTokenRepository。</figcaption>
</figure>

正如你从这个例子中学到的，CsrfFilter 会将生成的 CSRF 令牌添加到名为 _csrf 的 HTTP 请求属性中（见图 9.4）。了解这一点后，我们就知道在
CsrfFilter 处理之后，可以通过该属性获取令牌的值。对于这个小型应用，我们选择在 CsrfFilter 之后添加一个自定义过滤器，这一点你在第
5 章已经学过。你可以利用这个自定义过滤器，在我们通过 HTTP GET 调用接口时，将应用生成的 CSRF
令牌打印到控制台。这样，我们就可以从控制台复制令牌的值，并用它来进行 HTTP POST 的变更操作。在代码清单 9.1
中，你可以看到用于测试的控制器类及其包含的两个接口的定义。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211600176.png){ loading=lazy }
  <figcaption>图 9.4 在 CsrfFilter 之后插入 CsrfTokenLogger（高亮部分），可以让它从请求的 _csrf 属性中获取令牌的值，而 CsrfFilter 正是将令牌存放在这里。CsrfTokenLogger 会将 CSRF 令牌输出到应用的控制台，方便我们在进行 HTTP POST 请求到某个端点时获取和使用该令牌。</figcaption>
</figure>

```java title="清单9.1 包含两个端点的控制器类"

@RestController
public class HelloController {

	@GetMapping("/hello")
	public String getHello() {
		return "Get Hello!";
	}

	@PostMapping("/hello")
	public String postHello() {
		return "Post Hello!";
	}
}
```

清单9.2定义了一个自定义过滤器，用于在控制台打印CSRF令牌的值。我将这个自定义过滤器命名为CsrfTokenLogger。每当过滤器被调用时，它会从请求属性 _
csrf 中获取CSRF令牌的值，并将其打印到控制台。请求属性名 _csrf
是CsrfFilter设置生成的CSRF令牌的地方，其值是CsrfToken类的一个实例。这个CsrfToken实例包含了CSRF令牌的字符串值，可以通过调用getToken()
方法获取。

```java title="清单9.2 自定义过滤器类的定义"
public class CsrfTokenLogger implements Filter {

	private Logger logger =
			Logger.getLogger(CsrfTokenLogger.class.getName());

	@Override
	public void doFilter(
			ServletRequest request,
			ServletResponse response,
			FilterChain filterChain)
			throws IOException, ServletException {

		CsrfToken o =
				(CsrfToken) request.getAttribute("_csrf");

		logger.info("CSRF token " + token.getToken());

		filterChain.doFilter(request, response);
	}
}

```

在配置类中，我们添加了自定义过滤器。下面的代码展示了配置类。请注意，在这个代码中我并没有禁用 CSRF 防护。

```java title="代码清单 9.3 在配置类中添加自定义过滤器"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain configure(HttpSecurity http)
			throws Exception {

		http.addFilterAfter(
						new CsrfTokenLogger(), CsrfFilter.class)
				.authorizeHttpRequests(
						c -> c.anyRequest().permitAll()
				);

		return http.build();
	}
}
```

现在我们可以开始测试这些接口了。首先，使用 HTTP GET 方法调用接口。由于 CsrfTokenRepository 接口的默认实现会在服务器端通过
HTTP 会话（session）来存储令牌值，因此我们还需要记住会话 ID。为此，我在调用时添加了 -v 参数，这样可以在响应中看到更多详细信息，包括会话
ID。调用接口如下：

```shell
curl -v http://localhost:8080/hello
```

返回此（已截断的）响应：

```shell
...
< Set-Cookie: JSESSIONID=21ADA55E10D70BA81C338FFBB06B0206;
...
Get Hello!
```

按照应用控制台中的提示，您可以找到包含 CSRF 令牌的日志行：

```shell
INFO 21412 --- [nio-8080-exec-1] c.l.ssia.filters.CsrfTokenLogger : CSRF token tAlE3LB_R_KN48DFlRChc…
```

!!! note

    你可能会好奇，客户端是如何获取 CSRF 令牌的。他们既无法猜测，也不能通过服务器日志读取。这个示例是我特意设计的，目的是让你更容易理解 CSRF 防护的实现原理。正如你将在第 9.2 节中看到的，后端应用有责任在 HTTP 响应中添加 CSRF 令牌的值，供客户端使用。

如果你在调用该端点时使用 HTTP POST 方法但未提供 CSRF 令牌，服务器将返回 403 Forbidden 状态，如下命令行所示：

```shell
curl -XPOST http://localhost:8080/hello
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

但是，如果你提供了正确的 CSRF 令牌值，请求就会成功。你还需要指定会话 ID（JSESSIONID），因为 CsrfTokenRepository 的默认实现会将
CSRF 令牌的值存储在会话中。

```shell
curl -X POST   http://localhost:8080/hello 
-H 'Cookie: JSESSIONID=21ADA55E10D70BA81C338FFBB06B0206'   
-H 'X-CSRF-TOKEN: tAlE3LB_R_KN48DFlRChc…'
```

响应体是

```shell
Post Hello!
```

## 在实际场景中应用CSRF防护

在本节中，我们将讨论如何在实际场景中应用 CSRF 防护。现在你已经了解了 Spring Security 中 CSRF
防护的工作原理，接下来需要弄清楚在真实项目中应该在哪些地方使用它。哪些类型的应用需要启用 CSRF 防护？

你会在运行于浏览器中的 Web 应用中使用 CSRF 防护，因为你需要预期浏览器能够执行对应用内容进行更改的操作。这里我能举的最基本的例子，就是一个基于标准
Spring MVC 流程开发的简单 Web 应用。在第 6 章讨论表单登录时，我们其实已经做过这样一个应用，而且那个 Web 应用实际上也用了
CSRF 防护。你有没有注意到，那个应用的登录操作用的是 HTTP POST？那为什么我们当时并没有专门处理 CSRF
呢？原因在于，我们当时并没有开发任何会修改数据的操作。

对于默认的表单登录，Spring Security 会自动为我们正确地应用 CSRF 保护。框架会负责将 CSRF
令牌添加到登录请求中。接下来，我们将开发一个类似的应用，以更深入地了解 CSRF 保护的工作原理。正如图 9.5 所示，本节内容包括：

- 构建一个带有登录表单的 Web 应用示例
- 了解默认登录实现如何使用 CSRF 令牌
- 在主页面实现一次 HTTP POST 调用

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211641123.png){ loading=lazy }
  <figcaption>图9.5 计划。本节中，我们将首先构建并分析一个简单的应用，以了解Spring Security如何实现CSRF防护，随后我们会编写自己的POST请求。</figcaption>
</figure>

在这个示例应用中，你会发现，只有正确使用 CSRF 令牌，HTTP POST 请求才能正常工作。在这里，你将学习如何在网页表单中应用 CSRF
令牌。要实现这个应用，我们首先需要创建一个新的 Spring Boot 项目。你可以在项目 ssia-ch9-ex2 中找到这个示例。下面的代码片段展示了所需的依赖项：

``` xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

接下来，我们当然需要配置表单登录，并至少添加一个用户。下面的代码展示了配置类，其中定义了 UserDetailsService，添加了一个用户，并配置了
formLogin 方法。

```java title="代码清单9.4 配置类的定义"
public class ProjectConfig {

	@Bean
	public UserDetailsService uds() {
		var uds = new InMemoryUserDetailsManager();

		var u1 = User.withUsername("mary")
				.password("12345")
				.authorities("READ")
				.build();

		uds.createUser(u1);

		return uds;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.formLogin(
				c -> c.defaultSuccessUrl("/main", true)
		);


		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}
}

```

我们在名为 controllers 的包中添加一个用于主页面的控制器类，并在 Maven 项目的 resources/templates 文件夹下创建一个
main.html 文件。main.html 文件暂时可以保持为空，因为在首次运行应用程序时，我们主要关注登录页面如何使用 CSRF
令牌。下面的代码展示了用于主页面的 MainController 类。

``` java title="代码清单9.5 MainController 类的定义"
@Controller
public class MainController {

 @GetMapping("/main")
 public String main() {
   return "main.html";
 }
}
```

运行应用程序后，您可以访问默认的登录页面。如果使用浏览器的元素检查功能查看该表单，您会发现默认实现的登录表单会发送 CSRF
令牌。这也是为什么即使登录请求采用 HTTP POST 方法，启用 CSRF 防护后依然能够正常登录的原因！如图 9.6 所示，登录表单通过隐藏的输入字段传递
CSRF 令牌。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211654769.png){ loading=lazy }
  <figcaption>图9.6 默认的表单登录会通过隐藏输入项将 CSRF 令牌发送到请求中。这也是为什么使用 HTTP POST 方法的登录请求在启用 CSRF 防护时能够正常工作的原因。</figcaption>
</figure>

那么，如果我们要开发自己的端点，并使用 POST、PUT 或 DELETE 这些 HTTP 方法呢？对于这些情况，如果启用了 CSRF 防护，我们就必须注意发送
CSRF 令牌的值。为了测试这一点，我们可以在应用中添加一个使用 HTTP POST 的端点。我们会从主页调用这个端点，并为此新建一个控制器，命名为
ProductController。在这个控制器里，我们定义了一个使用 HTTP POST 的端点 /product/add。接下来，我们会在主页上使用一个表单来调用这个端点。下面的代码展示了
ProductController 类的定义。

```java title="代码清单9.6 ProductController类的定义"

@Controller
@RequestMapping("/product")
public class ProductController {

	private Logger logger =
			Logger.getLogger(ProductController.class.getName());

	@PostMapping("/add")
	public String add(@RequestParam String name) {
		logger.info("Adding product " + name);
		return "main.html";
	}
}
```

该端点接收一个请求参数，并将在应用程序控制台中打印出来。下面的代码展示了在 main.html 文件中定义的表单。

```html title="清单9.7 main.html页面中的表单定义"

<form action="/product/add" method="post">
    <span>Name:</span>
    <span><input type="text" name="name"/></span>
    <span><button type="submit">Add</button></span>
</form>
```

现在你可以重新运行应用程序并测试表单了。你会发现，在提交请求时会显示一个默认的错误页面，说明服务器响应返回了 HTTP 403
Forbidden 状态（见图 9.7）。造成这个状态的原因是缺少 CSRF 令牌。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211727878.png){ loading=lazy }
  <figcaption>图 9.7 如果未包含 CSRF 令牌，服务器将拒绝所有使用 HTTP POST 方法发起的请求。用户会被重定向到一个标准错误页面，页面响应状态为 HTTP 403 Forbidden（禁止访问）。</figcaption>
</figure>

为了解决这个问题，并让服务器允许请求，我们需要在通过表单发起的请求中添加 CSRF
令牌。一个简单的方法是像默认表单登录那样，使用一个隐藏的输入组件。具体实现方式可以参考下面的代码示例。

```html title="代码清单9.8 通过表单请求添加CSRF令牌"

<form action="/product/add" method="post">
    <span>Name:</span>
    <span><input type="text" name="name"/></span>
    <span><button type="submit">Add</button></span>

    <input type="hidden"
           th:name="${_csrf.parameterName}"
           th:value="${_csrf.token}"/>
</form>

```

!!! note

    在这个示例中，我们使用了Thymeleaf，因为它能够以一种非常直接的方式在视图中获取请求属性的值。在我们的场景下，我们需要输出CSRF令牌。请注意，CsrfFilter 会将令牌的值添加到请求的 _csrf 属性中。当然，这并不是必须用Thymeleaf来实现，你也可以选择任何其他方式将令牌值输出到响应中。

重新运行应用程序后，你可以再次测试该表单。这一次，服务器成功接收了请求，应用程序也在控制台打印了日志，证明执行已经成功。此外，如果你检查一下表单，还能发现其中有一个隐藏的输入项，其值就是
CSRF 令牌（见图 9.8）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211730750.png){ loading=lazy }
  <figcaption>图9.8 现在主页面上定义的表单会在请求中发送 CSRF 令牌的值。这样，服务器就会允许该请求并执行对应的控制器操作。在页面的源代码中，你可以看到表单用于在请求中传递 CSRF 令牌的隐藏输入项。</figcaption>
</figure>

提交表单后，您应该会在应用程序控制台中看到类似如下的一行信息：

```shell
INFO 20892 --- [nio-8080-exec-7] c.l.s.controllers.ProductController    : Adding product Chocolate
```

当然，对于页面中任何调用变更操作的行为或异步 JavaScript 请求，你都需要发送有效的 CSRF
令牌。这是应用程序最常用的方法，用来确保请求不是来自第三方。第三方请求可能会试图冒充用户，代表用户执行某些操作。

CSRF令牌在前后端由同一台服务器负责的架构中效果很好，主要是因为实现简单。但当客户端与后端服务相互独立时，CSRF令牌就不太适用了。这种情况通常出现在以移动应用作为客户端，或者前端网页独立开发的场景中。如今，使用Angular、ReactJS或Vue.js等框架开发的Web客户端在Web应用架构中非常常见，因此你也需要了解在这些情况下如何实现安全防护。我们将在本书的第四部分详细讨论这类架构设计。

在第13到第16章中，你将学习如何实现OAuth 2规范。该规范在组件解耦方面具有显著优势，使得应用授权客户端时能够将认证与资源分离。

!!! note

	这看起来可能是个微不足道的错误，但根据我的经验，我在很多应用中都见过太多次——绝不要用 HTTP GET 方法来执行会修改数据的操作！不要实现任何通过 HTTP GET 接口就能更改数据的行为。请记住，调用 HTTP GET 接口时并不需要 CSRF 令牌。

## 自定义CSRF防护

在本节中，你将学习如何自定义 Spring Security 提供的 CSRF 防护方案。由于应用的需求各不相同，任何由框架提供的实现都必须具备足够的灵活性，才能轻松适配各种场景。Spring
Security 中的 CSRF 防护机制也不例外。本节的示例将带你实践在自定义 CSRF 防护机制时最常见的一些需求。这些需求包括：

- 配置应用 CSRF 的路径
- 管理 CSRF 令牌

我们只在这样一种情况下使用 CSRF 保护：页面本身是由服务器生成的，并且该页面会消费同一服务器提供的资源。  
这种页面可以是一个 Web 应用，而它所调用的端点由不同的来源暴露出来，就像我们在第 9.2 节中讨论的那样；也可以是一个移动应用。

对于移动应用这种场景，你可以使用 OAuth 2 授权流程，我们会在第 13 至第 16 章中进行讨论。

默认情况下，CSRF 保护会应用到所有使用除 GET、HEAD、TRACE 或 OPTIONS 之外的 HTTP 方法调用的端点路径上。你已经在第 5
章中学过如何完全关闭 CSRF 保护。但如果你只想对应用中的某些路径关闭 CSRF 保护，该怎么办呢？你可以像第 6 章中为表单登录方式自定义
HTTP Basic 那样，通过一个 Customizer 对象快速完成这项配置。

在这里，我们创建了一个新项目，只添加了 Web 和 Security 相关的依赖，如下面的代码片段所示。你可以在项目 ssia-ch9-ex3
中找到这个示例。依赖如下：

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

在这个应用中，我们添加了两个通过 HTTP POST 调用的端点，但希望其中一个不使用 CSRF 保护（见图 9.9）。代码清单 9.9
定义了对应的控制器类，我将其命名为 HelloController。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211746410.png){ loading=lazy }
  <figcaption>图 9.9 应用程序在通过 HTTP POST 调用 /hello 端点时需要提供 CSRF 令牌，但允许对 /ciao 端点发起不带 CSRF 令牌的 HTTP POST 请求。</figcaption>
</figure>

```java title="清单 9.9 HelloController 类的定义"

@RestController
public class HelloController {

	@PostMapping("/hello")
	public String postHello() {
		return "Post Hello!";
	}

	@PostMapping("/ciao")
	public String postCiao() {
		return "Post Ciao";
	}
}

```

若要自定义 CSRF 防护配置，可以在 `securityFilterChain()` 方法中，通过 `HttpSecurity` 对象的 `csrf()` 方法配合 `Customizer`
对象进行设置。下面的示例代码展示了这种用法。

``` java title="清单 9.10 用于配置 CSRF 防护的 Customizer 对象"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.csrf(c -> {
			c.ignoringRequestMatchers("/ciao");
		});

		http.authorizeHttpRequests(
				c -> c.anyRequest().permitAll()
		);

		return http.build();
	}

```

调用 `ignoringRequestMatchers(String paths)` 方法时，你可以指定路径表达式，用来表示你希望从 CSRF 保护机制中排除的路径。更通用的一种做法是使用
`RequestMatcher`。这样一来，你既可以使用常规的路径表达式来定义排除规则，也可以使用正则表达式（regex）。在使用 `CsrfCustomizer`
对象的 `ignoringRequestMatchers()` 方法时，你可以传入任意实现了 `RequestMatcher` 的参数。下面的代码片段展示了如何使用
`MvcRequestMatcher` 来调用 `ignoringRequestMatchers()` 方法，而不是使用以 `String` 形式提供路径的
`ignoringRequestMatchers()`：

``` java
HandlerMappingIntrospector i = new HandlerMappingIntrospector();
MvcRequestMatcher r = new MvcRequestMatcher(i, "/ciao");
c.ignoringRequestMatchers(r);
```

或者你也可以类似地使用正则匹配器：

``` java
String pattern = ".*[0-9].*";
String httpMethod = HttpMethod.POST.name();
RegexRequestMatcher r = new RegexRequestMatcher(pattern, httpMethod);
c.ignoringRequestMatchers(r);
```

在应用需求中，经常还会遇到另一个诉求：自定义 CSRF 令牌的管理方式。正如你已经了解的，默认情况下，应用会将 CSRF 令牌存储在服务器端的
HTTP 会话（Session）中。这种简单的方式适用于小型应用，但对于需要处理大量请求、并且必须进行水平扩展的应用来说，就不太理想了。HTTP
会话是有状态的，会削弱应用的可扩展性。

假设你现在想修改应用处理令牌的方式，不再把它们放在 HTTP Session 里，而是存到某个数据库中。为此，Spring Security
提供了三个需要你实现的契约接口：

- **CsrfToken**——描述 CSRF 令牌本身
- **CsrfTokenRepository**——描述用于创建、存储和加载 CSRF 令牌的对象
- **CsrfTokenRequestHandler**——描述用于管理如何将生成好的 CSRF 令牌设置到 HTTP 请求上的对象

在实现 CsrfToken 这个契约时，你需要指定该对象的三个主要特性（代码清单 9.11 定义了 CsrfToken 契约）：

- 请求中用于承载 CSRF 令牌值的请求头名称（默认是 `X-CSRF-TOKEN`）
- 请求中用于保存令牌值的属性名称（默认是 `_csrf`）
- 令牌本身的值

```java title="清单 9.11 CsrfToken 接口的定义"
public interface CsrfToken extends Serializable {

	String getHeaderName();

	String getParameterName();

	String getToken();
}
```

通常情况下，你只需要一个 `CsrfToken` 类型的实例，用来在其属性中保存这三个关键信息。为此，Spring Security 提供了一个名为
`DefaultCsrfToken` 的实现，我们在示例中也会使用它。`DefaultCsrfToken` 实现了 `CsrfToken`
接口约定，并创建包含所需值的不可变实例：请求属性名、请求头名称，以及实际的令牌值。

`CsrfTokenRepository` 接口则是用于管理 CSRF 令牌组件的契约。若要改变应用管理令牌的方式，你需要实现 `CsrfTokenRepository`
接口，从而可以将自定义实现插入到框架中使用。接下来，我们将修改本节中使用的应用，为 `CsrfTokenRepository` 新增一个实现，将令牌存储到数据库中。图
9.10 展示了本例中我们实现的各个组件以及它们之间的关联关系。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511211759877.png){ loading=lazy }
  <figcaption>图 9.10 CsrfToken 使用了自定义的 CsrfTokenRepository 实现。该自定义实现通过 JpaRepository 在数据库中管理 CSRF 令牌。</figcaption>
</figure>

在这个示例中，我们使用数据库中的一张表来存储 CSRF 令牌。我们假定客户端拥有一个唯一的 ID，用来标识自身。应用程序需要使用这个标识符来获取并校验对应的
CSRF 令牌。

通常，这个唯一 ID 会在用户登录时获取，并且每次登录都应当不同。这种令牌管理策略和把令牌存放在内存中的方式类似，只不过那时你使用的是会话
ID。也就是说，在本例中新的标识符本质上只是替代了会话 ID。

另一种做法是使用带有效期的 CSRF 令牌。在这种方式下，令牌会在你设定的时间后自动过期。你可以将令牌存入数据库，而无需将其与特定用户
ID 关联。你只需要检查 HTTP 请求中携带的令牌是否存在且尚未过期，就能决定是否允许该请求。

!!! note "练习"

	当你完成这个示例（我们使用一个标识符来保存并分配 CSRF 令牌）之后，再实现第二种方式：使用会过期的 CSRF 令牌。

为了简化示例，我们只关注 CsrfTokenRepository 的实现，并且假设客户端已经拥有一个生成好的标识符。为了操作数据库，我们需要在
pom.xml 文件中再添加几个依赖：

``` xml
<dependency>
 <groupId>org.springframework.boot</groupId>
 <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
 <groupId>com.mysql</groupId>
 <artifactId>mysql-connector-j</artifactId>
</dependency>
```

在 `application.properties` 文件中，我们需要添加用于数据库连接的相关配置项：

```properties
spring.datasource.url=jdbc:mysql://localhost/spring
?useLegacyDatetimeCode=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=
spring.sql.init.mode=always
```

为了让应用在启动时自动在数据库中创建所需的数据表，你可以在项目的 resources 文件夹中添加一个 schema.xml 文件。该文件中应包含用于创建数据表的
SQL 语句：

```sql
CREATE TABLE IF NOT EXISTS `spring`.`token`
(
    `id`         INT         NOT NULL AUTO_INCREMENT,
    `identifier` VARCHAR(45) NULL,
    `token`      TEXT        NULL,
    PRIMARY KEY (`id`)
);
```

我们使用带有 JPA 实现的 Spring Data 来连接数据库，因此需要定义实体类和 JpaRepository 接口。在名为 `entities`
的包中，我们按照下面的代码清单定义 JPA 实体。

```java title="清单 9.12 JPA 实体类的定义"

@Entity
public class Token {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String identifier;
	private String token;

	// Omitted code

}

```

我们的 JpaRepository 接口 `JpaTokenRepository` 可以按照下面的代码来定义。你只需要提供一个方法 `findTokenByIdentifier()`
，用于根据特定客户端的标识，从数据库中获取对应的 CSRF 令牌。

```java title="清单 9.13 JpaTokenRepository 接口的定义"
public interface JpaTokenRepository
		extends JpaRepository<Token, Integer> {

	Optional<Token> findTokenByIdentifier(String identifier);
}
```

在已经可以访问实现好的数据库之后，我们现在就可以开始编写 `CsrfTokenRepository` 的实现类了，我将其命名为
`CustomCsrfTokenRepository`。下面的代码清单定义了这个类，并重写了 `CsrfTokenRepository` 的三个方法。

```java title="清单 9.14 CsrfTokenRepository 接口的实现"

@Component
public class CustomCsrfTokenRepository implements CsrfTokenRepository {

	private final JpaTokenRepository jpaTokenRepository;

	// Omitted constructor

	@Override
	public CsrfToken generateToken(
			HttpServletRequest httpServletRequest) {
		// ...
	}

	@Override
	public void saveToken(
			CsrfToken csrfToken,
			HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) {
		// ...
	}

	@Override
	public CsrfToken loadToken(
			HttpServletRequest httpServletRequest) {
		// ...
	}
}
```

CustomCsrfTokenRepository 从 Spring 上下文中注入一个 JpaTokenRepository 实例，以便访问数据库。CustomCsrfTokenRepository
使用这个实例来从数据库中读取或保存 CSRF 令牌。应用需要生成新令牌时，CSRF 保护机制会调用 generateToken() 方法。代码清单 9.15
展示了我们在本示例中对该方法的实现。我们使用 UUID 类生成一个新的随机 UUID 值，并且与 Spring Security
默认实现保持一致，继续使用相同的请求头和请求属性名称：X-CSRF-TOKEN 和 _csrf。

```java title="清单 9.15 generateToken() 方法的实现"

@Override
public CsrfToken generateToken(HttpServletRequest httpServletRequest) {
	String uuid = UUID.randomUUID().toString();
	return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", uuid);
}
```

saveToken() 方法会为特定客户端保存生成的令牌。在默认的 CSRF 保护实现中，应用程序是通过 HTTP 会话来识别 CSRF
令牌的。而在我们的场景中，我们假定客户端本身拥有一个唯一标识符。客户端会在请求中通过名为 X-IDENTIFIER 的请求头发送这个唯一
ID 的值。

在该方法的逻辑中，我们会先检查这个值在数据库中是否存在：如果存在，就用新的令牌值更新数据库；如果不存在，就为这个 ID
新建一条记录，并写入新的 CSRF 令牌值。下面的代码清单展示了 saveToken() 方法的实现。

``` java title="清单 9.16 saveToken() 方法的实现"
@Override
public void saveToken(
   CsrfToken csrfToken, 
   HttpServletRequest httpServletRequest, 
   HttpServletResponse httpServletResponse) {
    String identifier = 
        httpServletRequest.getHeader("X-IDENTIFIER");

    Optional<Token> existingToken =
jpaTokenRepository.findTokenByIdentifier(identifier);

    if (existingToken.isPresent()) {
       Token token = existingToken.get();
       token.setToken(csrfToken.getToken());
    } else {
       Token token = new Token();
       token.setToken(csrfToken.getToken());
       token.setIdentifier(identifier);
       jpaTokenRepository.save(token);
    }
}

```

`loadToken()` 方法的实现会加载令牌的详细信息（如果存在），否则返回 null。下面的代码示例展示了该方法的具体实现。

``` java title="代码清单 9.17：loadToken() 方法的实现"

@Override
public CsrfToken loadToken(
		HttpServletRequest httpServletRequest) {

	String identifier = httpServletRequest.getHeader("X-IDENTIFIER");

	Optional<Token> existingToken =
			jpaTokenRepository
					.findTokenByIdentifier(identifier);

	if (existingToken.isPresent()) {
		Token token = existingToken.get();
		return new DefaultCsrfToken(
				"X-CSRF-TOKEN",
				"_csrf",
				token.getToken());
	}

	return null;
}
```

我们自定义实现了 CsrfTokenRepository，并在配置类中声明为一个 Bean。随后，通过 CsrfConfigurer 的 csrfTokenRepository() 方法将该 Bean 集成到 CSRF 防护机制中。下面的代码展示了这个配置类的定义。

```java title="清单9.18 自定义 CsrfTokenRepository 的配置类"
@Configuration
public class ProjectConfig {

  private final CustomCsrfTokenRepository customTokenRepository;

  // Omitted constructor

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) 
    throws Exception {
    
    http.csrf(c -> {
      c.csrfTokenRepository(customTokenRepository);
    });

    http.authorizeHttpRequests(
      c -> c.anyRequest().permitAll()
    );
      
    return http.build();
  }

}

```

最后，我们还需要补充一个 CsrfTokenRequestHandler，才能让整个流程顺利运行。幸运的是，Spring Security 已经为我们提供了一个实现——CsrfTokenRequestAttributeHandler。这个实现会在通过 HTTP GET 方法调用某个接口时，直接使用 CsrfTokenRepository 的 generateToken() 方法生成一个新的令牌，并将生成的 CsrfToken 作为请求属性添加到请求中。

你可以通过继承 CsrfTokenRequestAttributeHandler 类，来自定义其简单行为。例如，Spring Security 默认使用的实现（名为 XorCsrfTokenRequestAttributeHandler）具有更复杂的处理方式。该实现会使用 SecureRandom 对象生成一个随机值，然后将其字节数组与 CsrfTokenRepository 生成的令牌进行异或操作，从而混合生成最终的令牌。

不过，为了避免让我们的示例变得过于复杂，并让你能够专注于配置部分，我们将设置一个简单的 CsrfTokenRequestAttributeHandler，用于在 HTTP 请求对象上管理 CSRF 令牌。下面的代码展示了如何在配置类中配置 CsrfTokenRequestAttributeHandler。

``` java title="代码清单9.19 自定义 CsrfTokenRepository 的配置类"
@Configuration
public class ProjectConfig {

  private final CustomCsrfTokenRepository customTokenRepository;

  // Omitted constructor

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) 
    throws Exception {
    
    http.csrf(c -> {
      c.csrfTokenRepository(customTokenRepository);    
      c.csrfTokenRequestHandler(     
        new CsrfTokenRequestAttributeHandler()
      );
    });

    http.authorizeHttpRequests(
      c -> c.anyRequest().permitAll()
    );

    return http.build();
  }

}

```

在清单9.9中定义的控制器类中，我们还添加了一个使用HTTP GET方法的端点。我们需要这个方法来在测试实现时获取CSRF令牌：

```java
@GetMapping("/hello")
public String getHello() {
 return "Get Hello!";
}
```

现在，您可以启动应用程序并测试用于管理令牌的新实现。我们通过 HTTP GET 请求调用接口，以获取 CSRF 令牌的值。在调用时，必须在 X-IDENTIFIER 请求头中使用客户端的 ID，这是根据需求设定的。系统会生成一个新的 CSRF 令牌值，并将其存储到数据库中。调用示例如下：

```shell
curl -H "X-IDENTIFIER:12345" http://localhost:8080/hello
Get Hello!
```

如果你在数据库中查找 token 表，会发现应用程序为标识符为 12345 的客户端新增了一条记录。在我的例子中，数据库里生成的 CSRF token 值是 2bc652f5-258b-4a26-b456-928e9bad71f8。我们会用这个值，通过 HTTP POST 方法调用 /hello 接口，就像下面的代码片段所示。当然，我们还需要提供客户端 ID，应用程序会用它从数据库中检索 token，并与我们在请求中提供的值进行比对。

```shell
curl -XPOST -H "X-IDENTIFIER:12345" -H "X-CSRF-TOKEN:2bc652f5-258b-4a26-b456-928e9bad71f8" http://localhost:8080/hello
Post Hello! 
```

图9.11描述了流程。

如果我们尝试使用 POST 方法调用 /hello 端点而不提供所需的请求头,将会收到 HTTP 状态码 403 Forbidden 的响应。要验证这一点,可以使用以下方式调用该端点

```shell
curl -XPOST http://localhost:8080/hello
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

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511220909305.png){ loading=lazy }
  <figcaption>图9.11 首先，GET请求生成CSRF令牌并将其值存储在数据库中。后续的任何POST请求都必须发送这个值。然后CsrfFilter会检查请求中的值是否与数据库中的值相对应。根据检查结果，请求会被接受或拒绝。</figcaption>
</figure>

## 总结 

- CSRF 是一种攻击类型,用户被诱骗访问包含伪造脚本的页面。该脚本可以冒充已登录应用的用户,并代表他们执行操作。 
- Spring Security 默认启用 CSRF 防护。 
- CSRF 防护逻辑在 Spring Security 架构中的入口点是一个 HTTP 过滤器。 
- 你可以自定义 CSRF 防护功能。Spring Security 提供了三个简单的契约接口,你可以通过实现并插入它们来定义自定义的 CSRF 防护能力: 
	- CsrfToken——描述 CSRF 令牌本身 
	- CsrfTokenRepository——描述用于创建、存储和加载 CSRF 令牌的对象 
	- CsrfTokenRequestHandler——描述用于管理生成的 CSRF 令牌如何设置到 HTTP 请求上的对象
