#!/bin/sh
# 
# The contents of this file are subject to the terms of the Common Development and
# Distribution License (the License). You may not use this file except in compliance
# with the License.
# 
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
# http://www.netbeans.org/cddl.txt.
# 
# When distributing Covered Code, include this CDDL Header Notice in each file and
# include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
# the following below the CDDL Header, with the fields enclosed by brackets []
# replaced by your own identifying information:
# 
#     "Portions Copyrighted [year] [name of copyright owner]"
# 
# The Original Software is NetBeans. The Initial Developer of the Original Software
# is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
# Rights Reserved.
#
# Initializing variables:
#  SYSTEM_LOCALE      - system locale name, e.g. en_US
#  LAUNCHER_LOCALE     - current launcher locale name which language is used for output messages
#  LAUNCHER_FULL_PATH - the full path to the running script
#  CURRENT_DIRECTORY  - current directory
#  LAUNCHER_NAME      - script name
#  LAUNCHER_JAVA      - java to run the jar
#  LAUNCHER_JAVA_EXE  - path to the java.exe to run the jar
#  LAUNCHER_CLASSPATH - classpath to run the jvm
#  EXTRACT_ONLY       - just extract all data and don`t run java
#  LAUNCHER_TEMP          - temporary directory for random subdirectory
#  LAUNCHER_EXTRACT_DIR - directory for extracting data if --extract was specified
#  LAUNCHER_TEMP_RUNNING  - random temporary directory with all extracted data
#  USE_DEBUG_OUTPUT   - use debug output
#  OUTPUT_FILE        - use file for output
#  TEST_JVM_FILE_PATH - path to the TestJVM file
#  FILE_BLOCK_SIZE    - constant value for block size 
#  LAUNCHER_STUB_SIZE - size of the launcher stub itself
#  LAUNCHER_TRACKING_SIZE - size of the start of next extracting file
 
ARG_JAVAHOME="--javahome"
ARG_DEBUG="--debug"
ARG_OUTPUT="--output"
ARG_EXTRACT="--extract"
ARG_JAVA_ARG_PREFIX="-J"
ARG_TEMPDIR="--tempdir"
ARG_CLASSPATHA="--classpath-append"
ARG_CLASSPATHP="--classpath-prepend"
ARG_HELP="--help"
ARG_SILENT="--silent"
ARG_NOSPACECHECK="--nospacecheck"
ARG_LOCALE="--locale"

USE_DEBUG_OUTPUT=0
PERFORM_FREE_SPACE_CHECK=1
SILENT_MODE=0
EXTRACT_ONLY=0
SHOW_HELP_ONLY=0
LOCAL_OVERRIDDEN=0
APPEND_CP=
PREPEND_CP=
LAUNCHER_APP_ARGUMENTS=
LAUNCHER_JVM_ARGUMENTS=
ERROR_OK=0
ERROR_TEMP_DIRECTORY=2
ERROR_TEST_JVM_FILE=3
ERROR_JVM_NOT_FOUND=4
ERROR_JVM_UNCOMPATIBLE=5
ERROR_EXTRACT_ONLY=6
ERROR_INPUTOUPUT=7
ERROR_FREESPACE=8
ERROR_INTEGRITY=9

VERIFY_OK=1
VERIFY_NOJAVA=2
VERIFY_UNCOMPATIBLE=3

MSG_ERROR_JVM_NOT_FOUND="nlu.jvm.notfoundmessage"
MSG_ERROR_USER_ERROR="nlu.jvm.usererror"
MSG_ERROR_JVM_UNCOMPATIBLE="nlu.jvm.uncompatible"
MSG_ERROR_INTEGRITY="nlu.integrity"
MSG_ERROR_FREESPACE="nlu.freespace"
MSG_RUNNING="nlu.running"
MSG_EXTRACTING="nlu.extracting"
MSG_JVM_SEARCH="nlu.jvm.search"
MSG_ARG_JAVAHOME="nlu.arg.javahome"
MSG_ARG_DEBUG="nlu.arg.debug"
MSG_ARG_EXTRACT="nlu.arg.extract"
MSG_ARG_TEMPDIR="nlu.arg.tempdir"
MSG_ARG_CPA="nlu.arg.cpa"
MSG_ARG_CPP="nlu.arg.cpp"
MSG_ARG_HELP="nlu.arg.help"
MSG_USAGE="nlu.msg.usage"

entryPoint() {		
	CURRENT_DIRECTORY=`pwd`
	LAUNCHER_NAME=`echo $0`
	parseCommandLineArguments "$@"
	initializeVariables            
	setLauncherLocale	
	if [ 1 -eq $SHOW_HELP_ONLY ] ; then
		showHelp
	fi
        createTempDirectory
        extractBundledData     
	verifyIntegrity

	if [ 0 -eq $EXTRACT_ONLY ] ; then 
            searchJava
	    executeMainClass
	else 
	    exitProgram $ERROR_OK
	fi
}

