package com.example.behavioral.interpreter;

/**
 * 解释器模式 - 反例
 * 问题：解析逻辑用字符串处理硬编码，扩展新语法需要修改 evaluate 方法
 */
public class InterpreterBadExample {
    public static void main(String[] args) {
        InterpreterBadExample demo = new InterpreterBadExample();
        // ❌ 只支持简单的 AND/OR，且逻辑全硬编码
        System.out.println(demo.evaluate("true AND false")); // false
        System.out.println(demo.evaluate("true OR false"));  // true
        System.out.println(demo.evaluate("true AND true"));  // true
        // 支持 NOT？要修改 evaluate 方法 ❌
    }

    // ❌ 字符串处理硬编码，扩展性差
    public boolean evaluate(String expression) {
        String[] parts = expression.split(" ");
        if (parts.length == 3) {
            boolean left  = Boolean.parseBoolean(parts[0]);
            String  op    = parts[1];
            boolean right = Boolean.parseBoolean(parts[2]);
            if ("AND".equals(op)) return left && right;
            if ("OR".equals(op))  return left || right;
        }
        if (parts.length == 1) {
            return Boolean.parseBoolean(parts[0]);
        }
        throw new IllegalArgumentException("不支持的表达式: " + expression);
    }
}
