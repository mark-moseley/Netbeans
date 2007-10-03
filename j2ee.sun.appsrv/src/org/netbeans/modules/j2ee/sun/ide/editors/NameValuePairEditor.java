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
 * NameValuePairEditor.java
 *
 * Created on January 16, 2002, 6:25 PM
 */

package org.netbeans.modules.j2ee.sun.ide.editors;

import java.util.ResourceBundle;
import org.netbeans.modules.j2ee.sun.ide.editors.ui.DDTableModelEditor;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

/**
 *
 * @author  shirleyc
 */
public class NameValuePairEditor extends javax.swing.JPanel implements DDTableModelEditor {

    static final ResourceBundle bundle =
        ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/editors/Bundle");
    
    /** Creates new form NameValuePairEditor */
    public NameValuePairEditor() {
        initComponents();
        getAccessibleContext().setAccessibleName(bundle.getString("LBL_NameValuePairEditorName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(bundle.getString("LBL_NameValuePairEditorDescription")); // NOI18N
//        org.openide.util.HelpCtx.setHelpIDString(this, "AS_RTT_NameValueEditor"); // NOI18N
        
        nameField.getAccessibleContext().setAccessibleDescription(bundle.getString("colHdrParamName")); // NOI18N
        valueField.getAccessibleContext().setAccessibleDescription(bundle.getString("colHdrParamValue")); // NOI18N
        //descField.getAccessibleContext().setAccessibleDescription(bundle.getString("colHdrParamDescription")); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {/*GEN-BEGIN:initComponents*/
        java.awt.GridBagConstraints gridBagConstraints;

        iLabel1 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        //iLabel2 = new javax.swing.JLabel();
        //descField = new javax.swing.JTextField();
        iLabel3 = new javax.swing.JLabel();
        valueField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        iLabel1.setForeground(java.awt.Color.black);
        iLabel1.setText(bundle.getString("colHdrParamName")); // NOI18N
        iLabel1.setLabelFor(nameField);
        iLabel1.setDisplayedMnemonic(bundle.getString("colHdrParamName_Mnemonic").charAt(0)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(iLabel1, gridBagConstraints);

        nameField.setColumns(40);
        nameField.setText("jTextField1"); // NOI18N
        nameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(nameField, gridBagConstraints);

        /*iLabel2.setForeground(java.awt.Color.black);
        iLabel2.setText(bundle.getString("colHdrParamDescription")); // NOI18N
        iLabel2.setLabelFor(descField);
        iLabel2.setDisplayedMnemonic(bundle.getString("colHdrParamDescription_Mnemonic").charAt(0)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(iLabel2, gridBagConstraints);

        descField.setColumns(40);
        descField.setText("jTextField2"); // NOI18N
        descField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;        
        add(descField, gridBagConstraints);        
        */
        
        iLabel3.setForeground(java.awt.Color.black);
        iLabel3.setText(bundle.getString("colHdrParamValue")); // NOI18N
        iLabel3.setLabelFor(valueField);
        iLabel3.setDisplayedMnemonic(bundle.getString("colHdrParamValue_Mnemonic").charAt(0)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(iLabel3, gridBagConstraints);

        valueField.setColumns(40);
        valueField.setText("jTextField3");// NOI18N
        valueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueFieldActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        //gridBagConstraints.gridy = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(valueField, gridBagConstraints);

    }/*GEN-END:initComponents*/

    private void valueFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueFieldActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_valueFieldActionPerformed

    private void nameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameFieldActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_nameFieldActionPerformed

    /*private void descFieldActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
    }  */  
    
    public javax.swing.JPanel getPanel() {
        return this;
    }   
    
    public java.lang.Object getValue() {
        NameValuePair retVal = new NameValuePair();
        retVal.setParamName((String)nameField.getText());
        //retVal.setParamDescription((String)descField.getText());
        retVal.setParamValue((String)valueField.getText());
        return retVal;
    }    

    public void setValue(java.lang.Object obj) {
        NameValuePair inVal;
        try {
            inVal = (NameValuePair) obj;
            nameField.setText(inVal.getParamName());
            //descField.setText(inVal.getParamDescription());
            valueField.setText(inVal.getParamValue());
        }catch (ClassCastException cce) {
        }
    }
   
    //private javax.swing.JTextField descField;
    private javax.swing.JLabel iLabel3;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField nameField;
    private javax.swing.JTextField valueField;
    private javax.swing.JLabel iLabel2;
    private javax.swing.JLabel iLabel1;
    // End of variables declaration//GEN-END:variables

}
