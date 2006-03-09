/*
 * WSWebExtAttributesPanel.java
 *
 */

package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ExtendedServletsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.MarkupLanguagesType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webext.WSWebExtDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;

/*
 *
 * @author  dlm198383
 */
public class WSExtendedServletPanel extends /*javax.swing.JPanel*/ SectionInnerPanel implements java.awt.event.ItemListener, javax.swing.event.ChangeListener {
    
    //private WSWebExtRootCustomizer masterPanel;
    ExtendedServletsType extendedServlet;
    WSWebExtDataObject dObj;
    SectionView view;
    
    private javax.swing.JCheckBox localTransactionCheckBox;
    private javax.swing.JTextField transactionNameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel unresolvedLabel;
    private javax.swing.JComboBox unresolvedActionComboBox;
    private javax.swing.JCheckBox resolverCheckBox;
    private javax.swing.JComboBox resolverComboBox;
    private javax.swing.JCheckBox boundaryCheckBox;
    private javax.swing.JComboBox boundaryComboBox;
    
    
    public WSExtendedServletPanel(SectionView view, WSWebExtDataObject dObj,  ExtendedServletsType extendedServlet) {
        super(view);
        this.view=view;
        this.dObj=dObj;
        this.extendedServlet=extendedServlet;
        initComponents();
        
        bindLocalTransactionComponents();
        
        initLocalTransactionComponents();
        
        ((LocalTransactionPanel)containerPanel).setEnabledComponents();
        
        
        nameField.setText(extendedServlet.getXmiId());
        hrefField.setText(extendedServlet.getHref());
        addModifier(nameField);
        addModifier(hrefField);
        addValidatee(nameField);
        addValidatee(hrefField);
        
        getSectionView().getErrorPanel().clearError();
        
        
        int size=extendedServlet.sizeMarkupLanguages();
        MarkupLanguagesType [] markupLanguages = extendedServlet.getMarkupLanguages();
        for(int i=0;i<size;i++) {
            markupLanguagesTabPanel.addTab(/*markupLanguages[i].getName()*/""+(i+1),new WSMarkupLanguagesPanel(view,dObj,markupLanguages[i],extendedServlet,markupLanguagesTabPanel));
        }
        
    }
    
