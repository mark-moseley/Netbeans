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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.awt.Color;
import java.util.Map;
import java.util.WeakHashMap;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;



/**
 * Filters some original tree of nodes (represented by {@link TreeModel}).
 *
 * @author   Jan Jancura and Gordon Prieur
 */
public class BoldVariablesTableModelFilterFirst implements TableModelFilter, Constants {
    
    private Map variableToValueType = new WeakHashMap();
    private Map variableToValueValue = new WeakHashMap();
    private Map variableToValueToString = new WeakHashMap();
    
    public Object getValueAt(TableModel original, Object row, String columnID)
                            throws UnknownTypeException {
        Object result = original.getValueAt(row, columnID);
        if ( columnID.equals(LOCALS_TYPE_COLUMN_ID) || columnID.equals(WATCH_TYPE_COLUMN_ID)) {
            return bold(row, (String) result, variableToValueType);
        }
        if (columnID.equals (LOCALS_VALUE_COLUMN_ID) || columnID.equals (WATCH_VALUE_COLUMN_ID)) {
            return bold(row, (String) result, variableToValueValue);
        }
        if (columnID.equals (LOCALS_TO_STRING_COLUMN_ID) || columnID.equals (WATCH_TO_STRING_COLUMN_ID)) {
            return bold(row, (String) result, variableToValueToString);
        }
        return result;
    }
    
    public boolean isReadOnly(TableModel original, Object row, String columnID)
                            throws UnknownTypeException {
        return original.isReadOnly(row, columnID);
    }
    
    public void setValueAt(TableModel original, Object row, String columnID, Object value)
                            throws UnknownTypeException {
        original.setValueAt(row, columnID, value);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
    }
    
    private String bold(Object variable, String value, Map map) {
        if (map.containsKey(variable)) {
            String oldValue = (String) map.get(variable);
            if (oldValue == value || oldValue != null && oldValue.equals(value)) {
                return toHTML(value, false, false, null);
            }
            map.put(variable, value);
            return toHTML(value, true, false, null);
        } else {
            map.put(variable, value);
            return toHTML(value, false, false, null);
        }
    }
    
    public static String toHTML(String text, boolean bold,  boolean italics, Color color) {
        if (text == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append("<html>"); // NOI18N
        if (bold) {
            sb.append("<b>"); // NOI18N
        }
        if (italics) {
            sb.append("<i>"); // NOI18N
        }
        if (color != null) {
            sb.append("<font color="); // NOI18N
            sb.append(Integer.toHexString ((color.getRGB () & 0xffffff)));
            sb.append(">"); // NOI18N
        }
        text = text.replaceAll("&", "&amp;"); // NOI18N
        text = text.replaceAll("<", "&lt;"); // NOI18N
        text = text.replaceAll(">", "&gt;"); // NOI18N
        sb.append(text);
        if (color != null) {
            sb.append("</font>"); // NOI18N
        }
        if (italics) {
            sb.append("</i>"); // NOI18N
        }
        if (bold) {
            sb.append("</b>"); // NOI18N
        }
        sb.append("</html>"); // NOI18N
        return sb.toString();
    }
}
