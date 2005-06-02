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

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.ManifestManager;

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
        implVer.setSelected(origDep.hasImplementationDepedendency());
        ManifestManager.PackageExport[] pp = origDep.getModuleEntry().getPublicPackages();
        boolean anyAvailablePkg = pp != null && pp.length != 0;
        includeInCP.setEnabled(anyAvailablePkg);
        includeInCP.setSelected(origDep.hasCompileDependency());
        availablePkg.setEnabled(anyAvailablePkg);
        DefaultListModel model = new DefaultListModel();
        if (anyAvailablePkg) {
            // XXX only temporary(?)
            for (int i = 0; i < pp.length; i++) {
                model.addElement(pp[i].getPackage());
            }
        } else {
            model.addElement("<empty>"); // NOI18N
        }
        availablePkg.setModel(model);
        versionChanged(null);
    }
    
    ModuleDependency getEditedDependency() {
        ModuleDependency dep = new ModuleDependency(origDep.getModuleEntry(),
                releaseVersionValue.getText().trim(),
                specVerValue.getText().trim(),
                includeInCP.isSelected(),
                implVer.isSelected());
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
        jarLocation = new javax.swing.JLabel();
        releaseVersion = new javax.swing.JLabel();
        releaseVersionValue = new javax.swing.JTextField();
        specVer = new javax.swing.JRadioButton();
        specVerValue = new javax.swing.JTextField();
        implVer = new javax.swing.JRadioButton();
        includeInCP = new javax.swing.JCheckBox();
        availablePkgSP = new javax.swing.JScrollPane();
        availablePkg = new javax.swing.JList();
        codeNameBaseValue = new javax.swing.JTextField();
        jarLocationValue = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(6, 6, 6, 6)));
        setPreferredSize(new java.awt.Dimension(400, 300));
        codeNameBase.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_CNB_Mnem").charAt(0));
        codeNameBase.setLabelFor(codeNameBaseValue);
        codeNameBase.setText(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_CNB"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(codeNameBase, gridBagConstraints);

        jarLocation.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_JAR_Mnem").charAt(0));
        jarLocation.setLabelFor(jarLocationValue);
        jarLocation.setText(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_JAR"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 12);
        add(jarLocation, gridBagConstraints);

        releaseVersion.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_MajorReleaseVersion_Mnem").charAt(0));
        releaseVersion.setText(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_MajorReleaseVersion"));
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
        specVer.setMnemonic(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_SpecificationVersion_Mnem").charAt(0));
        specVer.setSelected(true);
        specVer.setText(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_SpecificationVersion"));
        specVer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionChanged(evt);
            }
        });

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
        implVer.setMnemonic(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_ImplementationVersion_Mnem").charAt(0));
        implVer.setText(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_ImplementationVersion"));
        implVer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                versionChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(implVer, gridBagConstraints);

        includeInCP.setMnemonic(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_IncludeAPIPackages_Mnem").charAt(0));
        includeInCP.setText(org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_IncludeAPIPackages"));
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

        codeNameBaseValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(codeNameBaseValue, gridBagConstraints);

        jarLocationValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(jarLocationValue, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void versionChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionChanged
        specVerValue.setEnabled(specVer.isSelected());
    }//GEN-LAST:event_versionChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList availablePkg;
    private javax.swing.JScrollPane availablePkgSP;
    private javax.swing.JLabel codeNameBase;
    private javax.swing.JTextField codeNameBaseValue;
    private javax.swing.JRadioButton implVer;
    private javax.swing.JCheckBox includeInCP;
    private javax.swing.JLabel jarLocation;
    private javax.swing.JTextField jarLocationValue;
    private javax.swing.JLabel releaseVersion;
    private javax.swing.JTextField releaseVersionValue;
    private javax.swing.JRadioButton specVer;
    private javax.swing.JTextField specVerValue;
    private javax.swing.ButtonGroup versionGroup;
    // End of variables declaration//GEN-END:variables
    
}
