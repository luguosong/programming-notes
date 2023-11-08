package com.luguosong._12_reflection;

import java.util.Date;

/**
 * @author luguosong
 */
public class ClassDemo {
    public static void main(String[] args) {
        Date date1 = new Date();
        Date date2 = new Date();

        // 相同类创建的对象，Class对象是一样的
        System.out.println(date1.getClass() == date2.getClass()); // true
    }
}
