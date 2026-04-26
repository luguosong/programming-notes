package com.example.structural.composite;

/**
 * 组合模式 - 反例
 * 问题：用 instanceof 判断是文件还是目录，新增节点类型需要修改所有这样的方法
 */
public class CompositeBadExample {
    public static void main(String[] args) {
        // 构造文件系统树
        DirectoryBad root = new DirectoryBad("root");
        root.addChild(new FileBad("a.txt", 100));
        DirectoryBad docs = new DirectoryBad("docs");
        docs.addChild(new FileBad("b.pdf", 500));
        docs.addChild(new FileBad("c.md",  200));
        root.addChild(docs);

        System.out.println("总大小: " + calculateSize(root) + " bytes");
    }

    // ❌ 使用 instanceof 区分叶子和容器，扩展性差
    static long calculateSize(Object node) {
        if (node instanceof FileBad f) {
            return f.getSize();
        } else if (node instanceof DirectoryBad d) {
            long total = 0;
            for (Object child : d.getChildren()) {
                total += calculateSize(child); // 递归时仍需 instanceof
            }
            return total;
        }
        return 0; // ❌ 新增类型就得修改这里
    }
}

// 叶子节点
class FileBad {
    private final String name;
    private final long   size;
    public FileBad(String name, long size) { this.name = name; this.size = size; }
    public long getSize() { return size; }
    public String getName() { return name; }
}

// 容器节点（与 FileBad 无公共接口）
class DirectoryBad {
    private final String        name;
    private final java.util.List<Object> children = new java.util.ArrayList<>();
    public DirectoryBad(String name) { this.name = name; }
    public void addChild(Object child)           { children.add(child); }
    public java.util.List<Object> getChildren()  { return children;     }
    public String getName()                       { return name;         }
}
