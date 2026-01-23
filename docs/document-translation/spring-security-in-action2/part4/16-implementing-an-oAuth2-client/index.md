# 实现OAuth2客户端

本章内容包括：

- 实现 OAuth 2 登录
- 实现 Spring Security OAuth 2 客户端
- 使用客户端凭证授权类型

在多个后端服务协同的场景中，常常需要实现后台应用之间的通信。若系统基于 OAuth 2
构建了认证和授权机制，建议在应用间的调用时也采用相同方案进行身份验证。虽然开发者有时为保持系统一致性和提升安全性而选择
HTTP Basic 或 API Key（第6章）等更简便的认证方式，但从设计和安全角度来看，使用 OAuth 2 的客户端凭证授权类型是更理想的选择。

还记得 OAuth 2 的各个参与者（图 16.1）吗？我们在第 14 章讨论了授权服务器，在第 15 章讨论了资源服务器。本章将专注于客户端。我们将探讨如何使用
Spring Security 实现 OAuth 2 客户端，以及何时以及如何将后端应用程序转变为 OAuth 2 系统中的客户端。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205092717276.png){ loading=lazy }
  <figcaption>图 16.1 OAuth2 的相关角色。本章我们将讨论客户端，以及后端应用程序如何在采用 OAuth 2 设计的认证与授权系统中充当客户端。</figcaption>
</figure>

好的，也许图 16.1 并不能完全说明我们接下来要讨论的内容。我们会先从用户登录说起，同时也会重点讲述如何让一个后端应用作为另一个后端应用的客户端。使用
Spring Security 设计的后端应用也可以充当客户端。图 16.2 展示了我们在本章中要讨论的另一种情况。本章我们要解决的，就是如何实现两个后端应用之间的通信，使其中一个真正成为
OAuth 2 客户端。在这种情况下，我们需要借助 Spring Security 来构建一个 OAuth 2 客户端。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205092930015.png){ loading=lazy }
  <figcaption>图 16.2 后端应用可能会成为另一个后端应用的客户端。我们在本章中讨论这种情况。</figcaption>
</figure>

第16.1节介绍了如何借助 Spring Security 在 Spring MVC Web 应用中轻松实现 OAuth 2 登录。我们将使用 Google、GitHub
等外部授权服务提供商。你将学会为应用实现登录功能，使用户能够使用 Google 或 GitHub 凭据进行身份验证。采用相同的方法，也可以实现基于自建授权服务器的登录。

在 16.2 节中，我们通过服务使用客户端的自定义实现，并讨论了如何使用客户端凭证授权类型。

## 实现OAuth 2登录

本节讨论如何在 Spring Web 应用中实现 OAuth 2 登录。对于标准场景（即授权服务器正确实现了 OAuth 2 与 OpenID Connect 规范），使用
Spring Boot 配置身份验证非常简单。我们将从一个经典案例入手（适用于大多数知名提供商，如 Google、GitHub、Facebook 和 Okta）。

然后我会向你展示自定义配置背后的原理，帮助你覆盖各种定制化场景。在本节末尾，你将能够为你的 Spring web 应用实现任何 OAuth 2
提供者的登录，甚至允许用户在多个提供者之间自由选择身份验证方式。

### 使用通用提供商实现身份验证

在本节中，我们将实现最简单的登录场景，仅允许应用用户通过一个提供方登录。此次演示中我选择了 Google 作为用户身份验证提供方。

我们首先在项目中添加一些资源，以实现具备上述登录能力的简单 Spring Web 应用。清单 16.1 展示了演示应用所需的依赖项。你可以在
ssia-ch16-ex1 项目中找到这个示例。你会发现其中引入了一个我们在前几章尚未使用的新依赖：`OAuth 2 client dependency`。

``` xml
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

如果你需要快速复习一下如何使用 Spring Boot 构建 Web 应用，我另一本书《Spring Start Here》（Manning，2020）的第 7 章和第 8
章可以帮助你快速回忆这些技能。下面的代码片段展示了我们这个示例 Web 应用中最基础的控制器，它目前只包含一个主页：

```java

