package com.imooc.api.controller.admin;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.pojo.bo.SaveFriendLinkBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "首页友情链接维护", tags = {"首页友情链接维护的controller"})
@RequestMapping("friendLinkMng")
public interface FriendLinkControllerApi {

    @PostMapping ("/saveOrUpdateFriendLink")
    @ApiOperation(value = "新增或者修改友情链接",notes = "新增或者修改友情链接",httpMethod = "POST")
    public GraceJSONResult saveOrUpdateFriendLink(@RequestBody @Valid SaveFriendLinkBO saveFriendLinkBO);

    @PostMapping ("/getFriendLinkList")
    @ApiOperation(value = "查询友情链接列表",notes = "查询友情链接列表",httpMethod = "POST")
    public GraceJSONResult getFriendLinkList();

    @PostMapping ("/delete")
    @ApiOperation(value = "删除友情链接",notes = "删除友情链接",httpMethod = "POST")
    public GraceJSONResult delete(@RequestParam String linkId);

    @GetMapping("portal/list")
    @ApiOperation(value = "门户端查询友情链接列表",notes = "门户端查询友情链接列表",httpMethod = "GET")
    public GraceJSONResult queryPortalAllFriendLinkList();
}