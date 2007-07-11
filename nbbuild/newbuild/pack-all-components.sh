set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

pack_component() 
{
    dist=$1
    base_name=$2
    component=$3
    filter=$4
    zip -q -r $dist/$base_name-$component.zip $filter
#    gtar cvzf $dist/targz/$base_name-$component.tar.gz $filter
#    gtar cvjf $dist/tarbz2/$base_name-$component.tar.bz2 $filter
}

###################################################################
#
# Pack all the components
#
###################################################################

cd $NB_ALL/nbbuild

#Pack the distrubutions
find netbeans | egrep -v "netbeans/(extra|testtools|cnd)" | zip -q $DIST/zip/$BASENAME-full.zip -@
find netbeans | egrep -v "netbeans/(extra|testtools|mobility|enterprise|visualweb|uml|ruby|cnd|soa|identity)" | zip -q $DIST/zip/$BASENAME-basic.zip -@
find netbeans | egrep -v "netbeans/(extra|testtools|uml|ruby|cnd|soa)" | zip -q $DIST/zip/$BASENAME-standard.zip -@

mkdir $DIST/zip/moduleclusters

#Pack all the NetBeans
pack_component $DIST/zip/moduleclusters $BASENAME all-in-one netbeans

cd $NB_ALL/nbbuild/netbeans

#Continue with individual component
pack_component $DIST/zip/moduleclusters $BASENAME uml "uml*"
rm -rf uml*

pack_component $DIST/zip/moduleclusters $BASENAME visualweb "visualweb*"
rm -rf visualweb*

pack_component $DIST/zip/moduleclusters $BASENAME ruby "ruby*"
rm -rf ruby*

pack_component $DIST/zip/moduleclusters $BASENAME profiler "profiler*"
rm -rf profiler*

pack_component $DIST/zip/moduleclusters $BASENAME platform "platform*"
rm -rf platform*

pack_component $DIST/zip/moduleclusters $BASENAME mobility "mobility*"
rm -rf mobility*

pack_component $DIST/zip/moduleclusters $BASENAME ide "ide*"
rm -rf ide*

pack_component $DIST/zip/moduleclusters $BASENAME xml "xml*"
rm -rf xml*

pack_component $DIST/zip/moduleclusters $BASENAME harness "harness*"
rm -rf harness*

pack_component $DIST/zip/moduleclusters $BASENAME enterprise "enterprise*"
rm -rf enterprise*

pack_component $DIST/zip/moduleclusters $BASENAME soa "soa*"
rm -rf soa*

pack_component $DIST/zip/moduleclusters $BASENAME identity "identity*"
rm -rf identity*

pack_component $DIST/zip/moduleclusters $BASENAME apisupport "apisupport*"
rm -rf apisupport*

pack_component $DIST/zip/moduleclusters $BASENAME java "java*"
rm -rf java*

pack_component $DIST/zip/moduleclusters $BASENAME cnd "cnd*"
rm -rf cnd*

pack_component $DIST/zip/moduleclusters $BASENAME nb6.0-etc "*"
