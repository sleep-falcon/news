package com.imooc.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.admin.mapper.AdminUserMapper;
import com.imooc.admin.service.AdminUserService;
import com.imooc.api.service.BaseService;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.PagedGridResult;
import com.org.n3r.idworker.Sid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class AdminUserServiceImpl extends BaseService implements AdminUserService {
    @Autowired
    public AdminUserMapper adminUserMapper;

    @Autowired
    private Sid sid;

    @Override
    public AdminUser queryAdminByUsername(String username) {
        Example adminExample = new Example(AdminUser.class);
        Example.Criteria criteria = adminExample.createCriteria();
        criteria.andEqualTo("username",username);
        return adminUserMapper.selectOneByExample(adminExample);
    }

    @Override
    public PagedGridResult queryAdminList(Integer page, Integer pageSize) {
        Example adminExample = new Example(AdminUser.class);
        adminExample.orderBy("createdTime").desc();
        //PageHelper插件相当于一个拦截器
        PageHelper.startPage(page,pageSize);
        List<AdminUser> adminUserList = adminUserMapper.selectByExample(adminExample);
        return setterPagedGrid(adminUserList,page);
    }

    @Transactional
    @Override
    public void createAdminUser(NewAdminBO newAdminBO) {
        String adminId = sid.nextShort();
        AdminUser adminuser = new AdminUser();
        adminuser.setId(adminId);
        adminuser.setUsername(newAdminBO.getUsername());
        adminuser.setAdminName(newAdminBO.getAdminName());
        //密码不为空，需要加密存入数据库
        if(StringUtils.isNotBlank(newAdminBO.getPassword())){
            String pwd = BCrypt.hashpw(newAdminBO.getPassword(),BCrypt.gensalt());
            adminuser.setPassword(pwd);
        }
        //如果人脸上传，FaceID不为空，存入数据库
        if(StringUtils.isNotBlank(newAdminBO.getFaceId())){
            adminuser.setFaceId(newAdminBO.getFaceId());
        }

        adminuser.setCreatedTime(new Date());
        adminuser.setUpdatedTime(new Date());

        int result = adminUserMapper.insert(adminuser);
        if(result!=1){
            GraceException.display(ResponseStatusEnum.ADMIN_CREATE_ERROR);
        }


    }
}
