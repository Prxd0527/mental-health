package com.mentalhealth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.User;
import com.mentalhealth.mapper.UserMapper;
import com.mentalhealth.service.UserService;
import com.mentalhealth.utils.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(String username, String password) {
        if (StrUtil.hasBlank(username, password)) {
            throw new RuntimeException("用户名和密码不能为空");
        }

        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            throw new RuntimeException("学号/邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT");
        user.setStatus(1);

        baseMapper.insert(user);

        user.setPassword(null);
        return user;
    }

    @Override
    public String login(String username, String password) {
        if (StrUtil.hasBlank(username, password)) {
            throw new RuntimeException("用户名和密码不能为空");
        }

        User user = baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return jwtUtils.generateToken(user.getUsername(), user.getId(), user.getRole());
    }

    @Override
    public List<User> getTeacherList() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "TEACHER")
                .eq(User::getStatus, 1);
        List<User> teachers = baseMapper.selectList(wrapper);
        for (User t : teachers) {
            t.setPassword(null);
        }
        return teachers;
    }

    @Override
    public User getUserProfile(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (StrUtil.hasBlank(oldPassword, newPassword)) {
            throw new RuntimeException("旧密码和新密码不能为空");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("新密码长度不能少于6位");
        }

        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        baseMapper.updateById(user);
    }

    @Override
    public void updateProfile(Long userId, User profileData) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 只允许更新部分字段（防止前端传入恶意字段如 role/status 被覆盖）
        if (StrUtil.isNotBlank(profileData.getRealName())) {
            user.setRealName(profileData.getRealName());
        }
        if (StrUtil.isNotBlank(profileData.getAvatar())) {
            user.setAvatar(profileData.getAvatar());
        }
        if (profileData.getGender() != null) {
            user.setGender(profileData.getGender());
        }
        if (StrUtil.isNotBlank(profileData.getIntro())) {
            user.setIntro(profileData.getIntro());
        }
        if (StrUtil.isNotBlank(profileData.getEmail())) {
            user.setEmail(profileData.getEmail());
        }

        baseMapper.updateById(user);
    }
}
