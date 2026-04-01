# OAuth2 授权类型

OAuth2 规范定义了多种`授权类型（Grant Type）`，对应不同的应用场景。本文详细介绍各授权类型的流程机制，安全威胁分析见[安全实践](../security/index.md)。

## 授权码流程（Authorization Code）

授权码流程是 `OAuth2 最推荐`的标准流程，适用于有服务端的 Web 应用。核心优势：Access Token 仅在服务端与授权服务器之间交换，不经过浏览器，安全性最高。

``` mermaid
sequenceDiagram
    participant 用户浏览器
    participant 客户端服务端
    participant 授权服务器
    participant 资源服务器

    用户浏览器->>授权服务器: 1. 重定向至授权端点<br/>?response_type=code&client_id=...&redirect_uri=...&scope=...&state=...
    授权服务器-->>用户浏览器: 2. 展示登录/授权页面
    用户浏览器->>授权服务器: 3. 用户登录并同意授权
    授权服务器-->>用户浏览器: 4. 重定向回 redirect_uri<br/>?code=AUTH_CODE&state=...
    用户浏览器->>客户端服务端: 5. 转发授权码
    客户端服务端->>授权服务器: 6. POST /token<br/>grant_type=authorization_code&code=...&client_id=...&client_secret=...
    授权服务器-->>客户端服务端: 7. 返回 Access Token + Refresh Token
    客户端服务端->>资源服务器: 8. GET /api/resource<br/>Authorization: Bearer <access_token>
    资源服务器-->>客户端服务端: 9. 返回受保护资源
```

`执行过程说明：`

1. `发起授权请求`：用户在客户端点击"使用第三方登录"等按钮，客户端将用户浏览器重定向到授权服务器的授权端点。请求中携带 `response_type=code`（表明要使用授权码模式）、`client_id`（标识是哪个客户端）、`redirect_uri`（授权完成后的回调地址）、`scope`（申请的权限范围）和 `state`（防 CSRF 的随机值）。
2. `展示授权页面`：授权服务器返回登录/同意授权页面给用户浏览器。
3. `用户完成认证授权`：用户在授权服务器的页面完成登录并同意授权申请的权限范围。此步骤完全在授权服务器侧完成，客户端`看不到`用户的凭证。
4. `颁发授权码`：授权服务器将浏览器重定向到 `redirect_uri`，在查询参数中附带一次性短效授权码 `code` 和原始 `state` 值。授权码通常有效期极短（几分钟内），且只能使用一次。
5. `转发授权码`：浏览器访问回调地址，客户端服务端从请求中取出授权码。客户端应先验证 `state` 与步骤 1 中发出的值一致，防止 CSRF 攻击。
6. `凭码换 Token`：客户端服务端`在后端`携带授权码、`client_id`、`client_secret` 向授权服务器的 Token 端点发起请求。此步骤不经过浏览器，避免了 Token 暴露在前端。
7. `颁发 Token`：授权服务器验证授权码有效后，颁发 Access Token（用于访问资源）和 Refresh Token（用于续期）。
8. `访问受保护资源`：客户端使用 `Authorization: Bearer <access_token>` 请求头携带 Access Token，向资源服务器发起 API 请求。
9. `返回资源`：资源服务器验证 Token 有效（签名、过期时间、scope 权限）后，返回受保护的资源数据。

`关键参数说明：`

| 参数 | 位置 | 说明 |
|------|------|------|
| `response_type=code` | 授权请求 | 告知授权服务器返回授权码 |
| `client_id` | 授权请求 | 注册的客户端标识 |
| `redirect_uri` | 授权请求 | 回调地址，必须与注册值一致 |
| `scope` | 授权请求 | 请求的权限范围 |
| `state` | 授权请求 | 随机字符串，防 CSRF，授权服务器原样返回 |
| `code` | 授权响应 | 一次性短效授权码 |
| `grant_type=authorization_code` | Token 请求 | 告知 Token 端点使用授权码流程 |

## PKCE（Proof Key for Code Exchange）

PKCE（RFC 7636）是授权码流程的`安全增强扩展`，对于`公开客户端（SPA、移动应用）为必选`，机密客户端也推荐使用。

`流程机制：`

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

`执行过程说明：`

