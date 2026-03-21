# 大学生心理健康咨询与“树洞”倾诉系统

## 项目概括
本项目旨在开发一个基于 SpringBoot 和 Vue 的前后端分离系统，为大学生提供一个安全、私密的情绪疏导平台，结合专业的心理咨询预约服务，实现危机干预与日常心理健康的双重保障。核心目标是构建“匿名倾诉（树洞） + 心理科普 + 测评 + 实时咨询预约”的一站式服务闭环。

## 技术选型
- **主要编程语言**: Java 17 (后端), JavaScript/TypeScript (前端)
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
| 11 | `report` | Report | 举报与审核表 |
| 12 | `teacher_schedule` | TeacherSchedule | 教师排班表 |
| 13 | `feedback` | Feedback | 咨询评价表（1-5星匿名评价） |
| 14 | `mood_log` | MoodLog | 心情打卡日记表（每日Emoji+笔记） |

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

### 二期优化核心治理模型（模块10）
- **触发器式危机预警**：树洞与心理测评增设前置拦截处理器。检测到高危成分时，系统拒绝发布/警示，抛出 `CrisisException` 阻断，返回心理援助热线，并实时输出后台告警日志。
- **UGC 内容举报闭环**：前端增设 `ReportDialog` 标准件接收用户投诉；后端 `ReportService` 落库审查；超管系统追加 `ReportManage` 看板展示并实施靶向“一键屏蔽/驳回”。

### 前端架构（F0-F9）
- **技术栈**：Vue 3 (Composition API + `<script setup>`) + Vite 5 + Element Plus + Pinia + Axios + Vue Router 4。
- **项目结构**：单前端项目同时服务学生/教师/管理员三种角色，通过路由守卫 + 菜单权限区分。
- **路由体系**：Hash 模式；学生端使用 `StudentLayout`（顶栏+内容+页脚），教师/管理端使用 `AdminLayout`（可折叠侧栏+顶栏+内容区）。
- **认证机制**：登录后 JWT 存 `localStorage`，Axios 请求拦截器自动注入 `Authorization: Bearer`，401 自动清 Token 跳登录页。
- **Pinia Store**：`auth.js` 管理 Token 持久化 + JWT payload 解析 + 角色计算属性（`isStudent/isTeacher/isAdmin`）。
- **Vite 代理**：`/api` → `localhost:8080`（REST）, `/ws` → `ws://localhost:8080`（WebSocket）, `/uploads` → `localhost:8080`（文件资源）。
- **设计风格**：薄荷绿治愈主题 `#5CB8A5`，辅以淡紫 `#B8A9C9`、暖橙 `#E8A87C`；全局 CSS 变量覆盖 Element Plus 主题色。
- **文件总量**：API 层 10 个（auth/article/post/comment/quiz/appointment/schedule/chat/admin/common），视图 17 个，布局 2 个，通用组件 2 个。

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
| **10. 二期优化 - 危机预警与干预** | 已完成 | AI | 2026-03-20 | 2026-03-20 | 实现高危关键词与测评高分触发告警与熔断 |
| **11. 二期优化 - UGC举报审核闭环** | 已完成 | AI | 2026-03-20 | 2026-03-20 | 完成双端投诉入口与总后台审查面板机制构建 |
| **12. 二期优化 - 咨询后评价与反馈** | 已完成 | AI | 2026-03-20 | 2026-03-20 | 1-5星匿名评价、前端弹窗、“我的预约”页集成完成 |
| **13. 二期优化 - 情绪管理与心情打卡** | 已完成 | AI | 2026-03-20 | 2026-03-20 | Emoji日记打卡+月度情绪统计+趋势图+日历网格全功能落地 |
| **14. 二期优化 - 隐私与免责配置** | 已完成 | AI | 2026-03-20 | 2026-03-20 | 首次登录隐私协议弹窗(滚动必读+勾选确认)，localStorage持久化 |
| **15. 工程运维完善** | 跳过 | — | — | — | 
| **16. 用户手册与数据报表** | 跳过 | — | — | — | 
| **P0. 阻断性功能补全** | 已完成 | sylvia | 待定 | 2026-03-15 | CORS/资源映射/个人中心/初始数据 |
| **P1. 功能增强** | 已完成 | sylvia | 待定 | 2026-03-15 | 管理端18接口/题目CRUD/评论分页/仪表盘 |
| **P2. 增值功能** | 已完成 | sylvia | 待定 | 2026-03-15 | 消息已读/评论嵌套/预约取消 |
| **10. 后端集成测试与环境配置优化** | 已完成 | AI | 2026-03-19 | 2026-03-19 | 完成 9 大核心 Controller 类 42 个用例的 MockMvc 自动化测试，测试全通过 |
| **11. 后端 API 功能实测（全量端点覆盖）** | 已完成 | AI | 2026-03-20 | 2026-03-20 | 覆盖 13 个 Controller 共 66 个端点功能实测，通过率 100% |
| **F0. 前端工程初始化与基础设施** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | Vite+Vue3+ElementPlus+Pinia+Axios, 全局样式/拦截器/路由守卫/布局组件 |
| **F1. 前端认证模块与首页** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | LoginView/RegisterView/HomeView, JWT 持久化登录 |
| **F2. 前端科普文章模块** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | 文章列表（分页搜索）+ 文章详情（富文本渲染） |
| **F3. 前端树洞系统模块** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | 树洞广场（排序/标签/分页）+ 详情评论区 + 匿名发布 |
| **F4. 前端心理测评模块** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | 量表列表 + 答题页（步进式）+ 结果展示 |
| **F5. 前端预约咨询模块** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | 咨询师名片 + 排班联动预约 + 我的预约记录 |
| **F6. 前端即时聊天模块** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | WebSocket 实时通信 + 会话列表 + 消息气泡 |
| **F7. 前端个人中心** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | 用户信息 + 测评历史 + 修改密码 |
| **F8. 前端教师端** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | 排班管理 + 预约审批 + 侧栏布局 |
| **F9. 前端管理端** | 已完成 | sylvia | 2026-03-19 | 2026-03-19 | 仪表盘 + 用户管理 + 文章管理 + 题库管理 |

