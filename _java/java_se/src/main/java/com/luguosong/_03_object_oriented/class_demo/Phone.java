package com.luguosong._03_object_oriented.class_demo;

/**
 * 类中的字段和方法
 *
 * @author luguosong
 */
public class Phone {
    /*
     * 字段
     * */
    String name; //品牌
    double price; //价格

    /*
     * 方法
     * */
    public void call() {
        System.out.println("打电话");
    }

    public void sendMessage(String message) {
        System.out.println("发送短信：" + message);
    }
}
