/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.util.WeakHashMap;
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
//import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
//import org.netbeans.modules.cnd.debugger.gdb.ObjectVariable;
//import org.netbeans.modules.cnd.debugger.gdb.Super;
//import org.netbeans.modules.cnd.debugger.gdb.This;
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
    
    public Object getValueAt(Object row, String columnID) throws
            UnknownTypeException {
        
        if ( columnID.equals(LOCALS_TO_STRING_COLUMN_ID) ||
                columnID.equals(WATCH_TO_STRING_COLUMN_ID)
                ) {
            /*NM TEMPORARY COMMENTED OUT
            if (row instanceof Super)
                return "";
            else
             
            if (row instanceof ObjectVariable) {
                try {
                    return ((ObjectVariable) row).getToStringValue ();
                } catch (InvalidExpressionException ex) {
                    return getMessage (ex);
                }
            }
            NM*/
            if (row instanceof Variable)
                return ((Variable) row).getValue();
        } else
            if ( columnID.equals(LOCALS_TYPE_COLUMN_ID) ||
                columnID.equals(WATCH_TYPE_COLUMN_ID)
                ) {
            if (row instanceof Variable)
                return getShort(((Variable) row).getType());
            } else
                if ( columnID.equals(LOCALS_VALUE_COLUMN_ID) ||
                columnID.equals(WATCH_VALUE_COLUMN_ID)
                ) {
            /*NM TEMPORARY COMMENTED OUT
            if (row instanceof JPDAWatch) {
                JPDAWatch w = (JPDAWatch) row;
                String e = w.getExceptionDescription ();
                if (e != null)
                    return ">" + e + "<";
                return w.getValue ();
            }
            NM*/
            if (row instanceof Variable)
                return ((Variable) row).getValue();
                }
        if (row.toString().startsWith("No current thread")) { // NOI18N
            return NbBundle.getMessage(VariablesTableModel.class, "NoCurrentThreadVar"); // NOI18N
        }
        throw new UnknownTypeException(row);
    }
    
    public boolean isReadOnly(Object row, String columnID) throws
            UnknownTypeException {
        if (row instanceof Variable) {
            if ( columnID.equals(LOCALS_TO_STRING_COLUMN_ID) ||
                    columnID.equals(WATCH_TO_STRING_COLUMN_ID) ||
                    columnID.equals(LOCALS_TYPE_COLUMN_ID) ||
                    columnID.equals(WATCH_TYPE_COLUMN_ID)
                    ) return true;
            if ( columnID.equals(LOCALS_VALUE_COLUMN_ID) ||
                    columnID.equals(WATCH_VALUE_COLUMN_ID)
                    ) {
                /*NM TEMPORARY COMMENTED OUT
                if (row instanceof This)
                    return true;
                 */
                if ( row instanceof LocalVariable ||
                     row instanceof Field /*NM ||
                     row instanceof JPDAWatch */
                        )
                    return !debugger.canBeModified();
                return true;
            }
        }
        if (row.toString().startsWith("No current thread")) { // NOI18N
            return true; // NOI18N
        }
        throw new UnknownTypeException(row);
    }
    
    public void setValueAt(Object row, String columnID, Object value)
    throws UnknownTypeException {
        if (row instanceof LocalVariable) {
            if (columnID.equals(LOCALS_VALUE_COLUMN_ID)) {
                try {
                    ((LocalVariable) row).setValue((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor =
                            new NotifyDescriptor.Message(
                            e.getLocalizedMessage(),
                            NotifyDescriptor.WARNING_MESSAGE
                            );
                    DialogDisplayer.getDefault().notify(descriptor);
                }
                return;
            }
        }
        if (row instanceof Field) {
            if ( columnID.equals (LOCALS_VALUE_COLUMN_ID) ||
                 columnID.equals (WATCH_VALUE_COLUMN_ID)
            ) {
                try {
                    ((Field) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor =
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (),
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        /*NM TEMPORARY COMMENTED OUT
        if (row instanceof JPDAWatch) {
            if ( columnID.equals (LOCALS_VALUE_COLUMN_ID) ||
                 columnID.equals (WATCH_VALUE_COLUMN_ID)
            ) {
                try {
                    ((JPDAWatch) row).setValue ((String) value);
                } catch (InvalidExpressionException e) {
                    NotifyDescriptor.Message descriptor =
                        new NotifyDescriptor.Message (
                            e.getLocalizedMessage (),
                            NotifyDescriptor.WARNING_MESSAGE
                        );
                    DialogDisplayer.getDefault ().notify (descriptor);
                }
                return;
            }
        }
        NM*/
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
    
    static String getShort(String c) {
        if (c == null) return null;
        int i = c.lastIndexOf('.');
        if (i < 0) return c;
        return c.substring(i + 1);
    }
    
    /*NM TEMPORARY COMMENTED OUT
    private static String getMessage (InvalidExpressionException e) {
        String m = e.getLocalizedMessage ();
        if (m == null)
            m = e.getMessage ();
        return ">" + m + "<";
    }
     */
}