@Controller
public class HomeController {

	@GetMapping("/")
	public String home() {
		return "index.html";
	}
}
```

下面的代码片段展示了身份验证成功完成后我们预计访问的简易演示 HTML 页面：

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<h1>Home</h1>
</body>
</html>
```

清单 16.2 展示了将 OAuth 2 登录配置为 Web 应用认证方式的设置。这样配置后，应用会自动采用授权码模式，引导用户跳转到指定的授权服务器登录，并在认证成功后再重定向回来。这一流程完全符合我们在第
13 至第 15 章的讲解，并在这些章节中通过 cURL 多次进行了演示。

```java title="列表 16.2 配置 OAuth 2 登录"

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2Login(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated());

		return http.build();
	}
}

```

我猜你在想，是不是还得像第13到15章里学的那样填写授权 URL、令牌 URL、客户端 ID、客户端密钥等等？没错，这些信息仍然必需。幸运的是，Spring
Security 又能帮上忙了。如果你的应用使用的是 Spring Security 认为是知名的提供商，大多数这些信息会被预先填好。你只需配置应用的客户端凭证即可。Spring
Security 目前认为以下提供商是知名的：

- Google
- GitHub
- Okta
- Facebook

Spring Security 在 CommonOAuth2Provider 类中预先配置了这些提供者的详细信息。所以如果你使用其中任何一个，只需在应用配置中配置客户端凭证即可，便能正常运行。下面的代码片段展示了使用
Google 时需要配置的两个属性，用于设置客户端 ID 和客户端密钥（我省略了凭证的具体值）：

```properties
spring.security.oauth2.client.registration.google.client-id=790…
spring.security.oauth2.client.registration.google.client-secret=GOC…
```

我在此所指的是你已经在 Google 开发者控制台中注册了你的应用——也就是说你从那里获取应用的唯一凭证。如果你还没做过这一步，而且打算为你的应用配置
Google 身份验证，可以参考 Google 的详细文档，了解如何在 Google 上注册你的 OAuth 2
应用，网址是 [http://mng.bz/eEvz](https://developers.google.com/identity/protocols/oauth2?hl=zh-cn)。图 16.3
展示了在正确配置了这个知名提供商时，应用是如何显示 Google 登录界面的。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205112305722.png){ loading=lazy }
  <figcaption>图 16.3 在 Web 浏览器中访问应用时，浏览器会将你重定向到 Google 登录页面。如果你成功通过 Google 验证，就会被重定向回应用的主页面。</figcaption>
</figure>

### 为用户提供更多可能性

我想你现在上网也够多了，应该都发现很多应用都提供了不止一种登录方式。有时候你甚至可以从四五个平台中任选其一登录。这种做法的好处在于，并不是所有人都同时拥有某个社交网络的账户。有些人有
Facebook 账号，但也有人更喜欢用 LinkedIn。有些开发者倾向于用 GitHub 登录，而另一些则会选择 Gmail。

使用 Spring Security，你可以轻松实现这个功能，甚至可以同时使用多个提供商。比如我希望让应用的用户可以通过 Google 或 GitHub
登录。只需为两个提供商以相同方式配置凭据即可。下面的代码片段展示了在 application.properties 文件中添加 GitHub
作为认证方式所需的属性。请记住，必须保留我们在 16.1.1 节中为 Google 已配置的内容：

```properties
spring.security.oauth2.client.registration.github.client-id=03…
spring.security.oauth2.client.registration.github.client-secret=c5d…
```

与其他任何服务提供商一样，您也需要先注册应用，并在 application.properties 文件中配置客户端 ID 和密钥。不同提供商的注册方式有所不同。GitHub
的注册说明可以在 [http://mng.bz/p1YG](https://docs.github.com/zh/apps/creating-github-apps/about-creating-github-apps/about-creating-github-apps)
找到。

在要求您进行身份验证之前，应用会先提供两种登录选项：我们之前配置的那两个（图 16.4）。您必须选择 Google 或 GitHub
中的一个来登录。选定首选提供商后，应用会将您重定向到该提供商的专属身份验证页面。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205131656184.png){ loading=lazy }
  <figcaption>图 16.4 该应用在认证时允许用户在 GitHub 和 Google 之间进行选择。</figcaption>