parseCommandLineArguments() {
	while [ $# != 0 ]
	do
		case "$1" in
                $ARG_DEBUG)
                        USE_DEBUG_OUTPUT=1;;
		$ARG_NOSPACECHECK)
                        PERFORM_FREE_SPACE_CHECK=0;;
                $ARG_OUTPUT)
			if [ -n "$2" ] ; then
                        	OUTPUT_FILE="$2"
				if [ -f "$OUTPUT_FILE" ] ; then
					# clear output file first
					rm -f "$OUTPUT_FILE" > /dev/null 2>&1
					touch "$OUTPUT_FILE"
				fi
                        	shift
			fi
			;;
		$ARG_HELP)
			SHOW_HELP_ONLY=1
			;;
		$ARG_JAVAHOME)
			if [ -n "$2" ] ; then
				LAUNCHER_JAVA="$2"
				shift
			fi
			;;
		$ARG_TEMPDIR)
			if [ -n "$2" ] ; then
				LAUNCHER_TEMP="$2"
				shift
			fi
			;;
		$ARG_EXTRACT)
			if [ -n "$2" ] ; then
				LAUNCHER_EXTRACT_DIR="$2"
				EXTRACT_ONLY=1
				shift
			fi
			;;
		$ARG_SILENT)
			SILENT_MODE=1
			parseJvmAppArgument "$1"
			;;
		$ARG_LOCALE)
			SYSTEM_LOCALE="$2"
			LOCAL_OVERRIDDEN=1			
			parseJvmAppArgument "$1"
			;;
		$ARG_CLASSPATHP)
			if [ -n "$2" ] ; then
				if [ -z "$PREPEND_CP" ] ; then
					PREPEND_CP="$2"
				else
					PREPEND_CP="$2":"$PREPEND_CP"
				fi
				shift
			fi
			;;
		$ARG_CLASSPATHA)
			if [ -n "$2" ] ; then
				if [ -z "$APPEND_CP" ] ; then
					APPEND_CP="$2"
				else
					APPEND_CP="$APPEND_CP":"$2"
				fi
				shift
			fi
			;;

		*)
			parseJvmAppArgument "$1"
		esac
                shift
	done
}

setLauncherLocale() {        index=0
	if [ 0 -eq $LOCAL_OVERRIDDEN ] ; then		
        	SYSTEM_LOCALE=`echo $LANG | sed "s/\..*//"`
		debug "Setting initial launcher locale from the system : $SYSTEM_LOCALE"
	else	
		debug "Setting initial launcher locale using command-line argument : $SYSTEM_LOCALE"
	fi

	LAUNCHER_LOCALE="$SYSTEM_LOCALE"
	
	if [ -n "$LAUNCHER_LOCALE" ] ; then
		# check if $LAUNCHER_LOCALE is in UTF-8
		if [ 0 -eq $LOCAL_OVERRIDDEN ] ; then
			removeUTFsuffix=`echo "$LAUNCHER_LOCALE" | sed "s/\.UTF-8//"`
			isUTF=`ifEquals "$removeUTFsuffix" "$LAUNCHER_LOCALE"`
			if [ 1 -eq $isUTF ] ; then
				#set launcher locale to the default if the system locale name doesn`t containt  UTF-8
				LAUNCHER_LOCALE=""
			fi
		fi

        	localeChanged=0	
		localeCounter=0
		while [ $localeCounter -lt $LAUNCHER_LOCALES_NUMBER ] ; do		
		    arg=`eval echo "$""LAUNCHER_LOCALE_NAME_$localeCounter"`		
                    if [ -n "$arg" ] ; then 
                        # if not a default locale			
			# $comp length shows the difference between $SYSTEM_LOCALE and $arg
  			# the less the length the less the difference and more coincedence

                        comp=`echo "$SYSTEM_LOCALE" | sed -e "s/^${arg}//"`				
			length1=`awk 'END{ print length(a) }' a="$comp" < /dev/null`
			length2=`awk 'END{ print length(b) }' b="$LAUNCHER_LOCALE" < /dev/null`
                        if [ $length1 -lt $length2 ] ; then	
				# more coincidence between $SYSTEM_LOCALE and $arg than between $SYSTEM_LOCALE and $arg
                                compare=`ifLess "$comp" "$LAUNCHER_LOCALE"`
				
                                if [ 1 -eq $compare ] ; then
                                        LAUNCHER_LOCALE="$arg"
                                        localeChanged=1
                                        debug "... setting locale to $arg"
                                fi
                                if [ -z "$comp" ] ; then
					# means that $SYSTEM_LOCALE equals to $arg
                                        break
                                fi
                        fi   
                    else 
                        comp="$SYSTEM_LOCALE"
                    fi
		    localeCounter=`expr "$localeCounter" + 1`
       		done
		if [ $localeChanged -eq 0 ] ; then 
                	#set default
                	LAUNCHER_LOCALE=""
        	fi
        fi

        
        debug "Final Launcher Locale : $LAUNCHER_LOCALE"
}

ifLess() {
	compare=`awk 'END { if ( a < b ) { print 1 } else { print 0 } }' a="$1" b="$2" < /dev/null`
	echo $compare
}


ifGreater() {
	compare=`awk 'END { if ( a > b ) { print 1 } else { print 0 } }' a="$1" b="$2" < /dev/null`
	echo $compare
}

ifEquals() {
	compare=`awk 'END { if ( a == b ) { print 1 } else { print 0 } }' a="$1" b="$2" < /dev/null`
	echo $compare
}

ifNumber() 
{
	result=0
	if  [ -n "$1" ] ; then 
		num=`echo "$1" | sed 's/[0-9]*//g' 2>/dev/null`
		if [ -z "$num" ] ; then
			result=1
		fi
	fi 
	echo $result
}

ifPathRelative() {
	param="$1"
	removeRoot=`echo "$param" | sed "s/^\\\///" 2>/dev/null`
	echo `ifEquals "$param" "$removeRoot"` 2>/dev/null
}


initializeVariables() {	
	debug "Launcher name is $LAUNCHER_NAME"
	systemName=`uname`
	debug "System name is $systemName"
	isMacOSX=`ifEquals "$systemName" "Darwin"`	
	isSolaris=`ifEquals "$systemName" "SunOS"`
	if [ 1 -eq $isSolaris ] ; then
		POSSIBLE_JAVA_EXE_SUFFIX="$POSSIBLE_JAVA_EXE_SUFFIX_SOLARIS"
	else
		POSSIBLE_JAVA_EXE_SUFFIX="$POSSIBLE_JAVA_EXE_SUFFIX_COMMON"
	fi
	systemInfo=`uname -a 2>/dev/null`
	debug "System Information:"
	debug "$systemInfo"             
	debug ""
	DEFAULT_DISK_BLOCK_SIZE=512
	LAUNCHER_TRACKING_SIZE=$LAUNCHER_STUB_SIZE
	LAUNCHER_TRACKING_SIZE_BYTES=`expr "$LAUNCHER_STUB_SIZE" \* "$FILE_BLOCK_SIZE"`
	getLauncherLocation
}

