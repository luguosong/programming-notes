# 协议端点

## OAuth2 授权端点

OAuth2AuthorizationEndpointConfigurer 用于自定义 OAuth2 授权端点。它提供了一系列扩展点，允许你对 OAuth2
授权请求的预处理、核心处理以及后处理逻辑进行定制。

OAuth2AuthorizationEndpointConfigurer 提供以下配置选项：

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.authorizationEndpoint(authorizationEndpoint ->
									authorizationEndpoint
											//	authorizationRequestConverter()：添加一个 AuthenticationConverter（预处理器），
											//	用于在尝试从 HttpServletRequest 中提取 OAuth2 授权请求（或同意信息）时，
											//	将其转换为 OAuth2AuthorizationCodeRequestAuthenticationToken 
											//	或 OAuth2AuthorizationConsentAuthenticationToken 的实例。
											.authorizationRequestConverter(authorizationRequestConverter)
											//	authorizationRequestConverters()：设置一个 Consumer，
											//	用于访问默认以及（可选）新增的 AuthenticationConverter 列表，
											//	从而可以添加、移除或自定义某个特定的 AuthenticationConverter。
											.authorizationRequestConverters(authorizationRequestConvertersConsumer)
											//	authenticationProvider()：添加一个 AuthenticationProvider（主处理器），
											//	用于对 OAuth2AuthorizationCodeRequestAuthenticationToken 
											//	或 OAuth2AuthorizationConsentAuthenticationToken 进行认证。
											.authenticationProvider(authenticationProvider)
											//	authenticationProviders()：设置一个 Consumer，
											//	用于访问默认以及（可选）新增的 AuthenticationProvider 列表，
											//	从而可以添加、移除或自定义某个特定的 AuthenticationProvider。
											.authenticationProviders(authenticationProvidersConsumer)
											//	authorizationResponseHandler()：用于处理“已认证”的 OAuth2AuthorizationCodeRequestAuthenticationToken 
											//	并返回 OAuth2AuthorizationResponse 的 AuthenticationSuccessHandler（后处理器）。
											.authorizationResponseHandler(authorizationResponseHandler)
											//	errorResponseHandler()：用于处理 OAuth2AuthorizationCodeRequestAuthenticationException 
											//	并返回 OAuth2Error 响应的 AuthenticationFailureHandler（后处理器）。
											.errorResponseHandler(errorResponseHandler)
											//	consentPage()：在授权请求流程中若需要用户同意（consent），将资源所有者重定向到的自定义同意页 URI。
											.consentPage("/oauth2/v1/authorize")
							)
			);

	return http.build();
}
```

OAuth2AuthorizationEndpointConfigurer 会配置 OAuth2AuthorizationEndpointFilter，并将其注册到 OAuth2 授权服务器的
SecurityFilterChain @Bean 中。OAuth2AuthorizationEndpointFilter 是用于处理 OAuth2 授权请求（以及用户同意）的过滤器。

OAuth2AuthorizationEndpointFilter 默认包含以下配置：

- AuthenticationConverter——一个 DelegatingAuthenticationConverter，由
  OAuth2AuthorizationCodeRequestAuthenticationConverter 和 OAuth2AuthorizationConsentAuthenticationConverter 组成。
- AuthenticationManager——一个 AuthenticationManager，由 OAuth2AuthorizationCodeRequestAuthenticationProvider 和
  OAuth2AuthorizationConsentAuthenticationProvider 组成。
- AuthenticationSuccessHandler——内部实现，用于处理“已认证”的 OAuth2AuthorizationCodeRequestAuthenticationToken，并返回
  OAuth2AuthorizationResponse。
- AuthenticationFailureHandler——内部实现，使用与 OAuth2AuthorizationCodeRequestAuthenticationException 关联的
  OAuth2Error，并返回 OAuth2Error 响应。

### 自定义授权请求验证

OAuth2AuthorizationCodeRequestAuthenticationValidator 是用于校验授权码（Authorization Code Grant）中特定 OAuth2
授权请求参数的默认校验器。默认实现会校验 redirect_uri 和 scope 参数；如果校验失败，将抛出
OAuth2AuthorizationCodeRequestAuthenticationException。

OAuth2AuthorizationCodeRequestAuthenticationProvider 支持通过提供自定义的授权请求校验器来覆盖默认校验逻辑：将一个类型为
Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> 的校验器传入 setAuthenticationValidator()。

!!! note

    OAuth2AuthorizationCodeRequestAuthenticationContext 持有 OAuth2AuthorizationCodeRequestAuthenticationToken，而该 Token 包含 OAuth2 授权请求参数。

!!! warning

    如果校验失败，认证校验器必须抛出 OAuth2AuthorizationCodeRequestAuthenticationException。

在开发生命周期阶段，一个常见需求是允许在 redirect_uri 参数中使用 localhost。

下面的示例展示了如何为 OAuth2AuthorizationCodeRequestAuthenticationProvider 配置一个自定义的认证校验器，以允许
redirect_uri 参数中包含 localhost：

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.authorizationEndpoint(authorizationEndpoint ->
									authorizationEndpoint
											.authenticationProviders(configureAuthenticationValidator())
							)
			);

	return http.build();
}

private Consumer<List<AuthenticationProvider>> configureAuthenticationValidator() {
	return (authenticationProviders) ->
			authenticationProviders.forEach((authenticationProvider) -> {
				if (authenticationProvider instanceof OAuth2AuthorizationCodeRequestAuthenticationProvider) {
					Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> authenticationValidator =
							// Override default redirect_uri validator
							new CustomRedirectUriValidator()
									// Reuse default scope validator
									.andThen(OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR);

					((OAuth2AuthorizationCodeRequestAuthenticationProvider) authenticationProvider)
							.setAuthenticationValidator(authenticationValidator);
				}
			});
}

static class CustomRedirectUriValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

	@Override
	public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext authenticationContext) {
		OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
				authenticationContext.getAuthentication();
		RegisteredClient registeredClient = authenticationContext.getRegisteredClient();
		String requestedRedirectUri = authorizationCodeRequestAuthentication.getRedirectUri();

		// Use exact string matching when comparing client redirect URIs against pre-registered URIs
		if (!registeredClient.getRedirectUris().contains(requestedRedirectUri)) {
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
			throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
		}
	}
}
```

## OAuth2推送式授权请求端点

OAuth2PushedAuthorizationRequestEndpointConfigurer 用于自定义 OAuth2 Pushed Authorization Request（PAR）端点。它提供了一系列扩展点，允许你对
OAuth2 PAR 请求的预处理、核心处理以及后处理逻辑进行定制。

