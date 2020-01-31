# Pull base image
FROM openjdk:12-alpine

# Maintainer
MAINTAINER "darius.sas@outlook.com"

ARG JAR_FILE=target/astracker-0.9.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]