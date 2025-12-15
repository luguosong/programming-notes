# 在单页应用（SPA）中使用PKCE进行身份认证

本指南将演示如何配置 Spring Authorization Server，以支持启用 Proof Key for Code
Exchange（PKCE）的单页应用（SPA）。本指南旨在说明如何支持公共客户端，并在客户端认证中强制要求使用 PKCE。

!!! note

    Spring Authorization Server 不会为公共客户端签发刷新令牌。我们建议采用 Backend for Frontend（BFF）模式，作为避免暴露公共客户端的替代方案。更多信息请参见 gh-297。

## 启用CORS

SPA 由一组静态资源组成，可通过多种方式部署。它既可以与后端分开部署，例如通过 CDN 或独立的 Web 服务器；也可以使用 Spring Boot
与后端一同部署。

当 SPA 托管在不同的域名下时，可以使用跨域资源共享（CORS）来允许应用与后端进行通信。

例如，如果你在本地 4200 端口运行 Angular 开发服务器，可以定义一个 `CorsConfigurationSource` 的 `@Bean`，并在 Spring
Security 中通过 `cors()` DSL 配置允许预检请求（pre-flight requests），如下例所示：

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
			//  未认证时，从授权端点重定向到登录页
			.exceptionHandling((exceptions) -> exceptions
				.defaultAuthenticationEntryPointFor(
					new LoginUrlAuthenticationEntryPoint("/login"),
					new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
				)
			);

		return http.cors(Customizer.withDefaults()).build();
	}

	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
			throws Exception {
		http
			.authorizeHttpRequests((authorize) -> authorize
				.anyRequest().authenticated()
			)
			// 表单登录负责处理从授权服务器过滤器链重定向到登录页面
			.formLogin(Customizer.withDefaults());

		return http.cors(Customizer.withDefaults()).build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.addAllowedOrigin("http://127.0.0.1:4200");
		config.setAllowCredentials(true);
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
```

## 配置公共客户端

SPA 无法安全地存储凭据，因此必须视为公共客户端。公共客户端应当强制使用用于授权码交换的证明密钥（PKCE）。

沿用前面的示例，你可以将 Spring Authorization Server 配置为支持公共客户端：将客户端认证方式设置为 none，并像下面的示例那样强制启用
PKCE：

``` yaml
spring:
  security:
    oauth2:
      authorizationserver:
        client:
          public-client:
            registration:
              client-id: "public-client"
              client-authentication-methods:
                - "none"
              authorization-grant-types:
                - "authorization_code"
              redirect-uris:
                - "http://127.0.0.1:4200"
              scopes:
                - "openid"
                - "profile"
            require-authorization-consent: true
            require-proof-key: true
```

!!! warning

    `requireProofKey` 设置至关重要，可有效防止 PKCE 降级攻击。

## 使用客户端进行身份验证

一旦服务器配置为支持公共客户端，大家常会问：我该如何对客户端进行认证并获取访问令牌？简而言之：和对待其他任何客户端一样。

SPA 是运行在浏览器中的应用，因此它和其他客户端一样，使用基于重定向的流程。这个问题通常源于一种误解：以为可以通过 REST API
来完成认证——但在 OAuth 2 中并不是这样。

要更详细地回答这个问题，需要先理解 OAuth2 和 OpenID Connect 中涉及的相关流程——这里指的是`授权码模式（Authorization Code Flow）`
。授权码模式的步骤如下：

1. 客户端通过重定向到`授权端点（Authorization Endpoint）`来发起 OAuth2 请求。对于`公共客户端（public client）`，这一步会生成
   `code_verifier` 并计算 `code_challenge`，随后将其作为查询参数发送。
2. 如果用户尚未完成认证，授权服务器会重定向到登录页面。认证完成后，用户会再次被重定向回授权端点。
3. 如果用户尚未对所请求的 scope（权限范围）完成授权且需要用户同意，则会显示同意页面（consent page）。
4. 用户同意后，授权服务器会生成 `authorization_code`，并通过 `redirect_uri` 重定向回客户端。
5. 客户端通过查询参数获取 `authorization_code`，并向`令牌端点（Token Endpoint)`发起请求。对于公共客户端，这一步会发送
   `code_verifier` 参数，用它来替代凭据进行认证。

正如你所看到的，这个流程相当复杂，而这份概览也只是浅尝辄止。

!!! tip

    建议你使用单页应用框架所支持的成熟可靠的前端库来处理授权码（Authorization Code）流程。
