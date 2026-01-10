# 1. 빌드 스테이지
FROM gradle:8-jdk21 AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY *.gradle.kts .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon

# 2. 실행 스테이지
FROM amazoncorretto:21-alpine

WORKDIR /app
RUN apk --no-cache add curl
COPY --from=builder /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
