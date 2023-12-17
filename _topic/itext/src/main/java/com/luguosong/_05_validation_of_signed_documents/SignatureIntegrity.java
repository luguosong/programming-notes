package com.luguosong._05_validation_of_signed_documents;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.EncryptionAlgorithms;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * @author luguosong
 */
public class SignatureIntegrity {

    public void verifySignatures(String path) throws IOException, GeneralSecurityException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(path));
        SignatureUtil signUtil = new SignatureUtil(pdfDoc);
        List<String> names = signUtil.getSignatureNames();

        System.out.println(path);
        for (String name : names) {
            System.out.println("===== " + name + " =====");
            verifySignature(signUtil, name);
        }

        pdfDoc.close();
    }

    public PdfPKCS7 verifySignature(SignatureUtil signUtil, String name) throws GeneralSecurityException {
        PdfPKCS7 pkcs7 = signUtil.readSignatureData(name);

        System.out.println("签名覆盖整个文档: " + signUtil.signatureCoversWholeDocument(name));
        System.out.println("文档修订: " + signUtil.getRevision(name) + " of " + signUtil.getTotalRevisions());
        System.out.println("完整性检查通过？" + pkcs7.verifySignatureIntegrityAndAuthenticity());

        return pkcs7;
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException, NoSuchFieldException, IllegalAccessException {
        // 添加BouncyCastleProvider
        Security.addProvider(new BouncyCastleProvider());

        /*
         * 通过反射让itext库支持国密算法
         * */
        Field digestNamesField = DigestAlgorithms.class.getDeclaredField("digestNames");
        digestNamesField.setAccessible(true);
        HashMap<String, String> digestNames = (HashMap<String, String>) digestNamesField.get(null);
        digestNames.put("1.2.156.10197.1.401", "SM3");

        Field algorithmNamesField = EncryptionAlgorithms.class.getDeclaredField("algorithmNames");
        algorithmNamesField.setAccessible(true);
        HashMap<String, String> algorithmNames = (HashMap<String, String>) algorithmNamesField.get(null);
        algorithmNames.put("1.2.156.10197.1.501", "SM2");

        Scanner scanner = new Scanner(System.in);
        String input;
        SignatureIntegrity app = new SignatureIntegrity();
        while (true) {
            System.out.print("请输入文件路径（输入 'exit' 退出）: ");
            input = scanner.nextLine();
            // 检查输入是否为 'exit'，如果是则退出循环
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            // 在这里你可以使用输入的文件路径进行处理，例如读取文件等操作
            app.verifySignatures(input);
            System.out.println("程序已退出。");
        }
        scanner.close();
    }
}
