#! /bin/sh

# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.

file=$NBCND_RC
prompt="[Enter] "
pgm=true

if [ -x /usr/ucb/echo ]
then
    # Solaris' echo doesn't support the -n option, so use an alaternate echo
    ECHO=/usr/ucb/echo
else
    ECHO=echo
fi

while [ -n "$1" ]
do
    case "$1" in
    -p)
        prompt="$2"
        shift
        ;;

    -f)
        file="$2"
        shift
        ;;

    /*|./*|[a-zA-Z]:/*)
        pgm="$1"
        shift
        break;
        ;;

    *)
        pgm="./$1"
        shift
        break
        ;;
    esac
    shift
done

"$pgm" "$@"
rc=$?

if [ -n "$file" ]
then
    echo $rc > "$file"
fi

$ECHO -n "$prompt"
read a
exit $rc
