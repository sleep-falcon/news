package com.imooc.exceptions;

import com.imooc.grace.result.ResponseStatusEnum;

/**
 * 自定义的异常
 * 目的：统一处理异常信息
 *      便于解耦，service与controller错误的解耦，不会被service返回的类型限制
 */
public class MyCustomException extends RuntimeException{
    public ResponseStatusEnum getRse() {
        return rse;
    }

    public void setRse(ResponseStatusEnum rse) {
        this.rse = rse;
    }

    private  ResponseStatusEnum rse;
    public MyCustomException(ResponseStatusEnum rse){
        super("异常状态码为："+rse.status()+"；具体异常信息为："+ rse.msg());
        this.rse = rse;
    }

}
