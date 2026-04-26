package com.example.behavioral.visitor;

/**
 * 访问者模式 - 反例
 * 问题：新增操作（如压缩）需要修改所有文件类，违反开闭原则
 */
public class VisitorBadExample {
    public static void main(String[] args) {
        ImageFileBad img  = new ImageFileBad("photo.jpg", 1024);
        TextFileBad  text = new TextFileBad("readme.txt", 256);

        System.out.println("总大小: " + (img.getSize() + text.getSize()) + " KB");
        img.compress();
        text.compress();
        // 新增"加密"操作？要在 ImageFileBad 和 TextFileBad 里各自新增方法 ❌
    }
}

class ImageFileBad {
    private final String name;
    private final long   size;

    public ImageFileBad(String name, long size) { this.name = name; this.size = size; }

    public long getSize() { return size; }

    // ❌ 操作方法硬编码在文件类里
    public void compress() {
        System.out.println("[图片压缩] " + name + " 使用 JPEG 压缩");
    }
}

class TextFileBad {
    private final String name;
    private final long   size;

    public TextFileBad(String name, long size) { this.name = name; this.size = size; }

    public long getSize() { return size; }

    // ❌ 每种操作都要在所有文件类里重复添加
    public void compress() {
        System.out.println("[文本压缩] " + name + " 使用 GZIP 压缩");
    }
}
