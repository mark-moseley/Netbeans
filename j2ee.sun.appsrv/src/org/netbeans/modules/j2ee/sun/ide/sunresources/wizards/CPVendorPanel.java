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
/*
 * CPVendorPanel.java -- synopsis.
 *
 */


package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.DocumentListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.modules.j2ee.sun.api.restricted.ResourceConfigurator;
import org.netbeans.modules.j2ee.sun.api.restricted.ResourceUtils;
import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Field;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

public class CPVendorPanel extends ResourceWizardPanel implements ChangeListener, DocumentListener, ListDataListener {
    
    static final long serialVersionUID = 93474632245456421L;
    
    private ArrayList dbconns;
    private ResourceConfigHelper helper;
    private FieldGroup generalGroup, propGroup, vendorGroup;
    private boolean useExistingConnection = true;
    private String[] vendors;
    private boolean firstTime = true;
    private boolean setupValid = true;
    
    private static final String CONST_TRUE = "true"; // NOI18N
        
    /** Creates new form DBSchemaConnectionpanel */
    public CPVendorPanel(ResourceConfigHelper helper, Wizard wiardInfo) {
        this.firstTime = true;
        this.helper = helper;
        this.generalGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __General); 
        this.propGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __Properties); 
        this.vendorGroup = FieldGroupHelper.getFieldGroup(wiardInfo, __PropertiesURL); 
        ButtonGroup bg = new ButtonGroup();
        dbconns = new ArrayList();
        
        setName(bundle.getString("TITLE_ConnPoolWizardPanel_dbConn")); //NOI18N

        initComponents ();
                
        nameLabel.setLabelFor(nameField);
        nameComboBox.registerKeyboardAction(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    nameComboBox.requestFocus();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);            

        bg.add(existingConnRadioButton);
        bg.add(newCofigRadioButton);
        bg.getSelection().addChangeListener(this);
        try{
            DatabaseConnection[] cons = ConnectionManager.getDefault().getConnections();
            for(int i=0; i < cons.length; i++){
                existingConnComboBox.addItem(cons[i].getName());
                dbconns.add(cons[i]);
            }
        }catch(Exception ex){
            // Connection could not be found
        }
        if (existingConnComboBox.getItemCount() == 0) {
            existingConnComboBox.insertItemAt(bundle.getString("NoConnection"), 0); //NOI18N
            newCofigRadioButton.setSelected(true);
            newCofigRadioButton.setEnabled(true);
            nameComboBox.setEnabled(true);
            existingConnComboBox.setEnabled(false);
        } else {
            existingConnComboBox.insertItemAt(bundle.getString("SelectFromTheList"), 0); //NOI18N
            existingConnRadioButton.setSelected(true);
            existingConnRadioButton.setEnabled(true);
            existingConnComboBox.setEnabled(true);
            nameComboBox.setEnabled(false);
            setExistingConnData();
        }
        
        Field vendorField = FieldHelper.getField(this.generalGroup, __DatabaseVendor);
        vendors = FieldHelper.getTags(vendorField);
        for (int i = 0; i < vendors.length; i++) {
            nameComboBox.addItem(bundle.getString("DBVendor_" + vendors[i]));   //NOI18N
        }
        
        if (nameComboBox.getItemCount() == 0)
            nameComboBox.insertItemAt(bundle.getString("NoTemplate"), 0); //NOI18N
        else
            nameComboBox.insertItemAt(bundle.getString("SelectFromTheList"), 0); //NOI18N
        nameComboBox.setSelectedIndex(0);
        
        existingConnComboBox.getModel().addListDataListener(this);
        nameComboBox.getModel().addListDataListener(this);
        isXA.setSelected(helper.getData().getString(__IsXA).equals(CONST_TRUE));  //NOI18N
        isXA.addChangeListener(this);
        newCofigRadioButton.addChangeListener(this);
        
        this.firstTime = false;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        descriptionTextArea = new javax.swing.JTextArea();
        nameLabel = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        existingConnRadioButton = new javax.swing.JRadioButton();
        existingConnComboBox = new javax.swing.JComboBox();
        newCofigRadioButton = new javax.swing.JRadioButton();
        nameComboBox = new javax.swing.JComboBox();
        isXA = new javax.swing.JCheckBox();

        setMaximumSize(new java.awt.Dimension(600, 350));
        setMinimumSize(new java.awt.Dimension(600, 350));
        setPreferredSize(new java.awt.Dimension(600, 350));

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setText(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "Description")); // NOI18N
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setFocusable(false);
        descriptionTextArea.setOpaque(false);
        descriptionTextArea.setRequestFocusEnabled(false);
        descriptionTextArea.setVerifyInputWhenFocusTarget(false);

        nameLabel.setLabelFor(nameField);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "LBL_pool-name")); // NOI18N

        nameField.setText(this.helper.getData().getString(__Name));
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.nameFieldActionPerformed(evt);
            }
        });
        nameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                CPVendorPanel.this.nameFieldKeyReleased(evt);
            }
        });

        existingConnRadioButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(existingConnRadioButton, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ExistingConnection")); // NOI18N

        existingConnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.existingConnComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(newCofigRadioButton, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "NewConfiguration")); // NOI18N

        nameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.nameComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(isXA, org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "isXA")); // NOI18N
        isXA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CPVendorPanel.this.isXAActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                .add(30, 30, 30)
                .add(existingConnComboBox, 0, 539, Short.MAX_VALUE)
                .addContainerGap())
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(isXA)
                        .addContainerGap())
                    .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(21, 21, 21)
                            .add(nameComboBox, 0, 538, Short.MAX_VALUE)
                            .addContainerGap())
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(newCofigRadioButton)
                            .addContainerGap())
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(existingConnRadioButton)
                            .addContainerGap())
                        .add(jPanel1Layout.createSequentialGroup()
                            .add(nameLabel)
                            .add(10, 10, 10)
                            .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
                            .addContainerGap()))))
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(descriptionTextArea, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                .add(9, 9, 9))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(descriptionTextArea)
                .add(17, 17, 17)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .add(18, 18, 18)
                .add(existingConnRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(existingConnComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(newCofigRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(nameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(isXA)
                .add(67, 67, 67))
        );

        descriptionTextArea.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_DescriptionA11yName")); // NOI18N
        descriptionTextArea.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_DescriptionA11yDesc")); // NOI18N
        nameLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_pool-nameA11yDesc")); // NOI18N
        existingConnRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_ExistingConnectionA11yDesc")); // NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_ExistingConnectionComboBoxA11yName")); // NOI18N
        existingConnComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_ExistingConnectionComboBoxA11yDesc")); // NOI18N
        newCofigRadioButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_NewConnectionA11yDesc")); // NOI18N
        nameComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_NewConnectionComboBoxA11yName")); // NOI18N
        nameComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_NewConnectionComboBoxA11yDesc")); // NOI18N
        isXA.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "ACS_isXA_A11yDesc")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(11, 11, 11))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "TITLE_ConnPoolWizardPanel_dbConn")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CPVendorPanel.class, "TITLE_ConnPoolWizardPanel_dbConn")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void nameFieldKeyReleased(java.awt.event.KeyEvent evt) {
        // Add your handling code here:
        ResourceConfigData data = this.helper.getData();
        String value = data.getString(__Name);
        String newValue = nameField.getText();
        if (!value.equals(newValue)) {
            this.helper.getData().setString(__Name, newValue);
        }
        fireChange(this);
    }

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        setResourceName();
    }
    
    public String getNameField() {
        return nameField.getText();
    }
    
    private void setResourceName() {
        ResourceConfigData data = this.helper.getData();
        String value = data.getString(__Name);
        String newValue = nameField.getText();
        if (!value.equals(newValue)) {
            this.helper.getData().setString(__Name, newValue);
            fireChange(this);
        }
        
        if((this.getRootPane().getDefaultButton() != null) && (this.getRootPane().getDefaultButton().isEnabled())){
            this.getRootPane().getDefaultButton().doClick();
        }
    }
    
    private void isXAActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        setNewConfigData(false); 
    }

    private void nameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        setNewConfigData(true);      
