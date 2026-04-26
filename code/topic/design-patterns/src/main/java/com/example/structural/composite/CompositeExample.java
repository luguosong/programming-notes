package com.example.structural.composite;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合模式 - 正例
 * 文件和目录实现同一接口，客户端无需区分，递归操作自然统一
 */
public class CompositeExample {
    public static void main(String[] args) {
        // ✅ 构造文件系统树（叶子和容器都是 FileSystemNode）
        FileSystemNode root = new Directory("root");
        root.add(new FileLeaf("a.txt", 100));

        FileSystemNode docs = new Directory("docs");
        docs.add(new FileLeaf("b.pdf", 500));
        docs.add(new FileLeaf("c.md",  200));
        root.add(docs);

        // ✅ 对 root 调用 getSize()，不需要 instanceof，多态自动处理
        System.out.println("总大小: " + root.getSize() + " bytes"); // 800
        root.print("");
    }
}

// 统一组件接口
interface FileSystemNode {
    long   getSize();
    void   print(String indent);
    default void add(FileSystemNode node) { throw new UnsupportedOperationException(); }
}

// 叶子节点：文件
class FileLeaf implements FileSystemNode {
    private final String name;
    private final long   size;

    public FileLeaf(String name, long size) { this.name = name; this.size = size; }

    @Override public long getSize()              { return size; }
    @Override public void print(String indent)   { System.out.println(indent + "📄 " + name + " (" + size + "B)"); }
}

// 容器节点：目录（可包含任意 FileSystemNode）
class Directory implements FileSystemNode {
    private final String              name;
    private final List<FileSystemNode> children = new ArrayList<>();

    public Directory(String name) { this.name = name; }

    @Override
    public void add(FileSystemNode node) { children.add(node); }

    @Override
    public long getSize() {
        // ✅ 对所有子节点调用同一接口，无需 instanceof
        return children.stream().mapToLong(FileSystemNode::getSize).sum();
    }

    @Override
    public void print(String indent) {
        System.out.println(indent + "📁 " + name + " (" + getSize() + "B)");
        children.forEach(c -> c.print(indent + "  "));
    }
}
