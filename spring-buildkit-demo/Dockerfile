FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /workspace

COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle build.gradle .
COPY --chown=gradle:gradle settings.gradle .
COPY --chown=gradle:gradle src src

RUN gradle clean build --no-daemon

FROM eclipse-temurin:21-jre-alpine


RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar app.jar


USER spring:spring


EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
