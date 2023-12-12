package com.luguosong;

/**
 * @author luguosong
 */
public class RunTimeDemo {
    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        System.out.println("处理器数量：" + runtime.availableProcessors()); //32
        System.out.println("空闲内存数：" + runtime.freeMemory()); // 505329720
        System.out.println("总内存数：" + runtime.totalMemory()); // 510656512
        System.out.println("可用最大内存数：" + runtime.maxMemory()); //7575961600
    }
}
