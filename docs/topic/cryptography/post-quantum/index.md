---
title: 后量子密码
---

# 后量子密码

**本文你会学到**：

- 量子计算机为什么能威胁现有的加密体系？Shor 算法和 Grover 算法分别影响哪些算法
- NIST 后量子标准化选出了哪些算法？ML-KEM 和 ML-DSA 的核心原理
- 如何使用 BouncyCastle 实现 ML-KEM 密钥封装和 ML-DSA 数字签名
- 混合加密为什么是当前过渡期的最佳实践？如何用 ECDH + ML-KEM 组合
- 迁移到后量子密码时有哪些常见陷阱需要避免

## 为什么需要后量子密码？

### 量子计算机的威胁

你平时用的 HTTPS、TLS、数字证书，底层依赖的大多是 RSA、ECDH、ECDSA 这类非对称算法。它们的安全性建立在一个核心假设上：**大数分解和离散对数问题在经典计算机上几乎不可能解决**。

但量子计算机完全改变了这个假设。

1994 年，Peter Shor 提出了 Shor 算法——一个能在多项式时间内解决大数分解和离散对数问题的量子算法。这意味着，一旦足够大的量子计算机出现，**RSA、ECDH、ECDSA 等非对称加密算法将彻底不安全**。攻击者可以用 Shor 算法从公钥推算出私钥，从而解密所有通信内容。

💡 用一个类比来理解：经典计算机是"逐个尝试锁的齿形"，量子计算机是"同时试所有齿形"——这就是量子叠加态的威力。Shor 算法让量子计算机找到了一种"聪明的同时尝试"方式，把破解难度从"宇宙年龄级"降到了"几小时级"。

### Shor 算法与 Grover 算法

量子计算对密码学的影响主要来自两个算法：

| 算法 | 影响 | 受影响算法 | 对策 |
|------|------|-----------|------|
| **Shor 算法** | 大数分解、离散对数 → 多项式时间 | RSA、ECDH、ECDSA、DSA | 替换为后量子算法 |
| **Grover 算法** | 暴力搜索 → 平方级加速 | AES、SHA-256、ChaCha20 | **密钥/哈希长度翻倍** |

关键区别在于：**Shor 算法是毁灭性的**，它直接破解非对称算法的数学基础；而 **Grover 算法只是加速了暴力搜索**，对策很简单——把 AES-128 升级到 AES-256 就能抵消 Grover 算法的影响。

### "先收集，后破解"攻击

你可能觉得量子计算机还很遥远，但有一个威胁已经迫在眉睫。攻击者今天就可以截获并存储加密通信数据，等未来量子计算机成熟后再解密。这种攻击叫做 **"先收集，后破解"（Harvest Now, Decrypt Later）**。

这意味着：即使你的数据只需要保密 5 年，但如果 5 年后量子计算机就能破解当前算法，那你现在用的加密就已经不安全了。政府机构、金融系统、医疗数据——这些需要长期保密的场景，必须**现在就开始迁移**。

⚠️ 特别是 TLS 握手中交换的对称密钥——如果握手使用的是 RSA 或 ECDH，攻击者存下握手报文，将来用 Shor 算法恢复出对称密钥，就能解密整个会话。

### 对密码学体系的影响总结

```mermaid
graph TD
    A[量子计算威胁] --> B[Shor 算法]
    A --> C[Grover 算法]
    B --> D[非对称算法失效]
    B --> E[数字签名失效]
    D --> F["RSA / ECDH / DSA → 需替换"]
    E --> G["ECDSA / RSA签名 → 需替换"]
    C --> H[对称算法安全性减半]
    H --> I["AES-128 → AES-256 即可应对"]
```

结论很明确：**对称加密和哈希只需简单升级密钥长度**，但**非对称加密和数字签名需要全新的算法**——这就是后量子密码学（Post-Quantum Cryptography, PQC）要解决的问题。

## NIST 后量子标准化

### 标准化历程

2016 年，NIST（美国国家标准与技术研究院）启动了后量子密码标准化项目，面向全球征集能抵抗量子攻击的加密算法。经过多轮筛选和评估，2024 年正式发布了首批标准：

