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

package org.netbeans.modules.debugger.jpda.ui.views;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.NodeModelFilter;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeExpansionModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * This delegating CompoundModelImpl loads all models from DebuggerManager.
 * getDefault ().getCurrentEngine ().lookup (viewType, ..) lookup.
 *
 * <p>
 * This class is identical to org.netbeans.modules.debugger.ui.views.ViewModelListener.
 *
 * @author   Jan Jancura
 */
class ViewModelListener extends DebuggerManagerAdapter {
    
    private static boolean verbose = 
        System.getProperty ("netbeans.debugger.models") != null;

    private String          viewType;
    private JComponent      view;
    private List models = new ArrayList(11);
    
    
    ViewModelListener (
        String viewType,
        JComponent view
    ) {
        this.viewType = viewType;
        this.view = view;
        setUp();
    }
    
    void setUp() {
        DebuggerManager.getDebuggerManager ().addDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        updateModel ();
    }

    void destroy () {
        DebuggerManager.getDebuggerManager ().removeDebuggerListener (
            DebuggerManager.PROP_CURRENT_ENGINE,
            this
        );
        Models.setModelsToView (
            view, 
            Models.EMPTY_MODEL
        );
    }

    public void propertyChange (PropertyChangeEvent e) {
        updateModel ();
    }
    
    private List joinLookups(DebuggerEngine e, DebuggerManager dm, Class service) {
        List es = e.lookup (viewType, service);
        List ms = dm.lookup(viewType, service);
        ms.removeAll(es);
        es.addAll(ms);
        return es;
    }
    
    private synchronized void updateModel () {
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        DebuggerEngine e = dm.getCurrentEngine ();
        
        List treeModels;
        List treeModelFilters;
        List treeExpansionModels;
        List nodeModels;
        List nodeModelFilters;
        List tableModels;
        List tableModelFilters;
        List nodeActionsProviders;
        List nodeActionsProviderFilters;
        List columnModels;
        List mm;
        if (e != null) {
            treeModels =            joinLookups(e, dm, TreeModel.class);
            treeModelFilters =      joinLookups(e, dm, TreeModelFilter.class);
            treeExpansionModels =   joinLookups(e, dm, TreeExpansionModel.class);
            nodeModels =            joinLookups(e, dm, NodeModel.class);
            nodeModelFilters =      joinLookups(e, dm, NodeModelFilter.class);
            tableModels =           joinLookups(e, dm, TableModel.class);
            tableModelFilters =     joinLookups(e, dm, TableModelFilter.class);
            nodeActionsProviders =  joinLookups(e, dm, NodeActionsProvider.class);
            nodeActionsProviderFilters = joinLookups(e, dm, NodeActionsProviderFilter.class);
            columnModels =          joinLookups(e, dm, ColumnModel.class);
            mm =                    joinLookups(e, dm, Model.class);
        } else {
            treeModels =            dm.lookup (viewType, TreeModel.class);
            treeModelFilters =      dm.lookup (viewType, TreeModelFilter.class);
            treeExpansionModels =   dm.lookup (viewType, TreeExpansionModel.class);
            nodeModels =            dm.lookup (viewType, NodeModel.class);
            nodeModelFilters =      dm.lookup (viewType, NodeModelFilter.class);
            tableModels =           dm.lookup (viewType, TableModel.class);
            tableModelFilters =     dm.lookup (viewType, TableModelFilter.class);
            nodeActionsProviders =  dm.lookup (viewType, NodeActionsProvider.class);
            nodeActionsProviderFilters = dm.lookup (viewType, NodeActionsProviderFilter.class);
            columnModels =          dm.lookup (viewType, ColumnModel.class);
            mm =                    dm.lookup (viewType, Model.class);
        }
        
        models.clear();
        models.add(treeModels);
        models.add(treeModelFilters);
        models.add(treeExpansionModels);
        models.add(nodeModels);
        models.add(nodeModelFilters);
        models.add(tableModels);
        models.add(tableModelFilters);
        models.add(nodeActionsProviders);
        models.add(nodeActionsProviderFilters);
        models.add(columnModels);
        models.add(mm);
        
        Models.setModelsToView (
            view, 
            Models.createCompoundModel (models)
        );
    }

    
    // innerclasses ............................................................

    private static class EmptyModel implements NodeModel {
        
        public String getDisplayName (Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return "Name"; // TODO: Localized ???
            }
            throw new UnknownTypeException (node);
        }
        
        public String getIconBase (Object node) throws UnknownTypeException {
            if (node == TreeModel.ROOT) {
                return "org/netbeans/modules/debugger/resources/DebuggerTab";
            }
            throw new UnknownTypeException (node);
        }
        
        public String getShortDescription (Object node) 
        throws UnknownTypeException {
            throw new UnknownTypeException (node);
        }
        
        public void addModelListener (ModelListener l) {}
        public void removeModelListener (ModelListener l) {}
    }
}
