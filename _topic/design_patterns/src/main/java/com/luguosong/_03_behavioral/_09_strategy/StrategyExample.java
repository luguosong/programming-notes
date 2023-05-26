package com.luguosong._03_behavioral._09_strategy;

/**
 * 策略模式示例
 *
 * @author luguosong
 */
public class StrategyExample {
    public static void main(String[] args) {
        Context context = new Context();

        // 创建具体策略对象A
        Strategy strategyA = new ConcreteStrategyA();
        // 设置策略A
        context.setStrategy(strategyA);
        context.executeStrategy();

        // 创建具体策略对象B
        Strategy strategyB = new ConcreteStrategyB();
        // 设置策略B
        context.setStrategy(strategyB);
        context.executeStrategy();
    }

    /**
     * 策略（Strategy）
     */
    static interface Strategy {
        /**
         * 执行策略的方法
         */
        void execute();
    }

    /**
     * 具体策略类A，实现了策略接口
     */
    static class ConcreteStrategyA implements Strategy {
        @Override
        public void execute() {
            System.out.println("策略A执行");
        }
    }

    /**
     * 具体策略类B，实现了策略接口
     */
    static class ConcreteStrategyB implements Strategy {
        @Override
        public void execute() {
            System.out.println("策略B执行");
        }
    }

    /**
     * 上下文类，用于执行策略
     */
    static class Context {
        private Strategy strategy;

        /**
         * 设置策略
         *
         * @param strategy 策略对象
         */
        public void setStrategy(Strategy strategy) {
            this.strategy = strategy;
        }

        /**
         * 执行策略
         */
        public void executeStrategy() {
            strategy.execute();
        }
    }


}
