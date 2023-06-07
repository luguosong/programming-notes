package com.luguosong._02_structural._01_adapter;

/**
 * 对象适配器
 *
 * @author luguosong
 */
public class AdapterExample {
    public static void main(String[] args) {
        // 创建服务对象
        Service service = new Service();

        // 创建适配器并传入服务对象
        ClientInterface adapter = new Adapter(service);

        // 通过适配器调用服务对象的请求
        adapter.request();
    }

    // 客户端接口
    static interface ClientInterface {
        void request();
    }

    // 服务类
    static class Service {
        public void specificRequest() {
            System.out.println("与客户端接口不匹配的服务");
        }
    }


    /**
     * 适配器类
     * <p>
     * ⭐通过组合的方式实现适配器
     */
    static class Adapter implements ClientInterface {
        private Service service;

        public Adapter(Service service) {
            this.service = service;
        }

        @Override
        public void request() {
            System.out.println("使用适配器进行适配");
            service.specificRequest();
        }
    }
}