## 代码检查与问题记录

### 后端审查记录 (2026-03-15)
【已修复】共发现 8 个潜在问题，涉及 UserService、SecurityConfig、GlobalExceptionHandler、SensitiveWordUtils 等核心模块，并已全部修复。详情可参见 Git 提交历史。

### 前端代码自检记录 (2026-03-19)
在完成 F0-F9 阶段前端开发后执行全面的代码审查，发现以下 5 个潜在问题：

1. **[高优先] WebSocket 重连死循环溢出缺陷（ChatView）**
   - **问题说明**：在 `ChatView.vue` 的 `onUnmounted` 生命周期中调用了 `ws.close()`，但同时触发了 `ws.onclose` 事件，此时 `onclose` 内硬编码了 `setTimeout` 以实现断线重连。这会导致即使用户离开聊天页面并销毁了组件，WebSocket 也会在后台无限死循环尝试重连，消耗系统资源与网络带宽。
   - **改进建议**：引入 `isUnmounted` 标志位。在组件卸载时设置为 `true`，`onclose` 回调中需判断 `!isUnmounted` 时才执行重新连接尝试。
2. **[中优先] 全局异常捕获机制与无限 Loading 状态（多页面组件）**
   - **问题说明**：为了代码精简，项目中大量 `try/catch` 的 `catch` 块被写成了 `{ /* ignore */ }`，虽然前端全局 Axios 拦截器会统一拦截并 `ElMessage` 弹出错误，但前端如果在 `try` 块内发生了其他不可预知的非请求型 JavaScript 报错（如拼写错误等），异常将被默默吞没，极为不利于排查问题。此外，部分表格和列表页的 `loading.value = false` 写在 `try` 内部的末尾，一旦中途报错将导致无限 Loading（例如 `UserManage.vue` 中已正确放置在外部或 `finally`，但部分旧代码如业务页中仍需要复核）。
   - **改进建议**：将 `catch` 块中的 `/* ignore */` 替换为 `console.warn(e)` 输出调试信息，并且严格将 `loading.value = false` 放置在 `finally` 块中。
