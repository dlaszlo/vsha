FROM adoptopenjdk:latest
COPY vsha.jar vsha.jar
COPY application.properties application.properties
COPY logback.xml logback.xml
ENTRYPOINT ["java", "-Xmx512m", "-jar","-Djava.awt.headless=true", "-Dlogging.config=file:///logback.xml", "-Dspring.config.location=file:///application.properties","-Dlog.directory=/var/log/homeautomation", "/vsha.jar"]
