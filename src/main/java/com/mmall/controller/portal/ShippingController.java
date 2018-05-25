package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.service.impl.ShippingServiceImpl;
import com.mmall.service.impl.pojo.Shipping;
import com.mmall.service.impl.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 用户地址的控制类
 *
 * @author Liupeng
 * @createTime 2018-05-14 22:20
 **/
@Controller
@RequestMapping("/shipping/") //窄化请求映射
public class ShippingController {
    @Autowired
    private ShippingServiceImpl iShippingService;

    /**
     * 当前用户新增地址
     * @param session 用户session
     * @param shipping 用户输入的地址！！！ shipping用于数据绑定
     * @return 添加地址是否成功
     */
    @RequestMapping(value = "add.do")
    @ResponseBody
    //使用springmvc的数据绑定功能，使得传入参数时，不需要过于复杂
    public ServerResponse add(HttpSession session, Shipping shipping){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.add(user.getId(),shipping);
    }

    /**
     * 删除操作
     * @param session 用户session
     * @param shippingId 用户地址在数据表中的id
     * @return 是否删除成功
     */
    @RequestMapping("del.do")
    @ResponseBody
    //使用springmvc的数据绑定功能，使得传入参数时，不需要过于复杂
    public ServerResponse delete(HttpSession session, Integer shippingId ){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.del(user.getId(),shippingId);
    }

    /**
     *  更新操作
     * @param session 用户session
     * @param shipping shipping数据绑定，用于更新操作
     * @return
     */
    @RequestMapping("update.do")
    @ResponseBody
    //使用springmvc的数据绑定功能，使得传入参数时，不需要过于复杂  更新方法
    public ServerResponse update(HttpSession session, Shipping shipping){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.update(user.getId(),shipping);
    }

    /**
     * 查询一个地址方法
     * @param session  用户session
     * @param shippingId 该地址的id
     * @return shipping集合
     */
    @RequestMapping("select.do")
    @ResponseBody
    //查询方法
    public ServerResponse select(HttpSession session, Integer shippingId ){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iShippingService.select(user.getId(),shippingId);
    }

    /**
     * 查询用户所有的地址
     * @param pageNum 分页数量
     * @param pageSize 每页数量
     * @param session 用户session
     * @return 当前用户所有地址集合
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "pageNum",defaultValue = "1") Integer pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10")Integer pageSize,
                                         HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return  iShippingService.list(user.getId(),pageNum,pageSize);
    }

}