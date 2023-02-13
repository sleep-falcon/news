package com.imooc.user.services;

import com.imooc.enums.Sex;
import com.imooc.pojo.vo.RegionRatioVO;
import com.imooc.utils.PagedGridResult;

import java.util.Date;
import java.util.List;


public interface MyFansService {
    /**
     * 查询用户是否关注作家
     */
    public Boolean isFollow(String writerId, String fanId);

    /**
     * 关注成为粉丝
     */
    public void follow(String writerId, String fanId);

    /**
     * 取关
     */
    public void unfollow(String writerId, String fanId);

    /**
     * 查询粉丝列表
     */
    public PagedGridResult queryMyFansList(String writerId, Integer page, Integer pageSize );

    /**
     * 查询粉丝数(性别)
     */
    public Integer queryFansCounts(String writerId, Sex sex);

    /**
     * 查询粉丝数(地域)
     */
    public List<RegionRatioVO> queryFansCountsByRegion(String writerId);
}
