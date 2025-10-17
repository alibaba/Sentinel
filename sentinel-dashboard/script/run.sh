#!/bin/sh

echo "work dir: ${WORK_DIR}"
export LOG_PATH="${WORK_DIR}/logs"

mkdir -p ${LOG_PATH}

# 按实际情况修改
echo "user JAVA_OPTS: ${JAVA_OPTS}"
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseG1GC -XX:MaxGCPauseMillis=500"
JAVA_OPTS="${JAVA_OPTS} -XX:ErrorFile=${LOG_PATH}/hs_err_pid%p.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${LOG_PATH} -XX:+HeapDumpBeforeFullGC -XX:+HeapDumpAfterFullGC -Xloggc:${LOG_PATH}/gc.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintCommandLineFlags"

exec java ${JAVA_OPTS} -Duser.home=${WORK_DIR} -jar ${WORK_DIR}/app.jar