parseJvmAppArgument() {
        param="$1"
	arg=`echo "$param" | sed "s/^-J//"`
	argEscaped=`escapeString "$arg"`

	if [ "$param" = "$arg" ] ; then
	    LAUNCHER_APP_ARGUMENTS="$LAUNCHER_APP_ARGUMENTS $argEscaped"
	else
	    LAUNCHER_JVM_ARGUMENTS="$LAUNCHER_JVM_ARGUMENTS $argEscaped"
	fi	
}

getLauncherLocation() {
	# if file path is relative then prepend it with current directory
	if [ 1 -eq `ifPathRelative "$LAUNCHER_NAME"` ] ; then
		debug "Running launcher with relative path"
		LAUNCHER_FULL_PATH=`echo $CURRENT_DIRECTORY/$LAUNCHER_NAME | sed 's/\"//g'`			
	else 
		debug "Running launcher with absolute path"
		LAUNCHER_FULL_PATH=$LAUNCHER_NAME
	fi

	LAUNCHER_FULL_PATH=`normalizePath "$LAUNCHER_FULL_PATH"`
	LAUNCHER_DIR=`dirname "$LAUNCHER_FULL_PATH"`
	debug "Full path = $LAUNCHER_FULL_PATH"
}

getLauncherSize() {
	ls -l "$LAUNCHER_FULL_PATH" | awk ' { print $5 }' 2>/dev/null
}

verifyIntegrity() {
	size=`getLauncherSize`
	extractedSize=$LAUNCHER_TRACKING_SIZE_BYTES
	if [ 1 -eq `ifNumber "$size"` ] ; then
		debug "... check integrity"
		debug "... minimal size : $extractedSize"
		debug "... real size    : $size"

        	if [ $size -lt $extractedSize ] ; then
			debug "... integration check FAILED"
			message "$MSG_ERROR_INTEGRITY" `normalizePath "$LAUNCHER_FULL_PATH"`
			exitProgram $ERROR_INTEGRITY
		fi
		debug "... integration check OK"
	fi
}
showHelp() {
	msg0=`message "$MSG_USAGE"`
	msg1=`message "$MSG_ARG_JAVAHOME $ARG_JAVAHOME"`
	msg2=`message "$MSG_ARG_TEMPDIR $ARG_TEMPDIR"`
	msg3=`message "$MSG_ARG_EXTRACT $ARG_EXTRACT"`
	msg4=`message "$MSG_ARG_DEBUG $ARG_DEBUG"`
	msg5=`message "$MSG_ARG_CPA $ARG_CLASSPATHA"`
	msg6=`message "$MSG_ARG_CPP $ARG_CLASSPATHP"`
	msg7=`message "$MSG_ARG_HELP $ARG_HELP"`
	out "$msg0"
	out "$msg1"
	out "$msg2"
	out "$msg3"
	out "$msg4"
	out "$msg5"
	out "$msg6"
	out "$msg7"
	exitProgram $ERROR_OK
}

exitProgram() {
	if [ 0 -eq $EXTRACT_ONLY ] ; then
	    if [ -n "$LAUNCHER_TEMP_RUNNING" ] && [ -d "$LAUNCHER_TEMP_RUNNING" ]; then		
		debug "Removing directory $LAUNCHER_TEMP_RUNNING"
		rm -rf "$LAUNCHER_TEMP_RUNNING" > /dev/null 2>&1
	    fi
	fi
	exit $1
}

debug() {
        if [ $USE_DEBUG_OUTPUT -eq 1 ] ; then
		timestamp=`date '+%Y-%m-%d %H:%M:%S'`
                out "[$timestamp]> $1"
        fi
}

out() {
	
        if [ -n "$OUTPUT_FILE" ] ; then
                printf "%s\n" "$@" >> "$OUTPUT_FILE"
        elif [ 0 -eq $SILENT_MODE ] ; then
                printf "%s\n" "$@"
	fi
}

message() {        
        msg=`getMessage "$@"`
        out "$msg"
}

showTempDirMessage() {
        out "Cannot create temporary directory $1"
        exit $ERROR_TEMP_DIRECTORY
}

createTempDirectory() {
	if [ 0 -eq $EXTRACT_ONLY ] ; then
            if [ -z "$LAUNCHER_TEMP" ] ; then
		if [ 0 -eq $EXTRACT_ONLY ] ; then 
                    SYSTEM_TEMP="/tmp"
                    if [ -d "$SYSTEM_TEMP" ] ; then
                                debug "Using system temp"
                                LAUNCHER_TEMP="/tmp"
                    else
                        debug "Using home dir for temp"
                        LAUNCHER_TEMP="$HOME"
                    fi
		else
		    #extract only : to the curdir
		    LAUNCHER_TEMP="$CURRENT_DIRECTORY"		    
		fi
            fi
            # if temp dir does not exist then try to create it
            if [ ! -d "$LAUNCHER_TEMP" ] ; then
                mkdir -p "$LAUNCHER_TEMP" > /dev/null 2>&1
                if [ $? -ne 0 ] ; then                        
                        showTempDirMessage "$LAUNCHER_TEMP"
                fi
            fi		
            debug "Launcher TEMP ROOT = $LAUNCHER_TEMP"
            subDir=`date '+%u%m%M%S'`
            subDir=`echo ".nbi-$subDir.tmp"`
            LAUNCHER_TEMP_RUNNING="$LAUNCHER_TEMP/$subDir"
	else
	    #extracting to the $LAUNCHER_EXTRACT_DIR
            debug "Launcher Extracting ROOT = $LAUNCHER_EXTRACT_DIR"
	    LAUNCHER_TEMP_RUNNING="$LAUNCHER_EXTRACT_DIR"
	fi

        if [ ! -d "$LAUNCHER_TEMP_RUNNING" ] ; then
                mkdir -p "$LAUNCHER_TEMP_RUNNING" > /dev/null 2>&1
                if [ $? -ne 0 ] ; then                        
                        showTempDirMessage "$LAUNCHER_TEMP_RUNNING"
                fi
        else
                debug "$LAUNCHER_TEMP_RUNNING is directory and exist"
        fi
        debug "Using subdir $LAUNCHER_TEMP_RUNNING for extracting data"
}
extractBundledData() {
        debug "Extracting data..."
	message "$MSG_EXTRACTING"
	debug "Extracting testJVM file data..."
        extractTestJVMFile
	debug "Extracting bundled jars  data..."
        extractJars	
	debug "Extracting bundled JVMs ..."
	extractJVMFiles
        debug "Extracting finished..."
}

