package com.imooc.article.html;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.imooc","com.org.n3r.idworker"})
public class application {
    public static void main(String[] args) {
        SpringApplication.run(application.class,args);
    }
}
