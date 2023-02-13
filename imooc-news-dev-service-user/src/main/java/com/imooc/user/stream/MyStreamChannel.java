package com.imooc.user.stream;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;

/**
 * 声明构建通道channel
 */
@Component
public interface MyStreamChannel {
    String INPUT = "myInput";
    String OUTPUT = "myOutput";
    @Output(MyStreamChannel.OUTPUT)
    MessageChannel output();

    @Input(MyStreamChannel.INPUT)
    SubscribableChannel input();


}
