package com.imooc.api.config;

import com.imooc.api.intercepters.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class IntercepterConfig implements WebMvcConfigurer {
    @Bean
    public PassportIntercepter passportIntercepter(){
        return new PassportIntercepter();
    }

    @Bean
    public ArticlereadInterceptor articlereadInterceptor (){
        return new ArticlereadInterceptor();
    }


    @Bean
    public UserTokenIntercepter userTokenIntercepter(){
        return new UserTokenIntercepter();
    }

    @Bean
    public UserActiveIntercepter userActiveIntercepter(){
        return new UserActiveIntercepter();
    }

    @Bean
    public AdminTokenIntercepter adminTokenIntercepter(){
        return new AdminTokenIntercepter();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(passportIntercepter()).addPathPatterns("/passport/getSMSCode");

        registry.addInterceptor(userTokenIntercepter()).addPathPatterns("/user/getAccountInfo")
                .addPathPatterns("/user/updateUserInfo").addPathPatterns("/fs/uploadFace")
                .addPathPatterns("/fs/uploadSomeFiles").addPathPatterns("/fans/follow")
                .addPathPatterns("/fans/unfollow");

        registry.addInterceptor(userActiveIntercepter())
                .addPathPatterns("/fs/uploadSomeFiles")
                .addPathPatterns("/fans/follow")
                .addPathPatterns("/fans/unfollow");


        registry.addInterceptor(adminTokenIntercepter()).addPathPatterns("/adminMng/adminIsExist")
                .addPathPatterns("/adminMng/addNewAdmin").addPathPatterns("/adminMng/getAdminList")
                .addPathPatterns("/fs/uploadToGridFS").addPathPatterns("/fs/readInGridFS")
                .addPathPatterns("/friendLinkMng/saveOrUpdateFriendLink")
                .addPathPatterns("/friendLinkMng/getFriendLinkList")
                .addPathPatterns("/friendLinkMng/delete")
                .addPathPatterns("/appUser/queryAll")
                .addPathPatterns("/appUser/userDetail")
                .addPathPatterns("/appUser/freezeUserOrNot");

        registry.addInterceptor(articlereadInterceptor())
                .addPathPatterns("/portal/article/readArticle");

    }

}
