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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.j2ee.websphere6.dd.beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext.WSEjbExtDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;

/**
 *
 * @author  dlm198383
 */
public class EjbExtensionPanel extends SectionInnerPanel implements java.awt.event.ItemListener {
    EjbExtensionsType ejbExtension;
    WSEjbExtDataObject dObj;
    
    private javax.swing.JCheckBox localTransactionCheckBox;
    private javax.swing.JTextField transactionNameField;
    
    
    private javax.swing.JComboBox unresolvedActionComboBox;
    private javax.swing.JCheckBox resolverCheckBox;
    private javax.swing.JComboBox resolverComboBox;
    private javax.swing.JCheckBox boundaryCheckBox;
    private javax.swing.JComboBox boundaryComboBox;
    
    
    private static String [] Types=new String [] {
        NbBundle.getMessage(EjbExtensionPanel.class,"LBL_TypeSession"),
        NbBundle.getMessage(EjbExtensionPanel.class,"LBL_TypeEntity"),
        NbBundle.getMessage(EjbExtensionPanel.class,"LBL_TypeMessageDriven")};
    /** Creates new form WSResRefBindingsPanel */
    public EjbExtensionPanel(SectionView view, WSEjbExtDataObject dObj,  EjbExtensionsType ejbExtension) {
        
        super(view);
        this.dObj=dObj;
        this.ejbExtension=ejbExtension;
        initComponents();
        bindLocalTransactionComponents();
        
        initLocalTransactionComponents();
        
        ((LocalTransactionPanel)jPanel1).setEnabledComponents();
        
        typeComboBox.setModel(new DefaultComboBoxModel(Types));
        
        nameField.setText(ejbExtension.getXmiName());
        idField.setText(ejbExtension.getXmiId());
        beanIdField.setText(ejbExtension.getHref());
        
        String [] data = new String[] {
            ejbExtension.getEjbExtensionsType(),
            ejbExtension.getEnterpriseBean(),
            ejbExtension.getHref(),
            ejbExtension.getXmiId(),
            ejbExtension.getXmiName(),
            ejbExtension.getXmiType()
        };
        addModifier(nameField);
        addModifier(idField);
        addModifier(beanIdField);
        
        addValidatee(nameField);
        addValidatee(idField);
        addValidatee(beanIdField);
        
        
        String xmiType=ejbExtension.getXmiType();
        if(xmiType!=null) {
            if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_SESSION)) {
                typeComboBox.setSelectedIndex(0);
            } else if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_ENTITY)) {
                typeComboBox.setSelectedIndex(1);
            } else if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN)) {
                typeComboBox.setSelectedIndex(2);
            } else {
                typeComboBox.setSelectedIndex(-1);
            }
        } else {
            typeComboBox.setSelectedIndex(-1);
        }
        typeComboBox.addItemListener(this);
    }
    
    private void bindLocalTransactionComponents(){
        LocalTransactionPanel localTransactionPanel=(LocalTransactionPanel)jPanel1;
        
        localTransactionCheckBox=localTransactionPanel.getLocalTransactionCheckBox();
        transactionNameField=localTransactionPanel.getTransactionNameField();
        unresolvedActionComboBox=localTransactionPanel.getUnresolvedActionComboBox();
        resolverCheckBox=localTransactionPanel.getResolverCheckBox();
        resolverComboBox=localTransactionPanel.getResolverComboBox();
        boundaryCheckBox=localTransactionPanel.getBoundaryCheckBox();
        boundaryComboBox=localTransactionPanel.getBoundaryComboBox();
        localTransactionPanel.setComponentsBackground(SectionVisualTheme.getSectionActiveBackgroundColor());
        
    }
    
    public void initLocalTransactionComponents() {
        addModifier(nameField);
        addModifier(transactionNameField);
        addValidatee(transactionNameField);
        boolean localTransactionEnabled=(ejbExtension.getLocalTransaction()==null)?false:true;
        localTransactionCheckBox.setSelected(localTransactionEnabled);
        
        if(localTransactionEnabled) {
            transactionNameField.setText(ejbExtension.getLocalTransactionXmiId());
            
            unresolvedActionComboBox.setSelectedItem(ejbExtension.getLocalTransactionUnresolvedAction());
            String str=ejbExtension.getLocalTransactionResolver();
            if(str==null) {
                resolverCheckBox.setSelected(false);
            } else {
                resolverCheckBox.setSelected(true);
                resolverComboBox.setSelectedItem(str);
            }
            
            str=ejbExtension.getLocalTransactionBoundary();
            if(str==null) {
                boundaryCheckBox.setSelected(false);
            } else {
                boundaryCheckBox.setSelected(true);
                boundaryComboBox.setSelectedItem(str);
            }
        }
        
        
        localTransactionCheckBox.addItemListener(this);
        unresolvedActionComboBox.addItemListener(this);
        
        resolverCheckBox.addItemListener(this);
        resolverComboBox.addItemListener(this);
        
        boundaryCheckBox.addItemListener(this);
        boundaryComboBox.addItemListener(this);
    }
    
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==nameField) {
            ejbExtension.setXmiName((String)value);
        }
        if (source==idField) {
            ejbExtension.setXmiId((String)value);
            
        }
        if (source==beanIdField) {
            ejbExtension.setHref((String)value);
        }
        
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
	dObj.setChangedFromUI(true);
        String selectedString=(String)typeComboBox.getSelectedItem();
        if(selectedString!=null) {
            if(selectedString.equals(Types[0])) { //session
                ejbExtension.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_SESSION);
                ejbExtension.setEjbExtensionsType(DDXmiConstants.EJB_EXTENSIONS_TYPE_SESSION);
            } else if(selectedString.equals(Types[1])) { //entity
                ejbExtension.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_ENTITY);
                ejbExtension.setEjbExtensionsType(DDXmiConstants.EJB_EXTENSIONS_TYPE_ENTITY);
            } else if(selectedString.equals(Types[2])) { //message driven
                ejbExtension.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN);
                ejbExtension.setEjbExtensionsType(DDXmiConstants.EJB_EXTENSIONS_TYPE_MESSAGEDRIVEN);
            } else {
                ejbExtension.setXmiType(null);
                ejbExtension.setEjbExtensionsType(null);
            }
        }
        changeLocalTransactionState();
        dObj.modelUpdatedFromUI();
        //dObj.setChangedFromUI(true);
        dObj.setChangedFromUI(false);
    }
    
    public void changeLocalTransactionState() {
        if(localTransactionCheckBox.isSelected()) {
            ejbExtension.setLocalTransaction("");
            ejbExtension.setLocalTransactionXmiId(transactionNameField.getText());
            
            ejbExtension.setLocalTransactionUnresolvedAction(
                    unresolvedActionComboBox.getSelectedItem().toString());
            
            ejbExtension.setLocalTransactionResolver(
                    resolverCheckBox.isSelected()?
                        resolverComboBox.getSelectedItem().toString():
                        null);
            ejbExtension.setLocalTransactionBoundary(
                    boundaryCheckBox.isSelected()?
                        boundaryComboBox.getSelectedItem().toString():
                        null);
        } else {
            ejbExtension.setLocalTransaction(null);
            //ejbExtension.setLocalTransactionXmiId(null);
        }
        ((LocalTransactionPanel)jPanel1).setEnabledComponents();
    }
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==nameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==idField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==beanIdField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Bean Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==transactionNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Local transaction name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (nameField==source) {
            nameField.setText(ejbExtension.getXmiName());
        }
        if (idField==source) {
            idField.setText(ejbExtension.getXmiId());
        }
        if (beanIdField==source) {
            beanIdField.setText(ejbExtension.getHref());
        }
        if (transactionNameField==source) {
            transactionNameField.setText(ejbExtension.getLocalTransactionXmiId());
        }
        
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("Name".equals(errorId)) return nameField;
        if ("ID".equals(errorId)) return idField;
        if ("Bean Name".equals(errorId)) return beanIdField;
        if("Local transaction name".equals(errorId)) return transactionNameField;
        return null;
    }
    
    /** This will be called before model is changed from this panel
     */
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        nameLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        idField = new javax.swing.JTextField();
        beanIdField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        jPanel1 = new LocalTransactionPanel();
        jSeparator1 = new javax.swing.JSeparator();

        nameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Name"));

        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Id"));

        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_BeanName"));

        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_BeanType"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(nameLabel)
                                    .add(jLabel2)
                                    .add(jLabel3))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(jLabel4))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(idField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .add(beanIdField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .add(typeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 164, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE)))
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(nameLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(idField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(beanIdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel3))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(typeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel4))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(2, 2, 2)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField beanIdField;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox typeComboBox;
    // End of variables declaration//GEN-END:variables
    
}
