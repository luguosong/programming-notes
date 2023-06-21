package com.luguosong._03_behavioral._07_observer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luguosong
 */
public class ObserverExample {
    public static void main(String[] args) {
        ConcretePublisher publisher = new ConcretePublisher();

        ConcreteSubscriber subscriber1 = new ConcreteSubscriber("订阅者 1");
        ConcreteSubscriber subscriber2 = new ConcreteSubscriber("订阅者 2");

        /*
         * 添加两位订阅者
         * */
        publisher.subscribe(subscriber1);
        publisher.subscribe(subscriber2);
        publisher.doSomething();

        /*
         * 删除一个订阅者
         * */
        System.out.println("===删除订阅者 2===");
        publisher.unsubscribe(subscriber2);
        publisher.doSomething();
    }

    // 发布者接口
    static interface Publisher {
        void subscribe(Subscriber subscriber);

        void unsubscribe(Subscriber subscriber);

        void notifySubscribers(String event);
    }

    // 订阅者接口
    static interface Subscriber {
        void update(String event);
    }

    // 具体发布者类
    static class ConcretePublisher implements Publisher {
        private List<Subscriber> subscribers = new ArrayList<>();

        @Override
        public void subscribe(Subscriber subscriber) {
            subscribers.add(subscriber);
        }

        /**
         * 删除订阅者
         *
         * @param subscriber 订阅者
         */
        @Override
        public void unsubscribe(Subscriber subscriber) {
            subscribers.remove(subscriber);
        }

        /**
         * ⭐观察者核心代码，通知所有观察者
         *
         * @param event 事件
         */
        @Override
        public void notifySubscribers(String event) {
            for (Subscriber subscriber : subscribers) {
                subscriber.update(event);
            }
        }


        /**
         * 发布者特定的行为，当状态改变时调用该方法通知订阅者
         */
        public void doSomething() {
            // 状态改变或执行特定行为
            String event = "发生了某些事情";
            notifySubscribers(event);
        }
    }

    // 具体订阅者类
    static class ConcreteSubscriber implements Subscriber {
        private String name;

        public ConcreteSubscriber(String name) {
            this.name = name;
        }

        @Override
        public void update(String event) {
            System.out.println(name + " 收到事件: " + event);
            // 执行相应操作
        }
    }
}
