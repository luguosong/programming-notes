package com.luguosong.behavioral.chain_of_responsibility.middleware.impl;

import com.luguosong.behavioral.chain_of_responsibility.middleware.Middleware;

/**
 * 具体处理程序：检查用户角色。
 *
 * @author luguosong
 */
public class RoleCheckMiddleware extends Middleware {
	@Override
	public boolean check(String email, String password) {
		if (email.equals("admin@example.com")) {
			System.out.println("Hello, admin!");
			return true;
		}
		System.out.println("Hello, user!");
		return checkNext(email, password);
	}
}
