# 核心模型/组件

## RegisteredClient

RegisteredClient 用来表示在授权服务器中已注册的客户端。客户端必须先在授权服务器上完成注册，之后才能发起授权码（authorization_code）、客户端凭证（client_credentials）等授权许可流程。

在客户端注册过程中，系统会为客户端分配一个唯一的客户端标识符（client identifier），以及（可选的）客户端密钥（client
secret，是否需要取决于客户端类型），还会绑定与该客户端标识符相关的一系列元数据。客户端的元数据范围很广，可以包括对人类用户展示的字符串（例如客户端名称），也可以包含与具体协议流程相关的配置项（例如合法重定向
URI 列表）等。

!!! note

	在 Spring Security 的 OAuth2 Client 支持中，与之对应的客户端注册模型是 `ClientRegistration`。

客户端的主要作用是请求访问受保护的资源。客户端首先通过向授权服务器进行身份验证并出示授权许可来请求访问令牌。授权服务器会对客户端和授权许可进行验证，如果均合法有效，则签发访问令牌。之后，客户端就可以通过出示该访问令牌，向资源服务器请求访问受保护的资源。

以下示例展示了如何配置一个 RegisteredClient，使其可以使用 authorization_code 授权方式来请求访问令牌：

``` java
RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
	.clientId("client-a")
	.clientSecret("{noop}secret") // {noop} 表示 Spring Security 中 NoOpPasswordEncoder 的 PasswordEncoder ID。
	.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
	.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
	.redirectUri("http://127.0.0.1:8080/authorized")
	.scope("scope-a")
	.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
	.build();
```

在 Spring Security 的 OAuth2 Client 支持中，对应的配置如下：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          client-a:
            provider: spring
            client-id: client-a
            client-secret: secret
            authorization-grant-type: authorization_code
            redirect-uri: "http://127.0.0.1:8080/authorized"
            scope: scope-a
        provider:
          spring:
            issuer-uri: http://localhost:9000
```

已注册客户端（RegisteredClient）具有与其唯一客户端标识符关联的元数据（属性），其定义如下：

``` java
public class RegisteredClient implements Serializable {
	// id：唯一标识该 RegisteredClient 的 ID。
	private String id;
	// clientId：客户端标识符。  
	private String clientId;
	// clientIdIssuedAt：客户端标识符的签发时间。
	private Instant clientIdIssuedAt;
	// clientSecret：客户端密钥。该值应使用 Spring Security 的 PasswordEncoder 进行编码。
	private String clientSecret;
	// clientSecretExpiresAt：客户端密钥的过期时间。 
	private Instant clientSecretExpiresAt;
	// clientName：客户端的描述性名称。在某些场景下可能会被使用，例如在授权同意页面展示客户端名称时。  
	private String clientName;
	// clientAuthenticationMethods：客户端可使用的认证方式。
	// 支持的值包括：client_secret_basic、client_secret_post、private_key_jwt、client_secret_jwt，以及 none（公共客户端）。 
	private Set<ClientAuthenticationMethod> clientAuthenticationMethods;
	// authorizationGrantTypes：客户端可使用的授权类型。
	// 支持的值包括：authorization_code、client_credentials、refresh_token、urn:ietf:params:oauth:grant-type:device_code 和 urn:ietf:params:oauth:grant-type:token-exchange。
	private Set<AuthorizationGrantType> authorizationGrantTypes;
	// redirectUris：客户端在基于重定向的流程中可使用的已注册重定向 URI，例如 authorization_code 授权模式。  
	private Set<String> redirectUris;
	// postLogoutRedirectUris：客户端在登出后可使用的重定向 URI。 
	private Set<String> postLogoutRedirectUris;
	// scopes：客户端被允许申请的 scope 列表。
	private Set<String> scopes;
	// clientSettings：客户端的自定义配置项，例如是否要求 PKCE、是否要求授权同意等。
	private ClientSettings clientSettings;
	// tokenSettings：为该客户端签发的 OAuth2 令牌的自定义配置项，例如访问令牌/刷新令牌的存活时间、是否复用刷新令牌等。
	private TokenSettings tokenSettings;