| 标准编号 | 算法 | 类型 | 原名 | 基于的数学问题 |
|---------|------|------|------|--------------|
| **FIPS 203** | **ML-KEM** | 密钥封装 | CRYSTALS-Kyber | 模格（Module Lattice） |
| **FIPS 204** | **ML-DSA** | 数字签名 | CRYSTALS-Dilithium | 模格（Module Lattice） |
| **FIPS 205** | **SLH-DSA** | 数字签名 | SPHINCS+ | 哈希（Hash-based） |

此外，**FN-DSA**（原 Falcon）预计也将作为第四个标准发布。

### 为什么选了"格"？

你可能好奇为什么 NIST 最终选择了基于格的算法作为主力。核心原因是**综合平衡**：

- ✅ 密钥和密文大小适中（不像 Classic McEliece 的公钥动辄几百 KB）
- ✅ 计算速度足够快（不像某些哈希签名那样慢）
- ✅ 安全性经过充分研究（格问题已研究了 20 多年）
- ✅ 无状态（不像 XMSS/LMS 那样需要管理签名计数器）

💡 可以把"格问题"理解为：在一个高维网格中找到离原点最近的点。经典计算机和量子计算机都不知道高效方法——这和 RSA 依赖大数分解不同，格问题对量子计算机同样困难。

## ML-KEM：后量子密钥封装

### 什么是 KEM？

当你用 ECDH 做密钥交换时，双方各生成一个临时密钥对，交换公钥后各自计算出相同的共享密钥。这个过程需要双方**同时在线**交互。

但很多场景下，你希望像加密一样：**一方生成密文，另一方收到后解密出共享密钥**。这就是 **KEM（Key Encapsulation Mechanism，密钥封装机制）** 的核心思想：

- **封装（encapsulate）**：用接收方的公钥生成密文 + 共享密钥
- **解封装（decapsulate）**：用私钥从密文恢复共享密钥

💡 KEM 像是一个"信封"——你把共享密钥装进信封（密文），只有持有私钥的接收方才能打开信封取出密钥。和 ECDH 的区别在于：ECDH 需要双方同时参与，KEM 只需要一方操作。

### 密钥生成与封装/解封装

来看一个完整的 ML-KEM 流程。BouncyCastle 提供了两种 API：标准 JCA 和底层 API。我们用底层 API 演示，因为它能直接访问共享密钥，更清晰地展示原理。

``` java title="ML-KEM-1024 完整封装/解封装流程"
// 1. 生成密钥对
MLKEMKeyPairGenerator kpg = new MLKEMKeyPairGenerator();
kpg.init(new MLKEMKeyGenerationParameters(RANDOM, MLKEMParameters.ml_kem_1024));
AsymmetricCipherKeyPair keyPair = kpg.generateKeyPair();

MLKEMPublicKeyParameters pubKey = (MLKEMPublicKeyParameters) keyPair.getPublic();
MLKEMPrivateKeyParameters privKey = (MLKEMPrivateKeyParameters) keyPair.getPrivate();

// 2. 封装：用公钥生成密文和共享密钥
MLKEMGenerator generator = new MLKEMGenerator(RANDOM);
SecretWithEncapsulation encResult = generator.generateEncapsulated(pubKey);
byte[] ciphertext = encResult.getEncapsulation();  // 密文（发送给对方）
byte[] sharedSecretAlice = encResult.getSecret();  // Alice 的共享密钥

// 3. 解封装：用私钥从密文恢复共享密钥
MLKEMExtractor extractor = new MLKEMExtractor(privKey);
byte[] sharedSecretBob = extractor.extractSecret(ciphertext);  // Bob 的共享密钥

// 4. 验证双方共享密钥一致
assertArrayEquals(sharedSecretAlice, sharedSecretBob);
```

这个流程和 ECDH 的关键区别在于：**只有一方需要持有私钥**（接收方），发送方只需要接收方的公钥就能完成封装。这让它特别适合非交互场景（比如邮件加密）。

### 三种安全等级对比

ML-KEM 提供三种参数集，对应不同的 NIST 安全等级：

| 算法 | NIST 等级 | 公钥 | 私钥 | 密文 | 共享密钥 |
|------|---------|------|------|------|---------|
| ML-KEM-512 | 1（≈ AES-128） | 800 B | 1,584 B | 768 B | 32 B |
| ML-KEM-768 | 3（≈ AES-192） | 1,184 B | 2,400 B | 1,088 B | 32 B |
| ML-KEM-1024 | 5（≈ AES-256） | 1,568 B | 3,168 B | 1,568 B | 32 B |

