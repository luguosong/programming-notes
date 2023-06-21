package com.luguosong._03_behavioral._11_visitor;

/**
 * @author luguosong
 */
public class VisitorExample {
    public static void main(String[] args) {
        Element elementA = new ConcreteElementA();
        Element elementB = new ConcreteElementB();

        Visitor visitor = new ConcreteVisitor();

        elementA.accept(visitor); // 客户端通过抽象接口与元素进行交互
        elementB.accept(visitor);
    }

    // 访问者接口
    static interface Visitor {
        void visit(ConcreteElementA element);

        void visit(ConcreteElementB element);
    }

    // 具体访问者
    static class ConcreteVisitor implements Visitor {
        @Override
        public void visit(ConcreteElementA element) {
            System.out.println("访问者正在访问ConcreteElementA:x=" + element.x + ",y=" + element.y);
            // 对ConcreteElementA的访问操作和算法
            element.operationA();
        }

        @Override
        public void visit(ConcreteElementB element) {
            System.out.println("访问者正在访问ConcreteElementB:x=" + element.x);
            // 对ConcreteElementB的访问操作和算法
            element.operationB();
        }
    }

    // 元素接口
    static interface Element {
        /**
         * 接受访问者的访问
         *
         * @param visitor 访问者
         */
        void accept(Visitor visitor);
    }

    // 具体元素A
    static class ConcreteElementA implements Element {

        int x = 0;
        int y = 0;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        void operationA() {
            // 具体元素A的操作
        }
    }

    // 具体元素B
    static class ConcreteElementB implements Element {
        int x = 1;

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        void operationB() {
            // 具体元素B的操作
        }
    }
}
