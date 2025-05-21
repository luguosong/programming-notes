package com.luguosong.get_param.object_and_mapper;


import com.luguosong.get_param.object_and_mapper.mapper.EmployeesMapper;
import com.luguosong.get_param.pojo.Employees;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

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
        Employees searchEmployees = new Employees();
        searchEmployees.setFirstName("强");
        searchEmployees.setLastName("王");
        Employees employees1 = mapper.getEmployeesByName1(searchEmployees);
        HashMap<String, String> searchMap = new HashMap<>();
        searchMap.put("firstName", "芳");
        searchMap.put("lastName", "陈");
        Employees employees2 = mapper.getEmployeesByName2(searchMap);
        System.out.println(employees1);
        System.out.println(employees2);
    }
}
