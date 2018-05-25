package com.mmall.controller.portal;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.service.IOrderService;
import com.mmall.service.impl.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

/**
 * orderControll控制
 *
 * @author Liupeng
 * @create 2018-05-16 21:50
 **/
@Controller
@RequestMapping("/order/")
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);


    /**
     * 创建订单
     * @param session 当前session，判断是否登陆
     * @param shippingId  收货地址id
     * @return 创建订单是否成功
     */
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session,Integer shippingId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    /**
     * 取消订单
     * @param session 当前session，判断是否登陆
     * @param orderNo 要取消的订单号码
     * @return 取消订单是否成功
     */
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    /**
     *前端 获取购物车中的商品信息
     * @param session  当前用户session
     * @return 返回orderProductVo对象，用于前端进行展示
     */
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    /**
     *  前端用户 获得订单详情
     * @param session 当前session 用于判断是否登陆
     * @param orderNo 订单号码
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(), orderNo);
    }

    /**
     * 用户端的订单列表页面
     * @param session 当前session 用于判断是否登陆
     * @param pageNum 默认pageNum
     * @param pageSize 默认pageSize
     * @return 返回当前用户的列表
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }




    /**
     *支付宝的支付方法
     * @param session
     * @param orderNo
     * @param request
     * @return
     */
    @RequestMapping("pay.do")
    @ResponseBody
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");

        return iOrderService.pay(orderNo, user.getId(), path);
    }

    /**
     *支付回调方法！！！
     * 支付宝回调接口
     * @param request 支付宝返回的请求
     * @return 如果回调成功 ，则返回支付宝要求的success字段，否则返回失败！！
     * 其中，还要判断是不是支付宝进行的回调
     */
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> map = Maps.newHashMap();
        //通过map得到所有的参数集合
        Map requestMap = request.getParameterMap();
        //生成迭代器，迭代map
        Iterator iterator = requestMap.keySet().iterator();
        while (iterator.hasNext()){
            String name = (String)iterator.next();
            //通过name获得requestMap中的所有值
            String[] values = (String[]) requestMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //将参数放入新设置的集合中
            map.put(name, valueStr);
        }
        //将支付宝回调的信息，加到日志中
        LOGGER.info("支付宝回调，sign:{},trade_status{},参数:{}", map.get("sign"), map.get("trade_status"), map.toString());
        map.remove("sign_type");
        try {
            //非常重要！！！ 验证回调的正确性，是不是支付宝发过来的，并且还要避免重复通知
            boolean alipayRSACheckV2 = AlipaySignature.rsaCheckV2(map, Configs.getAlipayPublicKey(), "utf-8", Configs.getSignType());

            if(!alipayRSACheckV2){
                return ServerResponse.createByErrorMessage("非法请求，验证不通过，在乱来，我就找网警了");
            }

        } catch (AlipayApiException e) {
            LOGGER.error("支付宝回调异常");
            e.printStackTrace();
        }
        //todo 验证各种数据

        ServerResponse serverResponse = iOrderService.aliCallback(map);
        if (serverResponse.isSuccess()) {
            return  Const.AlipayCallback.RESPONSE_SECCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }


    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }

        ServerResponse serverResponse =  iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if (serverResponse.isSuccess()) {
            //支付成功，则返回true
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }




}
