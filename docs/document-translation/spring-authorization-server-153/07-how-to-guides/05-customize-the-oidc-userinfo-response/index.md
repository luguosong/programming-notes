
# 自定义OpenID Connect 1.0的UserInfo响应

本指南将介绍如何自定义 Spring Authorization Server 的 UserInfo 端点。本文旨在演示如何启用该端点，并利用现有的自定义选项生成自定义响应。

## 启用用户信息端点

OpenID Connect 1.0 的 UserInfo 端点是受 OAuth2 保护的资源，在发起 UserInfo 请求时，必须携带访问令牌，并以 Bearer Token
的形式传递。

!!! note

    从 OpenID Connect 身份验证请求获取的 Access Token 必须按照 [OAuth 2.0 Bearer Token](https://openid.net/specs/openid-connect-core-1_0.html#RFC6750) 用法 [RFC6750] 第 2 节，以 Bearer Token 的形式发送。

在自定义响应之前，你需要先启用 UserInfo 端点。下面的代码清单展示了如何启用 OAuth2 资源服务器配置。

``` java
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class EnableUserInfoSecurityConfig {

	//用于协议端点的 Spring Security 过滤器链。  
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
				OAuth2AuthorizationServerConfigurer.authorizationServer();

		http
				.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
				.with(authorizationServerConfigurer, (authorizationServer) ->
						authorizationServer
								//	启用 OpenID Connect 1.0 后，将自动配置资源服务器支持，使 UserInfo 请求能够使用访问令牌进行身份验证。
								.oidc(Customizer.withDefaults())
				)
				.authorizeHttpRequests((authorize) ->
						authorize
								.anyRequest().authenticated()
				)
				.exceptionHandling((exceptions) -> exceptions
						.defaultAuthenticationEntryPointFor(
								new LoginUrlAuthenticationEntryPoint("/login"),
								new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
						)
				);

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests((authorize) -> authorize
						.anyRequest().authenticated()
				)
				.formLogin(Customizer.withDefaults());

		return http.build();
	}

	//用于校验访问令牌的 JwtDecoder 实例。
	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withDefaultPasswordEncoder()
				.username("user")
				.password("password")
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(userDetails);
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("messaging-client")
				.clientSecret("{noop}secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.redirectUri("http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc")
				.redirectUri("http://127.0.0.1:8080/authorized")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.ADDRESS)
				.scope(OidcScopes.EMAIL)
				.scope(OidcScopes.PHONE)
				.scope(OidcScopes.PROFILE)
				.scope("message.read")
				.scope("message.write")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.build();

		return new InMemoryRegisteredClientRepository(registeredClient);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}

}
```

## 自定义用户信息响应

以下各节将介绍一些用于自定义用户信息响应的选项。

### 自定义ID Token

默认情况下，UserInfo 响应会基于令牌响应中返回的 id_token 里的 claims 生成。采用默认策略时，UserInfo 响应只会根据授权阶段请求的
scopes 返回相应的标准 claims。

自定义 UserInfo 响应的推荐方式，是将标准 claims 直接加入 id_token。下面的示例展示了如何向 id_token 添加 claims。

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class IdTokenCustomizerConfig {

	//用于自定义 id_token 的 OAuth2TokenCustomizer 实例。  
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(
			OidcUserInfoService userInfoService) {
		return (context) -> {
			if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
				//一个自定义服务，用于以特定领域的方式获取用户信息。
				OidcUserInfo userInfo = userInfoService.loadUser(
						context.getPrincipal().getName());
				context.getClaims().claims(claims ->
						claims.putAll(userInfo.getClaims()));
			}
		};
	}

}
```

下面的代码清单展示了一个自定义服务，用于以领域特定的方式查询用户信息：

``` java
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;

/**
 * 用于查询用户信息、以便自定义 {@code id_token} 的示例服务。
 */
@Service
public class OidcUserInfoService {

	private final UserInfoRepository userInfoRepository = new UserInfoRepository();

	public OidcUserInfo loadUser(String username) {
		return new OidcUserInfo(this.userInfoRepository.findByUsername(username));
	}

	static class UserInfoRepository {

		private final Map<String, Map<String, Object>> userInfo = new HashMap<>();

		public UserInfoRepository() {
			this.userInfo.put("user1", createUser("user1"));
			this.userInfo.put("user2", createUser("user2"));
		}

		public Map<String, Object> findByUsername(String username) {
			return this.userInfo.get(username);
		}

