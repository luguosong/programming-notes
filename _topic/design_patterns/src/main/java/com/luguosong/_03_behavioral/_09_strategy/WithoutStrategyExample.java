package com.luguosong._03_behavioral._09_strategy;

/**
 * 不使用策略的情况
 *
 * @author luguosong
 */
public class WithoutStrategyExample {

    public static void main(String[] args) {
        Context context = new Context();
        context.doSomething("A");
        context.doSomething("B");
    }

    /**
     * 不使用策略的Context类
     * <p>
     * 当策略变多时，Context类的executeStrategy方法会变得很复杂，很难维护
     */
    static class Context {
        /**
         * 执行策略的方法
         */
        public void doSomething(String strategyType) {
            if ("A".equals(strategyType)) {
                executeStrategyA();
            } else if ("B".equals(strategyType)) {
                executeStrategyB();
            }
        }

        private static void executeStrategyA() {
            System.out.println("执行策略A");
        }

        private static void executeStrategyB() {
            System.out.println("执行策略B");
        }
    }
}