OAuth2PushedAuthorizationRequestEndpointConfigurer 提供以下配置选项：

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.pushedAuthorizationRequestEndpoint(pushedAuthorizationRequestEndpoint ->
									pushedAuthorizationRequestEndpoint
											//	pushedAuthorizationRequestConverter()：添加一个 AuthenticationConverter（前置处理器），用于在尝试从 HttpServletRequest 中提取 OAuth2 推送授权请求（PAR）并将其转换为 OAuth2PushedAuthorizationRequestAuthenticationToken 实例时使用。
											.pushedAuthorizationRequestConverter(pushedAuthorizationRequestConverter)
											//	pushedAuthorizationRequestConverters()：设置一个 Consumer，用于访问默认的以及（可选）额外添加的 AuthenticationConverter 列表，从而可以新增、移除或自定义某个特定的 AuthenticationConverter。
											.pushedAuthorizationRequestConverters(pushedAuthorizationRequestConvertersConsumer)
											//	authenticationProvider()：添加一个 AuthenticationProvider（主处理器），用于对 OAuth2PushedAuthorizationRequestAuthenticationToken 进行认证。
											.authenticationProvider(authenticationProvider)
											//	authenticationProviders()：设置一个 Consumer，用于访问默认的以及（可选）额外添加的 AuthenticationProvider 列表，从而可以新增、移除或自定义某个特定的 AuthenticationProvider。
											.authenticationProviders(authenticationProvidersConsumer)
											//	pushedAuthorizationResponseHandler()：用于处理“已认证”的 OAuth2PushedAuthorizationRequestAuthenticationToken 并返回 OAuth2 推送授权响应的 AuthenticationSuccessHandler（后置处理器）。
											.pushedAuthorizationResponseHandler(pushedAuthorizationResponseHandler)
											//	errorResponseHandler()：用于处理 OAuth2AuthenticationException 并返回 OAuth2Error 响应的 AuthenticationFailureHandler（后置处理器）。
											.errorResponseHandler(errorResponseHandler)
							)
			);

	return http.build();
}
```

OAuth2PushedAuthorizationRequestEndpointConfigurer 用于配置 OAuth2PushedAuthorizationRequestEndpointFilter，并将其注册到
OAuth2 授权服务器的 SecurityFilterChain @Bean 中。OAuth2PushedAuthorizationRequestEndpointFilter 是用于处理 OAuth2
推送授权请求（pushed authorization requests）的 Filter。

OAuth2PushedAuthorizationRequestEndpointFilter 默认配置如下：

- AuthenticationConverter——一个 DelegatingAuthenticationConverter，由
  OAuth2AuthorizationCodeRequestAuthenticationConverter 组成。
- AuthenticationManager——一个 AuthenticationManager，由 OAuth2PushedAuthorizationRequestAuthenticationProvider 组成。
- AuthenticationSuccessHandler——内部实现，用于处理处于“已认证（authenticated）”状态的
  OAuth2PushedAuthorizationRequestAuthenticationToken，并返回 OAuth2 推送授权响应。
- AuthenticationFailureHandler——OAuth2ErrorAuthenticationFailureHandler。

### 自定义推送授权请求验证

OAuth2AuthorizationCodeRequestAuthenticationValidator 是默认的校验器，用于校验授权码模式（Authorization Code Grant）中
OAuth2 推送授权请求（PAR）的特定参数。默认实现会校验 redirect_uri 和 scope 参数；如果校验失败，将抛出
OAuth2AuthorizationCodeRequestAuthenticationException。

OAuth2PushedAuthorizationRequestAuthenticationProvider 支持通过 setAuthenticationValidator() 注入一个自定义的认证校验器（类型为
Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext>），以覆盖默认的推送授权请求校验逻辑。

!!! note

    OAuth2AuthorizationCodeRequestAuthenticationContext 持有 OAuth2AuthorizationCodeRequestAuthenticationToken，后者包含 OAuth2 推送式授权请求（PAR）的参数。

!!! warning

    如果校验失败，认证校验器必须抛出 OAuth2AuthorizationCodeRequestAuthenticationException。

在开发生命周期阶段，一个常见的使用场景是允许在 `redirect_uri` 参数中使用 `localhost`。  
下面的示例展示了如何为 `OAuth2PushedAuthorizationRequestAuthenticationProvider` 配置一个自定义认证校验器，以允许在
`redirect_uri` 参数中使用 `localhost`：

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.pushedAuthorizationRequestEndpoint(pushedAuthorizationRequestEndpoint ->
									pushedAuthorizationRequestEndpoint
											.authenticationProviders(configureAuthenticationValidator())
							)
			);

	return http.build();
}

private Consumer<List<AuthenticationProvider>> configureAuthenticationValidator() {
	return (authenticationProviders) ->
			authenticationProviders.forEach((authenticationProvider) -> {
				if (authenticationProvider instanceof OAuth2PushedAuthorizationRequestAuthenticationProvider) {
					Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> authenticationValidator =
							// 重写默认的 redirect_uri 校验器
							new CustomRedirectUriValidator()
									// 复用默认作用域校验器
									.andThen(OAuth2AuthorizationCodeRequestAuthenticationValidator.DEFAULT_SCOPE_VALIDATOR);

					((OAuth2PushedAuthorizationRequestAuthenticationProvider) authenticationProvider)
							.setAuthenticationValidator(authenticationValidator);
				}
			});
}

static class CustomRedirectUriValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

	@Override
	public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext authenticationContext) {
		OAuth2AuthorizationCodeRequestAuthenticationToken authorizationCodeRequestAuthentication =
				authenticationContext.getAuthentication();
		RegisteredClient registeredClient = authenticationContext.getRegisteredClient();
		String requestedRedirectUri = authorizationCodeRequestAuthentication.getRedirectUri();

		// 在将客户端重定向 URI 与预先注册的 URI 进行比较时，请使用精确字符串匹配。
		if (!registeredClient.getRedirectUris().contains(requestedRedirectUri)) {
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
			throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
		}
	}
}
```

## OAuth2设备授权端点

OAuth2DeviceAuthorizationEndpointConfigurer 提供了自定义 OAuth2 设备授权端点的能力。它定义了一些扩展点，使你可以定制
OAuth2 设备授权请求在预处理、核心处理以及后处理阶段的逻辑。

