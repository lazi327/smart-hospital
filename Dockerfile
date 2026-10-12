# 智慧医院服务系统 —— Render Docker 构建
# Render 无原生 Java runtime，Spring Boot 需通过 Dockerfile 部署（多阶段构建）

# ---------- 构建阶段 ----------
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- 运行阶段 ----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /build/target/smart-hospital-1.0.0.jar app.jar

# Render 会注入 PORT 覆盖此默认值；应用内 server.port 读取 ${PORT:8080}
ENV PORT=10000 \
    TZ=Asia/Shanghai \
    JAVA_OPTS="-XX:MaxRAMPercentage=70.0"

RUN useradd -r -u 1001 appuser
USER appuser
EXPOSE 10000

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
