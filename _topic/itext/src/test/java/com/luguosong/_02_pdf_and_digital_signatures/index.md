---
layout: note
title: PDF和数字签名
nav_order: 20
parent: iText 7
---

# 签名Hello World

```java
/**
 * 添加可见签名到文档的简单示例
 *
 * @author luguosong
 */
public class C2_01_SignHelloWorld {
    public static void sign(String path,
                            PdfSigner.CryptoStandard signatureType,
                            String digestAlgorithm) {
        try {
            BouncyCastleProvider provider = new BouncyCastleProvider();
            Security.addProvider(provider);
            PdfReader pdfReader = new PdfReader("02_pdf_and_digital_signatures/01_SignHelloWorld/empty_document.pdf");
            PdfSigner pdfSigner = new PdfSigner(pdfReader,
                    // 创建输出流，用于将签署后的PDF文档写入文件
                    Files.newOutputStream(Paths.get(path)),
                    // 创建StampingProperties对象，用于设置签名属性
                    new StampingProperties());
            // 创建签名外观
            Rectangle rect = new Rectangle(36, 648, 200, 100);
            PdfSignatureAppearance appearance = pdfSigner.getSignatureAppearance();
            appearance.setReason("yuanyin")
                    .setLocation("weizhi")
                    //指定是否将签名字段之前的外观用作已签名字段的背景。 “false”值是默认值。
                    .setReuseAppearance(false)
                    .setPageRect(rect)
                    .setPageNumber(1);
            pdfSigner.setFieldName("sig");
            IExternalSignature pks = new PrivateKeySignature(KeyStoreUtil.getPrivateKey(), digestAlgorithm, new BouncyCastleProvider().getName());
            IExternalDigest digest = new BouncyCastleDigest();

            pdfSigner.signDetached(digest, pks, KeyStoreUtil.getCertificates(), null, null, null, 0, signatureType);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        String outFolder = "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/01_SignHelloWorld/";
        sign(outFolder + "hello_signed1.pdf", PdfSigner.CryptoStandard.CMS, DigestAlgorithms.SHA256);
        sign(outFolder + "hello_signed2.pdf", PdfSigner.CryptoStandard.CMS, DigestAlgorithms.SHA512);
        sign(outFolder + "hello_signed3.pdf", PdfSigner.CryptoStandard.CADES, DigestAlgorithms.SHA256);
        sign(outFolder + "hello_signed4.pdf", PdfSigner.CryptoStandard.CADES, DigestAlgorithms.RIPEMD160);
    }

}
```

# 签署大型PDF文件

![](https://cdn.jsdelivr.net/gh/guosonglu/images@master/blog-img/20230406143257.png)

``` java
PdfSigner pdfSigner = new PdfSigner(pdfReader,
                    // 创建输出流，用于将签署后的PDF文档写入文件
                    Files.newOutputStream(Paths.get("_topic/itext/src/test/resources/02_pdf_and_digital_signatures/02_SignHelloWorldWithTempFile/hello_signed_with_temp.pdf")),
                    //*****增加临时写入的文件*****
                    "_topic/itext/src/test/resources/02_pdf_and_digital_signatures/02_SignHelloWorldWithTempFile/",
                    // 创建StampingProperties对象，用于设置签名属性
                    new StampingProperties());
```

# 创建和签署签名字段

```java
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
```

# 创建不同的签名外观

## 早期PDF绘制

- `n0`-背景层。
- `n1`-有效性层，用于未知和有效状态。(不再使用)
- `n2`-签名外观，包含关于签名的信息。
- `n3`-有效性层，用于无效状态。(不再使用)
- `n4`-文本层，用于签名状态的文本表示。(不再使用)

{: .warning}
> 自Acrobat 6 (2003)以来，不再推荐使用层`n1`、`n3`和`n4`。

