package com.imooc.api.controller.admin;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(value = "管理员admin维护", tags = {"管理员admin维护的controller"})
@RequestMapping("adminMng")
public interface AdminMngControllerApi {

    @PostMapping ("/adminLogin")
    @ApiOperation(value = "admin登陆的接口",notes = "admin登陆的接口",httpMethod = "POST")
    public GraceJSONResult adminLogin(@RequestBody @Valid AdminLoginBO adminLoginBO,
                                      HttpServletRequest request,
                                      HttpServletResponse response);

    @PostMapping ("/adminIsExist")
    @ApiOperation(value = "查询admin用户名是否存在",notes = "查询admin用户名是否存在",httpMethod = "POST")
    public GraceJSONResult adminIsExist(@RequestParam String username);

    @PostMapping ("/addNewAdmin")
    @ApiOperation(value = "创建admin",notes = "创建admin",httpMethod = "POST")
    public GraceJSONResult addNewAdmin(@RequestBody NewAdminBO newAdminBO,
                                       HttpServletRequest request,
                                       HttpServletResponse response);
    @PostMapping ("/getAdminList")
    @ApiOperation(value = "查询admin列表",notes = "查询admin列表",httpMethod = "POST")
    public GraceJSONResult getAdminList(
            @ApiParam(name = "page",value = "查询下一页的第几页",required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize",value = "分页查询每一页的条数",required = false)
            @RequestParam Integer pageSize);

    @PostMapping ("/adminLogout")
    @ApiOperation(value = "admin退出登录",notes = "admin退出登录",httpMethod = "POST")
    public GraceJSONResult adminLogout(@RequestParam String adminId,
                                       HttpServletRequest request,
                                       HttpServletResponse response);

    @PostMapping ("/adminFaceLogin")
    @ApiOperation(value = "admin管理员的人脸登陆",notes = "admin管理员的人脸登陆",httpMethod = "POST")
    public GraceJSONResult adminFaceLogin(@RequestBody AdminLoginBO adminLoginBO,
                                       HttpServletRequest request,
                                       HttpServletResponse response);



}