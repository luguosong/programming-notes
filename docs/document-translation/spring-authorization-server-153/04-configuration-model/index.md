# 配置

## 默认配置

OAuth2AuthorizationServerConfiguration 是一个 @Configuration，用于提供 OAuth2 授权服务器的最小默认配置。

OAuth2AuthorizationServerConfiguration 通过 OAuth2AuthorizationServerConfigurer 应用默认配置，并注册一个
SecurityFilterChain 类型的 @Bean，该 Bean 由支持 OAuth2 授权服务器的所有基础设施组件组成。

这个用于 OAuth2 授权服务器的 SecurityFilterChain @Bean 默认配置了以下协议端点：

- OAuth2 授权端点（OAuth2 Authorization endpoint）
- OAuth2 设备授权端点（OAuth2 Device Authorization Endpoint）
- OAuth2 设备验证端点（OAuth2 Device Verification Endpoint）
- OAuth2 令牌端点（OAuth2 Token endpoint）
- OAuth2 令牌自省端点（OAuth2 Token Introspection endpoint）
- OAuth2 令牌撤销端点（OAuth2 Token Revocation endpoint）
- OAuth2 授权服务器元数据端点（OAuth2 Authorization Server Metadata endpoint）
- JWK 集合端点（JWK Set endpoint）

!!! note

	只有在注册了一个 `JWKSource<SecurityContext>` 的 @Bean 时，才会配置 JWK 集合（JWK Set）端点。

下面的示例展示了如何使用 `OAuth2AuthorizationServerConfiguration` 来应用最小化的默认配置：

``` java
@Configuration
@Import(OAuth2AuthorizationServerConfiguration.class)
public class AuthorizationServerConfig {

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		List<RegisteredClient> registrations = ...
		return new InMemoryRegisteredClientRepository(registrations);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = ...
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

}
```

!!! note

	重要说明：`authorization_code` 授权模式要求资源所有者已通过认证。因此，除了默认的 OAuth2 安全配置之外，还必须额外配置用户认证机制。

