package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("feedback")
public class Feedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long appointmentId;
    private Long studentId;
    private Long teacherId;

    /** 1-5 星评分 */
    private Integer rating;

    /** 评价内容（可选） */
    private String content;

    /** 是否匿名 */
    private Integer isAnonymous;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String teacherName;
}
