package com.imooc.api.intercepters;

import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class BaseIntercepter {
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";
    @Autowired
    public RedisOperator redisOperator;
    public boolean verifyUserIdToken(String id,
                                     String token,
                                     String redisKeyPrefix){
        if (StringUtils.isNotBlank(id)&&StringUtils.isNotBlank(token)){
            String redistoken  = redisOperator.get(redisKeyPrefix+":"+id);
            if(StringUtils.isBlank(redistoken)){
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            }else{
                if(redistoken.equalsIgnoreCase(token)){
                    return true;
                }else{
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        }else{
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
            return false;
        }
    }

    // 从cookie中取值
    public String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for(Cookie cookie : cookies){
            if(cookie.getName().equals(key)){
                String value = cookie.getValue();
                return value;
            }
        }
        return null;
    }
}
