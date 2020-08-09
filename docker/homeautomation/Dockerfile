FROM arm32v7/openjdk:11

EXPOSE 8080

ENV TZ=Europe/Budapest

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl jq \
 && rm -rf /var/lib/apt/lists/*

COPY vsha.jar vsha.jar
COPY application.properties application.properties
COPY logback.xml logback.xml
ENTRYPOINT ["java", \
            "-Xmx512m", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Djava.awt.headless=true", \
            "-Dlogging.config=file:///logback.xml", \
            "-Dspring.config.location=file:///application.properties", \
            "-Dlog.directory=/var/log/homeautomation", \
            "-jar", "/vsha.jar" \
]

HEALTHCHECK --start-period=15s --interval=1m --timeout=10s --retries=5 \
            CMD curl --silent --fail --request GET http://localhost:8080/actuator/health \
                | jq --exit-status '.status == "UP"' || exit 1