OAuth2DeviceAuthorizationEndpointConfigurer 提供以下配置选项。

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.deviceAuthorizationEndpoint(deviceAuthorizationEndpoint ->
									deviceAuthorizationEndpoint
											//	deviceAuthorizationRequestConverter()：添加一个 AuthenticationConverter（前置处理器），
											//	用于尝试从 HttpServletRequest 中提取 OAuth2 设备授权请求，
											//	并将其转换为 OAuth2DeviceAuthorizationRequestAuthenticationToken 实例。
											.deviceAuthorizationRequestConverter(deviceAuthorizationRequestConverter)
											//	deviceAuthorizationRequestConverters()：设置一个 Consumer，
											//	用于访问默认以及（可选）额外添加的 AuthenticationConverter 列表，
											//	从而可以添加、移除或自定义某个特定的 AuthenticationConverter。
											.deviceAuthorizationRequestConverters(deviceAuthorizationRequestConvertersConsumer)
											//	authenticationProvider()：添加一个 AuthenticationProvider（主处理器），
											//	用于对 OAuth2DeviceAuthorizationRequestAuthenticationToken 进行认证。
											.authenticationProvider(authenticationProvider)
											//	authenticationProviders()：设置一个 Consumer，
											//	用于访问默认以及（可选）额外添加的 AuthenticationProvider 列表，
											//	从而可以添加、移除或自定义某个特定的 AuthenticationProvider。
											.authenticationProviders(authenticationProvidersConsumer)
											//	deviceAuthorizationResponseHandler()：
											//	用于处理“已认证”的 OAuth2DeviceAuthorizationRequestAuthenticationToken 
											//	并返回 OAuth2DeviceAuthorizationResponse 的 AuthenticationSuccessHandler（后置处理器）。
											.deviceAuthorizationResponseHandler(deviceAuthorizationResponseHandler)
											//	errorResponseHandler()：用于处理 OAuth2AuthenticationException 
											//	并返回 OAuth2Error 响应的 AuthenticationFailureHandler（后置处理器）。
											.errorResponseHandler(errorResponseHandler)
											//	verificationUri()：自定义终端用户验证页面的 URI，
											//	用于在第二设备上引导资源所有者前往该页面。
											.verificationUri("/oauth2/v1/device_verification")
							)
			);

	return http.build();
}
```

OAuth2DeviceAuthorizationEndpointConfigurer 用于配置 OAuth2DeviceAuthorizationEndpointFilter，并将其注册到 OAuth2 授权服务器的
SecurityFilterChain @Bean 中。OAuth2DeviceAuthorizationEndpointFilter 是用于处理 OAuth2 设备授权请求的 Filter。

OAuth2DeviceAuthorizationEndpointFilter 默认配置如下：

- AuthenticationConverter —— OAuth2DeviceAuthorizationRequestAuthenticationConverter。
- AuthenticationManager —— 由 OAuth2DeviceAuthorizationRequestAuthenticationProvider 组成的 AuthenticationManager。
- AuthenticationSuccessHandler —— 内部实现：负责处理处于“已认证”状态的
  OAuth2DeviceAuthorizationRequestAuthenticationToken，并返回 OAuth2DeviceAuthorizationResponse。
- AuthenticationFailureHandler —— OAuth2ErrorAuthenticationFailureHandler。

## OAuth2设备验证端点

OAuth2DeviceVerificationEndpointConfigurer 支持自定义 OAuth2 设备验证端点（即“用户交互”端点）。它提供了一系列扩展点，允许你针对
OAuth2 设备验证请求定制预处理、核心处理以及后处理逻辑。

OAuth2DeviceVerificationEndpointConfigurer 提供以下配置选项：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.deviceVerificationEndpoint(deviceVerificationEndpoint ->
                    deviceVerificationEndpoint
						//	deviceVerificationRequestConverter()：
						//	添加一个 AuthenticationConverter（预处理器），
						//	用于在尝试从 HttpServletRequest 中提取 OAuth2 设备验证请求（或同意请求）时，
						//	将其转换为 OAuth2DeviceVerificationAuthenticationToken 
						//	或 OAuth2DeviceAuthorizationConsentAuthenticationToken 实例。
                        .deviceVerificationRequestConverter(deviceVerificationRequestConverter)
						//	deviceVerificationRequestConverters()：
						//	设置一个 Consumer，用于访问默认及（可选）新增的 AuthenticationConverter 列表，
						//	从而支持添加、移除或自定义某个特定的 AuthenticationConverter。
                        .deviceVerificationRequestConverters(deviceVerificationRequestConvertersConsumer)
						//	authenticationProvider()：添加一个 AuthenticationProvider（主处理器），
						//	用于对 OAuth2DeviceVerificationAuthenticationToken 
						//	或 OAuth2DeviceAuthorizationConsentAuthenticationToken 进行认证。
                        .authenticationProvider(authenticationProvider)
						//	authenticationProviders()：设置一个 Consumer，
						//	用于访问默认及（可选）新增的 AuthenticationProvider 列表，
						//	从而支持添加、移除或自定义某个特定的 AuthenticationProvider。
                        .authenticationProviders(authenticationProvidersConsumer)
						//	deviceVerificationResponseHandler()：
						//	用于处理“已认证”的 OAuth2DeviceVerificationAuthenticationToken 
						//	的 AuthenticationSuccessHandler（后处理器），并引导资源所有者返回其设备。
                        .deviceVerificationResponseHandler(deviceVerificationResponseHandler)
						//	errorResponseHandler()：用于处理 OAuth2AuthenticationException 
						//	并返回错误响应的 AuthenticationFailureHandler（后处理器）。
                        .errorResponseHandler(errorResponseHandler)
						//	consentPage()：自定义同意页的 URI；如果在设备验证请求流程中需要授权同意，
						//	将把资源所有者重定向到该页面。
                        .consentPage("/oauth2/v1/consent")
				)
		);

	return http.build();
}
```

OAuth2DeviceVerificationEndpointConfigurer 用于配置 OAuth2DeviceVerificationEndpointFilter，并将其注册到 OAuth2 授权服务器的
SecurityFilterChain @Bean 中。OAuth2DeviceVerificationEndpointFilter 是用于处理 OAuth2 设备验证请求（以及用户同意授权）的过滤器。

OAuth2DeviceVerificationEndpointFilter 默认配置如下：

- AuthenticationConverter——一个 DelegatingAuthenticationConverter，由 OAuth2DeviceVerificationAuthenticationConverter 和
  OAuth2DeviceAuthorizationConsentAuthenticationConverter 组成。
- AuthenticationManager——一个 AuthenticationManager，由 OAuth2DeviceVerificationAuthenticationProvider 和
  OAuth2DeviceAuthorizationConsentAuthenticationProvider 组成。
- AuthenticationSuccessHandler——一个 SimpleUrlAuthenticationSuccessHandler，用于处理“已认证”的
  OAuth2DeviceVerificationAuthenticationToken，并将用户重定向到成功页面（/?success）。
- AuthenticationFailureHandler——一个内部实现，使用与 OAuth2AuthenticationException 关联的 OAuth2Error，并返回 OAuth2Error
  响应。

## OAuth2令牌端点

OAuth2TokenEndpointConfigurer 用于自定义 OAuth2 Token 端点。它提供了一系列扩展点，允许你针对 OAuth2
访问令牌请求的前置处理、核心处理以及后置处理逻辑进行定制。

