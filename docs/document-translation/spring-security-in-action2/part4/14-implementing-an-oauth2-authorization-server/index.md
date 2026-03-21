# 14.实现OAuth 2授权服务器

本章内容包括：

- 实现 Spring Security OAuth 2 授权服务器
- 使用授权码与客户端凭证授权类型
- 配置不透明与透明访问令牌
- 使用令牌撤销与令牌校验

第13章涵盖了 OAuth 2 和 OpenID Connect。我们讨论了在基于 OAuth 2
规范的身份验证与授权系统中发挥作用的各类角色。其中之一是授权服务器，它的职责是对用户及其使用的应用（即客户端）进行身份验证，并颁发可作为访问后端受保护资源的身份验证凭证（令牌）。有时，客户端也会代表用户完成此操作。

Spring 生态系统提供了一种高度可定制的方式来实现 OAuth 2/OpenID Connect 授权服务器。Spring Security 授权服务器目前是使用
Spring 构建授权服务器的事实标准。在本章中，我们将回顾该框架提供的主要功能，并实现一个自定义授权服务器。图 14.1 回顾了第 13
章中讨论的 OAuth 2 角色以及授权服务器的作用。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124171125101.png){ loading=lazy }
  <figcaption>图 14.1 OAuth 2 场景中的参与者。授权服务器保护用户和客户端信息，并颁发令牌，客户端调用资源服务器端点时可以使用这些令牌获取授权。</figcaption>
</figure>

我们从第 14.1 节的一个简单示例开始实现，该示例使用默认配置。默认配置意味着授权服务器将颁发非不透明令牌。在第 14.2
节中，我们验证所实现的内容能够配合授权码授权类型工作；随后在第 14.3 节，我们展示了客户端凭证授权类型的用法。在第 14.4
节，我们继续配置授权服务器以支持不透明令牌和令牌内省。第 14.5 节则以令牌注销为本章讨论的收尾。

在我们开始之前，我想先说明一下：使用 Spring Security
实现授权服务器的方式已经与往年完全不同。本章我们将讨论这种全新的方法，但你也可能需要了解旧方式下如何实现授权服务器（例如当需要维护尚未升级的现有应用时）。在这种情况下，我建议阅读本书第一版的第
13 章。

## 使用JWT(JSON web tokens)实现基本身份验证

在本节中，我们将使用 Spring Security 授权服务器框架搭建一个基础的 OAuth 2
授权服务器。接下来我们会逐一介绍配置中需要对接的各个关键组件，并分别进行解析。随后，我们将围绕两种最常用的 OAuth 2
授权方式——授权码和客户端凭证——对应用进行测试。你可以在项目 ssia-ch14-ex1 中找到该示例的实现。

要让授权服务器正常运行，你需要配置的主要组件包括：

1. `协议端点的配置过滤器(The configuration filter for protocol endpoints)` ——
   帮助你定义与授权服务器能力相关的特定配置，包括各种自定义（我们会在第14.3节中详细讨论）。
2. `身份验证配置过滤器(The authentication configuration filter)` —— 类似于任何使用 Spring Security 保护的 Web
   应用，你会使用此过滤器来定义身份验证和授权配置，以及其他安全机制（如跨域资源共享 CORS 和跨站请求伪造
   CSRF）的配置（详见第2到第10章）。
3. `用户详情管理组件(The user details management components)` —— 与任何通过 Spring Security 实现的身份验证流程一样，这些通过
   UserDetailsService Bean 和 PasswordEncoder 来建立，具体工作方式详见第3章和第4章。
4. `客户端详情管理(The client details management)` —— 授权服务器使用一个名为 RegisteredClientRepository
   的组件来管理客户端凭据及其他详情。
5. `密钥对管理（The key-pairs ,用于签名和验证令牌）` ——
   使用非不透明令牌时，授权服务器会用私钥为令牌签名，并提供一个公钥供资源服务器验证令牌。授权服务器通过一个“密钥源”组件来管理公私钥对。
6. `通用应用设置(The general app settings)` —— 一个名为 AuthorizationServerSettings 的组件帮助你配置一些通用的定制项，例如应用公开的端点等。

图14.2 展示了我们需要接入并配置的组件，以让一个最简授权服务器应用正常运行。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124172314509.png){ loading=lazy }
  <figcaption>图 14.2 使用 Spring Security 实现的授权服务器所需配置和集成的组件</figcaption>
