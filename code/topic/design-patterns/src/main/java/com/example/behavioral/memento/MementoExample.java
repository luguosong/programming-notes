package com.example.behavioral.memento;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 备忘录模式 - 正例
 * TextEditor 自己创建和恢复快照（Memento），UndoManager 只管存储，不接触内部状态
 */
public class MementoExample {
    public static void main(String[] args) {
        TextEditor  editor  = new TextEditor();
        UndoManager history = new UndoManager();

        editor.type("Hello");
        history.save(editor.save()); // 保存快照 ✅

        editor.type(", World");
        history.save(editor.save()); // 保存快照

        editor.type("!!!");
        System.out.println("当前: " + editor.getContent()); // Hello, World!!!

        editor.restore(history.undo()); // ✅ 撤销，恢复到"Hello, World"
        System.out.println("撤销: " + editor.getContent()); // Hello, World

        editor.restore(history.undo()); // ✅ 再次撤销
        System.out.println("再撤销: " + editor.getContent()); // Hello
    }
}

// 备忘录：封装编辑器的内部状态快照（不可变）
class Memento {
    private final String content;  // 对外不可见，只有 TextEditor 能访问

    Memento(String content) { this.content = content; }

    String getContent() { return content; } // 包私有，TextEditor 可访问
}

// 原发器：TextEditor 自己负责创建和恢复快照
class TextEditor {
    private String content = "";

    public void type(String text) { content += text; }

    // ✅ 自己创建快照，不暴露内部状态
    public Memento save() { return new Memento(content); }

    // ✅ 自己恢复快照，外部只传递 Memento 对象
    public void restore(Memento memento) {
        if (memento != null) { content = memento.getContent(); }
    }

    public String getContent() { return content; }
}

// 管理者：只负责存储 Memento，不关心其内容
class UndoManager {
    private final Deque<Memento> history = new ArrayDeque<>();

    public void save(Memento memento) { history.push(memento); }

    public Memento undo() {
        if (history.isEmpty()) { System.out.println("没有可撤销的操作"); return null; }
        return history.pop(); // ✅ 返回 Memento，不需要理解其内部
    }
}
