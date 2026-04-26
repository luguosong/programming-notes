package com.example.behavioral.command;

/**
 * 命令模式 - 反例
 * 问题：操作直接修改状态，无法撤销
 */
public class CommandBadExample {
    public static void main(String[] args) {
        TextEditorBad editor = new TextEditorBad();
        editor.type("Hello");
        editor.type(", World");
        System.out.println("内容: " + editor.getContent());
        // ❌ 无法撤销，想回到 "Hello" 状态已不可能
        System.out.println("❌ 没有 undo 功能，无法回退");
    }
}

// ❌ 直接修改状态，没有历史记录
class TextEditorBad {
    private final StringBuilder content = new StringBuilder();

    public void type(String text) {
        content.append(text); // 直接修改，无法撤销 ❌
    }

    public String getContent() { return content.toString(); }
}
