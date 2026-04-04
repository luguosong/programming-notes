# JWT 令牌

JWT（JSON Web Token，RFC 7519）是 OAuth2 和 OIDC 中最常用的令牌格式。

**把它想象成一封透明的密封信件**：信封上写着收件人、发件人、过期时间（Header + Payload 里的 Claims），信封口盖有一个蜡封（Signature）。任何人拿起这封信都能透过透明信封看到里面的内容（Base64url 编码，不是加密），但只有拿到信封的人无法伪造蜡封——除非他偷到了发信人的印章（私钥）。

JWT 是一种`自描述的、紧凑的、URL 安全的`令牌，能在各方之间传递声明（Claims）。

**本文你会学到：**
- 🧩 JWT 的三段式结构分别是什么，各自包含什么信息
- 📋 七个注册声明（iss、sub、aud、exp、nbf、iat、jti）的含义与验证规则
- 🔑 JWK（JSON Web Key）的结构、参数和密钥类型
- ✍️ JWS（签名）和 JWE（加密）的区别
- 🔧 常见签名算法的适用场景
- ✅ 资源服务器验证 JWT 的完整流程
- 🆚 Opaque Token 与 JWT 的选择依据
- 📜 RFC 9068 对 JWT Access Token 的标准化要求

## 🧩 JWT 结构

JWT 由三部分组成，用 `.` 分隔。继续用密封信件的类比——Header 是信封信息，Payload 是信件内容，Signature 是蜡封：

```
Header.Payload.Signature
```

例如（先有个直观感受，后面逐段解释）：
```
eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9
.eyJzdWIiOiIxMjM0NTYiLCJzY29wZSI6InJlYWQiLCJleHAiOjE3NTMxMjMyMDB9
.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

每部分均为 `Base64url 编码`——注意，这只是编码，不是加密！就像把中文翻译成拼音，任何人都能还原，只是方便在 URL 里传输。

### Header（头部）

声明令牌类型和签名算法——信封上的基本信息：

``` json
{
  "alg": "RS256",
  "typ": "JWT",
  "kid": "key-id-1"
}
```

### Payload（载荷）

包含实际的声明数据（Claims）——信件里的内容：

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
    JWT Payload 中的内容任何人都可以 Base64url 解码查看——就像透明信封，谁都能看里面写了什么。`不要在 Payload 中存放敏感信息`（如密码、信用卡号）。若需要加密，应使用 JWE（也就是换成不透明的信封）。

### 注册声明（Registered Claims）

RFC 7519 定义了七个`注册声明`，它们是 JWT 生态中最常用、最具互操作性的声明。每个声明都有标准的语义，不同系统能够统一理解和处理。

!!! info "声明的三类命名空间"

    RFC 7519 将声明分为三类：
    - `注册声明`（Registered Claims）：IANA 注册的标准声明（如 `iss`、`exp`），互操作性最好
    - `公共声明`（Public Claims）：使用碰撞 resistant 命名空间的自定义声明（如 `http://example.com/is_root`）
    - `私有声明`（Private Claims）：生产者和消费者私下约定的声明，仅在封闭环境使用，存在命名冲突风险

| 声明 | 全称 | 类型 | 说明 |
|------|------|------|------|
| `iss` | Issuer | StringOrURI | 令牌的签发者（如授权服务器的 URL）。资源服务器用它确认令牌来自可信的授权服务器 |
| `sub` | Subject | StringOrURI | 令牌的主体（通常是用户标识）。声明中的其他信息通常是关于这个主体的 |
| `aud` | Audience | StringOrURI 或数组 | 令牌的目标受众（预期的接收方）。`必须处理该 JWT 的每一方都应在 aud 中找到自己的标识`，否则`必须拒绝`该 JWT |
| `exp` | Expiration Time | NumericDate | 过期时间（Unix 时间戳，秒）。当前时间`必须早于`此值，否则拒绝。实现者`可以`预留少量时钟偏差容忍度（通常不超过几分钟） |
| `nbf` | Not Before | NumericDate | 生效时间（Unix 时间戳，秒）。当前时间`必须晚于或等于`此值。同样可以预留时钟偏差容忍度 |
| `iat` | Issued At | NumericDate | 签发时间（Unix 时间戳，秒）。可用于判断令牌的"年龄"，辅助实现刷新策略 |
| `jti` | JWT ID | String | 令牌的唯一标识符。分配方式必须确保`极低碰撞概率`（跨不同签发者也不能重复）。可用于防止令牌重放 |

