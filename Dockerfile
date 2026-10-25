# Stage 1: Frontend build
FROM node:20-alpine AS frontend
RUN npm install -g pnpm
WORKDIR /app/frontend
COPY frontend/package.json frontend/pnpm-lock.yaml ./
RUN pnpm install
COPY frontend/ ./
RUN pnpm build

# Stage 2: Java build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY .mvn ./.mvn
COPY mvnw pom.xml ./
COPY src ./src
COPY --from=frontend /app/frontend/dist ./target/classes/static/
RUN chmod +x mvnw && ./mvnw clean package

# Stage 3: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/family-tree-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
