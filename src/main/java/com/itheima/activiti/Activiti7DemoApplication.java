package com.itheima.activiti;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.itheima.activiti"})
@MapperScan("com.itheima.activiti.mapper")
@EnableCaching
public class Activiti7DemoApplication {
    private Logger logger = LoggerFactory.getLogger(Activiti7DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(Activiti7DemoApplication.class, args);
    }
}
