package com.imooc.article.service.impl;


import com.github.pagehelper.PageHelper;
import com.imooc.api.config.RabbitMQDelayConfig;
import com.imooc.api.controller.BaseController;
import com.imooc.api.service.BaseService;
import com.imooc.article.mapper.ArticleMapper;
import com.imooc.article.mapper.ArticleMapperCustome;
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
import com.imooc.pojo.eo.ArticleEo;
import com.imooc.utils.PagedGridResult;
import com.mongodb.client.gridfs.GridFSBucket;
import com.org.n3r.idworker.Sid;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

@Service
public class ArticleServiceImpl extends BaseService implements ArticleService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    public ArticleMapper articleMapper;

    @Autowired
    private ElasticsearchTemplate estemplate;

    @Autowired
    private ArticleMapperCustome articleMapperCustome;

    @Autowired
    private Sid sid;

    @Transactional
    @Override
    public void createArticle(NewArticleBO newArticleBO, Category category) {
        String articleId = sid.nextShort();
        Article article = new Article();
        BeanUtils.copyProperties(newArticleBO,article);
        article.setId(articleId);

        article.setCategoryId(newArticleBO.getCategoryId());
        article.setArticleStatus(ArticleReviewStatus.REVIEWING.type);
        article.setCommentCounts(0);
        article.setReadCounts(0);
        article.setIsDelete(YesOrNo.NO.type);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());

        if(article.getIsAppoint()== ArticleAppointType.TIMING.type){
            article.setPublishTime(newArticleBO.getPublishTime());
        }else if(article.getIsAppoint()== ArticleAppointType.IMMEDIATELY.type) {
            article.setPublishTime(new Date());
        }

        int res = articleMapper.insert(article);
        if(res!=1){
            GraceException.display(ResponseStatusEnum.ARTICLE_CREATE_ERROR);
        }

        //发送延迟消息到mq，计算定时发布的时间与当前时间的时间差，则为往后延迟的时间
        if(article.getIsAppoint()== ArticleAppointType.TIMING.type){
            Date futureDate = article.getPublishTime();
            Date nowDate = new Date();

            int delayTimes = (int)(futureDate.getTime()-nowDate.getTime());

            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    //设置消息的持久
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    //设置消息的延迟时间（单位毫秒）
                    message.getMessageProperties().setDelay(delayTimes);
                    return message;
                }
            };
            rabbitTemplate.convertAndSend(RabbitMQDelayConfig.EXCHANGE_DELAY
                    ,"delay.article"
                    ,articleId
                    ,messagePostProcessor);

        }

        //TODO 文章文本自动检测（自动审核）
        String reviewTextResult = ArticleReviewLevel.REVIEW.type;
        if(reviewTextResult.equalsIgnoreCase(ArticleReviewLevel.PASS.type)){
            //修改当前文章，标记状态为通过
            updateArticleStatus(articleId,ArticleReviewStatus.SUCCESS.type);
        }else if(reviewTextResult.equalsIgnoreCase(ArticleReviewLevel.REVIEW.type)){
            //修改当前文章，标记状态为人工审核
            updateArticleStatus(articleId,ArticleReviewStatus.WAITING_MANUAL.type);
        }else{
            //审核不通过
            updateArticleStatus(articleId,ArticleReviewStatus.FAILED.type);
        }
    }

    @Transactional
    @Override
    public void updateAppointToPublich() {
        articleMapperCustome.updateAppointToPublich();
    }

    @Override
    public PagedGridResult queryMyArticleList(String userId, String keyword,
                                              Integer status, Date startDate, Date endDate,
                                              Integer page, Integer pageSize) {
        Example example = new Example(Article.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("publishUserId",userId);

        if(StringUtils.isNotBlank(keyword)){
            criteria.andLike("title","%"+keyword+"%");
        }

        if(ArticleReviewStatus.isArticleStatusValid(status)){
            criteria.andEqualTo("articleStatus",status);
        }

        if(status!=null&&status==12){
            criteria.andEqualTo("articleStatus",ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus",ArticleReviewStatus.WAITING_MANUAL.type);
        }

        criteria.andEqualTo("isDelete",YesOrNo.NO.type);

        if(startDate!=null){
            criteria.andGreaterThanOrEqualTo("publishTime",startDate);
        }
        if(endDate!=null){
            criteria.andLessThanOrEqualTo("publishTime",endDate);
        }
        PageHelper.startPage(page,pageSize);
        List<Article> alist= articleMapper.selectByExample(example);

        return setterPagedGrid(alist,page);
    }

    @Transactional
    @Override
    public void updateArticleStatus(String articleId, Integer pending) {
        Example example = new Example(Article.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id",articleId);
        Article pendingArticle = new Article();
        pendingArticle.setArticleStatus(pending);
        int res =  articleMapper.updateByExampleSelective(pendingArticle,example);
        if(res!=1){
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }

        //如果审核通过，查询article，把相应的数据字段存入es
        if(pending==ArticleReviewStatus.SUCCESS.type){
            Article result = articleMapper.selectByPrimaryKey(articleId);
            //如果即时发布，则审核通过后存入es中
            if(result.getIsAppoint()==ArticleAppointType.IMMEDIATELY.type){
                ArticleEo articleEo = new ArticleEo();
                BeanUtils.copyProperties(result,articleEo);
                IndexQuery indexQuery = new IndexQueryBuilder().withObject(articleEo).build();
                estemplate.index(indexQuery);
            }
            //TODO 定时发布此处不会存入es，需要在定时的延迟队列中执行
        }

    }

    @Override
    public PagedGridResult queryAllArticleListAdmin(Integer status, Integer page, Integer pageSize) {
        Example articleExample = new Example(Article.class);
        articleExample.orderBy("createTime").desc();

        Example.Criteria criteria = articleExample.createCriteria();
        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }

        // 审核中是机审和人审核的两个状态，所以需要单独判断
        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }

        //isDelete 必须是0
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        /**
         * page: 第几页
         * pageSize: 每页显示条数
         */
        PageHelper.startPage(page, pageSize);
        List<Article> list = articleMapper.selectByExample(articleExample);
        return setterPagedGrid(list, page);
    }

    @Transactional
    @Override
    public void deleteArticle(String userId, String articleId) {
        Example articleExample = makeExampleCriteria(userId, articleId);

        Article pending = new Article();
        pending.setIsDelete(YesOrNo.YES.type);

        int result = articleMapper.updateByExampleSelective(pending, articleExample);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }

        estemplate.delete(ArticleEo.class,articleId);
        deleteHTML(articleId);

    }

    @Transactional
    @Override
    public void withdrawArticle(String userId, String articleId) {
        Example articleExample = makeExampleCriteria(userId, articleId);
        Article pending = new Article();
        pending.setArticleStatus(ArticleReviewStatus.WITHDRAW.type);

        int result = articleMapper.updateByExampleSelective(pending, articleExample);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }
        estemplate.delete(ArticleEo.class,articleId);
        deleteHTML(articleId);
    }

    private Example makeExampleCriteria(String userId, String articleId) {
        Example articleExample = new Example(Article.class);
        Example.Criteria criteria = articleExample.createCriteria();
        criteria.andEqualTo("publishUserId", userId);
        criteria.andEqualTo("id", articleId);
        return articleExample;
    }

    @Transactional
    @Override
    public void updateArticleToGridFS(String articleId, String fileId) {
        Article pendingArticle = new Article();
        pendingArticle.setId(articleId);
        pendingArticle.setMongoFileId(fileId);
        articleMapper.updateByPrimaryKeySelective(pendingArticle);
    }

    @Autowired
    private GridFSBucket gridFSBucket;
    /**
     * 文章撤回删除后，删除静态化的html
     */
    private void deleteHTML(String articleId) {
        // 1. 查询文章的mongoFileId
        Article pending = articleMapper.selectByPrimaryKey(articleId);
        String articleMongoId = pending.getMongoFileId();

        // 2. 删除GridFS上的文件
        gridFSBucket.delete(new ObjectId(articleMongoId));

        // 3. 删除消费端的HTML文件
        doDeleteArticleHTML(articleId);
        //doDeleteArticleHTMLByMQ(articleId);
    }

    @Autowired
    public RestTemplate restTemplate;
    private void doDeleteArticleHTML(String articleId) {
        String url = "http://html.imoocnews.com:8002/article/html/delete?articleId=" + articleId;
        ResponseEntity<Integer> responseEntity = restTemplate.getForEntity(url, Integer.class);
        int status = responseEntity.getBody();
        if (status != HttpStatus.OK.value()) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }
    }

    @Override
    public void updateArticleToPublich(String articleId) {
        Article article = new Article();
        article.setId(articleId);
        article.setIsAppoint(ArticleAppointType.IMMEDIATELY.type);
        articleMapper.updateByPrimaryKeySelective(article);
    }
}
