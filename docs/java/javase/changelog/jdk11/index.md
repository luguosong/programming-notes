---
title: JDK 11 更新日志
description: JDK 11 各更新版本的变更记录
---

# JDK 11 更新日志

JDK 11 各更新版本（11.0.X）的变更记录，按发布时间倒序排列。数据来源于 [Oracle JDK 11 Release Notes](https://www.oracle.com/java/technologies/javase/11u-relnotes.html)。

> JDK 11 为 LTS 版本，Oracle 首选支持至 2026 年 9 月。

## 11.0.31（2026-04-21）

### 变更与增强

- 包含 IANA 时区数据 2026a

### 安全修复

- 修复多个安全漏洞（详见 [Oracle Critical Patch Update April 2026](https://www.oracle.com/security-alerts/cpuapr2026.html)）

### Bug 修复

- 完整修复列表详见 [JDK 11.0.31 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-31-relnotes.html)

## 11.0.30（2026-01-20）

### 变更与增强

- 包含 IANA 时区数据 2025b
- RMI 默认启用 TLS 端点识别（JDK-8341496）
- Windows 安装器改为版本特定目录并使用 junction（JDK-8310932）
- `jcmd` 命令移至 headless JDK RPM（JDK-8359443）

### 安全修复

- 禁用 TLS 1.2 和 DTLS 1.2 握手中的 SHA-1 签名（JDK-8340321）
- 禁用 TLS_RSA 密码套件（JDK-8245545）
- 修复多个安全漏洞（详见 [Oracle Critical Patch Update January 2026](https://www.oracle.com/security-alerts/cpujan2026.html)）

### 其他变更

- 基于 Debian 的 Linux 默认时区检测改为使用 `/etc/localtime`（JDK-8345213）
- 新增 `com.sun.security.allowedAIALocations` 属性过滤 AIA 扩展 URI（JDK-8368032）

### Bug 修复

- 完整修复列表详见 [JDK 11.0.30 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-30-relnotes.html)

---

## 11.0.29（2025-10-21）

完整发布说明详见 [JDK 11.0.29 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-29-relnotes.html)。

---

## 11.0.28（2025-07-15）

完整发布说明详见 [JDK 11.0.28 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-28-relnotes.html)。

---

## 11.0.27（2025-04-15）

完整发布说明详见 [JDK 11.0.27 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-27-relnotes.html)。

---

## 11.0.26（2025-01-21）

完整发布说明详见 [JDK 11.0.26 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-26-relnotes.html)。

---

## 11.0.25（2024-10-15）

完整发布说明详见 [JDK 11.0.25 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-25-relnotes.html)。

---

## 11.0.24（2024-07-16）

完整发布说明详见 [JDK 11.0.24 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-24-relnotes.html)。

---

## 11.0.23（2024-04-16）

完整发布说明详见 [JDK 11.0.23 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-23-relnotes.html)。

---

## 11.0.22（2024-01-16）

完整发布说明详见 [JDK 11.0.22 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-22-relnotes.html)。

---

## 11.0.21（2023-10-17）

完整发布说明详见 [JDK 11.0.21 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-21-relnotes.html)。

---

## 11.0.20（2023-07-18）

完整发布说明详见 [JDK 11.0.20 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-20-relnotes.html)。

---

## 11.0.19（2023-04-18）

完整发布说明详见 [JDK 11.0.19 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-19-relnotes.html)。

---

## 11.0.18（2023-01-17）

完整发布说明详见 [JDK 11.0.18 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-18-relnotes.html)。

---

## 11.0.17（2022-10-18）

完整发布说明详见 [JDK 11.0.17 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-17-relnotes.html)。

---

## 11.0.16.1（2022-08-18）

完整发布说明详见 [JDK 11.0.16.1 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-16-1-relnotes.html)。

---

## 11.0.16（2022-07-19）

完整发布说明详见 [JDK 11.0.16 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-16-relnotes.html)。

---

## 11.0.15.1（2022-04-19）

完整发布说明详见 [JDK 11.0.15.1 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-15-1-relnotes.html)。

---

## 11.0.15（2022-04-19）

完整发布说明详见 [JDK 11.0.15 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-15-relnotes.html)。

---

## 11.0.14（2022-01-18）

完整发布说明详见 [JDK 11.0.14 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-14-relnotes.html)。

---

## 11.0.13（2021-10-19）

完整发布说明详见 [JDK 11.0.13 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-13-relnotes.html)。

---

## 11.0.12（2021-07-20）

完整发布说明详见 [JDK 11.0.12 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-12-relnotes.html)。

---

## 11.0.11（2021-04-20）

完整发布说明详见 [JDK 11.0.11 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-11-relnotes.html)。

---

## 11.0.10（2021-01-19）

完整发布说明详见 [JDK 11.0.10 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-10-relnotes.html)。

---

## 11.0.9（2020-10-20）

完整发布说明详见 [JDK 11.0.9 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-9-relnotes.html)。

---

## 11.0.8（2020-07-14）

完整发布说明详见 [JDK 11.0.8 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-8-relnotes.html)。

---

## 11.0.7（2020-04-14）

完整发布说明详见 [JDK 11.0.7 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-7-relnotes.html)。

---

## 11.0.6（2020-01-14）

完整发布说明详见 [JDK 11.0.6 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-6-relnotes.html)。

---

## 11.0.5（2019-10-15）

完整发布说明详见 [JDK 11.0.5 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-5-relnotes.html)。

---

## 11.0.4（2019-07-16）

完整发布说明详见 [JDK 11.0.4 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-4-relnotes.html)。

---

## 11.0.3（2019-04-16）

完整发布说明详见 [JDK 11.0.3 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-3-relnotes.html)。

---

## 11.0.2（2019-01-15）

完整发布说明详见 [JDK 11.0.2 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-2-relnotes.html)。

---

## 11.0.1（2018-10-16）

完整发布说明详见 [JDK 11.0.1 Release Notes](https://www.oracle.com/java/technologies/javase/11-0-1-relnotes.html)。
