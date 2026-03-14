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

| # | 表名 | 实体类 | 说明 |
|---|------|--------|------|
| 1 | `user` | User | 用户表（学生/教师/管理员共用，role 区分） |
| 2 | `post` | Post | 树洞文章表 |
| 3 | `comment` | Comment | 评论表（支持 parent_id 嵌套） |
| 4 | `quiz` | Quiz | 测评量表表 |
| 5 | `question` | Question | 量表题目表（JSON 存储选项及分值） |
| 6 | `quiz_result` | QuizResult | 测评结果表 |
| 7 | `quiz_rule` | - | 评分规则配置表（待对接） |
| 8 | `article` | Article | 科普资讯文章表 |
| 9 | `appointment` | Appointment | 预约记录表 |
| 10 | `chat_message` | ChatMessage | 聊天消息记录表 |
| 11 | `report` | - | 举报与审核表（待对接） |
| 12 | `teacher_schedule` | TeacherSchedule | 教师排班表 |

## 技术实现细节

### 认证与安全体系（模块1）
- **JWT 无状态认证**：`JwtUtils` 基于 HMAC-SHA512 签发 Token，携带 `userId`、`role` 声明，有效期 24h。
- **请求拦截链**：`JwtAuthenticationFilter`（OncePerRequestFilter）从 `Authorization: Bearer xxx` 提取并校验 Token，将 userId 注入 `SecurityContext.principal`，角色前缀 `ROLE_` 自动拼接。
- **密码安全**：BCryptPasswordEncoder（通过 IoC 注入，与 SecurityConfig 中 @Bean 保持单例一致）加密存储。
- **放行规则**：`/api/auth/**`（注册登录）、公开读取类接口（科普列表、树洞广场、问卷列表）、WebSocket `/ws/**` 端点均已放行。

### DFA 敏感词过滤引擎（模块2）
- **算法**：基于 Hutool `WordTree`（确定有限自动机 DFA）实现多模式串匹配，时间复杂度 O(n)。
- **策略**：检测到敏感词后等长替换为 `***`（使用 StringBuilder 避免频繁对象创建）。
- **扩展点**：`addWord()` / `resetWords()` 支持管理后台动态维护词库。

### 树洞与评论系统（模块4）
- **发布流程**：内容先经 DFA 脱敏 → 初始化属性（likes=0, views=0, status=1）→ 缺省匿名 → 入库。
- **广场排序**：`hot` 模式按 `likes DESC, views DESC, createTime DESC` 三级排序；默认按 `createTime DESC`。
- **匿名机制**：`is_anonymous=1` 时返回固定名「匿名用户」和默认头像；非匿名时回显 `real_name`，缺省则对 `username` 居中脱敏（`StrUtil.hide`）。

### 预约咨询体系（模块5）
- **冲突检测**：提交预约前查询同 teacherId + 同 appointDate + 同 timeSlot 且状态为 `PENDING/APPROVED` 的记录数，>0 则拒绝。
- **状态机**：`PENDING` → `APPROVED`/`REJECTED`/`COMPLETED`/`CANCELED`，由 `processAppointment()` 驱动流转。
- **双端信息补全**：学生端查询自动填充咨询师姓名；教师端查询自动填充学生姓名。

### 心理测评体系（模块6）
- **数据模型**：`Quiz`（量表）→ `Question`（题目，JSON 存储选项及分值）→ `QuizResult`（作答结果）三级关联。
- **计分算法**：当前采用粗分累加模型——前端传递 `Map<questionId, score>`，服务端遍历 values 求和。
- **建议生成**：基于总分区间映射（<30/30-60/>60）输出三级建议文案，支持后续扩展为数据库配置化（`quiz_rule` 表已预建）。

### 教师排班机制（模块7）
- **Upsert 策略**：同一 `teacherId + workDate` 组合通过唯一键约束保证幂等；`publishSchedule()` 先查后决定 insert 或 update。
- **日历查询**：`getAvailableSlots()` 按日期范围筛选教师未来排班，前端可直接渲染日历视图。

### WebSocket 即时通讯（模块8）
- **连接管理**：`WebSocketServer` 用 `ConcurrentHashMap<Long, Session>` 维护在线连接池，以 userId 为 key。
- **消息流**：接收 JSON → Jackson 反序列化 → 填充 senderId/createTime → 持久化到 `chat_message` → 查找目标 Session 推送；目标离线则仅入库等待上线后通过历史接口拉取。
- **字段说明**：`ChatMessage.type` 支持 TEXT/IMAGE 等扩展；`is_read` 字段已预留但尚未实现已读回执。

### 超管治理层（模块9）
- **权限校验**：手动检查 `ROLE_ADMIN` 权限（待优化为 `@PreAuthorize` 注解）。
- **操作范围**：用户状态切换（封禁/解禁）、文章 CRUD + 上下架、问卷 CRUD + 上下线。

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
| **P0. 阻断性功能补全** | 已完成 | sylvia | 待定 | 2026-03-15 | CORS/资源映射/个人中心/初始数据 |

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

## 相关文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| API 接口文档 | `docs/API接口文档.md` | 全量 REST 端点契约，供前端联调 |
| 产品审视报告 | `docs/产品审视与开发补全清单.md` | 功能缺口分析 + 开发路线图 |
| 数据库脚本 | `database/schema.sql` | 全部 12 张表的 DDL |
