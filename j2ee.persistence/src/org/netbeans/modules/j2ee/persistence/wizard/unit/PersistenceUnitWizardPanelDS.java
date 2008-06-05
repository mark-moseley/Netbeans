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

package org.netbeans.modules.j2ee.persistence.wizard.unit;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.modules.j2ee.core.api.support.Strings;
import org.netbeans.modules.j2ee.persistence.provider.DefaultProvider;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.Provider;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSource;
import org.netbeans.modules.j2ee.persistence.spi.datasource.JPADataSourcePopulator;
import org.netbeans.modules.j2ee.persistence.util.PersistenceProviderComboboxHelper;
import org.netbeans.modules.j2ee.persistence.wizard.library.PersistenceLibrarySupport;
import org.netbeans.modules.j2ee.persistence.wizard.unit.PersistenceUnitWizardPanel.TableGeneration;
import org.openide.util.NbBundle;

/**
 *
 * @author  Martin Adamek
 */
public class PersistenceUnitWizardPanelDS extends PersistenceUnitWizardPanel {
    
    public PersistenceUnitWizardPanelDS(Project project, ChangeListener changeListener, boolean editName) {
        this(project, changeListener, editName, TableGeneration.CREATE);
    }
    
    public PersistenceUnitWizardPanelDS(Project project, ChangeListener changeListener,
            boolean editName, TableGeneration tg) {

        super(project);
        initComponents();
        setTableGeneration(tg);
       
        if (ProviderUtil.isValidServerInstanceOrNone(project)){
            connectDatasources();
        }
        
        PersistenceProviderComboboxHelper comboHelper = new PersistenceProviderComboboxHelper(project);
        comboHelper.connect(providerCombo);
        unitNameTextField.setText(getCandidateName());
        unitNameTextField.selectAll();

        // unit name editing is not available when adding first PU
        unitNameTextField.setVisible(editName);
        unitNameLabel.setVisible(editName);
        
        unitNameTextField.getDocument().addDocumentListener(new ValidationListener());
        errorMessage.setForeground(Color.RED);
    }
    
    /**
     * Pre-selects appropriate table generation strategy radio button.
     */
    private void setTableGeneration(TableGeneration tg){
        if (TableGeneration.CREATE.equals(tg)){
            ddlCreate.setSelected(true);
        } else if (TableGeneration.DROP_CREATE.equals(tg)){
            ddlDropCreate.setSelected(true);
        } else {
            ddlUnkown.setSelected(true);
        }
    }
    
