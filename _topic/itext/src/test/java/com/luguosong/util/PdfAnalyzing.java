package com.luguosong.util;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;
import javafx.stage.DirectoryChooser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author luguosong
 */
public class PdfAnalyzing {

    private static final String src="E:\\IdeaCode\\programming-learning\\_topic\\itext\\src\\test\\resources\\02_pdf_and_digital_signatures\\06_SignatureAppearances\\signature_appearance_1.pdf";
    public static void main(String[] args) {
        try {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);

            PdfDocument document = new PdfDocument(new PdfReader(src));
            SignatureUtil signUtil = new SignatureUtil(document);
            List<String> names = signUtil.getSignatureNames();


            for (String name : names) {
                System.out.println("===== " + name + " =====");
                PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);
                System.out.println("签名覆盖整个文档: " + signUtil.signatureCoversWholeDocument(name));
                System.out.println("文档修订: " + signUtil.getRevision(name) + " of " + signUtil.getTotalRevisions());
                System.out.println("完整性检查正常? " + pkcs7.verifySignatureIntegrityAndAuthenticity());
            }
            document.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
