FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY common-lib/pom.xml common-lib/pom.xml
COPY monolith-service/pom.xml monolith-service/pom.xml

COPY common-lib common-lib
COPY monolith-service monolith-service

RUN mvn clean package -B -pl monolith-service -am -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/monolith-service/target/monolith-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
