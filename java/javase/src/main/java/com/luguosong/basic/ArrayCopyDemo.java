package com.luguosong.basic;

/**
 * 数组复制
 *
 * @author luguosong
 */
public class ArrayCopyDemo {
	public static void main(String[] args) {
		String[] copyFrom = {
				"Affogato", "Americano", "Cappuccino", "Corretto", "Cortado",
				"Doppio", "Espresso", "Frappucino", "Freddo", "Lungo", "Macchiato",
				"Marocchino", "Ristretto" };

		String[] copyTo = new String[7];
		System.arraycopy(copyFrom, 2, copyTo, 0, 7);
		for (String coffee : copyTo) {
			System.out.print(coffee + " ");
		}
	}
}
