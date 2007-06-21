#
# Gererated Makefile - do not edit!
#
# Edit the Makefile in the project folder instead (../Makefile). Each target
# has a -pre and a -post target defined where you can add custumized code.
#
# This makefile implements configuration specific macros and targets.


# Environment
MKDIR=mkdir
CP=cp
CCADMIN=CCadmin
RANLIB=ranlib
CC=gcc
CCC=g++
CXX=g++
FC=g77

# Include project Makefile
include Makefile

# Object Directory
OBJECTDIR=build/Debug_x64_gnu/GNU-Solaris-x86

# Object Files
OBJECTFILES= \
	${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.common/src/CommonUtils.o \
	${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.unix/src/jni_UnixNativeUtils.o

# C Compiler Flags
CFLAGS=-m64 -fPIC -static-libgcc -shared -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE

# CC Compiler Flags
CCFLAGS=-m64 -fPIC -static-libgcc -shared -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE
CXXFLAGS=-m64 -fPIC -static-libgcc -shared -D_FILE_OFFSET_BITS=64 -D_LARGEFILE_SOURCE

# Fortran Compiler Flags
FFLAGS=

# Link Libraries and Options
LDLIBSOPTIONS=

# Build Targets
.build-conf: ${BUILD_SUBPROJECTS} dist/solaris-amd64.so

dist/solaris-amd64.so: ${OBJECTFILES}
	${MKDIR} -p dist
	${LINK.c} -shared -o dist/solaris-amd64.so -s ${OBJECTFILES} ${LDLIBSOPTIONS} 

${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.common/src/CommonUtils.o: ../.common/src/CommonUtils.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.common/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/solaris -o ${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.common/src/CommonUtils.o ../.common/src/CommonUtils.c

${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.unix/src/jni_UnixNativeUtils.o: ../.unix/src/jni_UnixNativeUtils.c 
	${MKDIR} -p ${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.unix/src
	$(COMPILE.c) -s -I/usr/java/include -I/usr/java/include/solaris -o ${OBJECTDIR}/_ext/home/dl198383/tmp/nbi/engine/native/jnilib/solaris-x86/../.unix/src/jni_UnixNativeUtils.o ../.unix/src/jni_UnixNativeUtils.c

# Subprojects
.build-subprojects:

# Clean Targets
.clean-conf:
	${RM} -r build/Debug_x64_gnu
	${RM} dist/solaris-amd64.so

# Subprojects
.clean-subprojects:

# Enable dependency checking
.KEEP_STATE:
.KEEP_STATE_FILE:.make.state.${CONF}
