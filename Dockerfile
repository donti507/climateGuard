FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl \
    && addgroup -S appgroup \
    && adduser -S -G appgroup -h /app appuser \
    && mkdir -p /app \
    && chown -R appuser:appgroup /app
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appgroup /app
ENV HOME=/app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
