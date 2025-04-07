package com.luguosong.basic;

/**
 * 增量/减量运算符前缀和后缀
 *
 * @author luguosong
 */
public class PrePostDemo {
	public static void main(String[] args) {
		int i = 3;
		i++;
		// prints 4
		System.out.println(i);
		++i;
		// prints 5
		System.out.println(i);
		// prints 6
		System.out.println(++i);
		// prints 6
		System.out.println(i++);
		// prints 7
		System.out.println(i);
	}
}
