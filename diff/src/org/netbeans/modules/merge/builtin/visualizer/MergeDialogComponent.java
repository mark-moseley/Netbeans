/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.TabbedPaneUI;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;

import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.awt.MouseUtils;
import org.openide.awt.JPopupMenuPlus;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;
import org.openide.util.*;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;
import org.netbeans.api.javahelp.Help;

/**
 * This is a component, that acts as a non modal dialog.
 * There are problems with accessibility to non-modal dialogs,
 * therefore this approach was chosen.
 * @author  Martin Entlicher
 */
public class MergeDialogComponent extends TopComponent implements ChangeListener {
    
    public static final String PROP_PANEL_CLOSING = "panelClosing"; // NOI18N
    public static final String PROP_ALL_CLOSED = "allPanelsClosed"; // NOI18N
    public static final String PROP_ALL_CANCELLED = "allPanelsCancelled"; // NOI18N
    public static final String PROP_PANEL_SAVE = "panelSave"; // NOI18N
    
    private Map<MergePanel, MergeNode> nodesForPanels = new HashMap<MergePanel, MergeNode>();
    
    /** Creates new form MergeDialogComponent */
    public MergeDialogComponent() {
        initComponents();
        initListeners();
        putClientProperty("PersistenceType", "Never");
        setName(org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "MergeDialogComponent.title"));
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(MergeDialogComponent.class, "ACSN_Merge_Dialog_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MergeDialogComponent.class, "ACSD_Merge_Dialog_Component")); // NOI18N
        mergeTabbedPane.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MergeDialogComponent.class, "ACSN_Merge_Tabbed_Pane")); // NOI18N
        mergeTabbedPane.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MergeDialogComponent.class, "ACSD_Merge_Tabbed_Pane")); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MergeDialogComponent.class);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mergeTabbedPane = new javax.swing.JTabbedPane();
        buttonsPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        mergeTabbedPane.setTabPlacement(javax.swing.JTabbedPane.BOTTOM);
        mergeTabbedPane.setPreferredSize(new java.awt.Dimension(600, 600));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mergeTabbedPane, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(okButton, org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_OK")); // NOI18N
        okButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergeDialogComponent.class).getString("ACS_BTN_OKA11yDesc")); // NOI18N
        okButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        buttonsPanel.add(okButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_Cancel")); // NOI18N
        cancelButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergeDialogComponent.class).getString("ACS_BTN_CancelA11yDesc")); // NOI18N
        cancelButton.addActionListener(formListener);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        buttonsPanel.add(cancelButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(helpButton, org.openide.util.NbBundle.getMessage(MergeDialogComponent.class, "BTN_Help")); // NOI18N
        helpButton.setToolTipText(org.openide.util.NbBundle.getBundle(MergeDialogComponent.class).getString("ACS_BTN_HelpA11yDesc")); // NOI18N
        helpButton.addActionListener(formListener);
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
    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        FormListener() {}
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == okButton) {
                MergeDialogComponent.this.okButtonActionPerformed(evt);
            }
            else if (evt.getSource() == cancelButton) {
                MergeDialogComponent.this.cancelButtonActionPerformed(evt);
            }
            else if (evt.getSource() == helpButton) {
                MergeDialogComponent.this.helpButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        // Add your handling code here:
        //List panelsToCloseList;
        Component[] panels;
        synchronized (this) {
            panels = mergeTabbedPane.getComponents();
        }
        boolean warning = false;
        ArrayList<String> unsavedPanelNames = new ArrayList<String>();
        ArrayList<SaveCookie> saveCookies = new ArrayList<SaveCookie>();
        for (int i = 0; i < panels.length; i++) {
            MergePanel panel = (MergePanel) panels[i];
            if((panel.getNumUnresolvedConflicts() > 0) && (!warning))
                warning = true;
            MergeNode node = nodesForPanels.get(panel);
            SaveCookie sc;
            if ((sc = node.getCookie(SaveCookie.class)) != null) {
                unsavedPanelNames.add(panel.getName());
                saveCookies.add(sc);
            }
        }
        Object ret;
        // XXX can format with one format string
        if (unsavedPanelNames.size() == 1) {           
            ret = DialogDisplayer.getDefault().notify(
            new NotifyDescriptor.Confirmation((warning)?NbBundle.getMessage(MergeDialogComponent.class,"SaveFileWarningQuestion",unsavedPanelNames.get(0)):
                                              NbBundle.getMessage(MergeDialogComponent.class,"SaveFileQuestion",unsavedPanelNames.get(0)),
                                              NotifyDescriptor.YES_NO_CANCEL_OPTION));
        } else if (unsavedPanelNames.size() > 1) {
            ret = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation((warning)?NbBundle.getMessage(MergeDialogComponent.class,"SaveFilesWarningQuestion",new Integer(unsavedPanelNames.size())):
                                                  NbBundle.getMessage(MergeDialogComponent.class,"SaveFilesQuestion",new Integer(unsavedPanelNames.size())),
                                                  NotifyDescriptor.YES_NO_CANCEL_OPTION));
        } else {
            if(warning){
                ret = DialogDisplayer.getDefault().notify(
                new NotifyDescriptor.Confirmation(NbBundle.getMessage(MergeDialogComponent.class,"WarningQuestion",new Integer(unsavedPanelNames.size())),
                                                  NotifyDescriptor.OK_CANCEL_OPTION));
                if(ret.equals(NotifyDescriptor.NO_OPTION))
                    return;
            }else
                ret = NotifyDescriptor.YES_OPTION;
        }
        if (!NotifyDescriptor.YES_OPTION.equals(ret) && !NotifyDescriptor.NO_OPTION.equals(ret)) return ;
        if (NotifyDescriptor.YES_OPTION.equals(ret) || NotifyDescriptor.OK_OPTION.equals(ret)) {
            for (SaveCookie sc: saveCookies) {
                IOException ioException = null;
                try {
                    sc.save();
                } catch (UserQuestionException uqex) {
                    Object status = DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Confirmation(uqex.getLocalizedMessage()));
                    if (status == NotifyDescriptor.OK_OPTION || status == NotifyDescriptor.YES_OPTION) {
                        boolean success;
                        try {
                            uqex.confirmed();
                            success = true;
                        } catch (IOException ioex) {
                            success = false;
                            ioException = ioex;
                        }
                        if (success) {
                            try {
                                sc.save();
                            } catch (IOException ioex) {
                                ioException = ioex;
                            }
                        }
                    } else if (status != NotifyDescriptor.NO_OPTION) {
                        // cancel
                        return ;
                    }
                } catch (IOException ioEx) {
                    ioException = ioEx;
                }
                if (ioException != null) {
                    ErrorManager.getDefault().notify(ioException);
                    // cancel the close - there was an error on save
                    return ;
                }
            }
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
    }//GEN-LAST:event_okButtonActionPerformed
    
    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        Help help = (Help) Lookup.getDefault().lookup(Help.class);
        help.showHelp(getHelpCtx());
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
        Rectangle bounds = WindowManager.getDefault().getCurrentWorkspace().getBounds();
        return new Dimension(bounds.width / 2, (int) (bounds.height / 1.25));
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton helpButton;
    private javax.swing.JTabbedPane mergeTabbedPane;
    private javax.swing.JButton okButton;
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

    public void open(Workspace workspace) {
        super.open(workspace);
        requestActive();
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
            Node node = nodesForPanels.get(panel);
            if (node != null) {
                setActivatedNodes(new Node[] { node });
            }
        }
    }
    
    /** Popup menu reaction implementation */
    private class PopupMenuImpl extends MouseUtils.PopupMouseAdapter {
        public PopupMenuImpl () {}
        
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
        
        private Reference<MergePanel> mergePanelRef;
        
        public MergeNode(MergePanel panel) {
            super(org.openide.nodes.Children.LEAF);
            panel.addPropertyChangeListener(WeakListeners.propertyChange(this, panel));
            mergePanelRef = new WeakReference<MergePanel>(panel);
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
                Throwable cause = vetoEx.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else {
                    throw new java.io.IOException(vetoEx.getLocalizedMessage());
                }
            }
            //System.out.println("SAVE called.");
            //deactivateSave();
        }
        
        private class CloseCookieImpl extends Object implements CloseCookie {
            public CloseCookieImpl () {}
        
            public boolean close() {
                try {
                    MergeDialogComponent.this.fireVetoableChange(PROP_PANEL_CLOSING, null, mergePanelRef.get());
                } catch (PropertyVetoException vetoEx) {
                    return false;
                }
                removeMergePanel(mergePanelRef.get());
                return true;
            }
        }
        
    }
}
