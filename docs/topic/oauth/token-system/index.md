---
title: 令牌体系
---

# OAuth2 令牌体系

OAuth2 的核心是令牌——客户端拿到令牌才能访问资源。但令牌不是铁板一块，不同类型有不同的用途和安全特性。

打个比方：令牌就像游乐园里的一套**凭证系统**。Access Token 是你手腕上的**临时手环**（戴着就能玩），Refresh Token 是手环到期时去前台换新手环的**续期凭证**，而 Authorization Code 只是在售票窗口拿到的**取号条**——取号条本身不能进任何项目，你得拿着它去换真正的手环。

**本文你会学到：**

- Access Token 和 Refresh Token 各自的用途和生命周期
- Bearer Token 的三种传递方式及其安全差异
- 什么是 Sender-Constrained Token，为什么它比普通令牌更安全
- WWW-Authenticate 错误响应的格式和含义

## Access Token（访问令牌）

Access Token（RFC 6749 Section 1.4）是客户端访问受保护资源的`凭证`，相当于临时通行证。把它想象成一张限时限区的**游乐园手环**——戴上了就能玩对应区域的项目，但有时间限制。

- `有限期`：通常较短（几分钟到几小时），过期后需要刷新
- `有限权限`：仅包含授权时指定的 Scope 权限
- `格式不固定`：可以是 JWT（包含信息的自描述令牌），也可以是 Opaque Token（纯随机字符串），格式由授权服务器决定

!!! note "Access Token 的使用原则"

    - Access Token `不应被客户端读取或解释`——客户端不是令牌的目标受众，令牌是给资源服务器看的
    - Access Token `不传达用户身份信息`——如果需要用户信息，应使用 OIDC 的 ID Token
    - Access Token `只应用于请求资源服务器`——不应在客户端之间传递或用于其他用途

## Bearer Token 的传递方式

Access Token 还有一个重要区分：**它是否绑定了特定的使用者？**

`Bearer Token`（不记名令牌，RFC 6750）是最常见的访问令牌类型。Bearer 的英文意思是"持有者"——谁拿着它谁就能用，就像`现金`一样。它可以是短十六进制字符串，也可以是结构化的 JWT。使用 Bearer Token 时，客户端与资源服务器之间的通信`必须使用 TLS`（HTTPS）保护，防止令牌在传输中被截获。

RFC 6750 定义了三种向资源服务器传递 Bearer Token 的方式，安全性和适用场景各有不同：

| 方式 | 安全性 | 服务器是否必须支持 | 适用场景 |
|------|--------|-------------------|---------|
| `Authorization` 请求头 | 高 | 必须 | 所有场景（推荐） |
| Form-Encoded Body | 中 | 可选 | 极少数无法设置请求头的浏览器环境 |
| URI Query Parameter | 低 | 可选 | OAuth 2.1 已禁止 |

### Authorization 请求头（推荐）

RFC 6750 推荐的传递方式。客户端在 HTTP 请求头中使用 `Bearer` 认证方案发送 Token：

``` http
GET /resource HTTP/1.1
Host: server.example.com
Authorization: Bearer mF_9.B5f-4.1JqM
```

资源服务器`必须支持`这种方式。语法与 HTTP Basic 认证类似：`Authorization: Bearer <token>`。

### Form-Encoded Body Parameter

当浏览器环境无法访问 `Authorization` 请求头时（极少数场景），可以将 Token 放在请求体中。RFC 6750 规定了严格的**使用前提**——必须同时满足以下所有条件：

- 请求的 `Content-Type` 为 `application/x-www-form-urlencoded`
- 请求体为单部分（非 multipart）
- 请求体内容全部为 ASCII 字符
- HTTP 方法**不能是 GET**（GET 请求没有请求体）

``` http
POST /resource HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded

access_token=mF_9.B5f-4.1JqM
```

> 除非确实无法使用 `Authorization` 请求头，否则`不应使用`这种方式。资源服务器`可选支持`。

### URI Query Parameter（不推荐）

将 Token 放在 URL 查询参数中传递：

``` http
GET /resource?access_token=mF_9.B5f-4.1JqM HTTP/1.1
Host: server.example.com
```

