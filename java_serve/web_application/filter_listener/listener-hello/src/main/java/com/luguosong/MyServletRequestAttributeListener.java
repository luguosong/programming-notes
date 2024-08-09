package com.luguosong;

import jakarta.servlet.ServletRequestAttributeEvent;
import jakarta.servlet.ServletRequestAttributeListener;

/**
 * @author luguosong
 */
public class MyServletRequestAttributeListener implements ServletRequestAttributeListener {
    /*
    * request域中值被创建
    * */
    @Override
    public void attributeAdded(ServletRequestAttributeEvent srae) {
        System.out.println("request域中值被创建");
    }

    /*
    * request域中值被销毁
    * */
    @Override
    public void attributeRemoved(ServletRequestAttributeEvent srae) {
        System.out.println("request域中值被销毁");
    }

    /*
    * request域中值被替换
    * */
    @Override
    public void attributeReplaced(ServletRequestAttributeEvent srae) {
        System.out.println("request域中值被替换");
    }
}
