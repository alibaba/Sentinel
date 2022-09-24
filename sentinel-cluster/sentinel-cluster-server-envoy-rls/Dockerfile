FROM java:8-jre

ENV SENTINEL_HOME /sentinel

COPY target/sentinel-envoy-rls-token-server.jar $SENTINEL_HOME/

WORKDIR $SENTINEL_HOME

ENTRYPOINT ["sh", "-c"]
CMD ["java -Dcsp.sentinel.log.dir=/sentinel/logs/ ${JAVA_OPTS} -jar sentinel-envoy-rls-token-server.jar"]