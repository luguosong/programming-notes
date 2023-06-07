package com.luguosong._02_structural._01_adapter;

/**
 * @author luguosong
 */
public class AdapterByClassExample {
    public static void main(String[] args) {
        //创建适配器对象
        Adapter adapter = new Adapter();

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
     * ⭐通过继承的方式实现适配器
     */
    static class Adapter extends Service implements ClientInterface {

        @Override
        public void request() {
            System.out.println("使用适配器进行适配");
            specificRequest();
        }
    }
}