对比一下你熟悉的老算法：RSA-2048 的公钥只有 256 字节，但 ML-KEM-512 的公钥就有 800 字节——**后量子算法的公钥和密文普遍更大**。这是为量子安全性付出的代价。

⚠️ **ML-KEM 的一个重要特性**：解封装时如果密文被篡改，它**不会抛出异常**，而是返回一个不同的共享密钥。这是设计上的有意选择——避免通过异常信息泄露密文是否合法。

``` java title="错误密文导致不同的共享密钥"
MLKEMExtractor extractor = new MLKEMExtractor(privKey);
byte[] fakeCiphertext = new byte[extractor.getEncapsulationLength()];
RANDOM.nextBytes(fakeCiphertext);

// 不会抛异常，但返回的共享密钥与正确密文完全不同
byte[] wrongSecret = extractor.extractSecret(fakeCiphertext);
assertNotEquals(HEX.formatHex(correctSecret), HEX.formatHex(wrongSecret));
```

## ML-DSA：后量子数字签名

### 签名与验签

ML-DSA（Module-Lattice-Based Digital Signature Algorithm）是后量子数字签名算法，原名为 CRYSTALS-Dilithium。和 RSA/ECDSA 一样，它的使用方式非常简单——生成密钥对、签名、验签。

好消息是：**BouncyCastle 1.80 之后，ML-DSA 注册在标准 BC Provider 中**，可以直接用 JCA 的 `Signature` 接口操作，用法和 ECDSA 完全一样。

``` java title="ML-DSA-65 完整签名/验签流程"
byte[] message = "Hello, Post-Quantum World!".getBytes();

// 1. 生成 ML-DSA-65 密钥对（使用标准 JCA 接口）
KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-DSA-65", "BC");
KeyPair keyPair = kpg.generateKeyPair();

// 2. 用私钥签名
Signature signer = Signature.getInstance("ML-DSA-65", "BC");
signer.initSign(keyPair.getPrivate());
signer.update(message);
byte[] signature = signer.sign();  // 约 3,309 字节

// 3. 用公钥验证签名
Signature verifier = Signature.getInstance("ML-DSA-65", "BC");
verifier.initVerify(keyPair.getPublic());
verifier.update(message);
boolean verified = verifier.verify(signature);  // true
```

如果消息被篡改，验签会失败：

``` java title="篡改消息后签名验证失败"
verifier.initVerify(keyPair.getPublic());
verifier.update("Tampered message!".getBytes());
boolean verified = verifier.verify(signature);  // false
```

🎯 **迁移友好性**：注意看上面的代码——除了算法名从 `"ECDSA"` 换成了 `"ML-DSA-65"`，其余 API 调用完全一样。这正是 JCA 抽象层的价值：算法替换对业务代码透明。

### 三种安全等级对比

ML-DSA 同样提供三种参数集：

| 算法 | NIST 等级 | 公钥 | 私钥 | 签名 |
|------|---------|------|------|------|
| ML-DSA-44 | 2（≈ SHA-256） | 1,312 B | 2,576 B | 2,420 B |
| ML-DSA-65 | 3（≈ SHA-384） | 1,952 B | 4,000 B | 3,309 B |
| ML-DSA-87 | 5（≈ SHA-512） | 2,592 B | 4,896 B | 4,627 B |

对比 ECDSA P-256：公钥 64 字节、签名 64 字节。ML-DSA-65 的签名大小是 ECDSA 的约 50 倍——**后量子签名的体积显著增大**。这对带宽受限的场景（如 IoT 设备）是需要权衡的因素。

## 混合加密——过渡期最佳实践

### 为什么需要混合？

你可能会想：既然 ML-KEM 和 ML-DSA 已经标准化了，直接替换不就行了吗？问题在于：**传统算法目前并没有被破解**，而新算法虽然理论上安全，但时间还不够长，可能存在未知漏洞。

混合加密的核心思路是：**同时使用传统算法和后量子算法，只要其中任何一个没被破解，通信就是安全的**。

💡 这就像给门装了两把锁——一把传统机械锁，一把新型电子锁。小偷要同时破解两把锁才能进来。即使将来电子锁被发现有缺陷，机械锁仍然保护着你。

