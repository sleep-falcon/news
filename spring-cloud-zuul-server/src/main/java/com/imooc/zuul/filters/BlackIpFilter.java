package com.imooc.zuul.filters;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.ZuulFilterResult;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 构建zuul自定义过滤器
 */
@Component
@RefreshScope
public class BlackIpFilter extends ZuulFilter {
    public BlackIpFilter() {
        super();
    }

    @Override
    public boolean isStaticFilter() {
        return super.isStaticFilter();
    }

    @Override
    public String disablePropertyName() {
        return super.disablePropertyName();
    }

    @Override
    public boolean isFilterDisabled() {
        return super.isFilterDisabled();
    }

    @Override
    public ZuulFilterResult runFilter() {
        return super.runFilter();
    }

    @Override
    public int compareTo(ZuulFilter filter) {
        return super.compareTo(filter);
    }


    /**
     * 定义过滤器的类型
     * pre:请求被路由之前执行
     * route:在路由请求的时候执行
     * post:请求路由之后执行
     * error:处理请求时发生错误时执行
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 过滤器执行顺序，配置多个有顺序的过滤器
     * 执行顺序：从小到大
     * @return
     */
    @Override
    public int filterOrder() {
        return 2;
    }

    /**
     * 是否开启过滤器
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Value("${blackIp.continueCounts}")
    private Integer continueCounts;

    @Value("${blackIp.timeInterval}")
    private Integer timeInterval;

    @Value("${blackIp.limitTimes}")
    private Integer limitTimes;

    @Autowired
    private RedisOperator redisOperator;

    /**
     * 过滤器业务实现
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {

        System.out.println("执行IP黑名单过滤器。。。");

        System.out.println("continueCounts: "+continueCounts);

        System.out.println("timeInterval: "+timeInterval);

        System.out.println("limitTimes: "+limitTimes);



        //获得上下文对象
        RequestContext context = RequestContext.getCurrentContext();
        HttpServletRequest request = context.getRequest();

        //获得IP
        String ip = IPUtil.getRequestIp(request);

        /**
         * 判断IP10秒内的请求次数是否超过10次
         * 如果超过，则限制这个IP访问15秒，15秒以后再放行
         */
        final String ipRedisKey = "zuul-ip:" +ip;
        final String ipRedisLimitKey = "zuul-ip-limit:"+ip;

        //获得当前IP这个key的剩余时间
        long ttl = redisOperator.ttl(ipRedisLimitKey);
        //如果当前限制IP的key还存在剩余时间，说明这个IP不能访问，继续等待
        if(ttl>0){
            stopRequest(context);
            return null;
        }

        //在redis中累加IP的请求次数
        long requestCounts = redisOperator.increment(ipRedisKey,1);
        //从0开始计数，初期访问为1，设置过期时间
        if(requestCounts==1){
            redisOperator.expire(ipRedisKey,timeInterval);
        }

        //如果还能取得请求次数，说明用户连续请求的次数落在10S内
        //一旦请求次数超过连续访问限制，则限制IP的访问
        if(requestCounts>continueCounts){
            //限制访问时间
            redisOperator.set(ipRedisLimitKey,ipRedisLimitKey,limitTimes);
            stopRequest(context);
        }

        return null; //无意义，可以不用管
    }

    private void stopRequest(RequestContext context){
        //停止zuul向下路由，禁止通信
        context.setSendZuulResponse(false);
        context.setResponseStatusCode(200);

        context.setResponseBody(JsonUtils.objectToJson(
                GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR)));
        context.getResponse().setCharacterEncoding("utf-8");
        context.getResponse().setContentType(MediaType.APPLICATION_JSON_VALUE);

    }
}
