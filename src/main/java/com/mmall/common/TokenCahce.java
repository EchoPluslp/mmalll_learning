package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;


/**
 * Token的处理类
 *
 * @author Liupeng
 * @create 2018-04-30 9:24
 **/
public class TokenCahce {
    //声明日志
    private static Logger logger = LoggerFactory.getLogger(TokenCahce.class);

    //设置token前缀
    public static final  String TOKEN_PREFIX = "token_";
    //
    private static LoadingCache<String,String> loadingCache = CacheBuilder.newBuilder().initialCapacity(1000)
            .maximumSize(1000)//设置缓存最大容量，并使用LRU（最近最少使用算法）淘汰过多的数据
            .expireAfterAccess(12, TimeUnit.HOURS)//设置缓存最长有效时间
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    //默认数据加载实现方法，当调用get取值值，如果没有取到值，就会调用该方法.
                    return "null";//这里返回一个字符串，避免后续处理出现NULLPointException
                }
            });

    public static void setKey(String key,String value){
        loadingCache.put(key,value);
    }

    public static String getKey(String key){
        String value = "";
        try {
            value = loadingCache.get(key);
            if(Objects.equals(value,"null")){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get Error",e);
        }
        return null;
    }
}