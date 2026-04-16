---
---

# OAuth2 动态客户端注册与管理

前面在「核心概念」中提到，客户端必须先在授权服务器上注册才能使用 OAuth2。传统方式是开发者手动在授权服务器的管理控制台中填写 Client ID、Secret、Redirect URI 等信息——就像入住酒店需要到`前台人工登记`。

但如果同一个应用要对接多个部署环境（开发、测试、生产），或者第三方开发者要接入你的 API，手动注册就变得繁琐甚至不可行。RFC 7591 定义的**动态客户端注册**协议，让客户端可以通过标准 HTTP API 自动完成注册——就像酒店有了`自助入住机`，刷身份证就能自动办理入住，不需要前台人工操作。而 RFC 7592 进一步定义了注册后的`管理操作`——读取、更新、删除注册信息，就像入住后可以自助修改房间的延长日期、更换房型或退房。

**本文你会学到：**

- 🎯 动态客户端注册解决什么问题，以及它的协议流程（RFC 7591）
- 🔓 开放注册与受保护注册的区别和适用场景
- 📋 客户端元数据字段详解（核心字段、客户端信息、密钥与软件标识）
- 🔗 授权类型（`grant_types`）与响应类型（`response_types`）的对应关系
- 📡 注册请求与响应的 HTTP 格式和错误处理
- 🛡️ 软件声明（Software Statement）如何防止客户端冒充
- 🔧 注册后的管理操作：读取、更新、删除（RFC 7592）
- 🔑 注册访问令牌与凭证轮换机制
- 🔒 动态注册的安全与隐私考量

## 📊 动态注册怎么走？——协议流程

动态注册的协议流程非常简洁——客户端向授权服务器的注册端点发送一个 HTTP POST 请求，带上想要注册的元数据，授权服务器验证后返回 `client_id` 和其他注册信息。注册完成后，还可以通过客户端配置端点进行后续管理操作：

``` mermaid
sequenceDiagram
    participant Dev as 开发者/客户端
    participant AS as 授权服务器

    rect rgb(200, 200, 200)
        Note over Dev,AS: 可选准备步骤
        Dev->>AS: (A) 获取初始访问令牌（Initial Access Token）
        Dev->>AS: (B) 获取软件声明（Software Statement）
    end
    Dev->>AS: (C) POST /register<br/>（携带客户端元数据）
    AS-->>Dev: (D) 201 Created<br/>（返回 client_id + registration_access_token）

    rect rgb(210, 230, 210)
        Note over Dev,AS: 注册管理操作（RFC 7592）
        Dev->>AS: (E) GET/PUT/DELETE /register/{client_id}<br/>（携带 registration_access_token）
        AS-->>Dev: (F) 200 OK / 204 No Content
    end
```

(A) 和 (B) 是可选的准备工作——开放注册不需要它们，受保护注册需要初始访问令牌。(C) 是注册请求本身，(D) 是注册成功的响应。(E)(F) 是注册后的管理操作，使用注册时返回的 `registration_access_token` 认证。

## 🔀 开放注册 vs 受保护注册

授权服务器可以选择两种注册模式，对应不同的信任级别：

`开放注册`
:   任何客户端都可以注册，无需任何凭证。适用于开放平台场景（如公共 API 网关），但授权服务器应`配合速率限制`防止滥用，并警惕恶意注册。

`受保护注册`
:   需要持有`初始访问令牌`（Initial Access Token）才能调用注册端点。初始令牌通常通过授权服务器的`开发者门户`手动获取。适用于需要审核第三方接入的场景。

!!! tip "两种模式可以共存"
    同一个授权服务器可以同时支持开放注册和受保护注册——对信任的合作伙伴提供受保护注册，对普通开发者开放自助注册。

## 📋 注册时要填哪些信息？——客户端元数据

注册请求的本质就是提交一组客户端元数据。RFC 7591 定义了以下标准字段，授权服务器`必须忽略`它不认识的元数据字段——这与 OAuth2 的扩展性原则一致。

