package com.luguosong.mybatisplushello;

import com.luguosong.mybatisplushello.entity.User;
import com.luguosong.mybatisplushello.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MybatisPlusHelloApplicationTests {

	@Autowired
	private UserMapper userMapper;

	@Test
	void testSelect() {
		List<User> users = userMapper.selectList(null);
		users.forEach(System.out::println);
	}

}
