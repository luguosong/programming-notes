package com.luguosong.basic;

/**
 * 使用java.util.Arrays工具类复制数组
 *
 * @author luguosong
 */
public class ArrayCopyOfDemo2 {
	public static void main(String[] args) {
		String[] copyFrom = {
				"Affogato", "Americano", "Cappuccino", "Corretto", "Cortado",
				"Doppio", "Espresso", "Frappucino", "Freddo", "Lungo", "Macchiato",
				"Marocchino", "Ristretto" };

		String[] copyTo = java.util.Arrays.copyOfRange(copyFrom, 2, 9);
		for (String coffee : copyTo) {
			System.out.print(coffee + " ");
		}
	}
}
