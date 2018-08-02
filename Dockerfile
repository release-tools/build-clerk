FROM openjdk:8-jre-alpine

LABEL MAINTAINER="Pete Cornish <outofcoffee@gmail.com>"

RUN mkdir -p /opt/scmwebhook

ADD build/libs/scmwebhook.jar /opt/scmwebhook/scmwebhook.jar

EXPOSE 9090

WORKDIR /opt/scmwebhook

ENTRYPOINT [ "java", "-jar", "scmwebhook.jar" ]
