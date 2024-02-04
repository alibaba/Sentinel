

rem ===========================================================================================
rem custom Configuration
rem ===========================================================================================
set SERVER_NAME=sentinel.dashboard


rem ===========================================================================================
rem Constant Configuration
rem ===========================================================================================
set JAR=sentinel-dashboard.jar
set RUN_NAME=alibaba.%SERVER_NAME%
set JAVA_HOME=%JAVA_HOME%

set DIR_BIN=%DIR_HOME%\bin
set DIR_TARGET=%DIR_HOME%\target
set DIR_CONF=%DIR_HOME%\conf
set DIR_LOGS=%DIR_HOME%\logs
set DIR_GC=%DIR_HOME%\gc


rem ===========================================================================================
rem JVM Configuration
rem ===========================================================================================
set JVM_OPTS=
rem set JVM_OPTS=%JVM_OPTS% -server -Xms512m -Xmx512m -Xmn24m -Xss256K -XX:MetaspaceSize=128m
rem set JVM_OPTS=%JVM_OPTS% -XX:SurvivorRatio=4 -XX:MaxTenuringThreshold=15
rem set JVM_OPTS=%JVM_OPTS% -XX:+UseParNewGC -XX:+UseConcMarkSweepGC
rem set JVM_OPTS=%JVM_OPTS% -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=0
rem set JVM_OPTS=%JVM_OPTS% -XX:+DoEscapeAnalysis -XX:-UseLargePages
rem set JVM_OPTS=%JVM_OPTS% -XX:+UseFastAccessorMethods -XX:+AggressiveOpts
rem set JVM_OPTS=%JVM_OPTS% -XX:+DisableExplicitGC
rem set JVM_OPTS=%JVM_OPTS% -XX:HeapDumpPath=%DIR_GC%\dump.hprof -XX:-OmitStackTraceInFastThrow -XX:+HeapDumpOnOutOfMemoryError
rem set JVM_OPTS=%JVM_OPTS% -Xloggc:%DIR_GC%\gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=100 -XX:GCLogFileSize=100M
rem set JVM_OPTS=%JVM_OPTS% -Xloggc:%DIR_GC%\gc.log -verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=100 -XX:GCLogFileSize=100M


rem ===========================================================================================
rem JAVA -D Configuration
rem ===========================================================================================
set JAR_D_OPTS=
set JAR_D_OPTS=%JAR_D_OPTS% -Dfile.encoding=UTF-8
set JAR_D_OPTS=%JAR_D_OPTS% -Dapp.dir.home=%DIR_HOME%

rem ===========================================================================================
rem JAVA -jar Configuration
rem ===========================================================================================
set JAR_OPTS=
set JAR_OPTS=%JAR_OPTS% -jar %DIR_TARGET%\%JAR%


rem ===========================================================================================
rem Spring Configuration
rem ===========================================================================================
set SPRING_OPTS=
set SPRING_OPTS=%SPRING_OPTS% --spring.config.additional-location=%DIR_CONF%\
rem set SPRING_OPTS=%SPRING_OPTS% --logging.config=%DIR_CONF%\logback-spring.xml
