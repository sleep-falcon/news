package com.imooc.article.controller;

import com.imooc.api.config.RabbitMQConfig;
import com.imooc.api.controller.BaseController;
import com.imooc.api.controller.article.ArticleControllerApi;
import com.imooc.api.controller.user.HelloControllerApi;
import com.imooc.article.service.ArticleService;
import com.imooc.enums.ArticleCoverType;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.exceptions.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.pojo.vo.AppUserVo;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.gridfs.GridFS;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.swagger.models.auth.In;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.*;


@RestController
public class ArticleController extends BaseController implements ArticleControllerApi {
    final static Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private ArticleService articleService;

    @Override
    public GraceJSONResult createArticle(NewArticleBO newArticleBO) {
        //?????????????????????????????????????????????????????????
        if(newArticleBO.getArticleType()== ArticleCoverType.ONE_IMAGE.type){
            if(StringUtils.isBlank(newArticleBO.getArticleCover())){
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_COVER_NOT_EXIST_ERROR);
            }
        }else if(newArticleBO.getArticleType()== ArticleCoverType.WORDS.type){
            newArticleBO.setArticleCover("");
        }

        //????????????ID????????????
        String collection = redisOperator.get(REDIS_ALL_CATEGORY);
        if(StringUtils.isBlank(collection)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }else{
            List<Category> catList = JsonUtils.jsonToList(collection,Category.class);
            Category temp = null;
            for(Category c: catList){
                if(c.getId()==newArticleBO.getCategoryId()){
                    temp = c;
                    break;
                }
            }
            if(temp==null){
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_CATEGORY_NOT_EXIST_ERROR);
            }else{
                articleService.createArticle(newArticleBO,temp);
            }
        }
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryMyList(String userId, String keyword, Integer status,
                                       Date startDate, Date endDate, Integer page,
                                       Integer pageSize) {
        if(StringUtils.isBlank(userId)){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_QUERY_PARAMS_ERROR);
        }

        if(page==null){
            page = COMMON_START_PAGE;
        }
        if(pageSize==null){
            pageSize = COMMON_PAGE_SIZE;
        }

        //?????????????????????service
        PagedGridResult res = articleService.queryMyArticleList(userId,  keyword,  status,
                                          startDate,  endDate,  page, pageSize);

        return GraceJSONResult.ok(res);
    }

    @Override
    public GraceJSONResult queryAllList(Integer status, Integer page, Integer pageSize) {
        if (page == null) {
            page = COMMON_START_PAGE;
        }

        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        PagedGridResult gridResult = articleService.queryAllArticleListAdmin(status, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }

    @Override
    public GraceJSONResult doReview(String articleId, Integer passOrNot) {
        Integer pendingStatus;

        if(passOrNot== YesOrNo.YES.type){
            //????????????
            pendingStatus = ArticleReviewStatus.SUCCESS.type;
        } else if (passOrNot ==YesOrNo.NO.type) {
            //????????????
            pendingStatus = ArticleReviewStatus.FAILED.type;
        }else {
            return  GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }
        //???????????????????????????????????????????????????/??????
        articleService.updateArticleStatus(articleId,pendingStatus);
        if(pendingStatus==ArticleReviewStatus.SUCCESS.type){
            //????????????????????????????????????
            try {
                //createArticleHtml
                String articleGridfsId = createArticleHtmlToGridFS(articleId);
                //?????????????????????????????????????????????
                articleService.updateArticleToGridFS(articleId,articleGridfsId);
                //??????????????????????????????html
                //doDownloadArticleHTML(articleId,articleGridfsId);

                //???????????????mq?????????????????????????????????????????????html
                doDownloadArticleHTMLByMQ(articleId,articleGridfsId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return GraceJSONResult.ok();
    }

    private void doDownloadArticleHTML(String articelId, String fileId){
        String url = "http://html.imoocnews.com:8002/article/html/download?articleId="
                +articelId+"&articleMongoId="+fileId;
         ResponseEntity<Integer> res= restTemplate.getForEntity(url,Integer.class);
         int status = res.getBody();
         if(status!= HttpStatus.OK.value()){
             GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
         }
    }
    //?????????
    private void doDownloadArticleHTMLByMQ(String articelId, String fileId){
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_ARTICLE
                ,"article.download.do",articelId+","+fileId);

    }

    @Value("${freemarker.html.article}")
    private String articlePath;

    /**
     * ????????????html
     * @param articleId
     * @throws Exception
     */
    public void createArticleHtml(String articleId) throws Exception{
        //??????freemarker????????????
        Configuration cfg = new Configuration(Configuration.getVersion());

        //??????freemarker????????????????????????????????????
        String classpath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classpath+"templates"));

        //??????????????????ftl??????
        Template template = cfg.getTemplate("detail.ftl","utf-8");

        //???????????????????????????
        //????????????rest
        ArticleDetailVO detailVO = getArticleDetail(articleId);

        //?????????????????????ftl???????????????html
        File temp = new File(articlePath);
        if(!temp.exists()){
            temp.mkdirs();
        }
        Writer out = new FileWriter(temp+File.separator+articleId +".html");
        Map<String,Object> map = new HashMap<>();
        map.put("articleDetail",detailVO);

        template.process(map,out);
        out.close();
    }

    public String createArticleHtmlToGridFS(String articleId) throws Exception{
        //??????freemarker????????????
        Configuration cfg = new Configuration(Configuration.getVersion());

        //??????freemarker????????????????????????????????????
        String classpath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classpath+"templates"));

        //??????????????????ftl??????
        Template template = cfg.getTemplate("detail.ftl","utf-8");

        //???????????????????????????
        //????????????rest
        ArticleDetailVO detailVO = getArticleDetail(articleId);
        Map<String,Object> map = new HashMap<>();
        map.put("articleDetail",detailVO);

        String htmlContent =  FreeMarkerTemplateUtils.processTemplateIntoString(template,map);
        //System.out.println(htmlContent);
        InputStream inputStream = IOUtils.toInputStream(htmlContent);
        ObjectId fileId =  gridFSBucket.uploadFromStream(articleId+".html",inputStream);
        return fileId.toString();
    }

    @Autowired
    private RestTemplate restTemplate;
    //???????????????????????????,????????????rest
    public ArticleDetailVO getArticleDetail(String articleId) {

        String url
                = "http://www.imoocnews.com:8001/portal/article/detail?articleId=" + articleId;
        ResponseEntity<GraceJSONResult> responseEntity
                = restTemplate.getForEntity(url, GraceJSONResult.class);
        GraceJSONResult bodyResult = responseEntity.getBody();
        ArticleDetailVO detailVO = null;
        if (bodyResult.getStatus() == 200) {
            String detailJson = JsonUtils.objectToJson(bodyResult.getData());
            detailVO = JsonUtils.jsonToPojo(detailJson, ArticleDetailVO.class);
        }
        return detailVO;
    }


    @Override
    public GraceJSONResult delete(String userId, String articleId) {
        articleService.deleteArticle(userId, articleId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult withdraw(String userId, String articleId) {
        articleService.withdrawArticle(userId, articleId);
        return GraceJSONResult.ok();
    }
}