在默认配置中，OpenID Connect 1.0 是禁用的。下面的示例演示了如何通过初始化 `OidcConfigurer` 来启用 OpenID Connect 1.0：

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();
	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.oidc(Customizer.withDefaults())    // 初始化 `OidcConfigurer`
			);
	return http.build();
}
```

除了默认的协议端点之外，OAuth2 授权服务器的 SecurityFilterChain @Bean 还配置了以下 OpenID Connect 1.0 协议端点： 

- OpenID Connect 1.0 提供者配置端点  
- OpenID Connect 1.0 登出端点  
- OpenID Connect 1.0 用户信息端点

!!! note

	OpenID Connect 1.0 的客户端注册端点默认是禁用的，因为很多部署场景并不需要动态客户端注册。

!!! tip

	`OAuth2AuthorizationServerConfiguration.jwtDecoder(JWKSource<SecurityContext>)` 是一个用于注册 JwtDecoder @Bean 的便捷（静态）工具方法。该 @Bean 对于 OpenID Connect 1.0 的 UserInfo 端点和 Client Registration 端点来说是必需的。

下面的示例展示了如何注册一个 JwtDecoder 的 @Bean：

```java
@Bean
public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
	return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
}
```

OAuth2AuthorizationServerConfiguration 的主要目的，是提供一种便捷方式，为 OAuth2 授权服务器应用一套最小化的默认配置。不过，在大多数情况下，仍然需要对这些配置进行自定义。

## 自定义配置

OAuth2AuthorizationServerConfigurer 提供了对 OAuth2 授权服务器安全配置进行全面自定义的能力。它允许你指定要使用的核心组件，例如 RegisteredClientRepository、OAuth2AuthorizationService、OAuth2TokenGenerator 等。此外，它还允许你自定义各个协议端点的请求处理逻辑，例如授权端点、设备授权端点、设备验证端点、令牌端点、令牌自省端点等。

OAuth2AuthorizationServerConfigurer 提供以下配置选项：

```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				//	registeredClientRepository()：用于管理新建和已存在客户端的 RegisteredClientRepository（必需）。
				.registeredClientRepository(registeredClientRepository)
				//	authorizationService()：用于管理新建和已有授权的 OAuth2AuthorizationService。
				.authorizationService(authorizationService)
				//	authorizationConsentService()：用于管理新建和已有授权许可的 OAuth2AuthorizationConsentService。
				.authorizationConsentService(authorizationConsentService)
				//	authorizationServerSettings()：用于自定义 OAuth2 授权服务器配置的 AuthorizationServerSettings（必需）。
				.authorizationServerSettings(authorizationServerSettings)
				//	tokenGenerator()：用于生成 OAuth2 授权服务器所支持令牌的 OAuth2TokenGenerator。
				.tokenGenerator(tokenGenerator)
				//	clientAuthentication()：用于配置 OAuth2 客户端认证的配置器。
				.clientAuthentication(clientAuthentication -> { })
				//	authorizationEndpoint()：用于配置 OAuth2 授权端点的配置器。
				.authorizationEndpoint(authorizationEndpoint -> { })
				//	pushedAuthorizationRequestEndpoint()：用于配置 OAuth2 推送授权请求（Pushed Authorization Request）端点的配置器。
				.pushedAuthorizationRequestEndpoint(pushedAuthorizationRequestEndpoint -> { })
				//	deviceAuthorizationEndpoint()：用于配置 OAuth2 设备授权端点的配置器。
				.deviceAuthorizationEndpoint(deviceAuthorizationEndpoint -> { })
				//	deviceVerificationEndpoint()：用于配置 OAuth2 设备验证端点的配置器。
				.deviceVerificationEndpoint(deviceVerificationEndpoint -> { })
				//	tokenEndpoint()：用于配置 OAuth2 Token 端点的配置器。
				.tokenEndpoint(tokenEndpoint -> { })
				//	tokenIntrospectionEndpoint()：用于配置 OAuth2 令牌内省端点的配置器。
				.tokenIntrospectionEndpoint(tokenIntrospectionEndpoint -> { })
				//	tokenRevocationEndpoint()：用于配置 OAuth2 令牌撤销端点的配置器。
				.tokenRevocationEndpoint(tokenRevocationEndpoint -> { })
				//	authorizationServerMetadataEndpoint()：用于配置 OAuth2 授权服务器元数据端点的配置器。
				.authorizationServerMetadataEndpoint(authorizationServerMetadataEndpoint -> { })
				.oidc(oidc -> oidc
					//	providerConfigurationEndpoint()：用于配置 OpenID Connect 1.0 提供方配置端点的配置器。
					.providerConfigurationEndpoint(providerConfigurationEndpoint -> { })
					//	logoutEndpoint()：用于配置 OpenID Connect 1.0 登出端点的配置器。
					.logoutEndpoint(logoutEndpoint -> { })
					//	userInfoEndpoint()：用于配置 OpenID Connect 1.0 UserInfo 端点的配置器。
					.userInfoEndpoint(userInfoEndpoint -> { })
					//	clientRegistrationEndpoint()：用于配置 OpenID Connect 1.0 客户端注册端点的配置器。
					.clientRegistrationEndpoint(clientRegistrationEndpoint -> { })
				)
		);

	return http.build();
}
```

## 配置授权服务器设置

AuthorizationServerSettings 包含 OAuth2 授权服务器的配置设置。它指定了各个协议端点的 URI，以及发行者标识符（issuer identifier）。协议端点的默认 URI 如下：

``` java
public final class AuthorizationServerSettings extends AbstractSettings {

	...

	public static Builder builder() {
		return new Builder()
			.authorizationEndpoint("/oauth2/authorize")
			.pushedAuthorizationRequestEndpoint("/oauth2/par")
			.deviceAuthorizationEndpoint("/oauth2/device_authorization")
			.deviceVerificationEndpoint("/oauth2/device_verification")
			.tokenEndpoint("/oauth2/token")
			.tokenIntrospectionEndpoint("/oauth2/introspect")
			.tokenRevocationEndpoint("/oauth2/revoke")
			.jwkSetEndpoint("/oauth2/jwks")
			.oidcLogoutEndpoint("/connect/logout")
			.oidcUserInfoEndpoint("/userinfo")
			.oidcClientRegistrationEndpoint("/connect/register");
	}

