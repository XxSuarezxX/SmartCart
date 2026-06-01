FROM node:20-alpine AS frontend-build
WORKDIR /frontend

COPY smartcart-frontend/package*.json ./
RUN npm ci

COPY smartcart-frontend/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN rm -rf ./src/main/resources/static/*

COPY --from=frontend-build /frontend/dist/smartcart-frontend/ ./src/main/resources/static/

RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]