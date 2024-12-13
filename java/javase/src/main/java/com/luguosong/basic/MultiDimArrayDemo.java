package com.luguosong.basic;

/**
 * 多维数组
 *
 * @author luguosong
 */
public class MultiDimArrayDemo {
	public static void main(String[] args) {
		String[][] names = {
				{"Mr. ", "Mrs. ", "Ms. "},
				{"Smith", "Jones"}
		};
		// Mr. Smith
		System.out.println(names[0][0] + names[1][0]);
		// Ms. Jones
		System.out.println(names[0][2] + names[1][1]);
	}
}
