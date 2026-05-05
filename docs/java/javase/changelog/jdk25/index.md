---
title: JDK 25 更新日志
description: JDK 25 各更新版本的变更记录
---

# JDK 25 更新日志

JDK 25 各更新版本（25.0.X）的变更记录，按发布时间倒序排列。数据来源于 [Oracle JDK 25 Release Notes](https://www.oracle.com/java/technologies/javase/25u-relnotes.html)。

> JDK 25 为当前 LTS 版本（2025 年 9 月发布），Oracle 首选支持至 2033 年 9 月。

## 25.0.3（2026-04-21）

### 变更与增强

- 包含 IANA 时区数据 2026a
- `keytool` 和 `jarsigner` 改进密码处理，输出重定向时不再回显密码
- G1 GC 新增 `UseGCOverheadLimit` 支持，GC 开销过高时抛出 OOM
- Oracle JDK `src.zip` 新增 JCE/JGSS/JSSE 安全组件源码
- 新增 `jdk.crypto.disabledAlgorithms` 安全属性，可在 JCE 层禁用指定算法

### 安全修复

- 修复多个安全漏洞（详见 [Oracle Critical Patch Update April 2026](https://www.oracle.com/security-alerts/cpuapr2026.html)）
- 不再信任中华电信（Chunghwa）2026-03-17 之后签发的 TLS 服务器证书

### Bug 修复

- 完整修复列表详见 [JDK 25.0.3 Release Notes](https://www.oracle.com/java/technologies/javase/25-0-3-relnotes.html)

---

## 25.0.2（2026-01-20）

完整发布说明详见 [JDK 25.0.2 Release Notes](https://www.oracle.com/java/technologies/javase/25-0-2-relnotes.html)。

---

## 25.0.1（2025-10-21）

完整发布说明详见 [JDK 25.0.1 Release Notes](https://www.oracle.com/java/technologies/javase/25-0-1-relnotes.html)。

---

## 25 GA（2025-09-16）

JDK 25 首个正式发布版本。完整发布说明详见 [JDK 25 Release Notes](https://www.oracle.com/java/technologies/javase/25-relnotes.html)。
