# 10.配置CORS

本章内容包括

- 定义 CORS
- 应用 CORS 配置

在本章中,我们将讨论跨域资源共享(CORS)以及如何在 Spring Security 中应用它。首先,什么是 CORS,为什么你需要关注它?CORS 的需求源于
Web 应用程序。默认情况下,浏览器不允许向加载网站的域以外的任何域发起请求。例如,如果你从 `example.com` 访问网站,浏览器将不允许该网站向
`api.example.com` 发起请求。图 10.1 展示了这个概念。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511221633530.png){ loading=lazy }
  <figcaption>图 10.1 跨域资源共享(CORS)。当从 example.com 访问时,该网站无法向 api.example.com 发起请求,因为这些请求属于跨域请求。</figcaption>
</figure>

我们可以简单地说，应用程序使用 CORS
机制来放宽这种严格的策略，并在某些条件下允许不同源之间的请求。你需要了解这一点，因为你很可能需要在应用程序中使用它，尤其是在当今前后端分离的应用架构中。通常情况下，前端应用使用
Angular、ReactJS 或 Vue 等框架开发，并托管在诸如 example.com 这样的域名上，但它需要调用托管在另一个域名（如
api.example.com）上的后端接口。

本章提供了一些示例，帮助你学习如何在 Web 应用中应用 CORS 策略，并且展示了如何避免在应用中留下安全漏洞。

## CORS如何工作？

本节讨论了 CORS 在 Web 应用中的应用。如果你是 example.com 的所有者，而 example.org 的开发者出于某种原因决定从他们的网站调用你的
REST 接口（api.example.com），他们将无法成功调用。同样的情况也可能出现在其他域通过 iframe 加载你的应用时（参见图 10.2）。

!!! note

	iframe 是一种 HTML 元素，用于在一个网页中嵌入由另一个网页生成的内容（例如，在 example.com 的页面中集成来自 example.org 的内容）。

任何情况下，应用在两个不同域之间进行调用都是被禁止的。当然，有时你确实需要进行这样的跨域调用，这时 CORS
就会允许你指定哪些域可以发起请求，以及可以共享哪些具体信息。CORS 机制是基于 HTTP 头部工作的（见图 10.3）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511222257768.png){ loading=lazy }
  <figcaption>图 10.2 即便 example.org 页面是通过 example.com 域加载到 iframe 中，从 example.org 内容发起的调用也无法加载。此外，即便应用发起了请求，浏览器也不会接受其响应。</figcaption>
</figure>

最重要的是：

- Access-Control-Allow-Origin——指定哪些外来域（源）可以访问你域上的资源。
- Access-Control-Allow-Methods——在想要允许其他域访问时，仅针对某些 HTTP 方法开放。例如，当你只允许 example.com 通过 HTTP
  GET 调用某个接口时会用到它。
- Access-Control-Allow-Headers——限定请求中可以使用哪些头部。例如，某些请求你并不希望客户端带上特定的头部。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511222259126.png){ loading=lazy }
  <figcaption>图 10.3 启用跨域请求。example.org 服务器添加 Access-Control-Allow-Origin 头，指定浏览器应该接受其响应的请求来源。如果调用发生的域名在允许的来源列表中，浏览器就会接受该响应。</figcaption>
</figure>

在 Spring Security 中，默认情况下这些响应头都不会被加入响应中。那么我们从最基本的情况开始：如果你在应用中没有配置
CORS，发起跨域请求时会发生什么？当应用发送请求时，它期望响应中包含一个 Access-Control-Allow-Origin 头，里面是服务器允许的来源。如果没有这个头（就像
Spring Security 默认行为那样），浏览器就不会接受该响应。我们用一个小型 Web 应用来演示这个情况。我们创建一个新项目，使用下一个代码片段中列出的依赖（你可以在项目
ssia-ch10-ex1 中找到这个例子）：

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
 <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

我们定义了一个控制器类，其中包含主页面的一个动作以及一个 REST 端点。由于该类是一个普通的 Spring MVC @Controller
类，因此我们还必须显式地为该端点添加 @ResponseBody 注解。下面的示例展示了该控制器的定义。

``` java title="第10.1节 控制器类的定义"

@Controller
public class MainController {

	private Logger logger =
			Logger.getLogger(MainController.class.getName());

	@GetMapping("/")
	public String main() {
		return "main.html";
	}

	@PostMapping("/test")
	@ResponseBody
	public String test() {
		logger.info("Test method called");
		return "HELLO";
	}
}

```

