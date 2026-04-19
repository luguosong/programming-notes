---
title: 密钥存储
---

# 密钥存储

**本文你会学到**：

- 密钥生成后为什么不能随意存放在磁盘或数据库中
- `KeyStore` API 如何像"保险柜"一样安全地管理密钥和证书
- JKS、PKCS#12、BCFKS 三种密钥库格式的区别和适用场景
- `keytool` 命令行工具的常用操作
- 如何为不同安全需求选择合适的密钥库格式
- JCEKS 与 JKS 的差异，以及何时仍会遇到它
- 如何使用操作系统原生密钥存储（TPM / OS Keychain）隔离密钥
- 密钥轮换策略：最小化长期密钥暴露风险
- Shamir's Secret Sharing：门限方案如何分散密钥保管风险

## 🤔 为什么需要密钥存储？

前面的章节中，你学会了生成密钥对、创建数字签名、加解密数据。但你有没有想过一个问题——**密钥用完之后存哪？**

最直接的想法是：把私钥序列化成文件，存到磁盘上。但这样做有严重的隐患：

- **私钥明文暴露**：任何能访问文件系统的人都能读取私钥，签名和加密形同虚设
- **缺少完整性保护**：文件被篡改后，你可能拿到一个被替换的公钥/私钥
- **没有统一的条目管理**：密钥、证书、信任锚散落在不同文件中，难以维护

你需要一个**既能加密保护密钥、又能验证完整性的安全容器**。

💡 把 `KeyStore` 想象成一个**保险柜**——你把贵重物品（密钥、证书）放进去，用密码锁住柜门。打开柜门需要密码，而柜子本身还有防篡改机制，确保里面的东西没被动过。

Java 提供的 `java.security.KeyStore` 类就是这样的"保险柜"。

### 密钥存储面临的威胁模型

理解 KeyStore 的设计，需要先明确它在防御什么。密钥存储系统面临的主要威胁来自**拥有文件系统访问权限但不应拥有密钥的攻击者**：

```mermaid
graph TD
    A["密钥存储威胁模型"] --> B["磁盘层面的攻击<br/>读取密钥库文件"]
    A --> C["内存层面的攻击<br/>从进程内存提取密钥"]
    A --> D["密码层面的攻击<br/>暴力破解密钥库密码"]
    A --> E["完整性攻击<br/>篡改密钥库内容"]

    F["KeyStore 的防御"] --> G["PBKDF2/Scrypt 增加破解成本<br/>（PKCS#12 / BCFKS）"]
    F --> H["HMAC 完整性校验<br/>（PKCS#12 / BCFKS）"]
    F --> I["密码保护<br/>即使文件泄露也需密码"]

    classDef threat fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    classDef defense fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px

    class A,B,C,D,E threat
    class F,G,H,I defense
```

- **磁盘泄露**：密钥库文件被复制到外部（备份泄露、恶意软件窃取、员工带走）。KeyStore 用密码加密密钥内容，攻击者即使拿到文件，没有密码也无法提取密钥
- **内存提取**：应用运行时，密钥已加载到内存中。`PrivateCredential` 对象在 Java 中受保护，但特权进程（如 root）或 JVM 级别的攻击（如 Unsafe API）仍可能提取。这不是 KeyStore 能解决的问题，而是操作系统级别的安全边界
- **密码暴力破解**：攻击者尝试各种密码打开密钥库。KeyStore 通过 PBKDF2/Scrypt 增加每次尝试的计算成本，使暴力破解在实际时间内不可行
- **完整性篡改**：攻击者替换密钥库文件中的公钥或证书。PKCS#12 和 BCFKS 的存储格式包含 HMAC 完整性校验——如果文件被篡改，加载时会抛出异常。但 JKS 缺乏显式的完整性校验，更容易受到此类攻击

⚠️ KeyStore 的安全性建立在**密码强度**的基础上。如果密钥库密码是 `123456`，所有加密和完整性保护都形同虚设。密码应该足够长且随机（至少 16 个字符），或者直接使用随机生成的密钥作为密钥库密码。

### 密钥泄露的代价：真实世界案例

理解密钥存储的重要性，最直接的方式是看看现实中"没保管好密钥"导致的后果。

⚠️ **Heartbleed（2014年）**

2014年4月，安全研究员披露了 OpenSSL 的 `heartbeat` 扩展存在内存越界读取漏洞（CVE-2014-0160）。攻击者通过构造恶意心跳包，让服务器在响应中泄露最多 64KB 的内存内容——而这段内存恰好可能包含服务器的 **TLS 私钥**。

一旦私钥泄露，攻击者可以：

- 解密历史上（无前向安全）的所有 HTTPS 流量
- 伪造服务器身份，发动中间人攻击
- 盗取存储在内存中的会话 token 和用户密码

Heartbleed 影响了当时约 17% 的 HTTPS 服务器，包括大量银行、政府网站。修复后，受影响的服务器不仅需要打补丁，还必须：生成新的私钥、向 CA 申请证书吊销、申请新证书、更换所有可能泄露的 Session Token。

这正是**密钥轮换**和**证书吊销**机制存在意义的最佳例证（参见后文「密钥轮换策略：最小化长期暴露」）。

⚠️ **DigiNotar CA 私钥泄露（2011年）**

DigiNotar 是荷兰的一家受信任证书颁发机构（CA）。2011年，攻击者入侵其系统并窃取了 CA 的**根私钥**。利用这把私钥，攻击者为 `*.google.com`、`login.yahoo.com` 等域名签发了 531 张伪造证书，并被用于针对伊朗用户的大规模中间人攻击。

