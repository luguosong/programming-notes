package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import com.luguosong.util.KeyStoreUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * 使用快捷方式定义签名的外观
 * 而不是使用底层的n0和n2层
 *
 * @author luguosong
 */
public class C2_05_SignatureAppearance {

    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/05_SignatureAppearance/";

    public static final String SRC = DEST + "empty_signature_form_field.pdf";

    /**
     *
     */
    public static final String IMG = DEST + "1t3xt.gif";

    /**
     * 签名域name
     */
    public static final String SIGNAME = "Signature1";

    /**
     *
     */
    private static BouncyCastleProvider provider = new BouncyCastleProvider();

    /**
     * 设置自定义文本和一个自定义字体
     */
    public static void sign1(){
        try {
            PdfReader reader = new PdfReader(SRC);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(DEST+"signature_appearance1.pdf"), new StampingProperties());

            //创建签名外观
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Custom appearance example1");
            appearance.setLocation("Ghent1");

            //指定签名域名称
            //这个名称与文件中已经存在的字段名称相对应。
            signer.setFieldName(SIGNAME);


            //设置标识签名者的签名文本。
            appearance.setLayer2Text("This document was signed by Bruno Specimen");
            //设置 n2 和 n4 图层字体。如果字体大小为零，将使用自动调整。
            appearance.setLayer2Font(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN));

            //IExternalSignature接口表示数字签名的外部实现
            //PrivateKeySignature是实现IExternalSignature接口的一个具体类，它使用给定的私钥、摘要算法和提供者来创建数字签名。
            IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
            //IExternalDigest接口表示数字签名所使用的消息摘要算法的外部实现，
            //BouncyCastleDigest是实现IExternalDigest接口的一个具体类，它使用Bouncy Castle库提供的算法来计算消息摘要。在此示例中，它用于计算待签名数据的摘要，以便进行数字签名。
            IExternalDigest digest = new BouncyCastleDigest();

            //使用分离模式、CMS或CAdES等效模式签署文件。
            signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置一个自定义的文本和一个背景图像
     */
    public static void sign2(){
        try {
            PdfReader reader = new PdfReader(SRC);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(DEST+"signature_appearance2.pdf"), new StampingProperties());

            //创建签名外观
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Custom appearance example2");
            appearance.setLocation("Ghent2");

            //指定签名域名称
            //这个名称与文件中已经存在的字段名称相对应。
            signer.setFieldName(SIGNAME);


            //设置标识签名者的签名文本。
            appearance.setLayer2Text("This document was signed by Bruno Specimen");
            //设置图层 2 的背景图像。
            appearance.setImage(ImageDataFactory.create(IMG));
            //设置要应用于背景图像的缩放比例。如果它为零，图像将完全填满矩形。
            // 如果它小于零，图像将填充矩形但会保持比例。
            // 如果它大于零，则将应用缩放。在任何情况下，图像都将始终居中。
            // 默认情况下为零
            appearance.setImageScale(1);

            //IExternalSignature接口表示数字签名的外部实现
            //PrivateKeySignature是实现IExternalSignature接口的一个具体类，它使用给定的私钥、摘要算法和提供者来创建数字签名。
            IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
            //IExternalDigest接口表示数字签名所使用的消息摘要算法的外部实现，
            //BouncyCastleDigest是实现IExternalDigest接口的一个具体类，它使用Bouncy Castle库提供的算法来计算消息摘要。在此示例中，它用于计算待签名数据的摘要，以便进行数字签名。
            IExternalDigest digest = new BouncyCastleDigest();

            //使用分离模式、CMS或CAdES等效模式签署文件。
            signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sign3(){
        try {
            PdfReader reader = new PdfReader(SRC);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(DEST+"signature_appearance3.pdf"), new StampingProperties());

            //创建签名外观
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Custom appearance example3");
            appearance.setLocation("Ghent3");

            //指定签名域名称
            //这个名称与文件中已经存在的字段名称相对应。
            signer.setFieldName(SIGNAME);


            //设置标识签名者的签名文本。
            appearance.setLayer2Text("This document was signed by Bruno Specimen");
            //设置图层 2 的背景图像。
            appearance.setImage(ImageDataFactory.create(IMG));
            //设置要应用于背景图像的缩放比例。如果它为零，图像将完全填满矩形。
            // 如果它小于零，图像将填充矩形但会保持比例。
            // 如果它大于零，则将应用缩放。在任何情况下，图像都将始终居中。
            // 默认情况下为零
            appearance.setImageScale(-1);

            //IExternalSignature接口表示数字签名的外部实现
            //PrivateKeySignature是实现IExternalSignature接口的一个具体类，它使用给定的私钥、摘要算法和提供者来创建数字签名。
            IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
            //IExternalDigest接口表示数字签名所使用的消息摘要算法的外部实现，
            //BouncyCastleDigest是实现IExternalDigest接口的一个具体类，它使用Bouncy Castle库提供的算法来计算消息摘要。在此示例中，它用于计算待签名数据的摘要，以便进行数字签名。
            IExternalDigest digest = new BouncyCastleDigest();

            //使用分离模式、CMS或CAdES等效模式签署文件。
            signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Security.addProvider(provider);

        //确保文件夹存在
        File file = new File(DEST);
        file.mkdirs();

        sign1();
        sign2();
        sign3();
    }
}