	...

}
```

!!! note

	AuthorizationServerSettings 是一个必需的组件。

!!! tip

    提示：如果尚未提供，使用 `@Import(OAuth2AuthorizationServerConfiguration.class)` 会自动注册一个 `AuthorizationServerSettings` 的 `@Bean`。

以下示例展示了如何自定义配置设置并注册一个 `AuthorizationServerSettings` 的 @Bean：

```java
@Bean
public AuthorizationServerSettings authorizationServerSettings() {
	return AuthorizationServerSettings.builder()
		.issuer("https://example.com")
		.authorizationEndpoint("/oauth2/v1/authorize")
		.pushedAuthorizationRequestEndpoint("/oauth2/v1/par")
		.deviceAuthorizationEndpoint("/oauth2/v1/device_authorization")
		.deviceVerificationEndpoint("/oauth2/v1/device_verification")
		.tokenEndpoint("/oauth2/v1/token")
		.tokenIntrospectionEndpoint("/oauth2/v1/introspect")
		.tokenRevocationEndpoint("/oauth2/v1/revoke")
		.jwkSetEndpoint("/oauth2/v1/jwks")
		.oidcLogoutEndpoint("/connect/v1/logout")
		.oidcUserInfoEndpoint("/connect/v1/userinfo")
		.oidcClientRegistrationEndpoint("/connect/v1/register")
		.build();
}
```

AuthorizationServerContext 是一个上下文对象，用于保存授权服务器运行环境的信息。它提供对 AuthorizationServerSettings 的访问能力，以及对“当前”发行者标识符（issuer identifier）的访问。

!!! note

    注意：如果未在 `AuthorizationServerSettings.builder().issuer(String)` 中配置发行者标识符（issuer identifier），则会从当前请求中解析该标识符。

!!! note

    AuthorizationServerContext 可以通过 AuthorizationServerContextHolder 访问，后者使用 ThreadLocal 将其与当前请求线程关联起来。

## 配置客户端认证

OAuth2ClientAuthenticationConfigurer 提供了自定义 OAuth2 客户端认证的能力。它定义了一系列扩展点，使你可以针对客户端认证请求的“前置处理、主处理以及后置处理”逻辑进行自定义。

OAuth2ClientAuthenticationConfigurer 提供以下配置选项：

```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.clientAuthentication(clientAuthentication ->
					clientAuthentication
						//	authenticationConverter()：添加一个 AuthenticationConverter（预处理器），
						// 	在尝试从 HttpServletRequest 中提取客户端凭证并转换为 OAuth2ClientAuthenticationToken 实例时使用。
						.authenticationConverter(authenticationConverter)
						//	authenticationConverters()：用于设置一个 Consumer，
						//	它可以访问默认的以及（可选）新添加的 AuthenticationConverter 列表，
						//	从而可以对某个具体的 AuthenticationConverter 进行添加、移除或自定义配置。
						.authenticationConverters(authenticationConvertersConsumer)
						//	authenticationProvider()：添加一个用于对 OAuth2ClientAuthenticationToken 
						//	进行认证的 AuthenticationProvider（主处理器）。
						.authenticationProvider(authenticationProvider)
						//	authenticationProviders()：用于设置一个 Consumer，
						//	它可以访问默认以及（可选）新增的 AuthenticationProvider 列表，
						//	从而实现添加、移除或自定义特定 AuthenticationProvider 的能力。
						.authenticationProviders(authenticationProvidersConsumer)
						//	authenticationSuccessHandler()：
						//	用于处理客户端认证成功并将 OAuth2ClientAuthenticationToken 
						//	关联到 SecurityContext 的 AuthenticationSuccessHandler（后置处理器）。
						.authenticationSuccessHandler(authenticationSuccessHandler)
						//	errorResponseHandler()：用于处理客户端认证失败并返回 OAuth2Error 
						//	响应的 AuthenticationFailureHandler（后置处理器）。
						.errorResponseHandler(errorResponseHandler)	
				)
		);

	return http.build();
}
```

OAuth2ClientAuthenticationConfigurer 用于配置 OAuth2ClientAuthenticationFilter，并将其注册到 OAuth2 授权服务器的 SecurityFilterChain @Bean 中。OAuth2ClientAuthenticationFilter 是负责处理客户端认证请求的过滤器。

默认情况下，OAuth2 令牌端点、OAuth2 令牌自省端点以及 OAuth2 令牌撤销端点都要求进行客户端认证。支持的客户端认证方式包括：client_secret_basic、client_secret_post、private_key_jwt、client_secret_jwt、tls_client_auth、self_signed_tls_client_auth，以及 none（公开客户端）。

OAuth2ClientAuthenticationFilter 的默认配置如下：

- **AuthenticationConverter** —— 一个 `DelegatingAuthenticationConverter`，由以下组件组成：  
  `JwtClientAssertionAuthenticationConverter`、`X509ClientCertificateAuthenticationConverter`、`ClientSecretBasicAuthenticationConverter`、`ClientSecretPostAuthenticationConverter` 和 `PublicClientAuthenticationConverter`。

- **AuthenticationManager** —— 一个由以下组件构成的 `AuthenticationManager`：  
  `JwtClientAssertionAuthenticationProvider`、`X509ClientCertificateAuthenticationProvider`、`ClientSecretAuthenticationProvider` 和 `PublicClientAuthenticationProvider`。

- **AuthenticationSuccessHandler** —— 一个内部实现，用于将已通过认证的 `OAuth2ClientAuthenticationToken`（当前的 `Authentication`）绑定到 `SecurityContext`。

- **AuthenticationFailureHandler** —— 一个内部实现，会使用 `OAuth2AuthenticationException` 中携带的 `OAuth2Error` 来返回 OAuth2 错误响应。

### 自定义JWT客户端声明校验

JwtClientAssertionDecoderFactory.DEFAULT_JWT_VALIDATOR_FACTORY 是默认的工厂，用于为指定的 RegisteredClient 提供一个 OAuth2TokenValidator<Jwt>，并用于校验 Jwt 客户端断言中的 iss、sub、aud、exp 和 nbf 等声明（claim）。

JwtClientAssertionDecoderFactory 允许通过提供自定义工厂（类型为 Function<RegisteredClient, OAuth2TokenValidator<Jwt>>）并传入 setJwtValidatorFactory()，来覆盖默认的 Jwt 客户端断言校验逻辑。

!!! note

    JwtClientAssertionDecoderFactory 是 JwtClientAssertionAuthenticationProvider 默认使用的 JwtDecoderFactory，用于为指定的 RegisteredClient 提供 JwtDecoder，并在 OAuth2 客户端认证过程中用于验证 JWT 持有者令牌。

自定义 `JwtClientAssertionDecoderFactory` 的一个常见用例，是在 Jwt 客户端断言中对额外的声明（claims）进行校验。

下面的示例展示了如何通过自定义的 `JwtClientAssertionDecoderFactory` 来配置 `JwtClientAssertionAuthenticationProvider`，以便在 JWT 客户端断言中校验一个额外的声明（claim）：

```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.clientAuthentication(clientAuthentication ->
					clientAuthentication
						.authenticationProviders(configureJwtClientAssertionValidator())
				)
		);

	return http.build();
}

