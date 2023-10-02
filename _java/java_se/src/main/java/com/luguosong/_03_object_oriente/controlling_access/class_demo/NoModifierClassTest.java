package com.luguosong._03_object_oriente.controlling_access.class_demo;

/**
 * @author luguosong
 */
public class NoModifierClassTest {
    public static void main(String[] args) {
        /*
         * 报错
         * 'xxx.class_package.NoModifierClass' 在 'xxx.class_demo.class_package' 中不为 public。
         * 无法从外部软件包访问
         * */
        //new NoModifierClass().test();
    }
}
