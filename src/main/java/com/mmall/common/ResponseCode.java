package com.mmall.common;

/**
 * 响应编码的枚举类
 * 对于每个情况，都进行了对应的描述
 * 如果后期需要增加情况，直接加在后面
 *
 * @author Liupeng
 * @create 2018-04-28 22:50
 **/
public enum ResponseCode {
    SUCCESS(0, "SUCCESS"),
    ERROR(1, "ERROR"),
    NEED_LOGIN(10, "NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT");

    private final int code;//从前端得到的响应
    private final String desc;//从前端得到的信息描述

    ResponseCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
    public int getCode(){
        return this.code;
    }
    public String getDesc(){
        return this.desc;
    }
}