</figure>

### 使用自定义授权服务器

Spring Security 定义了四个常用的认证提供者，如 16.1.1 节和 16.1.2 节所述。但如果你想使用一个不在常用提供者列表中的认证提供者怎么办？你还有很多其他选择，比如
LinkedIn、Twitter、Yahoo 等。你也可以使用在第 14 章中学习过的自定义授权服务器。

你可以使用任意提供商（包括你自己搭建的）来配置 OAuth 2 登录。本节将利用第 14 章中搭建的授权服务器，展示如何配置自定义的
OAuth 2 登录。为了方便学习并保持示例独立，我已将第 14 章中讨论的 ssia-ch14-ex1 项目内容复制到本章的一个新项目中，命名为
ssia-ch16-ex1-as。

我们只需确保客户端配置与本章要实现的内容一致。清单 16.2 显示了在授权服务器中注册的客户端，最关键的是确保重定向 URI
与我们希望为其实现登录功能的应用所期望的地址相匹配：

```shell
 http://localhost:8080/login/oauth2/code/my_authorization_server
```

图 16.5 分析了重定向 URI 的结构。可以看到，标准的重定向 URI 使用 /login/oauth2/code 路径，后面紧跟授权服务器的名称。在本例中，我为授权服务器命名为
my_authorization_server。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205134608264.png){ loading=lazy }
  <figcaption>图 16.5 标准的重定向 URI 格式。其路径的最后一部分是提供者的名称。</figcaption>
</figure>

下面的代码片段展示了授权服务器的配置部分，用于注册客户端的详细信息。稍后本节我们还会用到这些信息，并在应用端进行相应配置。

``` java title="清单 16.3 授权服务器端注册的客户端详情"
@Bean
public RegisteredClientRepository registeredClientRepository() {
 var registeredClient = RegisteredClient
   .withId(UUID.randomUUID().toString())
   .clientId("client")
   .clientSecret("secret")
   .clientAuthenticationMethod(
      ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
   .authorizationGrantType(
      AuthorizationGrantType.AUTHORIZATION_CODE)
   .redirectUri(
     "http://localhost:8080/login/oauth2/code/my_authorization_server")
   .scope(OidcScopes.OPENID)
   .build();

   return new InMemoryRegisteredClientRepository(registeredClient);
 }
```

请记住，同一台系统上不能在相同端口号下启动两个应用。由于该 Web 应用使用了 8080 端口，所以我们必须将授权服务器的端口改为其他值。如下方代码片段所示，本示例中我选择使用
7070，并在 application.properties 文件中进行了相关配置：

```properties
server.port=7070
```

现在我们可以继续配置 Web 应用程序了。由于在 16.1.1 和 16.1.2 节的示例中使用了一个通用的提供者，我们无需额外定义它。Spring
Security 已经掌握了通用提供者所需的所有细节。但如果要使用不同的提供者，就需要配置几项内容。Spring Security 需要知道以下内容（如第
13 章和第 14 章所述）：

- 授权端点，以便在授权码流程中知道把用户重定向到哪里
- 应用程序必须调用以获取访问令牌的令牌端点
- 应用程序需要调用以验证访问令牌的密钥集端点

好消息是，如果你的提供商（授权服务器）正确实现了 OpenID Connect 协议，你只需配置发行者 URI。应用会使用该 URI
自动获取所需的所有细节，例如授权端点、令牌端点以及密钥集合的 URI。如果授权服务器未能遵循 OpenID Connect 协议，就必须在
application.properties 文件中明确配置这三项细节。

由于第14章中构建的授权服务器已正确实现 OpenID Connect 协议，因此我们可以依赖发行者 URI。下面的代码片段展示了如何配置发行者
URI。注意我为该提供者起了一个名字。在本例中，我选择用 my_authorization_server 来标识它，不过你也可以用任意名称来标识你的提供者：

```properties
spring.security.oauth2.client.provider.my_authorization_server.issuer-uri=http://127.0.0.1:7070
```

