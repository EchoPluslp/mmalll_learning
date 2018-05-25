package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车的Vo
 *
 * @author Liupeng
 * @createTime 2018-05-07 23:21'
 * 用于向前端访问购物车的中的所有信息
 **/
public class CartVo {
    private List<CartProductVo> cartProductVoList;
    private BigDecimal cartTotalprice;
    private Boolean allChecked;//是否已经勾选
    private String imageHost; //主图片

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalprice() {
        return cartTotalprice;
    }

    public void setCartTotalprice(BigDecimal cartTotalprice) {
        this.cartTotalprice = cartTotalprice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}