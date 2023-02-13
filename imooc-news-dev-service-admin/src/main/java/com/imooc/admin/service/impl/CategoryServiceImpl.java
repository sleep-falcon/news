package com.imooc.admin.service.impl;

import com.imooc.admin.mapper.CategoryrMapper;
import com.imooc.admin.service.CategoryService;
import com.imooc.api.controller.BaseController;
import com.imooc.api.service.BaseService;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Category;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class CategoryServiceImpl extends BaseService implements CategoryService {
    @Autowired
    private CategoryrMapper categoryrMapper;

    @Autowired
    private RedisOperator redisOperator;

    @Transactional
    @Override
    public void updateCategory(Category category) {
        int result = categoryrMapper.updateByPrimaryKey(category);
        if(result!=1){
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 直接使用redis删除缓存即可，用户端在查询的时候会直接查库，再把最新的数据放入到缓存中
        redisOperator.del(REDIS_ALL_CATEGORY);
    }

    @Transactional
    @Override
    public void addCategory(Category category) {
        //分类不需要很多，因此ID不需要自增
        int result = categoryrMapper.insert(category);
        if(result!=1){
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
        // 直接使用redis删除缓存即可，用户端在查询的时候会直接查库，再把最新的数据放入到缓存中
        redisOperator.del(REDIS_ALL_CATEGORY);

    }


    @Override
    public List<Category> queryAllCategory() {
        return categoryrMapper.selectAll();
    }


    @Override
    public boolean isExistName(String catName, String oldCatname) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name",catName);
        if(StringUtils.isNotBlank(oldCatname)){
            criteria.andNotEqualTo("name",oldCatname);
        }
        List<Category> catlist  = categoryrMapper.selectByExample(example);
        boolean flag = false;
        if(catlist!=null&&!catlist.isEmpty()&&catlist.size()>0){
            flag = true;
        }
        return flag;
    }
}
