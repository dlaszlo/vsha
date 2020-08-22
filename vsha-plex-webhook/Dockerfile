FROM arm32v7/openjdk:11

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl jq \
 && rm -rf /var/lib/apt/lists/*

COPY build/libs/vsha-plex-webhook.jar vsha-plex-webhook.jar
COPY docker/application.yml application.yml
COPY docker/logback.xml logback.xml
ENTRYPOINT ["java", \
            "-Xmx512m", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Djava.awt.headless=true", \
            "-Dlogging.config=file:///logback.xml", \
            "-Dspring.config.location=file:///application.yml", \
            "-Dlog.directory=/var/log/plex-webhook", \
            "-jar", "/vsha-plex-webhook.jar" \
]

HEALTHCHECK --start-period=15s --interval=1m --timeout=10s --retries=5 \
            CMD curl --silent --fail --request GET http://localhost/plex-webhook/actuator/health \
                | jq --exit-status '.status == "UP"' || exit 1