根私钥是整个 CA 信任体系的根基——一旦泄露，攻击者可以为任意域名伪造合法证书。事件曝光后，主要浏览器（Chrome、Firefox、IE）相继将 DigiNotar 从信任列表中移除，该公司随即申请破产。

```mermaid
graph TD
    A["攻击者入侵 DigiNotar 服务器"] --> B["窃取 CA 根私钥"]
    B --> C["为 google.com 等域名<br/>签发 531 张伪造证书"]
    C --> D["对伊朗用户发动 MITM 攻击<br/>拦截 HTTPS 流量"]
    D --> E["浏览器吊销 DigiNotar 信任<br/>公司申请破产"]

    classDef attack fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:2px
    classDef impact fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:1px
    class A,B,C attack
    class D,E impact
```

💡 **核心教训**（来自 *Real-World Cryptography* Ch.10）：密钥存储不是"把文件藏起来"——你还需要访问控制、审计日志、入侵检测，以及在密钥泄露时快速响应的能力。`HSM`（硬件安全模块）能做到**密钥物理上不可导出**，参见「硬件密码学」。

## 🗝️ KeyStore 概念

### KeyStore 能存什么？

`KeyStore` 是一个密码保护的容器，可以存储三类条目：

```mermaid
graph TD
    KS["KeyStore 密钥库"]

    KS --> PKE["PrivateKeyEntry<br/>私钥条目"]
    KS --> SKE["SecretKeyEntry<br/>对称密钥条目"]
    KS --> TCE["TrustedCertificateEntry<br/>信任证书条目"]

    PKE --> PK["私钥（Private Key）"]
    PKE --> CC["证书链（Certificate Chain）"]

    SKE --> SK["对称密钥（Secret Key）"]

    TCE --> TC["可信证书（Trusted Cert）"]

    classDef ks fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:2px
    classDef entry fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:1px
    classDef item fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px

    class KS ks
    class PKE,SKE,TCE entry
    class PK,CC,SK,TC item
```

每种条目的用途：

| 条目类型 | 存储内容 | 典型场景 |
|---------|---------|---------|
| `PrivateKeyEntry` | 私钥 + 关联的证书链 | TLS 服务端证书、代码签名 |
| `SecretKeyEntry` | 对称密钥（AES 密钥等） | 数据加密密钥的持久化 |
| `TrustedCertificateEntry` | 仅公钥证书（无私钥） | CA 根证书、信任锚 |

### KeyStore API 基本操作

`KeyStore` 的使用围绕四个核心操作展开：

``` java title="KeyStore 核心操作流程"
// 1. 获取 KeyStore 实例（指定类型）
KeyStore ks = KeyStore.getInstance("PKCS12");

// 2. 初始化空密钥库（创建新库）或加载已有库（传入 InputStream）
ks.load(null, null);  // null, null = 创建空密钥库

// 3. 存入条目
//    私钥条目：需要关联证书链
ks.setKeyEntry("alias", privateKey, keyPassword, certChain);
//    信任证书条目：只需证书
ks.setCertificateEntry("ca-root", trustedCert);

// 4. 写入文件（持久化）
try (FileOutputStream fos = new FileOutputStream("keystore.p12")) {
    ks.store(fos, "storePass".toCharArray());
}
```

几个关键点：

- **别名（alias）**：每个条目用字符串别名标识，类似保险柜里的标签
- **两层密码**：库密码（`storePass`）保护整个文件，密钥密码（`keyPass`）保护单个私钥——但并非所有格式都支持两层密码
- **`load(null, null)`**：传入两个 `null` 表示创建空密钥库，而非从文件加载

> 以下所有代码示例来自 Maven 模块 `code/topic/cryptography/key-storage/`，可直接运行。

## ☕ JKS：Java 原始格式

JKS（Java KeyStore）是 Java 最早的密钥库格式，由 SUN Provider 实现。

### 创建与加载 JKS

``` java title="创建 JKS 密钥库并存入私钥和信任证书"
// 创建 JKS 实例
KeyStore ks = KeyStore.getInstance("JKS");
ks.load(null, null); // 初始化空密钥库

// 存入私钥（需关联证书链）
X509Certificate[] certChain = new X509Certificate[]{cert};
ks.setKeyEntry("my-key", privateKey, "changeit".toCharArray(), certChain);

// 存入信任证书
ks.setCertificateEntry("trusted-root", cert);

// 写入文件
try (FileOutputStream fos = new FileOutputStream("test.jks")) {
    ks.store(fos, "changeit".toCharArray());
}

// 重新加载并验证
KeyStore loadedKs = KeyStore.getInstance("JKS");
try (FileInputStream fis = new FileInputStream("test.jks")) {
    loadedKs.load(fis, "changeit".toCharArray());
}
Key loadedKey = loadedKs.getKey("my-key", "changeit".toCharArray());
```

### JKS 的局限性

JKS 作为 Java 早期的格式，存在几个明显的不足：

1. **私钥保护薄弱**：使用基于 SHA-1 的专有流密码加密，算法强度不够
2. **不支持 SecretKey**：无法存储对称密钥（AES 等），只能存私钥和证书
3. **仅支持 RSA/DSA**：不支持 EC 等现代密钥算法
4. **完整性验证不足**：在不提供密码的情况下，仍能列出大部分条目信息