	...

}
```

## RegisteredClientRepository

RegisteredClientRepository 是用于注册新客户端和查询已有客户端的核心组件。其他组件在执行特定协议流程时（例如客户端认证、授权码处理、令牌自省、动态客户端注册等）都会使用到它。

目前提供的 RegisteredClientRepository 实现有 InMemoryRegisteredClientRepository 和 JdbcRegisteredClientRepository。  
InMemoryRegisteredClientRepository 会将 RegisteredClient 实例存储在内存中，**只建议在开发和测试阶段使用**。  
JdbcRegisteredClientRepository 是基于 JDBC 的实现，通过 JdbcOperations 将 RegisteredClient 实例持久化到数据库中。

!!! note

    RegisteredClientRepository 是一个必需的组件。

下面的示例演示了如何注册一个 `RegisteredClientRepository` 的 @Bean：

``` java
@Bean
public RegisteredClientRepository registeredClientRepository() {
	List<RegisteredClient> registrations = ...
	return new InMemoryRegisteredClientRepository(registrations);
}
```

或者，你也可以通过 `OAuth2AuthorizationServerConfigurer` 来配置 `RegisteredClientRepository`：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.registeredClientRepository(registeredClientRepository)
		)
	    ...

	return http.build();
}
```

!!! note

    当需要同时应用多项配置选项时，`OAuth2AuthorizationServerConfigurer` 会非常有用。

## OAuth2Authorization

OAuth2Authorization 用于表示一次 OAuth2 授权，它保存与授权相关的状态信息。该授权是由资源所有者授予给客户端的，或者在
client_credentials 授权类型的情况下，由客户端自行获取的授权。

!!! tip

    在 Spring Security 的 OAuth2 Client 支持中，与之对应的授权模型是 `OAuth2AuthorizedClient`。

在授权流程成功完成后，会创建一个 `OAuth2Authorization` 实例，并将一个 `OAuth2AccessToken`、一个（可选的）`OAuth2RefreshToken`
，以及与所执行的授权类型相关的额外状态信息关联起来。与一个 `OAuth2Authorization` 关联的 `OAuth2Token` 实例会因授权类型不同而有所差异。

对于 OAuth2 的 `authorization_code` 授权模式，会关联一个 `OAuth2AuthorizationCode`、一个 `OAuth2AccessToken`，以及一个（可选的）
`OAuth2RefreshToken`。

对于 OpenID Connect 1.0 的 `authorization_code` 授权模式，会关联一个 `OAuth2AuthorizationCode`、一个 `OidcIdToken`、一个
`OAuth2AccessToken`，以及一个（可选的）`OAuth2RefreshToken`。

对于 OAuth2 的 `client_credentials` 授权模式，则只会关联一个 `OAuth2AccessToken`。

`OAuth2Authorization` 及其属性定义如下：

``` java
public class OAuth2Authorization implements Serializable {
	//id：唯一标识该 OAuth2Authorization 的 ID。 
	private String id;
	//registeredClientId：唯一标识 RegisteredClient 的 ID。 
	private String registeredClientId;
	//principalName：资源所有者（或客户端）的主体名称。  
	private String principalName;
	//authorizationGrantType：所使用的 AuthorizationGrantType。
	private AuthorizationGrantType authorizationGrantType;
	//authorizedScopes：为客户端授权的作用域（scope）集合。 
	private Set<String> authorizedScopes;
	//tokens：与已执行授权类型对应的 OAuth2Token 实例及其相关元数据。 
	private Map<Class<? extends OAuth2Token>, Token<?>> tokens;
	//attributes：与已执行授权类型相关的附加属性，例如已认证的 Principal、OAuth2AuthorizationRequest 等。
	private Map<String, Object> attributes;

	...

}
```

OAuth2Authorization 及其关联的 OAuth2Token 实例都有固定的生命周期。新签发的 OAuth2Token 处于激活状态，当它过期或被作废（撤销）后就会变为非激活状态。当所有关联的 OAuth2Token 实例都处于非激活状态时，对应的 OAuth2Authorization 也就（隐式地）处于非激活状态。每个 OAuth2Token 都封装在一个 OAuth2Authorization.Token 中，该类提供了 isExpired()、isInvalidated() 和 isActive() 等访问方法，用于判断令牌是否已过期、是否已失效以及当前是否处于激活状态。

OAuth2Authorization.Token 还提供了 getClaims() 方法，用于返回与该 OAuth2Token 相关联的声明（claims）（如果存在的话）。

## OAuth2AuthorizationService

OAuth2AuthorizationService 是用于存储新授权并查询已有授权的核心组件。其他组件在执行特定协议流程时都会用到它，例如客户端认证、授权码处理、令牌自省、令牌吊销、动态客户端注册等。

提供的 OAuth2AuthorizationService 实现包括 InMemoryOAuth2AuthorizationService 和 JdbcOAuth2AuthorizationService。  
InMemoryOAuth2AuthorizationService 会将 OAuth2Authorization 实例存储在内存中，**仅推荐在开发和测试阶段使用**。  
JdbcOAuth2AuthorizationService 是基于 JDBC 的实现，通过使用 JdbcOperations 将 OAuth2Authorization 实例持久化存储。

