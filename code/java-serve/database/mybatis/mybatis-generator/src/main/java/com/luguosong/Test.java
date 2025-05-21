package com.luguosong;

import com.luguosong.mapper.EmployeesMapper;
import com.luguosong.pojo.Employees;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author luguosong
 */
public class Test {
    public static void main(String[] args) throws IOException {
        InputStream is = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory sessionFactory = builder.build(is);
        SqlSession sqlSession = sessionFactory.openSession();
        EmployeesMapper employeesMapper = sqlSession.getMapper(EmployeesMapper.class);
        Employees employees = employeesMapper.selectByPrimaryKey(2);
        System.out.println(employees);
    }
}
