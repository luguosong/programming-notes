package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

/**
 * 多人工作流签章
 *
 * @author luguosong
 */
public class C2_10_SignatureWorkflow {
    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/10_SignatureWorkflow/";

    public static final String SRC = DEST + "empty_signature_form_field.pdf";

    private static BouncyCastleProvider provider = new BouncyCastleProvider();

    public static final char[] PASSWORD = "password".toCharArray();

    public static final String ALICE = DEST + "alice";
    public static final String BOB = DEST + "bob";
    public static final String CAROL = DEST + "carol";
    public static final String DAVE = DEST + "dave";


    private static class TextFieldCellRenderer extends CellRenderer {
        public String name;

        public TextFieldCellRenderer(Cell modelElement, String name) {
            super(modelElement);
            this.name = name;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);
            PdfFormField field = PdfFormField.createText(drawContext.getDocument(), getOccupiedAreaBBox(), name);
            PdfAcroForm.getAcroForm(drawContext.getDocument(), true).addField(field);
        }
    }


    private static class SignatureFieldCellRenderer extends CellRenderer {
        public String name;

        public SignatureFieldCellRenderer(Cell modelElement, String name) {
            super(modelElement);
            this.name = name;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);
            PdfFormField field = PdfFormField.createSignature(drawContext.getDocument(), getOccupiedAreaBBox());
            field.setFieldName(name);
            field.getWidgets().get(0).setHighlightMode(PdfAnnotation.HIGHLIGHT_INVERT);
            field.getWidgets().get(0).setFlags(PdfAnnotation.PRINT);
            PdfAcroForm.getAcroForm(drawContext.getDocument(), true).addField(field);
        }
    }

    protected static Cell createTextFieldCell(String name) {
        Cell cell = new Cell();
        cell.setHeight(20);
        cell.setNextRenderer(new TextFieldCellRenderer(cell, name));
        return cell;
    }

    protected static Cell createSignatureFieldCell(String name) {
        Cell cell = new Cell();
        cell.setHeight(50);
        cell.setNextRenderer(new SignatureFieldCellRenderer(cell, name));
        return cell;
    }

    /**
     * 创建空白的签章表单
     */
    public static void createForm() {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(SRC));
            Document doc = new Document(pdfDoc);

            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            table.addCell("Written by Alice");
            table.addCell(createSignatureFieldCell("sig1"));
            table.addCell("For approval by Bob");
            table.addCell(createTextFieldCell("approved_bob"));
            table.addCell(createSignatureFieldCell("sig2"));
            table.addCell("For approval by Carol");
            table.addCell(createTextFieldCell("approved_carol"));
            table.addCell(createSignatureFieldCell("sig3"));
            table.addCell("For approval by Dave");
            table.addCell(createTextFieldCell("approved_dave"));
            table.addCell(createSignatureFieldCell("sig4"));
            doc.add(table);

            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 认证签名
     *
     * @param keystore
     * @param src
     * @param name
     * @param dest
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static void certify(String keystore, String src, String name, String dest) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(Files.newInputStream(Paths.get(keystore)), PASSWORD);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
            Certificate[] chain = ks.getCertificateChain(alias);

            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(dest)), new StampingProperties().useAppendMode());

            // 设置签名位置和签名等级
            signer.setFieldName(name);
            signer.setCertificationLevel(PdfSigner.CERTIFIED_FORM_FILLING);

            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            // Sign the document using the detached mode, CMS or CAdES equivalent.
            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 填充表单
     *
     * @param src   源文件
     * @param dest  目标文件
     * @param name  字段名
     * @param value 字段值
     */
    public static void fillOut(String src, String dest, String name, String value) {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest),
                    new StampingProperties().useAppendMode());

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            form.getField(name).setValue(value);
            form.getField(name).setReadOnly(true);

            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 设置批准签名
     *
     * @param keystore
     * @param src
     * @param name
     * @param dest
     */
    public static void sign(String keystore, String src, String name, String dest) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(new FileInputStream(keystore), PASSWORD);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
            Certificate[] chain = ks.getCertificateChain(alias);

            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(dest), new StampingProperties().useAppendMode());
            signer.setFieldName(name);

            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void fillOutAndSign(String keystore, String src, String name, String fname, String value, String dest) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(Files.newInputStream(Paths.get(keystore)), PASSWORD);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
            Certificate[] chain = ks.getCertificateChain(alias);

            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(dest)), new StampingProperties().useAppendMode());
            signer.setFieldName(name);

            PdfAcroForm form = PdfAcroForm.getAcroForm(signer.getDocument(), true);
            form.getField(fname).setValue(value);
            form.getField(fname).setReadOnly(true);

            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new File(DEST).mkdirs();

        Security.addProvider(provider);

        // 创建空白的签章表单
        createForm();

        //Alice进行认证签名
        String aliceCertifiedFile = DEST + "step1_signed_by_alice.pdf";
        certify(ALICE, SRC, "sig1", aliceCertifiedFile);

        String bobFilledFile = DEST + "step2_signed_by_alice_and_filled_out_by_bob.pdf";
        String bobSignedFile = DEST + "step3_signed_by_alice_and_bob.pdf";
        //Bob将"Read and Approved by Bob"插入文本
        fillOut(aliceCertifiedFile, bobFilledFile, "approved_bob", "Read and Approved by Bob");
        //Bob进行批准签名
        sign(BOB, bobFilledFile, "sig2", bobSignedFile);

        String carolFilledFile = DEST + "step4_signed_by_alice_and_bob_filled_out_by_carol.pdf";
        String carolSignedFile = DEST + "step5_signed_by_alice_bob_and_carol.pdf";
        //Carol将"Read and Approved by Carol"插入文本
        fillOut(bobSignedFile, carolFilledFile, "approved_carol", "Read and Approved by Carol");
        //Carol进行批准签名
        sign(CAROL, carolFilledFile, "sig3", carolSignedFile);

        String daveFilledCertifiedFile = DEST + "step6_signed_by_alice_bob_carol_and_dave.pdf";
        fillOutAndSign(DAVE, carolSignedFile, "sig4",
                "approved_dave", "Read and Approved by Dave", daveFilledCertifiedFile);
    }

}
