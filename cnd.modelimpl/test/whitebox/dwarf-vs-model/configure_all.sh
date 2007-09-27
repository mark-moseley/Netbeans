#!/bin/bash

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
# particular file as subject to the "Classpath" exception as provided
# by Sun in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#

function main {

	script_dir="../scripts"
	
	s_projects="Freeway ddd-3.3.11 mico-2.3.10 mysql-5.0.18 Python-2.4 clucene-0.9.10"
	# + boost_1_33_1
	
	i=0
	for P in ${s_projects}; do
		i=`expr $i + 1`
		projects[$i]=$P
	done
	
	cnt=${#projects[*]}
	
	root=${TESTCODE}
	
	if [ ! -d ${root} ]; then
		echo "Directory ${root} does not exist"
		return
	fi
	
	i=0;
	while [ $i -lt ${cnt} ]; do
		i=`expr $i + 1`
		dir="${root}/${projects[i]}"
		echo "==================== Configuring ${dir} ===================="
		(
			cd ${dir}
			#if [ "${projects[i]}" == "mysql-5.0.18" ]; then
			#	./configure --with-ndbcluster CFLAGS="-g3 -gdwarf-2" CXXFLAGS="-g3 -gdwarf-2"
			#else
			if [ "${projects[i]}" == "perl-5.8.5" ]; then
				./configure.gnu
			else
				./configure
			fi
			#fi
		)
	done

}

main $@