OAuth2TokenEndpointConfigurer 提供以下配置选项：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.tokenEndpoint(tokenEndpoint ->
                    tokenEndpoint
						//	accessTokenRequestConverter()：
						//	添加一个 AuthenticationConverter（预处理器），用于在尝试从 HttpServletRequest 中提取 OAuth2 访问令牌请求，
						//	并将其转换为 OAuth2AuthorizationGrantAuthenticationToken 实例时使用。
                        .accessTokenRequestConverter(accessTokenRequestConverter)
						//	accessTokenRequestConverters()：
						//	设置一个 Consumer，用于访问默认以及（可选）新增的 AuthenticationConverter 列表，
						//	从而可以添加、移除或自定义某个特定的 AuthenticationConverter。
                        .accessTokenRequestConverters(accessTokenRequestConvertersConsumer)
						//	authenticationProvider()：
						//	添加一个 AuthenticationProvider（主处理器），
						//	用于对 OAuth2AuthorizationGrantAuthenticationToken 进行认证。
                        .authenticationProvider(authenticationProvider)
						//	authenticationProviders()：
						//	设置一个 Consumer，用于访问默认以及（可选）新增的 AuthenticationProvider 列表，
						//	从而可以添加、移除或自定义某个特定的 AuthenticationProvider。
                        .authenticationProviders(authenticationProvidersConsumer)
						//	accessTokenResponseHandler()：
						//	用于处理 OAuth2AccessTokenAuthenticationToken 
						//	并返回 OAuth2AccessTokenResponse 的 AuthenticationSuccessHandler（后处理器）。
                        .accessTokenResponseHandler(accessTokenResponseHandler)
						//	errorResponseHandler()：
						//	用于处理 OAuth2AuthenticationException 
						//	并返回 OAuth2Error 响应的 AuthenticationFailureHandler（后处理器）。
                        .errorResponseHandler(errorResponseHandler)
				)
		);

	return http.build();
}
```

OAuth2TokenEndpointConfigurer 用于配置 OAuth2TokenEndpointFilter，并将其注册到 OAuth2 授权服务器的 SecurityFilterChain
`@Bean` 中。OAuth2TokenEndpointFilter 是用于处理 OAuth2 访问令牌请求的 Filter。

支持的授权类型包括：`authorization_code`、`refresh_token`、`client_credentials`、
`urn:ietf:params:oauth:grant-type:device_code` 以及 `urn:ietf:params:oauth:grant-type:token-exchange`。

OAuth2TokenEndpointFilter 默认配置如下：

- AuthenticationConverter —— 一个 DelegatingAuthenticationConverter，由
  OAuth2AuthorizationCodeAuthenticationConverter、OAuth2RefreshTokenAuthenticationConverter、OAuth2ClientCredentialsAuthenticationConverter、OAuth2DeviceCodeAuthenticationConverter
  和 OAuth2TokenExchangeAuthenticationConverter 组成。
- AuthenticationManager —— 一个 AuthenticationManager，由
  OAuth2AuthorizationCodeAuthenticationProvider、OAuth2RefreshTokenAuthenticationProvider、OAuth2ClientCredentialsAuthenticationProvider、OAuth2DeviceCodeAuthenticationProvider
  和 OAuth2TokenExchangeAuthenticationProvider 组成。
- AuthenticationSuccessHandler —— OAuth2AccessTokenResponseAuthenticationSuccessHandler。
- AuthenticationFailureHandler —— OAuth2ErrorAuthenticationFailureHandler。

### 自定义客户端凭据授予请求验证

OAuth2ClientCredentialsAuthenticationValidator 是用于校验 OAuth2 客户端凭证（Client Credentials）授权类型请求中特定参数的默认校验器。默认实现会校验
scope 参数；如果校验失败，会抛出 OAuth2AuthenticationException。

OAuth2ClientCredentialsAuthenticationProvider 支持通过 setAuthenticationValidator() 提供一个类型为
Consumer<OAuth2ClientCredentialsAuthenticationContext> 的自定义认证校验器，从而覆盖默认的请求校验逻辑。

!!! tip

    OAuth2ClientCredentialsAuthenticationContext 持有 OAuth2ClientCredentialsAuthenticationToken，而该 Token 中包含 OAuth2 客户端凭证（Client Credentials）授权请求的参数。

!!! warning

    如果验证失败，身份验证校验器`必须`抛出 `OAuth2AuthenticationException`。

下面的示例演示了如何为 `OAuth2ClientCredentialsAuthenticationProvider` 配置一个自定义的认证校验器，以覆盖默认的 Scope
校验逻辑：

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.tokenEndpoint(tokenEndpoint ->
									tokenEndpoint
											.authenticationProviders(configureAuthenticationValidator())
							)
			);

	return http.build();
}

private Consumer<List<AuthenticationProvider>> configureAuthenticationValidator() {
	return (authenticationProviders) ->
			authenticationProviders.forEach((authenticationProvider) -> {
				if (authenticationProvider instanceof OAuth2ClientCredentialsAuthenticationProvider) {
					Consumer<OAuth2ClientCredentialsAuthenticationContext> authenticationValidator =
							new CustomScopeValidator();

					// 覆盖默认的作用域校验
					((OAuth2ClientCredentialsAuthenticationProvider) authenticationProvider)
							.setAuthenticationValidator(authenticationValidator);
				}
			});
}

static class CustomScopeValidator implements Consumer<OAuth2ClientCredentialsAuthenticationContext> {

	@Override
	public void accept(OAuth2ClientCredentialsAuthenticationContext authenticationContext) {
		OAuth2ClientCredentialsAuthenticationToken clientCredentialsAuthentication =
				authenticationContext.getAuthentication();

		Set<String> requestedScopes = clientCredentialsAuthentication.getScopes();
		RegisteredClient registeredClient = authenticationContext.getRegisteredClient();
		Set<String> allowedScopes = registeredClient.getScopes();

		// TODO 实现作用域校验

	}
}
```

### 与DPoP绑定的访问令牌

RFC 9449《OAuth 2.0 证明持有（DPoP）》是一种应用层机制，用于对访问令牌进行发送方约束（sender-constraining）。

DPoP 的核心目标是防止未授权或不合法的客户端滥用泄露或被盗的访问令牌：授权服务器在签发访问令牌时，将其与某个公钥绑定，并要求客户端在资源服务器使用该访问令牌时，证明自己持有与该公钥对应的私钥。

通过 DPoP 实现发送方约束的访问令牌，与典型的 Bearer Token（持有者令牌）形成对比：Bearer Token 只要被任何客户端拿到就能使用。

DPoP 引入了 “DPoP Proof（DPoP 证明）” 的概念：它是由客户端创建的 JWT，并作为 HTTP 请求的一个 Header 发送。客户端通过 DPoP
证明来证明自己持有与某个公钥对应的私钥。

当客户端发起访问令牌请求时，会在 HTTP Header 中给该请求附带一个 DPoP 证明。授权服务器会将访问令牌与该 DPoP
证明中关联的公钥进行绑定（即发送方约束）。

当客户端发起受保护资源请求时，同样会在 HTTP Header 中再次附带一个 DPoP 证明。

资源服务器会获取与访问令牌绑定的公钥信息：要么直接从访问令牌（JWT）中取得，要么通过 OAuth 2.0 的 Token
Introspection（令牌自省）端点获取。随后，资源服务器会校验：绑定在访问令牌上的公钥是否与 DPoP 证明中的公钥一致；同时还会校验
DPoP 证明中的访问令牌哈希是否与请求中携带的访问令牌匹配。

DPoP 访问令牌请求

要使用 DPoP 请求一个与公钥绑定的访问令牌，客户端在向 OAuth 2.0 Token 端点发起访问令牌请求时，必须在 DPoP Header 中提供一个有效的
DPoP 证明。这适用于所有访问令牌请求，不受授权类型影响（例如 authorization_code、refresh_token、client_credentials 等）。

下面的 HTTP 请求示例展示了一个带有 DPoP Header（内含 DPoP 证明）的 authorization_code 访问令牌请求：

