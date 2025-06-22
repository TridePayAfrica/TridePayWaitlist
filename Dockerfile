# Use a lightweight OpenJDK 17 base image
FROM openjdk:17-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY TrideWaitlist-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose the port your application runs on (default for Spring Boot is 8080)
EXPOSE 8080

# Command to run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]