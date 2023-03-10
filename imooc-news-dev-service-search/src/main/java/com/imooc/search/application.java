package com.imooc.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class
                                    , MongoAutoConfiguration.class})
@ComponentScan(basePackages = {"com.imooc","com.org.n3r.idworker"})

public class application {
    public static void main(String[] args) {
        SpringApplication.run(application.class,args);
    }
}
