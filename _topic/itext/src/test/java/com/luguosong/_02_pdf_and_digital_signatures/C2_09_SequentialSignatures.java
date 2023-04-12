package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.renderer.CellRenderer;
import com.itextpdf.layout.renderer.DrawContext;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.security.Security;

/**
 * @author luguosong
 */
public class C2_09_SequentialSignatures {
    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/09_SequentialSignatures/";

    public static final String SRC = DEST + "empty_document.pdf";

    private static BouncyCastleProvider provider = new BouncyCastleProvider();


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

    public static void main(String[] args) {
        //创建目标文件夹
        new File(DEST).mkdirs();
        //添加BouncyCastleProvider
        Security.addProvider(provider);

        //创建带有三个签名域的空白PDF文档
        createForm();
    }
}
