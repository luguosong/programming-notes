package com.luguosong.anno;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 切面
 *
 * 注解@Order控制多个切面的执行顺序
 * @author luguosong
 */
@Component
@Aspect
@Order(3)
public class UserAspect {

    /*
    * 通用切点表达式
    * */
    @Pointcut("execution(* com.luguosong.anno.UserServiceImpl.*(..))")
    public void pointcut() {
    }

    //前置通知
    @Before("execution(* com.luguosong.anno.UserServiceImpl.*(..))")
    public void before() {
        System.out.println("前置通知");
    }

    //后置通知
    @AfterReturning("pointcut()")
    public void afterReturning() {
        System.out.println("后置通知");
    }

    /*
    * 环绕通知
    *
    * ❗如果发生异常，不会执行 环绕后置通知
    * */
    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("环绕前置通知");
        Object o = joinPoint.proceed();
        System.out.println("环绕后置通知");
        return o;
    }

    //异常通知
    @AfterThrowing("pointcut()")
    public void afterThrowing() {
        System.out.println("异常通知");
    }

    //最终通知
    @After("pointcut()")
    public void after(){
        System.out.println("最终通知");
    }

}