⚠️ **JDK 9 起，默认密钥库类型已从 JKS 改为 PKCS12**。JKS 仍可使用但不推荐用于新项目。

## 🔧 keytool：JDK 自带的密钥管理工具

`keytool` 是 JDK 自带的命令行工具，无需编写代码就能完成密钥库的常见操作。在实际开发中，创建 TLS 证书、管理信任库等场景下 `keytool` 非常常用。

### 生成密钥对

```bash
keytool -genkeypair \
    -alias server-key \
    -keyalg RSA \
    -keysize 2048 \
    -dname "CN=localhost, O=MyOrg, C=CN" \
    -validity 365 \
    -keystore server.p12 \
    -storepass changeit \
    -storetype PKCS12
```

`-dname` 指定证书的 X.500 主题名称（Subject DN）。如果不指定，`keytool` 会交互式地逐项询问。

### 查看密钥库内容

```bash
# 列出所有条目
keytool -list -keystore server.p12 -storepass changeit

# 查看某个条目的详细信息（-v 显示详细证书链）
keytool -list -v -alias server-key -keystore server.p12 -storepass changeit
```

### 生成证书签名请求（CSR）

当你有了密钥对和自签名证书，通常需要向 CA（证书颁发机构）申请正式证书：

```bash
keytool -certreq \
    -alias server-key \
    -file server.csr \
    -keystore server.p12 \
    -storepass changeit
```

生成的 `.csr` 文件是 Base64 编码的 PKCS#10 证书请求，包含你的公钥和身份信息，用私钥签名保护完整性。

### 导入 CA 签发的证书

收到 CA 的签发证书后，需要分两步导入：

```bash
# 第一步：导入 CA 的根证书（信任锚）
keytool -import \
    -alias ca-root \
    -keystore server.p12 \
    -file ca-trustanchor.pem \
    -storepass changeit

# 第二步：导入 CA 签发给你的证书（替换原来的自签名证书）
keytool -import \
    -alias server-key \
    -keystore server.p12 \
    -file ca-response.pem \
    -storepass changeit
```

整个证书签发流程可以用时序图表示：

```mermaid
sequenceDiagram
    participant Dev as 开发者
    participant KS as KeyStore
    participant CA as 证书颁发机构

    Dev->>KS: keytool -genkeypair<br/>生成密钥对 + 自签名证书
    Dev->>KS: keytool -certreq<br/>导出证书签名请求（CSR）
    Dev->>CA: 提交 CSR
    CA-->>Dev: 签发正式证书
    Dev->>KS: keytool -import ca-root<br/>导入 CA 根证书
    Dev->>KS: keytool -import server-key<br/>导入正式证书（替换自签名）

    classDef dev fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:2px
    classDef ks fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:1px
    classDef ca fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:2px
    class Dev dev
    class KS ks
    class CA ca
```

### 导出证书

```bash
# 导出为 PEM 格式（Base64 编码）
keytool -exportcert \
    -alias server-key \
    -keystore server.p12 \
    -file server-cert.pem \
    -storepass changeit \
    -rfc
```

`-rfc` 参数指定输出 RFC 1421 格式（即 PEM 的 `-----BEGIN CERTIFICATE-----` 格式）。

### 在 Java 中调用 keytool

你也可以在代码中通过 `ProcessBuilder` 调用 `keytool`，然后用 `KeyStore` API 验证结果：

``` java title="通过 ProcessBuilder 调用 keytool 并验证结果"
// 调用 keytool 创建密钥库
ProcessBuilder pb = new ProcessBuilder(
    "keytool", "-genkeypair",
    "-alias", "test-key",
    "-keyalg", "RSA", "-keysize", "2048",
    "-dname", "CN=Keytool Test, O=Luguosong, C=CN",
    "-validity", "365",
    "-keystore", ksFile.getAbsolutePath(),
    "-storepass", "changeit",
    "-storetype", "PKCS12"
);
pb.redirectErrorStream(true);
Process process = pb.start();
int exitCode = process.waitFor();
assertEquals(0, exitCode);

// 用 KeyStore API 加载验证
KeyStore ks = KeyStore.getInstance("PKCS12");
try (FileInputStream fis = new FileInputStream(ksFile)) {
    ks.load(fis, "changeit".toCharArray());
}
assertTrue(ks.containsAlias("test-key"));
```

## 📦 PKCS#12：行业标准格式

PKCS#12（Public-Key Cryptography Standards #12）是 IETF 标准格式（RFC 7292），文件扩展名通常为 `.p12` 或 `.pfx`。它是目前**最广泛使用的密钥库格式**——浏览器、OpenSSL、Java、Windows 等都支持。

### 创建与加载 PKCS#12

``` java title="创建并加载 PKCS#12 密钥库"
// 创建 PKCS#12 密钥库（JDK 自带支持，无需 BC Provider）
KeyStore ks = KeyStore.getInstance("PKCS12");
ks.load(null, null);

// 存入私钥 + 证书链
X509Certificate[] certChain = new X509Certificate[]{cert};
ks.setKeyEntry("server-key", privateKey, "changeit".toCharArray(), certChain);

// 写入 .p12 文件
try (FileOutputStream fos = new FileOutputStream("server.p12")) {
    ks.store(fos, "changeit".toCharArray());
}

// 加载并验证
KeyStore loadedKs = KeyStore.getInstance("PKCS12");
try (FileInputStream fis = new FileInputStream("server.p12")) {
    loadedKs.load(fis, "changeit".toCharArray());
}
Key privateKey = loadedKs.getKey("server-key", "changeit".toCharArray());
```

