package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("quiz_rule")
public class QuizRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long quizId;
    private Integer minScore;
    private Integer maxScore;
    private String conclusion;
}
