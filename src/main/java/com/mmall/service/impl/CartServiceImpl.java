package com.mmall.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.service.ICartService;
import com.mmall.service.impl.pojo.Cart;
import com.mmall.service.impl.pojo.Product;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车的service的实现类
 *
 * @author Liupeng
 * @createTime 2018-05-05 21:23
 **/
@Service("iCartService")
public class CartServiceImpl implements ICartService{

    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private ProductMapper productMapper;


    /**
     * 添加商品到用户购物车中
     * @param userId 用户ID
     * @param productId 商品ID
     * @param count 商品数量
     * @return 是否添加成功
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count){
        //判断参数是否正确
        if(productId == null || count == null){
          return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
      }
        //通过UserId和parductId查找当前用户的Cart
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart == null){
            //说明该商品没有在购物车的记录中，需要添加该商品信息
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);//设置数量
            //默认添加商品就选中
            cartItem.setChecked(Const.Cart.CHECKED);//设置选中状态
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        }else{
            //说明该购物车里面有商品，则只添加数量
            count = count + cart.getQuantity();
            cart.setQuantity(count);
            //更新购物车信息
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    /**
     *修改购物车中的信息
     * @param userId 用户id，用于获得用户购物车信息
     * @param productId 需要修改的物品id
     * @param count prodcut修改后的数量
     * @return 是否修改成功
     */
    @Override
    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count) {
        //判断参数是否正确
        if(productId == null || count == null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //获得cart对象
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if(cart != null){
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);//放在if外面应该是一样的
        }
        return this.list(userId);
    }

    /**
     *删除购物车中的商品，可同时删除多个
     * @param userId 用户id
     * @param productIds  商品id集合。与前端约定，多个productId使用，进行分割
     * @return  是否成功删除购物车中的信息
     */
    public ServerResponse<CartVo> deleteProduct(Integer userId,String productIds){
        //使用Guava库，对productIds进行切割。
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductId(userId,productList);
        return this.list(userId);
    }

    /**
     * 展示购物车中的信息
     * @param userId 当前用户的购物车信息
     * @return 购物车的cartVo对象
     */
    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 反选或者正选操作
     * @param userId 用户id
     * @param productId 产品id
     * @param checked 是否选中商品
     * @return 当前商品的checked是否勾选信息
     */
    @Override
    public ServerResponse<CartVo> SelectOrUnselect(Integer userId,Integer productId,Integer checked){
        cartMapper.checkOruncheckProduct(userId,productId,checked);
        return this.list(userId);
    }

    /**
     * 通过userid查询当前购物车中的所有数量信息
     * 注意：
     * 在对应的mapper中，使用了IFNULL 语句，是因为，在mybatis默认的sum函数，如果没有结果时，
     * 则sum是为null的，但是函数的返回值又是int，所有null类型不能赋给int类型
     * 我们可以在service层处理该异常，也可以在dao层处理异常，但是最好的处理方法
     * 就是在sql语句执行处，加入IFNULL mybatis内置函数进行处理
     * IFNULL(sum(quantity),0) 如果为null，则默认为0。
     * @param userId 用户id
     * @return 当前购物车中的所有商品的数量
     */
    @Override
    public ServerResponse<Integer> selectCartProductCount(Integer userId){
        if(userId == null){
            return  ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
         * 购物车核心方法
         * @param userid 用户id号
         * @return 购物车的vo对象
         */
    private CartVo getCartVoLimit(Integer userid) {
        CartVo cartVo = new CartVo();
        List<Cart> cartList = cartMapper.selectCartByUserId(userid);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        // 商业计算精度处理 BigDecimalUtil类中
        // Start----------
        //购物车中的所有商品总价
        BigDecimal cartBigDecimal = new BigDecimal("0");//先开始默认总价为0
        //Test End----------

        if (CollectionUtils.isNotEmpty(cartList)) {
            for (Cart cartItem : cartList) {
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setProductId(cartItem.getProductId());
                cartProductVo.setUserId(userid);

                //通过购物车信息，得到商品信息
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                //继续拼接vo信息
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductStock(product.getStock());
                    cartProductVo.setProductPrice(product.getPrice());
                    //判断商品的库存和当前购物车中的数量是否一致
                    int buylimitCount = 0;//当前最多的库存数量
                    if (product.getStock() >= cartItem.getQuantity()) {
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                        //库存充足的是否
                        buylimitCount = cartItem.getQuantity();
                    } else {
                        //购买数量超过了库存后，需要提示前端，并更新购物车中的数量
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        buylimitCount = product.getStock();
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buylimitCount);
                        //通过id来更新数量
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buylimitCount);
                    //计算当前商品的总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(), cartItem.getQuantity()));
                    //记录是否勾选
                    cartProductVo.setProductChecked(cartItem.getChecked());
                //如果勾选了，则记录价格到总价中
                if (cartProductVo.getProductChecked() == Const.Cart.CHECKED) {
                    //将当前的商品总价，加到购物车的总价中去
                    cartBigDecimal = BigDecimalUtil.add(cartProductVo.getProductTotalPrice().doubleValue(), cartBigDecimal.doubleValue());
                }
                //向list中写入当前cartProductVo
                // cartProductVoList购物车和ProductVo对象
                }
                cartProductVoList.add(cartProductVo);
        }
    }
        //设置CartVo对象
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalprice(cartBigDecimal);
        //判断是否全部选中,true是全选，false不是全选
        cartVo.setAllChecked(this.getAllCheckedStatus(userid));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    //判断该用户的购物车是否全选
    private boolean getAllCheckedStatus(Integer userid){
        if(userid == null){
            return false;
        }
        //根据用户名查找当前有没有未被勾选的商品，如果有则没有全选，如果没有则全选了!!!
        //这种方法可以使得sql语句变得很简单!!!!!!!
        return cartMapper.selectCartProductStatusByUserId(userid)==0;//
    }
}