### 核心注册字段

| 字段 | 类型 | 必须 | 说明 |
|------|------|------|------|
| `redirect_uris` | string[] | ✅ | 回调地址列表，使用重定向流程的客户端必须注册 |
| `token_endpoint_auth_method` | string | 可选 | 令牌端点认证方式，默认 `client_secret_basic`。可选值：`none`（公开客户端）、`client_secret_basic`、`client_secret_post`，或注册到 IANA 的其他值 |
| `grant_types` | string[] | 可选 | 允许的授权类型，默认 `authorization_code` |
| `response_types` | string[] | 可选 | 允许的响应类型，默认 `code` |
| `scope` | string | 可选 | 允许的 Scope 列表（空格分隔）。未指定时授权服务器可颁发默认 Scope |

### 客户端展示信息

这些字段用于在授权页面向用户展示客户端的身份和相关信息：

| 字段 | 类型 | 说明 |
|------|------|------|
| `client_name` | string | 客户端的展示名称（推荐始终发送）。支持多语言（通过 `#` + BCP 47 语言标签，如 `client_name#ja`） |
| `client_uri` | string | 客户端信息主页（推荐始终发送），授权服务器应以可点击的形式展示 |
| `logo_uri` | string | 客户端 Logo 的 URL，授权服务器应在授权确认页面展示 |
| `contacts` | string[] | 负责人的联系方式（通常是邮箱），授权服务器可以将这些信息展示给用户 |
| `tos_uri` | string | 服务条款 URL，描述用户授权时接受的合约关系 |
| `policy_uri` | string | 隐私政策 URL，描述数据收集和使用方式 |

### 密钥与软件标识

| 字段 | 类型 | 说明 |
|------|------|------|
| `jwks_uri` | string | 客户端公钥 JWK Set 的 URL（`推荐`，便于密钥轮换） |
| `jwks` | object | 客户端公钥 JWK Set（内联，适用于无公网 URL 的原生应用） |
| `software_id` | string | 软件唯一标识符（如 UUID），跨实例保持不变，用于关联同一软件的不同注册实例 |
| `software_version` | string | 软件版本号，每次更新软件时应变更 |

!!! warning "`jwks_uri` 和 `jwks` 互斥"
    两者不能同时出现在同一请求或响应中。优先使用 `jwks_uri`，因为它支持远程密钥轮换，无需重新注册。

## 🔗 授权类型与响应类型的对应关系

`grant_types` 和 `response_types` 分别对应令牌端点和授权端点的参数，看起来是独立的，但它们之间存在内在关联——因为每种授权类型隐含了特定的响应类型。授权服务器应确保客户端注册不会进入不一致状态，否则返回 `invalid_client_metadata` 错误：

| grant_types 包含 | response_types 包含 |
|-------------------|---------------------|
| `authorization_code` | `code` |
| `implicit` | `token` |
| `password` | （无） |
| `client_credentials` | （无） |
| `refresh_token` | （无） |

> 例如：注册了 `authorization_code` 就必须同时注册 `code` 响应类型；注册了 `client_credentials` 则不需要任何响应类型（因为它不经过授权端点）。

## 📡 注册请求怎么发？

客户端向注册端点发送 `POST` 请求，请求体为 JSON 格式：

### 开放注册（无初始访问令牌）

``` http
POST /register HTTP/1.1
Content-Type: application/json
Accept: application/json
Host: server.example.com

{
  "redirect_uris": [
    "https://client.example.org/callback",
    "https://client.example.org/callback2"
  ],
  "client_name": "My Example Client",
  "token_endpoint_auth_method": "client_secret_basic",
  "scope": "read write photos"
}
```

### 受保护注册（带初始访问令牌）

初始访问令牌以 Bearer Token 的形式通过 `Authorization` 请求头传递。获取初始令牌的方式不在 RFC 7591 的范围内——通常是开发者通过授权服务器的管理门户手动获取：

