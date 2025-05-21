package com.luguosong.ioc.annotation.dependency_injection;

import org.springframework.stereotype.Component;

/**
 * @author luguosong
 */
@Component
public class Basketball implements Hobby{
    @Override
    public void play() {
        System.out.println("爱好打篮球");
    }
}
