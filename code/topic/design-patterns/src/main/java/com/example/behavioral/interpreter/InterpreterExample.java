package com.example.behavioral.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * 解释器模式 - 正例
 * 每种语法规则封装为一个 Expression 类，新增语法只需添加新类
 */
public class InterpreterExample {
    public static void main(String[] args) {
        // 构建表达式：(isLoggedIn AND isAdmin) OR isSuperUser
        Map<String, Boolean> context = new HashMap<>();
        context.put("isLoggedIn",  true);
        context.put("isAdmin",     false);
        context.put("isSuperUser", true);

        Expression isLoggedIn  = new VariableExpression("isLoggedIn");
        Expression isAdmin     = new VariableExpression("isAdmin");
        Expression isSuperUser = new VariableExpression("isSuperUser");

        // ✅ 组合表达式：(isLoggedIn AND isAdmin) OR isSuperUser
        Expression rule = new OrExpression(
            new AndExpression(isLoggedIn, isAdmin),
            isSuperUser
        );

        System.out.println("有权限访问？" + rule.interpret(context)); // true

        // ✅ NOT 语法：新增一个 NotExpression 类即可，不改其他代码
        Expression notAdmin = new NotExpression(isAdmin);
        System.out.println("非管理员？" + notAdmin.interpret(context)); // true
    }
}

// 抽象表达式
interface Expression {
    boolean interpret(Map<String, Boolean> context);
}

// 终结符：变量（从上下文中读取值）
class VariableExpression implements Expression {
    private final String name;
    public VariableExpression(String name) { this.name = name; }

    @Override
    public boolean interpret(Map<String, Boolean> context) {
        return context.getOrDefault(name, false);
    }
}

// 非终结符：AND
class AndExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public AndExpression(Expression left, Expression right) {
        this.left  = left;
        this.right = right;
    }

    @Override
    public boolean interpret(Map<String, Boolean> context) {
        return left.interpret(context) && right.interpret(context);
    }
}

// 非终结符：OR
class OrExpression implements Expression {
    private final Expression left;
    private final Expression right;

    public OrExpression(Expression left, Expression right) {
        this.left  = left;
        this.right = right;
    }

    @Override
    public boolean interpret(Map<String, Boolean> context) {
        return left.interpret(context) || right.interpret(context);
    }
}

// ✅ 非终结符：NOT（新增语法，只需一个新类）
class NotExpression implements Expression {
    private final Expression expression;
    public NotExpression(Expression expression) { this.expression = expression; }

    @Override
    public boolean interpret(Map<String, Boolean> context) {
        return !expression.interpret(context);
    }
}
