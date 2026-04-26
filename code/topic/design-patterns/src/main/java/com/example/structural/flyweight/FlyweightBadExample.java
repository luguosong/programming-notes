package com.example.structural.flyweight;

/**
 * 享元模式 - 反例
 * 问题：每棵树对象都独立存储纹理、颜色等重复数据，内存浪费严重
 */
public class FlyweightBadExample {
    public static void main(String[] args) {
        // ❌ 10000 棵树，每棵都存储相同的纹理数据
        TreeBad[] forest = new TreeBad[10_000];
        for (int i = 0; i < forest.length; i++) {
            // 每个对象都独立持有 texture 字符串（假设很大）
            forest[i] = new TreeBad(i * 10, i * 5, "oak", "#228B22", "oak_texture_data_HUGE_STRING");
        }
        System.out.println("❌ 创建了 " + forest.length + " 棵树，每棵都存储重复纹理数据");
        System.out.println("最后一棵：" + forest[9999]);
    }
}

// ❌ 外在状态和内在状态都存在同一对象里，内存浪费
class TreeBad {
    // 外在状态（每棵树不同，合理）
    private final int    x;
    private final int    y;
    // 内在状态（所有同类树都相同，重复！）
    private final String species;    // 树种
    private final String color;      // 颜色
    private final String texture;    // 纹理数据（可能很大）

    public TreeBad(int x, int y, String species, String color, String texture) {
        this.x       = x;
        this.y       = y;
        this.species = species;
        this.color   = color;
        this.texture = texture;
    }

    @Override public String toString() {
        return "Tree{x=" + x + ", y=" + y + ", species=" + species + "}";
    }
}
