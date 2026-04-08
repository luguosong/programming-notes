---
title: 授权流程
---

# OAuth2 授权流程

授权流程就像不同类型的门禁卡办理方式——有的是你本人亲自去前台办卡，有的是公司之间批量办理。OAuth2 根据不同场景设计了多种授权流程，但核心目标始终不变：`让应用代替你访问数据，但不交出你的密码`。

**本文你会学到：**

- 授权码流程的完整执行过程和每一步的作用
- PKCE 如何防止授权码被截获滥用
- Refresh Token 的刷新机制
- 客户端凭证流程和设备授权流程的使用场景
- 隐式流程和 ROPC 为何被弃用

## 授权码流程（Authorization Code）

> 把授权码流程想象成去银行办理代办业务——你不能让代办人直接拿到你的身份证原件（Access Token），而是先让银行给你一张排号条（授权码），代办人拿排号条去柜台，出示自己的员工证（client_secret），才能换取你要办的凭证。排号条即使丢了也没关系，因为没有员工证的陌生人拿着它也办不了业务。

授权码流程是 `OAuth2 最推荐`的标准流程，适用于有服务端的 Web 应用。其核心设计思想是将授权过程拆分为`前端通道（Front Channel）`和`后端通道（Back Channel）`两个阶段：

- `前端通道`：通过浏览器重定向在用户、客户端、授权服务器之间传递`授权码`——授权码本身不具备访问资源的能力，即使被截获也无法独立使用
- `后端通道`：客户端服务端直接与授权服务器通信，凭授权码 + 客户端凭证换取 Access Token——Token 仅在服务端之间传输，不经过浏览器

``` mermaid
sequenceDiagram
    participant RO as 资源所有者
    participant CF as 客户端前端<br/>(浏览器)
    participant CB as 客户端后端
    participant AS as 授权服务器
    participant RS as 资源服务器

    Note over RO,AS: 前端通道（Front Channel）— 通过浏览器重定向
    RO->>CF: 1. 点击"使用第三方登录"
    CF->>CB: 2. 浏览器向客户端后端发起请求
    CB-->>CF: 3. HTTP 302 重定向至授权端点
    CF->>AS: 4. GET /authorize?response_type=code<br/>&client_id=...&redirect_uri=...&scope=...&state=...
    Note over AS: 验证 client_id、redirect_uri、scope
    AS-->>CF: 5. 展示登录/授权确认页面
    RO->>AS: 6. 用户登录并同意授权
    Note over AS: 生成一次性授权码<br/>与请求信息绑定存储
    AS-->>CF: 7. HTTP 302 重定向回 redirect_uri<br/>?code=AUTH_CODE&state=...
    CF->>CB: 8. 浏览器跟随重定向，授权码送达客户端后端

    Note over CB,AS: 后端通道（Back Channel）— 服务端直接通信
    Note over CB: 验证 state 防 CSRF
    CB->>AS: 9. POST /token<br/>grant_type=authorization_code&code=...<br/>Authorization: Basic BASE64(client_id:client_secret)
    Note over AS: 验证客户端凭证 + 授权码<br/>授权码用后即焚
    AS-->>CB: 10. 返回 Access Token + Refresh Token

    CF->>CB: 11. 用户在前端发起资源请求
    CB->>RS: 12. GET /api/resource<br/>Authorization: Bearer <access_token>
    RS-->>CB: 13. 返回受保护资源
    CB-->>CF: 14. 将数据返回前端展示
```

### 为什么需要授权码作为中间步骤？

授权码流程使用授权码作为中间步骤，而不是在授权端点直接返回 Access Token，这是出于安全考虑：

- `前端通道的重定向容易被拦截`：浏览器重定向通过 URL 传递参数，可能出现在浏览器历史记录、服务器日志（Referrer 头）中，也可能被页面中的第三方脚本读取。如果直接携带 Access Token，泄露风险极高
- `授权码无法独立使用`：即使攻击者截获了授权码，没有 `client_secret` 就无法在令牌端点换取 Token。而 `client_secret` 保存在客户端的服务端，不会暴露给浏览器
- `授权码是一次性的`：授权服务器在验证授权码后会立即将其从存储中删除，即使授权码泄露，攻击者也只有极短的时间窗口（通常几分钟），且仍必须同时持有客户端凭证才能使用

