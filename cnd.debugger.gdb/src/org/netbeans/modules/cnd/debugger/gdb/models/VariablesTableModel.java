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

import javax.swing.JToolTip;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.debugger.gdb.Field;
import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.Variable;

/*
 * VariablesTableModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class VariablesTableModel implements TableModel, Constants {
    
    private GdbDebugger      debugger;
    private ContextProvider  lookupProvider;
    
    public VariablesTableModel(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
    }
    
    public Object getValueAt(Object row, String columnID) throws UnknownTypeException {
        
        if (columnID.equals(LOCALS_TO_STRING_COLUMN_ID) || columnID.equals(WATCH_TO_STRING_COLUMN_ID)) {
            if (row instanceof Variable) {
                return ((Variable) row).getValue();
            }
        } else if (columnID.equals(LOCALS_TYPE_COLUMN_ID) || columnID.equals(WATCH_TYPE_COLUMN_ID)) {
            if (row instanceof Variable) {
                return ((Variable) row).getType();
            }
        } else if ( columnID.equals(LOCALS_VALUE_COLUMN_ID) || columnID.equals(WATCH_VALUE_COLUMN_ID)) {
            if (row instanceof Variable) {
                return ((Variable) row).getValue();
            }
        }
        if (row instanceof JToolTip) {
            row = ((JToolTip) row).getClientProperty("getShortDescrption"); // NOI18N
            if (row instanceof Variable) {
                return ((Variable) row).getType();
            } else if (row == null) {
                return "";
            }
        }
        if (row.toString().startsWith("No current thread")) { // NOI18N
            return NbBundle.getMessage(VariablesTableModel.class, "NoCurrentThreadVar"); // NOI18N
        }
        throw new UnknownTypeException(row);
    }
    
    public boolean isReadOnly(Object row, String columnID) throws UnknownTypeException {
        if (row instanceof Variable) {
            if (columnID.equals(LOCALS_TO_STRING_COLUMN_ID) ||
                    columnID.equals(WATCH_TO_STRING_COLUMN_ID) ||
                    columnID.equals(LOCALS_TYPE_COLUMN_ID) ||
                    columnID.equals(WATCH_TYPE_COLUMN_ID)) {
                return true;
            }
            if (columnID.equals(LOCALS_VALUE_COLUMN_ID) || columnID.equals(WATCH_VALUE_COLUMN_ID)) {
                if (row instanceof AbstractVariable) {
                    return ((AbstractVariable) row).getFieldsCount() != 0;
                } else {
                return true;
                }
            }
        } else if (row.toString().startsWith("No current thread")) { // NOI18N
            return true; // NOI18N
        }
        throw new UnknownTypeException(row);
    }
    
    public void setValueAt(Object row, String columnID, Object value) throws UnknownTypeException {
        if (row instanceof LocalVariable) {
            if (columnID.equals(LOCALS_VALUE_COLUMN_ID) || columnID.equals(WATCH_VALUE_COLUMN_ID)) {
                if (row instanceof GdbWatchVariable) {
                    ((GdbWatchVariable) row).setValueAt((String) value);
                } else {
                    ((LocalVariable) row).setValue((String) value);
                }
                return;
            }
        } else if (row instanceof Field) {
            if (columnID.equals (LOCALS_VALUE_COLUMN_ID) || columnID.equals (WATCH_VALUE_COLUMN_ID)) {
                try {
                    ((Field) row).setValue((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                            e.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(descriptor);
                }
                return;
            }
        }
        throw new UnknownTypeException(row);
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
}
