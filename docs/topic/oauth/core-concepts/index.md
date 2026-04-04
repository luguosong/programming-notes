# OAuth2 核心概念

在深入具体的授权流程之前，我们先理解 OAuth2 体系的基础概念。别急，我会用生活中的类比帮你建立直觉，然后再给出精确定义。

**本文你会学到：**
- OAuth2 中的四种角色分别扮演什么「戏份」
- 客户端为什么分「机密」和「公开」两种，以及如何注册和认证
- Access Token、Refresh Token、授权码这三种凭证各自的作用
- Bearer Token 的三种传递方式（推荐/受限/禁止）及使用条件
- 资源服务器返回的 WWW-Authenticate 错误响应格式和状态码
- Scope 如何控制应用权限
- OAuth2 提供了哪些核心端点来完成整个交互
- OAuth2 为什么能持续演进而不破坏已有实现

## 四种角色

把 OAuth2 想象成一个**快递代取系统**：你（收件人）委托快递站（授权服务器）授权快递员（客户端）从某个仓库（资源服务器）代取你的包裹。OAuth2 规范（RFC 6749）定义了四个参与者，正好对应这个场景：

`Resource Owner`（资源所有者）
:   拥有受保护资源的实体，通常是`最终用户`。就像包裹的主人——**只有你有权决定**谁可以代取你的包裹。例如，GitHub 上存储代码的账号持有人。

`Client`（客户端）
:   代表资源所有者访问受保护资源的应用程序。就像你雇佣的`快递员`——他代你去仓库取包裹。注意：Client 并不是指浏览器，而是`请求访问资源的应用本身`。可以是 Web 应用、移动应用、桌面应用或后端服务。

`Authorization Server`（授权服务器）
:   负责验证资源所有者身份并颁发访问令牌的服务器。就像快递站的`前台工作人员`——核实你的身份后，给快递员开一张「代取凭证」。它是整个 OAuth2 流程的核心。

`Resource Server`（资源服务器）
:   托管受保护资源的服务器，接受访问令牌并返回资源。就像`仓库管理员`——看到合法的代取凭证才放行。一个授权服务器可以对应多个资源服务器。

``` mermaid
graph LR
    RO[资源所有者] -->|授权| AS[授权服务器]
    AS -->|颁发 Token| Client[客户端]
    Client -->|携带 Token| RS[资源服务器]
    RS -->|返回资源| Client
    classDef role fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef server fill:transparent,stroke:#539bf5,color:#adbac7,stroke-width:2px
    class RO,Client role
    class AS,RS server
```

## 客户端类型与注册

### 机密客户端与公开客户端

为什么要把客户端分成两类？因为一个根本问题：**这个应用能不能安全地保管一个密钥？**

想象一下：你把银行保险柜的钥匙交给谁更放心？交给一个有独立锁好房间的保安（机密客户端），还是交给一个在大街上随时可能被翻口袋的快递员（公开客户端）？

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

客户端在使用授权服务器之前，必须先`注册`——就像入住酒店需要先办理入住登记。注册后获得：

`Client ID`
:   客户端的唯一标识符，公开可见，用于授权请求中标识应用。类似于用户的"用户名"，在系统中唯一标识该客户端。

`Client Secret`
:   客户端密钥，`只有机密客户端才有`，类似密码，不能暴露在前端代码中。

`Redirect URI`
:   授权完成后授权服务器回调的地址，必须精确匹配注册值，防止授权码被重定向到恶意地址。

注册时还需要为客户端配置一组允许的 `Scope`（以空格分隔的字符串列表），限定该客户端可以申请的最大权限范围。

### 客户端认证方式

机密客户端在向授权服务器的令牌端点发起请求时，需要`证明自己确实是注册的那个应用`（RFC 6749 Section 2.3）。常见的认证方式有三种：

`Client Secret`（客户端密钥）
:   最基础的认证方式，RFC 6749 定义了两种传递方式：
    - `client_secret_basic`（推荐）：通过 HTTP Basic 认证传递（`Authorization: Basic BASE64(client_id:client_secret)`）
    - `client_secret_post`：将 `client_id` 和 `client_secret` 作为请求体参数传递。仅限无法使用 HTTP Basic 的场景，且参数`只能放在 request-body 中，绝不能放在 request URI 中`

`Mutual TLS`（双向 TLS，RFC 8705）
:   客户端使用 TLS 客户端证书进行认证，同时可以将访问令牌与该证书绑定，使令牌即使被盗也无法在其他客户端使用。