!!! note

    OAuth2AuthorizationService 是一个可选组件，默认实现为 InMemoryOAuth2AuthorizationService。

下面的示例展示了如何注册一个 OAuth2AuthorizationService 的 @Bean：

```java
@Bean
public OAuth2AuthorizationService authorizationService() {
	return new InMemoryOAuth2AuthorizationService();
}
```

或者，你也可以通过 OAuth2AuthorizationServerConfigurer 来配置 OAuth2AuthorizationService：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.authorizationService(authorizationService)
		)
	    ...

	return http.build();
}
```

!!! note

    当需要同时应用多种配置选项时，可以使用 `OAuth2AuthorizationServerConfigurer`。

## OAuth2AuthorizationConsent

OAuth2AuthorizationConsent 表示在 OAuth2 授权请求流程中产生的一次授权“同意”（决策）——例如 authorization_code 授权模式中，用于保存资源所有者授予客户端的权限（authorities）。

在为某个客户端授权访问时，资源所有者可以只同意客户端所请求权限中的一部分。典型的使用场景就是 authorization_code 授权流程：客户端提出所需的 scope（范围），而资源所有者则对这些请求的 scope 进行同意（或拒绝）。

当一次 OAuth2 授权请求流程完成后，会创建（或更新）一个 OAuth2AuthorizationConsent 实例，用来将最终授予的权限与对应的客户端和资源所有者关联起来。

OAuth2AuthorizationConsent 及其各个属性定义如下：

``` java
public final class OAuth2AuthorizationConsent implements Serializable {
	// registeredClientId：唯一标识 RegisteredClient 的 ID。  
	private final String registeredClientId;    
	// principalName：资源所有者的主体名称（principal name）。
	private final String principalName; 
	// authorities：资源所有者授予客户端的权限集合。  
	// 每一项权限可以代表作用域（scope）、声明（claim）、许可（permission）、角色（role）等。
	private final Set<GrantedAuthority> authorities;    

	...

}
```

## OAuth2AuthorizationConsentService

OAuth2AuthorizationConsentService 是用于存储新的授权同意信息以及查询已有授权同意信息的核心组件。它主要被用于实现 OAuth2 授权请求流程的组件中——例如 authorization_code 授权模式。

目前提供的 OAuth2AuthorizationConsentService 实现有 InMemoryOAuth2AuthorizationConsentService 和 JdbcOAuth2AuthorizationConsentService。  
InMemoryOAuth2AuthorizationConsentService 会将 OAuth2AuthorizationConsent 实例存储在内存中，仅推荐用于开发和测试环境。  
JdbcOAuth2AuthorizationConsentService 则是基于 JDBC 的实现，通过 JdbcOperations 将 OAuth2AuthorizationConsent 实例持久化到数据库中。

!!! note

    OAuth2AuthorizationConsentService 是一个可选组件，默认实现为 InMemoryOAuth2AuthorizationConsentService。

以下示例展示了如何注册一个 OAuth2AuthorizationConsentService 的 @Bean：

```java
@Bean
public OAuth2AuthorizationConsentService authorizationConsentService() {
	return new InMemoryOAuth2AuthorizationConsentService();
}
```

或者，你也可以通过 OAuth2AuthorizationServerConfigurer 来配置 OAuth2AuthorizationConsentService：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.authorizationConsentService(authorizationConsentService)
		)
	    ...

	return http.build();
}
```

!!! note

    当需要同时应用多个配置选项时，`OAuth2AuthorizationServerConfigurer` 会非常有用。

## OAuth2TokenContext

OAuth2TokenContext 是一个上下文对象，用于保存与 OAuth2Token 相关的信息，并被 OAuth2TokenGenerator 和 OAuth2TokenCustomizer 使用。  

OAuth2TokenContext 提供了以下访问方法：

