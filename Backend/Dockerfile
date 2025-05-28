FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /app

# Add DNS configuration to fix connectivity issues
#RUN echo "nameserver 8.8.8.8" > /etc/resolv.conf  (Not needed here as handled in dockerdesktop)

# Install dos2unix to fix line ending issues
RUN apk add --no-cache dos2unix

# Copy maven executable and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Fix line endings and make the maven wrapper executable
RUN dos2unix ./mvnw && chmod +x ./mvnw

# Download all dependencies
RUN ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application
RUN ./mvnw package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Environment variables will be set in docker-compose.yml
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]