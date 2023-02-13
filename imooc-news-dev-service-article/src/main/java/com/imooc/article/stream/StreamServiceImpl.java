package com.imooc.article.stream;

import com.imooc.pojo.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * 开启绑定器
 * 绑定通道channel
 */
@Service
@EnableBinding(MyStreamChannel.class)
public class StreamServiceImpl implements StreamService{

    @Autowired
    private MyStreamChannel streamChannel;

    @Override
    public void eat(String group) {
        streamChannel.output().send(MessageBuilder.withPayload(group).build());
    }

    @Override
    public void sendMsg() {
        AppUser appUser = new AppUser();
        appUser.setNickname("test");
        appUser.setId("2022");

        //消息通过绑定器发送给mq
        streamChannel.output().send(MessageBuilder.withPayload(appUser).build());
    }
}
