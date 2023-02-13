package com.imooc.api.controller.user.fallbacks;

import com.imooc.api.controller.user.UserInfoControllerApi;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.bo.UpdateUserBo;
import com.imooc.pojo.vo.AppUserVo;
import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserControllerFactoryFallback
        implements FallbackFactory<UserInfoControllerApi> {
    @Override
    public UserInfoControllerApi create(Throwable throwable) {

        return new UserInfoControllerApi(){
            @Override
            public GraceJSONResult getAccountInfo(String userId) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
            }

            @Override
            public GraceJSONResult getUserInfo(String userId) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
            }

            @Override
            public GraceJSONResult updateUserInfo(UpdateUserBo updateUserBo) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR);
            }

            @Override
            public GraceJSONResult queryByIds(String userIds) {
                System.out.println("进入客户端（服务调用者）的降级处理");
                List<AppUserVo> publishList = new ArrayList<>();
                return GraceJSONResult.ok(publishList);
            }
        };
    }
}
