package com.imooc.admin.service;

import com.imooc.pojo.mo.FriendLinkMO;

import java.util.List;

public interface FriendLinkService {
    /**
     * 新增或更新友情链接
     */
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO);

    /**
     * 查询所有友情链接
     */
    public List<FriendLinkMO> queryAllList();

    /**
     * 删除友情链接
     */
    public void delete(String linkId);

    /**
     * 首页查询友情链接
     */
    public List<FriendLinkMO> queryIndexAllList();

}
