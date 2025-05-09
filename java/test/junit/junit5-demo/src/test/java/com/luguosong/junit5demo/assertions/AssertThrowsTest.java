package com.luguosong.junit5demo.assertions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author luguosong
 */
public class AssertThrowsTest {
	@Test
	void test() {
		ArithmeticException exception = assertThrows(ArithmeticException.class, () -> {
			int a = 1 / 0;
		});

		// 会返回异常
		System.out.println(exception.getMessage());
	}
}
