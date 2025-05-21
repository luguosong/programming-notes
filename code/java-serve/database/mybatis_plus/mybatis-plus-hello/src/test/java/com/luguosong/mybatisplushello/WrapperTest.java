package com.luguosong.mybatisplushello;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.luguosong.mybatisplushello.entity.User;
import com.luguosong.mybatisplushello.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author luguosong
 */
@SpringBootTest
public class WrapperTest {
	@Autowired
	private UserMapper userMapper;

	/*
	 * 条件查询
	 * */
	@Test
	void testSelect() {
		/*
		 * 构造条件
		 * */
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.select("name", "age"); //查询指定字段
		wrapper.like("name", "o");
		wrapper.ge("age", 20);

		/*
		 * 查询
		 * */
		List<User> users = userMapper.selectList(wrapper);
		users.forEach(System.out::println);
	}

	/*
	 * 条件更新
	 * */
	@Test
	void testUpdate() {
		//构造更新条件
		UpdateWrapper<User> wrapper = new UpdateWrapper<>();
		wrapper.setSql("age=10");
		wrapper.in("id", List.of(1, 2, 3));

		//更新数据
		userMapper.update(null, wrapper);
	}

	/*
	 * 使用Lambda构造条件
	 * 👍🏻推荐使用，防止硬编码
	 * */
	@Test
	void testLambdaSelect() {
		/*
		 * 构造条件
		 * */
		LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
		wrapper.select(User::getName, User::getAge); //查询指定字段
		wrapper.like(User::getName, "o");
		wrapper.ge(User::getAge, 20);

		/*
		 * 查询
		 * */
		List<User> users = userMapper.selectList(wrapper);
		users.forEach(System.out::println);
	}
}
