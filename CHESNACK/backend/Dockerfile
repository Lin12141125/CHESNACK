# ============ 构建阶段 ============
# 基础镜像走 daocloud 镜像源（国内网络直连 Docker Hub 不稳定）
FROM docker.m.daocloud.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# 阿里云 Maven 镜像加速
COPY docker/settings.xml /root/.m2/settings.xml
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B package -DskipTests

# ============ 运行阶段 ============
FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