### PKCS#12 与 JKS 的关键区别

PKCS#12 相比 JKS 有几个重要改进：

- **跨平台兼容**：OpenSSL、Windows、macOS、浏览器都能直接读取 `.p12` 文件
- **支持多种算法**：不限于 RSA/DSA，EC 等现代算法也可存储
- **更好的密钥保护**：使用标准加密算法保护私钥，而非 JKS 的专有方案
- **信任证书存储**：同样支持 `setCertificateEntry()` 存储信任锚

``` java title="PKCS#12 中存储信任证书"
KeyStore trustStore = KeyStore.getInstance("PKCS12");
trustStore.load(null, null);

// 存储多个 CA 根证书
trustStore.setCertificateEntry("root-ca-1", ca1Cert);
trustStore.setCertificateEntry("root-ca-2", ca2Cert);

// 同时存储一个带私钥的条目
trustStore.setKeyEntry("server-key", privateKey, "changeit".toCharArray(),
    new X509Certificate[]{serverCert});

try (FileOutputStream fos = new FileOutputStream("truststore.p12")) {
    trustStore.store(fos, "changeit".toCharArray());
}
```

### 密码设计差异

PKCS#12 的一个独特设计是：**密钥密码可以为 `null`**。PKCS#12 原本为"个人信息交换"设计，理念是"一个密码保护整个文件"。虽然 API 仍允许传密钥密码，但多数实现只用库密码保护一切：

``` java
// PKCS#12 中密钥密码可以设为 null
ks.setKeyEntry("key", privateKey, null, certChain); // ✅ 合法
```

## 🏰 BCFKS：Bouncy Castle 专有格式

BCFKS（Bouncy Castle FIPS KeyStore）是 Bouncy Castle 为满足 FIPS（联邦信息处理标准）合规要求而设计的密钥库格式。如果你的项目使用 Bouncy Castle 且对安全合规有较高要求，BCFKS 是最合适的选择。

### 创建与加载 BCFKS

``` java title="创建 BCFKS 密钥库"
// 使用 BC Provider 创建 BCFKS 实例
KeyStore ks = KeyStore.getInstance("BCFKS", "BC");
ks.load(null, null);

// 存入私钥（BCFKS 支持密钥密码，与 JKS 类似）
X509Certificate[] certChain = new X509Certificate[]{cert};
ks.setKeyEntry("my-key", privateKey, "changeit".toCharArray(), certChain);
ks.setCertificateEntry("trusted-cert", cert);

// 使用 BCFKSLoadStoreParameter 写入文件
try (OutputStream out = new FileOutputStream("test.bcfks")) {
    BCFKSLoadStoreParameter storeParam =
        new BCFKSLoadStoreParameter.Builder(out, "changeit".toCharArray())
            .build();
    ks.store(storeParam);
}
```

⚠️ 注意 BCFKS 的加载方式：必须使用 `BCFKSLoadStoreParameter` 而非传统的 `InputStream + char[]` 方式。

``` java title="加载 BCFKS 密钥库"
KeyStore loadedKs = KeyStore.getInstance("BCFKS", "BC");
try (InputStream in = new FileInputStream("test.bcfks")) {
    BCFKSLoadStoreParameter loadParam =
        new BCFKSLoadStoreParameter.Builder(in, "changeit".toCharArray())
            .build();
    loadedKs.load(loadParam);
}
Key key = loadedKs.getKey("my-key", "changeit".toCharArray());
```

### 使用 Scrypt 保护

BCFKS 的一个强大特性是支持自定义密码保护算法。默认使用 PBKDF2 + HMAC-SHA256，你也可以切换为 Scrypt——一种**内存密集型**的密钥派生函数，比 PBKDF2 更抗 GPU/ASIC 暴力破解（详见「基于密码的密钥生成」）。

``` java title="使用 Scrypt 保护 BCFKS 密钥库"
KeyStore ks = KeyStore.getInstance("BCFKS", "BC");
ks.load(null, null);

ks.setKeyEntry("scrypt-key", privateKey, "changeit".toCharArray(),
    new X509Certificate[]{cert});

// 配置 Scrypt 参数
// N（CPU/内存成本）= 65536, r（块大小）= 8, p（并行因子）= 1
PBKDFConfig scryptConfig = new ScryptConfig.Builder(65536, 8, 1).build();

try (OutputStream out = new FileOutputStream("scrypt-protected.bcfks")) {
    BCFKSLoadStoreParameter storeParam =
        new BCFKSLoadStoreParameter.Builder(out, "changeit".toCharArray())
            .withStorePBKDFConfig(scryptConfig) // 使用 Scrypt 替代默认 PBKDF2
            .build();
    ks.store(storeParam);
}

// 加载时也需提供相同的 Scrypt 配置
KeyStore loadedKs = KeyStore.getInstance("BCFKS", "BC");
try (InputStream in = new FileInputStream("scrypt-protected.bcfks")) {
    BCFKSLoadStoreParameter loadParam =
        new BCFKSLoadStoreParameter.Builder(in, "changeit".toCharArray())
            .withStorePBKDFConfig(scryptConfig)
            .build();
    loadedKs.load(loadParam);
}
```

Scrypt 的核心优势在于"内存硬度"——它会占用大量内存来增加暴力破解成本。GPU 擅长并行计算但不擅长大内存操作，所以 Scrypt 对 GPU 攻击的抵抗效果明显优于 PBKDF2。

### BCFKS vs IBCFKS

