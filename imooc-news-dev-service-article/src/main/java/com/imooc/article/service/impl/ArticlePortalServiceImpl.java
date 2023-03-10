package com.imooc.article.service.impl;


import com.github.pagehelper.PageHelper;
import com.imooc.api.service.BaseService;
import com.imooc.article.mapper.ArticleMapper;
import com.imooc.article.mapper.ArticleMapperCustome;
import com.imooc.article.service.ArticlePortalService;
import com.imooc.article.service.ArticleService;
import com.imooc.enums.ArticleAppointType;
import com.imooc.enums.ArticleReviewLevel;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Article;
import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.utils.PagedGridResult;
import com.org.n3r.idworker.Sid;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class ArticlePortalServiceImpl extends BaseService implements ArticlePortalService {
    @Autowired
    public ArticleMapper articleMapper;

    @Override
    public PagedGridResult queryIndexArticle(String keyword, Integer category, Integer page, Integer pageSize) {
        Example example = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExample(example);

        if(StringUtils.isNotBlank(keyword)){
            criteria.andLike("title","%"+keyword+"%");
        }
        if(category!=null){
            criteria.andEqualTo("categoryId",category);
        }

        PageHelper.startPage(page,pageSize);
        List<Article> list = articleMapper.selectByExample(example);
        return setterPagedGrid(list,page);
    }

    @Override
    public List<Article> queryHotList() {
        Example example = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExample(example);
        PageHelper.startPage(1,5);
        List<Article> list = articleMapper.selectByExample(example);
        return list;
    }
    private Example.Criteria setDefaultArticleExample(Example example){
        example.orderBy("publishTime").desc();
        Example.Criteria criteria = example.createCriteria();
        /**
         * ??????????????????????????????
         * isAppoint = ???????????????????????????????????????
         * isDelete = ?????????
         * articleStatus = ????????????
         */
        criteria.andEqualTo("isAppoint",YesOrNo.NO.type);
        criteria.andEqualTo("isDelete",YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus",ArticleReviewStatus.SUCCESS.type);
        return criteria;
    }
    @Override
    public PagedGridResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {
        Example articleExample = new Example(Article.class);

        Example.Criteria criteria = setDefaultArticleExample(articleExample);
        criteria.andEqualTo("publishUserId", writerId);

        /**
         * page: ?????????
         * pageSize: ??????????????????
         */
        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryGoodArticleListOfWriter(String writerId) {
        Example articleExample = new Example(Article.class);
        articleExample.orderBy("publishTime").desc();

        Example.Criteria criteria = setDefaultArticleExample(articleExample);
        criteria.andEqualTo("publishUserId", writerId);

        /**
         * page: ?????????
         * pageSize: ??????????????????
         */
        PageHelper.startPage(1, 5);
        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, 1);
    }

    @Override
    public ArticleDetailVO queryDetail(String articleid) {
        Article article = new Article();
        article.setId(articleid);
        article.setIsAppoint(YesOrNo.NO.type);
        article.setIsDelete(YesOrNo.NO.type);
        article.setArticleStatus(ArticleReviewStatus.SUCCESS.type);

        Article result = articleMapper.selectOne(article);

        ArticleDetailVO detailVO = new ArticleDetailVO();

        BeanUtils.copyProperties(result,detailVO);

        detailVO.setCover(result.getArticleCover());

        return detailVO;
    }
}