setTestJVMClasspath() {
	testjvmname=`basename "$TEST_JVM_PATH"`
	removeClassSuffix=`echo "$testjvmname" | sed 's/\.class$//'`
	notClassFile=`ifEquals "$testjvmname" "$removeClassSuffix"`
		
	if [ -d "$TEST_JVM_PATH" ] ; then
		TEST_JVM_CLASSPATH="$TEST_JVM_PATH"
		debug "... testJVM path is a directory"
	elif [ -L "$TEST_JVM_PATH" ] && [ $notClassFile -eq 1 ] ; then
		TEST_JVM_CLASSPATH="$TEST_JVM_PATH"
		debug "... testJVM path is a link but not a .class file"
	else
		if [ $notClassFile -eq 1 ] ; then
			debug "... testJVM path is a jar/zip file"
			TEST_JVM_CLASSPATH="$TEST_JVM_PATH"
		else
			debug "... testJVM path is a .class file"
			TEST_JVM_CLASSPATH=`dirname "$TEST_JVM_PATH"`
		fi        
	fi
	debug "... testJVM classpath is : $TEST_JVM_CLASSPATH"
}

extractTestJVMFile() {
        TEST_JVM_PATH=`resolvePath $TEST_JVM_FILE_TYPE "$TEST_JVM_FILE_PATH"`
        if [ $TEST_JVM_FILE_TYPE -eq 0 ] ; then	
	    debug "Extracting TestJVM file size=$TEST_JVM_FILE_SIZE to $TEST_JVM_PATH"
	    extractFile "$TEST_JVM_FILE_SIZE" "$TEST_JVM_PATH"
	    debug "... done"
        fi 
	setTestJVMClasspath
        
}

installJVM() {
	jvmFile="$1"
	# install JVM and set LAUNCHER_JAVA_EXE here
}

extractJars() {
        counter=0
	while [ $counter -lt $JARS_NUMBER ] ; do
		JAR_TYPE=`eval echo "$""JAR_TYPE_$counter"`
		if [ $JAR_TYPE -eq 0 ] ; then
			BUNDLED_JAR_NAME=`eval echo "$""JAR_PATH_$counter"`
			BUNDLED_JAR_SIZE=`eval echo "$""JAR_SIZE_$counter"`
            		BUNDLED_JAR_PATH=`resolvePath $JAR_TYPE "$BUNDLED_JAR_NAME"`

	    		debug "Extracting bundled JAR file size=$BUNDLED_JAR_SIZE to $BUNDLED_JAR_PATH"		
            		extractFile "$BUNDLED_JAR_SIZE" "$BUNDLED_JAR_PATH"
			debug "... done"
		fi		
		counter=`expr "$counter" + 1`
	done
}

extractJVMFiles() {
	javaCounter=0
	while [ $javaCounter -lt $JAVA_LOCATION_NUMBER ] ; do		
		fileType=`eval echo "$""JAVA_LOCATION_TYPE_$javaCounter"`		
		if [ $fileType -eq 0 ] ; then 
			# bundled

			JVM_FILE_NAME=`eval echo "$""JAVA_LOCATION_PATH_$jvmCounter"`
			JVM_FILE_SIZE=`eval echo "$""JAVA_LOCATION_SIZE_$jvmCounter"`
	            	JVM_PATH=`resolvePath $fileType "$JVM_FILE_NAME"`
	        
			debug "Extracting JVM file size=$JVM_FILE_SIZE to $JVM_PATH"	    
	            	extractFile "$JVM_FILE_SIZE" "$JVM_PATH"
	        	debug "... extracting JVM done"
		fi
		javaCounter=`expr "$javaCounter" + 1`
	done
}

resolvePath () {
	fileType=$1
	path="$2"
	parent=""
	# resolve here external jar : launcher dir, user dir, javahome dir, etc
	# known types:
	# 0 - bundled
  	# 1 - absolute
	# 2 - from javahome
	# 3 - from userhome
	# 4 - from launcher parent dir
	# 5 - from launcher tmpdir
	#
	case $fileType in
        	0)                        		
			parent="$LAUNCHER_TEMP_RUNNING"
			;;
		1)                        		
			;;
		2)	
			parent="$LAUNCHER_JAVA"
			;;
		3)
			parent="$HOME"
			;;
		4)
			parent="$LAUNCHER_DIR"
			;;
		5)
			parent="$LAUNCHER_TEMP_RUNNING"
			;;
		*)	
			;;
	esac
	if [ -n "$parent" ] ; then
		path="$parent"/"$path"
	fi
	echo "$path"
}

processJarsClasspath() {
	JARS_CLASSPATH=""
	jarsCounter=0
	while [ $jarsCounter -lt $JARS_NUMBER ] ; do
		cpFile=`eval echo "$""JAR_PATH_$jarsCounter"`
		fileType=`eval echo "$""JAR_TYPE_$jarsCounter"`
		debug "Adding external file [$jarsCounter] with type=$fileType to classpath: $cpFile"
		if [ -n "$cpFile" ] ; then			
			resolvedFile=`resolvePath $fileType "$cpFile"`
			
			if [ -z "$JARS_CLASSPATH" ] ; then
				JARS_CLASSPATH="$resolvedFile"
			else
				JARS_CLASSPATH="$JARS_CLASSPATH":"$resolvedFile"
			fi
			
			
		fi
		jarsCounter=`expr "$jarsCounter" + 1`
	done
	debug "Jars classpath : $JARS_CLASSPATH"
}

