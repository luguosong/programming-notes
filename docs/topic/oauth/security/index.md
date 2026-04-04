# OAuth2 安全实践

正确理解并实现 OAuth2 并不简单。本文按照 OAuth2 交互的阶段梳理安全威胁和防护机制：授权请求阶段如何防止篡改与劫持、令牌颁发后如何安全存储与使用、以及常见攻击模式的应对策略。各安全机制的流程实现见「授权类型」。

**本文你会学到：**

- PKCE 如何防止授权码被截获后滥用
- state 和 nonce 参数各自的防御目标
- PAR 如何将授权请求从浏览器 URL 中隐藏
- 令牌安全存储的最佳实践（SPA 和服务端）
- Refresh Token Rotation 的安全价值
- DPoP 与 mTLS 两种发送者约束令牌的区别
- 常见攻击模式（授权码注入、Token 重定向等）的应对策略
- RFC 6750 对 Bearer Token 的安全建议（短有效期、Cookie 禁令、TLS 证书链验证等）

## 授权请求阶段防护

OAuth2 的安全防护可以按照攻击发生的时间段来理解——授权请求阶段（用户点击登录到获得授权码）、令牌存储使用阶段（拿到 Token 之后）。先看第一阶段。

### PKCE：防止授权码截获攻击

`攻击场景：` 在没有 PKCE 的情况下，移动应用使用自定义 URL Scheme（如 `myapp://callback`）接收授权码。恶意应用可以注册相同的 URL Scheme，在操作系统层面拦截回调，从而获得授权码。

简单说：没有 PKCE 时，谁拿到了授权码就能换 Token；有了 PKCE，只有生成验证码的那个客户端实例才能换。

``` mermaid
sequenceDiagram
    participant 恶意App
    participant 正常App
    participant 授权服务器

    正常App->>授权服务器: 发起授权请求（无 PKCE）
    授权服务器-->>恶意App: 授权码被恶意App截获（注册了相同 URL Scheme）
    恶意App->>授权服务器: 用截获的授权码换取 Token
    授权服务器-->>恶意App: 颁发 Access Token（授权服务器无法区分）
```

`PKCE 如何防御：` PKCE 在客户端生成 `code_verifier`（随机密钥），并在授权请求时发送其哈希值 `code_challenge`。Token 端点在换取 Token 时要求客户端提供原始 `code_verifier` 进行验证。**恶意应用只截获了授权码，却不知道 `code_verifier`，无法通过 Token 端点的验证。**

!!! success "PKCE 的使用建议"

    - `所有公开客户端（SPA、移动端）`：PKCE 为必选
    - `机密客户端（Web 应用服务端）`：也应使用 PKCE（额外安全层）
    - PKCE 不是客户端认证的替代品，使用客户端认证的应用也应该同时使用 PKCE

PKCE 的流程机制详见「授权类型 · PKCE」。

### state 参数：防 CSRF 攻击

`攻击场景：` CSRF 攻击通过诱使已登录用户点击精心构造的链接，将攻击者的授权码绑定到受害者账号。

`防护方式：` 客户端在授权请求中包含随机 `state` 参数，授权服务器原样返回。客户端回调时**必须验证 `state` 与发出请求时的值一致**。

``` java
// 生成随机 state 并存储到 session
String state = UUID.randomUUID().toString();
session.setAttribute("oauth2_state", state);

// 回调时验证
String returnedState = request.getParameter("state");
String expectedState = (String) session.getAttribute("oauth2_state");
// 使用 Objects.equals 避免 returnedState 为 null 时的 NullPointerException
if (!Objects.equals(returnedState, expectedState)) {
    throw new SecurityException("state 不匹配，可能是 CSRF 攻击");
}
```

### nonce：防 ID Token 重放攻击（OIDC）

`攻击场景：` 攻击者截获一个合法用户的 ID Token，在其过期前将其发送给另一个系统假冒该用户身份。

`防护方式：` 客户端在 OIDC 授权请求中携带随机 `nonce`，授权服务器将 `nonce` 嵌入 ID Token。客户端验证 ID Token 时检查 `nonce` 是否与请求时一致。

### PAR：推送授权请求（RFC 9126）

常规授权流程中，授权请求参数（`client_id`、`scope`、`redirect_uri` 等）通过浏览器重定向的 URL 查询参数传递，存在以下问题：

