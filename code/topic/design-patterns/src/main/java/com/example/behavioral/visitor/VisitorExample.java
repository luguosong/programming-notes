package com.example.behavioral.visitor;

import java.util.List;

/**
 * 访问者模式 - 正例
 * 新增操作只需新建访问者类，文件类不需要改动
 */
public class VisitorExample {
    public static void main(String[] args) {
        List<FileNode> files = List.of(
            new ImageFile("photo.jpg",   1024),
            new TextFile("readme.txt",   256),
            new ImageFile("banner.png",  512)
        );

        // ✅ 用不同访问者执行不同操作，文件类不变
        FileVisitor sizeCalc  = new SizeCalculator();
        FileVisitor compressor = new Compressor();

        files.forEach(f -> f.accept(sizeCalc));
        System.out.println("总大小: " + ((SizeCalculator) sizeCalc).getTotalSize() + " KB");

        System.out.println("--- 压缩 ---");
        files.forEach(f -> f.accept(compressor));

        // ✅ 新增"加密"操作：只需 class EncryptVisitor implements FileVisitor {...}
    }
}

// 节点接口：接受访问者
interface FileNode {
    void accept(FileVisitor visitor);
    long getSize();
}

// 具体节点：图片文件
class ImageFile implements FileNode {
    private final String name;
    private final long   size;

    public ImageFile(String name, long size) { this.name = name; this.size = size; }

    @Override public void accept(FileVisitor visitor) { visitor.visitImage(this); }
    @Override public long getSize() { return size; }
    public String getName() { return name; }
}

// 具体节点：文本文件
class TextFile implements FileNode {
    private final String name;
    private final long   size;

    public TextFile(String name, long size) { this.name = name; this.size = size; }

    @Override public void accept(FileVisitor visitor) { visitor.visitText(this); }
    @Override public long getSize() { return size; }
    public String getName() { return name; }
}

// 访问者接口
interface FileVisitor {
    void visitImage(ImageFile imageFile);
    void visitText(TextFile textFile);
}

// ✅ 具体访问者：计算大小
class SizeCalculator implements FileVisitor {
    private long totalSize = 0;

    @Override public void visitImage(ImageFile f) { totalSize += f.getSize(); }
    @Override public void visitText(TextFile f)   { totalSize += f.getSize(); }

    public long getTotalSize() { return totalSize; }
}

// ✅ 具体访问者：压缩文件（新增操作，不改文件类）
class Compressor implements FileVisitor {
    @Override
    public void visitImage(ImageFile f) {
        System.out.println("[图片压缩] " + f.getName() + " 使用 JPEG 压缩");
    }

    @Override
    public void visitText(TextFile f) {
        System.out.println("[文本压缩] " + f.getName() + " 使用 GZIP 压缩");
    }
}
