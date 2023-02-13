package com.imooc.user.services;

import com.imooc.pojo.AppUser;
import com.imooc.pojo.bo.UpdateUserBo;
import com.imooc.utils.PagedGridResult;


public interface UserService {
    /**
     * 判断用户是否存在，存在则返回用户信息
     */
    public AppUser queryMobileIsExist(String mobile);
    /**
     * 不存在，记录用户到数据库
     */
    public AppUser createUser(String mobile);

    /**
     * 根据用户ID查询用户信息
     * @param userId
     * @return
     */
    public AppUser getUser(String userId);

    /**
     * 用户修改信息，完善资料，并激活
     * @param updateUserBo
     */
    public void updateUserInfo(UpdateUserBo updateUserBo);
}
