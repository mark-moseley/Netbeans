#!/bin/sh

# 
#                 Sun Public License Notice
# 
# The contents of this file are subject to the Sun Public License
# Version 1.0 (the "License"). You may not use this file except in
# compliance with the License. A copy of the License is available at
# http://www.sun.com/
# 
# The Original Code is NetBeans. The Initial Developer of the Original
# Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
# Microsystems, Inc. All Rights Reserved.
# 

#set -x

#
# It is used for generating xml/*/l10n.list files.
#
# Run it from xml/.. directory.
#

# reset old list
L10N_LIST="xml/l10n.list"
echo -n > $L10N_LIST

for module in api catalog compat core css tax text-edit tools tree-edit xsl; do
    MODULE_HOME="xml/${module}"
    MODULE_L10N_LIST="${MODULE_HOME}/l10n.list"
    MODULE_L10N_LIST_TEMP="${MODULE_L10N_LIST}.temp"

    rm -f ${MODULE_L10N_LIST_TEMP}

    ## src
    for sub in / /compat/ /lib/ /lib/compat/; do
        MODULE_SRC=xml/${module}${sub}src
        if [ -e $MODULE_SRC ]; then
            find $MODULE_SRC -name "Bundle.properties" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.html" | grep -v "_ja.html" | grep -v "package.html" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.template" | grep -v "_ja.template" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "mf-layer.xml" | grep -v "mf-layer_ja.xml" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.xml" | grep -v "_ja.xml" | grep "/templates/" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.xsl" | grep -v "_ja.xsl" | grep "/templates/" >> ${MODULE_L10N_LIST_TEMP}
        fi
    done
    
    ## javahelp
    MODULE_JAVAHELP=$MODULE_HOME/javahelp
    if [ -e $MODULE_JAVAHELP ]; then
        for ext in html gif xml hs jhm; do
            echo "$MODULE_JAVAHELP/org/netbeans/modules/xml/$module/docs/**/*.$ext" >> ${MODULE_L10N_LIST_TEMP}
        done
    fi

    ## sort
    cat ${MODULE_L10N_LIST_TEMP} | sort > ${MODULE_L10N_LIST}
    rm ${MODULE_L10N_LIST_TEMP}

#    find xml/${module} -name "Bundle.properties" | sort > ${MODULE_L10N_LIST}
#    find xml/${module} -name "*.html" | grep -v "_ja.html" | grep -v "package.html" | sort >> ${MODULE_L10N_LIST}

    cat ${MODULE_L10N_LIST} >> $L10N_LIST
done
