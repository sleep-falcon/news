package com.imooc.api.controller.article;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.CommentReplyBO;
import com.imooc.pojo.bo.NewArticleBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@Api(value = "文章评论相关业务的controller", tags = {"文章评论相关业务的controller"})
@RequestMapping("comment")
public interface CommentControllerApi {
    @PostMapping("createComment")
    @ApiOperation(value = "用户发表评论", notes = "用户发表评论", httpMethod = "POST")
    public GraceJSONResult createComment(@RequestBody @Valid CommentReplyBO commentReplyBO);

    @GetMapping("counts")
    @ApiOperation(value = "用户评论数查询", notes = "用户评论数查询", httpMethod = "GET")
    public GraceJSONResult counts(@RequestParam String articleId);

    @GetMapping("list")
    @ApiOperation(value = "查询文章所有评论列表", notes = "查询文章所有评论列表", httpMethod = "GET")
    public GraceJSONResult list(@RequestParam String articleId,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize);

    @PostMapping("mng")
    @ApiOperation(value = "查询我的评论管理列表", notes = "查询我的评论管理列表", httpMethod = "POST")
    public GraceJSONResult mng(@RequestParam String writerId,
                               @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                               @RequestParam Integer page,
                               @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                               @RequestParam Integer pageSize);


    @PostMapping("/delete")
    @ApiOperation(value = "作者删除评论", notes = "作者删除评论", httpMethod = "POST")
    public GraceJSONResult delete(@RequestParam String writerId,
                                  @RequestParam String commentId);

}