```shell
POST /oauth2/token HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded
DPoP: eyJraWQiOiJyc2EtandrLWtpZCIsInR5cCI6ImRwb3Arand0IiwiYWxnIjoiUlMyNTYiLCJqd2siOnsia3R5IjoiUlNBIiwiZSI6IkFRQUIiLCJraWQiOiJyc2EtandrLWtpZCIsIm4iOiIzRmxxSnI1VFJza0lRSWdkRTNEZDdEOWxib1dkY1RVVDhhLWZKUjdNQXZRbTdYWE5vWWttM3Y3TVFMMU5ZdER2TDJsOENBbmMwV2RTVElOVTZJUnZjNUtxbzJRNGNzTlg5U0hPbUVmem9ST2pRcWFoRWN2ZTFqQlhsdW9DWGRZdVlweDRfMXRmUmdHNmlpNFVoeGg2aUk4cU5NSlFYLWZMZnFoYmZZZnhCUVZSUHl3QmtBYklQNHgxRUFzYkM2RlNObWtoQ3hpTU5xRWd4YUlwWThDMmtKZEpfWklWLVdXNG5vRGR6cEtxSGN3bUI4RnNydW1sVllfRE5WdlVTRElpcGlxOVBiUDRIOTlUWE4xbzc0Nm9SYU5hMDdycTFob0NnTVNTeS04NVNhZ0NveGxteUUtRC1vZjlTc01ZOE9sOXQwcmR6cG9iQnVoeUpfbzVkZnZqS3cifX0.eyJodG0iOiJQT1NUIiwiaHR1IjoiaHR0cHM6Ly9zZXJ2ZXIuZXhhbXBsZS5jb20vb2F1dGgyL3Rva2VuIiwiaWF0IjoxNzQ2ODA2MzA1LCJqdGkiOiI0YjIzNDBkMi1hOTFmLTQwYTUtYmFhOS1kZDRlNWRlYWM4NjcifQ.wq8gJ_G6vpiEinfaY3WhereqCCLoeJOG8tnWBBAzRWx9F1KU5yAAWq-ZVCk_k07-h6DIqz2wgv6y9dVbNpRYwNwDUeik9qLRsC60M8YW7EFVyI3n_NpujLwzZeub_nDYMVnyn4ii0NaZrYHtoGXOlswQfS_-ET-jpC0XWm5nBZsCdUEXjOYtwaACC6Js-pyNwKmSLp5SKIk11jZUR5xIIopaQy521y9qJHhGRwzj8DQGsP7wMZ98UFL0E--1c-hh4rTy8PMeWCqRHdwjj_ry_eTe0DJFcxxYQdeL7-0_0CIO4Ayx5WHEpcUOIzBRoN32RsNpDZc-5slDNj9ku004DA

grant_type=authorization_code\
&client_id=s6BhdRkqt\
&code=SplxlOBeZQQYbYS6WxSbIA\
&redirect_uri=https%3A%2F%2Fclient%2Eexample%2Ecom%2Fcb\
&code_verifier=bEaL42izcC-o-xBk0K2vuJ6U-y1p9r_wW2dFWIWgjz-
```

以下展示了 DPoP Proof JWT 的 Header 和 Claims 的表示形式：

``` json
{
  "typ": "dpop+jwt",
  "alg": "RS256",
  "jwk": {
    "kty": "RSA",
    "e": "AQAB",
    "n": "3FlqJr5TRskIQIgdE3Dd7D9lboWdcTUT8a-fJR7MAvQm7XXNoYkm3v7MQL1NYtDvL2l8CAnc0WdSTINU6IRvc5Kqo2Q4csNX9SHOmEfzoROjQqahEcve1jBXluoCXdYuYpx4_1tfRgG6ii4Uhxh6iI8qNMJQX-fLfqhbfYfxBQVRPywBkAbIP4x1EAsbC6FSNmkhCxiMNqEgxaIpY8C2kJdJ_ZIV-WW4noDdzpKqHcwmB8FsrumlVY_DNVvUSDIipiq9PbP4H99TXN1o746oRaNa07rq1hoCgMSSy-85SagCoxlmyE-D-of9SsMY8Ol9t0rdzpobBuhyJ_o5dfvjKw"
  }
}
```

```json
{
  "htm": "POST",
  "htu": "https://server.example.com/oauth2/token",
  "iat": 1746806305,
  "jti": "4b2340d2-a91f-40a5-baa9-dd4e5deac867"
}
```

以下代码展示了一个如何生成 DPoP Proof JWT 的示例：

``` java
RSAKey rsaKey = ...
JWKSource<SecurityContext> jwkSource = (jwkSelector, securityContext) -> jwkSelector
		.select(new JWKSet(rsaKey));
NimbusJwtEncoder jwtEncoder = new NimbusJwtEncoder(jwkSource);

JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256)
		.type("dpop+jwt")
		.jwk(rsaKey.toPublicJWK().toJSONObject())
		.build();
JwtClaimsSet claims = JwtClaimsSet.builder()
		.issuedAt(Instant.now())
		.claim("htm", "POST")
		.claim("htu", "https://server.example.com/oauth2/token")
		.id(UUID.randomUUID().toString())
		.build();

Jwt dPoPProof = jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims));
```

在授权服务器成功校验 DPoP 证明后，会将 DPoP 证明中的公钥与签发的访问令牌进行绑定（发送方约束，sender-constrained）。

下面的访问令牌响应将 token_type 参数设置为 DPoP，用于告知客户端：该访问令牌已绑定到其 DPoP 证明的公钥：

``` shell
HTTP/1.1 200 OK
Content-Type: application/json
Cache-Control: no-store

{
 "access_token": "Kz~8mXK1EalYznwH-LC-1fBAo.4Ljp~zsPE_NeO.gxU",
 "token_type": "DPoP",
 "expires_in": 2677
}
```

#### 公钥确认

资源服务器必须能够识别访问令牌是否与 DPoP 绑定，并验证其与 DPoP
证明所对应公钥的绑定关系。该绑定通过将公钥与访问令牌关联来实现，且资源服务器需要能够访问到这种关联信息，例如：直接在访问令牌（JWT）中嵌入公钥哈希，或通过令牌内省（token
introspection）获取。

当访问令牌以 JWT 形式表示时，公钥哈希包含在确认方法（cnf）声明下的 jkt 声明中。

下面的示例展示了一个 JWT 访问令牌的声明，其中包含带有 jkt 声明的 cnf 声明；该 jkt 值是 DPoP 证明公钥的 JWK SHA-256
指纹（Thumbprint）：

``` json
{
  "sub":"user@example.com",
  "iss":"https://server.example.com",
  "nbf":1562262611,
  "exp":1562266216,
  "cnf":
  {
    "jkt":"CQMknzRoZ5YUi7vS58jck1q8TmZT8wiIiXrCN1Ny4VU"
  }
}
```

## OAuth2令牌自省端点

OAuth2TokenIntrospectionEndpointConfigurer 提供了自定义 OAuth2 令牌自省（Token Introspection）端点的能力。它定义了一系列扩展点，允许你定制
OAuth2 自省请求的预处理、主处理以及后处理逻辑。

