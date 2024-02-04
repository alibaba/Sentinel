#!/bin/bash

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

cygwin=false
darwin=false
os400=false
case "`uname`" in
CYGWIN*) cygwin=true;;
Darwin*) darwin=true;;
OS400*) os400=true;;
esac

cd $(dirname $0)/..
DIR_HOME="${PWD}"

source ${DIR_HOME}/bin/env.sh

[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=$HOME/jdk/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/local/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/usr/java
[ ! -e "$JAVA_HOME/bin/java" ] && JAVA_HOME=/opt/java
[ ! -e "$JAVA_HOME/bin/java" ] && unset JAVA_HOME

if [ -z "$JAVA_HOME" ]; then
  if $darwin; then
    if [ -x '/usr/libexec/java_home' ] ; then
      export JAVA_HOME=`/usr/libexec/java_home`
    elif [ -d "/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home" ]; then
      export JAVA_HOME="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Home"
    fi
  else
    JAVA_PATH=`dirname $(readlink -f $(which javac))`
    if [ "x$JAVA_PATH" != "x" ]; then
      export JAVA_HOME=`dirname $JAVA_PATH 2>/dev/null`
    fi
  fi
  if [ -z "$JAVA_HOME" ]; then
        echo  "Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better!"
  fi
fi


PID=`ps -ef | grep -i ${RUN_NAME} | grep -i ${DIR_TARGET} | grep java | grep -v grep | awk '{print $2}'`

if [ -z "$PID" ]; then
  echo "No ${RUN_NAME} is running."
else
  echo "[pid:${PID}] [${RUN_NAME}] is running..."
  echo "kill ${PID}" && kill ${PID} && echo "[pid:${PID}] [${RUN_NAME}] Send shutdown signal to server successful"

  #wait server stop
  LOOPS=0
  while(true)
  do
    PID=`ps -ef | grep -i ${RUN_NAME} | grep -i ${DIR_TARGET} | grep java | grep -v grep | awk '{print $2}'`

    if [ -z "$PID" ]; then
      echo "Shutdown successful! Cost $LOOPS seconds."
        break;
    fi
    #judge time out
    if [ "$LOOPS" -gt 180 ]; then
        echo "Stop server cost time over 180 seconds. Now force stop it."
        kill -9 $PID && echo "Force stop successful."
        break;
    fi

    let LOOPS=LOOPS+1
    sleep 1
  done
fi



if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA="java"
fi

if [ ! -d "${DIR_LOGS}" ]; then
  mkdir -p  ${DIR_LOGS}
fi

if [ ! -d "${DIR_GC}" ]; then
  mkdir -p  ${DIR_GC}
fi


echo "#####################################################################################" >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######     DIR_HOME: " ${DIR_HOME}                                                     >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######    JAVA_HOME: " ${JAVA_HOME}                                                    >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######    BASH_OPTS: " ${BASH_OPTS}                                                    >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######     JVM_OPTS: " ${JVM_OPTS}                                                     >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######   JAR_D_OPTS: " ${JAR_D_OPTS}                                                   >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######     JAR_OPTS: " ${JAR_OPTS}                                                     >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######  SPRING_OPTS: " ${SPRING_OPTS}                                                  >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "######     RUN_NAME: " ${RUN_NAME}                                                     >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "#####################################################################################" >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &


echo "#####################################################################################" >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "${JAVA} ${BASH_OPTS} ${JVM_OPTS} ${JAR_D_OPTS} ${JAR_OPTS} ${SPRING_OPTS} ${RUN_NAME}" >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
echo "#####################################################################################" >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
nohup ${JAVA} ${BASH_OPTS} ${JVM_OPTS} ${JAR_D_OPTS} ${JAR_OPTS} ${SPRING_OPTS} ${RUN_NAME}  >> ${DIR_LOGS}/sentinel-dashboard.out 2>&1 &
PID=`ps -ef | grep -i ${RUN_NAME} | grep -i ${DIR_TARGET} | grep java | grep -v grep | awk '{print $2}'`
echo "[pid:${PID}] [${RUN_NAME}] is starting，you can check the ${DIR_LOGS}/logs/start.out"