- 请求参数在 URL 中`对浏览器和中间网络可见`，可能被日志记录或第三方脚本读取
- URL 长度受浏览器限制，`复杂的授权请求`（如携带 Rich Authorization Requests）可能超出限制
- 请求未经认证，任何人都可以`伪造`包含合法 `client_id` 的授权链接

PAR（Pushed Authorization Requests，RFC 9126）通过`将授权请求从前端通道移至后端通道`来解决这些问题：

``` mermaid
sequenceDiagram
    participant Client as 客户端服务端
    participant AS as 授权服务器
    participant RO as 用户浏览器

    Client->>AS: 1. POST /par（后端通道）<br/>携带完整授权参数 + 客户端凭证
    AS-->>Client: 2. 返回 request_uri（一次性引用）
    Client-->>RO: 3. 302 重定向至授权端点<br/>?client_id=...&request_uri=...
    RO->>AS: 4. 浏览器访问授权端点<br/>授权服务器根据 request_uri 还原完整请求
    AS-->>RO: 5. 展示授权页面，后续流程与标准授权码流程相同
```

客户端先将完整的授权参数通过后端通道直接提交给授权服务器的 PAR 端点，获得一个短效的 `request_uri` 引用。然后将用户重定向到授权端点时只需携带 `client_id` 和 `request_uri`——真正的请求参数不再暴露在浏览器 URL 中。

## 令牌安全

拿到 Token 之后，安全问题并没有结束。Token 存在哪里？丢了怎么办？下面的机制逐一解答。

### 令牌存储安全

#### 浏览器端（SPA）

| 存储位置 | XSS 风险 | CSRF 风险 | 推荐程度 |
|---------|---------|----------|---------|
| `localStorage` | ❌ 高（JS 可读取） | 低 | ❌ 不推荐存放 Access Token |
| `sessionStorage` | ❌ 高（JS 可读取） | 低 | ❌ 不推荐存放 Access Token |
| `HttpOnly Cookie` | ✅ 低（JS 不可读） | ⚠️ 需配合 CSRF 防护 | ✅ 推荐（配合 SameSite=Strict） |
| 内存（变量） | ✅ 低 | 低 | ✅ 推荐（刷新后需重新获取） |

!!! tip "BFF（Backend for Frontend）模式"

    SPA 安全性最佳实践是使用 BFF 模式：Token 完全保存在服务端，前端只持有 Session Cookie（HttpOnly + SameSite）。

#### 服务端

- Refresh Token 应加密存储，避免数据库泄露导致长期访问权限被盗
- 为进一步提升安全性，可以只存储令牌值的`密码学哈希`，即使数据库被攻破令牌也不会泄露
- 不应在日志中打印 Token 内容

### Refresh Token Rotation

`机制：` 每次使用 Refresh Token 换取新的 Access Token 时，同时颁发一个新的 Refresh Token，`旧的 Refresh Token 立即失效`。

``` mermaid
sequenceDiagram
    participant 客户端
    participant 授权服务器

    客户端->>授权服务器: 使用 Refresh Token A 换取新 Token
    授权服务器-->>客户端: 新 Access Token + 新 Refresh Token B（Token A 已失效）
    Note over 客户端: 存储 Refresh Token B，丢弃 A
```

`安全价值：` 如果 Refresh Token 被盗，攻击者在使用被盗 Token 后，原始客户端下次刷新时会发现 Token 已失效（Rotation 检测），可以立即告警并撤销所有相关 Token。

打个比方：这就像每次用信用卡消费后银行自动换一张新卡号——如果旧卡号被盗，下次刷卡时银行会发现异常。

### 发送者约束令牌：DPoP 与 mTLS

常规的 Bearer Token 如同现金——谁持有谁就能使用。一旦泄露，攻击者可以在任何设备上直接使用。`发送者约束令牌`通过将令牌与特定客户端的密钥绑定，使令牌即使被盗也无法在其他客户端使用。

#### DPoP（RFC 9449）

DPoP（Demonstrating Proof of Possession）通过`加密方式将令牌绑定到客户端持有的密钥对`：

1. 客户端生成一个`临时密钥对`（公钥 + 私钥）
2. 每次请求时，客户端用私钥签发一个 `DPoP Proof JWT`，包含请求的 HTTP 方法、URL 和时间戳
3. 授权服务器在颁发令牌时将令牌与该公钥绑定
4. 资源服务器验证 DPoP Proof JWT 的签名，确认请求方持有与令牌绑定的私钥