1. `生成 code_verifier`：客户端在发起授权请求前，本地生成一个 43~128 位的随机字符串作为 `code_verifier`（高熵随机值，每次请求都不同）。
2. `计算 code_challenge`：对 `code_verifier` 执行 `BASE64URL(SHA256(code_verifier))` 计算得到 `code_challenge`。这是一个单向哈希，无法从 `code_challenge` 反推出 `code_verifier`。
3. `发送授权请求`：将 `code_challenge` 和 `code_challenge_method=S256` 附加到授权请求中一起发送给授权服务器。授权服务器将 `code_challenge` 与本次会话绑定存储。
4. `返回授权码`：用户完成登录授权后，授权服务器颁发授权码，并在服务端记录该授权码与 `code_challenge` 的关联。
5. `附带 code_verifier 换 Token`：客户端在 Token 请求中同时提供授权码和原始 `code_verifier`。
6. `服务端验证`：授权服务器对收到的 `code_verifier` 执行 SHA256 哈希，与之前存储的 `code_challenge` 比对。只有发起授权请求的那个客户端实例才知道 `code_verifier`，攻击者即使截获了授权码也无法通过此验证。
7. `颁发 Access Token`：验证通过后颁发 Access Token。
8-9. 后续携带 Token 访问资源服务器，与授权码流程相同。

PKCE 用 `code_verifier`/`code_challenge` 绑定替代 `client_secret`，即使授权码被截获，攻击者也无法在 Token 端点使用（因为不知道 `code_verifier`）。

!!! tip "OAuth 2.1 草案变化"
    OAuth 2.1 草案已将 PKCE 对所有授权码流程设为强制（包括机密客户端），即使今天使用 OAuth 2.0 实现，也建议始终开启 PKCE。

## 客户端凭证流程（Client Credentials）

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

`执行过程说明：`

1. `直接请求 Token`：客户端服务使用自身的 `client_id` 和 `client_secret` 向 Token 端点发起请求。此流程没有用户参与，整个过程在服务端之间完成。`scope` 表示此客户端申请的服务权限范围（由管理员在授权服务器侧预先配置）。
2. `颁发 Access Token`：授权服务器验证客户端凭证有效后，颁发 Access Token。由于没有用户参与，通常`不颁发 Refresh Token`——Access Token 过期后直接用凭证重新申请即可。
3. `携带 Token 请求资源`：客户端服务以 `Bearer` Token 请求资源服务器的 API 端点。
4. `返回资源`：资源服务器验证 Token 后返回数据。

!!! note "无 Refresh Token"
    客户端凭证流程通常不颁发 Refresh Token，Access Token 过期后直接重新请求即可。

## 隐式流程（Implicit）— 已不推荐

隐式流程曾经是 SPA 应用的标准方案，但 `OAuth 2.0 Security Best Current Practice（BCP）已将其标为不推荐`。

``` mermaid
sequenceDiagram
    participant 用户浏览器（SPA）
    participant 授权服务器
    participant 资源服务器

    用户浏览器（SPA）->>授权服务器: 1. 重定向至授权端点<br/>?response_type=token&client_id=...&redirect_uri=...&scope=...&state=...
    授权服务器-->>用户浏览器（SPA）: 2. 展示登录/授权页面
    用户浏览器（SPA）->>授权服务器: 3. 用户登录并同意授权
    授权服务器-->>用户浏览器（SPA）: 4. 重定向回 redirect_uri<br/>#access_token=...&token_type=Bearer&expires_in=3600&state=...
    Note over 用户浏览器（SPA）: Token 暴露在 URL fragment 中<br/>浏览器历史记录、Referrer 头均可见
    用户浏览器（SPA）->>资源服务器: 5. GET /api/resource<br/>Authorization: Bearer <access_token>
    资源服务器-->>用户浏览器（SPA）: 6. 返回受保护资源
```

`执行过程说明：`

1. `发起授权请求`：SPA 将用户重定向至授权端点，使用 `response_type=token`（区别于授权码流程的 `code`），要求授权服务器`直接返回` Access Token，而非先返回授权码。
2. `展示授权页面`：授权服务器返回登录/同意页面。
3. `用户完成认证授权`：用户完成登录并同意授权。
4. `直接返回 Access Token`：授权服务器将 Token **放在 URL 的 `#fragment`（哈希片段）部分**重定向回 `redirect_uri`。`#fragment` 不会发送到服务器，但浏览器历史记录中会保留完整 URL，第三方脚本（XSS、广告脚本）也能读取 `location.hash`，存在严重泄露风险。此流程`没有 client_secret 验证`，也`不颁发 Refresh Token`。
5. `直接请求资源`：SPA 的 JavaScript 代码从 URL fragment 中解析出 Token，然后直接携带 Token 请求资源服务器。
6. `返回资源`：资源服务器验证 Token 后返回数据。

`问题所在：` `response_type=token` 导致 Access Token 直接出现在 URL 的 `#fragment` 部分——浏览器历史记录、服务器日志（Referrer 头）、页面内 JavaScript 均可读取，存在泄露风险。此外，隐式流程无法颁发 Refresh Token，且无法验证 Token 的接收方（缺少 `aud` 绑定）。

