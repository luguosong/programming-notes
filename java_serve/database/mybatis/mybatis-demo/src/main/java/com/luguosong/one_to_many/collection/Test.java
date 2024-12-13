package com.luguosong.one_to_many.collection;

import com.luguosong.one_to_many.collection.mapper.DepartmentsMapper;
import com.luguosong.one_to_many.pojo.Departments;
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
        DepartmentsMapper mapper = sqlSession.getMapper(DepartmentsMapper.class);
        Departments department = mapper.getDepartmentById(2);

        System.out.println(department);
    }
}
