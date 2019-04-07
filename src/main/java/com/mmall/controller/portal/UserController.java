package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.service.impl.UserServiceImpl;
import com.mmall.service.impl.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * 前台，用户控制器
 *
 * @author Liupeng
 * @create 2018-04-27 22:03
 **/

@RestController
@RequestMapping(value = "/user/")
public class UserController {

    @Autowired
    private UserServiceImpl iUserService;


    /**
     * 创建登陆方法 使该方法映射login.do，并设置为只能接收post请求
     * @param username 前端传来的用户名
     * @param password 前端传来的密码
     * @param session 当前用户的session
     * @return 如果登陆成功返回用户，否则提示错误
     */
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
     //xml文件配置方式：dispatcher-servlet.xml文件中的supportedMediaTypes属性
    public ServerResponse<User> login(String username, String password, HttpSession session){
        //在controller层调用service层函数 server-mybatis-dao
        ServerResponse<User> response = iUserService.login(username, password);
        //当登陆成功后，将用户放在session中
        if(response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        return response;
    }

    /**
     * 登出功能实现
     * @param session 当前的session
     * @return  当前状态
     */
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);
        return  ServerResponse.createBySuccess("登出成功");
    }

    /**
     * 注册功能实现
     * @param user 前端收到的信息进行了封装
     * @return 注册成功或者失败
     */
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    /**
     * 检验当前用户名或者邮箱是否在db中唯一
     * @param str 用户名或者邮箱
     * @param type str的具体类型 通过静态常量实现
     * @return string类型，用户名或者邮箱存在或者不存在
     */
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkVaild(String str,String type){
        return iUserService.checkVaild(str,type);
    }

    /**
     * 得到当前用户的信息
     * @param session 当前session
     * @return  返回当前User对象
     */
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null){
            return ServerResponse.createBySuccess(user);
        }
        //从当前session中未获得用户值，所以是未登录的！！！
        return ServerResponse.createByErrorMessage("用户未登陆，无法获取当前用户的信息");
    }

    /**
     * 获得用户忘记密码后的密码提示问题
     * @param username 用户名
     * @return 当前用户名的密码提示问题
     */
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
       return iUserService.forgetGetQuestion(username);
    }

    /**
     * 使用本地缓存,检查当前用户输入的忘记密码提示问题的答案是否正确
     * tokenCache 用于本地缓存，增加修改密码的凭证，避免知道了答案和密码，使其他人随意更改
     * @param username  用户名
     * @param question 密码提示问题
     * @param answer  密码提示答案
     * @return  是否修改成功
     */
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String>  forgetCheckAnswer(String username,String question,String answer){
            return iUserService.checkAnswer(username,question,answer);
    }

    /**
     * 验证密码提示问题后进行重置密码操作
     * @param username 当前重置密码的用户名
     * @param passwordNew  新密码
     * @param forgetToken 验证密码问题时产生的token
     * @return  重置密码成功或者异常
     */
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
            return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    /**
     * 登陆状态下的重置密码
     * @param session 当前session 用于获取当前用户
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @return 是否修改成功
     */
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登陆");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    /**
     * 修改用户的信息
     * @param session  当前session
     * @param user  从前端传来的修改好的信息
     * @return 修改成功后，将user传到前端进行展示
     */
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(HttpSession session,User user){
        User current_user = (User) session.getAttribute(Const.CURRENT_USER);
        if(current_user == null){
            return ServerResponse.createByErrorMessage("用户未登陆");
        }
        //防止越权问题，这里将从前端传过来的id username，重新设置为session中的id username
        user.setId(current_user.getId());
        user.setUsername(current_user.getUsername());

        ServerResponse<User> response = iUserService.update_information(user);
        if(response.isSuccess()){
            //更新session中的user
            session.setAttribute(Const.CURRENT_USER,response.getData());
        }
        //无论成功与否，都返回当前response。因为，里面存有是否成功的消息
        return response;
    }

    /**
     * 获取当前用户的详细信息
     * @param session 当前session
     * @return user详情，用于传到前端进行展示
     */
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getinformation(HttpSession session){
        User current_user = (User)session.getAttribute(Const.CURRENT_USER);
        if(current_user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登陆，需要强制登陆，status=10");
        }
        return iUserService.getinformation(current_user.getId());
    }

}