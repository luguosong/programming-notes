# JWT 令牌

JWT（JSON Web Token，RFC 7519）是 OAuth2 和 OIDC 中最常用的令牌格式。它是一种`自描述的、紧凑的、URL 安全的`令牌，能在各方之间传递声明（Claims）。

## JWT 结构

JWT 由三部分组成，用 `.` 分隔：

```
Header.Payload.Signature
```

例如：
```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9
.eyJzdWIiOiIxMjM0NTYiLCJzY29wZSI6InJlYWQiLCJleHAiOjE3NTMxMjMyMDB9
.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

每部分均为 `Base64url 编码`（非加密，可解码查看）。

### Header（头部）

声明令牌类型和签名算法：

``` json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id-1"
}
```

### Payload（载荷）

包含实际的声明数据（Claims）：

``` json
{
  "sub": "user-123",
  "iss": "https://auth.example.com",
  "aud": "my-api",
  "exp": 1753123200,
  "iat": 1753119600,
  "scope": "read write"
}
```

!!! warning "Payload 是 Base64url 编码，不是加密"
    JWT Payload 中的内容任何人都可以解码查看。`不要在 Payload 中存放敏感信息`（如密码、信用卡号）。若需要加密，应使用 JWE。

### Signature（签名）

签名是对 `Header.Payload` 的数字签名，用于`防止篡改`：

```
RSASSA-PKCS1-v1_5(
  BASE64URL(Header) + "." + BASE64URL(Payload),
  privateKey
)
```

验证者持有公钥，可以验证签名，但无法伪造。

## JWS vs JWE

| 格式 | 全称 | 作用 | 常见场景 |
|------|------|------|---------|
| `JWS` | JSON Web Signature | 签名（防篡改），内容可见 | Access Token、ID Token（绝大多数场景） |
| `JWE` | JSON Web Encryption | 加密（防窃取），内容不可见 | 包含敏感声明的令牌 |

普通的 `Header.Payload.Signature` 三段式 JWT 就是 JWS 格式。

## 签名算法对比

| 算法 | 类型 | 密钥 | 适用场景 |
|------|------|------|---------|
| `RS256` | 非对称（RSA） | 私钥签名，公钥验证 | 生产环境推荐，资源服务器只需公钥即可验证 |
| `ES256` | 非对称（ECDSA） | 私钥签名，公钥验证 | 比 RS256 更小的密钥和签名，性能更好 |
| `HS256` | 对称（HMAC） | 同一密钥签名和验证 | 仅适合单体应用，微服务场景中需共享密钥存在安全风险 |

!!! tip "生产环境推荐 RS256 或 ES256"
    非对称算法允许授权服务器私钥签名，任何持有公钥的资源服务器都可以验证，且不需要共享私钥，安全性更高。

## JWKS 端点（公钥集合）

授权服务器通过 `JWKS（JSON Web Key Set）端点`发布公钥，资源服务器可以动态获取用于验证 Token 的公钥。

``` http
GET /oauth2/jwks
```

响应示例：

``` json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "key-id-1",
      "use": "sig",
      "n": "oBH5...",
      "e": "AQAB"
    }
  ]
}
```

资源服务器根据 JWT Header 中的 `kid` 字段，在 JWKS 中找到对应公钥进行签名验证。

## 令牌验证完整流程

资源服务器收到携带 JWT 的请求后，验证步骤：

``` mermaid
flowchart TD
    A[收到 Bearer Token] --> B[Base64url 解码 Header]
    B --> C[根据 kid 从 JWKS 获取公钥]
    C --> D{验证签名}
    D -->|失败| E[返回 401 Unauthorized]
    D -->|成功| F{验证 exp 是否过期}
    F -->|过期| E
    F -->|未过期| G{验证 iss 是否匹配}
    G -->|不匹配| E
    G -->|匹配| H{验证 aud 是否包含本服务}
    H -->|不匹配| E
    H -->|匹配| I[提取 scope/sub 等声明进行业务授权]
    I --> J[返回受保护资源]
```

## Opaque Token vs JWT

| 维度 | Opaque Token | JWT |
|------|-------------|-----|
| `格式` | 随机字符串（无信息） | 结构化 JSON（含声明） |
| `验证方式` | 调用 Introspection 端点 | 本地验证签名 |
| `网络开销` | 每次请求都需远程验证 | 仅首次获取公钥，之后本地验证 |
| `令牌撤销` | 即时生效（授权服务器删除即可） | 有延迟（只能等 exp 过期） |
| `适用场景` | 需要即时撤销；资源服务器无法缓存 | 高并发；接受一定撤销延迟 |

!!! tip "选择建议"
    大多数场景下选 JWT。若有严格的即时撤销需求（如高安全场景），考虑 Opaque Token 配合 Introspection，或使用 JWT + 短有效期策略。

## JWT Access Token 规范（RFC 9068）

RFC 9068 为 OAuth 2.0 Access Token 定义了标准的 JWT 编码方式（JWT Profile for Access Tokens），规定了一组`必须包含的声明`，使不同授权服务器颁发的 JWT Access Token 具有统一结构，资源服务器可以用一致的方式验证和解析。

`RFC 9068 必须包含的声明：`

| 声明 | 说明 |
|------|------|
| `iss` | 颁发者标识（授权服务器的 URL） |
| `exp` | 过期时间（Unix 时间戳） |
| `aud` | 受众（令牌预期的资源服务器标识） |
| `sub` | 主体（用户标识或客户端标识） |
| `client_id` | 请求该令牌的客户端标识 |
| `iat` | 签发时间 |
| `jti` | 令牌唯一标识（用于防重放） |

`Header 中的额外要求：`

- `typ` 必须设为 `at+jwt`，用于将 JWT Access Token 与其他类型的 JWT（如 ID Token）区分开

``` json title="JWT Access Token 示例（RFC 9068 格式）"
// Header
{
  "alg": "RS256",
  "typ": "at+jwt",
  "kid": "key-id-1"
}
// Payload
{
  "iss": "https://auth.example.com",
  "exp": 1753123200,
  "aud": "https://api.example.com",
  "sub": "user-123",
  "client_id": "my-client-app",
  "iat": 1753119600,
  "jti": "unique-token-id-abc123",
  "scope": "read write"
}
```

`RFC 9068 的价值：` 在此规范之前，各授权服务器的 JWT Access Token 格式各不相同，资源服务器需要针对每个授权服务器编写专门的解析逻辑。RFC 9068 通过统一声明集和 `typ: at+jwt` 标识，让资源服务器可以用`通用逻辑`验证来自不同授权服务器的 Access Token。同时，将令牌信息全部编码在 JWT 自身中，无需查询数据库即可验证，适合高并发场景。

---

`上一篇：` [OpenID Connect](../openid-connect/index.md)
`下一篇：` [安全实践](../security/index.md)
