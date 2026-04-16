---
title: 端点与发现
---

# OAuth2 端点与发现

OAuth2 通过一组标准化的 HTTP 端点完成授权、令牌颁发和资源访问。理解每个端点的职责和协议格式，是正确实现 OAuth2 的基础。

把授权服务器想象成一个**政务服务中心**——你去办社保，得先到"取号窗口"拿号（授权端点），再到"办理窗口"提交材料换证（令牌端点），办完之后被引导到"出口通道"离开（重定向端点）。OAuth2 的各个端点就像服务中心的不同窗口，各司其职，按流程串起来完成整个授权过程。

**本文你会学到：**

- 授权端点和令牌端点的职责与请求格式
- 重定向端点的安全要求
- 令牌内省端点的请求/响应格式和缓存策略
- 令牌撤销端点的使用方式
- 授权服务器元数据发现（RFC 8414）

## 授权端点与令牌端点

这两个是 OAuth2 最基础的端点，所有授权流程都围绕它们展开：

| 端点 | 方式 | 通道 | 作用 |
|------|------|------|------|
| `Authorization Endpoint` | GET | 前端通道（浏览器重定向） | 用户授权页面，客户端将用户重定向到此处进行登录和授权确认 |
| `Token Endpoint` | POST | 后端通道（服务端直连） | 客户端用授权码/凭证换取 Token 的接口，机密客户端需在此认证 |

## 重定向端点（Redirection Endpoint）

`Redirection Endpoint` 是客户端提供的回调地址，授权服务器在完成用户授权后将响应（授权码或错误信息）通过浏览器重定向发送到此处。RFC 6749 Section 3.1.2 对其有严格规范：

- `必须是绝对 URI`（如 `https://example.com/callback`），不能是相对路径
- `必须预先注册`到授权服务器，授权服务器会将请求中的 `redirect_uri` 与注册值进行精确比较
- `可注册多个`重定向 URI（如不同环境的回调地址）
- 授权失败时`不得重定向`到无效或未注册的 URI（防止开放重定向攻击）
- 重定向端点的响应页面`不应包含第三方脚本`（如分析代码、广告网络），防止授权码在到达客户端之前被第三方读取。如果必须包含，客户端提取凭证的脚本`必须`最先执行

## 令牌内省端点（RFC 7662）

令牌内省（Token Introspection）为资源服务器提供了一种`查询令牌信息`的机制。资源服务器将收到的 Access Token 发送到授权服务器的内省端点，获取令牌是否有效、关联的用户、授权的 Scope 等信息。

这对`不透明令牌（Opaque Token）`尤为重要——不透明令牌本身不包含任何信息，资源服务器必须通过内省端点才能验证和解析它。打个比方：不透明令牌就像一张`磁条卡`——卡面上没有任何信息，必须刷到读卡器（内省端点）上才能知道里面记录了什么。而 JWT 则像一张`明文收据`——上面写明了有效期、权限等所有信息，资源服务器自己就能读。

### 内省请求

资源服务器向内省端点发送 `POST` 请求，参数以 `application/x-www-form-urlencoded` 格式传递。内省端点`必须要求认证`（防止攻击者批量探测令牌），常见的认证方式是客户端凭证（HTTP Basic）或 Bearer Token：

``` http
POST /introspect HTTP/1.1
Host: server.example.com
Accept: application/json
Content-Type: application/x-www-form-urlencoded
Authorization: Basic czZCaGRSa3F0MzpnWDFmQmF0M2JW

token=mF_9.B5f-4.1JqM&token_type_hint=access_token
```

| 参数 | 必须 | 说明 |
|------|------|------|
| `token` | ✅ | 要查询的令牌值 |
| `token_type_hint` | 可选 | 令牌类型提示（`access_token` 或 `refresh_token`），帮助授权服务器优化查找。如果按提示找不到，服务器`必须`扩展到所有令牌类型继续搜索 |

### 内省响应

授权服务器返回 JSON 响应，其中 `active` 字段是`唯一必须`的字段：

**活跃令牌的响应：**

``` json
{
  "active": true,
  "client_id": "l238j323ds-23ij4",
  "username": "jdoe",
  "scope": "read write dolphin",
  "sub": "Z5O3upPC88QrAjx00dis",
  "aud": "https://protected.example.net/resource",
  "iss": "https://server.example.com/",
  "exp": 1419356238,
  "iat": 1419350238
}
```

**无效或过期令牌的响应：**

``` json
{
  "active": false
}
```

!!! warning "不要泄露无效令牌的信息"
    对于无效令牌，授权服务器`只应返回` `{"active": false}`，`不应包含`任何额外信息（如为什么无效、原始 Scope 等），防止攻击者通过内省端点探测授权服务器的内部状态。

