package com.luguosong.behavioral.chain_of_responsibility;

import com.luguosong.behavioral.chain_of_responsibility.middleware.Middleware;
import com.luguosong.behavioral.chain_of_responsibility.middleware.impl.RoleCheckMiddleware;
import com.luguosong.behavioral.chain_of_responsibility.middleware.impl.ThrottlingMiddleware;
import com.luguosong.behavioral.chain_of_responsibility.middleware.impl.UserExistsMiddleware;
import com.luguosong.behavioral.chain_of_responsibility.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author luguosong
 */
public class Demo {
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	private static Server server;

	private static void init() {
		server = new Server();
		server.register("admin@example.com", "12345678");
		server.register("user@example.com", "123456");

		// 所有检查都已连接。客户可以使用相同的组件构建各种链。
		Middleware middleware = Middleware.link(
				new ThrottlingMiddleware(2),
				new UserExistsMiddleware(server),
				new RoleCheckMiddleware()
		);

		// 服务器从客户端代码获取链。
		server.setMiddleware(middleware);
	}

	public static void main(String[] args) throws IOException {
		init();

		boolean success;
		do {
			System.out.print("输入邮箱: ");
			String email = reader.readLine();
			System.out.print("输入密码: ");
			String password = reader.readLine();
			success = server.logIn(email, password);
		} while (!success);
	}
}
