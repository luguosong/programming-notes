package com.luguosong._16_multithreading;

/**
 * @author luguosong
 */
public class CreateThread1 {
    public static int a = 0;

    public static void main(String[] args) {


        //创建线程对象
        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + ":" + i + "," + ++a);
                try {
                    //暂停0.1秒
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        /*
         * 1.启动线程
         * 2.调用当前线程的run方法
         * */
        thread1.start();

        // 主线程执行for循环
        for (int i = 0; i < 5; i++) {
            System.out.println(Thread.currentThread().getName() + ":" + i + "," + ++a);
            try {
                //暂停0.1秒
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
