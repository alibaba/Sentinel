#!/bin/sh

# Copyright 1999-2018 Alibaba Group Holding Ltd.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


#===========================================================================================
# custom Configuration
#===========================================================================================
SERVER_NAME="sentinel.dashboard"


#===========================================================================================
# Constant Configuration
#===========================================================================================
JAR="sentinel-dashboard.jar"
RUN_NAME="alibaba.${SERVER_NAME}"
JAVA_HOME="${JAVA_HOME}"

DIR_BIN="${DIR_HOME}/bin"
DIR_TARGET="${DIR_HOME}/target"
DIR_CONF="${DIR_HOME}/conf"
DIR_LOGS="${DIR_HOME}/logs"
DIR_GC="${DIR_HOME}/gc"


#===========================================================================================
# /bin/bash Configuration
#===========================================================================================
BASH_OPTS=""


#===========================================================================================
# JVM Configuration
#===========================================================================================
JVM_OPTS=""
J_VERSION=`java -version 2>&1 |sed  '1!d' | sed -e 's/"//g' |awk '{print $3}'`
echo "current java version is ${J_VERSION}"
#if  [[ "${J_VERSION}" == "1.8"* ]];then
#  JVM_OPTS="${JVM_OPTS} -server -Xms512m -Xmx512m -Xmn24m -Xss256K -XX:MetaspaceSize=128m "
#  JVM_OPTS="${JVM_OPTS} -XX:SurvivorRatio=4 -XX:MaxTenuringThreshold=15 "
#  JVM_OPTS="${JVM_OPTS} -XX:+UseParNewGC -XX:+UseConcMarkSweepGC "
#  JVM_OPTS="${JVM_OPTS} -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0 "
#  JVM_OPTS="${JVM_OPTS} -XX:+DoEscapeAnalysis -XX:-UseLargePages "
#  JVM_OPTS="${JVM_OPTS} -XX:+UseFastAccessorMethods -XX:+AggressiveOpts "
#  JVM_OPTS="${JVM_OPTS} -XX:+DisableExplicitGC "
#  JVM_OPTS="${JVM_OPTS} -XX:HeapDumpPath=${DIR_GC}/dump.hprof -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError "
#  JVM_OPTS="${JVM_OPTS} -Xloggc:${DIR_GC}/gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=100 -XX:GCLogFileSize=100M "
#fi


#===========================================================================================
# JAVA -D Configuration
#===========================================================================================
JAR_D_OPTS=""
JAR_D_OPTS="${JAR_D_OPTS} -Dfile.encoding=UTF-8 "
JAR_D_OPTS="${JAR_D_OPTS} -Dapp.dir.home=${DIR_HOME} "

#===========================================================================================
# JAVA -jar Configuration
#===========================================================================================
JAR_OPTS=""
JAR_OPTS="${JAR_OPTS} -jar ${DIR_TARGET}/${JAR} "


#===========================================================================================
# Spring Configuration
#===========================================================================================
SPRING_OPTS=""
SPRING_OPTS="${SPRING_OPTS} --spring.config.additional-location=${DIR_CONF}/ "
#SPRING_OPTS="${SPRING_OPTS} --logging.config=${DIR_CONF}/logback-spring.xml "
