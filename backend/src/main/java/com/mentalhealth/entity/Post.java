package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String title;
    private String content;
    private String images;
    private String tags;
    private Integer isAnonymous; // 0-实名, 1-匿名
    private Integer likes;
    private Integer views;
    private Integer status; // 0-屏蔽, 1-正常

    private LocalDateTime createTime;

    // 冗余字端，用于返回前端展示，不插入数据库
    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String authorAvatar;
}
