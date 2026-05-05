---
title: JDK 8 更新日志
description: JDK 8 各更新版本的变更记录
---

# JDK 8 更新日志

JDK 8 各更新版本（8uXXX）的变更记录，按发布时间倒序排列。数据来源于 [Oracle JDK 8 Release Notes](https://www.oracle.com/java/technologies/javase/8u-relnotes.html)。

> JDK 8 已过免费公共更新期限，Oracle 仅对 Java SE 订阅用户提供商业更新。

## 8u491（2026-04-21）

### 变更与增强

- 包含 IANA 时区数据 2026a
- `keytool` 密码处理增强：当输出被重定向时改进密码输入行为（不再回显密码），同样适用于 `jarsigner` 和 JAAS `TextCallbackHandler`
- Oracle JDK `src.zip` 新增 JCE/JGSS/JSSE 安全组件源码
- 新增 `jdk.crypto.disabledAlgorithms` 安全属性，可在 JCE 层禁用指定算法（初始默认不禁用任何算法）

### 安全修复

- 修复多个安全漏洞（详见 [Oracle Critical Patch Update April 2026](https://www.oracle.com/security-alerts/cpuapr2026.html)）
- 不再信任中华电信（Chunghwa）2026-03-17 之后签发的 TLS 服务器证书

### Bug 修复

- 完整修复列表详见 [JDK 8u491 Release Notes](https://www.oracle.com/java/technologies/javase/8u491-relnotes.html)

---

## 8u481（2026-01-20）

### 变更与增强

- 包含 IANA 时区数据 2025b
- RMI over TLS 默认启用端点识别
- 默认禁用 TLS 1.2/DTLS 1.2 握手中的 SHA-1 签名算法
- 默认禁用所有 TLS_RSA 密码套件（不具备前向安全性）
- Debian 系 Linux 默认时区检测改用 `/etc/localtime` 替代 `/etc/timezone`
- `jcmd -l` 和 `jps` 命令可发现 Docker 容器中运行的 JVM 进程
- Windows 安装器改用版本目录 + junction 方式
- 新增 `com.sun.security.allowedAIALocations` 安全属性，过滤证书 AIA 扩展中的 URI

### 安全修复

- 修复多个安全漏洞（详见 [Oracle Critical Patch Update January 2026](https://www.oracle.com/security-alerts/cpujan2026.html)）

### Bug 修复

- 完整修复列表详见 [JDK 8u481 Release Notes](https://www.oracle.com/java/technologies/javase/8u481-relnotes.html)

---

## 8u471（2025-10-21）

### 变更与增强

- 包含 IANA 时区数据 2025b
- 新增 TLS 协议级别签名方案禁用机制（`HandshakeSignature` / `CertificateSignature`）
- 支持通配符禁用 TLS 密码套件（如 `TLS_RSA_*`）
- `java.security.debug` 系统属性新增线程和调试时间戳选项
- 更新 XML Security 到 Santuario 3.0.5
- JNDI 新增 LDAP/RMI 协议级对象工厂过滤器
- Linux RPM 安装器改用 systemd 管理 `jexec` 服务
- 禁止 Windows 命令行参数的"最佳匹配"字符映射
- XPath 支持 `FEATURE_SECURE_PROCESSING` 外部访问限制

### 安全修复

- 修复多个安全漏洞（详见 [Oracle Critical Patch Update October 2025](https://www.oracle.com/security-alerts/cpuoct2025.html))
- 移除 4 个已停用的 AffirmTrust 根证书

### Bug 修复

- 完整修复列表详见 [JDK 8u471 Release Notes](https://www.oracle.com/java/technologies/javase/8u471-relnotes.html)

---

## 8u461（2025-07-15）

完整发布说明详见 [JDK 8u461 Release Notes](https://www.oracle.com/java/technologies/javase/8u461-relnotes.html)。

---

## 8u451（2025-04-15）

完整发布说明详见 [JDK 8u451 Release Notes](https://www.oracle.com/java/technologies/javase/8u451-relnotes.html)。

---

## 8u441（2025-01-21）

完整发布说明详见 [JDK 8u441 Release Notes](https://www.oracle.com/java/technologies/javase/8u441-relnotes.html)。

---

## 8u431（2024-10-15）

完整发布说明详见 [JDK 8u431 Release Notes](https://www.oracle.com/java/technologies/javase/8u431-relnotes.html)。

---

## 8u421（2024-07-16）

完整发布说明详见 [JDK 8u421 Release Notes](https://www.oracle.com/java/technologies/javase/8u421-relnotes.html)。

---

## 8u411（2024-04-16）

完整发布说明详见 [JDK 8u411 Release Notes](https://www.oracle.com/java/technologies/javase/8u411-relnotes.html)。

---

## 8u401（2024-01-16）

完整发布说明详见 [JDK 8u401 Release Notes](https://www.oracle.com/java/technologies/javase/8u401-relnotes.html)。

---

## 8u391（2023-10-17）

完整发布说明详见 [JDK 8u391 Release Notes](https://www.oracle.com/java/technologies/javase/8u391-relnotes.html)。

---

## 8u381（2023-07-18）

完整发布说明详见 [JDK 8u381 Release Notes](https://www.oracle.com/java/technologies/javase/8u381-relnotes.html)。

---

## 8u371（2023-04-18）

完整发布说明详见 [JDK 8u371 Release Notes](https://www.oracle.com/java/technologies/javase/8u371-relnotes.html)。

---

## 8u361（2023-01-17）

完整发布说明详见 [JDK 8u361 Release Notes](https://www.oracle.com/java/technologies/javase/8u361-relnotes.html)。

---

## 8u351（2022-10-18）

完整发布说明详见 [JDK 8u351 Release Notes](https://www.oracle.com/java/technologies/javase/8u351-relnotes.html)。

---

## 8u341（2022-07-19）

完整发布说明详见 [JDK 8u341 Release Notes](https://www.oracle.com/java/technologies/javase/8u341-relnotes.html)。

---

## 8u333（2022-05-02）

完整发布说明详见 [JDK 8u333 Release Notes](https://www.oracle.com/java/technologies/javase/8u333-relnotes.html)。

---

## 8u331（2022-04-19）

完整发布说明详见 [JDK 8u331 Release Notes](https://www.oracle.com/java/technologies/javase/8u331-relnotes.html)。

---

## 8u321（2022-01-18）

完整发布说明详见 [JDK 8u321 Release Notes](https://www.oracle.com/java/technologies/javase/8u321-relnotes.html)。

---

## 8u311（2021-10-19）

完整发布说明详见 [JDK 8u311 Release Notes](https://www.oracle.com/java/technologies/javase/8u311-relnotes.html)。

---

## 8u301（2021-07-20）

完整发布说明详见 [JDK 8u301 Release Notes](https://www.oracle.com/java/technologies/javase/8u301-relnotes.html)。

---

## 8u291（2021-04-20）

完整发布说明详见 [JDK 8u291 Release Notes](https://www.oracle.com/java/technologies/javase/8u291-relnotes.html)。

---

## 8u281（2021-01-19）

完整发布说明详见 [JDK 8u281 Release Notes](https://www.oracle.com/java/technologies/javase/8u281-relnotes.html)。

---

## 8u271（2020-10-20）

完整发布说明详见 [JDK 8u271 Release Notes](https://www.oracle.com/java/technologies/javase/8u271-relnotes.html)。

---

## 8u261（2020-07-14）

完整发布说明详见 [JDK 8u261 Release Notes](https://www.oracle.com/java/technologies/javase/8u261-relnotes.html)。

---

## 8u251（2020-04-14）

完整发布说明详见 [JDK 8u251 Release Notes](https://www.oracle.com/java/technologies/javase/8u251-relnotes.html)。

---

## 8u241（2020-01-14）

完整发布说明详见 [JDK 8u241 Release Notes](https://www.oracle.com/java/technologies/javase/8u241-relnotes.html)。

---

## 8u231（2019-10-15）

完整发布说明详见 [JDK 8u231 Release Notes](https://www.oracle.com/java/technologies/javase/8u231-relnotes.html)。

---

## 8u221（2019-07-16）

完整发布说明详见 [JDK 8u221 Release Notes](https://www.oracle.com/java/technologies/javase/8u221-relnotes.html)。

---

## 8u212（2019-04-16）

完整发布说明详见 [JDK 8u212 Release Notes](https://www.oracle.com/java/technologies/javase/8u212-relnotes.html)。

---

## 8u211（2019-01-15）

完整发布说明详见 [JDK 8u211 Release Notes](https://www.oracle.com/java/technologies/javase/8u211-relnotes.html)。

---

## 8u202（2019-01-15）

完整发布说明详见 [JDK 8u202 Release Notes](https://www.oracle.com/java/technologies/javase/8u202-relnotes.html)。

---

## 8u201（2019-01-15）

完整发布说明详见 [JDK 8u201 Release Notes](https://www.oracle.com/java/technologies/javase/8u201-relnotes.html)。

---

## 8u192（2018-10-16）

完整发布说明详见 [JDK 8u192 Release Notes](https://www.oracle.com/java/technologies/javase/8u192-relnotes.html)。

---

## 8u191（2018-10-16）

完整发布说明详见 [JDK 8u191 Release Notes](https://www.oracle.com/java/technologies/javase/8u191-relnotes.html)。

---

## 8u181（2018-07-17）

完整发布说明详见 [JDK 8u181 Release Notes](https://www.oracle.com/java/technologies/javase/8u181-relnotes.html)。

---

## 8u172（2018-04-17）

完整发布说明详见 [JDK 8u172 Release Notes](https://www.oracle.com/java/technologies/javase/8u172-relnotes.html)。

---

## 8u171（2018-04-17）

完整发布说明详见 [JDK 8u171 Release Notes](https://www.oracle.com/java/technologies/javase/8u171-relnotes.html)。

---

## 8u162（2018-01-16）

完整发布说明详见 [JDK 8u162 Release Notes](https://www.oracle.com/java/technologies/javase/8u162-relnotes.html)。

---

## 8u152（2017-10-17）

完整发布说明详见 [JDK 8u152 Release Notes](https://www.oracle.com/java/technologies/javase/8u152-relnotes.html)。

---

## 8u151（2017-10-17）

完整发布说明详见 [JDK 8u151 Release Notes](https://www.oracle.com/java/technologies/javase/8u151-relnotes.html)。

---

## 8u144（2017-07-18）

完整发布说明详见 [JDK 8u144 Release Notes](https://www.oracle.com/java/technologies/javase/8u144-relnotes.html)。

---

## 8u141（2017-07-18）

完整发布说明详见 [JDK 8u141 Release Notes](https://www.oracle.com/java/technologies/javase/8u141-relnotes.html)。

---

## 8u131（2017-04-18）

完整发布说明详见 [JDK 8u131 Release Notes](https://www.oracle.com/java/technologies/javase/8u131-relnotes.html)。

---

## 8u121（2017-01-17）

完整发布说明详见 [JDK 8u121 Release Notes](https://www.oracle.com/java/technologies/javase/8u121-relnotes.html)。

---

## 8u112（2016-10-18）

完整发布说明详见 [JDK 8u112 Release Notes](https://www.oracle.com/java/technologies/javase/8u112-relnotes.html)。

---

## 8u111（2016-10-18）

完整发布说明详见 [JDK 8u111 Release Notes](https://www.oracle.com/java/technologies/javase/8u111-relnotes.html)。

---

## 8u102（2016-07-19）

完整发布说明详见 [JDK 8u102 Release Notes](https://www.oracle.com/java/technologies/javase/8u102-relnotes.html)。

---

## 8u101（2016-07-19）

完整发布说明详见 [JDK 8u101 Release Notes](https://www.oracle.com/java/technologies/javase/8u101-relnotes.html)。

---

## 8u92（2016-04-19）

完整发布说明详见 [JDK 8u92 Release Notes](https://www.oracle.com/java/technologies/javase/8u92-relnotes.html)。

---

## 8u91（2016-04-19）

完整发布说明详见 [JDK 8u91 Release Notes](https://www.oracle.com/java/technologies/javase/8u91-relnotes.html)。

---

## 8u77（2016-03-23）

完整发布说明详见 [JDK 8u77 Release Notes](https://www.oracle.com/java/technologies/javase/8u77-relnotes.html)。

---

## 8u74（2016-02-05）

完整发布说明详见 [JDK 8u74 Release Notes](https://www.oracle.com/java/technologies/javase/8u74-relnotes.html)。

---

## 8u73（2016-02-05）

完整发布说明详见 [JDK 8u73 Release Notes](https://www.oracle.com/java/technologies/javase/8u73-relnotes.html)。

---

## 8u72（2016-01-19）

完整发布说明详见 [JDK 8u72 Release Notes](https://www.oracle.com/java/technologies/javase/8u72-relnotes.html)。

---

## 8u71（2016-01-19）

完整发布说明详见 [JDK 8u71 Release Notes](https://www.oracle.com/java/technologies/javase/8u71-relnotes.html)。

---

## 8u66（2015-10-20）

完整发布说明详见 [JDK 8u66 Release Notes](https://www.oracle.com/java/technologies/javase/8u66-relnotes.html)。

---

## 8u65（2015-10-20）

完整发布说明详见 [JDK 8u65 Release Notes](https://www.oracle.com/java/technologies/javase/8u65-relnotes.html)。

---

## 8u60（2015-08-03）

完整发布说明详见 [JDK 8u60 Release Notes](https://www.oracle.com/java/technologies/javase/8u60-relnotes.html)。

---

## 8u51（2015-07-14）

完整发布说明详见 [JDK 8u51 Release Notes](https://www.oracle.com/java/technologies/javase/8u51-relnotes.html)。

---

## 8u45（2015-04-14）

完整发布说明详见 [JDK 8u45 Release Notes](https://www.oracle.com/java/technologies/javase/8u45-relnotes.html)。

---

## 8u40（2015-03-03）

完整发布说明详见 [JDK 8u40 Release Notes](https://www.oracle.com/java/technologies/javase/8u40-relnotes.html)。

---

## 8u31（2015-01-20）

完整发布说明详见 [JDK 8u31 Release Notes](https://www.oracle.com/java/technologies/javase/8u31-relnotes.html)。

---

## 8u25（2014-10-14）

完整发布说明详见 [JDK 8u25 Release Notes](https://www.oracle.com/java/technologies/javase/8u25-relnotes.html)。

---

## 8u20（2014-08-19）

完整发布说明详见 [JDK 8u20 Release Notes](https://www.oracle.com/java/technologies/javase/8u20-relnotes.html)。

---

## 8u11（2014-07-15）

完整发布说明详见 [JDK 8u11 Release Notes](https://www.oracle.com/java/technologies/javase/8u11-relnotes.html)。

---

## 8u5（2014-04-15）

完整发布说明详见 [JDK 8u5 Release Notes](https://www.oracle.com/java/technologies/javase/8u5-relnotes.html)。

---

## JDK 8 GA（2014-03-18）

JDK 8 首个正式发布版本，引入 Lambda 表达式、Streams API、默认方法、`java.time`（JSR 310）等重大特性。完整发布说明详见 [JDK 8 Release Notes](https://www.oracle.com/java/technologies/javase/8-relnotes.html)。
