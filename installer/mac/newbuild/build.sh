#!/bin/bash

# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.

# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.

# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"

# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.

set -x -e 

if [ -z "$1" ] || [ -z "$2" ]|| [ -z "$3" ] || [ -z "$4" ]; then
    echo "usage: $0 zipdir prefix buildnumber ml_build"
    echo ""
    echo "zipdir is the dir which contains the zip modulclusters"
    echo "prefix-buildnumber is the distro filename prefix, e.g. netbeans-hudson-trunk-2464"
    echo "ml_build is 1 if ml builds are requared and 0 if not"
    echo "zipdir should contain <basename>-ide.zip, <basename>-java.zip, <basename>-ruby.zip,..." 
    exit 1
fi

zipmodulclustersdir=$1
prefix=$2
buildnumber=$3
ml_build=$4
ml_postfix=""

instrumentation_options=""
if [ -n "$5" ] && [ -n "$6" ] && [ -n "$7" ] ; then
   echo "INFO : INSTRUMENTED BUILD"
   rm /tmp/nbi_instr.temp
   instrumentation_options="-Dinstrument.jars=true -Demma.sh.file=\"$5\" -Demma.txt.file=\"$6\" -Demma.jar.file=\"$7\" -Demma.out.file=/tmp/nbi_instr.temp"

   if [ -n "$8" ] ; then
	bash_exec="$8"
   else 
	bash_exec=/bin/bash
   fi
   instrumentation_options="$instrumentation_options  -Dbash.executable=\"$bash_exec\""
else 
   echo "INFO : STANDARD BUILD"
fi

if [ 1 -eq $ml_build ] ; then
ml_postfix="-ml"
fi

basename=`dirname "$0"`
. "$basename"/build-private.sh

cd "$basename"
chmod -R a+x *.sh

commonname=$zipmodulclustersdir/$prefix-$buildnumber 

ant -f $basename/build.xml build-all-dmg -Dcommon.name=$commonname -Dprefix=$prefix -Dbuildnumber=$buildnumber -Dml_postfix=$ml_postfix -Dgf_builds_host=$GLASSFISH_BUILDS_HOST -Dopenesb_builds_host=$OPENESB_BUILDS_HOST -Dbinary_cache_host=$BINARY_CACHE_HOST $instrumentation_options

