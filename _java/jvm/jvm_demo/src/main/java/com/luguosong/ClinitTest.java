package com.luguosong;

/**
 * @author luguosong
 */
public class ClinitTest {

    //这段代码会被收集到<clinit>类构造器中
    public static int num1 = 10;

    static {
        //这段代码也会被收集到<clinit>类构造器中
        num1 = 11;
        num2 = 22;
    }

    //这段代码会被收集到<clinit>类构造器中
    //因为<clinit>类构造器是按照源代码顺序收集的
    //因此这段代码会覆盖上面的num2 = 22
    public static int num2 = 20;

    public static void main(String[] args) {
        System.out.println(num1); //11
        System.out.println(num2); //22
    }
}
