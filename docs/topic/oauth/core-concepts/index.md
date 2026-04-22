---
title: 核心概念
---

# OAuth2 核心概念

在深入具体的授权流程之前，我们先理解 OAuth2 体系的基础概念。别急，我会用生活中的类比帮你建立直觉，然后再给出精确定义。

**本文你会学到：**

- OAuth2 的四种角色及其职责
- 机密客户端与公开客户端的区别，以及客户端认证方式
- 客户端注册的核心要素（Client ID、Client Secret、Redirect URI）
- Scope（权限范围）的作用与使用方式

## 🎭 四种角色

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

## 🔑 客户端类型与注册

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

> 上面描述的是传统的`手动注册`方式——开发者登录授权服务器的管理控制台，逐一填写这些信息。RFC 7591 还定义了`动态客户端注册`协议，允许客户端通过标准 HTTP API 自动完成注册，无需人工干预。动态注册详见「扩展协议」。

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

## 🎛️ Scope（权限范围）

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

> 需要更细粒度权限控制的场景，可以使用 Rich Authorization Requests（RAR, RFC 9396），详见「扩展协议」。

---

`上一篇：` [OAuth2](../index.md)
`下一篇：` [授权流程](../authorization-flows/index.md)
