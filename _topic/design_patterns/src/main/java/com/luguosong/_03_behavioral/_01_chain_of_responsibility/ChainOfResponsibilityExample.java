package com.luguosong._03_behavioral._01_chain_of_responsibility;

/**
 * @author luguosong
 */
public class ChainOfResponsibilityExample {
    public static void main(String[] args) {
        // 创建具体处理者实例
        Handler handler1 = new ConcreteHandler1();
        Handler handler2 = new ConcreteHandler2();

        // 设置处理链
        handler1.setNextHandler(handler2);

        // 发送请求
        handler1.handleRequest("Type1"); // ConcreteHandler1 handles the request.
        handler1.handleRequest("Type2"); // ConcreteHandler2 handles the request.
        handler1.handleRequest("Type3"); // No handler can handle the request.
    }


    /**
     * 处理者接口
     */
    static public interface Handler {
        void handleRequest(String requestType);

        void setNextHandler(Handler nextHandler);
    }


    /**
     * 基础处理者类
     */
    static public abstract class BaseHandler implements Handler {
        protected Handler nextHandler;

        public void setNextHandler(Handler nextHandler) {
            this.nextHandler = nextHandler;
        }
    }


    /**
     * 具体处理者类
     */
    static public class ConcreteHandler1 extends BaseHandler {
        public void handleRequest(String requestType) {
            if (requestType.equals("Type1")) {
                System.out.println("ConcreteHandler1 handles the request.");
                // 处理请求的逻辑
            } else if (nextHandler != null) {
                nextHandler.handleRequest(requestType);
            }
        }
    }

    /**
     * 具体处理者类
     */
    static public class ConcreteHandler2 extends BaseHandler {
        public void handleRequest(String requestType) {
            if (requestType.equals("Type2")) {
                System.out.println("ConcreteHandler2 handles the request.");
                // 处理请求的逻辑
            } else if (nextHandler != null) {
                nextHandler.handleRequest(requestType);
            }
        }
    }
}
