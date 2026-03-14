package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long quizId;
    private String content;
    private String options; // 格式 JSON, 如: [{"label":"A","text":"经常","score":3}, ...]

    // 从1开始排序
    private Integer sortOrder;
}
