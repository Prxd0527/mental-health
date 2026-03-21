package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("quiz")
public class Quiz {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String description;
    private Integer status; // 0-下线, 1-在线

    private LocalDateTime createTime;

    /** 题目数量（非数据库字段，由服务层填充） */
    @TableField(exist = false)
    private Integer questionCount;
}
