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

/**
 * Represents <em>Compiling</em> panel in Netbeans Module customizer.
 *
 * @author Martin Krauskopf
 */
final class CustomizerCompiling extends NbPropertyPanel.Single {
    
    /** Creates new form CustomizerCompiling */
    CustomizerCompiling(final SingleModuleProperties props) {
        super(props, CustomizerCompiling.class);
        initComponents();
        refresh();
    }
    
    void refresh() {
        debug.setSelected(getBooleanProperty(SingleModuleProperties.BUILD_COMPILER_DEBUG));
        deprecation.setSelected(getBooleanProperty(SingleModuleProperties.BUILD_COMPILER_DEPRECATION));
    }
    
    public void store() {
        setBooleanProperty(SingleModuleProperties.BUILD_COMPILER_DEBUG, debug.isSelected());
        setBooleanProperty(SingleModuleProperties.BUILD_COMPILER_DEPRECATION, deprecation.isSelected());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        innerPane = new javax.swing.JPanel();
        debug = new javax.swing.JCheckBox();
        deprecation = new javax.swing.JCheckBox();

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        innerPane.setLayout(new java.awt.GridLayout(2, 1));

        org.openide.awt.Mnemonics.setLocalizedText(debug, org.openide.util.NbBundle.getMessage(CustomizerCompiling.class, "CTL_GenerateDebugInfo"));
        innerPane.add(debug);

        org.openide.awt.Mnemonics.setLocalizedText(deprecation, org.openide.util.NbBundle.getMessage(CustomizerCompiling.class, "CTL_ReportDeprecation"));
        innerPane.add(deprecation);

        add(innerPane);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox debug;
    private javax.swing.JCheckBox deprecation;
    private javax.swing.JPanel innerPane;
    // End of variables declaration//GEN-END:variables
    
}
