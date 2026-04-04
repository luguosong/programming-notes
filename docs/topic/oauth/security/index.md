# OAuth2 安全实践

正确理解并实现 OAuth2 并不简单。本文按照 OAuth2 交互的阶段梳理安全威胁和防护机制：授权请求阶段如何防止篡改与劫持、令牌颁发后如何安全存储与使用、以及常见攻击模式的应对策略。各安全机制的流程实现见「授权流程」。

**本文你会学到：**

- 🔐 PKCE 如何防止授权码被截获后滥用
- 🎯 state 和 nonce 参数各自的防御目标
- 📤 PAR 如何将授权请求从浏览器 URL 中隐藏
- 🗄️ 令牌安全存储的最佳实践（SPA 和服务端）
- 🔄 Refresh Token Rotation 的安全价值
- 🧬 DPoP（RFC 9449）核心机制：Proof JWT 结构、令牌绑定、公钥确认、Nonce 防护
- ⚖️ DPoP 与 mTLS 两种发送者约束令牌的对比与适用场景
- ⚔️ 常见攻击模式（授权码注入、Token 重定向等）的应对策略
- 📋 RFC 6750 对 Bearer Token 的安全建议（短有效期、Cookie 禁令、TLS 证书链验证等）

## 🚧 授权请求阶段防护

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

`PKCE 如何防御：` 客户端生成随机 `code_verifier`，授权请求中发送其哈希 `code_challenge`，Token 端点要求提供原始 `code_verifier` 验证。**恶意应用只截获了授权码，却不知道 `code_verifier`，无法通过验证。**

!!! success "PKCE 的使用建议"

    - `所有公开客户端（SPA、移动端）`：PKCE 为必选
    - `机密客户端（Web 应用服务端）`：也应使用 PKCE（额外安全层）
    - PKCE 不是客户端认证的替代品，使用客户端认证的应用也应该同时使用 PKCE

PKCE 的流程机制详见「授权流程 · PKCE」。

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

常规授权流程中，授权请求参数通过浏览器 URL 传递，存在以下问题：

- 参数对浏览器和中间网络`可见`，可能被日志记录或第三方脚本读取
- 请求未经认证，任何人都可以`伪造`授权链接

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

客户端先将完整的授权参数通过后端通道提交给 PAR 端点，获得短效的 `request_uri` 引用。重定向到授权端点时只需携带 `client_id` 和 `request_uri`——请求参数不再暴露在浏览器 URL 中。

## 🔑 令牌安全

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

- Refresh Token 应加密存储，推荐只存储令牌值的`密码学哈希`
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

`安全价值：` 如果 Refresh Token 被盗，原始客户端下次刷新时会发现 Token 已失效（Rotation 检测），可以立即告警并撤销所有相关 Token——类似信用卡消费后自动换卡号。

### 发送者约束令牌：DPoP 与 mTLS

常规 Bearer Token 如同现金——谁持有谁就能使用。`发送者约束令牌`将令牌与特定客户端的密钥绑定，即使泄露也无法在其他客户端使用。

#### DPoP（RFC 9449）

DPoP（Demonstrating Proof of Possession，RFC 9449）是一种`应用层`的令牌绑定机制——它通过让客户端证明自己持有某个密钥对，将 Access Token 绑定到该密钥对上。就像给门禁卡加上了**指纹验证**——刷卡不够，还得验证指纹，别人即使偷了你的卡也刷不开。

``` mermaid
sequenceDiagram
    participant Client as 客户端
    participant AS as 授权服务器
    participant RS as 资源服务器

    Client->>AS: (A) Token 请求<br/>（携带 DPoP Proof）
    Note right of Client: DPoP Proof 包含<br/>公钥 + HTTP 方法 + URL + 时间戳
    AS-->>Client: (B) DPoP-Bound Token<br/>（token_type=DPoP）
    Note left of AS: 令牌与客户端公钥绑定

    Client->>RS: (C) 资源请求<br/>（携带 Token + 新的 DPoP Proof）
    Note right of Client: Proof 还包含 Access Token 的哈希
    RS-->>Client: (D) 返回受保护资源
    Note left of RS: 验证：Proof 公钥 == Token 绑定的公钥<br/>且 Proof 中的 Token 哈希 == 实际 Token
```

