/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.lib.cvsclient.admin;

/**
 * A class for comparing the file's date and the CVS/Entries date.
 *
 * @author  Thomas Singer
 * @version Sep 29, 2001
 */
public class DateComparator {

    private static final long SECONDS_PER_HOUR = 3600;

    private static final DateComparator singleton = new DateComparator();

    /**
     * Returns an instance of a DateComparator.
     */
    public static DateComparator getInstance() {
        return singleton;
    }

    /**
     * This class is a singleton. There is no need to subclass or
     * instantiate it outside.
     */
    private DateComparator() {
    }

    /**
     * Compares the specified dates, whether they should be treated as equal.
     * Returns true to indicate equality.
     */
    public boolean equals(final long fileTime, final long entryTime) {
        final long fileTimeSeconds = fileTime / 1000;
        final long entryTimeSeconds = entryTime / 1000;
        final long difference = Math.abs(fileTimeSeconds - entryTimeSeconds);
        // differences smaller than 1 second are treated as equal to catch rounding errors
        if (difference < 1) {
            return true;
        }

        // 1-hour-differences (caused by daylight-saving) are treated as equal
        if (difference >= SECONDS_PER_HOUR - 1
                && difference <= SECONDS_PER_HOUR + 1) {
            return true;
        }
        return false;
    }
}
