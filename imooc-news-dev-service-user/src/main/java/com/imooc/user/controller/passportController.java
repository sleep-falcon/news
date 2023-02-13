package com.imooc.user.controller;

import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.user.PassportControllerApi;
import com.imooc.enums.UserStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.bo.RegistLoginBo;
import com.imooc.user.services.UserService;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.SMSUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;


@RestController
public class passportController extends BaseController implements PassportControllerApi {
    final static Logger logger = LoggerFactory.getLogger(passportController.class);
    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest request)  throws Exception {
        String userip = IPUtil.getRequestIp(request);
        //根据用户的IP进行限制，60秒内只能获得一次验证码
        redisOperator.setnx60s(MOBILE_SMSCODE+":"+userip,"1234");
        //生成随机验证码并且发送短信
        String random = (int)((Math.random()*9+1)*1000)+"";
        //smsUtils.sendSMS(mobile,random); //调用阿里云短信发送服务
        redisOperator.set(MOBILE_SMSCODE+":"+mobile,random,30*60);
        logger.info("send~");
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult doLogin(RegistLoginBo registLoginBo, HttpServletRequest request,
                                   HttpServletResponse response) {

        String mobile = registLoginBo.getMobile();
        String smsCode = registLoginBo.getSmsCode();
        //1.校验验证码是否匹配
        String redisSMScode = redisOperator.get(MOBILE_SMSCODE+":"+mobile);
        //是否失效(为空/不匹配)
        if(StringUtils.isBlank(redisSMScode)||!redisSMScode.equalsIgnoreCase(smsCode)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }
        //判断用户是否注册
        AppUser appUser = userService.queryMobileIsExist(mobile);
        if(appUser!=null&&appUser.getActiveStatus()==UserStatus.FROZEN.type){
            //状态异常，禁止登陆
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_FROZEN);
        } else if(appUser==null){
            //未注册，创建用户
            appUser = userService.createUser(mobile);
        }
        //保存用户分布式会话的相关操作
        int status = appUser.getActiveStatus();
        if(status!=UserStatus.FROZEN.type){
            //未冻结，保存token和基本信息到redis
            String uToken = UUID.randomUUID().toString().trim();
            redisOperator.set(REDIS_USER_TOKEN+":"+appUser.getId(),uToken);
            redisOperator.set(REDIS_USER_INFO+":"+appUser.getId(), JsonUtils.objectToJson(appUser));
            //保存用户ID和token到cookie中
            setCookie(request,response,"utoken",uToken,COOKIE_MONTH);
            setCookie(request,response,"uid",appUser.getId(),COOKIE_MONTH);
        }

        //用户登陆/注册成功后，删除redis中的验证码==》验证码只能使用一次
        redisOperator.del(MOBILE_SMSCODE+":"+mobile);

        //返回用户状态---与前端的约定
        return GraceJSONResult.ok(status);
    }

    @Override
    public GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response) {
        // redis中清除
        redisOperator.del(REDIS_USER_TOKEN+":"+userId);
        setCookie(request,response,"utoken","",COOKIE_DELETE);
        setCookie(request,response,"uid","",COOKIE_DELETE);
        return GraceJSONResult.ok();
    }
}
