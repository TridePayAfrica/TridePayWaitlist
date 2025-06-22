# Stage 1: Build with Maven
FROM maven:3.9.4-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# make sure mvn is on PATH
ENV MAVEN_HOME=/opt/maven \
    PATH=$MAVEN_HOME/bin:$PATH

# now mvn will resolve
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run with JDK
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Copy fat JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your app listens on
EXPOSE 8080

# Launch the app
ENTRYPOINT ["java","-jar","app.jar"]