### 执行过程详解

1. `用户发起操作`：用户在客户端前端页面点击"使用第三方登录"等按钮，触发授权流程。

2. `浏览器向客户端后端发起请求`：用户的点击触发浏览器向客户端后端的登录端点发起 HTTP 请求（如 `GET /login/oauth2`）。

3. `客户端后端构造授权请求并重定向`：客户端后端生成授权请求 URL，通过 HTTP 302 将浏览器重定向到授权服务器的`授权端点（Authorization Endpoint）`。请求中携带 `response_type=code`（表明使用授权码模式）、`client_id`（标识客户端身份）、`redirect_uri`（授权完成后的回调地址）、`scope`（申请的权限范围）和 `state`（防 CSRF 的随机值，客户端后端需保存此值以便后续验证）。

4. `浏览器访问授权端点`：浏览器跟随重定向，携带上述参数访问授权服务器。

5. `授权服务器验证请求并展示授权页面`：授权服务器的授权端点收到请求后，执行以下验证：
    - 验证 `client_id` 是否对应已注册的客户端——如果客户端不存在，直接展示授权服务器自己的错误页面（`不会`通过 `redirect_uri` 返回错误，因为此时 `redirect_uri` 可能是伪造的，指向钓鱼页面或恶意软件下载地址）
    - 核对 `redirect_uri` 是否与该客户端注册时预配置的地址一致
    - 将请求的 `scope` 与客户端注册的允许 scope 做对比，确保不会超出权限范围
    - 验证通过后，如果用户未登录则先重定向到登录页面，然后展示授权确认页面

6. `用户完成认证并同意授权`：用户在授权服务器的页面上完成登录并同意授权。认证过程完全在用户与授权服务器之间完成，客户端`不会接触`用户的凭证。OAuth 协议不限定认证方式——可以是用户名/密码、加密证书、安全令牌或联合单点登录（SSO）等。授权确认页面通常展示客户端信息和 scope 列表（以复选框形式呈现），用户可以选择`批准全部或仅批准部分`权限，因此客户端最终获得的 scope 可能少于申请的范围。

7. `授权服务器颁发授权码`：用户同意授权后，授权服务器确定用户最终批准了哪些 scope（再次校验未超出客户端注册的范围，防止表单被篡改注入额外 scope），然后生成一个`一次性短效授权码`，将其与客户端请求信息、批准的 scope 绑定存储，最后通过 HTTP 302 将浏览器重定向到 `redirect_uri`，在查询参数中附带授权码 `code` 和原始 `state` 值。如果用户拒绝授权，则通过 `redirect_uri` 返回 `error=access_denied`。

8. `授权码送达客户端后端`：浏览器跟随重定向访问回调地址（`redirect_uri` 指向客户端后端），客户端后端从请求参数中取出授权码。

9. `客户端后端凭码换 Token`：客户端后端首先验证 `state` 与步骤 3 中生成的值一致（防止 CSRF 攻击），然后向授权服务器的`令牌端点（Token Endpoint）`发起 POST 请求，携带授权码和客户端凭证。按照 OAuth 规范，如果授权请求中指定了 `redirect_uri`，则令牌请求中也必须包含相同的 `redirect_uri`（防止攻击者利用已被攻破的重定向地址将授权码注入到另一个会话中）。客户端凭证通常通过 HTTP Basic 认证传递（`Authorization: Basic BASE64(client_id:client_secret)`），也可以通过表单参数传递。

10. `授权服务器验证并颁发 Token`：令牌端点收到请求后执行以下处理：
    - 验证客户端身份（`client_id` + `client_secret`）
    - 查找授权码并验证其是否有效、是否属于当前客户端
    - `用后即焚`：验证通过后立即从存储中删除授权码，确保一次性使用
    - 生成 Access Token 和 Refresh Token，连同授权的 scope 一起存储，然后返回给客户端后端

