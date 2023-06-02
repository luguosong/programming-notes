package com.luguosong._01_creational._05_prototype;

/**
 * 原型设计模式：浅克隆
 *
 * @author luguosong
 */
public class PrototypeExample {
    public static void main(String[] args) throws CloneNotSupportedException {
        //创建原始对象
        ConcretePrototype primitiveObject = new ConcretePrototype("张三", new Member("hello"));
        ConcretePrototype prototypeObject = primitiveObject.clone();
        System.out.println(prototypeObject.name);
        //结果为ture，浅克隆，对象类型只复制引用
        System.out.println(primitiveObject.member == prototypeObject.member);
    }


    /**
     * 为了演示JDK自带的clone方法不会创建新的对象，只是复制引用
     */
    static class Member {
        String parameter;

        public Member(String parameter) {
            this.parameter = parameter;
        }
    }

    /**
     * 具体原型
     */
    static class ConcretePrototype implements Cloneable {
        String name;

        Member member;

        public ConcretePrototype(String name, Member member) {
            this.name = name;
            this.member = member;
        }

        /**
         * ⭐重写JDK自带的clone原生方法
         *
         * @return
         */
        @Override
        public ConcretePrototype clone() {
            try {
                return (ConcretePrototype) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
