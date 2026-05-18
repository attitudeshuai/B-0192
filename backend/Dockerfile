# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Package the application
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/course-selection-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "sleep 10 && java -Xmx512m -jar app.jar"]
