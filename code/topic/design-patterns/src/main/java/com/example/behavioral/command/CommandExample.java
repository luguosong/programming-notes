package com.example.behavioral.command;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 命令模式 - 正例
 * 每个操作封装为 Command 对象，可入队排序、可撤销
 */
public class CommandExample {
    public static void main(String[] args) {
        TextEditor editor   = new TextEditor();
        EditorHistory history = new EditorHistory();

        Command typeHello = new TypeCommand(editor, "Hello");
        Command typeWorld = new TypeCommand(editor, ", World");

        history.execute(typeHello);
        history.execute(typeWorld);
        System.out.println("输入后: " + editor.getContent()); // Hello, World

        history.undo(); // ✅ 撤销最后一次输入
        System.out.println("撤销后: " + editor.getContent()); // Hello

        history.undo(); // ✅ 再撤销
        System.out.println("再撤销: " + editor.getContent()); // (空)
    }
}

// 命令接口
interface Command {
    void execute();
    void undo();
}

// 接收者：文本编辑器（只负责实际操作，不管历史）
class TextEditor {
    private final StringBuilder content = new StringBuilder();

    public void appendText(String text)  { content.append(text);                                  }
    public void deleteText(int length)   { content.delete(content.length() - length, content.length()); }
    public String getContent()           { return content.toString();                              }
}

// 具体命令：输入文本
class TypeCommand implements Command {
    private final TextEditor editor;
    private final String     text;

    public TypeCommand(TextEditor editor, String text) {
        this.editor = editor;
        this.text   = text;
    }

    @Override public void execute() { editor.appendText(text);         } // ✅ 执行
    @Override public void undo()    { editor.deleteText(text.length()); } // ✅ 撤销
}

// 调用者：历史记录管理器
class EditorHistory {
    private final Deque<Command> history = new ArrayDeque<>();

    public void execute(Command cmd) {
        cmd.execute();
        history.push(cmd); // 执行后入栈
    }

    public void undo() {
        if (history.isEmpty()) { System.out.println("没有可撤销的操作"); return; }
        history.pop().undo(); // 弹出栈顶命令并撤销 ✅
    }
}
