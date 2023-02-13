package com.imooc.article.controller;

import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.article.ArticleControllerApi;
import com.imooc.api.controller.article.CommentControllerApi;
import com.imooc.article.service.ArticleService;
import com.imooc.article.service.CommentService;
import com.imooc.enums.ArticleCoverType;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Category;
import com.imooc.pojo.bo.CommentReplyBO;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.pojo.vo.AppUserVo;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;


@RestController
public class CommentController extends BaseController implements CommentControllerApi {
    final static Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    private CommentService commentService;

    @Override
    public GraceJSONResult createComment(CommentReplyBO commentReplyBO) {
        //根据留言用户的ID，查询他的昵称，用于存入数据表进行字段冗余处理
        String userId = commentReplyBO.getCommentUserId();

        //发起远程调用获得用户基本信息
        Set<String> idSet = new HashSet<>();
        idSet.add(userId);
        AppUserVo temp = getBasicuserList(idSet).get(0);
        String nickname = temp.getNickname();
        String face = temp.getFace();

        //保存用户评论的信息到数据库
        commentService.publishComment(commentReplyBO.getArticleId()
                ,commentReplyBO.getFatherId(),commentReplyBO.getContent()
                ,userId,nickname,face);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult counts(String articleId) {

        return GraceJSONResult.ok(getCountsFromRedis(REDIS_ARTICLE_COMMENT_COUNTS+":"+articleId));
    }

    @Override
    public GraceJSONResult list(String articleId, Integer page, Integer pageSize) {
        if(page==null){
            page = COMMON_START_PAGE;
        }
        if(pageSize==null){
            pageSize = COMMON_PAGE_SIZE;
        }
        PagedGridResult res = commentService.queryArticleComments(articleId,page,pageSize);

        return GraceJSONResult.ok(res);
    }

    @Override
    public GraceJSONResult mng(String writerId, Integer page, Integer pageSize) {

        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = commentService.queryWriterCommentsMng(writerId, page, pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult delete(String writerId, String commentId) {
        commentService.deleteComment(writerId, commentId);
        return GraceJSONResult.ok();
    }
}
