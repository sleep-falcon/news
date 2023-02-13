package com.imooc.admin.service;

import com.imooc.pojo.Category;

import java.util.List;

public interface CategoryService {
    /**
     * 更新分类
     */
    public void updateCategory(Category category);

    /**
     * 新增分类
     */
    public void addCategory(Category category);

    /**
     * 查询所有分类
     */
    public List<Category> queryAllCategory();


    /**
     * 查询分类名是否已存在
     */
    boolean isExistName(String catName, String oldCatname);
}
