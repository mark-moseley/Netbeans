/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.project.ui.customizer;
import javax.swing.JPanel;
import org.netbeans.api.mobility.project.ui.customizer.ProjectProperties;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.spi.mobility.project.ui.customizer.CustomizerPanel;
import org.netbeans.spi.mobility.project.ui.customizer.support.VisualPropertySupport;
import org.netbeans.spi.mobility.project.ui.customizer.VisualPropertyGroup;
import org.openide.util.NbBundle;

/** Customizer for general project attributes.
 *
 * @author  phrebejk, Adam Sotona
 */
public class CustomizerJar extends JPanel implements CustomizerPanel, VisualPropertyGroup {
    
    private static final String[] PROPERTY_GROUP = new String[] { DefaultPropertiesDescriptor.DIST_JAR,
    DefaultPropertiesDescriptor.DIST_JAD,
    DefaultPropertiesDescriptor.JAR_COMPRESS };
    
    private VisualPropertySupport vps;
    
    /** Creates new form CustomizerCompile */
    public CustomizerJar() {
        initComponents();
        initAccessibility();
    }
    
    
    public void initValues(ProjectProperties props, String configuration) {
        vps = VisualPropertySupport.getDefault(props);
        vps.register(defaultCheck, configuration, this);
    }
    
    
    public String[] getGroupPropertyNames() {
        return PROPERTY_GROUP;
    }
    
    public void initGroupValues(final boolean useDefault) {
        vps.register( jarField, DefaultPropertiesDescriptor.DIST_JAR, useDefault );
        vps.register( jadField, DefaultPropertiesDescriptor.DIST_JAD, useDefault );
        vps.register( jCheckBoxCommpress, DefaultPropertiesDescriptor.JAR_COMPRESS, useDefault );
        jadLabel.setEnabled(!useDefault);
        jarLabel.setEnabled(!useDefault);
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
        jadLabel = new javax.swing.JLabel();
        jadField = new javax.swing.JTextField();
        jarLabel = new javax.swing.JLabel();
        jarField = new javax.swing.JTextField();
        jCheckBoxCommpress = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(defaultCheck, NbBundle.getMessage(CustomizerJar.class, "LBL_Use_Default")); // NOI18N
        defaultCheck.setMargin(new java.awt.Insets(0, 0, 0, 2));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(defaultCheck, gridBagConstraints);
        defaultCheck.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJar.class, "ACSD_CustJar_UseDefault")); // NOI18N

        jadLabel.setLabelFor(jadField);
        org.openide.awt.Mnemonics.setLocalizedText(jadLabel, NbBundle.getMessage(CustomizerJar.class, "LBL_CustJar_JADName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 6);
        add(jadLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jadField, gridBagConstraints);
        jadField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJar.class, "ACSD_CustJar_JadName")); // NOI18N

        jarLabel.setLabelFor(jarField);
        org.openide.awt.Mnemonics.setLocalizedText(jarLabel, NbBundle.getMessage(CustomizerJar.class, "LBL_CustJar_JARName")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 6);
        add(jarLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jarField, gridBagConstraints);
        jarField.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJar.class, "ACSD_CustJar_JarName")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBoxCommpress, NbBundle.getMessage(CustomizerJar.class, "LBL_CustJar_Compress")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 0, 0, 0);
        add(jCheckBoxCommpress, gridBagConstraints);
        jCheckBoxCommpress.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJar.class, "ACSD_CustJar_Compress")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void initAccessibility() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(CustomizerJar.class, "ACSN_CustJar"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CustomizerJar.class, "ACSD_CustJar"));
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox defaultCheck;
    private javax.swing.JCheckBox jCheckBoxCommpress;
    private javax.swing.JTextField jadField;
    private javax.swing.JLabel jadLabel;
    private javax.swing.JTextField jarField;
    private javax.swing.JLabel jarLabel;
    // End of variables declaration//GEN-END:variables
    

    // Private methods for classpath data manipulation -------------------------
    
}
