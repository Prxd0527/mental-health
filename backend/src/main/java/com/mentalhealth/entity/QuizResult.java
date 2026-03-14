package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quiz_result")
public class QuizResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long quizId;
    private Integer totalScore;
    @TableField("result_desc")
    private String suggestion;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String quizTitle; // 界面显示关联测验名
}
