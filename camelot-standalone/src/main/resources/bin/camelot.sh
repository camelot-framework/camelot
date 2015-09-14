#!/bin/bash
DEBUG_OPTS="$DEBUG_OPTS -XX:+HeapDumpOnOutOfMemoryError"
GC_OPTS="-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled"
MEMORY_OPTS="-XX:MaxPermSize=512m -Xms512m -Xmx4096m"
WD=$(cd $(dirname "$0")/.. && pwd -P)

JAVA_ARGS="-server -Xbootclasspath/a:$WD/conf -Xbootclasspath/a:$WD/ $GC_OPTS $MEMORY_OPTS $DEBUG_OPTS -Dlog4j.configuration=file:$WD/conf/log4j.properties"
CAMELOT_ARGS="-Dcamelot.wd=$WD -Dplugins.local.repository=$userHome/.m2/repository"

scriptDir=$WD/bin
scriptFile=$scriptDir/run-web.sh
serviceNameLo="camelot"
serviceName="Camelot"
serviceLogFile="$WD/log/camelot.log"
args="$JAVA_ARGS $CAMELOT_ARGS -jar camelot.jar $WD/conf/jetty.xml jetty.home=$WD"

. $WD/bin/init.sh

