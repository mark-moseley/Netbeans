#!/bin/bash
set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

cd $BASE_DIR
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" -PA -d NBI installer/infra/build

cd NBI

if [ ! -z $NATIVE_MAC_MACHINE ]; then
   ssh $NATIVE_MAC_MACHINE rm -f $MAC_PATH/zip/*
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't remove old bits"
       exit $ERROR_CODE;
   fi
   scp -q -v $DIST/zip/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip
   if [ 1 -eq $ML_BUILD ] ; then
	scp -q -v $DIST/ml/zip/$BASENAME*.zip $NATIVE_MAC_MACHINE:$MAC_PATH/zip-ml
   fi
   ERROR_CODE=$?
   if [ $ERROR_CODE != 0 ]; then
       echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't put the zips"
       exit $ERROR_CODE;
   fi
   ssh $NATIVE_MAC_MACHINE $MAC_PATH/run-mac-installer.sh $ML_BUILD > $MAC_LOG 2>&1 &
fi

bash build.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi

set +x
RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
#Wait for the end of native mac build
while [ $RUNNING_JOBS_COUNT -ge 1 ]; do
    #1 or more jobs
    sleep 10
    jobs > /dev/null
    RUNNING_JOBS_COUNT=`jobs | wc -l | tr " " "\n" | grep -v '^$'`
done
set -x

if [ -d $DIST/ml ]; then
    mv $DIST/installers/ml/* $DIST/ml
    rm -rf $DIST/installers/ml
fi

mv $DIST/installers/* $DIST
rmdir $DIST/installers

#Check if Mac installer was OK, 10 "BUILD SUCCESSFUL" messages should be in Mac log
if [ ! -z $NATIVE_MAC_MACHINE ]; then
    IS_MAC_FAILED=`cat $MAC_LOG | grep "BUILD FAILED" | wc -l | tr " " "\n" | grep -v '^$'`
    IS_MAC_CONNECT=`cat $MAC_LOG | grep "Connection timed out" | wc -l | tr " " "\n" | grep -v '^$'`
    if [ $IS_MAC_FAILED -eq 0 -a $IS_MAC_CONNECT -eq 0 ]; then
        #copy the bits back
        mkdir -p $DIST/bundles
        scp $NATIVE_MAC_MACHINE:$MAC_PATH/installer/mac/dist/* $DIST/bundles
        EXIT_CODE=$?
        if [ $ERROR_CODE != 0 ]; then
            echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
            exit $ERROR_CODE;
        fi    
	if [ 1 -eq $ML_BUILD ] ; then
		scp $NATIVE_MAC_MACHINE:$MAC_PATH/dist/* $DIST/ml/bundles
                EXIT_CODE=$?
                if [ $ERROR_CODE != 0 ]; then
                    echo "ERROR: $ERROR_CODE - Connection to MAC machine $NATIVE_MAC_MACHINE failed, can't get installers"
                    exit $ERROR_CODE;
                fi    
	fi
    else
        tail -100 $MAC_LOG
        echo "ERROR: - Native Mac NBI installers build failed"
        exit 1;
    fi
fi

cd $DIST
bash $DIRNANE/files-info.sh bundles zip
ERROR_CODE=$?
if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
#    exit $ERROR_CODE;
fi

if [ $ML_BUILD == 1 ]; then
    cd $DIST/ml
    bash $DIRNANE/files-info.sh bundles zip
    ERROR_CODE=$?
    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Counting of MD5 sums and size failed"
#        exit $ERROR_CODE;
    fi
fi