!!! note

	我们在本地系统上同时运行授权服务器和我们使用的 Web 应用。将这两个应用运行在同一系统并通过浏览器访问，可能会因为浏览器用于存储用户会话的 Cookie 而产生问题。因此，我建议用 IP 地址“127.0.0.1”指代其中一个应用，用 DNS 名称“localhost”指代另一个。即使从网络角度来看这两个地址是相同的，都指向同一台本地系统，但浏览器会将它们视为不同的来源，从而能够正确地管理会话。在这个示例中，我用“127.0.0.1”指代授权服务器，用“localhost”指代 Web 应用。

清单 16.4 展示了客户端注册配置。除了指明提供者外，该客户端注册比我们在第 16.1.1 节和第 16.1.2 节编写、使用通用提供者时要长一些。除了客户端
ID 与客户端密钥之外，还需要填写以下内容：

- 提供者名称——为需要使用的非通用提供者指定一个名称。
- 客户端认证方式——应用调用提供者受保护端点时使用的认证方式（通常是 HTTP Basic）。
- 重定向 URI——完成正确认证后，应用期望提供者重定向用户的 URI。此 URI 必须与授权服务器端注册的某个 URI（见清单 16.3）相匹配。
- Web 应用请求的作用域——Web 应用请求的作用域只能是授权服务器端注册的那些之一（见清单 16.3）。

``` properties title="图16.4 客户端注册配置"
spring.security.oauth2.client.
registration.my_authorization_server
.client-id=client
spring.security.oauth2.client.
registration.my_authorization_server.
client-name=Custom
Spring.security.oauth2.client.
registration.my_authorization_server.
client-secret=secret
spring.security.oauth2.client.
registration.my_authorization_server.
provider=my_authorization_server
spring.security.oauth2.client.
registration.my_authorization_server.
client-authentication-method=client_secret_basic
spring.security.oauth2.client.
registration.my_authorization_server.redirect-uri=
http://localhost:8080/login/oauth2/code/my_authorization_server
spring.security.oauth2.client.
registration.my_authorization_server.scope[0]=openid
```

你可以启动授权服务器和 Web 应用。记住，必须先启动授权服务器。当 Web 应用启动时，它会调用 issue URI
以获取所需的其余详细信息。启动两个应用后，在浏览器中通过地址 `http://localhost:8080` 访问 Web
应用。图16.6显示，自定义提供者现在出现在列表中，用户可以选择它进行身份验证。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205135737927.png){ loading=lazy }
  <figcaption>图 16.6 自定义授权服务器现在出现在用户可从中选择以进行身份验证的提供商列表中。</figcaption>
</figure>

### 为你的配置增加灵活性

通常情况下，我们需要比 properties
文件提供的功能更灵活。有时，我们需要能够在不重新部署应用的情况下动态修改凭据；在其他情况下，我们希望根据特定条件开启或关闭某些提供者，甚至根据逻辑控制对这些提供者的访问。对于这种需求，仅仅把凭据写在
properties 文件里，让 Spring Boot 自动处理，就不再适用了。

不过，如果你了解背后的运行机制，就可以根据需要自定义提供者的各项细节。你只需记住两种类型：

- **ClientRegistration**——该对象用来定义客户端访问授权服务器所需的各项信息（凭证、重定向 URI、授权 URI 等）。
- **ClientRegistrationRepository**——这个接口用于定义获取客户端注册信息的逻辑。例如，你可以实现一个客户端注册仓库，让应用从数据库或自定义的凭据库中获取这些注册信息。

在这个示例中，我保持设置简单。仍然使用 application.properties 文件，不过属性名换了，用以说明现在已经不再是由 Spring Boot
替我们配置的。不过，即便很直观，这个示例展示的正是如果你打算将配置信息存储在数据库或通过某个接口获取时所采用的方式。不论是哪种情况，都必须妥善实现
ClientRegistrationRepository 接口。

您将 ClientRegistrationRepository 组件定义为 Spring Bean，应用会使用您的实现来获取客户端注册信息。清单 16.5
展示了一个使用内存实现的示例。在这个例子中，我完成了三项工作：

