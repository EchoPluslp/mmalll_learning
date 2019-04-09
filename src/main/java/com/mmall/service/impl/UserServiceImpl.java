package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCahce;
import com.mmall.dao.UserMapper;
import com.mmall.service.IUserService;
import com.mmall.service.impl.pojo.User;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

/**
 * 用户接口的实现类
 *
 * @author Liupeng
 * @create 2018-04-27 22:51
 **/
@Service("iUserService")//添加注解使之称为service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        //检查登陆的用户名存不存在
        int resultCount = userMapper.CheckUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("当前用户不存在");
        }
        // md5密码登陆 应该是判断md5加密后的值
        String md5PassWord = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5PassWord);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //到达说明，账号和密码都正确 ，将密码置null
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    /**
     * 注册功能实现
     *
     * @param user 当前填写的信息封装User类
     * @return String 注册成功或者失败
     */
    @Override
    public ServerResponse<String> register(User user) {
        //校验用户名和邮箱是否存在-----------start--------------
        ServerResponse vaildResponse = checkVaild(user.getUsername(), Const.USERNAME);
        if (!vaildResponse.isSuccess()) {
            return vaildResponse;
        }
        vaildResponse = checkVaild(user.getEmail(), Const.EMAIL);
        if (!vaildResponse.isSuccess()) {
            return vaildResponse;
        }
//----------------------end-------------
        checkVaild(user.getEmail(), Const.EMAIL);

//        设置当前用户的权限
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5 加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        //插入用户到db中---start-------------
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            ServerResponse.createByErrorMessage("注册失败");
        }
        //----------------------end-------------

        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 检验输入的用户名或者email是否存在
     *
     * @param str  要检验的值
     * @param type 值的类型
     * @return 是否存在
     */
    @Override
    public ServerResponse<String> checkVaild(String str, String type) {
        if (StringUtils.isNotBlank(type)) {//判断type是否为空
            if (Objects.equals(type, Const.USERNAME)) {
                int resultCount = userMapper.CheckUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Objects.equals(type, Const.EMAIL)) {
                int resultCount = userMapper.CheckEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("EMAIL已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        //说明当前用户不存在
        return ServerResponse.createBySuccessMessage("检验成功");
    }

    /**
     * 忘记密码时，根据用户名获取到用户的密码提示问题
     *
     * @param username 当前用户名
     * @return 根据当前用户名获取密码提示问题
     */
    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        //检验当前用户名是否存在
        ServerResponse servceCount = checkVaild(username, Const.USERNAME);
        if (servceCount.isSuccess()) {
            return ServerResponse.createByErrorMessage("当前用户名不存在");
        }
        //当前用户名存在，检验问题是否为空
        String question = userMapper.selectQuestionByUsername(username);
        if (question != null) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("当前用户名找回密码的问题是空的");
    }

    /**
     * 判断忘记密码的问题和答案是否正确，如果正确，生成用户Token
     * 根据参数在数据库中进行查找，如果找到了，则说明正确，如果没有则说明问题答案错误
     *
     * @param username 提示问题的用户名
     * @param question 密码提示问题
     * @param answer   密码提示答案
     * @return 问题的答案是否正确
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明当前当前用户的问题及答案都正确。
            String forgetToken = UUID.randomUUID().toString();//生成本地tokenCache

            TokenCahce.setKey(TokenCahce.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccessMessage(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    /**
     * 根据用户名重新设置密码
     *
     * @param username    用户名
     * @param passwordNew 新密码
     * @param forgetToken 回答问题的token
     * @return 未登录状态下的重置密码是否成功
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        //判断token参数
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        //判断用户名是否存在
        ServerResponse response = checkVaild(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createByErrorMessage("当前用户不存在");
        }
        //判断本地缓存中的token是否存在
        String token = TokenCahce.getKey(TokenCahce.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }

        //此时，判断当前token和缓存中的token是否相同
        if (StringUtils.equals(token, forgetToken)) {
            //对新密码进行md5加密
            String MD5PasswordNew = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, MD5PasswordNew);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("重置密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("重置密码错误");
    }

    /**
     * 在登陆状态下，修改密码，注意防止横向越权的问题
     *
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @param user        当前用户
     * @return 登陆状态下修改密码是否成功
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //为了防止横向越权，我们需要知道当前旧密码是否与当前用户匹配，
        //因为我们会查询一个count（1） ，如果不指定id，那么很容易会返回true。
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        //这里最好选择updateByPrimaryKeySelective方法根据主键，并选择性更新
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 修改个人用户信息
     *
     * @param user 当前用户信息
     * @return user 修改后的用户封装
     */
    @Override
    public ServerResponse<User> update_information(User user) {
        //对email 进行检验: 当更新了email后，不能和数据库中其他人的email重合
        int resultCount = userMapper.CheckEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已存在，请更换email再次尝试");
        }

        //目前可修改这几个参数，其余的可随意添加
        User userNew = new User();
        userNew.setId(user.getId());
        userNew.setEmail(user.getEmail());
        userNew.setPhone(user.getPhone());
        userNew.setQuestion(user.getQuestion());
        userNew.setAnswer(user.getAnswer());

        //使用新的uesr更新数据库中的信息
        resultCount = userMapper.updateByPrimaryKeySelective(userNew);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功！！！", userNew);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    /**
     * 获取当前用户的详细信息
     *
     * @param id 通过id查找用户详情
     * @return 当前用户
     */
    @Override
    public ServerResponse<User> getinformation(Integer id) {
        //根据id查询当前用户是否存在
        User user = userMapper.selectByPrimaryKey(id);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        //将密码置为空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse<String> checkAdminRole(User user) {
        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}