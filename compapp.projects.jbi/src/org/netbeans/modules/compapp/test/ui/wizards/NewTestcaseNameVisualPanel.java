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

package org.netbeans.modules.compapp.test.ui.wizards;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public class NewTestcaseNameVisualPanel extends JPanel implements DocumentListener {
    
    private NewTestcaseNameWizardPanel panel;
    
    /** Creates new form NewTestcaseNameVisualPanel */
    public NewTestcaseNameVisualPanel(NewTestcaseNameWizardPanel panel) {
        initComponents();
        this.panel = panel;
        // Register listener on the textFields to make the automatic updates
        mNameTf.getDocument().addDocumentListener(this);        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        mNameLbl = new javax.swing.JLabel();
        mNameTf = new javax.swing.JTextField();

        mNameLbl.setLabelFor(mNameTf);
        org.openide.awt.Mnemonics.setLocalizedText(mNameLbl, org.openide.util.NbBundle.getMessage(NewTestcaseNameVisualPanel.class, "LBL_Testcase_Name")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(mNameLbl)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mNameTf, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(mNameLbl)
                    .add(mNameTf, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(281, Short.MAX_VALUE))
        );

        mNameLbl.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewTestcaseNameVisualPanel.class, "ACS_TEST_CASE_NAME_LABEL")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NewTestcaseNameVisualPanel.class, "ACS_NewTestcaseNameVisualPanel_A11YDesc")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel mNameLbl;
    private javax.swing.JTextField mNameTf;
    // End of variables declaration//GEN-END:variables

    public JTextField getTestcaseNameTf() {
        return mNameTf;
    }
    
    public String getTestcaseName() {
        return mNameTf.getText();
    }
    
    public String getName() {
        return NbBundle.getMessage(NewTestcaseNameVisualPanel.class, 
                                   "LBL_Enter_the_testcase_name"); //NOI18N
    }

        // Implementation of DocumentListener --------------------------------------
    public void changedUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateTexts(e);
    }
    // End if implementation of DocumentListener -------------------------------

    boolean valid(WizardDescriptor wizardDescriptor) {
        if (mNameTf.getText().length() == 0) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(NewTestcaseNameVisualPanel.class, "LBL_Testcase_name_cannot_be_empty")); //NOI18N
            return false; // Display name not specified
        }
        
        // 2. no existing testcase under Test node has name: mComponent.getTestcaseName()
        if (panel.mTestDir.getFileObject(mNameTf.getText()) != null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(NewTestcaseNameVisualPanel.class, "LBL_Name_is_already_used_by_another_testcase")); //NOI18N
            return false;
        }
        
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); //NOI18N
        return true;
    }
    
    /** Handles changes in the project name and project directory
     */
    private void updateTexts(DocumentEvent e) {
       panel.fireChangeEvent(); // Notify that the panel changed
    }
}
