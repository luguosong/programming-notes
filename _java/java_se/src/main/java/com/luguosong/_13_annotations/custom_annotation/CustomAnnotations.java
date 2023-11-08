package com.luguosong._13_annotations.custom_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author luguosong
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAnnotations {

    /*
    * 设置元素
    *
    * 没有如何元素的注解称为标记注解
    * */
    int id();
    String description() default "no description";
}
