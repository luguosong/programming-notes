# 15.实现OAuth2资源服务器

本章内容包括：

- 实现 Spring Security OAuth 2 资源服务器
- 使用携带自定义声明的 JWT 令牌
- 为不透明令牌配置自省或吊销机制
- 实现更复杂的应用场景和多租户配置

本章探讨如何在 OAuth 2 体系中保护后端应用。按照 OAuth 2 的术语，资源服务器其实就是一个后端服务。第 14 章我们学习了如何用
Spring Security 实现授权服务器的职责，现在该讨论如何使用授权服务器生成的令牌了。

在实际场景中，你可能会也可能不会像第 14 章那样自建授权服务器；很多团队更愿意采用第三方实现。可选方案很多，从 Keycloak
这样的开源项目到 Okta、Cognito、Azure AD 等企业级产品一应俱全——本书第一版的第 18 章就提供了 Keycloak 的示例。

即便你可以通过配置现成的授权服务器而无需自行开发，你仍必须在后端正确落实认证与授权。因此，我认为本章至关重要；掌握这里的内容，很大概率能对你的工作产生实质帮助。图
15.1 回顾了 OAuth 2 的参与者，也标示出我们在本书这一部分的学习进度。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125134025857.png){ loading=lazy }
  <figcaption>图15.1 在 OAuth 2 中，应用的后端被称为资源服务器，因为它保护用户和客户端的资源（即数据以及可在数据上执行的操作）。</figcaption>
</figure>

我们将在第15.1节开始本章，讨论 JSON Web Token（JWT）的资源服务器配置。如今 JWT 最常与 OAuth 2
体系结合使用，这正是我们从它们入手的原因。在第15.2节中，我们将介绍如何自定义 JWT，并在主体或头部声明中使用自定义值。

在第 15.3 节中，我们探讨如何配置资源服务器使用自省来验证令牌。当使用不透明令牌，或希望系统能在令牌过期前将其吊销时，自省流程就显得尤为实用。

我们将在第15.4节讨论多租户等更高级的配置案例，作为本章的收尾。

## JWT 验证配置

在本节中，我们讨论如何配置资源服务器以验证并使用 JWT。这是一种非透明的令牌（令牌本身包含资源服务器用于授权的数据）。要使用
JWT，资源服务器需要证明它们是可信的——也就是说，预期的授权服务器确实签发了这些令牌，以此作为对用户和/或客户端通过身份验证的证明。其次，资源服务器还需要读取令牌中的数据，并据此实现授权规则。

我们将通过实际操作来学习如何配置资源服务器，也就是从零开始实现并配置它。首先创建一个新的 Spring Boot
项目并添加所需依赖，然后实现一个用于测试的演示端点（资源），接着配置认证和授权。我们将按照以下步骤进行：

1. 在项目中添加所需依赖（我们使用 Maven，因此在 pom.xml 中配置）。
2. 声明一个用于测试实现的占位端点。
3. 通过配置服务的公钥集 URI，完成 JWT 认证。
4. 实现授权规则。
5. 验证实现：  
   a. 使用授权服务器生成令牌；  
   b. 使用该令牌调用第2步创建的占位端点。

下方列出了所需的依赖项。除了 Web 和 Spring Security 相关依赖外，我们还需要添加资源服务器的起步依赖。

``` xml title="清单15.1 实现资源服务器所需的依赖"
<dependency>
   <groupId>org.springframework.boot</groupId>  
   <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>     
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

一旦依赖就位，我们就创建一个用于最终测试实现的临时端点。下面的代码展示了一个简单的控制器，它在路径 /demo 上提供一个端点。

```java title="清单15.2 声明一个用于测试的简单端点"

@RestController
public class DemoController {

	@GetMapping("/demo")
	public String demo() {
		return "Demo";
	}
}