OAuth2TokenIntrospectionEndpointConfigurer 提供以下配置选项：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.tokenIntrospectionEndpoint(tokenIntrospectionEndpoint ->
                    tokenIntrospectionEndpoint
						//	`introspectionRequestConverter()`：
						//	添加一个 `AuthenticationConverter`（预处理器），
						//	用于在尝试从 `HttpServletRequest` 中提取 OAuth2 introspection 请求，
						//	并将其转换为 `OAuth2TokenIntrospectionAuthenticationToken` 实例时使用。
                        .introspectionRequestConverter(introspectionRequestConverter)
						//	`introspectionRequestConverters()`：
						//	设置一个 `Consumer`，用于访问默认以及（可选）额外添加的 `AuthenticationConverter` 列表，
						//	从而支持新增、移除或自定义某个特定的 `AuthenticationConverter`。
                        .introspectionRequestConverters(introspectionRequestConvertersConsumer)
						//	`authenticationProvider()`：
						//	添加一个 `AuthenticationProvider`（主处理器），
						//	用于对 `OAuth2TokenIntrospectionAuthenticationToken` 进行认证。
                        .authenticationProvider(authenticationProvider)
						//	`authenticationProviders()`：
						//	设置一个 `Consumer`，用于访问默认以及（可选）额外添加的 `AuthenticationProvider` 列表，
						//	从而支持新增、移除或自定义某个特定的 `AuthenticationProvider`。
                        .authenticationProviders(authenticationProvidersConsumer)
						//	`introspectionResponseHandler()`：
						//	用于处理“已认证”的 `OAuth2TokenIntrospectionAuthenticationToken` 
						//	并返回 OAuth2 Token Introspection 响应的 `AuthenticationSuccessHandler`（后处理器）。
                        .introspectionResponseHandler(introspectionResponseHandler)
						//	`errorResponseHandler()`：
						//	用于处理 `OAuth2AuthenticationException` 
						//	并返回 `OAuth2Error` 响应的 `AuthenticationFailureHandler`（后处理器）。
                        .errorResponseHandler(errorResponseHandler)
				)
		);

	return http.build();
}
```

OAuth2TokenIntrospectionEndpointConfigurer 用于配置 OAuth2TokenIntrospectionEndpointFilter，并将其注册到 OAuth2 授权服务器的
SecurityFilterChain @Bean 中。OAuth2TokenIntrospectionEndpointFilter 是用于处理 OAuth2 Token 内省（introspection）请求的
Filter。

OAuth2TokenIntrospectionEndpointFilter 默认包含以下配置：

- AuthenticationConverter——OAuth2TokenIntrospectionAuthenticationConverter。
- AuthenticationManager——由 OAuth2TokenIntrospectionAuthenticationProvider 组成的 AuthenticationManager。
- AuthenticationSuccessHandler——内部实现，用于处理已通过认证的 OAuth2TokenIntrospectionAuthenticationToken，并返回 OAuth2
  Token 内省响应。
- AuthenticationFailureHandler——OAuth2ErrorAuthenticationFailureHandler。

## OAuth2令牌撤销端点

OAuth2TokenRevocationEndpointConfigurer 提供了自定义 OAuth2 令牌撤销（Token Revocation）端点的能力。它定义了一些扩展点，允许你对
OAuth2 撤销请求的前置处理、核心处理以及后置处理逻辑进行定制。

OAuth2TokenRevocationEndpointConfigurer 提供了以下配置选项：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
				.tokenRevocationEndpoint(tokenRevocationEndpoint ->
                    tokenRevocationEndpoint
						//	revocationRequestConverter()：
						//	添加一个 AuthenticationConverter（预处理器），
						//	用于在尝试从 HttpServletRequest 中提取 OAuth2 
						//	撤销请求并转换为 OAuth2TokenRevocationAuthenticationToken 实例时使用。
                        .revocationRequestConverter(revocationRequestConverter)
						//	revocationRequestConverters()：
						//	设置一个 Consumer，用于访问默认以及（可选）新增的 AuthenticationConverter 列表，
						//	从而支持添加、移除或自定义某个特定的 AuthenticationConverter。
                        .revocationRequestConverters(revocationRequestConvertersConsumer)
						//	authenticationProvider()：
						//	添加一个 AuthenticationProvider（主处理器），
						//	用于对 OAuth2TokenRevocationAuthenticationToken 进行认证。
                        .authenticationProvider(authenticationProvider)
						//	authenticationProviders()：
						//	设置一个 Consumer，用于访问默认以及（可选）新增的 AuthenticationProvider 列表，
						//	从而支持添加、移除或自定义某个特定的 AuthenticationProvider。
                        .authenticationProviders(authenticationProvidersConsumer)
						//	revocationResponseHandler()：
						//	用于处理“已认证”的 OAuth2TokenRevocationAuthenticationToken 
						//	并返回 OAuth2 撤销响应的 AuthenticationSuccessHandler（后处理器）。
                        .revocationResponseHandler(revocationResponseHandler)
						//	errorResponseHandler()：
						//	用于处理 OAuth2AuthenticationException 
						//	并返回 OAuth2Error 响应的 AuthenticationFailureHandler（后处理器）。
                        .errorResponseHandler(errorResponseHandler)
				)
		);

	return http.build();
}
```

OAuth2TokenRevocationEndpointConfigurer 用于配置 OAuth2TokenRevocationEndpointFilter，并将其注册到 OAuth2 授权服务器的
SecurityFilterChain @Bean 中。OAuth2TokenRevocationEndpointFilter 是用于处理 OAuth2 撤销请求的过滤器。

OAuth2TokenRevocationEndpointFilter 的默认配置如下：

- AuthenticationConverter：OAuth2TokenRevocationAuthenticationConverter。
- AuthenticationManager：由 OAuth2TokenRevocationAuthenticationProvider 组成的 AuthenticationManager。
- AuthenticationSuccessHandler：内部实现，用于处理处于“已认证”状态的 OAuth2TokenRevocationAuthenticationToken，并返回
  OAuth2 撤销响应。
- AuthenticationFailureHandler：OAuth2ErrorAuthenticationFailureHandler。

## OAuth2授权服务器元数据端点

OAuth2AuthorizationServerMetadataEndpointConfigurer 用于自定义 OAuth2 授权服务器元数据（Metadata）端点。它提供了一个扩展点，允许你定制
OAuth2 授权服务器元数据的响应内容。  
OAuth2AuthorizationServerMetadataEndpointConfigurer 提供以下配置项：

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.authorizationServerMetadataEndpoint(authorizationServerMetadataEndpoint ->
									authorizationServerMetadataEndpoint
											//	authorizationServerMetadataCustomizer()：
											//	提供一个可访问 OAuth2AuthorizationServerMetadata.Builder 
											//	的 Consumer，使你能够自定义授权服务器配置中的声明（claims）。
											.authorizationServerMetadataCustomizer(authorizationServerMetadataCustomizer)
							)
			);

	return http.build();
}
```

OAuth2AuthorizationServerMetadataEndpointConfigurer 用于配置 OAuth2AuthorizationServerMetadataEndpointFilter，并将其注册到
OAuth2 授权服务器的 SecurityFilterChain @Bean 中。OAuth2AuthorizationServerMetadataEndpointFilter 是一个用于返回
OAuth2AuthorizationServerMetadata 响应的 Filter。

## JWK集合端点

OAuth2AuthorizationServerConfigurer 提供对 JWK Set 端点的支持。

OAuth2AuthorizationServerConfigurer 会配置 NimbusJwkSetEndpointFilter，并将其注册到 OAuth2 授权服务器的
SecurityFilterChain @Bean 中。NimbusJwkSetEndpointFilter 是负责返回 JWK Set 的过滤器。

!!! note

    仅当注册了一个 `JWKSource<SecurityContext>` 的 `@Bean` 时，才会配置 JWK Set 端点。

## OpenID Connect 1.0提供方配置端点

OidcProviderConfigurationEndpointConfigurer 提供了自定义 OpenID Connect 1.0 提供方配置（Provider
Configuration）端点的能力。它定义了一个扩展点，使你可以定制 OpenID Provider Configuration 的响应内容。

OidcProviderConfigurationEndpointConfigurer 提供以下配置选项。

```java

