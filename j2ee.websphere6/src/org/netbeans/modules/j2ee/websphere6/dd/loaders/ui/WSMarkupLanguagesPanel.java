/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import java.awt.event.ActionEvent;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ExtendedServletsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.MarkupLanguagesType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.PageType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webext.WSWebExtDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author  dlm198383
 */
public class WSMarkupLanguagesPanel extends SectionInnerPanel implements java.awt.event.ItemListener{
    
    
    ExtendedServletsType extendedServlet;
    MarkupLanguagesType markupLanguage;
    WSWebExtDataObject dObj;
    javax.swing.JTabbedPane markupLanguagesTabPanel;
    
    
    public WSMarkupLanguagesPanel(SectionView view, final WSWebExtDataObject dObj,  final MarkupLanguagesType markupLanguage,final ExtendedServletsType extendedServlet,final javax.swing.JTabbedPane markupLanguagesTabPanel) {
        super(view);
        this.dObj=dObj;        
        this.markupLanguage=markupLanguage;
        this.markupLanguagesTabPanel=markupLanguagesTabPanel;
        this.extendedServlet=extendedServlet;
        initComponents();
        nameComboBox.setModel(new javax.swing.DefaultComboBoxModel(MarkupLanguagesType.AVALIABLE_NAMES));
        mimeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(MarkupLanguagesType.AVALIABLE_MIME_TYPES));
        
        idField.setText(markupLanguage.getXmiId());
        nameComboBox.setSelectedItem(markupLanguage.getName());
        mimeTypeComboBox.setSelectedItem(markupLanguage.getMimeType());
        
        addModifier(idField);
        nameComboBox.addItemListener(this);
        mimeTypeComboBox.addItemListener(this);
        errorPageComboBox.addItemListener(this);
        defaultPageComboBox.addItemListener(this);
        
        setComboBoxModels();
        
        PageTableModel model = new PageTableModel(dObj.getModelSynchronizer());
        PagesTablePanel ptp= new PagesTablePanel(dObj, model,errorPageComboBox,defaultPageComboBox);
        ptp.setModel(markupLanguage,markupLanguage.getPages());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        //gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        gridBagConstraints.weightx = 1.0;
        pagesContainerPanel.add(ptp,gridBagConstraints);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedTab=markupLanguagesTabPanel.getSelectedIndex();
                markupLanguagesTabPanel.removeTabAt(selectedTab);
                for(int i=0;i<markupLanguagesTabPanel.getTabCount();i++) {
                 markupLanguagesTabPanel.setTitleAt(i,""+(i+1));   
                }                
                extendedServlet.removeMarkupLanguages(markupLanguage);
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(false);
            }
        });
    }
    public void setComboBoxModels() {
        int pagesNumber=markupLanguage.getPages().length;
        if(pagesNumber!=0) {
            String [] pagesNames=new String[pagesNumber];
            for(int i=0;i<pagesNumber;i++) {
                pagesNames[i]=markupLanguage.getPages(i).getXmiId();
            }
            javax.swing.DefaultComboBoxModel modelError=new javax.swing.DefaultComboBoxModel(pagesNames);
            errorPageComboBox.setModel(modelError);
            errorPageComboBox.setSelectedItem(markupLanguage.getErrorPage());
            
            javax.swing.DefaultComboBoxModel modelDefault=new javax.swing.DefaultComboBoxModel(pagesNames);
            defaultPageComboBox.setModel(modelDefault);
            defaultPageComboBox.setSelectedItem(markupLanguage.getDefaultPage());
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==idField) {
            markupLanguage.setXmiId(idField.getText().trim());
        } 
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==idField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {        
        if (idField==source) {
            idField.setText(markupLanguage.getXmiId());
        }
    }
    /*
    protected void signalUIChange() {
        dObj.modelUpdatedFromUI();
    }*/
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("ID".equals(errorId)) return idField;
        return null;
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        if(evt.getSource()==nameComboBox) {
            markupLanguage.setName((String)nameComboBox.getSelectedItem());
            
        } else if(evt.getSource()==mimeTypeComboBox) {
            markupLanguage.setMimeType((String)mimeTypeComboBox.getSelectedItem());
        } else if(evt.getSource()==errorPageComboBox) {
            markupLanguage.setErrorPage((String)errorPageComboBox.getSelectedItem());
        } else if(evt.getSource()==defaultPageComboBox) {
            markupLanguage.setDefaultPage((String)defaultPageComboBox.getSelectedItem());
        }
        // TODO add your handling code here:
        dObj.modelUpdatedFromUI();
        //dObj.setChangedFromUI(true);
        dObj.setChangedFromUI(false);
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
        jLabel1 = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        mimeTypeComboBox = new javax.swing.JComboBox();
        errorPageComboBox = new javax.swing.JComboBox();
        nameComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        defaultPageComboBox = new javax.swing.JComboBox();
        pagesContainerPanel = new javax.swing.JPanel();
        deleteButton = new javax.swing.JButton();

        jLabel1.setText("Name:");

        jLabel2.setText("ID:");

        jLabel4.setText("MIME Type:");

        jLabel3.setText("Error Page:");

        jLabel6.setText("Default Page:");

        defaultPageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultPageComboBoxActionPerformed(evt);
            }
        });

        pagesContainerPanel.setLayout(new java.awt.GridBagLayout());

        pagesContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        deleteButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_DeleteMarkupLanguage"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pagesContainerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 459, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel6)
                            .add(jLabel4)
                            .add(jLabel2)
                            .add(jLabel1)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(idField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, nameComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, defaultPageComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, errorPageComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.LEADING, mimeTypeComboBox, 0, 112, Short.MAX_VALUE))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 102, Short.MAX_VALUE)
                                .add(deleteButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(idField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(nameComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(deleteButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(mimeTypeComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(errorPageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel6)
                    .add(defaultPageComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(pagesContainerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void defaultPageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultPageComboBoxActionPerformed
            }//GEN-LAST:event_defaultPageComboBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox defaultPageComboBox;
    private javax.swing.JButton deleteButton;
    private javax.swing.JComboBox errorPageComboBox;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox mimeTypeComboBox;
    private javax.swing.JComboBox nameComboBox;
    private javax.swing.JPanel pagesContainerPanel;
    // End of variables declaration//GEN-END:variables
    
}