`Private Key JWT`（私钥 JWT，RFC 7521/7523）
:   客户端使用私钥签发一个 JWT 断言，授权服务器使用预先注册的公钥验证签名。密钥对管理比共享密钥更安全。

!!! warning "PKCE 不是客户端认证"

    PKCE 用于防止授权码被截获后被滥用，但它`不能替代`客户端认证。使用客户端认证的应用也应该同时使用 PKCE。

!!! info "RFC 6749 对客户端认证的限制"

    - RFC 6749 推荐优先使用 `client_secret_basic`（HTTP Basic），`client_secret_post` 仅限无法使用 Basic 的场景
    - 授权服务器`不应`向公开客户端（原生应用、浏览器应用）颁发 `client_secret` 用于认证
    - 每次请求客户端`只能使用一种`认证方法

## 令牌体系

OAuth2 中有三种重要的凭证，初学者很容易混淆。用一个类比来区分它们：

> 把 Access Token 想象成**临时出入证**，Refresh Token 想象成**出入证续期凭证**，授权码想象成**办理出入证的取号条**——取号条本身不能进门，只是用来换真正的出入证。

### Access Token（访问令牌）

Access Token（RFC 6749 Section 1.4）是客户端访问受保护资源的`凭证`，相当于临时通行证。把它想象成一张限时限区的**游乐园手环**——戴上了就能玩对应区域的项目，但有时间限制。

- `有限期`：通常较短（几分钟到几小时），过期后需要刷新
- `有限权限`：仅包含授权时指定的 Scope 权限
- `格式不固定`：可以是 JWT（包含信息的自描述令牌），也可以是 Opaque Token（纯随机字符串），格式由授权服务器决定

!!! note "Access Token 的使用原则"

    - Access Token `不应被客户端读取或解释`——客户端不是令牌的目标受众，令牌是给资源服务器看的
    - Access Token `不传达用户身份信息`——如果需要用户信息，应使用 OIDC 的 ID Token
    - Access Token `只应用于请求资源服务器`——不应在客户端之间传递或用于其他用途

### Bearer Token 与发送者约束令牌

Access Token 还有一个重要区分：**它是否绑定了特定的使用者？**

`Bearer Token`（不记名令牌，RFC 6750）
:   最常见的访问令牌类型。Bearer 的英文意思是"持有者"——谁拿着它谁就能用，就像`现金`一样。它可以是短十六进制字符串，也可以是结构化的 JWT。使用 Bearer Token 时，客户端与资源服务器之间的通信`必须使用 TLS`（HTTPS）保护，防止令牌在传输中被截获。

    RFC 6750 定义了三种向资源服务器传递 Bearer Token 的方式：

    1. `Authorization` 请求头（推荐）：`Authorization: Bearer <token>`
    2. 请求体参数：`access_token=xxx`（仅限 form-encoded body）
    3. URI 查询参数（不推荐，OAuth 2.1 已禁止）：Token 会暴露在浏览器历史记录、服务器日志和 Referrer 头中

    三种方式的详细说明如下。

#### Authorization 请求头（推荐）

RFC 6750 推荐的传递方式。客户端在 HTTP 请求头中使用 `Bearer` 认证方案发送 Token：

``` http
GET /resource HTTP/1.1
Host: server.example.com
Authorization: Bearer mF_9.B5f-4.1JqM
```

资源服务器`必须支持`这种方式。语法与 HTTP Basic 认证类似：`Authorization: Bearer <token>`。

#### Form-Encoded Body Parameter

当浏览器环境无法访问 `Authorization` 请求头时（极少数场景），可以将 Token 放在请求体中。RFC 6750 规定了严格的**使用前提**——必须同时满足以下所有条件：

- 请求的 `Content-Type` 为 `application/x-www-form-urlencoded`
- 请求体为单部分（非 multipart）
- 请求体内容全部为 ASCII 字符
- HTTP 方法**不能是 GET**（GET 请求没有请求体）

``` http
POST /resource HTTP/1.1
Host: server.example.com
Content-Type: application/x-www-form-urlencoded

access_token=mF_9.B5f-4.1JqM
```

> 除非确实无法使用 `Authorization` 请求头，否则`不应使用`这种方式。资源服务器`可选支持`。

#### URI Query Parameter（不推荐）

将 Token 放在 URL 查询参数中传递：

``` http
GET /resource?access_token=mF_9.B5f-4.1JqM HTTP/1.1
Host: server.example.com
```

RFC 6750 明确指出这种方式存在**严重安全隐患**：URL 会被记录在浏览器历史记录、Web 服务器访问日志、代理服务器日志中，还可能通过 `Referer` 头泄露给第三方网站。OAuth 2.1 已`完全禁止`此方式。

