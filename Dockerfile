FROM amd64/buildpack-deps:buster-curl as installer

ARG SENTINEL_VERSION
ARG HOT_FIX_FLAG=""

RUN set -x \
    && curl -SL --output /var/tmp/sentinel-dashboard.jar https://github.com/alibaba/Sentinel/releases/download/${SENTINEL_VERSION}/sentinel-dashboard-${SENTINEL_VERSION}.jar \
    && cp /var/tmp/sentinel-dashboard.jar /home \
    && rm -rf /var/tmp/sentinel-dashboard.jar

FROM openjdk:8-jre-slim

WORKDIR $BASE_DIR

# copy nacos bin
COPY --from=installer ["/home/sentinel-dashboard.jar", "/home/sentinel-dashboard.jar"]

ENV JAVA_OPTS '-Dserver.port=8080 -Dcsp.sentinel.dashboard.server=localhost:8080'

RUN chmod -R +x /home/sentinel-dashboard.jar

EXPOSE 8080

CMD java ${JAVA_OPTS} -jar /home/sentinel-dashboard.jar

