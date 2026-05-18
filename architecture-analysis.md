# 选课系统架构梳理

## 一、学生选课完整链路

从前端发起请求到数据落库，整个调用链经过以下层次：

```
前端 Vue 组件 → axios 拦截器 / request 封装 → Spring Controller → Spring Service → Spring Data JPA Repository → MySQL 数据库
```

### 1.1 详细调用流程

以"学生选一门课"为例：

**第 1 步：前端发起请求**（`frontend/src/views/Course.vue:142-153`）

学生在课程列表页点击"选课"按钮，调用 `handleSelectCourse`：
- 从 `localStorage` 中取出 `userId`（登录时存储）
- 向 `/api/selections` 发送 POST 请求，payload 为 `{ student: { id }, course: { id } }`

**第 2 步：axios 请求封装**（`frontend/src/api/request.js:1-17`）

- 所有请求通过 axios 实例发出，`baseURL` 为 `/api`
- 响应拦截器将 `response.data` 直接返回（即后端返回的 JSON body）
- 错误拦截器打印错误并 reject
- **注意**：这里**没有**配置请求拦截器来自动携带 token，token 仅存在 localStorage 中，请求并不会自动带上

**第 3 步：Controller 层接收**（`backend/.../controller/SelectionController.java:28-31`）

`SelectionController.create()` 接收 `@RequestBody Selection`，直接透传给 Service。Controller 没有做任何参数校验或业务判断。

**第 4 步：Service 层处理**（`backend/.../service/SelectionService.java:29-31`）

`SelectionService.save()` 只有一行 `selectionRepository.save(selection)`，没有任何业务逻辑。

**第 5 步：Repository 层持久化**（`backend/.../repository/SelectionRepository.java:1-16`）

继承 `JpaRepository<Selection, Long>`，由 Spring Data JPA 自动生成 SQL。

**第 6 步：数据库落地**（`backend/init.sql:35-43`）

`selection` 表上有一个 `UNIQUE KEY unique_selection (student_id, course_id)` 约束，保证同一学生不能重复选同一课程。如果违反此约束，会抛出 `DataIntegrityViolationException`，被全局异常处理器捕获并返回 409。

### 1.2 退课链路（学生视角）

`frontend/src/views/Selection.vue:202-218` 中点击"退选"按钮：
- 调用 `request.delete('/selections/{row.id}')`
- 后端 `SelectionController.delete()` → `SelectionService.deleteById()` → `SelectionRepository.deleteById()`
- 如果该选课记录已有成绩（`grade !== null`），前端按钮会被 `disabled`，从 UI 层面禁止退选

---

## 二、现有接口鉴权机制

### 2.1 鉴权方式：**既不是 JWT，也不是 Session**

当前系统实际上**没有真正的后端鉴权**。登录返回的 token 只是一个"伪 token"。

**登录逻辑**（`backend/.../controller/AuthController.java:24-59`）：

1. 接收 `LoginRequest`（username + password）
2. 先在 `admin` 表中按 username 查找，密码明文比对
3. 若不是管理员，再在 `student` 表中按 `student_number` 查找，密码明文比对（学生默认密码为 `123456`）
4. 登录成功后返回一个拼接的字符串作为"token"：
   - 管理员：`"admin-token-" + admin.getId()`
   - 学生：`"student-token-" + student.getId()`

**关键问题**：

| 问题 | 说明 |
|------|------|
| **token 无签名** | 只是一个简单的字符串拼接，客户端完全可以伪造（比如 `student-token-999`） |
| **无服务端存储** | token 不存在数据库或任何存储中，后端无法校验其有效性 |
| **无拦截校验** | 没有 Spring Security 或自定义 `HandlerInterceptor`，任何 API 都可以不带 token 直接调用 |
| **密码明文存储** | `admin` 和 `student` 表的 password 字段都是明文 |
| **无过期机制** | token 没有过期时间，一旦生成永久有效 |
| **前端未携带 token** | axios 实例没有配置请求拦截器自动加 token 到 header（`request.js` 中无 `interceptors.request`） |

### 2.2 前端权限控制

权限仅在前端通过 `localStorage` 中的 `role` 和 `token` 做路由守卫（`frontend/src/router/index.js:58-74`）：

- 未登录（无 token）→ 跳转到 `/login`
- 学生访问管理员页面 → 重定向回 `/courses`
- 按钮级控制：学生看不到"添加选课""编辑"按钮，只能看到"退选"

