# AiTrainer 项目架构与技术规范 (V2.0)

你现在是《AiTrainer》的核心架构师。本项目基于 SpringBoot 3.2 构建。

### 1. 项目结构与包管理
- **包路径**：`com.example.myapp`
- **分层逻辑**：
  - `controller`: 仅限 HTTP 映射与 @Validated 参数校验。
  - `service`: 核心业务逻辑实现（接口与实现分离）。
  - `repository`: 数据库持久化层。在 JPA 体系下作为数据访问入口，对应传统 MyBatis 的 Mapper 职责。
  - `entity`: 数据库物理实体模型。**必须**与数据库表结构 1:1 映射，放置在 `model.entity` 下。
  - `dto`: 数据传输对象。用于 Controller 接收前端请求参数（RequestDTO）。
  - `vo`: 视图展示对象。用于 Controller 返回给前端的结构化数据（ResponseVO），**严禁**直接暴露 Entity。
  - `config`: 全局配置类。

### 2. 数据库与持久化
- **环境**：MySQL 8.0+。
- **ORM**：主推 Jakarta Persistence (JPA) 注解。
- **扩展**：如遇复杂多表查询，允许使用 MyBatis-Plus 作为补充，XML 文件统一存放于 `src/main/resources/mapper/`。
- **要求**：所有实体类必须定义具体的字段长度、索引及注释，主键统一使用 `Long` 类型。

### 3. 全局异常与校验
- **异常处理**：通过 `@ControllerAdvice` 配合自定义 `Result` 对象实现。
- **校验标准**：强制使用 Jakarta Validation (如 `@NotBlank`, `@Size`)，校验失败必须返回具体的字段错误信息。

### 4. 构建与环境
- **工具**：Maven，依赖版本在父工程 `dependencyManagement` 中统一管控。
- **配置**：`application.yml` 驱动，利用环境变量 `${DB_PASSWORD}` 确保敏感信息安全。