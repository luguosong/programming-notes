package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.signatures.*;
import com.luguosong.util.KeyStoreUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.Security;

/**
 * 创建pdf签名域，并对其进行签名
 *
 * @author luguosong
 */
public class C2_03_CreateFileWithEmptyField {

    /**
     * 案例文件夹
     */
    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/03_SignEmptyField/";

    /**
     * 签名域name
     */
    public static final String SIGNAME = "Signature1";

    /**
     * 创建一个带有签名域的pdf
     *
     * @param fileName
     */
    public static void createPdf(String fileName) {
        try {
            File file = new File(DEST);
            file.mkdirs();

            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(DEST + fileName));

            Document doc = new Document(pdfDoc);
            doc.add(new Paragraph("Hello World!"));

            // 创建一个签名域
            PdfFormField field = PdfFormField.createSignature(pdfDoc, new Rectangle(72, 632, 200, 100));
            field.setFieldName(SIGNAME);
            field.setPage(1);

            //将签名域插入静态表单
            PdfAcroForm.getAcroForm(pdfDoc, true).addField(field);

            doc.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 对PDF进行签名
     *
     * @param src  源文件路径
     * @param dest 目标文件路径
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void sign(String src, String dest)
            throws GeneralSecurityException, IOException {

        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        PdfReader reader = new PdfReader(DEST + src);
        PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(DEST + dest)), new StampingProperties());

        // 创建签名外观
        signer.getSignatureAppearance()
                .setReason("Test")
                .setLocation("Ghent");

        //这个名称与文件中已经存在的字段名称相对应。
        signer.setFieldName(SIGNAME);

        IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
        IExternalDigest digest = new BouncyCastleDigest();

        // 使用分离模式、CMS或CAdES等效模式签署文件。
        signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        String src = "hello_empty.pdf";
        String dest = "field_signed.pdf";
        createPdf(src);
        sign(src, dest);
    }
}