1. 从属性文件中注入凭证值；
2. 使用所有所需信息创建一个 ClientRegistration 对象；
3. 在内存中的 ClientRegistrationRepository 实现中进行配置。

您可以在 ssia-ch16-ex2 项目中找到此示例。

```java title="清单 16.5 实现自定义逻辑"

@Configuration
public class SecurityConfig {

	@Value("${client-id}")
	private String clientId;

	@Value("${client-secret}")
	private String clientSecret;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2Login(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		return new InMemoryClientRegistrationRepository(
				this.googleClientRegistration());
	}

	private ClientRegistration googleClientRegistration() {
		return CommonOAuth2Provider.GOOGLE.getBuilder("google")
				.clientId(clientId)
				.clientSecret(clientSecret)
				.build();
	}

}

```

### 管理OAuth2登录的授权

在本节中，我们讨论如何使用认证信息。在大多数情况下，应用需要知道是谁登录了。这种需求通常是为了对界面进行差异化展示，或是施加各种授权限制。值得庆幸的是，在这方面，使用
oauth2Login() 认证方法与其他任何认证方式并无差异。

还记得我们从第2章开始讨论的 Spring Security 身份验证设计（如图16.7所示）吗？成功验证的最后一步总是将认证信息添加到安全上下文中，使用
oauth2Login() 也不例外。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205141216101.png){ loading=lazy }
  <figcaption>图 16.7 Spring Security 中的身份验证流程。完成身份验证后，应用会将已验证主体的详细信息添加到安全上下文中。</figcaption>
</figure>

知道认证详情保存在安全上下文后，就可以像之前讨论的任何其他认证方式（httpBasic()、formLogin() 或 oauth2ResourceServer()
）那样使用它们：

- 可以将 Authentication 对象作为方法参数注入（见图 16.5）。
- 可以在应用的任意位置从安全上下文中获取它（SecurityContextHolder.getContext().getAuthentication()）。
- 可以使用第 11 章和第 12 章中讨论的前置/后置注解。

你可以使用 Authentication 接口获取标准的用户信息，比如用户名和权限。如果需要获取自定义信息，可以参考 16.6
节中的示例直接使用接口的实现类。对于 OAuth 2 来说，OAuth2AuthenticationPrincipal 类就是该接口的实现。不过需要记住，为了便于维护，建议尽可能都通过
Authentication 接口来获取所需信息，只有在别无选择（比如需要获取接口引用无法提供的某个细节）时才直接依赖具体实现。

``` java title="清单16.6 获取认证详细信息"
@Controller
public class HomeController {

  @GetMapping("/")
  public String home(
   [CA]OAuth2AuthenticationToken authentication) {
    // do something with the authentication
    return "index.html";
  }
}

```

## 实现OAuth2客户端

本节讨论如何将服务实现为 OAuth 2 客户端。在面向服务的系统中，应用之间经常会相互通信。在这种情况下，发起请求的应用就会成为目标应用的客户端。大多数时候，如果我们决定基于
OAuth 2 为这些请求实现认证，该应用会使用客户端凭证授权（client credentials grant）类型来获取访问令牌。

客户端凭证授权类型不涉及用户身份。因此，无需重定向 URI 或授权 URI。只需要凭证，客户端就可通过向令牌 URI 发送请求来完成认证并获取访问令牌。图
16.8 回顾了我们在第 13 章讨论的客户端凭证授权类型。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205144308499.png){ loading=lazy }
  <figcaption>图16.8 客户端凭证授权类型。客户端使用客户端凭证向令牌端点发起请求进行身份验证。身份验证成功后，客户端会获得一个访问令牌，用于访问资源服务器端的资源。</figcaption>
</figure>

让我们用一个简单的示例向你展示如何使用 Spring Security 实现 OAuth 2
客户端功能。我们将构建一个应用，使用客户端凭证授权类型从授权服务器获取访问令牌。为了简化示例，我们仅讨论如何获取访问令牌，这对演示如何构建请求已经足够。只要你知道如何获取访问令牌，就可以用任何技术发送
HTTP 请求，因为任意技术都可以轻松添加请求头（记住要在 Authorization 请求头中添加以 “Bearer” 为前缀的访问令牌值）。