extractFile() {
        start=$LAUNCHER_TRACKING_SIZE
        size=$1 #absolute size
        name=$2 #relative part
        fullBlocks=`expr $size / $FILE_BLOCK_SIZE`
        fullBlocksSize=`expr "$FILE_BLOCK_SIZE" \* "$fullBlocks"`
        oneBlocks=`expr  $size - $fullBlocksSize`
	oneBlocksStart=`expr "$start" + "$fullBlocks"`

	checkFreeSpace $size "$name"	
	LAUNCHER_TRACKING_SIZE_BYTES=`expr "$LAUNCHER_TRACKING_SIZE" \* "$FILE_BLOCK_SIZE"`

	if [ 0 -eq $diskSpaceCheck ] ; then
		dir=`dirname "$name"`
		message "$MSG_ERROR_FREESPACE" "$dir" "$ARG_TEMPDIR"	
		exitProgram $ERROR_FREESPACE
	fi

        if [ 0 -lt "$fullBlocks" ] ; then
                # file is larger than FILE_BLOCK_SIZE
                dd if="$LAUNCHER_FULL_PATH" of="$name" \
                        bs="$FILE_BLOCK_SIZE" count="$fullBlocks" skip="$start"\
			> /dev/null  2>&1
		LAUNCHER_TRACKING_SIZE=`expr "$LAUNCHER_TRACKING_SIZE" + "$fullBlocks"`
		LAUNCHER_TRACKING_SIZE_BYTES=`expr "$LAUNCHER_TRACKING_SIZE" \* "$FILE_BLOCK_SIZE"`
        fi
        if [ 0 -lt "$oneBlocks" ] ; then
		dd if="$LAUNCHER_FULL_PATH" of="$name.tmp.tmp" bs="$FILE_BLOCK_SIZE" count=1\
			skip="$oneBlocksStart"\
			 > /dev/null 2>&1

		dd if="$name.tmp.tmp" of="$name" bs=1 count="$oneBlocks" seek="$fullBlocksSize"\
			 > /dev/null 2>&1

		rm -f "$name.tmp.tmp"
		LAUNCHER_TRACKING_SIZE=`expr "$LAUNCHER_TRACKING_SIZE" + 1`

		LAUNCHER_TRACKING_SIZE_BYTES=`expr "$LAUNCHER_TRACKING_SIZE_BYTES" + "$oneBlocks"`
        fi
}

searchJava() {
	message "$MSG_JVM_SEARCH"
        if [ ! -f "$TEST_JVM_CLASSPATH" ] && [ ! -h "$TEST_JVM_CLASSPATH" ] && [ ! -d "$TEST_JVM_CLASSPATH" ]; then
                debug "Cannot find file for testing JVM at $TEST_JVM_CLASSPATH"
		message "$MSG_ERROR_JVM_NOT_FOUND" "$ARG_JAVAHOME"
                exitProgram $ERROR_TEST_JVM_FILE
        else
		if [ -z "$LAUNCHER_JAVA_EXE" ] ; then
                	if [ -n "$LAUNCHER_JAVA" ] ; then
                        	verifyJVM "$LAUNCHER_JAVA"
			
				if [ $VERIFY_UNCOMPATIBLE -eq $verifyResult ] ; then
			    		message "$MSG_ERROR_JVM_UNCOMPATIBLE" "$LAUNCHER_JAVA" "$ARG_JAVAHOME"
			    		exitProgram $ERROR_JVM_UNCOMPATIBLE
				elif [ $VERIFY_NOJAVA -eq $verifyResult ] ; then
					message "$MSG_ERROR_USER_ERROR" "$LAUNCHER_JAVA" "$ARG_JAVAHOME"
			    		exitProgram $ERROR_JVM_NOT_FOUND
				fi
                	fi
		fi

		if [ -z "$LAUNCHER_JAVA_EXE" ] ; then
		    # search java in the environment
		
            	    ptr="$POSSIBLE_JAVA_ENV"
            	    while [ -n "$ptr" ] && [ -z "$LAUNCHER_JAVA_EXE" ] ; do
			argJavaHome=`echo "$ptr" | sed "s/:.*//"`
			back=`echo "$argJavaHome" | sed "s/\\\//\\\\\\\\\//g"`
		    	end=`echo "$ptr"       | sed "s/${back}://"`
			argJavaHome=`echo "$back" | sed "s/\\\\\\\\\//\\\//g"`
			ptr="$end"
                        eval evaluated=`echo \\$$argJavaHome` > /dev/null
                        if [ -n "$evaluated" ] ; then
                                debug "EnvVar $argJavaHome=$evaluated"				
                                verifyJVM "$evaluated"
                        fi
            	    done
		fi
		if [ -z "$LAUNCHER_JAVA_EXE" ] ; then
		    # search java in the common system paths
		    javaCounter=0
            	    while [ $javaCounter -lt $JAVA_LOCATION_NUMBER ] && [ -z "$LAUNCHER_JAVA_EXE" ] ; do
		    	argJavaHome=`eval echo "$""JAVA_LOCATION_PATH_$javaCounter"`
		    	fileType=`eval echo "$""JAVA_LOCATION_TYPE_$javaCounter"`
		    	argJavaHome=`resolvePath $fileType "$argJavaHome"`

		    	debug ".. checking location $argJavaHome"
			
			if [ $fileType -eq 0 ] ; then # bundled
				installJVM  "$argJavaHome"
	        	fi

                        verifyJVM "$argJavaHome"

			if [ -z "$LAUNCHER_JAVA_EXE" ] ; then				

				# if path ends with "/" then check children
				tmpArg=`removeEndSlashes "$argJavaHome"`
				if [ `ifEquals "$argJavaHome" "$tmpArg"` -eq 0 ] ; then					
					argJavaHome="$tmpArg"
					if [ -z "$argJavaHome" ] ; then
						argJavaHome="/"
					fi
					# search in all visible child directories
					debug ".. checking all children of $argJavaHome"
					if [ -d "$argJavaHome" ] || [ -L "$argJavaHome" ] ; then
					        debug ".. getting all children ..."
						children=`ls -d -1 "$argJavaHome"/*/ 2> /dev/null`
						children=`removeEndSlashes "$children"`
						child="$children"
						counter=1
						while [ -n "$child" ] && [ -z "$LAUNCHER_JAVA_EXE" ] ; do
							child=`echo "$children" | sed -n "${counter}p"`
							counter=`expr "$counter" + 1`
							if [ -n "$child" ] ; then
				                		debug ".... checking child $child"
								verifyJVM "$child"
							fi					
						done
					fi
				fi
			fi
			javaCounter=`expr "$javaCounter" + 1`
            	    done
		fi
        fi
	if [ -z "$LAUNCHER_JAVA_EXE" ] ; then
		message "$MSG_ERROR_JVM_NOT_FOUND" "$ARG_JAVAHOME"
		exitProgram $ERROR_JVM_NOT_FOUND
	fi
}

