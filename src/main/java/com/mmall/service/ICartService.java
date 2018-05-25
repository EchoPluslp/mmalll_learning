package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * 购物车接口service
 *
 * @author Liupeng
 * @create 2018-05-05 21:23
 **/

public interface ICartService {
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);
    ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count);
    ServerResponse<CartVo> deleteProduct(Integer userId,String productIds);

    ServerResponse<CartVo> list(Integer id);

    ServerResponse<CartVo> SelectOrUnselect(Integer id,Integer productId,Integer checked);

    ServerResponse<Integer> selectCartProductCount(Integer userId);
}
