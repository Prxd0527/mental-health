package com.mentalhealth.controller;

import com.mentalhealth.common.Result;
import com.mentalhealth.entity.User;
import com.mentalhealth.service.UserService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid AuthRequest request) {
        User user = userService.register(request.getUsername(), request.getPassword());
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody @Valid AuthRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    @Data
    public static class AuthRequest {
        @NotBlank(message = "账号不能为空")
        private String username;
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    @GetMapping("/teachers")
    public Result<java.util.List<User>> getTeachers() {
        return Result.success(userService.getTeacherList());
    }
}
