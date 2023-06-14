package com.luguosong._02_structural._06_flyweight;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luguosong
 */
public class FlyweightExample {
    public static void main(String[] args) {
        FlyweightFactory flyweightFactory = new FlyweightFactory();

        // 创建多个情景对象
        Context context1 = new Context(12, "Red");
        Context context2 = new Context(14, "Blue");
        Context context3 = new Context(12, "Green");

        // 客户端通过享元工厂获取或创建享元对象，并传递情景对象作为外在状态参数
        CharacterFlyweight characterA = flyweightFactory.getCharacterFlyweight('A');
        characterA.draw(context1);

        CharacterFlyweight characterB = flyweightFactory.getCharacterFlyweight('B');
        characterB.draw(context2);

        CharacterFlyweight characterA2 = flyweightFactory.getCharacterFlyweight('A');
        characterA2.draw(context3);
    }

    // 享元实现类
    static class CharacterFlyweight {
        /**
         * 内部状态
         */
        private char character;

        public CharacterFlyweight(char character) {
            this.character = character;
        }

        public void draw(Context context) {
            System.out.println("绘制字符(内部状态) '" + character + "' 大小（外部状态） " + context.getSize() + " ，颜色（外部状态） " + context.getColor());
        }
    }

    // 情景类（外部状态）
    static class Context {
        private int size;
        private String color;

        public Context(int size, String color) {
            this.size = size;
            this.color = color;
        }

        public int getSize() {
            return size;
        }

        public String getColor() {
            return color;
        }
    }

    // 享元工厂
    static class FlyweightFactory {
        private Map<Character, CharacterFlyweight> flyweights = new HashMap<>();

        public CharacterFlyweight getCharacterFlyweight(char character) {
            CharacterFlyweight flyweight = flyweights.get(character);

            if (flyweight == null) {
                flyweight = new CharacterFlyweight(character);
                flyweights.put(character, flyweight);
            }

            return flyweight;
        }
    }
}
