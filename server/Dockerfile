FROM openjdk:8-jre-alpine

LABEL MAINTAINER="Pete Cornish <outofcoffee@gmail.com>"

RUN mkdir -p /opt/bouncer

ADD build/libs/bouncer.jar /opt/bouncer/bouncer.jar

EXPOSE 9090

WORKDIR /opt/bouncer

ENTRYPOINT [ "java", "-jar", "bouncer.jar" ]
