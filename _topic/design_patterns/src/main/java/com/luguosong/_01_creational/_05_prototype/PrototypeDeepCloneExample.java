package com.luguosong._01_creational._05_prototype;

import java.io.*;

/**
 * 深克隆
 *
 * @author luguosong
 */
public class PrototypeDeepCloneExample {
    public static void main(String[] args) throws CloneNotSupportedException {
        //创建原始对象
        ConcretePrototype primitiveObject = new ConcretePrototype("张三", new Member("hello"));
        ConcretePrototype prototypeObject = primitiveObject.clone();
        System.out.println(prototypeObject.name);
        //结果为false，深克隆
        System.out.println(primitiveObject.member == prototypeObject.member); //false
    }


    /**
     * 为了演示深克隆
     */
    static class Member implements Serializable {
        String parameter;

        public Member(String parameter) {
            this.parameter = parameter;
        }
    }

    /**
     * 具体原型
     */
    static class ConcretePrototype implements Cloneable, Serializable {
        String name;

        Member member;

        public ConcretePrototype(String name, Member member) {
            this.name = name;
            this.member = member;
        }

        /**
         * ⭐深克隆
         *
         * @return
         */
        @Override
        public ConcretePrototype clone() {

            try {
                //将对象写入流中
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                new ObjectOutputStream(bos).writeObject(this);

                //将对象从流中取出
                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                return (ConcretePrototype) new ObjectInputStream(bis).readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