> `aud` 的值类型是特殊的：当只有一个受众时可以是单个字符串，有多个受众时必须是字符串数组。例如 `"aud": "my-api"` 或 `"aud": ["api-1", "api-2"]`。

!!! important "exp 和 aud 是最关键的验证声明"

    所有声明都是可选的（取决于具体应用），但 `exp` 和 `aud` 在安全场景中几乎`必须验证`：
    - 不验证 `exp` → 过期令牌可能被接受
    - 不验证 `aud` → 令牌可能被用于非预期的资源服务器（令牌重定向攻击）

### Signature（签名）

签名是对 `Header.Payload` 的数字签名——信封上的蜡封，用于`防止篡改`：

```
RSASSA-PKCS1-v1_5(
  BASE64URL(Header) + "." + BASE64URL(Payload),
  privateKey
)
```

验证者持有公钥，可以验证签名，但无法伪造——就像你有发信人的印章图案（公钥），可以核对蜡封是不是真的，但没有印章本身（私钥）就盖不出新的。

## ✍️ JWS vs JWE

既然提到了 JWE，简单说一下两者的区别：

| 格式 | 全称 | 作用 | 常见场景 |
|------|------|------|---------|
| `JWS` | JSON Web Signature | 签名（防篡改），内容可见 | Access Token、ID Token（绝大多数场景） |
| `JWE` | JSON Web Encryption | 加密（防窃取），内容不可见 | 包含敏感声明的令牌 |

普通的 `Header.Payload.Signature` 三段式 JWT 就是 JWS 格式。

## 🔧 签名算法对比

签名算法决定了蜡封的「防伪级别」。核心区别在于密钥的管理方式：

| 算法 | 类型 | 密钥 | 适用场景 |
|------|------|------|---------|
| `RS256` | 非对称（RSA） | 私钥签名，公钥验证 | 生产环境推荐，资源服务器只需公钥即可验证 |
| `ES256` | 非对称（ECDSA） | 私钥签名，公钥验证 | 比 RS256 更小的密钥和签名，性能更好 |
| `HS256` | 对称（HMAC） | 同一密钥签名和验证 | 仅适合单体应用，微服务场景中需共享密钥存在安全风险 |

!!! tip "生产环境推荐 RS256 或 ES256"
    非对称算法允许授权服务器私钥签名，任何持有公钥的资源服务器都可以验证，且不需要共享私钥——就像银行用私钥盖章，所有分行用公钥验证，谁都不需要把印章实物送来送去，安全性更高。

## 🔑 JWKS 端点与 JWK 结构（RFC 7517）

### 为什么需要 JWKS 端点

问题来了：资源服务器怎么拿到授权服务器的公钥来验证签名？不可能硬编码在代码里——密钥会轮换。

答案是**JWKS（JSON Web Key Set）端点**——授权服务器通过这个端点`公开发布`自己的公钥，资源服务器可以动态获取。

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

!!! info "JWKS 端点的缓存策略"

    资源服务器`可以缓存` JWKS 响应以减少对授权服务器的调用。授权服务器应在响应中包含适当的缓存头（如 `Cache-Control`），并在密钥轮换前提前发布新密钥到 JWKS，设置旧的密钥足以覆盖现有 Token 的有效期。旧密钥过期后从 JWKS 中移除。

### JWK 格式详解

JWK（JSON Web Key，RFC 7517）是一个 JSON 对象，表示一个`密码学密钥`。JWK Set 是一个包含多个 JWK 的 JSON 对象，`必须`包含 `keys` 数组成员。一个 JWKS 响应中可以有多个密钥（例如轮换期间的旧密钥和新密钥）。

#### 通用参数

每个 JWK 都包含以下通用参数（与密钥类型无关）：

| 参数 | 说明 |
|------|------|
| `kty` | ✅ 必须。密钥类型（Key Type），如 `RSA`、`EC`、`oct`（对称密钥） |
| `use` | 公钥用途：`sig`（签名/验证）、`enc`（加密/解密）。与 `key_ops` 不应同时使用 |
| `key_ops` | 密钥操作数组，比 `use` 更细粒度：`sign`、`verify`、`encrypt`、`decrypt`、`wrapKey`、`unwrapKey`、`deriveKey`、`deriveBits` |
| `alg` | 密钥的预期算法（如 `RS256`、`ES256`） |
| `kid` | 密钥标识符，用于在密钥集中匹配特定密钥。密钥轮换时新旧密钥有不同的 `kid` |

