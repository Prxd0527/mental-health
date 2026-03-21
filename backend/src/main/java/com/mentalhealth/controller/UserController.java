package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.User;
import com.mentalhealth.service.UserService;
import com.mentalhealth.utils.SecurityUtils;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Resource
    private UserService userService;

    // ====================== 认证相关（公开接口） ======================

    @PostMapping("/auth/register")
    public Result<User> register(@RequestBody @Valid AuthRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword());
        return Result.success(user);
    }

    @PostMapping("/auth/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid AuthRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());

        // 登录成功后同时返回用户基础信息，前端无需二次请求
        User user = userService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername()));
        user.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    @GetMapping("/auth/teachers")
    public Result<List<User>> getTeachers() {
        return Result.success(userService.getTeacherList());
    }

    @Data
    public static class AuthRequest {
        @NotBlank(message = "账号不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    // ====================== 个人中心（需登录） ======================

    /**
     * 获取当前登录用户的个人信息
     */
    @GetMapping("/user/profile")
    public Result<User> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        User profile = userService.getUserProfile(userId);
        return Result.success(profile);
    }

    /**
     * 修改密码
     */
    @PutMapping("/user/password")
    public Result<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success("密码修改成功");
    }

    /**
     * 修改个人资料（头像、姓名、简介等）
     */
    @PutMapping("/user/profile")
    public Result<String> updateProfile(@RequestBody User profileData) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null)
            return Result.error(401, "尚未登录");

        userService.updateProfile(userId, profileData);
        return Result.success("资料更新成功");
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "旧密码不能为空")
        private String oldPassword;
        @NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
