# Pull base image
FROM openjdk:12-alpine

# This file is only used when manually building the jar with docker (and not with mvn+jib)
# Maintainer
MAINTAINER "darius.sas@outlook.com"

RUN mkdir -p astracker-wd
WORKDIR  astracker-wd

ARG JAR_FILE=target/astracker-0.9.0-jar-with-dependencies.jar
ARG ARCAN_JAVA_DIR=arcan/Arcan-1.4.0-SNAPSHOT
ARG ARCAN_CPP_FILE=arcan/Arcan-c-1.0.2-RELEASE-jar-with-dependencies.jar

RUN mkdir -p arcan
COPY ${JAR_FILE} astracker.jar
COPY ${ARCAN_JAVA_DIR} ${ARCAN_JAVA_DIR}
COPY ${ARCAN_CPP_FILE} ${ARCAN_CPP_FILE}

RUN mkdir -p output-folder
RUN mkdir -p cloned-projects

ENTRYPOINT ["java","-cp","astracker.jar", "org.rug.WebMain"]