package com.imooc.article.task;

import com.imooc.article.service.ArticleService;
import com.imooc.pojo.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


//@Configuration  //标记配置类
//@EnableScheduling //开启定时任务
public class TaskPublishArticles {

    @Autowired
    private  ArticleService articleService;

    @Scheduled(cron = "0/3 * * * * ?")
    private  void publishArticles(){


        //调用文章service,把当前时间应该定时发布的文章改为即时
        articleService.updateAppointToPublich();
    }
}
