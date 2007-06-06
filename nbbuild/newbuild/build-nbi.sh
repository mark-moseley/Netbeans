#!/bin/bash
set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

cd $BASE_DIR
cvs -d :pserver:anoncvs@cvs.netbeans.org:/cvs checkout -D "$CVS_STAMP" -PA -d NBI installer/infra/build/build.sh installer/infra/build/build.properties installer/infra/build/build.xml

cd NBI

unset LD_PRELOAD

bash build.sh
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - NBI installers build failed"
    exit $ERROR_CODE;
fi
