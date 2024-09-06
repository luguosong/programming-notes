package com.luguosong;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * @author luguosong
 */
public class MyServletContextListener implements ServletContextListener {

    /*
     * ServletContext对象初创建时调用
     * */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("ServletContext对象被创建了");
    }

    /*
     * ServletContext对象销毁时调用
     * */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("ServletContext对象被销毁了");
    }

}
