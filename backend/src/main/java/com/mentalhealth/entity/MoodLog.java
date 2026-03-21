package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("mood_log")
public class MoodLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 心情标识: HAPPY / GOOD / NEUTRAL / SAD / TERRIBLE */
    private String mood;

    /** 对应 Emoji */
    private String emoji;

    /** 心情备注 */
    private String note;

    /** 打卡日期 */
    private LocalDate logDate;

    private LocalDateTime createTime;
}
