package com.luguosong.junit5demo.assertions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author luguosong
 */
public class AssertEqualsTest {
	@Test
	void test() {
		/*
		 * 第一个参数为期望值，第二个参数为实际值
		 * */
		assertEquals(3, 1 + 1);
		System.out.println("test");
	}

	@Test
	void test2() {
		/*
		 * 第一个参数为期望值，第二个参数为实际值
		 * */
		assertEquals(3, 1 + 1, "自定义失败消息：执行结果不是期望的3");
	}
}
