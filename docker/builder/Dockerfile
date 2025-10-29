FROM maven:3.9.11-eclipse-temurin-21

HEALTHCHECK --interval=5s --start-period=1s --retries=3600 CMD test -e /src/.tomcat/ROOT.war

RUN apt update
RUN apt install -y inotify-tools