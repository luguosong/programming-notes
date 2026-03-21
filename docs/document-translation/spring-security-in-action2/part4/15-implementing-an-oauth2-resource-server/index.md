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

在本节中，我们将讨论如何配置资源服务器以验证并使用 JWT（JSON Web 令牌）。JWT 是非不透明令牌，包含资源服务器可用于授权的数据。要使用
JWT，资源服务器需要证明这些令牌是真实有效的，也就是说，预期的授权服务器确实已颁发这些令牌，作为对用户和/或客户端身份验证的凭证。其次，资源服务器还需读取令牌中的数据，并基于这些数据执行授权策略。

我们将通过实践来学习如何配置资源服务器，也就是说，亲自实现一个并从头开始配置。首先创建一个新的 Spring Boot
项目，并添加所需的依赖项。接着实现一个演示端点（用于测试的资源），并着手配置认证与授权。我们将按照以下步骤进行：

1. 在项目中添加所需依赖（由于使用 Maven，具体在 pom.xml 中配置）。
2. 声明一个用于测试的虚拟端点。
3. 通过配置服务的 JWT 公钥 URI，实现对 JWT 的认证。
4. 实现授权规则。
5. 通过以下方式测试实现：
	1. 使用授权服务器生成 token。
	2. 利用该 token 访问第 2 步创建的虚拟端点。

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

下面的代码片段（紧跟在项目符号之后）展示了可以在浏览器中使用的 URL，用于跳转到授权服务器的 /authorize
端点。请记住，需要提供一些参数，其值必须与在授权服务器中配置的内容一致。必须发送的参数包括：

- response_type——如果要使用授权码授权类型，请设置为 "code"。
- client_id——客户端 ID。
- scope——希望访问的作用域。可以是授权服务器中配置的任意作用域。
- redirect_uri——授权服务器在成功认证后重定向客户端的 URI。该 URI 应为授权服务器中配置的 URI 之一。
- code_challenge——如果使用 PKCE（用于代码交换的证明密钥），需要提供代码挑战与验证器对中的代码挑战值。
- code_challenge_method——如果使用 PKCE，必须指定用于对代码验证器加密的哈希函数（例如 SHA-256）：

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

你已经定义了一个自定义的认证对象，接下来的任务是让应用知道如何把 JWT 转换成这个自定义对象。你可以像清单 15.9 所示那样配置一个特定的
Converter。注意我们使用了两个泛型类型：Jwt 和 CustomAuthentication。第一个泛型类型 Jwt 是转换器的输入，第二个类型
CustomAuthentication 是输出。因此，这个转换器会把 Jwt 对象（即 Spring Security 中读取 JWT 访问令牌的标准约定）转换成我们在清单
15.8 中实现的自定义类型（参见图 15.5）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251204145230198.png){ loading=lazy }
  <figcaption>图 15.5 自定义转换器实现逻辑，将访问令牌中的信息填充到自定义身份验证结构中。</figcaption>
</figure>

```java title="清单 15.9 将访问令牌转换为认证对象"

@Component
public class JwtAuthenticationConverter
		implements Converter<Jwt, CustomAuthentication> {

	@Override
	public CustomAuthentication convert(Jwt source) {
		List<GrantedAuthority> authorities =
				List.of(() -> "read");

		String priority =
				String.valueOf(source.getClaims().get("priority"));

		return new CustomAuthentication(source,
				authorities,
				priority);
	}
}

```

您还可以在清单 15.9
中看到，我定义了一个虚拟权限。在实际场景中，这些权限通常来自访问令牌（因为它们在授权服务器层面进行管理），也可能来自数据库或其他第三方系统（因为它们从业务角度进行管理）。在这个示例中，为简化起见，我为所有请求都添加了一个虚拟的“read”权限。但要记住，这也是处理权限的地方（这些权限最终也会出现在
security context 的 authentication 对象中，因为它们在大多数情况下对授权规则至关重要）。

接下来的清单展示了如何配置自定义转换器。在此例中，我通过依赖注入从 Spring 上下文中获取转换器 Bean，然后使用 JWT 认证配置中的
jwtAuthenticationConverter() 方法。

``` java title="清单 15.10 配置自定义认证转换器"

@Configuration
public class ProjectConfig {

	// omitted code

	//在类字段中注入转换器对象
	private final JwtAuthenticationConverter converter;

	// omitted constructor

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2ResourceServer(
				c -> c.jwt(
						j -> j.jwkSetUri(keySetUri)
								.jwtAuthenticationConverter(converter) //在身份验证机制中配置转换器对象
				)
		);

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}
}

```

