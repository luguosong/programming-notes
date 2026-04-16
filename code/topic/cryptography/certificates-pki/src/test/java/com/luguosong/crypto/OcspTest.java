package com.luguosong.crypto;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cert.ocsp.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OCSP（在线证书状态协议）演示
 * <p>
 * 包含 OCSP 请求的生成和 OCSP 响应的构建
 */
class OcspTest {

    @BeforeAll
    static void setup() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成 RSA 2048 密钥对
     */
    private static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGen.initialize(2048);
        return keyPairGen.generateKeyPair();
    }

    /**
     * 创建一个简单的 V3 CA 证书
     */
    private static X509Certificate createCACertificate(KeyPair caKeyPair) throws Exception {
        X500Name caName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Test OCSP CA")
                .addRDN(BCStyle.O, "Test Org")
                .build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        var certBuilder = new JcaX509v3CertificateBuilder(
                caName, BigInteger.valueOf(1), notBefore, notAfter,
                caName, caKeyPair.getPublic()
        );
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(caKeyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic()));

        X509CertificateHolder holder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }

    /**
     * 创建一个终端实体证书
     */
    private static X509Certificate createEndEntityCert(KeyPair caKeyPair, BigInteger serial) throws Exception {
        KeyPair eeKeyPair = generateRsaKeyPair();
        X500Name caName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Test OCSP CA")
                .addRDN(BCStyle.O, "Test Org")
                .build();
        X500Name eeName = new X500NameBuilder(BCStyle.INSTANCE)
                .addRDN(BCStyle.CN, "Test Server")
                .addRDN(BCStyle.O, "Test Org")
                .build();

        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);

        var certBuilder = new JcaX509v3CertificateBuilder(
                caName, serial, notBefore, notAfter,
                eeName, eeKeyPair.getPublic()
        );

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(eeKeyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false,
                extUtils.createAuthorityKeyIdentifier(caKeyPair.getPublic()));

        X509CertificateHolder holder = certBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate())
        );
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
    }

    @Test
    void shouldGenerateOCSPRequest() throws Exception {
        // 1. 准备 CA 证书和终端实体证书
        KeyPair caKeyPair = generateRsaKeyPair();
        X509Certificate caCert = createCACertificate(caKeyPair);
        BigInteger eeSerial = BigInteger.valueOf(3001);
        X509Certificate eeCert = createEndEntityCert(caKeyPair, eeSerial);

        // 2. 构造 OCSP 请求
        //    OCSPReqBuilder 用于构建 OCSP 请求（RFC 6960）
        OCSPReqBuilder reqBuilder = new OCSPReqBuilder();

        // 3. 添加要查询的证书信息
        //    CertificateID 通过签发者名称 + 签发者公钥 + 证书序列号唯一标识一张证书
        //    使用 JcaDigestCalculatorProviderBuilder 提供 DigestCalculator
        CertificateID certId = new CertificateID(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build().get(CertificateID.HASH_SHA1),
                new X509CertificateHolder(caCert.getEncoded()),
                eeSerial
        );
        reqBuilder.addRequest(certId);

        // 4. 生成 OCSP 请求（不签名，匿名请求）
        OCSPReq ocspRequest = reqBuilder.build();

        // 5. 断言检查
        assertNotNull(ocspRequest, "OCSP 请求不应为 null");
        assertEquals(1, ocspRequest.getRequestList().length, "应包含 1 个证书状态查询");
        assertEquals(eeSerial, ocspRequest.getRequestList()[0].getCertID().getSerialNumber(),
                "请求中的序列号应匹配");

        System.out.println("=== OCSP 请求生成成功 ===");
        System.out.println("查询证书数: " + ocspRequest.getRequestList().length);
        System.out.println("查询序列号: " + ocspRequest.getRequestList()[0].getCertID().getSerialNumber());

        // 6. 打印请求的 DER 编码大小
        byte[] encoded = ocspRequest.getEncoded();
        System.out.println("OCSP 请求 DER 编码大小: " + encoded.length + " bytes");
    }

    @Test
    void shouldGenerateOCSPResponse() throws Exception {
        // 1. 准备 CA 证书和终端实体证书
        KeyPair caKeyPair = generateRsaKeyPair();
        X509Certificate caCert = createCACertificate(caKeyPair);
        BigInteger eeSerial = BigInteger.valueOf(4001);
        X509Certificate eeCert = createEndEntityCert(caKeyPair, eeSerial);
        System.out.println("CA 证书: " + caCert.getSubjectX500Principal().getName());
        System.out.println("终端实体证书序列号: " + eeCert.getSerialNumber());

        // 2. 构造 CertificateID（与请求中使用的方式相同）
        CertificateID certId = new CertificateID(
                new JcaDigestCalculatorProviderBuilder().setProvider("BC").build().get(CertificateID.HASH_SHA1),
                new X509CertificateHolder(caCert.getEncoded()),
                eeSerial
        );

        // 3. 构建基本 OCSP 响应
        //    使用 CA 证书的主体名称作为响应者标识
        X500Name caName = new X500Name(caCert.getSubjectX500Principal().getName());
        BasicOCSPRespBuilder respBuilder = new BasicOCSPRespBuilder(new RespID(caName));

        // 4. 添加证书状态：CertificateStatus.GOOD 表示证书有效（未被吊销）
        Date now = new Date();
        Date nextUpdate = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L);  // 7 天后过期
        respBuilder.addResponse(
                certId,
                CertificateStatus.GOOD,  // 状态：good（有效）
                now,                      // thisUpdate：本次更新时间
                nextUpdate                // nextUpdate：下次更新时间
        );

        // 5. 签发 OCSP 响应
        //    build() 参数：ContentSigner（签名器）、证书链（null 表示不附加）、产生时间
        BasicOCSPResp basicResp = respBuilder.build(
                new JcaContentSignerBuilder("SHA256WithRSA").setProvider("BC")
                        .build(caKeyPair.getPrivate()),
                null,    // 不附加额外证书链
                now      // 产生时间
        );

        // 6. 包装为 OCSPResp（响应状态码映射）
        OCSPResp ocspResponse = new OCSPRespBuilder().build(
                OCSPResp.SUCCESSFUL,  // 响应状态：成功
                basicResp
        );

        // 7. 断言检查
        assertNotNull(ocspResponse, "OCSP 响应不应为 null");
        assertEquals(OCSPResp.SUCCESSFUL, ocspResponse.getStatus(), "响应状态应为 SUCCESSFUL");

        // 8. 解析并验证响应内容
        BasicOCSPResp parsedBasicResp = (BasicOCSPResp) ocspResponse.getResponseObject();
        assertNotNull(parsedBasicResp, "基本 OCSP 响应不应为 null");

        // 获取响应中的单个响应条目
        SingleResp[] responses = parsedBasicResp.getResponses();
        assertEquals(1, responses.length, "应包含 1 个响应条目");

        SingleResp singleResp = responses[0];
        assertEquals(eeSerial, singleResp.getCertID().getSerialNumber(), "响应序列号应匹配");

        // 验证证书状态为 good（getCertStatus() 返回 null 表示 GOOD）
        Object status = singleResp.getCertStatus();
        assertNull(status, "证书状态为 GOOD 时 getCertStatus() 返回 null");
        System.out.println("证书状态: GOOD（有效）");

        System.out.println("=== OCSP 响应生成成功 ===");
        System.out.println("响应状态码: " + ocspResponse.getStatus());
        System.out.println("查询序列号: " + singleResp.getCertID().getSerialNumber());
        System.out.println("证书状态: GOOD");
        System.out.println("本次更新: " + singleResp.getThisUpdate());
        System.out.println("下次更新: " + singleResp.getNextUpdate());

        // 9. 验证 OCSP 响应的签名
        //    使用 JcaContentVerifierProviderBuilder 构建验证器
        boolean sigValid = parsedBasicResp.isSignatureValid(
                new JcaContentVerifierProviderBuilder()
                        .setProvider("BC")
                        .build(caKeyPair.getPublic())
        );
        assertTrue(sigValid, "OCSP 响应签名应有效");
        System.out.println("OCSP 响应签名验证通过");
    }
}
