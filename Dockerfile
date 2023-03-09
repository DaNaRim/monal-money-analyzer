FROM maven:3.8.6-amazoncorretto-17 AS maven

WORKDIR /usr/src/app

COPY pom.xml .

RUN mvn verify --fail-never

COPY . /usr/src/app

RUN mvn package -DskipTests -Dcheckstyle.skip


FROM openjdk:17-jdk-alpine

ARG JAR_FILE=monal.jar

WORKDIR /opt/app

COPY --from=maven /usr/src/app/target /opt/app/

ENTRYPOINT ["java","-jar","monal.jar"]
