package com.luguosong._01_creational._03_builder;

/**
 * 建造者模式
 *
 * @author luguosong
 */
public class BuilderExample {
    public static void main(String[] args) {

        /*
        * 采用相同的主管类，不同的具体生成器进行测试
        * */

        Builder builder1 = new ConcreteBuilder1();
        Director director1 = new Director(builder1);
        director1.construct();
        Product product1 = builder1.getResult();
        System.out.println("Product 1 Parts: " + product1.getPart1() + ", " + product1.getPart2() + ", " + product1.getPart3());

        Builder builder2 = new ConcreteBuilder2();
        Director director2 = new Director(builder2);
        director2.construct();
        Product product2 = builder2.getResult();
        System.out.println("Product 2 Parts: " + product2.getPart1() + ", " + product2.getPart2() + ", " + product2.getPart3());
    }

    // 产品类
    static class Product {
        private String part1;
        private String part2;
        private String part3;

        public void setPart1(String part1) {
            this.part1 = part1;
        }

        public void setPart2(String part2) {
            this.part2 = part2;
        }

        public void setPart3(String part3) {
            this.part3 = part3;
        }

        public String getPart1() {
            return part1;
        }

        public String getPart2() {
            return part2;
        }

        public String getPart3() {
            return part3;
        }
    }

    // 抽象生成器接口
    static interface Builder {
        void buildPart1();

        void buildPart2();

        void buildPart3();

        Product getResult();
    }

    // 具体生成器实现 1
    static class ConcreteBuilder1 implements Builder {
        private Product product;

        public ConcreteBuilder1() {
            this.product = new Product();
        }

        public void buildPart1() {
            product.setPart1("使用生成器1生成Part 1 ");
        }

        public void buildPart2() {
            product.setPart2("使用生成器1生成Part 2 ");
        }

        public void buildPart3() {
            product.setPart3("使用生成器1生成Part 3 ");
        }

        public Product getResult() {
            return product;
        }
    }

    // 具体生成器实现 2
    static class ConcreteBuilder2 implements Builder {
        private Product product;

        public ConcreteBuilder2() {
            this.product = new Product();
        }

        public void buildPart1() {
            product.setPart1("使用生成器2生成Part 1 ");
        }

        public void buildPart2() {
            product.setPart2("使用生成器2生成Part 2 ");
        }

        public void buildPart3() {
            product.setPart3("使用生成器2生成Part 3 ");
        }

        public Product getResult() {
            return product;
        }
    }

    // 主管类,负责装配
    static class Director {
        private Builder builder;

        public Director(Builder builder) {
            this.builder = builder;
        }

        /**
         * ⭐组装
         *
         * 这个方法其实也可以加入到抽象生成器接口中，这样就可以不用主管类了。但这样做有点不符合单一原则
         */
        public void construct() {
            builder.buildPart1();
            builder.buildPart2();
            builder.buildPart3();
        }
    }
}