3. **[低优先] XSS 跨站脚本安全隐患（ArticleDetailView）**
   - **问题说明**：文章详情目前使用 `v-html="article.content"` 直接渲染富文本内容。如果后台超管系统允许录入不安全的 HTML 内容，可能导致 XSS 攻击。
   - **改进建议**：虽然管理员上传可暂时认为可信，但长期维护应引入 `DOMPurify` 库对 HTML 字符串进行安全净化处理，再通过 `v-html` 渲染。
4. **[低优先] 空标签解析边界死机风险（TreeholeSquare/TreeholeDetail）**
   - **问题说明**：`post.tags.split(',')` 方法直接跟在 `v-if="post.tags"` 之后执行，如果后端由于脏数据返回了 `tags: " "` 或是仅包含空字符的内容，依然能够安全拆分。不过，若后台数据传回其他未预期的数据类型而非字符串，调用 `.split()` 可能会导致 Vue 渲染失败并产生组件白屏。
   - **改进建议**：将数据解析逻辑抽离为 `computed` 或工具函数进行安全类型检查。
5. **[优化点] 缺乏统一部署的表单验证封装**
   - **问题说明**：`LoginView` 和 `RegisterView` 中利用了完整的 `ElForm` 校验规则，这是合理的。但在“发表树洞”、“提交预约评论”时往往使用的是手动 JS 判断，不够严谨统一。
   - **改进建议**：所有涉及重要信息输入的框口都应当统一引入 `el-form` 和统一的校验 rules 以加强用户体验一致性。
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
| C | 多个 Controller 中 `getCurrentUserId()` 方法重复 | 代码冗余 | 已在 P1 阶段统一迁移到 `SecurityUtils` 工具类 |

---

> 第二次检查日期：2026-03-15 | P0+P1+P2 全量代码复查 | 审查 20+ 个文件

### 🔴 编译/运行级缺陷（已修复 ✅）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------| 
| 9 | `Question.java` | 实体缺少 `type` 字段，但 `init-data.sql` INSERT 语句含 `type` 列，会导致数据库操作时字段遗漏 | 补充 `private String type;` 字段 |
| 10 | `Appointment.java` | 状态常量注释写 `CANCELED`，但 `AppointmentServiceImpl.cancelAppointment` 中使用 `CANCELLED`，大小写/拼写不一致 | 统一为 `CANCELLED` |

### 🟡 安全/规范类问题（已修复 ✅）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------| 
| 11 | `SecurityConfig.java` | 未放行 `/api/comment/tree/**` 和 `/api/comment/all/**`，匿名用户无法查看树形评论和全量评论 | 将三个评论公开端点合并放行 |
| 12 | `AdminController.java` | `resetPassword()` 中 `new BCryptPasswordEncoder()` 违反 IoC 原则，与 SecurityConfig 中 Bean 不一致 | 改为 `@Resource` 注入 `PasswordEncoder` |

### ⚠️ 建议改进项（暂不处理）