		private static Map<String, Object> createUser(String username) {
			return OidcUserInfo.builder()
					.subject(username)
					.name("First Last")
					.givenName("First")
					.familyName("Last")
					.middleName("Middle")
					.nickname("User")
					.preferredUsername(username)
					.profile("https://example.com/" + username)
					.picture("https://example.com/" + username + ".jpg")
					.website("https://example.com")
					.email(username + "@example.com")
					.emailVerified(true)
					.gender("female")
					.birthdate("1970-01-01")
					.zoneinfo("Europe/Paris")
					.locale("en-US")
					.phoneNumber("+1 (604) 555-1234;ext=5678")
					.phoneNumberVerified(false)
					.claim("address", Collections.singletonMap("formatted", "Champ de Mars\n5 Av. Anatole France\n75007 Paris\nFrance"))
					.updatedAt("1970-01-01T00:00:00Z")
					.build()
					.getClaims();
		}
	}

}
```

### 自定义用户信息映射器

要想对用户信息（user info）响应进行彻底定制，你可以提供一个自定义的用户信息映射器，用来生成用于渲染响应的对象——也就是 Spring
Security 中的 `OidcUserInfo` 实例。该映射器的实现会接收一个 `OidcUserInfoAuthenticationContext` 实例，其中包含当前请求的相关信息，包括
`OAuth2Authorization`。

下面的代码清单展示了在直接使用 `OAuth2AuthorizationServerConfigurer` 时，如何使用这一可用的自定义选项。

``` java
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;
import java.util.function.Function;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class JwtUserInfoMapperSecurityConfig {

	//用于协议端点（Protocol Endpoints）的 Spring Security 过滤器链。  
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		//一个以领域（domain）为中心的用户信息映射器，用于按业务语义映射声明（claims）。  
		Function<OidcUserInfoAuthenticationContext, OidcUserInfo> userInfoMapper = (context) -> {
			OidcUserInfoAuthenticationToken authentication = context.getAuthentication();
			JwtAuthenticationToken principal = (JwtAuthenticationToken) authentication.getPrincipal();

			return new OidcUserInfo(principal.getToken().getClaims());
		};

		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
				OAuth2AuthorizationServerConfigurer.authorizationServer();

		http
			.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
			.with(authorizationServerConfigurer, (authorizationServer) ->
				authorizationServer
					//	启用 OpenID Connect 1.0 后，会自动配置资源服务器支持，使 User Info 请求可使用访问令牌完成认证。  
					.oidc((oidc) -> oidc
						.userInfoEndpoint((userInfo) -> userInfo
							//	一个示例，展示如何通过配置项自定义用户信息映射器。
							.userInfoMapper(userInfoMapper)
						)
					)
			)
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			.exceptionHandling((exceptions) -> exceptions
				.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"),
					new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
				)
			);

		return http.build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			.formLogin(Customizer.withDefaults());

		return http.build();
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withDefaultPasswordEncoder()
				.username("user")
				.password("password")
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(userDetails);
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("messaging-client")
				.clientSecret("{noop}secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.redirectUri("http://127.0.0.1:8080/login/oauth2/code/messaging-client-oidc")
				.redirectUri("http://127.0.0.1:8080/authorized")
				.scope(OidcScopes.OPENID)
				.scope("message.read")
				.scope("message.write")
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.build();

		return new InMemoryRegisteredClientRepository(registeredClient);
	}

	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		KeyPair keyPair = generateRsaKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		RSAKey rsaKey = new RSAKey.Builder(publicKey)
				.privateKey(privateKey)
				.keyID(UUID.randomUUID().toString())
				.build();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return new ImmutableJWKSet<>(jwkSet);
	}

	private static KeyPair generateRsaKey() {
		KeyPair keyPair;
		try {
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(2048);
			keyPair = keyPairGenerator.generateKeyPair();
		}
		catch (Exception ex) {
			throw new IllegalStateException(ex);
		}
		return keyPair;
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}

}
```

此配置会将访问令牌中的声明（在使用 Getting Started 配置时，访问令牌为 JWT）映射到用户信息（User Info）响应中，并提供以下内容：

1. 用于协议端点（Protocol Endpoints）的 Spring Security 过滤器链。
2. 一个以领域（domain）为中心的用户信息映射器，用于按业务语义映射声明（claims）。
3. 启用 OpenID Connect 1.0 后，会自动配置资源服务器支持，使 User Info 请求可使用访问令牌完成认证。
4. 一个示例，展示如何通过配置项自定义用户信息映射器。

用户信息映射器并不局限于映射 JWT 中的声明；这里只是一个简单示例，用于演示自定义能力。类似前面自定义 ID Token
声明的示例，你也可以提前自定义访问令牌本身的声明，如下例所示：

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class JwtTokenCustomizerConfig {

	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
		return (context) -> {
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				context.getClaims().claims((claims) -> {
					claims.put("claim-1", "value-1");
					claims.put("claim-2", "value-2");
				});
			}
		};
	}

}
```

无论你是直接自定义 user info 响应，还是基于这个示例并进一步自定义 access token，都可以通过查询数据库、执行 LDAP
查询、请求其他服务，或采用任何其他方式来获取你希望在 user info 响应中呈现的信息。
