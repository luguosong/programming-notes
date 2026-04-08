---
title: 扩展协议
description: OAuth2 生态中主要扩展协议的概览与导航，包括 PKCE、PAR、RAR、DPoP、mTLS 等
---

# 扩展协议

OAuth2 的核心规范（RFC 6749）定义了基础框架——四种角色、几种授权类型、Token 的颁发与使用。但在实际生产环境中，仅靠核心规范远远不够。就像你买了一台基础款电脑后，还需要加装内存条、独立显卡、外接硬盘等专业配件，OAuth2 也需要一系列扩展协议来满足安全性、互操作性和高级功能需求。

**本文你会学到：**

- OAuth2 生态中主要扩展协议的用途和适用场景
- 每个扩展协议解决了什么问题
- 如何选择适合自己场景的扩展协议

## 扩展协议概览

下表列出了 OAuth2 生态中最常用的扩展协议。不是每个项目都需要全部实现——根据你的安全要求和部署场景，按需选用即可。

| 扩展协议 | RFC | 解决的问题 | 适用场景 |
|---------|-----|-----------|---------|
| PKCE | 7636 | 防止授权码被截获后滥用 | 公开客户端（SPA、移动端），实际已成为所有授权码流程的标配 |
| PAR | 9126 | 授权请求参数暴露在浏览器 URL 中 | 高安全要求环境、复杂授权请求 |
| RAR | 9396 | 传统 Scope 字符串粒度太粗 | 需要精确声明权限范围（如指定某个资源实例） |
| Token Exchange | 8693 | 不同系统间的身份与授权传递 | 微服务架构、跨系统令牌转换 |
| DPoP | 9449 | Bearer Token 谁持有谁就能用 | 令牌泄露风险高的环境，需将令牌绑定到客户端密钥 |
| mTLS | 8705 | 基于 TLS 证书的客户端认证与令牌绑定 | 企业级服务端应用，已有 PKI 基础设施 |
| 动态客户端注册 | 7591/7592 | 客户端需手动预配置才能接入 | 多租户平台、开放 API 生态 |

## PKCE（RFC 7636）

PKCE（Proof Key for Code Exchange）通过 `code_verifier` / `code_challenge` 机制，确保只有发起授权请求的那个客户端实例能使用授权码换 Token。即使攻击者截获了授权码，没有对应的 `code_verifier` 也无法通过验证。

PKCE 是现代 OAuth2 实现`几乎必选`的扩展——RFC 9700（OAuth 2.0 Security Best Current Practice）要求所有授权码流程都必须使用 PKCE，包括机密客户端。详细流程见「授权流程 · PKCE」，安全分析见「安全实践 · PKCE」。

## PAR（RFC 9126）

PAR（Pushed Authorization Requests）将授权请求参数从前端通道（浏览器 URL）移到后端通道（直接向授权服务器 POST）。客户端先提交完整参数获得一个短效的 `request_uri`，再重定向浏览器到授权端点——URL 中不再暴露任何敏感参数。

PAR 解决了两个问题：参数对浏览器历史记录、日志、第三方脚本`可见`；未经认证的任何人都能`伪造`授权链接。详见「安全实践 · PAR」。

## RAR（RFC 9396）

RAR（Rich Authorization Requests）允许客户端在授权请求中声明`细粒度的权限`，比传统 Scope 字符串更精确。

传统 Scope 是一维的权限标签——`scope=read` 只能表达"读权限"，但无法表达"读哪个用户的哪些资源"。RAR 使用 JSON 格式的权限声明（Authorization Detail），可以精确到资源实例级别。

一个 RAR 权限声明的结构：

``` json
{
  "type": "photo_access",
  "locations": ["https://photos.example.com/album/123"],
  "actions": ["read"],
  "datatypes": ["image/jpeg"]
}
```

`核心字段：`

| 字段 | 说明 |
|------|------|
| `type` | 权限类型标识符，由授权服务器定义或注册。不同 `type` 决定了其他字段的结构 |
| `locations`（可选） | 权限适用的资源位置（URL 列表） |
| `actions`（可选） | 允许的操作列表（如 `read`、`write`、`delete`） |
| `datatypes`（可选） | 允许访问的数据类型 |
| `identifier`（可选） | 资源的唯一标识符 |

RAR 与传统 Scope 可以`同时使用`——Scope 声明粗粒度权限，RAR 声明细粒度权限。授权服务器在颁发的 Token 中会包含完整的 Authorization Detail 信息。

!!! example "Scope vs RAR 对比"

    **传统 Scope 方式：**
    ```
    scope=photos:read photos:write
    ```
    → 只知道"可以读写照片"，不知道具体哪个相册

    **RAR 方式：**
    ``` json
    [
      {
        "type": "photo_access",
        "locations": ["https://photos.example.com/album/123"],
        "actions": ["read"]
      }
    ]
    ```
    → 精确到"只能读取 123 号相册的照片"

> RAR 特别适用于 API 粒度化授权场景。如果你的应用只需要粗粒度的权限控制，传统 Scope 就足够了。

## Token Exchange（RFC 8693）

Token Exchange 允许客户端用一种令牌`换取另一种令牌`。典型场景：用外部的 ID Token 或 Access Token 换取本系统的 Access Token，实现跨系统的身份传递和信任转换。

请求中通过 `grant_type=urn:ietf:params:oauth:grant-type:token-exchange` 和 `subject_token` / `actor_token` 参数指定源令牌，授权服务器验证后颁发目标令牌。详见「端点与发现 · 令牌交换端点」。

## DPoP（RFC 9449）

DPoP（Demonstrating Proof of Possession）通过客户端持有密钥对签发的证明（DPoP Proof JWT），将 Access Token 绑定到特定客户端。就像给门禁卡加上了指纹验证——刷卡不够，还得验证指纹，别人即使偷了你的卡也刷不开。

DPoP 在`应用层`实现令牌绑定，客户端自行生成临时密钥对，无需 CA 证书或 PKI 基础设施，部署成本低。即使令牌泄露，也无法在没有私钥的其他设备上使用。详见「安全实践 · 发送者约束令牌」。

## mTLS（RFC 8705）

mTLS（Mutual TLS）通过`双向 TLS 认证`实现令牌绑定：客户端使用 TLS 证书发起请求，授权服务器将令牌与证书指纹绑定，访问资源时验证 TLS 证书一致性。

与 DPoP 不同，mTLS 在`传输层`实现，提供更强的安全性（TLS 层完整保护所有通信内容），但需要 CA 签发的证书和 PKI 基础设施，部署成本较高。详见「安全实践 · 发送者约束令牌」，客户端认证方式见「核心概念 · 客户端认证方式」。

## 动态客户端注册（RFC 7591/7592）

传统方式下，客户端需要在授权服务器上`手动预配置`（注册 Client ID、Secret、Redirect URI 等）。动态客户端注册允许客户端通过标准 HTTP API 在`运行时自动完成注册`，无需人工干预。

这在多租户平台和开放 API 生态中特别有用——第三方开发者只需调用注册接口即可接入，无需联系管理员手动配置。RFC 7591 定义了核心注册协议，RFC 7592 补充了客户端管理的 CRUD 操作。详见「动态客户端注册」。

---

`上一篇：` [OpenID Connect](../openid-connect/index.md)
`下一篇：` [安全实践](../security/index.md)
