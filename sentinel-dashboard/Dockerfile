FROM buildpack-deps:buster-curl as installer

ARG SENTINEL_VERSION=1.8.6.2

RUN set -x \
    && curl -SL --output /home/sentinel-dashboard-apollo.jar \
    https://github.com/fengjx/Sentinel/releases/download/dashboard%2Fapollo%2Fv${SENTINEL_VERSION}/sentinel-dashboard-apollo-${SENTINEL_VERSION}.jar

FROM openjdk:8-jdk-alpine
LABEL maintainer="fengjx<https://github.com/fengjx>"

ENV TZ=Asia/Shanghai
ENV WORK_DIR=/sentinel-dashboard
ENV JAVA_OPTS=${JAVA_OPTS}

RUN mkdir -p ${WORK_DIR}
WORKDIR ${WORK_DIR}

# copy sentinel jar
COPY --from=installer ["/home/sentinel-dashboard-apollo.jar", "${WORK_DIR}/app.jar"]
COPY ["./script/run.sh", "${WORK_DIR}"]
RUN chmod 755 ${WORK_DIR}/*.sh

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "./run.sh"]