11. `前端发起资源请求`：用户在客户端前端页面发起操作（如查看个人资料），浏览器将请求发送到客户端后端。

12. `客户端后端携带 Token 访问资源`：客户端后端使用 `Authorization: Bearer <access_token>` 请求头携带 Access Token，向资源服务器发起 API 请求。令牌由客户端后端保存和发送，不会暴露给前端页面或用户，进一步降低泄露风险。

13. `资源服务器返回数据`：资源服务器验证 Token 有效（签名、过期时间、scope 权限）后，根据 Token 携带的 scope 决定返回哪些数据，最终返回受保护的资源。

14. `数据返回前端展示`：客户端后端将资源服务器的响应数据处理后返回给前端，前端渲染展示给用户。

### 授权端点错误响应（RFC 6749 Section 4.1.2.1）

当授权请求失败时，授权服务器通过 `redirect_uri` 的 **query component** 返回错误信息。错误响应包含三个字段：

| 字段 | 必需性 | 说明 |
|------|--------|------|
| `error` | 必须 | ASCII 错误码（见下表） |
| `error_description` | 可选 | 人类可读的 ASCII 错误描述（辅助调试） |
| `error_uri` | 可选 | 包含错误详细信息网页的 URI |

七个标准错误码：

| 错误码 | 含义 |
|--------|------|
| `invalid_request` | 缺少必需参数、参数值无效、参数重复、请求格式错误 |
| `unauthorized_client` | Client 无权使用此方法请求授权码 |
| `access_denied` | Resource Owner 或授权服务器拒绝了请求 |
| `unsupported_response_type` | 授权服务器不支持用此方法获取授权码 |
| `invalid_scope` | 请求的 scope 无效、未知或格式错误 |
| `server_error` | 授权服务器遇到意外条件（内部错误） |
| `temporarily_unavailable` | 授权服务器暂时过载或维护中 |

!!! info inline end "ABNF（RFC 6749 Appendix A）"
    `code = 1*VSCHAR`（一个或多个可见 ASCII 字符），`state = 1*VSCHAR`

### Token 响应格式

``` json
{
  "access_token": "eyJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "dGhpcyBpcyBhIHJlZnJlc2gg...",
  "scope": "read_profile read_email"
}
```

| 字段 | 说明 |
|------|------|
| `access_token` | 访问令牌，用于请求受保护资源 |
| `token_type` | 令牌类型，通常为 `Bearer` |
| `expires_in` | Access Token 有效期（秒） |
| `refresh_token` | 刷新令牌，用于在 Access Token 过期后获取新 Token |
| `scope` | 实际授权的权限范围。当与客户端请求的范围一致时为 RECOMMENDED，不一致时为 REQUIRED |

!!! info "Token 响应的 HTTP 要求（RFC 6749 Section 5.1）"

    - `Content-Type` 必须为 `application/json;charset=UTF-8`
    - 必须包含 `Cache-Control: no-store` 和 `Pragma: no-cache` 响应头（`禁止缓存` Token 响应）
    - 如果颁发的 scope 与客户端请求的不同，响应中`必须`包含 `scope` 参数明确告知实际授权范围（即 scope 缩窄时必须显式返回）

### 令牌端点错误响应（RFC 6749 Section 5.2）

令牌端点的错误响应与授权端点不同：错误信息以 `application/json` 格式在响应体中返回（非 URL 参数），默认 HTTP 状态码为 400（Bad Request）。

| 错误码 | 含义 | 特殊状态码 |
|--------|------|-----------|
| `invalid_request` | 缺少必需参数、参数重复、多种认证机制、格式错误 | 400 |
| `invalid_client` | Client 认证失败（未知 client、无认证、不支持的方法） | **可能返回 401** + `WWW-Authenticate` 头 |
| `invalid_grant` | 授权码/凭证/refresh token 无效、过期、被撤销或不匹配 | 400 |
| `unauthorized_client` | 已认证的 Client 无权使用此授权类型 | 400 |
| `unsupported_grant_type` | 授权服务器不支持此授权类型 | 400 |
| `invalid_scope` | 请求的 scope 无效、未知或超出 Resource Owner 授权范围 | 400 |

