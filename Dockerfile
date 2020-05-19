# Pull base image
FROM openjdk:12-alpine

# This file is only used when manually building the jar with docker (and not with mvn+jib)
# Maintainer
MAINTAINER "darius.sas@outlook.com"

EXPOSE 8080

RUN mkdir -p astracker-wd
WORKDIR  astracker-wd

ARG JAR_FILE=target/astracker-1.0.0.jar
ARG ARCAN_JAVA_DIR=arcan/Arcan-1.4.0-SNAPSHOT
ARG ARCAN_CPP_FILE=arcan/Arcan-c-1.0.2-RELEASE-jar-with-dependencies.jar

ARG STATES_DIR=states
ARG CLONED_REPOS_DIR=cloned-projects
ARG OUTPUT_DIR=output-folder

RUN mkdir -p arcan
COPY ${JAR_FILE} astracker.jar
COPY ${ARCAN_JAVA_DIR} ${ARCAN_JAVA_DIR}
COPY ${ARCAN_CPP_FILE} ${ARCAN_CPP_FILE}

RUN mkdir -p ${STATES_DIR}
RUN mkdir -p ${CLONED_REPOS_DIR}
RUN mkdir -p ${OUTPUT_DIR}
RUN mkdir -p ${OUTPUT_DIR}/trackASOutput/

COPY ${CLONED_REPOS_DIR} ${CLONED_REPOS_DIR}
COPY ${OUTPUT_DIR}/trackASOutput ${OUTPUT_DIR}/trackASOutput/

ENTRYPOINT ["java","-jar","astracker.jar"]