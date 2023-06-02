package com.luguosong._01_creational._06_singleton;

/**
 * 单例模式
 *
 * @author luguosong
 */
public class SingletonExample {
    public static void main(String[] args) {
        /*
         * 饿汉式单例
         * */
        System.out.println(Singleton1.getInstance() == Singleton1.getInstance());


        /*
         * 线程不安全的懒汉式单例
         * */
        new Thread(() -> {
            System.out.println("Singleton2:" + Singleton2.getInstance());
        }).start();
        new Thread(() -> {
            System.out.println("Singleton2:" + Singleton2.getInstance());
        }).start();


        /*
         * 线程安全的懒汉式单例
         * */
        new Thread(() -> {
            System.out.println("Singleton3:" + Singleton3.getInstance());
        }).start();
        new Thread(() -> {
            System.out.println("Singleton3:" + Singleton3.getInstance());
        }).start();

        /*
         * 线程安全双重检查的懒汉式单例
         * */
        new Thread(() -> {
            System.out.println("Singleton4:" + Singleton4.getInstance());
        }).start();
        new Thread(() -> {
            System.out.println("Singleton4:" + Singleton4.getInstance());
        }).start();

        //静态内部类实现懒汉式
        new Thread(() -> {
            System.out.println("Singleton5:" + Singleton5.getInstance());
        }).start();
        new Thread(() -> {
            System.out.println("Singleton5:" + Singleton5.getInstance());
        }).start();

        //枚举类型实现单例
        System.out.println(Singleton6.INSTANCE == Singleton6.INSTANCE);
    }

    /**
     * 饿汉式单例
     */
    static class Singleton1 {
        // 私有静态变量，在类加载时就创建实例
        private static final Singleton1 INSTANCE = new Singleton1();

        // 私有构造函数，防止外部实例化对象
        private Singleton1() {
            // 初始化操作
        }

        // 公共静态方法，用于获取单例实例
        public static Singleton1 getInstance() {
            return INSTANCE;
        }
    }

    /**
     * 懒汉式单例：线程不安全
     */
    static class Singleton2 {
        private static Singleton2 instance;

        private Singleton2() {
        }

        public static Singleton2 getInstance() {


            if (instance == null) {
                //强制等待让其切换其它线程，模拟其线程不安全性
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                instance = new Singleton2();
            }
            return instance;
        }
    }

    /**
     * 懒汉式单例：线程安全
     */
    static class Singleton3 {
        private static Singleton3 instance;

        private Singleton3() {
        }

        /**
         * 加上synchronized关键字，保证线程安全
         *
         * @return
         */
        public static synchronized Singleton3 getInstance() {


            if (instance == null) {
                //强制等待让其切换其它线程，模拟其线程不安全性
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                instance = new Singleton3();
            }
            return instance;
        }
    }

    /**
     * 懒汉式单例：线程安全，双重检查锁
     */
    static class Singleton4 {
        //使用volatile修饰
        private static volatile Singleton4 instance;

        private Singleton4() {
        }

        /**
         * 双重检查锁
         *
         * @return 单例对象
         */
        public static Singleton4 getInstance() {
            if (instance == null) {
                synchronized (Singleton4.class) {
                    if (instance == null) {
                        //强制等待让其切换其它线程，模拟其线程不安全性
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        instance = new Singleton4();
                    }
                }
            }
            return instance;
        }
    }

    /*
     * 静态内部类实现懒汉式
     * */
    static class Singleton5 {
        private Singleton5() {
        }

        /**
         * 静态内部类只有在使用时才会被加载，从而实现懒加载
         */
        private static class SingletonHolder {
            private static final Singleton5 INSTANCE = new Singleton5();
        }

        public static Singleton5 getInstance() {
            return SingletonHolder.INSTANCE;
        }
    }

    /*
     * 枚举方式实现饿汉式单例
     * */
    static enum Singleton6 {
        INSTANCE;
    }
}
