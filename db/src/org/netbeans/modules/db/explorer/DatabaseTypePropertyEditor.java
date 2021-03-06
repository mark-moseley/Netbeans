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

package org.netbeans.modules.db.explorer;

import java.awt.*;
import java.beans.*;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/** A property editor for Color class.
*/
public class DatabaseTypePropertyEditor implements PropertyEditor {

    private int[] constants;
    private String[] names;
    private int index;
    private String name;
    private PropertyChangeSupport support;

    public DatabaseTypePropertyEditor() {
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
        support = new PropertyChangeSupport(this);
        constants = new int[] {
            java.sql.Types.ARRAY,
            java.sql.Types.BIGINT,
            java.sql.Types.BINARY,
            java.sql.Types.BIT,
            java.sql.Types.BLOB,
            java.sql.Types.CHAR,
            java.sql.Types.CLOB,
            java.sql.Types.DATE,
            java.sql.Types.DECIMAL,
            java.sql.Types.DISTINCT,
            java.sql.Types.DOUBLE,
            java.sql.Types.FLOAT,
            java.sql.Types.INTEGER,
            java.sql.Types.JAVA_OBJECT,
            java.sql.Types.LONGVARBINARY,
            java.sql.Types.LONGVARCHAR,
            java.sql.Types.NUMERIC,
            java.sql.Types.REAL,
            java.sql.Types.REF,
            java.sql.Types.SMALLINT,
            java.sql.Types.TIME,
            java.sql.Types.TIMESTAMP,
            java.sql.Types.TINYINT,
            java.sql.Types.VARBINARY,
            java.sql.Types.VARCHAR,
            java.sql.Types.OTHER};
        names = new String[] {
            bundle.getString("SQL_ARRAY"), //NOI18N
            bundle.getString("SQL_BIGINT"), //NOI18N
            bundle.getString("SQL_BINARY"), //NOI18N
            bundle.getString("SQL_BIT"), //NOI18N
            bundle.getString("SQL_BLOB"), //NOI18N
            bundle.getString("SQL_CHAR"), //NOI18N
            bundle.getString("SQL_CLOB"), //NOI18N
            bundle.getString("SQL_DATE"), //NOI18N
            bundle.getString("SQL_DECIMAL"), //NOI18N
            bundle.getString("SQL_DISTINCT"), //NOI18N
            bundle.getString("SQL_DOUBLE"), //NOI18N
            bundle.getString("SQL_FLOAT"), //NOI18N
            bundle.getString("SQL_INTEGER"), //NOI18N
            bundle.getString("SQL_JAVA_OBJECT"), //NOI18N
            bundle.getString("SQL_LONGVARBINARY"), //NOI18N
            bundle.getString("SQL_LONGVARCHAR"), //NOI18N
            bundle.getString("SQL_NUMERIC"), //NOI18N
            bundle.getString("SQL_REAL"), //NOI18N
            bundle.getString("SQL_REF"), //NOI18N
            bundle.getString("SQL_SMALLINT"), //NOI18N
            bundle.getString("SQL_TIME"), //NOI18N
            bundle.getString("SQL_TIMESTAMP"), //NOI18N
            bundle.getString("SQL_TINYINT"), //NOI18N
            bundle.getString("SQL_VARBINARY"), //NOI18N
            bundle.getString("SQL_VARCHAR"), //NOI18N
            bundle.getString("SQL_OTHER") //NOI18N
        }; //NOI18N
    }

    public DatabaseTypePropertyEditor(int[] types, String[] titles) {
        support = new PropertyChangeSupport(this);
        constants = types;
        names = titles;
    }

    public Object getValue () {
        return new Integer(constants[index]);
    }

