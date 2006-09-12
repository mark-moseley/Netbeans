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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;

import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;

import org.netbeans.spi.debugger.ui.AttachType;
import org.netbeans.spi.debugger.ui.Controller;

import org.openide.util.NbBundle;


public class ConnectorPanel extends JPanel implements ActionListener,
Controller {

    /** Contains list of AttachType names.*/
    private JComboBox             cbAttachTypes;
    /** Switches off listenning on cbAttachTypes.*/
    private boolean               doNotListen;
    /** Contains list of installed AttachTypes.*/
    private List                  attachTypes;
    /** Currentlydisplayed panel.*/
    private JComponent            currentPanel;
    /** Current attach type, which is stored into settings for the next invocation. */
    private AttachType            currentAttachType;


    public ConnectorPanel ()  {
        getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, "ACSD_ConnectorPanel")
        );
        cbAttachTypes = new JComboBox ();
        cbAttachTypes.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        attachTypes = DebuggerManager.getDebuggerManager ().lookup (
            null, AttachType.class
        );
        String defaultAttachTypeName =
                Properties.getDefault ().getProperties ("debugger").getString ("last_attach_type", null);
        int defaultIndex = 0;
        int i, k = attachTypes.size ();
        Collections.sort(attachTypes, new Comparator() {
            public int compare(Object o1, Object o2) {
                if (!(o1 instanceof AttachType) || !(o2 instanceof AttachType)) return 0;
                return ((AttachType) o1).getTypeDisplayName().compareTo(((AttachType) o2).getTypeDisplayName());
            }
        });
        for (i = 0; i < k; i++) {
            AttachType at = (AttachType) attachTypes.get (i);
            cbAttachTypes.addItem (at.getTypeDisplayName ());
            if ( (defaultAttachTypeName != null) &&
                 (defaultAttachTypeName.equals (at.getClass ().getName ()))
            )
                defaultIndex = i;
        }

        cbAttachTypes.setActionCommand ("SwitchMe!"); // NOI18N
        cbAttachTypes.addActionListener (this);

        setLayout (new GridBagLayout ());
        setBorder (new EmptyBorder (11, 11, 0, 10));
        refresh (defaultIndex);
    }
    
    private void refresh (int index) {
        JLabel cbLabel = new JLabel (
            NbBundle.getMessage (ConnectorPanel.class, "CTL_Connect_through")
        ); // NOI18N
        cbLabel.setDisplayedMnemonic (
            NbBundle.getMessage (ConnectorPanel.class, 
                "CTL_Connect_through_Mnemonic").charAt (0)
        ); // NOI18N
        cbLabel.getAccessibleContext ().setAccessibleDescription (
            NbBundle.getMessage (ConnectorPanel.class, 
                "ACSD_CTL_Connect_through")// NOI18N
        ); 
        cbLabel.setLabelFor (cbAttachTypes);

        GridBagConstraints c = new GridBagConstraints ();
        c.insets = new Insets (0, 0, 6, 6);
        add (cbLabel, c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.gridwidth = 0;
        c.insets = new Insets (0, 3, 6, 0);
        doNotListen = true;
        cbAttachTypes.setSelectedIndex (index);
        doNotListen = false;
        add (cbAttachTypes, c);
        c.insets = new Insets (0, 0, 6, 0);
        add (new JSeparator(), c);
        c = new GridBagConstraints ();
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = java.awt.GridBagConstraints.BOTH;
        c.gridwidth = 0;
        AttachType attachType = (AttachType) attachTypes.get (index);
        this.currentAttachType = attachType;
        currentPanel = attachType.getCustomizer ();
        
        add (currentPanel, c);
    }


    /**
     * Called when a user selects debugger type in a combo-box.
     */
    public void actionPerformed (ActionEvent e) {
        if (doNotListen) return;
        if (e.getActionCommand ().equals ("SwitchMe!")); // NOI18N
        removeAll ();
        refresh (((JComboBox) e.getSource ()).getSelectedIndex ());
        Component w = getParent ();
        while (!(w instanceof Window))
            w = w.getParent ();
        if (w != null) ((Window) w).pack (); // ugly hack...
        return;
    }
    
    public boolean cancel () {
        return ((Controller) currentPanel).cancel ();
    }
    
    public boolean ok () {
        String defaultAttachTypeName = currentAttachType.getClass().getName();
        Properties.getDefault().getProperties("debugger").setString("last_attach_type", defaultAttachTypeName);
        return ((Controller) currentPanel).ok ();
    }    
}



