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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;

/**
 * @author luguosong
 */
public class C2_09_SequentialSignatures {
    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/09_SequentialSignatures/";

    public static final String SRC = DEST + "empty_signature_form_field.pdf";

    private static BouncyCastleProvider provider = new BouncyCastleProvider();

    public static final String ALICE = DEST + "alice";
    public static final String BOB = DEST + "bob";
    public static final String CAROL = DEST + "carol";
    public static final char[] PASSWORD = "password".toCharArray();

    /**
     * 为了在签名域上绘制签名域，我们需要创建一个自定义的CellRenderer。
     * 在这里，我们将使用它来绘制签名域，但不会绘制任何内容。
     * 请注意，我们需要设置PdfFormField的名称，以便我们可以在后面使用它。
     */
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

    /**
     * 自定义CellRenderer，用于在单元格中绘制签名域
     */
    public static void createForm() {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(SRC));
            Document doc = new Document(pdfDoc);


            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            table.addCell("Signer 1: Alice");
            table.addCell(createSignatureFieldCell("sig1"));
            table.addCell("Signer 2: Bob");
            table.addCell(createSignatureFieldCell("sig2"));
            table.addCell("Signer 3: Carol");
            table.addCell(createSignatureFieldCell("sig3"));
            doc.add(table);

            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义CellRenderer，用于在单元格中绘制签名域
     *
     * @param name
     * @return
     */
    private static Cell createSignatureFieldCell(String name) {
        Cell cell = new Cell();
        cell.setHeight(50);
        cell.setNextRenderer(new SignatureFieldCellRenderer(cell, name));
        return cell;
    }

    /**
     * 签名
     *
     * @param keystore 证书库
     * @param level    签名级别
     * @param src      源文件
     * @param dest     目标文件
     * @param name     签名域名称
     */
    public static void sign(String keystore, int level, String src, String dest, String name) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(Files.newInputStream(Paths.get(keystore)), PASSWORD);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);

            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader,
                    Files.newOutputStream(Paths.get(dest)),
                    new StampingProperties().useAppendMode());

            signer.setFieldName(name);
            signer.setCertificationLevel(level);

            IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            signer.signDetached(digest, pks, ks.getCertificateChain(alias), null, null, null,
                    0, PdfSigner.CryptoStandard.CMS);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        //创建目标文件夹
        new File(DEST).mkdirs();
        //添加BouncyCastleProvider
        Security.addProvider(provider);

        //创建带有三个签名域的空白PDF文档
        createForm();

        sign(ALICE, PdfSigner.CERTIFIED_FORM_FILLING,SRC,DEST+"01_01_signed_by_alice.pdf", "sig1");
        sign(BOB, PdfSigner.NOT_CERTIFIED,DEST+"01_01_signed_by_alice.pdf",DEST+"01_02_signed_by_bob.pdf", "sig2");
        sign(CAROL, PdfSigner.NOT_CERTIFIED,DEST+"01_02_signed_by_bob.pdf",DEST+"01_03_signed_by_carol.pdf", "sig3");

        sign(ALICE, PdfSigner.NOT_CERTIFIED,SRC,DEST+"02_01_signed_by_alice.pdf", "sig1");
        sign(BOB, PdfSigner.NOT_CERTIFIED,DEST+"02_01_signed_by_alice.pdf",DEST+"02_02_signed_by_bob.pdf", "sig2");
        sign(CAROL, PdfSigner.CERTIFIED_FORM_FILLING,DEST+"02_02_signed_by_bob.pdf",DEST+"02_03_signed_by_carol.pdf", "sig3");

        sign(ALICE, PdfSigner.NOT_CERTIFIED,SRC,DEST+"03_01_signed_by_alice.pdf", "sig1");
        sign(BOB, PdfSigner.NOT_CERTIFIED,DEST+"03_01_signed_by_alice.pdf",DEST+"03_02_signed_by_bob.pdf", "sig2");
        sign(CAROL, PdfSigner.CERTIFIED_NO_CHANGES_ALLOWED,DEST+"03_02_signed_by_bob.pdf",DEST+"03_03_signed_by_carol.pdf", "sig3");

        sign(ALICE, PdfSigner.CERTIFIED_FORM_FILLING,SRC,DEST+"04_01_signed_by_alice.pdf", "sig1");
        sign(BOB, PdfSigner.NOT_CERTIFIED,DEST+"04_01_signed_by_alice.pdf",DEST+"04_02_signed_by_bob.pdf", "sig2");
        sign(CAROL, PdfSigner.CERTIFIED_FORM_FILLING,DEST+"04_02_signed_by_bob.pdf",DEST+"04_03_signed_by_carol.pdf", "sig3");
    }
}
