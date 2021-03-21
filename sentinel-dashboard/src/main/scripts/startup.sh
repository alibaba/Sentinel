#!/bin/bash
export SERVICE_NAME=sentinel-dashboard

cd $(dirname $0)/..

JAVA_OPTS="${JAVA_OPTS:- -Xmx512M -XX:+UseG1GC}"

nohup java ${JAVA_OPTS} -jar ${SERVICE_NAME}*.jar >> console.log 2>&1 &