@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
					authorizationServer
							.oidc(oidc ->
									oidc
											.providerConfigurationEndpoint(providerConfigurationEndpoint ->
													providerConfigurationEndpoint
															//	providerConfigurationCustomizer()：
															//	用于提供一个可访问 `OidcProviderConfiguration.Builder` 的 `Consumer`，
															//	以便自定义 OpenID Provider 配置中的声明（claims）。
															.providerConfigurationCustomizer(providerConfigurationCustomizer)
											)
							)
			);

	return http.build();
}
```

OidcProviderConfigurationEndpointConfigurer 用于配置 OidcProviderConfigurationEndpointFilter，并将其注册到 OAuth2 授权服务器的
SecurityFilterChain（@Bean）中。OidcProviderConfigurationEndpointFilter 是用于返回 OidcProviderConfiguration 响应的过滤器。

## OpenID Connect 1.0 登出端点

OidcLogoutEndpointConfigurer 提供了自定义 OpenID Connect 1.0 Logout 端点的能力。它定义了一些扩展点，允许你针对 RP 发起的
Logout 请求，自定义预处理、核心处理以及后处理逻辑。

OidcLogoutEndpointConfigurer 提供以下配置选项：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
                .oidc(oidc ->
                    oidc
                        .logoutEndpoint(logoutEndpoint ->
                            logoutEndpoint
								//	logoutRequestConverter()：
								//	添加一个 AuthenticationConverter（预处理器），
								//	用于在尝试从 HttpServletRequest 中提取 Logout 
								//	请求并转换为 OidcLogoutAuthenticationToken 实例时使用。
                                .logoutRequestConverter(logoutRequestConverter)
								//	logoutRequestConverters()：
								//	设置一个 Consumer，用于访问默认以及（可选）额外添加的 AuthenticationConverter 列表，
								//	从而支持对指定 AuthenticationConverter 进行新增、移除或自定义。
                                .logoutRequestConverters(logoutRequestConvertersConsumer)
								//	authenticationProvider()：
								//	添加一个 AuthenticationProvider（主处理器），
								//	用于对 OidcLogoutAuthenticationToken 进行认证。
                                .authenticationProvider(authenticationProvider)
								//	authenticationProviders()：
								//	设置一个 Consumer，用于访问默认以及（可选）额外添加的 AuthenticationProvider 列表，
								//	从而支持对指定 AuthenticationProvider 进行新增、移除或自定义。
                                .authenticationProviders(authenticationProvidersConsumer)
								//	logoutResponseHandler()：
								//	用于处理“已认证”的 OidcLogoutAuthenticationToken 
								//	并执行登出的 AuthenticationSuccessHandler（后处理器）。
                                .logoutResponseHandler(logoutResponseHandler)
								//	errorResponseHandler()：
								//	用于处理 OAuth2AuthenticationException 并返回
								//	错误响应的 AuthenticationFailureHandler（后处理器）。
                                .errorResponseHandler(errorResponseHandler)
                        )
                )
		);

	return http.build();
}
```

OidcLogoutEndpointConfigurer 用于配置 OidcLogoutEndpointFilter，并将其注册到 OAuth2 授权服务器的 SecurityFilterChain
@Bean 中。OidcLogoutEndpointFilter 是用于处理 RP 发起的注销（RP-Initiated Logout）请求并执行终端用户（End-User）注销的
Filter。

OidcLogoutEndpointFilter 的默认配置如下：

- AuthenticationConverter —— OidcLogoutAuthenticationConverter。
- AuthenticationManager —— 由 OidcLogoutAuthenticationProvider 组成的 AuthenticationManager。
- AuthenticationSuccessHandler —— OidcLogoutAuthenticationSuccessHandler。
- AuthenticationFailureHandler —— 内部实现：使用与 OAuth2AuthenticationException 关联的 OAuth2Error，并返回 OAuth2Error
  响应。

!!! note

    OidcLogoutAuthenticationProvider 使用 SessionRegistry 来查找与发起登出请求的终端用户关联的 SessionInformation 实例。

!!! tip

    OidcClientInitiatedLogoutSuccessHandler 是 Spring Security 的 OAuth2 Client 支持中用于配置 OpenID Connect 1.0 RP 发起登出（RP-Initiated Logout）的对应配置项。

### 自定义注销请求验证

OidcLogoutAuthenticationValidator 是用于校验特定 OpenID Connect RP 发起登出（RP-Initiated Logout）请求参数的默认校验器。默认实现会校验
`post_logout_redirect_uri` 参数；如果校验失败，将抛出 `OAuth2AuthenticationException`。

OidcLogoutAuthenticationProvider 支持通过提供自定义的登出请求校验器来覆盖默认校验逻辑：将一个类型为
`Consumer<OidcLogoutAuthenticationContext>` 的校验器传入 `setAuthenticationValidator()`。

!!! note

    OidcLogoutAuthenticationContext 持有 OidcLogoutAuthenticationToken，而后者包含注销请求的参数。

!!! warning

    如果验证失败，认证校验器必须抛出 OAuth2AuthenticationException。

下面的示例演示了如何为 `OidcLogoutAuthenticationProvider` 配置自定义的身份验证校验器：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
                .oidc(oidc ->
                    oidc
                        .logoutEndpoint(logoutEndpoint ->
                            logoutEndpoint
                                .authenticationProviders(configureAuthenticationValidator())
                        )
                )
		);

	return http.build();
}

private Consumer<List<AuthenticationProvider>> configureAuthenticationValidator() {
	return (authenticationProviders) ->
			authenticationProviders.forEach((authenticationProvider) -> {
				if (authenticationProvider instanceof OidcLogoutAuthenticationProvider oidcLogoutAuthenticationProvider) {
					Consumer<OidcLogoutAuthenticationContext> authenticationValidator = new CustomPostLogoutRedirectUriValidator();
					oidcLogoutAuthenticationProvider.setAuthenticationValidator(authenticationValidator);
				}
			});
}

static class CustomPostLogoutRedirectUriValidator implements Consumer<OidcLogoutAuthenticationContext> {

	@Override
	public void accept(OidcLogoutAuthenticationContext authenticationContext) {
		OidcLogoutAuthenticationToken oidcLogoutAuthentication =
				authenticationContext.getAuthentication();
		RegisteredClient registeredClient = authenticationContext.getRegisteredClient();

		// TODO

	}
}
```

## OpenID Connect 1.0用户信息端点

OidcUserInfoEndpointConfigurer 用于自定义 OpenID Connect 1.0 的 UserInfo 端点。它提供了一系列扩展点，允许你针对 UserInfo
请求的前置处理、核心处理以及后置处理逻辑进行定制。

OidcUserInfoEndpointConfigurer 提供以下配置选项：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
                .oidc(oidc ->
                    oidc
                        .userInfoEndpoint(userInfoEndpoint ->
                            userInfoEndpoint
								//	userInfoRequestConverter()：
								//	添加一个 AuthenticationConverter（前置处理器），
								//	用于在尝试从 HttpServletRequest 中提取 UserInfo 
								//	请求并转换为 OidcUserInfoAuthenticationToken 实例时使用。
                                .userInfoRequestConverter(userInfoRequestConverter)
								//	userInfoRequestConverters()：
								//	设置一个 Consumer，用于访问默认以及（可选）额外添加的 AuthenticationConverter 列表，
								//	从而支持添加、移除或自定义某个特定的 AuthenticationConverter。
                                .userInfoRequestConverters(userInfoRequestConvertersConsumer)
								//	authenticationProvider()：
								//	添加一个 AuthenticationProvider（主处理器），
								//	用于对 OidcUserInfoAuthenticationToken 进行认证。
                                .authenticationProvider(authenticationProvider)
								//	authenticationProviders()：
								//	设置一个 Consumer，用于访问默认以及（可选）额外添加的 AuthenticationProvider 列表，
								//	从而支持添加、移除或自定义某个特定的 AuthenticationProvider。
                                .authenticationProviders(authenticationProvidersConsumer)
								//	userInfoResponseHandler()：
								//	用于处理“已认证”的 OidcUserInfoAuthenticationToken 
								//	并返回 UserInfo 响应的 AuthenticationSuccessHandler（后置处理器）。
                                .userInfoResponseHandler(userInfoResponseHandler)
								//	errorResponseHandler()：
								//	用于处理 OAuth2AuthenticationException 
								//	并返回 UserInfo 错误响应的 AuthenticationFailureHandler（后置处理器）。
                                .errorResponseHandler(errorResponseHandler)
								//	userInfoMapper()：
								//	用于从 OidcUserInfoAuthenticationContext 中提取声明（claims），
								//	并映射为 OidcUserInfo 实例的 Function。
                                .userInfoMapper(userInfoMapper)
                        )
                )
		);

	return http.build();
}
```

