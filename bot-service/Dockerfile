FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory in the build stage
WORKDIR /app

# Copy only the parent POM (for dependency caching)
COPY pom.xml .

# Copy the POM of each module to download dependencies before copying the code
COPY commons/pom.xml ./commons/
COPY bot-service/pom.xml ./bot-service/
COPY photo-service/pom.xml ./photo-service/
COPY quest-service/pom.xml ./quest-service/

# Download dependencies (without building) to leverage caching
RUN mvn dependency:go-offline -pl commons,bot-service,photo-service,quest-service -am

# Copy the source code
COPY commons/ ./commons/
COPY bot-service/ ./bot-service/

# Build only the required modules
RUN mvn clean package -pl commons,bot-service -am -DskipTests

# Create the final image
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the built JAR from the previous container
COPY --from=build /app/bot-service/target/bot-service-0.0.1-SNAPSHOT.jar bot-service-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "bot-service-0.0.1-SNAPSHOT.jar"]
