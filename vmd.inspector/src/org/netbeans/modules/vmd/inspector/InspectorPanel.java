/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.inspector;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

import javax.swing.*;
import org.netbeans.modules.vmd.api.inspector.OrderedNavigatorPanel;

/**
 * @author Karol Harezlak
 */

public final class InspectorPanel implements OrderedNavigatorPanel, LookupListener {
    
    private static InspectorPanel INSTANCE;
    
    public static InspectorPanel getInstance() {
        synchronized(InspectorPanel.class) {
            if (INSTANCE == null)
                INSTANCE = new InspectorPanel();
            return INSTANCE;
        }
    }
    
    private InspectorUI ui;
    private Lookup lookup;
    
    private InspectorPanel() {
    }
    
    public String getDisplayName() {
        return "Mobility Inspector"; // TODO Bundle
    }
    
    public String getDisplayHint() {
        return "Shows the design structure"; // TODO Bundle
    }
    
    public JComponent getComponent() {
        return getUI();
    }
    
    private InspectorUI getUI() {
        if (ui == null)
            ui = new InspectorUI();
        return ui;
    }
    
    public void panelActivated(Lookup lookup) {
    }
    
    public void panelDeactivated() {
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public void resultChanged(LookupEvent ev) {
    }

    public Integer getOrder() {
        return 1000;
    }
    
}
