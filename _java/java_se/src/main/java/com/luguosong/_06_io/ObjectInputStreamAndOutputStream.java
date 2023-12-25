package com.luguosong._06_io;

import java.io.*;
import java.util.Date;

/**
 * 对象流读写文件
 *
 * @author luguosong
 */
public class ObjectInputStreamAndOutputStream {

    /*
     * ❗❗❗对象流读写的对象必须实现Serializable接口
     * */
    static class Person implements Serializable {

        /*
         * ⭐全局常量，当类的属性发生变化时，让程序在反序列化时认出是当初的类而不至于报错
         * */
        private static final long serialVersionUID = 1L;

        String name;

        /*
        * ⭐使用transient修饰的属性不会被序列化
        * */
        transient int age;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    '}';
        }
    }

    public static void main(String[] args) {
        File file = new File("_java/java_se/src/main/resources/io/object_" + new Date().getTime() + "temp.txt");

        ObjectInputStream objectInputStream = null;
        ObjectOutputStream objectOutputStream = null;

        try {
            /*
             * 写文件
             * */
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
            objectOutputStream.writeInt(12); //对象流也可以写入基本类型
            objectOutputStream.writeObject(new Person("luguosong", 18)); //对象流写入对象
            objectOutputStream.flush();

            /*
             * 读文件
             * */
            objectInputStream = new ObjectInputStream(new FileInputStream(file));
            System.out.println(objectInputStream.readInt()); //对象流也可以读取基本类型
            Person person = (Person) objectInputStream.readObject(); //对象流读取对象
            System.out.println(person);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (objectInputStream != null) {
                    objectInputStream.close();
                }
                if (objectOutputStream != null) {
                    objectOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

/*
12
Person{name='luguosong', age=0}
* */