此外，我们还需要定义一个配置类，在其中禁用 CSRF 保护，以便简化示例，让你可以专注于 CORS
机制。同时，我们允许所有端点的非认证访问。下一段代码展示了该配置类的定义。

```java title="清单10.2 配置类的定义"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.csrf(
				c -> c.disable()
		);

		http.authorizeHttpRequests(
				c -> c.anyRequest().permitAll()
		);


		return http.build();
	}
}
```

当然，我们还需要在项目的 resources/templates 文件夹中定义 main.html 文件。main.html 文件包含调用 /test 端点的 JavaScript
代码。为了模拟跨域调用，我们可以在浏览器中使用域名 localhost 访问页面。而在 JavaScript 代码中，我们通过 IP 地址 127.0.0.1
发起调用。即便 localhost 和 127.0.0.1 都指向同一台主机，但浏览器将它们视为不同的字符串，从而认为它们属于不同的域。下一个列表定义了
main.html 页面。

``` html title="清单 10.3 main.html 页面"
<!DOCTYPE HTML>
<html lang=»en»>
<head>
    <script>
        const http = new XMLHttpRequest();
        const url = 'http://127.0.0.1:8080/test';
        http.open("POST", url);
        http.send();

        http.onreadystatechange = (e) => {
            document
                    .getElementById("output")
                    .innerHTML = http.responseText;
        }
    </script>
</head>
<body>
<div id="output"></div>
</body>
</html>

```

启动应用并在浏览器中访问 localhost:8080 后，会发现页面没有任何内容。我们本来期望看到页面上显示 HELLO，因为 /test
接口返回的就是这个。不过，当我们查看浏览器控制台时，看到的是 JavaScript 调用抛出的错误，错误信息如下：

```shell
Access to XMLHttpRequest at 'http://127.0.0.1:8080/test' from origin 'http://localhost:8080' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

这一点非常重要！我遇到很多开发者把 CORS 误解为类似于授权或 CSRF 保护那样的限制。实际上，CORS 的作用是为跨域调用放宽原本的严格约束，而不是增加限制。即便在
CORS 加了限制的情况下，有些场景下仍然可以调用接口。不过这种行为并非始终发生。有时浏览器会先发起一条使用 HTTP OPTIONS
方法的调用，以测试该请求是否被允许。我们把这类测试请求称为预检请求（preflight request）。如果预检请求失败，浏览器就不会继续发起原始请求。

预检请求及是否发起该请求由浏览器负责，你无需自行实现这部分逻辑。但了解它很重要，这样即便你没有为特定域名指定任何 CORS
策略，也不会因为看到跨域调用后台而感到意外。使用 Angular 或 ReactJS 等框架开发的客户端应用也可能出现这种情况。图 10.4
展示了该请求的流程。浏览器在省略预检请求时，如果 HTTP 方法是 GET、POST 或 OPTIONS
且仅包含一些基本头信息（具体可见官方文档 [https://fetch.spec.whatwg.org/#http-cors-protocol](https://fetch.spec.whatwg.org/#http-cors-protocol)
的描述），那么就不会发送预检请求。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/202511231601001.png){ loading=lazy }
  <figcaption>图 10.4 对于简单请求，浏览器会直接将原始请求发送到服务器。如果服务器不允许该来源，浏览器会拒绝响应。在某些情况下，浏览器会先发送一个预检请求，测试服务器是否接受该来源。如果预检请求通过，浏览器就会发送原始请求。</figcaption>
</figure>

在我们的示例中，浏览器会发起请求，但如果响应中未指明 origin（如图10.1和10.2所示），我们就不会接受该响应。CORS
机制最终是与浏览器相关的，并不是一种用于保护接口的安全手段。它唯一能保证的是，只有你允许的源域名才可以从浏览器中的特定页面发起请求。

## 使用@CrossOrigin注解应用 CORS 策略

本节介绍如何使用 @CrossOrigin 注解配置跨域资源共享（CORS），以允许来自不同域的请求。你可以将 @CrossOrigin
注解直接放在定义接口的方法上，并通过指定允许的源和请求方法来进行配置。正如本节所述，使用 @CrossOrigin 注解的优势在于可以轻松地为每个接口单独设置
CORS。

我们在 10.1 节创建的应用中演示了 @CrossOrigin 的用法。要让该应用支持跨域调用，唯一需要做的就是在控制器类中的 test() 方法上添加
@CrossOrigin 注解。下列示例展示了如何通过该注解将本地主机设置为允许的来源。

```java title="清单 10.4 使 localhost 成为允许的源"

