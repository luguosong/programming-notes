package com.luguosong.dynamic_sql.sql_if;


import com.luguosong.dynamic_sql.pojo.Employees;
import com.luguosong.dynamic_sql.sql_if.mapper.EmployeesMapper;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

        EmployeesMapper mapper = sqlSession.getMapper(EmployeesMapper.class);

        Employees select1 = new Employees();
        select1.setFirstName("强");
        List<Employees> employees1 = mapper.selectEmployees(select1);
        System.out.println(employees1);

        Employees select2 = new Employees();
        select2.setLastName("王");
        List<Employees> employees2 = mapper.selectEmployees(select2);
        System.out.println(employees2);

    }
}
