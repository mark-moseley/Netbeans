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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Jiri Rechtacek, Radek Matous
 */
public class PluginManagerUI extends javax.swing.JPanel  {
    private List<UpdateUnit> units = Collections.emptyList ();
    private UnitTable installedTable;
    private UnitTable availableTable;
    private UnitTable updateTable;
    private UnitTable localTable;
    private JButton closeButton;
    final RequestProcessor.Task initTask;
    private Object helpInstance = null;
    
    /** Creates new form PluginManagerUI */
    public PluginManagerUI (JButton closeButton) {
        this.closeButton = closeButton;
        initComponents ();
        postInitComponents ();
        //start initialize method as soon as possible
        initTask = Utilities.startAsWorkerThread (new Runnable () {
            public void run () {
                initialize ();
                setSelectedTab();
            }
        });
    }
    
    private Window findWindowParent () {
        Component c = this;
        while(c != null) {
            c = c.getParent ();
            if (c instanceof Window) {
                return (Window)c;
            }
        }
        return null;
    }
    
    void setWaitingState (boolean waitingState) {
        boolean enabled = !waitingState;

        for (Component c : tpTabs.getComponents()) {
            if (c instanceof UnitTab) {
                ((UnitTab) c).setWaitingState (waitingState);
            }            
        }               
        
        // the Close & Help buttons are always enabled
        bClose.setEnabled (true);
        bHelp.setEnabled (getHelpInstance () != null);
        
        Component parent = getParent ();
        Component rootPane = getRootPane ();
        if (parent != null) {
            parent.setEnabled (enabled);
        }
        if (rootPane != null) {
            if (enabled) {
                rootPane.setCursor (null);
            } else {
                rootPane.setCursor (Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR));
            }
        }
    }

    @Override
    public void addNotify () {
        super.addNotify ();
        //show progress for initialize method
        final Window w = findWindowParent ();
        if (w != null) {
            w.addWindowListener (new WindowAdapter (){
                @Override
                public void windowOpened (WindowEvent e) {
                    final WindowAdapter waa = this;
                    setWaitingState (true);
                    Utilities.startAsWorkerThread (PluginManagerUI.this, new Runnable () {
                        public void run () {
                            try {
                                initTask.waitFinished ();
                                w.removeWindowListener (waa);
                            } finally {
                                setWaitingState (false);
                            }
                        }
                    }, NbBundle.getMessage (PluginManagerUI.class, "UnitTab_InitAndCheckingForUpdates"));
                }
            });
        }
        HelpCtx.setHelpIDString (this, PluginManagerUI.class.getName ());
        tpTabs.addChangeListener (new ChangeListener () {
            public void stateChanged (ChangeEvent evt) {
                HelpCtx.setHelpIDString (PluginManagerUI.this, getHelpCtx ().getHelpID ());
            }
        });
    }
    
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        unitilialize ();
    }
    
    public void initialize () {
        try {
            units = UpdateManager.getDefault ().getUpdateUnits (Utilities.getUnitTypes ());
            // postpone later
            // getLocalDownloadSupport().getUpdateUnits();
            SwingUtilities.invokeAndWait (new Runnable () {
                public void run () {
                    refreshUnits ();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace (ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace (ex);
        }
    }
    
    //workaround of #96282 - Memory leak in org.netbeans.core.windows.services.NbPresenter
    public void unitilialize () {
        Utilities.startAsWorkerThread (new Runnable () {
            public void run () {
                //ensures that uninitialization runs after initialization
                initTask.waitFinished ();
                //ensure exclusivity between this uninitialization code and refreshUnits (which can run even after this dialog is disposed)
                synchronized(initTask) {
                    units = null;
                    installedTable = null;
                    availableTable = null;
                    updateTable = null;
                    localTable = null;
                }
            }
        }, 10000);
    }
    
    void setProgressComponent (final JLabel detail, final JComponent progressComponent) {
        if (SwingUtilities.isEventDispatchThread ()) {
            setProgressComponentInAwt (detail, progressComponent);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    setProgressComponentInAwt (detail, progressComponent);
                }
            });
        }
    }

    private UnitTable createTabForModel(UnitCategoryTableModel model) {
        UnitTable table = new UnitTable(model);
        selectFirstRow(table);
        
        UnitTab tab = new UnitTab(table, new UnitDetails(), this);
        tpTabs.add(tab, model.getTabIndex());
        decorateTabTitle(table);
        return table;
    }
    
    private void setProgressComponentInAwt (JLabel detail, JComponent progressComponent) {
        assert pProgress != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        pProgress.setVisible (true);

        java.awt.GridBagConstraints gridBagConstraints;
        
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        pProgress.add(progressComponent, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        pProgress.add(detail, gridBagConstraints);

        revalidate ();
    }
    
    void unsetProgressComponent (final JLabel detail, final JComponent progressComponent) {
        if (SwingUtilities.isEventDispatchThread ()) {
            unsetProgressComponentInAwt (detail, progressComponent);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    unsetProgressComponentInAwt (detail, progressComponent);
                }
            });
        }
    }

    private void setSelectedTab() {
        Component component = tpTabs.getSelectedComponent();
        if (component instanceof UnitTab) {
            UnitTab unitTab = (UnitTab) component;
            if (!unitTab.getModel().isTabEnabled()) {
                for (int i = 0; i < tpTabs.getComponentCount(); i++) {
                    component = tpTabs.getComponentAt(i);
                    if (component instanceof UnitTab) {
                        unitTab = (UnitTab) component;
                        if (unitTab.getModel().isTabEnabled() && unitTab.getModel().canBePrimaryTab()) {
                            tpTabs.setSelectedIndex(i);
                            break;
                        }
                    } else {
                        tpTabs.setSelectedIndex(i);
                        break;                        
                    }
                }
            }
        }
    }
    
    private void unsetProgressComponentInAwt (JLabel detail, JComponent progressComponent) {
        assert pProgress != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        pProgress.remove (detail);
        pProgress.remove (progressComponent);
        pProgress.setVisible (false);
        revalidate ();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tpTabs = new javax.swing.JTabbedPane();
        pProgress = new javax.swing.JPanel();
        bClose = closeButton;
        bHelp = new javax.swing.JButton();

        tpTabs.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        tpTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tpTabsStateChanged(evt);
            }
        });

        pProgress.setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(bClose, org.openide.util.NbBundle.getMessage(PluginManagerUI.class, "UnitTab_bClose_Text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bHelp, org.openide.util.NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI.bHelp.text")); // NOI18N
        bHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bHelpActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(pProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 123, Short.MAX_VALUE)
                        .add(bClose)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bHelp))
                    .add(tpTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 813, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tpTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(pProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(bHelp)
                        .add(bClose, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        tpTabs.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PluginManagerUI.class, "ACN_Tabs")); // NOI18N
        tpTabs.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PluginManagerUI.class, "ACD_Tabs")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(PluginManagerUI.class, "ACN_PluginManagerUI")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(PluginManagerUI.class, "ACD_PluginManagerUI")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
