/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Message;
import org.openide.util.NbBundle;

import javax.swing.*;
import org.netbeans.modules.debugger.Controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;

import org.netbeans.modules.web.debug.util.Utils;

/**
* Customizer of JspEvent
*
* @author Martin Grebac
*/
class JspBreakpointPanel extends JPanel implements Controller, Runnable {

    private JspEvent event;
    private boolean valid = false;
    
    static final long serialVersionUID =-8164649328980808272L;

    /** Creates new form JspBreakpointPanel */
    public JspBreakpointPanel(JspEvent e) {
   
        event = e;
        initComponents ();
        putClientProperty("HelpID", "jsp_breakpoint");//NOI18N

        Listener l = new Listener(this);
        cboxJspSourcePath.getEditor().getEditorComponent().addKeyListener(l);
        cboxJspSourcePath.addActionListener(l);
        
        // a11y
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_LineBreakpointPanel")); // NOI18N
        cboxJspSourcePath.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_CTL_Source_name")); // NOI18N
        tfLineNumber.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(JspBreakpointPanel.class).getString("ACSD_CTL_Line_number")); // NOI18N

        Object[] objs = Utils.getJsps();
        if (objs != null) {
            if (objs.length != 0) {
                cboxJspSourcePath.setModel(
                    new DefaultComboBoxModel(objs)
                );
            }
        }
        
        String jspSourcePath = event.getJspName();
        cboxJspSourcePath.setSelectedItem(jspSourcePath == null ? "" : jspSourcePath.trim());        
        fillLineNumber();
        run();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        lblLineNumber = new javax.swing.JLabel();
        tfLineNumber = new javax.swing.JTextField();
        lblJspSourcePath = new javax.swing.JLabel();
        cboxJspSourcePath = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        lblLineNumber.setLabelFor(tfLineNumber);
        lblLineNumber.setText(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Line_number"));
        lblLineNumber.setDisplayedMnemonic(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Line_number_mnemonic").charAt(0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        add(lblLineNumber, gridBagConstraints);

        tfLineNumber.setColumns(7);
        tfLineNumber.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                tfLineNumberFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                tfLineNumberFocusLost(evt);
            }
        });
        tfLineNumber.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                tfLineNumberKeyTyped(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        add(tfLineNumber, gridBagConstraints);

        lblJspSourcePath.setText(NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Source_name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 2, 2);
        add(lblJspSourcePath, gridBagConstraints);

        cboxJspSourcePath.setEditable(true);
        cboxJspSourcePath.getEditor().getEditorComponent().addFocusListener(new java.awt.event.FocusListener() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                cboxJspSourcePathFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                cboxJspSourcePathFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        add(cboxJspSourcePath, gridBagConstraints);

    }//GEN-END:initComponents

    private void tfLineNumberKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfLineNumberKeyTyped
        // Add your handling code here:
        run();
    }//GEN-LAST:event_tfLineNumberKeyTyped

    private void tfLineNumberFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfLineNumberFocusGained
        if (!evt.isTemporary()) {
            ((JTextField) evt.getComponent()).selectAll();
        }
    }//GEN-LAST:event_tfLineNumberFocusGained

    private void tfLineNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfLineNumberFocusLost
        if (!evt.isTemporary()) {
            if (tfLineNumber.getText().trim().length() > 0) {
                try {
                    int i = Integer.parseInt(tfLineNumber.getText ());
                    if (i < 1) {
                        DialogDisplayer.getDefault().notify (
                            new Message (
                                NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Bad_line_number"),  //NOI18N
                                NotifyDescriptor.ERROR_MESSAGE
                            )
                        );
                    } else if (event != null) {
                            event.setLineNumber(i);
                    }                    
                } catch (NumberFormatException e) {
                    DialogDisplayer.getDefault().notify (
                        new Message (
                            NbBundle.getBundle(JspBreakpointPanel.class).getString("CTL_Bad_line_number"),  //NOI18N
                            NotifyDescriptor.ERROR_MESSAGE
                        )
                    );
                }
            }
        }
    }//GEN-LAST:event_tfLineNumberFocusLost

    private void cboxJspSourcePathFocusGained(java.awt.event.FocusEvent evt) {
        if (!evt.isTemporary()) {
            cboxJspSourcePath.getEditor().selectAll();
        }
    }

    private void cboxJspSourcePathFocusLost(java.awt.event.FocusEvent evt) {
        if (!evt.isTemporary()) {
            ComboBoxEditor editor = cboxJspSourcePath.getEditor();
            String value = ((String)editor.getItem()).trim();
            event.setJspName(value);
        }
    }

    private void fillLineNumber () {
    /*    if (!isAcceptableDataObject()) {
            return;
        }*/
        int lnum = event.getLineNumber();
        if (lnum < 1)  {
            tfLineNumber.setText ("");  //NOI18N
        } else {
            tfLineNumber.setText ("" + lnum); // NOI18N
        }
    }
    
    /******************************/
    /* CONTROLLER:                */
    /******************************/
    
    //interface org.netbeans.modules.debugger.Controller
    public boolean ok() {
        return true; //TEMP
    }
    
    //interface org.netbeans.modules.debugger.Controller
    public boolean cancel() {
        return true;
    }
    
    //interface org.netbeans.modules.debugger.Controller
    public boolean isValid() {
        return valid;
    }
    
    /** thread that evaluates entered parameters and enables 'OK' button based on evaluation
     */
    public void run () {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                
                boolean nv = false;

                // check values
                String ln = tfLineNumber.getText().trim();
                String jsp = (cboxJspSourcePath.getSelectedItem() == null) ? "" : cboxJspSourcePath.getSelectedItem().toString().trim();
                if (ln.length() > 0) {
                    try {
                        int i = Integer.parseInt (ln);
                        if (i > 0) {
                            nv = true;
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                if (jsp.length() < 1) {
                    nv = false;
                }                
                if (valid == nv) {
                    return;
                }
                valid = nv;
                firePropertyChange(PROP_VALID, Boolean.valueOf(!valid), Boolean.valueOf(valid));
            }
        });
    }    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboxJspSourcePath;
    private javax.swing.JLabel lblJspSourcePath;
    private javax.swing.JLabel lblLineNumber;
    private javax.swing.JTextField tfLineNumber;
    // End of variables declaration//GEN-END:variables

    static class Listener extends KeyAdapter implements ActionListener {
        Runnable r;
        Listener (Runnable v) {
            r = v;
        }
        public void keyTyped (java.awt.event.KeyEvent evt) {
            r.run ();
        }
        public void actionPerformed (ActionEvent evt) {
            r.run ();
        }
    }
}