攻击者即使截获了 Access Token，也因为没有对应的私钥而无法构造有效的 DPoP Proof，令牌形同废纸。

#### mTLS（RFC 8705）

mTLS（Mutual TLS）通过`双向 TLS 认证`实现令牌绑定：

1. 客户端使用 TLS 客户端证书与授权服务器建立连接并请求令牌
2. 授权服务器将颁发的令牌与客户端证书的指纹绑定
3. 客户端访问资源服务器时，同样使用该证书建立 TLS 连接
4. 资源服务器验证 TLS 连接中的证书指纹与令牌绑定的指纹是否一致

mTLS 还可以作为`客户端认证方式`替代 `client_secret`，提供更强的身份验证，详见「核心概念 · 客户端认证方式」。

!!! info "DPoP vs mTLS"

    - `DPoP` 在应用层实现，不依赖底层 TLS 基础设施，部署更灵活，适合移动端和 SPA
    - `mTLS` 在传输层实现，需要 TLS 证书管理，安全性更强但部署成本更高，适合企业级服务端应用

## 常见攻击模式与应对

### 授权码注入

攻击者将已使用过的授权码或其他客户端的授权码注入受害者的 Token 请求。

`防护：` 使用 PKCE（`code_verifier` 绑定了请求者身份）；确保 `state` 验证。

### Token 重定向攻击

攻击者将颁发给资源服务器 A 的 Access Token 用于访问资源服务器 B。如果资源服务器 B 没有验证令牌的受众（`aud`），就会误认为该令牌是合法的，导致未授权访问。

`防护：` 授权服务器在颁发令牌时应绑定受众（`aud` 声明），资源服务器`必须验证`令牌的 `aud` 是否包含自身标识。JWT Access Token（RFC 9068）将 `aud` 列为必须声明。

### 恶意 redirect_uri

攻击者构造包含恶意 `redirect_uri` 的授权链接，诱使用户点击后将授权码发送到攻击者服务器。此外，若 `redirect_uri` 被配置为内网地址（如 `http://192.168.1.1/admin`），授权服务器在请求该地址时可能被用于`探测内网服务（SSRF）`。

`防护：` 授权服务器必须严格校验 `redirect_uri` 与注册时的值`精确匹配`（不允许通配符或模糊匹配）；不应允许 `redirect_uri` 为内网 IP 段地址。

### Token 泄露后应对

1. `短有效期`：Access Token 应设置较短有效期（5-15 分钟）
2. `令牌撤销`：实现 Revocation 端点（RFC 7009），支持主动撤销
3. `发送者约束`：将 Token 绑定到客户端密钥（DPoP 或 mTLS），即使 Token 泄露也无法在其他设备使用
4. `Refresh Token Rotation`：配合令牌轮换机制，检测并阻断被盗令牌的使用

### RFC 6750 安全建议

RFC 6750（Bearer Token Usage）除了定义 Bearer Token 的使用方式，还给出了多条安全建议。以下是值得关注的要点：

#### 短有效期令牌

RFC 6750 建议授权服务器颁发`短有效期`（1 小时或更短）的 Bearer Token，特别是颁发给浏览器环境的 Token。短有效期可以显著降低 Token 泄露后的影响时间窗口——即使 Token 被截获，攻击者的可用时间也非常有限。

#### 签发受限受众的令牌

授权服务器`应`在 Token 中包含 `aud`（受众）声明，将 Token 的使用范围限定在预期的资源服务器。这可以有效防止 Token 重定向攻击——即使 Token 被截获，攻击者也无法用它访问其他资源服务器。

#### 禁止在 Cookie 中存储 Bearer Token

RFC 6750 明确规定：Bearer Token `不得存储在可明文传输的 Cookie 中`。Cookie 默认通过明文 HTTP 传输，如果 Bearer Token 存在 Cookie 中，即使整个站点使用 HTTPS，也可能在特定场景下被泄露（如从 HTTPS 页面降级到 HTTP 时的 Cookie 发送行为）。如果必须使用 Cookie 存储，必须配合 CSRF 防护措施。

#### 验证 TLS 证书链

