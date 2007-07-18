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

import java.awt.BorderLayout;
import java.awt.Color;
import org.openide.util.Lookup;
import javax.swing.*;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Karol Harezlak
 */

public final class InspectorPanel implements NavigatorPanel {

    private static InspectorPanel INSTANCE;
    
    private JPanel panel;
    private Lookup lookup;
    private final InstanceContent ic;
    
    public static InspectorPanel getInstance() {
        synchronized (InspectorPanel.class) {
            if (INSTANCE == null) {
                INSTANCE = new InspectorPanel();
            }
            return INSTANCE;
        }
    }
    
    private InspectorPanel() {
        this.ic = new InstanceContent();
        this.lookup = new AbstractLookup(ic);
        this.panel = new JPanel(new BorderLayout());
        this.panel.setBackground(Color.WHITE);
    }

    public String getDisplayName() {
        return NbBundle.getMessage(InspectorPanel.class, "LBL_InspectorPanelDisplayName"); //NOI18N
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(InspectorPanel.class, "LBL_InspectorPanelHint"); //NOI18N
    }

    public synchronized JComponent getComponent() {
        return panel;
    }

    public void panelActivated(Lookup lookup) {
    }

    public void panelDeactivated() {
    }

    public Lookup getLookup() {
        return lookup;
    }

    public InstanceContent getInstanceContent() {
        return ic;
    }

}