以上就是我们为了使用访问令牌的自定义声明所需进行的全部配置。现在让我们测试一下实现，验证其是否按预期工作。下一段代码展示了我对
/demo 端点所做的修改：我让 /demo 端点返回来自安全上下文的 Authentication 实例。因为 Spring 知道如何在类型为 Authentication
的参数中自动注入该值，所以我只需添加这个参数，然后让端点的处理方法原样返回即可：

```java

@GetMapping("/demo")
public Authentication demo(Authentication a) {
	return a;
}
```

如果一切按预期运行，当你向 /demo 端点发送请求时，会收到一个响应，响应体与下列示例类似。注意，自定义的 `priority`
属性已经正确地出现在认证对象中，并且值为 `HIGH`。

``` json
{
  "authorities": [
    {
      "authority": "read"
    }
  ],
  "details": {
     "remoteAddress": "0:0:0:0:0:0:0:1",
     "sessionId": null
  },
  "authenticated": true,
    …

  "name": "bill",
  "priority": "HIGH",
 }

```

## 通过内省配置令牌验证

在本节中，我们将讨论使用内省机制进行访问令牌验证。若应用使用不透明令牌，或者希望在授权服务器层面实现令牌撤销，那么内省就是你必须采用的令牌验证方式。图15.6再次展示了内省流程，详见第14.4节。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251204151651204.png){ loading=lazy }
  <figcaption>图 15.6 令牌自省。在资源服务器无法依赖基于签名的访问令牌验证（例如需要撤销令牌的情况），或者令牌本身不包含详细信息（如不透明令牌）时，资源服务器必须向授权服务器发起查询，以确认令牌的有效性并获取更多相关信息。</figcaption>
</figure>

我们将实现一个资源服务器来演示 introspection 的用法。要实现这个目标，需要按以下步骤操作：

1. 确保授权服务器将资源服务器识别为一个客户端。资源服务器需要在授权服务器端注册客户端凭据。
2. 在资源服务器端配置认证，使其使用 introspection。
3. 从授权服务器获取访问令牌。
4. 使用一个示例端点证明我们在步骤 3 中获取的访问令牌能够按预期正常工作。

下面的代码片段展示了创建客户端实例的示例，我们将在授权服务器端注册该客户端。这个客户端代表的是我们的资源服务器。正如图15.6所示，资源服务器会向授权服务器发送请求（用于检查），因此资源服务器也同时成为了授权服务器的一个客户端。

要发送自省请求，资源服务器需要客户端凭证进行身份验证，就像其他任何客户端一样。在这个示例中，我将修改我们在第14章讨论不透明令牌时创建的
ssia-ch14-ex4 项目：

``` java
RegisteredClient resourceServer =   
  RegisteredClient.withId(UUID.randomUUID().toString())
           .clientId("resource_server")
           .clientSecret("resource_server_secret")
           .clientAuthenticationMethod(
              ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
           .authorizationGrantType(
              AuthorizationGrantType.CLIENT_CREDENTIALS)
           .build();
```

请记住，像我在前面的代码片段中那样硬编码密码和配置数据是绝对不应该的。我已经尽可能简化了这些示例，以便让你专注于我们正在讨论的主题。在实际的应用中，应该将配置放在实现之外的文件中，并安全地持久化保存敏感信息（比如凭据）。

下面的清单展示了如何将客户端详情的两个实例（客户端自身和资源服务器）添加到授权服务器的 RegisteredClientRepository 组件中。

```java title="清单 15.12 RegisteredClientRepository 定义"

@Bean
public RegisteredClientRepository registeredClientRepository() {
	RegisteredClient registeredClient =
			RegisteredClient.withId(UUID.randomUUID().toString())
					.clientId("client")
					.clientSecret("secret")
					.clientAuthenticationMethod(
							ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
					.tokenSettings(TokenSettings.builder()
							.accessTokenFormat(OAuth2TokenFormat.REFERENCE)
							.accessTokenTimeToLive(Duration.ofHours(12))
							.build())
					.scope("CUSTOM")
					.build();

	RegisteredClient resourceServer =
			RegisteredClient.withId(UUID.randomUUID().toString())
					.clientId("resource_server")
					.clientSecret("resource_server_secret")
					.clientAuthenticationMethod(
							ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.authorizationGrantType(
							AuthorizationGrantType.CLIENT_CREDENTIALS)
					.build();

	return new InMemoryRegisteredClientRepository(
			registeredClient,
			resourceServer);
}

```