``` java
public interface OAuth2TokenContext extends Context {

	//getRegisteredClient()：与授权许可关联的 RegisteredClient 对象。
	default RegisteredClient getRegisteredClient() ...

	//getPrincipal()：资源所有者（或客户端）的 Authentication 实例。  
	default <T extends Authentication> T getPrincipal() ...

	//getAuthorizationServerContext()：保存授权服务器运行时环境信息的 AuthorizationServerContext 对象。
	default AuthorizationServerContext getAuthorizationServerContext() ...

	//getAuthorization()：与授权许可关联的 OAuth2Authorization 对象。  
	@Nullable
	default OAuth2Authorization getAuthorization() ...

	//getAuthorizedScopes()：为客户端授权的作用域（scope）集合。 
	default Set<String> getAuthorizedScopes() ...

	//getTokenType()：要生成的 OAuth2TokenType。支持的取值包括 code、access_token、refresh_token 和 id_token。
	default OAuth2TokenType getTokenType() ...

	//getAuthorizationGrantType()：与授权许可关联的 AuthorizationGrantType。  
	default AuthorizationGrantType getAuthorizationGrantType() ...

	//getAuthorizationGrant()：由处理该授权许可的 AuthenticationProvider 所使用的 Authentication 实例。
	default <T extends Authentication> T getAuthorizationGrant() ...

	...

}
```
 
## OAuth2TokenGenerator

OAuth2TokenGenerator 负责根据提供的 OAuth2TokenContext 中的信息生成一个 OAuth2Token。  
生成的 OAuth2Token 主要取决于 OAuth2TokenContext 中指定的 OAuth2TokenType 类型。  
例如，当 OAuth2TokenType 的取值为：

- `code` 时，会生成 `OAuth2AuthorizationCode`；
- `access_token` 时，会生成 `OAuth2AccessToken`；
- `refresh_token` 时，会生成 `OAuth2RefreshToken`；
- `id_token` 时，会生成 `OidcIdToken`。

此外，生成的 OAuth2AccessToken 的具体格式取决于为 RegisteredClient 配置的 `TokenSettings.getAccessTokenFormat()`。  
如果格式为 `OAuth2TokenFormat.SELF_CONTAINED`（默认值），则会生成一个 `Jwt`；  
如果格式为 `OAuth2TokenFormat.REFERENCE`，则会生成一个“不透明”（opaque）令牌。

最后，如果生成的 OAuth2Token 包含一组 claims 并实现了 `ClaimAccessor` 接口，则这些 claims 可以通过 `OAuth2Authorization.Token.getClaims()` 访问。

OAuth2TokenGenerator 主要由实现授权码处理流程的组件使用，例如 `authorization_code`、`client_credentials` 和 `refresh_token` 等授权模式。

当前提供的实现包括：`OAuth2AccessTokenGenerator`、`OAuth2RefreshTokenGenerator` 和 `JwtGenerator`。  
其中，`OAuth2AccessTokenGenerator` 会生成“不透明”（`OAuth2TokenFormat.REFERENCE`）的访问令牌，而 `JwtGenerator` 会生成 `Jwt`（`OAuth2TokenFormat.SELF_CONTAINED`）访问令牌。

!!! note

	OAuth2TokenGenerator 是一个可选组件，默认情况下为 DelegatingOAuth2TokenGenerator，由 OAuth2AccessTokenGenerator 和 OAuth2RefreshTokenGenerator 组合而成。

!!! note

    如果注册了一个 JwtEncoder 的 @Bean 或者一个 JWKSource<SecurityContext> 的 @Bean，那么 DelegatingOAuth2TokenGenerator 中还会额外组合（加入）一个 JwtGenerator。

OAuth2TokenGenerator 提供了极大的灵活性，因为它可以支持任意自定义格式的 access_token 和 refresh_token。

下面的示例展示了如何注册一个 OAuth2TokenGenerator 的 @Bean：

``` java
@Bean
public OAuth2TokenGenerator<?> tokenGenerator() {
	JwtEncoder jwtEncoder = ...
	JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
	OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
	OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
	return new DelegatingOAuth2TokenGenerator(
			jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
}
```

