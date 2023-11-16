package com.luguosong._02_pdf_and_digital_signatures;

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
public class SignEmptyField {
    public void sign(String src, String name, String dest, Certificate[] chain, PrivateKey pk, String digestAlgorithm,
                     String provider, PdfSigner.CryptoStandard subfilter, String reason, String location)
            throws GeneralSecurityException, IOException {
        PdfReader reader = new PdfReader(src);
        PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());

        // 创建签名外观
        signer.getSignatureAppearance()
                .setReason(reason)
                .setLocation(location)

                //指定在字段被签名之前的外观是否将被用作已签名字段的背景。"false"值是默认值。
                .setReuseAppearance(false);

        // ⭐这个名称对应于文档中已经存在的字段名称。
        signer.setFieldName(name);

        IExternalSignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);
        IExternalDigest digest = new BouncyCastleDigest();

        // 使用分离模式、CMS或CAdES等效方式对文档进行签署。
        signer.signDetached(digest, pks, chain, null, null, null, 0, subfilter);
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


        SignEmptyField app = new SignEmptyField();
        String src = "_topic/itext/src/main/resources/pdf/hello_to_sign.pdf";
        String dest = "_topic/itext/src/main/resources/pdf/chapter02/SignEmptyField/";
        // PdfSigner.CryptoStandard.CMS表示adbe.pkcs7.detached子过滤器
        // 使用SHA256摘要算法
        app.sign(src, "Signature1", dest + "hello_signed1.pdf", chain, pk, DigestAlgorithms.SHA256, provider.getName(),
                PdfSigner.CryptoStandard.CMS, "Test 1", "Ghent");
        // PdfSigner.CryptoStandard.CMS表示adbe.pkcs7.detached子过滤器
        // 使用SHA512摘要算法
        app.sign(src, "Signature1", dest + "hello_signed2.pdf", chain, pk, DigestAlgorithms.SHA512, provider.getName(),
                PdfSigner.CryptoStandard.CMS, "Test 2", "Ghent");
        // PdfSigner.CryptoStandard.CADES表示ETSI.CAdES.detached子过滤器
        // 使用SHA256摘要算法
        app.sign(src, "Signature1", dest + "hello_signed3.pdf", chain, pk, DigestAlgorithms.SHA256, provider.getName(),
                PdfSigner.CryptoStandard.CADES, "Test 3", "Ghent");
        // PdfSigner.CryptoStandard.CADES表示ETSI.CAdES.detached子过滤器
        // 使用RIPEMD160摘要算法
        app.sign(src, "Signature1", dest + "hello_signed4.pdf", chain, pk, DigestAlgorithms.RIPEMD160, provider.getName(),
                PdfSigner.CryptoStandard.CADES, "Test 4", "Ghent");
    }
}
