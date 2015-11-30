#!/bin/bash
WD=$(cd $(dirname "$0")/.. && pwd -P)

DEBUG_OPTS=${DEBUG_OPTS:-"-XX:+HeapDumpOnOutOfMemoryError"}
GC_OPTS=${GC_OPTS:-"-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled"}
MEMORY_OPTS=${MEMORY_OPTS:-"-Xms512m -Xmx4096m"}
JAVA_ARGS="-server -Xbootclasspath/a:$WD/conf -Xbootclasspath/a:$WD/ $GC_OPTS $MEMORY_OPTS $DEBUG_OPTS -Dlog4j.configuration=file:$WD/conf/log4j.properties"
CAMELOT_ARGS="-Dcamelot.wd=$WD -Dplugins.local.repository=$userHome/.m2/repository"

scriptDir=$WD/bin
serviceNameLo="camelot-front"
serviceName="CamelotFront"
serviceLogFile="$WD/log/camelot-front.log"

. $WD/bin/classpath
args="$JAVA_ARGS $CAMELOT_ARGS -cp '$CLASSPATH' org.eclipse.jetty.start.Main $WD/conf/jetty.xml jetty.home=$WD/front"
. $WD/bin/runner

