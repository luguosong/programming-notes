# 实战：Spring Authorization Server

!!! warning "版本迁移重要说明"

    **Spring Authorization Server 1.5.x 是最后一代独立版本。** 从 Spring Security 7.0 起，Spring Authorization Server 的功能已合并进 Spring Security 主项目，不再作为单独的依赖存在。

    - 1.5.x 对应 Spring Boot 3.5.x，artifactId 为 `spring-security-oauth2-authorization-server`
    - Spring Security 7.0 起，直接引入 `spring-boot-starter-oauth2-authorization-server`
    - 本文以 1.5.x 为主，文末提供 7.0 迁移要点

## 快速入门

### Maven 依赖

``` xml title="pom.xml"
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <!-- Spring Authorization Server 1.5.x（最后独立版本） -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-oauth2-authorization-server</artifactId>
    </dependency>
</dependencies>
```

### 最小化配置

``` java title="AuthorizationServerConfig.java"
@Configuration
@Import(OAuth2AuthorizationServerConfiguration.class) // (1)!
public class AuthorizationServerConfig {

    // (2)!
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("my-client")
            .clientSecret("{noop}my-secret") // (3)!
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .redirectUri("http://localhost:8080/login/oauth2/code/my-client")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope("read")
            .clientSettings(ClientSettings.builder()
                .requireProofKey(true) // (4)!
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofDays(7))
                .reuseRefreshTokens(false) // (5)!
                .build())
            .build();
        return new InMemoryRegisteredClientRepository(client);
    }

    // (6)!
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = generateRsa();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private static RSAKey generateRsa() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        return new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(UUID.randomUUID().toString())
            .build();
    }

    private static KeyPair generateRsaKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
            .issuer("http://localhost:9000") // (7)!
            .build();
    }
}
```

1. 导入 OAuth2 授权服务器的默认安全配置
2. 注册客户端仓库（生产环境应使用 `JdbcRegisteredClientRepository`）
3. `{noop}` 表示明文密码，生产环境必须使用 BCrypt 编码
4. 强制要求 PKCE，适用于公开客户端
5. 禁用 Refresh Token 复用，启用 Rotation 机制
6. JWK 密钥源，生产环境应从文件或 KMS 加载固定密钥
7. Issuer 必须与客户端配置的 issuer-uri 一致

### 配置 Spring Security 以开放授权服务器端点

``` java title="SecurityConfig.java"
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1) // (1)!
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
            throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
            OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
            .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
            .with(authorizationServerConfigurer, authorizationServer ->
                authorizationServer
                    .oidc(withDefaults()) // (2)!
            )
            .exceptionHandling(exceptions ->
                exceptions.defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
            );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
            throws Exception {
        http
            .authorizeHttpRequests(authorize ->
                authorize.anyRequest().authenticated()
            )
            .formLogin(withDefaults()); // (3)!

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // 生产环境替换为数据库实现
        // ⚠️ withDefaultPasswordEncoder() 在 Spring Security 6.x 已标注 @Deprecated
        // 仅供快速演示，生产环境请使用 BCryptPasswordEncoder 或数据库存储
        UserDetails user = User.builder()
            .username("admin")
            .password("{bcrypt}" + new BCryptPasswordEncoder().encode("password"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
```

1. 授权服务器 Security 链优先级必须高于默认链
2. 启用 OpenID Connect 1.0 支持
3. 默认使用表单登录，生产环境可自定义登录页

## 自定义授权页面

默认情况下 Spring Authorization Server 提供一个简单的授权确认页面。通过 `authorizationEndpoint()` 可以替换为自定义页面：

``` java title="AuthorizationServerConfig.java（授权页面自定义片段）"
http
    .with(authorizationServerConfigurer, authorizationServer ->
        authorizationServer
            .authorizationEndpoint(authorizationEndpoint ->
                authorizationEndpoint
                    .consentPage("/oauth2/consent") // (1)!
            )
            .oidc(withDefaults())
    );
```

1. 指定自定义授权确认页面的路径，需在对应 Controller 中处理该路由并返回 Thymeleaf/HTML 页面

``` java title="ConsentController.java"
@Controller
public class ConsentController {

    // 展示授权确认页面（列出请求的 scope 供用户勾选）
    @GetMapping("/oauth2/consent")
    public String consent(Principal principal, Model model,
            @RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
            @RequestParam(required = false, value = OAuth2ParameterNames.SCOPE) String scope, // (1)!
            @RequestParam(OAuth2ParameterNames.STATE) String state) {
        model.addAttribute("clientId", clientId);
        // scope 可能为 null（客户端未请求任何 scope 时），需做空值防护
        Set<String> scopes = scope != null
            ? new HashSet<>(Arrays.asList(scope.split(" ")))
            : Collections.emptySet();
        model.addAttribute("scopes", scopes);
        model.addAttribute("state", state);
        model.addAttribute("principalName", principal.getName());
        return "consent"; // 对应 templates/consent.html
    }
}
```

1. `required = false` 防止 scope 参数为 null 时抛出 `MissingServletRequestParameterException`

## 自定义令牌 Claims

使用 `OAuth2TokenCustomizer` 向 Access Token 或 ID Token 中添加自定义声明：

``` java title="TokenCustomizerConfig.java"
@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
    return context -> {
        if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
            // 向 Access Token 添加用户角色
            Authentication principal = context.getPrincipal();
            Set<String> authorities = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
            context.getClaims().claim("roles", authorities);
        }

        if (OidcParameterNames.ID_TOKEN.equals(context.getTokenType().getValue())) {
            // 向 ID Token 添加额外用户信息
            context.getClaims().claim("custom_claim", "custom_value");
        }
    };
}
```

## 配置 application.yml

``` yaml title="application.yml"
server:
  port: 9000

spring:
  security:
    user:
      # 开发调试用，生产环境移除
      name: admin
      password: password
```

## Spring Security 7.0 迁移要点

从 Spring Authorization Server 1.5.x 迁移到 Spring Security 7.0 的主要变化：

| 变化项 | 1.5.x | 7.0+ |
|--------|-------|------|
| 依赖 artifactId | `spring-security-oauth2-authorization-server` | `spring-boot-starter-oauth2-authorization-server` |
| `OAuth2AuthorizationServerConfigurer` | 独立类 | 集成进 Spring Security DSL |
| `@Import(OAuth2AuthorizationServerConfiguration.class)` | 常用 | 通过 Security Filter Chain 配置 |

!!! tip "关注官方迁移指南"
    Spring Security 7.0 发布后，请参考官方迁移文档：[Spring Authorization Server 迁移至 Spring Security 7.0](../../../document-translation/spring-authorization-server-153/moving-to-spring-security-70/index.md)

---

**上一篇：** [安全实践](../security/index.md)
**下一篇：** [实战：客户端与资源服务器](../spring-client-resource/index.md)
