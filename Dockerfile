# 1) Build stage: use Maven to compile & package
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app

# only copy POM first so we get dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# now copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# 2) Runtime stage: slim JRE image
FROM openjdk:17-jdk-alpine
WORKDIR /app

# copy the fat-jar from the builder
COPY --from=builder /app/target/TrideWaitlist-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