normalizePath() {	
	argument="$1"
	# replace all /./ to /
	while [ 0 -eq 0 ] ; do	
		testArgument=`echo "$argument" | sed 's/\/\.\//\//g' 2> /dev/null`
		if [ -n "$testArgument" ] && [ 0 -eq `ifEquals "$argument" "$testArgument"` ] ; then
		    	# something changed
			argument="$testArgument"
		else
			break
		fi	
	done

        # remove /. a the end (if the resulting string is not zero)
	testArgument=`echo "$argument" | sed 's/\/\.$//' 2> /dev/null`
	if [ -n "$testArgument" ] ; then
		argument="$testArgument"
	fi

	# replace more than 2 separators to 1
	testArgument=`echo "$argument" | sed 's/\/\/*/\//g' 2> /dev/null`
	if [ -n "$testArgument" ] ; then
		argument="$testArgument"
	fi
	
	echo "$argument"	
}

resolveSymlink() {  
    pathArg="$1"	
    while [ -h "$pathArg" ] ; do
        ls=`ls -ld "$pathArg"`
        link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    
        if expr "$link" : '^/' 2> /dev/null >/dev/null; then
		pathArg="$link"
        else
		pathArg="`dirname "$pathArg"`"/"$link"
        fi
	pathArg=`normalizePath "$pathArg"` 
    done
    echo "$pathArg"
}

verifyJVM() {                
    javaTryPath=`normalizePath "$1"` 
    verifyJavaHome "$javaTryPath"
    if [ $VERIFY_OK -ne $verifyResult ] ; then
	savedResult=$verifyResult

    	if [ 0 -eq $isMacOSX ] ; then
        	#check private jre
		javaTryPath="$javaTryPath""/jre"
		verifyJavaHome "$javaTryPath"	
    	else
		#check MacOSX Home dir
		javaTryPath="$javaTryPath""/Home"
		verifyJavaHome "$javaTryPath"			
	fi	
	
	if [ $VERIFY_NOJAVA -eq $verifyResult ] ; then
		verifyResult=$savedResult
	fi 
    fi
}

removeEndSlashes() {
 arg="$1"
 tryRemove=`echo "$arg" | sed 's/\/\/*$//' 2>/dev/null`
 if [ -n "$tryRemove" ] ; then
	arg="$tryRemove"
 fi
 echo "$arg"
}

checkJavaHierarchy() {
	# return 0 on no java
	# return 1 on jre
	# return 2 on jdk

	tryJava="$1"
	javaHierarchy=0
	if [ -n "$tryJava" ] ; then
		if [ -d "$tryJava" ] || [ -L "$tryJava" ] ; then # existing directory or a symlink        			
			javaLib="$tryJava"/"lib"
	        
			if [ -d "$javaLib" ] || [ -L "$javaLib" ] ; then
				javaLibDtjar="$javaLib"/"dt.jar"
				if [ -f "$javaLibDtjar" ] || [ -f "$javaLibDtjar" ] ; then
					#definitely JDK as the JRE doesn`t have dt.jar
					javaHierarchy=2				
				else
					#check if we inside JRE
					javaLibJce="$javaLib"/"jce.jar"
					javaLibCharsets="$javaLib"/"charsets.jar"					
					javaLibRt="$javaLib"/"rt.jar"
					if [ -f "$javaLibJce" ] || [ -L "$javaLibJce" ] || [ -f "$javaLibCharsets" ] || [ -L "$javaLibCharsets" ] || [ -f "$javaLibRt" ] || [ -L "$javaLibRt" ] ; then
						javaHierarchy=1
					fi
					
				fi
			fi
		fi
	fi
	if [ 0 -eq $javaHierarchy ] ; then
		debug "... no java there"
	elif [ 1 -eq $javaHierarchy ] ; then
		debug "... JRE there"
	elif [ 2 -eq $javaHierarchy ] ; then
		debug "... JDK there"
	fi
}

