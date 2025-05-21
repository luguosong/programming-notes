package com.luguosong;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * @author luguosong
 */
public class MyHttpSessionListener implements HttpSessionListener {
    /*
    * session对象被创建
    * */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("session对象被创建");
    }

    /*
    * session对象被销毁
    * */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("session对象被销毁");
    }
}