</figure>

首先，我们需要将所需的依赖项添加到项目中。下面的代码片段展示了需要在 pom.xml 项目中加入的依赖项：

``` xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
</dependency>
```

我们像下面的代码片段那样，在标准的 Spring 配置类中编写配置。

``` java
@Configuration
public class SecurityConfig {

}
```

请记住，与其他 Spring 应用一样，bean 可以定义在多个配置类中，也可以根据具体情况通过标识注解来定义。如果你需要复习一下 Spring
上下文的管理，推荐阅读我写的另一本书《Spring Start Here》（Manning，2021）中的第一部分。

我们来看看清单 14.1，其中展示了协议端点的配置过滤器。`applyDefaultSecurity()`
方法是一个实用方法，用于定义一组最小配置，日后可根据需要覆盖。在调用该方法后，清单演示了如何使用
`OAuth2AuthorizationServerConfigurer` 配置器对象的 `oidc()` 方法启用 OpenID Connect 协议。

此外，清单 14.1 中的过滤器指定了应用在要求用户登录时需要重定向的认证页面。我们需要这个配置，因为示例中计划启用授权码授权类型，而这意味着用户必须进行身份验证。Spring
Web 应用的默认路径是 /login，因此除非我们配置自定义路径，否则在配置授权服务器时会采用该路径。

```java title="清单 14.1 实现用于配置协议端点的过滤器"

@Bean
@Order(1)
public SecurityFilterChain asFilterChain(HttpSecurity http)
		throws Exception {

	//调用工具方法为授权服务器端点应用默认配置
	OAuth2AuthorizationServerConfiguration
			.applyDefaultSecurity(http);

	//启用 OpenID Connect 协议
	http.getConfigurer(
					OAuth2AuthorizationServerConfigurer.class)
			.oidc(Customizer.withDefaults());

	//为用户指定认证页面
	http.exceptionHandling((e) ->
			e.authenticationEntryPoint(
					new LoginUrlAuthenticationEntryPoint("/login"))
	);

	return http.build();
}

```

清单 14.2 配置了认证与授权。这些配置的工作方式与我们在第2到第10章讨论的任何 Web 应用类似。在清单 14.2 中，我仅设置了最基本的配置：

1. 启用表单登录认证，让应用为用户提供一个简洁的登录页进行身份验证；
2. 指定应用只允许已认证的用户访问任意端点。

除了认证和授权之外，你还可以在这里配置其他内容，例如第9章讲的 CSRF 防护机制或第10章讲的 CORS。

还要注意我在示例14.1和14.2中使用的@Order注解。因为应用上下文中配置了多个SecurityFilterChain实例，所以需要通过该注解明确它们在配置中所应具有的优先级顺序。

```java title="清单 14.2 实现授权配置的过滤器"

@Bean
@Order(2) // 我们将过滤器设置为在协议端点之后进行解释。
public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
		throws Exception {

	//我们启用了表单登录认证方式
	http.formLogin(Customizer.withDefaults());

	//我们配置所有端点都需要认证。
	http.authorizeHttpRequests(
			c -> c.anyRequest().authenticated()
	);

	return http.build();
}

```

如果你预计客户端会使用你构建的授权服务器来处理需要用户认证的授权类型（比如授权码模式），那么你的服务器就必须管理用户信息！幸运的是，要实现用户信息管理，你可以沿用第3章和第4章学到的方法。只需提供一个
UserDetailsService 和一个 PasswordEncoder 实现即可。

Listing 14.3 给出了这两个组件的定义。在这个例子中，我们使用了一个内存实现的 UserDetailsService，但请记住，在第 3
章你已经学会了如何为它编写自定义实现。在大多数情况下，就像对待其他 Web 应用一样，你会将这些信息保存在数据库中。因此，你必须为
UserDetailsService 接口编写自定义实现。

另外，还记得我们在第4章讨论过，NoOpPasswordEncoder 只能用于学习示例。它不会对密码做任何处理，密码以明文形式存在，任何有权限访问的人都能看到，显然这样不安全。我们应该始终使用带有强哈希函数（比如
BCrypt）的密码编码器。

```java title="清单 14.3 定义用户详情管理"

@Bean
public UserDetailsService userDetailsService() {
	UserDetails userDetails = User.withUsername("bill")
			.password("password")
			.roles("USER")
			.build();

	return new InMemoryUserDetailsManager(userDetails);
}

@Bean
public PasswordEncoder passwordEncoder() {
	return NoOpPasswordEncoder.getInstance();
}
```