这属于**前端展示层控制**，安全上可以被绕过。

---

## 三、功能扩展分析："只允许在开课前 3 天内退课"

### 3.1 需要改动的模块

要实现这个功能，核心需求是：**需要知道"课程什么时候开课"，并且在退课时校验当前时间是否距开课时间 ≤ 3 天**。

### 3.2 改动清单

#### 3.2.1 数据库层（`init.sql`）

`course` 表当前只有 `course_number`、`name`、`credits` 三个字段，**没有开课时间**。需要增加字段：

- 给 `course` 表添加 `start_date`（DATE）或 `start_time`（DATETIME）字段，表示课程的开课日期/时间

#### 3.2.2 实体类层（`backend/.../entity/Course.java`）

- 添加 `private LocalDate startDate;`（或 `LocalDateTime`）字段，配合 JPA 注解 `@Column`
- JPA `ddl-auto=update` 会自动同步表结构，不需要手动改 SQL

#### 3.2.3 Repository 层

- `CourseRepository` 继承 `JpaRepository`，添加字段后无需改动，Spring Data JPA 会自动处理新字段的 CRUD

#### 3.2.4 Service 层（`backend/.../service/SelectionService.java`）

需要在 `deleteById` 方法中增加业务逻辑：
- 通过 `Selection` 记录拿到关联的 `Course`
- 读取 Course 的 `startDate`
- 判断 `startDate - 当前日期` 是否在 0~3 天范围内
  - 如果已开课超过 3 天（或已开课），抛出业务异常
  - 如果符合条件，执行删除
- 由于需要关联查询 Course，可以考虑在 `SelectionRepository` 增加 `@Query` 直接 JOIN 查出关联课程，或者在 Service 层分别调用

#### 3.2.5 Controller 层（`backend/.../controller/SelectionController.java`）

- `delete` 方法签名不变（仍为 `@DeleteMapping("/{id}")`），由 Service 层抛出业务异常即可
- 建议新增一个 `BusinessException` 类和对应的异常处理方法，返回明确的错误码和中文提示（当前只有 `GlobalExceptionHandler` 处理 `DataIntegrityViolationException` 和通用 `Exception`，不够精细）

#### 3.2.6 前端层

**课程管理页面**（`Course.vue`）：
- 课程的增/编/表单中需要增加"开课日期"字段（`el-date-picker`）
- 列表表格中展示开课日期

**选课/退课页面**（`Selection.vue`）：
- 退课前可以做一层预校验：获取该选课记录关联课程的开课时间，判断是否允许退课
- 如果允许，显示"退选"按钮；不允许则禁用并提示原因
- **注意**：前端校验只能改善用户体验，**不能替代后端校验**

#### 3.2.7 初始化数据（`init.sql`）

- 新增的 `start_date` 字段需要为已有的课程填入合理的开课日期
- 如果字段允许为 NULL，也需要在业务逻辑中处理 NULL 的情况（如 NULL 表示无开课时间限制）

### 3.3 改动影响总结

| 模块 | 改动内容 | 影响范围 |
|------|---------|---------|
| `Course.java` | 新增 `startDate` 字段 | 所有使用 Course 实体的地方（新增字段不破坏已有逻辑） |
| `SelectionService.java` | `deleteById` 增加时间校验逻辑 | 仅影响退课操作，其他功能不受影响 |
| `SelectionController.java` | 无需改动方法签名，但可能需新增异常处理 | 无破坏性变更 |
| `GlobalExceptionHandler.java` | 新增 `BusinessException` 的处理方法 | 不影响已有异常处理 |
| `init.sql` | `course` 表加字段 + 测试数据 | 仅新部署时生效 |
| `Course.vue` | 表单加"开课日期"字段、表格展示 | 前端页面 |
| `Selection.vue` | 退课时预校验开课时间 | 前端页面 |

### 3.4 实现建议

1. **建议把时间校验放在 Service 层**，而不是 Controller 或 Repository 层，符合分层架构原则
2. **建议新增自定义异常类**（如 `BusinessException`），而不是用通用 `RuntimeException`，便于全局异常处理器分类处理
3. **课程表新增字段时**，要考虑已有数据的兼容性（老数据没有开课日期），可以设置字段为可选（nullable），业务逻辑中对 NULL 做特殊处理（如"无开课时间则不受时间限制"）
4. **前端预校验 + 后端强校验**：前端给用户即时反馈，后端是最终安全保障
