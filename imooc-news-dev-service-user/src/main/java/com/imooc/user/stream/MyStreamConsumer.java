package com.imooc.user.stream;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

/**
 * 构建消费端
 */
@Component
@EnableBinding(MyStreamChannel.class)
public class MyStreamConsumer {

//    /**
//     * 监听并实现消息的消费和相关业务处理
//     * @param object
//     */
//    @StreamListener(MyStreamChannel.INPUT)
//    public void receiveMsg(Object object){
//        System.out.println(object.toString());
//
//    }

    @StreamListener(MyStreamChannel.INPUT)
    public void receiveMsg(String group){
        System.out.println(group);

    }

}
