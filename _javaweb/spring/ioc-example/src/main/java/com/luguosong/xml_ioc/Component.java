package com.luguosong.xml_ioc;

/**
 * 组件
 *
 * @author luguosong
 */
public class Component {

    String msg;

    // Util依赖
    Util util;

    // 无参构造
    private Component() {
        msg = "通过Component的无参构造方法创建Component对象";
    }

    // 有参构造，使用构造函数进行依赖注入
    public Component(Util util, String msg) {
        this.msg = msg;
        this.util = util;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Util getUtil() {
        return util;
    }

    public void setUtil(Util util) {
        this.util = util;
    }

    public void method() {
        System.out.println("Component中method方法被调用");
    }
}
