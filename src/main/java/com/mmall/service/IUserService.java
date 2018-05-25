package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.service.impl.pojo.User;

/**
 * 用户service层
 * 采用接口为以后AOP做准备,也可以使用clglib进行动态代理，但是使用类扩展肯定没有接口方便
 *  service->mybatis->dao
 * @author Liupeng
 * @create 2018-04-27 22:52
 **/
public interface IUserService {
    ServerResponse login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkVaild(String str,String type);

    ServerResponse<String> forgetGetQuestion(String username);

    ServerResponse<String> checkAnswer(String username,String password,String answer);

    ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);

    ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user);

    ServerResponse<User> update_information(User user);

    ServerResponse<User> getinformation(Integer id);

    ServerResponse<String> checkAdminRole(User user);
    }
