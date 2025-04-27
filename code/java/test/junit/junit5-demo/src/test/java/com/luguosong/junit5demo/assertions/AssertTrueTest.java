package com.luguosong.junit5demo.assertions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author luguosong
 */
public class AssertTrueTest {
	@Test
	void test(){
		assertTrue(9>10,"断言失败，结果不为True");
	}
}
