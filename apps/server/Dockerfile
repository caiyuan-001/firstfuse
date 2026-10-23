# ============================================================
# firstfuse-server — Spring Boot 3 + JDK 17 多阶段构建
# 本地 .env 的 PG_HOST/REDIS_HOST/MINIO_HOST/AI_*/NEBULA_* 通过
# docker-compose env_file 注入容器环境变量，Spring Boot 自动识别
# ============================================================
FROM maven:3.10-eclipse-temurin-17 AS builder

WORKDIR /app

# 先 COPY pom.xml 利用 Docker 层缓存（依赖不变就不重下）
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# ============================================================
# 运行阶段：仅 JRE，镜像更小更安全
# ============================================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 非 root 用户运行
RUN addgroup -S app && adduser -S app -G app
COPY --from=builder --chown=app:app /app/target/firstfuse-server.jar ./
USER app

EXPOSE 8080

# JVM 内存默认值（容器部署会被 JAVA_OPTS 覆盖）
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseZGC -XX:+ZGenerational"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar firstfuse-server.jar"]