授权服务器需要一个 RegisteredClientRepository 组件来管理客户端信息。RegisteredClientRepository 接口的作用类似于
UserDetailsService，但它用于检索客户端详情。同样，框架也提供了 RegisteredClient 对象，用于描述授权服务器所识别的客户端应用。

为了呼应第三章和第四章所学内容，可以把 RegisteredClient 对应客户端，就像 UserDetails 对应用户一样。同样地，RegisteredClientRepository
对客户端详情的作用，就像 UserDetailsService 之于用户详情（参见图 14.3）。

在这个示例中，我们将使用内存实现，以便你能够专注于授权服务器的整体实现。不过，在真实的应用中，你极有可能需要为这个接口提供一个从数据库获取数据的实现。为了实现这一点，你需要以类似第3章中实现
UserDetailsService 接口的方式来实现 RegisteredClientRepository 接口。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124224514032.png){ loading=lazy }
  <figcaption>图14.3 为了管理客户端详情，我们使用 RegisteredClientRepository 实现。RegisteredClientRepository 使用 RegisteredClient 对象来表示客户端信息。</figcaption>
</figure>

下一个代码示例展示了内存中 RegisteredClientRepository Bean 的定义。该方法创建一个包含必需信息的 RegisteredClient
实例，并将其存储在内存中，以便授权服务器在认证过程中使用。

```java title="示例14.4 实现客户端详情管理"

@Bean
public RegisteredClientRepository registeredClientRepository() {
	RegisteredClient registeredClient =
			RegisteredClient
					.withId(UUID.randomUUID().toString())
					.clientId("client")
					.clientSecret("secret")
					.clientAuthenticationMethod(
							ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.authorizationGrantType(
							AuthorizationGrantType.AUTHORIZATION_CODE)
					.redirectUri("https://www.manning.com/authorized")
					.scope(OidcScopes.OPENID)
					.build();

	return new InMemoryRegisteredClientRepository
			(registeredClient);
}
```

我们在创建 RegisteredClient 实例时指定的详细信息如下：

- 唯一的内部 ID——一个唯一标识客户端的值，仅用于内部应用流程。
- 客户端 ID——一个外部客户端标识，类似于用户的用户名。
- 客户端密钥——类似于用户的密码。
- 客户端认证方式——说明授权服务器期望客户端在发送访问令牌请求时如何进行认证。
- 授权授予类型——授权服务器允许该客户端使用的授予类型。一个客户端可能使用多种授予类型。
- 重定向 URI——授权服务器允许客户端在使用授权码授予类型时请求重定向以提供授权码的某个 URI 地址。
- 范围——定义请求访问令牌的目的。该范围可在后续的授权规则中使用。

在此示例中，客户端仅使用授权码授权类型。不过，也可以配置支持多种授权类型的客户端。如果希望某个客户端能够使用多种授权类型，需要按下面代码片段所示进行配置。这里定义的客户端可以使用任意授权类型（授权码、客户端凭证或刷新令牌）：

```java
RegisteredClient registeredClient =
		RegisteredClient
				.withId(UUID.randomUUID().toString())
				.clientId("client")
				.clientSecret("secret")
				.clientAuthenticationMethod(
						ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(
						AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(
						AuthorizationGrantType.CLIENT_CREDENTIALS)
				.authorizationGrantType(
						AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("https://www.manning.com/authorized")
				.scope(OidcScopes.OPENID)
				.build();       
```

同样地，通过多次调用 `redirectUri()` 方法，你可以指定多个允许的重定向 URI。同理，客户端也可能拥有多个可访问的作用域。在真实的应用中，这些信息都会存储在数据库里，由你为
`RegisteredClientRepository` 编写的自定义实现去获取这些信息。

除了配置用户和客户端的信息外，如果授权服务器使用非不透明令牌（在第 13
章讨论），你还必须配置密钥对管理。对于非不透明令牌，授权服务器使用私钥对令牌进行签名，并向客户端提供公钥，供客户端用来验证令牌的真实性。

`JWKSource` 是为 Spring Security 授权服务器提供密钥管理的对象。清单 14.5 展示了如何在应用上下文中配置 `JWKSource`
。在这个示例中，我通过编程方式创建密钥对，并将它添加到授权服务器可用的密钥集合中。在真实应用中，密钥会从安全存储的位置（例如环境中配置的
vault）中读取。

