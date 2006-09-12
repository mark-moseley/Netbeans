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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.Vector;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;



/**
 *
 * @author   Jan Jancura
 */
public class ThreadsTableModel implements TableModel, Constants {
    
    private Vector listeners = new Vector ();
    

    public Object getValueAt (Object row, String columnID) throws 
    UnknownTypeException {
        //if (row instanceof javax.swing.JToolTip) {
        //    will throw UnknownTypeException - the value is used for tooltips
        //}
        if (row instanceof MonitorModel.ThreadWithBordel) 
            row = ((MonitorModel.ThreadWithBordel) row).originalThread;
        if (row instanceof JPDAThreadGroup) {
            if (THREAD_STATE_COLUMN_ID.equals (columnID)) 
                return "";
            if (THREAD_SUSPENDED_COLUMN_ID.equals (columnID)) {
                JPDAThreadGroup group = (JPDAThreadGroup) row;
                JPDAThread[] threads = group.getThreads ();
                if (threads.length < 1) return Boolean.FALSE;
                return Boolean.valueOf (threads [0].isSuspended ());
            }
        }
        if (row instanceof JPDAThread) {
            if (THREAD_STATE_COLUMN_ID.equals (columnID)) {
                String description = getThreadStateDescription(((JPDAThread) row).getState ());
                if (description != null) {
                    return description;
                }
            } else
            if (THREAD_SUSPENDED_COLUMN_ID.equals (columnID))
                return Boolean.valueOf (((JPDAThread) row).isSuspended ());
        }
        throw new UnknownTypeException (row);
    }
    
    private static String getThreadStateDescription(int state) {
        switch (state) {
            case JPDAThread.STATE_MONITOR:
                return NbBundle.getMessage (
                    ThreadsTableModel.class, 
                    "CTL_Thread_State_OnMonitor"
                );
            case JPDAThread.STATE_NOT_STARTED:
                return NbBundle.getMessage (
                    ThreadsTableModel.class, 
                    "CTL_Thread_State_NotStarted"
                );
            case JPDAThread.STATE_RUNNING:
                return NbBundle.getMessage (
                    ThreadsTableModel.class, 
                    "CTL_Thread_State_Running"
                );
            case JPDAThread.STATE_SLEEPING:
                return NbBundle.getMessage (
                    ThreadsTableModel.class, 
                    "CTL_Thread_State_Sleeping"
                );
            case JPDAThread.STATE_UNKNOWN:
                return NbBundle.getMessage (
                    ThreadsTableModel.class, 
                    "CTL_Thread_State_Unknown"
                );
            case JPDAThread.STATE_WAIT:
                return NbBundle.getMessage (
                    ThreadsTableModel.class, 
                    "CTL_Thread_State_Waiting"
                );
            case JPDAThread.STATE_ZOMBIE:
                return NbBundle.getMessage (
                    ThreadsTableModel.class, 
                    "CTL_Thread_State_Zombie"
                );
            default: ErrorManager.getDefault().log(ErrorManager.WARNING, "Unknown thread state: "+state);
                    return null;
        }
    }
    
    public boolean isReadOnly (Object row, String columnID) throws 
    UnknownTypeException {
        if (row instanceof MonitorModel.ThreadWithBordel) 
            row = ((MonitorModel.ThreadWithBordel) row).originalThread;
        if (row instanceof JPDAThreadGroup) {
            if (THREAD_STATE_COLUMN_ID.equals (columnID)) 
                return true;
            if (THREAD_SUSPENDED_COLUMN_ID.equals (columnID)) 
                return false;
        }
        if (row instanceof JPDAThread) {
            if (THREAD_STATE_COLUMN_ID.equals (columnID))
                return true;
            else
            if (THREAD_SUSPENDED_COLUMN_ID.equals (columnID))
                return false;
        }
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (Object row, String columnID, Object value) 
    throws UnknownTypeException {
        if (row instanceof MonitorModel.ThreadWithBordel) 
            row = ((MonitorModel.ThreadWithBordel) row).originalThread;
        if (row instanceof JPDAThreadGroup) {
            if (THREAD_SUSPENDED_COLUMN_ID.equals (columnID)) {
                if (((Boolean) value).booleanValue ())
                    ((JPDAThreadGroup) row).suspend ();
                else
                    ((JPDAThreadGroup) row).resume ();
                fireTableValueChanged (row, Constants.THREAD_SUSPENDED_COLUMN_ID);
                return;
            }
        }
        if (row instanceof JPDAThread) {
            if (THREAD_SUSPENDED_COLUMN_ID.equals (columnID)) {
                if (value.equals (Boolean.TRUE))
                    ((JPDAThread) row).suspend ();
                else 
                    ((JPDAThread) row).resume ();
                fireTableValueChanged (row, Constants.THREAD_SUSPENDED_COLUMN_ID);
                return;
            }
        }
        throw new UnknownTypeException (row);
    }
    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
    private void fireTableValueChanged (Object o, String propertyName) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TableValueChanged (this, o, propertyName)
            );
    }
}
