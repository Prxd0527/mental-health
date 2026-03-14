# 大学生心理健康咨询与“树洞”倾诉系统

## 项目概括
本项目旨在开发一个基于 SpringBoot 和 Vue 的前后端分离系统，为大学生提供一个安全、私密的情绪疏导平台，结合专业的心理咨询预约服务，实现危机干预与日常心理健康的双重保障。核心目标是构建“匿名倾诉（树洞） + 心理科普 + 测评 + 实时咨询预约”的一站式服务闭环。

## 技术选型
- **主要编程语言**: Java 8/17/21 (后端), JavaScript/TypeScript (前端)
- **关键库/框架**: SpringBoot (后端基础), Vue.js (前端视图), MyBatis-Plus (数据库持久层)
- **核心组件**: WebSocket (即时通讯), Hutool-SensitiveUtil (DFA敏感词过滤算法), Spring Security + JWT (安全认证)
- **数据存储**: MySQL (业务数据), Redis (建议引入，用于WebSocket会话状态、频繁访问的科普文章缓存等)
- **版本控制**: Git
- **其他工具**: Maven/Gradle (构建)

## 项目结构 / 模块划分
- `backend/`: Java SpringBoot 后端核心代码
  - `controller/`: API 接口控制层
  - `service/`: 业务逻辑模块 (含预约、树洞、测评等)
  - `mapper/`: 数据库交互模块
  - `websocket/`: 实时通讯处理模块
  - `utils/`: 工具类 (含DFA过滤器等)
- `frontend-student/`: 学生端 Vue 前端代码 (建议做移动端适配)
- `frontend-admin/`: 咨询师及系统管理员 Vue 前端后台 (建议PC端适配)
- `database/`: 数据库脚本及数据模型定义 (DDL/DML)
- `docs/`: 开发文档、接口说明与需求文档

## 核心功能 / 模块详解
- **前端系统（学生/用户端）**
  - **科普与搜索**: 心理健康知识文章展示、中心公告、关键字检索。
  - **“树洞”倾诉模块**: 文本/图文心情发布、防窥隐私设计（前台完全去标识化）、情感标签匹配、树洞广场（按时间或热度排列）、点赞与匿名评论交互。
  - **预约咨询模块**: 咨询师名片库、分时段预约日历表、预约诉求提交、预约状态跟踪（待审核/已同意/已拒绝等）。
  - **心理测评模块**: 内置常用量表展示（如SCL-90等）、动态顺滑答题流程、多维度及总分自动计算（目前需求只看粗分/总分）、测评结果与防抑郁干预建议生成。
  - **个人中心**: 自助注册与登录（学号/邮箱）、修改密码、树洞和评论沉淀、历史测评报告查阅、预约记录查询。
- **咨询师后台（教师端）**
  - **日程与排班管理**: 可视化工作日历、未来空闲时间段配置。
  - **预约审批管理**: 学生工单受理、同意/拒绝（附带反馈理由）。
  - **在线咨询工作台**: Web端即时通讯窗口（WebSocket支持）、历史文字谈话记录溯源。
- **总后台管理系统（管理员端）**
  - **用户与权限管理**: 学生及咨询师台账维护、Excel批量导录、账号封解与密码重置。
  - **内容审核与风控**: DFA前置过滤追踪、用户举报复审单处理、不当言论/违规树洞强制下架处理。
  - **题库与量表管理**: 单复选题目配置、量表题与选项的分值映射、多元结果区间设定。

## 数据模型
[待梳理需求完毕后详细设计，主要涵盖 User, Post(树洞), Comment, Appointment, Quiz, Question, QuizResult, ChatMessage等实体。]

## 技术实现细节
*(本部分初始为空。在后续开发每一个模块/功能时，AI 会自动将该模块/功能的技术实现方案、关键代码片段说明等填充至此。)*

## 开发状态跟踪
| 模块/功能 | 状态 | 负责人 | 计划完成日期 | 实际完成日期 | 备注 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **0. 需求确认与数据库设计** | 已完成 | sylvia | 待定 | 2026-03-15 | 需求已确认，数据库设计完毕 |
| **1. 基础服务 (登录验证、权限控制)** | 已完成 | sylvia | 待定 | 2026-03-15 | 安全拦截配置落地 |
| **2. 共用设施 (图片上传、DFA敏感词引擎)**| 已完成 | sylvia | 待定 | 2026-03-15 | 引入Hutool DFA，实现基础上传 |
| **3. 学生端 - 心理科普内容展示** | 已完成 | sylvia | 待定 | 2026-03-15 | 资讯列表与检索接口就绪 |
| **4. 学生端 - 树洞系统与点赞评论** | 已完成 | sylvia | 待定 | 2026-03-15 | 树洞发布/评论与DFA处理落地 |
| **5. 学生端 - 咨询师展示与预约日历** | 已完成 | sylvia | 待定 | 2026-03-15 | 咨询师查询与双端双向操作完成 |
| **6. 学生端 - 心理测评体系构建** | 已完成 | sylvia | 待定 | 2026-03-15 | 问卷获取、答题得分计算接口上线 |
| **7. 教师端 - 排班与工单审批系统** | 已完成 | sylvia | 待定 | 2026-03-15 | 教师自定义排期与审批集成 |
| **8. WebSocket 在线私聊服务中心** | 已完成 | sylvia | 待定 | 2026-03-15 | 即时通讯信道及消息落库完成 |
| **9. 管理端 - 系统账号与题库维护** | 已完成 | sylvia | 待定 | 2026-03-15 | 超管控制台 API 已接通 |

