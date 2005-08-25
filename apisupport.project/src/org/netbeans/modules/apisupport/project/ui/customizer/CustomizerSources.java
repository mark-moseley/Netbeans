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

import org.netbeans.modules.apisupport.project.ui.UIUtil;

/**
 * Represents <em>Sources</em> panel in Netbeans Module customizer.
 *
 * @author mkrauskopf
 */
final class CustomizerSources extends NbPropertyPanel.Single {
    
    /** Creates new form CustomizerSources */
    CustomizerSources(final SingleModuleProperties props) {
        super(props);
        initComponents();
        refresh();
    }
    
    void refresh() {
        if (getProperties().getSuiteDirectory() == null) {
            moduleSuite.setVisible(false);
            moduleSuiteValue.setVisible(false);
        } else {
            UIUtil.setText(moduleSuiteValue, getProperties().getSuiteDirectory());
        }
        srcLevelValue.removeAllItems();
        for (int i = 0; i < SingleModuleProperties.SOURCE_LEVELS.length; i++) {
            srcLevelValue.addItem(SingleModuleProperties.SOURCE_LEVELS[i]);
        }
        srcLevelValue.setSelectedItem(getProperty(SingleModuleProperties.JAVAC_SOURCES));
        UIUtil.setText(prjFolderValue, getProperties().getProjectDirectory());
    }
    
    public void store() {
        setProperty(SingleModuleProperties.JAVAC_SOURCES,
                (String) srcLevelValue.getSelectedItem());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        prjFolder = new javax.swing.JLabel();
        srcLevel = new javax.swing.JLabel();
        srcLevelValue = new javax.swing.JComboBox();
        filler = new javax.swing.JLabel();
        prjFolderValue = new javax.swing.JTextField();
        moduleSuite = new javax.swing.JLabel();
        moduleSuiteValue = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        prjFolder.setLabelFor(prjFolderValue);
        org.openide.awt.Mnemonics.setLocalizedText(prjFolder, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_ProjectFolder"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(prjFolder, gridBagConstraints);

        srcLevel.setLabelFor(srcLevelValue);
        org.openide.awt.Mnemonics.setLocalizedText(srcLevel, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_SourceLevel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 12);
        add(srcLevel, gridBagConstraints);

        srcLevelValue.setPrototypeDisplayValue("mmm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(18, 0, 0, 0);
        add(srcLevelValue, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weighty = 1.0;
        add(filler, gridBagConstraints);

        prjFolderValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(prjFolderValue, gridBagConstraints);

        moduleSuite.setLabelFor(moduleSuiteValue);
        org.openide.awt.Mnemonics.setLocalizedText(moduleSuite, org.openide.util.NbBundle.getMessage(CustomizerSources.class, "LBL_ModeleSuite"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 12);
        add(moduleSuite, gridBagConstraints);

        moduleSuiteValue.setEditable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(moduleSuiteValue, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filler;
    private javax.swing.JLabel moduleSuite;
    private javax.swing.JTextField moduleSuiteValue;
    private javax.swing.JLabel prjFolder;
    private javax.swing.JTextField prjFolderValue;
    private javax.swing.JLabel srcLevel;
    private javax.swing.JComboBox srcLevelValue;
    // End of variables declaration//GEN-END:variables
    
}
