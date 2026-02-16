# Stage 1: Build the application
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
# Copy pom.xml and source code
#COPY pom.xml .
#COPY src ./src
# Build the JAR, skipping tests for speed
#RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
# Copy only the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port for our SSE stream and map
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]