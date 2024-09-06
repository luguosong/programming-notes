package com.luguosong;

import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;

/**
 * @author luguosong
 */
public class MyHttpSessionAttributeListener implements HttpSessionAttributeListener {

    /*
     * session域值被添加
     * */
    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        System.out.println("session域值被添加");
    }

    /*
     * session域值被删除
     * */
    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        System.out.println("session域值被删除");
    }


    /*
     * session域值被替换
     * */
    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        System.out.println("session域值被替换");
    }
}