    private void connectDatasources() {
        JPADataSourcePopulator dsPopulator = project.getLookup().lookup(JPADataSourcePopulator.class);
        dsPopulator.connect(dsCombo);
        
        dsCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                checkValidity();
            }
        });
        
        ((JTextComponent)dsCombo.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void insertUpdate(DocumentEvent e) {
                checkValidity();
            }
            public void removeUpdate(DocumentEvent e) {
                checkValidity();
            }
        });
    }
    
    
    /**
     * Checks whether this panel is in valid state (see <code>#isValidPanel()</code>)
     * and fires appropriate property changes.
     */
    private void checkValidity(){
        if (isValidPanel()) {
            firePropertyChange(IS_VALID, false, true);
        } else {
            firePropertyChange(IS_VALID, true, false);
        }
    }
    
    public String getPersistenceUnitName() {
        return unitNameTextField.getText();
    }
    
    public String getDatasource() {
        return ((JTextComponent)dsCombo.getEditor().getEditorComponent()).getText();
    }
    
    public void setPreselectedDB(String db) {
        boolean hasItem = false;
        for (int i = 0; i < dsCombo.getItemCount(); i++) {
            if (dsCombo.getItemAt(i) instanceof JPADataSource) {
                JPADataSource ds = (JPADataSource) dsCombo.getItemAt(i);
                if (ds.getJndiName().equals(db)) {
                    hasItem = true;
                    break;
                }
            }
        }
        dsCombo.setSelectedItem(db);
        dsCombo.setEnabled(!hasItem);
    }
    
    public boolean isNonDefaultProviderEnabled() {
        return !(providerCombo.getSelectedItem() instanceof DefaultProvider);
    }
    
    public String getNonDefaultProvider() {
        return ((Provider) providerCombo.getSelectedItem()).getProviderClass();
    }
    
    
    public boolean isJTA() {
        return jtaCheckBox.isEnabled() && jtaCheckBox.isSelected();
    }
    
    
    public String getTableGeneration() {
        if (ddlCreate.isSelected()) {
            return Provider.TABLE_GENERATION_CREATE;
        } else if (ddlDropCreate.isSelected()) {
            return Provider.TABLE_GENERATION_DROPCREATE;
        } else {
            return Provider.TABLE_GENERATTION_UNKOWN;
        }
    }
    
    public boolean isValidPanel() {
        try{
            if (!isNameValid()){
                return false;
            }
        } catch (InvalidPersistenceXmlException ipx){
            setErrorMessage(NbBundle.getMessage(PersistenceUnitWizardDescriptor.class,"ERR_InvalidPersistenceXml", ipx.getPath())); //NOI18N
            return false;
        }
        return true;
    }
    
    /**
     * Checks whether name of the persistence unit is valid, i.e. it's not
     * empty and it's unique.
     */
    private boolean isNameValid() throws InvalidPersistenceXmlException{
        return Strings.isEmpty(getPersistenceUnitName()) ? false : isNameUnique();
    }
    
    public Provider getSelectedProvider() {
        return (Provider) providerCombo.getSelectedItem();
    }
    
    public void setErrorMessage(String msg){
        errorMessage.setText(msg);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        tableCreationButtonGroup = new javax.swing.ButtonGroup();
        unitNameLabel = new javax.swing.JLabel();
        unitNameTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        ddlCreate = new javax.swing.JRadioButton();
        ddlDropCreate = new javax.swing.JRadioButton();
        ddlUnkown = new javax.swing.JRadioButton();
        datasourceLabel = new javax.swing.JLabel();
        jtaCheckBox = new javax.swing.JCheckBox();
        dsCombo = new javax.swing.JComboBox();
        persistenceProviderLabel = new javax.swing.JLabel();
        providerCombo = new javax.swing.JComboBox();
        errorMessage = new javax.swing.JLabel();

        setName(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_Step1")); // NOI18N

        unitNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_UnitName").charAt(0));
        unitNameLabel.setLabelFor(unitNameTextField);
        unitNameLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_UnitName")); // NOI18N

        unitNameTextField.setColumns(40);
        unitNameTextField.setText("em");

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_SpecifyPersistenceProvider")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_TableGeneration")); // NOI18N

        tableCreationButtonGroup.add(ddlCreate);
        ddlCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_Create_mnem").charAt(0));
        ddlCreate.setSelected(true);
        ddlCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_Create")); // NOI18N
        ddlCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        tableCreationButtonGroup.add(ddlDropCreate);
        ddlDropCreate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_DropCreate_mnem").charAt(0));
        ddlDropCreate.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_DropCreate")); // NOI18N
        ddlDropCreate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        tableCreationButtonGroup.add(ddlUnkown);
        ddlUnkown.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_None_mnem").charAt(0));
        ddlUnkown.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_None")); // NOI18N
        ddlUnkown.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        datasourceLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_DatasourceName").charAt(0));
        datasourceLabel.setLabelFor(dsCombo);
        datasourceLabel.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_ServerDataSource")); // NOI18N

        jtaCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("CHB_JTA_mnem").charAt(0));
        jtaCheckBox.setSelected(true);
        jtaCheckBox.setText(org.openide.util.NbBundle.getMessage(PersistenceUnitWizardPanelDS.class, "LBL_JTA")); // NOI18N
        jtaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        dsCombo.setEditable(true);

        persistenceProviderLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle").getString("MN_Provider").charAt(0));
        persistenceProviderLabel.setLabelFor(providerCombo);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/persistence/wizard/unit/Bundle"); // NOI18N
        persistenceProviderLabel.setText(bundle.getString("LBL_PersistenceProvider")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(unitNameLabel)
                            .add(persistenceProviderLabel)
                            .add(datasourceLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(unitNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                            .add(providerCombo, 0, 380, Short.MAX_VALUE)
                            .add(dsCombo, 0, 380, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlCreate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlDropCreate)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(ddlUnkown))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jtaCheckBox)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1)
                    .add(errorMessage, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(unitNameLabel)
                    .add(unitNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(persistenceProviderLabel)
                    .add(providerCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(datasourceLabel)
                    .add(dsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jtaCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(ddlCreate)
                    .add(ddlDropCreate)
                    .add(ddlUnkown)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(errorMessage, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        unitNameLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UnitName")); // NOI18N
        unitNameTextField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UnitName")); // NOI18N
        jLabel2.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TableGeneration")); // NOI18N
        ddlCreate.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Create")); // NOI18N
        ddlDropCreate.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DropCreate")); // NOI18N
        ddlUnkown.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_None")); // NOI18N
        datasourceLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_DatasourceName")); // NOI18N
        jtaCheckBox.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_JTA")); // NOI18N
        persistenceProviderLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_Provider")); // NOI18N
        persistenceProviderLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Provider")); // NOI18N
        providerCombo.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Provider")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel datasourceLabel;
    private javax.swing.JRadioButton ddlCreate;
    private javax.swing.JRadioButton ddlDropCreate;
    private javax.swing.JRadioButton ddlUnkown;
    private javax.swing.JComboBox dsCombo;
    private javax.swing.JLabel errorMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JCheckBox jtaCheckBox;
    private javax.swing.JLabel persistenceProviderLabel;
    private javax.swing.JComboBox providerCombo;
    private javax.swing.ButtonGroup tableCreationButtonGroup;
    private javax.swing.JLabel unitNameLabel;
    private javax.swing.JTextField unitNameTextField;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Document listener that invokes <code>checkValidity</code> when
     * changes are made.
     */
    private class ValidationListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            checkValidity();
        }
        public void removeUpdate(DocumentEvent e) {
            checkValidity();
        }
        public void changedUpdate(DocumentEvent e) {
            checkValidity();
        }
    }
}

