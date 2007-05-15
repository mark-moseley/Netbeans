/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */

package org.netbeans.installer.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Kirill Sorokin
 */
public final class DateUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    public static String getTimestamp() {
        return COMPACT_TIMESTAMP.format(new Date());
    }
    
    public static String getFormattedTimestamp() {
        return DETAILED_TIMESTAMP.format(new Date());
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private DateUtils() {
        // does nothing
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final DateFormat COMPACT_TIMESTAMP =
            new SimpleDateFormat("yyyyMMddHHmmss"); // NOI18N
    
    public static final DateFormat DETAILED_TIMESTAMP =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); // NOI18N
}