private Consumer<List<AuthenticationProvider>> configureJwtClientAssertionValidator() {
	return (authenticationProviders) ->
		authenticationProviders.forEach((authenticationProvider) -> {
			if (authenticationProvider instanceof JwtClientAssertionAuthenticationProvider) {
				// 自定义 JwtClientAssertionDecoderFactory
				JwtClientAssertionDecoderFactory jwtDecoderFactory = new JwtClientAssertionDecoderFactory();
				Function<RegisteredClient, OAuth2TokenValidator<Jwt>> jwtValidatorFactory = (registeredClient) ->
					new DelegatingOAuth2TokenValidator<>(
						// 使用默认验证器
						JwtClientAssertionDecoderFactory.DEFAULT_JWT_VALIDATOR_FACTORY.apply(registeredClient),
						// 添加自定义验证器
						new JwtClaimValidator<>("claim", "value"::equals));
				jwtDecoderFactory.setJwtValidatorFactory(jwtValidatorFactory);

				((JwtClientAssertionAuthenticationProvider) authenticationProvider)
					.setJwtDecoderFactory(jwtDecoderFactory);
			}
		});
}
```

### 自定义双向TLS客户端认证

X509ClientCertificateAuthenticationProvider 用于在 OAuth2 客户端认证过程中，当使用 ClientAuthenticationMethod.TLS_CLIENT_AUTH 或 ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH 方式时，对接收到的客户端 X509Certificate 证书链进行认证。它还会与一个“证书验证器”（Certificate Verifier）组合使用，该验证器用于在 TLS 握手成功完成后，对客户端的 X509Certificate 内容进行校验。

#### PKI双向TLS 方法

对于 PKI 双向 TLS（ClientAuthenticationMethod.TLS_CLIENT_AUTH）方式，证书校验器的默认实现会将客户端 X509Certificate 的主体可分辨名称（Subject DN）与 RegisteredClient.getClientSettings().getX509CertificateSubjectDN() 中的配置进行比对验证。

如果你需要验证客户端 X509Certificate 的其他属性，例如主题备用名称（SAN）条目，可以参考下面的示例，了解如何通过自定义证书校验实现来配置 X509ClientCertificateAuthenticationProvider：

```java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.clientAuthentication(clientAuthentication ->
					clientAuthentication
						.authenticationProviders(configureX509ClientCertificateVerifier())
				)
		);

	return http.build();
}

