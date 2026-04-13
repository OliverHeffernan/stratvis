FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY backend/pom.xml backend/pom.xml
COPY backend/src backend/src
COPY contracts/analysis-output-schema.json contracts/analysis-output-schema.json

WORKDIR /workspace/backend
RUN mvn -DskipTests clean package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/backend/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
