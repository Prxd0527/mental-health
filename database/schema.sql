-- 创建数据库
CREATE DATABASE IF NOT EXISTS mental_health DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mental_health;

-- 1. 用户表 (学生与教师/管理员共用，通过 role 区分)
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '学号/邮箱(注册登录用)',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `role` VARCHAR(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色: STUDENT-学生, TEACHER-咨询师, ADMIN-管理员',
  `gender` TINYINT(1) DEFAULT NULL COMMENT '性别: 0-女, 1-男, 2-未知',
  `intro` TEXT DEFAULT NULL COMMENT '简介(咨询师展示用)',
  `expertise` VARCHAR(255) DEFAULT NULL COMMENT '擅长领域(咨询师展示用, 逗号分隔)',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 树洞文章表 (Post)
CREATE TABLE `post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '发布者ID (关联user表)',
  `content` TEXT NOT NULL COMMENT '文本内容',
  `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL列表 (JSON格式或逗号分隔)',
  `tags` VARCHAR(255) DEFAULT NULL COMMENT '情感/话题标签 (逗号分隔)',
  `is_anonymous` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否匿名 (前台展示依据此字段)',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `views` INT NOT NULL DEFAULT 0 COMMENT '浏览量/热度',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-已删除/屏蔽, 1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='树洞文章表';

-- 3. 评论表 (Comment)
CREATE TABLE `comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT NOT NULL COMMENT '关联的树洞ID',
  `user_id` BIGINT NOT NULL COMMENT '评论者ID',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论ID (用于子评论/回复, NULL为顶级评论)',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `is_anonymous` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否匿名',
  `likes` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-已删除/屏蔽, 1-正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
  PRIMARY KEY (`id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 4. 测评量表 (Quiz)
CREATE TABLE `quiz` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(255) NOT NULL COMMENT '量表名称 (如SCL-90)',
  `description` TEXT DEFAULT NULL COMMENT '量表介绍',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '状态: 0-下线, 1-上线',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评量表表';

-- 5. 量表题目表 (Question)
CREATE TABLE `question` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quiz_id` BIGINT NOT NULL COMMENT '归属量表ID',
  `type` VARCHAR(20) NOT NULL DEFAULT 'SINGLE' COMMENT '类型: SINGLE-单选, MULTIPLE-多选',
  `content` TEXT NOT NULL COMMENT '题目题干',
  `options` JSON NOT NULL COMMENT '选项列表 (JSON数组, 包含选项内容和分值)',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  PRIMARY KEY (`id`),
  KEY `idx_quiz_id` (`quiz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='量表题目表';

-- 6. 测评结果/报告表 (QuizResult)
CREATE TABLE `quiz_result` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `quiz_id` BIGINT NOT NULL COMMENT '量表ID',
  `user_id` BIGINT NOT NULL COMMENT '作答用户ID',
  `total_score` INT NOT NULL COMMENT '计算总分 (业务逻辑计算出)',
  `result_desc` TEXT COMMENT '测评结论/建议 (基于总分匹配规则段)',
  `answers` JSON DEFAULT NULL COMMENT '用户原始答题记录',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评结果表';

-- 7. 评分规则配置表 (QuizRule) - 针对总分区间
CREATE TABLE `quiz_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `quiz_id` BIGINT NOT NULL COMMENT '关联量表ID',
  `min_score` INT NOT NULL COMMENT '最小分 (包含)',
  `max_score` INT NOT NULL COMMENT '最大分 (包含)',
  `conclusion` TEXT NOT NULL COMMENT '该分数段对应的结论与建议',
  PRIMARY KEY (`id`),
  KEY `idx_quiz_id` (`quiz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测评评分规则表';

-- 8. 科普资讯表 (Article)
CREATE TABLE `article` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL COMMENT '文章标题',
  `content` LONGTEXT NOT NULL COMMENT '富文本内容',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
  `category` VARCHAR(50) DEFAULT NULL COMMENT '分类(文章/公告等)',
  `views` INT NOT NULL DEFAULT 0 COMMENT '阅读量',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0-草稿/隐藏, 1-发布',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科普资讯文章表';

-- 9. 预约记录表 (Appointment)
CREATE TABLE `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` BIGINT NOT NULL COMMENT '预约学生ID',
  `teacher_id` BIGINT NOT NULL COMMENT '预约咨询师ID',
  `appoint_date` DATE NOT NULL COMMENT '预约日期',
  `time_slot` VARCHAR(50) NOT NULL COMMENT '时间段 (如: 09:00-10:00)',
  `requirement` TEXT COMMENT '学生诉求/病情简述',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待审核, APPROVED-已同意, REJECTED-已拒绝, COMPLETED-已完成, CANCELED-已取消',
  `feedback` TEXT DEFAULT NULL COMMENT '咨询师审批反馈/拒绝理由',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询预约记录表';

-- 10. 聊天消息表 (ChatMessage) - 固化聊天记录备查
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `type` VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型: TEXT, IMAGE等',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息记录表';

-- 11. 举报信息表 (Report) - 树洞系统风控
CREATE TABLE `report` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `reporter_id` BIGINT NOT NULL COMMENT '举报人ID',
  `target_type` VARCHAR(20) NOT NULL COMMENT '举报目标类型: POST-树洞, COMMENT-评论',
  `target_id` BIGINT NOT NULL COMMENT '目标ID',
  `reason` VARCHAR(255) NOT NULL COMMENT '举报原因',
  `status` VARCHAR(20) NOT NULL DEFAULT 'UNPROCESSED' COMMENT '处理状态: UNPROCESSED-未处理, PROCESSED-已处理(屏蔽), REJECTED-已驳回(正常)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报与审核表';

-- 12. 教师排班表 (TeacherSchedule) - 咨询师可用时间段管理
CREATE TABLE `teacher_schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `teacher_id` BIGINT NOT NULL COMMENT '教师/咨询师ID',
  `work_date` DATE NOT NULL COMMENT '开放预约的日期',
  `available_slots` VARCHAR(500) DEFAULT NULL COMMENT '可用时间段 (逗号分隔, 如: 09:00-10:00,10:30-11:30)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_date` (`teacher_id`, `work_date`),
  KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师排班表';

-- 13. 咨询评价表 (Feedback) - 咨询后评价与反馈
CREATE TABLE `feedback` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `appointment_id` BIGINT NOT NULL COMMENT '关联预约ID',
  `student_id` BIGINT NOT NULL COMMENT '评价学生ID',
  `teacher_id` BIGINT NOT NULL COMMENT '被评价咨询师ID',
  `rating` TINYINT NOT NULL COMMENT '评分 1-5 星',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容（可选）',
  `is_anonymous` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否匿名',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_appointment_id` (`appointment_id`),
  KEY `idx_teacher_id` (`teacher_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询评价表';

-- 14. 心情打卡表 (MoodLog) - 每日 Emoji 日记
CREATE TABLE `mood_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `mood` VARCHAR(20) NOT NULL COMMENT '心情标识: HAPPY/GOOD/NEUTRAL/SAD/TERRIBLE',
  `emoji` VARCHAR(10) NOT NULL COMMENT '对应 Emoji 符号',
  `note` VARCHAR(300) DEFAULT NULL COMMENT '心情备注（可选）',
  `log_date` DATE NOT NULL COMMENT '打卡日期',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `log_date`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心情打卡日记表';