    public void setValue (Object object) {
        ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle");
//        if (!(object instanceof Number)) {
//            String message = MessageFormat.format(bundle.getString("EXC_CannotOperateWith"), new String[] {object.toString()}); // NOI18N
//            throw new IllegalArgumentException(message);
//        }
//        int ii = ((Number)object).intValue ();

//cannot use previous code because of MSSQL ODBC problems - see DriverSpecification.getRow() for more info
        Integer type;       
        try {
            type = new Integer(object.toString());
        } catch (NumberFormatException exc) {
            String message = MessageFormat.format(bundle.getString("EXC_CannotOperateWith"), new String[] {object.toString()}); // NOI18N
            throw new IllegalArgumentException(message);        
        }
        
        int ii = type.intValue();
//end of MSSQL hack
        
        int i;
        int k = constants.length;
        
        for (i = 0; i < k; i++)
            if (constants [i] == ii)
                break;
        
        if (i == k) {
            switch (ii) { //cannot find 'ii' type, try to find it in java.sql.Types
                case -7: name = bundle.getString("SQL_BIT"); break; //NOI18N
                case -6: name = bundle.getString("SQL_TINYINT"); break; //NOI18N
                case 5: name = bundle.getString("SQL_SMALLINT"); break; //NOI18N
                case 4: name = bundle.getString("SQL_INTEGER"); break; //NOI18N
                case -5: name = bundle.getString("SQL_BIGINT"); break; //NOI18N
                case 6: name = bundle.getString("SQL_FLOAT"); break; //NOI18N
                case 7: name = bundle.getString("SQL_REAL"); break; //NOI18N
                case 8: name = bundle.getString("SQL_DOUBLE"); break; //NOI18N
                case 2: name = bundle.getString("SQL_NUMERIC"); break; //NOI18N
                case 3: name = bundle.getString("SQL_DECIMAL"); break; //NOI18N
                case 1: name = bundle.getString("SQL_CHAR"); break; //NOI18N
                case 12: name = bundle.getString("SQL_VARCHAR"); break; //NOI18N
                case -1: name = bundle.getString("SQL_LONGVARCHAR"); break; //NOI18N
                case 91: name = bundle.getString("SQL_DATE"); break; //NOI18N
                case 92: name = bundle.getString("SQL_TIME"); break; //NOI18N
                case 93: name = bundle.getString("SQL_TIMESTAMP"); break; //NOI18N
                case -2: name = bundle.getString("SQL_BINARY"); break; //NOI18N
                case -3: name = bundle.getString("SQL_VARBINARY"); break; //NOI18N
                case -4: name = bundle.getString("SQL_LONGVARBINARY"); break; //NOI18N
                case 0: name = bundle.getString("SQL_NULL"); break; //NOI18N
                case 1111: name = bundle.getString("SQL_OTHER"); break; //NOI18N
                case 2000: name = bundle.getString("SQL_JAVA_OBJECT"); break; //NOI18N
                case 2001: name = bundle.getString("SQL_DISTINCT"); break; //NOI18N
                case 2002: name = bundle.getString("SQL_STRUCT"); break; //NOI18N
                case 2003: name = bundle.getString("SQL_ARRAY"); break; //NOI18N
                case 2004: name = bundle.getString("SQL_BLOB"); break; //NOI18N
                case 2005: name = bundle.getString("SQL_CLOB"); break; //NOI18N
                case 2006: name = bundle.getString("SQL_REF"); break; //NOI18N
                default: name = bundle.getString("SQL_UNKNOWN"); //NOI18N
            }
            index = 0;
        } else {
            index = i;
            name = names [i];
        }

        support.firePropertyChange (null, null, null);
    }

    public String getAsText () {
        return name;
    }

    public void setAsText (String string) throws IllegalArgumentException {
        int i, k = names.length;
        for (i = 0; i < k; i++) if (names [i].equals (string)) break;
        if (i == k) {
            String message = MessageFormat.format(NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle").getString("EXC_CannotFindAsText"), new String[] {string}); // NOI18N
            throw new IllegalArgumentException(message);
        }
        index = i;
        name = names [i];
        return;
    }

    public String getJavaInitializationString () {
        return "" + index; //NOI18N
    }

    public String[] getTags () {
        return names;
    }

    public boolean isPaintable () {
        return false;
    }

    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    public boolean supportsCustomEditor () {
        return false;
    }

    public Component getCustomEditor () {
        return null;
    }

    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener (propertyChangeListener);
    }
}