``` http
POST /register HTTP/1.1
Content-Type: application/json
Accept: application/json
Authorization: Bearer ey23f2.adfj230.af32-developer321
Host: server.example.com

{
  "redirect_uris": ["https://client.example.org/callback"],
  "client_name": "My Protected Client",
  "token_endpoint_auth_method": "client_secret_basic",
  "policy_uri": "https://client.example.org/policy.html",
  "jwks": {
    "keys": [{
      "e": "AQAB",
      "n": "nj3YJwsLUFl9BmpAbkOswCNVx17Eh9wMO-_AReZwBq...",
      "kty": "RSA"
    }]
  }
}
```

> 注意：上面的 `jwks` 字段直接内联了公钥，适用于无法提供公网 URL 的原生应用。Web 应用推荐使用 `jwks_uri` 指向远程 JWK Set 文档。

## 📬 注册成功会返回什么？

### 成功响应

成功注册后，授权服务器返回 `201 Created`，包含 `client_id` 和所有已注册的元数据：

``` http
HTTP/1.1 201 Created
Content-Type: application/json
Cache-Control: no-store
Pragma: no-cache

{
  "client_id": "s6BhdRkqt3",
  "client_secret": "cf136dc3c1fc93f31185e5885805d",
  "client_id_issued_at": 2893256800,
  "client_secret_expires_at": 2893276800,
  "redirect_uris": [
    "https://client.example.org/callback",
    "https://client.example.org/callback2"
  ],
  "grant_types": ["authorization_code", "refresh_token"],
  "client_name": "My Example Client",
  "token_endpoint_auth_method": "client_secret_basic",
  "scope": "read write photos"
}
```

响应中的关键标识字段：

| 字段 | 必须 | 说明 |
|------|------|------|
| `client_id` | ✅ | 授权服务器颁发的唯一标识符 |
| `client_secret` | 可选 | 机密客户端的密钥（仅机密客户端会颁发） |
| `client_id_issued_at` | 可选 | `client_id` 的颁发时间（Unix 时间戳，秒） |
| `client_secret_expires_at` | 颁发 secret 时必须 | `client_secret` 的过期时间（0 表示永不过期） |

!!! note "授权服务器可以修改元数据"
    授权服务器可以拒绝或替换客户端请求的任何元数据值。例如，客户端请求 `scope=read write admin`，授权服务器可能只批准 `scope=read write`。客户端应检查响应中的实际值，确认是否满足使用需求。如果不满，可通过 RFC 7592（注册管理协议）尝试更新。

### 错误响应

注册失败时返回 `400 Bad Request`，错误码定义如下：

| 错误码 | 含义 | 示例场景 |
|--------|------|---------|
| `invalid_redirect_uri` | 回调地址无效 | 回调 URI 被加入黑名单 |
| `invalid_client_metadata` | 元数据字段无效 | `grant_types` 与 `response_types` 不匹配 |
| `invalid_software_statement` | 软件声明格式无效 | JWT 签名验证失败、格式错误 |
| `unapproved_software_statement` | 软件声明的签发者不受信任 | 签发者不在授权服务器的信任列表中 |

``` http
HTTP/1.1 400 Bad Request
Content-Type: application/json
Cache-Control: no-store
Pragma: no-cache

{
  "error": "invalid_redirect_uri",
  "error_description": "The redirection URI http://sketchy.example.com is not allowed by this server."
}
```

## 🛡️ 怎么防止客户端冒充？——软件声明

前面提到，客户端注册时提交的元数据都是`自声明`的——客户端说自己叫什么名字、用什么 Logo，全是自己说了算。这带来了一个安全风险：恶意客户端可以冒充合法客户端的名称和 Logo 来欺骗用户。

`软件声明`（Software Statement）是解决这个问题的机制。它是一个由可信第三方（如软件发布者）`数字签名`的 JWT，包含客户端的元数据声明。就像入住酒店时，不是自己口头报身份，而是出示了一张由公安机关盖章的**身份证**——前台不需要认识你，但信任盖章的机构。

### 软件声明的结构

