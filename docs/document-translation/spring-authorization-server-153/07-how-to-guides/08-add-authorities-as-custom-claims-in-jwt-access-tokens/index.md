# 在JWT访问令牌中将权限作为自定义声明添加

本指南将演示如何在 JWT 访问令牌中加入资源所有者的授权信息（authorities）。“authorities” 一词可能有不同的表现形式，例如资源所有者的角色（roles）、权限（permissions）或所属组（groups）。

为了让资源服务器能够获取资源所有者的授权信息，我们会在访问令牌中添加自定义声明（custom claims）。当客户端使用该访问令牌访问受保护资源时，资源服务器即可获得资源所有者的访问级别等信息，并可将其用于其他场景，从而带来更多使用价值与收益。

## 为 JWT 访问令牌添加自定义声明

你可以通过定义一个 `OAuth2TokenCustomizer<JWTEncodingContext>` 的 `@Bean`，为访问令牌（access token）添加自定义 Claim。需要注意的是，这个 `@Bean` 只能定义一次，因此必须谨慎确认你定制的是正确的令牌类型——这里是访问令牌。如果你想定制 ID Token，更多信息请参考 [User Info Mapper](https://docs.spring.io/spring-authorization-server/reference/guides/how-to-userinfo.html#customize-user-info-mapper) 指南。

下面示例演示了如何为访问令牌添加自定义 Claim——也就是说，授权服务器签发的每一个访问令牌都会包含这些自定义 Claim。

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class CustomClaimsConfiguration {
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
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

## 将权限作为自定义声明添加到 JWT 访问令牌中

要把资源所有者的权限信息加入 JWT 访问令牌，可以参考上面的自定义 Claim 映射方式，将 Principal 的权限列表写入一个自定义 Claim 中。

为了演示，我们定义一个带有一组权限的示例用户，并在访问令牌里通过自定义 Claim 填充这些权限信息。

``` java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class CustomClaimsWithAuthoritiesConfiguration {
	@Bean
	public UserDetailsService users() {
		UserDetails user = User.withDefaultPasswordEncoder()
				//使用基于内存的 UserDetailsService 定义一个示例用户 user1。
				.username("user1")
				.password("password")
				//为 user1 分配角色。
				.roles("user", "admin")
				.build();
		return new InMemoryUserDetailsManager(user);
	}

	//定义一个 OAuth2TokenCustomizer<JwtEncodingContext> 的 @Bean，用于自定义 JWT 的 Claims。
	@Bean
	public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
		return (context) -> {
			//检查该 JWT 是否为访问令牌（access token）。
			if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
				//通过 JwtEncodingContext 获取默认的 Claims。
				context.getClaims().claims((claims) -> {
					Set<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities())
							.stream()
							.map(c -> c.replaceFirst("^ROLE_", ""))
							// 从 Principal 对象中提取角色信息。角色以带有 ROLE_ 前缀的字符串形式存储，因此这里需要去掉该前缀。
							.collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet)); 
					//将自定义 Claim roles 设置为上一步收集到的角色集合。
					claims.put("roles", roles); 
				});
			}
		};
	}
}
```

由于进行了此项自定义配置，用户的权限信息将作为自定义声明（custom claim）包含在访问令牌（access token）中。

