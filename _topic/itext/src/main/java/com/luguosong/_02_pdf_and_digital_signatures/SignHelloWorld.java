package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

/**
 * @author luguosong
 */
public class SignHelloWorld {
    /**
     * @param src             源文件
     * @param dest            目标文件
     * @param chain           证书链
     * @param pk              私钥
     * @param digestAlgorithm 摘要算法
     * @param provider        提供者
     * @param signatureType   签名类型
     * @param reason          签名原因
     * @param location        签名地点
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public void sign(String src, String dest, Certificate[] chain, PrivateKey pk, String digestAlgorithm,
                     String provider, PdfSigner.CryptoStandard signatureType, String reason, String location)
            throws GeneralSecurityException, IOException {
        // 创建PdfReader对象
        PdfReader reader = new PdfReader(src);
        // 创建PdfSigner对象
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());

        // 创建签名外观
        Rectangle rect = new Rectangle(36, 648, 200, 100);
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance.setReason(reason)
                .setLocation(location)
                // 指定在字段签名之前的外观是否将被用作签名字段的背景。"false"值是默认值。
                .setReuseAppearance(false)
                .setPageRect(rect)
                .setPageNumber(1);
        signer.setFieldName("sig");

        //ExternalSignature接口的实现来创建签名
        IExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);
        // ExternalDigest接口的实现来创建摘要
        IExternalDigest digest = new BouncyCastleDigest();

        // 使用分离模式签署文档，CMS或CAdES等效方式。
        signer.signDetached(digest, pks, chain, null, null, null, 0, signatureType);
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        // 添加BouncyCastleProvider
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        // 创建并初始化密钥库,
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream("_topic/itext/src/main/resources/keystore/ks"), "12345678".toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, "12345678".toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);


        SignHelloWorld app = new SignHelloWorld();
        String src = "_topic/itext/src/main/resources/pdf/hello.pdf";
        String dest = "_topic/itext/src/main/resources/pdf/chapter02/SignHelloWorld/";
        // PdfSigner.CryptoStandard.CMS表示adbe.pkcs7.detached子过滤器
        // 使用SHA256摘要算法
        app.sign(src, dest + "hello_signed1.pdf", chain, pk, DigestAlgorithms.SHA256, provider.getName(),
                PdfSigner.CryptoStandard.CMS, "Test 1", "Ghent");
        // PdfSigner.CryptoStandard.CMS表示adbe.pkcs7.detached子过滤器
        // 使用SHA512摘要算法
        app.sign(src, dest + "hello_signed2.pdf", chain, pk, DigestAlgorithms.SHA512, provider.getName(),
                PdfSigner.CryptoStandard.CMS, "Test 2", "Ghent");
        // PdfSigner.CryptoStandard.CADES表示ETSI.CAdES.detached子过滤器
        // 使用SHA256摘要算法
        app.sign(src, dest + "hello_signed3.pdf", chain, pk, DigestAlgorithms.SHA256, provider.getName(),
                PdfSigner.CryptoStandard.CADES, "Test 3", "Ghent");
        // PdfSigner.CryptoStandard.CADES表示ETSI.CAdES.detached子过滤器
        // 使用RIPEMD160摘要算法
        app.sign(src, dest + "hello_signed4.pdf", chain, pk, DigestAlgorithms.RIPEMD160, provider.getName(),
                PdfSigner.CryptoStandard.CADES, "Test 4", "Ghent");
    }
}