BCFKS 默认行为是：密钥一旦被密码解锁，就可以在同一个 `KeyStore` 实例中被重复使用。这在单线程应用中没问题，但在多线程环境中存在风险——线程 A 解锁一个密钥后，线程 B 可以无需密码直接使用它。

IBCFKS（Immutable BCFKS）是 BCFKS 的不可变包装器，磁盘格式与 BCFKS 完全相同，但加载后行为不同：**每次访问密钥都必须提供密码**，天然保证线程隔离。适用于需要严格密钥隔离的多线程服务端场景。

## 🔄 JCEKS：弥补 JKS 缺陷的过渡格式

在 JKS 与 PKCS#12 之间，还有一个经常被忽视的格式：`JCEKS`（Java Cryptography Extension KeyStore）。它是 JKS 的改进版，由 `SunJCE` Provider 实现。

### 为什么会有 JCEKS？

JKS 有一个严重的缺陷：**不支持存储对称密钥**（`SecretKeyEntry`）。如果你想把 AES 密钥或 HMAC 密钥存入 JKS，`KeyStore` 会直接抛出 `KeyStoreException`。`JCEKS` 的出现就是为了填补这个空白：在兼容 JKS 大部分功能的基础上，新增了对 `SecretKeyEntry` 的支持，并用 `3DES`（Triple-DES）加密替换了 JKS 的专有流密码方案。

``` java title="JCEKS 存储对称密钥示例"
// 生成 AES-256 对称密钥
KeyGenerator keyGen = KeyGenerator.getInstance("AES");
keyGen.init(256);
SecretKey aesKey = keyGen.generateKey();

// 使用 JCEKS 存储（JKS 在此处会抛出 KeyStoreException）
KeyStore jceks = KeyStore.getInstance("JCEKS");
jceks.load(null, null);

KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(aesKey);
KeyStore.ProtectionParameter protection =
    new KeyStore.PasswordProtection("changeit".toCharArray());
jceks.setEntry("aes-key", entry, protection); // ✅ JCEKS 支持

try (FileOutputStream fos = new FileOutputStream("secrets.jceks")) {
    jceks.store(fos, "changeit".toCharArray());
}
```

### JCEKS 的局限性

尽管 `JCEKS` 修复了 JKS 的缺陷，但它本身的安全性仍然不理想：

- **3DES 已过时**：3DES 仅提供约 112 位有效安全强度，远低于现代 AES-256 标准
- **私有格式**：同样是 Java 专有格式，无法跨平台使用
- **JDK 9+ 不推荐**：新项目应直接使用 `PKCS#12`（支持 `SecretKeyEntry` 且默认 AES 加密）

### JKS → JCEKS → PKCS#12 的演进

| 格式 | 能存私钥？ | 能存对称密钥？ | 密钥保护算法 | 跨平台 |
|------|----------|------------|------------|--------|
| `JKS` | ✅ | ❌ | 专有流密码（SHA-1） | ❌ |
| `JCEKS` | ✅ | ✅ | 3DES-CBC | ❌ |
| `PKCS#12` | ✅ | ✅ | AES-CBC（现代实现） | ✅ |

如果你还在维护使用 `JCEKS` 的遗留系统，可以用 `keytool -importkeystore` 将其迁移到 `PKCS#12`：

```bash
keytool -importkeystore \
    -srckeystore secrets.jceks -srcstoretype JCEKS \
    -destkeystore secrets.p12  -deststoretype PKCS12 \
    -srcstorepass changeit -deststorepass changeit
```

## ⚖️ 格式对比与选择

### 三种格式对比

| 特性 | JKS | PKCS#12 | BCFKS |
|------|-----|---------|-------|
| 提供者 | SUN（JDK 内置） | SUN（JDK 内置） | Bouncy Castle |
| 文件扩展名 | `.jks` | `.p12` / `.pfx` | `.bcfks` |
| 跨平台兼容 | 仅 Java | OpenSSL / 浏览器 / 系统等 | 仅 Bouncy Castle |
| 支持密钥类型 | 私钥 + 证书 | 私钥 + 证书 + 对称密钥 | 私钥 + 证书 + 对称密钥 |
| 密钥保护算法 | SHA-1 专有流密码 | 标准加密算法 | PBKDF2 / Scrypt 可选 |
| 密钥密码支持 | 支持 | 支持（通常用 null） | 支持 |
| FIPS 合规 | 不支持 | 部分实现 | 支持（配合 BC-FJA） |
| 默认格式（JDK 9+） | 否 | 是 | 否 |

### 如何选择？

```mermaid
graph TD
    START["需要存储密钥和证书"]
    START --> Q1{"需要跨平台<br/>（OpenSSL / 浏览器 / 系统）？"}
    Q1 -->|是| PKCS12["选择 PKCS#12"]
    Q1 -->|否| Q2{"需要 FIPS 合规<br/>或自定义保护算法？"}
    Q2 -->|是| BCFKS["选择 BCFKS"]
    Q2 -->|否| Q3{"是纯 Java 项目<br/>且需要密钥密码隔离？"}
    Q3 -->|是| BCFKS
    Q3 -->|否| PKCS12

    classDef node fill:transparent,stroke:#768390,color:#adbac7,stroke-width:1px
    classDef decision fill:transparent,stroke:#f57c00,color:#adbac7,stroke-width:1px
    classDef result fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class START node
    class Q1,Q2,Q3 decision
    class PKCS12,BCFKS result
```

**推荐选择**：

