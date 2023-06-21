package com.luguosong._03_behavioral._08_state;

/**
 * @author luguosong
 */
public class WithoutStateExample {
    public static void main(String[] args) {
        System.out.println("====初始状态================================");
        GumballMachine gumballMachine = new GumballMachine(5);
        System.out.println(gumballMachine);

        System.out.println("====第一轮投币================================");
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        System.out.println(gumballMachine);

        System.out.println("====第二轮投币================================");
        gumballMachine.insertQuarter();
        gumballMachine.ejectQuarter();
        gumballMachine.turnCrank();
        System.out.println(gumballMachine);

        System.out.println("====第三轮投币================================");
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        gumballMachine.ejectQuarter();
        System.out.println(gumballMachine);

        System.out.println("====第四轮投币================================");
        gumballMachine.insertQuarter();
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        System.out.println(gumballMachine);
    }

    static public class GumballMachine {

        /*
         * 状态
         * */
        final static int SOLD_OUT = 0; //售罄
        final static int NO_QUARTER = 1; //未投币
        final static int HAS_QUARTER = 2; //已投币
        final static int SOLD = 3; //售出

        int state = SOLD; //初始状态：售罄
        int count = 0; //剩余数量

        /*
         * 构造
         * */
        public GumballMachine(int count) {
            this.count = count;
            if (count > 0) {
                state = NO_QUARTER;
            }
        }

        /**
         * 动作：投币
         * 根据不同状态做出不同响应
         */
        public void insertQuarter() {
            if (state == HAS_QUARTER) {
                System.out.println("无法再投入另一个硬币");
            } else if (state == NO_QUARTER) {
                state = HAS_QUARTER;
                System.out.println("你投入了一个硬币");
            } else if (state == SOLD_OUT) {
                System.out.println("无法投入硬币，机器已售罄");
            } else if (state == SOLD) {
                System.out.println("请等待，我们已经在发放一颗口香糖");
            }
        }

        /**
         * 动作：退币
         * 根据不同状态做出不同响应
         */
        public void ejectQuarter() {
            if (state == HAS_QUARTER) {
                System.out.println("退还硬币");
                state = NO_QUARTER;
            } else if (state == NO_QUARTER) {
                System.out.println("你还没有投入硬币");
            } else if (state == SOLD) {
                System.out.println("抱歉，你已经转动了手柄");
            } else if (state == SOLD_OUT) {
                System.out.println("无法退还硬币，你还没有投入硬币");
            }
        }

        /**
         * 动作：转动曲柄
         * 根据不同状态做出不同响应
         */
        public void turnCrank() {
            if (state == SOLD) {
                System.out.println("转动两次也不会得到另一颗口香糖！");
            } else if (state == NO_QUARTER) {
                System.out.println("你转动了手柄，但没有投入硬币");
            } else if (state == SOLD_OUT) {
                System.out.println("你转动了手柄，但口香糖已售罄");
            } else if (state == HAS_QUARTER) {
                System.out.println("你转动了手柄...");
                state = SOLD;
                dispense();
            }
        }

        /**
         * 动作：发放糖果
         * 根据不同状态做出不同响应
         */
        private void dispense() {
            if (state == SOLD) {
                System.out.println("一颗口香糖从出口滚出来");
                count = count - 1;
                if (count == 0) {
                    System.out.println("糟糕，口香糖售罄！");
                    state = SOLD_OUT;
                } else {
                    state = NO_QUARTER;
                }
            } else if (state == NO_QUARTER) {
                System.out.println("你需要先支付");
            } else if (state == SOLD_OUT) {
                System.out.println("没有发放口香糖");
            } else if (state == HAS_QUARTER) {
                System.out.println("没有发放口香糖");
            }
        }

        /*
         * toString方法
         * */
        @Override
        public String toString() {
            StringBuffer result = new StringBuffer();
            result.append("当前情况-->库存: " + count + " 颗口香糖");
            result.append("，机器状态为 ");
            if (state == SOLD_OUT) {
                result.append("售罄");
            } else if (state == NO_QUARTER) {
                result.append("等待投币");
            } else if (state == HAS_QUARTER) {
                result.append("等待转动手柄");
            } else if (state == SOLD) {
                result.append("正在售出");
            }
            result.append("\n");
            return result.toString();
        }
    }
}
