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

package org.netbeans.modules.bpel.debugger.ui.source;

import java.io.File;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.bpel.debugger.api.SourcePath;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author   Jan Jancura
 */
public class SourcesNodeModel implements CheckNodeModel {

    public static final String SOURCE_ROOT =
        "org/netbeans/modules/bpel/debugger/ui/resources/image/execution/PROCESS";

    private SourcePath              sourcePath;
    // set of filters
    private Set                     enabledSourceRoots = new HashSet ();
    private Set                     disabledSourceRoots = new HashSet ();
    
    public SourcesNodeModel(ContextProvider lookupProvider) {
        sourcePath = lookupProvider.lookupFirst(null, SourcePath.class);
    }
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return NbBundle.getBundle(SourcesNodeModel.class).getString("CTL_SourcesModel_Column_Name_Name");
        } else
        if (o instanceof String) {
            return (String)o;
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        return getDisplayName(o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof String) {
            return SOURCE_ROOT;
        } else
        throw new UnknownTypeException (o);
    }

    public void addModelListener (ModelListener l) {
    }

    public void removeModelListener (ModelListener l) {
    }

    public boolean isCheckable(Object node) throws UnknownTypeException {
        return true;
    }

    public boolean isCheckEnabled(Object node) throws UnknownTypeException {
        return true;
    }

    public Boolean isSelected(Object node) throws UnknownTypeException {
        if (node instanceof String) {
            return Boolean.valueOf(isEnabled((String)node));
        } else {
            throw new UnknownTypeException (node);
        }
    }

    public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
        if (node instanceof String) {
            setEnabled ((String) node, selected.booleanValue ());
            return;
        } else {
            throw new UnknownTypeException (node);
        }
    }

    // other methods ...........................................................

    private boolean isEnabled (String root) {
        String[] sourceRoots = sourcePath.getSelectedSources ();
        int i, k = sourceRoots.length;
        for (i = 0; i < k; i++)
            if (sourceRoots [i].equals (root)) return true;
        return false;
    }

    private void setEnabled (String root, boolean enabled) {
        Set sourceRoots = new HashSet (Arrays.asList (
            sourcePath.getSelectedSources ()
        ));
        if (enabled) {
            enabledSourceRoots.add (root);
            disabledSourceRoots.remove (root);
            sourceRoots.add (root);
        } else {
            disabledSourceRoots.add (root);
            enabledSourceRoots.remove (root);
            sourceRoots.remove (root);
        }
        String[] ss = new String [sourceRoots.size ()];
        sourcePath.setSelectedSources ((String[]) sourceRoots.toArray (ss));
    }

}
