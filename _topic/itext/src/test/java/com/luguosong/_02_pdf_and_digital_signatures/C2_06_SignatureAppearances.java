package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import com.luguosong.util.KeyStoreUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Date;

/**
 * 自定义渲染模式
 *
 * @author luguosong
 */
public class C2_06_SignatureAppearances {

    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/06_SignatureAppearances/";

    public static final String SRC = DEST + "empty_signature_form_field.pdf";

    public static final String IMG = DEST + "1t3xt.gif";

    public static final String SIGNAME = "Signature1";

    private static BouncyCastleProvider provider = new BouncyCastleProvider();

    public static void sign(String dest, PdfSignatureAppearance.RenderingMode renderingMode, ImageData image) {
        PdfReader reader = null;
        try {
            reader = new PdfReader(SRC);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Appearance");
            appearance.setLocation("Ghent");

            signer.setFieldName(SIGNAME);

            //设置标识签名者的签名文本
            appearance.setLayer2Text("Signed on " + new Date().toString());

            //设置此签名的渲染模式。
            appearance.setRenderingMode(renderingMode);

            //当渲染模式被设置为RenderingMode.GRAPHIC或RenderingMode.GRAPHIC_AND_DESCRIPTION时，设置要渲染的图像对象。
            appearance.setSignatureGraphic(image);



            //IExternalSignature接口表示数字签名的外部实现
            //PrivateKeySignature是实现IExternalSignature接口的一个具体类，它使用给定的私钥、摘要算法和提供者来创建数字签名。
            IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
            //IExternalDigest接口表示数字签名所使用的消息摘要算法的外部实现，
            //BouncyCastleDigest是实现IExternalDigest接口的一个具体类，它使用Bouncy Castle库提供的算法来计算消息摘要。在此示例中，它用于计算待签名数据的摘要，以便进行数字签名。
            IExternalDigest digest = new BouncyCastleDigest();

            signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {


        try {
            File file = new File(DEST);
            file.mkdirs();

            Security.addProvider(provider);

            ImageData image = ImageDataFactory.create(IMG);

            sign(DEST + "signature_appearance_1.pdf", PdfSignatureAppearance.RenderingMode.DESCRIPTION, null);
            sign(DEST + "signature_appearance_2.pdf", PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION, null);
            sign(DEST + "signature_appearance_3.pdf", PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION, image);
            sign(DEST + "signature_appearance_4.pdf", PdfSignatureAppearance.RenderingMode.GRAPHIC, image);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
