# OAuth 2.1：规范整合与演进

OAuth 2.1 并不是一个全新的协议，而是对 OAuth 2.0 生态中`多年最佳实践的整合与固化`。它将分散在多个 RFC 和 BCP（Best Current Practice）中的安全要求汇总为一份规范，消除了 OAuth 2.0 中已被证明不安全的选项，使新实现从一开始就处于安全状态。

**本文你会学到：**
- 🤔 OAuth 2.1 为什么出现——它解决了 OAuth 2.0 的哪些痛点
- 📚 OAuth 2.1 整合了哪些 RFC 和最佳实践
- ⚖️ OAuth 2.0 与 2.1 的七大关键差异（每一条都很重要）
- 🚀 如何将现有的 OAuth 2.0 实现迁移到 2.1

## 🤔 为什么需要 OAuth 2.1？

OAuth 2.0（RFC 6749）发布于 2012 年，距今已超过十年。在这期间，社区通过实践发现了多种安全问题，并通过一系列独立 RFC 和安全建议逐步修补：

- PKCE（RFC 7636，2015）解决了授权码截获攻击
- 原生应用最佳实践（RFC 8252，2017）禁止嵌入式 WebView
- 安全最佳实践（RFC 9700）明确废弃隐式流程和密码流程
- 浏览器应用最佳实践草案规范了 SPA 的安全架构

问题在于：开发者需要`同时阅读和理解 6+ 个 RFC`，才能正确实现一个安全的 OAuth2 系统。OAuth 2.1 的目标就是`将所有这些最佳实践合并为一份规范`，降低实现门槛，减少因遗漏某个 RFC 而引入的安全漏洞。

打个比方：OAuth 2.0 就像一部**使用说明书散落在各处的家电**——安全手册在抽屉里，安装指南在纸箱里，保修卡在另一个房间里。OAuth 2.1 就是把所有说明书**装订成了一本完整的操作手册**，你只需要看这一本就够了。

## 📚 OAuth 2.1 整合了哪些规范？

OAuth 2.1 将以下规范的核心要求合并为一个文档：

| 原始规范 | 关键内容 | 在 OAuth 2.1 中的处理 |
|---------|---------|---------------------|
| RFC 6749 | OAuth 2.0 核心框架 | 基础框架保留，移除不安全部分 |
| RFC 6750 | Bearer Token 使用方式 | 集成，禁止 URI 查询参数传递 Token |
| RFC 7636 | PKCE（Proof Key for Code Exchange） | 升级为所有授权码流程的强制要求 |
| RFC 8252 | 原生应用 OAuth 最佳实践 | 集成关键安全要求 |
| RFC 9700 | OAuth 2.0 安全最佳实践（BCP） | 将安全建议升级为规范要求 |
| 浏览器应用草案 | 浏览器端应用 OAuth 最佳实践 | 集成 SPA 安全指南 |

## ⚖️ 与 OAuth 2.0 的七大关键差异

### 1. PKCE 成为授权码流程的强制要求

最先要说也是最关键的：PKCE 从"推荐"变成了"必须"。

在 OAuth 2.0 中，PKCE 是可选扩展（RFC 7636），主要面向公开客户端。OAuth 2.1 将 PKCE `提升为所有授权码流程的默认要求`，包括机密客户端。

| 维度 | OAuth 2.0 | OAuth 2.1 |
|------|----------|----------|
| 公开客户端 | PKCE 推荐 | PKCE **必须** |
| 机密客户端 | PKCE 可选 | PKCE **必须** |
| `code_challenge_method` | `plain` 或 `S256` | 推荐 `S256`（`plain` 仅在技术上无法支持 SHA256 时允许） |

!!! tip "影响"
    所有授权请求必须携带 `code_challenge` 参数，所有 Token 请求必须携带 `code_verifier` 参数。不提供 PKCE 参数的授权请求应被拒绝。

PKCE 的详细流程机制见「授权流程」中的 PKCE 章节。

