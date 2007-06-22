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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  petr
 */
public class JSFConfigurationPanelVisual extends javax.swing.JPanel implements HelpCtx.Provider, DocumentListener  {

    private JSFConfigurationPanel panel;
    
    private ArrayList <Library> jsfLibraries;
    private boolean webModule25Version = true;
    
    /** Creates new form JSFConfigurationPanelVisual */
    public JSFConfigurationPanelVisual(JSFConfigurationPanel panel, boolean customizer) {
        initComponents();
        this.panel = panel;
        initLibraries();
        
        tURLPattern.getDocument().addDocumentListener(this);
        cbPackageJars.setVisible(false);
        if (customizer){
            enableComponents(false);
        }
    }
    
    private void initLibraries(){
        Library libraries[] = LibraryManager.getDefault().getLibraries();
        Vector <String> items = new Vector();
        jsfLibraries = new ArrayList();
        
        for (int i = 0; i < libraries.length; i++) {
            if (libraries[i].getName().startsWith("JSF-") || libraries[i].getName().equals("jsf12")) { //NOI18N
                String displayName = libraries[i].getDisplayName();
                items.add(displayName);
                jsfLibraries.add(libraries[i]);
            }
        }
        
        cbLibraries.setModel(new DefaultComboBoxModel(items));
        if (items.size() == 0){
            rbRegisteredLibrary.setEnabled(false);
            cbLibraries.setEnabled(false);
            rbNewLibrary.setSelected(true);
            panel.setLibrary(null);
        } else {
            rbRegisteredLibrary.setEnabled(true);
            rbRegisteredLibrary.setSelected(true);
            cbLibraries.setEnabled(true);
        }
        repaint();
    }

    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jsfTabbedPane = new javax.swing.JTabbedPane();
        confPanel = new javax.swing.JPanel();
        lServletName = new javax.swing.JLabel();
        tServletName = new javax.swing.JTextField();
        lURLPattern = new javax.swing.JLabel();
        tURLPattern = new javax.swing.JTextField();
        cbValidate = new javax.swing.JCheckBox();
        cbVerify = new javax.swing.JCheckBox();
        cbPackageJars = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        libPanel = new javax.swing.JPanel();
        rbRegisteredLibrary = new javax.swing.JRadioButton();
        cbLibraries = new javax.swing.JComboBox();
        rbNewLibrary = new javax.swing.JRadioButton();
        lDirectory = new javax.swing.JLabel();
        jtFolder = new javax.swing.JTextField();
        jbBrowse = new javax.swing.JButton();
        lVersion = new javax.swing.JLabel();
        jtVersion = new javax.swing.JTextField();
        rbNoneLibrary = new javax.swing.JRadioButton();

        setLayout(new java.awt.CardLayout());

        lServletName.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lServletName").charAt(0));
        lServletName.setLabelFor(tServletName);
        lServletName.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_Servlet_Name")); // NOI18N

        tServletName.setEditable(false);
        tServletName.setText("Faces Servlet");

        lURLPattern.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_lURLPattern").charAt(0));
        lURLPattern.setLabelFor(tURLPattern);
        lURLPattern.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_URL_Pattern")); // NOI18N

        tURLPattern.setText("/faces/*");

        cbValidate.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_cbValidate").charAt(0));
        cbValidate.setSelected(true);
        cbValidate.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Validate_XML")); // NOI18N
        cbValidate.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbValidate.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbVerify.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle").getString("MNE_cbVerify").charAt(0));
        cbVerify.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Verify_Objects")); // NOI18N
        cbVerify.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbVerify.setMargin(new java.awt.Insets(0, 0, 0, 0));

