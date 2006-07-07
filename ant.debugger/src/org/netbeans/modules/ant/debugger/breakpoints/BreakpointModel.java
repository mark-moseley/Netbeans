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

package org.netbeans.modules.ant.debugger.breakpoints;

import java.util.Vector;
import javax.swing.Action;

import org.apache.tools.ant.module.api.support.TargetLister;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.ant.debugger.AntDebugger;
import org.netbeans.modules.ant.debugger.Utils;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.spi.debugger.ui.Constants;
import org.openide.filesystems.FileObject;
import org.openide.text.Annotatable;

import org.openide.text.Line;

/**
 *
 * @author   Jan Jancura
 */
public class BreakpointModel implements NodeModel, TableModel, Constants {
    
    public static final String      LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/Breakpoint";
    public static final String      LINE_BREAKPOINT_PC =
        "org/netbeans/modules/debugger/resources/editor/Breakpoint+PC";
    public static final String      DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/editor/DisabledBreakpoint";
    
    private Vector                  listeners = new Vector ();
    
    
    // NodeModel implementation ................................................
    
    /**
     * Returns display name for given node.
     *
     * @throws  ComputingException if the display name resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve display name for given node type
     * @return  display name for given node
     */
    public String getDisplayName (Object node) throws UnknownTypeException {
        if (node instanceof AntBreakpoint) {
            AntBreakpoint breakpoint = (AntBreakpoint) node;
            FileObject fileObject = (FileObject) breakpoint.getLine ().
                getLookup ().lookup (FileObject.class);
            return fileObject.getNameExt () + ":" + 
                (breakpoint.getLine ().getLineNumber () + 1);
        }
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns icon for given node.
     *
     * @throws  ComputingException if the icon resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve icon for given node type
     * @return  icon for given node
     */
    public String getIconBase (Object node) throws UnknownTypeException {
        if (node instanceof AntBreakpoint) {
            AntBreakpoint breakpoint = (AntBreakpoint) node;
            if (!((AntBreakpoint) node).isEnabled ())
                return DISABLED_LINE_BREAKPOINT;
            AntDebugger debugger = getDebugger ();
            if ( debugger != null &&
                 Utils.contains (
                     debugger.getCurrentLine (), 
                     breakpoint.getLine ()
                 )
             )
                return LINE_BREAKPOINT_PC;
            return LINE_BREAKPOINT;
        }
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns tooltip for given node.
     *
     * @throws  ComputingException if the tooltip resolving process 
     *          is time consuming, and the value will be updated later
     * @throws  UnknownTypeException if this NodeModel implementation is not
     *          able to resolve tooltip for given node type
     * @return  tooltip for given node
     */
    public String getShortDescription (Object node) 
    throws UnknownTypeException {
        if (node instanceof AntBreakpoint) {
            AntBreakpoint breakpoint = (AntBreakpoint) node;
            return breakpoint.getLine ().getDisplayName ();
        }
        throw new UnknownTypeException (node);
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
        
     
    // TableModel implementation ......................................
    
    /**
     * Returns value to be displayed in column <code>columnID</code>
     * and row identified by <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@link org.netbeans.spi.viewmodel.TreeModel#getChildren}.
     *
     * @param node a object returned from 
     *         {@link org.netbeans.spi.viewmodel.TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @throws ComputingException if the value is not known yet and will 
     *         be computed later
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return value of variable representing given position in tree table.
     */
    public Object getValueAt (Object node, String columnID) throws 
    UnknownTypeException {
        if (node instanceof AntBreakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID))
                return Boolean.valueOf (((AntBreakpoint) node).isEnabled ());
        }
        throw new UnknownTypeException (node);
    }
    
    /**
     * Returns true if value displayed in column <code>columnID</code>
     * and row <code>node</code> is read only. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@link TreeModel#getChildren}.
     *
     * @param node a object returned from {@link TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     *
     * @return true if variable on given position is read only
     */
    public boolean isReadOnly (Object node, String columnID) throws 
    UnknownTypeException {
        if (node instanceof AntBreakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID))
                return false;
        }
        throw new UnknownTypeException (node);
    }
    
    /**
     * Changes a value displayed in column <code>columnID</code>
     * and row <code>node</code>. Column ID is defined in by 
     * {@link ColumnModel#getID}, and rows are defined by values returned from 
     * {@link TreeModel#getChildren}.
     *
     * @param node a object returned from {@link TreeModel#getChildren} for this row
     * @param columnID a id of column defined by {@link ColumnModel#getID}
     * @param value a new value of variable on given position
     * @throws UnknownTypeException if there is no TableModel defined for given
     *         parameter type
     */
    public void setValueAt (Object node, String columnID, Object value) 
    throws UnknownTypeException {
        if (node instanceof AntBreakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID))
                if (((Boolean) value).equals (Boolean.TRUE))
                    ((AntBreakpoint) node).enable ();
                else
                    ((AntBreakpoint) node).disable ();
        } else
        throw new UnknownTypeException (node);
    }
        
     
    // TableModel implementation ......................................

    public void fireChanges () {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.TreeChanged (this)
            );
    }
    
    private static AntDebugger getDebugger () {
        DebuggerEngine engine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (engine == null) return null;
        return (AntDebugger) engine.lookupFirst (null, AntDebugger.class);
    }
}
