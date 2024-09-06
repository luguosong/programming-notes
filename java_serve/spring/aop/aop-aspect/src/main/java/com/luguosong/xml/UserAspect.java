package com.luguosong.xml;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

/**
 * @author luguosong
 */
public class UserAspect {
    //前置通知
    public void before() {
        System.out.println("前置通知");
    }

    //后置通知
    public void afterReturning() {
        System.out.println("后置通知");
    }

    /*
     * 环绕通知
     *
     * ❗如果发生异常，不会执行 环绕后置通知
     * */
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("环绕前置通知");
        Object o = joinPoint.proceed();
        System.out.println("环绕后置通知");
        return o;
    }

    //异常通知
    public void afterThrowing() {
        System.out.println("异常通知");
    }

    //最终通知
    public void after(){
        System.out.println("最终通知");
    }
}