或者，你也可以通过 `OAuth2AuthorizationServerConfigurer` 来配置 `OAuth2TokenGenerator`：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.tokenGenerator(tokenGenerator)
		)
	    ...

	return http.build();
}
```

!!! note

    当需要同时应用多种配置选项时，OAuth2AuthorizationServerConfigurer 会非常有用。

## OAuth2TokenCustomizer

OAuth2TokenCustomizer 提供了自定义 OAuth2Token 属性的能力，这些属性可以通过提供的 OAuth2TokenContext 进行访问。它通常由 OAuth2TokenGenerator 使用，用来自定义 OAuth2Token 在真正生成之前的各类属性。

使用泛型类型为 OAuth2TokenClaimsContext（实现自 OAuth2TokenContext）的 OAuth2TokenCustomizer<OAuth2TokenClaimsContext>，可以对“非透明”（opaque）的 OAuth2AccessToken 的 claims 进行自定义。通过 OAuth2TokenClaimsContext.getClaims() 可以获取到 OAuth2TokenClaimsSet.Builder，从而支持添加、替换或删除 claims。

下面的示例展示了如何实现一个 OAuth2TokenCustomizer<OAuth2TokenClaimsContext>，并将其配置到 OAuth2AccessTokenGenerator 中：

``` java
@Bean
public OAuth2TokenGenerator<?> tokenGenerator() {
	JwtEncoder jwtEncoder = ...
	JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
	OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
	accessTokenGenerator.setAccessTokenCustomizer(accessTokenCustomizer());
	OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
	return new DelegatingOAuth2TokenGenerator(
			jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
}

@Bean
public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> accessTokenCustomizer() {
	return context -> {
		OAuth2TokenClaimsSet.Builder claims = context.getClaims();
		// Customize claims

	};
}
```

!!! note

    如果未将 OAuth2TokenGenerator 作为 @Bean 提供，或未通过 OAuth2AuthorizationServerConfigurer 进行配置，系统会自动配置一个 OAuth2TokenCustomizer<OAuth2TokenClaimsContext> @Bean，并使用 OAuth2AccessTokenGenerator。

声明为泛型类型 `JwtEncodingContext`（实现了 `OAuth2TokenContext`）的 `OAuth2TokenCustomizer<JwtEncodingContext>`，可以用来自定义 `Jwt` 的 Header 和 Claim。`JwtEncodingContext.getJwsHeader()` 可访问 `JwsHeader.Builder`，从而支持新增、替换和移除 Header；`JwtEncodingContext.getClaims()` 可访问 `JwtClaimsSet.Builder`，从而支持新增、替换和移除 Claim。

下面的示例演示了如何实现一个 `OAuth2TokenCustomizer<JwtEncodingContext>`，并将其配置到 `JwtGenerator` 中：

``` java
@Bean
public OAuth2TokenGenerator<?> tokenGenerator() {
	JwtEncoder jwtEncoder = ...
	JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
	jwtGenerator.setJwtCustomizer(jwtCustomizer());
	OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
	OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
	return new DelegatingOAuth2TokenGenerator(
			jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
}

@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
	return context -> {
		JwsHeader.Builder headers = context.getJwsHeader();
		JwtClaimsSet.Builder claims = context.getClaims();
		if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
			// Customize headers/claims for access_token

		} else if (context.getTokenType().getValue().equals(OidcParameterNames.ID_TOKEN)) {
			// Customize headers/claims for id_token

		}
	};
}
```

!!! note

    如果未将 OAuth2TokenGenerator 以 @Bean 方式提供，或未通过 OAuth2AuthorizationServerConfigurer 进行配置，系统会自动配置一个 OAuth2TokenCustomizer<JwtEncodingContext> 的 @Bean，并使用 JwtGenerator。

!!! tip

    关于如何自定义 ID Token 的示例，请参阅指南[《操作指南：自定义 OpenID Connect 1.0 UserInfo 响应》](/document-translation/spring-authorization-server-153/07-how-to-guides/05-customize-the-oidc-userinfo-response/)。

## SessionRegistry

如果启用了 OpenID Connect 1.0，就会使用一个 `SessionRegistry` 实例来跟踪已认证的会话。与 OAuth2 授权端点关联的默认 `SessionAuthenticationStrategy` 实现会通过 `SessionRegistry` 注册新创建的已认证会话。

!!! note

    如果未注册 `SessionRegistry` 的 `@Bean`，将使用默认实现 `SessionRegistryImpl`。

!!! warning

    如果注册了一个 SessionRegistry 的 @Bean，且它是 SessionRegistryImpl 的实例，那么也应该注册一个 HttpSessionEventPublisher 的 @Bean，因为它负责将会话生命周期事件（例如 SessionDestroyedEvent）通知给 SessionRegistryImpl，从而能够移除对应的 SessionInformation 实例。

当终端用户发起注销请求时，OpenID Connect 1.0 的注销端点会通过 SessionRegistry 查找与已认证终端用户关联的 SessionInformation，并据此执行注销操作。  

如果启用了 Spring Security 的并发会话控制（Concurrent Session Control）功能，建议注册一个 SessionRegistry @Bean，确保它能在 Spring Security 的并发会话控制与 Spring Authorization Server 的注销功能之间共享。  

下面的示例演示了如何注册 SessionRegistry @Bean 以及 HttpSessionEventPublisher @Bean（SessionRegistryImpl 所必需）：

```java
@Bean
public SessionRegistry sessionRegistry() {
	return new SessionRegistryImpl();
}

@Bean
public HttpSessionEventPublisher httpSessionEventPublisher() {
	return new HttpSessionEventPublisher();
}
```
