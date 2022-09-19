FROM openjdk:8-jdk-alpine
LABEL maintainer="fengjx<https://github.com/fengjx>"

ENV TZ=Asia/Shanghai
ENV WORK_DIR=/sentinel-dashboard
ENV JAVA_OPTS=${JAVA_OPTS}

ARG DIST_DIR=.dist/

RUN mkdir -p ${WORK_DIR}
WORKDIR ${WORK_DIR}

COPY ${DIST_DIR} ${WORK_DIR}

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "./run.sh"]
