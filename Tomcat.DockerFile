FROM maven:3.9.6-eclipse-temurin-11 as builder

COPY . /src
WORKDIR /src

# RUN mvn test

RUN mvn clean package

FROM tomcat:8.5.86 as runtime

# Only copy the final release from the build stage
COPY --from=builder /src/target/gazetteer.war  /usr/local/tomcat/webapps/ROOT.war
