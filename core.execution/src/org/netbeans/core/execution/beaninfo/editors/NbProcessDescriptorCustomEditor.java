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

package org.netbeans.core.execution.beaninfo.editors;

import java.io.File;

import javax.swing.JFileChooser;

import org.openide.execution.NbProcessDescriptor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Custom property editor for NbProcessDescriptor class.
*
* @author  Ian Formanek
*/
public class NbProcessDescriptorCustomEditor extends javax.swing.JPanel implements EnhancedCustomPropertyEditor {
    private NbProcessDescriptorEditor editor;

    private static int DEFAULT_WIDTH = 530;
    private static int DEFAULT_HEIGHT = 400;

    static final long serialVersionUID =-2766277953540349247L;
    /** Creates new NbProcessDescriptorCustomEditor
     * @param editor the NbProcessDescriptorEditor
     */
    public NbProcessDescriptorCustomEditor (NbProcessDescriptorEditor editor) {
        this.editor = editor;
        initComponents ();
        
        if ( editor.pd != null ) {
            processField.setText (editor.pd.getProcessName ());
            argumentsArea.setText (editor.pd.getArguments ());
            hintArea.setText (editor.pd.getInfo ());
        }
        
        processLabel.setDisplayedMnemonic(getString("CTL_NbProcessDescriptorCustomEditor.processLabel.mnemonic").charAt(0));
        argumentsLabel.setDisplayedMnemonic(getString("CTL_NbProcessDescriptorCustomEditor.argumentsLabel.mnemonic").charAt(0));
        argumentKeyLabel.setDisplayedMnemonic(getString("CTL_NbProcessDescriptorCustomEditor.argumentKeyLabel.mnemonic").charAt(0));

        processField.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.processLabel"));
        argumentsArea.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.argumentsLabel"));
        hintArea.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.argumentKeyLabel"));
        jButton1.getAccessibleContext().setAccessibleDescription(getString("ACSD_NbProcessDescriptorCustomEditor.jButton1"));
        
        getAccessibleContext().setAccessibleDescription(getString("ACSD_CustomNbProcessDescriptorEditor"));

        HelpCtx.setHelpIDString (this, NbProcessDescriptorCustomEditor.class.getName ());
    }

    public java.awt.Dimension getPreferredSize() {
        java.awt.Dimension inh = super.getPreferredSize ();
        return new java.awt.Dimension (DEFAULT_WIDTH, Math.max (inh.height, DEFAULT_HEIGHT));
    }

    /** Get the customized property value.
    * @return the property value
    * @exception InvalidStateException when the custom property editor does not contain a valid property value
    *            (and thus it should not be set)
    */
    public Object getPropertyValue () throws IllegalStateException {
        if ( editor.pd == null )
            return new NbProcessDescriptor (processField.getText (), argumentsArea.getText () );
        return new NbProcessDescriptor (processField.getText (), argumentsArea.getText (), editor.pd.getInfo ());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        processLabel = new javax.swing.JLabel();
        processField = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        argumentsLabel = new javax.swing.JLabel();
        argumentsScrollPane = new javax.swing.JScrollPane();
        argumentsArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        argumentKeyLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        hintArea = new javax.swing.JTextArea();
        
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints1;
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));
        processLabel.setText(getString("CTL_NbProcessDescriptorCustomEditor.processLabel.text"));
        processLabel.setLabelFor(processField);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 12);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
        add(processLabel, gridBagConstraints1);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 5);
        gridBagConstraints1.weightx = 3.0;
        add(processField, gridBagConstraints1);
        
        jButton1.setText(getString("CTL_NbProcessDescriptorCustomEditor.jButton1.text"));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
        add(jButton1, gridBagConstraints1);
        
        argumentsLabel.setText(getString("CTL_NbProcessDescriptorCustomEditor.argumentsLabel.text"));
        argumentsLabel.setLabelFor(argumentsArea);
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 11, 12);
        gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(argumentsLabel, gridBagConstraints1);
        
        argumentsScrollPane.setMinimumSize(new java.awt.Dimension(22, 35));
        argumentsScrollPane.setViewportView(argumentsArea);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 1;
        gridBagConstraints1.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.insets = new java.awt.Insets(0, 0, 11, 0);
        gridBagConstraints1.weightx = 1.0;
        gridBagConstraints1.weighty = 1.0;
        add(argumentsScrollPane, gridBagConstraints1);
        
        jPanel1.setLayout(new java.awt.BorderLayout(0, 2));
        
        argumentKeyLabel.setText(getString("CTL_NbProcessDescriptorCustomEditor.argumentKeyLabel.text"));
        argumentKeyLabel.setLabelFor(hintArea);
        jPanel1.add(argumentKeyLabel, java.awt.BorderLayout.NORTH);
        
        hintArea.setLineWrap(true);
        hintArea.setEditable(false);
        hintArea.setBackground((java.awt.Color) javax.swing.UIManager.getDefaults().get("Label.background"));
        jScrollPane1.setViewportView(hintArea);
        
        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        
        gridBagConstraints1 = new java.awt.GridBagConstraints();
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints1.weightx = 7.0;
        gridBagConstraints1.weighty = 7.0;
        add(jPanel1, gridBagConstraints1);
        
    }//GEN-END:initComponents

    private void jButton1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // Add your handling code here:
        JFileChooser chooser = org.netbeans.beaninfo.editors.FileEditor.createHackedFileChooser();
        chooser.setMultiSelectionEnabled (false);
        File init = new File(processField.getText()); // #13372
        if (init.isFile()) {
            chooser.setCurrentDirectory(init.getParentFile());
            chooser.setSelectedFile(init);
        }
        int retVal = chooser.showOpenDialog (this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            String absolute_name = chooser.getSelectedFile ().getAbsolutePath ();
            //System.out.println("file:" + absolute_name); // NOI18N
            processField.setText (absolute_name);
        }
    }//GEN-LAST:event_jButton1ActionPerformed




    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel processLabel;
    private javax.swing.JTextField processField;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel argumentsLabel;
    private javax.swing.JScrollPane argumentsScrollPane;
    private javax.swing.JTextArea argumentsArea;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel argumentKeyLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea hintArea;
    // End of variables declaration//GEN-END:variables

    private static final String getString(String s) {
        return NbBundle.getMessage(NbProcessDescriptorCustomEditor.class, s);
    }

}
