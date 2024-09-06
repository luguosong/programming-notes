package com.luguosong.chapter02_pdf_and_digital_signatures;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

/**
 * @author luguosong
 */
public class SignHelloWorldWithTempFile {
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // 设置BC库
        BouncyCastleProvider provider = new BouncyCastleProvider();
        Security.addProvider(provider);

        //初始化密钥库
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream("docs/topics/itext-signatures/src/main/resources/ks"), "12345678".toCharArray());

        //获取私钥和证书
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, "12345678".toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);

        //读取待签名文件
        PdfReader reader = new PdfReader("hello.pdf");
        /*
         * 创建签章处理对象
         *
         * ⭐参数三设置输出暂时写入的文件，用于大文件签章
         * */
        PdfSigner signer = new PdfSigner(
                reader,
                new FileOutputStream("docs/topics/itext-signatures/src/main/resources/C2_02_SignHelloWorldWithTempFile/hello_signed.pdf"),
                "docs/topics/itext-signatures/src/main/resources/C2_02_SignHelloWorldWithTempFile",
                new StampingProperties());
        signer
                .setReason("Test") //设置签名原因。
                .setLocation("Ghent") //设置签名地点。
                .setPageRect(new Rectangle(36, 648, 200, 100)) //设置签名区域。
                .setPageNumber(1) //设置签名页码。
                .setFieldName("sig"); //设置指示要签名的字段的名称。
        //指示现有的外观是否需要作为背景重新使用。
        signer.getSignatureField().setReuseAppearance(false);

        /*
         * 参数一：一个 PrivateKey 对象
         * 参数二：一个哈希算法（例如 "SHA-1", "SHA-256",...）。
         * 参数三：一个安全提供者（例如 "BC"）。
         * */
        IExternalSignature pks = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, provider.getName());
        //摘要的实现
        IExternalDigest digest = new BouncyCastleDigest();

        /*
         * 参数一：提供摘要的实现
         * 参数二：提供实际签名的接口
         * 参数三：证书链
         * 参数四：CRL列表
         * 参数五：OCSP客户端
         * 参数六：时间戳客户端
         * 参数七：为签名保留的大小。如果为0则会进行估算
         * 参数八：Signature. CMS或Signature. CADES
         * */
        signer.signDetached(digest, pks, chain, null, null, null, 0, PdfSigner.CryptoStandard.CMS);
    }
}