> `use` 和 `key_ops` 不应同时使用。如果同时使用，两者传达的信息`必须一致`。`use` 更简洁（只有 sig/enc 两个值），`key_ops` 更灵活但更复杂。

#### X.509 证书参数

JWK 还可以包含与 X.509 证书关联的参数，用于将公钥与传统 PKI 体系对接：

| 参数 | 说明 |
|------|------|
| `x5u` | X.509 证书或证书链的 URL（`必须`通过 HTTPS 获取） |
| `x5c` | X.509 证书链数组（base64 编码的 DER 证书，第一个包含公钥） |
| `x5t` | X.509 证书的 SHA-1 指纹（base64url 编码） |
| `x5t#S256` | X.509 证书的 SHA-256 指纹（base64url 编码，推荐） |

#### RSA 密钥参数

| 参数 | 说明 |
|------|------|
| `n` | RSA 模数（base64url 编码） |
| `e` | RSA 公共指数（base64url 编码，通常为 `AQAB`，即 65537） |
| `d` | RSA 私钥指数（base64url 编码，仅私钥） |
| `p`、`q` | RSA 素因子（仅私钥） |
| `dp`、`dq`、`qi` | RSA CRT 参数（仅私钥） |

#### EC（椭圆曲线）密钥参数

| 参数 | 说明 |
|------|------|
| `crv` | 曲线名称（如 `P-256`、`P-384`、`P-521`） |
| `x` | 公钥的 X 坐标（base64url 编码） |
| `y` | 公钥的 Y 坐标（base64url 编码） |
| `d` | 私钥值（base64url 编码，仅私钥） |

#### 对称密钥参数

| 参数 | 说明 |
|------|------|
| `k` | 对称密钥值（base64url 编码） |
| `kty` | 值为 `oct`（Octet sequence） |

!!! warning "JWK 中的私钥安全"

    JWK `可以`表示私钥（包含 `d`、`p`、`q` 等参数），但`必须防止`未经授权的方访问非公钥信息。RFC 7517 推荐使用 JWE 加密包含私钥的 JWK（即 Encrypted JWK）。在 OAuth 场景中，JWKS 端点`只应发布公钥`，绝不能暴露私钥。

#### JWK 示例

一个 RSA 公钥（用于 RS256 签名验证）：

``` json
{
  "kty": "RSA",
  "kid": "2024-01-01",
  "use": "sig",
  "alg": "RS256",
  "n": "oAHV...（base64url 编码的 RSA 模数）",
  "e": "AQAB"
}
```

一个 EC 公钥（用于 ES256 签名验证，DPoP 中常用）：

``` json
{
  "kty": "EC",
  "kid": "dpop-key-1",
  "crv": "P-256",
  "x": "l8tFrhx-34tV3hRICRDY9zCkDlpBhF42UQUfWVAWBFs",
  "y": "9VE4jf_Ok_o64zbTTlcuNJajHmt6v9TDVrU0CdvGRDA"
}
```

一个 JWK Set（包含两个密钥，支持密钥轮换）：

``` json
{
  "keys": [
    {
      "kty": "RSA",
      "kid": "2024-01-01",
      "use": "sig",
      "alg": "RS256",
      "n": "oAHV...",
      "e": "AQAB"
    },
    {
      "kty": "RSA",
      "kid": "2023-06-01",
      "use": "sig",
      "alg": "RS256",
      "n": "vxBM...",
      "e": "AQAB"
    }
  ]
}
```

> 注意：JWK Set 中 `keys` 数组的顺序`不暗示`优先级（除非应用层自行定义）。实现遇到不认识的 `kty`、缺少必须参数或参数值超出支持范围的 JWK 时，`应忽略`它。

## ✅ 令牌验证完整流程

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
    classDef step fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:1px
    classDef decision fill:transparent,stroke:#e3b341,color:#adbac7,stroke-width:1px
    classDef error fill:transparent,stroke:#e5534b,color:#adbac7,stroke-width:1px
    classDef success fill:transparent,stroke:#57ab5a,color:#adbac7,stroke-width:2px
    class A,B,C step
    class D,F,G,H decision
    class E error
    class I,J success
