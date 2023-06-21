package com.luguosong._03_behavioral._02_command;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luguosong
 */
public class CommandExample {
    public static void main(String[] args) {
        // 创建多个接收者对象
        Receiver receiver1 = new Receiver("接收者1");
        Receiver receiver2 = new Receiver("接收者2");

        // 创建具体命令对象，并将接收者对象传递给它
        Command command1 = new ConcreteCommand(receiver1);
        Command command2 = new ConcreteCommand(receiver2);

        // 创建发送者对象，并添加命令
        Sender sender = new Sender();
        sender.addCommand(command1);
        sender.addCommand(command2);

        // 发送者触发命令
        sender.invokeCommands();
    }

    // 命令接口
    interface Command {
        void execute();
    }

    // 具体命令类，实现了Command接口
    static class ConcreteCommand implements Command {
        private Receiver receiver;

        public ConcreteCommand(Receiver receiver) {
            this.receiver = receiver;
        }

        public void execute() {
            receiver.action();
        }
    }

    // 接收者类
    static class Receiver {
        private String name;

        public Receiver(String name) {
            this.name = name;
        }

        public void action() {
            System.out.println(name + " 执行命令...");
        }
    }

    // 发送者类
    static class Sender {
        private List<Command> commands = new ArrayList<>();

        public void addCommand(Command command) {
            commands.add(command);
        }

        public void invokeCommands() {
            System.out.println("发送者触发命令...");
            for (Command command : commands) {
                command.execute();
            }
        }
    }
}