```

在这个示例中，你需要使用一个授权服务器。可以使用我们在第14章、项目 ssia-ch14-ex1 中创建的那个。

由于我们希望在同一系统上同时启动授权服务器和资源服务器，就需要为它们配置不同的端口。授权服务器默认使用
8080，因此可以把资源服务器的端口改成其他值。我把它设为 9090，你也可以选择系统上任意空闲的端口。下面的代码片段展示了在
application.properties 文件中添加的属性，用于修改端口：

```properties
server.port=9090
```

启动项目 ssia-ch14-ex1 中的授权服务器以及当前应用程序。你可以在书附带的项目中找到这个示例，具体位于项目 ssia-ch15-ex1。

请记住在第 14 章中提到，OpenID Connect 授权服务器会提供一个 URL，用于获取其配置（包括授权、令牌、公钥集等 URL）。下面的示例展示了所谓的
well-known URL：

```shell
http://localhost:8080/.well-known/openid-configuration
```

你需要这个链接来获取授权服务器公开的用于提供公钥集合的地址，以便资源服务器可以用它来验证令牌。资源服务器需要调用该端点并获取这组公钥，然后再利用其中一个公钥来验证访问令牌的签名（见图
15.2）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125135025633.png){ loading=lazy }
  <figcaption>图 15.2 资源服务器通过授权服务器提供的端点获取一组公钥，然后使用这些密钥验证访问令牌的签名。</figcaption>
</figure>

清单 15.3 提醒你调用授权服务器提供的知名配置端点时会得到的响应。如你所见，返回的数据中包含公钥集合
URI。这正是我们需要在资源服务器中配置的内容，以便它能够验证 JWT。

``` json title="列表 15.3：包含密钥集 URI 的知名 OpenID 配置响应"
{
    "issuer": "http://localhost:8080",
    "authorization_endpoint": "http://localhost:8080/oauth2/authorize",
    "device_authorization_endpoint":
 "http://localhost:8080/oauth2/device_authorization",
    "token_endpoint": "http://localhost:8080/oauth2/token",

    …

    "jwks_uri": "http://localhost:8080/oauth2/jwks",
    …
    
}
```

要配置公共密钥集的 URI，我们首先在项目的 application.properties 文件中声明它。然后，配置类可以将其注入到一个属性字段中，并用来配置资源服务器的认证。

```properties
keySetURI=http://localhost:8080/oauth2/jwks
```

列表 15.4 展示了配置类如何将公钥集 URI 值注入到一个属性中。该配置类还定义了一个 SecurityFilterChain 类型的 Bean，应用程序会使用这个
SecurityFilterChain Bean 来配置认证，与我们在本书前面章节中所做的方式类似。

```java title="列表 15.4 在配置类中注入属性值"

@Configuration
public class ProjectConfig {

	@Value("${keySetURI}")
	private String keySetUri;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		return http.build();
	}
}

```

为了配置认证，我们会使用 HttpSecurity 对象的 oauth2ResourceServer() 方法。这个方法与本书第二、第三部分中我们用过的
httpBasic() 和 formLogin() 类似。

类似于 `httpBasic()` 和 `formLogin()`，你需要提供一个 `Customizer` 接口的实现来配置认证。在示例 15.5 中，你可以看到我使用
`Customizer` 对象的 `jwt()` 方法来设置 JWT 认证。随后，我又在该 `jwt()` 方法上应用了一个 `Customizer`，通过 `jwkSetUri()`
方法配置公钥集 URI。

```java title="清单 15.5 使用 JWT 配置认证"

@Configuration
public class ProjectConfig {

	@Value("${keySetURI}")
	private String keySetUri;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2ResourceServer(
				c -> c.jwt(
						j -> j.jwkSetUri(keySetUri)
				)
		);

		return http.build();
	}
}

```

请记得将这些端点设置为需要身份验证。默认情况下，端点并未受保护，因此要测试认证流程，首先需要确保 /demo
端点要求身份验证。下面的代码片段配置了应用的授权规则。在这个示例中，我们可以将所有端点都设为必须通过身份验证。

``` java
http.authorizeHttpRequests(
   c -> c.anyRequest().authenticated()
);
```

下列清单展示了该配置类的完整内容。

```java title="清单 15.6：完整的配置类"

@Configuration
public class ProjectConfig {

