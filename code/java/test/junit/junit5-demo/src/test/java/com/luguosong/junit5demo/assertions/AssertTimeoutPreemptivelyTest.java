package com.luguosong.junit5demo.assertions;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * @author luguosong
 */
public class AssertTimeoutPreemptivelyTest {
	@Test
	void test(){
		assertTimeoutPreemptively(ofMillis(10), () -> {
			// 模拟耗时超过10毫秒的任务。
			new CountDownLatch(1).await();
		});
	}
}