要完全还原一个真实系统的环境将会非常复杂，我更希望你关注授权服务器的实现。不过，请记住，在真实应用中，每次应用重启时重新生成密钥是不可取的（就像我们的示例那样）。如果真实应用这样做，那么每当发生新的部署，之前签发的令牌将无法再使用（因为无法再用已有密钥验证）。

因此，在我们的例子中，编程生成密钥是可以的，并且有助于展示授权服务器的工作原理。而在真实应用中，你必须将密钥安全地存储起来，并从指定位置读取。

```java title="清单 14.5 实现密钥对集合管理"

@Bean
public JWKSource<SecurityContext> jwkSource()
		throws NoSuchAlgorithmException {

	KeyPairGenerator keyPairGenerator =
			KeyPairGenerator.getInstance("RSA");

	keyPairGenerator.initialize(2048);
	KeyPair keyPair = keyPairGenerator.generateKeyPair();

	RSAPublicKey publicKey =
			(RSAPublicKey) keyPair.getPublic();
	RSAPrivateKey privateKey =
			(RSAPrivateKey) keyPair.getPrivate();

	RSAKey rsaKey = new RSAKey.Builder(publicKey)
			.privateKey(privateKey)
			.keyID(UUID.randomUUID().toString())
			.build();

	JWKSet jwkSet = new JWKSet(rsaKey);
	return new ImmutableJWKSet<>(jwkSet);
}

```

最后，我们需要添加到最简配置中的最后一个组件是 AuthorizationServerSettings 对象（示例
14.6）。该对象允许你自定义授权服务器所暴露的所有端点路径。如果你按照下面的示例创建该对象，这些端点路径将会采用一些后面本节将要分析的默认设置。

```java title="清单 14.6 配置授权服务器通用设置"

@Bean
public AuthorizationServerSettings authorizationServerSettings() {
	return AuthorizationServerSettings.builder().build();
}
```

现在我们可以启动应用并进行测试，看看是否正常运行。在 14.2 节中，我们将运行授权码流程。接着，在 14.3
节，我们会验证客户端凭证流程是否在我们的授权码实现中按预期工作。

## 运行授权码授权模式

在本节中，我们将测试第 14.1 节中实现的授权服务器。我们期望通过已注册的客户端信息，能够遵循授权码流程并获取访问令牌。我们将按以下步骤进行：

1. 检查授权服务器暴露的端点；
2. 使用授权端点获取授权码；
3. 使用授权码获取访问令牌。

第一步是查找授权服务器公开的端点路径。由于我们没有配置自定义路径，因此只能使用默认值。那么默认值是什么？可以通过下段示例中的
OpenID 配置端点来获取这些信息。该请求使用 HTTP GET 方法，不需要认证。

```shell
http://localhost:8080/.well-known/openid-configuration
```

调用 OpenID 配置端点时，您应该会收到如下所示的响应。

```json title="图 14.7 OpenID 配置请求的响应"
{
  "issuer": "http://localhost:8080",
  "authorization_endpoint": "http://localhost:8080/oauth2/authorize",
  "token_endpoint": "http://localhost:8080/oauth2/token",
  "token_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post",
    "client_secret_jwt",
    "private_key_jwt"
  ],
  "jwks_uri": "http://localhost:8080/oauth2/jwks",
  "userinfo_endpoint": "http://localhost:8080/userinfo",
  "response_types_supported": [
    "code"
  ],
  "grant_types_supported": [
    "authorization_code",
    "client_credentials",
    "refresh_token"
  ],
  "revocation_endpoint": "http://localhost:8080/oauth2/revoke",
  "revocation_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post",
    "client_secret_jwt",
    "private_key_jwt"
  ],
  "introspection_endpoint": "http://localhost:8080/oauth2/introspect",
  "introspection_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "client_secret_post",
    "client_secret_jwt",
    "private_key_jwt"
  ],
  "subject_types_supported": [
    "public"
  ],
  "id_token_signing_alg_values_supported": [
    "RS256"
  ],
  "scopes_supported": [
    "openid"
  ]
}

```

让我们看一下图14.4，回顾一下第13章讨论过的授权码流程。现在我们用它来演示我们搭建的授权服务器运行良好。

因为我们的示例没有客户端，所以需要我们自己模拟一个。现在你已经知道了授权端点，可以把它粘贴到浏览器地址栏中，模拟客户端重定向用户到该地址的过程。下面的代码片段展示了这个授权请求：