在对清单 15.12 所做的修改之后，我们现在拥有一组资源服务器可以用来调用授权服务器公开的 introspection
端点的凭据。接下来我们可以开始实现资源服务器。该示例位于项目 ssia-ch15-ex3 中。清单 15.13 展示了在属性文件中我是如何配置
introspection 所需的三个关键值的：

- 授权服务器公开的 introspection URI，资源服务器可以通过它来验证令牌
- 资源服务器的客户端 ID，用于在调用 introspection 端点时标识自身
- 资源服务器的客户端密钥，资源服务器会将其与客户端 ID 一起用于向 introspection 端点发送请求时的认证

此外，我还将服务器端口改为 9090，与应用服务器（8080）不同，从而使两个应用能够同时运行。

``` properties
server.port=9090
introspectionUri=http://localhost:8080/
  oauth2/introspect

resourceserver.clientID=resource_server
resourceserver.secret=resource_server_secret
```

然后，您可以将属性文件中的值注入配置类的字段，并使用这些值来设置认证。下面的清单展示了配置类如何将属性文件中的值注入到字段中。

``` java title="清单15.14 将值注入配置类字段"
@Configuration
public class ProjectConfig {

  @Value("${introspectionUri}")
  private String introspectionUri;

  @Value("${resourceserver.clientID}")
  private String resourceServerClientID;

  @Value("${resourceserver.secret}")
  private String resourceServerSecret;
    
}

```

使用 introspection URI 和凭证来配置身份验证。配置方式与我们为 JWT 访问令牌所做的配置类似——同样通过 HttpSecurity 对象的
oauth2ResourceServer() 方法。不过，这里我们调用的是 oauth2ResourceServer() 定制器对象中的另一个配置方法：opaqueToken()。在
opaqueToken() 方法中，配置 introspection URI 和凭证。下面的清单展示了该设置。

``` java title="图 15.15 配置资源服务器对不透明令牌的身份验证"

@Configuration
public class ProjectConfig {

	@Value("${introspectionUri}")
	private String introspectionUri;

	@Value("${resourceserver.clientID}")
	private String resourceServerClientID;

	@Value("${resourceserver.secret}")
	private String resourceServerSecret;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2ResourceServer(
				c -> c.opaqueToken(
						o -> o.introspectionUri(introspectionUri)
								.introspectionClientCredentials(
										resourceServerClientID,
										resourceServerSecret)
				)
		);

		return http.build();
	}
}

```

请记得也要加入授权配置。下面的代码片段展示了你在第 7 章和第 8 章中学到的标准做法，让所有端点都必须进行请求认证：

``` java
http.authorizeHttpRequests(
 c -> c.anyRequest().authenticated()
);
```

下面的代码清单展示了该配置类的完整内容。

``` java title="清单 15.16 配置类的完整内容"

@Configuration
public class ProjectConfig {

	@Value("${introspectionUri}")
	private String introspectionUri;

	@Value("${resourceserver.clientID}")
	private String resourceServerClientID;

	@Value("${resourceserver.secret}")
	private String resourceServerSecret;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http)
			throws Exception {

		http.oauth2ResourceServer(
				c -> c.opaqueToken(
						o -> o.introspectionUri(introspectionUri)
								.introspectionClientCredentials(
										resourceServerClientID,
										resourceServerSecret)
				)
		);

		http.authorizeHttpRequests(
				c -> c.anyRequest().authenticated()
		);

		return http.build();
	}
}

```

一个像下述代码片段中的简单 `/demo` 端点就足以让我们验证认证是否正常工作：

``` java
@RestController
public class DemoController {

 @GetMapping("/demo")
 public String demo() {
   return "Demo";
 }
}
```

现在您可以同时启动两个应用：授权服务器和资源服务器。它们需要同时运行。下方代码片段包含了一个 cURL 命令，可用于向 /token
端点发送请求。为简化示例，我采用了客户端凭证授权类型，但您也可以使用第 14
章所介绍的任意一种授权类型来获取访问令牌。请记住，无论通过何种方式获取访问令牌，资源服务器的配置都是相同的。

``` shell
curl -X POST 'http://localhost:8080/oauth2/token? \
client_id=client& \
grant_type=client_credentials' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA=='
```

如果请求成功，响应中会返回访问令牌。响应体如下所示，我已经截断了令牌的值，以便更好地展示在页面上：

``` json
{
   "access_token": "2zLyYA8b6Q54-…",
   "token_type": "Bearer",
   "expires_in": 43199
}
```

