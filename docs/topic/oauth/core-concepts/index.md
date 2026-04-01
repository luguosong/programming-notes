# OAuth2 核心概念

在学习具体的授权流程之前，先理解 OAuth2 体系中的基础概念：谁参与了授权过程、客户端如何标识自身、交换的是什么凭证、权限边界如何划定、以及通过哪些端点完成交互。

## 四种角色

OAuth2 规范（RFC 6749）定义了四种参与者：

`Resource Owner`（资源所有者）
:   拥有受保护资源的实体，通常是`最终用户`。例如，GitHub 上存储代码的账号持有人。

`Client`（客户端）
:   代表资源所有者访问受保护资源的应用程序。可以是 Web 应用、移动应用、桌面应用或后端服务。
    注意：Client 并不是指浏览器，而是`请求访问资源的应用本身`。

`Authorization Server`（授权服务器）
:   负责验证资源所有者身份并颁发访问令牌的服务器。它是整个 OAuth2 流程的核心。

`Resource Server`（资源服务器）
:   托管受保护资源的服务器，接受访问令牌并返回资源。一个授权服务器可以对应多个资源服务器。

``` mermaid
graph LR
    RO[资源所有者] -->|授权| AS[授权服务器]
    AS -->|颁发 Token| Client[客户端]
    Client -->|携带 Token| RS[资源服务器]
    RS -->|返回资源| Client
```

## 客户端类型与注册

### 机密客户端与公开客户端

OAuth2（RFC 6749 Section 2.1）根据客户端`能否安全保管凭证`，将客户端分为两类：

`机密客户端（Confidential Client）`
:   能够安全地存储和使用 `client_secret` 的应用。典型代表是有独立后端服务器的 Web 应用——密钥保存在服务端，前端浏览器无法访问。

`公开客户端（Public Client）`
:   无法安全存储 `client_secret` 的应用。典型代表是单页应用（SPA）、移动端 App 和桌面应用——它们的代码运行在用户设备上，任何内嵌的密钥都可能被提取。

| 特征 | 机密客户端 | 公开客户端 |
|------|-----------|-----------|
| 典型应用 | 服务端 Web 应用、后端微服务 | SPA、移动 App、桌面应用、CLI 工具 |
| Client Secret | ✔ 安全保管在服务端 | ✘ 无法安全存储 |
| 令牌端点认证 | client_secret / mTLS / Private Key JWT | 仅依赖 PKCE |
| Refresh Token 被盗风险 | 较低（需同时窃取客户端凭证） | 较高（攻击者可冒充客户端使用） |

!!! tip "不同客户端场景的安全要求"

    - `原生/移动应用`（RFC 8252）：禁止使用嵌入式 WebView 进行授权（易受钓鱼攻击），应使用系统浏览器或 ASWebAuthenticationSession 等平台 API，并搭配 PKCE
    - `浏览器应用/SPA`（OAuth BCP 草案）：应使用授权码流程 + PKCE，而非已弃用的隐式流程
    - `服务端应用`：使用 `client_secret` 或更强的认证方式（mTLS、Private Key JWT）

### 客户端注册

客户端在使用授权服务器之前，必须先`注册`（RFC 6749 Section 2）。注册后获得：

`Client ID`
:   客户端的唯一标识符，公开可见，用于授权请求中标识应用。类似于用户的"用户名"，在系统中唯一标识该客户端。

`Client Secret`
:   客户端密钥，`只有机密客户端才有`，类似密码，不能暴露在前端代码中。

`Redirect URI`
:   授权完成后授权服务器回调的地址，必须精确匹配注册值，防止授权码被重定向到恶意地址。

注册时还需要为客户端配置一组允许的 `Scope`（以空格分隔的字符串列表），限定该客户端可以申请的最大权限范围。

### 客户端认证方式

机密客户端在向授权服务器的令牌端点发起请求时，需要`证明自身身份`（RFC 6749 Section 2.3）。常见的认证方式有三种：

`Client Secret`（客户端密钥）
:   最基础的认证方式。可以通过 HTTP Basic 认证（`Authorization: Basic BASE64(client_id:client_secret)`）或表单参数（`client_id` + `client_secret`）传递。

`Mutual TLS`（双向 TLS，RFC 8705）
:   客户端使用 TLS 客户端证书进行认证，同时可以将访问令牌与该证书绑定，使令牌即使被盗也无法在其他客户端使用。