!!! warning "额外要求"
    使用 URI Query Parameter 方式时，客户端`应`同时发送 `Cache-Control: no-store` 头，防止响应被缓存。资源服务器在成功响应中`应`包含 `Cache-Control: private` 头。

#### WWW-Authenticate 错误响应

当资源服务器拒绝请求时，返回 `WWW-Authenticate` 响应头，使用 `Bearer` 认证方案。RFC 6750 定义了三个标准错误码：

``` http
# 未携带 Token 时
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="example"

# Token 过期时
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="example",
                      error="invalid_token",
                      error_description="The access token expired"
```

| 错误码 | HTTP 状态码 | 含义 |
|--------|------------|------|
| `invalid_request` | 400 | 请求缺少必需参数、参数重复、使用了多种 Token 传递方式 |
| `invalid_token` | 401 | Token 过期、被撤销、格式错误或无效。客户端`可以`用新 Token 重试 |
| `insufficient_scope` | 403 | Token 权限不足。响应中`可包含` `scope` 属性告知需要的权限 |

> 注意：如果请求完全没有携带认证信息（客户端不知道需要认证），资源服务器`不应`返回错误码细节，只返回 `WWW-Authenticate: Bearer realm="example"` 即可——这可以避免向潜在攻击者泄露信息。

`Sender-Constrained Token`（发送者约束令牌）
:   令牌与特定客户端的密钥绑定，使用时客户端必须`证明持有对应的私钥`。就像`实名绑定的门禁卡`——即使丢了，别人也刷不开，因为系统只认原持有人的指纹。即使令牌被盗，攻击者也无法使用。实现方式包括 mTLS（RFC 8705）和 DPoP（RFC 9449），详见「安全实践」。

### Refresh Token（刷新令牌）

Access Token 过期了怎么办？总不能让用户反复登录吧。Refresh Token（RFC 6749 Section 1.5）就是来解决这个问题的——它让你在 Access Token 过期后`无需用户重新授权`即可获取新的 Access Token。把它想象成出入证的**续期凭证**：手环到期了，拿续期凭证去前台换一个新手环，不用重新排队买票。

- `有效期更长`（几天到几个月）
- `只发送给授权服务器`，不发送给资源服务器
- `不能超越原始授权范围`：刷新时可以请求原始 Scope 的子集，但不能获取更多权限
- `公开客户端和机密客户端都可使用`，但公开客户端的 Refresh Token 被盗风险更高（攻击者可以冒充客户端使用），建议通过 DPoP 绑定来缓解
- 某些授权类型（如客户端凭证）不颁发 Refresh Token

Refresh Token 的存在使授权服务器可以为 Access Token 设置`较短的有效期`（如几分钟），兼顾安全性和用户体验——Access Token 频繁过期降低了泄露后的影响时间窗口，而 Refresh Token 让用户无需反复登录授权。

> 小结：Access Token 是「干活的凭证」（访问资源），Refresh Token 是「续命的凭证」（获取新 Access Token），授权码是「办证的取号条」（换 Access Token）。三者各司其职。

### Authorization Code（授权码）

授权码是授权码流程中的`临时短效凭证`，用于换取 Access Token。

!!! warning "授权码不是令牌"

    授权码本身不能用于访问资源，只能用一次，且有效期极短（通常 10 分钟内）。它的作用是通过前端通道（浏览器重定向）安全地将授权意图传递给客户端后端，再由后端通过安全的后端通道换取真正的 Access Token。详细流程见「授权类型」。

## Scope（权限范围）

Scope（RFC 6749 Section 3.3）是 OAuth2 中`限制应用对用户账户访问范围`的机制。

打个比方：你去酒店开房，前台给你一张房卡。这张房卡可以开哪些门？只能开你自己的房间？还是也能开健身房和游泳池？Scope 就是你在登记时勾选的**权限清单**。客户端在授权请求中申请一个或多个 Scope，用户在同意页面上查看这些权限请求后决定是否批准，最终颁发的令牌受批准的 Scope 限制。

```
scope=read:repos write:repos
scope=openid profile email
```

!!! info inline end "ABNF（RFC 6749 Appendix A）"
    `scope = scope-token *(SP scope-token)`，其中 `scope-token = 1*NQCHAR`（不含引号和空格的可见 ASCII 字符）

`Scope 的关键特性：`