```

## 🆚 Opaque Token vs JWT

除了 JWT，OAuth2 中还有另一种常见的令牌格式：Opaque Token。它们怎么选？简单来说：

| 维度 | Opaque Token | JWT |
|------|-------------|-----|
| `格式` | 随机字符串（无信息） | 结构化 JSON（含声明） |
| `验证方式` | 调用 Introspection 端点 | 本地验证签名 |
| `网络开销` | 每次请求都需远程验证 | 仅首次获取公钥，之后本地验证 |
| `令牌撤销` | 即时生效（授权服务器删除即可） | 有延迟（只能等 exp 过期） |
| `适用场景` | 需要即时撤销；资源服务器无法缓存 | 高并发；接受一定撤销延迟 |

!!! tip "选择建议"
    大多数场景下选 JWT。若有严格的即时撤销需求（如高安全场景），考虑 Opaque Token 配合 Introspection，或使用 JWT + 短有效期策略。

## 📜 JWT Access Token 规范（RFC 9068）

前面提到的 Opaque Token vs JWT 对比，其实反映了一个现实问题：**各家授权服务器颁发的 JWT Access Token 格式各不相同**。资源服务器需要针对每个授权服务器写专门的解析逻辑，很麻烦。

RFC 9068 就是来解决这个问题——它为 OAuth 2.0 Access Token 定义了`标准的 JWT 编码方式`（JWT Profile for Access Tokens），规定了一组`必须包含的声明`，让不同授权服务器颁发的 JWT Access Token 具有统一结构，资源服务器可以用一致的方式验证和解析。

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

> 📝 **小结**：JWT 的核心优势是自包含 + 本地验证，劣势是撤销有延迟。大多数场景选 JWT + 短有效期就够了。如果有严格的即时撤销需求，考虑 Opaque Token 配合 Introspection。

## 🛡️ JWT 安全考量

### 算法篡改攻击（alg: none）

RFC 7519 定义了 `Unsecured JWT`——使用 `alg: "none"` 且签名为空字符串的 JWT，用于由外部机制（如 TLS）提供安全保障的场景。但在 OAuth2 中，`alg: none` `绝对不应被接受`——否则攻击者可以将任何 JWT 的签名算法改为 `none`，伪造任意令牌。

`防护`：JWT 验证时`必须检查` `alg` 声明是否在应用允许的算法列表中（如 `["RS256", "ES256"]`），拒绝未知或不安全的算法。

### 密钥来源信任

JWK 和 JWKS 的公钥来自授权服务器。如果攻击者能篡改 JWKS 响应（如通过 DNS 劫持），就能用自己的公钥替换合法公钥，从而签发"合法"的 JWT。

`防护`：
- 通过 `iss` 声明确定 JWKS 端点的来源（通过授权服务器元数据发现 `jwks_uri`）
- 获取 JWKS 时`必须验证` TLS 证书链
- 可以通过 `x5c` 或 `x5t#S256` 参数将公钥绑定到受信任的 X.509 证书，建立额外的信任链

### 不安全算法与密钥长度

`防护`：
- HMAC 算法（`HS256`）的密钥`必须`有足够的熵（至少 256 位），使用弱密钥（如密码字符串）可被暴力破解
- RSA 密钥长度`至少 2048 位`，推荐 3072 或 4096 位
- ECDSA 使用 `P-256` 及以上曲线

### 不要在 Payload 中存放敏感信息

Payload 只是 Base64url 编码，任何人都能解码查看。以下内容`不应`出现在 JWT 中：密码、信用卡号、个人身份信息（PII）、健康记录等。如果必须传递敏感信息，应使用 JWE 加密整个 JWT。

!!! info "关于 Claim 命名冲突"

    自定义声明时，优先使用公共声明命名空间（如 `https://example.com/claims/custom_claim`）避免与注册声明或其他应用冲突。私有声明仅在封闭环境中使用，且存在命名冲突风险——不同服务可能使用相同的短名称表示不同含义。

---

`上一篇：` [令牌体系](../token-system/index.md)
`下一篇：` [端点与发现](../endpoints/index.md)
