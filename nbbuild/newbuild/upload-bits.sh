#!/bin/bash
set -x

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_NIGHTLY_DIRNAME=`pwd`
export BUILD_DESC=trunk-nightly
source init.sh

ssh -p 222 $DIST_SERVER mkdir -p $DIST_SERVER_PATH/.$DATESTAMP
scp -P 222 -q -r -v $DIST/* $DIST_SERVER:$DIST_SERVER_PATH/.$DATESTAMP > $SCP_LOG 2>&1

ssh -p 222 $DIST_SERVER mv $DIST_SERVER_PATH/.$DATESTAMP $DIST_SERVER_PATH/$DATESTAMP

ssh -p 222 $DIST_SERVER rm $DIST_SERVER_PATH/latest.old
ssh -p 222 $DIST_SERVER mv $DIST_SERVER_PATH/latest $DIST_SERVER_PATH/latest.old
ssh -p 222 $DIST_SERVER ln -s $DIST_SERVER_PATH/$DATESTAMP $DIST_SERVER_PATH/latest