@PostMapping("/test")
@ResponseBody
@CrossOrigin("http://localhost:8080")
public String test() {
	logger.info("Test method called");
	return "HELLO";
}

```

你可以重新运行并测试该应用。此时页面上应该会显示由 /test 端点返回的字符串：HELLO。

`@CrossOrigin` 的 `value` 参数接受一个数组，允许你定义多个来源，例如 `@CrossOrigin({"example.com", "example.org"})`
。你还可以通过注解的 `allowedHeaders` 属性和 `methods` 属性来设置允许的请求头和方法。对于来源和请求头都可以使用星号（*
）表示全部来源或全部请求头。但我建议你慎用这种方式，最好明确筛选允许的来源和请求头，避免让任何域名都能访问你应用的资源。

允许所有来源会让应用暴露于跨站脚本（XSS）请求之中，最终可能引发 DDoS
攻击。我个人即便在测试环境中也会避免允许所有来源。我知道有些应用运行在配置不当的基础设施上——测试和生产共用同一数据中心。正如我们在第一章讨论的那样，更明智的做法是将所有适用安全措施的层级独立对待，不要因为基础设施表面上不允许某些漏洞就假设应用本身不存在这些问题。

在端点定义处使用 @CrossOrigin
直接指定规则的优点是，可以清楚地看到这些规则。但缺点是可能会显得啰嗦，需要重复很多代码。此外，新开发的端点可能会忘记添加该注解，从而带来风险。在第10.3节中，我们会讨论如何在配置类中集中管理
CORS 配置。

## 使用CorsConfigurer应用CORS

虽然使用 @CrossOrigin 注解很方便，但如你在 10.2 节中学到的，在很多情况下我们可能更希望在一个地方统一定义 CORS 配置。在本节中，我们将第
10.1 和 10.2 节中所用的示例做出修改，改为在配置类中通过 Customizer 来应用 CORS 配置。下面的代码片段展示了我们在配置类中需要做的更改，以便定义我们希望允许的源。

```java title="列表 10.5 在配置类中集中定义 CORS 配置"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.cors(c -> {
			CorsConfigurationSource source = request -> {
				CorsConfiguration config = new CorsConfiguration();
				config.setAllowedOrigins(
						List.of("example.com", "example.org"));
				config.setAllowedMethods(
						List.of("GET", "POST", "PUT", "DELETE"));
				config.setAllowedHeaders(List.of("*"));
				return config;
			};
			c.configurationSource(source);
		});

		http.csrf(
				c -> c.disable()
		);

		http.authorizeHttpRequests(
				c -> c.anyRequest().permitAll()
		);

		return http.build();
	}
}

```

我们从 HttpSecurity 对象调用的 cors() 方法接收一个 Customizer<CorsConfigurer> 类型的参数。针对该参数，我们设置了一个
CorsConfigurationSource，它会为 HTTP 请求返回 CorsConfiguration。CorsConfiguration
用来说明允许的来源、请求方法和请求头。如果采用这种方式，至少需要明确指定来源和方法；若只指定来源，应用就不会放行请求。这是因为
CorsConfiguration 对象默认并未定义任何请求方法。

在这个示例中，为了让说明更直观，我直接使用 SecurityFilterChain Bean 以 lambda 表达式的形式实现了
CorsConfigurationSource。我强烈建议在你的应用中将这段代码放在一个单独的类里。因为在真实项目中，代码往往会更长，如果不通过配置类进行拆分，可能会变得难以阅读。

## 总结

- CORS 指的是当某个特定域名下托管的 Web 应用尝试访问来自其他域名的内容时的情况。
- 默认情况下，浏览器不允许跨域请求发生。因此，通过 CORS 配置可以允许在浏览器中运行的 Web 应用从不同域名调用部分资源。
- 你可以通过在接口上使用 @CrossOrigin 注解，或在配置类中借助 HttpSecurity 对象的 cors() 方法进行集中配置来实现 CORS。
