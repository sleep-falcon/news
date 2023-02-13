package com.imooc.article.service.impl;


import com.github.pagehelper.PageHelper;
import com.imooc.api.service.BaseService;
import com.imooc.article.mapper.ArticleMapper;
import com.imooc.article.mapper.ArticleMapperCustome;
import com.imooc.article.mapper.CommentsMapper;
import com.imooc.article.mapper.CommentsMapperCustome;
import com.imooc.article.service.ArticlePortalService;
import com.imooc.article.service.ArticleService;
import com.imooc.article.service.CommentService;
import com.imooc.enums.ArticleAppointType;
import com.imooc.enums.ArticleReviewLevel;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Article;
import com.imooc.pojo.Category;
import com.imooc.pojo.Comments;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.pojo.vo.CommentsVO;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import com.org.n3r.idworker.Sid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
public class CommentServiceImpl extends BaseService implements CommentService {
    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private Sid sid;

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private RedisOperator redisOperator;

    @Autowired
    private CommentsMapperCustome commentsMapperCustome;


    @Transactional
    @Override
    public void publishComment(String articleId, String fatherComment, String content, String userId, String nickname,String face) {
        ArticleDetailVO article =  articlePortalService.queryDetail(articleId);
        Comments comments = new Comments();
        comments.setId(sid.nextShort());


        comments.setWriterId(article.getPublishUserId());
        comments.setArticleTitle(article.getTitle());
        comments.setArticleCover(article.getCover());
        comments.setArticleId(articleId);

        comments.setFatherId(fatherComment);
        comments.setCommentUserId(userId);
        comments.setCommentUserNickname(nickname);

        comments.setContent(content);
        comments.setCreateTime(new Date());
        comments.setCommentUserFace(face);

        commentsMapper.insert(comments);
        redisOperator.increment(REDIS_ARTICLE_COMMENT_COUNTS+":"+articleId,1);
    }

    @Override
    public PagedGridResult queryArticleComments(String articleId, Integer page, Integer pageSize) {
        Map<String, Object> map = new HashMap<>();
        map.put("articleId",articleId);

        PageHelper.startPage(page,pageSize);

        List<CommentsVO> list = commentsMapperCustome.queryArticleCommentsList(map);

        return setterPagedGrid(list,page);
    }
    @Override
    public PagedGridResult queryWriterCommentsMng(String writerId, Integer page, Integer pageSize) {

        Comments comment = new Comments();
        comment.setCommentUserId(writerId);

        PageHelper.startPage(page, pageSize);
        List<Comments> list = commentsMapper.select(comment);
        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void deleteComment(String writerId, String commentId) {
        Comments comment = new Comments();
        comment.setId(commentId);
        comment.setCommentUserId(writerId);

        commentsMapper.delete(comment);
    }
}