verifyJavaHome() { 
    verifyResult=$VERIFY_NOJAVA
    java=`removeEndSlashes "$1"`
    debug "... verify    : $java"    

    java=`resolveSymlink "$java"`    
    debug "... real path : $java"

    checkJavaHierarchy "$java"
	
    if [ 0 -ne $javaHierarchy ] ; then 
	testJVMclasspath=`escapeString "$TEST_JVM_CLASSPATH"`
	testJVMclass=`escapeString "$TEST_JVM_CLASS"`

        pointer="$POSSIBLE_JAVA_EXE_SUFFIX"
        while [ -n "$pointer" ] && [ -z "$LAUNCHER_JAVA_EXE" ]; do
            arg=`echo "$pointer" | sed "s/:.*//"`
	    back=`echo "$arg" | sed "s/\\\//\\\\\\\\\//g"`
	    end=`echo "$pointer"       | sed "s/${back}://"`
	    arg=`echo "$back" | sed "s/\\\\\\\\\//\\\//g"`
	    pointer="$end"
            javaExe="$java/$arg"	    

            if [ -x "$javaExe" ] ; then		

                command="$javaExe -classpath $testJVMclasspath $testJVMclass"

                debug "Executing java verification command..."
		debug "$command"
                output=`eval "$command" 2>/dev/null`
                javaVersion=`echo "$output"   | sed "2d;3d;4d;5d"`
		javaVmVersion=`echo "$output" | sed "1d;3d;4d;5d"`
		vendor=`echo "$output"        | sed "1d;2d;4d;5d"`
		osname=`echo "$output"        | sed "1d;2d;3d;5d"`
		osarch=`echo "$output"        | sed "1d;2d;3d;4d"`

		debug "Java :"
                debug "       executable = {$javaExe}"	
		debug "      javaVersion = {$javaVersion}"
		debug "    javaVmVersion = {$javaVmVersion}"
		debug "           vendor = {$vendor}"
		debug "           osname = {$osname}"
		debug "           osarch = {$osarch}"
		comp=0

		if [ -n "$javaVersion" ] && [ -n "$javaVmVersion" ] && [ -n "$vendor" ] && [ -n "$osname" ] && [ -n "$osarch" ] ; then
		    debug "... seems to be java indeded"
		    subs=`echo "$javaVmVersion" | sed "s/${javaVersion}//;s/${javaVmVersion}//"`
		    if [ -n "$subs" ] ; then
		        javaVersion=`echo "$javaVmVersion" | sed "s/.*${javaVersion}/${javaVersion}/"`
		    fi
		    #remove build number
		    javaVersion=`echo "$javaVersion" | sed 's/-.*$//'`
		    verifyResult=$VERIFY_UNCOMPATIBLE

	            if [ -n "$javaVersion" ] ; then
			debug " checking java version = {$javaVersion}"
			javaCompCounter=0

			while [ $javaCompCounter -lt $JAVA_COMPATIBLE_PROPERTIES_NUMBER ] && [ -z "$LAUNCHER_JAVA_EXE" ] ; do				
				comp=1
				setJavaCompatibilityProperties_$javaCompCounter
				debug "Min Java Version : $JAVA_COMP_VERSION_MIN"
				debug "Max Java Version : $JAVA_COMP_VERSION_MAX"
				debug "Java Vendor      : $JAVA_COMP_VENDOR"
				debug "Java OS Name     : $JAVA_COMP_OSNAME"
				debug "Java OS Arch     : $JAVA_COMP_OSARCH"

				compMin=`ifLess "$javaVersion" "$JAVA_COMP_VERSION_MIN"`
				compMax=`ifGreater "$javaVersion" "$JAVA_COMP_VERSION_MAX"`
				if [ -n "$JAVA_COMP_VERSION_MIN" ] && [ 1 -eq $compMin ] ; then
				    comp=0
				fi
		                if [ -n "$JAVA_COMP_VERSION_MAX" ] && [ 1 -eq $compMax ] ; then
		    	    	    comp=0
		                fi				
				if [ -n "$JAVA_COMP_VENDOR" ] ; then
					debug " checking vendor = {$vendor}, {$JAVA_COMP_VENDOR}"
					subs=`echo "$vendor" | sed "s/${JAVA_COMP_VENDOR}//"`
					if [ `ifEquals "$subs" "$vendor"` -eq 1 ]  ; then
						comp=0
						debug "... vendor incompatible"
					fi
				fi
	
				if [ -n "$JAVA_COMP_OSNAME" ] ; then
					debug " checking osname = {$osname}, {$JAVA_COMP_OSNAME}"
					subs=`echo "$osname" | sed "s/${JAVA_COMP_OSNAME}//"`
					
					if [ `ifEquals "$subs" "$osname"` -eq 1 ]  ; then
						comp=0
						debug "... osname incompatible"
					fi
				fi
				if [ -n "$JAVA_COMP_OSARCH" ] ; then
					debug " checking osarch = {$osarch}, {$JAVA_COMP_OSARCH}"
					subs=`echo "$osarch" | sed "s/${JAVA_COMP_OSARCH}//"`
					
					if [ `ifEquals "$subs" "$osarch"` -eq 1 ]  ; then
						comp=0
						debug "... osarch incompatible"
					fi
				fi
				if [ $comp -eq 1 ] ; then
				        LAUNCHER_JAVA_EXE="$javaExe"
					LAUNCHER_JAVA="$java"
					verifyResult=$VERIFY_OK
		    		fi
				debug "       compatible = [$comp]"
				javaCompCounter=`expr "$javaCompCounter" + 1`
			done
		    fi		    
		fi		
            fi	    
        done
   fi
}

