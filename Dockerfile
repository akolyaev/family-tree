FROM eclipse-temurin:26-jdk-alpine AS builder
WORKDIR /app
COPY .mvn ./.mvn
COPY mvnw pom.xml ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw clean package -Dmaven.test.skip=true

FROM eclipse-temurin:26-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/family-tree-*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
