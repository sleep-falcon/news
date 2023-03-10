package com.imooc.article.mapper;

import com.imooc.my.mapper.MyMapper;
import com.imooc.pojo.Comments;
import com.imooc.pojo.vo.CommentsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public interface CommentsMapperCustome{
    /**
     * 查询文章评论
     */
    public List<CommentsVO> queryArticleCommentsList(@Param("paramMap")Map<String, Object> map);
}