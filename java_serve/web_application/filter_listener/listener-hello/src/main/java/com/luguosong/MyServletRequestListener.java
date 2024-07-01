package com.luguosong;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

/**
 * @author luguosong
 */
public class MyServletRequestListener implements ServletRequestListener {

    /*
     * request对象被创建时调用
     * */
    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        System.out.println("request对象被创建了");
    }

    /*
     * request对象被销毁时调用
     * */
    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        System.out.println("request对象被销毁");
    }


}
