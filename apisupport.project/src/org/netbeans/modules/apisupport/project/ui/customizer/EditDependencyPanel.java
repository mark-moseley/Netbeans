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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.net.URL;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.ManifestManager;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;

/**
 * Represents panel for editing dependency details. Shown e.g. after <em>Edit</em>
 * button on the <code>CustomizerLibraries</code> panel has been pushed.
 *
 * @author Martin Krauskopf
 */
public final class EditDependencyPanel extends JPanel {
    
    private final ModuleDependency origDep;
    private final URL javadoc;
    
    private final ManifestManager.PackageExport[] pp;
    
    /** Creates new form EditDependencyPanel */
    public EditDependencyPanel(final ModuleDependency dep, final NbPlatform platform) {
        this.origDep = dep;
        this.pp = origDep.getModuleEntry().getPublicPackages();
        initComponents();
        initDependency();
        if (platform == null) { // NetBeans.org module
            javadoc = Util.findJavadocForNetBeansOrgModules(origDep);
        } else {
            javadoc = Util.findJavadoc(origDep, platform);
        }
        showJavadocButton.setEnabled(javadoc != null);
    }
    
    private void refresh() {
        specVerValue.setEnabled(specVer.isSelected());
        includeInCP.setEnabled(implVer.isSelected() || hasAvailablePackages());
        if (!includeInCP.isEnabled()) {
            includeInCP.setSelected(false);
        } // else leave the user's selection
    }
    
    private boolean hasAvailablePackages() {
        return pp.length > 0;
    }
    
    /** Called first time dialog is opened. */
    private void initDependency() {
        UIUtil.setText(codeNameBaseValue, origDep.getModuleEntry().getCodeNameBase());
        UIUtil.setText(jarLocationValue, origDep.getModuleEntry().getJarLocation().getAbsolutePath());
        UIUtil.setText(releaseVersionValue, origDep.getReleaseVersion());
        UIUtil.setText(specVerValue, origDep.hasImplementationDepedendency() ?
            origDep.getModuleEntry().getSpecificationVersion() :
            origDep.getSpecificationVersion());
        implVer.setSelected(origDep.hasImplementationDepedendency());
        availablePkg.setEnabled(hasAvailablePackages());
        DefaultListModel model = new DefaultListModel();
        if (hasAvailablePackages()) {
            // XXX should show all subpackages in the case of recursion is set
            // to true instead of e.g. org/**
            SortedSet/*<String>*/ packages = new TreeSet();
            for (int i = 0; i < pp.length; i++) {
                packages.add(pp[i].getPackage() + (pp[i].isRecursive() ? ".**" : "")); // NOI18N
            }
            Iterator it = packages.iterator();
            while (it.hasNext()) {
                model.addElement((String) it.next());
            }
        } else {
            model.addElement(NbBundle.getMessage(EditDependencyPanel.class, "EditDependencyPanel_empty"));
        }
        availablePkg.setModel(model);
        includeInCP.setSelected(origDep.hasCompileDependency());
        refresh();
    }
    
    public ModuleDependency getEditedDependency() {
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
        showJavadocButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setPreferredSize(new java.awt.Dimension(400, 300));
        codeNameBase.setLabelFor(codeNameBaseValue);
        org.openide.awt.Mnemonics.setLocalizedText(codeNameBase, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_CNB"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(codeNameBase, gridBagConstraints);

        jarLocation.setLabelFor(jarLocationValue);
        org.openide.awt.Mnemonics.setLocalizedText(jarLocation, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_JAR"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 12);
        add(jarLocation, gridBagConstraints);

        releaseVersion.setLabelFor(releaseVersionValue);
        org.openide.awt.Mnemonics.setLocalizedText(releaseVersion, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_MajorReleaseVersion"));
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
        specVer.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(specVer, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_SpecificationVersion"));
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
        org.openide.awt.Mnemonics.setLocalizedText(implVer, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_ImplementationVersion"));
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

        org.openide.awt.Mnemonics.setLocalizedText(includeInCP, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "LBL_IncludeAPIPackages"));
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

        org.openide.awt.Mnemonics.setLocalizedText(showJavadocButton, org.openide.util.NbBundle.getMessage(EditDependencyPanel.class, "CTL_ShowJavadoc"));
        showJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showJavadoc(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(24, 0, 0, 0);
        add(showJavadocButton, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void showJavadoc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavadoc
        HtmlBrowser.URLDisplayer.getDefault().showURL(javadoc);
    }//GEN-LAST:event_showJavadoc
    
    private void versionChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_versionChanged
        refresh();
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
    private javax.swing.JButton showJavadocButton;
    private javax.swing.JRadioButton specVer;
    private javax.swing.JTextField specVerValue;
    private javax.swing.ButtonGroup versionGroup;
    // End of variables declaration//GEN-END:variables
    
}
