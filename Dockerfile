FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/TrideWaitlist-0.0.1-SNAPSHOT.jar /app/TrideWaitlist-0.0.1-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "TrideWaitlist-0.0.1-SNAPSHOT.jar"]