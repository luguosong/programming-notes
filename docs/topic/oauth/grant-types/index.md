# OAuth2 授权类型

OAuth2 规范定义了多种**授权类型（Grant Type）**，对应不同的应用场景。本文详细介绍各授权类型的流程机制，安全威胁分析见[安全实践](../security/index.md)。

## 授权码流程（Authorization Code）

授权码流程是 **OAuth2 最推荐**的标准流程，适用于有服务端的 Web 应用。核心优势：Access Token 仅在服务端与授权服务器之间交换，不经过浏览器，安全性最高。

``` mermaid
sequenceDiagram
    participant 用户浏览器
    participant 客户端服务端
    participant 授权服务器

    用户浏览器->>授权服务器: 1. 重定向至授权端点<br/>?response_type=code&client_id=...&redirect_uri=...&scope=...&state=...
    授权服务器-->>用户浏览器: 2. 展示登录/授权页面
    用户浏览器->>授权服务器: 3. 用户登录并同意授权
    授权服务器-->>用户浏览器: 4. 重定向回 redirect_uri<br/>?code=AUTH_CODE&state=...
    用户浏览器->>客户端服务端: 5. 转发授权码
    客户端服务端->>授权服务器: 6. POST /token<br/>grant_type=authorization_code&code=...&client_id=...&client_secret=...
    授权服务器-->>客户端服务端: 7. 返回 Access Token + Refresh Token
```

授权完成后，客户端携带获得的 Access Token 向资源服务器发起请求，资源服务器验证 Token 后返回受保护的资源，完整流程可参考[专题概述](../index.md)中的全景图。

**关键参数说明：**

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

PKCE（RFC 7636）是授权码流程的**安全增强扩展**，对于**公开客户端（SPA、移动应用）为必选**，机密客户端也推荐使用。

**流程机制：**

``` mermaid
sequenceDiagram
    participant 客户端
    participant 授权服务器

    Note over 客户端: 1. 生成随机 code_verifier（43-128 位随机字符）
    Note over 客户端: 2. 计算 code_challenge = BASE64URL(SHA256(code_verifier))
    客户端->>授权服务器: 3. 授权请求中附带<br/>code_challenge + code_challenge_method=S256
    授权服务器-->>客户端: 4. 返回授权码（记录 code_challenge）
    客户端->>授权服务器: 5. Token 请求中附带 code_verifier
    Note over 授权服务器: 6. 验证：SHA256(code_verifier) == 存储的 code_challenge
    授权服务器-->>客户端: 7. 验证通过，返回 Access Token
```

PKCE 用 `code_verifier`/`code_challenge` 绑定替代 `client_secret`，即使授权码被截获，攻击者也无法在 Token 端点使用（因为不知道 `code_verifier`）。

!!! tip "OAuth 2.1 草案变化"
    OAuth 2.1 草案已将 PKCE 对所有授权码流程设为强制（包括机密客户端），即使今天使用 OAuth 2.0 实现，也建议始终开启 PKCE。

## 客户端凭证流程（Client Credentials）

适用于**没有用户参与的机器对机器（M2M）调用**，如微服务间互相调用 API。

``` mermaid
sequenceDiagram
    participant 客户端服务
    participant 授权服务器

    客户端服务->>授权服务器: POST /token<br/>grant_type=client_credentials&client_id=...&client_secret=...&scope=...
    授权服务器-->>客户端服务: 返回 Access Token（无 Refresh Token）
```

!!! note "无 Refresh Token"
    客户端凭证流程通常不颁发 Refresh Token，Access Token 过期后直接重新请求即可。

## 隐式流程（Implicit）— 已不推荐

隐式流程曾经是 SPA 应用的标准方案，但 **OAuth 2.0 Security Best Current Practice（BCP）已将其标为不推荐**。

**问题所在：** Access Token 直接通过 URL fragment 返回给浏览器，暴露在浏览器历史记录和 Referrer 头中，存在泄露风险。

!!! danger "请使用授权码流程 + PKCE 替代"
    现代 SPA 应用应使用**授权码流程 + PKCE**，而非隐式流程。

## 资源所有者密码凭证（ROPC）— 已不推荐

ROPC 允许客户端直接收集用户名和密码后发送给授权服务器换取 Token。**OAuth 2.1 草案已将其移除。**

!!! danger "强烈不推荐"
    ROPC 破坏了 OAuth2 的核心设计原则：用户凭证不应暴露给客户端应用。仅在无法使用其他流程的遗留系统迁移场景中考虑。

## 设备授权流程（Device Flow）

适用于**无浏览器或输入受限的设备**（智能电视、命令行工具、IoT 设备）。

``` mermaid
sequenceDiagram
    participant 设备
    participant 用户浏览器
    participant 授权服务器

    设备->>授权服务器: POST /device_authorization<br/>client_id=...&scope=...
    授权服务器-->>设备: device_code + user_code + verification_uri
    Note over 设备: 展示 user_code 和 URL 给用户
    用户浏览器->>授权服务器: 访问 verification_uri，输入 user_code 并登录授权
    loop 轮询
        设备->>授权服务器: POST /token<br/>grant_type=urn:ietf:params:oauth:grant-type:device_code&device_code=...
        授权服务器-->>设备: authorization_pending 或 Access Token
    end
```

## 适用场景对比

| 授权类型 | 适用场景 | 推荐程度 | 需要用户参与 |
|---------|---------|---------|------------|
| 授权码 + PKCE | 所有有用户的应用（SPA、Web、移动端） | ✅ 强烈推荐 | ✔ |
| 客户端凭证 | 微服务、后台任务、M2M | ✅ 推荐 | ✘ |
| 设备授权 | 智能电视、CLI 工具、IoT | ✅ 推荐 | ✔（用辅助设备） |
| 隐式流程 | —（已废弃） | ❌ 不推荐 | ✔ |
| ROPC | 遗留系统迁移（仅此场景） | ⚠️ 避免 | ✔ |

---

**上一篇：** [核心概念](../core-concepts/index.md)
**下一篇：** [OpenID Connect](../openid-connect/index.md)
