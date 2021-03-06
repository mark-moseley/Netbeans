set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

mkdir -p $NB_ALL
cd  $NB_ALL

if [ -d $DIST ]; then
    rm -rf $DIST
fi

#Clean the leftovers from the last build.
#For more info about "cvspurge" take a look 
#at http://www.red-bean.com/cvsutils/

set +x
for module in `ls | grep -v "CVS"`; do
    cvspurge $module > /dev/null
    ERROR_CODE=$?

    if [ $ERROR_CODE != 0 ]; then
	echo "ERROR: Purge of $module failed - removing it"
	rm -rf $module;
    fi
done
set -x