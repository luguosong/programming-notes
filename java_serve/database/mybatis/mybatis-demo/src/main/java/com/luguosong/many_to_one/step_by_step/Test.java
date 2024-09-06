package com.luguosong.many_to_one.step_by_step;


import com.luguosong.many_to_one.pojo.Employees;
import com.luguosong.many_to_one.step_by_step.mapper.EmployeesMapper;
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
        Employees employees = mapper.getEmployeesById(5);

        //因为开启了延迟加载，如果只是获取员工属性，只会执行第一步sql
        System.out.println(employees.getFirstName());

        //当获取全部属性（包括部门属性），会执行第二步sql
        System.out.println(employees);
    }
}
