#!/bin/bash

serviceUser=`id -un`
serviceGroup=`id -gn`
userHome=`eval echo ~$serviceUser`

applDir="$scriptDir"
serviceUserHome="$applDir"
maxShutdownTime=15
pidFile="$WD/work/$serviceNameLo.pid"
javaCommand="java"
javaExe="$JAVA_HOME/bin/$javaCommand"
commandLine="$javaExe $args"

mkdir -p $WD/work/repo
mkdir -p $WD/log

# Makes the file $1 writable by the group $serviceGroup.
function makeFileWritable {
local filename="$1"
   touch $filename || return 1
   chgrp "$serviceGroup" $filename || return 1
   chmod g+w $filename || return 1
   return 0; }

# Returns 0 if the process with PID $1 is running.
function checkProcessIsRunning {
   local pid="$1"
   if [ -z "$pid" -o "$pid" == " " ]; then return 1; fi
   if ps -p "$pid" > /dev/null 2>&1; then return 0; else return 1; fi }

# Returns 0 if the process with PID $1 is our Java service process.
function checkProcessIsOurService {
   local pid="$1"
   if [ -z "$pid" -o "$pid" == " " ]; then return 1; fi
   if [ "$(pgrep -f "$serviceNameLo") == *$pid*" ]; then return 0; else return 1; fi }

# Returns 0 when the service is running and sets the variable $pid to the PID.
function getServicePID {
   if [ ! -f $pidFile ]; then return 1; fi
   pid="$(<$pidFile)"
   checkProcessIsRunning $pid || return 1
   checkProcessIsOurService $pid || return 1
   return 0; }

function startServiceProcess {
   cd $applDir || return 1
   rm -f $pidFile
   makeFileWritable $pidFile || return 1
   makeFileWritable $serviceLogFile || return 1
   cmd="nohup $commandLine >>$serviceLogFile 2>&1 & echo \$! >$pidFile"
   # Don't forget to add -H so the HOME environment variable will be set correctly.
   $SHELL -c "$cmd" || return 1
   sleep 2
   pid="$(<$pidFile)"
   if checkProcessIsRunning $pid; then :; else
      echo -ne "\n$serviceName start failed, see logfile $serviceLogFile."
      return 1
   fi
   return 0; }

function stopServiceProcess {
   kill $pid || return 1
   for ((i=0; i<maxShutdownTime*10; i++)); do
      checkProcessIsRunning $pid
      if [ $? -ne 0 ]; then
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   echo -e "\n$serviceName did not terminate within $maxShutdownTime seconds, sending SIGKILL..."
   kill -s KILL $pid || return 1
   local killWaitTime=15
   for ((i=0; i<killWaitTime*10; i++)); do
      checkProcessIsRunning $pid
      if [ $? -ne 0 ]; then
         rm -f $pidFile
         return 0
         fi
      sleep 0.1
      done
   echo "Error: $serviceName could not be stopped within $maxShutdownTime+$killWaitTime seconds!"
   return 1; }

function startService {
   getServicePID
   if [ $? -eq 0 ]; then echo "$serviceName is already running"; RETVAL=0; return 0; fi
   echo -n "Starting $serviceName... "
   startServiceProcess
   if [ $? -ne 0 ]; then RETVAL=1; echo "failed"; return 1; fi
   echo "started (pid: $pid). See log file $serviceLogFile"
   RETVAL=0
   return 0; }

function stopService {
   getServicePID
   if [ $? -ne 0 ]; then echo "$serviceName is not running"; RETVAL=0; return 0; fi
   echo -n "Stopping $serviceName... "
   stopServiceProcess
   if [ $? -ne 0 ]; then RETVAL=1; echo "failed"; return 1; fi
   echo "stopped (pid: $pid)"
   RETVAL=0
   return 0; }

function checkServiceStatus {
   echo -n "Checking for $serviceName: "
   if getServicePID; then
	echo "running (pid: $pid)"
	RETVAL=0
   else
	echo "stopped"
	RETVAL=3
   fi
   return 0; }

function main {
   RETVAL=0
   case "$1" in
      start)
         startService
         ;;
      stop)
         stopService
         ;;
      restart)
         stopService && startService
         ;;
      status)
         checkServiceStatus
         ;;
      *)
         echo "Usage: $0 {start|stop|restart|status}"
         exit 1
         ;;
      esac
   exit $RETVAL
}

main $1