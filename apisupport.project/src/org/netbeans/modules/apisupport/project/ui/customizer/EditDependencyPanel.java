/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.File;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.ModuleList;

/**
 * Represents panel for editing dependency details. Shown after <em>Edit</em>
 * button on the <code>CustomizerLibraries</code> panel has been pushed.
 *
 * @author  mkrauskopf
 */
final class EditDependencyPanel extends JPanel {
    
    private final ModuleDependency origDep;
    
    /** Creates new form EditDependencyPanel */
    EditDependencyPanel(final ModuleDependency dep) {
        this.origDep = dep;
        initComponents();
        readFromEntry();
    }
    
    private void readFromEntry() {
        codeNameBaseValue.setText(origDep.getModuleEntry().getCodeNameBase());
        jarLocationValue.setText(origDep.getModuleEntry().getJarLocation().getAbsolutePath());
        releaseVersionValue.setText(origDep.getReleaseVersion());
        specVerValue.setText(origDep.getSpecificationVersion());
    }
    
    ModuleDependency getEditedDependency() {
        ModuleDependency dep = new ModuleDependency(origDep.getModuleEntry(),
                releaseVersionValue.getText().trim(),
                specVerValue.getText().trim());
        return dep;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        versionGroup = new javax.swing.ButtonGroup();
        codeNameBase = new javax.swing.JLabel();
        codeNameBaseValue = new javax.swing.JLabel();
        jarLocation = new javax.swing.JLabel();
        jarLocationValue = new javax.swing.JLabel();
        releaseVersion = new javax.swing.JLabel();
        releaseVersionValue = new javax.swing.JTextField();
        specVer = new javax.swing.JRadioButton();
        specVerValue = new javax.swing.JTextField();
        implVer = new javax.swing.JRadioButton();
        includeInCP = new javax.swing.JCheckBox();
        availablePkgSP = new javax.swing.JScrollPane();
        availablePkg = new javax.swing.JList();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 6, 6, 6)));
        setPreferredSize(new java.awt.Dimension(400, 300));
        codeNameBase.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_CNB_Mnem").charAt(0));
        codeNameBase.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_CNB"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(codeNameBase, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(codeNameBaseValue, gridBagConstraints);

        jarLocation.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_JAR_Mnem").charAt(0));
        jarLocation.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_JAR"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jarLocation, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jarLocationValue, gridBagConstraints);

        releaseVersion.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_MajorReleaseVersion_Mnem").charAt(0));
        releaseVersion.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_MajorReleaseVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 12);
        add(releaseVersion, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(releaseVersionValue, gridBagConstraints);

        versionGroup.add(specVer);
        specVer.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_SpecificationVersion_Mnem").charAt(0));
        specVer.setSelected(true);
        specVer.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_SpecificationVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(specVer, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(specVerValue, gridBagConstraints);

        versionGroup.add(implVer);
        implVer.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_ImplementationVersion_Mnem").charAt(0));
        implVer.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_ImplementationVersion"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(implVer, gridBagConstraints);

        includeInCP.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_IncludeAPIPackages_Mnem").charAt(0));
        includeInCP.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle").getString("LBL_IncludeAPIPackages"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(includeInCP, gridBagConstraints);

        availablePkgSP.setViewportView(availablePkg);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weighty = 1.0;
        add(availablePkgSP, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList availablePkg;
    private javax.swing.JScrollPane availablePkgSP;
    private javax.swing.JLabel codeNameBase;
    private javax.swing.JLabel codeNameBaseValue;
    private javax.swing.JRadioButton implVer;
    private javax.swing.JCheckBox includeInCP;
    private javax.swing.JLabel jarLocation;
    private javax.swing.JLabel jarLocationValue;
    private javax.swing.JLabel releaseVersion;
    private javax.swing.JTextField releaseVersionValue;
    private javax.swing.JRadioButton specVer;
    private javax.swing.JTextField specVerValue;
    private javax.swing.ButtonGroup versionGroup;
    // End of variables declaration//GEN-END:variables
    
}
