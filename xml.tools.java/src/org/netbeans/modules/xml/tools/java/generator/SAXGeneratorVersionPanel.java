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
package org.netbeans.modules.xml.tools.java.generator;

import org.netbeans.modules.xml.tools.java.generator.SAXGeneratorAbstractPanel;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Kuzel
 * @version
 */
public final class SAXGeneratorVersionPanel extends SAXGeneratorAbstractPanel implements ActionListener {

    /** Serial Version UID */
    private static final long serialVersionUID =-3731567998368428526L;


    /** Creates new form SAXGeneratorVersionPanel */
    public SAXGeneratorVersionPanel() {
//        try {
//            this.putClientProperty(WizardDescriptor.PROP_HELP_URL, new URL("nbresloc:/org/netbeans/modules/xml/tools/generator/SAXGeneratorVersionPanel.html"));  //NOI18N        
//        } catch (MalformedURLException ex) {
//        }            
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        descTextArea = new javax.swing.JTextArea();
        jaxpLabel = new javax.swing.JLabel();
        jaxpVersionComboBox = new javax.swing.JComboBox();
        versionLabel = new javax.swing.JLabel();
        versionComboBox = new javax.swing.JComboBox();
        propagateSAXCheckBox = new javax.swing.JCheckBox();

        setName(NbBundle.getMessage(SAXGeneratorVersionPanel.class, "SAXGeneratorVersionPanel.Form.name")); // NOI18N
        setPreferredSize(new java.awt.Dimension(480, 350));
        setLayout(new java.awt.GridBagLayout());

        descTextArea.setEditable(false);
        descTextArea.setFont(javax.swing.UIManager.getFont ("Label.font"));
        descTextArea.setForeground(new java.awt.Color(102, 102, 153));
        descTextArea.setLineWrap(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/xml/tools/java/generator/Bundle"); // NOI18N
        descTextArea.setText(bundle.getString("DESC_saxw_versions")); // NOI18N
        descTextArea.setWrapStyleWord(true);
        descTextArea.setDisabledTextColor(javax.swing.UIManager.getColor ("Label.foreground"));
        descTextArea.setEnabled(false);
        descTextArea.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(descTextArea, gridBagConstraints);

        jaxpLabel.setLabelFor(jaxpVersionComboBox);
        jaxpLabel.setText(NbBundle.getMessage(SAXGeneratorVersionPanel.class,"SAXGeneratorVersionPanel.jaxpLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jaxpLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(jaxpVersionComboBox, gridBagConstraints);

        versionLabel.setLabelFor(versionComboBox);
        versionLabel.setText(NbBundle.getMessage(SAXGeneratorVersionPanel.class,"SAXGeneratorCustomizer.versionLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(versionLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(versionComboBox, gridBagConstraints);

        propagateSAXCheckBox.setText(NbBundle.getMessage(SAXGeneratorVersionPanel.class,"SAXGeneratorVersionPanel.propagateSAXCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(propagateSAXCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    protected void updateModel() {
        model.setSAXversion(versionComboBox.getSelectedIndex() + 1);
        model.setJAXPversion(jaxpVersionComboBox.getSelectedIndex() + 1);
        model.setPropagateSAX(propagateSAXCheckBox.isSelected());
    }
    
    protected void initView() {
        initComponents();
	        
        //**** set mnemonics
        jaxpLabel.setDisplayedMnemonic(
                NbBundle.getMessage(SAXGeneratorVersionPanel.class,
                "SAXGeneratorVersionPanel.jaxpLabel.mne").charAt(0)); // NOI18N
        versionLabel.setDisplayedMnemonic(NbBundle.getMessage(
                SAXGeneratorVersionPanel.class,
                "SAXGeneratorCustomizer.versionLabel.mne").charAt(0)); // NOI18N
        propagateSAXCheckBox.setMnemonic(NbBundle.getMessage(
                SAXGeneratorVersionPanel.class,
                "SAXGeneratorVersionPanel.propagateSAXCheckBox.mne").charAt(0)); // NOI18N
        //****
        
        String items[] = new String[] {"SAX 1.0", "SAX 2.0"};  // NOI18N
        ComboBoxModel cbModel = new DefaultComboBoxModel(items);
        versionComboBox.setModel(cbModel);
        cbModel.setSelectedItem(items[model.getSAXversion() - 1]);
        
        items = new String[] {"JAXP 1.0", "JAXP 1.1"}; // NOI18N
        cbModel = new DefaultComboBoxModel(items);
        jaxpVersionComboBox.setModel(cbModel);
        cbModel.setSelectedItem(items[model.getJAXPversion() - 1]);
        
        initAccessibility();
    }
    
    protected void updateView() {
    }
    
    public void actionPerformed(java.awt.event.ActionEvent p1) {
        updateModel();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descTextArea;
    private javax.swing.JLabel jaxpLabel;
    private javax.swing.JComboBox jaxpVersionComboBox;
    private javax.swing.JCheckBox propagateSAXCheckBox;
    private javax.swing.JComboBox versionComboBox;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    /** Initialize accesibility
     */
    public void initAccessibility(){

        propagateSAXCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SAXGeneratorVersionPanel.class, "ACSD_propagateSAXCheckBox"));
        propagateSAXCheckBox.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SAXGeneratorVersionPanel.class, "ACSN_propagateSAXCheckBox"));
        
        jaxpVersionComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SAXGeneratorVersionPanel.class, "ACSD_jaxpVersionComboBox"));
        propagateSAXCheckBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SAXGeneratorVersionPanel.class, "ACSD_propagateSAXCheckBox"));
        
        versionComboBox.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SAXGeneratorVersionPanel.class, "ACSD_versionComboBox"));
        
        this.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SAXGeneratorVersionPanel.class, "ACSD_SAXGeneratorVersionPanel"));
    }    
}
