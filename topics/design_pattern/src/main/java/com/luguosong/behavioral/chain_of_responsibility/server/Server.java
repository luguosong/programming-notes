package com.luguosong.behavioral.chain_of_responsibility.server;

import com.luguosong.behavioral.chain_of_responsibility.middleware.Middleware;

import java.util.HashMap;
import java.util.Map;

/**
 * 模拟服务
 *
 * @author luguosong
 */
public class Server {
	private Map<String, String> users = new HashMap<>();
	private Middleware middleware;

	/**
	 * 客户端将对象链传递给服务器。这提高了灵活性并使测试服务器类更容易。
	 */
	public void setMiddleware(Middleware middleware) {
		this.middleware = middleware;
	}

	/**
	 * 服务器从客户端获取电子邮件和密码并向链发送授权请求。
	 */
	public boolean logIn(String email, String password) {
		if (middleware.check(email, password)) {
			System.out.println("授权成功！");

			// 在此为授权用户做些有用的事情。

			return true;
		}
		return false;
	}

	public void register(String email, String password) {
		users.put(email, password);
	}

	public boolean hasEmail(String email) {
		return users.containsKey(email);
	}

	public boolean isValidPassword(String email, String password) {
		return users.get(email).equals(password);
	}
}
