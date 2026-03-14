package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String realName;
    private String avatar;
    private String role;
    private Integer gender; // 0-女, 1-男, 2-未知
    private String intro;
    private String expertise;
    private Integer status; // 0-禁用, 1-正常

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