具体来说，混合方案的价值在于：

- **抗量子攻击**：ML-KEM 抵抗未来的量子计算机
- **抗经典攻击**：ECDH 抵抗当前的经典计算机攻击
- **抗新算法漏洞**：即使 ML-KEM 将来被发现有缺陷，ECDH 仍然提供安全保障

### ECDH + ML-KEM 混合 KEM

混合 KEM 的实现思路很直观：

```mermaid
graph LR
    A[生成 ECDH 密钥对] --> C[生成 ML-KEM 密钥对]
    C --> D[ML-KEM 封装<br/>得到密文 + 共享密钥 A]
    A --> E[ECDH 密钥协商<br/>得到共享密钥 B]
    D --> F[KDF 合并<br/>密钥 A + 密钥 B]
    E --> F
    F --> G[最终混合密钥]
```

来看具体实现：

``` java title="ECDH + ML-KEM 混合密钥交换"
// ===== 第一步：ECDH（传统密钥交换）=====
KeyPairGenerator ecdhKpg = KeyPairGenerator.getInstance("EC");
ecdhKpg.initialize(256);  // P-256 曲线
KeyPair ecdhKeyPair = ecdhKpg.generateKeyPair();

// ECDH 密钥协商
KeyAgreement ecdhKa = KeyAgreement.getInstance("ECDH");
ecdhKa.init(ecdhKeyPair.getPrivate());
ecdhKa.doPhase(ecdhPeerPair.getPublic(), true);
byte[] ecdhSecret = ecdhKa.generateSecret();

// ===== 第二步：ML-KEM（后量子密钥封装）=====
MLKEMKeyPairGenerator mlKemKpg = new MLKEMKeyPairGenerator();
mlKemKpg.init(new MLKEMKeyGenerationParameters(RANDOM, MLKEMParameters.ml_kem_768));
AsymmetricCipherKeyPair mlKemKeyPair = mlKemKpg.generateKeyPair();

MLKEMGenerator mlKemGen = new MLKEMGenerator(RANDOM);
SecretWithEncapsulation mlKemEncResult = mlKemGen.generateEncapsulated(mlKemKeyPair.getPublic());
byte[] mlKemSecret = mlKemEncResult.getSecret();

// ===== 第三步：KDF 合并两份共享密钥 =====
MessageDigest digest = MessageDigest.getInstance("SHA-256");
digest.update(ecdhSecret);
digest.update(mlKemSecret);
byte[] combinedSecret = digest.digest();  // 最终混合密钥
```

**关键点**：第三步的 KDF（密钥派生函数）不能简单地拼接——要用哈希函数合并，确保攻击者即使知道其中一个密钥也无法推导出另一个。生产环境建议使用 HKDF（HMAC-based KDF）而非简单哈希。

### 混合方案的安全性保证

混合方案的核心安全属性可以总结为：

| 攻击场景 | 攻击者已知 | 能否得到混合密钥 |
|---------|-----------|----------------|
| 经典攻击者破解了 ECDH | ECDH 私钥 | ❌ 仍然不知道 ML-KEM 密钥 |
| 量子攻击者破解了 ML-KEM | ML-KEM 私钥 | ❌ 仍然不知道 ECDH 密钥 |
| 两个算法同时被破解 | 双方私钥 | ⚠️ 此时才不安全 |

这就是"单点突破不影响整体安全"的设计——攻击者必须**同时**破解两个不相关的数学问题，难度远超单独破解任何一个。

### 混合方案的开销

混合不是免费的午餐——它增加了额外的带宽和计算开销：

| 组合方案 | 公钥总大小 | 密文总大小 |
|---------|----------|----------|
| ECDH P-256 + ML-KEM-512 | ~912 B | ~880 B |
| ECDH P-256 + ML-KEM-768 | ~1,296 B | ~1,200 B |
| ECDH P-256 + ML-KEM-1024 | ~1,680 B | ~1,680 B |

其中 ECDH P-256 的公钥约 65 字节。相比纯 ECDH（公钥 65 字节），混合方案的公钥大了约 12-25 倍。对于大多数现代网络应用来说，这点带宽开销完全可以接受。

⚠️ **注意**：上面示例中的 KDF 合并方式是简化版。生产环境应遵循 NIST SP 800-56C Rev. 2 规范，使用 `HybridValueParameterSpec`（Z' = Z \|\| T）或 `PQCOtherInfoGenerator`（通过 OtherInfo 字段混合），这些方法有正式的安全证明。

