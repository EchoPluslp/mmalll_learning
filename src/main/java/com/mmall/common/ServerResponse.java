package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * 服务端响应对象
 * 目前的接口设计：http://git.oschina.net/imooccode/happymmallwiki/wikis/
 *
 * 根据页面请求可以知道，参数有status，msg，data
 * @author Liupeng
 * @create 2018-04-28 22:41
 **/
/*
该注解，代表对值为null的参数，不进行json序列化。
保证序列化json对象，如果是null的对象，key也会消失
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable{
    private int status;
    private String msg;
    private T data;//在返回的时候可以指定泛型里面的内容，也可以不指定泛型的内容，可以得到方法声明一个返回类型
    //实际上可以返回多种类型

    //编写全部构造方法
    private ServerResponse (int status){
        this.status = status;
    }
    private ServerResponse(int status,String msg){
        this.status = status;
        this.msg = msg;
    }
    private ServerResponse(int status,T data){
        this.status = status;
        this.data = data;
    }
    private ServerResponse(int status,String msg,T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    @JsonIgnore
    //判断响应是否成功,json序列化时，忽略该字段
    public boolean isSuccess(){
        return this.status==ResponseCode.SUCCESS.getCode();
    }

    public int getStatus(){
        return this.status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    //创建多种成功的服务器响应
    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static <T> ServerResponse<T> createBySuccess(String msg,T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }

    //创建多种失败的服务器响应
    public static <T> ServerResponse<T> createByError(){
        return  new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
        return  new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return  new ServerResponse<T>(errorCode,errorMessage);
    }

    }
