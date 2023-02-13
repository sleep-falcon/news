package com.imooc.user.controller;

import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.user.AppUserMngControllerApi;
import com.imooc.api.controller.user.HelloControllerApi;
import com.imooc.enums.UserStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.user.services.AppUserMngService;
import com.imooc.user.services.UserService;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import io.swagger.models.auth.In;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;


@RestController
public class AppUserMngController extends BaseController implements AppUserMngControllerApi {
    final static Logger logger = LoggerFactory.getLogger(AppUserMngController.class);

    @Autowired
    private AppUserMngService appUserMngService;

    @Autowired
    private UserService userService;

    @Override
    public GraceJSONResult queryAll(String nickname, Integer status,
                                    Date startDate, Date endDate,
                                    Integer page, Integer pageSize) {
        if(page==null){
            page = COMMON_START_PAGE;
        }
        if(pageSize==null){
            pageSize = COMMON_PAGE_SIZE;
        }
        System.out.println(startDate);
        System.out.println(endDate);
        PagedGridResult res =  appUserMngService.queryAllUserList(nickname,status,startDate,endDate,page,pageSize);

        return GraceJSONResult.ok(res);
    }

    @Override
    public GraceJSONResult userDetail(String userId) {

        return GraceJSONResult.ok(userService.getUser(userId));
    }

    @Override
    public GraceJSONResult freezeUserOrNot(String userId, Integer doStatus) {

        if(!UserStatus.isUserStatusValid(doStatus)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_STATUS_ERROR);
        }
        appUserMngService.freezeOrNot(userId,doStatus);

        //刷新用户状态
        //1.删除用户会话，从而保障用户需要重新登陆以后来刷新他的会话状态
        //2.查询最新用户的信息，重新放入redis，做一次更新（不好）
        redisOperator.del(REDIS_USER_INFO+":"+userId);

        return GraceJSONResult.ok();
    }
}