```shell
http://localhost:8080/oauth2/authorize?
response_type=code&
client_id=client&
scope=openid&
redirect_uri=https://www.manning.com/authorized&
code_challenge=QYPAZ5NU8yvtlQ9erXrUYR-T5AGCjCF47vN-KsaI2A8&
code_challenge_method=S256
```

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251124160513972.png){ loading=lazy }
  <figcaption>图14.4 授权码授予类型。认证成功后，客户端会收到一个授权码。随后，客户端使用该授权码去获取访问令牌，从而访问受资源服务器保护的资源。</figcaption>
</figure>

对于授权请求，你可以看到我添加了几个参数：

- `response_type=code`——这个请求参数告诉授权服务器客户端希望使用授权码模式。别忘了一个客户端可能配置了多种授权模式，它需要告诉授权服务器自己想用哪一种。
- `client_id=client`——客户端标识类似于用户的“用户名”，用于在系统中唯一标识该客户端。
- `scope=openid`——指定客户端希望在本次认证中获得的权限范围。
- `redirect_uri=https://www.manning.com/authorized`——指定在认证成功后授权服务器要重定向到的 URI。这个 URI
  必须是当前客户端预先配置过的其中之一。
- `code_challenge=QYPAZ5NU8yvtlQ…`——如果使用了带 PKCE 的授权码（第 13
  章有介绍），必须在授权请求中提供码挑战。在请求令牌时，客户端需要发送验证码对，以证明它是最初发起该请求的同一个应用。PKCE
  流程默认启用。
- `code_challenge_method=S256`——这个请求参数说明用来从验证码生成挑战的哈希方法。本例中的 S256 表示使用了 SHA-256 作为哈希函数。

我建议使用带 PKCE 的授权码授权类型，但如果确实需要关闭该流程的 PKCE 增强，可以按下面的代码片段所示操作。请留意
clientSettings() 方法，它接受一个 ClientSettings 实例，您可以在其中指定禁用代码交换的证明密钥：

```java
RegisteredClient registeredClient = RegisteredClient
		.withId(UUID.randomUUID().toString())
		.clientId("client")
		// …
		.clientSettings(ClientSettings.builder()
				.requireProofKey(false)
				.build())
		.build();
```

在本例中，我们演示了默认且推荐使用的带 PKCE 的授权码流程。通过浏览器地址栏发送授权请求，我们模拟了图 14.4 中的第 2
步。授权服务器会将我们重定向至其登录页面，此时可使用用户名和密码进行身份验证，对应图 14.4 中的第 3 步。图 14.5
展示了授权服务器提供给用户的登录页面。


<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125105345341.png){ loading=lazy }
  <figcaption>图 14.5 授权服务器在响应授权请求时向用户展示的登录页面。</figcaption>
</figure>

在我们的实现中，我们只有一个用户（参见清单 14.3）。该用户的凭据是用户名 bill，密码为
password。用户输入正确凭据并点击“登录”按钮后，授权服务器会将用户重定向到请求的重定向 URI，并返回一个授权码（如图 14.6 所示；图
14.4 的步骤 4）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125105757993.png){ loading=lazy }
  <figcaption>图14.6 身份验证成功后，授权服务器将用户引导到指定的重定向 URI，并颁发授权码。客户端随后使用该授权码来获取访问令牌。</figcaption>
</figure>

一旦客户端获得授权码，就可以请求访问令牌。客户端可以通过令牌端点请求访问令牌。下面的代码片段展示了使用 cURL 请求令牌的示例，该请求采用
HTTP POST 方法。由于在注册客户端时指定需要使用 HTTP Basic 进行认证，因此令牌请求必须使用客户端 ID 和密钥通过 HTTP Basic
进行身份验证。

```shell
curl -X POST 'http://localhost:8080/oauth2/token?
client_id=client&
redirect_uri=https://www.manning.com/authorized&
grant_type=authorization_code&
code=ao2oz47zdM0D5gbAqtZVB…
code_verifier=qPsH306-… \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA=='
```

我们使用的请求参数包括：

- client_id=client —— 用于标识客户端
- redirect_uri=https://www.manning.com/authorized —— 授权服务器在用户成功认证后通过此重定向 URI 向客户端返回授权码
- grant_type=authorization_code —— 表示客户端使用哪种流程来请求访问令牌
- code=ao2oz47zdM0D5… —— 授权服务器提供给客户端的授权码值
- code_verifier=qPsH306-ZDD… —— 客户端在授权阶段发送挑战时所基于的验证器

