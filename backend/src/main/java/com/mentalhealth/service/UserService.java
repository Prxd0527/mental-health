package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.User;

import java.util.List;

public interface UserService extends IService<User> {

    /**
     * 用户注册
     */
    User register(String username, String password);

    /**
     * 用户登录返回Token
     */
    String login(String username, String password);

    /**
     * 获取咨询师列表
     */
    List<User> getTeacherList();

    /**
     * 获取当前用户个人信息（脱敏）
     */
    User getUserProfile(Long userId);

    /**
     * 修改密码
     *
     * @param userId      当前用户 ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 更新个人资料（头像、真实姓名、简介等）
     */
    void updateProfile(Long userId, User profileData);
}
