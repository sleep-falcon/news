package com.imooc.api.service;

import com.github.pagehelper.PageInfo;
import com.imooc.utils.PagedGridResult;

import java.util.List;

public class BaseService {
    public static final String REDIS_ALL_CATEGORY = "redis_all_category";

    public static final String REDIS_WRITER_FANS_COUNTS = "redis_writer_fans_counts";
    public static final String REDIS_MY_FOLLOW_COUNTS = "redis_my_follow_counts";

    public static final String REDIS_ARTICLE_COMMENT_COUNTS = "redis_article_comment_counts";
    public PagedGridResult setterPagedGrid(List<?> list, Integer page){
        //获得page相关信息
        PageInfo<?> pageInfo = new PageInfo<>(list);
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(list);
        gridResult.setRecords(pageInfo.getTotal()); //总记录数
        gridResult.setTotal(pageInfo.getPages()); //总页数
        gridResult.setPage(page);
        return gridResult;
    }
}
