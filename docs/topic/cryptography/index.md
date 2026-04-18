---
title: 密码学
icon: material/lock
---

# 密码学

密码学是信息安全的基石。无论是 HTTPS 加密传输、数字签名防篡改，还是密码安全存储，背后都依赖密码学算法的正确使用。

**打个比方**：密码学就像一个精密的保险箱系统——不同的锁（算法）保护不同的资产（数据），而管理这些锁的方式（协议和标准）决定了系统的整体安全性。

## 为什么学习密码学？

- 不理解密码学，就无法正确使用加密库（错误使用 AES-CBC 导致 Padding Oracle Attack 等案例层出不穷）
- 了解安全位数、密钥管理、证书体系等基础概念是安全开发的前提
- 后量子密码时代的到来意味着现有算法可能在未来十年内需要迁移
- 真实世界的密码学漏洞 90% 不在算法本身，而在**协议组合**与**误用**上——这正是本笔记着重补充的内容

## 笔记融合视角说明

本笔记融合了两本书的视角：

- **《Java Cryptography: Tools and Techniques》**（David Hook & Jon Eaves, 2025）— 提供 Java + Bouncy Castle 的实现细节，回答「**怎么写**」
- **《Real-World Cryptography》**（David Wong, Manning 2021）— 提供协议设计动机与真实世界案例，回答「**为什么这么设计**」「**会踩什么坑**」

每个模块在「Java 实现」主线之外，会补充「为什么这么设计」「真实世界案例」「现代推荐」等小节。如需通读 Wong 书的逐章直译，参见 [Real World Cryptography 翻译位](../../document-translation/real-world-cryptography/index.md)。

## 学习路径

### 基础篇：原语（Primitives）

密码学的「原料」——单独使用很少够安全，但所有协议都建立在它们之上：

1. **Java 密码学架构** — JCA Provider 机制、Bouncy Castle 安装、熵与安全位数
2. **对称加密** — AES、分组密码工作模式（ECB/CBC/CTR）、认证加密（GCM/EAX）、ChaCha20 流密码
3. **哈希与完整性** — 消息摘要（SHA）、MAC/HMAC、KDF/XOF、Merkle 树
4. **随机数与密钥派生** — PRNG、`/dev/urandom`、HKDF、阈值密码学（Shamir 秘密分享）
5. **基于密码的密钥生成** — PBKDF2、scrypt、Argon2

### 协议篇：非对称与公钥基础设施

掌握公钥密码与信任体系：

6. **非对称加密与混合加密** — RSA-OAEP、ECIES、KEM/DEM 范式
7. **密钥交换** — Diffie-Hellman、ECDH（X25519/X448）、密钥确认
8. **数字签名** — RSA-PSS、ECDSA、EdDSA、SM2、签名 substitution / malleability 攻击
9. **证书与 PKI** — X.509 证书、CRL、OCSP、证书路径验证、CA 信任链失败案例
10. **密钥存储** — KeyStore 类型（JKS/PKCS12/BCFKS）、keytool、硬件存储引子

### 应用篇：真实世界协议

把原语组合成端到端可用的安全系统：

11. **TLS** — 传输层安全、TLS 1.3 握手、JSSE、双向认证、DTLS、Noise 协议框架
12. **端到端加密** — Signal 协议、X3DH、Double Ratchet、PGP 为何在邮件场景失败
13. **用户认证** — SSO、OPAQUE（aPAKE）、TOTP、WebAuthn / FIDO2、CPace、SAS
14. **CMS 与 S/MIME** — 加密消息语法、电子邮件加密、时间戳协议
15. **OpenPGP** — PGP 协议、密钥环、Web of Trust 局限
16. **加密货币与 BFT 共识** — Bitcoin 工作原理、UTXO、Merkle 树、PoW、DiemBFT
17. **硬件密码学** — HSM、TPM、Secure Element、TEE、白盒密码、侧信道与常时间编程

### 前沿篇：下一代密码学

应对未来十年的挑战：

18. **后量子密码** — Grover/Shor 算法影响、ML-KEM（Kyber）、ML-DSA（Dilithium）、哈希签名、混合加密
19. **下一代密码学** — MPC（PSI）、FHE（基于 LWE 的 bootstrapping）、通用 ZKP（zk-SNARKs）

### 附录

- **ASN.1 基础** — 编码规则参考（学习密码学协议的前置知识）
- **密码学失败案例集** — Wong 书各章真实漏洞复盘的索引页（反面教材）

## 阅读建议

- **快速上手**：基础篇 → TLS → 用户认证 即可覆盖 90% 的 Web 应用场景
- **协议设计**：基础篇 → 协议篇全部 → 端到端加密 → 硬件密码学
- **学术 / 前沿**：基础篇 → 后量子密码 → 下一代密码学

