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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 * @author  Radek Matous
 */
public class SettingsTab extends javax.swing.JPanel {
    private DetailsPanel details;
    private JScrollPane  scrollerForDetails;
    private JTable table;
    private JScrollPane  scrollerForTable;
    private static final RequestProcessor RP = new RequestProcessor();
    private Action removeAction;
    private Action editAction;    
    private Action addAction;   
    private Listener listener;
    
    /** Creates new form UnitTab */
    public SettingsTab(PluginManagerUI manager) {
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
        
        cbCheckPeriod.setModel(new DefaultComboBoxModel(new String [] {
            getMessage("CTL_Update_every_startup"),
            getMessage("CTL_Update_every_day"),
            getMessage("CTL_Update_every_week"),
            getMessage("CTL_Update_every_2weeks"),
            getMessage("CTL_Update_every_month"),
            getMessage("CTL_Update_never")
        }));
        cbCheckPeriod.setSelectedIndex (getAutoUpdatePeriod ());
        cbModules.setSelected(Utilities.modulesOnly());
        cbPlugins.setSelected(!Utilities.modulesOnly());
        cbGlobalInstall.setSelected(Utilities.isGlobalInstallation());
        getSettingsTableModel ().setPluginManager (manager);
        TableColumn activeColumn = table.getColumnModel ().getColumn (0);
        activeColumn.setMaxWidth (table.getTableHeader ().getHeaderRect (0).width);
        
        editAction = new EditAction();
        removeAction = new RemoveAction();
        addAction = new AddAction();
        addButton.setAction(addAction);        
        addListener ();        
    }

    public String getDisplayName () {
        return NbBundle.getMessage (SettingsTab.class, "SettingsTab_displayName"); //NOI18N
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
        Utilities.startAsWorkerThread(new Runnable() {
            public void run() {
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
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
        lPluginsView = new javax.swing.JLabel();
        cbPlugins = new javax.swing.JRadioButton();
        cbModules = new javax.swing.JRadioButton();
        cbGlobalInstall = new javax.swing.JCheckBox();
        jSeparatorAdvanced = new javax.swing.JSeparator();

        lUpdateCenters.setLabelFor(spTab);
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

        org.openide.awt.Mnemonics.setLocalizedText(lPluginsView, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.lPluginsView.text")); // NOI18N

        pluginsViewGroup.add(cbPlugins);
        org.openide.awt.Mnemonics.setLocalizedText(cbPlugins, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.cbPlugins.text")); // NOI18N
        cbPlugins.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbPlugins.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbPlugins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbPluginsActionPerformed(evt);
            }
        });

        pluginsViewGroup.add(cbModules);
        org.openide.awt.Mnemonics.setLocalizedText(cbModules, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.cbModules.text")); // NOI18N
        cbModules.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbModules.setMargin(new java.awt.Insets(0, 0, 0, 0));
        cbModules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbModulesActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbGlobalInstall, org.openide.util.NbBundle.getMessage(SettingsTab.class, "SettingsTab.cbSharedInstall.text")); // NOI18N
        cbGlobalInstall.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbGlobalInstall.setMargin(new java.awt.Insets(0, 0, 0, 0));
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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(cbGlobalInstall))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(addButton)
                            .add(spTab, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 541, Short.MAX_VALUE))
                        .add(1, 1, 1))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(12, 12, 12)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(lPluginsView, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(lCheckPeriod))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(cbPlugins, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 151, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(cbModules)
                                    .add(cbCheckPeriod, 0, 253, Short.MAX_VALUE))
                                .add(58, 58, 58)
                                .add(bProxy))
                            .add(layout.createSequentialGroup()
                                .add(lGeneral)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jSeparatorAdvanced, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(lConnection)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jSeparatorConnection, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(lUpdateCenters)))
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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(lPluginsView)
                    .add(layout.createSequentialGroup()
                        .add(cbPlugins)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbModules)))
                .add(21, 21, 21)
                .add(cbGlobalInstall)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbGlobalInstallActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbGlobalInstallActionPerformed
    Utilities.setGlobalInstallation(cbGlobalInstall.isSelected());
}//GEN-LAST:event_cbGlobalInstallActionPerformed

private void cbCheckPeriodActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCheckPeriodActionPerformed
    setAutoUpdatePeriod (cbCheckPeriod.getSelectedIndex ());
}//GEN-LAST:event_cbCheckPeriodActionPerformed

