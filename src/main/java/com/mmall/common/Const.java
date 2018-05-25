package com.mmall.common;


import com.google.common.collect.Sets;

import java.util.Set;

/**
 * 常量类
 *
 * @author Liupeng
 * @create 2018-04-29 21:10
 **/
public class Const {

    public static final String CURRENT_USER = "currentUser";
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface ProductListOrderBy {
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Cart {
        int CHECKED = 1; //购物车选中状态
        int UNCHECK = 0;//购物车未选中状态

        //当前购买的商品数量是否超过库存
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";//没有超过库存
        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";//超过了库存
    }

    //因为普通用户和管理员是一个组，但是使用枚举过于繁重,所以当前可以使用内部接口类，来将常量进行分组
    public interface Role {
        int ROLE_CUSTOMER = 0; //普通用户
        int ROLE_ADMIN = 1;//管理员
    }

    public enum ProductStatusEnum {
        ON_SALE(1, "销售中");
        private String value;
        private int code;

        ProductStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

    }

    //todo 学习枚举类的创建过程
    public enum orderStatusEnum {
        CANCELED(0, "已取消"),
        NO_PAY(10, "未支付"),
        PAID(20, "已支付"),
        SHIPPED(40, "已发货"),
        ORDER_SUCCESS(50, "订单完成"),
        ORDER_CLOSE(60, "订单关闭");

        orderStatusEnum(int code, String value) {
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static orderStatusEnum codeOf(Integer code){
            for (orderStatusEnum orderStatusEnum : values()) {
                if (orderStatusEnum.getCode() == code) {
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("订单状态没有找到对应的枚举");
        }
    }

    //订单状态查询和返回给支付宝的信息
    public interface AlipayCallback {
        String TRADE_STATUS_WAIT_BUYED_PAY = "WAIT_BUYER_PAY";
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";

        String RESPONSE_SECCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    public enum payPlatform {

        ALIPAY(1, "支付宝");

        payPlatform(int code, String value)
        {
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue() {
            return this.value;
        }

        public int getCode() {
            return this.code;
            }
        }

    public enum PaymentTypeEnum {
        ONLINE_PAY(1, "在线支付");
        PaymentTypeEnum(int code, String value)
        {
            this.code = code;
            this.value = value;
        }

        private String value;
        private int code;

        public String getValue() {
            return this.value;
        }

        public int getCode() {
            return this.code;
        }

        //todo 判断传入的参数是不是在线支付
        public static PaymentTypeEnum codeOf(int code) {
            for (PaymentTypeEnum paymentTypeEnum : values()) {
                if (paymentTypeEnum.getCode() == code) {
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的支付状态美剧");
           }
        }
    }