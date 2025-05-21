package com.luguosong.mybatisplushello;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.luguosong.mybatisplushello.mapper")
public class MybatisPlusHelloApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlusHelloApplication.class, args);
    }

}
