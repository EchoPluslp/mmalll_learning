package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.service.impl.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * 后台管理员登陆
 *
 * @author Liupeng
 * @create 2018-04-30 16:13
 **/
@Controller
@RequestMapping("/manage/user")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "logon.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session){
        ServerResponse<User> response = iUserService.login(username,password);
        if(response.isSuccess()){
            //当前用户登陆成功
            User user = (User) session.getAttribute(Const.CURRENT_USER);
            if(user.getRole() == Const.Role.ROLE_ADMIN) {
                //说明登陆的是管理员
                session.setAttribute(Const.CURRENT_USER, user);
                return response;
            }else {
                //说明登陆的是普通用户
                return ServerResponse.createByErrorMessage("不是管理员，无法登陆");
            }
        }
        return response;//登陆失败，返回相应的信息
    }
}