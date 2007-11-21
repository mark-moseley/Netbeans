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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * @author  Radek Matous, Jirka Rechtacek
 */
public class SettingsTab extends javax.swing.JPanel {
    private DetailsPanel details;
    private JScrollPane  scrollerForDetails;
    private JTable table;
    private JScrollPane  scrollerForTable;

    private Action removeAction;
    private Action editAction;    
    private Action addAction;   
    private Listener listener;
    private PluginManagerUI manager;
    
    /** Creates new form UnitTab */
    public SettingsTab(PluginManagerUI manager) {
        this.manager = manager;
        initComponents();
        scrollerForTable = new JScrollPane();
        table = new Table();
        scrollerForTable.setViewportView(table);
        
        scrollerForDetails = new JScrollPane();
        details = new DetailsPanel();
        scrollerForDetails.setViewportView(details);
        
        spTab.setLeftComponent(scrollerForTable);
        spTab.setRightComponent(scrollerForDetails);
        
        table.setModel(new SettingsTableModel());        
        TableColumnModel columnModel = table.getColumnModel();
        columnModel.getColumn(1).setCellRenderer(new UpdateProviderRenderer ());
        
        
        cbCheckPeriod.setModel(new DefaultComboBoxModel(new String [] {
            getMessage("CTL_Update_every_startup"),
            getMessage("CTL_Update_every_day"),
            getMessage("CTL_Update_every_week"),
            getMessage("CTL_Update_every_2weeks"),
            getMessage("CTL_Update_every_month"),
            getMessage("CTL_Update_never")
        }));
        cbCheckPeriod.setSelectedIndex (getAutoUpdatePeriod ());
        //cbModules.setSelected(Utilities.modulesOnly());
        //cbPlugins.setSelected(!Utilities.modulesOnly());
        cbGlobalInstall.setSelected(Utilities.isGlobalInstallation());
        getSettingsTableModel ().setSettingsTab (this);
        TableColumn activeColumn = table.getColumnModel ().getColumn (0);
        activeColumn.setMaxWidth (table.getTableHeader ().getHeaderRect (0).width);
        
        editAction = new EditAction();
        removeAction = new RemoveAction();
        addAction = new AddAction();
        addButton.setAction(addAction);        
    }

    public String getDisplayName () {
        return NbBundle.getMessage (SettingsTab.class, "SettingsTab_displayName"); //NOI18N
    }
    
    PluginManagerUI getPluginManager () {
        return manager;
    }
        
    private void addListener () {
        if (listener == null) {
            listener = new Listener ();
            table.getSelectionModel ().addListSelectionListener (listener);
            getSettingsTableModel ().addTableModelListener (listener);
        }
    }
    
    private void removeListener () {
        if (listener != null) {
            table.getSelectionModel ().removeListSelectionListener (listener);
            getSettingsTableModel ().removeTableModelListener (listener);
            listener = null;
        }
    }
    
    @Override
    public void addNotify () {
        super.addNotify ();
        Utilities.startAsWorkerThread (new Runnable () {
            public void run () {
                getSettingsTableModel ().refreshModel ();                
            }
        });        
        addListener ();                
    }
    
    @Override
    public void removeNotify () {
        super.removeNotify ();
        removeListener ();
    }
            
