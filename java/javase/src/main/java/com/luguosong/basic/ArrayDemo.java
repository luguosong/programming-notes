package com.luguosong.basic;

/**
 * 数组入门案例
 *
 * @author luguosong
 */
public class ArrayDemo {
	public static void main(String[] args) {
		// 声明一个整数数组
		int[] anArray;

		// 为10个整数分配内存
		anArray = new int[10];

		// 初始化第一个元素
		anArray[0] = 100;
		// 初始化第二个元素
		anArray[1] = 200;
		// 等等...
		anArray[2] = 300;
		anArray[3] = 400;
		anArray[4] = 500;
		anArray[5] = 600;
		anArray[6] = 700;
		anArray[7] = 800;
		anArray[8] = 900;
		anArray[9] = 1000;

		System.out.println("索引0处的元素: "
				+ anArray[0]);
		System.out.println("索引1处的元素: "
				+ anArray[1]);
		System.out.println("索引2处的元素: "
				+ anArray[2]);
		System.out.println("索引3处的元素: "
				+ anArray[3]);
		System.out.println("索引4处的元素: "
				+ anArray[4]);
		System.out.println("索引5处的元素: "
				+ anArray[5]);
		System.out.println("索引6处的元素: "
				+ anArray[6]);
		System.out.println("索引7处的元素: "
				+ anArray[7]);
		System.out.println("索引8处的元素: "
				+ anArray[8]);
		System.out.println("索引9处的元素: "
				+ anArray[9]);
	}
}
