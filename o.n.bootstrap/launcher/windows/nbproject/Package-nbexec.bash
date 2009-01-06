#!/bin/bash -x

#
# Generated - do not edit!
#

# Macros
TOP=`pwd`
PLATFORM=Cygwin-Windows
TMPDIR=build/nbexec/${PLATFORM}/tmp-packaging
TMPDIRNAME=tmp-packaging
OUTPUT_PATH=nbexec.dll
OUTPUT_BASENAME=nbexec.dll
PACKAGE_TOP_DIR=libwindows.dll/

# Functions
function checkReturnCode
{
    rc=$?
    if [ $rc != 0 ]
    then
        exit $rc
    fi
}
function makeDirectory
# $1 directory path
# $2 permission (optional)
{
    mkdir -p "$1"
    checkReturnCode
    if [ "$2" != "" ]
    then
      chmod $2 "$1"
      checkReturnCode
    fi
}
function copyFileToTmpDir
# $1 from-file path
# $2 to-file path
# $3 permission
{
    cp "$1" "$2"
    checkReturnCode
    if [ "$3" != "" ]
    then
        chmod $3 "$2"
        checkReturnCode
    fi
}

# Setup
cd "${TOP}"
mkdir -p dist/nbexec/${PLATFORM}/package
rm -rf ${TMPDIR}
mkdir -p ${TMPDIR}

# Copy files and create directories and links
cd "${TOP}"
makeDirectory ${TMPDIR}/libwindows.dll/lib
copyFileToTmpDir "${OUTPUT_PATH}" "${TMPDIR}/${PACKAGE_TOP_DIR}lib/${OUTPUT_BASENAME}" 0644


# Generate tar file
cd "${TOP}"
rm -f dist/nbexec/${PLATFORM}/package/libwindows.dll.tar
cd ${TMPDIR}
tar -vcf ../../../../dist/nbexec/${PLATFORM}/package/libwindows.dll.tar *
checkReturnCode

# Cleanup
cd "${TOP}"
rm -rf ${TMPDIR}
