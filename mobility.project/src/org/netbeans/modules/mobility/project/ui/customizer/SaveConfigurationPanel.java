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
 * NewConfigurationPanel.java
 *
 * Created on February 11, 2004, 2:44 PM
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  gc149856
 */
public class SaveConfigurationPanel extends JPanel implements DocumentListener {
    
    final private JButton saveBtn;
    final private Collection<String> templateNames;
    
    /** Creates new form NewConfigurationPanel */
    public SaveConfigurationPanel(String cfgName, Collection<String> templateNames, JButton saveBtn) {
        this.templateNames = templateNames;
        initComponents();
        initAccessibility();
        jTextFieldName.setText(cfgName);
        jTextFieldName.setSelectionStart(0);
        jTextFieldName.setSelectionEnd(cfgName.length());
        this.saveBtn = saveBtn;
        jTextFieldName.getDocument().addDocumentListener(this);
        changedUpdate(null);
    }
    
    public String getName() {
        return jTextFieldName.getText();
    }
    
    final public boolean isValid() {
        final String name = jTextFieldName.getText();
        if (J2MEProjectUtils.ILEGAL_CONFIGURATION_NAMES.contains(name)) {
            errorPanel.setErrorBundleMessage("ERR_SaveCfg_ReservedWord"); //NOI18N
            return false;
        }
        if (!Utilities.isJavaIdentifier(name)) {
            errorPanel.setErrorBundleMessage("ERR_SaveCfg_MustBeJavaIdentifier"); //NOI18N
            return false;
        }
        if (templateNames.contains(name)) {
            errorPanel.setErrorBundleMessage("ERR_SaveCfg_NameExists"); //NOI18N
            return true;
        }
        errorPanel.setErrorBundleMessage(null);
        return true;
    }
    
    final public void changedUpdate(@SuppressWarnings("unused")
	final DocumentEvent e) {
        if (saveBtn != null) {
            saveBtn.setEnabled(isValid());
        }
    }
    
    public void insertUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    public void removeUpdate(final DocumentEvent e) {
        changedUpdate(e);
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        jTextFieldName = new javax.swing.JTextField();
        errorPanel = new org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel();

        setPreferredSize(new java.awt.Dimension(400, 100));
        setLayout(new java.awt.GridBagLayout());

        jLabel1.setLabelFor(jTextFieldName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(SaveConfigurationPanel.class, "LBL_SaveConfigPanel_TemplateName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 5, 0, 12);
        add(jTextFieldName, gridBagConstraints);
        jTextFieldName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SaveConfigurationPanel.class, "ADSC_SaveConfigPanel_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(errorPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(NewConfigurationPanel.class, "ACSN_SaveConfigPanel"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(NewConfigurationPanel.class, "ACSD_SaveConfigPanel"));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mobility.project.ui.customizer.ErrorPanel errorPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextFieldName;
    // End of variables declaration//GEN-END:variables
    
}
