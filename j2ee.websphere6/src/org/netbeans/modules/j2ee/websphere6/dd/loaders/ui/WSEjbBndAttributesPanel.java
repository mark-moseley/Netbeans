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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbbnd.*;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSEjbBnd;
import org.openide.util.NbBundle;
/**
 *
 * @author  dlm198383
 */
public class WSEjbBndAttributesPanel extends SectionInnerPanel implements java.awt.event.ItemListener{
    /** Creates new form WSEjbExtAttributesPanel */
    WSEjbBnd ejbbnd;
    WSMultiViewDataObject dObj;
    
    private javax.swing.JTextField cmpConnectionFactoryNameField;
    private javax.swing.JTextField cmpConnectionFactoryJndiNameField;
    private javax.swing.JCheckBox  cmpConnectionFactoryCheckBox;
    private javax.swing.JComboBox  cmpConnectionFactoryAuthType;
    private static final String FACTORY_NAME="Default CMP Connection Factory Name"; //NOI18N
    private static final String FACTORY_JNDI_NAME="Default CMP Connection Factory JNDI Name"; //NOI18N
    
    
    /** Creates new form WSEjbBndAttributesPanel */
    public WSEjbBndAttributesPanel(SectionView view,  WSMultiViewDataObject dObj,WSEjbBnd ejbbnd) {
        super(view);
        this.dObj=dObj;
        this.ejbbnd=ejbbnd;
        initComponents();
        bindCmpConnectionFactoryComponents();
        initCmpConnectionFactoryComponents();
        
        nameField.setText(ejbbnd.getXmiId());
        hrefField.setText(ejbbnd.getEjbJarHref());
        currentBackendIdField.setText(ejbbnd.getCurrentBackendId());
        addModifier(nameField);
        addModifier(hrefField);
        addModifier(currentBackendIdField);
    }
    private void bindCmpConnectionFactoryComponents() {
        CMPConnectionFactory panel=(CMPConnectionFactory)cmpConnectionFactoryPanel;
        cmpConnectionFactoryNameField=panel.getNameField();
        cmpConnectionFactoryJndiNameField=panel.getJndiNameField();
        cmpConnectionFactoryCheckBox=panel.getFactoryCheckBox();
        cmpConnectionFactoryAuthType=panel.getAuthTypeComboBox();
        panel.setComponentsBackground(SectionVisualTheme.getSectionActiveBackgroundColor());
        panel.setEnabledComponents();
    }
    
    private void initCmpConnectionFactoryComponents(){
        addModifier(cmpConnectionFactoryNameField);
        addModifier(cmpConnectionFactoryJndiNameField);
        addValidatee(cmpConnectionFactoryNameField);
        addValidatee(cmpConnectionFactoryJndiNameField);
        
        boolean factoryEnabled=(ejbbnd.getCmpConnectionFactory()==null)?false:true;
        cmpConnectionFactoryCheckBox.setSelected(factoryEnabled);
        
        if(factoryEnabled) {
            
            cmpConnectionFactoryNameField.setText(
                    ejbbnd.getCmpConnectionFactoryXmiId());
            
            cmpConnectionFactoryJndiNameField.setText(
                    ejbbnd.getCmpConnectionFactoryJndiName());
            
            cmpConnectionFactoryAuthType.setSelectedItem(
                    ejbbnd.getCmpConnectionFactoryResAuth());
            
        }
        cmpConnectionFactoryCheckBox.addItemListener(this);
        cmpConnectionFactoryAuthType.addItemListener(this);
        ((CMPConnectionFactory)cmpConnectionFactoryPanel).setEnabledComponents();
    }
    public void setValue(javax.swing.JComponent source,Object value) {
        if (source==nameField) {
            ejbbnd.setXmiId((String)value);
        } else if (source==hrefField) {
            ejbbnd.setEjbJarHref((String)value);
        } else if (source==currentBackendIdField) {
            ejbbnd.setCurrentBackendId((String)value);
        } else if(source==cmpConnectionFactoryNameField) {
            ejbbnd.setCmpConnectionFactoryXmiId((String)value);
        } else if(source==cmpConnectionFactoryJndiNameField) {
            ejbbnd.setCmpConnectionFactoryJndiName((String)value);
        }
        
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (nameField==source) {
            nameField.setText(ejbbnd.getXmiId());
        }
        if (hrefField==source) {
            hrefField.setText(ejbbnd.getEjbJarHref());
        }
        if (currentBackendIdField==source) {
            currentBackendIdField.setText(ejbbnd.getCurrentBackendId());
        }
        if (cmpConnectionFactoryNameField==source) {
            cmpConnectionFactoryNameField.setText(ejbbnd.getCmpConnectionFactoryXmiId());
        }
        if (cmpConnectionFactoryJndiNameField==source) {
            cmpConnectionFactoryNameField.setText(ejbbnd.getCmpConnectionFactoryJndiName());
        }
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("Name".equals(errorId)) return nameField;
        if ("Ejb-Jar ID".equals(errorId)) return hrefField;
        if("Current Backend ID".equals(errorId)) return currentBackendIdField;
        return null;
    }
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==nameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==hrefField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Ejb-Jar ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==currentBackendIdField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Current Backend ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==cmpConnectionFactoryNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, FACTORY_NAME, comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==cmpConnectionFactoryJndiNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, FACTORY_JNDI_NAME, comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    private void changeCmpConnectionFactoryState() {
        if(cmpConnectionFactoryCheckBox.isSelected()) {
            ejbbnd.setCmpConnectionFactory("");
            ejbbnd.setCmpConnectionFactoryXmiId(cmpConnectionFactoryNameField.getText());
            ejbbnd.setCmpConnectionFactoryResAuth(
                    cmpConnectionFactoryAuthType.getSelectedItem().toString());
        } else {
            ejbbnd.setCmpConnectionFactory(null);
        }
        ((CMPConnectionFactory)cmpConnectionFactoryPanel).setEnabledComponents();
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        dObj.setChangedFromUI(true);
        changeCmpConnectionFactoryState();
        // TODO add your handling code here:
        dObj.modelUpdatedFromUI();
        //dObj.setChangedFromUI(true);
        dObj.setChangedFromUI(false);
    }
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
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
        nameField = new javax.swing.JTextField();
        currentBackendIdLabel = new javax.swing.JLabel();
        currentBackendIdField = new javax.swing.JTextField();
        rootId = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();
        cmpConnectionFactoryPanel = new CMPConnectionFactory();
        ((CMPConnectionFactory)cmpConnectionFactoryPanel).getFactoryCheckBox().setText(NbBundle.getMessage(CMPConnectionFactory.class,"LBL_DefaultCMPConnectionFactory"));

        nameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Name"));

        currentBackendIdLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_CurrentBackendId"));

        rootId.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_EjbJarId"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(58, 58, 58)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(rootId)
                            .add(nameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(hrefField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)
                            .add(nameField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 345, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(currentBackendIdLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(currentBackendIdField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(cmpConnectionFactoryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)))
                .addContainerGap())
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
                    .add(rootId, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(currentBackendIdLabel)
                    .add(currentBackendIdField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(cmpConnectionFactoryPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cmpConnectionFactoryPanel;
    private javax.swing.JTextField currentBackendIdField;
    private javax.swing.JLabel currentBackendIdLabel;
    private javax.swing.JTextField hrefField;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel rootId;
    // End of variables declaration//GEN-END:variables
    
}
