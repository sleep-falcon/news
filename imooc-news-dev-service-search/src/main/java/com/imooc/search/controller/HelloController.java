package com.imooc.search.controller;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.search.pojo.Stu;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @GetMapping("createIndex")
    public Object createIndex(){
        elasticsearchTemplate.createIndex(Stu.class);
        return GraceJSONResult.ok();
    }

    @GetMapping("deleteIndex")
    public Object deleteIndex(){
        elasticsearchTemplate.deleteIndex(Stu.class);
        return GraceJSONResult.ok();
    }

    @GetMapping("addDoc")
    public Object addDoc(){
        Stu stu = new Stu();
        stu.setStuId(1001l);
        stu.setAge(15);
        stu.setName("imooc");
        stu.setMoney(100.2f);
        stu.setDesc("学习测试elastic search");
        IndexQuery query = new IndexQueryBuilder()
                                .withObject(stu)
                                .build();
        elasticsearchTemplate.index(query);
        return GraceJSONResult.ok();
    }

    @GetMapping("updateDoc")
    public Object updateDoc(){
        Map<String,Object> updateMap = new HashMap<>();
        updateMap.put("desc","hello world");
        updateMap.put("age",22);

        IndexRequest indexRequest = new IndexRequest();
        indexRequest.source(updateMap);
        UpdateQuery uq = new UpdateQueryBuilder()
                .withClass(Stu.class)
                .withId("1001")
                .withIndexRequest(indexRequest).build();

        elasticsearchTemplate.update(uq);
        return GraceJSONResult.ok();
    }

    @GetMapping("getDoc")
    public Object getDoc(String id){
        GetQuery getQuery = new GetQuery();
        getQuery.setId(id);
        Stu stu =  elasticsearchTemplate.queryForObject(getQuery,Stu.class);
        return GraceJSONResult.ok(stu.toString());
    }

    @GetMapping("deleteDoc")
    public Object deleteDoc(String id){
        elasticsearchTemplate.delete(Stu.class,id);
        return GraceJSONResult.ok();
    }

}