在这个示例中，我们要做的是配置一个应用，使用客户端凭证授权类型从 OAuth 2 授权服务器获取访问令牌。为了验证我们确实成功拿到访问令牌，我们会在一个示例接口的响应体中返回它。图
16.9 展示了我们要构建的流程，图中的步骤如下：

1. 用户（你）通过 cURL（或 Postman 等工具）调用我们命名为 /token 的示例接口；
2. 模拟应用的工具（cURL）把请求发送到我们为这个示例构建的应用；
3. 应用使用客户端凭证授权类型从授权服务器获取访问令牌；
4. 应用在 HTTP 响应体中把访问令牌的值返回给客户端；
5. 用户（你）在 HTTP 响应体中看到访问令牌的值。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205144523157.png){ loading=lazy }
  <figcaption>图 16.9 我们的演示构建了一个应用，能够使用客户端凭证授权类型从授权服务器获取访问令牌。为了验证该应用正确获取到了访问令牌，应用在响应演示端点调用时返回该令牌值。我们将这个演示端点命名为 /token。</figcaption>
</figure>

我们将沿用第14章中构建的授权服务器，本章对应的项目是
ssia-ch16-ex1-as。请记得先在授权服务器中添加一个客户端注册，支持使用客户端凭证授权类型。你可以修改第14章中已经配置过的那个（如下节所示），也可以新增一个满足该要求的客户端注册。

```java title="清单 16.7 授权服务器端注册的客户端详细信息"

@Bean
public RegisteredClientRepository registeredClientRepository() {
	var registeredClient = RegisteredClient
			.withId(UUID.randomUUID().toString())
			.clientId("client")
			.clientSecret("secret")
			.clientAuthenticationMethod(
					ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.authorizationGrantType(
					AuthorizationGrantType.CLIENT_CREDENTIALS)
			.scope(OidcScopes.OPENID)
			.build();

	return new InMemoryRegisteredClientRepository(registeredClient);
}

```

与其他认证方式类似，Spring Security 也提供了 HttpSecurity 对象的一个方法来将应用配置为 OAuth 2 客户端。在下面示例中调用
oauth2Client() 方法即可将应用配置为 OAuth 2 客户端。

```java title="列表 16.8 配置 OAuth 2 客户端认证"

@Configuration
public class ProjectConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2Client(Customizer.withDefaults());

		http.authorizeHttpRequests(
				c -> c.anyRequest().permitAll()
		);

		return http.build();
	}
}

```

应用还需要知道一些细节，以便向授权服务器发送访问令牌请求。正如你在第16.1节中了解到的，我们通过 ClientRegistrationRepository
组件提供这些细节。你可能会觉得清单16.9中的代码很熟悉，因为它与我们在清单16.4中编写的代码相似。

不过因为我使用的是非通用提供方，所以还得指定更多细节，比如作用域、令牌 URI 以及认证方式。注意我将客户端凭证配置为授权类型。

```java title="列表 16.9 为客户端应用配置客户端注册详细信息"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
		ClientRegistration c1 =
				ClientRegistration.withRegistrationId("1")
						.clientId("client")
						.clientSecret("secret")
						.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
						.clientAuthenticationMethod(
								ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
						.tokenUri("http://localhost:7070/oauth2/token")
						.scope(OidcScopes.OPENID)
						.build();

		var repository =
				new InMemoryClientRegistrationRepository(c1);

		return repository;
	}
}
```

客户端管理器组件负责发起获取访问令牌的必要请求。图16.10展示了控制器与客户端管理器之间的关系（以我们示例为例）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205144937209.png){ loading=lazy }
  <figcaption>图16.10 控制器使用客户端管理器从授权服务器获取访问令牌。客户端管理器是负责连接授权服务器并正确使用授权类型以获取访问令牌的 Spring Security 组件。</figcaption>
</figure>

类 `OAuth2AuthorizedClientManager` 定义了一个客户端管理器。下面的代码示例将客户端管理器配置为应用上下文中的一个 Bean。

