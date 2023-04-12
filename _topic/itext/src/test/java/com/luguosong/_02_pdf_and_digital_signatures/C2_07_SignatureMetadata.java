package com.luguosong._02_pdf_and_digital_signatures;

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
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
 * 设置签名字典的元数据
 *
 * @author luguosong
 */
public class C2_07_SignatureMetadata {

    public static final String DEST = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/07_SignatureMetadata/";

    public static final String SRC = DEST + "empty_signature_form_field.pdf";

    public static final String SIGNAME = "Signature1";
    public static void main(String[] args) {
        PdfReader reader = null;
        try {
            File file = new File(DEST);
            file.mkdirs();

            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);

            reader = new PdfReader(SRC);
            PdfSigner signer = new PdfSigner(reader, new FileOutputStream(DEST+"field_metadata.pdf"), new StampingProperties());

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            appearance.setReason("Test metadata");
            appearance.setLocation("Ghent");
            appearance.setContact("555 123 456");

            signer.setFieldName(SIGNAME);

            //向签名字典中添加自定义元数据
            signer.setSignatureEvent(
                    new PdfSigner.ISignatureEvent() {
                        @Override
                        public void getSignatureDictionary(PdfSignature sig) {
                            sig.put(PdfName.Name, new PdfString("Bruno L. Specimen"));
                        }
                    }
            );

            PrivateKeySignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), DigestAlgorithms.SHA256, provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            //使用分离模式、CMS或CAdES等效模式签署文件。
            signer.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, PdfSigner.CryptoStandard.CMS);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