	@Value("${keySetURI}")
	private String keySetUri;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2ResourceServer(
				c -> c.jwt(
						j -> j.jwkSetUri(keySetUri)
				)
		);

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}
}
```

现在该启动刚刚创建的资源服务器应用了。请确保授权服务器依然在运行。接下来需要利用第 14
章学到的技能来生成访问令牌。我们再回顾一下授权码授权流程的步骤（不过请记住，你也可以使用任何其他授权模式获取令牌——对资源服务器而言，只要你持有访问令牌，获取方式并不重要）。

使用授权码授权方式时，需要遵循以下步骤（见图 15.3）：

1. 将用户重定向至授权服务器的 /authorize 端点，提示其登录。
2. 使用用户的凭据完成认证，授权服务器会将你重定向至事先配置的重定向 URI，并携带授权码。
3. 获取重定向后返回的授权码，调用 /token 端点请求新的访问令牌。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125140332947.png){ loading=lazy }
  <figcaption>图 15.3 授权码授权模式：客户端将用户重定向到授权服务器的登录页面。用户成功完成身份验证后，授权服务器携带授权码重定向回客户端。客户端再利用该授权码获取访问令牌。</figcaption>
</figure>

紧接在要点之后的下一个代码片段展示了你可以在浏览器中使用的 URL，用于跳转到授权服务器的 /authorize
端点。请记住，你需要提供一些参数，并且它们的值必须与授权服务器中的配置保持一致。需要发送的参数包括：

- **response_type**——如果要使用授权码授权类型，值设为 `"code"`。
- **client_id**——客户端 ID。
- **scope**——你希望访问的范围，可以是授权服务器中配置的任意 scope。
- **redirect_uri**——授权服务器在用户成功认证后重定向客户端的 URI。该 URI 必须是授权服务器中已配置的地址之一。
- **code_challenge**——如果使用 PKCE（Proof Key for Code Exchange），需要提供由 code challenge 与 code verifier 组成的 code
  challenge。
- **code_challenge_method**——如果使用 PKCE，必须指定用于加密 code verifier 的哈希算法（例如 SHA-256）。

```shell
http://localhost:8080/oauth2/authorize?response_type=code&client_id=client&scope=openid&redirect_uri=https://www.manning.com/authorized&code_challenge=QYPAZ5NU8yvtlQ9erXrUYR-T5AGCjCF47vN-KsaI2A8&code_challenge_method=S256
```

!!! note

	请记得将授权 URL 粘贴到浏览器地址栏中，以便发出请求。

使用在授权服务器上配置的有效用户凭据登录，然后等待跳转到所请求的重定向 URI。授权服务器会提供授权码，你必须在发送至 /token
端点的请求中使用该授权码。

下一个代码片段展示了一个 cURL 命令示例，它向 /token 端点发送请求以获取访问令牌。请注意，为了适应页面，我已经截断了授权码的值。

```shell
curl -X POST 'http://localhost:8080/oauth2/token? \
client_id=client& \
redirect_uri=https://www.manning.com/authorized& \
grant_type=authorization_code& \
code=IhKRpq7GJ7P5VQI_...& \
code_verifier=qPsH306-ZDDaOE8DFzVn05TkN3ZZoVmI_6x4LsVglQI' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA=='
```

以下代码片段展示了 /token 请求的响应体。在该片段中，我对令牌值进行了截断：

```json
{
  "access_token": "eyJraWQiOiI2Zjk5ZmE3MC…",
  "scope": "openid",
  "id_token": "eyJraWQiOiI2Zjk5ZmE3MC0xNTQ2LTRkMjM…",
  "token_type": "Bearer",
  "expires_in": 299
}
```

现在，你可以在调用任何需要身份验证的端点时使用这个访问令牌。下面的示例代码展示了如何用 cURL 向 /demo 端点发送请求。注意，访问令牌必须以
Bearer 前缀的形式放在 Authorization 请求头中（见图 15.4）。Bearer 前缀的含义在于：谁拿到这个访问令牌，就能像其他持有者一样直接使用它。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125140648035.png){ loading=lazy }
  <figcaption>图 15.4 JR.R. 托尔金的小说《指环王》类比。访问令牌是一种珍贵的资源，握在谁手中，谁便能获取多个资源。</figcaption>
</figure>

以下代码片段示范了如何使用授权服务器发放的访问令牌，借助 cURL 命令向 /demo 端点发起请求。

```shell
curl 'http://localhost:9090/demo' \
--header 'Authorization: Bearer eyJraW…'
```

## 使用自定义JWT

不同系统的需求各不相同，即便是在身份验证和授权方面也是如此。很多时候，你需要通过访问令牌在授权服务器和资源服务器之间传递自定义的值，资源服务器便可以基于这些值来执行不同的授权规则。

在本节中，我们将实现一个示例，让授权服务器和资源服务器在访问令牌中使用自定义声明。授权服务器会定制 JWT，在其中添加一个名为 "
priority" 的声明；资源服务器随后读取该声明，并将其值注入SecurityContext中的认证实例。这样一来，资源服务器在实现任何授权规则时，都可以直接利用这一信息。

我们将按以下步骤进行：

1. 修改授权服务器，在访问令牌中加入自定义声明。
2. 修改资源服务器，读取自定义声明并将其存入SecurityContext。
3. 实现利用该自定义声明的授权规则。

但首先要做的是：我们需要在访问令牌的主体中添加一个自定义值，并将其放入 SecurityConfig 类。在授权服务器中，你可以通过添加一个类型为
OAuth2TokenCustomizer 的 Bean 来实现。下面的代码片段展示了这个 Bean 的定义。为便于说明并让你专注示例，我在一个名为
“priority” 的字段里添加了一个虚拟值。在真实场景中，这类自定义字段通常有具体用途，你可能还需要编写相应逻辑来设置它们的值。

```java

