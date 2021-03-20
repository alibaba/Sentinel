#!/bin/bash
export SERVICE_NAME=sentinel-dashboard

SERVICE_PID=$(ps -ef | grep java |grep "${SERVICE_NAME}" | grep -v grep | awk '{ print $2 }')
if [ -z "${SERVICE_PID}" ]
then
  echo "${SERVICE_NAME} is not running"
else
  echo "${SERVICE_NAME} is running, now try to kill them. PID information ${SERVICE_PID}"
  echo "${SERVICE_PID}" | xargs kill -9
fi