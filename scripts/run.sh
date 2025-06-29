#!/bin/bash

source ~/.jdk1.8

SCRIPT_DIR=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
WORK_DIR=$SCRIPT_DIR/..
WORK_DIR=$(realpath "$WORK_DIR")

if [[ -z "${INSIGHT_HOME}" ]]; then
    export INSIGHT_HOME=$WORK_DIR/build
    echo "The env variable INSIGHT_HOME has not been set, set INSIGHT_HOME=$WORK_DIR/build"
fi

export MDX_HOME=${INSIGHT_HOME}
export MDX_CONF=${INSIGHT_HOME}/conf

DATABASE_TYPE=mysql
SPRING_OPTS="$SPRING_OPTS -DINSIGHT_HOME=$INSIGHT_HOME -Dspring.config.name=insight,application -Dspring.profiles.active=$DATABASE_TYPE -Dspring.config.location=classpath:/,file:$INSIGHT_HOME/conf/"

jvm_xms=-Xms3g
jvm_xmx=-Xmx16g

JAVA_OPTS="$jvm_xms $jvm_xmx -XX:G1HeapRegionSize=4m -XX:MaxMetaspaceSize=512m -Dfile.encoding=UTF-8"

LOG_FILE="file:$MDX_CONF/log4j2.xml"
if [[ -e "${MDX_CONF}/log4j2.xml" ]];then
    echo "Use ${MDX_CONF}/log4j2.xml "
    JAVA_OPTS="$JAVA_OPTS -Dlog4j.configurationFile=${LOG_FILE}"
else
    JAVA_OPTS="$JAVA_OPTS -Dlog4j.configurationFile=log4j2.xml"
    echo "${MDX_CONF}/log4j2.xml not found, use default log4j2.xml"
fi

JAVA_OPTS="$JAVA_OPTS -Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true -Djava.awt.headless=true"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDateStamps -XX:+PrintGCDetails -Xloggc:$INSIGHT_HOME/logs/gc-%t.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$INSIGHT_HOME/logs/heapdump.hprof"

DEBUG_MODE=true
if [ "$DEBUG_MODE" == "true" ]
then
  JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
fi

rm -rf $INSIGHT_HOME/logs

if [[ ! -d "$INSIGHT_HOME/logs" ]]; then
    mkdir -p $INSIGHT_HOME/logs
fi

java ${JAVA_OPTS} ${SPRING_OPTS} -Dloader.path=$INSIGHT_HOME/lib/ -jar $INSIGHT_HOME/semantic*.jar 2>&1