- **大多数场景** → PKCS#12。它是 JDK 9+ 的默认格式，跨平台兼容性好，社区支持广泛
- **需要高安全合规** → BCFKS。支持 Scrypt 等现代保护算法，FIPS 兼容设计
- **JKS** → 仅在维护遗留系统时使用，新项目不应再选择

### 格式安全性的深层对比

表面上看，三种格式都是"用密码保护密钥"——但底层的安全机制差异很大：

| 安全维度 | JKS | PKCS#12 | BCFKS |
|---------|-----|---------|-------|
| **密钥派生** | 专有方案（未公开审计） | PKCS#12 专有 KDF（基于 SHA-1 的迭代哈希方案，RFC 7292 Appendix B；现代实现可切换 PBKDF2-HMAC-SHA256） | PBKDF2-HMAC-SHA256 或 Scrypt（可选） |
| **完整性保护** | 无显式 HMAC（依赖格式隐式校验） | 专有 KDF 派生 MAC key，使用 HMAC-SHA1 完整性校验（RFC 7292 Section 4；现代实现可切换 HMAC-SHA256） | HMAC-SHA256 完整性校验 |
| **密钥加密** | 专有流密码（SHA-1 基础） | 3DES-CBC（RFC 7292）或 AES-CBC（现代实现通过 PBES2 支持） | AES-256-CBC（默认） |
| **抗暴力破解** | 弱（迭代次数不可配置） | 中（专有 KDF 迭代次数可配置；切换至 PBKDF2 后更强） | 强（支持 Scrypt 内存硬度） |

⚠️ **PKCS#12 默认使用 3DES-CBC 加密和基于 SHA-1 哈希链的专有 KDF**——这些是 1990 年代末的设计选择，在当时足够安全，但以现代标准来看并不理想。如果你的密钥库需要长期保护高价值密钥，建议使用 BCFKS（PBKDF2-HMAC-SHA256 + AES-256-CBC 或 Scrypt）。

#### 完整性校验为什么重要？

密钥库的完整性校验防止了一种隐蔽的攻击：**替换公钥攻击**。

假设攻击者拿到了你的密钥库文件，虽然无法解密私钥（没有密码），但如果密钥库格式**缺乏有效的完整性校验**（如 JKS）或校验机制被实现错误绕过，他可以：
1. 保留你原来的私钥（加密状态不动）
2. 替换公钥证书为自己的公钥
3. 把篡改后的密钥库还回去

如果你后来用这个密钥库解密了"正确发送给你的消息"，攻击者替换后的公钥无法解密——你会以为"消息已损坏"。但如果攻击者用他自己的密钥给你发了"伪造消息"，替换后的公钥验证能通过（因为签名是攻击者做的）。

PKCS#12 和 BCFKS 的 HMAC 完整性校验正是为了防止这种篡改——任何对密钥库内容的修改都会在加载时被检测到。而 JKS 缺乏显式的完整性校验，更容易受到此类攻击。

> **重要区分**：密钥库格式的安全性解决的是"存储安全"问题——密钥在磁盘上如何保护。它与密钥本身的安全性（如 RSA-2048 vs RSA-3072）是不同层面的问题。

## 💻 操作系统密钥存储：TPM 与 OS Keychain

Java `KeyStore` 将密钥加密后存储在**文件**中——但文件总是可以被复制的。即使使用了强密码的 `.p12`，攻击者只要拿到文件就能离线暴力破解。

真正的硬件级保护需要让密钥**永远不离开安全边界**。这就是 `TPM`（Trusted Platform Module）和操作系统 Keychain 的设计哲学。

### TPM：让密钥物理上不可导出

`TPM` 是主板上的一颗专用安全芯片（ISO/IEC 11889 标准）。它的核心特性是：**密钥在芯片内部生成，永远不以明文形式离开芯片**。所有加密、签名、解密操作都在芯片内完成，主机只能发送"请用这个密钥加密这段数据"的指令，而无法提取密钥本身。

```mermaid
graph LR
    App["应用程序"] -->|"发送明文数据"| TPM["TPM 芯片"]
    TPM -->|"返回密文 / 签名"| App
    TPM -.->|"密钥永远不导出"| Key["密钥<br/>（芯片内部）"]

    classDef app fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:1px
    classDef tpm fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    classDef key fill:transparent,stroke:#7b1fa2,color:#adbac7,stroke-width:1px
    class App app
    class TPM tpm
    class Key key
```

`HSM`（硬件安全模块）是 `TPM` 的企业级版本，提供更高的吞吐量和 FIPS 认证，常用于 CA、支付系统等高安全场景。详细内容参见「硬件密码学」。

### Windows DPAPI：绑定用户身份的加密

Windows 提供 `DPAPI`（Data Protection API）——一种将密钥加密**绑定到当前用户 / 机器身份**的系统服务。即使 `.p12` 文件被复制到另一台机器，没有对应的 Windows 账户就无法解密。

Java 通过 `Windows-MY`（个人证书库）和 `Windows-ROOT`（信任根）访问 Windows 的系统密钥库：

``` java title="通过 Java 访问 Windows 系统证书库"
// Windows-MY：当前用户的个人证书（私钥由 DPAPI 保护）
KeyStore windowsKs = KeyStore.getInstance("Windows-MY");
windowsKs.load(null, null); // 系统密钥库无需额外密码

// 枚举证书库中的所有别名
Enumeration<String> aliases = windowsKs.aliases();
while (aliases.hasMoreElements()) {
    String alias = aliases.nextElement();
    X509Certificate cert = (X509Certificate) windowsKs.getCertificate(alias);
    System.out.println("别名：" + alias + "，主题：" +
        cert.getSubjectX500Principal().getName());
}
```

