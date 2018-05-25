package com.mmall.dao;

import com.mmall.service.impl.pojo.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdProductId(@Param("UserId") Integer UserId,@Param("productId") Integer ProductId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectCartProductStatusByUserId(Integer userId);

    int deleteByUserIdProductId(@Param("userId") Integer userId, @Param("productList") List productList);

    int checkOruncheckProduct(@Param("userId") Integer userId,@Param("productId") Integer ProductId,@Param("checked")Integer checked);

    int selectCartProductCount(@Param("userId") Integer userId);

    List<Cart> selectCheckedCartByUserId(Integer userId);
}