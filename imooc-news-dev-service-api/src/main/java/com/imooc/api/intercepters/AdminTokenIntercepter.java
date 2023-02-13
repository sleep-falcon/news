package com.imooc.api.intercepters;


import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;

public class AdminTokenIntercepter extends BaseIntercepter implements HandlerInterceptor {

    /**
     * 请求controller前检查
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        Enumeration names = request.getHeaderNames();
//        while(names.hasMoreElements()) {
//            String name = (String) names.nextElement();
//            Enumeration values = request.getHeaders(name);
//            if(values != null) {
//                while(values.hasMoreElements()) {
//                    String value = (String) values.nextElement();
//                    System.out.println(name + " : " + value );
//                }
//            }
//        }
        String userid = request.getHeader("adminuserid");
        String usertoken = request.getHeader("adminusertoken");
        //判断是否放行
        boolean run = verifyUserIdToken(userid,usertoken,REDIS_ADMIN_TOKEN);
        return run;
    }

    /**
     * 渲染视图之前
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * 渲染视图之后
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
