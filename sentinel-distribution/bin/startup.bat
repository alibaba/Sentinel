@echo off
rem Copyright 1999-2018 Alibaba Group Holding Ltd.
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

set DIR_HOME=%~dp0
rem added double quotation marks to avoid the issue caused by the folder names containing spaces.
rem removed the last 5 chars(which means \bin\) to get the base DIR.
set DIR_HOME=%DIR_HOME:~0,-5%

cd %DIR_HOME%

call %DIR_HOME%/bin/env.bat

title=%RUN_NAME% [%DIR_HOME%]

if not exist "%JAVA_HOME%\bin\java.exe" echo Please set the JAVA_HOME variable in your environment, We need java(x64)! jdk8 or later is better! & EXIT /B 1
set JAVA=%JAVA_HOME%\bin\java.exe



echo #####################################################################
echo ######     DIR_HOME:  %DIR_HOME%
echo ######    JAVA_HOME:  %JAVA_HOME%
echo ######     JVM_OPTS:  %JVM_OPTS%
echo ######   JAR_D_OPTS:  %JAR_D_OPTS%
echo ######     JAR_OPTS:  %JAR_OPTS%
echo ######  SPRING_OPTS:  %SPRING_OPTS%
echo ######     RUN_NAME:  %RUN_NAME%
echo #####################################################################
if not exist "%JAVA_HOME%\bin\java.exe" set JAVA=%JAVA_HOME%\bin\java.exe
if not exist %DIR_LOGS% md %DIR_LOGS%
if not exist %DIR_GC% md %DIR_GC%

call %JAVA% %JVM_OPTS% %JAR_D_OPTS% %JAR_OPTS% %SPRING_OPTS% %RUN_NAME% %*