package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.service.impl.pojo.User;
import com.mmall.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 后端的订单管理
 *
 * @author Liupeng
 * @createTime 2018-05-20 10:39
 **/
@Controller
@RequestMapping("/manage/order")
public class OrderManageController {
    @Autowired
    private IUserService iUserService;

    @Autowired
    private IOrderService iOrderService;


    /**
     * 展示当前所有订单
     * @param session 当前session 判断用户权限
     * @param pageNum pageNUm默认值
     * @param pageSize  pageSize默认值
     * @return  返回PageInfo 对象，交给前端进行展示
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> orderList(HttpSession session,
                                              @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10")int pageSize) {

        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }

        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageList(pageNum, pageSize);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

    /**
     * 得到订单详情页面
     * @param session 当前session 判断用户权限
     * @param orderNo 指定订单号码
     * @return orderVo对象
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<OrderVo>  orderDetail(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }

        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageDetail(orderNo);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

    /**
     * 通过订单号精准查找
     * @param session 当前session
     * @param orderNo  要查找的订单号码
     * @param pageNum 默认值为1
     * @param pageSize 默认值为10
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo>  orderSearch(HttpSession session, Long orderNo,
                                                @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            return iOrderService.manageSearch(orderNo,pageNum,pageSize);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

    /**
     * 发送货物
     * @param session 当前session， 用户判断是否是管理员
     * @param orderNo 需要发送货物的订单号
     * @return String-是否发送成功
     */
    @RequestMapping("send_goods.do")
    @ResponseBody
    public ServerResponse<String> orderSendGoods(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //是，执行发货操作
            return iOrderService.manageSendGoods(orderNo);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

}