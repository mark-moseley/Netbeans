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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.vmd.componentssupport.ui.wizard;

import java.awt.Dialog;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.vmd.componentssupport.ui.helpers.CustomComponentHelper;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;

/**
 *
 * @author  den
 */
public class DescriptorsVisualPanel extends javax.swing.JPanel {

    private static final String CONTENT_NUMBERED  = "WizardPanel_contentNumbered";  // NOI18N
    private static final String CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"; // NOI18N
    private static final String AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle";  // NOI18N
    
    /** Creates new form DescriptorsVisualPanel */
    public DescriptorsVisualPanel() {
        initComponents();

        myCompDescrList.setModel( new CompDescriptorsListModel() );
    }

    void readData( WizardDescriptor settings ) {
        myWizardDescriptor = settings;

        CompDescriptorsListModel model = 
                (CompDescriptorsListModel)myCompDescrList.getModel();
        List<Map<String, Object>> components 
                = (List<Map<String, Object>>)myWizardDescriptor.getProperty(
                        CustomComponentWizardIterator.CUSTOM_COMPONENTS);
        model.updateModel(components);
    }

    
    void storeData( WizardDescriptor settings ) {
        /*
         * nothing to save. 
         */
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        myCompDescrLabel = new javax.swing.JLabel();
        myAddButton = new javax.swing.JButton();
        myRemoveButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        myCompDescrList = new javax.swing.JList();

        myCompDescrLabel.setLabelFor(myCompDescrList);
        org.openide.awt.Mnemonics.setLocalizedText(myCompDescrLabel, org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "LBL_AddedComponentDescriptors")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(myAddButton, org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "BTN_AddComponentDescr")); // NOI18N
        myAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPressed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(myRemoveButton, org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "BTN_RemoveComponentDescr")); // NOI18N
        myRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePressed(evt);
            }
        });

        jScrollPane1.setViewportView(myCompDescrList);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(myCompDescrLabel)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(myAddButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(myRemoveButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(myCompDescrLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(myAddButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(myRemoveButton)
                        .addContainerGap(228, Short.MAX_VALUE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)))
        );

        myCompDescrLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "ACSN_AddedComponentDescriptors")); // NOI18N
        myCompDescrLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "ACSD_AddedComponentDescriptors")); // NOI18N
        myAddButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "ACSN_AddComponentDescr")); // NOI18N
        myAddButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "ACSD_AddComponentDescr")); // NOI18N
        myRemoveButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "ACSN_RemoveComponentDescr")); // NOI18N
        myRemoveButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DescriptorsVisualPanel.class, "ACSD_RemoveComponentDescr")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void addPressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPressed
    WizardDescriptor.Iterator iterator = new NewComponentDescriptor(myWizardDescriptor);
    myInnerDescriptor = new WizardDescriptor( iterator );
    myInnerDescriptor.putProperty( AUTO_WIZARD_STYLE, true );
    myInnerDescriptor.putProperty( CONTENT_DISPLAYED, true );
    myInnerDescriptor.putProperty( CONTENT_NUMBERED, true );
    Dialog dialog = DialogDisplayer.getDefault().createDialog( myInnerDescriptor );
    dialog.setVisible( true );
    readData(myWizardDescriptor);

}//GEN-LAST:event_addPressed

private void removePressed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePressed
    int index = myCompDescrList.getSelectedIndex();
    // remove in UI
    ((CompDescriptorsListModel)myCompDescrList.getModel()).remove(index);
    
    //remove from WizardDescriptor
    List<Map> components = (List<Map>)myWizardDescriptor.getProperty( 
                CustomComponentWizardIterator.CUSTOM_COMPONENTS);
    components.remove(index);
}//GEN-LAST:event_removePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton myAddButton;
    private javax.swing.JLabel myCompDescrLabel;
    private javax.swing.JList myCompDescrList;
    private javax.swing.JButton myRemoveButton;
    // End of variables declaration//GEN-END:variables

    
    private class CompDescriptorsListModel extends EditableListModel{

        public void updateModel(List<Map<String,Object>> components){
            if (components == null){
                return; 
            }
            
            // clean
            removeAllElements();
            
            for (Map<String, Object> component : components){
                String prefix = (String)component.get(
                        NewComponentDescriptor.CC_PREFIX);
                String typeID = (String)component.get(
                        NewComponentDescriptor.CD_TYPE_ID);
                
                assert prefix != null && typeID != null
                        : "Component data is not consistent";
                
                addElement(prefix + " [ " + typeID + " ]"); // NOI18N 
            }
        }
    }
    
    private WizardDescriptor myWizardDescriptor;
    private WizardDescriptor myInnerDescriptor;
}
