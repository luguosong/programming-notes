package com.luguosong.junit5demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author luguosong
 */
@DisplayName("测试类")
public class DisplayNameTest {
	@Test
	@DisplayName("测试方法")
	void test() {
		System.out.println("test");
	}
}
