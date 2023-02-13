package com.imooc.api.controller.user;
import com.imooc.api.controller.user.fallbacks.UserControllerFactoryFallback;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.UpdateUserBo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import static com.imooc.api.MyServiceList.SERVICE_USER;

@Api(value = "用户信息相关controller", tags = {"用户信息相关controller"})
@RequestMapping("/user")
@FeignClient(value = SERVICE_USER,fallbackFactory = UserControllerFactoryFallback.class)
public interface UserInfoControllerApi {

    @PostMapping("/getAccountInfo")
    @ApiOperation(value = "获得用户账户信息",notes = "获得用户账户信息",httpMethod = "POST")
    public GraceJSONResult getAccountInfo(@RequestParam String userId);

    @PostMapping("/getUserInfo")
    @ApiOperation(value = "获得用户基本信息",notes = "获得用户基本信息",httpMethod = "POST")
    public GraceJSONResult getUserInfo(@RequestParam String userId);

    @PostMapping("/updateUserInfo")
    @ApiOperation(value = "完善用户信息",notes = "完善用户信息",httpMethod = "POST")
    public GraceJSONResult updateUserInfo(@RequestBody @Valid UpdateUserBo updateUserBo);

    @GetMapping("/queryByIds")
    @ApiOperation(value = "根据用户ID set查询用户列表",notes = "根据用户ID set查询用户列表",httpMethod = "GET")
    public GraceJSONResult queryByIds(@RequestParam String userIds);

}