`Private Key JWT`（私钥 JWT，RFC 7521/7523）
:   客户端使用私钥签发一个 JWT 断言，授权服务器使用预先注册的公钥验证签名。密钥对管理比共享密钥更安全。

!!! warning "PKCE 不是客户端认证"

    PKCE 用于防止授权码被截获后被滥用，但它`不能替代`客户端认证。使用客户端认证的应用也应该同时使用 PKCE。

## 令牌体系

### Access Token（访问令牌）

Access Token（RFC 6749 Section 1.4）是客户端访问受保护资源的`凭证`，相当于临时通行证。

- `有限期`：通常较短（几分钟到几小时），过期后需要刷新
- `有限权限`：仅包含授权时指定的 Scope 权限
- `格式不固定`：可以是 JWT（包含信息的自描述令牌），也可以是 Opaque Token（纯随机字符串），格式由授权服务器决定

!!! note "Access Token 的使用原则"

    - Access Token `不应被客户端读取或解释`——客户端不是令牌的目标受众，令牌是给资源服务器看的
    - Access Token `不传达用户身份信息`——如果需要用户信息，应使用 OIDC 的 ID Token
    - Access Token `只应用于请求资源服务器`——不应在客户端之间传递或用于其他用途

### Bearer Token 与发送者约束令牌

根据令牌`是否绑定到特定客户端`，Access Token 分为两类：

`Bearer Token`（不记名令牌，RFC 6750）
:   最常见的访问令牌类型。Bearer Token 是不透明的字符串，任何持有它的人都可以使用——类似于现金，谁拿到谁就能用。它可以是短十六进制字符串，也可以是结构化的 JWT。使用 Bearer Token 时，客户端与资源服务器之间的通信`必须使用 TLS`（HTTPS）保护，防止令牌在传输中被截获。

`Sender-Constrained Token`（发送者约束令牌）
:   令牌与特定客户端的密钥绑定，使用时客户端必须`证明持有对应的私钥`。即使令牌被盗，攻击者也无法使用。实现方式包括 mTLS（RFC 8705）和 DPoP（RFC 9449），详见「安全实践」。

### Refresh Token（刷新令牌）

Refresh Token（RFC 6749 Section 1.5）用于在 Access Token 过期后`无需用户重新授权`即可获取新的 Access Token。

- `有效期更长`（几天到几个月）
- `只发送给授权服务器`，不发送给资源服务器
- `不能超越原始授权范围`：刷新时可以请求原始 Scope 的子集，但不能获取更多权限
- `公开客户端和机密客户端都可使用`，但公开客户端的 Refresh Token 被盗风险更高（攻击者可以冒充客户端使用），建议通过 DPoP 绑定来缓解
- 某些授权类型（如客户端凭证）不颁发 Refresh Token

Refresh Token 的存在使授权服务器可以为 Access Token 设置`较短的有效期`（如几分钟），兼顾安全性和用户体验——Access Token 频繁过期降低了泄露后的影响时间窗口，而 Refresh Token 让用户无需反复登录授权。

### Authorization Code（授权码）

授权码是授权码流程中的`临时短效凭证`，用于换取 Access Token。

!!! warning "授权码不是令牌"

    授权码本身不能用于访问资源，只能用一次，且有效期极短（通常 10 分钟内）。它的作用是通过前端通道（浏览器重定向）安全地将授权意图传递给客户端后端，再由后端通过安全的后端通道换取真正的 Access Token。详细流程见「授权类型」。

## Scope（权限范围）

Scope（RFC 6749 Section 3.3）是 OAuth2 中`限制应用对用户账户访问范围`的机制。客户端在授权请求中申请一个或多个 Scope，用户在同意页面上查看这些权限请求后决定是否批准，最终颁发的令牌受批准的 Scope 限制。

```
scope=read:repos write:repos
scope=openid profile email
```

`Scope 的关键特性：`

- 授权服务器或用户可以`修改`最终授予的 Scope——可以颁发比请求范围更小的 Scope（部分授权），但不会超出客户端注册时配置的允许范围
- OAuth 规范`不定义`具体的 Scope 值，因为权限划分高度依赖服务的内部架构和业务需求。例如 GitHub 使用 `repo`、`user:email`，Google 使用 `https://www.googleapis.com/auth/drive.readonly`
- 如果客户端对获批的 Scope 不满意，可以再次引导用户授权，但频繁请求会影响用户体验，因此建议`只申请必需的最小权限`

