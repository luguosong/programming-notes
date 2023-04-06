package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.luguosong.util.KeyStoreUtil;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * 自定义外观
 *
 * @author luguosong
 */
public class C2_04_CustomAppearance {

    /**
     * 文件夹
     */
    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/04_CustomAppearance/";

    /**
     * 签名域name
     */
    public static final String SIGNAME = "Signature1";
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        File file = new File(DEST);
        file.mkdirs();

        PdfReader reader = new PdfReader(DEST+"hello_empty.pdf");
        PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(DEST+"hello_signed.pdf")), new StampingProperties());

        PdfSignatureAppearance appearance = signer.getSignatureAppearance()
                .setReason("自定义外观示例")
                .setLocation("lgs");

        signer.setFieldName(SIGNAME);

        //获取背景层，画一个灰色的矩形作为背景。
        PdfFormXObject n0 = appearance.getLayer0();
        float x = n0.getBBox().toRectangle().getLeft();
        float y = n0.getBBox().toRectangle().getBottom();
        float width = n0.getBBox().toRectangle().getWidth();
        float height = n0.getBBox().toRectangle().getHeight();
        PdfCanvas canvas = new PdfCanvas(n0, signer.getDocument());
        canvas.setFillColor(ColorConstants.LIGHT_GRAY);
        canvas.rectangle(x, y, width, height);
        canvas.fill();

        //在第2层设置签名信息
        PdfFormXObject n2 = appearance.getLayer2();
        Paragraph p = new Paragraph("这份文件是由lgs签署的。");
        new Canvas(n2, signer.getDocument()).add(p);

        IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
        IExternalDigest digest = new BouncyCastleDigest();

        // 请使用分离模式（detached mode），或等效的CMS或CAdES方式签署该文件。
        signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);
    }
}
