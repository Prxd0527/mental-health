package com.mentalhealth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
@TableName("report")
public class Report implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long reporterId;
    private String targetType; // 'POST', 'COMMENT'
    private Long targetId;
    private String reason;
    private String status; // 'UNPROCESSED', 'PROCESSED', 'REJECTED'

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}
