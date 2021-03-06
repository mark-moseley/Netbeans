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
 * Customizer.java
 *
 * Created on 23.Mar 2004, 11:31
 */
package org.netbeans.modules.mobility.project.ui.customizer;

import java.util.Hashtable;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.openide.util.NbBundle;

/**
 *
 * @author  Adam Sotona
 */
public class CustomizerObfuscate extends JPanel implements CustomizerPanel, VisualPropertyGroup, ChangeListener {
    
    private static final String[] PROPERTY_GROUP = new String[] {DefaultPropertiesDescriptor.OBFUSCATION_LEVEL, DefaultPropertiesDescriptor.OBFUSCATION_CUSTOM};
    
    private VisualPropertySupport vps;
    private boolean useDefault;
    
    /** Creates new form CustomizerConfigs */
    public CustomizerObfuscate() {
        initComponents();
        initAccessibility();
        Hashtable<Integer,JLabel> values = new Hashtable<Integer,JLabel>(10, 1);
        final JLabel l1 = new JLabel(NbBundle.getMessage(CustomizerObfuscate.class, "LBL_CustomizerObfuscate_Level_Off"));
        final JLabel l2 = new JLabel(NbBundle.getMessage(CustomizerObfuscate.class, "LBL_CustomizerObfuscate_Level_Maximum"));
        values.put(Integer.valueOf("0"), l1);
        values.put(Integer.valueOf("9"), l2);
        for (int i = 1; i < 9; i++){
            values.put(new Integer(i), new JLabel(String.valueOf(i)));
        }
        levelSlider.setLabelTable(values);
        levelSlider.addChangeListener(this);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        defaultCheck = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        levelSlider = new javax.swing.JSlider();
        browserLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionArea = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        customArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(defaultCheck, NbBundle.getMessage(CustomizerObfuscate.class, "LBL_Use_Default")); // NOI18N
        defaultCheck.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(defaultCheck, gridBagConstraints);
        defaultCheck.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerObfuscate.class, "ACSD_CustObfusc_UseDefault")); // NOI18N

        jLabel2.setLabelFor(levelSlider);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, NbBundle.getMessage(CustomizerObfuscate.class, "LBL_CustomizerObfuscate_Level")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        levelSlider.setMajorTickSpacing(1);
        levelSlider.setMaximum(9);
        levelSlider.setPaintLabels(true);
        levelSlider.setSnapToTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 11);
        add(levelSlider, gridBagConstraints);
        levelSlider.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerObfuscate.class, "ACSD_CustObfusc_Level")); // NOI18N

        browserLabel.setLabelFor(descriptionArea);
        org.openide.awt.Mnemonics.setLocalizedText(browserLabel, NbBundle.getMessage(CustomizerObfuscate.class, "LBL_CustomizerObfuscate_LevelDescription")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        add(browserLabel, gridBagConstraints);

        descriptionArea.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground"));
        descriptionArea.setEditable(false);
        jScrollPane2.setViewportView(descriptionArea);
        descriptionArea.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerObfuscate.class, "ACSD_CustObfusc_Description")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        add(jScrollPane2, gridBagConstraints);

        jLabel1.setLabelFor(customArea);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, NbBundle.getMessage(CustomizerObfuscate.class, "LBL_CustomizerObfuscate_CustomScript")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 11);
        add(jLabel1, gridBagConstraints);

        jScrollPane1.setViewportView(customArea);
        customArea.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerObfuscate.class, "ACSD_CustObfusc_Additional")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 11);
        add(jScrollPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerObfuscate.class, "ACSN_CustomizerObfuscate"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerObfuscate.class, "ACSD_CustomizerObfuscate"));
    }
    
    public void initValues(ProjectProperties props, String configuration) {
        this.vps = VisualPropertySupport.getDefault(props);
        vps.register(defaultCheck, configuration, this);
    }
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    public void initGroupValues(final boolean useDefault) {
        this.useDefault = useDefault;
        vps.register(levelSlider, DefaultPropertiesDescriptor.OBFUSCATION_LEVEL, useDefault);
        vps.register(customArea, DefaultPropertiesDescriptor.OBFUSCATION_CUSTOM, useDefault);
        jLabel1.setEnabled(!useDefault);
        jLabel2.setEnabled(!useDefault);
        browserLabel.setEnabled(!useDefault);
        updateDescription();
    }
    
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e) {
        updateDescription();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel browserLabel;
    private javax.swing.JTextArea customArea;
    private javax.swing.JCheckBox defaultCheck;
    private javax.swing.JTextArea descriptionArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSlider levelSlider;
    // End of variables declaration//GEN-END:variables
    
    /** Updates description to reflect the one associated with given object.
     */
    private void updateDescription() {
        customArea.setEnabled(!useDefault && levelSlider.getValue()>0);
        customArea.setBackground(UIManager.getDefaults().getColor(useDefault || levelSlider.getValue()==0 ?  "TextField.inactiveBackground" : "TextField.background")); //NOI18N
        descriptionArea.setText(NbBundle.getMessage(CustomizerObfuscate.class, "DESC_ObfuscationLeve_"+levelSlider.getValue())); //NOI18N
    }
    
}