与授权端点错误码的主要差异：令牌端点多 `invalid_client`（客户端认证失败）和 `invalid_grant`（授权凭证无效），但没有 `access_denied`（由用户拒绝授权，只在授权端点发生）和 `unsupported_response_type`（与授权类型选择相关）。

### 关键参数一览

| 参数 | 所属阶段 | 说明 |
|------|---------|------|
| `response_type=code` | 授权请求 | 告知授权服务器返回授权码 |
| `client_id` | 授权请求 / Token 请求 | 注册的客户端标识 |
| `redirect_uri` | 授权请求 / Token 请求 | 回调地址，两次请求必须一致 |
| `scope` | 授权请求 | 请求的权限范围 |
| `state` | 授权请求 | 随机字符串，防 CSRF，授权服务器原样返回 |
| `code` | 授权响应 / Token 请求 | 一次性短效授权码 |
| `grant_type=authorization_code` | Token 请求 | 告知令牌端点使用授权码流程 |
| `client_secret` | Token 请求 | 客户端密钥，仅在后端通道传输 |
| `code_verifier` | Token 请求 | PKCE 验证码（OAuth 2.1 中为必选） |

## PKCE（Proof Key for Code Exchange）

PKCE（RFC 7636）是授权码流程的`安全增强扩展`，对于`公开客户端（SPA、移动应用）为必选`，机密客户端也推荐使用。

PKCE 的核心思路是：客户端在发起授权请求前生成一个随机密钥（`code_verifier`），将其哈希值（`code_challenge`）发给授权服务器，换 Token 时再提交原始密钥。即使授权码被截获，攻击者没有 `code_verifier` 也无法换 Token。

``` mermaid
sequenceDiagram
    participant 客户端
    participant 授权服务器
    participant 资源服务器

    Note over 客户端: 1. 生成随机 code_verifier（43-128 位随机字符）
    Note over 客户端: 2. 计算 code_challenge = BASE64URL(SHA256(code_verifier))
    客户端->>授权服务器: 3. 授权请求中附带<br/>code_challenge + code_challenge_method=S256
    授权服务器-->>客户端: 4. 返回授权码（记录 code_challenge）
    客户端->>授权服务器: 5. Token 请求中附带 code_verifier
    Note over 授权服务器: 6. 验证：SHA256(code_verifier) == 存储的 code_challenge
    授权服务器-->>客户端: 7. 验证通过，返回 Access Token
    客户端->>资源服务器: 8. GET /api/resource<br/>Authorization: Bearer <access_token>
    资源服务器-->>客户端: 9. 返回受保护资源
```

执行过程：

1. `生成 code_verifier`：客户端本地生成一个 43~128 位的随机字符串（高熵随机值，每次请求都不同）。
2. `计算 code_challenge`：对 `code_verifier` 执行 `BASE64URL(SHA256(code_verifier))` 得到 `code_challenge`。这是一个单向哈希，无法反推。
3. `发送授权请求`：将 `code_challenge` 和 `code_challenge_method=S256` 附加到授权请求中。授权服务器将其与本次会话绑定存储。
4. `返回授权码`：用户完成登录授权后，授权服务器颁发授权码，并在服务端记录该授权码与 `code_challenge` 的关联。
5. `附带 code_verifier 换 Token`：客户端在 Token 请求中同时提供授权码和原始 `code_verifier`。
6. `服务端验证`：授权服务器对收到的 `code_verifier` 执行 SHA256 哈希，与之前存储的 `code_challenge` 比对。只有发起授权请求的那个客户端实例才知道 `code_verifier`，攻击者即使截获了授权码也无法通过此验证。
7. `颁发 Access Token`：验证通过后颁发 Access Token。
8-9. 后续携带 Token 访问资源服务器，与授权码流程相同。

PKCE 用 `code_verifier`/`code_challenge` 绑定替代 `client_secret`，即使授权码被截获，攻击者也无法在 Token 端点使用（因为不知道 `code_verifier`）。

!!! tip "OAuth 2.1 草案变化"
    OAuth 2.1 草案已将 PKCE 对所有授权码流程设为强制（包括机密客户端），即使今天使用 OAuth 2.0 实现，也建议始终开启 PKCE。

