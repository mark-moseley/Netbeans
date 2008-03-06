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
 * BindingPanel.java
 *
 * Created on February 19, 2006, 8:46 AM
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.modules.websvc.customization.model.BindingCustomization;
import org.netbeans.modules.websvc.customization.model.CustomizationComponentFactory;
import org.netbeans.modules.websvc.customization.model.DefinitionsCustomization;
import org.netbeans.modules.websvc.customization.model.EnableMIMEContent;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionVisualTheme;
import org.netbeans.modules.xml.wsdl.model.Binding;
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
public class BindingPanel extends SaveableSectionInnerPanel {
    private Binding binding;
    private WSDLModel model;
    private boolean wsdlDirty;
    private ModelChangeListener modelListener;
    private BindingActionListener actionListener;
    private Definitions primaryDefinitions;
    
    /** Creates new form BindingPanel */
    public BindingPanel(SectionView view, Binding binding, Definitions primaryDefinitions){
        super(view);
        this.binding = binding;
        this.primaryDefinitions = primaryDefinitions;
        this.model = this.binding.getModel();
        initComponents();
        this.enableMIMEContentCB.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        
        sync();
        
        modelListener = new ModelChangeListener();
        WSDLModel primaryModel = primaryDefinitions.getModel();
        PropertyChangeListener pcl = WeakListeners.propertyChange(modelListener, primaryModel);
        primaryModel.addPropertyChangeListener(pcl);
        
        actionListener = new BindingActionListener();
        ActionListener al = (ActionListener)WeakListeners.create(ActionListener.class, actionListener,
                enableMIMEContentCB);
        enableMIMEContentCB.addActionListener(al);
    }
    
    class ModelChangeListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof EnableMIMEContent){
                EnableMIMEContent emc = (EnableMIMEContent)source;
                WSDLComponent parent = emc.getParent();
                if(parent instanceof DefinitionsCustomization){
                    sync();
                }
            }
        }
    }
    
    class BindingActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            setValue((JComponent)e.getSource(), null);
        }
    }
    
    private boolean getMIMEContentOfParent(){
        List<DefinitionsCustomization> dcs = primaryDefinitions.getExtensibilityElements(DefinitionsCustomization.class);
        if(dcs.size() > 0) {
            DefinitionsCustomization dc = dcs.get(0);
            EnableMIMEContent mimeContent = dc.getEnableMIMEContent();
            if(mimeContent != null){
                return mimeContent.isEnabled();
            }
        }
        return false;
    }
    
    private void sync(){
        List<BindingCustomization> ee =
                binding.getExtensibilityElements(BindingCustomization.class);
        if(ee.size() == 1){
            BindingCustomization bc = ee.get(0);
            EnableMIMEContent emc = bc.getEnableMIMEContent();
            if(emc != null){
                setEnableMIMEContent(emc.isEnabled());
            } else{
                setEnableMIMEContent(getMIMEContentOfParent());
            }
        } else{
            setEnableMIMEContent(getMIMEContentOfParent());
        }
    }
    
    public void setEnableMIMEContent(boolean enable){
        enableMIMEContentCB.setSelected(enable);
    }
    
    public boolean getEnableMIMEContent(){
        return enableMIMEContentCB.isSelected();
    }
    
    public JComponent getErrorComponent(String string) {
        return new javax.swing.JButton("error");
    }
    
    public void linkButtonPressed(Object object, String string) {
    }
    
    public void setValue(JComponent jComponent, Object object) {
        List <BindingCustomization> ee =
                binding.getExtensibilityElements(BindingCustomization.class);
        CustomizationComponentFactory factory = CustomizationComponentFactory.getDefault();
        if(jComponent == enableMIMEContentCB){
            if(ee.size() > 0){ //there is an extensibility element
                BindingCustomization bc = ee.get(0);
                EnableMIMEContent emc = bc.getEnableMIMEContent();
                if(emc == null){ //there is no EnableMIMEContent, create one
                    try{
                        model.startTransaction();
                        emc = factory.createEnableMIMEContent(model);
                        emc.setEnabled(this.getEnableMIMEContent());
                        bc.setEnableMIMEContent(emc);
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                        
                    }
                } else{ //there is an EnableWrapperStyle, reset it
                    try{
                        model.startTransaction();
                        emc.setEnabled(this.getEnableMIMEContent());
                        wsdlDirty = true;
                    } finally{
                            model.endTransaction();
                    }
                }
            } else{  //there is no extensibility element, add a new one and add a new
                //wrapper style element
                BindingCustomization bc = factory.createBindingCustomization(model);
                EnableMIMEContent emc = factory.createEnableMIMEContent(model);
                try{
                    model.startTransaction();
                    emc.setEnabled(this.getEnableMIMEContent());
                    bc.setEnableMIMEContent(emc);
                    binding.addExtensibilityElement(bc);
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

        enableMIMEContentCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle").getString("MNEMONIC_ENABLE_MIME_CONTENT").charAt(0));
        enableMIMEContentCB.setText(org.openide.util.NbBundle.getBundle(BindingPanel.class).getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/customization/multiview/Bundle"); // NOI18N
        enableMIMEContentCB.setToolTipText(bundle.getString("TOOLTIP_ENABLE_MIME")); // NOI18N
        enableMIMEContentCB.setActionCommand(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        enableMIMEContentCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(enableMIMEContentCB)
                .addContainerGap(409, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(25, 25, 25)
                .add(enableMIMEContentCB)
                .addContainerGap(30, Short.MAX_VALUE))
        );

        enableMIMEContentCB.getAccessibleContext().setAccessibleName(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
        enableMIMEContentCB.getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_ENABLE_MIME_CONTENT")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup emcButtonGroup;
    private javax.swing.JCheckBox enableMIMEContentCB;
    // End of variables declaration//GEN-END:variables
    
}