核心流程分四步：(A) 客户端在令牌请求中附带 DPoP Proof 证明持有密钥对；(B) 授权服务器将令牌与该公钥绑定并返回 `token_type=DPoP`；(C) 客户端在访问资源时再次附带 DPoP Proof；(D) 资源服务器验证公钥匹配和 Token 哈希。

!!! info "DPoP 不是客户端认证"

    DPoP 用于`发送者约束令牌`，它本身`不是`客户端认证方式。一个主要使用场景就是公开客户端（SPA、移动端），这些客户端不使用客户端认证。但 DPoP 与 `private_key_jwt` 等客户端认证方式完全兼容。

##### DPoP Proof JWT 结构

DPoP Proof 是一个客户端用私钥签发的 JWT，通过 `DPoP` HTTP 请求头传递——每个 HTTP 请求都需要一个`唯一的` Proof。

`JOSE Header`（必须字段）：

| 字段 | 说明 |
|------|------|
| `typ` | 固定值 `dpop+jwt`，用于将 DPoP JWT 与其他 JWT 区分开来，防止 JWT 替换攻击 |
| `alg` | 非对称签名算法（如 `ES256`、`PS256`），`禁止`使用 `none` 或对称算法（MAC） |
| `jwk` | 客户端的公钥（JWK 格式），`不得`包含私钥 |

`JWT Payload`（必须字段）：

| 字段 | 说明 |
|------|------|
| `jti` | Proof 的唯一标识符（至少 96 位随机数据或 UUID v4），服务器用于检测和防止重放 |
| `htm` | 当前请求的 HTTP 方法（如 `POST`、`GET`） |
| `htu` | 当前请求的 URI（不含查询参数和片段） |
| `iat` | JWT 的创建时间（Unix 时间戳） |

`条件字段`（在特定场景下必须包含）：

| 字段 | 场景 | 说明 |
|------|------|------|
| `ath` | 访问受保护资源时 | Access Token 的 SHA-256 哈希（base64url 编码），将 Proof 与特定 Token 绑定 |
| `nonce` | 服务器要求时 | 服务器通过 `DPoP-Nonce` 响应头提供的不可预测的随机值 |

一个用于 Token 请求的 DPoP Proof JWT（解码后）：

``` json
{"typ":"dpop+jwt","alg":"ES256","jwk":{"kty":"EC","x":"l8tFrhx-34tV3hRICRDY9zCkDlpBhF42UQUfWVAWBFs","y":"9VE4jf_Ok_o64zbTTlcuNJajHmt6v9TDVrU0CdvGRDA","crv":"P-256"}}
{"jti":"-BwC3ESc6acc2lTc","htm":"POST","htu":"https://server.example.com/token","iat":1562262616}
```

> 注意：DPoP Proof 只签名了 HTTP 方法和 URL，`不签名`请求体和其他请求头。这是刻意为之的设计——避免了 HTTP 消息规范化的复杂难题。消息完整性依赖 TLS 层保障。

##### 令牌请求与绑定

客户端在向令牌端点请求 Token 时，必须在请求中包含 `DPoP` 头（携带 DPoP Proof JWT）。授权服务器验证 Proof 后，将颁发的 Access Token 与客户端公钥绑定，并返回 `token_type: "DPoP"`。

!!! important "`token_type=DPoP` 是关键信号"

    客户端收到 `token_type=DPoP` 后才知道令牌已被绑定，后续`必须`使用 DPoP 方式访问资源。

`Refresh Token 的绑定规则：`

