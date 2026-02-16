# Use only the runtime stage since Jenkins already built the JAR
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the JAR directly from the Jenkins workspace target folder
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]