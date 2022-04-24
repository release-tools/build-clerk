FROM eclipse-temurin:8u322-b06-jre-focal

LABEL MAINTAINER="Pete Cornish <outofcoffee@gmail.com>"

RUN mkdir -p /opt/clerk

ADD build/libs/clerk.jar /opt/clerk/clerk.jar

EXPOSE 9090

WORKDIR /opt/clerk

ENTRYPOINT [ "java", "-jar", "clerk.jar" ]
