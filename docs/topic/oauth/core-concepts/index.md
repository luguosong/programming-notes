# OAuth2 核心概念

在学习具体的授权流程之前，先理解 OAuth2 体系中的基础概念：谁参与了授权过程、交换的是什么、权限边界如何划定。

## 四种角色

OAuth2 规范（RFC 6749）定义了四种参与者：

`Resource Owner`（资源所有者）
:   拥有受保护资源的实体，通常是**最终用户**。例如，GitHub 上存储代码的账号持有人。

`Client`（客户端）
:   代表资源所有者访问受保护资源的应用程序。可以是 Web 应用、移动应用、桌面应用或后端服务。
    注意：Client 并不是指浏览器，而是**请求访问资源的应用本身**。

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

## 令牌与授权码

### Access Token（访问令牌）

Access Token 是客户端访问受保护资源的**凭证**，相当于临时通行证。它的特点：

- **有限期**：通常较短（几分钟到几小时），过期后需要刷新
- **有限权限**：仅包含授权时指定的 Scope 权限
- **不透明或结构化**：可以是 JWT（包含信息的自描述令牌），也可以是 Opaque Token（纯随机字符串）

### Refresh Token（刷新令牌）

Refresh Token 用于在 Access Token 过期后**无需用户重新授权**即可获取新的 Access Token。特点：

- **有效期更长**（几天到几个月）
- **只发送给授权服务器**，不发送给资源服务器
- 某些授权类型（如客户端凭证）不颁发 Refresh Token

### Authorization Code（授权码）

授权码是授权码流程中的**临时短效凭证**，用于换取 Access Token。特点：

!!! warning "授权码不是令牌"

    授权码本身不能用于访问资源，只能用一次，且有效期极短（通常 10 分钟内）。它的作用是通过前端（浏览器）安全地将授权意图传递给后端，再由后端通过安全信道换取真正的 Access Token。

## Scope（权限范围）

Scope 是客户端请求的**权限集合**，用空格分隔多个权限。例如：

```
scope=read:repos write:repos
scope=openid profile email
```

授权服务器可以颁发比请求范围更小的 Scope（部分授权），但不会超出请求范围。

## 核心端点

| 端点 | 作用 |
|------|------|
| **Authorization Endpoint** | 用户授权页面，客户端将用户重定向到此处 |
| **Token Endpoint** | 客户端用授权码/凭证换取 Token 的接口 |
| **Introspection Endpoint** | 资源服务器验证 Token 有效性的接口（RFC 7662） |
| **Revocation Endpoint** | 主动撤销 Token 的接口（RFC 7009） |

## 客户端注册

客户端在使用授权服务器之前，必须先**注册**。注册后获得：

`Client ID`
:   客户端的唯一标识符，公开可见，用于授权请求中标识应用。

`Client Secret`
:   客户端密钥，**只有机密客户端（服务端应用）才有**，类似密码，不能暴露在前端代码中。

`Redirect URI`
:   授权完成后授权服务器回调的地址，必须精确匹配注册值，防止授权码被重定向到恶意地址。

!!! tip "公开客户端 vs 机密客户端"

    - **机密客户端（Confidential Client）**：有能力安全存储 Client Secret（如服务端应用）
    - **公开客户端（Public Client）**：无法安全存储密钥（如单页应用 SPA、移动端 App），必须使用 PKCE 替代 Client Secret

---

**下一篇：** [授权类型](../grant-types/index.md) — 了解 OAuth2 的五大授权流程及其适用场景
