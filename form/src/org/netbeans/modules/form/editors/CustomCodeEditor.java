/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form.editors;

import org.netbeans.modules.form.FormEditor;
import org.openide.nodes.Node;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;

/*
 * CustomCodeEditor.java
 *
 * Created on February 22, 2001, 6:47 PM
 */


/** Customizer for "code properties" used by JavaCodeGenerator.
 *
 * @author  vzboril
 */

public class CustomCodeEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {
    
     static final long serialVersionUID =-7413680598253484271L;

    /** Creates new form CustomCodeEditor */
    public CustomCodeEditor(Node.Property property) {
        this.property = property;
        initComponents();
        
        jLabel1.setText(getString("CustomCodeEditor.label1"));
        jLabel1.setDisplayedMnemonic(getMnemonic("CustomCodeEditor.label1"));
        jLabel1.setLabelFor(codeEditorPane);
        codeEditorPane.requestFocus();
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jScrollPane1 = new javax.swing.JScrollPane();
        codeEditorPane = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        codeEditorPane.setContentType("text/x-java");  // NOI18N
        try {
            codeEditorPane.setText((String) property.getValue());
        }
        catch (java.lang.reflect.InvocationTargetException e1){
            e1.printStackTrace();
        }
        catch (IllegalAccessException e2) {
            e2.printStackTrace();
        }
        codeEditorPane.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                codeEditorPaneFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                codeEditorPaneFocusLost(evt);
            }
        });

        jScrollPane1.setViewportView(codeEditorPane);

        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(5, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(jScrollPane1, gridBagConstraints1);

        jLabel1.setText("jLabel1");
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 0;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(12, 12, 0, 11);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jLabel1, gridBagConstraints1);

    }//GEN-END:initComponents

    private void codeEditorPaneFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_codeEditorPaneFocusLost
        // Add your handling code here:
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
    }//GEN-LAST:event_codeEditorPaneFocusLost

    private void codeEditorPaneFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_codeEditorPaneFocusGained
        // Add your handling code here:
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
    }//GEN-LAST:event_codeEditorPaneFocusGained

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        // Add your handling code here:
        codeEditorPane.requestFocus();
        codeEditorPane.getCaret().setVisible(true); // true is HARDCODED here due to BUG in MAC OS X
    }//GEN-LAST:event_formFocusGained

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        // Add your handling code here:
        codeEditorPane.requestFocus();
        codeEditorPane.getCaret().setVisible(codeEditorPane.hasFocus());
    }//GEN-LAST:event_formComponentShown


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane codeEditorPane;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    private Node.Property property;
   
    
    public Object getPropertyValue() throws IllegalStateException {
        return codeEditorPane.getText();
    }
    
    /** Localization. */
    private static final String mnemonic_suffix = ".mnemonic"; // NOI18N
    
    private static String getString(java.lang.String key) {
       return org.openide.util.NbBundle.getBundle (CustomCodeEditor.class).getString(key);
    }
    
    private char getMnemonic(java.lang.String key) {
       return org.openide.util.NbBundle.getBundle (CustomCodeEditor.class).getString(key + mnemonic_suffix).charAt(0);
    }
}
