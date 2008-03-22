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
 * PortTypePanel.java
 *
 * Created on February 19, 2006, 8:39 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.websvc.core.JaxWsUtils;
import org.netbeans.modules.websvc.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.customization.model.JavaClass;
import org.netbeans.modules.websvc.customization.model.PortTypeCustomization;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.util.WeakListeners;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author  Roderico Cruz
 */
public class PortTypePanel extends SaveableSectionInnerPanel {
    private PortType portType;
    private WSDLModel model;
    private boolean wsdlDirty;
    private DefaultItemListener defaultItemListener;
    private ModelChangeListener modelListener;
    private PortTypeActionListener listener;
    private Definitions primaryDefinitions;
    
    /** Creates new form PortTypePanel */
    public PortTypePanel(SectionView view, PortType portType,
            Node node, Definitions primaryDefinitions) {
        super(view);
        this.portType = portType;
        this.primaryDefinitions = primaryDefinitions;
        this.model = this.portType.getModel();
        initComponents();
        if(!isClient(node)){
            enableAsyncMappingCB.setVisible(false);
        }
        enableAsyncMappingCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_ASYNC"));
        enableWrapperStyleCB.setToolTipText(NbBundle.getMessage(DefinitionsPanel.class, "TOOLTIP_ENABLE_WRAPPER"));
        javaClassText.setToolTipText(NbBundle.getMessage(PortTypePanel.class, "TOOLTIP_PORTTYPE_CLASS"));
        
        syncButtons();
        syncJavaClass();
        
        defaultItemListener = new DefaultItemListener();
        ItemListener il = (ItemListener)WeakListeners.create(ItemListener.class, defaultItemListener, defaultJavaClassCB);
        defaultJavaClassCB.addItemListener(il);
        
        modelListener = new ModelChangeListener();
        WSDLModel primaryModel = primaryDefinitions.getModel();
        PropertyChangeListener pcl = WeakListeners.propertyChange(modelListener, primaryModel);
        primaryModel.addPropertyChangeListener(pcl);
        
        listener = new PortTypeActionListener();
        ActionListener eamListener = (ActionListener)WeakListeners.create(ActionListener.class,
                listener, enableAsyncMappingCB);
        enableAsyncMappingCB.addActionListener(eamListener);
        ActionListener ewsListener = (ActionListener)WeakListeners.create(ActionListener.class,
                listener, enableWrapperStyleCB);
        enableWrapperStyleCB.addActionListener(ewsListener);
        
        addModifier(javaClassText);
        addModifier(defaultJavaClassCB);
        addValidatee(javaClassText);
    }
    