    private static String getMessage(final String key) {
        return NbBundle.getMessage(SettingsTab.class, key); //NOI18N
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pluginsViewGroup = new javax.swing.ButtonGroup();
        lUpdateCenters = new javax.swing.JLabel();
        spTab = new javax.swing.JSplitPane();
        addButton = new javax.swing.JButton();
        lConnection = new javax.swing.JLabel();
        jSeparatorConnection = new javax.swing.JSeparator();
        lCheckPeriod = new javax.swing.JLabel();
        cbCheckPeriod = new javax.swing.JComboBox();
        bProxy = new javax.swing.JButton();
        lGeneral = new javax.swing.JLabel();
        cbGlobalInstall = new javax.swing.JCheckBox();
        jSeparatorAdvanced = new javax.swing.JSeparator();

        org.openide.awt.Mnemonics.setLocalizedText(lUpdateCenters, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lUpdateCenters.text")); // NOI18N

        spTab.setBorder(null);
        spTab.setDividerLocation(370);
        spTab.setResizeWeight(0.5);
        spTab.setOneTouchExpandable(true);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.addButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lConnection, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lConnection.text")); // NOI18N

        lCheckPeriod.setLabelFor(cbCheckPeriod);
        org.openide.awt.Mnemonics.setLocalizedText(lCheckPeriod, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lCheckPeriod.text")); // NOI18N

        cbCheckPeriod.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCheckPeriodActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bProxy, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.bProxy.text")); // NOI18N
        bProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bProxyActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lGeneral, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lGeneral.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbGlobalInstall, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.cbSharedInstall.text")); // NOI18N
        cbGlobalInstall.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 7, 0, 0));
        cbGlobalInstall.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbGlobalInstallActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(addButton)
                            .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE))
                        .add(1, 1, 1))
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(lCheckPeriod)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbCheckPeriod, 0, 506, Short.MAX_VALUE)
                        .add(58, 58, 58)
                        .add(bProxy))
                    .add(layout.createSequentialGroup()
                        .add(lGeneral)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparatorAdvanced, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 711, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(lConnection)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jSeparatorConnection, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 565, Short.MAX_VALUE))
                    .add(lUpdateCenters)
                    .add(cbGlobalInstall))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(lUpdateCenters)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 37, Short.MAX_VALUE)
                .add(4, 4, 4)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(layout.createSequentialGroup()
                        .add(addButton)
                        .add(11, 11, 11)
                        .add(jSeparatorConnection, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(lConnection))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bProxy)
                    .add(lCheckPeriod)
                    .add(cbCheckPeriod, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lGeneral, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jSeparatorAdvanced, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(cbGlobalInstall)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbGlobalInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbGlobalInstallActionPerformed
    if (cbGlobalInstall.isSelected ()) {
        // check write permissions
        Collection<File> dirs = Utilities.sharedDirs ();
        if (dirs.isEmpty ()) {
            NotifyDescriptor nd = new NotifyDescriptor (NbBundle.getMessage (SettingsTab.class, "SettingsTab.cbSharedInstall.NoSharedMessage"),
                    NbBundle.getMessage (SettingsTab.class, "SettingsTab.cbSharedInstall.NoSharedTitle"),
                    NotifyDescriptor.ERROR_MESSAGE,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object [] { NotifyDescriptor.OK_OPTION }, // options
                    null); // default option
            DialogDisplayer.getDefault ().notifyLater (nd);
            cbGlobalInstall.setSelected (false);
            return ;
        } else {
            for (File f : dirs) {
                if (f.exists () && f.isDirectory () && ! f.canWrite ()) {
                    NotifyDescriptor nd = new NotifyDescriptor (NbBundle.getMessage (SettingsTab.class, "SettingsTab.cbSharedInstall.ReadOnlyMessage", f),
                            NbBundle.getMessage (SettingsTab.class, "SettingsTab.cbSharedInstall.ReadOnlyTitle"),
                            NotifyDescriptor.ERROR_MESSAGE,
                            NotifyDescriptor.ERROR_MESSAGE,
                            new Object [] { NotifyDescriptor.OK_OPTION }, // options
                            null); // default option
                    DialogDisplayer.getDefault ().notifyLater (nd);
                    cbGlobalInstall.setSelected (false);
                    return ;
                }
            }
            Utilities.setGlobalInstallation (true);
        }
    } else {
        Utilities.setGlobalInstallation (false);
    }
}//GEN-LAST:event_cbGlobalInstallActionPerformed

private void cbCheckPeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCheckPeriodActionPerformed
    setAutoUpdatePeriod (cbCheckPeriod.getSelectedIndex ());
}//GEN-LAST:event_cbCheckPeriodActionPerformed
    
private void bProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bProxyActionPerformed
    OptionsDisplayer.getDefault ().open ("General"); //NOI18N
}//GEN-LAST:event_bProxyActionPerformed

public SettingsTableModel getSettingsTableModel() {
    return ((SettingsTableModel)table.getModel());
}

