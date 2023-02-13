package com.imooc.api.controller;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.vo.AppUserVo;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseController {
    @Autowired
    public RedisOperator redisOperator;

    @Autowired
    private RestTemplate restTemplate;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";
    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";
    public static final String REDIS_ALL_CATEGORY = "redis_all_category";
    public static final Integer COOKIE_MONTH = 30*24*60*60;
    public static final Integer COOKIE_DELETE = 0;
    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final String REDIS_WRITER_FANS_COUNTS = "redis_writer_fans_counts";
    public static final String REDIS_MY_FOLLOW_COUNTS = "redis_my_follow_counts";

    public static final String REDIS_ARTICLE_READ_COUNTS = "redis_article_read_counts";
    public static final String REDIS_ALREADY_READ = "redis_already_read";

    public static final String REDIS_ARTICLE_COMMENT_COUNTS = "redis_article_comment_counts";




    @Value("${website.domain-name}")
    public String DOMAIN_NAME;

    public void setCookie(HttpServletRequest request,
                          HttpServletResponse response,String cookieName, String cookievalue,
                          Integer maxAge){
        try {
            cookievalue = URLEncoder.encode(cookievalue,"utf-8");
            setCookieValue(request,response,cookieName,cookievalue,maxAge);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    public void setCookieValue(HttpServletRequest request,
                          HttpServletResponse response,String cookieName, String cookievalue,
                          Integer maxAge){
        Cookie cookie = new Cookie(cookieName,cookievalue);
        cookie.setMaxAge(maxAge);
        cookie.setDomain(DOMAIN_NAME);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletRequest request,
                             HttpServletResponse response,String cookieName){
        String deleteValue = null;
        try {
            deleteValue = URLEncoder.encode("","utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        setCookieValue(request,response,cookieName,deleteValue,COOKIE_DELETE);
    }

    public Integer getCountsFromRedis(String key){
        String countsStr = redisOperator.get(key);
        if(StringUtils.isBlank(countsStr)){
            countsStr = "0";
        }
        return Integer.valueOf(countsStr);
    }

    public List<AppUserVo> getBasicuserList(Set idSet) {
        String userServerUrlExecute
                = "http://user.imoocnews.com:8003/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);
        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(userServerUrlExecute, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        List<AppUserVo> userVoList = null;
        if (bodyResult.getStatus() == 200) {
            String userJson = JsonUtils.objectToJson(bodyResult.getData());
            userVoList = JsonUtils.jsonToList(userJson, AppUserVo.class);
        }
        return userVoList;
    }

}