软件声明是一个标准的 JWT（JWS 签名），`iss`（签发者）声明标识了为这些元数据担保的可信方：

``` json
{
  "software_id": "4NRB1-0XZABZI9E6-5SM3R",
  "client_name": "Example Statement-based Client",
  "client_uri": "https://client.example.net/"
}
```

### 使用软件声明注册

在注册请求中，通过 `software_statement` 字段传递签名的 JWT：

``` http
POST /register HTTP/1.1
Content-Type: application/json
Accept: application/json
Host: server.example.com

{
  "redirect_uris": [
    "https://client.example.org/callback"
  ],
  "software_statement": "eyJhbGciOiJSUzI1NiJ9.eyJzb2Z0d2FyZV9pZCI6...",
  "scope": "read write"
}
```

### 优先级规则

当注册请求中同一个元数据字段同时出现在普通 JSON 和软件声明中时：

- 如果授权服务器`信任`该软件声明的签发者，软件声明中的值`优先`
- 如果不信任或不支持软件声明，则`忽略`软件声明，使用普通 JSON 中的值

!!! tip "软件声明的推荐做法"
    - 推荐使用 `RS256` 签名算法
    - 推荐包含 `software_id` 声明，便于授权服务器关联同一软件的不同实例
    - 软件声明随所有客户端实例分发，获取方式不在 RFC 7591 范围内（通常是开发者向软件发布者注册获取）

## 🔒 动态注册有哪些安全风险？

### 传输安全

注册端点传输明文凭据（请求体中的元数据和响应中的 `client_secret`），`必须使用 TLS` 保护。客户端必须验证服务器的 TLS 证书链，防止 DNS 劫持。

### 回调地址验证

使用重定向流程的客户端`必须注册回调地址`，且注册的回调地址只能是以下三种之一：

| 类型 | 示例 | 说明 |
|------|------|------|
| TLS 保护的远程网站 | `https://client.example.com/callback` | 最常见，生产环境必须 |
| 本机 HTTP 地址 | `http://localhost:8080/callback` | 仅限开发环境 |
| 应用自定义 URI | `myapp://oauth_redirect` | 原生应用使用 |

这可以防止攻击者通过伪造的回调地址截获授权码或 Token。

### 防止客户端冒充

授权服务器不能仅凭客户端自称的信息来判断其真实性，应从整体上评估注册请求：

- 检查 Logo 域名是否与回调地址的域名`一致`
- 同一个 `software_id` 注册了不同的回调地址，可能是冒充行为
- 对最近注册的客户端向用户展示`额外警告`
- 验证 `client_uri`、`policy_uri`、`tos_uri` 等展示型 URL 是否指向有效页面

!!! warning "展示型 URL 的安全风险"
    恶意客户端可能在 `policy_uri` 等字段中放置恶意链接（如下载木马），诱导用户在授权页面点击。授权服务器应尽量验证这些 URL 的安全性，甚至可以在跳转前展示一个`中间警告页`。

### 客户端密钥安全

即使多个客户端实例共享同一个 `client_id`，授权服务器`不应`向它们颁发相同的 `client_secret`——否则一个实例泄露密钥后，所有实例都受影响。

### 不同授权类型的注册隔离

不同授权类型有不同的安全属性。授权服务器`可以要求`不同的授权类型分开注册：

- 使用 `authorization_code` 的客户端通常需要 `client_secret`
- 使用 `implicit` 的客户端不应有 `client_secret`
- `authorization_code`（代表用户授权）和 `client_credentials`（代表客户端自身）可能需要不同的 Scope

如果授权服务器不允许某些授权类型组合，应返回 `invalid_client_metadata` 错误。

## 👁️ 动态注册涉及哪些隐私问题？

RFC 7591 主要处理的是软件信息而非个人信息，隐私关注点较少：

