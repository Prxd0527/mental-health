package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId;
    private Long receiverId;

    private String content;
    // TEXT, IMAGE 等类型支持扩展
    private String type;

    // 0-未读, 1-已读
    private Integer isRead;

    private LocalDateTime createTime;
}