## Rich Authorization Requests（RAR, RFC 9396）

Scope 机制虽然简洁，但只能表达`粗粒度`的权限。例如 `scope=transfer` 只能表达"允许转账"，却无法限定转账金额上限或目标账户。Rich Authorization Requests（RAR）通过引入 `authorization_details` 参数，允许客户端在授权请求中携带`结构化的细粒度权限描述`。

### 基本结构

RAR 在授权请求中添加 `authorization_details` 参数（JSON 数组），每个元素是一个权限对象：

``` json
{
  "authorization_details": [
    {
      "type": "payment_initiation",
      "actions": ["initiate", "status", "cancel"],
      "locations": ["https://api.example.com/payments"],
      "instructedAmount": {
        "currency": "EUR",
        "amount": "123.50"
      },
      "creditorName": "Merchant A",
      "creditorAccount": {
        "iban": "DE02100100109307118603"
      }
    }
  ]
}
```

| 字段 | 说明 | 是否必须 |
|------|------|---------|
| `type` | 权限类型标识符，由 API 提供方定义（如 `payment_initiation`、`account_information`） | ✅ 必须 |
| `actions` | 允许的操作列表 | 可选 |
| `locations` | 目标资源服务器的 URI | 可选 |
| `datatypes` | 请求的数据类型 | 可选 |
| 自定义字段 | `type` 特定的业务字段（如金额、账户、有效期等） | 由 `type` 定义 |

### Scope vs RAR

| 维度 | Scope | RAR（authorization_details） |
|------|-------|---------------------------|
| 粒度 | 粗粒度（`read`、`write`、`transfer`） | 细粒度（金额、账户、操作类型、有效期） |
| 格式 | 空格分隔的字符串 | 结构化 JSON |
| 标准化 | 各服务自定义 | `type` 字段提供类型化结构 |
| 典型场景 | API 基础权限控制 | 金融支付、医疗数据、合规审计 |

!!! tip "Scope 和 RAR 可以同时使用"
    RAR 不替代 Scope，两者`可以在同一请求中并存`。例如使用 `scope=openid` 触发 OIDC 身份认证，同时使用 `authorization_details` 描述具体的支付权限。

## 核心端点

### 授权端点与令牌端点

这两个是 OAuth2 最基础的端点，所有授权流程都围绕它们展开：

| 端点 | 方式 | 通道 | 作用 |
|------|------|------|------|
| `Authorization Endpoint` | GET | 前端通道（浏览器重定向） | 用户授权页面，客户端将用户重定向到此处进行登录和授权确认 |
| `Token Endpoint` | POST | 后端通道（服务端直连） | 客户端用授权码/凭证换取 Token 的接口，机密客户端需在此认证 |

### 令牌内省端点（RFC 7662）

令牌内省（Token Introspection）为资源服务器提供了一种`查询令牌信息`的机制。资源服务器将收到的 Access Token 发送到授权服务器的内省端点，获取令牌是否有效、关联的用户、授权的 Scope 等信息。

这对`不透明令牌（Opaque Token）`尤为重要——不透明令牌本身不包含任何信息，资源服务器必须通过内省端点才能验证和解析它。

### 令牌撤销端点（RFC 7009）

令牌撤销（Token Revocation）允许客户端`主动通知授权服务器某个令牌不再需要`。典型场景是实现"登出"功能——用户退出应用时，客户端将 Access Token 和/或 Refresh Token 发送到撤销端点，授权服务器清理关联的安全凭证。

### 令牌交换（RFC 8693）

令牌交换（Token Exchange）允许客户端`用一组令牌换取另一组令牌`。典型应用场景包括：

- 多个移动应用之间实现`单点登录（SSO）`而无需打开浏览器
- 资源服务器将客户端的令牌`换成自己的令牌`，用于调用下游服务

### 授权服务器元数据发现（RFC 8414）

授权服务器元数据（也称 OAuth Discovery）定义了一种标准格式，让客户端可以`自动发现`授权服务器的配置信息，而无需手动配置每个端点地址。

客户端访问 `/.well-known/oauth-authorization-server` 即可获取 JSON 格式的元数据，包括授权端点地址、令牌端点地址、支持的 Scope 列表、支持的客户端认证方式等。OIDC 的发现文档（`/.well-known/openid-configuration`）是此规范的扩展。

---

`下一篇：` [授权类型](../grant-types/index.md) — 了解 OAuth2 的五大授权流程及其适用场景
