package com.luguosong._03_object_oriente;

/**
 * 使用常量实现枚举
 *
 * @author luguosong
 */
public class EnumClassDemo {
    public static void main(String[] args) {
        Season1 season = Season1.SPRING;
        System.out.println(season);
        System.out.println(season.getName());
        System.out.println(season.getDesc());
    }
}

class Season1 {
    private final String name; //名称

    private final String desc; //描述

    private Season1(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }

    public String toString() {
        return this.name + " - " + this.desc;
    }

    public static final Season1 SPRING = new Season1("春天", "春暖花开");
    public static final Season1 SUMMER = new Season1("夏天", "夏日炎炎");
    public static final Season1 FALL = new Season1("秋天", "秋高气爽");
    public static final Season1 WINTER = new Season1("冬天", "冰天雪地");

}


