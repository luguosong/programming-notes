package com.luguosong._02_structural._07_proxy;

/**
 * @author luguosong
 */
public class ProxyExample {
    public static void main(String[] args) {
        Proxy proxy = new Proxy();
        proxy.operation();
    }

    // 服务接口（Service Interface）：定义了服务和代理的共同操作
    static interface ServiceInterface {
        void operation();
    }

    // 服务（Service）：执行实际的操作
    static class RealService implements ServiceInterface {
        public void operation() {
            System.out.println("在实际服务中进行操作。");
        }
    }

    /**
     * 代理类
     */
    static class Proxy implements ServiceInterface {
        private ServiceInterface service;

        public Proxy() {
            this.service = new RealService();
        }

        public Proxy(ServiceInterface service) {
            this.service = service;
        }

        public void operation() {
            System.out.println("前置代理：代理完成任务（例如延迟初始化、记录日志、访问控制和缓存等）");
            //将请求传递给服务对象
            service.operation();
            System.out.println("后置代理：代理管理服务对象的生命周期");
        }
    }
}