- `contacts` 字段包含开发者联系信息，会被展示给用户和管理员，建议使用`专用邮箱`而非个人邮箱
- `client_id` 通常每实例唯一，原生应用的 `client_id` 可能关联到特定设备，但 OAuth 授权流程本身已可关联用户和客户端，动态注册不显著增加隐私风险
- 客户端`禁止自行生成` `client_id`——否则可能被用于跨授权服务器追踪用户

## 🔧 注册后怎么管理？——RFC 7592

RFC 7591 解决了"如何注册"的问题，但注册完成后呢？应用的回调地址可能变更、Scope 可能需要调整、应用下线时需要清理注册信息——这些都需要**管理操作**。RFC 7592（目前仍为 Experimental 状态）定义了三种管理操作，通过`客户端配置端点`（Client Configuration Endpoint）提供。

### 注册访问令牌与配置端点

注册成功后，授权服务器的响应中会额外返回两个管理所需的关键字段：

| 字段 | 说明 |
|------|------|
| `registration_client_uri` | ✅ 必须。客户端配置端点的完整 URL，用于后续的读取/更新/删除操作 |
| `registration_access_token` | ✅ 必须。注册访问令牌，用于认证所有对配置端点的请求 |

``` http
HTTP/1.1 201 Created
Content-Type: application/json
Cache-Control: no-store

{
  "registration_access_token": "reg-23410913-abewfq.123483",
  "registration_client_uri": "https://server.example.com/register/s6BhdRkqt3",
  "client_id": "s6BhdRkqt3",
  "client_secret": "cf136dc3c1fc93f31185e5885805d",
  "client_id_issued_at": 2893256800,
  "client_secret_expires_at": 2893276800,
  "redirect_uris": ["https://client.example.org/callback"],
  "grant_types": ["authorization_code", "refresh_token"],
  "client_name": "My Example Client",
  "token_endpoint_auth_method": "client_secret_basic"
}
```

> 配置端点的 URL 由授权服务器在注册响应中提供，客户端`不应自行拼接`。常见的 URL 模式包括路径参数（`/register/s6BhdRkqt3`）和查询参数（`/register?client_id=s6BhdRkqt3`）。

### 三种凭证的角色区分

整个动态注册流程中有三种不同的凭证，容易混淆：

| 凭证 | 用途 | 生命周期 | 使用端点 |
|------|------|---------|---------|
| `Initial Access Token` | 授权注册请求（受保护注册时使用） | 由授权服务器颁发，可共享给多个客户端实例 | 注册端点（`POST /register`） |
| `Registration Access Token` | 管理已注册的客户端（读取/更新/删除） | 注册时颁发，绑定到特定 `client_id`，`不应`在实例间共享 | 配置端点（`GET/PUT/DELETE /register/{id}`） |
| `Client Secret` | 客户端向令牌端点认证身份 | 每实例独立颁发，用于获取 Access Token | 令牌端点（`POST /token`） |

### 读取注册信息（GET）

查看客户端当前的注册状态——就像在酒店自助机上查看你的入住信息：

``` http
GET /register/s6BhdRkqt3 HTTP/1.1
Accept: application/json
Host: server.example.com
Authorization: Bearer reg-23410913-abewfq.123483
```

成功返回 `200 OK`，包含所有已注册的元数据。

!!! important "响应中可能包含新的凭证"
    读取响应中的 `client_secret` 和 `registration_access_token` `可能`与之前不同——授权服务器可能在读取时轮换凭证。客户端收到新凭证后`必须立即丢弃`旧凭证。

### 更新注册信息（PUT）

更新客户端的注册元数据。**关键规则**：更新请求`必须包含所有`元数据字段（不仅是需要修改的字段），因为更新操作是`全量替换`而非增量合并——就像编辑文档时"另存为"会覆盖整个文件，而不是只改其中一行。省略的字段会被视为请求删除。

