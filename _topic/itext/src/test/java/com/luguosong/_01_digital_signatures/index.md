---
layout: note
title: 了解数字签名的概念
nav_order: 10
parent: iText 7
---

# Hello World PDF

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230328092531.png)

# 带有签名的pdf文件

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230328101924.png)


# Hash摘要

## Java默认MessageDigest

```java
/**
 * 类名：C1_01_DigestDefault
 * 描述：信息摘要默认实现
 */
public class C1_01_DigestDefault {
    /**
     * 方法名：showTest
     * 描述：展示测试
     * @param algorithm 算法
     * @throws RuntimeException 运行时异常
     */
    public void showTest(String algorithm) {
        try {
            // 获取信息摘要实例
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            // 对字符串进行摘要
            byte[] digest = messageDigest.digest("hello world".getBytes("UTF-8"));
            // 输出摘要结果
            System.out.println("摘要使用 " + algorithm + ": " + digest.length);
            System.out.println("摘要: " + new BigInteger(1, digest).toString(16));
            // 验证摘要结果
            System.out.println("摘要验证 " + Arrays.equals(digest, messageDigest.digest("hello world".getBytes())));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 方法名：testDigest
     * 描述：测试信息摘要
     */
    @Test
    public void testDigest() {
        showTest("MD5");
        showTest("SHA-1");
        showTest("SHA-224");
        showTest("SHA-256");
        showTest("SHA-384");
        showTest("SHA-512");
        showTest("RIPEMD128");
        showTest("RIPEMD160");
        showTest("RIPEMD256");
    }
}
```

输出结果：

```shell
摘要使用 MD5: 16
摘要: 5eb63bbbe01eeed093cb22bb8f5acdc3
摘要验证 true
摘要使用 SHA-1: 20
摘要: 2aae6c35c94fcfb415dbe95f408b9ce91ee846ed
摘要验证 true
摘要使用 SHA-224: 28
摘要: 2f05477fc24bb4faefd86517156dafdecec45b8ad3cf2522a563582b
摘要验证 true
摘要使用 SHA-256: 32
摘要: b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9
摘要验证 true
摘要使用 SHA-384: 48
摘要: fdbd8e75a67f29f701a4e040385e2e23986303ea10239211af907fcbb83578b3e417cb71ce646efd0819dd8c088de1bd
摘要验证 true
摘要使用 SHA-512: 64
摘要: 309ecc489c12d6eb4cc40f50c902f2b4d0ed77ee511a7c7a9bcd3ca86d4cd86f989dd35bc5ff499670da34255b45b0cfd830e81f605dcf7dc5542e93ae9cd76f
摘要验证 true
RIPEMD128 MessageDigest not available
RIPEMD160 MessageDigest not available
RIPEMD256 MessageDigest not available
```

## BC库作为密码提供者

```java
public class C1_02_DigestBC {
    /**
     * 方法名：showTest
     * 描述：展示测试
     * @param algorithm 算法
     * @throws RuntimeException 运行时异常
     */
    public void showTest(String algorithm) {
        try {
            // 获取信息摘要实例,使用BC库作为密码提供者
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm,new BouncyCastleProvider());
            // 对字符串进行摘要
            byte[] digest = messageDigest.digest("hello world".getBytes("UTF-8"));
            // 输出摘要结果
            System.out.println("摘要使用 " + algorithm + ": " + digest.length);
            System.out.println("摘要: " + new BigInteger(1, digest).toString(16));
            // 验证摘要结果
            System.out.println("摘要验证 " + Arrays.equals(digest, messageDigest.digest("hello world".getBytes())));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * 方法名：testDigest
     * 描述：测试信息摘要
     */
    @Test
    public void testDigest() {
        showTest("MD5");
        showTest("SHA-1");
        showTest("SHA-224");
        showTest("SHA-256");
        showTest("SHA-384");
        showTest("SHA-512");
        showTest("RIPEMD128");
        showTest("RIPEMD160");
        showTest("RIPEMD256");
    }
}
```

输出结果：

```shell
摘要使用 MD5: 16
摘要: 5eb63bbbe01eeed093cb22bb8f5acdc3
摘要验证 true
摘要使用 SHA-1: 20
摘要: 2aae6c35c94fcfb415dbe95f408b9ce91ee846ed
摘要验证 true
摘要使用 SHA-224: 28
摘要: 2f05477fc24bb4faefd86517156dafdecec45b8ad3cf2522a563582b
摘要验证 true
摘要使用 SHA-256: 32
摘要: b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9
摘要验证 true
摘要使用 SHA-384: 48
摘要: fdbd8e75a67f29f701a4e040385e2e23986303ea10239211af907fcbb83578b3e417cb71ce646efd0819dd8c088de1bd
摘要验证 true
摘要使用 SHA-512: 64
摘要: 309ecc489c12d6eb4cc40f50c902f2b4d0ed77ee511a7c7a9bcd3ca86d4cd86f989dd35bc5ff499670da34255b45b0cfd830e81f605dcf7dc5542e93ae9cd76f
摘要验证 true
摘要使用 RIPEMD128: 16
摘要: c52ac4d06245286b33953957be6c6f81
摘要验证 true
摘要使用 RIPEMD160: 20
摘要: 98c615784ccb5fe5936fbc0cbe9dfdb408d92f0f
摘要验证 true
摘要使用 RIPEMD256: 32
摘要: d375cf9d9ee95a3bb15f757c81e93bb0ad963edf69dc4d12264031814608e37
摘要验证 true
```

