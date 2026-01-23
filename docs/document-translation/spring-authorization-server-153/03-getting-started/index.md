# 入门

如果你刚开始接触 Spring Authorization Server，下面的内容将带你一步步创建你的第一个应用程序。

## 系统要求

Spring Authorization Server 需要 Java 17 或更高版本的运行环境。

## 安装 Spring Authorization Server

Spring Authorization Server 可以在任何已经使用 Spring Security 的地方使用。

开始使用 Spring Authorization Server 最简单的方式，就是创建一个基于 Spring Boot 的应用程序。你可以使用 start.spring.io
来生成一个基础项目，或者参考默认的授权服务器示例作为指导。然后，将 Spring Authorization Server 的 Spring Boot Starter
作为依赖添加到项目中：

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-authorization-server</artifactId>
</dependency>
```

!!! tip

	有关在 Maven 或 Gradle 中使用 Spring Boot 的更多信息，请参阅[安装 Spring Boot](https://docs.spring.io/spring-boot/installing.html#getting-started.installing)。

或者，你也可以参考以下示例，在不使用 Spring Boot 的情况下集成 Spring Authorization Server：

``` xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-authorization-server</artifactId>
    <version>1.5.3</version>
</dependency>
```

## 开发你的第一个应用程序

要开始使用时，你需要先定义一些作为 @Bean 的最小必要组件。  
当你使用 `spring-boot-starter-oauth2-authorization-server` 这个依赖时，只要配置以下属性，Spring Boot 就会自动为你提供所需的
@Bean 定义：

``` yaml
server:
  port: 9000

logging:
  level:
    org.springframework.security: trace

spring:
  security:
    user:
      name: user
      password: password
    oauth2:
      authorizationserver:
        client:
          oidc-client:
            registration:
              client-id: "oidc-client"
              client-secret: "{noop}secret"
              client-authentication-methods:
                - "client_secret_basic"
              authorization-grant-types:
                - "authorization_code"
                - "refresh_token"
              redirect-uris:
                - "http://127.0.0.1:8080/login/oauth2/code/oidc-client"
              post-logout-redirect-uris:
                - "http://127.0.0.1:8080/"
              scopes:
                - "openid"
                - "profile"
            require-authorization-consent: true
```

!!! tip

	在完成入门体验之后，大多数用户都会希望自定义默认配置。下一节将演示如何自行提供所有必要的 Bean。

## 定义必需组件

如果你想自定义默认配置（无论是否使用 Spring Boot），可以在 Spring 的 @Configuration 类中，将所需的最少组件定义为一个或多个
@Bean。

这些组件可以定义如下：

``` java title="SecurityConfig.java:一个用于快速上手的最小化配置。"
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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// 用于协议端点的 Spring Security 过滤器链。
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
								.oidc(Customizer.withDefaults())    // 启用 OpenID Connect 1.0
				)
				.authorizeHttpRequests((authorize) ->
						authorize
								.anyRequest().authenticated()
				)
				// 当未通过授权端点认证时，重定向到登录页
				.exceptionHandling((exceptions) -> exceptions
						.defaultAuthenticationEntryPointFor(
								new LoginUrlAuthenticationEntryPoint("/login"),
								new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
						)
				);

		return http.build();
	}

	// 用于认证的 Spring Security 过滤器链。
	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
			throws Exception {
		http
				.authorizeHttpRequests((authorize) -> authorize
						.anyRequest().authenticated()
				)
				// 表单登录负责处理从授权服务器过滤器链跳转到登录页的重定向
				.formLogin(Customizer.withDefaults());

		return http.build();
	}

	// 用于检索待认证用户的 UserDetailsService 实例。
	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withDefaultPasswordEncoder()
				.username("user")
				.password("password")
				.roles("USER")
				.build();

		return new InMemoryUserDetailsManager(userDetails);
	}

	// 用于管理客户端的 RegisteredClientRepository 实例。
	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
				.clientId("oidc-client")
				.clientSecret("{noop}secret")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
				.redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
				.postLogoutRedirectUri("http://127.0.0.1:8080/")
				.scope(OidcScopes.OPENID)
				.scope(OidcScopes.PROFILE)
				.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
				.build();

		return new InMemoryRegisteredClientRepository(oidcClient);
	}

	// 一个用于签名访问令牌的 `com.nimbusds.jose.jwk.source.JWKSource` 实例。
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

	// 一个在启动时生成密钥的 `java.security.KeyPair` 实例，用于创建上面的 JWKSource。
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

	// 用于解码已签名访问令牌的 JwtDecoder 实例。
	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	// 用于配置 Spring Authorization Server 的 AuthorizationServerSettings 实例。
	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder().build();
	}

}
```
