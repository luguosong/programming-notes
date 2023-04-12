package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.signatures.*;
import com.luguosong.util.KeyStoreUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * 文档的认证级别示例
 *
 * @author luguosong
 */
public class C2_08_SignatureTypes {


    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/08_SignatureTypes/";

    public static final String SRC = DEST + "empty_signature_form_field.pdf";

    private static BouncyCastleProvider provider = new BouncyCastleProvider();


    /**
     * 采用不同的签名级别对文档进行第一次签名
     *
     * @param dest               目标文件
     * @param certificationLevel 签名级别
     */
    public static void sign(String dest, int certificationLevel) {
        PdfReader reader = null;
        try {
            //创建签名对象
            reader = new PdfReader(SRC);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties());

            //创建签名外观
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Test metadata");
            appearance.setLocation("Ghent");

            signer.setFieldName("Signature1");

            signer.setCertificationLevel(certificationLevel);

            PrivateKeySignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            //签名
            signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 在已签名的文档中添加批注
     *
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void addAnnotation(String src, String dest) {
        try {
            PdfReader reader = new PdfReader(src);
            //使用AppendMode模式
            PdfDocument pdfDoc = new PdfDocument(reader, new PdfWriter(dest), new StampingProperties().useAppendMode());

            PdfAnnotation comment = new PdfTextAnnotation(new Rectangle(200, 800, 50, 20))
                    .setOpen(true)
                    .setIconName(new PdfName("Comment"))
                    .setTitle(new PdfString("Finally Signed!"))
                    .setContents("Bruno Specimen has finally signed the document");
            pdfDoc.getFirstPage().addAnnotation(comment);

            pdfDoc.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加错误的批注
     *
     * @param src  源文件
     * @param dest 目标文件
     * @throws IOException
     */
    public static void addWrongAnnotation(String src, String dest) {
        try {
            PdfReader reader = new PdfReader(src);

            //不使用AppendMode模式
            PdfDocument pdfDoc = new PdfDocument(reader, new PdfWriter(dest));

            PdfAnnotation comment = new PdfTextAnnotation(new Rectangle(200, 800, 50, 20))
                    .setOpen(true)
                    .setIconName(new PdfName("Comment"))
                    .setTitle(new PdfString("Finally Signed!"))
                    .setContents("Bruno Specimen has finally signed the document");
            pdfDoc.getFirstPage().addAnnotation(comment);

            pdfDoc.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 在已签名的文档中添加文字
     *
     * @param src  源文件
     * @param dest 目标文件
     */
    public static void addText(String src, String dest) {
        try {
            PdfReader reader = new PdfReader(src);
            PdfDocument pdfDoc = new PdfDocument(reader, new PdfWriter(dest), new StampingProperties().useAppendMode());
            PdfPage firstPage = pdfDoc.getFirstPage();

            //添加文字
            new Canvas(firstPage, firstPage.getPageSize()).showTextAligned("TOP SECRET", 36, 820,
                    TextAlignment.LEFT);

            pdfDoc.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 再次签名
     *
     * @param src
     * @param dest
     */
    public static void signAgain(String src, String dest) {
        try {
            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties().useAppendMode());

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Test");
            appearance.setLocation("Ghent");
            appearance.setReuseAppearance(false);
            Rectangle rect = new Rectangle(36, 700, 200, 100);
            appearance.setPageRect(rect);
            appearance.setPageNumber(1);
            signer.setFieldName("Signature2");

            PrivateKeySignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        File file = new File(DEST);
        file.mkdirs();

        Security.addProvider(provider);

        //签章级别：未认证、认证不允许修改、认证允许表单填充、认证允许表单填充和注释
        sign(DEST + "01_no_certification.pdf", PdfSigner.NOT_CERTIFIED);
        sign(DEST + "01_certification.pdf", PdfSigner.CERTIFIED_NO_CHANGES_ALLOWED);
        sign(DEST + "01_certification_form_filling.pdf", PdfSigner.CERTIFIED_FORM_FILLING);
        sign(DEST + "01_certification_form_filling_and_annotations.pdf", PdfSigner.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS);

        //添加批注
        addAnnotation(DEST + "01_no_certification.pdf", DEST + "02_no_certification_with_annotation.pdf"); //签名有效
        addAnnotation(DEST + "01_certification.pdf", DEST + "02_certification_with_annotation.pdf"); //签名失效
        addAnnotation(DEST + "01_certification_form_filling.pdf", DEST + "02_certification_form_filling_with_annotation.pdf"); //签名失效
        addAnnotation(DEST + "01_certification_form_filling_and_annotations.pdf", DEST + "02_certification_form_filling_and_annotations_with_annotation.pdf"); //签名有效

        //添加错误的批注
        addWrongAnnotation(DEST + "01_no_certification.pdf", DEST + "03_no_certification_with_wrong_annotation.pdf"); //签名已损坏

        //添加文字
        addText(DEST + "01_no_certification.pdf", DEST + "04_no_certification_with_text.pdf"); //签名失效

        //再次签名
        signAgain(DEST + "01_no_certification.pdf", DEST + "05_no_certification_with_signature.pdf"); //第一个签名有效
        signAgain(DEST + "01_certification.pdf", DEST + "05_certification_with_signature.pdf"); //第一个签名失效
        signAgain(DEST + "01_certification_form_filling.pdf", DEST + "05_certification_form_filling_with_signature.pdf"); //第一个签名有效
        signAgain(DEST + "01_certification_form_filling_and_annotations.pdf", DEST + "05_certification_form_filling_and_annotations_with_signature.pdf"); //第一个签名有效

    }
}