        cbPackageJars.setSelected(true);
        cbPackageJars.setText(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "CB_Package_JARs")); // NOI18N
        cbPackageJars.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cbPackageJars.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jPanel1.setEnabled(false);
        jPanel1.setFocusable(false);
        jPanel1.setRequestFocusEnabled(false);

        org.jdesktop.layout.GroupLayout confPanelLayout = new org.jdesktop.layout.GroupLayout(confPanel);
        confPanel.setLayout(confPanelLayout);
        confPanelLayout.setHorizontalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 395, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(confPanelLayout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(lServletName)
                        .add(37, 37, 37)
                        .add(tServletName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE))
                    .add(confPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lURLPattern)
                            .add(cbValidate))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(cbVerify)
                            .add(tURLPattern, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)))
                    .add(confPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(cbPackageJars)))
                .addContainerGap())
        );
        confPanelLayout.setVerticalGroup(
            confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(confPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lServletName)
                    .add(tServletName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lURLPattern)
                    .add(tURLPattern, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(17, 17, 17)
                .add(confPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cbValidate)
                    .add(cbVerify))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cbPackageJars)
                .add(77, 77, 77)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        tServletName.getAccessibleContext().setAccessibleDescription(null);
        tURLPattern.getAccessibleContext().setAccessibleDescription(null);
        cbValidate.getAccessibleContext().setAccessibleDescription(null);
        cbVerify.getAccessibleContext().setAccessibleDescription(null);
        cbPackageJars.getAccessibleContext().setAccessibleDescription(null);

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Configuration"), confPanel); // NOI18N

        libPanel.setAlignmentX(0.2F);
        libPanel.setAlignmentY(0.2F);

        buttonGroup1.add(rbRegisteredLibrary);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/jsf/wizards/Bundle"); // NOI18N
        rbRegisteredLibrary.setText(bundle.getString("LBL_REGISTERED_LIBRARIES")); // NOI18N
        rbRegisteredLibrary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbRegisteredLibrary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbRegisteredLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbRegisteredLibraryItemStateChanged(evt);
            }
        });

        cbLibraries.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbLibraries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbLibrariesActionPerformed(evt);
            }
        });

        buttonGroup1.add(rbNewLibrary);
        rbNewLibrary.setText(bundle.getString("LBL_CREATE_NEW_LIBRARY")); // NOI18N
        rbNewLibrary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbNewLibrary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbNewLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNewLibraryItemStateChanged(evt);
            }
        });

        lDirectory.setText(bundle.getString("LBL_INSTALL_DIR")); // NOI18N

        jtFolder.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jtFolderKeyPressed(evt);
            }
        });

        jbBrowse.setText(bundle.getString("LBL_Browse")); // NOI18N
        jbBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbBrowseActionPerformed(evt);
            }
        });

        lVersion.setText(bundle.getString("LBL_VERSION")); // NOI18N

        jtVersion.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jtVersionKeyReleased(evt);
            }
        });

        buttonGroup1.add(rbNoneLibrary);
        rbNoneLibrary.setText(bundle.getString("LBL_Any_Library")); // NOI18N
        rbNoneLibrary.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbNoneLibrary.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbNoneLibrary.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                rbNoneLibraryItemStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout libPanelLayout = new org.jdesktop.layout.GroupLayout(libPanel);
        libPanel.setLayout(libPanelLayout);
        libPanelLayout.setHorizontalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(libPanelLayout.createSequentialGroup()
                        .add(rbRegisteredLibrary)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cbLibraries, 0, 326, Short.MAX_VALUE))
                    .add(rbNewLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE)
                    .add(libPanelLayout.createSequentialGroup()
                        .add(17, 17, 17)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(lDirectory)
                            .add(lVersion))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 43, Short.MAX_VALUE)
                        .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(jtVersion)
                            .add(jtFolder, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jbBrowse))
                    .add(rbNoneLibrary, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 476, Short.MAX_VALUE))
                .addContainerGap())
        );
        libPanelLayout.setVerticalGroup(
            libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(libPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rbRegisteredLibrary)
                    .add(cbLibraries, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbNewLibrary)
                .add(7, 7, 7)
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jbBrowse)
                    .add(lDirectory)
                    .add(jtFolder, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(libPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lVersion)
                    .add(jtVersion, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(rbNoneLibrary)
                .addContainerGap(138, Short.MAX_VALUE))
        );

        jsfTabbedPane.addTab(org.openide.util.NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_TAB_Libraries"), libPanel); // NOI18N

        add(jsfTabbedPane, "card10");
    }// </editor-fold>//GEN-END:initComponents

