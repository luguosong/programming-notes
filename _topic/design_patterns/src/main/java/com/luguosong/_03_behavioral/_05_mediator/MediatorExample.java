package com.luguosong._03_behavioral._05_mediator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luguosong
 */
public class MediatorExample {
    public static void main(String[] args) {
        // 创建中介者
        ConcreteMediator mediator = new ConcreteMediator();

        // 创建组件并注册到中介者
        Component component1 = new ConcreteComponent(mediator);
        Component component2 = new ConcreteComponent(mediator);
        mediator.registerComponent(component1);
        mediator.registerComponent(component2);

        // 组件之间通过中介者进行通信
        component1.send("你好，来自组件1");
        component2.send("你好，来自组件2");
    }

    // 中介者接口
    static interface Mediator {
        void notify(Component component, String message);
    }

    // 具体中介者
    static class ConcreteMediator implements Mediator {
        List<Component> components = new ArrayList<>();

        public void registerComponent(Component component) {
            components.add(component);
        }

        /**
         * ⭐中介者关键代码
         *
         * 消息发送方通过中介将消息发送给其他组件
         *
         * @param component 发送消息的组件
         * @param message  消息内容
         */
        @Override
        public void notify(Component component, String message) {
            for (Component c : components) {
                if (c != component) {
                    c.receive(message);
                }
            }
        }
    }

    // 组件类
    static abstract class Component {
        protected Mediator mediator;

        protected String name;

        public Component(Mediator mediator) {
            this.mediator = mediator;
        }

        public abstract void send(String message);

        public abstract void receive(String message);
    }

    // 具体组件类
    static class ConcreteComponent extends Component {
        public ConcreteComponent(Mediator mediator) {
            super(mediator);
        }

        @Override
        public void send(String message) {
            System.out.println("组件发送消息: " + message);
            mediator.notify(this, message);
        }

        @Override
        public void receive(String message) {
            System.out.println("组件接收消息: " + message);
        }
    }
}
