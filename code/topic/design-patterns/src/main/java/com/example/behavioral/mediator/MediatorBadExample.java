package com.example.behavioral.mediator;

/**
 * 中介者模式 - 反例
 * 问题：用户直接持有所有联系人引用，耦合度呈 O(n²) 增长
 */
public class MediatorBadExample {
    public static void main(String[] args) {
        UserBad alice = new UserBad("Alice");
        UserBad bob   = new UserBad("Bob");
        UserBad carol = new UserBad("Carol");

        // ❌ 每个用户都要持有其他用户的引用
        alice.addContact(bob);
        alice.addContact(carol);
        bob.addContact(alice);
        bob.addContact(carol);
        carol.addContact(alice);
        carol.addContact(bob);

        alice.sendMessage("大家好！");
        // 新增用户 Dave？每个人都要手动 addContact(dave) ❌
    }
}

class UserBad {
    private final String name;
    private final java.util.List<UserBad> contacts = new java.util.ArrayList<>();

    public UserBad(String name) { this.name = name; }

    public void addContact(UserBad user) { contacts.add(user); }

    public void sendMessage(String msg) {
        System.out.println(name + " 发送: " + msg);
        // ❌ 直接调用每个联系人
        contacts.forEach(c -> c.receive(name, msg));
    }

    public void receive(String from, String msg) {
        System.out.println(name + " 收到来自 " + from + " 的消息: " + msg);
    }
}
