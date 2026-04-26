package com.example.behavioral.memento;

/**
 * 备忘录模式 - 反例
 * 问题：撤销时需要在 UndoManagerBad 里直接暴露和操作编辑器内部状态
 */
public class MementoBadExample {
    public static void main(String[] args) {
        TextEditorBad editor  = new TextEditorBad();
        UndoManagerBad undo   = new UndoManagerBad();

        editor.setContent("Hello");
        undo.save(editor.getContent()); // ❌ 必须手动获取内部状态

        editor.setContent("Hello, World");
        undo.save(editor.getContent()); // ❌ 仍然直接暴露内部状态

        System.out.println("当前: " + editor.getContent()); // Hello, World

        // 撤销时，又得直接操作内部状态
        String last = undo.pop();
        editor.setContent(last); // ❌ 外部直接 set 内部状态
        System.out.println("撤销后: " + editor.getContent()); // Hello
    }
}

// ❌ 编辑器内部状态对外完全暴露
class TextEditorBad {
    private String content = "";
    public String getContent()            { return content; }
    public void   setContent(String text) { content = text; }
}

// ❌ 撤销管理器必须知道编辑器的内部字段类型
class UndoManagerBad {
    private final java.util.Deque<String> stack = new java.util.ArrayDeque<>();
    public void   save(String state) { stack.push(state);          }
    public String pop()              { return stack.isEmpty() ? "" : stack.pop(); }
}
