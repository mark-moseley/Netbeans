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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.db.dataview.util;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;

import net.java.hulp.i18n.Logger;
import org.netbeans.modules.db.dataview.logger.Localizer;
import org.netbeans.modules.db.dataview.meta.DBException;

/**
 * A {@link DataType}representing a timestamp value.
 * 
 * @author Ahimanikya Satapathy
 */
public class TimestampType  {
    // Irrespective of the JVM's Locale lets pick a Locale for use on any JVM
    private static Logger mLogger = Logger.getLogger(TimestampType.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    public static final Locale LOCALE = Locale.UK;
    private final DateFormat[] TIMESTAMP_PARSING_FORMATS = new DateFormat[]{
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", LOCALE),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", LOCALE),
        new SimpleDateFormat("yyyy-MM-dd", LOCALE),
        new SimpleDateFormat("MM-dd-yyyy", LOCALE),
        new SimpleDateFormat("HH:mm:ss", LOCALE),
        DateFormat.getTimeInstance(DateFormat.SHORT, LOCALE)
    };

    public TimestampType() {
        for (int i = 0; i < TIMESTAMP_PARSING_FORMATS.length; i++) {
            TIMESTAMP_PARSING_FORMATS[i].setLenient(false);
        }
    }

    public Object convert(Object value) throws DBException {
        if (null == value) {
            return null;
        } else if (value instanceof Timestamp) {
            return value;
        } else if (value instanceof String) {
            java.util.Date dVal = null;
            int i = 0;
            while (dVal == null && i < TIMESTAMP_PARSING_FORMATS.length) {
                dVal = TIMESTAMP_PARSING_FORMATS[i].parse((String) value, new ParsePosition(0));
                i++;
            }

            if (dVal == null) {
                 throw new DBException(mLoc.t("LOGR027: Invalid TimeStamp"));
            }
            return new Timestamp(dVal.getTime());
        } else {
                throw new DBException(mLoc.t("LOGR027: Invalid TimeStamp"));
        }
    }

}