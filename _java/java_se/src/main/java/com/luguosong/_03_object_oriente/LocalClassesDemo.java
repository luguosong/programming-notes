package com.luguosong._03_object_oriente;

/**
 * 局部类示例
 *
 * @author luguosong
 */
public class LocalClassesDemo {

    int field = 20;

    public void testMethod() {
        int localVariable = 0;

        class LocalClass {

            void accessField() {
                System.out.println("访问字段：" + field);
            }

            void setField() {
                //字段可以修改
                field = 30;
            }

            void accessLocalVariable() {
                System.out.println("访问局部变量：" + localVariable);
            }

            void setLocalVariable() {
                //❌变量 'localVariable' 从内部类中访问，需要为 final 或有效 final
                //localVariable = 1;
                System.out.println("无法修改局部变量");
            }
        }

        LocalClass localClass = new LocalClass();
        localClass.setField();
        localClass.accessField();
        localClass.setLocalVariable();
        localClass.accessLocalVariable();
    }

    public static void main(String[] args) {
        new LocalClassesDemo().testMethod();
    }
}