## 代码检查与问题记录

> 检查日期：2026-03-15 | 共发现 **8 项** 问题/改进点，均已修复。

### 🔴 编译级缺陷（已修复）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------|
| 1 | `UserService.java` | 接口中缺少 `login()` 方法声明，且缺少 `java.util.List` import，导致编译失败 | 补全 `login()` 方法签名和 `import java.util.List` |
| 2 | `QuizResult.java` | 实体字段 `suggestion` 无法匹配数据库列名 `result_desc`，运行时会报 Unknown column | 为 suggestion 字段加 `@TableField("result_desc")` 注解 |

### 🟡 安全/规范类问题（已修复）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------|
| 3 | `SecurityConfig.java` | 未放行 WebSocket 握手路径 `/ws/**` 和公开读取类 API（文章列表、广场、问卷列表等），匿名访客无法进入 | 新增 `.antMatchers("/ws/**", "/api/article/list", ...)` 放行规则 |
| 4 | `UserServiceImpl.java` | 直接 `new BCryptPasswordEncoder()` 违反 Spring IoC 规范，与 SecurityConfig 中 `@Bean` 声明的 PasswordEncoder 不一致 | 改为通过 `@Resource` 注入 `PasswordEncoder` |
| 5 | `GlobalExceptionHandler.java` | 仅有一个通用 Exception handler，无法区分参数校验错误和业务异常，且系统异常会暴露堆栈信息 | 细分为三级：`MethodArgumentNotValidException`(400) → `RuntimeException`(500 业务) → `Exception`(500 系统兜底，不暴露细节) |

### 🟢 性能/配置优化（已修复）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------|
| 6 | `SensitiveWordUtils.java` | `replaceSensitiveWord()` 中用 `String +=` 拼接替换字符，频繁创建临时对象 | 改用 `StringBuilder` |
| 7 | `application.yml` | 缺少文件上传大小限制和 Jackson 日期序列化配置，前后端日期交互可能 JSON 解析异常 | 新增 `spring.servlet.multipart` 限制 10MB 和 `spring.jackson` 格式化 |
| 8 | `schema.sql` | 缺少 `teacher_schedule` 建表语句，实体已创建但数据库无对应表 | 追加 `CREATE TABLE teacher_schedule` 建表 SQL |

### ⚠️ 已知但暂不处理的待确认项

| # | 描述 | 影响 | 备注 |
|---|------|------|------|
| A | 点赞操作（Post/Comment）直接 `setLikes(likes+1)` 后 updateById，高并发下存在竞态 | 低（项目属课程设计级别，并发极低） | 若后期流量放大可改用 `UPDATE SET likes = likes + 1` SQL |
| B | IDE 报大量 "not on classpath" 警告 | 无（IDE 假阳性） | 项目配置 Java 1.8 但运行环境为 JRE 21，不影响 Maven 编译 |
| C | 多个 Controller 中 `getCurrentUserId()` 方法重复 | 代码冗余 | 可后期抽取为 `BaseController` 或工具类 |

## 技术实现细节延伸补充（后端部分模块归纳）
- **心理测评体系 (Module 6)**: 采用 `Question/Quiz/QuizResult` 实体闭环结构。将答案 `Map<QuestionId, Score>` 在服务端实时比对并累加进行粗分计算。
- **排班与工单审批系统 (Module 7)**: 通过 `TeacherSchedule` 数据表完成咨询师时间切片存储。结合 `Appointment` 预留审批流。
- **WebSocket 在线实时通信 (Module 8)**: 在 `WebSocketServer` 组件中维系在线 `ConcurrentHashMap`，自动对消息进行 JSON 序列化持久化落库 (`ChatMessage`)。若对方断联会自动降级为离线留言。
- **超管系统治理层 (Module 9)**: 由 `AdminController` 开口，利用 Spring Security 的 Role 拦截及鉴权，提供了冻结账号、下架文章与暂停问卷开放的核心调控能力。
