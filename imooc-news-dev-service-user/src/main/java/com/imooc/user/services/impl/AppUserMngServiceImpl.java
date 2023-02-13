package com.imooc.user.services.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.api.service.BaseService;
import com.imooc.enums.Sex;
import com.imooc.enums.UserStatus;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.bo.UpdateUserBo;
import com.imooc.user.mapper.AppUserMapper;
import com.imooc.user.services.AppUserMngService;
import com.imooc.user.services.UserService;
import com.imooc.utils.*;
import com.org.n3r.idworker.Sid;
import io.swagger.models.auth.In;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class AppUserMngServiceImpl extends BaseService implements AppUserMngService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Override
    public PagedGridResult queryAllUserList(String nickname, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize) {

        Example example = new Example(AppUser.class);
        example.orderBy("createdTime").desc();
        Example.Criteria criteria = example.createCriteria();

        if(StringUtils.isNotBlank(nickname)){
            criteria.andLike("nickname","%"+nickname+"%");
        }
        if(UserStatus.isUserStatusValid(status)){
            criteria.andEqualTo("activeStatus",status);
        }
        if(startDate!=null){
            criteria.andGreaterThanOrEqualTo("createdTime",startDate);
        }
        if(endDate!=null){
            criteria.andLessThanOrEqualTo("createdTime",endDate);
        }
        PageHelper.startPage(page,pageSize);
        List<AppUser> res = appUserMapper.selectByExample(example);

        return setterPagedGrid(res,page);
    }

    @Transactional
    @Override
    public void freezeOrNot(String userId, Integer doStatus) {
        AppUser user = new AppUser();
        user.setId(userId);
        user.setActiveStatus(doStatus);
        appUserMapper.updateByPrimaryKeySelective(user);

    }
}