### 2. 移除隐式流程（Implicit Grant）

OAuth 2.1 `完全移除`了隐式流程（`response_type=token`）。

`移除原因：`

- Access Token 直接暴露在浏览器 URL 的 `#fragment` 中，易被浏览器历史记录、Referrer 头、页面内 JavaScript 读取
- 无法使用发送者约束（Sender-Constrained Token），令牌一旦泄露即可被任意方使用
- 无法颁发 Refresh Token，用户体验差
- 授权码流程 + PKCE 已能完全覆盖隐式流程的所有场景，且安全性显著提升

移除原因：隐式流程将 Access Token 放在 URL 片段中返回（`response_type=token`），这些 Token 容易通过浏览器历史记录、Referer 头等方式泄露，且无法进行发送者约束（DPoP/mTLS 绑定）。授权码流程 + PKCE 已经完全替代了隐式流程的使用场景。

!!! warning "迁移指引"
    仍在使用隐式流程的 SPA 应用应迁移至`授权码流程 + PKCE`。详见「授权流程」中的授权码流程。

### 3. 移除资源所有者密码凭证（ROPC）

OAuth 2.1 `完全移除`了密码授权类型（`grant_type=password`）。

`移除原因：`

- 用户密码直接暴露给客户端应用，违背 OAuth 的核心设计原则
- 无法支持多因素认证（MFA）、单点登录（SSO）等现代认证方式
- 无法在授权服务器侧集中管理会话和撤销
- 客户端可以记录和转发用户密码

移除原因：ROPC 要求客户端在内存中明文持有用户密码，无法使用 MFA、SSO 等高级认证方式，且密码一旦泄露会影响用户在授权服务器上的所有账户。应使用授权码流程替代。

!!! note "遗留系统迁移"
    对于因历史原因仍在使用 ROPC 的系统，建议逐步迁移到授权码流程。若应用本身就是资源所有者（即第一方应用），可考虑直接使用授权服务器的登录页面。

### 4. redirect_uri 必须精确字符串匹配

`redirect_uri` 的匹配规则，在 OAuth 2.0 中一直是个模糊地带。OAuth 2.1 彻底消除了这种模糊——必须精确匹配，没有任何商量余地。

OAuth 2.0 规范中 `redirect_uri` 的匹配规则较为模糊，部分实现允许通配符、前缀匹配或端口忽略。OAuth 2.1 明确要求：

- 授权服务器`必须使用精确的字符串匹配`来比较 `redirect_uri`
- 不允许通配符（`*`）
- 不允许前缀匹配或模式匹配
- 不允许忽略端口号

```
# 注册的 redirect_uri
https://app.example.com/callback

# ✅ 匹配
https://app.example.com/callback

# ❌ 不匹配（路径不同）
https://app.example.com/callback/extra

# ❌ 不匹配（缺少端口不等于默认端口）
https://app.example.com:443/callback
```

`安全价值：` 模糊匹配可能被攻击者利用，通过构造合法前缀的恶意 URL 窃取授权码。精确匹配从根本上杜绝了这类攻击。

### 5. 禁止在 URI 查询参数中传递 Bearer Token

OAuth 2.0（RFC 6750）允许通过三种方式传递 Bearer Token：

1. `Authorization` 请求头（推荐）
2. 表单编码的请求体
3. URI 查询参数（`?access_token=...`）

OAuth 2.1 `禁止第三种方式`，因为：

- 查询参数会出现在浏览器历史记录中
- 查询参数可能被服务器访问日志记录
- 查询参数可能通过 `Referrer` 头泄露给第三方网站

``` http
# ✅ OAuth 2.1 推荐方式
GET /api/resource HTTP/1.1
Authorization: Bearer eyJhbGciOiJSUzI1NiIs...

# ❌ OAuth 2.1 禁止
GET /api/resource?access_token=eyJhbGciOiJSUzI1NiIs...
```

### 6. Refresh Token 必须受限于发送者或一次性使用

