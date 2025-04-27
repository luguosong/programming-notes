package com.luguosong.junit5demo.assertions;

import org.junit.jupiter.api.Test;

import static java.time.Duration.ofMinutes;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/**
 * @author luguosong
 */
public class AssertTimeoutTest {
	@Test
	void test() {
		//
		String actualResult = assertTimeout(
				//设置超时时间
				ofMinutes(2),
				//执行代码
				() -> {
					// 执行耗时少于两分钟的任务。
					return "返回结果";
				}
		);
		System.out.println(actualResult);
	}
}
