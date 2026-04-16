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

本文基于《Java Cryptography: Tools and Techniques》（David Hook & Jon Eaves, 2025）整理，使用 Java + Bouncy Castle 作为演示平台。原书共 15 章，笔记按学习逻辑重组为以下模块。

## 学习路径

### 基础篇

从零开始建立密码学知识体系：

1. **Java 密码学架构** — JCA Provider 机制、Bouncy Castle 安装、熵与安全位数
2. **对称加密** — AES、分组密码工作模式（ECB/CBC/CTR）、认证加密（GCM/EAX）、ChaCha20 流密码
3. **哈希与完整性** — 消息摘要（SHA）、MAC/HMAC、KDF/XOF、Merkle 树
4. **基于密码的密钥生成** — PBKDF2、SCRYPT、密钥分割

### 进阶篇

掌握非对称密码和公钥基础设施：

5. **数字签名** — DSA、EdDSA、RSA 签名、SM2 国密签名
6. **密钥交换** — RSA 密钥传输、Diffie-Hellman、ECDH、密钥确认
7. **证书与 PKI** — X.509 证书、属性证书、CRL、OCSP、证书路径验证
8. **密钥存储** — KeyStore 类型（JKS/PKCS12/BCFKS）、keytool

### 高级篇

密码学协议与前沿方向：

9. **CMS 与 S/MIME** — 加密消息语法、电子邮件加密、时间戳协议
10. **OpenPGP** — PGP 协议、密钥环、签名与加密
11. **TLS** — 传输层安全、JSSE、双向认证、DTLS
12. **后量子密码** — ML-KEM、ML-DSA、混合加密
13. **ASN.1 基础** — 编码规则参考（学习密码学协议的前置知识）
