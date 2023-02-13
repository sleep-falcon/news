package com.imooc.user.services.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.api.service.BaseService;
import com.imooc.enums.Sex;
import com.imooc.enums.UserStatus;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.Fans;
import com.imooc.pojo.vo.RegionRatioVO;
import com.imooc.user.mapper.AppUserMapper;
import com.imooc.user.mapper.FansMapper;
import com.imooc.user.services.AppUserMngService;
import com.imooc.user.services.MyFansService;
import com.imooc.user.services.UserService;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import com.org.n3r.idworker.Sid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MyFansServiceImpl extends BaseService implements MyFansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private Sid sid;

    @Autowired
    private RedisOperator redisOperator;

    @Override
    public Boolean isFollow(String writerId, String fanId) {
        Fans fans = new Fans();
        fans.setFanId(fanId);
        fans.setWriterId(writerId);
        int count = fansMapper.selectCount(fans);
        return count>0?true:false;
    }

    @Transactional
    @Override
    public void follow(String writerId, String fanId) {
        //获得粉丝用户信息
        AppUser fanInfo = userService.getUser(fanId);
        String fanPkId = sid.nextShort();

        Fans fans = new Fans();
        fans.setFanId(fanId);
        fans.setWriterId(writerId);
        fans.setId(fanPkId);

        fans.setFace(fanInfo.getFace());
        fans.setFanNickname(fanInfo.getNickname());
        fans.setProvince(fanInfo.getProvince());
        fans.setSex(fanInfo.getSex());

        fansMapper.insert(fans);
        //redis作家粉丝数累加
        redisOperator.increment(REDIS_WRITER_FANS_COUNTS+":"+writerId,1);

        //redis我的关注数累加
        redisOperator.increment(REDIS_MY_FOLLOW_COUNTS+":"+fanId,1);
    }

    @Transactional
    @Override
    public void unfollow(String writerId, String fanId) {
        Fans fans = new Fans();
        fans.setFanId(fanId);
        fans.setWriterId(writerId);
        fansMapper.delete(fans);
        //redis作家粉丝数累减
        redisOperator.decrement(REDIS_WRITER_FANS_COUNTS+":"+writerId,1);
        //redis我的关注数累减
        redisOperator.decrement(REDIS_MY_FOLLOW_COUNTS+":"+fanId,1);
    }

    @Override
    public PagedGridResult queryMyFansList(String writerId, Integer page, Integer pageSize) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        PageHelper.startPage(page,pageSize);
        List<Fans> list= fansMapper.select(fans);
        return setterPagedGrid(list,page);
    }

    @Override
    public Integer queryFansCounts(String writerId, Sex sex) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setSex(sex.type);

        int count = fansMapper.selectCount(fans);
        return count;
    }

    public static final String[] regions = {"北京", "天津", "上海", "重庆",
            "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾",
            "内蒙古", "广西", "西藏", "宁夏", "新疆",
            "香港", "澳门"};
    @Override
    public List<RegionRatioVO> queryFansCountsByRegion(String writerId) {
        List<RegionRatioVO> res = new ArrayList<>();
        for(String r:regions){
            Fans fans = new Fans();
            fans.setWriterId(writerId);
            fans.setProvince(r);
            int count = 0;
            count = fansMapper.selectCount(fans);
            RegionRatioVO regionRatioVO = new RegionRatioVO();
            regionRatioVO.setName(r);
            regionRatioVO.setValue(count);
            res.add(regionRatioVO);
        }
        return res;
    }
}