checkFreeSpace() {
	size="$1"
	path=`dirname "$2"`	
	diskSpaceCheck=0

	if [ 0 -eq $PERFORM_FREE_SPACE_CHECK ] ; then
		diskSpaceCheck=1
	else
		# get size of the atomic entry (directory)
		freeSpaceDirCheck="$LAUNCHER_TEMP_RUNNING"/freeSpaceCheckDir
		debug "Checking space in $path (size = $size)"
		mkdir "$freeSpaceDirCheck"
		# POSIX compatible du return size in 1024 blocks
		du --block-size=$DEFAULT_DISK_BLOCK_SIZE "$freeSpaceDirCheck" 1>/dev/null 2>&1
		
		if [ $? -eq 0 ] ; then 
			debug "    getting POSIX du with 512 bytes blocks"
			atomicBlock=`du --block-size=$DEFAULT_DISK_BLOCK_SIZE "$freeSpaceDirCheck" | awk ' { print $A }' A=1 2>/dev/null` 
		else
			debug "    getting du with default-size blocks"
			atomicBlock=`du "$freeSpaceDirCheck" | awk ' { print $A }' A=1 2>/dev/null` 
		fi
		rm -rf "$freeSpaceDirCheck"
	        debug "    atomic block size : [$atomicBlock]"

                isBlockNumber=`ifNumber "$atomicBlock"`
		if [ 0 -eq $isBlockNumber ] ; then
			out "Can\`t get disk block size"
			exitProgram $ERROR_INPUTOUPUT
		fi
		requiredBlocks=`expr \( "$1" / $DEFAULT_DISK_BLOCK_SIZE \) + $atomicBlock` 1>/dev/null 2>&1
		if [ `ifNumber $1` -eq 0 ] ; then 
		        out "Can\`t calculate required blocks size"
			exitProgram $ERROR_INPUTOUPUT
		fi
		# get free block size
		column=4
		df -P --block-size="$DEFAULT_DISK_BLOCK_SIZE" "$path" 1>/dev/null 2>&1
		if [ $? -eq 0 ] ; then 
			# gnu df, use POSIX output
			 availableBlocks=`df -P --block-size="$DEFAULT_DISK_BLOCK_SIZE"  "$path" | sed "1d" | awk ' { print $A }' A=$column 2>/dev/null`
		else 
			# try POSIX output
			df -P "$path" 1>/dev/null 2>&1
			if [ $? -eq 0 ] ; then 
				 debug "    getting POSIX df with 512 bytes blocks"
				 availableBlocks=`df -P "$path" | sed "1d" | awk ' { print $A }' A=$column 2>/dev/null`
			# try  Solaris df from xpg4 
			elif  [ -x /usr/xpg4/bin/df ] ; then 
				 debug "    getting xpg4 df with default-size blocks"
				 availableBlocks=`/usr/xpg4/bin/df -P "$path" | sed "1d" | awk ' { print $A }' A=$column 2>/dev/null`
			# last chance to get free space
			else		
				 debug "    getting df with default-size blocks"
				 availableBlocks=`df "$path" | sed "1d" | awk ' { print $A }' A=$column 2>/dev/null`
			fi
		fi
		debug "    available blocks : [$availableBlocks]"
		if [ `ifNumber "$availableBlocks"` -eq 0 ] ; then
			out "Can\`t get the number of the available blocks on the system"
			exitProgram $ERROR_INPUTOUTPUT
		fi
		
		# compare
                debug "    required  blocks : [$requiredBlocks]"

		if [ $availableBlocks -gt $requiredBlocks ] ; then
			debug "... disk space check OK"
			diskSpaceCheck=1
		else 
		        debug "... disk space check FAILED"
		fi
	fi
}

prepareClasspath() {
    debug "Processing external jars ..."
    processJarsClasspath
 
    LAUNCHER_CLASSPATH=""
    if [ -n "$JARS_CLASSPATH" ] ; then
		if [ -z "$LAUNCHER_CLASSPATH" ] ; then
			LAUNCHER_CLASSPATH="$JARS_CLASSPATH"
		else
			LAUNCHER_CLASSPATH="$LAUNCHER_CLASSPATH":"$JARS_CLASSPATH"
		fi
    fi

    if [ -n "$PREPEND_CP" ] ; then
	debug "Prepending classpath with [$PREPEND_CP]"
	if [ -z "$LAUNCHER_CLASSPATH" ] ; then
		LAUNCHER_CLASSPATH="$PREPEND_CP"		
	else
		LAUNCHER_CLASSPATH="$PREPEND_CP":"$LAUNCHER_CLASSPATH"	
	fi
    fi
    if [ -n "$APPEND_CP" ] ; then
	debug "Appending classpath with [$APPEND_CP]"
	if [ -z "$LAUNCHER_CLASSPATH" ] ; then
		LAUNCHER_CLASSPATH="$APPEND_CP"	
	else
		LAUNCHER_CLASSPATH="$LAUNCHER_CLASSPATH":"$APPEND_CP"	
	fi
    fi
}

prepareArguments() {
    LAUNCHER_JVM_ARGUMENTS="$LAUNCHER_JVM_ARGUMENTS $JVM_ARGUMENTS"
    LAUNCHER_APP_ARGUMENTS="$LAUNCHER_APP_ARGUMENTS $APP_ARGUMENTS"
}

executeMainClass() {
	prepareClasspath
	prepareArguments
	debug "Running main jar..."
	message "$MSG_RUNNING"
	classpathEscaped=`escapeString "$LAUNCHER_CLASSPATH"`
	mainClassEscaped=`escapeString "$MAIN_CLASS"`
	launcherJavaExeEscaped=`escapeString "$LAUNCHER_JAVA_EXE"`
	tmpdirEscaped=`escapeString "$LAUNCHER_TEMP"`
	
	command="$launcherJavaExeEscaped $LAUNCHER_JVM_ARGUMENTS -Djava.io.tmpdir=$tmpdirEscaped -classpath $classpathEscaped $mainClassEscaped $LAUNCHER_APP_ARGUMENTS"

	debug "Running command : $command"
	if [ -n "$OUTPUT_FILE" ] ; then
		#redirect all stdout and stderr from the running application to the file
		eval "$command" >> "$OUTPUT_FILE" 2>&1
	else
		eval "$command"
	fi
	exitCode=$?
	debug "... java process finished with code $exitCode"
	exitProgram $exitCode
}

escapeString() {
	echo "$1" | sed "s/\\\/\\\\\\\/g;s/\ /\\\\ /g;s/\"/\\\\\"/g" # escape spaces & commas
}

getMessage() {
        getLocalizedMessage_$LAUNCHER_LOCALE $@
}

POSSIBLE_JAVA_ENV="JAVA:JAVA_HOME:JAVAHOME:JAVA_PATH:JAVAPATH:JDK:JDK_HOME:JDKHOME:ANT_JAVA:"
POSSIBLE_JAVA_EXE_SUFFIX_SOLARIS="bin/java:bin/sparcv9/java:"
POSSIBLE_JAVA_EXE_SUFFIX_COMMON="bin/java:"
