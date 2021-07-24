FROM arm32v7/openjdk:11

RUN apt-get update \
 && apt-get install -y --no-install-recommends curl jq \
 && rm -rf /var/lib/apt/lists/*

COPY build/libs/vsha.jar vsha.jar
COPY docker/application.yml application.yml
COPY docker/logback.xml logback.xml
ENTRYPOINT ["java", \
            "-Xmx512m", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Djava.awt.headless=true", \
            "-Dlogging.config=file:///logback.xml", \
            "-Dspring.config.location=file:///application.yml", \
            "-Dlog.directory=/var/log/homeautomation", \
            "-jar", "/vsha.jar" \
]