/*           
        usernameTextField.setText(""); //NOI18N
        passwordField.setText(""); //NOI18N
        
        data.setDriver(driverTextField.getText());
        data.setSchema(null);
        schemas = false;
*/       
    }
    
    private void setNewConfigData(boolean replaceProps) {
        if (firstTime) {
            return;
        }
        int index = nameComboBox.getSelectedIndex();

        if (index > 0) {
            if (useExistingConnection) {
                useExistingConnection = false; 
            }
            ResourceConfigData data = this.helper.getData();
            data.setString(__IsCPExisting, "false"); //NOI18N
            String vendorName = vendors[index - 1];     
            String savedVendorName = data.getString(__DatabaseVendor);
            String savedXA = data.getString(__IsXA);
            String XA = isXA.isSelected()?CONST_TRUE:"false";  //NOI18N
            boolean vendorNotChanged = vendorName.equals(savedVendorName);
            boolean isXANotChanged = XA.equals(savedXA);

            if (vendorNotChanged && isXANotChanged) {
                return;
            }
            if (!vendorNotChanged) {
                data.setString(__DatabaseVendor, vendorName);
            }
            if (!isXANotChanged) {
                data.setString(__IsXA, XA);
            }
            
            setDataSourceClassNameAndResTypeInData(vendorName);
            
            if (replaceProps) {
                setPropertiesInData(vendorName);
            }
        }    
    }
    
    private void setDataSourceClassNameAndResTypeInData(String vendorName) {
        //change datasource classname following database vendor change
        ResourceConfigData data = this.helper.getData();
        Field dsField;
        if (isXA.isSelected())
            dsField = FieldHelper.getField(this.generalGroup, __XADatasourceClassname);
        else
            dsField = FieldHelper.getField(this.generalGroup, __DatasourceClassname);
        data.setString(__DatasourceClassname, FieldHelper.getConditionalFieldValue(dsField, vendorName));
        
        if (isXA.isSelected()) {
            data.setString(__ResType, "javax.sql.XADataSource");  //NOI18N
            data.setString(__IsXA, CONST_TRUE);  //NOI18N
        }else {
            data.setString(__ResType, "javax.sql.DataSource");  //NOI18N
            data.setString(__IsXA, "false");  //NOI18N
        }
    }
    
    private void setPropertiesInData(String vendorName) {
        //change standard properties following database vendor change
        ResourceConfigData data = this.helper.getData();
        data.setProperties(new Vector());
        Field[] propFields = this.propGroup.getField();
        for (int i = 0; i < propFields.length; i++) {
            String value = FieldHelper.getConditionalFieldValue(propFields[i], vendorName);
            String name = propFields[i].getName();
            if (name.equals(__Url) && value.length() > 0)
                data.addProperty(name, FieldHelper.toUrl(value));
            else if (name.equals(__DatabaseName) && value.length() > 0)
                data.addProperty(name, FieldHelper.toUrl(value));
            else if (name.equals(__User) || name.equals(__Password)) {
                data.addProperty(propFields[i].getName(), value);
            }else{
                //All Others
                if(value.length() > 0 && (value.equals(__NotApplicable))){
                    data.addProperty(propFields[i].getName(), ""); //NOI18N
                }
            }
        }
    }
        
    private void existingConnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        setExistingConnData();
    }
    
    public void setExistingConnData() {
        if(existingConnComboBox.getSelectedIndex() > 0) {
            if (!useExistingConnection) {
                this.helper.getData().setResourceName(__JdbcConnectionPool);
                useExistingConnection = true;  
            }
            this.helper.getData().setString(__IsCPExisting, CONST_TRUE); //NOI18N
            DatabaseConnection dbconn = (DatabaseConnection)dbconns.get(existingConnComboBox.getSelectedIndex() - 1);
            String url = dbconn.getDatabaseURL();
            String user = dbconn.getUser();
            String password = dbconn.getPassword();
            if(user != null && (password == null || password.trim().length() == 0)){ 
                password = "()"; //NOI18N
            }
            String tmpStr = url;
            
            Field urlField = FieldHelper.getField(this.vendorGroup, "vendorUrls"); //NOI18N
            String vendorName = FieldHelper.getOptionNameFromValue(urlField, tmpStr);
                        
            ResourceConfigData data = this.helper.getData();    
            data.setProperties(new Vector());
            data.setString(__DatabaseVendor, vendorName);
            
            if (vendorName.equals("pointbase")) {  //NOI18N
                data.addProperty(__DatabaseName, dbconn.getDatabaseURL());
            }else if(vendorName.startsWith("derby")) {  //NOI18N)
                setDerbyProps(vendorName, url);
            }else    
                data.addProperty(__Url, url);
            data.addProperty(__User, user);
            data.addProperty(__Password, password);
            
            setDataSourceClassNameAndResTypeInData(vendorName);
        }
           
    }
    
    private void setDerbyProps(String vendorName, String url) {
        //change standard properties following database vendor change
        ResourceConfigData data = this.helper.getData();
        data.setProperties(new Vector());
        data.addProperty(__Url, url);
        Field[] propFields = this.propGroup.getField();
        for (int i = 0; i < propFields.length; i++) {
            String value = FieldHelper.getConditionalFieldValue(propFields[i], vendorName);
            if(value.equals(__NotApplicable)){
                String name = propFields[i].getName();
                if(vendorName.equals("derby_net")) {//NOI18N
                    String hostName = "";
                    String portNumber = "";
                    String databaseName = "";
                    try{
                        String workingUrl = url.substring(url.indexOf("//") + 2, url.length());
                        ResourceConfigurator rci = new ResourceConfigurator();
                        hostName = rci.getDerbyServerName(workingUrl);
                        portNumber = rci.getDerbyPortNo(workingUrl);
                        databaseName = rci.getDerbyDatabaseName(workingUrl);
                    }catch(java.lang.StringIndexOutOfBoundsException ex){
                    }
                    if (name.equals(__DerbyPortNumber)) {
                        data.addProperty(name, portNumber);
                    } else if (name.equals(__DerbyDatabaseName)) {
                        data.addProperty(name, databaseName);
                    } else if (name.equals(__ServerName)) {
                        data.addProperty(name, hostName);
                    }
                }   
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descriptionTextArea;
    private javax.swing.JComboBox existingConnComboBox;
    private javax.swing.JRadioButton existingConnRadioButton;
    private javax.swing.JCheckBox isXA;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox nameComboBox;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JRadioButton newCofigRadioButton;
    // End of variables declaration//GEN-END:variables

//    private static final ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N
    
    public boolean isValid() {
        if(! setupValid){
            setErrorMsg(bundle.getString("Err_InvalidSetup"));
            return false;
        }
        setErrorMsg(bundle.getString("Empty_String"));
        String name = nameField.getText();
        if (name == null || name.length() == 0){
            setErrorMsg(bundle.getString("Err_InvalidName"));
            return false;
        }else if(! ResourceUtils.isLegalResourceName(name)){
            setErrorMsg(bundle.getString("Err_InvalidName"));
            return false;
        }else if(! ResourceUtils.isUniqueFileName(name, this.helper.getData().getTargetFileObject(), __ConnectionPoolResource)){
            setErrorMsg(bundle.getString("Err_DuplFileName"));
            return false;
        }
        
        if (existingConnRadioButton.isSelected()) {
            if (existingConnComboBox.getSelectedIndex() > 0)
                return true;
            else
                setErrorMsg(bundle.getString("Err_ChooseDBConn"));
        }else if (newCofigRadioButton.isSelected()) {
            if (nameComboBox.getSelectedIndex() > 0)
                return true;
            else
                setErrorMsg(bundle.getString("Err_ChooseDBVendor"));
        } 
        
        return false;
    }

    public void removeUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange(this);
    }
    
    public void changedUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange(this);
    }
    
    public void insertUpdate(final javax.swing.event.DocumentEvent event) {
        fireChange(this);
    }

    public void intervalAdded(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }
    
    public void intervalRemoved(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }
    
    public void contentsChanged(final javax.swing.event.ListDataEvent p1) {
        fireChange(this);
    }

    public void stateChanged(final javax.swing.event.ChangeEvent p1) {
        if (firstTime) {
            return;
        }
        if (p1.getSource().getClass() == javax.swing.JToggleButton.ToggleButtonModel.class) {
            if (existingConnRadioButton.isSelected()) {
                //To solve a problem on Win2K only
                if (firstTime) {
                    return;
                }
                existingConnComboBox.setEnabled(true);
                nameComboBox.setEnabled(false);
//                isXA.setEnabled(false);
                setExistingConnData();
            } else {
                existingConnComboBox.setEnabled(false);
                nameComboBox.setEnabled(true);
                setNewConfigData(true);
            }  
        }
        fireChange(this);
    }
    
    public CPVendorPanel setFirstTime(boolean first) {
        this.firstTime = first;
        return this;
    }

    protected void initData() {
        /*if (existingConnRadioButton.isSelected()) {
            data.setExistingConn(true);
            if(existingConnComboBox.getSelectedIndex() > 0)
                data.setConnectionNodeInfo((ConnectionNodeInfo) connInfos.get(existingConnComboBox.getSelectedIndex() - 1));
            
            data.setDriver(null);
            data.setUrl(null);
            data.setUsername(null);
            data.setPassword(null);
        } else {
            data.setExistingConn(false);
            data.setDriver(driverTextField.getText());
            data.setUrl(urlTextField.getText());
            data.setUsername(usernameTextField.getText());
            data.setPassword(new String(passwordField.getPassword()));

            data.setConnectionNodeInfo(null);
        }*/
    }
    
    public HelpCtx getHelp() {
         return new HelpCtx("AS_Wiz_ConnPool_chooseDB"); //NOI18N
    }
    
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
        TemplateWizard wizard = (TemplateWizard)settings;
        String targetName = wizard.getTargetName();
        if(this.helper.getData().getString(__DynamicWizPanel).equals(CONST_TRUE)){ //NOI18N
            targetName = null;
        }  
        FileObject setupFolder = ResourceUtils.getResourceDirectory(this.helper.getData().getTargetFileObject());
        this.helper.getData().setTargetFileObject (setupFolder);
        if(setupFolder != null){
            String resourceName = this.helper.getData().getString(__Name);
            if((resourceName != null) && (! resourceName.equals(""))) {
                targetName = resourceName;
            }
            targetName = ResourceUtils.createUniqueFileName (targetName, setupFolder, __ConnectionPoolResource);
            this.helper.getData ().setTargetFile (targetName);
            this.nameField.setText(targetName);
            this.helper.getData().setString(__Name, targetName);
        }else
            setupValid = false;
    }
    
    public void setInitialFocus(){
        new setFocus(nameField);
    }
    
//    private boolean setupValid(){
//        return setupValid;
//    }
}