!!! danger "请使用授权码流程 + PKCE 替代"
    现代 SPA 应用应使用`授权码流程 + PKCE`，而非隐式流程。授权码流程中 Token 仅在后端与授权服务器之间交换，不经过浏览器 URL。

## 资源所有者密码凭证（ROPC）— 已不推荐

ROPC 允许客户端直接收集用户名和密码后发送给授权服务器换取 Token。`OAuth 2.1 草案已将其移除。`

``` mermaid
sequenceDiagram
    participant 用户
    participant 客户端应用
    participant 授权服务器
    participant 资源服务器

    用户->>客户端应用: 1. 直接在应用界面输入用户名和密码
    Note over 客户端应用: 客户端明文持有用户凭证
    客户端应用->>授权服务器: 2. POST /token<br/>grant_type=password&username=...&password=...&client_id=...&scope=...
    授权服务器-->>客户端应用: 3. 返回 Access Token + Refresh Token
    Note over 用户,客户端应用: 用户从未直接与授权服务器交互<br/>无法通过授权服务器集中管理会话或撤销
    客户端应用->>资源服务器: 4. GET /api/resource<br/>Authorization: Bearer <access_token>
    资源服务器-->>客户端应用: 5. 返回受保护资源
```

`执行过程说明：`

1. `用户在客户端输入凭证`：用户将用户名和密码直接输入到`客户端应用自己的界面`（而非授权服务器的登录页面）。客户端在内存中明文持有用户密码，随时可以记录或转发。
2. `客户端代为换 Token`：客户端将用户名、密码连同 `grant_type=password` 一起提交给授权服务器的 Token 端点。用户从未与授权服务器直接交互，无法通过浏览器重定向感知此过程是否被篡改。
3. `颁发 Token`：授权服务器验证用户名密码后颁发 Access Token 和 Refresh Token。
4. `访问资源服务器`：客户端携带 Access Token 请求资源服务器。
5. `返回资源`：资源服务器返回数据。

`问题所在：` 用户必须将凭证直接交给客户端应用，这破坏了 OAuth2 的核心设计原则——用户凭证只应由授权服务器处理。客户端应用可以记录密码，也无法使用 MFA（多因素认证）或 SSO（单点登录）等高级认证方式。

!!! danger "强烈不推荐"
    ROPC 破坏了 OAuth2 的核心设计原则：用户凭证不应暴露给客户端应用。仅在无法使用其他流程的遗留系统迁移场景中考虑。

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

`执行过程说明：`

1. `设备申请用户码`：受限设备（无键盘的智能电视、CLI 工具等）向授权服务器的设备授权端点发起请求，告知自身 `client_id` 和需要的权限 `scope`。
2. `获取展示信息`：授权服务器返回三个关键值：`device_code`（设备专用的内部凭证，用于轮询）、`user_code`（简短易输入的用户可读码，如 `BDJD-XZQD`）、`verification_uri`（用户需要在另一设备上访问的 URL，如 `example.com/activate`）。设备将 `user_code` 和 URL 展示给用户（显示在屏幕、打印、念出来等方式）。
3. `用户在辅助设备完成授权`：用户拿出手机或电脑，访问 `verification_uri`，输入设备上显示的 `user_code`，然后完成登录并同意授权。此步骤在用户的辅助设备浏览器上完成，整个认证过程在授权服务器侧进行，受限设备不参与。
4. `设备轮询等待`：受限设备以固定间隔（通常 5 秒）持续向 Token 端点轮询，携带 `device_code` 查询用户是否已完成授权。在用户未完成前，授权服务器返回 `authorization_pending`；用户完成授权后，授权服务器返回 Access Token。
5. `访问资源服务器`：设备获得 Access Token 后，携带 Token 向资源服务器发起 API 请求。
6. `返回资源`：资源服务器验证 Token 后返回受保护的数据。

## 适用场景对比

| 授权类型 | 适用场景 | 推荐程度 | 需要用户参与 |
|---------|---------|---------|------------|
| 授权码 + PKCE | 所有有用户的应用（SPA、Web、移动端） | ✅ 强烈推荐 | ✔ |
| 客户端凭证 | 微服务、后台任务、M2M | ✅ 推荐 | ✘ |
| 设备授权 | 智能电视、CLI 工具、IoT | ✅ 推荐 | ✔（用辅助设备） |
| 隐式流程 | —（已废弃） | ❌ 不推荐 | ✔ |
| ROPC | 遗留系统迁移（仅此场景） | ⚠️ 避免 | ✔ |

---

`上一篇：` [核心概念](../core-concepts/index.md)
`下一篇：` [OpenID Connect](../openid-connect/index.md)