``` http
PUT /register/s6BhdRkqt3 HTTP/1.1
Content-Type: application/json
Accept: application/json
Host: server.example.com
Authorization: Bearer reg-23410913-abewfq.123483

{
  "client_id": "s6BhdRkqt3",
  "client_secret": "cf136dc3c1fc93f31185e5885805d",
  "redirect_uris": [
    "https://client.example.org/callback",
    "https://client.example.org/alt"
  ],
  "grant_types": ["authorization_code", "refresh_token"],
  "token_endpoint_auth_method": "client_secret_basic",
  "client_name": "My New Example",
  "client_name#fr": "Mon Nouvel Exemple",
  "logo_uri": "https://client.example.org/newlogo.png"
}
```

更新请求的注意事项：

- `必须包含` `client_id` 且必须与当前值一致
- 如果包含 `client_secret`，其值`必须匹配`当前颁发的密钥（不能自行设置新密钥）
- `不得包含` `registration_access_token`、`registration_client_uri`、`client_id_issued_at`、`client_secret_expires_at`（这些字段由服务器管理）
- 授权服务器可以替换任何无效的元数据值为合适的默认值

成功返回 `200 OK`，同样可能包含轮换后的新凭证。

### 删除注册（DELETE）

注销客户端——就像在酒店自助机上办理退房。删除后，`client_id` 和 `client_secret` 立即失效，无法再用于授权端点和令牌端点：

``` http
DELETE /register/s6BhdRkqt3 HTTP/1.1
Host: server.example.com
Authorization: Bearer reg-23410913-abewfq.123483
```

成功返回 `204 No Content`。

!!! tip "删除后的清理"
    授权服务器`应该`同时撤销与该客户端关联的所有授权许可、Access Token 和 Refresh Token，彻底清理客户端的痕迹。

### 凭证轮换

为降低凭证泄露的影响，授权服务器可以在客户端读取或更新注册信息时`轮换`（Rotation）凭证——返回新的 `registration_access_token` 和/或 `client_secret`，旧凭证立即失效。

```
客户端持有: reg-token-A, secret-A
    |
    |  PUT /register/s6BhdRkqt3 (使用 reg-token-A)
    v
服务器返回: reg-token-B, secret-B  ← 旧凭证立即失效
    |
    |  客户端立即丢弃 reg-token-A, secret-A
    |  后续操作使用 reg-token-B, secret-B
    v
```

!!! warning "轮换频率由服务器决定"
    客户端`不能主动请求`凭证轮换，是否轮换以及轮换频率完全由授权服务器控制。如果 `registration_access_token` 在非读取/更新的情况下过期或失效，客户端将`无法再管理`自己的注册信息，只能重新注册（会获得新的 `client_id`）。

### 管理端点的安全考量

### 传输安全

客户端配置端点同样`必须使用 TLS` 保护，因为请求和响应中都包含明文凭据。

### 注册访问令牌的安全

`registration_access_token` 是管理客户端注册的`唯一认证凭证`——持有它就能读取、修改甚至删除客户端的注册信息（包括 `client_secret`）。因此：

- 必须具有`足够的熵`来抵御猜测攻击（参照 RFC 6819 Section 5.1.4.2.2）
- `不应在客户端实例间共享`——否则一个实例可以修改或删除所有实例的注册
- 作为 Bearer Token，应按照 RFC 6750 的要求保护
- `注册访问令牌不应过期`，否则客户端将失去管理能力（只能重新注册）

### 客户端已删除时的处理

如果客户端已被删除，但客户端仍持有有效的 `registration_access_token` 并尝试访问配置端点，授权服务器`必须返回 401 Unauthorized`——不能返回"客户端不存在"这类泄露信息的错误，且该令牌`应立即被撤销`。

> 小结：RFC 7591 + RFC 7592 共同构成了动态客户端注册的完整生命周期——RFC 7591 负责"出生"（创建注册），RFC 7592 负责"成长"（更新元数据）和"注销"（删除注册）。配合注册访问令牌的凭证轮换机制，可以在自动化便利性和安全性之间取得平衡。

---

`← 上一篇：` [核心概念](../core-concepts/index.md)
`→ 下一篇：` [授权类型](../grant-types/index.md)
`↩ 返回专题：` [OAuth2 & OpenID Connect](../index.md)