完整的响应字段说明：

| 字段 | 说明 |
|------|------|
| `active` | ✅ 必须。令牌是否活跃（已颁发、未过期、未撤销） |
| `scope` | 令牌关联的 Scope（空格分隔） |
| `client_id` | 请求该令牌的客户端标识 |
| `username` | 授权该令牌的用户标识（人类可读） |
| `token_type` | 令牌类型（如 `Bearer`） |
| `sub` | 令牌的主体（通常是用户的机器可读标识） |
| `aud` | 令牌的目标受众 |
| `iss` | 令牌的签发者 |
| `exp` | 过期时间（Unix 时间戳） |
| `iat` | 颁发时间（Unix 时间戳） |
| `nbf` | 生效时间（Unix 时间戳） |
| `jti` | 令牌的唯一标识 |

!!! info "授权服务器可以针对不同资源服务器返回不同信息"
    同一个令牌，不同资源服务器可能看到不同的 Scope——授权服务器可以限制每个资源服务器能了解的信息范围，防止某个资源服务器知道整个系统的全貌。

### 内省与缓存

资源服务器`可以缓存`内省响应以提高性能、减少对授权服务器的压力。但这引入了**实时性与安全性**的权衡：

- 缓存时间短 → 信息更及时，但内省调用更频繁，授权服务器负载更高
- 缓存时间长 → 网络开销更小，但存在`撤销窗口`——令牌被撤销后，缓存过期前该令牌仍可能被接受

如果响应包含 `exp` 字段，缓存`不得超过`该过期时间。高敏感环境可以`完全禁用缓存`。

## 令牌撤销端点（RFC 7009）

令牌撤销（Token Revocation）允许客户端`主动通知授权服务器某个令牌不再需要`。典型场景是实现"登出"功能——用户退出应用时，客户端将 Access Token 和/或 Refresh Token 发送到撤销端点，授权服务器清理关联的安全凭证。

## 令牌交换（RFC 8693）

令牌交换（Token Exchange）允许客户端`用一组令牌换取另一组令牌`。典型应用场景包括：

- 多个移动应用之间实现`单点登录（SSO）`而无需打开浏览器
- 资源服务器将客户端的令牌`换成自己的令牌`，用于调用下游服务

令牌交换的详细流程和参数说明详见「扩展协议」。

## 授权服务器元数据发现（RFC 8414）

授权服务器元数据（也称 OAuth Discovery）定义了一种标准格式，让客户端可以`自动发现`授权服务器的配置信息，而无需手动配置每个端点地址。

客户端访问 `/.well-known/oauth-authorization-server` 即可获取 JSON 格式的元数据，包括授权端点地址、令牌端点地址、支持的 Scope 列表、支持的客户端认证方式等。OIDC 的发现文档（`/.well-known/openid-configuration`）是此规范的扩展。

## 扩展性机制（RFC 6749 Section 8）

OAuth 2.0 被设计为`高度可扩展的框架`，定义了多个标准化扩展点，使其能够持续演进而不破坏已有实现。

打个比方：OAuth2 就像一部**智能手机的操作系统**——核心功能（打电话、发短信）不变，但通过"应用商店"（扩展点）可以不断安装新功能。PKCE、DPoP、RAR 这些扩展，都是后来上架的"应用"。

`新授权类型：` 通过绝对 URI 作为 `grant_type` 值注册。例如 Token Flow 使用 `urn:ietf:params:oauth:grant-type:token-exchange`，Device Flow 使用 `urn:ietf:params:oauth:grant-type:device_code`——这些都是 RFC 6749 之后的扩展，通过 URI 命名空间避免了与标准授权类型的冲突。

`新响应类型：` 注册到 IANA Authorization Endpoint Response Types Registry。标准定义了 `code`（返回授权码）和 `token`（直接返回 Token），扩展如 `code id_token`（OIDC 混合流程）通过注册加入。

`新端点参数：` 注册到 IANA OAuth Parameters Registry。例如 PKCE 引入的 `code_challenge`/`code_verifier`、PAR 引入的 `request_uri`、RAR 引入的 `authorization_details` 都是通过此机制成为标准参数的。

`核心原则：` 实现`必须忽略未知参数`——这是 OAuth 2.0 能持续演进的基础。一个 OAuth 2.0 客户端即使不认识某个新参数，也不应报错，而是简单地跳过它。这确保了新旧实现之间的互操作性。

---

`上一篇：` [JWT 令牌](../jwt/index.md)
`下一篇：` [OpenID Connect](../openid-connect/index.md)