- `公开客户端`：Refresh Token `必须`绑定到 DPoP 公钥。后续使用该 Refresh Token 刷新时，必须使用`同一密钥对`的 DPoP Proof
- `机密客户端`：Refresh Token `不绑定` DPoP 公钥——因为机密客户端已有客户端认证（`client_secret` 等）作为发送者约束机制，绑定到特定公钥反而限制了凭证轮换的灵活性
- 授权服务器`可以`选择只绑定 Refresh Token 而不绑定 Access Token（返回 `token_type=Bearer`），这在资源服务器尚未升级支持 DPoP 时是一种过渡策略

使用 Refresh Token 刷新时，同样需要附带 DPoP Proof（HTTP 格式与令牌请求相同，仅 `grant_type` 和参数不同）。

##### 公钥确认机制（cnf.jkt）

资源服务器如何知道一个 Access Token 绑定了哪个公钥？RFC 9449 定义了 `JWK Thumbprint` 确认方法——将公钥的 SHA-256 哈希（JWK Thumbprint，RFC 7638）存入 Token 中。资源服务器通过比对哈希值来验证公钥是否匹配。

`JWT Access Token`：公钥信息直接嵌入 Token 的 `cnf.jkt` 声明中：

``` json
{
  "sub": "someone@example.com",
  "iss": "https://server.example.com",
  "exp": 1562266216,
  "cnf": {
    "jkt": "0ZcOCORZNYy-DWpqq30jZyJGHTN0d2HglBV3uiguA4I"
  }
}
```

`不透明令牌`：通过令牌内省端点（RFC 7662）获取公钥信息。响应中同样包含 `cnf.jkt` 字段，且 `token_type` 必须为 `DPoP`：

``` json
{
  "active": true,
  "sub": "someone@example.com",
  "token_type": "DPoP",
  "cnf": {
    "jkt": "0ZcOCORZNYy-DWpqq30jZyJGHTN0d2HglBV3uiguA4I"
  }
}
```

> 注意：资源服务器向内省端点查询时`不需要`发送 DPoP Proof，返回的 `cnf.jkt` 用于`本地验证`后续客户端请求。

##### 受保护资源访问

访问受保护资源时，客户端需要同时发送两个东西：

1. `Authorization: DPoP <token>` 头——注意认证方案从 `Bearer` 变为 `DPoP`
2. `DPoP: <proof>` 头——一个新的 DPoP Proof JWT，`必须包含` `ath` 声明（Access Token 的哈希）

`ath` 声明将 Proof 与特定的 Access Token 绑定，防止攻击者将 Proof 与另一个 Token 配对使用（令牌替换攻击）。

`资源服务器的验证步骤`：格式与签名校验（`typ`/`alg`/`jwk`）、HTTP 方法与 URI 匹配（`htm`/`htu`）、nonce 匹配、时间窗口检查、`ath` 哈希匹配、公钥与 `cnf.jkt` 一致性验证。

##### WWW-Authenticate 错误响应

资源服务器在拒绝请求时，使用 `DPoP` 认证方案返回 `WWW-Authenticate` 响应头：

| 错误码 | 含义 |
|--------|------|
| `invalid_token` | DPoP 绑定验证失败（公钥不匹配或 Token 无效） |
| `invalid_dpop_proof` | DPoP Proof 本身无效（签名失败、缺少必须字段、`typ` 不是 `dpop+jwt`） |
| `use_dpop_nonce` | 服务器要求在 DPoP Proof 中包含 nonce |
| `insufficient_scope` | Token 权限不足（与 RFC 6750 相同） |

> `algs` 参数`应`包含在响应中，告知客户端接受的签名算法列表。SPA 需通过 `Access-Control-Expose-Headers` 暴露此头。

!!! tip "同时支持 Bearer 和 DPoP 的资源服务器"

    资源服务器同时支持两种方案时，`必须拒绝`以 Bearer 方式发送的 DPoP-Bound Token（防止降级使用）。可以通过多个 `WWW-Authenticate` 头同时宣告支持：`WWW-Authenticate: Bearer, DPoP algs="ES256 PS256"`。