| # | 描述 | 影响 | 建议 |
|---|------|------|------|
| D | WebSocket `/ws/chat/{userId}` 仅通过路径参数传入 userId，无 Token 校验，存在身份伪造风险 | 中（私聊场景需要安全保障） | 后期可在 `@OnOpen` 中从 Session 的 query 参数获取 JWT 并验证 |
| E | `ChatMessageServiceImpl.getConversationList()` 全量查询所有相关消息后内存分组，数据量大时性能不佳 | 低（初期数据量小） | 后期可改为 SQL GROUP BY 子查询，直接在数据库层取每组最新一条 |
| F | `CommentServiceImpl.getCommentsTree()` 中查找 replyToName 用 `stream().filter()` 遍历列表，O(n²) 复杂度 | 低（单帖评论量有限） | 可改为使用 map.get(parentId) 直接查找 |
| G | `application.yml` 中 `mybatis-plus.configuration.log-impl` 使用 StdOutImpl 输出全部 SQL 日志 | 无（开发阶段有用） | 生产部署时应移除或改为 SLF4J |

---

> 第四次检查日期：2026-03-21 | 用户反馈Bug修复 | 修复 3 个前后端交互缺陷

### 🔴 前后端交互缺陷（已修复 ✅）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------|
| 16 | `Result.java` | 后端 `msg` 字段名与前端 `request.js` 读取的 `message` 不匹配，导致所有错误消息在前端显示为"操作失败" | 将字段名 `msg` → `message`，统一前后端 |
| 17 | `UserController.java` | 修改密码接口 `@PostMapping` 与前端 `request.put` 不匹配，导致 405 错误 | 改为 `@PutMapping("/user/password")` |
| 18 | `QuizDoView.vue` | 答题页使用 `currentQuestion.title` 和 `opt.text`，但实体字段为 `content`、数据库选项用 `label`，导致题目和选项显示空白 | 修正为 `currentQuestion.content` 和 `opt.label` |
| 19 | `ProfileView.vue` | 修改密码按钮 text 样式不美观，与个人信息卡片不协调 | 改为 `plain round` 圆角按钮 + 分隔线 + 图标前缀 |
| 20 | `admin.js` | 用户/文章/题库管理3个查询路径与后端不匹配（`/admin/users` vs `/admin/user/list` 等），导致管理页面查不到数据 | 修正为 `/admin/user/list`、`/admin/article/list`、`/admin/quiz/list` |
| 21 | `report.js` | 举报管理3个 API 路径全部与后端 `ReportController` 不匹配（`/reports` vs `/report/submit`等），且处理举报 HTTP 方法 PUT vs POST 不一致 | 修正路径为 `/report/submit`、`/report/admin/list`、`/report/admin/{id}/process`，方法改为 POST |
| 22 | `DashboardView.vue` | 仪表盘字段名不匹配（前端 `userCount` vs 后端 `totalUsers` 等），且卡片标签与实际数据语义不一致 | 修正字段名并调整卡片标签与后端数据一致 |
| 23 | `Quiz.java` + `QuizServiceImpl.java` | Quiz 列表页题目数量显示为"— 题"，因 Quiz 实体无 questionCount 字段 | 添加 `@TableField(exist=false)` 虚拟字段，Service 层动态填充 |
| 24 | `tc60-data.sql` | TC60 量表无题目数据，答题页空白 | 创建25题×5级评分（0-4），满分100，分值越高越健康 |
| 25 | `QuizServiceImpl.java` | 评分建议硬编码且评分方向不直觉 | 改用 `quiz_rule` 表动态匹配 + 反转评分方向（低分=不健康） |

---

> 第三次检查日期：2026-03-19 | 后端集成单元测试全面覆盖 | 共编写并执行 42 个用例

### 🔴 测试环境级缺陷与阻断（已修复 ✅）

