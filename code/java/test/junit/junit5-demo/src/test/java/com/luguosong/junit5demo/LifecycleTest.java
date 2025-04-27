package com.luguosong.junit5demo;

import org.junit.jupiter.api.*;

/**
 * @author luguosong
 */
public class LifecycleTest {

	@BeforeAll
	static void initAll() {
		System.out.println("执行BeforeAll方法");
	}

	@BeforeEach
	void init() {
		System.out.println("执行BeforeEach方法");
	}

	@Test
	void test1() {
		System.out.println("执行测试方法1");
	}

	@Test
	void test2() {
		System.out.println("执行测试方法2");
	}

	@AfterEach
	void tearDown() {
		System.out.println("执行AfterEach方法");
	}

	@AfterAll
	static void tearDownAll() {
		System.out.println("执行AfterAll方法");
	}
}
