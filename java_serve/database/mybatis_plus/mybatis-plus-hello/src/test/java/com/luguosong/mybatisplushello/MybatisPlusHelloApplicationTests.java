package com.luguosong.mybatisplushello;

import com.luguosong.mybatisplushello.entity.Employees;
import com.luguosong.mybatisplushello.mapper.EmployeesMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class MybatisPlusHelloApplicationTests {

	@Autowired
	private EmployeesMapper employeesMapper;

	@Test
	void testSelect() {
		List<Employees> employees = employeesMapper.selectList(null);
		System.out.println(employees);
	}

}