RFC 6750 明确指出这种方式存在**严重安全隐患**：URL 会被记录在浏览器历史记录、Web 服务器访问日志、代理服务器日志中，还可能通过 `Referer` 头泄露给第三方网站。OAuth 2.1 已`完全禁止`此方式。

!!! warning "额外要求"

    使用 URI Query Parameter 方式时，客户端`应`同时发送 `Cache-Control: no-store` 头，防止响应被缓存。资源服务器在成功响应中`应`包含 `Cache-Control: private` 头。

### WWW-Authenticate 错误响应

当资源服务器拒绝请求时，返回 `WWW-Authenticate` 响应头，使用 `Bearer` 认证方案。RFC 6750 定义了三个标准错误码：

``` http
# 未携带 Token 时
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="example"

# Token 过期时
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="example",
                      error="invalid_token",
                      error_description="The access token expired"
```

| 错误码 | HTTP 状态码 | 含义 |
|--------|------------|------|
| `invalid_request` | 400 | 请求缺少必需参数、参数重复、使用了多种 Token 传递方式 |
| `invalid_token` | 401 | Token 过期、被撤销、格式错误或无效。客户端`可以`用新 Token 重试 |
| `insufficient_scope` | 403 | Token 权限不足。响应中`可包含` `scope` 属性告知需要的权限 |

注意：如果请求完全没有携带认证信息（客户端不知道需要认证），资源服务器`不应`返回错误码细节，只返回 `WWW-Authenticate: Bearer realm="example"` 即可——这可以避免向潜在攻击者泄露信息。

## Refresh Token（刷新令牌）

Access Token 过期了怎么办？总不能让用户反复登录吧。Refresh Token（RFC 6749 Section 1.5）就是来解决这个问题的——它让你在 Access Token 过期后`无需用户重新授权`即可获取新的 Access Token。把它想象成出入证的**续期凭证**：手环到期了，拿续期凭证去前台换一个新手环，不用重新排队买票。

- `有效期更长`（几天到几个月）
- `只发送给授权服务器`，不发送给资源服务器
- `不能超越原始授权范围`：刷新时可以请求原始 Scope 的子集，但不能获取更多权限
- `公开客户端和机密客户端都可使用`，但公开客户端的 Refresh Token 被盗风险更高（攻击者可以冒充客户端使用），建议通过 DPoP 绑定来缓解
- 某些授权类型（如客户端凭证）不颁发 Refresh Token

Refresh Token 的存在使授权服务器可以为 Access Token 设置`较短的有效期`（如几分钟），兼顾安全性和用户体验——Access Token 频繁过期降低了泄露后的影响时间窗口，而 Refresh Token 让用户无需反复登录授权。

> 小结：Access Token 是「干活的凭证」（访问资源），Refresh Token 是「续命的凭证」（获取新 Access Token），Authorization Code 是「办证的取号条」（换 Access Token）。三者各司其职。

## Sender-Constrained Token（发送者约束令牌）

前面介绍的 Bearer Token 有一个根本性的安全隐患：**谁拿到谁就能用**。就像丢了现金，捡到的人可以随便花。

`Sender-Constrained Token`（发送者约束令牌）解决了这个问题——它将令牌与特定客户端的密钥绑定，使用时客户端必须`证明持有对应的私钥`。就像`实名绑定的门禁卡`：即使丢了，别人也刷不开，因为系统只认原持有人的指纹。

两种主要的实现方式：

| 方式 | 标准 | 绑定机制 |
|------|------|---------|
| mTLS | RFC 8705 | 令牌绑定到客户端的 TLS 证书 |
| DPoP | RFC 9449 | 客户端每次请求附带私钥签名的证明 |

由于 Sender-Constrained Token 需要客户端参与证明过程，实现复杂度高于普通 Bearer Token。但在安全性要求较高的场景（如金融支付、医疗数据）中，它是必要的防护措施。详细机制见「安全实践」和「扩展协议 · DPoP」。

---

`上一篇：` [授权流程](../authorization-flows/index.md)
`下一篇：` [JWT 令牌](../jwt/index.md)