private Consumer<List<AuthenticationProvider>> configureX509ClientCertificateVerifier() {
	return (authenticationProviders) ->
			authenticationProviders.forEach((authenticationProvider) -> {
				if (authenticationProvider instanceof X509ClientCertificateAuthenticationProvider) {
					Consumer<OAuth2ClientAuthenticationContext> certificateVerifier = (clientAuthenticationContext) -> {
						OAuth2ClientAuthenticationToken clientAuthentication = clientAuthenticationContext.getAuthentication();
						RegisteredClient registeredClient = clientAuthenticationContext.getRegisteredClient();
						X509Certificate[] clientCertificateChain = (X509Certificate[]) clientAuthentication.getCredentials();
						X509Certificate clientCertificate = clientCertificateChain[0];

						// TODO Verify Subject Alternative Name (SAN) entry

					};

					((X509ClientCertificateAuthenticationProvider) authenticationProvider)
							.setCertificateVerifier(certificateVerifier);
				}
			});
}
```

#### 自签名证书的双向TLS方法

对于自签名证书双向 TLS（`ClientAuthenticationMethod.SELF_SIGNED_TLS_CLIENT_AUTH`）方式，证书校验器的默认实现会根据 `RegisteredClient.getClientSettings().getJwkSetUrl()` 配置获取客户端的 JSON Web Key Set，并期望在其中找到与 TLS 握手过程中收到的客户端 `X509Certificate` 相匹配的密钥。

!!! note

    `RegisteredClient.getClientSettings().getJwkSetUrl()` 配置项用于通过 JSON Web Key（JWK）集来获取客户端证书。证书以该 JWK 集中某个单独 JWK 的 `x5c` 参数形式进行表示。

#### 绑定客户端证书的访问令牌

当在令牌端点使用双向 TLS（Mutual-TLS）客户端认证时，授权服务器就可以将签发的访问令牌绑定到客户端的 X509 证书上。绑定的方式是对客户端的 X509 证书计算其 SHA-256 拇指指纹（thumbprint），并将该指纹与访问令牌关联起来。比如，对于一个 JWT 访问令牌，可以在顶层的 cnf（confirmation method，确认方法）声明中，加入一个 x5t#S256 声明，用于存放该 X509 证书的拇指指纹。

将访问令牌绑定到客户端的 X509Certificate，可以在访问受保护资源时实现持有者证明（proof-of-possession）机制。  
例如，受保护资源可以获取客户端在双向 TLS 认证中使用的 X509Certificate，然后验证该证书的指纹是否与访问令牌中关联的 x5t#S256 声明相匹配。

以下示例展示了如何为客户端启用证书绑定访问令牌：

```java
RegisteredClient mtlsClient = RegisteredClient.withId(UUID.randomUUID().toString())
		.clientId("mtls-client")
		.clientAuthenticationMethod(ClientAuthenticationMethod.TLS_CLIENT_AUTH)
		.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
		.scope("scope-a")
		.clientSettings(
				ClientSettings.builder()
						.x509CertificateSubjectDN("CN=mtls-client,OU=Spring Samples,O=Spring,C=US")
						.build()
		)
		.tokenSettings(
				TokenSettings.builder()
						.x509CertificateBoundAccessTokens(true)
						.build()
		)
		.build();
```