    private void bindLocalTransactionComponents(){
        LocalTransactionPanel localTransactionPanel=(LocalTransactionPanel)containerPanel;
        
        localTransactionCheckBox=localTransactionPanel.getLocalTransactionCheckBox();
        transactionNameField=localTransactionPanel.getTransactionNameField();
        unresolvedActionComboBox=localTransactionPanel.getUnresolvedActionComboBox();
        resolverCheckBox=localTransactionPanel.getResolverCheckBox();
        resolverComboBox=localTransactionPanel.getResolverComboBox();
        boundaryCheckBox=localTransactionPanel.getBoundaryCheckBox();
        boundaryComboBox=localTransactionPanel.getBoundaryComboBox();
        nameLabel=localTransactionPanel.getNameLabel();
        unresolvedLabel=localTransactionPanel.getUnresolvedActionLable();
        localTransactionPanel.setComponentsBackground(SectionVisualTheme.getSectionActiveBackgroundColor());
    }
    
    
    public void initLocalTransactionComponents() {        
        addModifier(transactionNameField);
        addValidatee(transactionNameField);
        boolean localTransactionEnabled=(extendedServlet.getLocalTransaction()==null)?false:true;
        localTransactionCheckBox.setSelected(localTransactionEnabled);
        
        if(localTransactionEnabled) {
            transactionNameField.setText(extendedServlet.getLocalTransactionXmiId());
            
            unresolvedActionComboBox.setSelectedItem(extendedServlet.getLocalTransactionUnresolvedAction());
            String str=extendedServlet.getLocalTransactionResolver();
            if(str==null) {
                resolverCheckBox.setSelected(false);
            } else {
                resolverCheckBox.setSelected(true);
                resolverComboBox.setSelectedItem(str);
            }
            
            str=extendedServlet.getLocalTransactionBoundary();
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
            extendedServlet.setXmiId((String)value);
        } else if (source==hrefField) {
            extendedServlet.setHref((String)value);
        } else if(source==transactionNameField) {
            extendedServlet.setLocalTransactionXmiId((String)value);
        }
        
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        //webext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
        dObj.modelUpdatedFromUI();
    }
    
    
    public void changeLocalTransactionState() {
        if(localTransactionCheckBox.isSelected()) {
            extendedServlet.setLocalTransaction("");
            extendedServlet.setLocalTransactionXmiId(transactionNameField.getText());
            
            extendedServlet.setLocalTransactionUnresolvedAction(
                    unresolvedActionComboBox.getSelectedItem().toString());
            
            extendedServlet.setLocalTransactionResolver(
                    resolverCheckBox.isSelected()?
                        resolverComboBox.getSelectedItem().toString():
                        null);
            extendedServlet.setLocalTransactionBoundary(
                    boundaryCheckBox.isSelected()?
                        boundaryComboBox.getSelectedItem().toString():
                        null);
        } else {
            extendedServlet.setLocalTransaction(null);
            //extendedServlet.setLocalTransactionXmiId(null);
        }
        ((LocalTransactionPanel)containerPanel).setEnabledComponents();
    }
    
    
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        // TODO add your handling code here:
        extendedServlet.setXmiId(nameField.getText());
        extendedServlet.setHref(hrefField.getText());
        changeLocalTransactionState();        
        dObj.modelUpdatedFromUI();
    }
    
    
    public javax.swing.JTextField getNameField() {
        return nameField;
    }
    public javax.swing.JTextField getHrefField() {
        return hrefField;
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==nameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Extended Servlet Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==hrefField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Extended Servlet HREF", comp));
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
            nameField.setText(extendedServlet.getXmiId());
        }
        if (hrefField==source) {
            hrefField.setText(extendedServlet.getHref());
        }
        if (transactionNameField==source) {
            transactionNameField.setText(extendedServlet.getLocalTransactionXmiId());
        }
        
    }
    /*
    protected void signalUIChange() {
        dObj.modelUpdatedFromUI();
    }*/
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("Extended Servlet Name".equals(errorId)) return nameField;
        if ("Extended Servlet HREF".equals(errorId)) return hrefField;
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
        jLabel1 = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        markupLanguagesTabPanel = new javax.swing.JTabbedPane();
        addMarkupLanguagesButton = new javax.swing.JButton();
        containerPanel = new LocalTransactionPanel();

        jLabel1.setText("HREF:");

        jLabel2.setText("Name:");

        jLabel5.setText("Markup Languages");

        addMarkupLanguagesButton.setText("Add");
        addMarkupLanguagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMarkupLanguagesButtonActionPerformed(evt);
            }
        });

        containerPanel.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel2)
                    .add(jLabel1))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .add(10, 10, 10))
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jSeparator1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(containerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .addContainerGap())
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(markupLanguagesTabPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 410, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 267, Short.MAX_VALUE)
                        .add(addMarkupLanguagesButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .add(6, 6, 6)
                .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(containerPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(14, 14, 14)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(addMarkupLanguagesButton))
                .add(11, 11, 11)
                .add(markupLanguagesTabPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void addMarkupLanguagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMarkupLanguagesButtonActionPerformed
         
        final MarkupLanguagePanel dialogPanel=new MarkupLanguagePanel();
        final EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(WSExtendedServletPanel.class,"TTL_MarkupLanguages"),true) {
            protected String validate() {
                String id = dialogPanel.getIdField().getText().trim();
                String name = ((String)dialogPanel.getNameComboBox().getSelectedItem()).trim();
                String mimeType = ((String)dialogPanel.getMimeTypeComboBox().getSelectedItem()).trim();
                int size=extendedServlet.sizeMarkupLanguages();
                
                for(int i=0;i<size;i++) {
                    MarkupLanguagesType ml=extendedServlet.getMarkupLanguages(i);
                    if(ml.getXmiId().equals(id)) {
                        return NbBundle.getMessage(WSExtendedServletPanel.class,"TXT_CurrentIdExists");
                    }
                }
                return null;
            }
        };
        dialog.setValid(false); // disable OK button
        
        
        javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
        dialogPanel.getIdField().getDocument().addDocumentListener(docListener);
        dialogPanel.getNameComboBox().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.checkValues();
            }
        });
        dialogPanel.getMimeTypeComboBox().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.checkValues();
            }
        });
        
        
        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
        d.setVisible(true);
        dialogPanel.getIdField().getDocument().removeDocumentListener(docListener);
        
        if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
            MarkupLanguagesType markupLang=new MarkupLanguagesType();
            markupLang.setXmiId(dialogPanel.getIdField().getText().trim());
            markupLang.setName(((String) dialogPanel.getNameComboBox().getSelectedItem()));
            markupLang.setMimeType(((String) dialogPanel.getMimeTypeComboBox().getSelectedItem()));
            markupLang.setErrorPage("");
            markupLang.setDefaultPage("");
            extendedServlet.addMarkupLanguages(markupLang);
            int count=markupLanguagesTabPanel.getTabCount();
            markupLanguagesTabPanel.addTab(""+(count+1),new WSMarkupLanguagesPanel(view,dObj,markupLang,extendedServlet,markupLanguagesTabPanel));
            markupLanguagesTabPanel.setSelectedIndex(count);
            markupLanguagesTabPanel.getTitleAt(count);
            
            dObj.modelUpdatedFromUI();
            dObj.setChangedFromUI(false);
        }
        
    }//GEN-LAST:event_addMarkupLanguagesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMarkupLanguagesButton;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JTextField hrefField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane markupLanguagesTabPanel;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables
    
}
