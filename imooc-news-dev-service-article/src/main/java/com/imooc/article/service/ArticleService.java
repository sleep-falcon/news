package com.imooc.article.service;

import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.utils.PagedGridResult;

import java.util.Date;
import java.util.List;

public interface ArticleService {
    /**
     * 发布文章
     */
    public void createArticle(NewArticleBO newArticleBO, Category category);

    /**
     * 更新定时发布为即时发布
     */
    public void updateAppointToPublich();

    /**
     * 更新单条文章为即时发布
     */
    public void updateArticleToPublich(String articleId);

    /**
     * 用户中心，查询我的文章列表
     */
    public PagedGridResult queryMyArticleList(String userId, String keyword, Integer status, Date startDate, Date endDate, Integer page, Integer pageSize);

    /**
     * 更改文章状态
     * @param articleId
     * @param pending
     */
    public void updateArticleStatus(String articleId, Integer pending);

    /**
     * 关联文章与GridFS上的html
     * @param articleId
     * @param fileId
     */
    public void updateArticleToGridFS(String articleId, String fileId);


    /**
     * 管理员查询文章列表
     */
    public PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize);

    /**
     * 用户删除文章
     * @param userId
     * @param articleId
     */
    public void  deleteArticle(String userId,String articleId);

    /**
     * 用户撤回文章
     * @param userId
     * @param articleId
     */
    public void  withdrawArticle(String userId,String articleId);
}
