# ===== BUILD =====
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Кэш зависимостей
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies || true

# Исходники
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ===== RUNTIME =====
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV JAVA_OPTS=""
COPY --from=build /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]