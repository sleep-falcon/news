package com.imooc.user.services;

import com.imooc.utils.PagedGridResult;

import java.util.Date;


public interface AppUserMngService {
    /**
     * 查询管理员列表
     * @param nickname
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryAllUserList(String nickname, Integer status,
                                            Date startDate, Date endDate,
                                            Integer page, Integer pageSize);

    /**
     * 冻结用户或解冻
     * @param userId
     * @param doStatus
     */
    public void freezeOrNot(String userId,Integer doStatus);
}
