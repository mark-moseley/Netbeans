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
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.awt.MouseUtils;
import org.openide.awt.JPopupMenuPlus;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;

/**
 * This is a component, that acts as a non modal dialog.
 * There are problems with accessibility to non-modal dialogs,
 * therefore this approach was chosen.
 * @author  Martin Entlicher
 */
public class MergeDialogComponent extends TopComponent implements ChangeListener {
    
    public static final String MERGE_MODE = "MergeModeName";
    
    public static final String PROP_PANEL_CLOSING = "panelClosing"; // NOI18N
    public static final String PROP_ALL_CLOSED = "allPanelsClosed"; // NOI18N
    public static final String PROP_ALL_CANCELLED = "allPanelsCancelled"; // NOI18N
    public static final String PROP_PANEL_SAVE = "panelSave"; // NOI18N
    
    private Map nodesForPanels = new HashMap();
    
    /** Creates new form MergeDialogComponent */
    public MergeDialogComponent() {
        initComponents();
        okButton.setMnemonic(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_OK_Mnemonic").charAt(0));  // NOI18N
        cancelButton.setMnemonic(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_Cancel_Mnemonic").charAt(0));  // NOI18N
        helpButton.setMnemonic(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_Help_Mnemonic").charAt(0));  // NOI18N
        initListeners();
        putClientProperty("PersistenceType", "Never");
        setName(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "MergeDialogComponent.title"));
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MergeDialogComponent.class);
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

        okButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergeDialogComponent.class).getString("ACS_BTN_OKA11yDesc"));
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

        cancelButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergeDialogComponent.class).getString("ACS_BTN_CancelA11yDesc"));
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

        helpButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergeDialogComponent.class).getString("ACS_BTN_HelpA11yDesc"));
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
        ArrayList unsavedPanelNames = new ArrayList();
        ArrayList saveCookies = new ArrayList();
        for (int i = 0; i < panels.length; i++) {
            MergePanel panel = (MergePanel) panels[i];
            MergeNode node = (MergeNode) nodesForPanels.get(panel);
            SaveCookie sc;
            if ((sc = (SaveCookie) node.getCookie(SaveCookie.class)) != null) {
                unsavedPanelNames.add(panel.getName());
                saveCookies.add(sc);
            }
        }
        Object ret;
        if (unsavedPanelNames.size() == 1) {
            ret = TopManager.getDefault().notify(
            new NotifyDescriptor.Message(NbBundle.getMessage(MergeDialogComponent.class,
                                                             "SaveFileQuestion",
                                                             unsavedPanelNames.get(0))));
        } else if (unsavedPanelNames.size() > 1) {
            ret = TopManager.getDefault().notify(
                new NotifyDescriptor.Message(NbBundle.getMessage(MergeDialogComponent.class,
                                                                 "SaveFilesQuestion",
                                                                 new Integer(unsavedPanelNames.size()))));
        } else {
            ret = NotifyDescriptor.OK_OPTION;
        }
        if (ret != NotifyDescriptor.OK_OPTION) return ;
        try {
            for (Iterator it = saveCookies.iterator(); it.hasNext(); ) {
                SaveCookie sc = (SaveCookie) it.next();
                sc.save();
            }
        } catch (java.io.IOException ioEx) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(ioEx.getLocalizedMessage()));
            return ;
        }
        for (int i = 0; i < panels.length; i++) {
            MergePanel panel = (MergePanel) panels[i];
            try {
                fireVetoableChange(PROP_PANEL_CLOSING, null, panel);
            } catch (PropertyVetoException pvex) {
                return ;
            }
            removeMergePanel(panel);
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
    
    protected void componentClosed() {
        Component[] panels;
        synchronized (this) {
            try {
                fireVetoableChange(PROP_ALL_CANCELLED, null, null);
            } catch (PropertyVetoException pvex) {}

            panels = mergeTabbedPane.getComponents();
        }
        for (int i = 0; i < panels.length; i++) {
            MergePanel panel = (MergePanel) panels[i];
            removeMergePanel(panel);
        }
    }
    
    /** @return Preferred size of editor top component  */
    public Dimension getPreferredSize() {
        Rectangle bounds = org.openide.TopManager.getDefault().getWindowManager().getCurrentWorkspace().getBounds();
        return new Dimension(bounds.width / 2, (int) (bounds.height / 1.25));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane mergeTabbedPane;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton helpButton;
    // End of variables declaration//GEN-END:variables
    
    public void addNotify() {
        super.addNotify();
                javax.swing.JRootPane root = getRootPane();
                if (root != null) root.setDefaultButton(okButton);
                /*
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                javax.swing.JRootPane root = getRootPane();
                if (root != null) root.setDefaultButton(okButton);
            }
        });
                 */
    }
    
    private void initListeners() {
        mergeTabbedPane.addMouseListener(new PopupMenuImpl());
        mergeTabbedPane.addChangeListener(this);
    }
    
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
        MergeNode node = new MergeNode(panel);
        nodesForPanels.put(panel, node);
        mergeTabbedPane.setSelectedComponent(panel);
        setActivatedNodes(new Node[] { node });
    }
    
    public synchronized void removeMergePanel(MergePanel panel) {
        mergeTabbedPane.remove(panel);
        nodesForPanels.remove(panel);
        if (mergeTabbedPane.getTabCount() == 0) {
            try {
                fireVetoableChange(PROP_ALL_CLOSED, null, null);
            } catch (PropertyVetoException pvex) {
                return ;
            }
            close();
        }
    }
    
    public MergePanel getSelectedMergePanel() {
        Component selected = mergeTabbedPane.getSelectedComponent();
        if (selected == null || !(selected instanceof MergePanel)) return null;
        return ((MergePanel) selected);
    }
    
    private static JPopupMenu createPopupMenu(MergePanel panel) {
        JPopupMenu popup = new JPopupMenuPlus();
        SystemAction[] actions = panel.getSystemActions();
        for (int i = 0; i < actions.length; i++) {
            if (actions[i] == null) {
                popup.addSeparator();
            } else if (actions[i] instanceof CallableSystemAction) {
                popup.add(((CallableSystemAction)actions[i]).getPopupPresenter());
                //add FileSystemAction to pop-up menu
            } else if (actions[i] instanceof FileSystemAction) {
                popup.add(((FileSystemAction)actions[i]).getPopupPresenter());
            }
        }
        return popup;
    }
    
    /** Shows given popup on given coordinations and takes care about the
     *  situation when menu can exceed screen limits.
     *  Copied from org.netbeans.core.windows.frames.DefaultContainerImpl
     */
    private static void showPopupMenu(JPopupMenu popup, Point p, Component comp) {
        SwingUtilities.convertPointToScreen(p, comp);
        Dimension popupSize = popup.getPreferredSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        
        if (p.x + popupSize.width > screenSize.width) {
            p.x = screenSize.width - popupSize.width;
        }
        if (p.y + popupSize.height > screenSize.height) {
            p.y = screenSize.height - popupSize.height;
        }
        SwingUtilities.convertPointFromScreen(p, comp);
        popup.show(comp, p.x, p.y);
    }
    
    /** Listen on tabbed pane merge panel selection */
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        MergePanel panel = (MergePanel) mergeTabbedPane.getSelectedComponent();
        if (panel != null) {
            Node node = (Node) nodesForPanels.get(panel);
            if (node != null) {
                setActivatedNodes(new Node[] { node });
            }
        }
    }
    
    /** Popup menu reaction implementation */
    private class PopupMenuImpl extends MouseUtils.PopupMouseAdapter {
        
        /** Called when the seqeunce of mouse events should lead to actual
         *  showing of the popup menu. */
        protected void showPopup(java.awt.event.MouseEvent mouseEvent) {
            TabbedPaneUI tabUI = mergeTabbedPane.getUI();
            int clickTab = tabUI.tabForCoordinate(mergeTabbedPane, mouseEvent.getX(), mouseEvent.getY());
            MergePanel panel = getSelectedMergePanel();
            if (panel == null) {
                return;
            }
            if (clickTab != -1) {
                //Click is on valid tab, not on empty area in tab
                showPopupMenu(createPopupMenu(panel), mouseEvent.getPoint(), mergeTabbedPane);
            }
        }
        
    }
    
    private class MergeNode extends org.openide.nodes.AbstractNode implements PropertyChangeListener, SaveCookie {
        
        private Reference mergePanelRef;
        
        public MergeNode(MergePanel panel) {
            super(org.openide.nodes.Children.LEAF);
            panel.addPropertyChangeListener(WeakListener.propertyChange(this, panel));
            mergePanelRef = new WeakReference(panel);
            getCookieSet().add(new CloseCookieImpl());
            //activateSave();
        }
        
        private void activateSave() {
            getCookieSet().add(this);
        }
        
        private void deactivateSave() {
            getCookieSet().remove(this);
        }
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (MergePanel.PROP_CAN_BE_SAVED.equals(propertyChangeEvent.getPropertyName())) {
                activateSave();
            } else if (MergePanel.PROP_CAN_NOT_BE_SAVED.equals(propertyChangeEvent.getPropertyName())) {
                deactivateSave();
            }
        }
        
        public void save() throws java.io.IOException {
            try {
                MergeDialogComponent.this.fireVetoableChange(PROP_PANEL_SAVE, null, mergePanelRef.get());
            } catch (PropertyVetoException vetoEx) {
                throw new java.io.IOException(vetoEx.getLocalizedMessage());
            }
            //System.out.println("SAVE called.");
            //deactivateSave();
        }
        
        private class CloseCookieImpl extends Object implements CloseCookie {
        
            public boolean close() {
                try {
                    MergeDialogComponent.this.fireVetoableChange(PROP_PANEL_CLOSING, null, mergePanelRef.get());
                } catch (PropertyVetoException vetoEx) {
                    return false;
                }
                removeMergePanel((MergePanel) mergePanelRef.get());
                return true;
            }
        }
        
    }
}
