package com.imooc.admin.controller;

import com.imooc.admin.service.AdminUserService;
import com.imooc.admin.service.CategoryService;
import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.admin.AdminMngControllerApi;
import com.imooc.api.controller.admin.CategoryMngControllerApi;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.pojo.Category;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.pojo.bo.SaveCategoryBO;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class CategoryMngController extends BaseController implements CategoryMngControllerApi {
    @Autowired
    private CategoryService categoryService;

    @Override
    public GraceJSONResult saveOrUpdateCategory(SaveCategoryBO newCategoryBO) {
        Category cat = new Category();
        BeanUtils.copyProperties(newCategoryBO,cat);
        //id为空则为新增，否则是update
        if(newCategoryBO.getId()==null){
            // 查询新增的分类名称不能重复存在
            boolean isExist = categoryService.isExistName(cat.getName(),null);
            if(!isExist){
                categoryService.addCategory(cat);
            }else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.CATEGORY_EXIST_ERROR);
            }
        }else{
            // 查询修改的分类名称不能重复存在
            boolean isExist = categoryService.isExistName(cat.getName(),newCategoryBO.getOldName());
            if(!isExist){
                categoryService.updateCategory(cat);
            }else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.CATEGORY_EXIST_ERROR);
            }
        }
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getCatList() {
        return GraceJSONResult.ok(categoryService.queryAllCategory());
    }

    @Override
    public GraceJSONResult getCats() {
        // 先从redis中查询，如果有，则返回，如果没有，则查询数据库库后先放缓存，放返回
        String allCatJson = redisOperator.get(REDIS_ALL_CATEGORY);

        List<Category> categoryList = null;
        if (StringUtils.isBlank(allCatJson)) {
            categoryList = categoryService.queryAllCategory();
            redisOperator.set(REDIS_ALL_CATEGORY, JsonUtils.objectToJson(categoryList));
        } else {
            categoryList = JsonUtils.jsonToList(allCatJson, Category.class);
        }

        return GraceJSONResult.ok(categoryList);
    }
}
