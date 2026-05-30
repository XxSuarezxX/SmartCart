# =========================================================
# Stage 1: Build Angular
# =========================================================
FROM node:20-alpine AS frontend-build
WORKDIR /frontend

COPY smartcart-frontend/package*.json ./
RUN npm ci

COPY smartcart-frontend/ ./
RUN npm run build

# =========================================================
# Stage 2: Build Spring Boot (Maven)
# =========================================================
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src

COPY --from=frontend-build /src/main/resources/static ./src/main/resources/static

RUN mvn -B clean package -DskipTests

# =========================================================
# Stage 3: Runtime
# =========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
