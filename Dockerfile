#BUILD STAGE
FROM maven:3.8.3-openjdk-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

#PACKAGE STAGE
FROM openjdk:17-jdk-slim
COPY --from=build /home/app/target/demo-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/usr/local/lib/demo.jar"]

# # Use Maven image to build the project, remove if not working
# FROM maven:3.8.7-eclipse-temurin-17 AS build
# WORKDIR /demo
# COPY . .
# RUN mvn clean package -DskipTests

# # Use a minimal JDK image to run the application
# FROM eclipse-temurin:17-jdk-alpine
# VOLUME /tmp
# COPY --from=build /demo/target/*.jar app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]
# EXPOSE 8081

# # Use a minimal JDK image to run the application, original code for docker desktop
# FROM eclipse-temurin:17-jdk-alpine
# VOLUME /tmp

# COPY target/*.jar app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]
# EXPOSE 8081