| # | 测试类/文件 | 问题描述 | 修复方案 |
|---|-------------|---------|---------| 
| 13 | `WebSocketConfig.java` | `@SpringBootTest` 默认缺乏 Servlet 容器环境引起 `javax.websocket.server.ServerContainer not available` 初始化报错，导致整个上下文启动失败。 | 追加 `@ConditionalOnProperty(name="websocket.enabled", matchIfMissing=true)` 注解，并在 `src/test/resources/application.properties` 中强制置为 `false`，从而在测试时跳过 WebSocket 的装配。 |
| 14 | `init-data.sql` | 数据库预置账户密码的密文 `$` 哈希均不是正确的 `admin123` 对应的 BCrypt 值，导致测试期间大量针对 `/api/auth/login` 的请求报出 500 NPE 密码错误。 | 生成并更新底层测试用户的所有密码加密为真实的 `admin123` 密文，保持数据同步。 |
| 15 | `*ControllerTest.java` | （数据超长报错截断/上下文缺少帖子数据）树洞和评论接口强关联依赖数据库初始中不存在的 ID 取数据，造成 NPE 问题。 | 通过原生的 `INSERT INTO ...` SQL 指令显式向数据库埋入 id 为 1 的测试树洞及评论基准数据。 |

---

> 第五次检查日期：2026-03-21 | 个人中心完善 + 管理后台修复

### 🟢 功能完善（个人中心）

| # | 文件 | 改动说明 |
|---|------|---------|
| 26 | `ProfileView.vue` | 完全重写：头像上传（hover 相机图标，5MB限制）、昵称/邮箱/性别/简介编辑表单、角色卡片、从 `GET /user/profile` 加载完整信息 |
| 27 | `auth.js` (store) | `setToken` 扩展支持 extraInfo 参数保存 realName/avatar；新增 `updateUserInfo` 方法供资料修改后同步 |
| 28 | `LoginView.vue` | 登录成功后将 realName/avatar 传递给 store |

### 🔴 管理后台修复（已修复 ✅）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------|
| 20 | `admin.js` | 用户/文章/题库管理3个查询路径与后端不匹配 | 修正为 `/admin/user/list` 等 |
| 21 | `report.js` | 举报管理3个API路径全错 + HTTP方法不一致 | 路径修正 + 方法改为 POST |
| 22 | `DashboardView.vue` | 仪表盘字段名不匹配 + 卡片标签语义不一致 | 字段名对齐 + 标签调整 |

### 🔴 预约与在线会话修复（已修复 ✅）

| # | 文件 | 问题描述 | 修复方案 |
|---|------|---------|---------|
| 23 | `AppointmentServiceImpl.java` | 咨询师可以给自己提交预约请求 | `submitAppointment` 中根据 `teacherId` 和 `studentId` 是否相等来拦截自预约 |
| 24 | `AppointmentServiceImpl.java` | 预约审批通过后前端聊天侧边栏空白 | `processAppointment` 修改状态为 `APPROVED` 时，自动利用 `ChatMessageService` 保存一条初始问候消息以激活线上交流 |
| 25 | `chat.js` / `ChatView.vue` | 侧边栏列表无法点击或刷新出历史（API 404报错 + 对象字段属性错配） | `chat.js` 的 Query 参数变更为 Path 路径传参。`ChatView.vue` 取值变量由 `targetId` 统一纠正为 `userId` 等对应字段名 |
| 26 | `ChatView.vue` | 聊天收发信息时，左侧会话列表（例如最后一条消息内容、未读红点等）未呈现“实时的”响应式更新效果 | 在 `ws.onmessage` 回调以及 `sendMessage` 发送逻辑中，加入异步 `getConversationList()` 重新拉取以更新侧边栏，从而实现前后统一的准实时大盘 |


| 27 | `WebSocketServer.java` | 接收方依然没能实时呈现出推送（点击后调 API 才能出来） | 后端的 `WebSocketServer` 中原先手动 `new ObjectMapper()` 没有提供 `LocalDateTime` 的反序列化支持。我给它注册了 `JavaTimeModule`，修复了这个引发推送异常中断的隐藏报错。 |

## 相关文档索引

| 文档 | 路径 | 说明 |
|------|------|------|
| API 接口文档 | `docs/API接口文档.md` | 全量 REST 端点契约，供前端联调 |
| 产品审视报告 | `docs/产品审视与开发补全清单.md` | 功能缺口分析 + 开发路线图 |
| 数据库脚本 | `database/schema.sql` | 全部 12 张表的 DDL |
