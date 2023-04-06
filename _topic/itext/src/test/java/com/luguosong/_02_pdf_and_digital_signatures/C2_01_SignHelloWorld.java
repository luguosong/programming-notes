package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;


import com.itextpdf.signatures.*;
import com.luguosong.util.KeyStoreUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * 添加可见签名到文档的简单示例
 *
 * @author luguosong
 */
public class C2_01_SignHelloWorld {
    public static void sign(String path,
                            PdfSigner.CryptoStandard signatureType,
                            String digestAlgorithm) {
        try {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            PdfReader pdfReader = new PdfReader("02_pdf_and_digital_signatures/01_SignHelloWorld/empty_document.pdf");
            PdfSigner pdfSigner = new PdfSigner(pdfReader,
                    // 创建输出流，用于将签署后的PDF文档写入文件
                    Files.newOutputStream(Paths.get(path)),
                    // 创建StampingProperties对象，用于设置签名属性
                    new StampingProperties());
            // 创建签名外观
            Rectangle rect = new Rectangle(36, 648, 200, 100);
            PdfSignatureAppearance appearance = pdfSigner.getSignatureAppearance();
            appearance.setReason("yuanyin")
                    .setLocation("weizhi")
                    //指定是否将签名字段之前的外观用作已签名字段的背景。 “false”值是默认值。
                    .setReuseAppearance(false)
                    .setPageRect(rect)
                    .setPageNumber(1);
            pdfSigner.setFieldName("sig");
            IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), digestAlgorithm, new BouncyCastleProvider().getName());
            IExternalDigest digest = new BouncyCastleDigest();

            pdfSigner.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, signatureType);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String outFolder = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/01_SignHelloWorld/";
        sign(outFolder + "hello_signed1.pdf", PdfSigner.CryptoStandard.CMS, DigestAlgorithms.SHA256);
        sign(outFolder + "hello_signed2.pdf", PdfSigner.CryptoStandard.CMS, DigestAlgorithms.SHA512);
        sign(outFolder + "hello_signed3.pdf", PdfSigner.CryptoStandard.CADES, DigestAlgorithms.SHA256);
        sign(outFolder + "hello_signed4.pdf", PdfSigner.CryptoStandard.CADES, DigestAlgorithms.RIPEMD160);
    }

}