## 迁移路线与建议

从当前体系迁移到后量子密码不是一蹴而就的事。以下是推荐的迁移路线：

### 优先级排序

1. **最高优先级——长期机密数据**：政府、军事、医疗、金融等需要 10 年以上保密性的数据，应立即开始评估混合方案
2. **高优先级——证书和签名基础设施**：代码签名、TLS 证书、固件签名等，迁移窗口较长（证书有效期通常 1-3 年），应尽早规划
3. **中等优先级——内部通信**：企业内网 TLS、API 通信等，可以等到库和框架原生支持后再迁移
4. **低优先级——短期数据**：会话 token、临时加密等，生命周期短，Grover 算法当前威胁有限

### 实施建议

- **使用混合方案过渡**：不要直接替换，而是同时使用 ECDH + ML-KEM、ECDSA + ML-DSA。这样即使新算法有缺陷，老算法仍然兜底
- **优先迁移 KEM**：密钥交换是 TLS 握手的关键环节，ML-KEM（FIPS 203）是 NIST 首个标准化的 PQC 算法，应优先采用
- **关注 TLS 1.3 的 PQC 扩展**：IETF 正在制定 TLS 后量子扩展（如 hybrid key exchange），等标准成熟后可以直接启用
- **测试先行**：在不影响生产的情况下，在测试环境验证后量子算法的性能和兼容性

### Java 开发者行动清单

- 升级 BouncyCastle 到 1.80+，即可使用 ML-KEM 和 ML-DSA 的标准 JCA 接口
- ML-DSA 通过 `Signature.getInstance("ML-DSA-65", "BC")` 即可使用，与 ECDSA 用法完全一致
- ML-KEM 建议使用底层 API（`MLKEMGenerator` / `MLKEMExtractor`）直接获取共享密钥
- 混合方案需要自行组合（目前 BouncyCastle 提供了 `HybridValueParameterSpec` 和 `PQCOtherInfoGenerator` 辅助类）

## 常见问题与陷阱

### "我们什么时候需要迁移？"

不是"量子计算机造出来了再迁移"——那时候已经太晚了。因为 **"先收集，后破解"** 攻击意味着今天的加密数据可能在明天被解密。如果你保护的数据需要保密 5 年以上，现在就该开始规划迁移。

### "后量子算法的公钥和签名太大了怎么办？"

这是事实，但目前大多数网络应用可以承受。对于带宽极度受限的场景（如嵌入式设备），可以：

- 使用 ML-KEM-512 而非 ML-KEM-1024（安全等级 1，但体积更小）
- 等待 HQC（基于编码的 KEM，正在第四轮标准化）等公钥更小的算法
- 使用 Classic McEliece（公钥极大但密文极小，适合下行传输场景）

### "ML-KEM 解封装不报错，怎么调试？"

这是一个容易踩的坑。ML-KEM 设计上对非法密文**静默返回不同的共享密钥**，而不是抛异常。这和 RSA 解密失败会抛 `BadPaddingException` 的行为完全不同。

- ✅ 正确做法：解封装后通过后续通信（如 TLS 的 Finished 消息）来验证共享密钥是否正确
- ❌ 错误做法：依赖异常来判断密文合法性

### "混合方案的 KDF 可以用简单拼接吗？"

**不可以**。简单拼接 `ecdhSecret || mlKemSecret` 作为密钥是不安全的。正确的做法是通过 KDF（如 HKDF、SHA-256 哈希）来混合两份密钥，确保：

- 攻击者不知道其中一份密钥时，无法推导出最终密钥
- 两份密钥的贡献是均匀混合的，不存在一方主导的情况

### "为什么不再用有状态签名算法（XMSS/LMS）？"

XMSS 和 LMS 是基于哈希的有状态签名算法，安全性非常好，但有一个致命缺陷：**私钥每次签名后都会改变（状态推进）**。如果签名后状态没有持久化，或者系统从旧备份恢复，就会导致密钥状态重用，从而严重削弱安全性。

NIST 推荐：如果没有 HSM（硬件安全模块），优先使用无状态算法（ML-DSA、SLH-DSA）。有状态算法只有在 HSM 保护下才考虑使用。