# keytool创建密钥库

```shell
keytool -genkey -alias demo -keyalg RSA -keysize 2048 -keystore ks
```

{: .warning}
> 前提，需要将jdk的bin目录加入环境变量

- `keytool`：Java 提供的密钥管理工具。
- `-genkey`：表示要生成新的密钥对。
- `-alias demo`：指定别名为 "demo"，可以根据需要更改。
- `-keyalg RSA`：使用 RSA 算法来生成密钥对。
- `-keysize 2048`：指定密钥长度为 2048 比特（bit），也可以根据需要更改。
- `-keystore ks`：指定要创建或修改的密钥库文件名为 "ks"，也可以根据需要更改。

# 公钥和私钥


{: .note-title}
> 
> 
> `Signature`类被用于提供数字签名算法的功能，用于对数字数据进行身份认证和完整性保证。
> 
> 数字签名算法可以是NIST标准的DSA，使用DSA和SHA-256算法。使用SHA-256消息摘要算法的DSA算法可以被指定为SHA256withDSA。在RSA算法中，签名算法可以被指定为例如SHA256withRSA。算法名称必须被指定，因为没有默认值。
> 
> `Signature对象`可以被用于生成和验证数字签名。使用Signature对象进行数据签名或验证数字签名包括以下三个步骤：
> 
> 1. **`初始化`**
> 初始化时，需要使用公钥（对于验证操作）或私钥（并可选使用安全随机数生成器）（对于签名操作）。使用initVerify方法进行公钥初始化，使用initSign(PrivateKey)或initSign(PrivateKey, SecureRandom)方法进行私钥初始化。
> 2. **`更新`**
> 根据初始化的类型，需要更新要进行签名或验证的字节。使用update方法进行更新。
> 3. **`签名或验证`**
> 对于所有更新过的字节进行签名或验证。使用sign方法进行签名操作，使用verify方法进行验证操作。
> 
> 需要注意的是，`Signature类`是`抽象类`并扩展自`SignatureSpi类`，这是由于历史原因。应用程序开发人员应只注意Signature类中定义的方法，超类中的所有方法是为了提供`加密服务供应商`提供数字签名算法的自定义实现而定义的。
> 
> Java平台的每个实现都必须支持以下标准的Signature算法：
> - SHA1withDSA
> - SHA1withRSA
> - SHA256withRSA

```java
/**
 * @author luguosong
 */
public class C1_03_EncryptDecrypt {

    /**
     * 公钥
     */
    private PublicKey publicKey;

    /**
     * 私钥
     */
    private Key privateKey;

    /**
     * 密码器
     */
    private Cipher cipher;

    /**
     * 签名器
     */
    private Signature signature;

    /**
     * 初始化方法
     * 从密钥库中加载公钥和私钥，初始化密码器和签名器。
     */
    @BeforeEach
    public void init() {
        try {
            // 从密钥库中加载公钥和私钥
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(C1_03_EncryptDecrypt.class.getClassLoader().getResourceAsStream("01_digital_signatures/ks"), "12345678".toCharArray());
            publicKey = ks.getCertificate("demo").getPublicKey();
            privateKey = ks.getKey("demo", "12345678".toCharArray());
            // 初始化密码器
            cipher = Cipher.getInstance("RSA");
            // 初始化签名器
            signature = Signature.getInstance("SHA256withRSA");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 加密和解密方法
     * 本方法演示了非对称加解密的过程，包括加密和解密。
     * 加密使用公钥，解密使用私钥。
     */
    @Test
    public void encryptionAndDecryption() {
        try {
            //加密
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypt = cipher.doFinal("hello world".getBytes());
            System.out.println("加密结果：" + new BigInteger(1, encrypt).toString(16));

            //解密
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypt = cipher.doFinal(encrypt);
            System.out.println("解密结果：" + new String(decrypt));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 签名和验签方法
     * 本方法演示了签名和验签的过程，包括签名和验签。
     * 签名使用私钥，验签使用公钥。
     * 签名的过程是先对数据进行哈希，然后使用私钥对哈希值进行加密，得到签名。
     * 验签的过程是先对数据进行哈希，然后使用公钥对签名进行解密，得到哈希值，然后比较两个哈希值是否相等。
     */
    @Test
    public void signingAndVerifyingSignatures() {
        try {
            String data = "hello world";

            //签名
            signature.initSign((PrivateKey) privateKey);
            signature.update(data.getBytes());
            byte[] sign = signature.sign();
            System.out.println("签名结果：" + new BigInteger(1, sign).toString(16));

            //验签
            signature.initVerify(publicKey);
            signature.update(data.getBytes());
            boolean verify = signature.verify(sign);
            System.out.println("验签结果：" + verify);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
```


  