!!! note

	务必高度重视每一个细节。如果任何数值与应用所掌握的信息或授权请求中提交的数据不一致，令牌请求就无法成功。

下一个片段展示了令牌请求的响应体。现在客户端已经拥有一个访问令牌，可以用它向资源服务器发送请求。

```json
{
  "access_token": "eyJraWQiOiI4ODlhNGFmO…",
  "scope": "openid",
  "id_token": "eyJraWQiOiI4ODlhNGFmOS1…",
  "token_type": "Bearer",
  "expires_in": 299
}
```

由于我们启用了 OpenID Connect 协议，因此不仅依赖 OAuth 2，令牌响应中还包含 ID 令牌。如果客户端在注册时启用了刷新令牌授权类型，响应中还会生成并发送刷新令牌。

### 生成代码验证器与挑战

在本节的示例中，我使用了带有 PKCE 的授权码流程。在授权请求和令牌请求中，我使用了事先生成的 challenge 和 verifier
值。我没有特别关注这些值，因为它们属于客户端的职责，而不是授权服务器或资源服务器生成的内容。在真实应用中，当你的 JavaScript
或移动应用使用 OAuth 2 流程时，需要自行生成这两个值。

不过，如果你感兴趣，我会在这个侧边栏里解释我是如何生成这两个数值的。你可以在项目 ssia-ch14-ex2 中找到这个示例。

验证码是一段随机的 32 字节数据。为了便于在 HTTP 请求中传输，需要使用 URL 安全的方式对其进行 Base64 编码，并去掉填充。下面的代码片段展示了在
Java 中如何实现这一过程：

``` java
SecureRandom secureRandom = new SecureRandom();
byte [] code = new byte[32];
secureRandom.nextBytes(code);
String codeVerifier = Base64.getUrlEncoder()
       .withoutPadding()
       .encodeToString(code);
```

一旦你获得了代码验证器，就可以使用哈希函数生成挑战。下面的代码片段展示了如何使用 SHA-256 哈希函数创建挑战。与验证器类似，你需要使用
Base64 将字节数组转换为字符串，以便通过 HTTP 请求更方便地传输。

``` java
MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

byte [] digested = messageDigest.digest(verifier.getBytes());
String codeChallenge = Base64.getUrlEncoder()
         .withoutPadding()
         .encodeToString(digested);
```

现在你已经拥有 verifier 和 challenge，可以按照本节讨论的方式，将它们用于授权请求和令牌请求中。

## 运行客户端凭证授权模式

在本节中，我们将使用 14.1
节实现的授权服务器来试用客户端凭证授权类型。请记住，客户端凭证授权是一种无需用户身份验证或同意即可让客户端获取访问令牌的流程。通常，最好不要让同一个客户端既能使用依赖用户的授权类型（例如授权码），又能使用与用户无关的授权类型（例如客户端凭证）。

正如你将在第 15 章（资源服务器篇）中了解到的，授权实现有时可能无法区分通过授权码模式获得的访问令牌和客户端凭据模式得到的令牌。因此，在这类场景中最好使用不同的注册配置，并通过不同的
scope 来区分令牌用途。

代码清单 14.8 展示了一个能够使用客户端凭据授权模式的注册客户端。你会注意到我还配置了一个不同的
scope。在这个例子里，“CUSTOM”只是我随意起的名称，你可以为 scope 选择任何名字。一般来说，scope
的名字应当有助于理解其用途。例如，如果这个应用需要通过客户端凭据模式获取令牌以检查资源服务器的存活状态，那么把 scope 命名为
“LIVENESS” 可能更直观。

本节讨论的示例可在项目 ssia-ch14-ex3 中找到。

```java title="列表 14.8：为客户端凭证授权类型配置已注册客户端"

@Bean
public RegisteredClientRepository registeredClientRepository() {
	RegisteredClient registeredClient =
			RegisteredClient.withId(UUID.randomUUID().toString())
					.clientId("client")
					.clientSecret("secret")
					.clientAuthenticationMethod(
							ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
					.scope("CUSTOM")
					.build();

	return new InMemoryRegisteredClientRepository(registeredClient);
}

```