private void cbModulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbModulesActionPerformed
    cbPluginsActionPerformed(evt);
}//GEN-LAST:event_cbModulesActionPerformed

    private void cbPluginsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPluginsActionPerformed
    if (Utilities.modulesOnly() != cbModules.isSelected()) {
        Utilities.setModulesOnly(cbModules.isSelected());
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                Utilities.presentRefreshProviders(getSettingsTableModel().getPluginManager(), false);
                getSettingsTableModel().getPluginManager().tableStructureChanged();
                getSettingsTableModel().getPluginManager().updateUnitsChanged();
            }
        });
    }
}//GEN-LAST:event_cbPluginsActionPerformed
    
private void bProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bProxyActionPerformed
    OptionsDisplayer.getDefault ().open ("General"); //NOI18N
}//GEN-LAST:event_bProxyActionPerformed

public SettingsTableModel getSettingsTableModel() {
    return ((SettingsTableModel)table.getModel());
}

private class Listener implements ListSelectionListener,  TableModelListener {
    public void valueChanged(ListSelectionEvent arg0) {
        modelOrSelectionChanged();
    }
       
    public void tableChanged(TableModelEvent arg0) {
        modelOrSelectionChanged();
    }
    
    private void modelOrSelectionChanged() {
        int rowIndex = table.getSelectedRow();
        if (rowIndex != -1) {
            UpdateUnitProvider uup = ((SettingsTableModel)table.getModel()).getUpdateUnitProvider(rowIndex);
            if (uup != null) {
                StringBuffer sb = new StringBuffer();
                details.setTitle(uup.getDisplayName());
                URL u = uup.getProviderURL();
                String desc = uup.getDescription () == null ? "" : uup.getDescription ();
                if (u != null) {
                    if (desc.length () > 0) {
                        sb.append("<b>" + getMessage("SettingsTab_UpdateUnitProvider_Description") + "</b><br>"); // NOI18N
                        sb.append (desc + "<br><br>"); // NOI18N
                    }
                    sb.append("<b>" + getMessage("SettingsTab_UpdateUnitProvider_URL") +  // NOI18N
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

    private void setData (final UpdateUnitProvider provider, UpdateUnitProviderPanel panel) {
        provider.setDisplayName (panel.getProviderName ());
        boolean forceRead = false;
        boolean refreshModel = false;
        try {
            URL oldUrl = provider.getProviderURL ();
            URL newUrl = new URL (panel.getProviderURL ());
            if (! oldUrl.equals (newUrl)) {
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
                getSettingsTableModel ().getPluginManager ().updateUnitsChanged ();
            } else {
                // was enabled and won't be more -> add it from model and read its content
                final boolean force = forceRead;
                RequestProcessor.getDefault ().post (new Runnable () {
                    public void run () {
                        Utilities.presentRefreshProvider (provider, getSettingsTableModel ().getPluginManager (), force);
                        getSettingsTableModel ().getPluginManager ().updateUnitsChanged ();
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
                        getSettingsTableModel().refreshModel();
                        table.getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
                        
                    }
                });
                DialogDisplayer.getDefault().createDialog(descriptor).setVisible(true);
            }
        }
    }
    
    private class RemoveAction extends AbstractAction {
        RemoveAction() {
            super(UnitTab.textForKey("SettingsTab.RemoveButton.text"));
            putValue (MNEMONIC_KEY, UnitTab.mnemonicForKey ("SettingsTab.RemoveButton.text"));
        }
        public void actionPerformed(ActionEvent arg0) {
            SettingsTableModel model = getSettingsTableModel();
            int[] rowIndexes = table.getSelectedRows();
            for (int rowIndex : rowIndexes) {
                if (rowIndex != -1) {
                    model.remove(rowIndex);
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
    private javax.swing.JRadioButton cbModules;
    private javax.swing.JRadioButton cbPlugins;
    private javax.swing.JSeparator jSeparatorAdvanced;
    private javax.swing.JSeparator jSeparatorConnection;
    private javax.swing.JLabel lCheckPeriod;
    private javax.swing.JLabel lConnection;
    private javax.swing.JLabel lGeneral;
    private javax.swing.JLabel lPluginsView;
    private javax.swing.JLabel lUpdateCenters;
    private javax.swing.ButtonGroup pluginsViewGroup;
    private javax.swing.JSplitPane spTab;
    // End of variables declaration//GEN-END:variables
    
}
