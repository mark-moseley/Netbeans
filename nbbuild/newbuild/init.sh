#!/bin/bash
set -x

#Initialize all the environment

#ML BUILD yes/no 1/0
if [ -z ${ML_BUILD} ]; then
    export ML_BUILD=1
fi
if [ -z ${LOCALES} ]; then
    export LOCALES=ja,zh_CN,pt_BR
fi

if [ -z ${UPLOAD_ML} ]; then
    export UPLOAD_ML=0
fi

if [ -z ${UPLOAD_JDK} ]; then
    export UPLOAD_JDK=0
fi

export ANT_OPTS="-Xmx512m"
export JAVA_HOME=$JDK_HOME

if [ -z ${DATESTAMP} ]; then
    export DATESTAMP=`date -u +%Y%m%d%H%M`
fi

BUILDNUM=$BUILD_DESC-$DATESTAMP
BUILDNUMBER=$DATESTAMP

if [ -z $BASE_DIR ]; then
    echo BASE_DIR variable not defined, using the default one: /space/NB-IDE
    echo if you want to use another base dir for the whole build feel free
    echo to define a BASE_DIR variable in your environment
    
    export BASE_DIR=/space/NB-IDE
fi

if [ -z $DIST_SERVER ]; then
    echo DIST_SERVER not defined: Upload will no work
fi

if [ -z $DIST_SERVER_PATH ]; then
    echo DIST_SERVER_PATH not defined using default
    DIST_SERVER_PATH=/releng/www/netbeans/6.0/nightly
fi

if [ -z $NB_ALL ]; then
    NB_ALL=$BASE_DIR/main
fi

DIST=$BASE_DIR/dist
LOGS=$DIST/logs
BASENAME=netbeans-$BUILDNUM
export BASENAME_PREFIX=netbeans-$BUILD_DESC

mkdir -p $DIST/zip
mkdir -p $LOGS

#LOGS
CVS_CHECKOUT_LOG=$LOGS/$BASENAME-cvs-checkout.log
IDE_BUILD_LOG=$LOGS/$BASENAME-build-ide.log
MOBILITY_BUILD_LOG=$LOGS/$BASENAME-build-mobility.log
VISUALWEB_BUILD_LOG=$LOGS/$BASENAME-build-visualweb.log
UML_BUILD_LOG=$LOGS/$BASENAME-build-uml.log
SOA_BUILD_LOG=$LOGS/$BASENAME-build-soa.log
RUBY_BUILD_LOG=$LOGS/$BASENAME-build-ruby.log
NBMS_BUILD_LOC=$LOGS/$BASENAME-build-nbms.log
SCP_LOG=$LOGS/$BASENAME-scp.log
MAC_LOG=$LOGS/$BASENAME-native_mac.log
MAC_LOG_NEW=$LOGS/$BASENAME-native_mac_new.log
INSTALLER_LOG=$LOGS/$BASENAME-installers.log
VISUALWEB_SANITY_LOG=$LOGS/$BASENAME-sanity-visualweb.log
