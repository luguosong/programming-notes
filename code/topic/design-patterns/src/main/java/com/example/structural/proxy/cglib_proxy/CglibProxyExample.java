package com.example.structural.proxy.cglib_proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * CGLIB 动态代理示例
 * <p>
 * CGLIB 通过字节码生成目标类的子类作为代理，无需目标类实现任何接口。
 * Spring AOP 在目标类没有接口时默认使用此方式。
 * <p>
 * 限制：无法代理 final 类或 final 方法（子类无法覆写）。
 * Java 17+ 运行需要 --add-opens java.base/java.lang=ALL-UNNAMED；Spring Boot 已自动处理。
 */
public class CglibProxyExample {
    public static void main(String[] args) {
        // ✅ CGLIB：直接代理普通类，无需接口——这是与 JDK 代理的核心区别
        OrderService service = CglibProxyFactory.createLoggingProxy(new OrderService());
        service.createOrder("商品C", 5);
        service.cancelOrder(3001L);
    }
}

// ✅ 普通类，无需实现任何接口
class OrderService {
    private long nextId = 3000L;

    public Long createOrder(String product, int quantity) {
        System.out.printf("[业务] 创建订单：%s x%d%n", product, quantity);
        return ++nextId;
    }

    public void cancelOrder(Long orderId) {
        System.out.println("[业务] 取消订单：" + orderId);
    }
}

// CGLIB 代理工厂：使用 Enhancer 生成继承目标类的子类
class CglibProxyFactory {
    @SuppressWarnings("unchecked")
    public static <T> T createLoggingProxy(T target) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(target.getClass()); // 继承目标类
        enhancer.setCallback(new LoggingInterceptor());
        // 生成子类实例（代理 IS-A 目标类，无需接口）
        return (T) enhancer.create();
    }
}

// 方法拦截器：拦截子类的所有方法调用，织入横切逻辑
class LoggingInterceptor implements MethodInterceptor {
    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        System.out.printf("[日志] → %s(%s)%n", method.getName(), formatArgs(args));
        // invokeSuper 调用父类（原始类）的方法，不会触发再次拦截
        Object result = proxy.invokeSuper(obj, args);
        System.out.printf("[日志] ← %s 返回: %s%n", method.getName(), result);
        return result;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "无参数";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(args[i]);
        }
        return sb.toString();
    }
}
