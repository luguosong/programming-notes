
# 使用社交账号登录进行身份验证

本指南将演示如何将 Spring Authorization Server 与社交登录提供方（如 Google、GitHub 等）集成用于身份认证。本文旨在展示如何用
OAuth 2.0 登录替换表单登录（Form Login）。

!!! note

    Spring Authorization Server 基于 Spring Security 构建，本指南将全程沿用 Spring Security 的相关概念。

## 使用社交账号登录平台注册

要开始使用，你需要先在你选择的社交登录服务商处创建并配置一个应用。常见的服务商包括：

- [Google](https://developers.google.com/identity/openid-connect/openid-connect?hl=zh-cn#appsetup)
- [GitHub](https://github.com/settings/developers)
- [Facebook](https://business.facebook.com/business/loginpage/?next=https%3A%2F%2Fdevelopers.facebook.com%2Fapps#)
- [Okta](https://developer.okta.com/signup/)

按照你的身份提供方的流程操作，直到系统提示你指定 Redirect URI 为止。配置 Redirect URI 时，先选定一个 registrationId（例如
`google`、`my-client`，或任何你想用的唯一标识符），后续你将用它同时配置 Spring Security 和你的身份提供方。

!!! note

    registrationId 是 Spring Security 中用于标识 ClientRegistration 的唯一标识符。默认的重定向 URI 模板为：{baseUrl}/login/oauth2/code/{registrationId}。更多信息请参阅 Spring Security 文档中的“设置重定向 URI”。

!!! tip

    例如，在本地使用 9000 端口进行测试，且 registrationId 为 google 时，你的 Redirect URI 应为 localhost:9000/login/oauth2/code/google。配置应用到对应的 OAuth 提供商时，将该值填写为 Redirect URI。

一旦你完成了与社交登录提供商的配置流程，你应该已经拿到了凭证（Client ID 和 Client Secret）。此外，你还需要查阅该提供商的文档，并记录以下信息：

- Authorization URI：用于在提供商处发起 authorization_code 流程的端点。
- Token URI：用于将 authorization_code 兑换为 access_token，并可选获取 id_token 的端点。
- JWK Set URI：用于获取验证 JWT 签名所需密钥的端点（当提供 id_token 时必需）。
- User Info URI：用于获取用户信息的端点（当不提供 id_token 时必需）。
- User Name Attribute：在 id_token 或 User Info 响应中包含用户名的 Claim 字段。

## 配置OAuth 2.0登录

一旦你在社交登录提供商处完成注册，就可以继续配置 Spring Security
以支持 [OAuth 2.0 登录](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html)。

### 添加OAuth2客户端依赖

首先，添加以下依赖项：

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### 注册客户端

接下来，使用前面获取的值来配置 `ClientRegistration`。以 Okta 为例，请配置以下属性：

``` yaml
okta:
  base-url: ${OKTA_BASE_URL}

spring:
  security:
    oauth2:
      client:
        registration:
          my-client:
            provider: okta
            client-id: ${OKTA_CLIENT_ID}
            client-secret: ${OKTA_CLIENT_SECRET}
            scope:
              - openid
              - profile
              - email
        provider:
          okta:
            authorization-uri: ${okta.base-url}/oauth2/v1/authorize
            token-uri: ${okta.base-url}/oauth2/v1/token
            user-info-uri: ${okta.base-url}/oauth2/v1/userinfo
            jwk-set-uri: ${okta.base-url}/oauth2/v1/keys
            user-name-attribute: sub
```

!!! note

    上述示例中的 registrationId 是 `my-client`。

!!! note

    上述示例演示了推荐的做法：通过环境变量（OKTA_BASE_URL、OKTA_CLIENT_ID 和 OKTA_CLIENT_SECRET）来配置 Provider URL、Client ID 和 Client Secret。更多信息请参阅 Spring Boot 参考文档中的“外部化配置（Externalized Configuration）”。

这个简单的示例展示了一个典型配置，但有些提供方可能还需要额外的配置。有关如何配置 ClientRegistration 的更多信息，请参阅
Spring Security 参考文档中的 Spring Boot 属性映射（Spring Boot Property Mappings）。

### 配置身份验证

最后，如果要将 Spring Authorization Server 配置为使用社交登录提供商进行身份认证，可以用 `oauth2Login()` 替代 `formLogin()`
。你还可以通过在 `exceptionHandling()` 中配置 `AuthenticationEntryPoint`，在用户未认证时自动将其重定向到该提供商。

延续我们之前的示例，使用 `@Configuration` 按如下示例配置 Spring Security：

``` java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	//用于协议端点（Protocol Endpoints）的 Spring Security 过滤器链。
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
			throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
				OAuth2AuthorizationServerConfigurer.authorizationServer();

		http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
				authorizationServer
					.oidc(Customizer.withDefaults())	// Enable OpenID Connect 1.0
			)
			.authorizeHttpRequests((authorize) ->
				authorize
					.anyRequest().authenticated()
			)
			// 未认证时重定向到 OAuth 2.0 登录端点
			// 来自授权端点
			.exceptionHandling((exceptions) -> exceptions
				//	配置一个 AuthenticationEntryPoint，用于重定向到 OAuth 2.0 登录端点。
				.defaultAuthenticationEntryPointFor( 
					new LoginUrlAuthenticationEntryPoint("/oauth2/authorization/my-client"),
					new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
				)
			);

		return http.build();
	}

	//用于认证的 Spring Security 过滤器链。
	@Bean 
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
			throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			//  OAuth2 登录会处理从授权服务器过滤器链重定向到 OAuth 2.0 登录端点的请求
			.oauth2Login(Customizer.withDefaults()); // 配置 OAuth 2.0 登录以完成认证。

		return http.build();
	}

}
```

如果你在入门时配置过 `UserDetailsService`，现在可以把它移除了。

## 高级使用场景

该授权服务器 Demo 示例展示了用于联合身份提供商的高级配置选项。你可以从以下用例中选择，查看各自的示例：

- 我想将用户信息保存到数据库中
- 我想将 Claims 映射到 ID Token 中

### 在数据库中获取用户数据

下面的示例 `AuthenticationSuccessHandler` 在用户首次登录时，会通过一个自定义组件将其信息记录到本地数据库中：

```java title="FederatedIdentityAuthenticationSuccessHandler"
import java.io.IOException;
import java.util.function.Consumer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public final class FederatedIdentityAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	private final AuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

	private Consumer<OAuth2User> oauth2UserHandler = (user) -> {
	};

	private Consumer<OidcUser> oidcUserHandler = (user) -> this.oauth2UserHandler.accept(user);

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
		if (authentication instanceof OAuth2AuthenticationToken) {
			if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
				this.oidcUserHandler.accept(oidcUser);
			} else if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
				this.oauth2UserHandler.accept(oauth2User);
			}
		}

		this.delegate.onAuthenticationSuccess(request, response, authentication);
	}

	public void setOAuth2UserHandler(Consumer<OAuth2User> oauth2UserHandler) {
		this.oauth2UserHandler = oauth2UserHandler;
	}

	public void setOidcUserHandler(Consumer<OidcUser> oidcUserHandler) {
		this.oidcUserHandler = oidcUserHandler;
	}

}
```

使用上面的 `AuthenticationSuccessHandler`，你可以接入自定义的 `Consumer<OAuth2User>`
，将用户信息捕获并保存到数据库或其他数据存储中，用于实现联邦账号关联（Federated Account Linking）或即时账号开通（JIT Account
Provisioning）等场景。下面是一个示例，仅将用户存储在内存中。

```java title="UserRepositoryOAuth2UserHandler"
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.security.oauth2.core.user.OAuth2User;

public final class UserRepositoryOAuth2UserHandler implements Consumer<OAuth2User> {

	private final UserRepository userRepository = new UserRepository();

	@Override
	public void accept(OAuth2User user) {
		// 在首次认证时，将用户信息保存到本地数据存储中
		if (this.userRepository.findByName(user.getName()) == null) {
			System.out.println("Saving first-time user: name=" + user.getName() + ", claims=" + user.getAttributes() + ", authorities=" + user.getAuthorities());
			this.userRepository.save(user);
		}
	}

	static class UserRepository {

		private final Map<String, OAuth2User> userCache = new ConcurrentHashMap<>();

		public OAuth2User findByName(String name) {
			return this.userCache.get(name);
		}

		public void save(OAuth2User oauth2User) {
			this.userCache.put(oauth2User.getName(), oauth2User);
		}

	}

}
```

### 将声明映射到 ID 令牌

下面的示例 `OAuth2TokenCustomizer` 会将身份验证提供方返回的用户声明（claims）映射到 Spring Authorization Server 生成的
`id_token` 中：

``` java title="FederatedIdentityIdTokenCustomizer"
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
public final class FederatedIdentityIdTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

	private static final Set<String> ID_TOKEN_CLAIMS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
			IdTokenClaimNames.ISS,
			IdTokenClaimNames.SUB,
			IdTokenClaimNames.AUD,
			IdTokenClaimNames.EXP,
			IdTokenClaimNames.IAT,
			IdTokenClaimNames.AUTH_TIME,
			IdTokenClaimNames.NONCE,
			IdTokenClaimNames.ACR,
			IdTokenClaimNames.AMR,
			IdTokenClaimNames.AZP,
			IdTokenClaimNames.AT_HASH,
			IdTokenClaimNames.C_HASH
	)));

	@Override
	public void customize(JwtEncodingContext context) {
		if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
			Map<String, Object> thirdPartyClaims = extractClaims(context.getPrincipal());
			context.getClaims().claims(existingClaims -> {
				// Remove conflicting claims set by this authorization server
				existingClaims.keySet().forEach(thirdPartyClaims::remove);

				// Remove standard id_token claims that could cause problems with clients
				ID_TOKEN_CLAIMS.forEach(thirdPartyClaims::remove);

				// Add all other claims directly to id_token
				existingClaims.putAll(thirdPartyClaims);
			});
		}
	}

	private Map<String, Object> extractClaims(Authentication principal) {
		Map<String, Object> claims;
		if (principal.getPrincipal() instanceof OidcUser oidcUser) {
			OidcIdToken idToken = oidcUser.getIdToken();
			claims = idToken.getClaims();
		} else if (principal.getPrincipal() instanceof OAuth2User oauth2User) {
			claims = oauth2User.getAttributes();
		} else {
			claims = Collections.emptyMap();
		}

		return new HashMap<>(claims);
	}

}
```

你可以将该自定义器通过发布为一个 `@Bean` 来配置到 Spring Authorization Server 中使用，如下例所示：

```java title="配置 FederatedIdentityIdTokenCustomizer"

@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> idTokenCustomizer() {
	return new FederatedIdentityIdTokenCustomizer();
}
```
