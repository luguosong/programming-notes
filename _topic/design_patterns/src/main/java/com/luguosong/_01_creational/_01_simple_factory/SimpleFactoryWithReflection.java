package com.luguosong._01_creational._01_simple_factory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

/**
 * @author luguosong
 */
public class SimpleFactoryWithReflection {
    public static void main(String[] args) {
        Product productA = Factory.getProduct("productA");
        productA.operation();

        Product productB = Factory.getProduct("productB");
        productB.operation();
    }

    // 简单工厂角色
    static class Factory {
        //1,定义容器对象存储对象
        private static HashMap<String, Product> map = new HashMap<String, Product>();

        static {
            //2.1 创建Properties对象
            Properties p = new Properties();
            //2.2 调用p对象中的load方法进行配置文件的加载
            InputStream is = Factory.class.getClassLoader().getResourceAsStream("com/luguosong/_01_creational/_01_simple_factory/config.properties");
            try {
                p.load(is);
                //从p集合中获取全类名并创建对象
                Set<Object> keys = p.keySet();
                for (Object key : keys) {
                    String className = p.getProperty((String) key);
                    //通过反射技术创建对象
                    Class clazz = Class.forName(className);
                    Product coffee = (Product) clazz.newInstance();
                    //将名称和对象存储到容器中
                    map.put((String) key, coffee);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //根据名称获取对象
        public static Product getProduct(String name) {
            return map.get(name);
        }
    }
}