与 JWT 访问令牌的使用方式相同，向受保护的端点发送请求时需要将令牌作为 “Authorization” 头的值，并且访问令牌前必须加上字符串
“Bearer”。下面的示例展示了可用于请求 /demo 端点的 cURL 命令。如果一切正常，你会在 200 OK 的响应状态中收到包含 “Demo”
字符串的响应体。

```shell
curl 'http://localhost:9090/demo' \
--header 'Authorization: Bearer 2zLyYA8b6Q54-…'
```

## 实现多租户系统

在真实的应用中，情况并不总是完美。有时候在与第三方集成时，我们不得不调整实现以适应某些非标准的案例；另外，有时候我们的后端需要依赖多个授权服务器来完成认证和授权（多租户系统）。在这种情况下，我们应该如何配置应用？

幸运的是，Spring Security 提供了灵活性，可以支持各种场景的实现。本节我们将讨论在更复杂的情况下如何配置资源服务器，比如多租户系统或与不遵循标准的应用进行交互。

让我们看看图 15.7，回顾一下本书前两部分详细讨论的 Spring Security 认证设计。一个过滤器会拦截 HTTP
请求，随后将认证责任交给认证管理器。认证管理器进一步调用实现了认证逻辑的认证提供者。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251204162538479.png){ loading=lazy }
  <figcaption>图15.7 认证类设计。在认证过程中，过滤器捕获请求并将其传递给认证管理器组件。该管理器随后调用执行具体认证逻辑的认证提供器。认证成功后，应用会将已认证主体的详细信息记录到安全上下文中。</figcaption>
</figure>

为什么要记住这个设计？因为对于资源服务器来说，就像其他任何身份验证方式一样，如果想自定义身份验证的工作方式，就必须更换身份验证提供器。

在资源服务器的场景中，Spring Security 允许你在配置中插入一个名为“认证管理器解析器”的组件（见图
15.8）。该组件使应用在运行时能够决定调用哪个认证管理器。通过这种方式，你可以将认证流程委托给任意自定义的认证管理器，而该管理器又可以使用自定义的认证提供者。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251204162807051.png){ loading=lazy }
  <figcaption>图 15.8 展示了实现身份验证管理器解析器的方式，您可以借此告诉应用应将身份验证职责委托给哪个身份验证管理器。</figcaption>
</figure>

如果你希望应用程序支持多个使用 JWT 的授权服务器，Spring Security 甚至提供了一个开箱即用的认证管理器解析器实现（图
15.9）。在这种情况下，你只需接入 Spring Security 提供的 `JwtIssuerAuthenticationManagerResolver` 自定义实现即可。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251204170942198.png){ loading=lazy }
  <figcaption>图 15.9 您的系统可能需要使用多个授权服务器来验证用户和客户端。</figcaption>
</figure>

列表15.17展示了如何在配置认证时使用authenticationManagerResolver()
方法。在这个例子中，你会看到我只需创建一个JwtIssuerAuthenticationResolver类的实例，并为其提供了所有授权服务器的签发地址。该示例已在项目
ssia-ch15-ex4 中实现。

!!! note

	请务必记住，不要在代码中直接写入 URL（或任何类似的可配置内容）。我们在示例中这么做，只是为了简化代码，让你能专注于最关键的学习内容。任何可自定义的内容都应该统一放在配置文件或环境变量中。

``` java title="列表 15.17 使用 JWT 访问令牌的两个授权服务器的操作"
@Configuration
public class ProjectConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) 
    throws Exception {
    
    http.oauth2ResourceServer(
      j -> j.authenticationManagerResolver(
               authenticationManagerResolver())
    );

    http.authorizeHttpRequests(
      c -> c.anyRequest().authenticated()
    );
    return http.build();
  }

  @Bean
  public AuthenticationManagerResolver<HttpServletRequest> 
    authenticationManagerResolver() {
    
    var a = new JwtIssuerAuthenticationManagerResolver(
        "http://localhost:7070", 
        "http://localhost:8080");

    return a;
  }
}
```

如图 15.10 所示配置，您的资源服务器可与运行在 7070 号和 8080 号端口的两个授权服务器协同工作。

然而，事情有时会更复杂。Spring Security 不可能覆盖所有的自定义需求。在这种情况下，如果你需要进一步扩展资源服务器的能力，就必须实现自定义的授权管理器解析器。

