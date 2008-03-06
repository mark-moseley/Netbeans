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
 * BindingOperationPanel.java
 *
 * Created on February 19, 2006, 8:48 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.websvc.customization.model.BindingCustomization;
import org.netbeans.modules.websvc.customization.model.BindingOperationCustomization;
import org.netbeans.modules.websvc.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.customization.model.EnableMIMEContent;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author  Roderico Cruz
 */
public class BindingOperationPanel extends SaveableSectionInnerPanel  {
    private BindingOperation bindingOperation;
    private WSDLModel model;
    private boolean wsdlDirty;
    private ModelChangeListener modelListener;
    private ModelChangeListener primaryModelListener;
    private Definitions primaryDefinitions;
    private WSDLModel primaryModel;
    
    /** Creates new form BindingOperationPanel */
    public BindingOperationPanel(SectionView view,
            BindingOperation bindingOperation, Definitions primaryDefinitions){
        super(view);
        this.bindingOperation = bindingOperation;
        this.primaryDefinitions = primaryDefinitions;
        this.model = this.bindingOperation.getModel();
        this.primaryModel = this.primaryDefinitions.getModel();
        initComponents();
        enableMIMEContentCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        bindingLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        bindingName.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        bindingName.setText(getParentOfBindingOperation(bindingOperation));
        
        sync();
        
        addModifier(enableMIMEContentCB);
        
        modelListener = new ModelChangeListener();
        PropertyChangeListener pcl = WeakListeners.propertyChange(modelListener, model);
        model.addPropertyChangeListener(pcl);
        
        if(primaryModel != model){
            primaryModelListener = new ModelChangeListener();
            PropertyChangeListener l = WeakListeners.propertyChange(primaryModelListener, primaryModel);
            primaryModel.addPropertyChangeListener(l);
        }
    }
    
    private String getParentOfBindingOperation(BindingOperation op){
        Binding binding = (Binding)op.getParent();
        return binding.getName();
    }
    
    class ModelChangeListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof EnableMIMEContent){
                EnableMIMEContent emc = (EnableMIMEContent)source;
                WSDLComponent parent = emc.getParent();
                if(parent instanceof DefinitionsCustomization ||
                        parent instanceof BindingCustomization){
                    sync();
                }
            }
        }
    }
    
    private void sync(){
        List<BindingOperationCustomization> ee =
                bindingOperation.getExtensibilityElements(BindingOperationCustomization.class);
        if(ee.size() == 1){
            BindingOperationCustomization boc = ee.get(0);
            EnableMIMEContent emc = boc.getEnableMIMEContent();
            if(emc != null){
                setEnableMIMEContent(emc.isEnabled());
            } else{
                setEnableMIMEContent(getMIMEContentOfParent());
            }
        } else{
            setEnableMIMEContent(getMIMEContentOfParent());
        }
    }
    
    private boolean getMIMEContentOfParent(){
        boolean isMIMEContent = false;
        Binding binding = (Binding)bindingOperation.getParent();
        List<BindingCustomization> bcs = binding.getExtensibilityElements(BindingCustomization.class);
        if(bcs.size() > 0) {  //there is a BindingCustomization
            BindingCustomization bc = bcs.get(0);
            EnableMIMEContent mimeContent = bc.getEnableMIMEContent();
            if(mimeContent != null){ //there is a mime content
                isMIMEContent =  mimeContent.isEnabled();
            }else{
                isMIMEContent = getMIMEContentFromDefinitions(primaryDefinitions);
            }
        } else{ //there is no BindingCustomization, look in Definitions
            isMIMEContent = getMIMEContentFromDefinitions(primaryDefinitions);
        }
        return isMIMEContent;
    }
    
    private boolean getMIMEContentFromDefinitions(Definitions definitions){
        List<DefinitionsCustomization> dcs = definitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0){
            DefinitionsCustomization dc = dcs.get(0);
            EnableMIMEContent mimeContent = dc.getEnableMIMEContent();
            if(mimeContent != null){
                return mimeContent.isEnabled();
            }
        }
        return false;
    }
    
    public void setEnableMIMEContent(boolean enable){
        enableMIMEContentCB.setSelected(enable);
    }
    
    public Boolean getEnableMIMEContent(){
        return enableMIMEContentCB.isSelected();
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <BindingOperationCustomization> ee =
                bindingOperation.getExtensibilityElements(BindingOperationCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == enableMIMEContentCB){
            if(ee.size() > 0){ //there is an extensibility element
                BindingOperationCustomization boc = ee.get(0);
                EnableMIMEContent emc = boc.getEnableMIMEContent();
                if(emc == null){ //there is no EnableMIMEContent, create one
                    try{
                        model.startTransaction();
                        emc = factory.createEnableMIMEContent(model);
                        emc.setEnabled(this.getEnableMIMEContent());
                        boc.setEnableMIMEContent(emc);
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
                BindingOperationCustomization boc = factory.createBindingOperationCustomization(model);
                EnableMIMEContent emc = factory.createEnableMIMEContent(model);
                try{
                    model.startTransaction();
                    emc.setEnabled(this.getEnableMIMEContent());
                    boc.setEnableMIMEContent(emc);
                    bindingOperation.addExtensibilityElement(boc);
                    wsdlDirty = true;
                } finally{
                        model.endTransaction();
                }
            }
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

        emcButtonGroup = new javax.swing.ButtonGroup();
        enableMIMEContentCB = new javax.swing.JCheckBox();
        bindingLabel = new javax.swing.JLabel();
        bindingName = new javax.swing.JLabel();

        enableMIMEContentCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_MIME_CONTENT").charAt(0));
        enableMIMEContentCB.setText(org.openide.util.NbBundle.getBundle(BindingOperationPanel.class).getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        enableMIMEContentCB.setToolTipText(bundle.getString("TOOLTIP_ENABLE_MIME")); // NOI18N
        enableMIMEContentCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableMIMEContentCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        bindingLabel.setText(bundle.getString("LBL_ENCLOSING_BINDING")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(21, 21, 21)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(bindingLabel)
                        .add(22, 22, 22)
                        .add(bindingName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 139, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(enableMIMEContentCB))
                .addContainerGap(201, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(13, 13, 13)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(bindingLabel)
                    .add(bindingName))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 21, Short.MAX_VALUE)
                .add(enableMIMEContentCB)
                .addContainerGap())
        );

        enableMIMEContentCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        bindingLabel.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENCLOSING_BINDING")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bindingLabel;
    private javax.swing.JLabel bindingName;
    private javax.swing.ButtonGroup emcButtonGroup;
    private javax.swing.JCheckBox enableMIMEContentCB;
    // End of variables declaration//GEN-END:variables
    
}
