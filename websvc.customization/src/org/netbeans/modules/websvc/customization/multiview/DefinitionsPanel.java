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
 * DefinitionsPanel.java
 *
 * Created on February 19, 2006, 8:33 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.api.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.api.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.api.customization.model.EnableMIMEContent;
import org.netbeans.modules.websvc.api.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.api.customization.model.JavaPackage;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.websvc.api.customization.model.CustomizationComponentFactory;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Roderico Cruz
 */
public class DefinitionsPanel extends SaveableSectionInnerPanel {
    private Definitions definitions;
    private WSDLModel model;
    private Node node;
    private boolean wsdlDirty;
    private DefinitionsActionListener listener;
    private DefaultItemListener defaultListener;
    
    /** Creates new form DefinitionsPanel */
    public DefinitionsPanel(SectionView view, Definitions definitions,
            Node node) {
        super(view);
        this.definitions = definitions;
        this.model = this.definitions.getModel();
        this.node = node;
        initComponents();
        disableEnterKey();
        if(!isClient(node)){
            enableAsyncMappingCB.setVisible(false);
        }
        
        enableAsyncMappingCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_ASYNC"));
        enableWrapperStyleCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_WRAPPER"));
        enableMIMEContentCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_MIME"));
        packageNameText.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_PACKAGE"));
        wsdlDirty = false;
        sync();
        
        defaultListener = new DefaultItemListener();
        ItemListener itemListener = (ItemListener)WeakListeners.create(ItemListener.class, defaultListener,
                defaultPackageCB);
        defaultPackageCB.addItemListener(itemListener);
        
        addValidatee(packageNameText);
        
        listener = new DefinitionsActionListener();
        addModifier(packageNameText);
        addModifier(defaultPackageCB);
        
        ActionListener eamListener = (ActionListener)WeakListeners.create(ActionListener.class, listener, enableAsyncMappingCB);
        enableAsyncMappingCB.addActionListener(eamListener);
        ActionListener emcListener = (ActionListener)WeakListeners.create(ActionListener.class, listener, enableMIMEContentCB);
        enableMIMEContentCB.addActionListener(emcListener);
        ActionListener ewsListener = (ActionListener)WeakListeners.create(ActionListener.class, listener, enableWrapperStyleCB);
        enableWrapperStyleCB.addActionListener(ewsListener);
    }
    
    
    class DefinitionsActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            setValue((JComponent)e.getSource(), null);
        }
    }
    
    private void sync(){
        List <DefinitionsCustomization> ee =
                definitions.getExtensibilityElements(DefinitionsCustomization.class);
        if (ee.size() == 1) {
            DefinitionsCustomization dc = ee.get(0);
            EnableAsyncMapping eam = dc.getEnableAsyncMapping();
            if(eam != null){
                setEnableAsyncMapping(eam.isEnabled());
            } else{ //default is false
                setEnableAsyncMapping(false);
            }
            
            EnableWrapperStyle ews = dc.getEnableWrapperStyle();
            if(ews != null){
                setEnableWrapperStyle(ews.isEnabled());
            } else{ //default is true
                setEnableWrapperStyle(true);
            }
            EnableMIMEContent emc = dc.getEnableMIMEContent();
            if (emc != null){
                setEnableMIMEContent(emc.isEnabled());
            } else{ //default is false
                setEnableMIMEContent(false);
            }
            JavaPackage javaPackage = dc.getPackage();
            if (javaPackage != null){
                setPackageName(javaPackage.getName());
            } else{ //default is false
                setPackageName(null);
            }
        } else {
            //no definitions bindings, set to defaults
            setEnableAsyncMapping(false);
            setEnableWrapperStyle(true);
            setEnableMIMEContent(false);
            setPackageName(null);
        }
    }
    
    public void setEnableAsyncMapping(boolean enable){
        enableAsyncMappingCB.setSelected(enable);
    }
    
    public boolean getEnableAsyncMapping(){
        return enableAsyncMappingCB.isSelected();
    }
    
    public void setEnableWrapperStyle(boolean enable){
        enableWrapperStyleCB.setSelected(enable);
    }
    
    public boolean getEnableWrapperStyle(){
        return enableWrapperStyleCB.isSelected();
    }
    
    public void setEnableMIMEContent(boolean enable){
        enableMIMEContentCB.setSelected(enable);
    }
    
    public boolean getEnableMIMEContent(){
        return enableMIMEContentCB.isSelected();
    }
    
    public void setPackageName(String name) {
        if (name == null) {
            packageNameText.setText("");
            defaultPackageCB.setSelected(true);
            packageNameText.setEnabled(false);
        } else {
            packageNameText.setEnabled(true);
            packageNameText.setText(name);
            defaultPackageCB.setSelected(false);

        }
    }
    
    public String getPackageName() {
        if (defaultPackageCB.isSelected()) {
            return null;
        } else {
            String packageName = packageNameText.getText().trim();
            if (packageName.length()>0) {
                return packageName;
            } else {
                return null;
            }
        }
    }
    
    public JComponent getErrorComponent(String string) {
        return new JButton();
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            if(defaultPackageCB.isSelected()){
                packageNameText.setEnabled(false);
            } else{
                packageNameText.setEnabled(true);
                packageNameText.requestFocus();
            }
        }
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <DefinitionsCustomization> ee =
                definitions.getExtensibilityElements(DefinitionsCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();

        //process default package name
        if(jComponent == packageNameText || jComponent == defaultPackageCB) {
            if (getPackageName() == null) {
                if (ee.size() == 1) { //there is an extensibility element
                    DefinitionsCustomization dc = ee.get(0);
                    JavaPackage javaPackage = dc.getPackage();
                    if(javaPackage != null){ //there is no EnableWrapperStyle, create one
                        try{
                            model.startTransaction();
                            dc.removePackage(javaPackage);
                            wsdlDirty = true;
                        } finally{
                            model.endTransaction();
                        }
                    }
                }
            } else {
                if (ee.size() == 1) { //there is an extensibility element
                    DefinitionsCustomization dc = ee.get(0);
                    JavaPackage javaPackage = dc.getPackage();
                    if(javaPackage == null){ //there is no EnableWrapperStyle, create one
                        try{
                            model.startTransaction();
                            javaPackage = factory.createJavaPackage(model);
                            javaPackage.setName(packageNameText.getText());
                            dc.setPackage(javaPackage);
                            wsdlDirty = true;
                        } finally{
                            model.endTransaction();
                        }
                    } else{ //there is an EnableWrapperStyle, reset it
                        try{
                            model.startTransaction();
                            javaPackage.setName(packageNameText.getText());
                            wsdlDirty = true;
                        } finally{
                            model.endTransaction();
                        }
                    }
                } else {  //there is no extensibility element, add a new one and add a new
                    //wrapper style element
                    DefinitionsCustomization dc = factory.createDefinitionsCustomization(model);
                    JavaPackage javaPackage = factory.createJavaPackage(model);
                    try{
                        model.startTransaction();
                        javaPackage.setName(packageNameText.getText());
                        dc.setPackage(javaPackage);
                        definitions.addExtensibilityElement(dc);
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                }
            }
        }
        //process Wrapper Style
        else if(jComponent == enableWrapperStyleCB){
            if(ee.size() == 1){ //there is an extensibility element
                DefinitionsCustomization dc = ee.get(0);
                EnableWrapperStyle ews = dc.getEnableWrapperStyle();
                if(ews == null){ //there is no EnableWrapperStyle, create one
                    try{
                        model.startTransaction();
                        ews = factory.createEnableWrapperStyle(model);
                        ews.setEnabled(this.getEnableWrapperStyle());
                        dc.setEnableWrapperStyle(ews);
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                } else{ //there is an EnableWrapperStyle, reset it
                    try{
                        model.startTransaction();
                        ews.setEnabled(this.getEnableWrapperStyle());
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                }
            } else{  //there is no extensibility element, add a new one and add a new
                //wrapper style element
                DefinitionsCustomization dc = factory.createDefinitionsCustomization(model);
                EnableWrapperStyle ews = factory.createEnableWrapperStyle(model);
                try{
                    model.startTransaction();
                    ews.setEnabled(this.getEnableWrapperStyle());
                    dc.setEnableWrapperStyle(ews);
                    definitions.addExtensibilityElement(dc);
                    wsdlDirty = true;
                } finally{
                    model.endTransaction();
                }
            }
        } else if(jComponent == enableAsyncMappingCB){  //process Async Mapping
            if(ee.size() == 1){ //there is an extensibility element
                DefinitionsCustomization dc = ee.get(0);
                EnableAsyncMapping eam = dc.getEnableAsyncMapping();
                if(eam == null){ //there is no EnableAsyncMapping, create one
                    try{
                        model.startTransaction();
                        eam = factory.createEnableAsyncMapping(model);
                        eam.setEnabled(this.getEnableAsyncMapping());
                        dc.setEnableAsyncMapping(eam);
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                } else{ //there is an EnableAsyncMapping, reset it
                    try{
                        model.startTransaction();
                        eam.setEnabled(this.getEnableAsyncMapping());
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                }
            } else{  //there is no extensibility element, add a new one and add a new
                //async mapping element
                DefinitionsCustomization dc = factory.createDefinitionsCustomization(model);
                EnableAsyncMapping eam = factory.createEnableAsyncMapping(model);
                try{
                    model.startTransaction();
                    eam.setEnabled(this.getEnableAsyncMapping());
                    dc.setEnableAsyncMapping(eam);
                    definitions.addExtensibilityElement(dc);
                    wsdlDirty = true;
                } finally{
                    model.endTransaction();
                }
            }
        } else if(jComponent == enableMIMEContentCB){  //process MIME content
            if(ee.size() == 1){ //there is an extensibility element
                DefinitionsCustomization dc = ee.get(0);
                EnableMIMEContent emc = dc.getEnableMIMEContent();
                if(emc == null){ //there is no EnableMIMEContent, create one
                    try{
                        model.startTransaction();
                        emc = factory.createEnableMIMEContent(model);
                        emc.setEnabled(this.getEnableMIMEContent());
                        dc.setEnableMIMEContent(emc);
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                } else{ //there is an EnableMIMEContent, reset it
                    try{
                        model.startTransaction();
                        emc.setEnabled(this.getEnableMIMEContent());
                        wsdlDirty = true;
                    } finally{
                        model.endTransaction();
                    }
                }
            } else{  //there is no extensibility element, add a new one and add a new
                //MIME content element
                DefinitionsCustomization dc = factory.createDefinitionsCustomization(model);
                EnableMIMEContent emc = factory.createEnableMIMEContent(model);
                try{
                    model.startTransaction();
                    emc.setEnabled(this.getEnableMIMEContent());
                    dc.setEnableMIMEContent(emc);
                    definitions.addExtensibilityElement(dc);
                    
                    wsdlDirty = true;
                } finally{
                    model.endTransaction();
                }
            }
        }
    }
    
    @Override
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == packageNameText){
            if(!JavaUtilities.isValidPackageName(val)){
                getSectionView().getErrorPanel().
                        setError(new Error(Error.TYPE_FATAL,
                        Error.ERROR_MESSAGE, val, comp));
                return;
            }
        }
        getSectionView().getErrorPanel().clearError();
    }
    
    public boolean wsdlIsDirty() {
        return wsdlDirty;
    }
    
    public void save() {
        if(wsdlDirty){
            this.setModelDirty(model);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        ewsButtonGroup = new javax.swing.ButtonGroup();
        eamButtonGroup = new javax.swing.ButtonGroup();
        emcButtonGroup = new javax.swing.ButtonGroup();
        packageLabel = new javax.swing.JLabel();
        packageNameText = new javax.swing.JTextField();
        enableWrapperStyleCB = new javax.swing.JCheckBox();
        enableAsyncMappingCB = new javax.swing.JCheckBox();
        enableMIMEContentCB = new javax.swing.JCheckBox();
        defaultPackageCB = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        packageLabel.setText(bundle.getString("LBL_PACKAGE_NAME")); // NOI18N

        enableWrapperStyleCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_WRAPPER_STYLE").charAt(0));
        enableWrapperStyleCB.setText(bundle.getString("LBL_ENABLE_WRAPPER_STYLE")); // NOI18N
        enableWrapperStyleCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableWrapperStyleCB.setContentAreaFilled(false);
        enableWrapperStyleCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        enableAsyncMappingCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_ASYNC_CLIENT").charAt(0));
        enableAsyncMappingCB.setText(bundle.getString("LBL_ENABLE_ASYNC_MAPPING")); // NOI18N
        enableAsyncMappingCB.setActionCommand(bundle.getString("LBL_ENABLE_ASYNC_MAPPING")); // NOI18N
        enableAsyncMappingCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableAsyncMappingCB.setContentAreaFilled(false);
        enableAsyncMappingCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        enableMIMEContentCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_MIME_CONTENT").charAt(0));
        enableMIMEContentCB.setText(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.setActionCommand(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableMIMEContentCB.setContentAreaFilled(false);
        enableMIMEContentCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        defaultPackageCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultPackageCB.setText(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultPackageCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultPackageCB.setContentAreaFilled(false);
        defaultPackageCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(packageLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(packageNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 173, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(26, 26, 26)
                        .add(defaultPackageCB))
                    .add(enableWrapperStyleCB)
                    .add(enableMIMEContentCB)
                    .add(enableAsyncMappingCB))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(packageLabel)
                    .add(packageNameText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(defaultPackageCB))
                .add(20, 20, 20)
                .add(enableWrapperStyleCB)
                .add(19, 19, 19)
                .add(enableMIMEContentCB)
                .add(19, 19, 19)
                .add(enableAsyncMappingCB)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        packageLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_PACKAGE_NAME")); // NOI18N
        enableWrapperStyleCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_WRAPPER_STYLE")); // NOI18N
        enableWrapperStyleCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_WRAPPER_STYLE")); // NOI18N
        enableAsyncMappingCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_ASYNC_MAPPING")); // NOI18N
        enableAsyncMappingCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_ASYNC_MAPPING")); // NOI18N
        enableMIMEContentCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        defaultPackageCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultPackageCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultPackageCB;
    private javax.swing.ButtonGroup eamButtonGroup;
    private javax.swing.ButtonGroup emcButtonGroup;
    private javax.swing.JCheckBox enableAsyncMappingCB;
    private javax.swing.JCheckBox enableMIMEContentCB;
    private javax.swing.JCheckBox enableWrapperStyleCB;
    private javax.swing.ButtonGroup ewsButtonGroup;
    private javax.swing.JLabel packageLabel;
    private javax.swing.JTextField packageNameText;
    // End of variables declaration//GEN-END:variables
    
}
