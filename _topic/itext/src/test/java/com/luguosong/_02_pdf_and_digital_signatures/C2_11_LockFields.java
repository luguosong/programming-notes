package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.PdfSigFieldLock;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.*;
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
import java.security.cert.Certificate;

/**
 * @author luguosong
 */
public class C2_11_LockFields {
    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/11_LockFields/";

    public static final String SRC = DEST + "empty_signature_form_field.pdf";

    public static final char[] PASSWORD = "password".toCharArray();

    private static BouncyCastleProvider provider = new BouncyCastleProvider();

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
        public PdfSigFieldLock lock;

        public SignatureFieldCellRenderer(Cell modelElement, String name, PdfSigFieldLock lock) {
            super(modelElement);
            this.name = name;
            this.lock = lock;
        }

        @Override
        public void draw(DrawContext drawContext) {
            super.draw(drawContext);
            PdfFormField field = PdfFormField.createSignature(drawContext.getDocument(), getOccupiedAreaBBox());
            field.setFieldName(name);

            // 如果存在锁定对象，则将锁定对象转换为PDF对象并将其添加到签名域中
            if (lock != null) {
                field.put(PdfName.Lock, lock.makeIndirect(drawContext.getDocument()).getPdfObject());
            }

            field.getWidgets().get(0).setFlag(PdfAnnotation.PRINT);
            field.getWidgets().get(0).setHighlightMode(PdfAnnotation.HIGHLIGHT_INVERT);
            PdfAcroForm.getAcroForm(drawContext.getDocument(), true).addField(field);
        }
    }

    protected static Cell createTextFieldCell(String name) {
        Cell cell = new Cell();
        cell.setHeight(20);
        cell.setNextRenderer(new TextFieldCellRenderer(cell, name));
        return cell;
    }

    protected static Cell createSignatureFieldCell(String name, PdfSigFieldLock lock) {
        Cell cell = new Cell();
        cell.setHeight(50);
        cell.setNextRenderer(new SignatureFieldCellRenderer(cell, name, lock));
        return cell;
    }

    public static void createForm() {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfWriter(SRC, new WriterProperties().setPdfVersion(PdfVersion.PDF_2_0)));
            Document doc = new Document(pdfDoc);

            Table table = new Table(UnitValue.createPercentArray(1)).useAllAvailableWidth();
            table.addCell("Written by Alice");
            table.addCell(createSignatureFieldCell("sig1", null));
            table.addCell("For approval by Bob");
            table.addCell(createTextFieldCell("approved_bob"));


            PdfSigFieldLock lock = new PdfSigFieldLock()
                    .setFieldLock(PdfSigFieldLock.LockAction.INCLUDE, "sig1", "approved_bob", "sig2");
            table.addCell(createSignatureFieldCell("sig2", lock));
            table.addCell("For approval by Carol");
            table.addCell(createTextFieldCell("approved_carol"));

            lock = new PdfSigFieldLock().setFieldLock(PdfSigFieldLock.LockAction.EXCLUDE, "approved_dave", "sig4");
            table.addCell(createSignatureFieldCell("sig3", lock));
            table.addCell("For approval by Dave");
            table.addCell(createTextFieldCell("approved_dave"));


            lock = new PdfSigFieldLock().setDocumentPermissions(PdfSigFieldLock.LockPermissions.NO_CHANGES_ALLOWED);
            table.addCell(createSignatureFieldCell("sig4", lock));
            doc.add(table);

            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void certify(String keystore, String src, String name, String dest) {
        try {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(Files.newInputStream(Paths.get(keystore)), PASSWORD);
            String alias = ks.aliases().nextElement();
            PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
            Certificate[] chain = ks.getCertificateChain(alias);

            PdfReader reader = new PdfReader(src);
            PdfSigner signer = new PdfSigner(reader, Files.newOutputStream(Paths.get(dest)), new StampingProperties().useAppendMode());

            // Set signer options
            signer.setFieldName(name);
            signer.setCertificationLevel(PdfSigner.CERTIFIED_FORM_FILLING);

            PdfAcroForm form = PdfAcroForm.getAcroForm(signer.getDocument(), true);
            form.getField(name).setReadOnly(true);

            PrivateKeySignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            // Sign the document using the detached mode, CMS or CAdES equivalent.
            signer.signDetached(digest, pks, chain, null, null, null,
                    0, PdfSigner.CryptoStandard.CMS);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void fillOutAndSign(String keystore, String src, String name, String fname, String value,
                                      String dest) {
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
            form.getField(name).setReadOnly(true);
            form.getField(fname).setReadOnly(true);

            PrivateKeySignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            signer.signDetached(digest, pks, chain, null, null, null,
                    0, PdfSigner.CryptoStandard.CMS);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void fillOut(String src, String dest, String name, String value) {
        try {
            PdfDocument pdfDoc = new PdfDocument(new PdfReader(src), new PdfWriter(dest),
                    new StampingProperties().useAppendMode());

            PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, true);
            form.getField(name).setValue(value);

            pdfDoc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        File file = new File(DEST);
        file.mkdirs();

        Security.addProvider(provider);

        createForm();

        certify(ALICE, SRC, "sig1", DEST + "step_1_signed_by_alice.pdf");

        fillOutAndSign(BOB, DEST + "step_1_signed_by_alice.pdf", "sig2", "approved_bob",
                "Read and Approved by Bob", DEST + "step_2_signed_by_alice_and_bob.pdf");
        fillOutAndSign(CAROL, DEST + "step_2_signed_by_alice_and_bob.pdf", "sig3", "approved_carol",
                "Read and Approved by Carol", DEST + "step_3_signed_by_alice_bob_and_carol.pdf");
        fillOutAndSign(DAVE, DEST + "step_3_signed_by_alice_bob_and_carol.pdf", "sig4", "approved_dave",
                "Read and Approved by Dave", DEST + "step_4_signed_by_alice_bob_carol_and_dave.pdf");
        fillOut(DEST + "step_1_signed_by_alice.pdf",DEST+"step_5_signed_by_alice_and_bob_broken_by_chuck.pdf",
                "approved_bob", "Changed by Chuck");
        fillOut(DEST + "step_3_signed_by_alice_bob_and_carol.pdf",DEST+"step_6_signed_by_dave_broken_by_chuck.pdf",
                "approved_carol", "Changed by Chuck");
    }
}
