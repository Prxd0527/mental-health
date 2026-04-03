package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article")
public class Article {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String content;
    private String coverImage;
    private String category;
    private Integer views;
    private Integer status; // 0-待审核/草稿, 1-已发布

    private Long authorId; // 投稿人ID（咨询师或管理员）

    private LocalDateTime createTime;
}