图14.7展示了我们在第13章讨论过的客户端凭据流程。为获取访问令牌，客户端只需发送请求，并使用自身的凭据（客户端ID和密钥）完成认证。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125112028460.png){ loading=lazy }
  <figcaption>图14.7 客户端凭证授权模式。应用仅凭自身的客户端凭证即可获取访问令牌。</figcaption>
</figure>

以下片段展示了一个 cURL 令牌请求。与我们在 14.2
节中执行授权码授权类型时使用的请求相比，可以看到这个请求更为简单。客户端只需说明它使用客户端凭证授权类型，以及它申请令牌的作用域。客户端通过在请求中使用
HTTP Basic 携带自身凭证来完成认证。

```shell
curl -X POST 'http://localhost:8080/oauth2/token?
grant_type=client_credentials&
scope=CUSTOM' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA=='
```

下一个代码片段展示了包含所请求访问令牌的 HTTP 响应体：

```shell
{
   "access_token": "eyJraWQiOiI4N2E3YjJiNS…",
   "scope": "CUSTOM",
   "token_type": "Bearer",
   "expires_in": 300
}
```

## 使用不透明令牌与令牌自省

在本章前文中，我们已经演示了授权码授权类型（14.2 节）和客户端凭据授权类型（14.3
节）。通过这两种方式，我们都成功配置了能够获取非不透明访问令牌的客户端。不过，你也可以轻松配置客户端使用不透明令牌。本节将展示如何配置已注册客户端以获取不透明令牌，以及授权服务器如何协助验证这些不透明令牌。本节所讨论的示例代码位于项目
ssia-ch14-ex4 中。

清单 14.9 演示了如何为注册客户端配置不透明令牌。请记住，不透明令牌可以用于任何授权类型。本节中我将使用客户端凭证授权类型，以尽量简化流程，便于你专注于我们讨论的主题。当然，你也可以通过授权码授权类型生成不透明令牌。

```java title="列表14.9 配置客户端使用不透明令牌"

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
							.build())
					.scope("CUSTOM")
					.build();

	return new InMemoryRegisteredClientRepository(registeredClient);
}

```

如果你按照第 14.3 节所学的方式请求访问令牌，你会得到一个不透明的令牌。该令牌更短，也不包含任何数据。下面的代码片段是一个用于请求访问令牌的
cURL 命令：

```shell
curl -X POST 'http://localhost:8080/oauth2/token?
grant_type=client_credentials&
scope=CUSTOM' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA=='
```

以下片段展示了当我们预期获得非不透明令牌时，返回结果与实际收到的响应类似。唯一的区别在于令牌本身：它不再是 JWT
令牌，而是一个不透明令牌。

```shell
{
   "access_token": "iED8-...",
   "scope": "CUSTOM",
   "token_type": "Bearer",
   "expires_in": 299
}
```

下一段代码展示了一个完整的不透明令牌示例。请注意，它要短得多，也没有 JWT 的常见结构（缺少由点分隔的三段）。

```shell
iED8-aUd5QLTfihDOTGUhKgKwzhJFzY
WnGdpNT2UZWO3VVDqtMONNdozq1
r9r7RiP0aNWgJipcEu5HecAJ75V
yNJyNuj-kaJvjpWL5Ns7Ndb7Uh6
DI6M1wMuUcUDEjJP
```

由于不透明令牌本身不包含数据，那么如何验证它，并获取授权服务器为某个客户端（以及可能的用户）生成该令牌的更多信息呢？最简单且最常用的方法就是直接向授权服务器查询。授权服务器会提供一个端点，允许你携带令牌发起请求，并返回该令牌所需的详细信息。这个过程被称为自省（见图14.8）。