客户端在向资源服务器发送请求时，`必须验证` TLS 证书链。不验证证书链可能导致 DNS 劫持攻击——攻击者通过伪造 DNS 将域名指向自己的服务器，获取到客户端发送的 Bearer Token。这一要求对客户端和资源服务器之间的通信同样适用。

#### 负载均衡场景的额外防护

在某些部署架构中（如使用负载均衡器），TLS 连接在负载均衡器处终止，后端服务器与前端的通信可能不再加密。这种场景下，Token 在前端（负载均衡器）和后端（实际服务器）之间的传输`必须`有额外的机密性保护措施——例如对 Token 进行加密。

### RFC 6749 安全考量补充

RFC 6749 Section 10 还涵盖了以下安全主题，这些在现代安全实践（RFC 9700 BCP）中仍然适用：

#### Clickjacking 攻击（Section 10.13）

攻击者在透明 `iframe` 中加载授权服务器的授权页面，覆盖在伪造的"同意授权"按钮上。用户以为点击的是正常按钮，实际在 iframe 中完成了授权，导致攻击者获取了授权码。

`防护：` 授权服务器应在所有用户交互端点设置 `X-Frame-Options: DENY` 或 `SAMEORIGIN` 响应头，防止页面被嵌入第三方站点的 iframe 中。

#### 代码注入与输入验证（Section 10.14）

授权服务器和客户端都必须`验证和清理`所有接收到的参数值。特别是 `state` 和 `redirect_uri`——攻击者可能注入恶意值，在客户端的回调页面中触发 XSS（跨站脚本攻击）或构造开放重定向。

#### 开放重定向器（Section 10.15）

错误配置的重定向端点可能成为`开放重定向器`，被用于钓鱼攻击。例如，如果授权服务器允许部分匹配 `redirect_uri`（如使用路径前缀 `https://example.com/app/*`），攻击者可能注册 `https://example.com/app/evil` 并构造合法的授权链接，将用户引导到钓鱼页面。

#### 凭证猜测攻击（Section 10.10）

授权服务器`必须防止`对 Access Token、Authorization Code、Refresh Token、密码和 Client 凭证的暴力猜测攻击。RFC 6749 要求 Token 的猜测概率`必须 <= 2^(-128)`，推荐 `<= 2^(-160)`——这意味着 Token 必须使用密码学安全的随机生成器，且有足够的熵（至少 128 位）。

#### 密码安全考量（Section 10.7）

ROPC 的风险`高于其他所有授权类型`，因为它维持了 OAuth 本身要避免的密码反模式：

- 客户端在内存中`明文持有`用户密码，可能被记录、滥用或意外泄露
- 如果任何第三方客户端被攻破，用户的密码也随之泄露
- 无法使用 MFA（多因素认证）、SSO（单点登录）等高级认证方式
- `应最小化使用`，仅在遗留系统迁移场景中作为过渡方案

## OAuth 安全最佳实践要点（RFC 9700）

如果你只记一件事，就记住下表——它是 RFC 9700（OAuth 2.0 Security Best Current Practice）的精华总结。

RFC 9700（OAuth 2.0 Security Best Current Practice）在主要提供商的实施经验和 RFC 6819 威胁模型基础上，总结了 OAuth 2.0 客户端和服务器的安全要求。以下是核心要点：

| 要求 | 说明 |
|------|------|
| 始终使用 PKCE | 所有授权码流程必须使用 PKCE，包括机密客户端 |
| 禁用隐式流程 | 不应使用 `response_type=token`，改用授权码流程 + PKCE |
| 禁用 ROPC | 不应使用密码授权类型（`grant_type=password`） |
| 精确匹配 redirect_uri | 授权服务器必须进行完全字符串匹配，不允许通配符 |
| 使用 state 或 PKCE 防 CSRF | 至少使用其中一种机制防止跨站请求伪造 |
| Access Token 短有效期 | 限制令牌泄露后的影响窗口 |
| 发送者约束令牌 | 推荐使用 DPoP 或 mTLS 绑定令牌到客户端 |
| Refresh Token Rotation | 公开客户端必须使用轮换机制 |
| Token 不应在 URL 中传递 | 避免 Token 出现在浏览器历史记录或服务器日志中 |

---

`上一篇：` [JWT 令牌](../jwt/index.md)
`下一篇：` [实战：授权服务器](../spring-auth-server/index.md)
