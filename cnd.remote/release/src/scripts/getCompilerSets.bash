#! /bin/bash
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

VERSION=0.4

# Prepend /usr/bin and /bin so we're ensured that standard Unix commands
# don't get replaced by a non-standard version
OPATH=$PATH
PATH=/usr/bin:/bin:$PATH

declare -a csets=('')
declare i=0

uname=$(type -p uname)
OS=$($uname -s)
ARCH=$($uname -m)

# Now restore the original path, but append a few directories
# where compiler sets may be found.
if [ "$OS" == "SunOS" ]
then
    PATH=$OPATH:/opt/sfw/bin:/usr/sfw/bin:/opt/SUNWspro/bin
else
    PATH=$OPATH:/usr/bin:/bin
fi

# First line read should be platform information
echo "$OS $ARCH"

# Now find the compiler collections from the user's PATH
IFS=:
for f in $PATH
do
    line=
    flavor=
    if [ "${f:0:1}" != "/" ]
    then
	continue	# skip relative directories
    fi

    if [ "$OS" == "SunOS" -a \( -x "$f/cc" -o -x "$f/CC" \) ]
    then
	inv=${f/prod//}/../inventory
	if [ -d "$inv/v17n1" ]
	then
	    line="SunStudio_13;$f"
	    flavor="SunStudio_13;"
	elif [ -d "$inv/v16n1" ]
	then
	    line="SunStudio_12;$f"
	    flavor="SunStudio_12;"
	elif [ -d "$inv/v15n1" ]
	then
	    line="SunStudio_11;$f"
	    flavor="SunStudio_11;"
	elif [ -d "$inv/v14n1" ]
	then
	    line="SunStudio_10;$f"
	    flavor="SunStudio_10;"
	elif [ -d "$inv/v13n1" ]
	then
	    line="SunStudio_9;$f"
	    flavor="SunStudio_9;"
	elif [ -d "$inv/v12n1" ]
	then
	    line="SunStudio_8;$f"
	    flavor="SunStudio_8;"
	else
	    line="SunStudio;$f"
	    flavor="SunStudio;"
	fi

	if [ -x "$f/cc" ]
	then
	    line="$line;cc"
	fi
	if [ -x "$f/CC" ]
	then
	    line="$line;CC"
	fi
	if [ -x "$f/dmake" ]
	then
	    line="$line;dmake"
	fi
        gdb=$(type -p gdb)
        if [ -n "$gdb" ]
        then
            line="$line;gdb=$gdb"
        fi
    elif [ -x "$f/gcc" -o -x "$f/g++" -o -x "$f/cc" -o -x "$f/CC" ]
    then
	line="GNU;$f"
	flavor="GNU;"
	if [ -x "$f/gcc" ]
	then
	    line="$line;gcc"
	fi
	if [ -x "$f/g++" ]
	then
	    line="$line;g++"
	fi
	if [ -x "$f/gdb" ]
	then
	    line="$line;gdb"
        else
            gdb=$(type -p gdb)
            if [ -n "$gdb" ]
            then
                line="$line;gdb=$gdb"
            fi
	fi
	if [ -x "$f/make" -a "$OS" != "SunOS" ]
	then
	    line="$line;make"
	fi
	if [ -x "$f/gmake" -a "$OS" == "SunOS" ]
	then
	    line="$line;gmake"
	fi
    fi
    if [ -n "$line" ]
    then
	found=
	len=${#flavor}
	for ((j=0; $j<$i; j=$j+1))
	do
	    # check the flavor of ${cset[$j]} and skip if its a duplicate
	    cpart="${cset[$j]}"
	    if [ "${cpart:0:$len}" == "$flavor" ]
	    then
		found=true
		break
	    fi
	done
	if [ -z "$found" ]
	then
	    cset[$i]=$line
	    ((i=$i+1))
	fi
    fi
done

# Print the set of compiler collections, one per line
if [ -n "${cset[*]}" ]
then
    for ((j=0; $j<$i; j=$j+1))
    do
	echo "${cset[$j]}"
    done
fi
