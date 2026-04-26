package com.example.structural.flyweight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 享元模式 - 正例
 * 将"内在状态"（树种、颜色、纹理）提取为共享的 TreeType 对象，
 * Tree 只保存"外在状态"（坐标），大幅降低内存占用
 */
public class FlyweightExample {
    public static void main(String[] args) {
        TreeTypeFactory factory = new TreeTypeFactory();
        Forest forest = new Forest();

        // ✅ 10000 棵橡树共享同一个 TreeType 对象
        for (int i = 0; i < 10_000; i++) {
            forest.plant(i * 10, i * 5, "oak", "#228B22", "oak_texture", factory);
        }
        // 再加 5000 棵松树，也共享各自的 TreeType
        for (int i = 0; i < 5_000; i++) {
            forest.plant(i * 8, i * 6, "pine", "#1B5E20", "pine_texture", factory);
        }

        System.out.println("✅ 树对象总数: " + forest.size());
        System.out.println("✅ TreeType（共享）对象数量: " + factory.typeCount()); // 只有 2 个！
        forest.get(0).draw();
    }
}

// 享元对象：只存储内在状态（可共享，不随每棵树变化）
class TreeType {
    private final String species;   // 树种
    private final String color;     // 颜色
    private final String texture;   // 纹理（内存大户）

    public TreeType(String species, String color, String texture) {
        this.species = species;
        this.color   = color;
        this.texture = texture;
    }

    // 渲染时接收外在状态（坐标）
    public void draw(int x, int y) {
        System.out.println("绘制 " + species + " 树 at (" + x + "," + y
                + ") color=" + color);
    }
}

// 享元工厂：确保相同内在状态只创建一个 TreeType
class TreeTypeFactory {
    private final Map<String, TreeType> cache = new HashMap<>();

    public TreeType get(String species, String color, String texture) {
        String key = species + "_" + color;
        return cache.computeIfAbsent(key, k -> {
            System.out.println("✅ 创建新 TreeType: " + key);
            return new TreeType(species, color, texture);
        });
    }

    public int typeCount() { return cache.size(); }
}

// 上下文对象：只存储外在状态（坐标）
class Tree {
    private final int      x;
    private final int      y;
    private final TreeType type; // 引用共享的享元对象

    public Tree(int x, int y, TreeType type) {
        this.x    = x;
        this.y    = y;
        this.type = type;
    }

    public void draw() { type.draw(x, y); }
}

// 客户端：管理大量树对象
class Forest {
    private final List<Tree> trees = new ArrayList<>();

    public void plant(int x, int y, String species, String color,
                      String texture, TreeTypeFactory factory) {
        TreeType type = factory.get(species, color, texture); // 从工厂获取共享对象
        trees.add(new Tree(x, y, type));
    }

    public int  size()         { return trees.size(); }
    public Tree get(int index) { return trees.get(index); }
}
