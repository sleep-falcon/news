package com.imooc.article.controller;

import com.imooc.api.config.RabbitMQConfig;
import com.imooc.api.config.RabbitMQDelayConfig;
import com.imooc.api.controller.user.HelloControllerApi;
import com.imooc.article.stream.StreamService;
import com.imooc.grace.result.GraceJSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


@RestController
@RequestMapping("producer")
public class helloController implements HelloControllerApi {
    final static Logger logger = LoggerFactory.getLogger(helloController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private StreamService streamService;

    @GetMapping("/stream")
    public Object stream(){
        for(int i = 0;i<10;i++){
            streamService.eat("group: "+i);
        }
        return "ok!~";
    }

    @GetMapping("/hellp")
    public Object hello(){
        /**
         * rabbitmq 的路由器配规则 (routing key)
         * display.*.*->*代表一个占位符
         *  例如：display.do.download 匹配
         *       display.do.download.do 不匹配
         *
         * display.#->#带表多个占位符
         *  例如：display.do.download 匹配
         *       display.do.download.do 匹配
         *
         *
         */
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE,"article.hello","这是生产者的消息");
        return GraceJSONResult.ok("hello");
    }

    @GetMapping("/dalay")
    public Object delay(){
        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //设置消息的持久
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                //设置消息的延迟时间（单位毫秒）
                message.getMessageProperties().setDelay(5000);
                return message;
            }
        };
        rabbitTemplate.convertAndSend(RabbitMQDelayConfig.EXCHANGE_DELAY
                ,"delay.hello"
                ,"这是生产者的延迟消息～"
                ,messagePostProcessor);
        System.out.println("生产者发送的延迟消息"+new Date());
        return GraceJSONResult.ok();
    }


}
