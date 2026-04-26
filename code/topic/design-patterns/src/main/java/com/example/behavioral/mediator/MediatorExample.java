package com.example.behavioral.mediator;

import java.util.ArrayList;
import java.util.List;

/**
 * 中介者模式 - 正例
 * ChatRoom 作为中介者统一协调，用户只与中介者交互
 */
public class MediatorExample {
    public static void main(String[] args) {
        ChatMediator room = new ChatRoom("技术交流群");

        ChatUser alice = new ChatUser("Alice", room);
        ChatUser bob   = new ChatUser("Bob",   room);
        ChatUser carol = new ChatUser("Carol", room);

        room.register(alice);
        room.register(bob);
        room.register(carol);

        alice.send("大家好！");           // 中介者广播给其他所有人 ✅
        bob.send("Alice 你好！");
        // ✅ 新增用户 Dave：只需 room.register(dave)，其他用户代码不变
    }
}

// 中介者接口
interface ChatMediator {
    void register(ChatUser user);
    void broadcast(ChatUser sender, String message);
}

// 具体中介者：聊天室（统一协调所有用户通信）
class ChatRoom implements ChatMediator {
    private final String          name;
    private final List<ChatUser>  users = new ArrayList<>();

    public ChatRoom(String name) { this.name = name; }

    @Override
    public void register(ChatUser user) {
        users.add(user);
        System.out.println(user.getName() + " 加入 [" + name + "]");
    }

    @Override
    public void broadcast(ChatUser sender, String message) {
        // 发送给除发送者以外的所有人
        users.stream()
             .filter(u -> u != sender)
             .forEach(u -> u.receive(sender.getName(), message));
    }
}

// 同事类：用户（只持有中介者引用，不持有其他用户）
class ChatUser {
    private final String       name;
    private final ChatMediator mediator; // ✅ 只与中介者交互

    public ChatUser(String name, ChatMediator mediator) {
        this.name     = name;
        this.mediator = mediator;
    }

    public String getName() { return name; }

    public void send(String message) {
        System.out.println("[" + name + "] 发送: " + message);
        mediator.broadcast(this, message); // 委托给中介者
    }

    public void receive(String from, String message) {
        System.out.println("[" + name + "] 收到来自 " + from + " 的消息: " + message);
    }
}
