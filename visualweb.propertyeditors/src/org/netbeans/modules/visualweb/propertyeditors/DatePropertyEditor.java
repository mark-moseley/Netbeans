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
package org.netbeans.modules.visualweb.propertyeditors;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openide.util.NbBundle;

import com.sun.rave.designtime.DesignProperty;

/**
 * An in-line property editor for java.util.Date objects. This editor looks for
 * a "dateFormatPattern" property to use to parse and format a Date.
 *
 * @author Edwin Goei
 */
public class DatePropertyEditor extends PropertyEditorBase {

    /**
     * Key used to specify a date format pattern within a property descriptor.
     */
    public final static String DATE_FORMAT_PATTERN = "dateFormatPattern"; //NOI18N

    private DateFormat getDateFormat() {
        // Look for a "dateFormat" property on the component and use that
        DesignProperty patternProp = getDesignProperty().getDesignBean().getProperty(DATE_FORMAT_PATTERN);
        Object patternPropValue = patternProp.getValue();
        if (patternPropValue instanceof String) {
            DateFormat df = new SimpleDateFormat((String) patternPropValue);
            return df;
        } else {
            // Fallback to using the default locale DateFormat
            return DateFormat.getDateInstance(DateFormat.SHORT);
        }
    }

    public String getAsText() {
        Date d = (Date) getValue();
        if (d != null) {
            return getDateFormat().format(d);
        } else {
            return ""; // NOI18N
        }
    }

    public void setAsText(String text) {
        if (text.trim().length() == 0) {
            setValue(null);
            return;
        }

        try {
            setValue(getDateFormat().parse(text));
        } catch (ParseException e) {
            String pattern = NbBundle.getMessage(DatePropertyEditor.class,
                    "DatePropertyEditor.formatErrorMessage",
                    new String[] { text });
            throw new IllegalTextArgumentException(MessageFormat.format(
                    pattern, new String[] { text }), e);
        }
    }

    public String getJavaInitializationString() {
        Date date = (Date) getValue();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // (new GregorianCalendar(year, month0, day)).getTime()
        return "(new java.util.GregorianCalendar(" + cal.get(Calendar.YEAR)
                + ", " + cal.get(Calendar.MONTH) + ", "
                + cal.get(Calendar.DAY_OF_MONTH) + ")).getTime()";
    }
}
