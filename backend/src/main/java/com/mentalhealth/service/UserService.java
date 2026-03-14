package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param username 学号/邮箱
     * @param password 密码
     * @return 注册成功的用户内容(脱敏)
     */
    User register(String username, String password);

    /**
     * 用户登录返回Token
     *
     * @param username 学号/邮箱
     * @param password 密码
     * @return JWT Token
     */
    String login(String username, String password);

    /**
     * 获取咨询师列表
     *
     * @return 角色为 TEACHER 的用户列表
     */
    List<User> getTeacherList();
}