private void rbNoneLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNoneLibraryItemStateChanged
    updateLibrary();
}//GEN-LAST:event_rbNoneLibraryItemStateChanged

private void jtVersionKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtVersionKeyReleased
    checkNewLibrarySetting();
}//GEN-LAST:event_jtVersionKeyReleased

private void rbNewLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbNewLibraryItemStateChanged
    updateLibrary();
}//GEN-LAST:event_rbNewLibraryItemStateChanged

private void cbLibrariesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbLibrariesActionPerformed
    panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()));
}//GEN-LAST:event_cbLibrariesActionPerformed

private void rbRegisteredLibraryItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_rbRegisteredLibraryItemStateChanged
    updateLibrary();
}//GEN-LAST:event_rbRegisteredLibraryItemStateChanged

private void jbBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbBrowseActionPerformed
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle(NbBundle.getMessage(JSFConfigurationPanelVisual.class,"LBL_SelectLibraryLocation")); //NOI18N
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    
    if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
        File projectDir = chooser.getSelectedFile();
        jtFolder.setText(projectDir.getAbsolutePath());
        checkNewLibrarySetting();
    }
}//GEN-LAST:event_jbBrowseActionPerformed

private void jtFolderKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jtFolderKeyPressed
    checkNewLibrarySetting();
}//GEN-LAST:event_jtFolderKeyPressed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbLibraries;
    private javax.swing.JCheckBox cbPackageJars;
    private javax.swing.JCheckBox cbValidate;
    private javax.swing.JCheckBox cbVerify;
    private javax.swing.JPanel confPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton jbBrowse;
    private javax.swing.JTabbedPane jsfTabbedPane;
    private javax.swing.JTextField jtFolder;
    private javax.swing.JTextField jtVersion;
    private javax.swing.JLabel lDirectory;
    private javax.swing.JLabel lServletName;
    private javax.swing.JLabel lURLPattern;
    private javax.swing.JLabel lVersion;
    private javax.swing.JPanel libPanel;
    private javax.swing.JRadioButton rbNewLibrary;
    private javax.swing.JRadioButton rbNoneLibrary;
    private javax.swing.JRadioButton rbRegisteredLibrary;
    private javax.swing.JTextField tServletName;
    private javax.swing.JTextField tURLPattern;
    // End of variables declaration//GEN-END:variables
 
    void enableComponents(boolean enable) {
        Component[] components;
        
        components = confPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }
        
        components = libPanel.getComponents();
        for (int i = 0; i < components.length; i++) {
            components[i].setEnabled(enable);
        }
        
        if (enable){
            updateLibrary();
        }  
    }
    
    boolean valid(WizardDescriptor wizardDescriptor) {
        String urlPattern = tURLPattern.getText();
        if (urlPattern == null || urlPattern.trim().equals("")){
          wizardDescriptor.putProperty("WizardPanel_errorMessage",                                  // NOI18N
                NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsEmpty"));
          return false;
        }
        if (!isPatternValid(urlPattern)){
          wizardDescriptor.putProperty("WizardPanel_errorMessage",                                  // NOI18N
                NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_URLPatternIsNotValid"));
          return false;
        }
        
        if (!webModule25Version && jsfLibraries.size() <= 0) {
            if ((rbNewLibrary.isSelected() && (jtFolder.getText().trim().length() <= 0 || jtVersion.getText().trim().length() <= 0))
                    || (rbRegisteredLibrary.isSelected() && cbLibraries.getItemCount() <= 0)) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage",                                  // NOI18N
                    NbBundle.getMessage(JSFConfigurationPanelVisual.class, "LBL_MissingJSF"));
                return false;
            }
        }
        
        if(wizardDescriptor!=null)
            wizardDescriptor.putProperty("WizardPanel_errorMessage", null);                             // NOI18N
        return true;
    }
    
    private boolean isPatternValid(String pattern){
        if (pattern.startsWith("*.")){
            String p = pattern.substring(2);
            if (p.indexOf('.') == -1 && p.indexOf('*') == -1
                    && p.indexOf('/') == -1 && !p.trim().equals(""))
                return true;
        }
        if (pattern.endsWith("/*") && pattern.startsWith("/"))
            return true;
        return false;
    }
    
    void validate (WizardDescriptor d) throws WizardValidationException {
//        projectLocationPanel.validate (d);
    }
    
    void read (WizardDescriptor d) {
        if (d.getProperty("j2eeLevel").equals("1.5")) //NOI81N
            webModule25Version = true;
        else
            webModule25Version = false;
        
//        projectLocationPanel.read(d);
//        optionsPanel.read(d);
    }

    void store(WizardDescriptor d) {
//        projectLocationPanel.store(d);
//        optionsPanel.store(d);
    }
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(JSFConfigurationPanelVisual.class);
    }
    
    public void removeUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void insertUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public void changedUpdate(javax.swing.event.DocumentEvent e) {
        panel.fireChangeEvent();
    }

    public String getServletName(){
        return tServletName.getText();
    }
    
    public void setServletName(String name){
        tServletName.setText(name);
    }
    
    public String getURLPattern(){
        return tURLPattern.getText();
    }
    
    public void setURLPattern(String pattern){
        tURLPattern.setText(pattern);
    }
    
    public boolean validateXML(){
        return cbValidate.isSelected();
    }
    
    public void setValidateXML(boolean ver){
        cbValidate.setSelected(ver);
    }
    
    public boolean verifyObjects(){
        return cbVerify.isSelected();
    }
    
    public void setVerifyObjects(boolean val){
        cbVerify.setSelected(val);
    }
    
    public boolean packageJars(){
        return cbPackageJars.isSelected();
    }
    
    private void updateLibrary(){
        if (cbLibraries.getItemCount() == 0)
            rbRegisteredLibrary.setEnabled(false);
        
        if (rbNoneLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(false);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.NONE);
            panel.setErrorMessage(null);
        } else if (rbRegisteredLibrary.isSelected()){
            enableNewLibraryComponent(false);
            enableDefinedLibraryComponent(true);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.USED);
            if (jsfLibraries.size() > 0){
                panel.setLibrary(jsfLibraries.get(cbLibraries.getSelectedIndex()));
            }
            panel.setErrorMessage(null);
        } else if (rbNewLibrary.isSelected()){
            enableNewLibraryComponent(true);
            enableDefinedLibraryComponent(false);
            panel.setLibraryType(JSFConfigurationPanel.LibraryType.NEW);
            checkNewLibrarySetting();
        }
    }
    
    private void enableDefinedLibraryComponent(boolean enabled){
        cbLibraries.setEnabled(enabled);
    }
    
    private void enableNewLibraryComponent(boolean enabled){
        lDirectory.setEnabled(enabled);
        jtFolder.setEnabled(enabled);
        jbBrowse.setEnabled(enabled);
        lVersion.setEnabled(enabled);
        jtVersion.setEnabled(enabled);
    }

    private void checkNewLibrarySetting(){
        String message = null;
        String fileName = jtFolder.getText();
        if (fileName == null || "".equals(fileName)){
            message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_PathIsNotFaceletsFolder");
        } else {
            File folder = new File(fileName);
            if (!JSFUtils.isJSFInstallFolder(folder)){
                message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_PathIsNotFaceletsFolder");
            } else {
                panel.setInstallFolder(folder);
                
                String version = jtVersion.getText().trim();
                if (version == null || "".equals(version)){
                    message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_VersionHasToBeDefined");
                } else{
                    String name = "jsf-"+ JSFUtils.convertLibraryVersion(version);  //NOI18N
                    int length = jsfLibraries.size();
                    for (int i = 0; i < length; i++) {
                        if(jsfLibraries.get(i).getName().equals(name)){
                            message = NbBundle.getMessage(JSFConfigurationPanelVisual.class, "MSG_VersionAlreadyExist");
                        }
                    }
                }
                if (message == null){
                    panel.setNewLibraryVersion(jtVersion.getText().trim());
                }
            }
        }
        panel.setErrorMessage(message);
    }

}
