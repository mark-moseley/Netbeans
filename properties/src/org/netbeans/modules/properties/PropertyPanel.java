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


package org.netbeans.modules.properties;


import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * Panel for customizing <code>Element.ItemElem</code> element.
 *
 * @author  Peter Zavadsky
 * @see Element.ItemElem
 */
public class PropertyPanel extends JPanel {

    /** Element to customize. */
    private Element.ItemElem element;


    /** Creates new <code>PropertyPanel</code>.
     * @param element element to customize */
    public PropertyPanel(Element.ItemElem element) {
        this.element = element;
        
        initComponents();
        initAccessibility();             
                
        keyText.setText(element.getKey());
        valueText.setText(element.getValue());
        commentText.setText(element.getComment());

        // Unregister Enter on text fields so default button could work.
        keyText.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        valueText.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        commentText.getKeymap().removeKeyStrokeBinding(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
        
        HelpCtx.setHelpIDString(this, Util.HELP_ID_ADDING);
    }

    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
        
        keyLabel.setLabelFor(keyText);
        valueLabel.setLabelFor(valueText);
        commentLabel.setLabelFor(commentText);
        
        keyText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
        valueText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
        commentText.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(PropertyPanel.class).getString("ACS_PropertyPanel"));                
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        keyLabel = new javax.swing.JLabel();
        keyText = new JTextField(25);
        valueLabel = new javax.swing.JLabel();
        valueText = new JTextField(25);
        commentLabel = new javax.swing.JLabel();
        commentText = new JTextField(25);

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(keyLabel, NbBundle.getBundle(PropertyPanel.class).getString("LBL_KeyLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(keyLabel, gridBagConstraints);

        keyText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyTextActionPerformed(evt);
            }
        });
        keyText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                keyTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 7, 0, 11);
        add(keyText, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(valueLabel, NbBundle.getBundle(PropertyPanel.class).getString("LBL_ValueLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(valueLabel, gridBagConstraints);

        valueText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valueTextActionPerformed(evt);
            }
        });
        valueText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                valueTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 0, 11);
        add(valueText, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(commentLabel, NbBundle.getBundle(PropertyPanel.class).getString("LBL_CommentLabel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 11, 0);
        add(commentLabel, gridBagConstraints);

        commentText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commentTextActionPerformed(evt);
            }
        });
        commentText.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                commentTextFocusLost(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 7, 11, 11);
        add(commentText, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void valueTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_valueTextFocusLost
        valueTextHandler();
    }//GEN-LAST:event_valueTextFocusLost

    private void workaround11364(ActionEvent evt) {
        JRootPane root = getRootPane();
        if (root != null) {
            JButton defaultButton = root.getDefaultButton();
            if (defaultButton != null) {
                defaultButton.doClick();
            }
        }
    }

    private void valueTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueTextActionPerformed
        valueTextHandler();
        workaround11364(evt);
    }//GEN-LAST:event_valueTextActionPerformed

    
    /** Value text field event handler. */
    private void valueTextHandler() {
        element.getValueElem().setValue(valueText.getText());
    }
    
    private void keyTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_keyTextFocusLost
        keyTextHandler();
    }//GEN-LAST:event_keyTextFocusLost

    private void keyTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyTextActionPerformed
        keyTextHandler();
        workaround11364(evt);
    }//GEN-LAST:event_keyTextActionPerformed

    /** Key text field event handler. */
    private void keyTextHandler() {
        element.getKeyElem().setValue(keyText.getText());
    }
    
    private void commentTextFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_commentTextFocusLost
        commentTextHandler();
    }//GEN-LAST:event_commentTextFocusLost

    private void commentTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commentTextActionPerformed
        commentTextHandler();
        workaround11364(evt);
    }//GEN-LAST:event_commentTextActionPerformed

    /** Comment text field event handler. */
    private void commentTextHandler() {
        element.getCommentElem().setValue(commentText.getText());
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel commentLabel;
    private javax.swing.JTextField commentText;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JTextField keyText;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JTextField valueText;
    // End of variables declaration//GEN-END:variables

}
