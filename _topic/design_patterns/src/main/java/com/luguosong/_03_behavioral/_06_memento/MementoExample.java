package com.luguosong._03_behavioral._06_memento;

import java.util.Stack;

/**
 * @author luguosong
 */
public class MementoExample {
    public static void main(String[] args) {
        Originator originator = new Originator(); //创建原发器
        Caretaker caretaker = new Caretaker();

        originator.setState("State 1"); //设置状态：1
        originator.printState(); // 打印当前状态：1

        caretaker.saveState(originator); //创建快照，并保存状态：1

        originator.setState("State 2"); //设置状态：2
        originator.printState(); // 打印当前状态：2


        caretaker.saveState(originator); //创建快照，并保存状态：2

        originator.setState("State 3"); //设置状态：3
        originator.printState(); // 打印当前状态：3


        caretaker.restoreState(originator); //回滚状态
        originator.printState(); //打印状态


        caretaker.restoreState(originator); // 回滚状态
        originator.printState(); //打印状态
    }

    // 原发器类
    static class Originator {
        private String state;

        /**
         * 设置状态
         *
         * @param state 状态
         */
        public void setState(String state) {
            this.state = state;
        }

        public Memento createMemento() {
            return new Memento(state);
        }

        /**
         * 获取备忘录中的状态，并将当前状态恢复
         *
         * @param memento
         */
        public void restoreFromMemento(Memento memento) {
            state = memento.getState();
        }

        /**
         * 打印状态
         */
        public void printState() {
            System.out.println("当前状态: " + state);
        }

        /**
         * private修饰，备忘录类作为内部类，只能被原发器类访问
         *
         * 备忘录类
         * <p>
         * 相当于快照记录器，只会被原发器类访问
         */
        private class Memento {
            private final String state;

            private Memento(String state) {
                this.state = state;
            }

            private String getState() {
                return state;
            }
        }
    }


    // 负责人类
    //static class Caretaker {
    //    private final Stack<Memento> mementoStack = new Stack<>();
    //
    //    /**
    //     * 拍摄原发器类的备忘录快照，存储进集合
    //     *
    //     * @param originator
    //     */
    //    public void saveState(Originator originator) {
    //        mementoStack.push(originator.createMemento());
    //    }
    //
    //    /**
    //     * 回滚快照
    //     *
    //     * @param originator 原发器
    //     */
    //    public void restoreState(Originator originator) {
    //        if (!mementoStack.isEmpty()) {
    //            Memento memento = mementoStack.pop();
    //            originator.restoreFromMemento(memento);
    //        }
    //    }
    //}

    // 负责人类
    static class Caretaker {
        private final Stack<Originator.Memento> mementoStack = new Stack<>();

        /**
         * 拍摄原发器类的备忘录快照，存储进集合
         *
         * @param originator
         */
        public void saveState(Originator originator) {
            mementoStack.push(originator.createMemento());
        }

        public void restoreState(Originator originator) {
            if (!mementoStack.isEmpty()) {
                Originator.Memento memento = mementoStack.pop();
                originator.restoreFromMemento(memento);
            }
        }
    }
}