##### Nonce 机制

DPoP Proof 只签了 HTTP 方法和 URL，攻击者可以**预生成** Proof（写入未来时间戳）并窃取到其他设备使用。`Nonce` 是服务器通过 `DPoP-Nonce` 响应头提供的不可预测随机值，客户端必须将其包含在后续 Proof 的 `nonce` 声明中，从而阻止预生成攻击。

!!! warning "Nonce 注意事项"

    - 授权服务器和资源服务器各自提供独立的 nonce，`不能混用`
    - 客户端只需保存一个 nonce 值，直到服务器提供新的
    - 一旦服务器开始要求 nonce，就`不能再接受`不带 nonce 的 Proof（防止降级攻击）

##### 授权码绑定（dpop_jkt）

DPoP 还可以将授权码绑定到客户端的 DPoP 密钥，实现`端到端`的授权流程绑定——从授权请求到 Token 请求再到资源访问，全程使用同一密钥对。

客户端在授权请求中携带 `dpop_jkt` 参数（公钥的 JWK Thumbprint，与 `cnf.jkt` 使用相同的计算方式）。授权服务器在 Token 请求中验证 DPoP Proof 的公钥指纹是否与 `dpop_jkt` 匹配，不匹配则`必须拒绝`。这可以防止攻击者截获授权码后用自己的密钥换 Token。

!!! info "与 PAR 的配合使用"

    使用 PAR（RFC 9126）时，可以直接在 PAR 请求中附带 `DPoP` 头——授权服务器会自动将 DPoP Proof 中的公钥作为绑定密钥，且这种方式更强（还提供了对私钥的持有证明）。两种方式授权服务器都`必须支持`。

##### 安全考量

`Proof 重放攻击：` 攻击者截获 DPoP Proof 后在同一端点重放。服务器`必须`限制 Proof 有效时间（推荐秒级或分钟级），通过跟踪 `jti` 拒绝重复使用。

`Proof 预生成攻击：` 控制客户端的攻击者可预生成未来时间戳的 Proof 并窃取到其他设备使用。使用 nonce 可`完全阻止`此攻击；不使用 nonce 时，`ath` 声明提供部分保护——应使用`短有效期 Access Token + Refresh Token` 模式最小化影响窗口。

`不可信代码：` 如果攻击者能在客户端上下文中执行代码（如 XSS），DPoP 的安全性无法保证——即使私钥不可导出，攻击者仍可在客户端在线时利用 DPoP Proof 发送任意请求。因此防止 XSS 和代码注入仍是根本防线，推荐配合 CSP。

#### mTLS（RFC 8705）

mTLS（Mutual TLS）通过`双向 TLS 认证`实现令牌绑定：客户端使用 TLS 证书请求令牌，授权服务器将令牌与证书指纹绑定，访问资源时验证 TLS 证书指纹一致性。

mTLS 还可以作为`客户端认证方式`替代 `client_secret`，提供更强的身份验证，详见「核心概念 · 客户端认证方式」。

!!! info "DPoP vs mTLS"

    | 维度 | DPoP | mTLS |
    |------|------|------|
    | 实现层 | 应用层（HTTP 头） | 传输层（TLS 握手） |
    | 证书管理 | 客户端自行生成临时密钥对 | 需要 CA 签发的 TLS 证书 |
    | 消息完整性 | 不覆盖请求体 | TLS 层完整保护 |
    | 适用场景 | SPA、移动端、无证书管理基础设施的环境 | 企业级服务端应用、有 PKI 基础设施的环境 |
    | 部署难度 | 低（无需基础设施变更） | 高（需要证书颁发和管理体系） |

## ⚔️ 常见攻击模式与应对

### 授权码注入

攻击者将已使用过的授权码或其他客户端的授权码注入受害者的 Token 请求。

