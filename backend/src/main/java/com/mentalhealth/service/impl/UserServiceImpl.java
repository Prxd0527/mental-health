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

        // 检查用户名是否已存在
        Long count = baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count > 0) {
            throw new RuntimeException("学号/邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        // BCrypt加密存储
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("STUDENT"); // 默认注册为学生
        user.setStatus(1); // 默认正常状态

        baseMapper.insert(user);

        // 脱敏返回
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
        // 脱敏返回密码
        for (User t : teachers) {
            t.setPassword(null);
        }
        return teachers;
    }
}