private void tpTabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tpTabsStateChanged
    Component component = ((JTabbedPane) evt.getSource ()).getSelectedComponent ();
    if (component instanceof SettingsTab) {
        ((SettingsTab)component).getSettingsTableModel ().refreshModel ();
    }
}//GEN-LAST:event_tpTabsStateChanged

private void bHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bHelpActionPerformed
        try {
            Object help = getHelpInstance ();
            if (help == null) {
                return;
            }
            Method showHelpM = help.getClass ().getMethod ("showHelp", HelpCtx.class); // NOI18N
            if (showHelpM != null) {
                showHelpM.invoke (help, getHelpCtx ());
            }
        } catch (Exception ex) {
            Logger.getLogger (PluginManagerUI.class.getName ()).log (Level.INFO, ex.getLocalizedMessage (), ex);
        }
}//GEN-LAST:event_bHelpActionPerformed

    @SuppressWarnings("unchecked")
    private Object getHelpInstance () {
        if (helpInstance == null) {
            try {
                Class clazz = Class.forName ("org.netbeans.api.javahelp.Help", // NOI18N
                        false, Thread.currentThread().getContextClassLoader());
                if (clazz == null) {
                    return null;
                }
                helpInstance = Lookup.getDefault ().lookup (clazz);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger (PluginManagerUI.class.getName ()).log (Level.INFO, ex.getLocalizedMessage (), ex);
            }
        }
        return helpInstance;
    }

    private boolean isValidHelpID (String id) {
        boolean res = true;
        try {
            Object help = getHelpInstance ();
            if (help == null) {
                return res;
            }
            Method isValidIDM = help.getClass ().getMethod ("isValidID", String.class, boolean.class); // NOI18N
            if (isValidIDM != null) {
                Object resO = isValidIDM.invoke (help, id, true);
                if (resO instanceof Boolean) {
                    res = (Boolean) resO;
                } else {
                    res = true;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger (PluginManagerUI.class.getName ()).log (Level.INFO, ex.getLocalizedMessage (), ex);
        }
        return res;
    }

    private HelpCtx getHelpCtx() {
        String id = PluginManagerUI.class.getName ();
        Component c = tpTabs.getSelectedComponent ();
        if (c instanceof UnitTab) {
            id = ((UnitTab) c).getHelpId ();
        } else if (c instanceof SettingsTab) {
            id = SettingsTab.class.getName ();
        }
        if (isValidHelpID (id)) {
            Logger.getLogger (PluginManagerUI.class.getName ()).log (Level.FINE, "HelpId is " + id);
        } else {
            Logger.getLogger (PluginManagerUI.class.getName ()).log (Level.INFO, id + " looks no valid HelpCtx. Is valid? " + isValidHelpID (id));
        }
        return new HelpCtx (id);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bClose;
    private javax.swing.JButton bHelp;
    private javax.swing.JPanel pProgress;
    private javax.swing.JTabbedPane tpTabs;
    // End of variables declaration//GEN-END:variables
    
    private void postInitComponents () {
        Containers.initNotify ();
        updateTable = createTabForModel(new UpdateTableModel(units));
        availableTable = createTabForModel(new AvailableTableModel (units));
        localTable = createTabForModel(new LocallyDownloadedTableModel (new LocalDownloadSupport()));
        installedTable = createTabForModel(new InstalledTableModel(units));
        
        SettingsTab tab = new SettingsTab (this);
        tpTabs.add (tab,4);
        tpTabs.setTitleAt(4, tab.getDisplayName());
        bHelp.setEnabled (getHelpInstance () != null);
    }
    
    void decorateTabTitle (UnitTable table) {
        UnitCategoryTableModel model = (UnitCategoryTableModel)table.getModel();
        int index = model.getTabIndex();
        tpTabs.setTitleAt (index, model.getDecoratedTabTitle());
        tpTabs.setEnabledAt(index, model.isTabEnabled());
        tpTabs.setToolTipTextAt(index, model.getTabTooltipText()); // NOI18N
    }
    
    private int findRowWithFirstUnit (UnitCategoryTableModel model) {
        for (int row = 0; row <= model.getRowCount (); row++) {
            if (model.getUnitAtRow (row) != null) {
                return row;
            }
        }
        return -1;
    }
    
    private void selectFirstRow (UnitTable table) {
        if (table.getSelectedRow () == -1) {
            UnitCategoryTableModel model = (UnitCategoryTableModel)table.getModel ();
            int row = findRowWithFirstUnit (model);
            if (row != -1) {
                table.getSelectionModel ().setSelectionInterval (row, row);
            }
        }
    }
    
    private void refreshUnits () {
        //ensure exclusivity between this refreshUnits code(which can run even after this dialog is disposed) and uninitialization code
        synchronized(initTask) {
            //return immediatelly if uninialization(after removeNotify) was alredy called
            if (units == null) return;
            //TODO: REVIEW THIS CODE - problem is that is called from called from AWT thread
            //UpdateManager.getDefault().getUpdateUnits() should never be called fromn AWT because it may cause
            //long terming starvation because in fact impl. of this method calls AutoUpdateCatalogCache.getCatalogURL
            //which is synchronized and may wait until cache is created
            //even more AutoUpdateCatalog.getUpdateItems () can at first start call refresh and thus writeToCache again
            units = UpdateManager.getDefault().getUpdateUnits(Utilities.getUnitTypes());
            UnitCategoryTableModel installTableModel = ((UnitCategoryTableModel)installedTable.getModel());
            UnitCategoryTableModel updateTableModel = ((UnitCategoryTableModel)updateTable.getModel());
            UnitCategoryTableModel availableTableModel = ((UnitCategoryTableModel)availableTable.getModel());
            LocallyDownloadedTableModel localTableModel = ((LocallyDownloadedTableModel)localTable.getModel());
            
            updateTableModel.setUnits(units);
            installTableModel.setUnits(units);
            availableTableModel.setUnits(units);
            localTableModel.setUnits(units);
            selectFirstRow(installedTable);
            selectFirstRow(updateTable);
            selectFirstRow(availableTable);
            selectFirstRow(localTable);
            decorateTabTitle(updateTable);
            decorateTabTitle(availableTable);
            decorateTabTitle(localTable);
            decorateTabTitle(installedTable);
            Component[] components = tpTabs.getComponents();
            for (Component component : components) {
                if (component instanceof UnitTab) {
                    UnitTab tab = (UnitTab)component;
                    tab.refreshState();
                }
            }
            setSelectedTab();
        }        
    }
        
    private LocalDownloadSupport getLocalDownloadSupport() {
            return  ((LocallyDownloadedTableModel)localTable.getModel()).getLocalDownloadSupport();
    }
    
    
    static boolean canContinue (String message) {
        return NotifyDescriptor.YES_OPTION.equals (DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Confirmation (message)));
    }
    //TODO: all the request for refresh should be cancelled if there is already one such running refresh task
    public void updateUnitsChanged () {
        refreshUnits ();
    }
    
    public void tableStructureChanged () {
        installedTable.resortByDefault ();
        ((UnitCategoryTableModel) installedTable.getModel ()).fireTableStructureChanged ();
        installedTable.setColumnsSize ();
        installedTable.resetEnableRenderer ();
        updateTable.resortByDefault ();
        ((UnitCategoryTableModel) updateTable.getModel ()).fireTableStructureChanged ();
        updateTable.setColumnsSize ();
        availableTable.resortByDefault ();
        ((UnitCategoryTableModel) availableTable.getModel ()).fireTableStructureChanged ();
        availableTable.setColumnsSize ();
    }
    
    public void buttonsChanged () {
        Component c = tpTabs.getSelectedComponent ();
        if (c instanceof UnitTab) {
            ((UnitTab) c).refreshState ();
        }
    }
    
}