### macOS Keychain：用户钥匙串

macOS 的 Keychain 是系统级密钥管理服务，应用程序通过 Keychain Services API 请求密钥，系统弹出授权对话框确认访问。Java 通过 `KeychainStore` Provider 访问：

``` java title="通过 Java 访问 macOS Keychain"
// 仅在 macOS 上有效
KeyStore keychainKs = KeyStore.getInstance("KeychainStore");
keychainKs.load(null, null);
```

### Android Keystore：硬件绑定的移动端方案

Android Keystore 是 Android 上最安全的密钥存储方案，支持将密钥绑定到 `TEE`（Trusted Execution Environment）或 `StrongBox`（独立安全芯片）：

- 密钥可配置为**仅在设备解锁时可用**（基于屏幕解锁状态）
- 密钥可配置为**需要用户认证**（指纹 / PIN）
- 支持硬件绑定（密钥无法备份或迁移）

Android Keystore 通过 Java `KeyStore` 的 `AndroidKeyStore` Provider 访问（仅限 Android 平台）。

### 选择合适的方案

| 方案 | 平台 | 密钥不可导出 | 用户认证 | Java 支持 |
|------|------|------------|--------|---------|
| `PKCS#12` 文件 | 全平台 | ❌ | ❌（可自实现） | ✅ 原生 |
| `Windows-MY` (DPAPI) | Windows | 部分 | 绑定用户账户 | ✅ |
| `KeychainStore` | macOS | 部分 | 系统弹窗授权 | ✅ |
| `AndroidKeyStore` | Android | ✅（硬件绑定） | 生物认证 | ✅ |
| `TPM` / `HSM` | 硬件 | ✅（物理隔离） | 物理访问控制 | 通过 PKCS#11 |

## 🔃 密钥轮换策略：最小化长期暴露

> "通过给密钥关联有效期并定期替换，你可以从一次潜在的泄露中'愈合'。轮换频率越高，攻击者能利用已知密钥的时间窗口就越短。"
>
> —— David Wong, *Real-World Cryptography* §8.7

### 为什么需要密钥轮换？

假设你用同一把 RSA 私钥签署了 10 年的合同文件。第 10 年，这把私钥因为某次漏洞泄露——攻击者现在可以对**过去 10 年**的所有签名操作进行伪造或否认。

**密钥轮换**（Key Rotation）的核心思想是：**限制单把密钥的暴露时间窗口**。

```mermaid
graph LR
    K1["密钥 v1<br/>2022.01~06"] --> K2["密钥 v2<br/>2022.07~12"]
    K2 --> K3["密钥 v3<br/>2023.01~06"]
    K3 --> K4["密钥 v4<br/>当前有效"]
    E1["v1 泄露 🔓"] -.->|"仅影响 v1 时期的数据"| K1

    classDef key fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:1px
    classDef current fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    classDef breach fill:transparent,stroke:#d32f2f,color:#adbac7,stroke-width:1px
    class K1,K2,K3 key
    class K4 current
    class E1 breach
```

### 前向安全性：轮换的极致形态

**前向安全性**（Forward Secrecy）要求：即使**当前**密钥泄露，也无法解密**过去**的消息。Signal 协议通过每条消息独立生成临时密钥对，实现了消息级别的前向安全（参见「随机性与密钥派生」中的 HKDF 和「证书与 PKI」中的 TLS 握手）。

### Java 密钥轮换实践

密钥轮换的一个常见实现模式是：在 `KeyStore` 中用**带版本号的别名**管理多个密钥，始终用最新版本加密，但保留旧版本用于解密历史数据。

``` java title="带版本控制的密钥轮换示例"
public class KeyRotationExample {
    private static final String KEY_ALIAS_PREFIX = "data-key-v";

    // 生成新版本密钥并存入 KeyStore
    public static int rotateKey(KeyStore ks, char[] storePass) throws Exception {
        int currentVersion = getCurrentVersion(ks);
        int newVersion = currentVersion + 1;

        // 生成新的 AES-256 密钥
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        SecretKey newKey = keyGen.generateKey();

        // 以版本化别名存入 KeyStore
        ks.setEntry(KEY_ALIAS_PREFIX + newVersion,
            new KeyStore.SecretKeyEntry(newKey),
            new KeyStore.PasswordProtection(storePass));

        return newVersion;
    }

    // 加密始终用最新版本密钥
    public static int getCurrentVersion(KeyStore ks) throws Exception {
        int max = 0;
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (alias.startsWith(KEY_ALIAS_PREFIX)) {
                int v = Integer.parseInt(alias.substring(KEY_ALIAS_PREFIX.length()));
                max = Math.max(max, v);
            }
        }
        return max;
    }
}
```

**实践建议**：

- 🎯 对称加密密钥：每 90 天轮换一次（或每加密 `2^32` 个块后强制轮换）
- 🎯 签名私钥：每 1-2 年轮换，公钥证书设置对应有效期
- 🎯 建立自动化轮换流程——人工操作在高压时刻往往会被遗忘
- 🎯 保留 N-2 版本的历史密钥用于解密老数据，定期清理过期密钥

### 密钥吊销：比轮换更紧急的操作

**密钥吊销**（Key Revocation）是当你发现密钥泄露时立即使之失效的机制。对于公钥证书，可以通过 `CRL`（证书吊销列表）或 `OCSP`（在线证书状态协议）发布吊销信息——参见「证书与 PKI」。

