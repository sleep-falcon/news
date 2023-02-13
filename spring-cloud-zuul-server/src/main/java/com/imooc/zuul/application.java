package com.imooc.zuul;

import com.imooc.api.config.RabbitMQConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.EnableZuulServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class
                                    , MongoAutoConfiguration.class})
@ComponentScan(basePackages = {"com.imooc","com.org.n3r.idworker"})
@EnableEurekaClient
//@EnableZuulServer
@EnableZuulProxy   //这是EnableZuulServer的增强升级版，当zuul和eureka,ribbon等组件共同使用，则增强版即可
public class application {
    public static void main(String[] args) {
        SpringApplication.run(application.class,args);
    }
}
