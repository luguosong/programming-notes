package com.luguosong.util;

public class TimeTool {
    public static void check(String title, Runnable task) {
        if (task == null) return;
        title = (title == null) ? "" : ("【" + title + "】");
        System.out.println(title);
        long begin = System.nanoTime();
        task.run();
        long end = System.nanoTime();
        long delta = end - begin;
        System.out.println("耗时：" + delta / 1000000.0 + "毫秒");
        System.out.println("-------------------------------------");
    }

    public static void check(Runnable task) {
        check(null, task);
    }
}