@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
	return context -> {
		JwtClaimsSet.Builder claims = context.getClaims();
		claims.claim("priority", "HIGH");
	};
}
```

通过这一微小改动，访问令牌现在包含自定义的 “priority” 字段。下一段代码展示了我生成的一个 JWT 访问令牌的 Base64 编码格式，而清单
15.7 则显示了解码后的主体，你可以在其中看到这个 “priority” 字段。

``` text
eyJraWQiOiI5ZTBjOTQ5Ny0zYmMyLTQ4Y2YtODU5MC04N2JmZjE2ZjczOTAiLCJhbGciOiJSUzI
1NiJ9.eyJzdWIiOiJiaWxsIiwiYXVkIjoiY2xpZW50IiwibmJmIjoxNjg3MjYzMzI5LCJzY29wZ
SI6WyJvcGVuaWQiXSwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiZXhwIjoxNjg3MjYz
NjI5LCJwcmlvcml0eSI6IkhJR0giLCJpYXQiOjE2ODcyNjMzMjl9.HrQECSO17tZD8HKXP0U7gm
dmea01vPgVypvcf3oR3uawiMdI_joQBsLY0zNWBIgktKn2w9-
rvgtjD2xmhWZgSxRsDW_GZofqOzV9T-
5llMuZlakF7SQLyI67UJZKuPTJK8hBd1OhnurGo7ikPfDWhaqyychKu_uI7SdFrQQVgVqbrmHii
syoURIrI9EwOhB036M7UPJnIWtOWc34fAoFHxqhPuGIVesHHX5qm6wx-
8_Orjz96eOujVSEuUGRNVtz35_SRjhozcLzgIo3Rt9lUfLI7HSzulfXTCpxtxja-
1E_l_dsk4VHSvLYJUZjlERp5kVJqSO_keaJt8JbDQ0new
```

清单 15.7 展示了先前介绍的访问令牌的解码主体。请记住，你可以轻松使用 jwt.io 在线工具获取 JWT 的解码形式。或者，你也可以使用任何其他
Base64 解码器分别对访问令牌的头部或主体进行 Base64 解码。接下来的清单将展示我们在授权服务器上的修改是否正常运行。

```json title="列表 15.7 自定义 JWT 访问令牌的 Base64 解码主体"
{
  "sub": "bill",
  "aud": "client",
  "nbf": 1687263329,
  "scope": [
    "openid"
  ],
  "iss": "http://localhost:8080",
  "exp": 1687263629,
  "priority": "HIGH",
  "iat": 1687263329
}

```

作为第二步，我们在资源服务器上进行修改。你可以继续使用第 15.1 节中的示例，但为了便于学习，我专门为这个示例创建了一个独立项目。你可以在项目
ssia-ch15-ex2 中找到本节讨论的实现。

我们需要遵循的步骤如下，才能让资源服务器识别访问令牌中的自定义声明：

1. 创建一个自定义认证对象，用于定义包含自定义数据的新结构。
2. 创建一个 JWT 认证转换器，用于描述如何将 JWT 转换为该自定义认证对象。
3. 将第 2 步创建的 JWT 认证转换器配置到认证机制中使用。
4. 修改 /demo 端点，使其返回SecurityContext中的认证对象。
5. 测试该端点，确认认证对象包含自定义的 “priority” 字段。

清单 15.8 展示了认证对象的定义。认证对象应当是直接或间接继承自 AbstractAuthenticationToken 的任意类。由于我们使用
JWT，更合适的是扩展更具体的 JwtAuthenticationToken，这样你就能直接沿用为 JWT 访问令牌设计的认证对象的基本形态。

请注意，代码清单 15.8 的自定义内容添加了一个名为 “priority”
的字段，用来承载访问令牌主体中的自定义声明值。用同样的方式，你可以添加应用在授权时所需的任何其他自定义信息。把这些信息直接放在SecurityContext的认证对象中，无论在端点层（第
7、8 章）还是方法层（第 11、12 章）进行配置，写起来都更轻松。

```java title="清单15.8：定义自定义认证对象"
public class CustomAuthentication
		extends JwtAuthenticationToken {

	private final String priority;

	public CustomAuthentication(
			Jwt jwt,
			Collection<? extends GrantedAuthority> authorities,
			String priority) {

		super(jwt, authorities);
		this.priority = priority;
	}

	public String getPriority() {
		return priority;
	}
}

```

你已经定义了一个自定义的认证对象，接下来的任务是让应用知道如何把 JWT 转换成这个自定义对象。你可以像清单 15.9 所示那样配置一个特定的 Converter。注意我们使用了两个泛型类型：Jwt 和 CustomAuthentication。第一个泛型类型 Jwt 是转换器的输入，第二个类型 CustomAuthentication 是输出。因此，这个转换器会把 Jwt 对象（即 Spring Security 中读取 JWT 访问令牌的标准约定）转换成我们在清单 15.8 中实现的自定义类型（参见图 15.5）。