    class PortTypeActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            setValue((JComponent)e.getSource(), null);
        }
    }
    
    class DefaultItemListener implements ItemListener{
        public void itemStateChanged(ItemEvent e) {
            //System.out.println("state changed in default java class");
            if(defaultJavaClassCB.isSelected()){
                javaClassText.setEnabled(false);
            } else{
                javaClassText.setEnabled(true);
                javaClassText.requestFocus();
            }
        }
        
    }
    
    class ModelChangeListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof EnableWrapperStyle){
                EnableWrapperStyle ews = (EnableWrapperStyle)source;
                WSDLComponent parent = ews.getParent();
                if(parent instanceof DefinitionsCustomization){
                    syncButtons();
                }
            } else if (source instanceof EnableAsyncMapping){
                EnableAsyncMapping eam = (EnableAsyncMapping)source;
                WSDLComponent parent = eam.getParent();
                if(parent instanceof DefinitionsCustomization){
                    syncButtons();
                }
            }
        }
    }
    
    private void syncJavaClass(){
        List<PortTypeCustomization> ee = portType.getExtensibilityElements(PortTypeCustomization.class);
        if(ee.size() > 0) {
            PortTypeCustomization pc = ee.get(0);
            JavaClass jc = pc.getJavaClass();
            if(jc != null){
                setJavaClass(jc.getName());
            } else{
                this.defaultJavaClassCB.setSelected(true);
                javaClassText.setEnabled(false);
            }
        } else{
            this.defaultJavaClassCB.setSelected(true);
            javaClassText.setEnabled(false);
        }
    }
    
    private void syncButtons(){
        List<PortTypeCustomization> ee = portType.getExtensibilityElements(PortTypeCustomization.class);
        if(ee.size() > 0){
            PortTypeCustomization pc = ee.get(0);
            EnableAsyncMapping eam = pc.getEnableAsyncMapping();
            if(eam != null){
                setEnableAsyncMapping(eam.isEnabled());
            } else{
                //look up default value from Definitions setting
                setEnableAsyncMapping(this.getAsyncMappingOfParent());
            }
            EnableWrapperStyle ews = pc.getEnableWrapperStyle();
            if(ews != null){
                setEnableWrapperStyle(ews.isEnabled());
            } else{
                //look up default value from Definitions setting
                setEnableWrapperStyle(this.getWrapperStyleOfParent());
            }
            
            
        } else{ //set to the default values
            setEnableAsyncMapping(this.getAsyncMappingOfParent());
            setEnableWrapperStyle(this.getWrapperStyleOfParent());
            
        }
    }
    
    private boolean getAsyncMappingOfParent(){
        List<DefinitionsCustomization> dcs = primaryDefinitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0) {
            DefinitionsCustomization dc = dcs.get(0);
            EnableAsyncMapping asyncMapping = dc.getEnableAsyncMapping();
            if(asyncMapping != null){
                return asyncMapping.isEnabled();
            }
        }
        return false;
    }
    
    private boolean getWrapperStyleOfParent(){
        List<DefinitionsCustomization> dcs = primaryDefinitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0){
            DefinitionsCustomization dc = dcs.get(0);
            EnableWrapperStyle wrapperStyle = dc.getEnableWrapperStyle();
            if(wrapperStyle != null){
                return wrapperStyle.isEnabled();
            }
        }
        return true;
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
    
    public Boolean getEnableWrapperStyle(){
        return enableWrapperStyleCB.isSelected();
    }
    
    public String getJavaClass(){
        return javaClassText.getText();
    }
    public void setJavaClass(String name){
        javaClassText.setText(name);
    }
    
    public JComponent getErrorComponent(String string) {
        return new JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <PortTypeCustomization> ee =
                portType.getExtensibilityElements(PortTypeCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == javaClassText ||
                jComponent == defaultJavaClassCB){
            String text = javaClassText.getText();
            if(text != null && !text.trim().equals("")
            && !defaultJavaClassCB.isSelected()){
                if(!JaxWsUtils.isJavaIdentifier(text)){
                    return;
                }
                if(ee.size() == 1){  //there is existing extensibility element
                    PortTypeCustomization pc = ee.get(0);
                    JavaClass jc = pc.getJavaClass();
                    if(jc == null){
                        try{
                            jc = factory.createJavaClass(model);
                            model.startTransaction();
                            jc.setName(text); //TODO Need to validate this before setting it
                            pc.setJavaClass(jc);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    } else{ //javaclass already exists
                        //reset the JavaClass
                        try{
                            model.startTransaction();
                            jc.setName(text);
                            wsdlDirty = true;
                        } finally{
                                model.endTransaction();
                        }
                    }
                }else{  //there is no ExtensibilityElement
                    //create extensibility element and add JavaClass
                    PortTypeCustomization pc = factory.createPortTypeCustomization(model);
                    JavaClass jc = factory.createJavaClass(model);
                    try{
                        model.startTransaction();
                        jc.setName(text);
                        pc.setJavaClass(jc);
                        portType.addExtensibilityElement(pc);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{ //no JavaClass specified, use default
                try{
                    if(ee.size() == 1){
                        PortTypeCustomization pc = ee.get(0);
                        JavaClass jc = pc.getJavaClass();
                        if(jc != null){
                            model.startTransaction();
                            pc.removeJavaClass(jc);
                            if(pc.getChildren().size() == 0){
                                portType.removeExtensibilityElement(pc);
                            }
                            wsdlDirty = true;
                        }
                    }
                } finally{
                        model.endTransaction();
                }
            }
        } else if(jComponent == enableWrapperStyleCB){
            if(ee.size() == 1){ //there is an extensibility element
                PortTypeCustomization pc = ee.get(0);
                EnableWrapperStyle ews = pc.getEnableWrapperStyle();
                if(ews == null){ //there is no EnableWrapperStyle, create one
                    try{
                        model.startTransaction();
                        ews = factory.createEnableWrapperStyle(model);
                        ews.setEnabled(this.getEnableWrapperStyle());
                        pc.setEnableWrapperStyle(ews);
                        wsdlDirty = true;
                    }finally{
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
                PortTypeCustomization pc = factory.createPortTypeCustomization(model);
                EnableWrapperStyle ews = factory.createEnableWrapperStyle(model);
                try{
                    model.startTransaction();
                    ews.setEnabled(this.getEnableWrapperStyle());
                    pc.setEnableWrapperStyle(ews);
                    portType.addExtensibilityElement(pc);
                    wsdlDirty = true;
                } finally{
                        model.endTransaction();
                }
            }
        } else if(jComponent == this.enableAsyncMappingCB){
            if(ee.size() == 1){ //there is an extensibility element
                PortTypeCustomization pc = ee.get(0);
                EnableAsyncMapping eam = pc.getEnableAsyncMapping();
                if(eam == null){ //there is no EnableAsyncMapping, create one
                    try{
                        model.startTransaction();
                        eam = factory.createEnableAsyncMapping(model);
                        eam.setEnabled(this.getEnableAsyncMapping());
                        pc.setEnableAsyncMapping(eam);
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
                //enable asyncmapping element
                PortTypeCustomization pc =  factory.createPortTypeCustomization(model);
                EnableAsyncMapping eam = factory.createEnableAsyncMapping(model);
                try{
                    model.startTransaction();
                    eam.setEnabled(this.getEnableAsyncMapping());
                    pc.setEnableAsyncMapping(eam);
                    portType.addExtensibilityElement(pc);
                    wsdlDirty = true;
                } finally{
                        model.endTransaction();
                }
            }
        }
    }
    
    public void documentChanged(JTextComponent comp, String val) {
        if(comp == javaClassText){
            if(!JaxWsUtils.isJavaIdentifier(val)){
                getSectionView().getErrorPanel().
                        setError(new Error(Error.TYPE_FATAL,
                        Error.ERROR_MESSAGE, val, comp));
                return;
            }
        }
        getSectionView().getErrorPanel().clearError();
    }
    
    public void rollbackValue(JTextComponent source) {
        if(source == javaClassText){
            String className = "";
            List <PortTypeCustomization> ee =
                    portType.getExtensibilityElements(PortTypeCustomization.class);
            if(ee.size() == 1){
                PortTypeCustomization ptc = ee.get(0);
                JavaClass jc = ptc.getJavaClass();
                if(jc != null){
                    className = jc.getName();
                }
            }
            javaClassText.setText(className);
        }
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
        javaClassLabel = new javax.swing.JLabel();
        javaClassText = new javax.swing.JTextField();
        defaultJavaClassCB = new javax.swing.JCheckBox();
        enableWrapperStyleCB = new javax.swing.JCheckBox();
        enableAsyncMappingCB = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        javaClassLabel.setText(bundle.getString("LBL_JAVA_CLASS")); // NOI18N

        defaultJavaClassCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_USE_DEFAULT").charAt(0));
        defaultJavaClassCB.setText(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultJavaClassCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        defaultJavaClassCB.setContentAreaFilled(false);
        defaultJavaClassCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        enableWrapperStyleCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_WRAPPER_STYLE").charAt(0));
        enableWrapperStyleCB.setText(bundle.getString("LBL_ENABLE_WRAPPER_STYLE")); // NOI18N
        enableWrapperStyleCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableWrapperStyleCB.setContentAreaFilled(false);
        enableWrapperStyleCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        enableAsyncMappingCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_ASYNC_CLIENT").charAt(0));
        enableAsyncMappingCB.setText(bundle.getString("LBL_ENABLE_ASYNC_MAPPING")); // NOI18N
        enableAsyncMappingCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableAsyncMappingCB.setContentAreaFilled(false);
        enableAsyncMappingCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(enableAsyncMappingCB)
                    .add(enableWrapperStyleCB)
                    .add(layout.createSequentialGroup()
                        .add(javaClassLabel)
                        .add(16, 16, 16)
                        .add(javaClassText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 237, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(21, 21, 21)
                        .add(defaultJavaClassCB)))
                .addContainerGap(33, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(26, 26, 26)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(javaClassLabel)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(javaClassText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(defaultJavaClassCB)))
                .add(18, 18, 18)
                .add(enableWrapperStyleCB)
                .add(15, 15, 15)
                .add(enableAsyncMappingCB)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        javaClassLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_JAVA_CLASS")); // NOI18N
        javaClassText.getAccessibleContext().setAccessibleName(bundle.getString("LBL_JAVA_CLASS")); // NOI18N
        javaClassText.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_JAVA_CLASS")); // NOI18N
        defaultJavaClassCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        defaultJavaClassCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_USE_DEFAULT")); // NOI18N
        enableWrapperStyleCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_WRAPPER_STYLE")); // NOI18N
        enableWrapperStyleCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_WRAPPER_STYLE")); // NOI18N
        enableAsyncMappingCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_ASYNC_MAPPING")); // NOI18N
        enableAsyncMappingCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_ASYNC_MAPPING")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultJavaClassCB;
    private javax.swing.ButtonGroup eamButtonGroup;
    private javax.swing.JCheckBox enableAsyncMappingCB;
    private javax.swing.JCheckBox enableWrapperStyleCB;
    private javax.swing.ButtonGroup ewsButtonGroup;
    private javax.swing.JLabel javaClassLabel;
    private javax.swing.JTextField javaClassText;
    // End of variables declaration//GEN-END:variables
    
}
