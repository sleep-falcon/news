package com.imooc.user.services.impl;

import com.imooc.enums.Sex;
import com.imooc.enums.UserStatus;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.bo.UpdateUserBo;
import com.imooc.user.mapper.AppUserMapper;
import com.imooc.user.services.UserService;
import com.imooc.utils.*;
import com.org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    public AppUserMapper appUserMapper;

    @Autowired
    public Sid sid;

    private static final String USER_FACE0 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxFw_8_qAIlFXAAAcIhVPdSg994.png";
    private static final String USER_FACE1 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUySASMbOAABBAXhjY0Y649.png";
    private static final String USER_FACE2 = "http://122.152.205.72:88/group1/M00/00/05/CpoxxF6ZUx6ANoEMAABTntpyjOo395.png";
    @Autowired
    private RedisOperator redisOperator;
    public static final String REDIS_USER_INFO = "redis_user_info";


    @Override
    public AppUser queryMobileIsExist(String mobile) {
        //构建查询实例
        Example userExample = new Example(AppUser.class);
        //创建查询
        Example.Criteria userCriteria = userExample.createCriteria();
        userCriteria.andEqualTo("mobile",mobile);
        AppUser appUser = appUserMapper.selectOneByExample(userExample);

        return appUser;
    }

    @Override
    public AppUser createUser(String mobile) {
        /**
         * 互联网项目都需考虑可扩展性
         * 如果未来业务激增，就需要分库分表
         * 那么数据库表主键id必须保证全局（全库）唯一
         */
        String userid = sid.nextShort();

        AppUser user = new AppUser();
        user.setId(userid);
        user.setMobile(mobile);
        user.setNickname("用户"+ DesensitizationUtil.commonDisplay(mobile));
        user.setFace(USER_FACE0);
        user.setBirthday(DateUtil.stringToDate("1900-01-01"));
        user.setSex(Sex.secret.type);
        user.setActiveStatus(UserStatus.INACTIVE.type);
        user.setTotalIncome(0);
        user.setCreatedTime(new Date());
        user.setUpdatedTime(new Date());
        appUserMapper.insert(user);
        return user;
    }

    @Override
    public AppUser getUser(String userId) {
        //构建查询实例
        Example userExample = new Example(AppUser.class);
        //创建查询
        Example.Criteria userCriteria = userExample.createCriteria();
        userCriteria.andEqualTo("id",userId);
        return appUserMapper.selectOneByExample(userExample);
    }

    @Override
    public void updateUserInfo(UpdateUserBo updateUserBo) {
        String userId = updateUserBo.getId();




        //保证双写一致，先删除redis中的数据，后更新数据库
        redisOperator.del(REDIS_USER_INFO+":"+userId);


        AppUser appUser = new AppUser();
        BeanUtils.copyProperties(updateUserBo,appUser);
        appUser.setUpdatedTime(new Date());
        appUser.setActiveStatus(UserStatus.ACTIVE.type);

        int result = appUserMapper.updateByPrimaryKeySelective(appUser);
        if(result!=1){
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        //再次查询用户的最新信息，放入redis中
        AppUser user = getUser(userId);
        redisOperator.set(REDIS_USER_INFO+":"+userId, JsonUtils.objectToJson(appUser));

        //在删除redis数据库未更新之前可能也会有请求，使得redis再次获得老数据，因此需要延时再次删除
        //这之中用户依然可能获得脏数据，但这是为了高可用性不能避免的
        //缓存双删策略
        try{
            Thread.sleep(100);
            redisOperator.del(REDIS_USER_INFO+":"+userId);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
