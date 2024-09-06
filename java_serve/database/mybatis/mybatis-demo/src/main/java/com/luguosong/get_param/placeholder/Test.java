package com.luguosong.get_param.placeholder;


import com.luguosong.get_param.placeholder.mapper.EmployeesMapper;
import com.luguosong.get_param.pojo.Employees;
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
        InputStream is = Resources.getResourceAsStream("mybatis-config-hello.xml");
        SqlSessionFactoryBuilder builder = new SqlSessionFactoryBuilder();
        SqlSessionFactory factory = builder.build(is);
        // 表示Java程序与数据库之间的会话
        SqlSession sqlSession = factory.openSession();
        // 动态创建Mapper接口对应的对象
        EmployeesMapper mapper = sqlSession.getMapper(EmployeesMapper.class);
        Employees employees1 = mapper.getEmployeesById(3);
        Employees employees2 = mapper.getEmployeesByName("芳");
        System.out.println(employees1);
        System.out.println(employees2);
    }
}