PKCE 的安全分析详见「安全实践 · PKCE」。

## Refresh Token 流程

当 Access Token 过期时，客户端无需再次引导用户走完整的授权流程，而是使用之前获得的 Refresh Token 直接向令牌端点换取新的 Access Token。

``` mermaid
sequenceDiagram
    participant CF as 客户端前端<br/>(浏览器)
    participant CB as 客户端后端
    participant AS as 授权服务器
    participant RS as 资源服务器

    CF->>CB: 1. 用户在前端发起资源请求
    CB->>RS: 2. 携带已过期的 Access Token 请求资源
    RS-->>CB: 3. 返回 401 Unauthorized（Token 已过期）
    CB->>AS: 4. POST /token<br/>grant_type=refresh_token&refresh_token=...<br/>+ 客户端凭证（机密客户端）
    Note over AS: 验证 Refresh Token 有效性<br/>验证客户端身份<br/>检查请求的 scope 未超出原始授权
    AS-->>CB: 5. 新 Access Token +（可选）新 Refresh Token
    CB->>RS: 6. 携带新 Access Token 重新请求资源
    RS-->>CB: 7. 返回受保护资源
    CB-->>CF: 8. 将数据返回前端（用户无感知刷新）
```

执行过程：

1. `前端发起资源请求`：用户在客户端前端页面发起操作，浏览器将请求发送到客户端后端。
2. `Access Token 过期`：客户端后端携带已过期的 Access Token 请求资源服务器，收到 401 响应。
3. `发起刷新请求`：客户端后端向令牌端点发起 POST 请求，携带 `grant_type=refresh_token` 和之前保存的 Refresh Token。机密客户端还需提供客户端凭证（`client_id` + `client_secret`）进行身份认证。
4. `授权服务器验证`：授权服务器验证 Refresh Token 是否有效、是否属于该客户端。如果请求中携带了 `scope`，还需确认未超出 Refresh Token 最初授权的范围（可以请求子集，但不能请求更多权限）。
5. `颁发新令牌`：验证通过后，授权服务器颁发新的 Access Token。授权服务器也可以同时颁发新的 Refresh Token 替换旧的（即 Refresh Token Rotation），旧 Token 立即失效。
6-8. 客户端后端使用新 Access Token 重新请求资源服务器，获取数据后返回给前端展示。整个刷新过程对用户完全透明，无需重新登录。

!!! note "Refresh Token 的安全注意事项"

    - `公开客户端`的 Refresh Token 被盗后攻击者可以冒充客户端使用，因此公开客户端`必须`配合 Refresh Token Rotation 机制，详见「安全实践」
    - `机密客户端`使用 Refresh Token 时需同时提供客户端凭证认证，被盗风险较低
    - Refresh Token `不应发送给资源服务器`，只发送给授权服务器的令牌端点

## 客户端凭证流程（Client Credentials）

> 想象两个公司之间的 API 调用——没有用户参与，纯粹是系统对系统。客户端凭证流程就像公司之间交换的"企业通行证"，用自己公司的公章（client_secret）去对方的门卫那里换一张临时工牌（Access Token）。没有员工需要到场签字，全程都是公司对公司的业务往来。

适用于`没有用户参与的机器对机器（M2M）调用`，如微服务间互相调用 API。

``` mermaid
sequenceDiagram
    participant 客户端服务
    participant 授权服务器
    participant 资源服务器

    客户端服务->>授权服务器: 1. POST /token<br/>grant_type=client_credentials&client_id=...&client_secret=...&scope=...
    授权服务器-->>客户端服务: 2. 返回 Access Token（无 Refresh Token）
    客户端服务->>资源服务器: 3. GET /api/resource<br/>Authorization: Bearer <access_token>
    资源服务器-->>客户端服务: 4. 返回受保护资源
```

执行过程：

