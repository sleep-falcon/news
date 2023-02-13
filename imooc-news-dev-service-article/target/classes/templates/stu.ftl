<html>
<head>
    <title>Hello</title>
</head>
<body>
    <#--
        freemarker 页面的语法构成：
        1 注释
        2 表达式${...}
        3 普通文本，基本的html标签
        4 指令
    -->
    <div>
        hello ${there}
    </div>
<br>
    <div>
        用户ID：${stu.uid}<br>
        用户姓名：${stu.username}<br>
        年龄：${stu.age}<br>
        生日：${stu.birthday?string('yyyy-MM-dd HH:mm:ss')}<br>
        账户余额：${stu.amount}<br>
        已育：${stu.haveChild?string('yes','no')}<br>
    </div>
<br>
    <div>
        <#list stu.articleList as article>
            <div>
                <span> ${article.id}</span>
                <span> ${article.title}</span>
            </div>
        </#list >
    </div>
    <br>
    <div>
        <#list stu.parents?keys as key>
            <div>
                ${stu.parents[key]}
            </div>
        </#list >
    </div>
<br>
<div>
    <#if stu.uid == '10010'>
        happy new year!
    </#if >

</div>
</body>
</html>