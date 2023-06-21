package com.luguosong._03_behavioral._08_state;

/**
 * 状态模式示例
 *
 * @author luguosong
 */
public class StateExample {
    public static void main(String[] args) {
        System.out.println("====初始状态================================");
        GumballMachine gumballMachine = new GumballMachine(2);
        System.out.println(gumballMachine);

        System.out.println("====第一轮投币================================");
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        System.out.println(gumballMachine);

        System.out.println("====第二轮投币================================");
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        System.out.println(gumballMachine);

        System.out.println("====第三轮投币================================");
        gumballMachine.refill(5);
        gumballMachine.insertQuarter();
        gumballMachine.turnCrank();
        System.out.println(gumballMachine);
    }

    /**
     * 状态
     */
    static public interface State {

        public void insertQuarter();
        public void ejectQuarter();
        public void turnCrank();
        public void dispense();

        public void refill();
    }

    /**
     * 具体状态：售罄
     */
    static public class SoldOutState implements State {
        GumballMachine gumballMachine;

        public SoldOutState(GumballMachine gumballMachine) {
            this.gumballMachine = gumballMachine;
        }

        public void insertQuarter() {
            System.out.println("无法投入硬币，糖果机已售罄");
        }

        public void ejectQuarter() {
            System.out.println("无法退回硬币，您尚未投入硬币");
        }

        public void turnCrank() {
            System.out.println("您转动了曲柄，但是没有糖果");
        }

        public void dispense() {
            System.out.println("没有发放糖果");
        }

        public void refill() {
            gumballMachine.setState(gumballMachine.getNoQuarterState());
        }

        public String toString() {
            return "售罄";
        }
    }

    /**
     * 具体状态：等待投币
     */
    static public class NoQuarterState implements State {
        GumballMachine gumballMachine;

        public NoQuarterState(GumballMachine gumballMachine) {
            this.gumballMachine = gumballMachine;
        }

        public void insertQuarter() {
            System.out.println("您投入了一个硬币");
            gumballMachine.setState(gumballMachine.getHasQuarterState());
        }

        public void ejectQuarter() {
            System.out.println("您尚未投入硬币");
        }

        public void turnCrank() {
            System.out.println("您转动了曲柄，但没有硬币");
        }

        public void dispense() {
            System.out.println("请先支付");
        }

        public void refill() {
        }

        public String toString() {
            return "等待投币";
        }
    }

    /**
     * 已投币，等待转动曲柄
     */
    static public class HasQuarterState implements State {
        GumballMachine gumballMachine;

        public HasQuarterState(GumballMachine gumballMachine) {
            this.gumballMachine = gumballMachine;
        }

        public void insertQuarter() {
            System.out.println("不能再投入另一个硬币");
        }

        public void ejectQuarter() {
            System.out.println("退回硬币");
            gumballMachine.setState(gumballMachine.getNoQuarterState());
        }

        public void turnCrank() {
            System.out.println("您转动了曲柄...");
            gumballMachine.setState(gumballMachine.getSoldState());
        }

        public void dispense() {
            System.out.println("没有发放糖果");
        }

        public void refill() {
        }

        public String toString() {
            return "已投币，等待转动曲柄";
        }
    }

    /**
     * 具体状态：正在发放一个糖果
     */
    static public class SoldState implements State {
        GumballMachine gumballMachine;

        public SoldState(GumballMachine gumballMachine) {
            this.gumballMachine = gumballMachine;
        }

        public void insertQuarter() {
            System.out.println("请等待，我们已经在给您一个糖果");
        }

        public void ejectQuarter() {
            System.out.println("抱歉，您已经转动了曲柄");
        }

        public void turnCrank() {
            System.out.println("转动两次也无法再得到一个糖果！");
        }

        public void dispense() {
            gumballMachine.releaseBall();
            if (gumballMachine.getCount() > 0) {
                gumballMachine.setState(gumballMachine.getNoQuarterState());
            } else {
                System.out.println("糟糕，糖果已售罄！");
                gumballMachine.setState(gumballMachine.getSoldOutState());
            }
        }

        public void refill() {
        }

        public String toString() {
            return "正在发放一个糖果";
        }
    }

    static public class GumballMachine {

        State soldOutState;
        State noQuarterState;
        State hasQuarterState;
        State soldState;

        State state;
        int count = 0;

        public GumballMachine(int numberGumballs) {
            soldOutState = new SoldOutState(this);
            noQuarterState = new NoQuarterState(this);
            hasQuarterState = new HasQuarterState(this);
            soldState = new SoldState(this);

            this.count = numberGumballs;
            if (numberGumballs > 0) {
                state = noQuarterState;
            } else {
                state = soldOutState;
            }
        }

        public void insertQuarter() {
            state.insertQuarter();
        }

        public void ejectQuarter() {
            state.ejectQuarter();
        }

        public void turnCrank() {
            state.turnCrank();
            state.dispense();
        }

        void releaseBall() {
            System.out.println("一个口香糖球从插槽中滚出来...");
            if (count > 0) {
                count = count - 1;
            }
        }

        int getCount() {
            return count;
        }

        void refill(int count) {
            this.count += count;
            System.out.println("口香糖机刚刚加满；它的新计数是: " + this.count);
            state.refill();
        }

        void setState(State state) {
            this.state = state;
        }
        public State getState() {
            return state;
        }

        public State getSoldOutState() {
            return soldOutState;
        }

        public State getNoQuarterState() {
            return noQuarterState;
        }

        public State getHasQuarterState() {
            return hasQuarterState;
        }

        public State getSoldState() {
            return soldState;
        }

        public String toString() {
            StringBuffer result = new StringBuffer();
            result.append("当前库存为: " + count );
            result.append("，机器状态为： " + state + "\n");
            return result.toString();
        }
    }

}