我们来考虑以下场景：你的资源服务器需要同时处理来自两个不同授权服务器的 JWT 和不透明令牌。假设资源服务器根据 “type”
参数的值来区分请求。如果 “type” 参数的值为 “jwt”，资源服务器就必须使用 JWT 访问令牌与某个授权服务器进行认证；否则，它就会使用带有不透明访问令牌的另外一个授权服务器。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251204171908439.png){ loading=lazy }
  <figcaption>图 15.10：使用两个不同的授权服务器，每个服务器处理不同类型的令牌。资源服务器根据客户端在 HTTP 请求头中携带的特定值，确定应使用哪个授权服务器来验证访问令牌。</figcaption>
</figure>

Listing 15.18 展示了该场景的实现。资源服务器根据 HTTP 请求中 “type” 头的值，选择不同的授权服务器。为实现这一点，资源服务器根据该头的值使用不同的认证管理器。

``` java title="图15.18 同时使用 JWT 和不透明令牌"
@Configuration
public class ProjectConfig {

  // Omitted code

  @Bean
  public AuthenticationManagerResolver<HttpServletRequest> 
    authenticationManagerResolver(
        JwtDecoder jwtDecoder, 
        OpaqueTokenIntrospector opaqueTokenIntrospector
    ) {
        
    AuthenticationManager jwtAuth = new ProviderManager(
      new JwtAuthenticationProvider(jwtDecoder)    
    );

    AuthenticationManager opaqueAuth = new ProviderManager(
      new OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector)
    );

    return (request) -> {
      if ("jwt".equals(request.getHeader("type"))) {
         return jwtAuth;
      } else {
         return opaqueAuth;
      }
    };
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder
            .withJwkSetUri("http://localhost:7070/oauth2/jwks")
            .build();
  }

  @Bean
  public OpaqueTokenIntrospector opaqueTokenIntrospector() {
    return new SpringOpaqueTokenIntrospector(
       "http://localhost:6060/oauth2/introspect",
       "client", "secret");
  }
}
```

以下清单展示了其余配置内容，通过 authenticationManagerResolver() 方法的 customizer 参数配置自定义授权管理器解析器。

``` java title="清单 15.19 配置 AuthenticationManagerResolver"
@Configuration
public class ProjectConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) 
    throws Exception {
    
    http.oauth2ResourceServer(
      j -> j.authenticationManagerResolver(
                authenticationManagerResolver(
                  jwtDecoder(), 
                  opaqueTokenIntrospector()
                ))
    );

    http.authorizeHttpRequests(
      c -> c.anyRequest().authenticated()
    );

    return http.build();
  }

  // Omitted code

}

```

即便在这个示例中，我们也使用了 Spring Security 提供的认证器实现：JwtAuthenticationProvider 和
OpaqueTokenAuthenticationProvider。其中，JwtAuthenticationProvider 实现了使用 JWT 访问令牌连接标准授权服务器的认证逻辑，而
OpaqueTokenAuthenticationProvider 则实现了处理不透明令牌的认证逻辑。但在实际应用中，可能还会遇到更复杂的情形。

如果你需要实现一些高度定制化的功能，比如要与一个完全不遵循任何标准的系统集成，那么你甚至可以自己实现一个定制的认证提供者。

## 摘要

- Spring Security 提供了用于实现 OAuth 2/OpenID Connect 资源服务器的支持。要将身份验证配置为 OAuth 2/OpenID Connect
  资源服务器，请使用 HttpSecurity 对象的 oauth2ResourceServer() 方法。
- 如果打算使用 JWT，则需通过 oauth2ResourceServer() 自定义器参数的 jwt() 方法完成相关配置。
- 如果系统使用不透明令牌或需要在授权服务器端撤销 JWT，也可以使用 Token 内省。在这种情况下，必须通过 oauth2ResourceServer()
  自定义器参数的 opaqueToken() 方法配置身份验证。
- 使用 JWT 时，必须设置公钥集合 URI。该 URI
  由授权服务器对外提供，资源服务器调用它以获取授权服务器端配置的密钥对的公钥部分。授权服务器使用私钥部分对访问令牌进行签名，而资源服务器则需要公钥部分来验证令牌。
- 使用内省时，需要配置内省 URI。资源服务器向该 URI 发送请求，以确认令牌是否有效以及获取更多相关信息。调用内省 URI
  时，资源服务器相当于授权服务器的客户端，因此需要使用自己的客户端凭据进行身份验证。
- Spring Security 还提供了使用身份验证管理器解析器组件自定义身份验证逻辑的能力。当必须实现更具体的场景（例如多租户或适配非标准实现）时，可以定义和配置这样的自定义组件。
