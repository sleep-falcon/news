package com.imooc.article.html;

import com.imooc.api.config.RabbitMQConfig;
import com.imooc.article.html.controller.ArticleHTMLComponent;
import com.imooc.article.html.controller.ArticleHTMLController;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;

@Component
public class RabbitMQComsumer {
    @Autowired
    private ArticleHTMLComponent htmlComponent;

    @RabbitListener(queues = {RabbitMQConfig.QUEUE_DOWNLOAD_HTML})
    public void watchQueue(String payload, Message message){
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        if(routingKey.equalsIgnoreCase("article.download.do")){
            String articleId = payload.split(",")[0];
            String fieldId = payload.split(",")[1];
            try {
                htmlComponent.download(articleId,fieldId);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
