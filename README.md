# Dilidili 🎥

Dilidili 是一个受BiliBili启发的视频分享平台。第二版采用 [Spring Cloud](https://spring.io/projects/spring-cloud) 微服务架构，相较于第一版的单体架构，功能更丰富、扩展性更强，支持用户认证、视频管理、弹幕、推荐系统、广告服务等，结合MySQL、Redis和MinIO，提供高性能的视频分享体验！😊

## 项目架构图 📊

以下是Dilidili项目的微服务架构图，展示模块关系和数据流：

![Dilidili Architecture](./docs/architecture.png)

## 核心功能 ✨

- **用户管理**：注册、登录、用户信息管理，基于JWT和Spring Security 🔒
- **视频服务**：视频上传、播放、点赞、搜索、删除，文件存储于 [MinIO](https://min.io/) 📤
- **弹幕系统**：实时弹幕互动，提升用户体验 💬
- **推荐系统**：基于用户行为的协同过滤推荐，Redis缓存优化性能 🎯
- **广告服务**：广告投放与性能跟踪，助力平台变现 📊
- **点赞与收藏**：用户可点赞和收藏视频，数据存储于MySQL 👍
- **服务注册与发现**：Eureka实现微服务动态管理 🌐
- **配置中心**：Spring Cloud Config统一管理配置 ⚙️

## 与第一版的主要区别 🚀

相较于第一版（单体架构的`dilidili`），第二版（[`dilidili_springcloud`][repo]）有以下重大升级：
- **架构升级**：从单体架构转为微service架构，拆分为11个模块（如`user-service`、`video-service`、`recommend-service`），提升可扩展性和维护性。
- **功能扩展**：
  - 新增**弹幕服务**（`danmu-service`）、**推荐系统**（`recommend-service`）、**广告服务**（`ad-service`）和**点赞收藏**（`like-collect-service`）。
  - 推荐系统基于用户行为（观看、点赞、评论）实现协同过滤，优于第一版的简单列表查询。
- **技术栈增强**：
  - 引入 [Spring Cloud](https://spring.io/projects/spring-cloud)（Eureka、Config）支持服务注册和配置管理。
  - 使用 [MinIO](https://min.io/) 替代本地文件存储，适合分布式环境。
  - 数据库从单一`dilidili`拆分为多个（如`dilidili_user`、`dilidili_video`），模块化管理。
- **部署优化**：支持分布式部署，需先启动Eureka和Config服务，适应高并发场景。
- **性能提升**：Redis缓存用户行为和推荐结果，MapStruct优化对象映射，Lombok简化代码。

## 技术栈 🛠️

- **Spring Boot**：微服务基础框架（版本3.4.3）
- **Spring Cloud**：微服务治理（版本2024.0.1，含Eureka、Config）
- **MyBatis-Plus**：ORM框架（版本3.5.8）
- **MySQL**：主数据库，模块化存储用户、视频、弹幕等
- **Redis**：缓存用户行为、推荐结果、JWT黑名单
- **MinIO**：分布式对象存储，存储视频文件（版本8.5.12）
- **JWT**：令牌认证（版本4.5.0）
- **Spring Security**：API安全保护
- **Lombok**：简化代码（版本1.18.34）
- **MapStruct**：对象映射（版本1.6.2）
- **Swagger**：API文档（Springdoc OpenAPI）

## 项目结构 📂

项目采用多模块微服务架构，核心模块包括：
- `common`：共享实体类（如`User`、`Video`）和工具类
- `api-gateway`：统一路由和认证
- `user-service`：用户管理
- `video-service`：视频管理
- `danmu-service`：弹幕功能
- `recommend-service`：视频推荐
- `ad-service`：广告管理
- `like-collect-service`：点赞和收藏
- `eureka-server`：服务注册与发现
- `config-server`：配置中心
- `auth-service`：认证服务

## 快速开始 🚀

### 前置条件
- **Java 21**：确保安装 [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)（`pom.xml`指定）
- **MySQL**：运行MySQL（默认端口3306）
- **Redis**：运行Redis（默认端口6379）
- **MinIO**：运行 [MinIO](https://min.io/docs/minio/container/index.html)（默认端口9000）
- **Maven**：用于构建项目

### 安装步骤
1. 克隆 [项目仓库][repo]：
   ```bash
   git clone https://github.com/qinghuan11/dilidili_springcloud.git
   cd dilidili_springcloud
   ```
2. 安装依赖：
   ```bash
   mvn clean install
   ```
3. 配置服务：
   - 配置`config-server`的`application.yml`，指定Git仓库或其他配置源。
   - 为各模块（如`user-service`、`video-service`）设置`application-dev.yml`中的MySQL、Redis、MinIO连接（见下方示例）。
4. 启动Eureka服务：
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
5. 启动Config服务：
   ```bash
   cd config-server
   mvn spring-boot:run
   ```
6. 启动其他模块（如`user-service`、`video-service`）：
   ```bash
   cd user-service
   mvn spring-boot:run
   ```
7. 访问API文档：启动`api-gateway`后，打开 [Swagger UI][swagger] 查看接口详情 📜

### 配置示例
**`user-service`（`application-dev.yml`）**：
```yaml
server:
  port: 8081
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dilidili_user?useSSL=false&serverTimezone=UTC
    username: root
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: qinghuan.dao.domain
  configuration:
    map-underscore-to-camel-case: true
```

**`video-service`（`application-dev.yml`）**：
```yaml
server:
  port: 8082
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/dilidili_video?useSSL=false&serverTimezone=UTC
    username: dilidili_user
    password: yourpassword
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
video:
  storage:
    path: /path/to/storage/
minio:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
```

**注意**：替换`yourpassword`、`your_access_key`、`your_secret_key`为实际值。

## API测试 🧪

通过Postman或 [Swagger UI][swagger] 测试API（经`api-gateway`访问）：
- 用户注册：`POST /api/users/register`
- 用户登录：`POST /api/users/login`
- 视频上传：`POST /api/videos/upload`（需JWT认证）
- 获取推荐：`GET /api/recommendations?userId={userId}&size={size}`

## 开发与贡献 🤝

欢迎为Dilidili贡献代码！参与步骤：
1. Fork [项目仓库][repo]。
2. 创建特性分支：`git checkout -b feature/your-feature`。
3. 提交更改：`git commit -m "添加你的功能"`。
4. 推送分支：`git push origin feature/your-feature`。
5. 提交Pull Request，描述更改详情。

欢迎贡献测试、文档、新微服务模块等！😄
