package com.imooc.article.controller;

import com.imooc.pojo.Article;
import com.imooc.pojo.Spouse;
import com.imooc.pojo.Stu;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;


@Controller
@RequestMapping("free")
public class FreemarkerController{

    @Value("${freemarker.html.target}")
    private String htmlTarget;

    @GetMapping("createHTML")
    @ResponseBody
    public String createHtml(Model model) throws IOException, TemplateException {
        //配置freemarker基本环境
        Configuration cfg = new Configuration(Configuration.getVersion());

        //声明freemarker模版所需加载的目录的位置
        String classpath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classpath+"templates"));

        //获得现有模版ftl文件
        Template template = cfg.getTemplate("stu.ftl","utf-8");

        //获得动态数据
        String test = "news";
        model.addAttribute("there",test);
        model = makemodel(model);

        //融合动态数据和ftl模版，生成html
        File temp = new File(htmlTarget);
        if(!temp.exists()){
            temp.mkdirs();
        }
        Writer out = new FileWriter(temp+File.separator+"10010" +".html");
        template.process(model,out);
        out.close();
        return "ok";
    }



    @GetMapping("/test")
    //@ResponseBody
    public String hello(Model model){

        //定义输出到模版中的内容
        String test = "news";
        model.addAttribute("there",test);

        makemodel(model);

        //返回的stu是一个template
        //匹配 *ftl
        return "stu";
    }

    private Model makemodel(Model model){
        Stu stu  = new Stu();
        stu.setUid("10010");
        stu.setUsername("news");
        stu.setAmount(88.86f);
        stu.setAge(18);
        stu.setHaveChild(true);
        stu.setBirthday(new Date());

        Spouse spouse = new Spouse();
        spouse.setUsername("Lucy");
        spouse.setAge(25);

        stu.setSpouse(spouse);

        stu.setArticleList(getArticles());
        stu.setParents(getParents());

        model.addAttribute("stu",stu);

        return model;
    }

    private List<Article> getArticles(){
        Article article1 = new Article();
        article1.setId("1001");
        article1.setTitle("我是第一行");

        Article article2 = new Article();
        article2.setId("1002");
        article2.setTitle("我是第2行");

        Article article3 = new Article();
        article3.setId("1001");
        article3.setTitle("我是第3行");

        List<Article> list = new ArrayList<>();
        list.add(article1);
        list.add(article2);
        list.add(article3);
        return list;
    }

    private Map<String,String> getParents(){
        Map<String,String> parents = new HashMap<>();
        parents.put("father","yly");
        parents.put("mother","sdsd");
        return parents;
    }

}
