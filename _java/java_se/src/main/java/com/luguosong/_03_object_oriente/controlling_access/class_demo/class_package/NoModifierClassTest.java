package com.luguosong._03_object_oriente.controlling_access.class_demo.class_package;

/**
 * @author luguosong
 */
public class NoModifierClassTest {
    public static void main(String[] args) {
        // NoModifierClass虽然没有public修饰，但是在同一个包下可以访问
        new NoModifierClass().test();
    }
}
