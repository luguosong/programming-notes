package com.luguosong.ioc.annotation.dependency_injection;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author luguosong
 */
@Component
public class User {

    //注入基本数据类型
    @Value("张三")
    private String name;

    /*
    * 根据类型注入
    *
    * ⭐接口只能有一个实现类
    * */
    @Autowired
    private Hobby hobby;

    /*
    * 根据名字注入（也就是bean的id）
    *
    * 这种情况允许接口有多个实现类
    * */
    @Autowired
    @Qualifier("bike")
    private Vehicle vehicle1;

    /*
    * 根据名字注入
    * */
    @Resource(name = "car")
    private Vehicle vehicle2;

    /*
    * @Resource省略name属性
    * 会根据属性名寻找对应名字的实现类
    * */
    @Resource
    private Vehicle car;

    public void play(){
        hobby.play();
    }

    public void driver(){
        vehicle1.drive();
        vehicle2.drive();
        car.drive();
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}
