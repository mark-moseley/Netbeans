/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.core.windows.actions;


import org.netbeans.core.windows.Constants;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * @author   Peter Zavadsky
 */
public class CloneDocumentAction extends AbstractAction
implements PropertyChangeListener {

    public CloneDocumentAction() {
        putValue(NAME, NbBundle.getMessage(CloneDocumentAction.class, "CTL_CloneDocumentAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if(tc == null || !(tc instanceof TopComponent.Cloneable)) {
            return;
        }
        
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        if(mode == null) {
            return;
        }
        
        if(mode.getKind() == Constants.MODE_KIND_EDITOR) {
            ActionUtils.cloneWindow(tc);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        setEnabled(tc instanceof TopComponent.Cloneable
            && mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR);
    }
    
}

