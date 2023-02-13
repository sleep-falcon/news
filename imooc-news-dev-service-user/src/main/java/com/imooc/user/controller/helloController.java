package com.imooc.user.controller;

import com.imooc.api.controller.user.HelloControllerApi;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.utils.RedisOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class helloController implements HelloControllerApi {
    final static Logger logger = LoggerFactory.getLogger(helloController.class);

    @Autowired
    private RedisOperator redisOperator;

    public Object hello(){
        logger.info("hello~");

        return GraceJSONResult.ok("hello");
    }

    @GetMapping("/redis")
    public Object redis(){
        redisOperator.set("age","18");
        return GraceJSONResult.ok(redisOperator.get("age"));
    }

}
