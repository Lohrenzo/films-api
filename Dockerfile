FROM tomcat:9.0.0
LABEL maintainer="Lorenzo"
ADD films-api.war /usr/local/tomcat/webapps/
EXPOSE 8080:8080
