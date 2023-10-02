package com.luguosong._03_object_oriente;

/**
 * @author luguosong
 */
public class EnumDemo {
    public static void main(String[] args) {
        Season2 season = Season2.SUMMER;
        System.out.println(season.getClass().getSuperclass()); //class java.lang.Enum

        System.out.println(season); //自带的toString打印的是枚举对象的名称 SPRING
        System.out.println(season.name()); //SPRING
        //values()方法返回的是一个数组，数组中存放的是枚举对象
        for (Season2 s : Season2.values()) {
            System.out.println(s);
        }
        System.out.println(season.ordinal()); //获取枚举对象的索引值，从0开始

    }
}

enum Season2 {
    /*
     * 声明多个对象，对象之间使用逗号隔开
     * */
    SPRING("春天", "春暖花开"),

    SUMMER("夏天", "夏日炎炎"),

    FALL("秋天", "秋高气爽"),

    WINTER("冬天", "冰天雪地");

    private final String name; //名称

    private final String desc; //描述

    private Season2(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

}
