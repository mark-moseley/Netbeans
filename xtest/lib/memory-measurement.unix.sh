#!/bin/sh
pid=$1
if [ "$pid" = "" ]
then
    pid=$$
fi
if [ "`uname`" = "Linux" ]
then
#    cat /proc/$pid/status | grep VmSize | sed -e 's/VmSize: *\t* *//' | sed -e 's/ .*//'
     cat /proc/$pid/status | grep VmSize | awk '{print $2}'
elif [ "`uname`" = "SunOS" ]
then
    pmap -x $pid | grep \^total | sed -e 's/.*Kb *//' | sed -e 's/ .*//'
fi