## 🤫 秘密分享：Shamir's Secret Sharing

> "密钥管理领域最大的挑战是：如果密钥存储在单一地点，这个地点就成为单点故障。"
>
> —— David Wong, *Real-World Cryptography* §8.8

### 当单点保管变成单点故障

假设你是一家公司的 CTO，公司所有的加密密钥都存在你的 `HSM` 里，而只有你知道解锁口令。你突然失联，公司就无法解密任何数据。

反过来，如果你把口令分别告诉了 5 个高管，那么任意一个高管被社会工程学攻击，整个系统就沦陷了。

你需要一个方案：**让密钥由多人共同掌管，但单个人又无法单独恢复密钥**。这就是 **Shamir 秘密分享**（Shamir's Secret Sharing，SSS）。

### 门限方案的数学原理

SSS 基于**多项式插值**的数学性质：

- 两点确定一条直线（1次多项式）
- 三点确定一条抛物线（2次多项式）
- n+1 个点确定一个 n 次多项式

将秘密（如 AES 密钥）编码为多项式的**常数项**，然后在多项式上取 n 个点分别分发给 n 个参与者。只需 m 个参与者提供各自的点，就能通过拉格朗日插值恢复多项式，进而得到秘密。

```mermaid
graph TD
    Secret["原始密钥（编码为多项式常数项）"]
    Secret --> S1["份额 1 → 参与者 A"]
    Secret --> S2["份额 2 → 参与者 B"]
    Secret --> S3["份额 3 → 参与者 C"]
    Secret --> S4["份额 4 → 参与者 D"]
    Secret --> S5["份额 5 → 参与者 E"]
    S2 & S3 & S5 --> R["任意 3 个份额即可恢复<br/>（3-of-5 门限方案）"]
    R --> Recovered["恢复的密钥"]

    classDef secret fill:transparent,stroke:#7b1fa2,color:#adbac7,stroke-width:2px
    classDef share fill:transparent,stroke:#0288d1,color:#adbac7,stroke-width:1px
    classDef recover fill:transparent,stroke:#388e3c,color:#adbac7,stroke-width:2px
    class Secret secret
    class S1,S2,S3,S4,S5 share
    class R,Recovered recover
```

💡 **关键性质**：

- `m-of-n` 门限：n 个份额中，至少需要 m 个才能恢复秘密
- 持有不足 m 个份额的攻击者**无法获得任何关于秘密的信息**（信息论安全）
- 单个份额大小与原始秘密相同

### Java 实现示例

Bouncy Castle 库提供了 SSS 的实现。以下示例演示 3-of-5 门限方案的分割与恢复逻辑：

``` java title="Shamir 秘密分享：概念性实现示例（需要 Bouncy Castle）"
import org.bouncycastle.crypto.generators.ShamirSecretSharingGenerator;
import org.bouncycastle.crypto.params.SecretShareParameters;
import java.security.SecureRandom;

// 原始秘密（如 AES-256 密钥）
SecureRandom rng = new SecureRandom();
byte[] secret = new byte[32];
rng.nextBytes(secret);

// 分割：5 个份额，门限为 3（参考 BC 文档获取精确 API）
// 实际使用请查阅 org.bouncycastle.crypto 包下的 Shamir 相关类
// 分割后各参与者各自安全保管自己的份额

// 恢复时：任意 3 个参与者提供份额，通过拉格朗日插值重建多项式常数项
// byte[] recovered = ShamirUtil.combine(share1, share3, share5);
// assertArrayEquals(secret, recovered); // ✅ 恢复成功
```

⚠️ **SSS 的局限性（来自 RWC §8.8）**：每次执行加密操作，持有各份额的参与者必须**将份额汇聚到同一地点**来重建密钥，这个汇聚时刻本身成了新的单点故障窗口。

更先进的方案——**分布式密钥生成**（DKG）和**阈值签名**（Threshold Signatures）——可以让参与者**在不重建私钥的情况下协作完成签名或解密**，私钥在全程中从未以明文形式完整存在。NIST 正在推进相关标准化工作。

**典型应用场景**：

- 🎯 `HSM` 根密钥的初始化（n 个安全管理员各持一份，防止任何单人作恶）
- 🎯 灾难恢复冷备密钥的安全保管
- 🎯 多方联合签名场景（配合 DKG 实现阈值签名）

## ✅ 小结

密钥存储看似只是"把密钥写到文件里"，但选择合适的密钥库格式对安全性影响很大。回顾本章的关键要点：

1. **KeyStore 是密码保护的容器**，支持三种条目类型：`PrivateKeyEntry`、`SecretKeyEntry`、`TrustedCertificateEntry`
2. **JKS 是历史遗留格式**，私钥保护薄弱且不支持对称密钥，不应在新项目中使用
3. **PKCS#12 是事实标准**，JDK 9+ 默认格式，跨平台兼容，适合大多数场景
4. **BCFKS 是高安全场景的选择**，支持 Scrypt 保护、FIPS 合规，但需要 Bouncy Castle
5. **keytool 是日常管理的好帮手**，创建密钥对、导出 CSR、导入证书等操作都可以命令行完成

---

## 📚 参考来源（本笔记增强部分）

- David Wong, *Real-World Cryptography* (Manning, 2021), Chapter 8 & 10
- 章节文本：会话工作区 `files/rwc-chapters/ch08.txt`、`ch10.txt`