<figure markdown="span">
  ![](https://cdn.jsdelivr.net/gh/luguosong/images@master/blog-img/20251125112400877.png){ loading=lazy }
  <figcaption>图 14.8 令牌内省。使用不透明令牌时，资源服务器需要向授权服务器发送请求，以确定令牌是否有效，并了解其签发对象的更多细节。</figcaption>
</figure>

下面的片段展示了调用授权服务器公开的自省端点的 cURL 命令。客户端在发送请求时必须使用 HTTP Basic
携带自身凭据进行认证。客户端将令牌作为请求参数发送，并在响应中获取该令牌的详细信息：

```shell
curl -X POST 'http://localhost:8080/oauth2/introspect?token=iED8-…' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA=='
```

下段代码展示了对有效令牌执行自省请求时的响应示例。令牌有效时，其状态显示为 “active”，响应中会返回授权服务器所掌握的全部令牌信息。

```json
{
  "active": true,
  "sub": "client",
  "aud": [
    "client"
  ],
  "nbf": 1682941720,
  "scope": "CUSTOM",
  "iss": "http://localhost:8080",
  "exp": 1682942020,
  "iat": 1682941720,
  "jti": "ff14b844-1627-4567-8657-bba04cac0370",
  "client_id": "client",
  "token_type": "Bearer"
}
```

如果该令牌不存在或已过期，其活动状态为 false，如下段代码所示：

```json
{
  "active": false
}
```

令牌的默认有效时间为 300 秒。在示例中，你最好延长令牌的生命周期，否则在测试时可用时间会不够，令人沮丧。代码清单 14.10
展示了如何修改令牌的存活时间。为了示例方便，我倾向于将其设置得很长（比如这里的 12 小时），但务必记住，真实应用中千万别设置得这么大。实际项目里通常会把存活时间控制在
10 到 30 分钟以内。

``` java title="列表 14.10 修改访问令牌的生存时间"
RegisteredClient registeredClient = RegisteredClient
        .withId(UUID.randomUUID().toString())
        .clientId("client")
         // …
        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
        .tokenSettings(TokenSettings.builder()
            .accessTokenFormat(OAuth2TokenFormat.REFERENCE)
            .accessTokenTimeToLive(Duration.ofHours(12))
            .build())
        .scope("CUSTOM")
        .build();

```

## 撤销令牌

假设你发现某个令牌被盗。该如何使这个令牌失效？令牌撤销是一种让授权服务器先前签发的令牌失效的办法。通常，访问令牌的生命周期较短，即便被盗也不易被滥用。不过有时你可能希望更加谨慎。

以下片段展示了一个可用于向授权服务器公开的令牌撤销端点发送请求的 cURL 命令。你可以使用本章中我们实现的任一项目进行测试。Spring
Security 授权服务器默认启用撤销功能。该请求只需携带待撤销的令牌，并使用客户端凭证进行 HTTP Basic 认证。一旦请求发送，该令牌将无法再被使用。

```shell
curl -X POST 'http://localhost:8080/oauth2/revoke?token=N7BruErWm-44-…' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' 
```

如果使用自省端点检查已撤销的令牌，即使其存活时间尚未到期，也应能看到该令牌在撤销后已不再有效。

```shell
curl -X POST 'http://localhost:8080/oauth2/introspect?token=N7BruErWm-44-…' \
--header 'Authorization: Basic Y2xpZW50OnNlY3JldA==' 
```

在某些情况下撤销令牌确实有意义，但并非总是必要。请记住，如果希望启用撤销功能，就意味着每次调用都需要执行内省（即便是非不透明令牌）来验证令牌是否仍然有效。如此频繁地进行内省可能会显著影响性能。你应该始终问自己：我真的需要这道额外的防护层吗？

还记得我们在第一章的讨论吗？有时候，把钥匙藏在地毯下就足够了；而有时候，你需要先进、复杂且昂贵的报警系统。选择哪种方式，取决于你要保护的东西。

## 总结

- Spring Security 授权服务器框架可以帮助你从头构建自定义的 OAuth 2 / OpenID Connect 授权服务器。
- 因为授权服务器负责管理用户和客户端信息，你必须实现相关组件来定义应用如何收集这些数据：
	- 要管理用户信息，授权服务器需要一个与其他 Web 应用类似的 Spring Security 组件：`UserDetailsService` 的实现。
	- 要管理客户端信息，授权服务器提供了一个必须实现的契约：`RegisteredClientRepository`。
- 你可以注册使用不同认证流程（授权类型）的客户端。最好不要让同一个客户端同时使用依赖用户的流程（如授权码模式）和不依赖用户的流程（如客户端凭证模式）。
- 使用非不透明令牌（通常是 JWT）时，还需要配置一个组件来管理授权服务器用于签名令牌的密钥对。这个组件称为 `JWKSource`。
- 使用不透明令牌（不包含数据的令牌）时，资源服务器必须通过内省端点验证令牌有效性并获取授权所需数据。
- 在某些情况下，你需要使已签发的令牌失效。授权服务器提供了撤销端点来实现这一能力。使用撤销机制时，资源服务器必须始终对令牌进行内省（即使是非不透明令牌），以验证其有效性。
