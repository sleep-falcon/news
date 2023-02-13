package com.imooc.admin.controller;

import com.imooc.admin.service.AdminUserService;
import com.imooc.api.config.CloudConfig;
import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.admin.AdminMngControllerApi;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
public class AdminMngController extends BaseController implements AdminMngControllerApi {
   final static Logger logger = LoggerFactory.getLogger(AdminMngController.class);

   @Autowired
   private RestTemplate restTemplate;

   @Autowired
   private RedisOperator redisOperator;

    @Autowired
    private AdminUserService adminUserService;

    @Override
    public GraceJSONResult getAdminList(Integer page, Integer pageSize) {
        if (page==null){
            page =COMMON_START_PAGE;
        }
        if(pageSize==null){
            pageSize =COMMON_PAGE_SIZE;
        }
        PagedGridResult res =  adminUserService.queryAdminList(page,pageSize);
        return GraceJSONResult.ok(res);
    }

    @Override
    public GraceJSONResult adminLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        //验证BO中的用户名，密码不为空
        if(StringUtils.isBlank(adminLoginBO.getUsername())||StringUtils.isBlank(adminLoginBO.getPassword())){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
        //查新admin用户的信息
        AdminUser admin = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());
        //判断admin不为空
        if(admin==null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
        //判断密码是否正确
        boolean isPwdMatch = BCrypt.checkpw(adminLoginBO.getPassword(),admin.getPassword());
        if(isPwdMatch){
            doLoginSetting(admin,request,response);
            return GraceJSONResult.ok();
        }else{
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }
    }

    /**
     * 用于admin用户登陆过后的基本信息
     * @param adminUser
     * @param request
     * @param response
     */
    private void doLoginSetting(AdminUser adminUser,
                                HttpServletRequest request,
                                HttpServletResponse response){
        //登陆后将token放入redis
        String token = UUID.randomUUID().toString();
        redisOperator.set(REDIS_ADMIN_TOKEN+":"+adminUser.getId(),token);
        //保存用户基本信息到cookie中
        setCookie(request,response,"atoken",token,COOKIE_MONTH);
        setCookie(request,response,"aid",adminUser.getId(),COOKIE_MONTH);
        setCookie(request,response,"aname",adminUser.getAdminName(),COOKIE_MONTH);
    }

    @Override
    public GraceJSONResult adminIsExist(String username) {
        checkAdminIsExist(username);
        return GraceJSONResult.ok();
    }

    /**
     * 判断是否已经添加同名admin
     * 是则抛出异常
     * @param username
     */
    private void checkAdminIsExist(String username){
        AdminUser admin = adminUserService.queryAdminByUsername(username);
        //判断admin不为空
        if(admin!=null){
            GraceException.display(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);
        }
    }


    @Override
    public GraceJSONResult addNewAdmin(NewAdminBO newAdminBO, HttpServletRequest request, HttpServletResponse response) {
        //用户名不能为空
        if(StringUtils.isBlank(newAdminBO.getUsername()) ){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }

        //base64不为空，代表人脸入库。否则需要密码与二次确认
        if(StringUtils.isBlank(newAdminBO.getImg64())){
            if(StringUtils.isBlank(newAdminBO.getPassword())){
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
            }
            if(StringUtils.isBlank(newAdminBO.getConfirmPassword())){
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
            }else{
                if(!newAdminBO.getPassword().equalsIgnoreCase(newAdminBO.getConfirmPassword())){
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
                }
            }
        }
        //校验用户名唯一
        checkAdminIsExist(newAdminBO.getUsername());

        //存入信息
        adminUserService.createAdminUser(newAdminBO);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult adminLogout(String adminId,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
        //从redies中删除会话token
        redisOperator.del(REDIS_ADMIN_TOKEN+":"+adminId);
        //从cookie中清理admin相关信息
        deleteCookie(request,response,"aid");
        deleteCookie(request,response,"atoken");
        deleteCookie(request,response,"aname");

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult adminFaceLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        //验证，判断用户名和人脸信息不能为空
        if(StringUtils.isBlank(adminLoginBO.getUsername())){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        //pic1
        String tempFace64 = adminLoginBO.getImg64();
        if(StringUtils.isBlank(tempFace64)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_NULL_ERROR);
        }

        //查询faceid=gridFS的主键id
        AdminUser user = adminUserService.queryAdminByUsername(adminLoginBO.getUsername());
        String faceid = user.getFaceId();
        if(StringUtils.isBlank(faceid)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        //请求文件服务获得base64数据
        String url = "http://files.imoocnews.com:8004/fs/readFace64InGridFS/?faceId="+faceid;
        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(url,GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        //pic2
        String base64 =(String)bodyResult.getData();

        //TODO
        //校验，调动外部api，判定可信度


        //admin登陆后的数据设置redis，cookie
        doLoginSetting(user,request,response);
        return GraceJSONResult.ok();


    }
}
