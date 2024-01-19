package com.luguosong.xml_ioc;

/**
 * @author luguosong
 */
public class ComponentFactory {
    // 静态工厂方法
    private static Component getComponent1() {
        return new Component(new Util(), "通过静态工厂方法创建Component对象");
    }

    // 实例工厂方法
    private Component getComponent2() {
        return new Component(new Util(), "通过实例工厂方法创建Component对象");
    }
}
