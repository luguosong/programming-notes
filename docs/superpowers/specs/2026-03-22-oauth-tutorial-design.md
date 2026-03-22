# OAuth2 & OpenID Connect 教程设计文档

**创建日期**：2026-03-22
**状态**：已批准

---

## 目标

在 `docs/topic/oauth/` 下编写一套 OAuth2 + OpenID Connect 教程，覆盖核心协议原理和 Spring 生态实战，按知识递进的方式拆分为多个独立文档。

---

## 核心约束

- **文档组织**：每个 Markdown 页面均使用独立文件夹（`topic/folder/index.md`）而非单文件（`topic/folder.md`）
- **Spring Authorization Server 版本**：1.5.x 是最后一代独立版本，之后已迁移至 Spring Security 7.0，文档须注明此迁移节点
- **框架范围**：Spring Authorization Server 1.5.x + Spring Security OAuth2（客户端/资源服务器）
- **文档定位**：原理 + 实践并重，按知识递进组织

---

## 文档目录结构

```
docs/topic/oauth/
├── index.md                           # OAuth 专题概述（入口）
├── core-concepts/
│   └── index.md                       # 核心概念
├── grant-types/
│   └── index.md                       # 授权类型
├── openid-connect/
│   └── index.md                       # OpenID Connect
├── jwt/
│   └── index.md                       # JWT 与令牌格式
├── security/
│   └── index.md                       # 安全实践
├── spring-auth-server/
│   └── index.md                       # 实战：授权服务器
└── spring-client-resource/
    └── index.md                       # 实战：客户端与资源服务器
```

---

## zensical.toml nav 变更

在 `专题研究` 节点下追加：

```toml
{ "OAuth2 & OIDC" = [
    "topic/oauth/index.md",
    { "核心概念" = "topic/oauth/core-concepts/index.md" },
    { "授权类型" = "topic/oauth/grant-types/index.md" },
    { "OpenID Connect" = "topic/oauth/openid-connect/index.md" },
    { "JWT 令牌" = "topic/oauth/jwt/index.md" },
    { "安全实践" = "topic/oauth/security/index.md" },
    { "实战：授权服务器" = "topic/oauth/spring-auth-server/index.md" },
    { "实战：客户端与资源服务器" = "topic/oauth/spring-client-resource/index.md" }
] }
```

---

## 各文档内容范围

### 1. `index.md` — OAuth 专题概述

**包含**：
- OAuth2 解决什么问题（传统密码共享问题）
- OAuth2 与 OpenID Connect 的关系（授权 vs 认证）
- 整体学习路径说明
- 协议全景图（Mermaid）

**不包含**：具体流程细节

---

### 2. `core-concepts/index.md` — 核心概念

**包含**：
- 四种角色：Resource Owner / Client / Resource Server / Authorization Server
- 两种令牌：Access Token / Refresh Token
- Scope（权限范围）
- 核心端点列表（Authorization Endpoint、Token Endpoint、Introspection、Revocation）
- 注册客户端（Client ID / Client Secret / Redirect URI）

**不包含**：具体授权流程步骤

---

### 3. `grant-types/index.md` — 授权类型

**包含**：
- 授权码流程（Authorization Code）— 完整时序图
- 隐式流程（Implicit）— 已不推荐，说明原因
- 客户端凭证流程（Client Credentials）
- 资源所有者密码凭证流程（ROPC）— 已不推荐，说明原因
- PKCE 扩展（Proof Key for Code Exchange）
- 设备授权流程（Device Flow）
- 各流程适用场景对比表

**不包含**：JWT 结构细节、Spring 配置

---

### 4. `openid-connect/index.md` — OpenID Connect

**包含**：
- OIDC 是 OAuth2 的身份层（认证 vs 授权 的清晰区分）
- ID Token 结构与标准 Claim（sub、iss、aud、exp、iat、nonce）
- UserInfo 端点
- OIDC 发现文档（.well-known/openid-configuration）
- OIDC 授权码流程（对比普通 OAuth2 的差异）
- scope：openid、profile、email、address、phone

**不包含**：JWT 签名验证细节（见 jwt 文档）

---

### 5. `jwt/index.md` — JWT 与令牌格式

**包含**：
- JWT 结构：Header / Payload / Signature（Base64url 编码）
- JWS（签名令牌）vs JWE（加密令牌）
- 常用签名算法：RS256 / ES256 / HS256 对比
- JWKS 端点（公钥获取）
- 令牌验证完整流程
- Opaque Token vs JWT 的权衡

**不包含**：具体框架配置

---

### 6. `security/index.md` — 安全实践

**包含**：
- PKCE 防止授权码拦截
- state 参数防 CSRF
- nonce 防重放（OIDC）
- 令牌存储安全（浏览器端）
- 令牌泄露后的应对
- 令牌刷新策略
- 常见攻击模式（授权码注入、SSRF via redirect_uri）

**不包含**：Spring 具体配置实现

---

### 7. `spring-auth-server/index.md` — 实战：授权服务器

**包含**：
- ⚠️ 版本说明：Spring Authorization Server 1.5.x 是最后一代独立版本；从 Spring Security 7.0 起已合并
- 快速入门（Spring Boot + Spring Authorization Server 1.5.x）
- 核心 Bean 配置：`AuthorizationServerSettings`、`RegisteredClientRepository`、`JWKSource`
- 自定义授权页面
- 配置 OIDC 支持
- 令牌自定义（Claims）
- 对应的 Spring Security 7.0 迁移说明

**不包含**：客户端/资源服务器配置

---

### 8. `spring-client-resource/index.md` — 实战：客户端与资源服务器

**包含**：
- OAuth2 客户端配置（`spring-boot-starter-oauth2-client`）
- `oauth2Login()`：用户登录（OIDC）
- `oauth2Client()`：代调 API（Client Credentials）
- 资源服务器配置（`spring-boot-starter-oauth2-resource-server`）
- JWT 验证配置（JWKS URI）
- Opaque Token 验证（Introspection）
- 测试资源服务器

**不包含**：授权服务器配置

---

## 文档内标准结构模板

每篇文档统一遵循以下结构（按实际内容删减）：

1. **是什么** — 概念定义，1-2 段
2. **为什么** — 解决的问题，对比旧方案
3. **怎么运作** — 原理图 / 时序图（Mermaid）
4. **代码示例** — 仅实战文档有完整代码块
5. **相关链接** — 指向本系列其他文档

---

## CLAUDE.md 规则补充

需在项目 `CLAUDE.md` 中添加：

> **文档目录规则（强制）**：`docs/` 下所有 Markdown 页面均采用独立文件夹形式，即 `文件夹名/index.md`，禁止直接创建 `文件夹名.md` 平级文件（`docs/topic/oauth/index.md` 的直接子内容文档除外——它本身即 oauth 的 index）。
