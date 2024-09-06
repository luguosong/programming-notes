package com.luguosong.ioc.create_bean;

import com.luguosong.ioc.User;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author luguosong
 */
public class FactoryMethod1 implements FactoryBean<User> {
    @Override
    public User getObject() throws Exception {
        return new User(12, "lsg");
    }

    @Override
    public Class<?> getObjectType() {
        return User.class;
    }

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }
}
