package com.luguosong.ioc.annotation.dependency_injection;

import org.springframework.stereotype.Component;

/**
 * @author luguosong
 */
@Component
public class Car implements Vehicle{
    @Override
    public void drive() {
        System.out.println("开汽车");
    }
}
