package com.luguosong.ioc.annotation.dependency_injection;

import org.springframework.stereotype.Component;

/**
 * @author luguosong
 */
@Component
public class Bike implements Vehicle{
    @Override
    public void drive() {
        System.out.println("骑自行车");
    }
}
