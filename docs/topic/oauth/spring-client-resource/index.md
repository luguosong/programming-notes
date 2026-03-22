# 实战：OAuth2 客户端与资源服务器

本文介绍如何使用 Spring Security 配置 OAuth2 **客户端应用**（让用户通过 OAuth2 登录）和 **资源服务器**（保护 API 端点并验证 Token）。

授权服务器的配置见[实战：授权服务器](../spring-auth-server/index.md)。

## OAuth2 客户端

### Maven 依赖

``` xml title="pom.xml"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 配置 application.yml

``` yaml title="application.yml"
spring:
  security:
    oauth2:
      client:
        registration:
          my-auth-server: # (1)!
            client-id: my-client
            client-secret: my-secret
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope: openid, profile, read
        provider:
          my-auth-server:
            issuer-uri: http://localhost:9000 # (2)!
```

1. 注册标识符，用于区分多个 OAuth2 提供方
2. 框架会自动从 `issuer-uri/.well-known/openid-configuration` 获取所有端点配置

### oauth2Login()：用户 OIDC 登录

让用户通过授权服务器登录，获取 OIDC 身份（ID Token）：

``` java title="ClientSecurityConfig.java"
@Configuration
@EnableWebSecurity
public class ClientSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers("/public/**").permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 ->
                oauth2.defaultSuccessUrl("/dashboard", true) // (1)!
            )
            .logout(logout ->
                logout.logoutSuccessUrl("/")
            );

        return http.build();
    }
}
```

1. 登录成功后重定向到 `/dashboard`

**在 Controller 中获取已认证用户信息：**

``` java title="DashboardController.java"
@GetMapping("/dashboard")
public String dashboard(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
    // 从 ID Token 中获取用户信息
    model.addAttribute("name", oidcUser.getFullName());
    model.addAttribute("email", oidcUser.getEmail());
    model.addAttribute("subject", oidcUser.getSubject()); // sub claim
    return "dashboard";
}
```

### oauth2Client()：客户端代调 API（Client Credentials）

当客户端需要以**自身身份**（而非用户身份）调用后端 API 时，使用客户端凭证流程：

``` yaml title="application.yml"
spring:
  security:
    oauth2:
      client:
        registration:
          backend-service:
            client-id: backend-client
            client-secret: backend-secret
            authorization-grant-type: client_credentials
            scope: api.read
        provider:
          backend-service:
            token-uri: http://localhost:9000/oauth2/token
```

``` java title="ApiClientConfig.java"
@Configuration
public class ApiClientConfig {

    @Bean
    public WebClient apiClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        // 自动获取并附加 Access Token 的 WebClient
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("backend-service");

        return WebClient.builder()
            .filter(oauth2Client)
            .build();
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .refreshToken()
                .build();

        DefaultOAuth2AuthorizedClientManager manager =
            new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }
}
```

---

## OAuth2 资源服务器

### Maven 依赖

``` xml title="pom.xml"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### JWT 验证配置

``` yaml title="application.yml"
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:9000 # (1)!
```

1. 框架自动从 issuer-uri 的发现文档获取 JWKS URI，缓存公钥并验证 JWT 签名

``` java title="ResourceServerConfig.java"
@Configuration
@EnableWebSecurity
public class ResourceServerConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                authorize
                    .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                    .requestMatchers("/api/admin/**").hasAuthority("SCOPE_admin") // (1)!
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(withDefaults())
            );

        return http.build();
    }
}
```

1. Spring Security 自动将 JWT 中的 `scope` claim 映射为 `SCOPE_xxx` 权限

**在 Controller 中提取 Token 信息：**

``` java title="ApiController.java"
@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "sub", jwt.getSubject(),
            "scopes", jwt.getClaim("scope"),
            "roles", jwt.getClaim("roles") // 自定义 claim
        );
    }
}
```

### Opaque Token 验证（Introspection）

若授权服务器颁发的是 Opaque Token，需要配置 Introspection 端点：

``` yaml title="application.yml"
spring:
  security:
    oauth2:
      resourceserver:
        opaquetoken:
          introspection-uri: http://localhost:9000/oauth2/introspect
          client-id: resource-server
          client-secret: resource-server-secret
```

``` java title="OpaqueResourceServerConfig.java"
@Configuration
@EnableWebSecurity
public class OpaqueResourceServerConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                authorize.anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 ->
                oauth2.opaqueToken(withDefaults()) // 替换 jwt() 为 opaqueToken()
            );
        return http.build();
    }
}
```

### 测试资源服务器

在测试中使用 `@WithMockUser` 或 `SecurityMockMvcRequestPostProcessors.jwt()` 模拟 JWT：

``` java title="ApiControllerTest.java"
@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenValidJwt_thenReturnsUserInfo() throws Exception {
        mockMvc.perform(get("/api/me")
                .with(jwt() // (1)!
                    .jwt(jwt -> jwt
                        .subject("test-user")
                        .claim("scope", "read write")
                        .claim("roles", List.of("ROLE_USER"))
                    )
                ))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sub").value("test-user"));
    }

    @Test
    void whenNoToken_thenReturns401() throws Exception {
        mockMvc.perform(get("/api/me"))
            .andExpect(status().isUnauthorized());
    }
}
```

1. `jwt()` 来自 `org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors`

---

**上一篇：** [实战：授权服务器](../spring-auth-server/index.md)
