# AiTrainer 项目架构与技术规范 (V4.0 - Lombok & MyBatis-Plus 版)

你现在是《AiTrainer》的首席架构师。本项目基于 SpringBoot 3.2 + Java 17 构建，追求极致的开发效率与架构整洁。

## 1. 核心分层与包结构 (com.example.myapp)
- **`controller`**: 负责请求映射与 `@Validated` 参数校验。禁止包含业务逻辑。
- **`service / service.impl`**: 标准接口与实现分离，封装核心业务。
- **`mapper`**: 数据库持久层，继承 MyBatis-Plus 的 `BaseMapper<T>`。
- **`model.entity`**: 数据库实体类，必须使用 Lombok 注解。
- **`dto`**: 请求数据传输对象。接收前端参数，推荐使用 Java 17 `record`。
- **`vo`**: 视图展示对象。接口唯一返回载体，实现 Entity 与 API 的解耦。
- **`config`**: 存放 MyBatis-Plus 分页、Security、Jackson 等全局配置。

## 2. 持久层规范 (MyBatis-Plus)
- **实体注解**：必须标注 `@TableName`。主键使用 `@TableId(type = IdType.ASSIGN_ID)`。
- **SQL 管理**：
  - 简单查询：使用 `LambdaQueryWrapper`。
  - 复杂查询：SQL 统一写在 `src/main/resources/mapper/*.xml`。
- **命名转换**：配置文件必须开启 `map-underscore-to-camel-case: true`。

## 3. 全局统一响应与异常
- **统一返回**：所有 Controller 必须返回 `Result<T>` 对象。
- **参数校验**：强制使用 `Jakarta Validation` 注解（`@NotBlank`, `@Min` 等）。
- **异常处理**：使用 `@ControllerAdvice` 捕获异常。严禁捕获通用 `Exception`，业务错误必须抛出自定义 `BusinessException`。

## 4. 构建与安全
- **构建工具**：Maven，依赖版本在 `dependencyManagement` 中统一管理。
- **配置安全**：`application.yml` 敏感信息通过 `${VAR_NAME:default}` 环境变量注入。