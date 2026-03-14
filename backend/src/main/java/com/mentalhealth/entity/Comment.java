package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;
    private Long userId;
    private Long parentId;
    private String content;
    private Integer isAnonymous;
    private Integer likes;
    private Integer status; // 0-屏蔽, 1-正常

    private LocalDateTime createTime;

    // 冗余展示字段
    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String authorAvatar;

    // 子评论（树形展示时使用，非数据库字段）
    @TableField(exist = false)
    private java.util.List<Comment> children;

    // 回复目标的用户名（"@xxx" 效果）
    @TableField(exist = false)
    private String replyToName;
}
