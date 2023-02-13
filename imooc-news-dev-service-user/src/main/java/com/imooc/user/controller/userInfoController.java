package com.imooc.user.controller;

import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.user.UserInfoControllerApi;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.bo.UpdateUserBo;
import com.imooc.pojo.vo.AppUserVo;
import com.imooc.pojo.vo.UserAccountVo;
import com.imooc.user.services.UserService;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.SMSUtils;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@DefaultProperties(defaultFallback = "")
public class userInfoController extends BaseController implements UserInfoControllerApi{
    final static Logger logger = LoggerFactory.getLogger(userInfoController.class);
    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserService userService;

    public GraceJSONResult defaultFallback(){
        System.out.println("这是全局的降级方法");
        return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
    }

    private AppUser getUser(String userId){
        //查询判断redis中是否包含用户信息，如果包含，则查询后直接返回，就不查询数据库了
        String userjson = redisOperator.get(REDIS_USER_INFO+":"+userId);
        AppUser appUser = null;
        if(StringUtils.isNotBlank(userjson)){
            appUser =  JsonUtils.jsonToPojo(userjson,AppUser.class);
        }else{
            appUser = userService.getUser(userId);
            //由于用户信息变动少，对于千万级别的网站，这类信息不会直接查询数据库
            //完全可以依靠redis减轻数据库并发压力，直接把查询后的数据存入到redis中
            redisOperator.set(REDIS_USER_INFO+":"+userId, JsonUtils.objectToJson(appUser));
        }
        return appUser;
    }

    @Override
    public GraceJSONResult getAccountInfo(String userId) {
        //判断参数不能为空
        if(StringUtils.isBlank(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }
        //根据用户ID，查询用户信息
        AppUser user = getUser(userId);
        //返回需要的用户信息，构建新的vo类
        UserAccountVo userAccountVo = new UserAccountVo();
        BeanUtils.copyProperties(user,userAccountVo);
        return GraceJSONResult.ok(userAccountVo);
    }

    @Override
    public GraceJSONResult updateUserInfo(UpdateUserBo updateUserBo) {
        //执行更新
        userService.updateUserInfo(updateUserBo);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getUserInfo(String userId) {
        //判断参数不能为空
        if(StringUtils.isBlank(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }
        //根据用户ID，查询用户信息
        AppUser user = getUser(userId);
        //返回需要的用户信息，构建新的vo类
        AppUserVo userVo = new AppUserVo();
        BeanUtils.copyProperties(user,userVo);
        //将用户的基本信息存储到浏览器上，减少请求压力

        //查询redis 中用户的关注数和粉丝数，放入userVo到前端渲染
        userVo.setMyFansCounts(getCountsFromRedis(REDIS_WRITER_FANS_COUNTS+":"+userId));
        userVo.setMyFollowCounts(getCountsFromRedis(REDIS_MY_FOLLOW_COUNTS+":"+userId));
        return GraceJSONResult.ok(userVo);
    }

    @Value("${server.port}")
    private String myport;
    @Override
    @HystrixCommand//(fallbackMethod = "queryByIdsfallback")
    public GraceJSONResult queryByIds(String userIds) {

        System.out.println(myport);

        //模拟异常
        //手动触发异常
//        int a = 1/0;

        //模拟超时异常
//        try{
//            Thread.sleep(60000);
//        }catch (InterruptedException e){
//            e.printStackTrace();
//        }

        if(StringUtils.isBlank(userIds)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        List<AppUserVo> publishList  = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds,String.class);
        for(String userId:userIdList){
            publishList.add(getBasicUserInfo(userId));
        }
        return GraceJSONResult.ok(publishList);
    }

    public GraceJSONResult queryByIdsfallback(String userIds) {

        System.out.println("这是降级方法");

        List<AppUserVo> publishList  = new ArrayList<>();
        List<String> userIdList = JsonUtils.jsonToList(userIds,String.class);
        for(String userId:userIdList){
            //手动构建空对象，详情页展示的用户信息可有可无
            AppUserVo userVo = new AppUserVo();
            publishList.add(userVo);
        }
        return GraceJSONResult.ok(publishList);
    }


    private AppUserVo getBasicUserInfo(String userId){
        AppUser user = getUser(userId);
        AppUserVo appUserVo = new AppUserVo();
        BeanUtils.copyProperties(user,appUserVo);
        return appUserVo;
    }
}
