# Stage 1: build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# copy the pom.xml and download dependencies first
# (This allows Docker to cache dependencies, making future builds MUCH faster)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the .jar file
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: run the application
FROM eclipse-temurin:21-jre-alpine

# Set the working directory
WORKDIR /app
# Copy ONLY the built .jar file from the 'builder' stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]