- 授权服务器或用户可以`修改`最终授予的 Scope——可以颁发比请求范围更小的 Scope（部分授权），但不会超出客户端注册时配置的允许范围。就像你申请了"房间 + 健身房 + 游泳池"的权限，前台可能只给你"房间 + 健身房"——这是允许的，但不能超出
- OAuth 规范`不定义`具体的 Scope 值，因为权限划分高度依赖服务的内部架构和业务需求。例如 GitHub 使用 `repo`、`user:email`，Google 使用 `https://www.googleapis.com/auth/drive.readonly`
- 如果客户端对获批的 Scope 不满意，可以再次引导用户授权，但频繁请求会影响用户体验，因此建议`只申请必需的最小权限`

## Rich Authorization Requests（RAR, RFC 9396）

Scope 机制虽然简洁，但有个局限——它只能表达`粗粒度`的权限。

举个例子：`scope=transfer` 只能表达"允许转账"，但**转多少？转到哪个账户？在什么时间段内有效？**这些细节 Scope 表达不了。Rich Authorization Requests（RAR）通过引入 `authorization_details` 参数，允许客户端在授权请求中携带`结构化的细粒度权限描述`——就像把原来一句话的"授权书"换成了一份详细写明金额、对象、期限的**合同**。

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

前面讲了角色、令牌、权限，那这些角色之间到底通过哪些「窗口」来交互呢？OAuth2 定义了一组标准化的 HTTP 端点（也就是 API 接口）。把授权服务器想象成一个**政务服务中心**，下面这些端点就是不同的办事窗口：

### 授权端点与令牌端点

这两个是 OAuth2 最基础的端点，所有授权流程都围绕它们展开：

| 端点 | 方式 | 通道 | 作用 |
|------|------|------|------|
| `Authorization Endpoint` | GET | 前端通道（浏览器重定向） | 用户授权页面，客户端将用户重定向到此处进行登录和授权确认 |
| `Token Endpoint` | POST | 后端通道（服务端直连） | 客户端用授权码/凭证换取 Token 的接口，机密客户端需在此认证 |

### 重定向端点（Redirection Endpoint）

`Redirection Endpoint` 是客户端提供的回调地址，授权服务器在完成用户授权后将响应（授权码或错误信息）通过浏览器重定向发送到此处。RFC 6749 Section 3.1.2 对其有严格规范：

- `必须是绝对 URI`（如 `https://example.com/callback`），不能是相对路径
- `必须预先注册`到授权服务器，授权服务器会将请求中的 `redirect_uri` 与注册值进行精确比较
- `可注册多个`重定向 URI（如不同环境的回调地址）
- 授权失败时`不得重定向`到无效或未注册的 URI（防止开放重定向攻击）
- 重定向端点的响应页面`不应包含第三方脚本`（如分析代码、广告网络），防止授权码在到达客户端之前被第三方读取。如果必须包含，客户端提取凭证的脚本`必须`最先执行

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

## 扩展性机制（RFC 6749 Section 8）

OAuth 2.0 被设计为`高度可扩展的框架`，定义了多个标准化扩展点，使其能够持续演进而不破坏已有实现。

打个比方：OAuth2 就像一部**智能手机的操作系统**——核心功能（打电话、发短信）不变，但通过"应用商店"（扩展点）可以不断安装新功能。PKCE、DPoP、RAR 这些扩展，都是后来上架的"应用"。

`新授权类型：` 通过绝对 URI 作为 `grant_type` 值注册。例如本文中介绍过的 Token Flow 使用 `urn:ietf:params:oauth:grant-type:token-exchange`，Device Flow 使用 `urn:ietf:params:oauth:grant-type:device_code`——这些都是 RFC 6749 之后的扩展，通过 URI 命名空间避免了与标准授权类型的冲突。

`新响应类型：` 注册到 IANA Authorization Endpoint Response Types Registry。标准定义了 `code`（返回授权码）和 `token`（直接返回 Token），扩展如 `code id_token`（OIDC 混合流程）通过注册加入。

`新端点参数：` 注册到 IANA OAuth Parameters Registry。例如 PKCE 引入的 `code_challenge`/`code_verifier`、PAR 引入的 `request_uri`、RAR 引入的 `authorization_details` 都是通过此机制成为标准参数的。

`核心原则：` 实现`必须忽略未知参数`——这是 OAuth 2.0 能持续演进的基础。一个 OAuth 2.0 客户端即使不认识某个新参数，也不应报错，而是简单地跳过它。这确保了新旧实现之间的互操作性。

---

`下一篇：` [授权类型](../grant-types/index.md) — 了解 OAuth2 的五大授权流程及其适用场景