OidcUserInfoEndpointConfigurer 用于配置 OidcUserInfoEndpointFilter，并将其注册到 OAuth2 授权服务器的 SecurityFilterChain
@Bean 中。OidcUserInfoEndpointFilter 是用于处理 UserInfo 请求并返回 OidcUserInfo 响应的 Filter。

OidcUserInfoEndpointFilter 默认配置如下：

- AuthenticationConverter——内部实现：从 SecurityContext 获取 Authentication，并基于 principal 创建
  OidcUserInfoAuthenticationToken。
- AuthenticationManager——由 OidcUserInfoAuthenticationProvider 组成的 AuthenticationManager；该 Provider 关联了一个内部实现的
  userInfoMapper，会根据授权时请求的 scope，从 ID Token 中提取标准声明（claims）。
- AuthenticationSuccessHandler——内部实现：处理“已认证”的 OidcUserInfoAuthenticationToken，并返回 OidcUserInfo 响应。
- AuthenticationFailureHandler——内部实现：使用与 OAuth2AuthenticationException 关联的 OAuth2Error，并返回 OAuth2Error 响应。

!!! tip

    你可以通过提供一个 OAuth2TokenCustomizer<JwtEncodingContext> 的 @Bean 来自定义 ID Token。

OpenID Connect 1.0 的 UserInfo 端点是受 OAuth2 保护的资源，UserInfo 请求中必须携带访问令牌，并以 Bearer Token 的方式发送。

!!! note

    OAuth2 资源服务器支持已自动配置，不过要使用 OpenID Connect 1.0 的 UserInfo 端点，必须提供一个 JwtDecoder 的 @Bean。

!!! tip

    [《操作指南：如何自定义 OpenID Connect 1.0 的 UserInfo 响应》](https://docs.spring.io/spring-authorization-server/reference/guides/how-to-userinfo.html)包含了自定义 UserInfo 端点的示例。

## OpenID Connect 1.0客户端注册端点

OidcClientRegistrationEndpointConfigurer 用于自定义 OpenID Connect 1.0
客户端注册端点。它提供了可扩展的扩展点，使你能够对客户端注册请求或客户端读取请求的预处理、核心处理以及后处理逻辑进行定制。

OidcClientRegistrationEndpointConfigurer 提供以下配置选项：

``` java
@Bean
public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
	OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
			OAuth2AuthorizationServerConfigurer.authorizationServer();

	http
		.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
		.with(authorizationServerConfigurer, (authorizationServer) ->
			authorizationServer
                .oidc(oidc ->
                    oidc
                        .clientRegistrationEndpoint(clientRegistrationEndpoint ->
                            clientRegistrationEndpoint
								//	clientRegistrationRequestConverter()：
								//	添加一个 AuthenticationConverter（预处理器），
								//	用于在尝试从 HttpServletRequest 中提取客户端注册请求或客户端
								//	读取请求，并将其转换为 OidcClientRegistrationAuthenticationToken 实例时使用。
                                .clientRegistrationRequestConverter(clientRegistrationRequestConverter)
								//	clientRegistrationRequestConverters()：
								//	设置一个 Consumer，用于访问默认以及（可选）新增的 AuthenticationConverter 列表，
								//	从而支持添加、移除或自定义某个特定的 AuthenticationConverter。
                                .clientRegistrationRequestConverters(clientRegistrationRequestConvertersConsumers)
								//	authenticationProvider()：
								//	添加一个 AuthenticationProvider（主处理器），
								//	用于对 OidcClientRegistrationAuthenticationToken 进行认证。
                                .authenticationProvider(authenticationProvider)
								//	authenticationProviders()：
								//	设置一个 Consumer，用于访问默认以及（可选）
								//	新增的 AuthenticationProvider 列表，从而支持添加、
								//	移除或自定义某个特定的 AuthenticationProvider。
                                .authenticationProviders(authenticationProvidersConsumer)
								//	clientRegistrationResponseHandler()：
								//	用于处理“已认证”的 OidcClientRegistrationAuthenticationToken 
								//	并返回客户端注册响应或客户端读取响应的 AuthenticationSuccessHandler（后处理器）。
                                .clientRegistrationResponseHandler(clientRegistrationResponseHandler)
								//	errorResponseHandler()：
								//	用于处理 OAuth2AuthenticationException 并返回客户端注册错误响应
								//	或客户端读取错误响应的 AuthenticationFailureHandler（后处理器）。
                                .errorResponseHandler(errorResponseHandler)
                        )
                )
		);

	return http.build();
}
```

!!! note

    OpenID Connect 1.0 的客户端注册端点默认处于禁用状态，因为很多部署场景并不需要动态客户端注册。

OidcClientRegistrationEndpointConfigurer 用于配置 OidcClientRegistrationEndpointFilter，并将其注册到 OAuth2 授权服务器的 SecurityFilterChain @Bean 中。OidcClientRegistrationEndpointFilter 是一个用于处理客户端注册（Client Registration）请求并返回 OidcClientRegistration 响应的 Filter。

!!! tip

    OidcClientRegistrationEndpointFilter 还会处理客户端读取（Client Read）请求，并返回 OidcClientRegistration 响应。

OidcClientRegistrationEndpointFilter 默认配置如下：  

- AuthenticationConverter——OidcClientRegistrationAuthenticationConverter。  
- AuthenticationManager——由 OidcClientRegistrationAuthenticationProvider 和 OidcClientConfigurationAuthenticationProvider 组合而成的 AuthenticationManager。  
- AuthenticationSuccessHandler——内部实现：处理已通过认证（authenticated）的 OidcClientRegistrationAuthenticationToken，并返回 OidcClientRegistration 响应。  
- AuthenticationFailureHandler——内部实现：使用 OAuth2AuthenticationException 关联的 OAuth2Error，并返回 OAuth2Error 响应。

OpenID Connect 1.0 的客户端注册端点是一个受 OAuth2 保护的资源，客户端注册（或客户端读取）请求必须携带访问令牌，并以 Bearer Token 的形式发送。

!!! note

    OAuth2 资源服务器支持已自动配置，但 OpenID Connect 1.0 客户端注册端点**必须**提供一个 `JwtDecoder` 的 `@Bean`。

!!! warning

    客户端注册请求中的访问令牌必须包含 OAuth2 作用域 `client.create`。

!!! warning

    客户端读取（Client Read）请求中的访问令牌必须具备 OAuth2 的 `client.read` 作用域。