private class Listener implements ListSelectionListener,  TableModelListener {
    public void valueChanged(ListSelectionEvent arg0) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                modelOrSelectionChanged ();
            }
        });
    }
       
    public void tableChanged(TableModelEvent arg0) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                modelOrSelectionChanged ();
            }
        });
    }
    
    private void modelOrSelectionChanged() {
        int rowIndex = table.getSelectedRow();
        if (rowIndex != -1) {
            UpdateUnitProvider uup = ((SettingsTableModel)table.getModel()).getUpdateUnitProvider(rowIndex);
            if (uup != null) {
                StringBuffer sb = new StringBuffer();
                details.setTitle(uup.getDisplayName());
                long lastTime = lastModification(uup);
                if (lastTime > 0) {
                    sb.append("<b>" + NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime", //NOI18N
                            "</b>")).append(new SimpleDateFormat().format(new Date(lastTime))).append("<br>");
                } else {
                    String never = getMessage("UnitTab_ReloadTime_Never");//NOI18N
                    sb.append("<b>" + NbBundle.getMessage(UnitTab.class, "UnitTab_ReloadTime", "</b>")).append(never).append("<br>");//NOI18N                            
                }                                
                URL u = uup.getProviderURL();
                String desc = uup.getDescription () == null ? "" : uup.getDescription ();
                if (u != null) {
                    if (desc.length() > 0) {
                        sb.append("<b>" + getMessage("SettingsTab_UpdateUnitProvider_Description") + "</b><br>"); // NOI18N
                        sb.append(desc + "<br><br>"); // NOI18N
                    }
                    sb.append("<b>" + getMessage("SettingsTab_UpdateUnitProvider_URL") + // NOI18N
                            " </b><a href=\"" + u.toExternalForm() + "\">" + u.toExternalForm() + "</a><br>"); // NOI18N
                }

                details.setText(sb.toString());
                details.setActionListener(removeAction);
                details.setActionListener2(editAction);
            }
        } else {
            details.setTitle(null);
            details.setText(null);
            details.setActionListener2(null);            
            details.setActionListener(null);
            
            ListSelectionModel lsm = table.getSelectionModel();
            lsm.setSelectionInterval(0, 0);
        }
    }
    }

    private long lastModification(UpdateUnitProvider unitProvider) {
        Preferences p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");//NOI18N
        return p.getLong(unitProvider.getName()+"_"+UnitTab.PROP_LAST_CHECK, 0);//NOI18N
    }

    private void setData (final UpdateUnitProvider provider, UpdateUnitProviderPanel panel) {
        provider.setDisplayName (panel.getProviderName ());
        boolean forceRead = false;
        boolean refreshModel = false;
        try {
            URL oldUrl = provider.getProviderURL ();
            URL newUrl = new URL (panel.getProviderURL ());
            if (! oldUrl.toExternalForm ().equals (newUrl.toExternalForm ())) {
                provider.setProviderURL (newUrl);
                refreshModel = true;
                forceRead = true;
            }
        } catch(MalformedURLException mex) {
            Exceptions.printStackTrace (mex);
        }
        boolean oldValue = provider.isEnabled ();
        if (oldValue != panel.isActive ()) {
            refreshModel = true;
        }
        if (refreshModel) {
            provider.setEnable (panel.isActive ());
            if (oldValue && ! forceRead) {
                // was enabled and won't be more -> remove it from model
                setWaitingState (true);
                Utilities.startAsWorkerThread (new Runnable () {
                    public void run () {
                        try {
                            getPluginManager ().updateUnitsChanged ();
                        } finally {
                            setWaitingState (false);
                        }
                    }
                });
            } else {
                final boolean force = forceRead;
                // was enabled and won't be more -> add it from model and read its content
                setWaitingState (true);
                Utilities.startAsWorkerThread (new Runnable () {

                    public void run () {
                        try {
                            Utilities.presentRefreshProviders (Collections.singleton (provider), getPluginManager (), force);
                            getPluginManager ().updateUnitsChanged ();
                        } finally {
                            setWaitingState (false);
                        }
                    }
                    
                });
            }
        }
    }
    
    private static DialogDescriptor getCustomizerDescriptor (UpdateUnitProviderPanel panel) {
        JButton bOK = panel.getOKButton ();
        Object[] options = new Object[2];
        options[0] = bOK;
        options[1] = DialogDescriptor.CANCEL_OPTION;
        
        DialogDescriptor descriptor = new DialogDescriptor (panel,panel.getDisplayName (),true,options,DialogDescriptor.OK_OPTION,DialogDescriptor.DEFAULT_ALIGN, null, null);
        return descriptor;
    }
    
    private class EditAction extends AbstractAction {
        EditAction () {
            super(UnitTab.textForKey("SettingsTab.EditButton.text"));
            putValue(MNEMONIC_KEY, UnitTab.mnemonicForKey("SettingsTab.EditButton.text"));
        }
        
        public void actionPerformed(ActionEvent arg0) {
            final int rowIndex = table.getSelectedRow();
            if (rowIndex != -1) {
                final UpdateUnitProvider provider = getSettingsTableModel().getUpdateUnitProvider(rowIndex);
                if (provider == null) return;
                final UpdateUnitProviderPanel panel = new UpdateUnitProviderPanel(provider.isEnabled(),
                        provider.getDisplayName(), // display name
                        provider.getProviderURL().toExternalForm(), // URL
                        true); // editing
                DialogDescriptor descriptor = getCustomizerDescriptor(panel);
                panel.getOKButton().addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent arg0) {
                        setData(provider, panel);
                        //getSettingsTableModel().refreshModel();
                        table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
                        
                    }
                });
                DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
                if (listener != null) {
                    listener.modelOrSelectionChanged ();
                }
            }
        }
    }
    
    private class RemoveAction extends AbstractAction {
        RemoveAction() {
            super(UnitTab.textForKey("SettingsTab.RemoveButton.text"));//NOI18N
            putValue (MNEMONIC_KEY, UnitTab.mnemonicForKey ("SettingsTab.RemoveButton.text"));//NOI18N
        }
        public void actionPerformed(ActionEvent arg0) {
            SettingsTableModel model = getSettingsTableModel();
            int[] rowIndexes = table.getSelectedRows();
            for (int rowIndex : rowIndexes) {
                if (rowIndex != -1) {
                    UpdateUnitProvider provider = model.getUpdateUnitProvider(rowIndex);
                    if (provider != null) {//NOI18N
                        String msg = NbBundle.getMessage(SettingsTab.class, "SettingsTab.bRemove.message",provider.getDisplayName());
                        NotifyDescriptor nd = new NotifyDescriptor.Confirmation (msg);
                        nd.setOptionType(NotifyDescriptor.YES_NO_OPTION);
                        Object object = DialogDisplayer.getDefault ().notify (nd);
                        if (NotifyDescriptor.YES_OPTION.equals (object)) {
                            model.remove(rowIndex);
                        }
                    }
                }
            }
            model.refreshModel();
            if (rowIndexes.length > 0) {
                if (model.getRowCount() > rowIndexes[0]) {
                    table.getSelectionModel().setSelectionInterval(rowIndexes[0], rowIndexes[0]);
                } else {
                    table.getSelectionModel().setSelectionInterval(0, 0);
                }
            }
        }
    }

    private class AddAction extends AbstractAction {
        AddAction() {
            super(UnitTab.textForKey("SettingsTab.AddButton.text"));
            putValue (MNEMONIC_KEY, UnitTab.mnemonicForKey ("SettingsTab.AddButton.text"));
        }
        public void actionPerformed(ActionEvent arg0) {
            final UpdateUnitProviderPanel panel = new UpdateUnitProviderPanel(true,
                    NbBundle.getMessage(SettingsTab.class, "SettingsTab_NewProviderName"), // NOI18N
                    NbBundle.getMessage(SettingsTab.class, "SettingsTab_NewProviderURL"), // NOI18N
                    false);
            DialogDescriptor descriptor = getCustomizerDescriptor(panel);
            panel.getOKButton().addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent arg0) {
                    try {
                        getSettingsTableModel().add
                                (panel.getProviderName(),
                                panel.getProviderName(),
                                new URL(panel.getProviderURL()),
                                panel.isActive());
                        getSettingsTableModel().refreshModel();
                        SettingsTableModel model = getSettingsTableModel();
                        for (int i = 0; i < model.getRowCount(); i++) {
                            String providerName = model.getValueAt(i, 1).toString();
                            if (panel.getProviderName() != null && panel.getProviderName().equals(providerName)) {
                                table.getSelectionModel().setSelectionInterval(i, i);
                            }
                            
                        }
                        
                        
                    } catch(MalformedURLException mex) {
                        Exceptions.printStackTrace(mex);
                    }
                }
            });
            DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
            
        }
    }
    
    private class Table extends JTable {
        public Table() {
            setShowGrid(false);
            setIntercellSpacing(new Dimension(0, 0));            
            getAccessibleContext().setAccessibleName(NbBundle.getMessage(UnitTable.class, "ACN_UpdateCentersTable")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(UnitTab.class, "ACD_UpdateCentersTable")); // NOI18N        
            lUpdateCenters.setLabelFor(this);
        }

        @Override
        public void addNotify() {
            super.addNotify();
            getParent().setBackground(getBackground());            
        }
        
        
        @Override
        public Component prepareRenderer(TableCellRenderer renderer,
                int rowIndex, int vColIndex) {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            Color bgColor = getBackground();
            Color bgColorDarker = UnitTable.getDarkerColor(bgColor);
            
            if (isRowSelected(rowIndex)) {
                c.setForeground(getSelectionForeground());
            } else {
                c.setForeground(getForeground());
            }
            
            if (!isCellSelected(rowIndex, vColIndex)) {
                if (rowIndex % 2 == 0) {
                    c.setBackground(bgColorDarker);
                } else {
                    c.setBackground(bgColor);
                }
            }
            
            return c;
        }
        
    }

    int getAutoUpdatePeriod () {
        return getAutoupdatePreferences ().getInt ("period", 2 /*EVERY_WEEK*/); // NOI18N
    }
    
    void setAutoUpdatePeriod (int period) {
        if (period != getAutoUpdatePeriod()) {
            getAutoupdatePreferences ().putInt ("period", period); // NOI18N
        }
    }
    
    private static Preferences getAutoupdatePreferences () {
        return NbPreferences.root ().node ("org/netbeans/modules/autoupdate");
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton bProxy;
    private javax.swing.JComboBox cbCheckPeriod;
    private javax.swing.JCheckBox cbGlobalInstall;
    private javax.swing.JSeparator jSeparatorAdvanced;
    private javax.swing.JSeparator jSeparatorConnection;
    private javax.swing.JLabel lCheckPeriod;
    private javax.swing.JLabel lConnection;
    private javax.swing.JLabel lGeneral;
    private javax.swing.JLabel lUpdateCenters;
    private javax.swing.ButtonGroup pluginsViewGroup;
    private javax.swing.JSplitPane spTab;
    // End of variables declaration//GEN-END:variables
 
    void setWaitingState (boolean waitingState) {
        getPluginManager ().setWaitingState (waitingState);
        table.setEnabled (! waitingState);
    }
    
    private static class UpdateProviderRenderer extends  DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel renderComponent = (JLabel) super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);

            if (value instanceof UpdateUnitProvider) {
                UpdateUnitProvider u = (UpdateUnitProvider) value;
                CATEGORY state = u.getCategory();
                if (CATEGORY.BETA.equals(state)) {
                    renderComponent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/autoupdate/ui/resources/icon-beta.png"))); // NOI18N
                } else if (CATEGORY.COMMUNITY.equals(state)) {
                    renderComponent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/autoupdate/ui/resources/icon-community.png"))); // NOI18N
                } else if (CATEGORY.STANDARD.equals(state)) {
                    renderComponent.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/autoupdate/ui/resources/icon-standard.png"))); // NOI18N
                }
                renderComponent.setText (u.getDisplayName());
                renderComponent.setHorizontalAlignment(SwingConstants.LEFT);
            }
            Component retval = renderComponent;
            return retval;
        }
    }
}
