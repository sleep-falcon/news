package com.imooc.api.intercepters;

import com.imooc.utils.IPUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ArticlereadInterceptor extends BaseIntercepter implements HandlerInterceptor {

    public static final String REDIS_ALREADY_READ = "redis_already_read";

    /**
     * 拦截请求，在访问controller调用之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userIp = IPUtil.getRequestIp(request);
        String articleId = request.getParameter("articleId");
        //设置针对当前用户IP的永久存在的key，存入redis，表示改IP用户已经阅读过了，无法累加阅读量
        //key存在不能再次设置
        boolean isExist= redisOperator.keyIsExist(REDIS_ALREADY_READ+":"+articleId+":"+userIp);
        if(isExist){
           return false;
        }
        return true;
    }



}
