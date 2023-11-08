package com.luguosong._13_annotations.custom_annotation;

/**
 * @author luguosong
 */
public class Test {

    //由于注定定义时设置了@Target(ElementType.METHOD)，因此，注解只能对方法使用
    //@CustomAnnotations(id = 1,description = "使用自定义注解")
    private int a = 0;

    @CustomAnnotations(id = 1, description = "使用自定义注解")
    public void test() {
        System.out.println("使用自定义注解");
    }
}
