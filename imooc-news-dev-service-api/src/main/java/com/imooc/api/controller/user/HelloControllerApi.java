package com.imooc.api.controller.user;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
@Api(value = "controller的标题", tags = {"xx功能的controller"})
public interface HelloControllerApi {
    /**
     * api的作用：
     * API相当于企业的部门经理，其他的服务层都是实现==员工
     * 老板（开发人员）看一下每个人的进度与工作
     * 老板不会去问员工，只会对接部门经理
     * 所有的API接口就是统一在这里进行管理和调度的，微服务也是如此
     *
     * 运作：
     * 现在所有接口都在这里暴露，实现都在各自的微服务中
     * 本项目只写接口不写实现，实现在各自的微服务工程
     * controller也就分散在各个微服务工程中，多了很难统一管理和查看
     * 其次微服务之间的调用都是基于接口的
     * 如果不这么做微服务之间就需要相互依赖==》高耦合
     * 此外，接口就是一套规范，实现都是各自工程去做
     * 如果未来出现新的Javaweb框架，不需要修改接口，只需要修改对应实现即可
     *
     *swagger2：基于接口的文档自动生成
     * 所有配置文件只需要一份==》管理方便
     * @return
     */
    @GetMapping("hello")
    @ApiOperation(value = "hello方法的接口",notes = "hello方法的接口",httpMethod = "GET")
    public Object hello();
}