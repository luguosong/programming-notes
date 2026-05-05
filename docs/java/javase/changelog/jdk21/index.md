---
title: JDK 21 更新日志
description: JDK 21 各更新版本的变更记录
---

# JDK 21 更新日志

JDK 21 各更新版本（21.0.X）的变更记录，按发布时间倒序排列。数据来源于 [Oracle JDK 21 Release Notes](https://www.oracle.com/java/technologies/javase/21u-relnotes.html)。

> JDK 21 为 LTS 版本（前一代 LTS），Oracle 首选支持至 2031 年 9 月。

## 21.0.11（2026-04-21）

### 变更与增强

- 包含 IANA 时区数据 2026a
- 多项 FreeBSD 兼容性代码改进
- 新增 `jdk.tls.server.newSessionTicketCount` 系统属性，可配置 TLSv1.3 会话恢复票据数量
- `keytool` 和 `jarsigner` 改进密码处理，输出重定向时不再回显密码
- G1 GC 新增 `UseGCOverheadLimit` 支持，GC 开销过高时抛出 OOM
- Oracle JDK `src.zip` 新增 JCE/JGSS/JSSE 安全组件源码
- 新增 `jdk.crypto.disabledAlgorithms` 安全属性，可在 JCE 层禁用指定算法

### 安全修复

- 修复多个安全漏洞（详见 [Oracle Critical Patch Update April 2026](https://www.oracle.com/security-alerts/cpuapr2026.html)）
- 不再信任中华电信（Chunghwa）2026-03-17 之后签发的 TLS 服务器证书

### Bug 修复

- 完整修复列表详见 [JDK 21.0.11 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-11-relnotes.html)

---

## 21.0.10（2026-01-20）

### 变更与增强

- 包含 IANA 时区数据 2025d
- RMI over TLS 默认启用端点识别
- 默认禁用 TLS 1.2/DTLS 1.2 握手中的 SHA-1 签名算法
- 默认禁用所有 TLS_RSA 密码套件（不具备前向安全性）
- Debian 系 Linux 默认时区检测改用 `/etc/localtime`
- Windows 安装器改用版本目录 + junction 方式

### 安全修复

- 修复多个安全漏洞（详见 [Oracle Critical Patch Update January 2026](https://www.oracle.com/security-alerts/cpujan2026.html)）

### Bug 修复

- 完整修复列表详见 [JDK 21.0.10 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-10-relnotes.html)

---

## 21.0.9（2025-10-21）

完整发布说明详见 [JDK 21.0.9 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-9-relnotes.html)。

---

## 21.0.8（2025-07-15）

完整发布说明详见 [JDK 21.0.8 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-8-relnotes.html)。

---

## 21.0.7（2025-04-15）

完整发布说明详见 [JDK 21.0.7 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-7-relnotes.html)。

---

## 21.0.6（2025-01-21）

完整发布说明详见 [JDK 21.0.6 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-6-relnotes.html)。

---

## 21.0.5（2024-10-15）

完整发布说明详见 [JDK 21.0.5 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-5-relnotes.html)。

---

## 21.0.4（2024-07-16）

完整发布说明详见 [JDK 21.0.4 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-4-relnotes.html)。

---

## 21.0.3（2024-04-16）

完整发布说明详见 [JDK 21.0.3 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-3-relnotes.html)。

---

## 21.0.2（2024-01-16）

完整发布说明详见 [JDK 21.0.2 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-2-relnotes.html)。

---

## 21.0.1（2023-10-17）

完整发布说明详见 [JDK 21.0.1 Release Notes](https://www.oracle.com/java/technologies/javase/21-0-1-relnotes.html)。

---

## 21 GA（2023-09-19）

JDK 21 首个正式发布版本。完整发布说明详见 [JDK 21 Release Notes](https://www.oracle.com/java/technologies/javase/21-relnotes.html)。