```java title="清单16.10 实现 OAuth 2 客户端管理器"

@Configuration
public class ProjectConfig {

	// Omitted code

	@Bean
	public OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(
			ClientRegistrationRepository clientRegistrationRepository,
			OAuth2AuthorizedClientRepository auth2AuthorizedClientRepository
	) {

		var provider =
				OAuth2AuthorizedClientProviderBuilder.builder()
						.clientCredentials()
						.build();

		var cm = new DefaultOAuth2AuthorizedClientManager(
				clientRegistrationRepository,
				auth2AuthorizedClientRepository);

		cm.setAuthorizedClientProvider(provider);

		return cm;
	}
}

```

你现在可以在需要获取访问令牌的任何地方使用客户端管理器。如图 16.10 所示，我让控制器直接使用客户端管理器，以简化这个示例，并让你专注于如何实现
OAuth 2 客户端的讨论。请记住，现实中的应用可能会更复杂。在一个正确划分对象职责的设计中，客户端管理器很可能会由代理对象使用，而不是由控制器直接调用（见图
16.11）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251205145407543.png){ loading=lazy }
  <figcaption>图16.11 现实中的应用会有更好的职责划分。与我们的示例不同，代理层借助客户端管理器获取令牌后，会使用该令牌向系统中的其他应用发送请求。</figcaption>
</figure>

下面的示例展示了如何注入客户端管理器实例，并演示了如何通过某个端点获取访问令牌。当调用应用所暴露的 /token
端点时，响应体中应包含访问令牌的值。

``` java title="清单 16.11 使用 OAuth 2 客户端管理器获取令牌"
@RestController
public class DemoController {

  private final OAuth2AuthorizedClientManager clientManager;

  // Omitted constructor

  @GetMapping("/token")
  public String token() {
    OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest
        .withClientRegistrationId("1")
        .principal("client")
        .build();

    var client = 
       clientManager.authorize(request);

    return client
      .getAccessToken().getTokenValue();
  }
}

```

使用以下 cURL 命令调用应用所暴露的端点：

```shell
curl http://localhost:8080/token
```

响应体应包含一个访问令牌的值，例如

```shell
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6Im
JpbGwiLCJpYXQiOjE1MTYyMzkwMjJ9.zjL2JXw0TVgNgTMUKmP0-PTPklULUVmV_5re50eZoHw
```

## 总结

- 在实现 Spring Web 应用时，我们通常需要配置认证功能。虽然可以通过 formLogin() 方法快速实现登录表单，但也可以让用户使用另一个系统的注册账号进行认证。
- 允许用户选择不同的系统登录，对用户和应用都有好处。用户无需记住额外的凭据，应用也不需要为所有用户管理凭据。
- Spring Security 认为 GitHub、Google、Facebook 和 Okta 是常用的提供方。对于这些常用提供方，Spring Security 已经掌握了在
  OAuth 2 框架下发起请求所需的所有细节，因此只需配置提供方所提供的客户端凭据即可完成登录功能的配置。
- 你也可以配置应用使用除常用提供方之外的其他提供方，但需要显式配置应用发起授予类型流程获取访问令牌所需的所有细节。需要配置的主要内容是三个
  URI：授权 URI、令牌 URI 和密钥集 URI。
- 一旦用户登录你的应用，即使是通过外部系统认证，应用还是会获取用户信息，并将其存入安全上下文中。这个过程遵循标准的 Spring
  Security 认证设计。因此，你可以像配置其他认证方式一样配置授权。
- 有时，后端服务会成为另一个后端应用的客户端。在这种情况下，想调用另一个应用并采用 OAuth 2
  方式的应用，需要获取访问令牌以便被其认证。服务可以使用客户端凭据授权类型来获取访问令牌。
- Spring Security 提供了一个名为客户端管理器（client
  manager）的对象。该对象实现了执行某种授权类型并获取访问令牌的逻辑。需要通过访问令牌对请求进行认证，并向另一应用发送请求的应用代理层会使用客户端管理器来获取访问令牌。
