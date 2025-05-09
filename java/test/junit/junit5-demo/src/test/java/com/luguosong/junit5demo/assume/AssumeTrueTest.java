package com.luguosong.junit5demo.assume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author luguosong
 */
public class AssumeTrueTest {
	@Test
	void test1() {
		int input = 10;

		assumeTrue(input > 0, "输入必须是正数");
	}

	@Test
	void test2() {
		int input = -10;

		assumeTrue(input > 0, "输入必须是正数");
		System.out.println("test");
	}
}
