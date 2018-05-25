package com.mmall.service.impl;

import com.alipay.api.AlipayResponse;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.demo.trade.config.Configs;
import com.alipay.demo.trade.model.ExtendParams;
import com.alipay.demo.trade.model.GoodsDetail;
import com.alipay.demo.trade.model.builder.AlipayTradePrecreateRequestBuilder;
import com.alipay.demo.trade.model.result.AlipayF2FPrecreateResult;
import com.alipay.demo.trade.service.AlipayTradeService;
import com.alipay.demo.trade.service.impl.AlipayTradeServiceImpl;
import com.alipay.demo.trade.utils.ZxingUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.*;
import com.mmall.service.IOrderService;
import com.mmall.service.impl.pojo.*;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.FTPUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.OrderItemVo;
import com.mmall.vo.OrderProductVo;
import com.mmall.vo.OrderVo;
import com.mmall.vo.ShippingVo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 订单的实现类
 *
 * @author Liupeng
 * @createTime 2018-05-16 21:58
 **/
@Service("iOrderService")
public class OrderServiceImpl implements IOrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private static AlipayTradeService tradeService = null;

    static {
        /** 使用Configs提供的默认参数
         *  AlipayTradeService可以使用单例或者为静态成员对象，不需要反复new
         */
        Configs.init("zfbinfo.properties");
        tradeService = new AlipayTradeServiceImpl.ClientBuilder().build();
    }

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ShippingMapper shippingMapper;


    /**
     * 支付订单
     * @param orderNo 需要支付的订单号码
     * @param userId 用户Id
     * @param path 二维码路径
     * @return  支付订单是否成功
     */
    public ServerResponse pay(Long orderNo, Integer userId, String path) {
        //与前端约定，使用map存放数据，比如，二维码地址，支付订单信息等.
        Map<String, String> resultMap = Maps.newHashMap();
        //根据用户id和订单id查找订单信息
        Order order = orderMapper.selectByuserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户没有对应的订单");
        }
        resultMap.put("orderNo", String.valueOf(order.getOrderNo()));

        /**
         * 根据支付宝中的demo，针对改项目进行修改
         */

        // (必填) 商户网站订单系统中唯一订单号，64个字符以内，只能包含字母、数字、下划线，
        // 需保证商户系统端不能重复，建议通过数据库sequence生成，
        String outTradeNo = order.getOrderNo().toString();

        // (必填) 订单标题，粗略描述用户的支付目的。如“xxx品牌xxx门店当面付扫码消费”
        String subject = new StringBuilder().append("嗨购扫码支付，订单号:").append(outTradeNo).toString();

        // (必填) 订单总金额，单位为元，不能超过1亿元
        // 如果同时传入了【打折金额】,【不可打折金额】,【订单总金额】三者,则必须满足如下条件:【订单总金额】=【打折金额】+【不可打折金额】
        String totalAmount = order.getPayment().toString();

        // (可选) 订单不可打折金额，可以配合商家平台配置折扣活动，如果酒水不参与打折，则将对应金额填写至此字段
        // 如果该值未传入,但传入了【订单总金额】,【打折金额】,则该值默认为【订单总金额】-【打折金额】
        String undiscountableAmount = "0";

        // 卖家支付宝账号ID，用于支持一个签约账号下支持打款到不同的收款账号，(打款到sellerId对应的支付宝账号)
        // 如果该字段为空，则默认为与支付宝签约的商户的PID，也就是appid对应的PID
        String sellerId = "";

        // 订单描述，可以对交易或商品进行一个详细地描述，比如填写"购买商品2件共15.00元"
        String body = new StringBuilder().append("订单").append(outTradeNo).append("购买商品总共")
                .append(totalAmount).append("元").toString();

        // 商户操作员编号，添加此参数可以为商户操作员做销售统计
        String operatorId = "test_operator_id";

        // (必填) 商户门店编号，通过门店号和商家后台可以配置精准到门店的折扣信息，详询支付宝技术支持
        String storeId = "test_store_id";

        // 业务扩展参数，目前可添加由支付宝分配的系统商编号(通过setSysServiceProviderId方法)，详情请咨询支付宝技术支持
        ExtendParams extendParams = new ExtendParams();
        extendParams.setSysServiceProviderId("2088100200300400500");

        // 支付超时，定义为120分钟
        String timeoutExpress = "120m";

        // 商品明细列表，需填写购买商品详细信息，
        List<GoodsDetail> goodsDetailList = new ArrayList<GoodsDetail>();

        //自定义sql，根据用户名和id获得到订单详情信息.
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(userId, orderNo);
        for (OrderItem orderItem : orderItemList) {
            // 创建一个商品信息，参数含义分别为商品id（使用国标）、名称、单价（单位为分）、数量，如果需要添加商品类别，详见GoodsDetail
            GoodsDetail goods = GoodsDetail.newInstance(orderItem.getProductId().toString(), orderItem.getProductName(),
                    BigDecimalUtil.mul(orderItem.getCurrentUnitPrice().doubleValue(), new Double(1000)).longValue(),
                    orderItem.getQuantity());
            // 创建好一个商品后添加至商品明细列表
            goodsDetailList.add(goods);
        }

        // 创建扫码支付请求builder，设置请求参数
        AlipayTradePrecreateRequestBuilder builder = new AlipayTradePrecreateRequestBuilder()
                .setSubject(subject).setTotalAmount(totalAmount).setOutTradeNo(outTradeNo)
                .setUndiscountableAmount(undiscountableAmount).setSellerId(sellerId).setBody(body)
                .setOperatorId(operatorId).setStoreId(storeId).setExtendParams(extendParams)
                .setTimeoutExpress(timeoutExpress)
                //支付宝回调地址
                .setNotifyUrl(PropertiesUtil.getProperty("alipay.callback.url"))//支付宝服务器主动通知商户服务器里指定的页面http路径,根据需要设置
                .setGoodsDetailList(goodsDetailList);


        AlipayF2FPrecreateResult result = tradeService.tradePrecreate(builder);

        switch (result.getTradeStatus()) {
            case SUCCESS:
                log.info("支付宝预下单成功: )");

                AlipayTradePrecreateResponse response = result.getResponse();
                dumpResponse(response);

                File folder = new File(path);
                if (!folder.exists()) {
                    folder.setWritable(true);
                    folder.mkdirs();
                    //保证二维码图片有地方放置
                }
                /**
                 * 细节处理：
                 * 我们得到的path路径的最后是没有/的
                 * 所以，在这里需要加上！！！！！！！！！！！！
                 *  需要修改为运行机器上的路径
                 */
                String qrPath = String.format(path + "/qr-%s.png", response.getOutTradeNo());
                //这是二维码文件名 ,这里 format后面的参数会替换%s!!
                String qrfilename = String.format("qr-%s.png", response.getOutTradeNo());

                //生成二维码
                ZxingUtils.getQRCodeImge(response.getQrCode(), 256, qrPath);

                File targetFile = new File(qrPath, qrfilename);
                //上传二维码到ftp服务器
                try {
                    FTPUtil.uploadFile(Lists.newArrayList(targetFile));
                } catch (IOException e) {
                    log.error("上传二维码失败", e);
                    e.printStackTrace();
                }
                log.info("qrPath:" + qrPath);

                //  注意分析拆分文件名，用于拼接url地址！！
                String qrUrl = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFile.getName();
                resultMap.put("qrUrl", qrUrl);//将url地址，放置map中。
                return ServerResponse.createBySuccess(resultMap);//返回当前map集合
            case FAILED:
                log.error("支付宝预下单失败!!!");
                return ServerResponse.createByErrorMessage("支付宝预下单失败!!!");
            case UNKNOWN:
                log.error("系统异常，预下单状态未知!!!");
                return ServerResponse.createByErrorMessage("系统异常，预下单状态未知!!!");
            default:
                log.error("不支持的交易状态，交易返回异常!!!");
                return ServerResponse.createByErrorMessage("不支持的交易状态，交易返回异常!!!");
        }
    }

    // 简单打印应答
    private void dumpResponse(AlipayResponse response) {
        if (response != null) {
            log.info(String.format("code:%s, msg:%s", response.getCode(), response.getMsg()));
            if (StringUtils.isNotEmpty(response.getSubCode())) {
                log.info(String.format("subCode:%s, subMsg:%s", response.getSubCode(),
                        response.getSubMsg()));
            }
            log.info("body:" + response.getBody());
        }
    }

    public ServerResponse aliCallback(Map<String, String> params) {
        //订单号
        Long orderNo = Long.parseLong(params.get("out_trade_no"));
        //通过该订单号，查询数据库中有没有数据库的信息
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有改订单号码，忽略回调");
        }
        //交易订单号
        String tradeNo = params.get("trade_no");
        //交易状态
        String tradeStatus = params.get("trade_status");
        //判断交易状态~避免支付宝重复调用
        if (order.getStatus() >= Const.orderStatusEnum.PAID.getCode()) {
            return ServerResponse.createBySuccessMessage("支付宝重复调用");
        }

        //如果从支付宝获得的交易状态是否成功，如果成功，则设置交易为已付款
        if (Const.AlipayCallback.TRADE_STATUS_TRADE_SUCCESS.equals(tradeStatus)) {
            order.setPaymentTime(DateTimeUtil.strToDate(params.get("gmt_payment")));
            order.setStatus(Const.orderStatusEnum.PAID.getCode());
            orderMapper.updateByPrimaryKeySelective(order);
        }

        //拼接PayInfo类
        PayInfo payInfo = new PayInfo();
        payInfo.setUserId(order.getUserId());
        payInfo.setOrderNo(order.getOrderNo());
        payInfo.setPayPlatform(Const.payPlatform.ALIPAY.getCode());
        payInfo.setPlatformNumber(tradeNo);
        payInfo.setPlatformStatus(tradeStatus);
        //将订单信息插入到数据库中
        payInfoMapper.insertSelective(payInfo);
        return ServerResponse.createBySuccess();
    }

    public ServerResponse queryOrderPayStatus(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByuserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户没有改订单信息");
        }
        //判断支付状态
        if (order.getStatus() >= Const.orderStatusEnum.PAID.getCode()) {
            //说明该订单是付款成功的订单
            return ServerResponse.createBySuccess();
        }
        //付款失败的订单
        return ServerResponse.createByError();
    }

    /**
     * 订单创建
     * @param userId 用户id
     * @param shippingId 收获地址id
     * @return 订单创建结果
     */
    public ServerResponse createOrder(Integer userId, Integer shippingId) {
        //查询购物车中被勾选的订单
        List<Cart> Cartlist = cartMapper.selectCartByUserId(userId);
        //计算订单总价
        ServerResponse serverResponse = this.getCartOrderItem(userId, Cartlist);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        if (CollectionUtils.isEmpty(orderItemList)) {
            return serverResponse.createByErrorMessage("购物车为空，生成订单失败");
        }
        //得到总价
        BigDecimal payTotal = this.getOrderTotalPrice(orderItemList);

        //生成订单类
        Order order = this.assembleOrder(userId, shippingId, payTotal);
        if (order == null) {
            return serverResponse.createByErrorMessage("生成订单错误");
        }
        //设置OrderItem中的订单
        for (OrderItem orderItem : orderItemList) {
            orderItem.setOrderNo(order.getOrderNo());
        }

        //  使用mybatis的批量插入
        orderItemMapper.batchInsert(orderItemList);
        //减少产品的库存
        this.reduceProductStock(orderItemList);
        //清空勾选了的购物车
        this.cleanCart(Cartlist);
        // 返回给前端的数据 ,支付金额，订单号，状态等信息！！！
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    private OrderVo assembleOrderVo(Order order, List<OrderItem> orderItemList) {
        OrderVo orderVo = new OrderVo();
        orderVo.setOrderNo(order.getOrderNo());
        orderVo.setPayment(order.getPayment());
        orderVo.setPaymentType(order.getPaymentType());
        //通过枚举判断的方式在获取值
        orderVo.setPaymentTypeDesc(Const.PaymentTypeEnum.codeOf(order.getPaymentType()).getValue());
        orderVo.setPostage(order.getPostage());
        orderVo.setStatus(order.getStatus());
        orderVo.setStatusDesc(Const.orderStatusEnum.codeOf(order.getStatus()).getValue());
        orderVo.setShippingId(order.getShippingId());

        Shipping shipping = shippingMapper.selectByPrimaryKey(order.getShippingId());
        //组装shippingVo对象
        if (shipping != null) {
            orderVo.setReceiverName(shipping.getReceiverName());
            orderVo.setShippingVo(this.assembleShippingVo(shipping));
        }

        //拼接时间
        orderVo.setPaymentTime(DateTimeUtil.dateToStr(order.getPaymentTime()));
        orderVo.setSendTime(DateTimeUtil.dateToStr(order.getSendTime()));
        orderVo.setEndTime(DateTimeUtil.dateToStr(order.getEndTime()));
        orderVo.setCreateTime(DateTimeUtil.dateToStr(order.getCreateTime()));
        orderVo.setCloseTime(DateTimeUtil.dateToStr(order.getCloseTime()));

        //设置图片地址前缀
        orderVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        for (OrderItem orderItem : orderItemList) {
            OrderItemVo orderItemVo = this.assebbleOrderItemVo(orderItem);
            orderItemVoList.add(orderItemVo);
        }
        //拼接对象
        orderVo.setOrderItemVoList(orderItemVoList);
        //返回
        return orderVo;
    }

    public ServerResponse<String> cancel(Integer userId, Long orderNo) {
        //得到订单号码
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("该用户此订单不存在");
        }
        //如果支付成功，目前不能取消订单----
        if (order.getStatus() != Const.orderStatusEnum.NO_PAY.getCode()) {
            return ServerResponse.createByErrorMessage("该订单已支付，无法取消，可进行退款操作");
        }
        Order newOrder = new Order();
        newOrder.setId(order.getId());
        newOrder.setStatus(Const.orderStatusEnum.CANCELED.getCode());
        int rowCount = orderMapper.updateByPrimaryKeySelective(newOrder);
        if (rowCount > 0) {
            return ServerResponse.createBySuccessMessage("订单取消成功");
        }
        return ServerResponse.createByErrorMessage("订单取消失败");
    }

    /**
     * 得到订单中购物车中的产品信息
     * @param userId 用户ID
     * @return 返回orderProductVo对象，用户展示
     */
    public ServerResponse getOrderCartProduct(Integer userId) {
        OrderProductVo orderProductVo = new OrderProductVo();
        //从购物车中获取数据
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        ServerResponse serverResponse = this.getCartOrderItem(userId, cartList);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        //从serverResponse中获得orderItem集合
        List<OrderItem> orderItemList = (List<OrderItem>) serverResponse.getData();
        List<OrderItemVo> orderItemVoList = Lists.newArrayList();
        BigDecimal payment = new BigDecimal("0");
        //计算总价
        for (OrderItem orderitem : orderItemList) {
            payment = BigDecimalUtil.add(payment.doubleValue(), orderitem.getTotalPrice().doubleValue());
            orderItemVoList.add(assebbleOrderItemVo(orderitem));
        }
        //拼接orderProductVo
        orderProductVo.setOrderItemVoList(orderItemVoList);
        orderProductVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        orderProductVo.setProductTotalPrice(payment);
        return serverResponse.createBySuccess(orderProductVo);
    }

    /**
     *
     * @param userId 用户ID
     * @param orderNo 订单号码
     * @return  通过拼接orderVo对象，将订单详情内容还给前端
     */
    public ServerResponse<OrderVo> getOrderDetail(Integer userId, Long orderNo) {
        Order order = orderMapper.selectByuserIdAndOrderNo(userId, orderNo);
        if (order == null) {
            return ServerResponse.createByErrorMessage("没有找到该订单");
        }
        List<OrderItem> orderItemList = orderItemMapper.getByOrderNoUserId(userId, orderNo);
        OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
        return ServerResponse.createBySuccess(orderVo);
    }

    /**
     * 获得订单列表
     * @param userId 当前用户ID
     * @param pageNum 默认pageNum 1
     * @param pageSize 默认pageSize 10
     * @return pageInfo 对象 前端通过PageHelper进行展示
     */
    public ServerResponse<PageInfo> getOrderList(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        //通过用户id获取到订单
        List<Order> orderList = orderMapper.selectByUserId(userId);
        List<OrderVo> orderVoList = this.assembleOrderVoList(orderList, userId);
        PageInfo page_Result = new PageInfo(orderList);
        page_Result.setList(orderVoList);
        return ServerResponse.createBySuccess(page_Result);
    }

    //后端操作---------------start---------------------------------

    /**
     *
     */
    /**
     * 后端操作，得到所有的list
     * @param pageNum 默认值，1
     * @param pageSize 默认值 10
     * @return  返回PageInfo类，对orderVoList 进行分页处理
     */
    public ServerResponse<PageInfo> manageList(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        //查询所有订单
        List<Order> allOrderlist = orderMapper.selectAllOrder();
        //用户端和后端的操作进行分离
        List<OrderVo> orderVoList = this.assembleOrderVoList(allOrderlist,null);
        //设置分页
        PageInfo pageResult = new PageInfo(allOrderlist);
        pageResult.setList(orderVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 得到订单详情页面
     * @param orderNo 订单号码
     * @return OrderVo对象，用于展示订单详情
     */
    public ServerResponse<OrderVo> manageDetail(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            return ServerResponse.createBySuccess(orderVo);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    /**
     * 通过订单号精准查找
     * @param orderNo 前端传入的，查找的订单号码
     * @param pageNum 默认值1
     * @param pageSize 默认值10
     * @return
     */
    public ServerResponse<PageInfo> manageSearch(Long orderNo,int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            List<OrderItem> orderItemList = orderItemMapper.getByOrderNo(orderNo);
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            PageInfo PageResult = new PageInfo(Lists.newArrayList(order));
            PageResult.setList(Lists.newArrayList(orderVo));
            return ServerResponse.createBySuccess(PageResult);
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }

    /**
     * 发货操作
     * @param orderNo 订单号
     * @return 发货是否成功
     */
    public ServerResponse<String> manageSendGoods(Long orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            if (order.getStatus() == Const.orderStatusEnum.PAID.getCode()) {
                order.setStatus(Const.orderStatusEnum.SHIPPED.getCode());
                order.setSendTime(new Date());
                int resultCount = orderMapper.updateByPrimaryKeySelective(order);
                if (resultCount > 0) {
                    return ServerResponse.createBySuccessMessage("订单发货成功");
                }
                return ServerResponse.createBySuccessMessage("订单发货失败");
            }
        }
        return ServerResponse.createByErrorMessage("订单不存在");
    }


    private List<OrderVo> assembleOrderVoList(List<Order> orderList,Integer userId) {
        List<OrderVo> orderVoList = Lists.newArrayList();
        for (Order order : orderList) {
            List<OrderItem> orderItemList = Lists.newArrayList();
            if (userId == null) {
                //todo 管理员查看订单详情时，不需要userId
                orderItemList = orderItemMapper.getByOrderNo(order.getOrderNo());

            }else{
                orderItemList = orderItemMapper.getByOrderNoUserId(userId, order.getOrderNo());
            }
            //组装orderVo
            OrderVo orderVo = this.assembleOrderVo(order, orderItemList);
            orderVoList.add(orderVo);
        }
        return orderVoList;
    }



    private OrderItemVo assebbleOrderItemVo(OrderItem orderItem) {
        OrderItemVo orderItemVo = new OrderItemVo();
        orderItemVo.setOrderNo(orderItem.getOrderNo());
        orderItemVo.setProductId(orderItem.getProductId());
        orderItemVo.setProductName(orderItem.getProductName());
        orderItemVo.setProductImage(orderItem.getProductImage());
        orderItemVo.setCurrentUnitPrice(orderItem.getCurrentUnitPrice());
        orderItemVo.setQuantity(orderItem.getQuantity());
        orderItemVo.setTotalPrice(orderItem.getTotalPrice());
        orderItemVo.setCreateTime(DateTimeUtil.dateToStr(orderItem.getCreateTime()));

        return orderItemVo;
    }

    private ShippingVo assembleShippingVo(Shipping shipping) {
        ShippingVo shippingVo = new ShippingVo();
        //组装shippingVo对象
        shippingVo.setReceiverAddress(shipping.getReceiverAddress());
        shippingVo.setReceiverCity(shipping.getReceiverCity());
        shippingVo.setReceiverDistrict(shipping.getReceiverDistrict());
        shippingVo.setReceiverMobile(shipping.getReceiverMobile());
        shippingVo.setReceiverName(shipping.getReceiverName());
        shippingVo.setReceiverPhone(shipping.getReceiverPhone());
        shippingVo.setReceiverProvince(shipping.getReceiverProvince());
        shippingVo.setReceiverZip(shipping.getReceiverZip());
        return shippingVo;
    }

    private void cleanCart(List<Cart> cartList) {
        for (Cart cart : cartList) {
            cartMapper.deleteByPrimaryKey(cart.getId());
        }
    }

    private void reduceProductStock(List<OrderItem> orderItemList) {
        for (OrderItem orderItem : orderItemList) {
            Product product = productMapper.selectByPrimaryKey(orderItem.getProductId());
            product.setStock(product.getStock() - orderItem.getQuantity());
            productMapper.updateByPrimaryKeySelective(product);
        }
    }

    private Order assembleOrder(Integer userId, Integer shippingId, BigDecimal payTotal) {
        Order order = new Order();
        long orderNo = this.generatorNo();
        order.setOrderNo(orderNo);
        order.setStatus(Const.orderStatusEnum.NO_PAY.getCode());
        order.setPostage(0);
        //设置支付方式！在线支付
        order.setPaymentType(Const.PaymentTypeEnum.ONLINE_PAY.getCode());
        order.setPayment(payTotal);
        order.setUserId(userId);
        order.setShippingId(shippingId);
        //todo 发货时间和付款时间
        int rowCount = orderMapper.insert(order);
        if (rowCount > 0) {
            return order;
        }
        return null;
    }

    /**
     * 生成订单号！！！
     *
     * @return 订单号
     * 使用时间戳进行取余
     * 这里不易使用时间+数据增长的模式，因为很容易可以看出当前有多少订单
     */
    private long generatorNo() {
        long currentTime = System.currentTimeMillis();
        return currentTime + new Random().nextInt(100);
    }

    //得到订单总价
    private BigDecimal getOrderTotalPrice(List<OrderItem> orderItemList) {
        BigDecimal totalPrice = new BigDecimal("0");
        for (OrderItem orderItem : orderItemList) {
            totalPrice = BigDecimalUtil.add(totalPrice.doubleValue(), orderItem.getTotalPrice().doubleValue());
        }
        return totalPrice;
    }

    /**
     * 得到购物车中的订单详情
     *
     * @param userId
     * @param cartList
     * @return
     */
    private ServerResponse getCartOrderItem(Integer userId, List<Cart> cartList) {
        List<OrderItem> orderItemList = Lists.newArrayList();
        if (CollectionUtils.isEmpty(cartList)) {
            return ServerResponse.createByErrorMessage("购物车为空");
        }
        //检验购物车的数据，包括产品的状态和数量
        for (Cart cartItem : cartList) {
            OrderItem orderItem = new OrderItem();
            Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
            //检验是否出售
            if (Const.ProductStatusEnum.ON_SALE.getCode() != product.getStatus()) {
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "不是出售状态");
            }
            //检验库存
            if (cartItem.getQuantity() > product.getStock()) {
                return ServerResponse.createByErrorMessage("产品" + product.getName() + "库存不足");
            }

            orderItem.setUserId(userId);
            orderItem.setProductName(product.getName());
            orderItem.setProductId(product.getId());
            orderItem.setProductImage(product.getMainImage());
            orderItem.setCurrentUnitPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setTotalPrice(BigDecimalUtil.mul(cartItem.getQuantity(), product.getPrice().doubleValue()));
            orderItemList.add(orderItem);
        }
        return ServerResponse.createBySuccess(orderItemList);

    }

}