package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.luguosong.util.KeyStoreUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * 使用临时文件签署文件
 * 一般用于签署大文件
 * @author luguosong
 */
public class C2_02_SignHelloWorldWithTempFile {
    public static void main(String[] args) {
        try {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            PdfReader pdfReader = new PdfReader("02_pdf_and_digital_signatures/01_SignHelloWorld/empty_document.pdf");
            PdfSigner pdfSigner = new PdfSigner(pdfReader,
                    // 创建输出流，用于将签署后的PDF文档写入文件
                    Files.newOutputStream(Paths.get("_topic/itext/src/test/resources/02_pdf_and_digital_signatures/02_SignHelloWorldWithTempFile/hello_signed_with_temp.pdf")),
                    //*****增加临时写入的文件*****
                    "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/02_SignHelloWorldWithTempFile/",
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
            IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, new BouncyCastleProvider().getName());
            IExternalDigest digest = new BouncyCastleDigest();

            pdfSigner.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
