#!/bin/bash

## Script to init GlassFish application server for automated tests.
## If not installed or newer binary is available, it uninstalls GlassFish, 
## and installs it. Then it always stops domain if running, kills all
## related processes, deletes domain and creates new domain.

set -x

###################################################################

# Initialization

AS_ROOT=/hudson/workdir/jobs/trunk/testappsrv
AS_BINARY=/hudson/glassfish-installer-v2-b52.jar
AS_HOME=${AS_ROOT}/glassfish
AS_DOMAIN=domain1
AS_PORT=8080

mkdir -p $AS_ROOT

###################################################################

cleanup() {
    # check status all domains, stop running domains
    for domain in `$AS_HOME/bin/asadmin list-domains | grep running | grep -v not` ; do
        if [ $domain != "running" ]; then
            echo "Stopping domain $domain."
            $AS_HOME/bin/asadmin stop-domain $domain
        fi
    done

    # kill all App server processes
    for pid in `ps -ef | grep $AS_HOME | grep -v grep | awk {'print $2'}` ; do
        kill -9 $pid
    done

    # delete and create new domain
    COUNT=`$AS_HOME/bin/asadmin list-domains | grep $AS_DOMAIN | wc -l`
    if [ "$COUNT" -eq 1 ]; then
        echo "Deleting domain $AS_DOMAIN."
        $AS_HOME/bin/asadmin delete-domain $AS_DOMAIN
    fi
    echo "Creating domain $AS_DOMAIN."
    echo AS_ADMIN_PASSWORD=adminadmin > $AS_HOME/passwd
    echo AS_ADMIN_ADMINPASSWORD=adminadmin >> $AS_HOME/passwd
    echo AS_ADMIN_USERPASSWORD=adminadmin >> $AS_HOME/passwd
    echo AS_ADMIN_MASTERPASSWORD=adminadmin >> $AS_HOME/passwd
    $AS_HOME/bin/asadmin create-domain --adminport 4848 --user admin --savemasterpassword=true --passwordfile $AS_HOME/passwd $AS_DOMAIN
}

###################################################################

uninstall() {
    if [ -x $AS_HOME/bin/uninstall ]; then
        $AS_HOME/bin/uninstall -silent
    fi

    # This is a temp hack as the "uninstall" command is broken
    if [ -d ${AS_HOME} ]; then
        rm -rf ${AS_HOME}
    fi
}

###################################################################

install() {
    # install only if new binary is available
    if [ -f ${AS_HOME}/version ]; then
        AS_VERSION=`cat ${AS_HOME}/version`
        if [ "${AS_BINARY}" = "${AS_VERSION}" ]; then
            return
        fi
    fi

    uninstall

    cd ${AS_ROOT}

    TEMP_DISPLAY="${DISPLAY}"
    unset DISPLAY

    # Creating statefile
    echo "A" > ${AS_ROOT}/sunappserver_statefile
    java -Xmx256m -jar ${AS_BINARY} < ${AS_ROOT}/sunappserver_statefile
    ERROR_CODE=$?
    rm -f ${AS_ROOT}/sunappserver_statefile
    DISPLAY="${TEMP_DISPLAY}"
    export DISPLAY

    if [ $ERROR_CODE != 0 ]; then
        echo "ERROR: $ERROR_CODE - Can't install Glassfish"
        exit $ERROR_CODE;
    fi

    # Setup Application Server
    ant -f ${AS_HOME}/setup.xml -Dinstance.port=${AS_PORT} -Ddomain.name=${AS_DOMAIN}

    echo ${AS_BINARY} > ${AS_HOME}/version
}

############################# MAIN ################################

install
cleanup

############################## END ################################
