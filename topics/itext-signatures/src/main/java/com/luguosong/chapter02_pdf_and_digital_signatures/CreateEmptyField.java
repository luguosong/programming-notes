package com.luguosong.chapter02_pdf_and_digital_signatures;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.SignatureFormFieldBuilder;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.FileNotFoundException;

/**
 * @author luguosong
 */
public class CreateEmptyField {
    /**
     * 该函数用于生成一个带有空白签名域的PDF文档。
     *
     * @param args 命令行参数（未使用）
     * @throws FileNotFoundException 如果指定的文件不存在
     */
    public static void main(String[] args) throws FileNotFoundException {
        //创建一个 Document 对象
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter("docs/topics/itext-signatures/src/main/resources/C2_04_CreateEmptyField/hello_empty.pdf"));
        Document doc = new Document(pdfDoc);

        doc.add(new Paragraph("Hello World!"));

        //创建签名表单字段
        PdfFormField field =
                new SignatureFormFieldBuilder(pdfDoc, "签名域1")
                        .setWidgetRectangle(new Rectangle(72, 632, 200, 100))
                        .createSignature();
        field.getFirstFormAnnotation().setPage(1);

        //设置小部件属性
        field.getWidgets().get(0).setHighlightMode(PdfAnnotation.HIGHLIGHT_INVERT).setFlags(PdfAnnotation.PRINT);

        //获取签名域的小部件外观特征，并如果不存在则创建一个新的特征字典。
        PdfDictionary mkDictionary = field.getWidgets().get(0).getAppearanceCharacteristics();
        if (null == mkDictionary) {
            mkDictionary = new PdfDictionary();
        }

        //设置签名域的背景颜色
        PdfArray black = new PdfArray();
        black.add(new PdfNumber(ColorConstants.BLACK.getColorValue()[0]));
        black.add(new PdfNumber(ColorConstants.BLACK.getColorValue()[1]));
        black.add(new PdfNumber(ColorConstants.BLACK.getColorValue()[2]));
        mkDictionary.put(PdfName.BC, black);
        //设置了签名域小部件的外观特征
        field.getWidgets().get(0).setAppearanceCharacteristics(mkDictionary);

        //将 签名表单字段 添加到pdfDoc这个PDF文档的AcroForm中。
        PdfAcroForm.getAcroForm(pdfDoc, true).addField(field);

        //创建一个带有签名提示文本的签名域背景
        Rectangle rect = new Rectangle(0, 0, 200, 100);
        PdfFormXObject xObject = new PdfFormXObject(rect);
        PdfCanvas canvas = new PdfCanvas(xObject, pdfDoc);
        canvas
                .setStrokeColor(ColorConstants.BLUE)
                .setFillColor(ColorConstants.LIGHT_GRAY)
                .rectangle(0 + 0.5, 0 + 0.5, 200 - 0.5, 100 - 0.5)
                .fillStroke()
                .setFillColor(ColorConstants.BLUE);
        new Canvas(canvas, rect).showTextAligned("SIGN HERE", 100, 50,
                TextAlignment.CENTER, (float) Math.toRadians(25));
        field.getWidgets().get(0).setNormalAppearance(xObject.getPdfObject());

        doc.close();
    }
}
