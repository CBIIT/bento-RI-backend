# Build stage
ARG ECR_REPO
ARG BENTO_API_VERSION
FROM maven:3.8.5-openjdk-17 as build
WORKDIR /usr/src/app
COPY . .

# update application.properties file
RUN rm -y src/main/resources/application.properties \
 && cp src/main/resources/application.properties.j2 src/main/resources/application.properties \
 && sed -i "s/{{bento_api_version}}/${BENTO_API_VERSION}/g" src/main/resources/application.properties

RUN mvn package -DskipTests

# Production stage
FROM ${ECR_REPO}/base-images:backend-jdk17
RUN rm -rf /usr/local/tomcat/webapps/ROOT
COPY --from=build /usr/src/app/target/Bento-0.0.1.war /usr/local/tomcat/webapps/ROOT.war
