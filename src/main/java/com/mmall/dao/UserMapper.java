package com.mmall.dao;

import com.mmall.service.impl.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    //检验用户名是否存在
    int CheckUsername(String username);

    //检验邮箱是否存在
    int CheckEmail(String email);

    //Mybatis在传入多个参数时，需要增加注解@Param,然后再xml文件中就使用value中的值//返回当前用户信息
    User selectLogin(@Param("username") String username, @Param("password") String password);

    //通过username获取到忘记密码的重置密码问题
    String selectQuestionByUsername(String username);

    //检验当前用户名下的忘记密码重置问题和问题答案是否正确
    int checkAnswer(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);

    //在登陆状态下，修改密码
    int updatePasswordByUsername(@Param("username") String username,@Param("passwordNew")String passwordNew);

    int checkPassword(@Param("password")String password,@Param("userid")Integer userid);

    //将邮箱和用户id进行绑定查询，使得数据库中始终只有一个唯一的email
    int CheckEmailByUserId(@Param("email")String email,@Param("userid")Integer userid);
}