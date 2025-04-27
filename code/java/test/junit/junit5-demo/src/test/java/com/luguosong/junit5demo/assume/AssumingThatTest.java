package com.luguosong.junit5demo.assume;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assumptions.assumingThat;

/**
 * @author luguosong
 */
public class AssumingThatTest {
	@Test
	void test() {
		assumingThat(1 > 2,
				() -> {
					System.out.println("仅在假设成立时执行");
				});

		// 在所有环境中执行这些断言
		System.out.println("总是执行");
	}
}
