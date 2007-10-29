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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Provide an interface to support datasource reference selection.
 * @author  Chris Webster
 */
public class SelectDatabasePanel extends javax.swing.JPanel {
    
    public static final String IS_VALID = "SelectDatabasePanel_isValid"; //NOI18N
    
    protected static final String PROTOTYPE_VALUE = "jdbc:pointbase://localhost/sample [pbpublic on PBPUBLIC] "; //NOI18N
    private final ServiceLocatorStrategyPanel slPanel;
    private final J2eeModuleProvider provider;
    private final Map<String, Datasource> references;
    private final Set<Datasource> moduleDatasources;
    private final Set<Datasource> serverDatasources;
    private boolean copyDataSourceToProject = false;
    
    public SelectDatabasePanel(J2eeModuleProvider provider, String lastLocator, Map<String, Datasource> references,
            Set<Datasource> moduleDatasources, Set<Datasource> serverDatasources, ClasspathInfo cpInfo) {
        initComponents();
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SelectDatabasePanel.class, "ACSD_ChooseDatabase"));
        this.provider = provider;
        this.references = references;
        this.moduleDatasources = moduleDatasources;
        this.serverDatasources = serverDatasources;
        
        dsRefCombo.setRenderer(new ReferenceListCellRenderer());
        dsRefCombo.setPrototypeDisplayValue(PROTOTYPE_VALUE);
        populateReferences();
        
        dsRefCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                checkDatasourceReference();
            }
        });

        slPanel = new ServiceLocatorStrategyPanel(lastLocator, cpInfo);
        serviceLocatorPanel.add(slPanel, BorderLayout.CENTER);
        slPanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(ServiceLocatorStrategyPanel.IS_VALID)) {
                    Object newvalue = evt.getNewValue();
                    if (newvalue instanceof Boolean) {
                        boolean isServiceLocatorOk = ((Boolean)newvalue).booleanValue();
                        if (isServiceLocatorOk) {
                            checkDatasourceReference();
                        } else {
                            firePropertyChange(IS_VALID, true, false);
                        }
                    }
                }
            }
        });
    }
    
    public String getDatasourceReference() {
        return (String) dsRefCombo.getSelectedItem();
    }

    public String getServiceLocator() {
        return slPanel.classSelected();
    }
    
    public boolean createServerResources() {
        return copyDataSourceToProject;
    }
    
    public Datasource getDatasource() {
        return references.get(getDatasourceReference());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dsRefLabel = new javax.swing.JLabel();
        serviceLocatorPanel = new javax.swing.JPanel();
        errorLabel = new javax.swing.JLabel();
        dsRefCombo = new javax.swing.JComboBox();
        buttonAdd = new javax.swing.JButton();

        dsRefLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "LBL_ConnectionMnemonic").charAt(0));
        dsRefLabel.setLabelFor(dsRefCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ejbcore/ui/logicalview/entres/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(dsRefLabel, bundle.getString("LBL_DsReference")); // NOI18N

        serviceLocatorPanel.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(errorLabel, " ");

        org.openide.awt.Mnemonics.setLocalizedText(buttonAdd, org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "LBL_Add")); // NOI18N
        buttonAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAddActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(errorLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 377, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dsRefLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dsRefCombo, 0, 351, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(buttonAdd))))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(serviceLocatorPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(dsRefLabel)
                    .add(dsRefCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(buttonAdd))
                .add(26, 26, 26)
                .add(serviceLocatorPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 114, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(25, 25, 25)
                .add(errorLabel))
        );

        dsRefLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "LBL_DsReference")); // NOI18N
        dsRefLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "ACSD_DsReference")); // NOI18N
        dsRefCombo.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "LBL_DsReference")); // NOI18N
        dsRefCombo.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "ACSD_DsRefCombo")); // NOI18N
        buttonAdd.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(SelectDatabasePanel.class, "ACSD_AddDataSourceRef")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void buttonAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAddActionPerformed
        DataSourceReferencePanel referencePanel = new DataSourceReferencePanel(provider, references.keySet(), moduleDatasources, serverDatasources);
        final DialogDescriptor dialogDescriptor = new DialogDescriptor(
                referencePanel,
                NbBundle.getMessage(SelectDatabasePanel.class, "LBL_AddDataSourceReference"), //NOI18N
                true,
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null);
        
        // register listener
        referencePanel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (DataSourceReferencePanel.IS_VALID.equals(evt.getPropertyName())) {
                    Object newvalue = evt.getNewValue();
                    if ((newvalue != null) && (newvalue instanceof Boolean)) {
                        dialogDescriptor.setValid(((Boolean) newvalue).booleanValue());
                    }
                }
            }
        });
        
        // initial invalidation
        dialogDescriptor.setValid(false);
        
        // show and eventually save
        Object option = DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (option == NotifyDescriptor.OK_OPTION) {
            final String refName = referencePanel.getReferenceName();
            references.put(refName, referencePanel.getDataSource());
            if (referencePanel.copyDataSourceToProject()) {
                // TODO how to copy it?
            }
            
            copyDataSourceToProject = referencePanel.copyDataSourceToProject();
            
            // update gui (needed because of sorting)
            populateReferences();
            // Ensure that the correct item is selected before listeners like FocusListener are called.
            // ActionListener.actionPerformed() is not called if this method is already called from
            // actionPerformed(), in that case selectItemLater should be set to true and setSelectedItem()
            // below is called asynchronously so that the actionPerformed() is called
            dsRefCombo.setSelectedItem(refName);
            
            boolean selectItemLater = false;
            if (selectItemLater) {
                SwingUtilities.invokeLater(new Runnable() { // postpone item selection to enable event firing from JCombobox.setSelectedItem()
                    public void run() {
                        dsRefCombo.setSelectedItem(refName);
                    }
                });
            }
            
        }
    }//GEN-LAST:event_buttonAddActionPerformed
    
    
            
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonAdd;
    private javax.swing.JComboBox dsRefCombo;
    private javax.swing.JLabel dsRefLabel;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JPanel serviceLocatorPanel;
    // End of variables declaration//GEN-END:variables
    
    protected void checkDatasourceReference() {
        if (dsRefCombo.getSelectedItem() instanceof String) {
            firePropertyChange(IS_VALID, false, true);
        } else {
            firePropertyChange(IS_VALID, true, false);
        }
    }

    private void populateReferences() {
        
        SortedSet<String> refNames = new TreeSet<String>(references.keySet());
        
        dsRefCombo.removeAllItems();
        for (String s : refNames) {
            dsRefCombo.addItem(s);
        }
    }
    
    private class ReferenceListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            if (value instanceof String) {
                String refName = (String) value;
                Datasource ds = references.get(refName);
                StringBuilder sb = new StringBuilder(refName);
                if (ds != null) {
                    sb.append(" ["); // NOI18N
                    sb.append(ds.getUrl());
                    sb.append("] "); // NOI18N
                }
                setText(sb.toString());
            } else {
                // should not get here
                setText(value != null ? value.toString() : ""); // NOI18N
            }
            setToolTipText(""); // NOI18N
            
            return this;
        }

    }
}
