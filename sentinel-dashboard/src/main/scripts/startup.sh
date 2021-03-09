#!/bin/bash
SERVICE_NAME=sentinel-dashboard

cd `dirname $0`/..

nohup java -jar ${SERVICE_NAME}*.jar >> start.log 2>&1 &