OAuth 2.1 要求公开客户端的 Refresh Token 必须满足以下条件之一：

- `发送者约束（Sender-Constrained）`：通过 DPoP 或 mTLS 将 Refresh Token 绑定到客户端密钥，令牌被盗后攻击者无法使用
- `一次性使用（One-Time Use）`：即 Refresh Token Rotation——每次使用后颁发新的 Refresh Token，旧的立即失效

| 客户端类型 | OAuth 2.0 | OAuth 2.1 |
|-----------|----------|----------|
| 机密客户端 | 凭客户端凭证保护 | 凭客户端凭证保护（不变） |
| 公开客户端 | 无强制要求 | **必须**发送者约束或一次性使用 |

`安全价值：` 公开客户端无法保管 `client_secret`，Refresh Token 被盗后攻击者可以直接冒充客户端使用。Rotation 机制使被盗 Token 只能使用一次，且原始客户端下次刷新时会触发异常检测。

Refresh Token Rotation 的详细机制见「安全实践」。

### 7. 令牌请求中 redirect_uri 的处理变化

在 OAuth 2.0 中，如果授权请求中包含了 `redirect_uri`，则令牌请求中也`必须`包含相同的 `redirect_uri` 进行二次验证。OAuth 2.1 对此进行了简化——当客户端只注册了一个 `redirect_uri` 时，令牌请求中不再需要重复发送该参数。

## 🔄 与 OAuth 2.0 的兼容性

如果你已经在用 OAuth 2.0，迁移到 2.1 的好消息是——基本上是做减法，不需要重写。

OAuth 2.1 与 OAuth 2.0 保持`向后兼容`：

- 遵循 OAuth 2.1 的实现`自动兼容` OAuth 2.0——因为 2.1 是 2.0 的安全子集
- OAuth 2.0 的实现`大部分兼容` 2.1——除了被移除的隐式流程和 ROPC，其余功能在 2.0 中同样支持
- 已有的 OAuth 2.0 基础设施（授权服务器、资源服务器）通常只需`配置调整`而非代码重写即可满足 2.1 要求

!!! info "OAuth 2.1 不是替代品"
    OAuth 2.1 没有引入新的授权流程或令牌类型。它的价值在于`将分散的安全要求集中为一个文档`，让开发者不再需要同时参考多个 RFC 来实现安全的 OAuth 系统。OAuth 2.0 生态中的扩展规范（如 Token Exchange RFC 8693、Device Authorization Grant RFC 8628 等）不受影响，继续独立适用。

## ✅ 迁移检查清单

下面这张清单可以帮你逐项检查现有实现是否已对齐 OAuth 2.1：

如果你的现有 OAuth 2.0 实现需要对齐 OAuth 2.1，可按以下清单逐项检查：

| 检查项 | 要求 | 状态 |
|--------|------|------|
| PKCE | 所有授权码流程（含机密客户端）启用 PKCE | :lucide-square: |
| 隐式流程 | 已禁用 `response_type=token` | :lucide-square: |
| ROPC | 已禁用 `grant_type=password` | :lucide-square: |
| redirect_uri 匹配 | 授权服务器使用精确字符串匹配 | :lucide-square: |
| Bearer Token 传递 | 禁止 URI 查询参数传递 Token | :lucide-square: |
| Refresh Token（公开客户端） | 启用 Rotation 或发送者约束 | :lucide-square: |
| TLS | 所有端点使用 HTTPS | :lucide-square: |

## 📊 OAuth 2.1 规范状态

!!! note "当前状态"
    截至撰写时，OAuth 2.1（`draft-ietf-oauth-v2-1`）仍为 IETF 草案阶段，但其包含的所有安全要求已在 RFC 9700（OAuth 2.0 Security Best Current Practice）中被正式采纳为最佳实践。主流授权服务器（如 Spring Authorization Server、Keycloak、Auth0）已全面支持这些要求。

---

`上一篇：` [实战：客户端与资源服务器](../spring-client-resource/index.md)
