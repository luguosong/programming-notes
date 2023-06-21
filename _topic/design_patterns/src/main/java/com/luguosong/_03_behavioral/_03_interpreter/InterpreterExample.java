package com.luguosong._03_behavioral._03_interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luguosong
 */
public class InterpreterExample {
    public static void main(String[] args) {
        // 创建表达式
        Expression expression = new SubtractExpression(
                new AddExpression(
                        new NumberExpression(10),
                        new NumberExpression(5)
                ),
                new NumberExpression(3)
        );

        // 创建上下文
        Context context = new Context();
        context.setVariable("x", 5);

        // 解释和执行表达式
        double result = expression.interpret(context);
        System.out.println("Result: " + result); // 输出: Result: 7.0
    }

    /**
     * 抽象表达式
     */
    static interface Expression {
        double interpret(Context context);
    }

    /**
     * 终结符表达式
     */
    static class NumberExpression implements Expression {
        private double number;

        public NumberExpression(double number) {
            this.number = number;
        }

        @Override
        public double interpret(Context context) {
            return number;
        }
    }

    /**
     * 非终结符表达式
     */
    static class AddExpression implements Expression {
        private Expression leftExpression;
        private Expression rightExpression;

        public AddExpression(Expression leftExpression, Expression rightExpression) {
            this.leftExpression = leftExpression;
            this.rightExpression = rightExpression;
        }

        @Override
        public double interpret(Context context) {
            return leftExpression.interpret(context) + rightExpression.interpret(context);
        }
    }

    /**
     * 非终结符表达式
     */
    static class SubtractExpression implements Expression {
        private Expression leftExpression;
        private Expression rightExpression;

        public SubtractExpression(Expression leftExpression, Expression rightExpression) {
            this.leftExpression = leftExpression;
            this.rightExpression = rightExpression;
        }

        @Override
        public double interpret(Context context) {
            return leftExpression.interpret(context) - rightExpression.interpret(context);
        }
    }


    static class Context {
        /**
         * 用于存储相关变量和对应的值
         */
        private Map<String, Double> variables;

        public Context() {
            variables = new HashMap<>();
        }

        public void setVariable(String variableName, double value) {
            variables.put(variableName, value);
        }

        public double getVariable(String variableName) {
            Double value = variables.get(variableName);
            if (value == null) {
                throw new IllegalArgumentException("Variable not found: " + variableName);
            }
            return value;
        }
    }


}
