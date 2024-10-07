package com.luguosong.behavioral.chain_of_responsibility.middleware.impl;

import com.luguosong.behavioral.chain_of_responsibility.middleware.Middleware;

/**
 * 具体处理程序。检查是否有过多的登录失败请求。
 *
 * @author luguosong
 */
public class ThrottlingMiddleware extends Middleware {
	private int requestPerMinute;
	private int request;
	private long currentTime;

	public ThrottlingMiddleware(int requestPerMinute) {
		this.requestPerMinute = requestPerMinute;
		this.currentTime = System.currentTimeMillis();
	}

	@Override
	public boolean check(String email, String password) {
		if (System.currentTimeMillis() > currentTime + 60_000) {
			request = 0;
			currentTime = System.currentTimeMillis();
		}

		request++;

		if (request > requestPerMinute) {
			System.out.println("请求次数超出限制！");
			Thread.currentThread().stop();
		}
		return checkNext(email, password);
	}
}
