# OAuth2 安全实践

正确理解并实现 OAuth2 并不简单。本文梳理常见的安全威胁和对应的防护机制。各安全机制的流程实现见[授权类型](../grant-types/index.md)。

## PKCE：防止授权码截获攻击

### 攻击场景

在没有 PKCE 的情况下，移动应用使用自定义 URL Scheme（如 `myapp://callback`）接收授权码。恶意应用可以注册相同的 URL Scheme，在操作系统层面拦截回调，从而获得授权码。

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

### PKCE 如何防御

PKCE 在客户端生成 `code_verifier`（随机密钥），并在授权请求时发送其哈希值 `code_challenge`。Token 端点在换取 Token 时要求客户端提供原始 `code_verifier` 进行验证。

**恶意应用只截获了授权码，却不知道 `code_verifier`，无法通过 Token 端点的验证。**

!!! success "PKCE 的使用建议"
    - `所有公开客户端（SPA、移动端）`：PKCE 为必选
    - `机密客户端（Web 应用服务端）`：也应使用 PKCE（额外安全层）

PKCE 的流程机制详见[授权类型 · PKCE](../grant-types/index.md#pkceproof-key-for-code-exchange)。

## state 参数：防 CSRF 攻击

### 攻击场景

CSRF 攻击通过诱使已登录用户点击精心构造的链接，将攻击者的授权码绑定到受害者账号。

### 防护方式

客户端在授权请求中包含随机 `state` 参数，授权服务器原样返回。客户端回调时**必须验证 `state` 与发出请求时的值一致**。

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

## nonce：防 ID Token 重放攻击（OIDC）

### 攻击场景

攻击者截获一个合法用户的 ID Token，在其过期前将其发送给另一个系统假冒该用户身份。

### 防护方式

客户端在 OIDC 授权请求中携带随机 `nonce`，授权服务器将 `nonce` 嵌入 ID Token。客户端验证 ID Token 时检查 `nonce` 是否与请求时一致。

## 令牌存储安全

### 浏览器端（SPA）

| 存储位置 | XSS 风险 | CSRF 风险 | 推荐程度 |
|---------|---------|----------|---------|
| `localStorage` | ❌ 高（JS 可读取） | 低 | ❌ 不推荐存放 Access Token |
| `sessionStorage` | ❌ 高（JS 可读取） | 低 | ❌ 不推荐存放 Access Token |
| `HttpOnly Cookie` | ✅ 低（JS 不可读） | ⚠️ 需配合 CSRF 防护 | ✅ 推荐（配合 SameSite=Strict） |
| 内存（变量） | ✅ 低 | 低 | ✅ 推荐（刷新后需重新获取） |

!!! tip "BFF（Backend for Frontend）模式"
    SPA 安全性最佳实践是使用 BFF 模式：Token 完全保存在服务端，前端只持有 Session Cookie（HttpOnly + SameSite）。

### 服务端

- Refresh Token 应加密存储，避免数据库泄露导致长期访问权限被盗
- 不应在日志中打印 Token 内容

## Refresh Token Rotation

### 什么是 Refresh Token Rotation

每次使用 Refresh Token 换取新的 Access Token 时，同时颁发一个新的 Refresh Token，`旧的 Refresh Token 立即失效`。

``` mermaid
sequenceDiagram
    participant 客户端
    participant 授权服务器

    客户端->>授权服务器: 使用 Refresh Token A 换取新 Token
    授权服务器-->>客户端: 新 Access Token + 新 Refresh Token B（Token A 已失效）
    Note over 客户端: 存储 Refresh Token B，丢弃 A
```

### 安全价值

如果 Refresh Token 被盗，攻击者在使用被盗 Token 后，原始客户端下次刷新时会发现 Token 已失效（Rotation 检测），可以立即告警并撤销所有相关 Token。

## 常见攻击模式

### 授权码注入

攻击者将已使用过的授权码或其他客户端的授权码注入受害者的 Token 请求。

`防护：` 使用 PKCE（code_verifier 绑定了请求者身份）；确保 `state` 验证。

### 恶意 redirect_uri

攻击者构造包含恶意 `redirect_uri` 的授权链接，诱使用户点击后将授权码发送到攻击者服务器。此外，若 `redirect_uri` 被配置为内网地址（如 `http://192.168.1.1/admin`），授权服务器在请求该地址时可能被用于`探测内网服务（SSRF）`。

`防护：` 授权服务器必须严格校验 `redirect_uri` 与注册时的值`精确匹配`（不允许通配符或模糊匹配）；不应允许 `redirect_uri` 为内网 IP 段地址。

### Token 泄露后应对

1. `短有效期`：Access Token 应设置较短有效期（5-15 分钟）
2. `令牌撤销`：实现 Revocation 端点（RFC 7009），支持主动撤销
3. `令牌绑定`：将 Token 绑定到客户端 TLS 证书（DPoP，RFC 9449），即使 Token 泄露也无法在其他设备使用

---

`上一篇：` [JWT 令牌](../jwt/index.md)
`下一篇：` [实战：授权服务器](../spring-auth-server/index.md)
