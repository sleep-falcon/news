package com.imooc.article.service;

import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.utils.PagedGridResult;

import java.util.Date;

public interface CommentService {
    /**
     * 发表评论
      */
    public void publishComment(String articleId,
                               String fatherComment,
                               String content,
                               String userId,
                               String nickname,
                               String face);

    /**
     * 查询评论列表
     */
    public PagedGridResult queryArticleComments(String articleId,
                               Integer page,Integer pageSize);

    /**
     * 查询我的评论管理列表
     */
    public PagedGridResult queryWriterCommentsMng(String writerId, Integer page, Integer pageSize);

    /**
     * 删除评论
     */
    public void deleteComment(String writerId, String commentId);


}
