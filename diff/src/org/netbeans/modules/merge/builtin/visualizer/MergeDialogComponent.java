/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.merge.builtin.visualizer;

import java.awt.Component;
import java.beans.PropertyVetoException;

import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;

/**
 * This is a component, that acts as a non modal dialog.
 * There are problems with accessibility to non-modal dialogs,
 * therefore this approach was chosen.
 * @author  Martin Entlicher
 */
public class MergeDialogComponent extends TopComponent {
    
    public static final String MERGE_MODE = "MergeModeName";
    
    public static final String PROP_PANEL_CLOSING = "panelClosing"; // NOI18N
    public static final String PROP_ALL_CLOSED = "allPanelsClosed"; // NOI18N
    public static final String PROP_ALL_CANCELLED = "allPanelsCancelled"; // NOI18N
    
    /** Creates new form MergeDialogComponent */
    public MergeDialogComponent() {
        initComponents();
        javax.swing.JRootPane root = getRootPane();
        if (root != null) root.setDefaultButton(okButton);
        setName(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "MergeDialogComponent.title"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        mergeTabbedPane = new javax.swing.JTabbedPane();
        buttonsPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        mergeTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        mergeTabbedPane.setPreferredSize(new java.awt.Dimension(600, 600));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mergeTabbedPane, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        okButton.setText(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_OK"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        buttonsPanel.add(okButton, gridBagConstraints);

        cancelButton.setText(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_Cancel"));
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        buttonsPanel.add(cancelButton, gridBagConstraints);

        helpButton.setText(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_Help"));
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        buttonsPanel.add(helpButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 11, 11);
        add(buttonsPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // Add your handling code here:
        //List panelsToCloseList;
        Component[] panels;
        synchronized (this) {
            panels = mergeTabbedPane.getComponents();
        }
        for (int i = 0; i < panels.length; i++) {
            MergePanel panel = (MergePanel) panels[i];
            if (panel.canClose()) {
                try {
                    fireVetoableChange(PROP_PANEL_CLOSING, null, panel);
                } catch (PropertyVetoException pvex) {
                    return ;
                }
                mergeTabbedPane.remove(panel);
            }
        }
        synchronized (this) {
            if (mergeTabbedPane.getTabCount() == 0) {
                try {
                    fireVetoableChange(PROP_ALL_CLOSED, null, null);
                } catch (PropertyVetoException pvex) {
                    return ;
                }
                close();
            }
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_helpButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        // Add your handling code here:
        synchronized (this) {
            try {
                fireVetoableChange(PROP_ALL_CANCELLED, null, null);
            } catch (PropertyVetoException pvex) {}
            close();
        }
    }//GEN-LAST:event_cancelButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane mergeTabbedPane;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton helpButton;
    // End of variables declaration//GEN-END:variables
    
    
    protected Mode getDockingMode(Workspace workspace) {
        Mode mode = workspace.findMode(MERGE_MODE);
        if (mode == null) {
            mode = workspace.createMode(
                MERGE_MODE, getName(),
                MergeDialogComponent.class.getResource(
                "/org/netbeans/modules/merge/builtin/visualizer/mergeModeIcon.gif" // NOI18N
            ));
        }
        return mode;
    }
    
    public void open(Workspace workspace) {
        //System.out.println("workspace = "+workspace);
        if (workspace == null) {
            workspace = org.openide.TopManager.getDefault().getWindowManager().getCurrentWorkspace();
        }
        Mode mergeMode = getDockingMode(workspace);
        mergeMode.dockInto(this);
        super.open(workspace);
        requestFocus();
    }
    
    public synchronized void addMergePanel(MergePanel panel) {
        mergeTabbedPane.addTab(panel.getName(), panel);
        javax.swing.JRootPane root = getRootPane();
        if (root != null) root.setDefaultButton(okButton);
    }
    
}
