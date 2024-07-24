# Build stage
ARG ECR_REPO
FROM maven:3.8.5-openjdk-17 as build
WORKDIR /usr/src/app
COPY . .
RUN mvn package -DskipTests

# Production stage
# FROM ${ECR_REPO}/base-images:backend-jdk17

# FROM tomcat:10.1.13-jdk17
# RUN apt-get update && apt-get install unzip
# RUN rm -rf /usr/local/tomcat/webapps.dist
# RUN rm -rf /usr/local/tomcat/webapps/ROOT

FROM tomcat:10.1.17-jdk17 AS fnl_base_image

RUN apt-get update && apt-get -y upgrade

# install dependencies and clean up unused files
RUN apt-get update && apt-get install unzip
RUN rm -rf /usr/local/tomcat/webapps.dist
RUN rm -rf /usr/local/tomcat/webapps/ROOT

# Modify the server.xml file to block error reportiing
RUN sed -i 's|</Host>|  <Valve className="org.apache.catalina.valves.ErrorReportValve"\n               showReport="false"\n               showServerInfo="false" />\n\n      </Host>|' conf/server.xml 

# expose ports
EXPOSE 8080
COPY --from=build /usr/src/app/target/Bento-0.0.1.war /usr/local/tomcat/webapps/ROOT.war
