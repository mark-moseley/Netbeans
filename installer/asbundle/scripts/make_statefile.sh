#!/bin/sh
#
# This script generates a statefile for the Sun App Server (personal edition)
# based on existing standards.  Functionality has now been added to extract
# the wizard IDs from the installers themselves

ErrorExit()
{
	echo ""
	echo ${p}: ERROR: $*
	Usage
	exit 1
}

GetOptions()
{
	# Set defaults

	#JAVA_HOME is expected to be set in environment

	PLATFORM="$FORTE_PORT"
	INSTALL_DIR="$SRCROOT/SUNWappserver"
	STATEFILE="sunappserver_statefile"
	STOREAUTH="FALSE"
	PASSWORD="npoacvubuubealduopqzvanqzva"

	# Parse arguments

	while [ $# -gt 0 -a "$1" != "" ]
	do
		arg=$1; shift

		case $arg in
		-wizard_id)
			if [ $# -eq 0 ]; then
			ErrorExit "-wizard_id requires an argument"
			fi
			WIZARD_ID="$1"
			shift
			;;
		-java_home)
			if [ $# -eq 0 ]; then
			ErrorExit "-java_home requires an argument"
			fi
			JDK_LOC="$1"
			shift
			;;
		-install_dir)
			if [ $# -eq 0 ]; then
			ErrorExit "-install_dir requires an argument"
			fi
			INSTALL_DIR="$1"
			shift
			;;
		-password)
			if [ $# -eq 0 ]; then
			ErrorExit "-password requires an argument"
			fi
			PASSWORD="$1"
			shift
			;;
		-statefile)
			if [ $# -eq 0 ]; then
			ErrorExit "-statefile requires an argument"
			fi
			STATEFILE="$1"
			shift
			;;
                -asinstaller)
                        if [ $# -eq 0 ]; then
                        ErrorExit "-statefile requires an argument"
                        fi
                        ASINSTALLER="$1"
                        shift
                        ;;
		-storeauth)
			STOREAUTH=TRUE
			;;
		*)
			ErrorExit "Unknown option: $arg"
			;;
		esac
	done

}

GetWizardID()
{
set -x

	if [ "${ASINSTALLER}x" = "x" ]; then
		ErrorExit "Unable to find PE installer from $APP_SERVER_LOCATION"
	fi

	CURRDIR=`pwd`
	TMPDIR=/tmp/${p}.$$
	mkdir -p $TMPDIR
	cd $TMPDIR
#	unzip -q $ASINSTALLER appserv.class package/libPassword.so > /dev/null 2>&1
set -x
	unzip -q $ASINSTALLER appserv.class 

	WIZARD_ID=`$JAVA_HOME/bin/java -cp $TMPDIR appserv -id`
	cd $CURRDIR
	rm -rf $TMPDIR
}

Usage()
{

  cat <<EOF

Usage: sh $p

Options:  -wizard_id <wizard_id> - Sets the Wizard ID to the specified value
          -java_home <java_home> - Sets the JDK_LOCATION to the specified location
          -install_dir <dir>     - Sets the location to install the server
          -statefile <file>      - Sets the filename to use for the statefile
          -password <pass>       - Sets the encrypted ASADMIN password
          -storeauth             - Sets STORE_AUTH_ADMIN to true (defaults to FALSE)
          -asinstaller           - Sets location and name of as installer

Description:

This script will generate a statefile for use with automated
installations of the Sun application server.  The hard-coded
default values are expected to change relatively frequently
as the version of the app server changes.

If using the default values, this script assumes that SRCROOT
is set.  If you override with the -install_dir argument,
you do not need to set SRCROOT.

EOF
}


p=`basename $0`

GetOptions "$@"

# If the wizard_id isn't passed on the ocmmand line, go figure it out
if [ "${WIZARD_ID}x" = "x" ]; then
	GetWizardID 
fi

cat << EOF > $STATEFILE
#
# Wizard Statefile generated by ${p}
#              
# Wizard Statefile section for Sun Java System Application Server 
#
[STATE_BEGIN Sun Java System Application Server ${WIZARD_ID}]
defaultInstallDirectory = $INSTALL_DIR
currentInstallDirectory = $INSTALL_DIR
JDK_LOCATION = ${JDK_LOC}
INST_ASADMIN_USERNAME = admin
INST_ASADMIN_PASSWORD = ${PASSWORD}
INST_ASADMIN_PORT = 4848
INST_ASWEB_PORT = 8080
INST_HTTPS_PORT = 8181
STORE_ADMIN_AUTH = ${STOREAUTH}
ADMIN_PASSWORD_ENCRYPTED = FALSE
CREATE_SAMPLES_DOMAIN = FALSE
CREATE_DESKTOP_SHORTCUT = FALSE
[STATE_DONE Sun Java System Application Server ${WIZARD_ID}]
EOF
