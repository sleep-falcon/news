package com.imooc.article.controller;

import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.article.ArticleControllerApi;
import com.imooc.api.controller.article.ArticlePortalControllerApi;
import com.imooc.api.controller.user.UserInfoControllerApi;
import com.imooc.article.service.ArticlePortalService;
import com.imooc.article.service.ArticleService;
import com.imooc.enums.ArticleCoverType;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Article;
import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.pojo.eo.ArticleEo;
import com.imooc.pojo.vo.AppUserVo;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.pojo.vo.IndexArticleVO;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@RestController
public class ArticlePortalController extends BaseController implements ArticlePortalControllerApi {
    final static Logger logger = LoggerFactory.getLogger(ArticlePortalController.class);

    @Autowired
    private ArticlePortalService articlePortalService;

    @Autowired
    private ElasticsearchTemplate estemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public GraceJSONResult eslist(String keyword, Integer category, Integer page, Integer pageSize) {
        /**
         * es??????
         * ???????????????????????????
         * ???????????????????????????
         * ????????????????????????
         *
         */
        //es?????????0?????????????????????page??????1
        if(page<1){
            return null;
        }
        page--;
        Pageable pageable = PageRequest.of(page,pageSize);
        SearchQuery query = null;
        AggregatedPage<ArticleEo> pagedArticle = null;

        if(StringUtils.isBlank(keyword)&&category==null){
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(pageable)
                    .build();
            pagedArticle= estemplate.queryForPage(query, ArticleEo.class);
        }

        if(StringUtils.isBlank(keyword)&&category!=null){
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("categoryId",category))
                    .withPageable(pageable)
                    .build();

            pagedArticle= estemplate.queryForPage(query, ArticleEo.class);

        }
        String searchField = "title";
        if(StringUtils.isNotBlank(keyword)&&category==null){
            String preTag = "<font color='red'>";
            String postTag = "<font color='red'>";

            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchQuery(searchField,keyword))
                    .withHighlightFields(new HighlightBuilder.Field(searchField)
                            .preTags(preTag)
                            .postTags(postTag))
                    .withPageable(pageable)
                    .build();
            pagedArticle = estemplate.queryForPage(query, ArticleEo.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<ArticleEo> articleHighLightList = new ArrayList<>();
                    SearchHits hits = searchResponse.getHits();

                    for (SearchHit hit : hits) {
                        HighlightField field = hit.getHighlightFields().get(searchField);
                        String title = field.getFragments()[0].toString();

                        // ????????????????????????????????????
                        String articleId = (String) hit.getSourceAsMap().get("id");
                        Integer categoryId = (Integer) hit.getSourceAsMap().get("categoryId");
                        Integer articleType = (Integer) hit.getSourceAsMap().get("articleType");
                        String articleCover = (String) hit.getSourceAsMap().get("articleCover");
                        String publishUserId = (String) hit.getSourceAsMap().get("publishUserId");
                        Long dateLong = (Long) hit.getSourceAsMap().get("publishTime");
                        Date publishTime = new Date(dateLong);

                        ArticleEo articleEO = new ArticleEo();
                        articleEO.setId(articleId);
                        articleEO.setTitle(title);
                        articleEO.setCategoryId(categoryId);
                        articleEO.setArticleType(articleType);
                        articleEO.setArticleCover(articleCover);
                        articleEO.setPublishUserId(publishUserId);
                        articleEO.setPublishTime(publishTime);
                        articleHighLightList.add(articleEO);
                    }
                    return new AggregatedPageImpl<>((List<T>)articleHighLightList
                            ,pageable,searchResponse.getHits().totalHits);
                }
                @Override
                public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                    return null;
                }
            });
        }

        //AggregatedPage<ArticleEo> pagedArticle= estemplate.queryForPage(query, ArticleEo.class);
        List<ArticleEo> articleEoList= pagedArticle.getContent();
        List<Article> articleList= new ArrayList<>();
        for(ArticleEo a:articleEoList){
            //System.out.println(a.getTitle());
            Article article = new Article();
            BeanUtils.copyProperties(a,article);
            articleList.add(article);
        }
        PagedGridResult gridResult = new PagedGridResult();
        gridResult.setRows(articleList);
        gridResult.setPage(page+1);
        gridResult.setTotal(pagedArticle.getTotalPages());
        gridResult.setRecords(pagedArticle.getTotalElements());
        return GraceJSONResult.ok(rebuildArticleGrid(gridResult));
    }

    @Override
    public GraceJSONResult list(String keyword, Integer category, Integer page, Integer pageSize) {
       if(page==null){
           page = COMMON_START_PAGE;
       }
       if(pageSize==null){
           pageSize = COMMON_PAGE_SIZE;
       }

       PagedGridResult gridResult = articlePortalService.queryIndexArticle(keyword,category,page,pageSize);

       //?????????????????????????????????????????????????????????????????????????????????

       List<Article> list = (List<Article>)gridResult.getRows();
       //???????????????id???????????????set?????????????????????
        Set<String> ids = new HashSet<>();
        List<String> idList = new ArrayList<>();
        for (Article a: list){
            //??????????????????ID Set
            ids.add(a.getPublishUserId());
            //??????????????????ID List
            idList.add(REDIS_ARTICLE_READ_COUNTS+":"+a.getId());
        }
        //??????redis???mget????????????API?????????????????????
        List<String> readCounts = redisOperator.mget(idList);


        //?????????????????????restTemplate??????????????????????????????????????????
//        String userSeverUrl = "http://user.imoocnews.com:8003/user/queryByIds?userIds="
//                + JsonUtils.objectToJson(ids);
//        ResponseEntity<GraceJSONResult> responseEntity = restTemplate.getForEntity(userSeverUrl,GraceJSONResult.class);

        GraceJSONResult bodyresult = userMicroService.queryByIds(JsonUtils.objectToJson(ids));

        List<AppUserVo> appUserVosList = null;
        if(bodyresult.getStatus()==200){
            String userjson = JsonUtils.objectToJson(bodyresult.getData());
            appUserVosList = JsonUtils.jsonToList(userjson,AppUserVo.class);
        }

        //????????????list?????????????????????
        List<IndexArticleVO> res = new ArrayList<>();

        for(int i  = 0;i<list.size();i++){
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            Article a = list.get(i);
            BeanUtils.copyProperties(a, indexArticleVO);
            //???appUserVosList???????????????????????????
            AppUserVo publisher =getUserIfPublish(a.getPublishUserId(),appUserVosList);
            indexArticleVO.setPublisherVO(publisher);

            //??????????????????????????????????????????
            String redisCountStr = readCounts.get(i);
            int rdcounts = 0;
            if(StringUtils.isNotBlank(redisCountStr))
                rdcounts = Integer.valueOf(redisCountStr);
            indexArticleVO.setReadCounts(rdcounts);
            res.add(indexArticleVO);
        }
        gridResult.setRows(res);

       return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult hotList() {
        return GraceJSONResult.ok(articlePortalService.queryHotList());
    }

    private AppUserVo getUserIfPublish(String pId, List<AppUserVo> pulishList){
        for (AppUserVo userVo:pulishList){
            if(userVo.getId().equalsIgnoreCase(pId)){
                return userVo;
            }
        }
        return null;
    }

    @Override
    public GraceJSONResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {

        System.out.println("writerId=" + writerId);

        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articlePortalService.queryArticleListOfWriter(writerId, page, pageSize);
        gridResult = rebuildArticleGrid(gridResult);
        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult queryGoodArticleListOfWriter(String writerId) {
        PagedGridResult gridResult = articlePortalService.queryGoodArticleListOfWriter(writerId);
        return GraceJSONResult.ok(gridResult);
    }

    private PagedGridResult rebuildArticleGrid(PagedGridResult gridResult) {
        // START
        List<Article> list = (List<Article>)gridResult.getRows();

        // 1. ???????????????id??????
        Set<String> idSet = new HashSet<>();
        List<String> idList = new ArrayList<>();
        for (Article a : list) {
            // 1.1 ??????????????????set
            idSet.add(a.getPublishUserId());
            // 1.2 ????????????id???list
            idList.add(REDIS_ARTICLE_READ_COUNTS + ":" + a.getId());
        }

        // ??????redis???mget????????????api?????????????????????
        List<String> readCountsRedisList = redisOperator.mget(idList);

        List<AppUserVo> publisherList = getPublisherList(idSet);

        // 3. ????????????list?????????????????????
        List<IndexArticleVO> indexArticleList = new ArrayList<>();

        for (int i = 0 ; i < list.size() ; i ++) {
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            Article a = list.get(i);
            BeanUtils.copyProperties(a, indexArticleVO);

            // 3.1 ???publisherList?????????????????????????????????
            AppUserVo publisher  = getUserIfPublish(a.getPublishUserId(), publisherList);
            indexArticleVO.setPublisherVO(publisher);

            // 3.2 ?????????????????????????????????????????????
            String redisCountsStr = readCountsRedisList.get(i);
            int readCounts = 0;
            if (StringUtils.isNotBlank(redisCountsStr)) {
                readCounts = Integer.valueOf(redisCountsStr);
            }
            indexArticleVO.setReadCounts(readCounts);

            indexArticleList.add(indexArticleVO);
        }
        gridResult.setRows(indexArticleList);
    // END
        return gridResult;
    }


    //????????????????????????????????????????????????????????????
    @Autowired
    private DiscoveryClient discoveryClient;
    @Autowired
    private UserInfoControllerApi userMicroService;

    /**
     * ????????????????????????????????????????????????
     * @param idSet
     * @return
     */
    private List<AppUserVo> getPublisherList(Set idSet) {
//        String serviceId = "SERVICE-USER";
//        List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceId);
//        ServiceInstance userInstance = instanceList.get(0);

//        String userServerUrlExecute
//                = "http://"+serviceId
//                +"/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);

//        ResponseEntity<GraceJSONResult> responseEntity
//                = restTemplate.getForEntity(userServerUrlExecute, GraceJSONResult.class);
//        GraceJSONResult bodyResult = responseEntity.getBody();

        GraceJSONResult bodyResult = userMicroService.queryByIds(JsonUtils.objectToJson(idSet));
        List<AppUserVo> publisherList = null;
        if (bodyResult.getStatus() == 200) {
            String userJson = JsonUtils.objectToJson(bodyResult.getData());
            publisherList = JsonUtils.jsonToList(userJson, AppUserVo.class);
        }else{
            publisherList = new ArrayList<>();
        }
        return publisherList;
    }

    @Override
    public GraceJSONResult detail(String articleId) {
       ArticleDetailVO detailVO = articlePortalService.queryDetail(articleId);
       Set<String> idSet = new HashSet<>();
        idSet.add(detailVO.getPublishUserId());
       List<AppUserVo> publish = getPublisherList(idSet);
       if(!publish.isEmpty()){
           detailVO.setPublishUserName(publish.get(0).getNickname());
       }
       detailVO.setReadCounts(getCountsFromRedis(REDIS_ARTICLE_READ_COUNTS+":"+articleId));
       return GraceJSONResult.ok(detailVO);
    }

    @Override
    public GraceJSONResult readArticle(String articleId, HttpServletRequest request) {
        String userIp = IPUtil.getRequestIp(request);
        //????????????????????????IP??????????????????key?????????redis????????????IP????????????????????????????????????????????????
        //key????????????????????????
        redisOperator.setnx(REDIS_ALREADY_READ+":"+articleId+":"+userIp,userIp);

        redisOperator.increment(REDIS_ARTICLE_READ_COUNTS+":"+articleId,1);
        return GraceJSONResult.ok();
    }

    @Override
    public Integer readCounts(String articleId) {
       return getCountsFromRedis(REDIS_ARTICLE_READ_COUNTS+":"+articleId);
    }
}