1. `直接请求 Token`：客户端服务使用自身的 `client_id` 和 `client_secret` 向 Token 端点发起请求。此流程没有用户参与，整个过程在服务端之间完成。`scope` 表示此客户端申请的服务权限范围（由管理员在授权服务器侧预先配置）。
2. `颁发 Access Token`：授权服务器验证客户端凭证有效后，颁发 Access Token。由于没有用户参与，通常`不颁发 Refresh Token`——Access Token 过期后直接用凭证重新申请即可。
3. `携带 Token 请求资源`：客户端服务以 `Bearer` Token 请求资源服务器的 API 端点。
4. `返回资源`：资源服务器验证 Token 后返回数据。

!!! note "无 Refresh Token"
    客户端凭证流程通常不颁发 Refresh Token，Access Token 过期后直接重新请求即可。

## 设备授权流程（Device Flow）

适用于`无浏览器或输入受限的设备`（智能电视、命令行工具、IoT 设备）。

``` mermaid
sequenceDiagram
    participant 设备
    participant 用户浏览器
    participant 授权服务器
    participant 资源服务器

    设备->>授权服务器: 1. POST /device_authorization<br/>client_id=...&scope=...
    授权服务器-->>设备: 2. device_code + user_code + verification_uri
    Note over 设备: 展示 user_code 和 URL 给用户
    用户浏览器->>授权服务器: 3. 访问 verification_uri，输入 user_code 并登录授权
    loop 轮询
        设备->>授权服务器: 4. POST /token<br/>grant_type=urn:ietf:params:oauth:grant-type:device_code&device_code=...
        授权服务器-->>设备: authorization_pending 或 Access Token
    end
    设备->>资源服务器: 5. GET /api/resource<br/>Authorization: Bearer <access_token>
    资源服务器-->>设备: 6. 返回受保护资源
```

执行过程：

1. `设备申请用户码`：受限设备（无键盘的智能电视、CLI 工具等）向授权服务器的设备授权端点发起请求，告知自身 `client_id` 和需要的权限 `scope`。
2. `获取展示信息`：授权服务器返回三个关键值：`device_code`（设备专用的内部凭证，用于轮询）、`user_code`（简短易输入的用户可读码，如 `BDJD-XZQD`）、`verification_uri`（用户需要在另一设备上访问的 URL，如 `example.com/activate`）。设备将 `user_code` 和 URL 展示给用户（显示在屏幕、打印、念出来等方式）。
3. `用户在辅助设备完成授权`：用户拿出手机或电脑，访问 `verification_uri`，输入设备上显示的 `user_code`，然后完成登录并同意授权。此步骤在用户的辅助设备浏览器上完成，整个认证过程在授权服务器侧进行，受限设备不参与。
4. `设备轮询等待`：受限设备以固定间隔（通常 5 秒）持续向 Token 端点轮询，携带 `device_code` 查询用户是否已完成授权。在用户未完成前，授权服务器返回 `authorization_pending`；用户完成授权后，授权服务器返回 Access Token。
5. `访问资源服务器`：设备获得 Access Token 后，携带 Token 向资源服务器发起 API 请求。
6. `返回资源`：资源服务器验证 Token 后返回受保护的数据。

## 隐式流程（Implicit）— ⚠️ 已弃用

!!! danger "已弃用"
    隐式流程因严重安全缺陷已在 OAuth 2.1 中移除。现代应用应使用`授权码流程 + PKCE` 替代。此处仅作历史参考。

隐式流程最初为`纯前端应用（SPA）`设计——当时浏览器无法安全保存 `client_secret`，也没有 PKCE 机制。它的做法是跳过授权码这个中间步骤，直接在授权端点通过 URL 片段（`fragment`）返回 Access Token。

``` mermaid
sequenceDiagram
    participant RO as 资源所有者
    participant CF as 客户端前端<br/>(浏览器)
    participant AS as 授权服务器
    participant RS as 资源服务器

    RO->>CF: 1. 点击"使用第三方登录"
    CF-->>RO: 2. 重定向至授权端点
    RO->>AS: 3. GET /authorize?response_type=token<br/>&client_id=...&redirect_uri=...&scope=...&state=...
    Note over AS: 验证请求参数
    AS-->>RO: 4. 展示登录/授权确认页面
    RO->>AS: 5. 用户登录并同意授权
    Note over AS: 直接生成 Access Token
    AS-->>CF: 6. HTTP 302 重定向回 redirect_uri<br/>#access_token=...&token_type=Bearer&expires_in=3600&state=...
    Note over CF: 从 URL 片段中提取 Access Token
    CF->>RS: 7. GET /api/resource<br/>Authorization: Bearer <access_token>
    RS-->>CF: 8. 返回受保护资源
```

