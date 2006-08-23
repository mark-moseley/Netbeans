#!/bin/bash 

# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
# 
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
# 
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.

function classpath() {

    local nbdist=${NBDIST-"../../../../latest-netbeans"}
    local cnddist=${CNDDIST-"../suite/build/cluster/"}

    CP=""

    local ide
    if [ -d "${nbdist}/ide6" ]; then
	ide="${nbdist}/ide6"
    else 
	if [ -d "${nbdist}/ide7" ]; then
	    ide="${nbdist}/ide7"
	else 
	    if [ -d "${nbdist}/ide8" ]; then
		ide="${nbdist}/ide8"
	    else 
		echo "Can not find ide subdirectory in Netbeans"
		return
	    fi
	fi
    fi

    local platform
    if [ -d "${nbdist}/platform6" ]; then
	platform="${nbdist}/platform6"
    else 
	if [ -d "${nbdist}/platform7" ]; then
	    platform="${nbdist}/platform7"
	else
	    echo "Can not find platform subdirectory in Netbeans"
	    return
	fi
    fi
    

    CP=${CP}:${ide}/modules/org-netbeans-modules-projectuiapi.jar
    CP=${CP}:${ide}/modules/org-netbeans-modules-projectapi.jar
    CP=${CP}:${platform}/lib/org-openide-util.jar
    CP=${CP}:${platform}/modules/org-openide-nodes.jar
    CP=${CP}:${platform}/core/org-openide-filesystems.jar
    CP=${CP}:${platform}/modules/org-openide-loaders.jar
    CP=${CP}:${platform}/lib/org-openide-modules.jar


    CP=${CP}:${cnddist}/modules/org-netbeans-modules-cnd-api-model.jar
    CP=${CP}:${cnddist}/modules/org-netbeans-modules-cnd-modelimpl.jar
    CP=${CP}:${cnddist}/modules/org-netbeans-modules-cnd-antlr.jar
    CP=${CP}:${cnddist}/modules/org-netbeans-modules-cnd.jar

    local error=""

    for F in `echo ${CP} | awk -F: '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	if [ ! -r ${F} ]; then
	    echo "File ${F} doesn't exist"
	    error="y"
	fi
    done

    if [ -n "${error}" ]; then
	CP=""
    else
	#print classpath
	echo "Using classpath:"
	for F in `echo ${CP} | awk -F: '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	    echo $F
	done
    fi
}

function params() {

    DEFS=""
    PARAMS=""

    while [ -n "$1" ]
    do
	case "$1" in
	    -J*)
		    DEFS="${DEFS} ${1#-J}"
		    ;;
	    --nb)
		    shift
		    echo "Using NB from $1"
		    NBDIST=$1
		    ;;
	    --cnd)
		    shift
		    echo "Using NB from $1"
		    CNDDIST=$1
		    ;;
	    -debug|--debug)
		    echo "debugging on port 5858"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5858"
		    ;;
	    --sdebug|-sdebug)
		    echo "wait to attach debugger on port 5858"
		    DEBUG_PROFILE="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5858"
		    ;;
	    --profile|-profile)
		    echo "profile on port 5140"
		    DEBUG_PROFILE="-agentpath:/opt/netbeans/5.0/profiler1/lib/deployed/jdk15/solaris-i386/libprofilerinterface.so=/opt/netbeans/5.0/profiler1/lib,5140"
		    ;;
	    --yprofile|-yprofile)
		    echo "profile using YourKit Profiler"
		    DEBUG_PROFILE="-agentlib:yjpagent"
		    ;;
	    *)
		    PARAMS="${PARAMS} $1"
		    ;;
	esac
	shift
    done

}

function main() {

    params $@

    classpath
    if [ -z "${CP}" ]; then
	echo "Can't find some necessary jars"
	return
    fi

    #DEFS="${DEFS} -Dcnd.modelimpl.trace=true"
    #DEFS="${DEFS} -Dparser.cache=true"
    DEFS="${DEFS} -Dparser.report.errors=true"
    DEFS="${DEFS} -Dparser.report.include.failures=true"
    #DEFS="${DEFS} -Dcnd.parser.trace=true"
    #DEFS="${DEFS} -Dlexer.tracepp=true"
    #DEFS="${DEFS} -Dppexpr.showtree=true"
    #DEFS="${DEFS} -Dlexer.tracecallback=true"
    #DEFS="${DEFS} -Dlexer.honest=true"
    #DEFS=""
    #DEFS="${DEFS} -Dcnd.modelimpl.c.define=true"
    #DEFS="${DEFS} -Dcnd.modelimpl.c.include=true"
    DEFS="${DEFS} -Dcnd.modelimpl.cpp.define=true"
    DEFS="${DEFS} -Dcnd.modelimpl.cpp.include=true"

    #includes
    INCL=""

    INCL="${INCL} -I/usr/local/include"
    INCL="${INCL} -I/usr/lib/gcc-lib/i586-suse-linux/3.3.4/include"
    INCL="${INCL} -I/usr/i586-suse-linux/include"
    INCL="${INCL} -I/usr/include"
    INCL="${INCL} -I/usr/X11R6/include"

    #INCL="${INCL} -I/usr/include/linux"
    #INCL="${INCL} -I/usr/include/gnu"
    #INCL="${INCL} -I/usr/include/g++"
    #INCL="${INCL} -I/usr/include/g++/backward"
    #INCL=""
    #INCL="${INCL} -I/home/vv159170/devarea/dwarf_res/Python-2.4/Include/"
    #INCL="${INCL} -I/home/vv159170/devarea/dwarf_res/Python-2.4"
    INCL="${INCL} -I.. -I."
    INCL=""

    #DEFS="${DEFS} -Dcnd.modelimpl.statistics=true"
    STAT=""
    STAT="${STAT} -s/tmp/stat/GLOBTRACEMODEL.stat"
    STAT="${STAT} -S/tmp/stat"
    STAT=""

    MAIN="org.netbeans.modules.cnd.modelimpl.trace.TraceModel"

    #set -x
    java -server ${DEBUG_PROFILE} -cp ${CP} ${DEFS} ${MAIN} ${STAT} ${INCL} ${PARAMS}
}

main $@
