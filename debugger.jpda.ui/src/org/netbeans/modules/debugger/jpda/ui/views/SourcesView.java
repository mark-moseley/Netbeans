/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.views;

import java.awt.BorderLayout;
import java.util.ArrayList;
import javax.swing.JComponent;
import org.netbeans.modules.debugger.jpda.ui.Utils;
import org.netbeans.spi.viewmodel.Models;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

public class SourcesView extends TopComponent {
    
    private transient JComponent tree;
    private transient ViewModelListener viewModelListener;
    
    
    public SourcesView () {
        setIcon (Utils.getIcon (
            "org/netbeans/modules/debugger/jpda/resources/root"
        ).getImage ());
    }

    protected String preferredID() {
        return this.getClass().getName();
    }

    protected void componentShowing () {
        super.componentShowing ();
        if (viewModelListener != null)
            return;
        if (tree == null) {
            setLayout (new BorderLayout ());
            tree = Models.createView 
                (null, null, null, null, new ArrayList ());
            tree.setName ("SourcesView");
            add (tree, "Center");  //NOI18N
        }
        if (viewModelListener != null)
            throw new InternalError ();
        viewModelListener = new ViewModelListener (
            "SourcesView",
            tree
        );
    }
    
    protected void componentHidden () {
        super.componentHidden ();
        viewModelListener.destroy ();
        viewModelListener = null;
    }
    
    public int getPersistenceType () {
        return PERSISTENCE_ALWAYS;
    }
        
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        if (tree == null) return false;
        return tree.requestFocusInWindow ();
    }
    
    public String getName () {
        return NbBundle.getMessage (SourcesView.class, "CTL_Sourcess_view");
    }
}