与授权码流程的关键区别：

- `response_type=token`（而非 `code`）：告知授权服务器直接返回 Token
- `Token 通过 URL 片段返回`：使用 `#`（fragment）而非 `?`（query），片段不会发送到服务器（`redirect_uri` 对应的服务端收不到 Token），但会保留在浏览器中
- `没有后端通道`：整个流程都在前端完成，不存在客户端后端与授权服务器的直接通信
- `不颁发 Refresh Token`：因为没有安全的存储位置

弃用原因：

- `Token 暴露在浏览器中`：URL 片段可被页面中的 JavaScript（包括第三方脚本）读取，存在浏览器历史记录中，也可能通过 Referrer 头泄露
- `无法验证客户端身份`：没有 `client_secret`，也没有 PKCE 的 `code_verifier`，任何能截获重定向的恶意应用都可以冒充合法客户端
- `Token 注入攻击`：攻击者可以将自己获取的 Token 注入到受害者的重定向 URL 中，客户端无法区分 Token 是否来自正常授权流程

## 资源所有者密码凭证（ROPC）— ⚠️ 已弃用

!!! danger "已弃用"
    ROPC 因违反 OAuth 核心设计原则已在 OAuth 2.1 中移除。现代应用应使用`授权码流程 + PKCE` 替代。此处仅作历史参考。

ROPC 的做法是`用户直接将用户名和密码交给客户端`，由客户端代为向授权服务器换取 Token。这完全违背了 OAuth 的初衷——`让应用不接触用户密码`。

``` mermaid
sequenceDiagram
    participant RO as 资源所有者
    participant Client as 客户端
    participant AS as 授权服务器
    participant RS as 资源服务器

    RO->>Client: 1. 输入用户名和密码
    Note over Client: 客户端直接持有用户凭证
    Client->>AS: 2. POST /token<br/>grant_type=password&username=...&password=...<br/>&client_id=...&scope=...
    Note over AS: 验证用户凭证
    AS-->>Client: 3. 返回 Access Token +（可选）Refresh Token
    Client->>RS: 4. GET /api/resource<br/>Authorization: Bearer <access_token>
    RS-->>Client: 5. 返回受保护资源
```

流程极为简单——客户端直接拿用户密码换 Token，跳过了授权码、浏览器重定向等所有保护机制。

弃用原因：

- `客户端直接接触用户密码`：OAuth 的核心价值就是让第三方应用不碰用户密码，ROPC 完全破坏了这一设计
- `无法支持多因素认证`：用户名/密码是唯一的认证方式，无法集成短信验证码、硬件密钥等现代认证手段
- `钓鱼风险`：用户被训练成"把密码交给客户端是正常的"，降低了对钓鱼攻击的警觉性
- `权限范围不可控`：虽然形式上有 `scope`，但客户端已经持有用户密码，实际上拥有了用户的全部权限


## 适用场景对比

| 授权流程 | 适用场景 | 推荐程度 | 需要用户参与 |
|---------|---------|---------|------------|
| 授权码 + PKCE | 所有有用户的应用（SPA、Web、移动端） | ✅ 强烈推荐 | 是 |
| 客户端凭证 | 微服务、后台任务、M2M | ✅ 推荐 | 否 |
| 设备授权 | 智能电视、CLI 工具、IoT | ✅ 推荐 | 是（用辅助设备） |
| 隐式流程 | ~~纯前端 SPA~~ | ❌ 已弃用 | 是 |
| ROPC | ~~高度信任的第一方应用~~ | ❌ 已弃用 | 是 |

---

`上一篇：` [核心概念](../core-concepts/index.md)
`下一篇：` [令牌体系](../token-system/index.md)
