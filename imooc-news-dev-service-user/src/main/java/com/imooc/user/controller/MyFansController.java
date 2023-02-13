package com.imooc.user.controller;

import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.user.MyFansControllerApi;
import com.imooc.api.controller.user.UserInfoControllerApi;
import com.imooc.enums.Sex;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.Fans;
import com.imooc.pojo.bo.UpdateUserBo;
import com.imooc.pojo.vo.AppUserVo;
import com.imooc.pojo.vo.FansCountsVO;
import com.imooc.pojo.vo.UserAccountVo;
import com.imooc.user.services.MyFansService;
import com.imooc.user.services.UserService;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.SMSUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
public class MyFansController extends BaseController implements MyFansControllerApi {
    final static Logger logger = LoggerFactory.getLogger(MyFansController.class);

    @Autowired
    private MyFansService fansService;

    @Override
    public GraceJSONResult isMeFollowThisWriter(String writerId, String fanId) {
        boolean res = fansService.isFollow(writerId,fanId);
        return GraceJSONResult.ok(res);
    }

    @Override
    public GraceJSONResult follow(String writerId, String fanId) {
        fansService.follow(writerId,fanId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult unfollow(String writerId, String fanId) {
        fansService.unfollow(writerId,fanId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryAll(String writerId, Integer page, Integer pageSize) {
        if (page==null){
            page =COMMON_START_PAGE;
        }
        if(pageSize==null){
            pageSize =COMMON_PAGE_SIZE;
        }

        return GraceJSONResult.ok(fansService.queryMyFansList(writerId,page,pageSize));
    }

    @Override
    public GraceJSONResult queryRatio(String writerId) {
        int man = fansService.queryFansCounts(writerId, Sex.man);
        int woman =  fansService.queryFansCounts(writerId, Sex.woman);
        FansCountsVO fansCountsVO = new FansCountsVO();
        fansCountsVO.setManCounts(man);
        fansCountsVO.setWomanCounts(woman);
        return GraceJSONResult.ok(fansCountsVO);
    }

    @Override
    public GraceJSONResult queryRatioByRegion(String writerId) {
        return GraceJSONResult.ok(fansService.queryFansCountsByRegion(writerId));
    }
}