`防护：` 使用 PKCE（`code_verifier` 绑定了请求者身份）；确保 `state` 验证。

### Token 重定向攻击

攻击者将颁发给资源服务器 A 的 Access Token 用于访问资源服务器 B。如果资源服务器 B 没有验证令牌的受众（`aud`），就会误认为该令牌是合法的，导致未授权访问。

`防护：` 授权服务器在颁发令牌时应绑定 `aud` 声明，资源服务器`必须验证` `aud` 是否包含自身标识。

### 恶意 redirect_uri

攻击者构造包含恶意 `redirect_uri` 的授权链接，诱使用户点击后将授权码发送到攻击者服务器。此外，若 `redirect_uri` 被配置为内网地址（如 `http://192.168.1.1/admin`），授权服务器在请求该地址时可能被用于`探测内网服务（SSRF）`。

`防护：` 授权服务器必须严格校验 `redirect_uri` 与注册值`精确匹配`，不应允许内网 IP 地址。

### Token 泄露后应对

1. `短有效期`（5-15 分钟） + `令牌撤销`（RFC 7009）
2. `发送者约束`（DPoP 或 mTLS）使泄露 Token 无法在其他设备使用
3. `Refresh Token Rotation` 检测并阻断被盗令牌

### RFC 6750 安全建议

RFC 6750（Bearer Token Usage）给出了以下安全建议：

#### 短有效期令牌

颁发`短有效期`（1 小时或更短）的 Bearer Token，显著降低泄露后的影响时间窗口。

#### 签发受限受众的令牌

授权服务器`应`在 Token 中包含 `aud`（受众）声明（详见「Token 重定向攻击」），这可以有效防止 Token 重定向攻击。

#### 禁止在 Cookie 中存储 Bearer Token

Bearer Token `不得存储在可明文传输的 Cookie 中`，即使在 HTTPS 环境下也可能因 HTTP 降级而泄露。如果必须使用 Cookie 存储，必须配合 CSRF 防护措施。

#### 验证 TLS 证书链

客户端`必须验证` TLS 证书链，否则可能遭受 DNS 劫持攻击。

#### 负载均衡场景的额外防护

当 TLS 在负载均衡器处终止时，负载均衡器与后端服务器之间的 Token 传输`必须`有额外机密性保护（如对 Token 加密）。

### RFC 6749 安全考量补充

RFC 6749 Section 10 的以下安全主题在现代实践中仍适用：

#### Clickjacking 攻击（Section 10.13）

攻击者在透明 `iframe` 中加载授权页面，覆盖在伪造按钮上诱骗用户授权。`防护：` 设置 `X-Frame-Options: DENY` 或 `SAMEORIGIN` 响应头。

#### 代码注入与输入验证（Section 10.14）

授权服务器和客户端都必须`验证和清理`所有接收到的参数值（特别是 `state` 和 `redirect_uri`），防止 XSS 或开放重定向。

#### 开放重定向器（Section 10.15）

错误配置的 `redirect_uri` 可能成为开放重定向器（详见「恶意 redirect_uri」）。

#### 凭证猜测攻击（Section 10.10）

Token 的猜测概率`必须 <= 2^(-128)`（推荐 `<= 2^(-160)`），必须使用密码学安全的随机生成器。

#### 密码安全考量（Section 10.7）

ROPC 的风险`高于其他所有授权类型`：客户端在内存中`明文持有`用户密码，无法使用 MFA/SSO 等高级认证方式。`应最小化使用`，仅在遗留系统迁移场景中作为过渡方案。

## ✅ OAuth 安全最佳实践要点（RFC 9700）

RFC 9700（OAuth 2.0 Security Best Current Practice）在主要提供商的实施经验和 RFC 6819 威胁模型基础上，总结了 OAuth 2.0 客户端和服务器的安全要求：

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

`上一篇：` [扩展协议](../extensions/index.md)
`下一篇：` [威胁模型与攻击面](../threat-model/index.md)
