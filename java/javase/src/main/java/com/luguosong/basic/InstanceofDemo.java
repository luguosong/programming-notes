package com.luguosong.basic;

/**
 * instanceof运算符示例
 *
 * @author luguosong
 */
public class InstanceofDemo {
	public static void main(String[] args) {

		Parent obj1 = new Parent();
		Parent obj2 = new Child();

		// => true
		System.out.println("obj1 instanceof Parent: "
				+ (obj1 instanceof Parent));

		// => false
		System.out.println("obj1 instanceof Child: "
				+ (obj1 instanceof Child));

		// => false
		System.out.println("obj1 instanceof MyInterface: "
				+ (obj1 instanceof MyInterface));

		// => true
		System.out.println("obj2 instanceof Parent: "
				+ (obj2 instanceof Parent));

		// => true
		System.out.println("obj2 instanceof Child: "
				+ (obj2 instanceof Child));

		// => true
		System.out.println("obj2 instanceof MyInterface: "
				+ (obj2 instanceof MyInterface));
	}
}

class Parent {
}

class Child extends Parent implements MyInterface {
}

interface MyInterface {
}
