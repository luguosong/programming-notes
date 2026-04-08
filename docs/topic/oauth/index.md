---
title: OAuth2
icon: lucide/shield-check
---

# OAuth2

OAuth2 和 OpenID Connect 是现代互联网应用中最重要的授权与认证标准。无论是第三方登录（"使用 GitHub 登录"）、API 访问控制，还是微服务间的安全调用，背后都离不开这两套协议。

**打个比方**：OAuth2 就像一个**门禁卡系统**——你（用户）授权给谁（应用）进入哪些区域（资源），完全由你决定，而且你可以随时收回门禁卡，不需要交出原始钥匙（密码）。

## ❓ 为什么需要 OAuth2

在 OAuth2 出现之前，第三方应用若要访问用户在其他服务上的资源，通常要求用户直接提交用户名和密码。这带来了严重问题：

- `密码泄露风险`：第三方应用一旦遭到攻击，用户主账号即暴露
- `无法细粒度控制权限`：第三方拿到密码后拥有账号的所有权限
- `无法撤销授权`：用户只能通过修改密码来撤销第三方的访问

OAuth2（RFC 6749）的核心思想是`令牌（Token）替代密码`：用户通过授权服务器授权，第三方应用只获得有限期的访问令牌，不接触用户密码，且用户可随时撤销。

## 🔗 OAuth2 与 OpenID Connect 的关系

!!! info "授权 vs 认证"

    - `OAuth2` 是`授权（Authorization）`协议：回答"这个应用能访问哪些资源？"
    - `OpenID Connect（OIDC）` 是构建在 OAuth2 之上的`认证（Authentication）`层：回答"当前用户是谁？"

    OIDC 并不是替代 OAuth2，而是在 OAuth2 的授权流程之上，额外颁发一个包含用户身份信息的 `ID Token`。

## 🗺️ 协议全景图

``` mermaid
sequenceDiagram
    participant RO as 资源所有者（用户）
    participant Client as 客户端应用
    participant AS as 授权服务器
    participant RS as 资源服务器

    Client->>RO: 1. 请求授权
    RO->>AS: 2. 用户登录并同意授权
    AS-->>Client: 3. 颁发授权码
    Client->>AS: 4. 用授权码换取 Access Token
    AS-->>Client: 5. 返回 Access Token（及 Refresh Token）
    Client->>RS: 6. 携带 Access Token 请求资源
    RS-->>Client: 7. 验证 Token 后返回资源
```

## 📚 学习路径

按以下顺序学习，可以建立从基础到实战的完整知识体系：

<div class="grid cards" markdown>

- :lucide-layers: `核心概念`

    四种角色、客户端类型、Scope 等基础概念

    [→ 核心概念](core-concepts/index.md)

- :lucide-refresh-cw: `授权流程`

    授权码、客户端凭证、设备授权、PKCE 等授权流程详解

    [→ 授权流程](authorization-flows/index.md)

- :lucide-ticket: `令牌体系`

    Access Token、Refresh Token、Bearer Token 的类型与使用

    [→ 令牌体系](token-system/index.md)

- :lucide-key: `JWT 令牌`

    JWT 结构、声明、JWK/JWKS、签名算法

    [→ JWT 令牌](jwt/index.md)

- :lucide-plug: `端点与发现`

    授权/令牌/内省/撤销端点、元数据发现

    [→ 端点与发现](endpoints/index.md)

- :lucide-user-check: `OpenID Connect`

    ID Token、UserInfo、Hybrid Flow、登出机制

    [→ OpenID Connect](openid-connect/index.md)

- :lucide-puzzle: `扩展协议`

    PKCE、PAR、RAR、DPoP、mTLS、动态注册

    [→ 扩展协议](extensions/index.md)

- :lucide-shield: `安全实践`

    PKCE 防护、DPoP、令牌存储、攻击应对

    [→ 安全实践](security/index.md)

- :lucide-swords: `威胁模型`

    RFC 6819 攻击面分析与威胁建模

    [→ 威胁模型](threat-model/index.md)

- :lucide-server: `实战：授权服务器`

    Spring Authorization Server 授权服务器搭建与配置

    [→ 实战：授权服务器](spring-auth-server/index.md)

- :lucide-monitor: `实战：客户端与资源服务器`

    OAuth2 客户端 + 资源服务器的 Spring Security 集成

    [→ 实战：客户端与资源服务器](spring-client-resource/index.md)

- :lucide-arrow-up-circle: `OAuth 2.1 演进`

    七大关键差异与迁移清单

    [→ OAuth 2.1 演进](oauth2-1/index.md)

</div>
