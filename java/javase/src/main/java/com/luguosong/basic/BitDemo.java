package com.luguosong.basic;

/**
 * 位运算符
 *
 * @author luguosong
 */
public class BitDemo {
	public static void main(String[] args) {
		int bitmask = 0x000F;
		int val = 0x2222;
		// prints "2"
		System.out.println(val & bitmask);
	}
}
