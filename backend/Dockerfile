# 后端 Dockerfile - 两阶段构建
# 阶段一：Maven 编译打包（使用完整 JDK + Maven 环境）
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# 先复制 pom.xml 利用 Docker 缓存层加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B
# 再复制源码并打包（跳过测试加快构建）
COPY src ./src
RUN mvn clean package -DskipTests -B

# 阶段二：运行（使用精简的 JRE 镜像，体积更小）
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 从构建阶段复制 JAR 包
COPY --from=build /app/target/*.jar app.jar
# 创建上传目录（如有其他运行时文件需要）
RUN mkdir -p /app/uploads
# 暴露后端端口
EXPOSE 8080
# 启动命令，支持通过环境变量覆盖配置
ENTRYPOINT ["java", "-jar", "app.jar", \
  "--spring.datasource.url=${DB_URL:jdbc:mysql://mysql:3306/mental_health?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}", \
  "--spring.datasource.username=${DB_USER:root}", \
  "--spring.datasource.password=${DB_PASSWORD:123456}"]
