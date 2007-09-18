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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.Field;

/*
 * VariablesNodeModel.java
 *
 * @author Nik Molchanov (copied from Jan Jancura's JPDA implementation)
 */
public class VariablesNodeModel implements NodeModel {
    
    public static final String FIELD =
            "org/netbeans/modules/debugger/resources/watchesView/Field"; // NOI18N
    public static final String LOCAL =
            "org/netbeans/modules/debugger/resources/localsView/LocalVariable"; // NOI18N
    public static final String FIXED_WATCH =
            "org/netbeans/modules/debugger/resources/watchesView/FixedWatch"; // NOI18N
    public static final String STATIC_FIELD =
            "org/netbeans/modules/debugger/resources/watchesView/StaticField"; // NOI18N
    public static final String SUPER =
            "org/netbeans/modules/debugger/resources/watchesView/SuperVariable"; // NOI18N
    
    private RequestProcessor evaluationRP = new RequestProcessor();
    private final Collection<ModelListener> modelListeners = new HashSet<ModelListener>();
    
    // Localizable messages
    private String LC_NoInfo = NbBundle.getMessage(VariablesNodeModel.class, "CTL_No_Info"); // NOI18N
    private String LC_NoCurrentThreadVar = NbBundle.getMessage(VariablesNodeModel.class,
            "NoCurrentThreadVar"); // NOI18N
    private String LC_LocalsModelColumnNameName = NbBundle.getMessage(VariablesNodeModel.class,
            "CTL_LocalsModel_Column_Name_Name"); // NOI18N
    private String LC_LocalsModelColumnNameDesc = NbBundle.getMessage(VariablesNodeModel.class,
            "CTL_LocalsModel_Column_Name_Desc"); // NOI18N
    
    // Non-localized magic strings
    private final String strNoInfo = "NoInfo"; // NOI18N
    private final String strSubArray = "SubArray"; // NOI18N
    private final String strNoCurrentThread = "No current thread"; // NOI18N
    
    public VariablesNodeModel(ContextProvider lookupProvider) {
    }
    
    public String getDisplayName(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return LC_LocalsModelColumnNameName;
        }
        if (o instanceof Field) {
            return ((Field) o).getName();
        }
        if (o instanceof LocalVariable) {
            return ((LocalVariable) o).getName();
        }
        
        String str = o.toString();
        if (str.equals(strNoInfo)) {
            return LC_NoInfo;
        }
        if (str.equals(strNoCurrentThread)) {
            return LC_NoCurrentThreadVar;
        }
        if (str.startsWith(strSubArray)) {
            int index = str.indexOf('-');
            //int from = Integer.parseInt(str.substring(8, index));
            //int to = Integer.parseInt(str.substring(index + 1));
            return NbBundle.getMessage(VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Name_SubArray", // NOI18N
                    str.substring(8, index), str.substring(index + 1));
        }
        throw new UnknownTypeException(o);
    }
    
    private Map<Object, String> shortDescriptionMap = new HashMap<Object, String>();
    
    public String getShortDescription(final Object o) throws UnknownTypeException {
        synchronized (shortDescriptionMap) {
            Object shortDescription = shortDescriptionMap.remove(o);
            if (shortDescription instanceof String) {
                return (String) shortDescription;
            } else if (shortDescription instanceof UnknownTypeException) {
                throw (UnknownTypeException) shortDescription;
            }
        }
        testKnown(o);
        // Called from AWT - we need to postpone the work...
        evaluationRP.post(new Runnable() {
            public void run() {
                String shortDescription = getShortDescriptionSync(o);
                if (shortDescription != null && shortDescription.length() > 0) {
                    synchronized (shortDescriptionMap) {
                        shortDescriptionMap.put(o, shortDescription);
                    }
                    fireModelChange(new ModelEvent.NodeChanged(VariablesNodeModel.this,
                            o, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        return ""; // NOI18N
    }
    
    private String getShortDescriptionSync(Object o) {
        if (o == TreeModel.ROOT) {
            return LC_LocalsModelColumnNameDesc;
        } else if (o instanceof Field) {
            return "(" + ((Field) o).getType() + ") " + ((Field) o).getValue(); // NOI18N
        } else if (o instanceof LocalVariable) {
            return "(" + ((LocalVariable) o).getType() + ") " + ((LocalVariable) o).getValue(); // NOI18N
        }
        
        String str = o.toString();
        if (str.startsWith(strSubArray)) {
            int index = str.indexOf('-');
            return NbBundle.getMessage(VariablesNodeModel.class,
                    "CTL_LocalsModel_Column_Descr_SubArray", // NOI18N
                    str.substring(8, index), str.substring(index + 1));
        } else if (str.equals(strNoInfo)) {
            return LC_NoInfo;
        } else if (str.equals(strNoCurrentThread)) {
            return LC_NoCurrentThreadVar;
        } else {
            return ""; // NOI18N
        }
    }
    
    private void testKnown(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT || o instanceof Field || o instanceof LocalVariable) {
            return;
        }
        String str = o.toString();
        if (str.startsWith(strSubArray) || str.equals(strNoInfo) || str.equals(strNoCurrentThread)) {
            return;
        }
        throw new UnknownTypeException(o);
    }
    
    public String getIconBase(Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT)
            return FIELD;
        if (o instanceof Field) {
            if (((Field) o).isStatic())
                return STATIC_FIELD;
            else
                return FIELD;
        }
        if (o instanceof LocalVariable)
            return LOCAL;
        /*NM TEMPORARY COMMENTED OUT
        if (o instanceof Super)
            return SUPER;
        if (o instanceof This)
            return FIELD;
         */
        String str = o.toString();
        if (str.startsWith(strSubArray))
            return LOCAL;
        if (str.equals(strNoInfo) || str.equals(strNoCurrentThread))
            return null;
        throw new UnknownTypeException(o);
    }
    
    public void addModelListener(ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }
    
    public void removeModelListener(ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }
}
