package com.luguosong.basic;

/**
 * @author luguosong
 */
public class ConditionalDemo1 {
	public static void main(String[] args) {
		int value1 = 1;
		int value2 = 2;
		if ((value1 == 1) && (value2 == 2))
			System.out.println("value1 is 1 AND value2 is 2");
		if ((value1 == 1) || (value2 == 1))
			System.out.println("value1 is 1 OR value2 is 1");
	}
}
