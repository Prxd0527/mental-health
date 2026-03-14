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
    private Integer status; // 0-隐藏/草稿, 1-发布

    private LocalDateTime createTime;
}
