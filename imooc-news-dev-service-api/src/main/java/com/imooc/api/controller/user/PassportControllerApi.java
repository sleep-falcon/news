package com.imooc.api.controller.user;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.RegistLoginBo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "用于用户注册登陆的controller", tags = {"用户注册登陆的controller"})
@RequestMapping("/passport")
public interface PassportControllerApi {
    /**
     * 获得短信验证码
     * @return
     */
    @GetMapping("/getSMSCode")
    @ApiOperation(value = "获得短信验证码",notes = "获得短信验证码",httpMethod = "GET")
    public GraceJSONResult getSMSCode(@RequestParam String mobile, HttpServletRequest request) throws Exception;


    @PostMapping("/doLogin")
    @ApiOperation(value = "一键注册登陆接口",notes = "一键注册登陆接口",httpMethod = "POST")
    public GraceJSONResult doLogin(@RequestBody @Valid RegistLoginBo registLoginBo,
                                   HttpServletRequest request,
                                   HttpServletResponse response);

    @PostMapping("/logout")
    @ApiOperation(value = "用户退出登录",notes = "用户退出登录",httpMethod = "POST")
    public GraceJSONResult logout(@RequestParam String userId,
                                   HttpServletRequest request,
                                   HttpServletResponse response);


}