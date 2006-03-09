/*
 * WSWebExtAttributesPanel.java
 *
 */

package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webext.*;

/**
 *
 * @author  dlm198383
 */
public class WSWebExtAttributesPanel extends  SectionInnerPanel  implements java.awt.event.ItemListener, javax.swing.event.ChangeListener {
    
    WSWebExt webext;
    WSWebExtDataObject dObj;
    
    /** Creates new form WSWebExtAttributesPanel */
    public WSWebExtAttributesPanel(SectionView view, WSWebExtDataObject dObj,  WSWebExt webext) {
        super(view);
        this.dObj=dObj;
        this.webext=webext;
        initComponents();
        fileServingCheckBox.setSelected(webext.getFileServingEnabled());        
        serveServletsCheckBox.setSelected(webext.getServeServletsByClassname());
        directoryBrowsingCheckBox.setSelected(webext.getDirectoryBrowsing());
        reloadIntervalCheckBox.setSelected(webext.getReload());
        SpinnerModel sm=new javax.swing.SpinnerNumberModel(0,0,600,1);
        reloadIntervalSpinner.setModel(sm);
        reloadIntervalSpinner.setValue(new Integer(webext.getReloadInterval()));
        precompileJPSCheckBox.setSelected(webext.getPrecompileJSPs());
        autoRequestEncCheckBox.setSelected(webext.getAutoRequestEncoding());
        autoResponseEncCheckBox.setSelected(webext.getAutoResponseEncoding());
        autoLoadFiltersCheckBox.setSelected(webext.getAutoLoadFilters());
        additionalClassPathField.setText(webext.getAdditionalClassPath());
        defaultErrorPageField.setText(webext.getDefaultErrorPage());
        hrefField.setText(webext.getWebApplicationHref());
        nameField.setText(webext.getXmiId());
        
        fileServingCheckBox.addItemListener(this);
        serveServletsCheckBox.addItemListener(this);
        directoryBrowsingCheckBox.addItemListener(this);
        reloadIntervalCheckBox.addItemListener(this);
        precompileJPSCheckBox.addItemListener(this);
        autoRequestEncCheckBox.addItemListener(this);
        autoResponseEncCheckBox.addItemListener(this);
        autoLoadFiltersCheckBox.addItemListener(this);
        reloadIntervalSpinner.addChangeListener(this);
        addModifier(additionalClassPathField);        
        addModifier(defaultErrorPageField);
        addModifier(hrefField);
        addModifier(nameField);
        //addModifier(virtualHostField);
        //getSectionView().getErrorPanel().clearError();
    }
    public void setValue(javax.swing.JComponent source,Object value) {
        if (source==additionalClassPathField) {
            webext.setAdditionalClassPath((String)value);
        } else if (source==defaultErrorPageField) {
            webext.setDefaultErrorPage((String)value);
        } else if(source==hrefField) {
            webext.setWebApplicationHref((String)value);
        }
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        //if ("name".equals(errorId)) return nameField;
        //if ("vhn".equals(errorId)) return virtualHostField;
        return null;
    }
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        webext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
        dObj.modelUpdatedFromUI();
    }
    
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        // TODO add your handling code here:
        
        webext.setPrecompileJSPs(precompileJPSCheckBox.isSelected());
        webext.setServeServletsByClassname(serveServletsCheckBox.isSelected());
        webext.setDirectoryBrowsing(directoryBrowsingCheckBox.isSelected());
        webext.setAutoRequestEncoding(autoRequestEncCheckBox.isSelected());
        webext.setAutoResponseEncoding(autoResponseEncCheckBox.isSelected());
        webext.setAutoLoadFilters(autoLoadFiltersCheckBox.isSelected());
        webext.setFileServingEnabled(fileServingCheckBox.isSelected());
        webext.setReload(reloadIntervalCheckBox.isSelected());
        webext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
        dObj.modelUpdatedFromUI();
        
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
        reloadIntervalCheckBox = new javax.swing.JCheckBox();
        reloadIntervalSpinner = new javax.swing.JSpinner();
        additionalClassPathLabel = new javax.swing.JLabel();
        additionalClassPathField = new javax.swing.JTextField();
        fileServingCheckBox = new javax.swing.JCheckBox();
        directoryBrowsingCheckBox = new javax.swing.JCheckBox();
        serveServletsCheckBox = new javax.swing.JCheckBox();
        precompileJPSCheckBox = new javax.swing.JCheckBox();
        autoRequestEncCheckBox = new javax.swing.JCheckBox();
        autoResponseEncCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        defaultErrorPageField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        autoLoadFiltersCheckBox = new javax.swing.JCheckBox();

        reloadIntervalCheckBox.setText("Reload Interval:");
        reloadIntervalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reloadIntervalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        reloadIntervalSpinner.setFont(new java.awt.Font("Courier", 0, 12));

        additionalClassPathLabel.setText("Additional Class Path:");

        fileServingCheckBox.setText("File Serving");
        fileServingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileServingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        directoryBrowsingCheckBox.setText("Directory Browsing");
        directoryBrowsingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryBrowsingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        serveServletsCheckBox.setText("Serve Servlets by Classname");
        serveServletsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serveServletsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        precompileJPSCheckBox.setText("Precompile JSPs");
        precompileJPSCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        precompileJPSCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        autoRequestEncCheckBox.setText("Auto Request Encoding");
        autoRequestEncCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoRequestEncCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        autoResponseEncCheckBox.setText("Auto Response Encoding");
        autoResponseEncCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoResponseEncCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setText("Default Error Page:");

        jLabel2.setText("Name in Web.xml:");

        jLabel3.setText("Name:");

        autoLoadFiltersCheckBox.setText("Auto Load Filters");
        autoLoadFiltersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoLoadFiltersCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(serveServletsCheckBox)
                    .add(fileServingCheckBox)
                    .add(directoryBrowsingCheckBox)
                    .add(precompileJPSCheckBox)
                    .add(autoRequestEncCheckBox)
                    .add(autoResponseEncCheckBox)
                    .add(layout.createSequentialGroup()
                        .add(reloadIntervalCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(reloadIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(jLabel1)
                            .add(additionalClassPathLabel)
                            .add(jLabel2)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .add(hrefField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .add(additionalClassPathField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, defaultErrorPageField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)))
                    .add(autoLoadFiltersCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(fileServingCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(serveServletsCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(directoryBrowsingCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(reloadIntervalCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(precompileJPSCheckBox))
                    .add(reloadIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autoRequestEncCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autoResponseEncCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autoLoadFiltersCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(additionalClassPathLabel)
                    .add(additionalClassPathField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(defaultErrorPageField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel2)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField additionalClassPathField;
    private javax.swing.JLabel additionalClassPathLabel;
    private javax.swing.JCheckBox autoLoadFiltersCheckBox;
    private javax.swing.JCheckBox autoRequestEncCheckBox;
    private javax.swing.JCheckBox autoResponseEncCheckBox;
    private javax.swing.JTextField defaultErrorPageField;
    private javax.swing.JCheckBox directoryBrowsingCheckBox;
    private javax.swing.JCheckBox fileServingCheckBox;
    private javax.swing.JTextField hrefField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField nameField;
    private javax.swing.JCheckBox precompileJPSCheckBox;
    private javax.swing.JCheckBox reloadIntervalCheckBox;
    private javax.swing.JSpinner reloadIntervalSpinner;
    private javax.swing.JCheckBox serveServletsCheckBox;
    // End of variables declaration//GEN-END:variables
    /*
    public javax.swing.JTextField getAdditionalClassPathField() {
        return additionalClassPathField;
    }
    public javax.swing.JCheckBox getAutoRequestEncCheckBox() {
        return autoRequestEncCheckBox;
    }
    public javax.swing.JCheckBox getAutoResponseEncCheckBox() {
        return autoResponseEncCheckBox;
    }
    public javax.swing.JCheckBox getDirectoryBrowsingCheckBox() {
        return directoryBrowsingCheckBox;
    }
    public javax.swing.JCheckBox getFileServingCheckBox(){
        return fileServingCheckBox;
    }
    public javax.swing.JCheckBox getPrecompileJPSCheckBox(){
        return precompileJPSCheckBox;
    }
    public javax.swing.JCheckBox getReloadIntervalCheckBox(){
        return reloadIntervalCheckBox;
    }
    public javax.swing.JSpinner getReloadIntervalSpinner(){
        return reloadIntervalSpinner;
    }
    public javax.swing.JCheckBox getServeServletsCheckBox(){
        return  serveServletsCheckBox;
    }*/
    
}
