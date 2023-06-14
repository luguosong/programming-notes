package com.luguosong._02_structural._03_composite;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luguosong
 */
public class CompositeExample {
    public static void main(String[] args) {
        // 创建叶节点
        Component leaf1 = new Leaf();
        Component leaf2 = new Leaf();

        // 创建容器，并添加叶节点
        Container container1 = new Container();
        container1.addComponent(leaf1);
        container1.addComponent(leaf2);

        // 创建另一个叶节点
        Component leaf3 = new Leaf();

        // 创建容器，并添加另一个叶节点和之前的容器
        Container container2 = new Container();
        container2.addComponent(leaf3);
        container2.addComponent(container1);

        // 执行容器操作，会递归执行包含的所有叶节点操作
        container2.operation("");
    }

    // 组件接口
    static interface Component {
        void operation(String prefix);
    }

    // 叶节点
    static class Leaf implements Component {
        @Override
        public void operation(String prefix) {
            System.out.println(prefix + "-📄叶节点操作");
        }
    }

    // 容器
    static class Container implements Component {
        private List<Component> components = new ArrayList<>();

        public void addComponent(Component component) {
            components.add(component);
        }

        public void removeComponent(Component component) {
            components.remove(component);
        }

        @Override
        public void operation(String prefix) {
            System.out.println(prefix + "-📁执行容器操作");
            for (Component component : components) {
                component.operation(prefix + " ");
            }
        }
    }
}
