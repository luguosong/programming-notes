package com.luguosong.behavioral.chain_of_responsibility.middleware;

/**
 * 基础处理者
 *
 * @author luguosong
 */
public abstract class Middleware {

	//下一个处理者
	private Middleware next;

	/*
	* 构建责任链
	* */
	public static Middleware link(Middleware first, Middleware... chain) {
		Middleware head = first;
		for (Middleware nextInChain: chain) {
			head.next = nextInChain;
			head = nextInChain;
		}
		return first;
	}

	/*
	 * 抽象处理方法，用于处理请求
	 * */
	public abstract boolean check(String email, String password);

	/*
	 * 处理者公共代码
	 * 对链中的下一个对象进行检查，如果已到达链中的最后一个对象，则结束遍历。
	 * */
	protected boolean checkNext(String email, String password) {
		if (next == null) {
			return true;
		}
		return next.check(email, password);
	}
}
